package com.skt.sf.rtti.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class DateUtil {
    /* 날짜포맷팅 문자열 반환 */
    public static String getFormattingDate(LocalDateTime ldt, String format) {
        return ldt.format(DateTimeFormatter.ofPattern(format));
    }

    /* sql.Timestamp 객체에서 요일숫자값 얻음(1 (Monday) to 7 (Sunday)) */
    public static int getDayOfWeek(Timestamp ts) {
        return ts.toLocalDateTime().getDayOfWeek().get((ChronoField.DAY_OF_WEEK));
    }

    /* 입력 시간정보에서 0분, 5분 기준으로 {minusMinute}분전 밀리세컨드정보 가져옴(마지막 자리 0~4분:0, 5~9분:5) */
    public static long getMinusMinute(LocalDateTime ldt, int minusMinute) {
        String minute = ldt.format(DateTimeFormatter.ofPattern("mm"));
        String firstMinute = String.valueOf(minute.charAt(0));
        int secondMinute = Integer.parseInt(minute.charAt(1)+"");

        int resultMinute = 0;

        if(secondMinute >=0 && secondMinute <= 4) {
            resultMinute = Integer.parseInt(firstMinute + 0);
        } else {
            resultMinute = Integer.parseInt(firstMinute + 5);
        }

        //System.out.println(ldt.getYear() + "|" + ldt.getMonth() + "|" + ldt.getDayOfMonth() + "|" + ldt.getHour() + "|" + ldt.getMinute());

        return LocalDateTime.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth(), ldt.getHour(), resultMinute, 00).minusMinutes(minusMinute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /* 입력 시간정보에서 0분, 5분 기준으로 {minusMinute}분후 밀리세컨드정보 가져옴(마지막 자리 0~4분:5, 5~9분:0) */
    public static long getPlusMinute(LocalDateTime ldt, int plusMinute) {
        String minute = ldt.format(DateTimeFormatter.ofPattern("mm"));
        int firstMinute = Integer.parseInt(minute.charAt(0)+"");
        int secondMinute = Integer.parseInt(minute.charAt(1)+"");

        int resultMinute = 0;
        int resultHour = ldt.getHour();

        if(secondMinute >=0 && secondMinute <= 4) {
            resultMinute = Integer.parseInt(firstMinute + "" + 5);
        } else {
            if(firstMinute == 5) { // 앞자리가 5이면(50분) 분은 00에 시간이 +1이 된다.
                firstMinute = 0;
                resultHour = (ldt.getHour() + 1) == 24 ? 0 : ldt.getHour() + 1;
            } else {
                firstMinute++;
            }

            resultMinute = Integer.parseInt(firstMinute + "" + 0);
        }

        //System.out.println(ldt.getYear() + "|" + ldt.getMonth() + "|" + ldt.getDayOfMonth() + "|" + ldt.getHour() + "|" + ldt.getMinute());

        return LocalDateTime.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth(), resultHour, resultMinute, 00).plusMinutes(plusMinute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // 전:후 분 차이 체크
    public static boolean isPrevAfterMinute(int minute, Timestamp targetTimestamp, Timestamp compareTimestamp) {
        long prevTime = getMinusMinute(targetTimestamp.toLocalDateTime(), minute);
        long afterTime = getPlusMinute(targetTimestamp.toLocalDateTime(), minute);
        long compareTime = compareTimestamp.getTime();

        return compareTime >= prevTime && compareTime <= afterTime;
    }
}
