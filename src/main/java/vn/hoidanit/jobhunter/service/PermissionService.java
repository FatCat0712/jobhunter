package vn.hoidanit.jobhunter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.PermissionRepository;

import java.util.Optional;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean isPermissionExist(Permission permission) {
        return permissionRepository.existsByApiPathAndMethodAndModule(
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule()
        );
    }

    public Permission fetchPermissionById(Long permissionId) {
        Optional<Permission> permissionInDB =  permissionRepository.findById(permissionId);
        return permissionInDB.orElse(null);
    }

    public Permission handleCreatePermission(Permission newPermission) {
        return permissionRepository.save(newPermission);
    }

    public Permission handleUpdatePermission(Permission updatePermission) {
        Permission permissionInDB = fetchPermissionById(updatePermission.getId());
        permissionInDB.setApiPath(updatePermission.getApiPath());
        permissionInDB.setMethod(updatePermission.getMethod());
        permissionInDB.setModule(updatePermission.getModule());
        return permissionRepository.save(permissionInDB);
    }

    public void handleDeletePermission(Long permissionId) {
        Permission permissionInDB = fetchPermissionById(permissionId);

        if (permissionInDB.getRoles() != null) {
            permissionInDB.getRoles().forEach(role -> role.getPermissions().remove(permissionInDB));
            permissionRepository.delete(permissionInDB);
        }
    }

    public boolean isSameName(Permission p) {
        Permission permission = fetchPermissionById(p.getId());
        return permission.getName().equals(p.getName());
    }

    public ResultPaginationDTO fetchPermissionsByPage(Specification<Permission> specs, Pageable pageable) {
        Page<Permission> page = permissionRepository.findAll(specs, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(page.getNumber() + 1);
        mt.setPages(page.getTotalPages());
        mt.setPageSize(page.getSize());
        mt.setTotal(page.getTotalPages());

        rs.setMeta(mt);
        rs.setResult(page.getContent());

        return rs;
    }


}
