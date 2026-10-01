package com.highlands.highlandscrmbackend.deal;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DealNumberGenerator {

    public String generate() {
        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        return "DEAL-" + suffix;
    }
}