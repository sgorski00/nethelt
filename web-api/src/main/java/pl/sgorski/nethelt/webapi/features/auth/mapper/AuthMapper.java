package pl.sgorski.nethelt.webapi.features.auth.mapper;

import org.mapstruct.Mapper;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.LoginUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.RegisterUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.LoginRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.RegisterUserRequest;

@Mapper(componentModel = "spring")
public interface AuthMapper {

  RegisterUserCommand toCommand(RegisterUserRequest request);

  LoginUserCommand toCommand(LoginRequest request);
}
