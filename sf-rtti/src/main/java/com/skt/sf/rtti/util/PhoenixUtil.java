package com.skt.sf.rtti.util;

import com.skt.sf.rtti.logic.TimeTickFeatureDomain;
import com.skt.sf.rtti.logic.TimeTickPeriodDomain;
import com.skt.sf.rtti.pool.PhoenixClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericData;
import org.apache.phoenix.jdbc.PhoenixDriver;
import org.apache.spark.sql.*;
import org.spark_project.guava.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import scala.collection.Seq;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

import static org.apache.spark.sql.functions.col;

/**
 * TODO 데이터를 불러왔다 못 불러왔다해서 우선 사용안함, 차후 좀더 테스트 후 사용해야 함
 */

@Component
@Slf4j
public class PhoenixUtil implements Serializable {
    @Value("${sf.rtti.zookeeper.hosts}") private String zookeeperHost;
    @Value("${sf.rtti.phoenix.url}") private String phoenixUrl;

    public Dataset<Row> loadPhoenix(SparkSession spark, String tableName) {
        return spark.sqlContext().read()
            .format("org.apache.phoenix.spark")
            .option("table", tableName)
            .option("zkUrl", zookeeperHost)
            .load();
    }

    public Column[] getTrafficInfoCols() {
        return new Column[] {col("roadId"), col("event_time"), col("speed_avg"), col("client_count"), col("client_sub"), col("client_unsub")};
    }

    public void insertFeatureData(TimeTickFeatureDomain domain) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = PhoenixClientFactory.getConnection(phoenixUrl);
            ps = con.prepareStatement("UPSERT INTO V2X_RTTI_FEATURE (roadId, event_time, mean, sd, flows, density, design_speed, lane, length, min_probe, speed, sflag, uflag) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");

            ps.setString(1, domain.getRoadId());
            ps.setTimestamp(2, domain.getEventTime());
            ps.setDouble(3, domain.getMean());
            ps.setDouble(4, domain.getSd());
            ps.setInt(5, domain.getFlows());
            ps.setInt(6, domain.getDensity());
            ps.setInt(7, domain.getDesignSpeed());
            ps.setInt(8, domain.getLane());
            ps.setInt(9, domain.getLength());
            ps.setDouble(10, domain.getMinProbe());
            ps.setDouble(11, domain.getSpeed());
            ps.setDouble(12, domain.getSflag());
            ps.setDouble(13, domain.getUflag());

            ps.executeUpdate();
            con.commit();
        } catch(Exception e) {
            log.error("V2X_RTTI_FEATURE data insert error!!", e);
        } finally {
            try {if(ps != null) ps.close();} catch(Exception e) {e.printStackTrace();}
            try {if(con != null) con.close();} catch(Exception e) {e.printStackTrace();}
        }
    }

    public void insertPeriodData(TimeTickPeriodDomain domain) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = PhoenixClientFactory.getConnection(phoenixUrl);
            ps = con.prepareStatement("UPSERT INTO V2X_RTTI_PERIOD (roadId,day_of_week,event_date,start_time,speed) VALUES(?,?,?,?,?)");

            ps.setString(1, domain.getRoadId());
            ps.setInt(2, domain.getDayOfWeek());
            ps.setString(3, domain.getEventDate());
            ps.setTimestamp(4, domain.getStartTime());
            ps.setDouble(5, domain.getMeanSpeedAvg());

            ps.executeUpdate();
            con.commit();
        } catch(Exception e) {
            log.error("V2X_RTTI_PERIOD data insert error!!", e);
        } finally {
            try {if(ps != null) ps.close();} catch(Exception e) {e.printStackTrace();}
            try {if(con != null) con.close();} catch(Exception e) {e.printStackTrace();}
        }
    }

    public void insertPatternData() {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = PhoenixClientFactory.getConnection(phoenixUrl);
            ps = con.prepareStatement("UPSERT INTO V2X_RTTI_PATTERN (roadId,day_of_week,event_date,start_time,end_time,count,mean) VALUES(?,?,?,?,?,?,?)");

            /*ps.setString(1, domain.getRoadId());
            ps.setTimestamp(2, domain.getEventTime());
            ps.setDouble(3, domain.getMean());
            ps.setDouble(4, domain.getSd());
            ps.setInt(5, domain.getFlows());
            ps.setInt(6, domain.getDensity());
            ps.setInt(7, domain.getDesignSpeed());*/

            ps.executeUpdate();
            con.commit();
        } catch(Exception e) {
            log.error("V2X_RTTI_PATTERN data insert error!!", e);
        } finally {
            try {if(ps != null) ps.close();} catch(Exception e) {e.printStackTrace();}
            try {if(con != null) con.close();} catch(Exception e) {e.printStackTrace();}
        }
    }

    public List<TimeTickPeriodDomain> selectPeriodListByDayOfWeek(TimeTickPeriodDomain domain) {
        Connection con = null;
        PreparedStatement ps = null;
        List<TimeTickPeriodDomain> list = null;
        ResultSet rs = null;

        try {
            con = PhoenixClientFactory.getConnection(phoenixUrl);
            ps = con.prepareStatement("select event_date, start_time from V2X_RTTI_PERIOD where roadId = ? and day_of_week = ? and event_date in (?, ?, ?) order by start_time desc");

            ps.setString(1, domain.getRoadId());
            ps.setInt(2, domain.getDayOfWeek());
            ps.setString(3, domain.getEventDate());
            ps.setString(4, DateUtil.getFormattingDate(domain.getStartTime().toLocalDateTime().minusDays(7), "yyyyMMdd"));
            ps.setString(5, DateUtil.getFormattingDate(domain.getStartTime().toLocalDateTime().minusDays(14), "yyyyMMdd"));

            rs = ps.executeQuery();
            list = new ArrayList<>();

            while(rs.next()) {
                list.add(TimeTickPeriodDomain.builder().eventDate(rs.getString("event_date")).startTime(rs.getTimestamp("start_time")).build());
            }
        } catch(Exception e) {
            log.error("selectPeriodByDayOfWeek error!!", e);
        } finally {
            try {if(rs != null) rs.close();} catch(Exception e) {e.printStackTrace();}
            try {if(ps != null) ps.close();} catch(Exception e) {e.printStackTrace();}
            try {if(con != null) con.close();} catch(Exception e) {e.printStackTrace();}
        }

        return list;
    }

    /* 이전 데이터 하나 가져옴 */
    public TimeTickPeriodDomain selectPeriodBeforeData(TimeTickPeriodDomain domain) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        TimeTickPeriodDomain resultDomain = null;

        try {
            con = PhoenixClientFactory.getConnection(phoenixUrl);
            ps = con.prepareStatement("select event_date, start_time from V2X_RTTI_PERIOD where roadId = ? and day_of_week = ? and event_date = ? order by start_time desc limit 1");

            ps.setString(1, domain.getRoadId());
            ps.setInt(2, domain.getDayOfWeek());
            ps.setString(3, domain.getEventDate());
            ps.setString(4, DateUtil.getFormattingDate(domain.getStartTime().toLocalDateTime(), "yyyyMMdd"));

            rs = ps.executeQuery();

            if(rs.next()) {
                resultDomain = TimeTickPeriodDomain.builder().eventDate(rs.getString("event_date")).startTime(rs.getTimestamp("start_time")).build();
            }
        } catch(Exception e) {
            log.error("selectPeriodBeforeData error!!", e);
        } finally {
            try {if(rs != null) rs.close();} catch(Exception e) {e.printStackTrace();}
            try {if(ps != null) ps.close();} catch(Exception e) {e.printStackTrace();}
            try {if(con != null) con.close();} catch(Exception e) {e.printStackTrace();}
        }

        return resultDomain;
    }
}