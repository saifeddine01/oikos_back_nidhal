package oikos.app.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import oikos.app.common.models.BienVendre;
import oikos.app.common.utils.NanoIDGenerator;

import javax.persistence.*;
import java.time.Instant;

@Table(name = "property_view")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyView {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private Instant ts;
    @Column(nullable = false, length = NanoIDGenerator.NANOID_SIZE)
    private String propID;
    @Column(nullable = false, length = NanoIDGenerator.NANOID_SIZE)
    private String ownerID;
    @Column(nullable = false, length = NanoIDGenerator.NANOID_SIZE)
    private String viewerID;


    public PropertyView(BienVendre prop, String viewerID) {
        this.propID = prop.getId();
        this.ownerID = prop.getUserId().getId();
        this.viewerID = viewerID;
        this.ts = Instant.now();
    }

    @Override
    public String toString() {
        return "PropertyView{" +
               "id=" + id +
               ", ts=" + ts +
               ", propID='" + propID + '\'' +
               ", ownerID='" + ownerID + '\'' +
               ", viewerID='" + viewerID + '\'' +
               '}';
    }
}