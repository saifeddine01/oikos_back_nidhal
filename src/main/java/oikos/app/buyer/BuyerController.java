package oikos.app.buyer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;
import java.text.MessageFormat;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 19/04/2021. */
@Slf4j
@RestController
@AllArgsConstructor
@Validated
@Monitor
@RequestMapping("/buyer")
public class BuyerController {
  private final BuyerService buyerService;

  @GetMapping("/{userID}/validate")
  @PreAuthorize("@buyerService.canDo('VALIDATE_BUYER',#userDetails.user.id,#userID)")
  public DoneResponse validateBuyer(
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String userID,
      @CurrentUser OikosUserDetails userDetails) {
    buyerService.validateBuyer(userID);
    return new DoneResponse(MessageFormat.format("Buyer {0} has been validated", userID));
  }
}
