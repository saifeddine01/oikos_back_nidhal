package oikos.app.common.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.InternalServerError;
import oikos.app.common.models.FileInfo;
import oikos.app.common.utils.NanoIDGenerator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@AllArgsConstructor
@Slf4j
public class FileService {
  public FileInfo uploadFile(MultipartFile file, Path directory) {
    // Normalize file name
    final var originalName = StringUtils.cleanPath(file.getOriginalFilename());
    final var extension =
      FilenameUtils.getExtension(file.getOriginalFilename());
    final var id = NanoIDGenerator.generateID();
    final var newName = id + "." + extension;
    try {
      // Check if the file's name contains invalid characters
      if (originalName.contains("..")) {
        throw new BaseException(
          "Sorry! Filename contains invalid path sequence " + originalName);
      }
      // Copy file to the target location (Replacing existing file with the same name)
      Path targetLocation = directory.resolve(newName);
      Files.copy(file.getInputStream(), targetLocation,
        StandardCopyOption.REPLACE_EXISTING);
      return new FileInfo(id, newName, file);
    } catch (IOException ex) {
      log.error("fileservice.uploadfile",ex);
      throw new InternalServerError(
        "Could not store file " + originalName + ". Please try again!", ex);
    }
  }

  public void deleteFile(Path directory, String fileName) {
    try {
      Files.deleteIfExists(directory.resolve(fileName));
    } catch (IOException e) {
      log.error("fileservice.deletefile",e);
      throw new InternalServerError(
        "fileservice.deletefile can't delete old picture", e);
    }
  }

  public Resource loadResource(Path directory, String filename) {
    try {

      final var filePath = directory.resolve(filename).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        log.error("fileservice.loadresource, resource doesn't exist");
        throw new InternalServerError("userservice.loadPicture");
      }
    } catch (MalformedURLException ex) {
      log.error("fileservice.loadresource",ex);
      throw new InternalServerError("userservice.loadPicture", ex);
    }
  }
}
