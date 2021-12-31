package oikos.app.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Facet;
import org.hibernate.search.annotations.Field;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Setter
@ToString
@RequiredArgsConstructor
@Embeddable
@Builder
@AllArgsConstructor
@Getter
public class PropertyAddress implements Serializable {
  // @Max(500)
  private String street;

  private String zipCode;

  @Field(name = "city")
  private String city;

  @Field(analyze = Analyze.NO, name = "stateFull")
  @Facet(name = "stateFull", forField = "stateFull")
  @Field(name = "state")
  private String stateFull;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    final PropertyAddress that = (PropertyAddress) o;

    if (!Objects.equals(street, that.street)) return false;
    if (!Objects.equals(zipCode, that.zipCode)) return false;
    if (!Objects.equals(city, that.city)) return false;
    return Objects.equals(stateFull, that.stateFull);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(street);
    result = 31 * result + (Objects.hashCode(zipCode));
    result = 31 * result + (Objects.hashCode(city));
    result = 31 * result + (Objects.hashCode(stateFull));
    return result;
  }
}
