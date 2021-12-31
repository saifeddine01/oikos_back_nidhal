package oikos.app.departements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentServiceTest {

  private DepartmentService service;

  @BeforeEach
  void setUp() {
    service = new DepartmentService();
  }

  @Test
  void add() {
    // Given
    var dep = Department.builder().id(0).code("test").name("test").build();
    // When
    service.add(dep, dep.getId());
    // Then
    assertThat(service.getDepartments()).containsEntry(0, dep);
    assertThat(service.getDepartments().get(0).getName()).isEqualTo("test");
    assertThat(service.getDepartments().get(0).getCode()).isEqualTo("test");
  }

  @Test
  void get() {
    // Given
    var dep = Department.builder().id(0).code("test").name("test").build();
    Map<Integer, Department> map = Map.of(0, dep);
    service.setDepartments(map);
    // When
    var department = service.get(0);
    // Then
    assertThat(department).isEqualTo(dep);
  }
}
