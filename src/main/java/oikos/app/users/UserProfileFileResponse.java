package oikos.app.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Mohamed Haamdi on 02/06/2021.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileFileResponse {
  String fileName;
  String fileType;
  String originalFileName;
  long size;
}
