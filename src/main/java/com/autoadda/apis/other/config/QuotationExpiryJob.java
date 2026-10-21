package com.autoadda.apis.other.config;

import com.autoadda.apis.other.service.QuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Hourly sweep that expires quotation requests and vendor quotes past their deadline.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuotationExpiryJob {

    private final QuotationService quotationService;

    @Scheduled(cron = "${quotation.expiry.cron:0 0 * * * *}")
    public void expireQuotations() {
        int requests = quotationService.processExpiredRequests();
        int quotes = quotationService.processExpiredQuotations();
        log.info("Quotation expiry sweep: {} requests, {} vendor quotes expired", requests, quotes);
    }
}
