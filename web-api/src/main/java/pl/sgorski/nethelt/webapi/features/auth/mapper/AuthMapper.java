package pl.sgorski.nethelt.webapi.features.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.LoginUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.RegisterUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.LoginRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.RegisterUserRequest;
import pl.sgorski.nethelt.webapi.features.auth.oauth.OAuthUserInfo;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;

@Mapper(componentModel = "spring")
public interface AuthMapper {

  RegisterUserCommand toCommand(RegisterUserRequest request);

  LoginUserCommand toCommand(LoginRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "identities", ignore = true)
  @Mapping(target = "authorities", ignore = true)
  User toEntity(RegisterUserCommand command);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  UserIdentity toIdentity(OAuthUserInfo userInfo);
}
