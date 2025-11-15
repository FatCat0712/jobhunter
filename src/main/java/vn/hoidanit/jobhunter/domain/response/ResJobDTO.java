package vn.hoidanit.jobhunter.domain.response;

import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.constant.LevelEnum;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResJobDTO {
    private Long id;
    private String name;
    private String location;
    private double salary;
    private int quantity;
    private LevelEnum level;
    private Instant startDate;
    private Instant endDate;

    private List<String> skills;
    private Instant createdAt;
    private String createdBy;

    private boolean active;

}
