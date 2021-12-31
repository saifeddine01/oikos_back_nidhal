package oikos.app.common.models;

import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Mohamed Haamdi on 02/06/2021.
 */
@Value public class FileInfo {
  String id;
  String newName;
  MultipartFile file;
}
