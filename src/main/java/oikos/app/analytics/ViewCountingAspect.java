package oikos.app.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.models.BienVendre;
import oikos.app.oikosservices.OikosService;
import oikos.app.security.OikosUserDetails;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class ViewCountingAspect {
    private final PropertyViewRepo propViewRepo;
    private final ServiceViewRepo serviceViewRepo;

    @AfterReturning(value = "execution(* oikos.app.common.controllers.PropertyController.findById(..))", returning = "prop")
    public void logAfterReturningPropView(BienVendre prop) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var user = (OikosUserDetails) auth.getPrincipal();
        var propView = new PropertyView(prop, user.getUsername());
        propViewRepo.save(propView);
    }

    @AfterReturning(value = "execution(* oikos.app.oikosservices.OikosServiceService.getService(..))", returning = "service")
    public void logAfterReturningServiceView(OikosService service) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var user = (OikosUserDetails) auth.getPrincipal();
        var serviceView = new ServiceView(service, user.getUsername());
        serviceViewRepo.save(serviceView);
    }
}
