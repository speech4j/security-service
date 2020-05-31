package org.speech4j.securityservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.speech4j.securityservice.dto.validation.Existing;
import org.speech4j.securityservice.dto.validation.New;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto implements UserDetails {
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
