package oikos.app.refunds;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.utils.Authorizable;
import oikos.app.notifications.NotificationService;
import oikos.app.orderfeedback.OrderFeedbackService;
import oikos.app.serviceproviders.ProviderService;
import oikos.app.users.Role;
import oikos.app.users.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundService implements Authorizable<RefundService.RefundMethods> {
  private final UserRepo userRepo;
  private final ProviderService providerService;
  private final RefundRepo refundRepo;
  private final OrderFeedbackService feedbackService;
  private final NotificationService notificationService;

  @ToString
  enum RefundMethods {
    ADD_REFUND(Names.ADD_REFUND),
    GET_REFUND(Names.GET_REFUND),
    ACCEPT_REFUND(Names.ACCEPT_REFUND),
    REFUSE_REFUND(Names.REFUSE_REFUND),
    GET_REFUNDS_FOR_MY_COMPANY(Names.GET_REFUNDS_FOR_MY_COMPANY),
    GET_REFUNDS_FOR_USER(Names.GET_REFUNDS_FOR_USER),
    GET_REFUNDS_BY_STATUS(Names.GET_REFUNDS_BY_STATUS),
    CANCEL_REFUND(Names.CANCEL_REFUND);
    private final String label;

    RefundMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String ADD_REFUND = "ADD_REFUND";
      public static final String GET_REFUND = "GET_REFUND";
      public static final String ACCEPT_REFUND = "ACCEPT_REFUND";
      public static final String REFUSE_REFUND = "REFUSE_REFUND";
      public static final String GET_REFUNDS_BY_STATUS = "GET_REFUNDS_BY_STATUS";
      public static final String CANCEL_REFUND = "CANCEL_REFUND";
      public static final String GET_REFUNDS_FOR_MY_COMPANY = "GET_REFUNDS_FOR_MY_COMPANY";
      public static final String GET_REFUNDS_FOR_USER = "GET_REFUNDS_FOR_USER";

      private Names() {}
    }
  }

  public Refund addRefund(CreateRefundRequest req) {
    if(refundRepo.existsByOrderID(req.getOrderFeedbackID())){
      throw new BaseException("Redund Request already exists.");
    }
    var feedback = feedbackService.getOrderFeedback(req.getOrderFeedbackID());
    var data =
        refundRepo.save(
            Refund.builder()
                .refundStatus(RefundStatus.Pending)
                .serviceCompany(feedback.getServiceCompany())
              .client(feedback.getCustomer())
                .order(feedback.getOrder())
                .build());
    notificationService.addNotification(
        data.getOrder().getClient().getId(),
        "The admin is looking through your refund request.",
        "/refunds/" + data.getId());
    notificationService.addNotification(
        data.getServiceCompany().getServiceOwner().getId(),
        "One of your orders is going through a refund request.",
        "/refunds/" + data.getId());
    return data;
  }

  public Refund getRefund(String refundID) {
    return refundRepo
        .findById(refundID)
        .orElseThrow(() -> new EntityNotFoundException(Refund.class, refundID));
  }

  public Refund acceptRefund(String refundID) {
    var refund = getRefund(refundID);
    if (refund.getRefundStatus() != RefundStatus.Pending) {
      throw new BaseException("The refund isn't pending so it can't change its status");
    }
    refund.setRefundStatus(RefundStatus.Accepted);
    notificationService.addNotification(
        refund.getOrder().getClient().getId(),
        "Your refund request has been accepted.",
        "/refunds/" + refund.getId());
    notificationService.addNotification(
        refund.getServiceCompany().getServiceOwner().getId(),
        "One of your orders needs to be refunded.",
        "/refunds/" + refund.getId());
    return refundRepo.save(refund);
  }

  public Refund refuseRefund(String refundID) {
    var refund = getRefund(refundID);
    if (refund.getRefundStatus() != RefundStatus.Pending) {
      throw new BaseException("The refund isn't pending so it can't change its status");
    }
    refund.setRefundStatus(RefundStatus.Refused);
    notificationService.addNotification(
        refund.getOrder().getClient().getId(),
        "Your refund request has been refused.",
        "/refunds/" + refund.getId());
    return refundRepo.save(refund);
  }

  public Page<Refund> getRefundsByStatus(RefundStatus status, Pageable paging) {
    return refundRepo.getRefundsByStatus(status, paging);
  }

  public Refund cancelRefund(String refundID) {
    var refund = getRefund(refundID);
    if (refund.getRefundStatus() != RefundStatus.Pending) {
      throw new BaseException("The refund isn't pending so it can't change its status");
    }
    refund.setRefundStatus(RefundStatus.Canceled);
    notificationService.addNotification(
        refund.getOrder().getClient().getId(),
        "Your refund request has been canceled.",
        "/refunds/" + refund.getId());
    notificationService.addNotification(
        refund.getServiceCompany().getServiceOwner().getId(),
        "One of your company's refund requests has been canceled by the client.",
        "/refunds/" + refund.getId());
    return refundRepo.save(refund);
  }

  @Override public boolean canDo(RefundMethods methodName, String userID,
    String objectID) {
    return switch (methodName){
      case ADD_REFUND -> userRepo.getOne(userID).getRoles().contains(Role.SECRETARY);
      case ACCEPT_REFUND ,REFUSE_REFUND-> userRepo.getOne(userID).getRoles().contains(Role.ADMIN);
      case CANCEL_REFUND -> getRefund(objectID).getOrder().getClient().getId().equals(userID);
      case GET_REFUNDS_BY_STATUS -> CollectionUtils.containsAny(userRepo.getOne(userID).getRoles(),
        List.of(Role.SECRETARY, Role.ADMIN));
      case GET_REFUND -> CollectionUtils.containsAny(userRepo.getOne(userID).getRoles(),
        List.of(Role.SECRETARY, Role.ADMIN)) ||
        getRefund(objectID).getOrder().getClient().getId().equals(userID) ||
        getRefund(objectID).getServiceCompany().getServiceOwner().getId().equals(userID) ;
      case GET_REFUNDS_FOR_MY_COMPANY,GET_REFUNDS_FOR_USER -> true;
    };
  }

  public Page<Refund> getRefundsForUser(String userID, Pageable paging) {
    return refundRepo.getRefundsForUser(userID,paging);
  }

  public Page<Refund> getRefundsForMyCompany(String userID, Pageable paging) {
    var companyID = providerService.getCompanyIDForUser(userID);
    return refundRepo.getRefundsForCompany(companyID,paging);
  }
}
