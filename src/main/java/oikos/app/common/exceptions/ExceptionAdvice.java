package oikos.app.common.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice class ExceptionAdvice {

  @ResponseBody @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND) ApiError entityNotFoundHandler(
    EntityNotFoundException ex) {
    return new ApiError(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ResponseBody @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN) ApiError authenticationExceptionHandler(
    AuthenticationException ex) {
    return new ApiError(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ResponseBody @ExceptionHandler(UsernameNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND) ApiError usernameNotFoundHandler(
    UsernameNotFoundException ex) {
    return new ApiError(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(value = {MissingServletRequestParameterException.class,
    MultipartException.class, MissingServletRequestPartException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiError missingParameterNotFoundHandler(Exception ex) {
    return new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ResponseBody @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN) ApiError accessDeniedHandler(
    AccessDeniedException ex) {
    return new ApiError(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ResponseBody @ExceptionHandler(InternalServerError.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ApiError internalServerErrorHandler(InternalServerError ex) {
    return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  @ResponseBody @ExceptionHandler(BaseException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST) ApiError baseExceptionHandler(
    BaseException ex) {
    return new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST) @ResponseBody
  public ApiError handleMethodArgumentNotValidException(
    MethodArgumentNotValidException ex) {
    ApiError error = new ApiError(HttpStatus.BAD_REQUEST,
      "Validation Error." + " Check the subError List for more information");
    error.addValidationErrors(ex.getBindingResult().getFieldErrors());
    return error;
  }

  @ExceptionHandler(InvalidFormatException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST) @ResponseBody
  public ApiError handleHttpMessageNotReadableException(
    InvalidFormatException ex) {
    return new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST) @ResponseBody
  public ApiError handleConstraintViolationException(
    ConstraintViolationException ex) {
    ApiError error = new ApiError(HttpStatus.BAD_REQUEST,
      "Validation Error." + " Check the subError List for more information");
    error.addValidationErrors(ex.getConstraintViolations());
    return error;
  }
}
