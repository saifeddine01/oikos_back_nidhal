package oikos.app.users;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.FileEmptyException;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.services.ResourceService;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 15/04/2021. */
@Slf4j
@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/user")
@Monitor
public class UserController {
  private final UserService service;
  private final ResourceService resourceService;
  private final ModelMapper modelMapper;

  @GetMapping("/me")
  public UserInfoResponse me(@CurrentUser OikosUserDetails user) {
    return service.getUserInfoByID(user.getUsername());
  }

  @PutMapping("/me")
  public DoneResponse updateMe(
      @Validated @RequestBody UpdateUserRequest dto, @CurrentUser OikosUserDetails user) {
    User me = service.updateMe(dto, user.getUsername());
    return new DoneResponse(
        MessageFormat.format("Changes for user {0} have been made.", me.getId()));
  }

  @PutMapping("/me/picture")
  public UserProfileFileResponse updateMyPicture(
      @RequestParam("file") MultipartFile file, @CurrentUser OikosUserDetails user) {
    if (file == null || file.isEmpty()) {
      throw new FileEmptyException("Please select a valid file for upload");
    }
    var res = service.updatePicture(user.getUsername(), file);
    return modelMapper.map(res, UserProfileFileResponse.class);
  }

  @DeleteMapping("/me/picture")
  public DoneResponse deleteMyPicture(@CurrentUser OikosUserDetails user) {
    service.deleteProfilePicture(user.getUsername());
    return new DoneResponse("Your profile picture has been deleted.");
  }

  @GetMapping("/me/picture")
  public ResponseEntity<Resource> getMyPicture(
      @CurrentUser OikosUserDetails userDetails, HttpServletRequest request) {
    final var resource = service.loadProfilePicture(userDetails.getUsername());
    return resourceService.getResourceResponse(request, resource);
  }

  @GetMapping("/{userID}/picture")
  public ResponseEntity<Resource> getUserPicture(
      @CurrentUser OikosUserDetails userDetails,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String userID,
      HttpServletRequest request) {
    final var resource = service.loadProfilePicture(userID);
    return resourceService.getResourceResponse(request, resource);
  }

  @GetMapping("/{userID}")
  public UserInfoResponse getUserInfo(
      @CurrentUser OikosUserDetails userDetails,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String userID) {
    return service.getUserInfoByID(userID);
  }
}
