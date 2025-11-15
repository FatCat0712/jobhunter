package vn.hoidanit.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Resume;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.resume.ResCreateResumeDTO;
import vn.hoidanit.jobhunter.domain.response.resume.ResFetchResumeDTO;
import vn.hoidanit.jobhunter.domain.response.resume.ResUpdateResumeDTO;
import vn.hoidanit.jobhunter.service.JobService;
import vn.hoidanit.jobhunter.service.ResumeService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ResumeController {
    private final ResumeService resumeService;
    private final UserService userService;
    private final JobService jobService;
    private final FilterSpecificationConverter filterSpecificationConverter;
    private final FilterBuilder filterBuilder;

    @Autowired
    public ResumeController(
            ResumeService resumeService, UserService userService,
            JobService jobService, FilterSpecificationConverter filterSpecificationConverter,
            FilterBuilder filterBuilder
    ) {
        this.resumeService = resumeService;
        this.userService = userService;
        this.jobService = jobService;
        this.filterSpecificationConverter = filterSpecificationConverter;
        this.filterBuilder = filterBuilder;
    }

    @ApiMessage("Create a resume")
    @PostMapping("/resumes")
    public ResponseEntity<ResCreateResumeDTO> createResume(@Valid @RequestBody Resume newResume) throws IdInvalidException {
       boolean isExist =  resumeService.checkResumeExistByUserAndJob(newResume);
       if(!isExist) {
           throw new IdInvalidException("User id/Job id khong ton tai");
       }
        Resume resume = resumeService.handleCreateResume(newResume);

//       create new resume
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.convertToCreateResumeDTO(resume));
    }

    @ApiMessage("Update a resume")
    @PutMapping("/resumes")
    public ResponseEntity<ResUpdateResumeDTO> updateResume(@RequestBody Resume newResume) throws IdInvalidException {
        Resume resume =  resumeService.fetchResumeById(newResume.getId());
        if(resume == null) {
            throw new IdInvalidException(String.format("Resume voi id = %d khong ton tai", newResume.getId()));
        }
        Resume updatedResume = resumeService.handleUpdateResume(newResume);
        return ResponseEntity.status(HttpStatus.OK).body(resumeService.convertToUpdateResumeDTO(updatedResume));
    }

    @ApiMessage("Delete a resume by id")
    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable(name = "id") Long id) throws IdInvalidException {
        Resume resume =  resumeService.fetchResumeById(id);
        if(resume == null) {
            throw new IdInvalidException(String.format("Resume voi id = %d khong ton tai", id));
        }
        resumeService.handleDeleteResume(id);
        return ResponseEntity.ok().build();
    }

    @ApiMessage("Fetch a resume by id")
    @GetMapping("/resumes/{id}")
    public ResponseEntity<ResFetchResumeDTO> fetchResumeById(@PathVariable(name = "id") Long id) throws IdInvalidException {
        Resume resume =  resumeService.fetchResumeById(id);
        if(resume == null) {
            throw new IdInvalidException(String.format("Resume voi id = %d khong ton tai", id));
        }
        Resume resumeInDB = resumeService.fetchResumeById(id);
        return ResponseEntity.ok().body(resumeService.convertToResumeDTO(resumeInDB));
    }

    @ApiMessage("Fetch resumes with pagination")
    @GetMapping("/resumes")
    public ResponseEntity<ResultPaginationDTO> fetchResumesWithPagination(
            @Filter Specification<Resume> spec,
            Pageable pageable
    )
    {
        List<Long> arrJobsIds = null;
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        User currentUser = userService.handleGetUserByUsername(email);

        if(currentUser != null) {
            Company userCompany = currentUser.getCompany();
            if(userCompany != null) {
                List<Job> companyJobs = userCompany.getJobs();
                if(companyJobs != null && !companyJobs.isEmpty()) {
                    arrJobsIds = companyJobs.stream().map(Job::getId).toList();

                    Specification<Resume> jobInSpec = filterSpecificationConverter.convert(filterBuilder
                            .field("job")
                            .in(filterBuilder.input(arrJobsIds)).get()
                    );

                    spec = spec.and(jobInSpec);
                }
            }
        }

        return ResponseEntity.ok().body(resumeService.fetchResumeByPage(spec, pageable));
    }

    @ApiMessage("Fetch resumes by user")
    @PostMapping("/resumes/by-user")
    public ResponseEntity<ResultPaginationDTO> fetchResumeByUser(
            Pageable pageable
    )
    {
        return ResponseEntity.ok().body(resumeService.fetchResumeByUser(pageable));
    }





}
