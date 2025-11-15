package vn.hoidanit.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.PermissionService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {
    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @ApiMessage("Create a permission")
    @PostMapping("/permissions")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission newPermission) throws IdInvalidException {
        boolean isExist = permissionService.isPermissionExist(newPermission);
        if(isExist) {
            throw new IdInvalidException("Permission da ton tai");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.handleCreatePermission(newPermission));
    }

    @ApiMessage("Update a message")
    @PutMapping("/permissions")
    public ResponseEntity<Permission> updatePermission(@Valid @RequestBody Permission updatePermission)  throws IdInvalidException {
        Permission permission = permissionService.fetchPermissionById(updatePermission.getId());
        if (permission == null) {
            throw new IdInvalidException(String.format("Permission with id = %d khong ton tai" , updatePermission.getId()));
        }

        boolean isExist = permissionService.isPermissionExist(updatePermission);
        if(isExist) {
//            check name
            if(permissionService.isSameName(updatePermission)) {
                throw new IdInvalidException("Permission da ton tai");
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(permissionService.handleUpdatePermission(updatePermission));
    }

    @ApiMessage("Fetch permissions by page")
    @GetMapping("/permissions")
    public ResponseEntity<ResultPaginationDTO> updatePermission(
            @Filter Specification<Permission> specs,
            Pageable pageable
    )
    {
        return ResponseEntity.status(HttpStatus.OK).body(permissionService.fetchPermissionsByPage(specs, pageable));
    }

    @ApiMessage("Delete permission by id")
    @DeleteMapping("/permissions/{id}")
    public ResponseEntity<Void> updatePermission(@PathVariable(name = "id") Long id) throws IdInvalidException {
        Permission permission = permissionService.fetchPermissionById(id);
        if (permission == null) {
            throw new IdInvalidException(String.format("Permission with id = %d khong ton tai" , id));
        }
        permissionService.handleDeletePermission(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
