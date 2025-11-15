package vn.hoidanit.jobhunter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResJobDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.repository.JobRepository;
import vn.hoidanit.jobhunter.repository.SkillRepository;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public JobService(JobRepository jobRepository, SkillRepository skillRepository, CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
        this.companyRepository = companyRepository;
    }

    public Job fetchByJobId(Long jobId) {
       Optional<Job> jobInDB =  jobRepository.findById(jobId);
       return jobInDB.orElse(null);
    }

    public Job handleCreateJob(Job newJob) {
//        check skills
        if(newJob.getSkills() != null) {
            List<Long> listSkillIds = newJob.getSkills().stream().map(Skill::getId).toList();

            List<Skill> skillsInDB = skillRepository.findByIdIn(listSkillIds);

            newJob.setSkills(skillsInDB);
        }

//        check company
            if(newJob.getCompany() != null) {
                Optional<Company> cOptional = companyRepository.findById(newJob.getCompany().getId());
                if(cOptional.isPresent()) {
                    newJob.setCompany(cOptional.get());
                }
            }

//        create job
        return jobRepository.save(newJob);
    }

    public void handleDeleteJob(Long jobId) {
        jobRepository.deleteById(jobId);
    }

    public ResultPaginationDTO listJobByPage(Specification<Job> spec, Pageable pageable) {
        Page<Job> page = jobRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(page.getNumber());
        meta.setPages(page.getTotalPages());
        meta.setPageSize(page.getSize());
        meta.setTotal(page.getTotalPages());

        rs.setMeta(meta);
        rs.setResult(page.getContent());

        return rs;
    }

    public Job handleUpdateJob(Job updateJob, Job existJob) {
        //        check skills
        if(updateJob.getSkills() != null) {
            List<Long> listSkillIds = updateJob.getSkills().stream().map(Skill::getId).toList();

            List<Skill> skillsInDB = skillRepository.findByIdIn(listSkillIds);

           existJob.setSkills(skillsInDB);
        }

//        check company
        if(updateJob.getCompany() != null) {
            Optional<Company> cOptional = companyRepository.findById(updateJob.getCompany().getId());
            if(cOptional.isPresent()) {
                existJob.setCompany(cOptional.get());
            }
        }

       existJob.setName(updateJob.getName());
       existJob.setLocation(updateJob.getLocation());
       existJob.setSalary(updateJob.getSalary());
       existJob.setQuantity(updateJob.getQuantity());
       existJob.setLevel(updateJob.getLevel());
       existJob.setDescription(updateJob.getDescription());
       existJob.setStartDate(updateJob.getStartDate());
       existJob.setEndDate(updateJob.getEndDate());
       existJob.setActive(updateJob.isActive());



        return jobRepository.save(existJob);
    }

    public ResJobDTO convertToJobDTO(Job job) {
        ResJobDTO dto = new ResJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setQuantity(job.getQuantity());
        dto.setLevel(job.getLevel());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());

        List<String> skillNames = job.getSkills().stream().map(Skill::getName).toList();
        dto.setSkills(skillNames);

        dto.setCreatedAt(job.getCreatedAt());
        dto.setCreatedBy(job.getCreatedBy());
        dto.setActive(job.isActive());

        return dto;
    }
}
