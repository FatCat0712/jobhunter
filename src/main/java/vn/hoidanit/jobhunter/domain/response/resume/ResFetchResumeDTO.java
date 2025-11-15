package vn.hoidanit.jobhunter.domain.response.resume;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.jobhunter.constant.ResumeStateEnum;

import java.time.Instant;

@Getter
@Setter
public class ResFetchResumeDTO {
    private Long id;
    private String email;
    private String url;
    private ResumeStateEnum status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private String companyName;

    private ResumeUser user;
    private ResumeJob job;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResumeUser{
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResumeJob {
        private Long id;
        private String name;
    }


}
