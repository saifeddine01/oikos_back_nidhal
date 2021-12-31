package oikos.app.common.entityResponses;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oikos.app.common.models.Status;


@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Data
@NoArgsConstructor
public class AppointmentResponse {
	 String id;

	 String title;

	 String description;
	 LocalDateTime dateStart;

	 LocalDateTime dateEnd;
	 Status status;
	 String appTaker;
	 String dispoId;
	///private String dispoId;
}
