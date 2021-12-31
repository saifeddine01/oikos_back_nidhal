package oikos.app.refunds;

import lombok.AllArgsConstructor;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

@RestController
@AllArgsConstructor
@Validated
@Monitor
public class RefundController {
  private final RefundService refundService;
  private final ModelMapper mapper;

  @PreAuthorize("@refundService.canDo('ADD_REFUND',#user.username,null)")
  @PostMapping("/refunds")
  public DoneResponse addRefund(
      @RequestBody CreateRefundRequest req, @CurrentUser OikosUserDetails user) {
    var data = refundService.addRefund(req);
    return new DoneResponse(
        MessageFormat.format("Refund request {0} has been sent to the admin.", data.getId()));
  }

  @PreAuthorize("@refundService.canDo('GET_REFUND',#user.username,#refundID)")
  @GetMapping("/refunds/{refundID}")
  public RefundResponse getRefund(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String refundID) {
    var data = refundService.getRefund(refundID);
    return mapper.map(data, RefundResponse.class);
  }

  @PreAuthorize("@refundService.canDo('GET_REFUNDS_BY_STATUS',#user.username,null)")
  @GetMapping("/refunds/")
  public Page<RefundResponse> getRefundsByStatus(
      @CurrentUser OikosUserDetails user,
      @RequestParam RefundStatus status,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = refundService.getRefundsByStatus(status, paging);
    return data.map(item -> mapper.map(item, RefundResponse.class));
  }

  @PreAuthorize("@refundService.canDo('GET_REFUNDS_FOR_USER',#user.username,null)")
  @GetMapping("/refunds/me")
  public Page<RefundResponse> getRefundsForCurrentUser(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = refundService.getRefundsForUser(user.getUsername(), paging);
    return data.map(item -> mapper.map(item, RefundResponse.class));
  }

  @PreAuthorize("@refundService.canDo('GET_REFUNDS_FOR_MY_COMPANY',#user.username,null)")
  @GetMapping("/providercompanies/me/refunds")
  public Page<RefundResponse> getRefundsForMyCompany(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = refundService.getRefundsForMyCompany(user.getUsername(), paging);
    return data.map(item -> mapper.map(item, RefundResponse.class));
  }

  @PreAuthorize("@refundService.canDo('ACCEPT_REFUND',#user.username,null)")
  @GetMapping("/refunds/{refundID}/accept")
  public DoneResponse acceptRefund(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String refundID) {
    var data = refundService.acceptRefund(refundID);
    return new DoneResponse(
        MessageFormat.format("Refund request {0} has been accepted.", data.getId()));
  }

  @PreAuthorize("@refundService.canDo('REFUSE_REFUND',#user.username,null)")
  @GetMapping("/refunds/{refundID}/refuse")
  public DoneResponse refuseRefund(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String refundID) {
    var data = refundService.refuseRefund(refundID);
    return new DoneResponse(
        MessageFormat.format("Refund request {0} has been refused.", data.getId()));
  }

  @PreAuthorize("@refundService.canDo('CANCEL_REFUND',#user.username,#refundID)")
  @GetMapping("/refunds/{refundID}/cancel")
  public DoneResponse cancelRefund(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String refundID) {
    var data = refundService.cancelRefund(refundID);
    return new DoneResponse(
        MessageFormat.format("Refund request {0} has been canceled.", data.getId()));
  }
}
