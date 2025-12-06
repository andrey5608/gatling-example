package org.andrey5608.performance.tests.web;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PerformanceTestsController {

    @GetMapping("/api/fast-response")
    public ResponseEntity<String> getFastResponse() {
        return ResponseEntity.ok("was that fast enough?");
    }

    @GetMapping("/api/slow-response")
    public ResponseEntity<String> getSlowResponse() throws InterruptedException {
        int minDelayMs = 1_000;
        int maxDelayMs = 2_000;
        TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(minDelayMs, maxDelayMs));
        return ResponseEntity.ok("this took a while");
    }
}
