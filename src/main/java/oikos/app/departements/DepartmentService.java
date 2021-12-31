package oikos.app.departements;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Mohamed Haamdi on 21/04/2021.
 */
@Service @Getter @Setter public class DepartmentService {
  private Map<Integer, Department> departments = new LinkedHashMap<>();

  public void add(Department dep, int number) {
    departments.put(number, dep);
  }

  public Department get(int number) {
    return departments.get(number);
  }
}
