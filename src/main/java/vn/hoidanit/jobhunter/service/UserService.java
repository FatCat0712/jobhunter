package vn.hoidanit.jobhunter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final RoleService roleService;

    @Autowired
    public UserService(UserRepository userRepository, CompanyService companyService, RoleService roleService) {
        this.userRepository = userRepository;
        this.companyService = companyService;
        this.roleService = roleService;
    }

    public User handleCreateUser(User user) {
//        check company
        if(user.getCompany() != null) {
            Company company = companyService.fetchCompanyById(user.getCompany().getId());
            user.setCompany(company);
        }

//        check role
        if(user.getRole() != null) {
            Role role = roleService.fetchRoleById(user.getRole().getId());
            user.setRole(role);
        }
        return userRepository.save(user);
    }

    public boolean isEmailExist(String email) {
       return userRepository.existsByEmail(email);
    }

    public void handleDeleteUser(Long id)  {
        userRepository.deleteById(id);
    }

    public User fetchUserById(Long id)  {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElse(null);
    }

    public ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable) {
        ResultPaginationDTO rs = new ResultPaginationDTO();
        Page<User> page = userRepository.findAll(spec, pageable);
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        List<ResUserDTO> dtos = page.getContent().stream().map(this::convertToResUserDTO).toList();

        rs.setMeta(meta);
        rs.setResult(dtos);

        return rs;
    }

    public User handleUpdateUser(User updateUser) throws IdInvalidException {
        User currentUser = fetchUserById(updateUser.getId());
        if(currentUser != null) {
            currentUser.setName(updateUser.getName());
            currentUser.setGender(updateUser.getGender());
            currentUser.setAge(updateUser.getAge());
            currentUser.setAddress(updateUser.getAddress());

            if(updateUser.getCompany() != null) {
                Company company = companyService.fetchCompanyById(updateUser.getCompany().getId());
                currentUser.setCompany(company);
            }

            if(updateUser.getRole() != null) {
                Role role = roleService.fetchRoleById(updateUser.getRole().getId());
                currentUser.setRole(role);
            }

        //     update
            currentUser = userRepository.save(currentUser);
        }
        return currentUser;
    }

    public User handleGetUserByUsername(String username) {
            return userRepository.findByEmail(username);
    }

    public void updateUserToken(String token, String email) {
        User currentUser = handleGetUserByUsername(email);
        if(currentUser != null) {
            currentUser.setRefreshToken(token);
            userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return userRepository.findByRefreshTokenAndEmail(token, email);
    }

    public ResCreateUserDTO convertToRestCreateUserDTO(User user) {
        ResCreateUserDTO dto = new ResCreateUserDTO();
        ResCreateUserDTO.CompanyUser company = new ResCreateUserDTO.CompanyUser();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setAge(user.getAge());
        dto.setCreatedAt(user.getCreatedAt());


        if(user.getCompany() != null) {
            company.setId(user.getCompany().getId());
            company.setName(user.getCompany().getName());
            dto.setCompany(company);
        }

        return dto;
    }

    public ResUpdateUserDTO convertToUpdateUserDTO(User user) {
        ResUpdateUserDTO dto = new ResUpdateUserDTO();
        ResUpdateUserDTO.CompanyUser company = new ResUpdateUserDTO.CompanyUser();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setAge(user.getAge());
        dto.setUpdatedAt(user.getUpdatedAt());

        if(user.getCompany() != null) {
            company.setId(user.getCompany().getId());
            company.setName(user.getCompany().getName());
            dto.setCompany(company);
        }

        return dto;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO dto = new ResUserDTO();
        ResUserDTO.CompanyUser company = new ResUserDTO.CompanyUser();
        ResUserDTO.RoleUser role = new ResUserDTO.RoleUser();

        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setAge(user.getAge());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedAt(user.getCreatedAt());

        if(user.getCompany() != null) {
            company.setId(user.getCompany().getId());
            company.setName(user.getCompany().getName());
            dto.setCompany(company);
        }

        if(user.getRole() != null) {
           role.setId(user.getRole().getId());
           role.setName(user.getRole().getName());
            dto.setRole(role);
        }

        return dto;
    }





}
