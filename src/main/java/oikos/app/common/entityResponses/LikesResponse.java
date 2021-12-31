package oikos.app.common.entityResponses;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import oikos.app.common.models.Likes;

@Data
@Builder
@Getter
@Setter
public class LikesResponse {
	private String id;
	private String userId;
	private String propertyId;
	private Likes status;
	private Instant createdAt;
	private Instant updatedAt;
}
