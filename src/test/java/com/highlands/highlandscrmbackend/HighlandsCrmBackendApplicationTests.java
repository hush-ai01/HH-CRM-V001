package com.highlands.highlandscrmbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "jwt.secret=test-secret-for-automated-tests-32-bytes-minimum-123456",
                "jwt.expiration-ms=1800000",
                "jwt.issuer=highlands-crm"
        }
)
class HighlandsCrmBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}