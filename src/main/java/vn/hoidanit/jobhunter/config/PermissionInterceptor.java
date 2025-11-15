package vn.hoidanit.jobhunter.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import vn.hoidanit.jobhunter.domain.Permission;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.error.PermissionException;

import java.util.List;

public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;


    @Override
    @Transactional
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = (String)request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

//        check permission
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        if(!email.isEmpty()) {
            User user = userService.handleGetUserByUsername(email);
            if(user != null) {
                Role role = user.getRole();
                if(role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream().anyMatch(perm -> perm.getApiPath().equals(path) && perm.getMethod().equals(httpMethod));
                    if(!isAllow) {
                        throw new PermissionException("Ban khong co quyen truy cap endpoint nay.");
                    }
                }
                else {
                    throw new PermissionException("Ban khong co quyen truy cap endpoint nay.");
                }
            }
        }

        return true;
    }
}
