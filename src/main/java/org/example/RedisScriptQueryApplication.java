package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Redis Script Query System - Main Application
 *
 * A dynamic Redis query system that allows users to execute JVM scripts (Groovy)
 * to query Redis data through a web interface with LLM-powered script analysis.
 */
@SpringBootApplication
public class RedisScriptQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisScriptQueryApplication.class, args);
    }
}
