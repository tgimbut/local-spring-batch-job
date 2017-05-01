package com.tgimbut.springbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class LocalSpringBatchJobApplication {

	public static void main(String[] args) {
		System.out.println("params: "+ Arrays.toString(args));
		ConfigurableApplicationContext context = SpringApplication.run(LocalSpringBatchJobApplication.class, args);
		context.close();
	}
}
