package oikos.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.utils.DepartmentUtils;
import oikos.app.departements.Department;
import oikos.app.departements.DepartmentService;
import oikos.app.common.exceptions.InternalServerError;
import oikos.app.common.models.MyPropertyType;
import oikos.app.notifications.NotificationService;
import oikos.app.common.repos.PropTypeRepo;
import oikos.app.common.services.AddTypeService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mohamed Haamdi on 20/03/2021.
 */
@Component @Slf4j @RequiredArgsConstructor public class FirstRunInitializer
  implements ApplicationListener<ContextRefreshedEvent> {
  private final LocalTestInitializer initializer;
  private final DepartmentService service;
  private final NotificationService notificationService;
  private final Path root = Paths.get("upload");
  private final AddTypeService addtype;
  public static Map<Integer, String> typeof = new HashMap<>();
  private final PropTypeRepo propRepo;

  @Value("classpath:data/departements.json") Resource resourceFile;

  @SneakyThrows @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    log.info("Checking for database initialisation state.");
    log.info("We start by populating the department list.");
    
    if(!propRepo.checkExist(1)) {
      log.info("Creating types for property");
      createPropType(1,"Appartement");
      createPropType(2,"Maison");
      createPropType(3,"Villa");
      createPropType(4,"Atelier");
    }
    for (MyPropertyType type : addtype.findall()) {
        typeof.put(type.getCode(), type.getName());
      }
    var objectMapper = new ObjectMapper();
    Department[] departments = objectMapper
      .readValue(objectMapper.createParser(resourceFile.getInputStream()),
        Department[].class);
    for (var i = 0; i < departments.length; i++) {
      departments[i].setId(i);
      service.add(departments[i], i);
      DepartmentUtils.getInstance().add(departments[i], i);
    }
    //TODO this should be done in the prop service instead
    log.info("Creating folder for uploads");
    try {
      if (!Files.exists(root))
        Files.createDirectory(root);
    } catch (IOException e) {
      log.error("firstrun.onapplicationevent", e);
      throw new InternalServerError("Could not initialize folder for upload!");
    }
    // We go through our notifications to schedule any that haven't been sent yet.
    var scheduledNotifications =
      notificationService.getScheduledNotifications();
    scheduledNotifications.stream()
      .filter(notification -> notification.getInstant().isBefore(Instant.now()))
      .forEach(notificationService::sendPastNotification);
    scheduledNotifications.stream()
      .filter(n -> n.getInstant().isAfter(Instant.now()))
      .forEach(notificationService::scheduleNotification);
    // We ensure that our test initialisation is done last
    initializer.onApplicationEvent(contextRefreshedEvent);
  }
  private void createPropType(int code ,String name) {
    MyPropertyType type=new MyPropertyType();
    type.setCode(code);
    type.setName(name);
    propRepo.save(type);
    log.info("adding "+name +" as type for property");
  }
}
