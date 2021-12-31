package oikos.app.appointementfeedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oikos.app.common.models.Appointment;
import oikos.app.common.models.BaseEntity;
import oikos.app.common.models.BienVendre;
import oikos.app.users.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/** Created by Mohamed Haamdi on 06/05/2021. */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(
    indexes = {
      @Index(name = "IDX_FEEDBACK_reviewer_id", columnList = "reviewer_id"),
      @Index(name = "IDX_FEEDBACK_property_id", columnList = "property_id"),
      @Index(name = "IDX_FEEDBACK_appointment_id", columnList = "appointment_id")
    })
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AppointmentFeedback extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  private User reviewer;

  @OneToOne(fetch = FetchType.LAZY)
  private Appointment appointment;

  @OneToOne(fetch = FetchType.LAZY)
  private BienVendre property;

  @Enumerated(EnumType.STRING)
  private Intrest intrest;

  private String opinion;
  private String promisePoints;
  private String priceOpinion;
}
