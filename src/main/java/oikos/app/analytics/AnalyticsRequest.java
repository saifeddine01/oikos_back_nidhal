package oikos.app.analytics;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

@Data
public class AnalyticsRequest {
    @Size(min = NANOID_SIZE, max = NANOID_SIZE)
    private String id;
    @NotNull
    private DetailLevel detailLevel;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
}
