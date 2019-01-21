package com.skt.sf.rtti;

import com.skt.sf.rtti.configure.Context;
import com.skt.sf.rtti.domain.TimeTickSpeedDomain;
import com.skt.sf.rtti.logic.TimeTickSpeedLogic;
import com.skt.sf.rtti.util.PhoenixUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import scala.Tuple2;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@SpringBootApplication
public class SfRttiApplication implements Serializable {
    @Autowired
    private PhoenixUtil phoenixUtil;

    @Autowired
    private TimeTickSpeedLogic timeTickSpeedLogic;

    @PostConstruct
    public void appStart() {
        log.info("SfRttiApplication Server start..");

        SparkSession sparkSession = SparkSession
            .builder().master("local[*]")
            .appName("SfRttiApplication").getOrCreate();

        Dataset<Row> rows = phoenixUtil.loadPhoenix(sparkSession, "V2X_RTTI_TRAFFIC_INFO_20181214");

        Dataset<Row> datas = rows.select(phoenixUtil.getTrafficInfoCols())
                .where("roadId = '142442498' and client_count > 1").orderBy("event_time").limit(5);

        timeTickSpeedLogic.run(datas.javaRDD());
    }

    public static void main(String[] args) {
        SpringApplication.run(SfRttiApplication.class, args);
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
    }
}

