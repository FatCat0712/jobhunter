package vn.hoidanit.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.RoleService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @ApiMessage("Create a role")
    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(@Valid @RequestBody Role newRole) throws IdInvalidException {
        boolean isExists = roleService.existsByName(newRole.getName());
        if(isExists) {
            throw new IdInvalidException(String.format("Role with name = %s da ton tai", newRole.getName()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.handleCreateRole(newRole));
    }

    @ApiMessage("Update a role")
    @PutMapping("/roles")
    public ResponseEntity<Role> updateRole(@Valid @RequestBody Role updateRole) throws IdInvalidException {
        Role roleInDB = roleService.fetchRoleById(updateRole.getId());
        if(roleInDB == null) {
            throw new IdInvalidException(String.format("Role with id = %sd khong ton tai", updateRole.getId()));
        }
        boolean isExists = roleService.existsByName(updateRole.getName());
        if(isExists) {
            throw new IdInvalidException(String.format("Role with name = %s da ton tai",updateRole.getName()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(roleService.handleUpdateRole(roleInDB, updateRole));
    }

    @ApiMessage("Fetch role by id")
    @GetMapping("/roles/{id}")
    public ResponseEntity<Role> getById(@PathVariable("id") Long id) throws IdInvalidException {
        Role role = roleService.fetchRoleById(id);
        if(role == null) {
            throw new IdInvalidException(String.format("Resume voi id = %d khong ton tai", id));
        }
        return ResponseEntity.ok().body(role);
    }

    @ApiMessage("Fetch roles with pagination")
    @GetMapping("/roles")
    public ResponseEntity<ResultPaginationDTO> fetchRolesByPage(
            @Filter Specification<Role> specs,
            Pageable pageable
    )
    {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.fetchRoleByPage(specs, pageable));
    }

    @ApiMessage("Delete a role by id")
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> updatePermission(@PathVariable(name = "id") Long id) throws IdInvalidException {
        Role roleInDB = roleService.fetchRoleById(id);
        if (roleInDB == null) {
            throw new IdInvalidException(String.format("Role with id = %d khong ton tai" , id));
        }
       roleService.handleDeleteRole(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }






}
