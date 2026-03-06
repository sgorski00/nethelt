package pl.sgorski.nethelt.webapi.features.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.RegisterUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.RegisterUserRequest;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    RegisterUserCommand toCommand(RegisterUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(RegisterUserCommand command);
}
