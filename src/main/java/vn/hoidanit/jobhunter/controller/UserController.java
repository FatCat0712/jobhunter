package vn.hoidanit.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.*;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.DuplicateEmailException;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User newUser) throws DuplicateEmailException {
        boolean isExist = userService.isEmailExist(newUser.getEmail());
        if(isExist) {
            throw new DuplicateEmailException(String.format("Email %s da ton tai, vui long su dung email khac", newUser.getEmail()));
        }
        String hashPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(hashPassword);
        User savedUser = userService.handleCreateUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.convertToRestCreateUserDTO(savedUser));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) throws IdInvalidException {
        User currentUser = userService.fetchUserById(id);
        if(currentUser == null) {
                throw new IdInvalidException(String.format("User voi id = %d khong ton tai", id));
        }
        userService.handleDeleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    fetch user by id
    @GetMapping("/users/{id}")
    @ApiMessage("Fetch user by id")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") Long id) throws IdInvalidException {
        User fetchUser = userService.fetchUserById(id);
        if(fetchUser == null) {
            throw new IdInvalidException(String.format("User voi id = %d khong ton tai", id));
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.convertToResUserDTO(fetchUser));
    }

//    fetch all users
    @GetMapping("/users")
    @ApiMessage("fetch all users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec,
            Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.fetchAllUsers(spec, pageable));
    }

//   update user
    @PutMapping("/users")
    @ApiMessage("Update a user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user) throws IdInvalidException {
        User updatedUser = userService.handleUpdateUser(user);
        if(updatedUser == null) {
            throw new IdInvalidException(String.format("User voi id = %d khong ton tai", user.getId()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(userService.convertToUpdateUserDTO(updatedUser));
    }
















}
