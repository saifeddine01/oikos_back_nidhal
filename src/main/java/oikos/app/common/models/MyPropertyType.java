package oikos.app.common.models;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter(value = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@ToString
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MyPropertyType implements Serializable {
	@Id
	@GeneratedValue(generator = "nano-generator")
	@GenericGenerator(name = "nano-generator", strategy = "oikos.app.common.utils.NanoIDGenerator")
	private String id;
	
	private int code;
	private String name;

}
