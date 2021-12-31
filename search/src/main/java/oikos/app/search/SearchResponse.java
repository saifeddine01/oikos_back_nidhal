package oikos.app.search;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SearchResponse {

  List<BienVendreResponse> results;

  List<SearchFacet> typeFacet;
  List<SearchFacet> standingFacet;
  List<SearchFacet> stateFacet;
  List<SearchFacet> priceFacet;
}
