package oikos.app.analytics;

import lombok.AllArgsConstructor;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@Monitor
public class AnalyticsController {
    private final AnalyticsService service;

    @PostMapping("/analytics/property")
    public List<ViewRecord> getPropertyAnalytics(@RequestBody AnalyticsRequest request, @CurrentUser OikosUserDetails user) {
        return service.getAnalytics(request, AnalyticsMethod.Props,user);
    }
    @PostMapping("/analytics/service")
    public List<ViewRecord> getServiceAnalytics(@RequestBody AnalyticsRequest request, @CurrentUser OikosUserDetails user) {
        return service.getAnalytics(request, AnalyticsMethod.Service,user);
    }
    @PostMapping("/analytics/company")
    public List<ViewRecord> getCompanyAnalytics(@RequestBody AnalyticsRequest request, @CurrentUser OikosUserDetails user) {
        return service.getAnalytics(request, AnalyticsMethod.Company,user);
    }
    @PostMapping("/analytics/seller")
    public List<ViewRecord> getSellerAnalytics(@RequestBody AnalyticsRequest request, @CurrentUser OikosUserDetails user) {
        return service.getAnalytics(request, AnalyticsMethod.Seller,user);
    }
}
