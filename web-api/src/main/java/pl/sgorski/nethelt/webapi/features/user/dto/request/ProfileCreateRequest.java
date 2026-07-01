package pl.sgorski.nethelt.webapi.features.user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ProfileCreateRequest(
    @NotNull @Size(min = 3, max = 100) String username,
    String firstName,
    String lastName,
    @Past LocalDate birthDate,
    @Size(max = 255) String bio) {}
