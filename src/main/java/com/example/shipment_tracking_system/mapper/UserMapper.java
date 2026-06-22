package com.example.shipment_tracking_system.mapper;

import com.example.shipment_tracking_system.dto.request.UserCreateRequest;
import com.example.shipment_tracking_system.dto.response.UserResponse;
import com.example.shipment_tracking_system.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreateRequest request);

    @Mapping(source = "role", target = "role")
    UserResponse toResponse(User user);
}