package oikos.app.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.facet.FacetSortOrder;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {
  private final EntityManager entityManager;
  private final ModelMapper mapper;
  private final IndexingTask task;

  public SearchResponse search(SearchRequest request, Pageable paging) {
    final var fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
    final var queryBuilder =
        fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(BienVendre.class)
            .get();
    var bool = queryBuilder.bool().must(queryBuilder.all().createQuery());
    if (request.getMinPrice() != null) {
      var minpriceQuery =
          queryBuilder.range().onField("price").above(request.getMinPrice()).createQuery();
      bool = bool.must(minpriceQuery);
    }
    if (request.getMaxPrice() != null) {
      var minpriceQuery =
          queryBuilder.range().onField("price").below(request.getMaxPrice()).createQuery();
      bool = bool.must(minpriceQuery);
    }
    if(request.getStanding() !=null) {
      var standingQuery = queryBuilder.keyword().onField("propStanding").matching(request.getStanding().name()).createQuery();
      bool = bool.must(standingQuery);
    }
    if(request.getType() != null) {
      var typeQuery = queryBuilder.keyword().onField("typeofprop").matching(request.getType()).createQuery();
      bool = bool.must(typeQuery);
    }
    if (request.getState() != null){
      var stateQuery = queryBuilder.keyword().onField("address.state").matching(request.getState()).createQuery();
      bool = bool.must(stateQuery);
    }
    if(request.getKeyword() != null) {
      var keywordQuery = queryBuilder.simpleQueryString().onFields("description","keyPoints","address.city").matching(request.getKeyword()).createQuery();
      bool = bool.must(keywordQuery);
    }


    //region facets

    final var typeFacetingRequest =
        queryBuilder
            .facet()
            .name("typeFaceting")
            .onField("typeofprop")
            .discrete()
            .orderedBy(FacetSortOrder.COUNT_DESC)
            .includeZeroCounts(false)
            .createFacetingRequest();
    final var standingFacetingRequest =
        queryBuilder
            .facet()
            .name("standingFaceting")
            .onField("propStanding")
            .discrete()
            .orderedBy(FacetSortOrder.COUNT_DESC)
            .includeZeroCounts(false)
            .createFacetingRequest();
    final var stateFacetingRequest =
        queryBuilder
            .facet()
            .name("stateFullFaceting")
            .onField("address.stateFull")
            .discrete()
            .orderedBy(FacetSortOrder.COUNT_DESC)
            .includeZeroCounts(false)
            .maxFacetCount(5)
            .createFacetingRequest();


    final var fullTextQuery =
        fullTextEntityManager.createFullTextQuery(bool.createQuery(), BienVendre.class);
    fullTextQuery.getFacetManager().enableFaceting(typeFacetingRequest);
    fullTextQuery.getFacetManager().enableFaceting(standingFacetingRequest);
    fullTextQuery.getFacetManager().enableFaceting(stateFacetingRequest);
    if (task.isCutoffsReady()) {
      final var cutoffs = task.getRangeCutoffs();
      final var priceFacetingRequest = queryBuilder.facet().name("priceFaceting")
        .onField("priceFacet").range().below(cutoffs.get(0)).from(cutoffs.get(0)).excludeLimit().to(cutoffs.get(1))
        .from(cutoffs.get(1)).excludeLimit().to(cutoffs.get(2)).from(cutoffs.get(2)).excludeLimit().to(cutoffs.get(3))
        .above(cutoffs.get(3)).excludeLimit().includeZeroCounts(false).orderedBy(FacetSortOrder.RANGE_DEFINITION_ORDER).maxFacetCount(5).createFacetingRequest();
      fullTextQuery.getFacetManager().enableFaceting(priceFacetingRequest);
    }
    var typeFacet =
        fullTextQuery.getFacetManager().getFacets("typeFaceting").stream()
            .map(f -> new SearchFacet(f.getValue(), f.getCount()))
            .collect(Collectors.toList());
    var standingFacet =
        fullTextQuery.getFacetManager().getFacets("standingFaceting").stream()
            .map(f -> new SearchFacet(f.getValue(), f.getCount()))
            .collect(Collectors.toList());
    var stateFacet =
        fullTextQuery.getFacetManager().getFacets("stateFullFaceting").stream()
            .map(f -> new SearchFacet(f.getValue(), f.getCount()))
            .collect(Collectors.toList());
    var priceFacet =
      fullTextQuery.getFacetManager().getFacets("priceFaceting").stream()
        .map(f -> new SearchFacet(f.getValue(), f.getCount()))
        .collect(Collectors.toList());
    //endregion
    Stream<BienVendre> resList =(Stream<BienVendre>)
        fullTextQuery
            .setFirstResult((int) paging.getOffset())
            .setMaxResults(paging.getPageSize())
          .setSort(getSort(request))
            .getResultStream();

    return SearchResponse.builder()
        .results(
            resList
                .map(item -> mapper.map(item, BienVendreResponse.class))
                .collect(Collectors.toList()))
        .typeFacet(typeFacet)
        .standingFacet(standingFacet)
        .stateFacet(stateFacet)
        .priceFacet(priceFacet)
        .build();
  }

  private Sort getSort(SearchRequest request) {
    if(request.getSort() == null){
      return Sort.RELEVANCE;
    }
    return switch (request.getSort()){
      case Price_High_To_Low -> new Sort(new SortField("price", SortField.Type.INT,true));
      case Price_Low_To_High -> new Sort(new SortField("price", SortField.Type.INT));
      case Newest_Arrivals -> new Sort(new SortField("createdAt", SortField.Type.STRING));
      default -> Sort.RELEVANCE;
    };
  }
}
