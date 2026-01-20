package com.billsplitting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BillSplittingMcpServerApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // with all the MCP tools and services properly configured
    }
}