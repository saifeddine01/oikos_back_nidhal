package oikos.app.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.BienVendre;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.oikosservices.OikosService;
import oikos.app.oikosservices.OikosServiceRepo;
import oikos.app.security.OikosUserDetails;
import oikos.app.seller.Seller;
import oikos.app.serviceproviders.models.ServiceCompany;
import oikos.app.serviceproviders.repos.ServiceCompanyRepo;
import oikos.app.users.Role;
import oikos.app.users.UserRepo;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalyticsService {
    private final NamedParameterJdbcTemplate template;
    private final BienaVendreRepo bienaVendreRepo;
    private final OikosServiceRepo serviceRepo;
    private final ServiceCompanyRepo companyRepo;
    private final UserRepo userRepo;

    public List<ViewRecord> getAnalytics(AnalyticsRequest req, AnalyticsMethod method, OikosUserDetails user) {
        String tablename = getTableAndColumn(method).getLeft();
        String column = getTableAndColumn(method).getRight();
        String weekordayselect = getWeekOrDay(req.getDetailLevel()).getLeft();
        String weekordayorderby = getWeekOrDay(req.getDetailLevel()).getMiddle();
        String weekorday = getWeekOrDay(req.getDetailLevel()).getRight();
        checkForEntityExistance(req, method);
        checkForAuthorization(user, method, req);
        var res = new ArrayList<ViewRecord>();
        var query = "SELECT date_part('year', ts::date) as year, " +
                    "       date_part('month', ts::date) AS month, " +
                    weekordayselect +
                    "       COUNT(id) as views " +
                    "FROM property_view " +
                    "where %1$s.%2$s = :id and " +
                    "%1$s.ts between :dateStart and :dateEnd " +
                    "GROUP BY year, month " + weekordayorderby +
                    "ORDER BY year, month" + weekordayorderby + ";";
        query = String.format(query, tablename, column);
        var rs = template.queryForRowSet(query, new MapSqlParameterSource().addValue("id", req.getId()).
                addValue("dateStart", Timestamp.from(req.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant())).
                addValue("dateEnd", Timestamp.from(req.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant())));
        while (rs.next()) {
            final var year = (int) rs.getFloat("year");
            final var month = (int) rs.getFloat("month");
            Integer thirdValue = null;
            if (req.getDetailLevel() == DetailLevel.WEEK || req.getDetailLevel() == DetailLevel.DAY) {
                thirdValue = (int) rs.getFloat(weekorday);
            }
            final var views = rs.getInt("views");
            res.add(new ViewRecord(year, month, Optional.ofNullable(thirdValue), views, req.getDetailLevel()));
        }

        return res;
    }

    private void checkForAuthorization(OikosUserDetails user, AnalyticsMethod method, AnalyticsRequest req) {
        boolean canDo = switch (method) {
            case Seller -> user.getUser().getId().equals(req.getId()) || CollectionUtils.containsAny(user.getUser().getRoles(),
                    List.of(Role.SECRETARY, Role.ADMIN));
            case Props -> CollectionUtils.containsAny(user.getUser().getRoles(),
                    List.of(Role.SECRETARY, Role.ADMIN)) || bienaVendreRepo.getOne(req.getId()).getUserId().getId().equals(user.getUsername());
            case Service, Company -> companyRepo.getOne(req.getId()).getServiceOwner().getId().equals(user.getUsername());
        };
        if (!canDo)
            throw new AccessDeniedException("FORBIDDEN");
    }

    private void checkForEntityExistance(AnalyticsRequest req, AnalyticsMethod method) {
        if (method == AnalyticsMethod.Props && !bienaVendreRepo.existsById(req.getId())) {
            throw new EntityNotFoundException(BienVendre.class, req.getId());
        }
        if (method == AnalyticsMethod.Service && !serviceRepo.existsById(req.getId())) {
            throw new EntityNotFoundException(OikosService.class, req.getId());
        }
        if (method == AnalyticsMethod.Seller && !userRepo.existsById(req.getId())) {
            throw new EntityNotFoundException(Seller.class, req.getId());
        }
        if (method == AnalyticsMethod.Company && companyRepo.existsById(req.getId())) {
            throw new EntityNotFoundException(ServiceCompany.class, req.getId());
        }
    }

    private Pair<String, String> getTableAndColumn(AnalyticsMethod method) {
        return switch (method) {
            case Props -> Pair.of("property_view", "propid");
            case Seller -> Pair.of("property_view", "ownerid");
            case Service -> Pair.of("service_view", "serviceid");
            case Company -> Pair.of("service_view", "companyid");
        };
    }

    private Triple<String, String, String> getWeekOrDay(DetailLevel detailLevel) {
        return switch (detailLevel) {
            case WEEK -> Triple.of("date_part('week', ts::date) AS week, ", ",week ", "week");
            case DAY -> Triple.of("date_part('day', ts::date) AS day, ", ",day ", "day");
            default -> Triple.of("", "", "");
        };
    }
}
