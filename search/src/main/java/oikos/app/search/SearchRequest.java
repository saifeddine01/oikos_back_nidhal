package oikos.app.search;

import lombok.Data;

@Data
public class SearchRequest {
  private Integer minPrice;
  private Integer maxPrice;
  private String keyword;
  private String state;
  private String type;
  private PropertyStanding standing;
  private SortDirection sort;
}
