package oikos.app.common.entityResponses;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import oikos.app.common.models.DisponibilityType;

@Data
@Builder
public class DispoResponse {
	private String id;
	private LocalDateTime dateStart;
	private LocalDateTime dateEnd;
	private String title;
	private String description;
	private boolean allDay;
	private String userId;
	private DisponibilityType dispotype;
}
