package com.movie.api.dto.person;

import com.movie.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel
public class PersonDto extends ABasicAdminDto {
    private String name;
    private String otherName;
    private String avatarPath;
    private String bio;
    private Integer gender;
    private Date dateOfBirth;
    private String country;
    private List<Integer> kinds;
}
