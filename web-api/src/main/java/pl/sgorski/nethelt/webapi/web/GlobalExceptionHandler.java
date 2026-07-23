package pl.sgorski.nethelt.webapi.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.sgorski.nethelt.webapi.exception.application.AlreadyExistsException;
import pl.sgorski.nethelt.webapi.exception.application.NotAllowedException;
import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;
import pl.sgorski.nethelt.webapi.exception.application.ValidationFailedException;

@Slf4j
@RestControllerAdvice
public final class GlobalExceptionHandler {

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setTitle("Unauthorized");
    problem.setDetail(ex.getMessage());
    return problem;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problem.setTitle("Access denied");
    problem.setDetail(ex.getMessage());
    return problem;
  }

  @ExceptionHandler(AlreadyExistsException.class)
  public ProblemDetail handleAlreadyExistsException(AlreadyExistsException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Conflict");
    problem.setDetail(ex.getMessage());
    return problem;
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFoundException(NotFoundException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Not Found");
    problem.setDetail(ex.getMessage());
    return problem;
  }

  @ExceptionHandler(NotAllowedException.class)
  public ProblemDetail handleNotAllowedException(NotAllowedException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Operation Not Allowed");
    problem.setDetail(ex.getMessage());
    return problem;
  }

  @ExceptionHandler(ValidationFailedException.class)
  public ProblemDetail handleValidationFailedException(ValidationFailedException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    problem.setTitle("Validation Failed");
    problem.setDetail(ex.getMessage());
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    problem.setTitle("Validation Failed");
    problem.setDetail(getValidationFailedMessage(ex));
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception ex) {
    var problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setDetail("Something went wrong. Please try again later or contact support.");
    log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
    return problemDetail;
  }

  private String getValidationFailedMessage(MethodArgumentNotValidException ex) {
    return ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(FieldError::getDefaultMessage)
        .orElse("Validation failed");
  }
}
