package com.skt.sf.rtti.util;

import com.skt.sf.rtti.domain.TimeTickSpeedDomain;
import com.skt.sf.rtti.logic.TimeTickFeatureDomain;
import com.skt.sf.rtti.logic.TimeTickPeriodDomain;
import org.apache.spark.sql.Row;

import java.util.*;
import java.util.stream.Collectors;

public class ConvertUtil {
    public static final int ROW_COUNT = 24;

    public static TimeTickPeriodDomain convertFeatureToPeriod(TimeTickFeatureDomain featureDomain) {
        return TimeTickPeriodDomain.builder()
                .roadId(featureDomain.getRoadId())
                .dayOfWeek(DateUtil.getDayOfWeek(featureDomain.getEventTime()))
                .eventDate(DateUtil.getFormattingDate(featureDomain.getEventTime().toLocalDateTime(), "yyyyMMdd"))
                .startTime(featureDomain.getEventTime())
                .meanSpeedAvg(featureDomain.getMean()).build();
    }

    public static TimeTickSpeedDomain convertRowToSpeed(Row row) {
        return TimeTickSpeedDomain.builder()
                .roadId(row.getString(0))
                .eventTime(row.getTimestamp(1))
                .speedAvg(row.getInt(2))
                .clientCount(row.getInt(3))
                .clientSub(row.getInt(4))
                .clientUnsub(row.getInt(5))
                .build();
    }

    public static Map<String, List<List<TimeTickPeriodDomain>>> getGroupedPeriodList(List<TimeTickPeriodDomain> periodDomainList) {
        Map<String, List<List<TimeTickPeriodDomain>>> dataMap = new HashMap<>();

        Set<Map.Entry<String, List<TimeTickPeriodDomain>>> dataSet =
                periodDomainList.stream().collect(Collectors.groupingBy(TimeTickPeriodDomain::getEventDate)).entrySet();

        for(Map.Entry<String, List<TimeTickPeriodDomain>> entry : dataSet) {
            String key = entry.getKey();
            List<TimeTickPeriodDomain> domainList = entry.getValue();

            if(domainList.size() >= ROW_COUNT) {
                List<List<TimeTickPeriodDomain>> dataGroupList = new ArrayList<>();
                List<TimeTickPeriodDomain> dataList = new ArrayList<>();
                TimeTickPeriodDomain prevDomain = null;
                int repeatCount = 0;

                for (TimeTickPeriodDomain domain : domainList) {
                    repeatCount++;

                    if (prevDomain == null) {
                        dataList.add(domain);
                        prevDomain = domain;
                        continue;
                    }

                    long curMilli = domain.getStartTime().getTime();
                    long targetMilli = DateUtil.getMinusMinute(prevDomain.getStartTime().toLocalDateTime(), 5);

                    if (curMilli >= targetMilli && curMilli < (targetMilli + (60 * 1000 * 5))) {
                        prevDomain = domain;
                        dataList.add(domain);
                    } else {
                        if (dataList.size() >= ROW_COUNT) {
                            dataGroupList.add(dataList);
                        } else {
                            if(domainList.size() - repeatCount >= ROW_COUNT) {
                                dataList = new ArrayList<>();
                                prevDomain = null;
                            } else {
                                break;
                            }
                        }
                    }
                }

                if (dataList.size() >= ROW_COUNT) {
                    dataGroupList.add(dataList);
                }

                if(dataGroupList.size() > 0) {
                    dataMap.put(key, dataGroupList);
                }
            }
        }

        return dataMap;
    }

    public static Map<String, List<TimeTickPeriodDomain>> getThreeWeekDataMap(List<TimeTickPeriodDomain> periodDomainList) {
        return periodDomainList.stream().collect(
                Collectors.groupingBy(TimeTickPeriodDomain::getEventDate, () -> new TreeMap<>((o1, o2) -> {
                    int num1 = Integer.parseInt(o1);
                    int num2 = Integer.parseInt(o2);

                    if(num1 > num2) {
                        return -1;
                    } else if(num1 < num2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }), Collectors.toList())
        );
    }
}
