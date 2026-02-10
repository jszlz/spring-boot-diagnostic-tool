package com.diagnostic.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 诊断监控仪表板应用
 */
@SpringBootApplication(scanBasePackages = "com.diagnostic")
public class DashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }
}
