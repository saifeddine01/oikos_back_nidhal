package oikos.app.common.models;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import oikos.app.users.User;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@Builder
@Getter(value = AccessLevel.PUBLIC)
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "Disponibilities")
public class Disponibility implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4868658101587613079L;

	@Id
	@GeneratedValue(generator = "nano-generator")
	@GenericGenerator(name = "nano-generator", strategy = "oikos.app.common.utils.NanoIDGenerator")
	private String id;

	@Column(name = "dateStart")
	private LocalDateTime dateStart;

	@Column(name = "dateEnd")
	private LocalDateTime dateEnd;
	@Builder.Default
	private boolean isAllDay = false;

	@JsonIgnore
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "users_id", referencedColumnName = "id")
	private User userDispo;

	private String title;
	private String description;
	private DisponibilityType dispotype;
	private String userId;
	
}
