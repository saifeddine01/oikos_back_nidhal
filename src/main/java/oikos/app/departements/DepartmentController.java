package oikos.app.departements;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.utils.Monitor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/** Created by Mohamed Haamdi on 21/04/2021. */
@Slf4j
@RestController
@AllArgsConstructor
@Validated
@Monitor
@RequestMapping("/public/departments")
public class DepartmentController {
  private final DepartmentService departmentService;

  @GetMapping
  public Collection<Department> getDepartmentList() {
    return departmentService.getDepartments().values();
  }
}
