package com.skt.sf.rtti.util;

import com.skt.sf.rtti.logic.TimeTickPeriodDomain;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Test {
    public static void main(String args[]) {
        // Date in String format.
        /*String dateString = "2015-03-01";

        // Converting date to Java8 Local date
        LocalDate startDate = LocalDate.parse(dateString);
        LocalDate endtDate = LocalDate.now();
        // Range = End date - Start date
        Long range = ChronoUnit.DAYS.between(startDate, endtDate);
        System.out.println("Number of days between the start date : " + dateString + " and end date : " + endtDate
                + " is  ==> " + range);

        range = ChronoUnit.DAYS.between(endtDate, startDate);
        System.out.println("Number of days between the start date : " + endtDate + " and end date : " + dateString
                + " is  ==> " + range);*/

        long milli = System.currentTimeMillis() - (60*60*1000*14);

        LocalDateTime localDateTime = new Timestamp(milli).toLocalDateTime();

        System.out.println(localDateTime.getHour());



        LocalDateTime compareDateTime = new Timestamp(milli + (60 * 1000)).toLocalDateTime();

        /*System.out.println(DateUtil.isPrevAfterMinute(1, new Timestamp(milli), new Timestamp(milli + (60 * 1000))));
        System.out.println(DateUtil.isPrevAfterMinute(1, new Timestamp(milli), new Timestamp(milli + (-60 * 1000))));
        System.out.println(DateUtil.isPrevAfterMinute(1, new Timestamp(milli), new Timestamp(milli + (61 * 1000))));
        System.out.println(DateUtil.isPrevAfterMinute(1, new Timestamp(milli), new Timestamp(milli + (-61 * 1000))));*/


       // Duration duration = Duration.between(localDateTime, compareDateTime);




        //System.out.println(duration.minusHours(duration.toHours()).toMinutes());
        //System.out.println(duration.minusHours(duration.toMillis()));
        //System.out.println(duration.minus);

        //ChronoUnit.MINUTES.

        //Period.between()


        //System.out.println(new Timestamp(milli));
        //System.out.println(new Timestamp(DateUtil.getPrevFiveTimeFromMinute(new Timestamp(milli).toLocalDateTime())));


        //System.out.println(System.currentTimeMillis());
        //System.out.println(new Timestamp(System.currentTimeMillis()).toLocalDateTime().getLong(ChronoField.MILLI_OF_DAY));
        //System.out.println(new Timestamp(System.currentTimeMillis()).toLocalDateTime().getLong(ChronoField.MILLI_OF_SECOND));
        //System.out.println(new Timestamp(System.currentTimeMillis()).toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());


        /*System.out.println(new Timestamp(System.currentTimeMillis()).toLocalDateTime().getDayOfWeek());
        System.out.println(new Timestamp(System.currentTimeMillis()).toLocalDateTime().getDayOfWeek().get((ChronoField.DAY_OF_WEEK)));
        System.out.println(new Timestamp(System.currentTimeMillis()).toLocalDateTime().minusDays(1));*/

        /*List<TimeTickPeriodDomain> list = new ArrayList<>();


        list.add(TimeTickPeriodDomain.builder().eventDate("20190100").startTime(new Timestamp(System.currentTimeMillis() - (60*1000*60*24))).build());
        list.add(TimeTickPeriodDomain.builder().eventDate("20190100").startTime(new Timestamp(System.currentTimeMillis() - (60*1000*60*24) - (60*1000*5))).build());
        list.add(TimeTickPeriodDomain.builder().eventDate("20190100").startTime(new Timestamp(System.currentTimeMillis() - (60*1000*60*24) - (60*1000*10))).build());

        list.add(TimeTickPeriodDomain.builder().eventDate("20190111").startTime(new Timestamp(System.currentTimeMillis())).build());
        list.add(TimeTickPeriodDomain.builder().eventDate("20190111").startTime(new Timestamp(System.currentTimeMillis() - (60*1000*5))).build());
        list.add(TimeTickPeriodDomain.builder().eventDate("20190111").startTime(new Timestamp(System.currentTimeMillis() - (60*1000*10))).build());

        list.add(TimeTickPeriodDomain.builder().eventDate("20190109").startTime(new Timestamp(System.currentTimeMillis() - (60*1000*60*48))).build());
        list.add(TimeTickPeriodDomain.builder().eventDate("20190109").startTime(new Timestamp(System.currentTimeMillis() - (60*1000*60*48) - (60*1000*5))).build());
        list.add(TimeTickPeriodDomain.builder().eventDate("20190109").startTime(new Timestamp(System.currentTimeMillis() - (60*1000*60*48) - (60*1000*10))).build());

        System.out.println(
                list.stream().collect(
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
                )
        );

        System.out.println(
                list.stream().collect(
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
                ).getClass()
        );*/





       /* double num = -1;
        System.out.println(num);
        System.out.println(num == -1);*/
    }
}
