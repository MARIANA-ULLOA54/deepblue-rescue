package com.deepblue.deepblue.rescue;

import org.springframework.boot.SpringApplication;

import com.deepblue.rescue.DeepblueRescueApplication;

public class TestDeepblueRescueApplication {

	public static void main(String[] args) {
		SpringApplication.from(DeepblueRescueApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
