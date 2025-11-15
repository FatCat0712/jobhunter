package vn.hoidanit.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.response.ResJobDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.JobService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class JobController {
    private final JobService jobService;

    @Autowired
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @ApiMessage("Create a job")
    @PostMapping("/jobs")
    public ResponseEntity<ResJobDTO> createJob(@RequestBody Job newJob) {
        Job createdJob = jobService.handleCreateJob(newJob);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.convertToJobDTO(createdJob));
    }

    @ApiMessage("Update a job")
    @PutMapping("/jobs")
    public ResponseEntity<ResJobDTO> updateJob(@RequestBody Job updateJob) throws IdInvalidException {
        Job existJob = jobService.fetchByJobId(updateJob.getId());
        if(existJob == null) {
            throw new IdInvalidException("Job not found");
        }
        Job createdJob = jobService.handleUpdateJob(updateJob, existJob);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.convertToJobDTO(createdJob));
    }

    @ApiMessage("Delete a job")
    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable(name = "id") Long id) throws IdInvalidException {
        Job existJob = jobService.fetchByJobId(id);
        if(existJob == null) {
            throw new IdInvalidException("Job not found");
        }
         jobService.handleDeleteJob(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiMessage("Get a job by id")
    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable(name = "id") Long id) throws IdInvalidException {
        Job existJob = jobService.fetchByJobId(id);
        if(existJob == null) {
            throw new IdInvalidException("Job not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(existJob);
    }

    @ApiMessage("Get job by page")
    @GetMapping("/jobs")
    public ResponseEntity<ResultPaginationDTO> listAllJobs(@Filter Specification<Job> spec, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(jobService.listJobByPage(spec, pageable));
    }



}
