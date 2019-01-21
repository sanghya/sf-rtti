package com.skt.sf.rtti.logic;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Builder
@ToString
public class TimeTickPatternDomain {
    @Getter @Setter private String roadId;
    @Getter @Setter private Timestamp eventTime;
    @Getter @Setter private int dayOfWeek;
    @Getter @Setter private String eventDate;
    @Getter @Setter private Timestamp startTime;
    @Getter @Setter private Timestamp endTime;
    @Getter @Setter private int count;
    @Getter @Setter private double mean;
}
