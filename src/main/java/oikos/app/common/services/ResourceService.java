package oikos.app.common.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Mohamed Haamdi on 02/06/2021.
 */
@Service
@AllArgsConstructor
@Slf4j
public class ResourceService {
  public ResponseEntity<Resource> getResourceResponse(
    HttpServletRequest request, Resource resource) {
    String contentType = null;
    try {
      contentType = request.getServletContext()
        .getMimeType(resource.getFile().getAbsolutePath());
    } catch (IOException ex) {
      log.info("Could not determine file type.");
    }
    // Fallback to the default content type if type could not be determined
    if (contentType == null) {
      contentType = "application/octet-stream";
    }
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(contentType))
      .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + resource.getFilename() + "\"")
      .body(resource);
  }
}
