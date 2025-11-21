package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.util.RedisDataGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Provides endpoints for generating and managing test data
 */
@Slf4j
@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DataController {

    private final RedisDataGenerator dataGenerator;

    public DataController(RedisDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    /**
     * Generate test data
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTestData(
            @RequestParam(defaultValue = "100") int users,
            @RequestParam(defaultValue = "50") int products,
            @RequestParam(defaultValue = "200") int orders
    ) {
        log.info("Generating test data: users={}, products={}, orders={}", users, products, orders);

        try {
            dataGenerator.generateTestData(users, products, orders);
            Map<String, Object> stats = dataGenerator.getDataStatistics();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test data generated successfully",
                    "statistics", stats
            ));
        } catch (Exception e) {
            log.error("Failed to generate test data", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Clear all test data
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearTestData() {
        log.info("Clearing all test data");

        try {
            dataGenerator.clearTestData();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "All test data cleared"
            ));
        } catch (Exception e) {
            log.error("Failed to clear test data", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get data statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = dataGenerator.getDataStatistics();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statistics", stats
            ));
        } catch (Exception e) {
            log.error("Failed to get statistics", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
