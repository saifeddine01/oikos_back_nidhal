package oikos.app.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Facet;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Indexed(interceptor = IndexWhenApprovedInterceptor.class)
@AnalyzerDef(
    name = "customanalyzer",
    tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
    filters = {
      @TokenFilterDef(factory = LowerCaseFilterFactory.class),
      @TokenFilterDef(factory = ASCIIFoldingFilterFactory.class),
      @TokenFilterDef(
          factory = SnowballPorterFilterFactory.class,
          params = {@Parameter(name = "language", value = "French")})
    })
@Analyzer(definition = "customanalyzer")
@Entity(name = "bienavendre")
public class BienVendre {
  @Id private String id;

  @Enumerated(EnumType.STRING)
  private Status status;

  @Field
  @SortableField
  @DateBridge(resolution = Resolution.DAY)
  private Instant createdAt;

  @Field(analyze = Analyze.NO, name = "typeofprop")
  @Facet(name = "typeofprop", forField = "typeofprop")
  private String typeofprop;

  @Field(analyze = Analyze.NO, name = "propStanding")
  @Facet(name = "propStanding", forField = "propStanding")
  private String propStanding;

  @Field private double allArea;

  @IndexedEmbedded @Embedded private PropertyAddress address;

  @Field(name = "description")
  private String description;

  @Field(name = "keyPoints")
  private String keyPoints;

  @Field private String yearConstruction;

  @Field(analyze = Analyze.NO, name = "price")
  @Facet(name = "priceFacet", forField = "price")
  private double price;
}
