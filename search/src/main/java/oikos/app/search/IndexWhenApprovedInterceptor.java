package oikos.app.search;

import org.hibernate.search.indexes.interceptor.EntityIndexingInterceptor;
import org.hibernate.search.indexes.interceptor.IndexingOverride;

public class IndexWhenApprovedInterceptor implements EntityIndexingInterceptor<BienVendre> {
  @Override
  public IndexingOverride onAdd(BienVendre bienVendre) {
    if (bienVendre.getStatus().equals(Status.Approved)) {
      return IndexingOverride.APPLY_DEFAULT;
    }
    return IndexingOverride.SKIP;
  }

  @Override
  public IndexingOverride onUpdate(BienVendre bienVendre) {
    if (bienVendre.getStatus().equals(Status.Approved)) {
      return IndexingOverride.UPDATE;
    }
    return IndexingOverride.REMOVE;
  }

  @Override
  public IndexingOverride onDelete(BienVendre bienVendre) {
    return IndexingOverride.APPLY_DEFAULT;
  }

  @Override
  public IndexingOverride onCollectionUpdate(BienVendre bienVendre) {
    return onUpdate(bienVendre);
  }
}
