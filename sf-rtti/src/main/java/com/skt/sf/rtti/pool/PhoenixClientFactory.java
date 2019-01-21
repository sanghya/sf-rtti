package com.skt.sf.rtti.pool;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
public class PhoenixClientFactory {
	public PhoenixClientFactory() {
    	try {
    		Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
    	} catch(Exception e) {
    		log.error("org.apache.phoenix.jdbc.PhoenixDriver load error", e);
    	}
    }

	public static Connection getConnection( String connStr ) {
    	Connection connection = null;
    	
		try {
			connection = DriverManager.getConnection(connStr);
		} catch(Exception e) {
			log.error("connection create fail!!", e);
		}

        return connection;
    }
}
