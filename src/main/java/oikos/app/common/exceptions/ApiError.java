package oikos.app.common.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Thanks to Bruno Cleite */
@Data
@JsonTypeInfo(
    include = JsonTypeInfo.As.WRAPPER_OBJECT,
    use = JsonTypeInfo.Id.CUSTOM,
    property = "error",
    visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class ApiError {

  private HttpStatus status;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private LocalDateTime timestamp;

  private String message;
  private List<ApiSubError> subErrors;

  private ApiError() {
    this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    this.message = "Unexpected error";
    this.timestamp = LocalDateTime.now();
  }

  public ApiError(HttpStatus status, String message) {
    this();
    this.status = status;
    this.message = message;
  }

  ApiError(HttpStatus status) {
    this();
    this.status = status;
    this.message = "Unexpected error";
  }

  ApiError(String message) {
    this();
    this.status = HttpStatus.BAD_REQUEST;
    this.message = message;
  }

  void addValidationErrors(List<FieldError> fieldErrors) {
    fieldErrors.forEach(this::addValidationError);
  }

  void addValidationError(List<ObjectError> globalErrors) {
    globalErrors.forEach(this::addValidationError);
  }

  void addValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
    constraintViolations.forEach(this::addValidationError);
  }

  interface ApiSubError {}

  @Data
  @EqualsAndHashCode(callSuper = false)
  @AllArgsConstructor
  static class ApiValidationError implements ApiSubError {
    private String object;
    private String field;
    private Object rejectedValue;
    private String message;

    ApiValidationError(String object, String message) {
      this.object = object;
      this.message = message;
    }
  }

  private void addSubError(ApiSubError subError) {
    if (subErrors == null) {
      subErrors = new ArrayList<>();
    }
    subErrors.add(subError);
  }

  private void addValidationError(
      String object, String field, Object rejectedValue, String message) {
    addSubError(new ApiValidationError(object, field, rejectedValue, message));
  }

  private void addValidationError(String object, String message) {
    addSubError(new ApiValidationError(object, message));
  }

  private void addValidationError(FieldError fieldError) {
    this.addValidationError(
        fieldError.getObjectName(),
        fieldError.getField(),
        fieldError.getRejectedValue(),
        fieldError.getDefaultMessage());
  }

  private void addValidationError(ObjectError objectError) {
    this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
  }

  /**
   * Utility method for adding error of ConstraintViolation. Usually when a @Validated validation
   * fails.
   *
   * @param cv the ConstraintViolation
   */
  private void addValidationError(ConstraintViolation<?> cv) {
    this.addValidationError(
        cv.getRootBeanClass().getSimpleName(),
        cv.getPropertyPath().toString(),
        cv.getInvalidValue(),
        cv.getMessage());
  }
}

class LowerCaseClassNameResolver extends TypeIdResolverBase {

  @Override
  public String idFromValue(Object value) {
    return value.getClass().getSimpleName().toLowerCase();
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    return idFromValue(value);
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.CUSTOM;
  }
}
