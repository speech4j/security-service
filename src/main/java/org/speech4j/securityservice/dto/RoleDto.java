package org.speech4j.securityservice.dto;

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
public class RoleDto {
    @Null(groups = {New.class, Existing.class})
    private int id;
    @Size(min = 4, max = 10, groups = {New.class, Existing.class})
    @Pattern(regexp = "^[a-zA-Z0-9]+$",
            groups = {New.class, Existing.class})
    @NotBlank(groups = {Existing.class})
    private String name;
}
