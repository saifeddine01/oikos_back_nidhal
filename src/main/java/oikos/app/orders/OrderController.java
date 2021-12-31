package oikos.app.orders;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

@RestController
@AllArgsConstructor
@Validated
@Monitor
public class OrderController {
  private final OrderService orderService;
  private final TemplateEngine templateEngine;
  private final ModelMapper mapper;

  @PreAuthorize("@orderService.canDo('ADD_ORDER',#user.username,null)")
  @PostMapping("/orders")
  public OrderResponse addOrder(
      @RequestBody CreateOrderRequest req, @CurrentUser OikosUserDetails user) {
    var data = orderService.addOrder(req, user.getUser());
    return mapper.map(data, OrderResponse.class);
  }

  @PreAuthorize("@orderService.canDo('GET_ORDERHISTORY_FOR_CLIENT',#user.username,null)")
  @GetMapping("/orders/me")
  public Page<OrderResponse> getOrderHistoryForClient(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = orderService.getOrderHistoryForClient(user.getUsername(), paging);
    return data.map(item -> mapper.map(item, OrderResponse.class));
  }

  @PreAuthorize("@orderService.canDo('GET_ORDERS_FOR_COLLABORATOR',#user.username,null)")
  @GetMapping("/orders/delegated/me")
  public Page<OrderResponse> getOrdersDelegatedToCollaborator(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = orderService.getOrdersDelegatedToCollaborator(user.getUsername(), paging);
    return data.map(item -> mapper.map(item, OrderResponse.class));
  }

  @PreAuthorize("@orderService.canDo('GET_ORDERHISTORY_FOR_COMPANYOWNER',#user.username,null)")
  @GetMapping("/providercompanies/me/orders")
  public Page<OrderResponse> getOrderHistoryForCompany(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = orderService.getOrderHistoryForCompanyOwner(user.getUsername(), paging);
    return data.map(item -> mapper.map(item, OrderResponse.class));
  }

  @PreAuthorize("@orderService.canDo('GET_ORDERHISTORY_FOR_SERVICE',#user.username,#serviceID)")
  @GetMapping("/services/{serviceID}/orders")
  public Page<OrderResponse> getOrderHistoryForService(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String serviceID) {
    Pageable paging = PageRequest.of(page, size);
    var data = orderService.getOrderHistoryForService(serviceID, paging);
    return data.map(item -> mapper.map(item, OrderResponse.class));
  }

  @PreAuthorize("@orderService.canDo('CANCEL_ORDER',#user.username,#orderID)")
  @GetMapping("/orders/{orderID}/cancel")
  public OrderResponse cancelOrder(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String orderID) {
    var data = orderService.cancelOrder(orderID);
    return mapper.map(data, OrderResponse.class);
  }

  @PreAuthorize("@orderService.canDo('PROCESS_ORDER',#user.username,#orderID)")
  @GetMapping("/orders/{orderID}/process")
  public OrderResponse processOrder(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String orderID) {
    var data = orderService.processOrder(orderID);
    return mapper.map(data, OrderResponse.class);
  }

  @PreAuthorize("@orderService.canDo('DELIVER_ORDER',#user.username,#orderID)")
  @GetMapping("/orders/{orderID}/deliver")
  public OrderResponse deliverOrder(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String orderID) {
    var data = orderService.deliverOrder(orderID);
    return mapper.map(data, OrderResponse.class);
  }

  @PreAuthorize("@orderService.canDo('DELEGATE_ORDER',#user.username,#orderID)")
  @GetMapping("/orders/{orderID}/delegate/{collaboratorID}")
  public OrderResponse delegateOrder(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String orderID,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String collaboratorID) {
    var data = orderService.delegateOrder(orderID, collaboratorID);
    return mapper.map(data, OrderResponse.class);
  }

  @PreAuthorize("@orderService.canDo('GET_ORDER',#user.username,#orderID)")
  @GetMapping("/orders/{orderID}")
  public OrderResponse getOrder(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String orderID) {
    var data = orderService.getOrder(orderID);
    return mapper.map(data, OrderResponse.class);
  }

  @PreAuthorize("@orderService.canDo('GET_ORDER_PDF',#user.username,#orderID)")
  @GetMapping(value = "/orders/{orderID}/pdf")
  public void getOrderPDF(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String orderID,
      HttpServletResponse response) {
    orderService.getOrderPDF(templateEngine, orderID, response);
  }

  @PreAuthorize("@orderService.canDo('GET_HISTORY_FOR_ORDER',#user.username,#orderID)")
  @GetMapping("/orders/{orderID}/history")
  public List<OrderHistory> getOrderHistory(
      @CurrentUser OikosUserDetails user, @PathVariable String orderID) {
    return orderService
        .getHistoryForOrder(orderID)
        .map(item -> mapper.map(item, OrderHistory.class))
        .collect(Collectors.toList());
  }
}
