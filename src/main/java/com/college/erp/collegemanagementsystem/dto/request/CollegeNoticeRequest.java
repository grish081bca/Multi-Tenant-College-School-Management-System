package com.college.erp.collegemanagementsystem.dto.request;

import com.college.erp.collegemanagementsystem.enums.NoticeAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollegeNoticeRequest {
    @NotBlank @Size(max = 160) private String title;
    @NotBlank @Size(max = 8000) private String body;
    @NotNull private NoticeAudience audience;
    @Size(max = 1000) private String description;
}
