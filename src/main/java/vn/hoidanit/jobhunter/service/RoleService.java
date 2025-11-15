package vn.hoidanit.jobhunter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.PermissionRepository;
import vn.hoidanit.jobhunter.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }



    public Role fetchRoleById(Long id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.orElse(null);
    }

    public Role handleCreateRole(Role newRole) {
        if(newRole.getPermissions() != null) {
            List<Long> permIds = newRole.getPermissions().stream().map(Permission::getId).toList();
            List<Permission> permissions = permissionRepository.findByIdIn(permIds);
            newRole.setPermissions(permissions);
        }
        return roleRepository.save(newRole);
    }

    public Role handleUpdateRole(Role roleInDB, Role updateRole) {
        if(updateRole.getPermissions() != null) {
            List<Long> permIds = updateRole.getPermissions().stream().map(Permission::getId).toList();
            List<Permission> permissions = permissionRepository.findByIdIn(permIds);
            roleInDB.setPermissions(permissions);
        }
        roleInDB.setName(updateRole.getName());
        roleInDB.setDescription(updateRole.getDescription());
        roleInDB.setActive(updateRole.isActive());

        return roleRepository.save(roleInDB);
    }

    public void handleDeleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }



    public ResultPaginationDTO fetchRoleByPage(Specification<Role> spec, Pageable pageable) {
        Page<Role> page = roleRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(page.getNumber());
        mt.setPages(page.getTotalPages());
        mt.setPageSize(page.getSize());
        mt.setTotal(page.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(page.getContent());

        return rs;

    }
}
