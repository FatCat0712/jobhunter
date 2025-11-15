package vn.hoidanit.jobhunter.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.ReqLoginDTO;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResLoginDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    @Autowired
    public AuthController(
            AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
            UserService userService, PasswordEncoder passwordEncoder
    ) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDto) {
//        Nap input gom username/password vao security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(reqLoginDto.getUsername(), reqLoginDto.getPassword());

//        Xac thuc nguoi dung => can viet ham loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

//        set thong tin nguoi dung dang nhap vao context ( co the su dung sau nay)
        SecurityContextHolder.getContext().setAuthentication(authentication);

//        issue new token/set refresh token as cookies
        ResLoginDTO resLoginDTO = generateResponseDTO(authentication.getName());
        ResponseCookie resCookies =  generateCookies(authentication.getName());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(resLoginDTO);
    }

    @ApiMessage("Register a new user")
    @PostMapping("/auth/register")
    public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody User newUser) throws IdInvalidException {
        boolean isEmailExist = userService.isEmailExist(newUser.getEmail());
        if(isEmailExist) {
            throw new IdInvalidException("Email" + newUser.getEmail() + "da ton tai, vui long su dung email");
        }
        String hashPassword =  passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(hashPassword);
        User savedUser = userService.handleCreateUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.convertToRestCreateUserDTO(savedUser));
    }

    @ApiMessage("fetch account")
    @GetMapping("/auth/account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        User currentUserDB = userService.handleGetUserByUsername(email);

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = resLoginDTO.new UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        if(currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(email);
            userLogin.setName(currentUserDB.getName());
            userLogin.setRole(currentUserDB.getRole());
            userGetAccount.setUser(userLogin);
        }

        return ResponseEntity.ok(userGetAccount);
    }

    @ApiMessage("Get User by refresh token")
    @GetMapping("/auth/refresh")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token") String refresh_token
    ) throws IdInvalidException {
//        check valid
        Jwt decodedToken = securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();

//        check user by token + email
            User currentUser = userService.getUserByRefreshTokenAndEmail(refresh_token, email);
            if(currentUser == null) {
                throw new IdInvalidException("Refresh token khong hop le");
            }

//        issue new token/set refresh token as cookies
        ResLoginDTO resLoginDTO = generateResponseDTO(email);
        ResponseCookie resCookies =  generateCookies(email);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(resLoginDTO);
    }

    @ApiMessage("Logout User")
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        if(email.isEmpty()) {
            throw new IdInvalidException("Access token khong hop le");
        }

//       update refresh_token = null
        userService.updateUserToken(null, email);

//        remove refresh token in cookies
        ResponseCookie deleteCookie = ResponseCookie
                .from("refresh_token", null)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    private ResLoginDTO generateResponseDTO(String email) {
        //        create access token
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        User currentUserDB = userService.handleGetUserByUsername(email);

        if(currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = resLoginDTO.new UserLogin(currentUserDB.getId(), currentUserDB.getEmail(), currentUserDB.getName(), currentUserDB.getRole());
            resLoginDTO.setUser(userLogin);
        }

        String access_token = securityUtil.createAccessToken(email, resLoginDTO);
        resLoginDTO.setAccessToken(access_token);

        return resLoginDTO;
    }

    public ResponseCookie generateCookies(String email) {
        ResLoginDTO resLoginDTO = generateResponseDTO(email);

//      create refresh token
        String newRefreshToken = securityUtil.createRefreshToken(email, resLoginDTO);

//        update user
        userService.updateUserToken(newRefreshToken, email);

//        set cookie

        return ResponseCookie
                .from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
    }

}
