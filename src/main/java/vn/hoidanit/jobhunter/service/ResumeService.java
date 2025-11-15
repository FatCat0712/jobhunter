package vn.hoidanit.jobhunter.service;

import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import com.turkraft.springfilter.parser.FilterParser;
import com.turkraft.springfilter.parser.node.FilterNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Resume;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.resume.ResCreateResumeDTO;
import vn.hoidanit.jobhunter.domain.response.resume.ResFetchResumeDTO;
import vn.hoidanit.jobhunter.domain.response.resume.ResUpdateResumeDTO;
import vn.hoidanit.jobhunter.repository.JobRepository;
import vn.hoidanit.jobhunter.repository.ResumeRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.util.SecurityUtil;

import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final FilterParser filterParser;
    private final FilterSpecificationConverter filterSpecificationConverter;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository, UserRepository userRepository, JobRepository jobRepository, FilterParser filterParser, FilterSpecificationConverter filterSpecificationConverter) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.filterParser = filterParser;
        this.filterSpecificationConverter = filterSpecificationConverter;
    }

    public Resume fetchResumeById(Long resumeId) {
        Optional<Resume> resumeInDB = resumeRepository.findById(resumeId);
        return resumeInDB.orElse(null);
    }

    public boolean checkResumeExistByUserAndJob(Resume resume) {
//        check user by id
        if(resume.getUser() == null) return false;
        Optional<User> userOptional = userRepository.findById(resume.getUser().getId());
        if(userOptional.isEmpty()) return false;

//        check job by id
        if(resume.getJob() == null) return false;
        Optional<Job> jobOptional = jobRepository.findById(resume.getJob().getId());
        if(jobOptional.isEmpty()) return false;

        return true;

    }

    public Resume handleCreateResume(Resume newResume) {
        return resumeRepository.save(newResume);
    }

    public Resume handleUpdateResume(Resume updatedResume) {
        Resume resumeInDB = fetchResumeById(updatedResume.getId());
        resumeInDB.setStatus(updatedResume.getStatus());
        return resumeRepository.save(resumeInDB);
    }

    public void handleDeleteResume(Long resumeId) {
        resumeRepository.deleteById(resumeId);
    }

    public ResultPaginationDTO fetchResumeByPage(Specification<Resume> specs, Pageable pageable) {
        Page<Resume> page = resumeRepository.findAll(specs, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(page.getNumber() + 1);
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        mt.setPageSize(page.getSize());

        List<ResFetchResumeDTO> dtos  = page.getContent().stream().map(this::convertToResumeDTO).toList();
        rs.setMeta(mt);
        rs.setResult(dtos);

        return rs;
    }

    public ResultPaginationDTO fetchResumeByUser(Pageable pageable) {
//        query builder
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        FilterNode node = filterParser.parse("email='" + email + "'");
        FilterSpecification<Resume> spec = filterSpecificationConverter.convert(node);

        Page<Resume> pageResume = resumeRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageResume.getTotalPages());
        mt.setTotal(pageResume.getTotalElements());

        rs.setMeta(mt);

        List<ResFetchResumeDTO> dto = pageResume.getContent().stream().map(this::convertToResumeDTO).toList();
        rs.setResult(dto);
        return rs;
    }

    public ResCreateResumeDTO convertToCreateResumeDTO(Resume resume) {
        ResCreateResumeDTO dto = new ResCreateResumeDTO();
        dto.setId(resume.getId());
        dto.setCreatedAt(resume.getCreatedAt());
        dto.setCreatedBy(resume.getCreatedBy());
        return dto;
    }

    public ResUpdateResumeDTO convertToUpdateResumeDTO(Resume resume) {
        ResUpdateResumeDTO dto = new ResUpdateResumeDTO();
        dto.setUpdatedAt(resume.getUpdatedAt());
        dto.setUpdatedBy(resume.getUpdatedBy());
        return dto;
    }

    public ResFetchResumeDTO convertToResumeDTO(Resume resume) {
        ResFetchResumeDTO dto = new ResFetchResumeDTO();
        dto.setId(resume.getId());
        dto.setEmail(resume.getEmail());
        dto.setUrl(resume.getUrl());
        dto.setStatus(resume.getStatus());
        dto.setCreatedAt(resume.getCreatedAt());
        dto.setUpdatedAt(resume.getUpdatedAt());
        dto.setCreatedBy(resume.getCreatedBy());
        dto.setUpdatedBy(resume.getUpdatedBy());

        if(resume.getJob() != null) {
            dto.setCompanyName(resume.getJob().getCompany().getName());
        }

        ResFetchResumeDTO.ResumeUser user = new ResFetchResumeDTO.ResumeUser();
        user.setId(resume.getUser().getId());
        user.setName(resume.getUser().getName());
        dto.setUser(user);

        ResFetchResumeDTO.ResumeJob job = new ResFetchResumeDTO.ResumeJob();
         job.setId(resume.getJob().getId());
         job.setName(resume.getJob().getName());
        dto.setJob(job);

        return dto;

    }

}
