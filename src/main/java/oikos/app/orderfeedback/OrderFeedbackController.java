package oikos.app.orderfeedback;

import lombok.AllArgsConstructor;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

@RestController
@AllArgsConstructor
@Validated
@Monitor
public class OrderFeedbackController {
  private final OrderFeedbackService feedbackService;
  private final ModelMapper mapper;

  @PreAuthorize("@orderFeedbackService.canDo('ADD_ORDER_FEEDBACK',#user.username,#req.orderID)")
  @PostMapping("/orderfeedbacks")
  public OrderFeedbackResponse addOrderFeedback(
      @RequestBody CreateOrderFeedbackRequest req, @CurrentUser OikosUserDetails user) {
    var data = feedbackService.addOrderFeedback(req, user.getUser());
    return mapper.map(data, OrderFeedbackResponse.class);
  }

  @PreAuthorize("@orderFeedbackService.canDo('GET_ORDER_FEEDBACK',#user.username,#feedbackID)")
  @GetMapping("/orderfeedbacks/{feedbackID}")
  public OrderFeedbackResponse getOrderFeedback(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String feedbackID) {
    var data = feedbackService.getOrderFeedback(feedbackID);
    return mapper.map(data, OrderFeedbackResponse.class);
  }

  @PreAuthorize("@orderFeedbackService.canDo('EDIT_ORDER_FEEDBACK',#user.username,#feedbackID)")
  @PutMapping("/orderfeedbacks/{feedbackID}")
  public OrderFeedbackResponse editOrderFeedback(
      @CurrentUser OikosUserDetails user,
      @RequestBody EditOrderFeedbackRequest req,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String feedbackID) {
    var data = feedbackService.editFeedback(feedbackID, req);
    return mapper.map(data, OrderFeedbackResponse.class);
  }

  @PreAuthorize("@orderFeedbackService.canDo('DELETE_ORDER_FEEDBACK',#user.username,#feedbackID)")
  @DeleteMapping("/orderfeedbacks/{feedbackID}")
  public DoneResponse deleteOrderFeedback(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String feedbackID) {
    feedbackService.deleteFeedback(feedbackID);
    return new DoneResponse(
        MessageFormat.format("Order feedback {0} has been deleted", feedbackID));
  }

  @PreAuthorize("@orderFeedbackService.canDo('GET_ORDER_FEEDBACKS',#user.username,null)")
  @GetMapping("/orderfeedbacks")
  public Page<OrderFeedbackResponse> getOrderFeedbacks(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    var data = feedbackService.getOrderFeedbacks(paging);
    return data.map(item -> mapper.map(item, OrderFeedbackResponse.class));
  }

  @PreAuthorize(
      "@orderFeedbackService.canDo('GET_ORDER_FEEDBACKS_FOR_COMPANY_OWNER',#user.username,null)")
  @GetMapping("/providercompanies/me/orderfeedbacks")
  public Page<OrderFeedbackResponse> getOrderFeedbacksForMyCompany(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = feedbackService.getOrderFeedbacksForCompanyOwner(user.getUsername(), paging);
    return data.map(item -> mapper.map(item, OrderFeedbackResponse.class));
  }

  @PreAuthorize(
      "@orderFeedbackService.canDo('GET_ORDER_FEEDBACKS_FOR_COMPANY',#user.username,null)")
  @GetMapping("/providercompanies/{companyID}/orderfeedbacks")
  public Page<OrderFeedbackResponse> getOrderFeedbacksForCompany(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String companyID) {
    Pageable paging = PageRequest.of(page, size);
    var data = feedbackService.getOrderFeedbacksForCompany(companyID, paging);
    return data.map(item -> mapper.map(item, OrderFeedbackResponse.class));
  }
}
