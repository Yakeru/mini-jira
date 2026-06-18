package com.yakeru.mini_jira;

import org.springframework.boot.SpringApplication;

public class TestMiniJiraApplication {

	public static void main(String[] args) {
		SpringApplication.from(MiniJiraApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
