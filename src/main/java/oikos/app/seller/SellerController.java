package oikos.app.seller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import oikos.app.users.UserInfoResponse;
import oikos.app.users.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 17/04/2021. */
@Slf4j
@RestController
@AllArgsConstructor
@Validated
@Monitor
@RequestMapping("/seller")
public class SellerController {
  private final SellerService sellerService;
  private final UserService userService;
  private final ModelMapper mapper;

  @PreAuthorize("@sellerService.canDo('UPDATE_MY_SELLER_PROFILE',#user.username,#user.username)")
  @PutMapping("/me")
  public DoneResponse updateMySellerProfile(
      @Validated @RequestBody UpdateSellerRequest dto, @CurrentUser OikosUserDetails user) {
    var response = sellerService.updateMySellerProfile(dto, user.getUsername());
    return new DoneResponse(
        MessageFormat.format("Seller info for user {0} has been updated", response.getId()));
  }

  @PreAuthorize("@sellerService.canDo('UPDATE_SELLER_PROFILE',#user.username,#sellerID)")
  @PutMapping("/{sellerID}")
  public DoneResponse updateSellerProfile(
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String sellerID,
      @Validated @RequestBody UpdateSellerRequest dto,
      @CurrentUser OikosUserDetails user) {
    var response = sellerService.updateSellerProfile(sellerID, dto);
    return new DoneResponse(
        MessageFormat.format("Seller info for user {0} has been updated", response.getId()));
  }

  @PreAuthorize("@sellerService.canDo('GET_ALL_SELLERS',#user.username,null)")
  @GetMapping
  public Page<UserInfoResponse> getAllSellers(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    return userService.getAllSellers(paging);
  }
}
