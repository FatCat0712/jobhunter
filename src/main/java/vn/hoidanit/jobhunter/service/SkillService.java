package vn.hoidanit.jobhunter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.SkillRepository;

import java.util.Optional;

@Service
public class SkillService {
    private final SkillRepository skillRepository;

    @Autowired
    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public Skill fetchSkillBySkillName(String skillName) {
        return skillRepository.findByName(skillName);
    }

    public Skill fetchSkillBySkillId(Long id) {
        Optional<Skill> skillInDB = skillRepository.findById(id);
        return skillInDB.orElse(null);
    }

    public Skill handleCreateSkill(Skill newSkill) {
        return skillRepository.save(newSkill);
    }

    public Skill handleUpdateSkill(Skill updateSkill) {
        Skill skillInDB = fetchSkillBySkillId(updateSkill.getId());
        skillInDB.setName(updateSkill.getName());
        return skillRepository.save(skillInDB);
    }

    public void handleDeleteSkill(Long id) {
        // delete job (inside job_skill table)
        Optional<Skill> skillOptional = skillRepository.findById(id);
        if(skillOptional.isPresent()) {
            Skill currentSkill = skillOptional.get();
            currentSkill.getJobs().forEach(job -> job.getSkills().remove(currentSkill));

            currentSkill.getSubscribers().forEach(sub -> sub.getSkills().remove(currentSkill));

            //     delete skill
            skillRepository.delete(currentSkill);
        }
    }

    public ResultPaginationDTO fetchAllSkills(Specification<Skill> spec, Pageable pageable) {
        Page<Skill> page = skillRepository.findAll(spec, pageable);

        ResultPaginationDTO resultDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(page.getNumber() + 1);
        mt.setPageSize(page.getSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());

        resultDTO.setMeta(mt);
        resultDTO.setResult(page.getContent());

        return resultDTO;
    }
}

