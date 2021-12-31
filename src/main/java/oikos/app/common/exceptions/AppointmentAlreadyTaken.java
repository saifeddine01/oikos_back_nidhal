package oikos.app.common.exceptions;

public class AppointmentAlreadyTaken extends Exception {
  public AppointmentAlreadyTaken(String errorMessage) {
    super(errorMessage);
  }
}
