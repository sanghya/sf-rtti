package com.skt.sf.rtti.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Builder
@ToString
public class TimeTickSpeedDomain implements Serializable, Comparable<TimeTickSpeedDomain> {
    @Getter @Setter private String roadId;
    @Getter @Setter private Timestamp eventTime;
    @Getter @Setter private int speedAvg;
    @Getter @Setter private List<TimeTickSpeedDomain> domainList;
    @Getter @Setter private int clientCount;
    @Getter @Setter private int clientSub;
    @Getter @Setter private int clientUnsub;
    @Getter @Setter private int lane;
    @Getter @Setter private int speedLimit;
    @Getter @Setter private int roadLength;

    @Override
    public int compareTo(TimeTickSpeedDomain domain) {
        return this.getSpeedAvg() - domain.getSpeedAvg();
    }

    public Timestamp getMaxEventTime() {
        if(domainList == null || domainList.size() == 0) {
            return null;
        }

        long eventTime = 0;

        for (TimeTickSpeedDomain domain: domainList) {
            eventTime = Math.max(eventTime, domain.getEventTime().getTime());
        }

        return new Timestamp(eventTime);
    }

    public int getSumClientCount() {
        if(domainList == null || domainList.size() == 0) {
            return 0;
        }

        int clientCount = 0;

        for (TimeTickSpeedDomain domain: domainList) {
            clientCount += domain.getClientCount();
        }

        return clientCount;
    }

    public int getDensity() {
        if(domainList == null || domainList.size() == 0) {
            return 0;
        }

        int density = 0;

        for (TimeTickSpeedDomain domain: domainList) {
            density += (domain.getClientSub() - domain.getClientUnsub());
        }

        return density;
    }

    public double getMeanSpeedAvg() {
        if(domainList == null || domainList.size() == 0) {
            return 0;
        }

        int density = 0;

        for (TimeTickSpeedDomain domain: domainList) {
            density += (domain.getClientSub() - domain.getClientUnsub());
        }

        return density;
    }
}
