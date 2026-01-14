package com.kos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class KosApplication {
	
	 protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
	        return builder.sources(KosApplication.class);
	    }

	public static void main(String[] args) {
		SpringApplication.run(KosApplication.class, args);
	}

}
