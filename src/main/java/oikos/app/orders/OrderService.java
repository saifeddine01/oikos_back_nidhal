package oikos.app.orders;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.utils.Authorizable;
import oikos.app.common.utils.PDFUtil;
import oikos.app.notifications.NotificationService;
import oikos.app.oikosservices.OikosServiceService;
import oikos.app.serviceproviders.ProviderService;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService implements Authorizable<OrderService.OrderMethods> {
  private final UserRepo userRepo;
  private final OrderRepo orderRepo;
  private final Javers javers;
  private final ProviderService providerService;
  private final OikosServiceService serviceService;
  private final NotificationService notificationService;

  @ToString
  enum OrderMethods {
    ADD_ORDER(Names.ADD_ORDER),
    GET_ORDERHISTORY_FOR_CLIENT(Names.GET_ORDERHISTORY_FOR_CLIENT),
    GET_ORDERHISTORY_FOR_COMPANYOWNER(Names.GET_ORDERHISTORY_FOR_COMPANYOWNER),
    GET_ORDERHISTORY_FOR_SERVICE(Names.GET_ORDERHISTORY_FOR_SERVICE),
    GET_ORDER(Names.GET_ORDER),
    GET_ORDER_PDF(Names.GET_ORDER_PDF),
    CANCEL_ORDER(Names.CANCEL_ORDER),
    GET_HISTORY_FOR_ORDER(Names.GET_HISTORY_FOR_ORDER),
    PROCESS_ORDER(Names.PROCESS_ORDER),
    DELEGATE_ORDER(Names.DELEGATE_ORDER),
    GET_ORDERS_FOR_COLLABORATOR(Names.GET_ORDERS_FOR_COLLABORATOR),
    DELIVER_ORDER(Names.DELIVER_ORDER);
    private final String label;

    OrderMethods(String label) {
      this.label = label;
    }

    public static class Names {
      public static final String ADD_ORDER = "ADD_ORDER";
      public static final String GET_ORDERHISTORY_FOR_CLIENT = "GET_ORDERHISTORY_FOR_CLIENT";
      public static final String GET_ORDERHISTORY_FOR_COMPANYOWNER =
          "GET_ORDERHISTORY_FOR_COMPANYOWNER";
      public static final String GET_ORDERHISTORY_FOR_SERVICE = "GET_ORDERHISTORY_FOR_SERVICE";
      public static final String GET_ORDER = "GET_ORDER";
      public static final String GET_ORDER_PDF = "GET_ORDER_PDF";
      public static final String CANCEL_ORDER = "CANCEL_ORDER";
      public static final String GET_HISTORY_FOR_ORDER = "GET_HISTORY_FOR_ORDER";
      public static final String PROCESS_ORDER = "PROCESS_ORDER";
      public static final String DELIVER_ORDER = "DELIVER_ORDER";
      public static final String DELEGATE_ORDER = "DELEGATE_ORDER";
      public static final String GET_ORDERS_FOR_COLLABORATOR = "GET_ORDERS_FOR_COLLABORATOR";

      private Names() {}
    }
  }

  public Order addOrder(CreateOrderRequest req, User client) {
    var service = serviceService.getService(req.getServiceID());
    var data =
        orderRepo.save(
            Order.builder()
                .orderStatus(OrderStatus.Pending)
                .client(client)
                .service(service)
                .paymentMethod(req.getPaymentMethod())
                .serviceCompany(service.getServiceCompany())
                .price(service.getPrice())
                .build());
    notificationService.addNotification(
        service.getServiceCompany().getServiceOwner().getId(),
        "You got a new order",
        "/orders/" + data.getId());
    return data;
  }

  public Page<Order> getOrderHistoryForClient(String userID, Pageable paging) {
    return orderRepo.getOrderHistoryForClient(userID, paging);
  }

  public Page<Order> getOrderHistoryForCompanyOwner(String userID, Pageable paging) {
    var companyID = providerService.getCompanyIDForUser(userID);
    return orderRepo.getOrderHistoryForCompany(companyID, paging);
  }

  public Page<Order> getOrderHistoryForService(String serviceID, Pageable paging) {
    return orderRepo.getOrderHistoryForService(serviceID, paging);
  }

  public Order getOrder(String orderID) {
    return orderRepo
        .findById(orderID)
        .orElseThrow(() -> new EntityNotFoundException(Order.class, orderID));
  }

  public Order cancelOrder(String orderID) {
    var order = getOrder(orderID);
    if (order.getOrderStatus() != OrderStatus.Pending) {
      throw new BaseException("Selected isn't pending so it can't be cancelled");
    }
    order.setOrderStatus(OrderStatus.Cancelled);
    final var company = order.getServiceCompany();
    notificationService.addNotification(
        company.getServiceOwner().getId(), "You got a cancelled order", "/orders/" + order.getId());
    return orderRepo.save(order);
  }

  public Stream<Order> getHistoryForOrder(String orderID) {
    final var order = getOrder(orderID);
    final var jqlQuery = QueryBuilder.byInstance(order).build();
    List<Shadow<Order>> shadows = javers.findShadows(jqlQuery);
    return shadows.stream().map(Shadow::get);
  }

  public Order processOrder(String orderID) {
    var order = getOrder(orderID);
    if (!(order.getOrderStatus().equals(OrderStatus.Pending) || order.getOrderStatus().equals(OrderStatus.Delegated))) {
      throw new BaseException("Selected order isn't pending or delegated so it can't be processed");
    }
    order.setOrderStatus(OrderStatus.Processing);
    notificationService.addNotification(
        order.getClient().getId(), "Your order is being processed", "/orders/" + order.getId());
    return orderRepo.save(order);
  }

  public Order deliverOrder(String orderID) {
    var order = getOrder(orderID);
    if (order.getOrderStatus() != OrderStatus.Processing) {
      throw new BaseException("Selected order isn't processing so it can't be marked as delivered");
    }
    order.setOrderStatus(OrderStatus.Delivered);
    notificationService.addNotification(
        order.getClient().getId(),
        "Your order has been delivered. Please consider leaving feedback.",
        "/orders/" + order.getId());
    return orderRepo.save(order);
  }

  @Override
  public boolean canDo(OrderMethods methodName, String userID, String objectID) {
    return switch (methodName){
      case ADD_ORDER,GET_ORDERHISTORY_FOR_CLIENT-> true;
      case CANCEL_ORDER -> getOrder(objectID).getClient().getId().equals(userID);
      case GET_ORDER,GET_ORDER_PDF,GET_HISTORY_FOR_ORDER -> getOrder(objectID).getServiceCompany().getServiceOwner().getId().equals(userID)
        || getOrder(objectID).getServiceCompany().getCollaborators().stream().anyMatch(item -> item.getId().equals(userID))
        || getOrder(objectID).getClient().getId().equals(userID);
      case DELEGATE_ORDER -> getOrder(objectID).getServiceCompany().getServiceOwner().getId().equals(userID);
      case DELIVER_ORDER,PROCESS_ORDER -> getOrder(objectID).getServiceCompany().getServiceOwner().getId().equals(userID)
        || (getOrder(objectID).getCollaborator() != null && getOrder(objectID).getCollaborator().getId().equals(userID));
      case GET_ORDERHISTORY_FOR_SERVICE -> serviceService.getService(objectID).getServiceCompany().getServiceOwner().getId().equals(userID)
        || serviceService.getService(objectID).getServiceCompany().getCollaborators().stream().anyMatch(item -> item.getId().equals(userID));
      case GET_ORDERHISTORY_FOR_COMPANYOWNER -> CollectionUtils.contains(userRepo.getOne(userID).getRoles().iterator(),Role.PROVIDER);
      case GET_ORDERS_FOR_COLLABORATOR -> CollectionUtils.contains(userRepo.getOne(userID).getRoles().iterator(),Role.COLLABORATOR);
    };
  }

  public void getOrderPDF(TemplateEngine templateEngine,String orderID, HttpServletResponse response) {
    final var order = getOrder(orderID);
    if(order.getOrderStatus() != OrderStatus.Delivered){
      throw new BaseException("Order isn't delivered yet. An invoice can't be created");
    }
    final var serviceCompany =  order.getServiceCompany();
    final var client = order.getClient();
    List<Map<String, Object>> listVars = new ArrayList<>();
    Map<String, Object> variables = new HashMap<>();
    variables.put("id", order.getId());
    variables.put("date", LocalDate.ofInstant(order.getUpdatedAt(), ZoneId.of("Europe/Paris")).format(
      DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    variables.put("companyName", serviceCompany.getName());
    variables.put("companyAddress1", serviceCompany.getAddress().getStreet());
    variables.put("companyAddress2", serviceCompany.getAddress().getZipCode()+" "+ serviceCompany
      .getAddress().getDepartmentName());
    variables.put("clientName", client.getFirstName()+" "+ client.getLastName());
    variables.put("clientPhone", client.getPhoneNumber());
    variables.put("clientEmail", client.getEmail());
    variables.put("paymentMethod", order.getPaymentMethod().getName());
    variables.put("serviceName", order.getService().getServiceType().getName());
    variables.put("servicePrice", order.getService().getPrice());
    variables.put("total", order.getService().getPrice());
    listVars.add(variables);
    PDFUtil.preview(templateEngine, "facture", listVars, response);
  }

  public Order delegateOrder(
    String orderID, String collaboratorID) {
    var order = getOrder(orderID);
    var collaborator = userRepo.findById(collaboratorID).orElseThrow(()->new EntityNotFoundException(User.class,collaboratorID));
    if(!collaborator.getRoles().contains(Role.COLLABORATOR)){
      throw new BaseException("Selected user is not a collaborator");
    }
    if(!collaborator.getCollaboratorProfile().getServiceCompany().getId().equals(
      order.getServiceCompany().getId())){
      throw new BaseException("Selected collaborator does not belong to your service company");
    }
    if (order.getOrderStatus() != OrderStatus.Pending) {
      throw new BaseException("Selected order isn't pending so it can't be delegated");
    }
    order.setOrderStatus(OrderStatus.Delegated);
    order.setCollaborator(collaborator);
    return orderRepo.save(order);
  }

  public Page<Order> getOrdersDelegatedToCollaborator(String userID,
    Pageable paging) {
    return orderRepo.getOrdersForCollaborator(userID, paging);
  }
}
