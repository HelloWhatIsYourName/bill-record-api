package com.hwiyn.billrecord.mapper;

import com.hwiyn.billrecord.dto.user.UserResponse;
import com.hwiyn.billrecord.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
