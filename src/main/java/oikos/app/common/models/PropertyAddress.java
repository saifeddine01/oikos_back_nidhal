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
	public class PropertyAddress implements Serializable {
		//@Max(500)
	  private String street;
		
	  private String zipCode;
		private String city;
		
	  private String stateFull;
	  
	  
}
