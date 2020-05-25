package com.vmware.cpsbu.test.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "com.vmware.cpsbu.test.*" })
@EntityScan("com.vmware.cpsbu.test.jpa")
@EnableJpaRepositories("com.vmware.cpsbu.test.dao.repo")
public class NumGeneratorApp {

	public static void main(String[] args) {
		SpringApplication.run(NumGeneratorApp.class, args);
	}
}
