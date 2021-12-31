package oikos.app.refunds;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepo extends JpaRepository<Refund, String> {
  @Query("select (count(r) > 0) from Refund r where r.order.id = :orderID")
  boolean existsByOrderID(@Param("orderID") String orderID);

  @Query("select r from Refund r where r.refundStatus = :refundStatus order by r.createdAt DESC")
  Page<Refund> getRefundsByStatus(
      @Param("refundStatus") RefundStatus refundStatus, Pageable pageable);

  @Query("select r from Refund r where r.client.id = :clientID order by r.createdAt DESC")
  Page<Refund> getRefundsForUser(@Param("clientID") String clientID, Pageable paging);

  @Query("select r from Refund r where r.serviceCompany.id = :companyID order by r.createdAt DESC")
  Page<Refund> getRefundsForCompany(@Param("companyID") String companyID, Pageable paging);
}
