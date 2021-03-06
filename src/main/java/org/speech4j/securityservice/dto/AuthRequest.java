package org.speech4j.securityservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.speech4j.securityservice.dto.validation.Existing;
import org.speech4j.securityservice.dto.validation.New;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @Size(min = 6, max = 32, groups = {New.class, Existing.class})
    @NotBlank(groups = {New.class, Existing.class})
    private String username;

    @Size(min = 6, max = 32, groups = {New.class, Existing.class})
    @NotBlank(groups = {New.class, Existing.class})
    @Pattern(regexp = "^[a-zA-Z0-9]+$", groups = {New.class, Existing.class})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
