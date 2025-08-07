package com.genius.primavera.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/batch/jobs")
@RequiredArgsConstructor
public class JobLauncherController {

    private final JobLauncher asyncJobLauncher;
    private final Job productIndexingJob;

    @PostMapping("/product-indexing/run")
    public ResponseEntity<Map<String, Object>> runProductIndexingJob(@RequestParam(value = "forceRestart", defaultValue = "false") boolean forceRestart) {

        Map<String, Object> response = new HashMap<>();

        try {
            JobParametersBuilder paramsBuilder = new JobParametersBuilder();
            paramsBuilder.addString("executionTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            paramsBuilder.addString("jobType", "MANUAL");
            if (forceRestart) paramsBuilder.addLong("timestamp", System.currentTimeMillis());
            JobParameters jobParameters = paramsBuilder.toJobParameters();
            var jobExecution = asyncJobLauncher.run(productIndexingJob, jobParameters);
            response.put("success", true);
            response.put("jobId", jobExecution.getId());
            response.put("jobStatus", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("message", "Product indexing job started successfully");
            log.info("Product indexing job started with ID: {}", jobExecution.getId());
            return ResponseEntity.ok(response);

        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("Job is already running", e);
            response.put("success", false);
            response.put("error", "JOB_ALREADY_RUNNING");
            response.put("message", "Product indexing job is already running");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (JobRestartException e) {
            log.error("Job restart failed", e);
            response.put("success", false);
            response.put("error", "JOB_RESTART_FAILED");
            response.put("message", "Failed to restart job. Try with forceRestart=true");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (JobInstanceAlreadyCompleteException e) {
            log.info("Job instance already completed", e);
            response.put("success", false);
            response.put("error", "JOB_ALREADY_COMPLETE");
            response.put("message", "This job instance has already completed. Use forceRestart=true to run again");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (JobParametersInvalidException e) {
            log.error("Invalid job parameters", e);
            response.put("success", false);
            response.put("error", "INVALID_PARAMETERS");
            response.put("message", "Invalid job parameters provided");
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Unexpected error while starting job", e);
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/product-indexing/status")
    public ResponseEntity<Map<String, Object>> getJobStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("jobName", "productIndexingJob");
        response.put("status", "READY");
        response.put("lastExecutionTime", LocalDateTime.now().minusHours(1));
        response.put("nextScheduledTime", LocalDateTime.now().plusHours(1));
        return ResponseEntity.ok(response);
    }
}