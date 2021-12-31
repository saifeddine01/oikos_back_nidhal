package oikos.app.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import oikos.app.common.models.BienVendre;
import oikos.app.common.models.Location;
import oikos.app.common.models.PropExport;
import oikos.app.common.models.PropertyAddress;
import oikos.app.common.models.PropertyVue;
import oikos.app.notifications.NotificationRepo;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.users.User;
import oikos.app.users.UserRepo;

@DataJpaTest
public class PropertyRepo {
	private final Pageable paging = PageRequest.of(0, 10);
	@Autowired
	private NotificationRepo underTest;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private BienaVendreRepo bien;

	@Test
	void testBien() {
		final var userID = new User("userID");
		userRepo.save(userID);

		BienVendre bb = BienVendre.builder().allArea(41).hasDependancy(false).hasPlannedWork(false).status(null)
				.description("description")
				.address(PropertyAddress.builder().city("city").stateFull("state").street("street").zipCode("80")
						.build())
				.isAdjoining(false).keyPoints("keys").nbBedrooms(555).yearConstruction("1996").userId(userID)
				
				.propExport(PropExport.builder().isSud(false).isEst(false).isOuest(false).isEst(true).build())
				.isOwner(false).livingArea(55).location(Location.builder().longitude(5555).latitude(555).build())
				.vueProp(PropertyVue.Vis_a_vis).build();

		var res = bien.save(bb);
		// Then
		assertThat(res.getId()).isNotNull();
		assertEquals("userID", res.getUserId().getId());

		assertEquals("description", res.getDescription());
	}

}
