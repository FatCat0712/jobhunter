package vn.hoidanit.jobhunter.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.constant.Gender;

import java.time.Instant;

@Getter
@Setter
public class ResUpdateUserDTO {
    private Long id;
    private String name;
    private Gender gender;
    private String address;
    private int age;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;
    private CompanyUser company;

    @Getter
    @Setter
    public static class CompanyUser {
        private Long id;
        private String name;
    }
}
