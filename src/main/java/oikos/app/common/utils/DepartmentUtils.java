package oikos.app.common.utils;

import oikos.app.departements.Department;

import java.util.LinkedHashMap;
import java.util.Map;

/** Created by Mohamed Haamdi on 25/06/2021 */
public class DepartmentUtils {
  private Map<Integer, Department> departments = new LinkedHashMap<>();

  public void add(Department dep, int number) {
    departments.put(number, dep);
  }

  public Department get(int number) {
    return departments.get(number);
  }
  private static DepartmentUtils single_instance = null;

  private DepartmentUtils() {}

  public static DepartmentUtils getInstance() {
    if (single_instance == null) {
      single_instance = new DepartmentUtils();
    }
    return single_instance;
  }
}
