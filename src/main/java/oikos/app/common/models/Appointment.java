package oikos.app.common.models;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@SuperBuilder
@Getter(value = AccessLevel.PUBLIC)
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "Appointment")
public class Appointment implements Serializable {
	@Id
	@GeneratedValue(generator = "nano-generator")
	@GenericGenerator(name = "nano-generator", strategy = "oikos.app.common.utils.NanoIDGenerator")
	private String id;

	private String title;

	private String description;

	@Column(name = "dateStart")
	private LocalDateTime dateStart;

	@Column(name = "dateEnd")
	private LocalDateTime dateEnd;

	@Builder.Default
	private Status status = Status.Pending;
	// TODO add the other user.
	private String appTaker;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "dispo_id", referencedColumnName = "id")
	private Disponibility disponibility;
}
