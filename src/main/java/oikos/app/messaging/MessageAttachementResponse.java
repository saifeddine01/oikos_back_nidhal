package oikos.app.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Mohamed Haamdi on 04/06/2021.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageAttachementResponse {
  String fileName;
  String fileType;
  String originalFileName;
  long size;
}
