package com.NorthrnLights.demo;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NorthernLightsApplication {

	public static void main(String[] args) {

		SpringApplication.run(NorthernLightsApplication.class, args);
	}

}
