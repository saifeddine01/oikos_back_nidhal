package oikos.app.common.utils;

/** Created by Mohamed Haamdi on 19/04/2021. */
public interface Authorizable<T extends Enum> {
  public boolean canDo(T methodName, String userID, String objectID);
}
