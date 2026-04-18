package com.example.ondas_be.application.mapper;

import com.example.ondas_be.application.dto.response.AuthResponse;
import com.example.ondas_be.application.dto.response.UserSummaryResponse;
import com.example.ondas_be.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    UserSummaryResponse toUserSummaryResponse(User user);

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "user", source = "user")
    AuthResponse toAuthResponse(User user, String accessToken, String refreshToken);
}
