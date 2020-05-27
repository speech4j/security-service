package org.speech4j.securityservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.speech4j.securityservice.dto.validation.Existing;
import org.speech4j.securityservice.dto.validation.New;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @Null(groups = {New.class, Existing.class})
    private String id;

    @Size(min = 11, max = 64, groups = {New.class})
    @NotBlank(groups = {New.class})
    @Pattern(regexp = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$",
            groups = {New.class})
    @Null(groups = {Existing.class})
    private String email;

    @Size(min = 6, max = 32, groups = {New.class, Existing.class})
    @NotBlank(groups = {New.class, Existing.class})
    @Pattern(regexp = "^[a-zA-Z0-9]+$", groups = {New.class, Existing.class})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
