package oikos.app.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import oikos.app.common.utils.NanoIDGenerator;
import oikos.app.oikosservices.OikosService;

import javax.persistence.*;
import java.time.Instant;

@Table(name = "service_view")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceView {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, length = NanoIDGenerator.NANOID_SIZE)
    private Instant ts;
    @Column(nullable = false, length = NanoIDGenerator.NANOID_SIZE)
    private String serviceID;
    @Column(nullable = false, length = NanoIDGenerator.NANOID_SIZE)
    private String companyID;
    @Column(nullable = false, length = NanoIDGenerator.NANOID_SIZE)
    private String viewerID;


    public ServiceView(OikosService service, String viewerID) {
        this.serviceID = service.getId();
        this.companyID = service.getServiceCompany().getId();
        this.viewerID = viewerID;
        this.ts = Instant.now();
    }


    @Override
    public String toString() {
        return "ServiceView{" +
               "id=" + id +
               ", ts=" + ts +
               ", serviceID='" + serviceID + '\'' +
               ", companyID='" + companyID + '\'' +
               ", viewerID='" + viewerID + '\'' +
               '}';
    }
}
