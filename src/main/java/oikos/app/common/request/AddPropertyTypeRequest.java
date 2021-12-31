package oikos.app.common.request;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddPropertyTypeRequest {
	@NotBlank
	private int code;
	@NotBlank
	private String name;
}
