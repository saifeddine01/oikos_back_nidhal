package oikos.app.orderfeedback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderFeedbackRepo extends JpaRepository<OrderFeedback, String> {
  @Query("select (count(o) > 0) from OrderFeedback o where o.order.id = :orderID")
  boolean existsByOrderID(@Param("orderID") String orderID);

  @Query(
      "select o from OrderFeedback o where o.serviceCompany.id = :companyID order by o.createdAt DESC")
  Page<OrderFeedback> getOrderFeedbacksForCompanyOwner(
      @Param("companyID") String companyID, Pageable pageable);

  @Query("select avg (o.rating) from OrderFeedback o where o.serviceCompany.id = :companyID")
  int avgRatingByCompany(@Param("companyID") String companyID);
}
