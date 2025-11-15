package vn.hoidanit.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.SkillService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.DuplicateSkillNameException;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class SkillController {
    private final SkillService skillService;

    @Autowired
    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @ApiMessage("Create a skill")
    @PostMapping("/skills")
    public ResponseEntity<Skill> createSkill(@Valid @RequestBody Skill newSkill) throws DuplicateSkillNameException {
        Skill existSkill = skillService.fetchSkillBySkillName(newSkill.getName());

        if(existSkill != null) {
            throw new DuplicateSkillNameException(String.format("Skill name = %s da ton tai", newSkill.getName()));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.handleCreateSkill(newSkill));
    }

    @ApiMessage("Update a skill")
    @PutMapping("/skills")
    public ResponseEntity<Skill> updateSkill(@RequestBody Skill updateSkill) throws DuplicateSkillNameException, IdInvalidException {
        Skill existSkill = skillService.fetchSkillBySkillId(updateSkill.getId());

        if(existSkill != null) {
            if(existSkill.getName().equals(updateSkill.getName())) {
                throw new DuplicateSkillNameException(String.format("Skill name = %s da ton tai", updateSkill.getName()));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(skillService.handleUpdateSkill(updateSkill));
        }
        else {
            throw new IdInvalidException(String.format("Skill voi id = %d khong ton tai", updateSkill.getId()));
        }
    }

    @ApiMessage("Delete a skill")
    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable("id") Long id) throws IdInvalidException {
//        check id
        Skill currentSkill = skillService.fetchSkillBySkillId(id);
        if(currentSkill == null) {
            throw new IdInvalidException("Skill id = " + id + " khong ton tai");
        }
        skillService.handleDeleteSkill(id);
        return  ResponseEntity.ok().body(null);
    }

    @ApiMessage("Fetch skills")
    @GetMapping("/skills")
    public ResponseEntity<ResultPaginationDTO> listSkills(
            @Filter Specification<Skill> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(skillService.fetchAllSkills(spec, pageable));
    }



}
