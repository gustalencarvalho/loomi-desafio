package com.ecommerce.order_processing_system.domain.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DigitalPolicy {

    public void sendEmail(String orderId, String licenseKey) {
        log.info("Send e-mail with product");
        String downloadLink = "https://download.fake.com/" + orderId;
        log.info("Your digital product is ready", "Download: " + downloadLink + "\nLicense: " + licenseKey);
    }
}
