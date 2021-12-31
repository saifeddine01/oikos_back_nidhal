package oikos.app.common.request;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class EstimationRequest {
	double nb_dep;
	double surface_reelle_bati;
	double nombre_pieces_principales;
	double x;
	double y;
	double IRIS;
}
