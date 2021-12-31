package oikos.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper mapper;

  @Override
  public void commence(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      AuthenticationException e)
      throws IOException, ServletException {
    log.error("Responding with unauthorized error. Message - {}", e.getMessage());
    httpServletResponse.setContentType("application/json");
    httpServletResponse.setStatus(401);
    var error = new ApiError(HttpStatus.UNAUTHORIZED, e.getMessage());
    mapper.writeValue(httpServletResponse.getWriter(), error);
    httpServletResponse.getWriter().close();
  }
}
