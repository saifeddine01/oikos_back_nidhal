package oikos.app.serviceproviders;

import lombok.AllArgsConstructor;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import oikos.app.serviceproviders.dtos.CompanyResponse;
import oikos.app.serviceproviders.dtos.EditCompanyRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 25/06/2021 */
@RestController
@AllArgsConstructor
@Validated
@Monitor
public class ProviderController {
  private final ProviderService providerService;
  private final ModelMapper modelMapper;

  @PreAuthorize("@providerService.canDo('APPROVE_COMPANY',#user.username,#companyID)")
  @GetMapping("/providercompanies/{companyID}/accept")
  public CompanyResponse approveCompany(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String companyID) {
    var data = providerService.approveCompany(companyID);
    return modelMapper.map(data, CompanyResponse.class);
  }

  @PreAuthorize("@providerService.canDo('REJECT_COMPANY',#user.username,#companyID)")
  @GetMapping("/providercompanies/{companyID}/reject")
  public DoneResponse rejectCompany(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String companyID) {
    providerService.rejectCompany(companyID);
    return new DoneResponse("Company " + companyID + "has been rejected");
  }

  @PreAuthorize("@providerService.canDo('GET_COMPANY',#user.username,#companyID)")
  @GetMapping("/providercompanies/{companyID}/")
  public CompanyResponse getCompany(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String companyID) {
    var data = providerService.getCompany(companyID);
    return modelMapper.map(data, CompanyResponse.class);
  }

  @PreAuthorize("@providerService.canDo('GET_COMPANIES',#user.username,null)")
  @GetMapping("/providercompanies/")
  public Page<CompanyResponse> getCompanies(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = providerService.getAllCompanies(paging);
    return data.map(item -> modelMapper.map(item, CompanyResponse.class));
  }

  @PreAuthorize("@providerService.canDo('GET_UNAPPROVED_COMPANIES',#user.username,null)")
  @GetMapping("/providercompanies/by-status/unapproved")
  public Page<CompanyResponse> getUnapprovedCompanies(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var data = providerService.getAllUnapprovedCompanies(paging);
    return data.map(item -> modelMapper.map(item, CompanyResponse.class));
  }

  @PreAuthorize("@providerService.canDo('GET_MY_COMPANY',#user.username,null)")
  @GetMapping("/providercompanies/me")
  public CompanyResponse getMyCompany(@CurrentUser OikosUserDetails user) {
    var data = providerService.getCompanyForUser(user.getUsername());
    return modelMapper.map(data, CompanyResponse.class);
  }

  @PreAuthorize("@providerService.canDo('EDIT_COMPANY',#user.username,#companyID)")
  @PutMapping("/providercompanies/{companyID}/")
  public CompanyResponse editCompany(
      @Validated @RequestBody EditCompanyRequest req,
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String companyID) {
    var data = providerService.editCompany(companyID, req);
    return modelMapper.map(data, CompanyResponse.class);
  }
}
