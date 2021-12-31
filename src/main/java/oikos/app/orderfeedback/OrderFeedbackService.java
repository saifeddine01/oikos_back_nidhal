package oikos.app.orderfeedback;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.utils.Authorizable;
import oikos.app.notifications.NotificationService;
import oikos.app.orders.OrderService;
import oikos.app.serviceproviders.ProviderService;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.modelmapper.ModelMapper;
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
public class OrderFeedbackService implements Authorizable<OrderFeedbackService.FeedbackMethods> {
  private final UserRepo userRepo;
  private final ModelMapper mapper;
  private final OrderFeedbackRepo feedbackRepo;
  private final OrderService orderService;
  private final NotificationService notificationService;
  private final ProviderService providerService;

  @ToString
  enum FeedbackMethods {
    ADD_ORDER_FEEDBACK(Names.ADD_ORDER_FEEDBACK),
    EDIT_ORDER_FEEDBACK(Names.EDIT_ORDER_FEEDBACK),
    DELETE_ORDER_FEEDBACK(Names.DELETE_ORDER_FEEDBACK),
    GET_ORDER_FEEDBACK(Names.GET_ORDER_FEEDBACK),
    GET_ORDER_FEEDBACKS(Names.GET_ORDER_FEEDBACKS),
    GET_ORDER_FEEDBACKS_FOR_COMPANY_OWNER(Names.GET_ORDER_FEEDBACKS_FOR_COMPANY_OWNER),
    GET_ORDER_FEEDBACKS_FOR_COMPANY(Names.GET_ORDER_FEEDBACKS_FOR_COMPANY);
    private final String label;

    FeedbackMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String ADD_ORDER_FEEDBACK = "ADD_ORDER_FEEDBACK";
      public static final String EDIT_ORDER_FEEDBACK = "EDIT_ORDER_FEEDBACK";
      public static final String DELETE_ORDER_FEEDBACK = "DELETE_ORDER_FEEDBACK";
      public static final String GET_ORDER_FEEDBACK = "GET_ORDER_FEEDBACK";
      public static final String GET_ORDER_FEEDBACKS = "GET_ORDER_FEEDBACKS";
      public static final String GET_ORDER_FEEDBACKS_FOR_COMPANY_OWNER =
          "GET_ORDER_FEEDBACKS_FOR_COMPANY_OWNER";
      public static final String GET_ORDER_FEEDBACKS_FOR_COMPANY =
          "GET_ORDER_FEEDBACKS_FOR_COMPANY";

      private Names() {}
    }
  }

  public OrderFeedback addOrderFeedback(CreateOrderFeedbackRequest req, User user) {
    if(feedbackRepo.existsByOrderID(req.getOrderID())){
      throw new BaseException("You already left a review on this order. Try to edit it instead.");
    }
    var order = orderService.getOrder(req.getOrderID());
    var data =
        feedbackRepo.save(
            OrderFeedback.builder()
                .order(order)
                .content(req.getContent())
                .isRefundRequest(req.isRefundRequest())
                .rating(req.getRating())
                .serviceCompany(order.getServiceCompany())
                .customer(user)
                .build());
    notificationService.addNotification(
        data.getServiceCompany().getServiceOwner().getId(),
        "An order got a new feedback",
        "/orderfeedbacks/" + data.getId());
    return data;
  }

  public OrderFeedback getOrderFeedback(String feedbackID) {
    return feedbackRepo
        .findById(feedbackID)
        .orElseThrow(() -> new EntityNotFoundException(OrderFeedback.class, feedbackID));
  }

  public Page<OrderFeedback> getOrderFeedbacks(Pageable paging) {
    return feedbackRepo.findAll(paging);
  }

  public Page<OrderFeedback> getOrderFeedbacksForCompanyOwner(String userID, Pageable paging) {
    var companyID = providerService.getCompanyIDForUser(userID);
    return getOrderFeedbacksForCompany(companyID, paging);
  }

  public Page<OrderFeedback> getOrderFeedbacksForCompany(String companyID, Pageable paging) {
    return feedbackRepo.getOrderFeedbacksForCompanyOwner(companyID, paging);
  }

  @Override public boolean canDo(FeedbackMethods methodName, String userID,
    String objectID) {
    return switch (methodName){
      case ADD_ORDER_FEEDBACK -> orderService.getOrder(objectID).getClient().getId().equals(userID);
      case GET_ORDER_FEEDBACK -> getOrderFeedback(objectID).getCustomer().getId().equals(userID) || getOrderFeedback(objectID).getServiceCompany().getServiceOwner().getId().equals(userID) || CollectionUtils
        .containsAny(userRepo.getOne(userID).getRoles(),
          List.of(Role.SECRETARY, Role.ADMIN));
      case GET_ORDER_FEEDBACKS ,GET_ORDER_FEEDBACKS_FOR_COMPANY-> CollectionUtils.containsAny(userRepo.getOne(userID).getRoles(),
        List.of(Role.SECRETARY, Role.ADMIN));
      case GET_ORDER_FEEDBACKS_FOR_COMPANY_OWNER -> true;
      case EDIT_ORDER_FEEDBACK,DELETE_ORDER_FEEDBACK -> getOrderFeedback(objectID).getCustomer().getId().equals(userID);
    };
  }

  public OrderFeedback editFeedback(String feedbackID,
    EditOrderFeedbackRequest req) {
    var feedback = getOrderFeedback(feedbackID);
    mapper.map(req,feedback);
    return feedbackRepo.save(feedback);
  }

  public void deleteFeedback(String feedbackID) {
    feedbackRepo.delete(getOrderFeedback(feedbackID));
  }
}
