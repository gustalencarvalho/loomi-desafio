package com.ecommerce.order_processing_system.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Slf4j
public class SchedulerShipping {

    @Scheduled(cron = "0 0 6 * * ?")
    public void simulateShipping() {
        log.info("Shipping product on the date {} ", LocalDateTime.now());
    }

}
