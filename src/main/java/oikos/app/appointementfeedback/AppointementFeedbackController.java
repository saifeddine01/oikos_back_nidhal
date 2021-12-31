package oikos.app.appointementfeedback;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Monitor;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

import static oikos.app.common.utils.NanoIDGenerator.NANOID_SIZE;

/** Created by Mohamed Haamdi on 07/05/2021. */
@Slf4j
@RestController
@AllArgsConstructor
@Validated
@Monitor
public class AppointementFeedbackController {
  private final AppointementFeedbackService appointementFeedbackService;
  private final ModelMapper mapper;

  @PreAuthorize("@appointementFeedbackService.canDo('ADD_FEEDBACK',#user.username,#user.username)")
  @PostMapping("/feedback")
  public AppointementFeedbackResponse addFeedback(
      @RequestBody AddAppointementFeedbackRequest req, @CurrentUser OikosUserDetails user) {
    var data = appointementFeedbackService.addFeedback(req, user.getUsername());
    final var res = mapper.map(data, AppointementFeedbackResponse.class);
    res.setReviewerID(user.getUsername());
    return res;
  }

  @PreAuthorize("@appointementFeedbackService.canDo('GET_ONE_FEEDBACK',#user.username,#feedbackID)")
  @GetMapping("/feedback/{feedbackID}")
  public AppointementFeedbackResponse getOneFeedback(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String feedbackID) {
    var data = appointementFeedbackService.getOneFeedback(feedbackID);
    final var res = mapper.map(data, AppointementFeedbackResponse.class);
    res.setReviewerID(user.getUsername());
    return res;
  }

  @PreAuthorize("@appointementFeedbackService.canDo('EDIT_FEEDBACK',#user.username,#feedbackID)")
  @PutMapping("/feedback/{feedbackID}")
  public AppointementFeedbackResponse editFeedback(
      @Validated @RequestBody EditAppointementFeedbackRequest req,
      @CurrentUser OikosUserDetails user,
      @PathVariable String feedbackID) {
    var data = appointementFeedbackService.editFeedback(feedbackID, req);
    final var res = mapper.map(data, AppointementFeedbackResponse.class);
    res.setReviewerID(user.getUsername());
    return res;
  }

  @PreAuthorize("@appointementFeedbackService.canDo('DELETE_FEEDBACK',#user.username,#feedbackID)")
  @DeleteMapping("/feedback/{feedbackID}")
  public DoneResponse deleteFeedback(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String feedbackID) {
    appointementFeedbackService.deleteFeedback(feedbackID);
    return new DoneResponse(
        MessageFormat.format("Feedback {0} has been deleted successfully.", feedbackID));
  }

  @PreAuthorize(
      "@appointementFeedbackService.canDo('LIST_FEEDBACKS_FOR_OWNER',#user.username,null)")
  @GetMapping("/feedback/myProperties")
  public Page<AppointementFeedbackResponse> listFeedbacksForOwner(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var res = appointementFeedbackService.listFeedbacksForOwner(user.getUsername(), paging);
    return res.map(feedback -> mapper.map(feedback, AppointementFeedbackResponse.class));
  }

  @PreAuthorize(
      "@appointementFeedbackService.canDo('LIST_FEEDBACKS_FOR_REVIEWER',#user.username,null)")
  @GetMapping("/feedback/myFeedbacks")
  public Page<AppointementFeedbackResponse> listFeedbacksForReviewer(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
    Pageable paging = PageRequest.of(page, size);
    var res = appointementFeedbackService.listFeedbacksForReviewer(user.getUsername(), paging);
    return res.map(feedback -> mapper.map(feedback, AppointementFeedbackResponse.class));
  }

  @PreAuthorize(
      "@appointementFeedbackService.canDo('LIST_FEEDBACKS_FOR_PROP',#user.username,#propID)")
  @GetMapping("/properties/{propID}/feedback")
  public Page<AppointementFeedbackResponse> listFeedbacksForProperty(
      @CurrentUser OikosUserDetails user,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String propID) {
    Pageable paging = PageRequest.of(page, size);
    var res = appointementFeedbackService.listFeedbacksForProperty(propID, paging);
    return res.map(feedback -> mapper.map(feedback, AppointementFeedbackResponse.class));
  }

  @PreAuthorize(
      "@appointementFeedbackService.canDo('GET_BY_APPOINTMENT',#user.username,#appointmentID)")
  @GetMapping("/appointment/{appointmentID}/feedback")
  public AppointementFeedbackResponse getFeedbackByAppointment(
      @CurrentUser OikosUserDetails user,
      @Size(min = NANOID_SIZE, max = NANOID_SIZE) @PathVariable String appointmentID) {
    return mapper.map(
        appointementFeedbackService.getFeedbackByAppointment(appointmentID),
        AppointementFeedbackResponse.class);
  }
}
