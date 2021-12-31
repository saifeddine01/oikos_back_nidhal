package oikos.app.orders;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@JaversSpringDataAuditable
public interface OrderRepo extends JpaRepository<Order, String> {
  @Query("select o from Order o where o.service.id = :serviceID order by o.createdAt DESC")
  Page<Order> getOrderHistoryForService(@Param("serviceID") String serviceID, Pageable pageable);

  @Query("select o from Order o where o.serviceCompany.id = :companyID order by o.createdAt DESC")
  Page<Order> getOrderHistoryForCompany(@Param("companyID") String companyID, Pageable pageable);

  @Query("select o from Order o where o.client.id = :clientID order by o.createdAt desc ")
  Page<Order> getOrderHistoryForClient(@Param("clientID") String clientID, Pageable pageable);

  @Query(
      "select o from Order o where o.collaborator is not null and o.collaborator.id = :collaboratorID order by o.updatedAt desc")
  Page<Order> getOrdersForCollaborator(
      @Param("collaboratorID") String collaboratorID, Pageable pageable);
}
