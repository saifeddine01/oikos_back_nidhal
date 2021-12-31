package oikos.app.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.configurations.AppProperties;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.exceptions.InternalServerError;
import oikos.app.common.services.FileService;
import oikos.app.security.SecurityService;
import oikos.app.security.exceptions.UserAlreadyExistException;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Created by Mohamed Haamdi on 08/02/2021 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
  private final ModelMapper mapper;
  private final UserRepo userRepo;
  private final SecurityService securityService;
  private final UserProfileFileRepo fileRepository;
  private final FileService fileService;
  private final AppProperties appProperties;
  private Path userDir;

  @CacheEvict(value = "users", key = "#userID")
  @Transactional
  public User updateMe(UpdateUserRequest dto, String userID) {
    log.info("Updating the info for user {}", userID);
    var user = userRepo.getOne(userID);
    if (!user.getPhoneNumber().equals(dto.getPhoneNumber())
        && userRepo.existsByPhoneNumber(dto.getPhoneNumber()))
      throw new UserAlreadyExistException(
          String.format("Phone Number already in use : %s", dto.getPhoneNumber()));
    if (!user.getEmail().equals(dto.getEmail()) && userRepo.existsByEmailIgnoreCase(dto.getEmail()))
      throw new UserAlreadyExistException(
          String.format("Email address already in use : %s", dto.getEmail()));
    mapper.map(dto, user);
    user = securityService.doUpdatePassword(dto, user);

    return userRepo.save(user);
  }

  @Transactional
  public Page<UserInfoResponse> getAllSellers(Pageable paging) {
    log.info("Generation user info all sellers page{}", paging.getPageNumber());
    var data = userRepo.getAllSellers(paging);
    return data.map(userInfo -> mapper.map(userInfo, UserInfoResponse.class));
  }

  @Cacheable(value = "users", key = "#userID", unless = "#result == null")
  @Transactional
  public UserInfoResponse getUserInfoByID(String userID) {
    log.info("Generation user info for {}", userID);
    var user =
        userRepo
            .getUserInfoById(userID)
            .orElseThrow(() -> new EntityNotFoundException(User.class, userID));
    return mapper.map(user, UserInfoResponse.class);
  }

  @CacheEvict(
      value = {"users", "usersPictures"},
      key = "#userID")
  @Transactional
  public UserProfileFile updatePicture(String userID, MultipartFile file) {
    log.info("Updating profile picture of user {}", userID);
    doDeletePicture(userID);
    var fileInfo = fileService.uploadFile(file, userDir);
    var u = userRepo.getOne(userID);
    var dbFile =
        UserProfileFile.builder()
            .id(fileInfo.getId())
            .fileName(fileInfo.getNewName())
            .fileType(fileInfo.getFile().getContentType())
            .originalName(fileInfo.getFile().getOriginalFilename())
            .size(fileInfo.getFile().getSize())
            .user(u)
            .build();
    dbFile = fileRepository.save(dbFile);
    u.setUserProfileFile(dbFile);
    userRepo.save(u);
    return dbFile;
  }

  @CacheEvict(
      value = {"users", "usersPictures"},
      key = "#userID")
  @Transactional
  public void deleteProfilePicture(String userID) {
    log.info("Deleting profile picture of user {}", userID);
    doDeletePicture(userID);
    final var user = userRepo.getOne(userID);
    user.setUserProfileFile(null);
    userRepo.save(user);
  }

  @Cacheable(value = "usersPictures", key = "#userID", unless = "#result == null")
  public Resource loadProfilePicture(String userID) {
    log.info("Getting profile picture of user{}", userID);
    var dbFile =
        fileRepository
            .getProfilePictureForUser(userID)
            .orElseThrow(() -> new EntityNotFoundException(UserProfileFile.class, userID));
    return fileService.loadResource(userDir, dbFile.getFileName());
  }

  private void doDeletePicture(String userID) {
    var oldPic = fileRepository.getProfilePictureForUser(userID);
    if (oldPic.isPresent()) {
      fileRepository.delete(oldPic.get());
      fileService.deleteFile(userDir, oldPic.get().getFileName());
    }
  }

  @PostConstruct
  private void initUserDir() {
    this.userDir = Paths.get(appProperties.getFiles().getUsers()).toAbsolutePath().normalize();
    try {
      if (!Files.exists(userDir)) {
        Files.createDirectory(userDir);
      }
    } catch (Exception exception) {
      log.error("userservice.inituserdir", exception);
      throw new InternalServerError("Error creating user profile directory", exception);
    }
  }
}
