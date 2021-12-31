package oikos.app.common.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Value;
import oikos.app.common.models.Likes;

@Builder
@Value
public class LikesRequest {
	@NotBlank
	String propId;
	@NotNull
	Likes like;
}
