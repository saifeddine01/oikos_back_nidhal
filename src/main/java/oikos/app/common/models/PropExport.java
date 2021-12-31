package oikos.app.common.models;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PropExport implements Serializable {
	//@NotNull
	private Boolean isNord;
	//@NotNull
	private Boolean isSud;
	//@NotNull
	private Boolean isEst;
	//@NotNull
	private Boolean isOuest;
}
