package oikos.app.appointementfeedback;

import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Appointment;
import oikos.app.common.models.BienVendre;
import oikos.app.common.repos.AppointmentRepo;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.notifications.NotificationService;
import oikos.app.users.User;
import oikos.app.users.UserRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Created by Mohamed Haamdi on 07/05/2021. */
@ExtendWith(MockitoExtension.class)
class AppointmentAppointementFeedbackServiceTest {
  @Mock private BienaVendreRepo propRepo;
  @Mock private AppointmentRepo appointmentRepo;
  @Mock private UserRepo userRepo;
  @Mock private AppointementFeedbackRepo appointementFeedbackRepo;
  @Mock private ModelMapper mapper;
  @Mock private NotificationService notificationService;
  @InjectMocks private AppointementFeedbackService underTest;

  @Test
  void listFeedbacksForOwner() {
    underTest.listFeedbacksForOwner("id", PageRequest.of(0, 10));
    verify(appointementFeedbackRepo).getAllFeedbackByOwnerID("id", PageRequest.of(0, 10));
  }

  @Test
  void listFeedbacksForReviewer() {
    underTest.listFeedbacksForReviewer("id", PageRequest.of(0, 10));
    verify(appointementFeedbackRepo).getFeedbackByReviewerID("id", PageRequest.of(0, 10));
  }

  @Test
  void listFeedbacksForProperty() {
    underTest.listFeedbacksForProperty("id", PageRequest.of(0, 10));
    verify(appointementFeedbackRepo).getFeedbackByPropID("id", PageRequest.of(0, 10));
  }

  @Test
  void getFeedbackByAppointment() {
    // given
    when(appointementFeedbackRepo.findFeedbackByAppointmentId("invalid"))
        .thenReturn(Optional.empty());
    when(appointementFeedbackRepo.findFeedbackByAppointmentId("valid"))
        .thenReturn(Optional.of(AppointmentFeedback.builder().id("valid").build()));
    // when
    var res = underTest.getFeedbackByAppointment("valid");
    // then
    assertThatThrownBy(() -> underTest.getFeedbackByAppointment("invalid"))
        .isInstanceOf(EntityNotFoundException.class);
    assertThat(res).isNotNull();
    assertThat(res.getId()).isEqualTo("valid");
  }

  @Nested
  class addAppointmentFeedback {
    @Test
    void addFeedbackShouldThrowWhenPropDoesntExist() {
      var req = new AddAppointementFeedbackRequest(null, "notValid", null, null, null, null);
      var reviewerID = "userID";
      when(propRepo.existsById(req.getPropertyID())).thenReturn(false);

      assertThatThrownBy(() -> underTest.addFeedback(req, reviewerID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addFeedbackShouldThrowWhenAppointmentDoesntExist() {
      var req = new AddAppointementFeedbackRequest("notValid", "propID", null, null, null, null);
      var reviewerID = "userID";
      when(propRepo.existsById(req.getPropertyID())).thenReturn(true);
      when(appointmentRepo.existsById(req.getAppointmentID())).thenReturn(false);

      assertThatThrownBy(() -> underTest.addFeedback(req, reviewerID))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addFeedbackShouldThrowWhenAppointmentAlreadyHasFeedback() {
      var req = new AddAppointementFeedbackRequest("appID", "propID", null, null, null, null);
      var reviewerID = "userID";
      when(propRepo.existsById(req.getPropertyID())).thenReturn(true);
      when(appointmentRepo.existsById(req.getAppointmentID())).thenReturn(true);
      when(appointementFeedbackRepo.existsByAppointmentId("appID")).thenReturn(true);

      assertThatThrownBy(() -> underTest.addFeedback(req, reviewerID))
          .isInstanceOf(BaseException.class)
          .hasMessageContaining("already has feedback");
    }

    @Test
    void addFeedBackSucceeds() {
      var req =
          new AddAppointementFeedbackRequest(
              "appID", "propID", Intrest.WANT_TO_BUY, "op", "pr", "op");
      var reviewerID = "userID";
      when(propRepo.existsById(req.getPropertyID())).thenReturn(true);
      when(appointmentRepo.existsById(req.getAppointmentID())).thenReturn(true);
      when(propRepo.getOne(req.getPropertyID()))
          .thenReturn(
              BienVendre.builder().userId(new User("userID")).id(req.getPropertyID()).build());
      when(appointmentRepo.getOne(req.getAppointmentID()))
          .thenReturn(Appointment.builder().id(req.getAppointmentID()).build());
      when(userRepo.getOne(reviewerID)).thenReturn(new User(reviewerID));
      when(appointementFeedbackRepo.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
      var res = underTest.addFeedback(req, reviewerID);
      assertThat(res).isNotNull();
      verify(notificationService)
          .addNotification(any(String.class), any(String.class), any(String.class));
    }
  }

  @Nested
  class getOneAppointmentFeedback {
    @Test
    void getOneNotFound() {
      // given
      when(appointementFeedbackRepo.findById("id")).thenReturn(Optional.empty());
      // then
      assertThatThrownBy(() -> underTest.getOneFeedback("id"))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getOneSuccess() {
      // given
      final var id = "id";
      var expected = AppointmentFeedback.builder().id(id).build();
      when(appointementFeedbackRepo.findById(id)).thenReturn(Optional.of(expected));
      // when
      var res = underTest.getOneFeedback(id);
      // then
      assertThat(res).isEqualTo(expected);
    }
  }

  @Nested
  class editAppointmentFeedback {
    @Test
    void editFeedbackNotFound() {
      // given
      when(appointementFeedbackRepo.findById("id")).thenReturn(Optional.empty());
      // when
      // then
      assertThatThrownBy(() -> underTest.editFeedback("id", any()))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void editFeedbackSuccess() {
      // given
      var dto = new EditAppointementFeedbackRequest(Intrest.WANT_TO_BUY, "op", "pr", "^roc");
      final Optional<AppointmentFeedback> feedback =
          Optional.of(
              AppointmentFeedback.builder()
                  .id("id")
                  .property(BienVendre.builder().userId(new User("userID")).build())
                  .build());
      when(appointementFeedbackRepo.findById("id")).thenReturn(feedback);
      when(appointementFeedbackRepo.save(any(AppointmentFeedback.class)))
          .thenAnswer(AdditionalAnswers.returnsFirstArg());
      // when
      var res = underTest.editFeedback("id", dto);
      // then
      verify(mapper).map(dto, feedback.get());
      verify(notificationService)
          .addNotification(any(String.class), any(String.class), any(String.class));
      assertThat(res).isNotNull();
    }
  }

  @Nested
  class deleteAppointmentFeedback {
    @Test
    void deleteFeedbackNotFound() {
      // given
      when(appointementFeedbackRepo.existsById("id")).thenReturn(false);
      // when
      // then
      assertThatThrownBy(() -> underTest.deleteFeedback("id"))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteFeedbackSuccess() {
      // given
      when(appointementFeedbackRepo.existsById("id")).thenReturn(true);
      // when
      underTest.deleteFeedback("id");
      // then
      verify(appointementFeedbackRepo).deleteById("id");
    }
  }

  @Nested
  class canDo {
    @ParameterizedTest
    @EnumSource(
        value = AppointementFeedbackService.FeedbackMethods.class,
        names = {
          AppointementFeedbackService.FeedbackMethods.Names.ADD_FEEDBACK,
          AppointementFeedbackService.FeedbackMethods.Names.LIST_FEEDBACKS_FOR_OWNER,
          AppointementFeedbackService.FeedbackMethods.Names.LIST_FEEDBACKS_FOR_REVIEWER,
          AppointementFeedbackService.FeedbackMethods.Names.GET_BY_APPOINTMENT
        })
    void canAddFeedbackAndListFeedbacksForOwnerAndReviewerAndGetByAppointment(
        AppointementFeedbackService.FeedbackMethods method) {
      // When
      var res = underTest.canDo(method, "userID", "objectID");
      // Then
      assertThat(res).isTrue();
    }

    @ParameterizedTest
    @EnumSource(
        value = AppointementFeedbackService.FeedbackMethods.class,
        names = {
          AppointementFeedbackService.FeedbackMethods.Names.EDIT_FEEDBACK,
          AppointementFeedbackService.FeedbackMethods.Names.DELETE_FEEDBACK
        })
    void canEditAndDeleteFeedback(AppointementFeedbackService.FeedbackMethods method) {
      // Given
      when(appointementFeedbackRepo.getOne("objectID"))
          .thenReturn(AppointmentFeedback.builder().reviewer(new User("userID")).build());
      when(appointementFeedbackRepo.getOne("objectID2"))
          .thenReturn(AppointmentFeedback.builder().reviewer(new User("userID2")).build());
      when(appointementFeedbackRepo.getOne("objectID3")).thenThrow(EntityNotFoundException.class);
      // When
      var res = underTest.canDo(method, "userID", "objectID");
      var res2 = underTest.canDo(method, "userID", "objectID2");
      // Then
      assertThat(res).isTrue();
      assertThat(res2).isFalse();
      assertThatThrownBy(() -> underTest.canDo(method, "userID", "objectID3"))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void canGetOneFeedback() {
      when(appointementFeedbackRepo.getOne("objectID"))
          .thenReturn(AppointmentFeedback.builder().reviewer(new User("userID")).build());
      when(appointementFeedbackRepo.getOne("objectID2"))
          .thenReturn(
              AppointmentFeedback.builder()
                  .reviewer(new User("userID99"))
                  .property(BienVendre.builder().userId(new User("userID")).build())
                  .build());
      var res =
          underTest.canDo(
              AppointementFeedbackService.FeedbackMethods.GET_ONE_FEEDBACK, "userID", "objectID");
      var res2 =
          underTest.canDo(
              AppointementFeedbackService.FeedbackMethods.GET_ONE_FEEDBACK, "userID", "objectID2");
      assertThat(res).isTrue();
    }

    @Test
    void canListFeedbacksForProp() {
      // given
      when(propRepo.getOne("objectID"))
          .thenReturn(BienVendre.builder().userId(new User("userID")).build());
      when(propRepo.getOne("objectID2"))
          .thenReturn(BienVendre.builder().userId(new User("userID2")).build());
      // when
      var res =
          underTest.canDo(
              AppointementFeedbackService.FeedbackMethods.LIST_FEEDBACKS_FOR_PROP,
              "userID",
              "objectID");
      var res2 =
          underTest.canDo(
              AppointementFeedbackService.FeedbackMethods.LIST_FEEDBACKS_FOR_PROP,
              "userID",
              "objectID2");
      // then
      assertThat(res).isTrue();
      assertThat(res2).isFalse();
    }
  }
}
