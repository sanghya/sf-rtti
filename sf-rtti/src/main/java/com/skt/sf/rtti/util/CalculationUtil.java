package com.skt.sf.rtti.util;

import com.skt.sf.rtti.domain.TimeTickSpeedDomain;
import com.skt.sf.rtti.logic.TimeTickPeriodDomain;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.*;

@Slf4j
public class CalculationUtil {
    /* 평균속도 구하기 */
    public static double getMean(TimeTickSpeedDomain paramDomain) {
        List<TimeTickSpeedDomain> domainList = paramDomain.getDomainList();

        if(domainList == null || domainList.size() <= 0) return 0.0;

        int speedAvg = 0;

        for (TimeTickSpeedDomain domain: domainList) {
            speedAvg += domain.getSpeedAvg();
        }

        return speedAvg / domainList.size();
    }

    /* 표준편차 구하기 */
    public static double getSD(TimeTickSpeedDomain paramDomain, double paramMeanValue) {
        List<TimeTickSpeedDomain> domainList = paramDomain.getDomainList();

        if(domainList == null || domainList.size() < 2) return 0.0; // 표본데이터가 2개 이상일때만 의미있음

        double sum = 0.0;
        double sd = 0.0;
        double diff;
        double meanValue = paramMeanValue <= 0 ? getMean(paramDomain) : paramMeanValue;

        for (TimeTickSpeedDomain domain: domainList) {
            diff = domain.getSpeedAvg() - meanValue;
            sum += diff * diff;
        }

        sd = Math.sqrt(sum / (domainList.size() - 1));

        return sd;
    }

    /* 밀도(sub-unsub) 구하기 */
    public static int getDensity(TimeTickSpeedDomain paramDomain) {
        List<TimeTickSpeedDomain> domainList = paramDomain.getDomainList();

        if(domainList == null || domainList.size() <= 0) return 0;

        int clientSub = 0;
        int clientUnsub = 0;

        for (TimeTickSpeedDomain domain: domainList) {
            clientSub =+ domain.getClientSub();
            clientUnsub =+ domain.getClientUnsub();
        }

        return clientSub - clientUnsub;
    }

    /* 변동계수(coefficient of variation) 구하기  */
    public static double getCV(TimeTickSpeedDomain paramDomain, double paramMeanValue) {
        double meanValue = paramMeanValue <= 0 ? getMean(paramDomain) : paramMeanValue;

        if(meanValue > 0) {
            return getSD(paramDomain, meanValue) / meanValue;
        } else {
            return 0.0;
        }
    }

    public static double getRankNum(final int[] speedAvgs, final double value) {
        if (speedAvgs.length == 0) {
            throw new IllegalArgumentException("Data array is empty");
        }

        int lowerCount = 0;
        int sameCount = 0;
        int n = speedAvgs.length;

        for (int i = 0; i<speedAvgs.length; i++) {
            if (speedAvgs[i] < value) {
                lowerCount++;
            } else if (speedAvgs[i] == value) {
                sameCount++;
            } else {
                break;
            }
        }

        if (sameCount == 0) {
            throw new IllegalArgumentException("Provided value do not exists in dataset: " + value);
        }

        return (lowerCount + 0.5 * sameCount) / n * 100;
    }

    /* cv sd/mean 값에 따른 outlier 제거(domainList 원소제거) */
    public static void removeOutlierByCV(TimeTickSpeedDomain paramDomain) {
        double meanValue = getMean(paramDomain);
        double cv = getCV(paramDomain, meanValue);
        double sd = cv >= 0.1 ? getSD(paramDomain, meanValue) : 0.0;

        List<TimeTickSpeedDomain> domainList = paramDomain.getDomainList();

        if(domainList == null || domainList.size() <= 0) return;

        List<Integer> removeIndexList = new ArrayList<>();
        int[] speedAvgs = new int[domainList.size()];

        for (int i = 0; i < domainList.size(); i++) {
            speedAvgs[i] = domainList.get(i).getSpeedAvg();
        }

        for (int i = 0; i < domainList.size(); i++) {
            double rankNum = getRankNum(speedAvgs, domainList.get(i).getSpeedAvg());

            if (cv < 0.05) { // remove the top 2% and bottom 3%
                if (rankNum <= 3 || rankNum >= 98) {
                    removeIndexList.add(i);
                }
            } else if (cv >= 0.05 && cv < 0.1) { // remove the top 5% and bottom 5%
                if (rankNum <= 5 || rankNum >= 95) {
                    removeIndexList.add(i);
                }
            } else if (cv >= 0.1 && cv < 0.15) { // remove the top 8% and bottom 7%
                if (rankNum <= 7 || rankNum >= 92) {
                    removeIndexList.add(i);
                }
            } else if (cv >= 0.1) { // remove the values outside mean +- standard deviation(rankNum < 평균 + 표준편차 || rankNum > 평균 - 표준편차)
                if((meanValue > (meanValue + sd)) || (meanValue < (meanValue - sd))) {
                    removeIndexList.add(i);
                }
            }
        }

        for (int index: removeIndexList) {
            domainList.remove(index);
        }
    }

    // 3주의 데이터를 가지고 starttime 및 count 체크해서 맵에 starttime, endtime, count 넣어줌
    public static Map<String, Object> checkPeriodTimeAndCountSetResult(TimeTickPeriodDomain periodDomain, Map<String, List<List<TimeTickPeriodDomain>>> groupedPeriodList) {
        List<List<TimeTickPeriodDomain>> curWeek = groupedPeriodList.get(periodDomain.getEventDate());
        List<List<TimeTickPeriodDomain>> onePrevWeek = groupedPeriodList.get(DateUtil.getFormattingDate(periodDomain.getStartTime().toLocalDateTime().minusDays(7), "yyyyMMdd"));
        List<List<TimeTickPeriodDomain>> twoPrevWeek = groupedPeriodList.get(DateUtil.getFormattingDate(periodDomain.getStartTime().toLocalDateTime().minusDays(14), "yyyyMMdd"));

        Map<String, Object> resultData = new HashMap<>();

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

        return null;
    }

    public static void checkTwoWeekData(Map<String, Object> resultData, List<List<TimeTickPeriodDomain>> targetWeek, List<List<TimeTickPeriodDomain>> compareWeek) {
        Timestamp patternStartTime = null;
        Timestamp patternEndTime = null;

        Timestamp targetTimestamp = null;
        Timestamp compareTimestamp = null;
        int patternCount = -1;

        for (List<TimeTickPeriodDomain> curList : targetWeek) {
            targetTimestamp = curList.get(0).getStartTime();

            if (compareWeek != null && compareWeek.size() > 0) {
                for (List<TimeTickPeriodDomain> prevList : compareWeek) {
                    compareTimestamp = prevList.get(0).getStartTime();

                    if (curList.size() > patternCount || prevList.size() > patternCount) {
                        if (DateUtil.isPrevAfterMinute(10, targetTimestamp, compareTimestamp)) { // starttime 10분 이상차이 && count >= 24
                            if (curList.size() >= prevList.size()) {
                                patternStartTime = targetTimestamp;
                                patternEndTime = curList.get(curList.size() - 1).getStartTime();
                                patternCount = curList.size();
                            } else {
                                patternStartTime = compareTimestamp;
                                patternEndTime = prevList.get(prevList.size() - 1).getStartTime();
                                patternCount = prevList.size();
                            }
                        }
                    }
                }
            }
        }
    }
}
