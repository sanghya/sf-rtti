package com.skt.sf.rtti.logic;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Builder
@ToString
public class TimeTickFeatureDomain {
    @Getter @Setter private String roadId;
    @Getter @Setter private Timestamp eventTime;
    @Getter @Setter private double mean;
    @Getter @Setter private double sd;
    @Getter @Setter private int flows;
    @Getter @Setter private int density;
    @Getter @Setter private int designSpeed;
    @Getter @Setter private int lane;
    @Getter @Setter private int length;
    @Getter @Setter private double minProbe;
    @Getter @Setter private double speed;
    @Getter @Setter private int sflag;
    @Getter @Setter private int uflag;
}
