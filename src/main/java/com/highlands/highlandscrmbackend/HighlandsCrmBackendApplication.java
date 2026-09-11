package com.highlands.highlandscrmbackend;

import com.highlands.highlandscrmbackend.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class HighlandsCrmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HighlandsCrmBackendApplication.class, args);
    }

}
