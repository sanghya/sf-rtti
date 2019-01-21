package com.skt.sf.rtti.configure;

import com.skt.sf.rtti.util.PhoenixUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import lombok.Getter;
import lombok.Setter;

public class Context {
	@Autowired private Environment env;
	@Autowired @Getter private PhoenixUtil phoenixUtil;

    public String getProperty(String propName) {
    	return env.getProperty(propName);
    }
}