package com.skt.sf.rtti.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource(value= {"classpath:app.properties"})
public class AppConfiguration {
	@Bean
	public Context context() {
		return new Context();
	}
	
	/* 생성해 주지 않아도 프로퍼티 파일 읽을수 있음 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}