package com.skt.sf.rtti.logic;

import com.skt.sf.rtti.domain.TimeTickSpeedDomain;
import com.skt.sf.rtti.util.CalculationUtil;
import com.skt.sf.rtti.util.ConvertUtil;
import com.skt.sf.rtti.util.DateUtil;
import com.skt.sf.rtti.util.PhoenixUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TimeTickSpeedLogic implements Serializable {
    @Autowired
    private PhoenixUtil phoenixUtil;

    public void run(JavaRDD<Row> rdd) {
        // Row 데이터를 TimeTickSpeedDomain 객체로 맵핑
        rdd.mapPartitionsToPair(rowIterator -> {
            List<Tuple2<String, TimeTickSpeedDomain>> resultList = new ArrayList<>();

            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();

                TimeTickSpeedDomain domain = ConvertUtil.convertRowToSpeed(row);

                resultList.add(new Tuple2<>(row.getString(0), domain));

                log.info("roadId : {}, data : {}", row.getString(0), domain);
            }

            return resultList.iterator();
        }).reduceByKey((domain1, domain2) -> { // roadId 를 기준으로 1차 데이터 누적
            log.info("reduceByKey : {}, {}", domain1, domain2);

            // CV 값에 따른 2차 outlier 제거 후 다시 데이터 계산을 위해서 domain 데이터 저장
            List<TimeTickSpeedDomain> domainList = domain1.getDomainList();

            if(domainList == null) {
                domainList = new ArrayList<>();
                domainList.add(domain1);
                domainList.add(domain2);
            } else {
                domainList.add(domain2);
            }

            return TimeTickSpeedDomain.builder()
                    .roadId(domain1.getRoadId())
                    .domainList(domainList).build();
        }).foreachPartition(tuple2Iterator -> {
            while(tuple2Iterator.hasNext()) {
                Tuple2 data = tuple2Iterator.next();

                log.info("final roadId : {}, data : {}", data._1, data._2);

                TimeTickSpeedDomain domain = (TimeTickSpeedDomain) data._2;

                Collections.sort(domain.getDomainList()); // speedAvg 오름차순 정렬

                log.info("sorted final roadId : {}, data : {}", data._1, data._2);

                CalculationUtil.removeOutlierByCV(domain);

                log.info("outlier final roadId : {}, data : {}", data._1, data._2);

                if (domain.getDomainList().size() > 1) {
                    double meanValue = CalculationUtil.getMean(domain);
                    int speedLimit = 30; // 임시로 하드코딩
                    int flows = domain.getSumClientCount();
                    double minProbe = (0.432529 * domain.getRoadLength() * 0.01) + (10.5266 * (meanValue / speedLimit)) - (0.21292 * meanValue) + (0.14277 * domain.getLane()) + 2.765705 + 16;

                    TimeTickFeatureDomain featureDomain = TimeTickFeatureDomain.builder()
                            .roadId(domain.getRoadId())
                            .eventTime(domain.getMaxEventTime())
                            .mean(meanValue)
                            .sd(CalculationUtil.getSD(domain, meanValue))
                            .flows(domain.getSumClientCount())
                            .density(domain.getDensity())
                            .designSpeed(speedLimit)
                            .lane(domain.getLane())
                            .length(domain.getRoadLength())
                            .minProbe(minProbe)
                            .speed(meanValue)
                            .sflag(0)
                            .uflag(0).build();

                    log.info("TimeTickFeatureDomain result : {}", featureDomain);

                    phoenixUtil.insertFeatureData(featureDomain); // RTTI Feature 데이터 삽입

                    if (false/*flows >= minProbe*/) {

                    } else { // 장시간 로그 등록
                        TimeTickPeriodDomain periodDomain = ConvertUtil.convertFeatureToPeriod(featureDomain);

                        TimeTickPeriodDomain beforeDomain = phoenixUtil.selectPeriodBeforeData(periodDomain);

                        if(beforeDomain != null) {
                           long targetMilli = DateUtil.getMinusMinute(periodDomain.getStartTime().toLocalDateTime(), 5);
                           long beforeMilli = beforeDomain.getStartTime().getTime();

                           if(beforeMilli >= targetMilli && beforeMilli < (targetMilli * 5 * 60 * 1000)) { // 이전 startTime이 현재와 5분 이내이면 count+1
                               periodDomain.setCount(beforeDomain.getCount() + 1);
                           }
                        }

                        phoenixUtil.insertPeriodData(periodDomain); // period 데이터 삽입

                        List<TimeTickPeriodDomain> periodDomainList = phoenixUtil.selectPeriodListByDayOfWeek(periodDomain);

                        log.info("periodDomainList : {}", periodDomainList);

                        Map<String, List<List<TimeTickPeriodDomain>>> groupedPeriodList = ConvertUtil.getGroupedPeriodList(periodDomainList); // 키 순서는 yymmdd 내림차순

                        log.info("groupedPeriodList : {}", groupedPeriodList);
                        log.info("groupedPeriodList.size() : {}", groupedPeriodList.size());

                        if (groupedPeriodList.size() > 1) { // 주 데이터는 2개 이상 존재해야함
                            List<List<TimeTickPeriodDomain>> curWeek = groupedPeriodList.get(periodDomain.getEventDate());
                            List<List<TimeTickPeriodDomain>> onePrevWeek = groupedPeriodList.get(DateUtil.getFormattingDate(periodDomain.getStartTime().toLocalDateTime().minusDays(7), "yyyyMMdd"));
                            List<List<TimeTickPeriodDomain>> twoPrevWeek = groupedPeriodList.get(DateUtil.getFormattingDate(periodDomain.getStartTime().toLocalDateTime().minusDays(14), "yyyyMMdd"));

                            Timestamp patternStartTime = null;
                            Timestamp patternEndTime = null;
                            int patternCount = -1;

                            log.info("curWeek : {}", curWeek);
                            log.info("onePrevWeek : {}", onePrevWeek);
                            log.info("twoPrevWeek : {}", twoPrevWeek);

                            Timestamp targetTimestamp = null;
                            Timestamp compareTimestamp = null;

                            if (curWeek != null && !curWeek.isEmpty()) { // 현재주 기준으로 전주 전전주 비교
                                for (List<TimeTickPeriodDomain> curList : curWeek) {
                                    targetTimestamp = curList.get(0).getStartTime();

                                    if (onePrevWeek != null && onePrevWeek.size() > 0) { // 전주 데이터 비교
                                        for (List<TimeTickPeriodDomain> onePrevList : onePrevWeek) {
                                            compareTimestamp = onePrevList.get(0).getStartTime();

                                            if (curList.size() > patternCount || onePrevList.size() > patternCount) {
                                                if (DateUtil.isPrevAfterMinute(10, targetTimestamp, compareTimestamp)) { // starttime 10분 이상차이 && count >= 24
                                                    if (curList.size() >= onePrevList.size()) {
                                                        patternStartTime = targetTimestamp;
                                                        patternEndTime = curList.get(curList.size() - 1).getStartTime();
                                                        patternCount = curList.size();
                                                    } else {
                                                        patternStartTime = compareTimestamp;
                                                        patternEndTime = onePrevList.get(onePrevList.size() - 1).getStartTime();
                                                        patternCount = onePrevList.size();
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (twoPrevWeek != null && twoPrevWeek.size() > 0) { // 2주전 데이터 비교
                                        for (List<TimeTickPeriodDomain> twoPrevList : twoPrevWeek) {
                                            compareTimestamp = twoPrevList.get(0).getStartTime();

                                            if (curList.size() > patternCount || twoPrevList.size() > patternCount) {
                                                if (DateUtil.isPrevAfterMinute(10, targetTimestamp, compareTimestamp)) { // starttime 10분 이상차이 && count >= 24
                                                    if (curList.size() >= twoPrevList.size()) {
                                                        patternStartTime = targetTimestamp;
                                                        patternEndTime = curList.get(curList.size() - 1).getStartTime();
                                                        patternCount = curList.size();
                                                    } else {
                                                        patternStartTime = compareTimestamp;
                                                        patternEndTime = twoPrevList.get(twoPrevList.size() - 1).getStartTime();
                                                        patternCount = twoPrevList.size();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (onePrevWeek != null && !onePrevWeek.isEmpty()) { // 전주와 2주전 데이터 비교
                                for (List<TimeTickPeriodDomain> onePrevList : onePrevWeek) {
                                    targetTimestamp = onePrevList.get(0).getStartTime();

                                    if (twoPrevWeek != null && twoPrevWeek.size() > 0) { // 2주전 데이터 비교
                                        for (List<TimeTickPeriodDomain> twoPrevList : twoPrevWeek) {
                                            compareTimestamp = twoPrevList.get(0).getStartTime();

                                            if (onePrevList.size() > patternCount || twoPrevList.size() > patternCount) {
                                                if (DateUtil.isPrevAfterMinute(10, targetTimestamp, compareTimestamp)) { // starttime 10분 이상차이 && count >= 24
                                                    if (onePrevList.size() >= twoPrevList.size()) {
                                                        patternStartTime = targetTimestamp;
                                                        patternEndTime = onePrevList.get(onePrevList.size() - 1).getStartTime();
                                                        patternCount = onePrevList.size();
                                                    } else {
                                                        patternStartTime = compareTimestamp;
                                                        patternEndTime = twoPrevList.get(twoPrevList.size() - 1).getStartTime();
                                                        patternCount = twoPrevList.size();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            //if()

                            // # of density >= TM 확인하는 부분
                        }
                    }
                }
            }
        });
    }
}
