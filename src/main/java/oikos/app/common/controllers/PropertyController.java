package oikos.app.common.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import oikos.app.common.entityResponses.LikesResponse;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.Appointment;
import oikos.app.common.models.BienVendre;
import oikos.app.common.models.PropertyFile;
import oikos.app.common.models.Status;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.common.repos.PropertyFileRepo;
import oikos.app.common.request.BienaVendreResponse;
import oikos.app.common.request.EstimationRequest;
import oikos.app.common.request.LikesRequest;
import oikos.app.common.request.PropertyEditRequest;
import oikos.app.common.request.PropertyRequest;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.services.PropertyLikesService;
import oikos.app.common.services.PropertyService;
import oikos.app.common.utils.PdfGenaratorUtil;
import oikos.app.notifications.CreateNotificationRequest;
import oikos.app.notifications.NotificationService;
import oikos.app.security.CurrentUser;
import oikos.app.security.OikosUserDetails;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/properties")
@Tag(name = "properties-Controller")
public class PropertyController {
	private final PropertyService service;
	private final BienaVendreRepo repo;
	private final PropertyFileRepo fileRepo;
	private final PropertyLikesService likes;
	private final Path root = Paths.get("upload");
	private final Path pdfroot = Paths.get("upload/pdf/");

	private final NotificationService notif;
	private static String appLink = "properties/";

	@Autowired
	private PdfGenaratorUtil util;

	@GetMapping(value = "/generate/pdf/{idbien}")
	public byte[] generatePdfWithResponse(Model model, @PathVariable("idbien") String idbien,
			@CurrentUser OikosUserDetails user) {
		var filename = service.generatePdfFile(model, idbien);
		return service.returnPdfMediaType(pdfroot, filename);
	}

	@GetMapping(value = "/generatePDF/{idbien}")
	public String generatePdfWithoutResponse(Model model, @PathVariable("idbien") String idbien,
			@CurrentUser OikosUserDetails user) {
		return service.generatePdfFile(model, idbien);
	}

	@GetMapping(value = "/listall")
	@ResponseBody
	public List<String> listAllPDFfiles(Model model, @CurrentUser OikosUserDetails user) {
		return service.listallPdfFiles(pdfroot);
	}

	@RequestMapping(value = "/pdf/{fileName}", produces = "application/pdf")
	@ResponseBody
	public byte[] getFile(Model model, @PathVariable("fileName") String fileName, @CurrentUser OikosUserDetails user) {
		return service.returnPdfMediaType(pdfroot, fileName);
	}

	
	 @GetMapping("/search/status")
	  public List<BienVendre> getPropertyByStatus(@CurrentUser OikosUserDetails user,
	    @RequestParam(required = false) String status) {
	    return service.getienByStatus(status);
	  }
	 
	 @GetMapping("/search/city")
	  public List<BienVendre> getPropertyByCity(@CurrentUser OikosUserDetails user,
	    @RequestParam(required = false) String city) {
	    return service.getienByCity(city);
	  }
	 
	 @GetMapping("/myproperties/search/status")
	  public List<BienVendre> getPropertyByUserandStatus(@CurrentUser OikosUserDetails user,
	    @RequestParam(required = false) String status) {
	    return service.getmesBienStatus(user.getUser(), status);
	  }

	@GetMapping(value = "/getmypdf/{id}", produces = "application/pdf")
	public byte[] getMyPdfWithPropertyId(@PathVariable("id") String id, @CurrentUser OikosUserDetails user) {
		String fileName = service.findmypdffile(pdfroot, id);

		return service.returnPdfMediaType(pdfroot, fileName);
	}

	@GetMapping("/downloadpdf/{filename}")
	public ResponseEntity<Resource> downlloadPdf(@CurrentUser OikosUserDetails user,
			@PathVariable("filename") String filename) {
		Resource file = service.loadPdf(filename, pdfroot);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@PreAuthorize("@propertyService.canDo('ADD_PROPERTY_WITHOUT_IMAGE',#user.username,#user.username)")
	@PostMapping("/create")
	public BienVendre addPropWithoutPictures(@Valid @RequestBody PropertyRequest dto,
			@CurrentUser OikosUserDetails user) {
		
		try {
			return service.createBienWithouPictures(dto, user.getUser());
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.ACCEPTED, "Please enter a valid request", ex);
		}
	}

	@PreAuthorize("@propertyService.canDo('ADD_PROP_WITHOUT_PICTURES_RESPONSE',#user.username,#user.username)")
	@PostMapping("/createprop")
	public BienaVendreResponse addPropWithouPicturesEditResponses(@Valid @RequestBody PropertyRequest dto,
			@CurrentUser OikosUserDetails user) {
		try {
			return service.createBienWithouPicturesResponses(dto, user.getUser());
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.ACCEPTED, "Please enter a valid request", ex);
		}
	}

	@PreAuthorize("@propertyService.canDo('DELETE_PROPERTY',#user.username,#idBien)")
	@DeleteMapping(value = "/{idBien}")
	public DoneResponse deleteBien(@PathVariable("idBien") String idBien, @CurrentUser OikosUserDetails user) {
		if (repo.existsById(idBien)) {
			repo.deleteById(idBien);
			return new DoneResponse("property with id " + idBien + "has been deleted with success");
		} else {
			return new DoneResponse("property with id " + idBien + "could not be found ");
		}
	}

	@PreAuthorize("@propertyService.canDo('GET_ALL',#user.username,#user.username)")
	@GetMapping("/")
	public Page<BienVendre> getAllProperties(@RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "allArea") String sortBy,
			@CurrentUser OikosUserDetails user) {
		return service.getAllBien(pageNo, pageSize, sortBy);
	}

	@PreAuthorize("@propertyService.canDo('ADD_PROPERTY_WITH_IMAGE',#user.username,#user.username)")
	@PostMapping("/uploadwith")
	public BienVendre uploadFileWithShort(@RequestPart("file") MultipartFile file,
			@RequestPart("json") PropertyRequest dto, @CurrentUser OikosUserDetails user) {
		return service.savewithFilesShort(dto, file, user.getUser());
	}

	@PreAuthorize("@propertyService.canDo('DELETE_IMAGE_PROP',#user.username,#idFile)")
	@DeleteMapping(value = "/img/{idFile}")
	public DoneResponse deleteImageProp(@PathVariable("idFile") String idFile, @CurrentUser OikosUserDetails user) {
		return service.deletePicture(idFile);
	}
	
	
	@DeleteMapping(value = "/img/")
	public DoneResponse deleteImageProp(@RequestBody List<String> idlist, @CurrentUser OikosUserDetails user) {
		return service.deleteSomePictureForProp(idlist);
	}
	
	//@PreAuthorize("@propertyService.canDo('DELETE_IMAGE_PROP',#user.username,#idFile)")
		@DeleteMapping(value = "/img/all/{idProp}")
		public DoneResponse deleteAllImagesForProp(@PathVariable("idProp") String idProp, @CurrentUser OikosUserDetails user) {
			return service.deleteAllPictureForProp(idProp);
		}

	@PreAuthorize("@propertyService.canDo('ADD_IMAGE_TO_PROPERTY',#user.username,#id)")
	@PostMapping("/upload/{id}")
	public ResponseEntity<DoneResponse> addPictureToProp(@RequestParam("file") MultipartFile file,
			@PathVariable String id, @CurrentUser OikosUserDetails user) {
		return service.save(file, id);
	}

	@PreAuthorize("@propertyService.canDo('ADD_PROPERTY_WITH_IMAGES',#user.username,#user.username)")
	@PostMapping("/uploadAll")
	public BienVendre addPropWithFiles(@RequestPart("file") MultipartFile[] file,
			@RequestPart("json") PropertyRequest dto, @CurrentUser OikosUserDetails user) {
		return service.savePropWithFiles(file, dto, user.getUser());
	}

	@PreAuthorize("@propertyService.canDo('ADD_IMAGES_TO_PROPERTY',#user.username,#id)")
	@PostMapping("/uploadFiles/{id}")
	public ResponseEntity<DoneResponse> addPicturesToProp(@RequestParam("file") MultipartFile[] files,@CurrentUser OikosUserDetails user,
			@PathVariable String id) {
		return service.saveFiles(files, id);
	}

	@PreAuthorize("@propertyService.canDo('APPROVE_PROPERTY',#user.username,#idBien)")
	@PostMapping("/approve/{idBien}")
	public DoneResponse approveBienRequest(@PathVariable String idBien,@CurrentUser OikosUserDetails user) {
		try {
			BienVendre bien = repo.findById(idBien)
					.orElseThrow(() -> new EntityNotFoundException(BienVendre.class, idBien));
			bien.setStatus(Status.Approved);
			repo.save(bien);
			CreateNotificationRequest notifreq = CreateNotificationRequest.builder()
					.content("Your property with title :" + bien.getDescription() + "has been approved")
					.userId(bien.getUserId().getId()).lien(appLink + bien.getId()).build();
			notif.addNotification(notifreq);
			return new DoneResponse("Property with Id : " + idBien + "has been approved");
		} catch (Exception exc) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id bien not found", exc);
		}
	}

	@PreAuthorize("@propertyService.canDo('REJECT_PROPETY',#user.username,#idBien)")
	@PostMapping("/reject/{idBien}")
	public DoneResponse rejectBienRequest(@PathVariable String idBien,@CurrentUser OikosUserDetails user) {
		try {
			BienVendre bien = repo.findById(idBien)
					.orElseThrow(() -> new EntityNotFoundException(BienVendre.class, idBien));
			bien.setStatus(Status.Rejected);
			repo.save(bien);
			CreateNotificationRequest notifreq = CreateNotificationRequest.builder()
					.content("Your property with title :" + bien.getDescription() + "has been rejected")
					.userId(bien.getUserId().getId()).lien(appLink + bien.getId()).build();
			notif.addNotification(notifreq);
			return new DoneResponse("Property with Id : " + idBien + "has been rejected");
		} catch (Exception exc) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id bien not found", exc);
		}
	}

	@PreAuthorize("@propertyService.canDo('EDIT_IMAGES_TO_PROPERTY',#user.username,#idBien)")
	@PutMapping("/{idBien}/files/{imageId}")
	public ResponseEntity<DoneResponse> editPropPicture(@PathVariable("idBien") String idBien,
			@PathVariable("imageId") String imageId, @CurrentUser OikosUserDetails user) {
		String message = "Could change the cover picture of your property";

		try {
			BienVendre bien = repo.findById(idBien)
					.orElseThrow(() -> new EntityNotFoundException(BienVendre.class, idBien));
			PropertyFile file = fileRepo.findById(imageId)
					.orElseThrow(() -> new EntityNotFoundException(PropertyFile.class, imageId));
			bien.setMainPic(file.getFileName());
			repo.save(bien);
			message = "Success: the cover picture of property have been changed to : " + file.getOriginalName();
			return ResponseEntity.status(HttpStatus.OK).body(new DoneResponse(message));
		} catch (Exception exc) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new DoneResponse(message));
		}
	}

	@PreAuthorize("@propertyService.canDo('EDIT_PROPERTY',#user.username,#idBien)")
	@PutMapping("/{idBien}")
	public BienVendre editProp(@PathVariable("idBien") String idBien, @Valid @RequestBody PropertyEditRequest dto,
			@CurrentUser OikosUserDetails user) {
		return service.editBien(dto, idBien, user.getUser());
	}

	@PreAuthorize("@propertyService.canDo('EDIT_PROPERTY',#user.username,#user.username)")
	@GetMapping("/download/{fileId}")
	public ResponseEntity<Resource> getFile(@CurrentUser OikosUserDetails user, @PathVariable String fileId) {
		Resource file = service.load(fileId);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@PreAuthorize("@propertyService.canDo('MY_PROPERTIES',#user.username,#user.username)")
	@GetMapping("/myproperties")
	public List<BienVendre> mesBien(@CurrentUser OikosUserDetails user) {
		return service.getmesBien(user.getUser());
	}

	@PreAuthorize("@propertyService.canDo('GET_ONE_PROPERTY',#user.username,#user.username)")
	@GetMapping("/{idBien}")
	public BienVendre findById(@PathVariable("idBien") String idBien, @CurrentUser OikosUserDetails user) {
		return service.getBienById(idBien);
	}

	 @PreAuthorize("@propertyService.canDo('FIND_IMAGES',#user.username,#idBien)")
	@GetMapping("/{idBien}/files")
	public List<PropertyFile> findImages(@PathVariable String idBien, @CurrentUser OikosUserDetails user) {
		return service.getMyImages(idBien);
	}

	 
	 
	@GetMapping(value = "/mediatype-image/{id}", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE,
			MediaType.IMAGE_GIF_VALUE })
	public @ResponseBody byte[] getImageWithMediaType(@PathVariable String id,@CurrentUser OikosUserDetails user) throws IOException {
		InputStream in = service.load(id).getInputStream();
		return IOUtils.toByteArray(in);
	}

	// @PreAuthorize("@propertyService.canDo('ESTIMER_PROPERTY',#user.username,#user.username)")
	
	@PostMapping("/estimer")
	public Object estimerBien(@RequestBody EstimationRequest req) {
		String fooResourceUrl = "http://95.141.37.156/immobilier/appartements2";
		String url = "https://deep-learning.hotline.direct:8020/estimationAppartement.php";
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<EstimationRequest> request = new HttpEntity<>(req);
		ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

		return response.getBody();
	}
	
	@PostMapping("/estimer/{idprop}")
	public List<Float> estimate(@PathVariable("idprop")String idprop){
		int price=100000;
		BienVendre bien = repo.findById(idprop)
				.orElseThrow(() -> new EntityNotFoundException(BienVendre.class, idprop));
		if(bien.getPrice()!=0) {
			price=bien.getPrice();
}
	List<Float> estimateprice=new ArrayList<>();
	estimateprice.add((float) (price*0.8));
	estimateprice.add((float) (price*0.9));
	estimateprice.add((float) price);
	return estimateprice;
	}

	@PreAuthorize("@propertyLikesService.canDo('ADD_LIKE',#user.username,#dto.propId)")
	@PostMapping("/like")
	public @Valid LikesResponse addLike(@Valid @RequestBody LikesRequest dto, @CurrentUser OikosUserDetails user) {
		return likes.addLike(dto, user.getUser());
	}

@PreAuthorize("@propertyLikesService.canDo('FIND_ALL_BY_PROPERTY',#user.username,#idprop)")
	@GetMapping("/{idprop}/allLikes")
	public @Valid List<LikesResponse> findallByProp(@PathVariable("idprop") String idprop,
			@CurrentUser OikosUserDetails user) {
		return likes.findallByProp(idprop);
	}

	@PreAuthorize("@propertyLikesService.canDo('MY_LIKES',#user.username,#user.username)")
	@GetMapping("/mylikes")
	public List<LikesResponse> mylikes(@RequestParam(required = false) String liketype,
			@CurrentUser OikosUserDetails user) {
		return likes.myLikes(user.getUser().getId(), liketype);
	}

	@PreAuthorize("@propertyLikesService.canDo('MOST_LIKE',#user.username,#user.username)")
	@GetMapping("/most")
	public List<String> mostLikedProperty(@RequestParam(required = false) String liketype,
			@CurrentUser OikosUserDetails user) {
		return likes.mostlike(liketype);
	}

	@PreAuthorize("@propertyLikesService.canDo('STATISTIC',#user.username,#user.username)")
	@GetMapping("/statstique")
	public List<Map<String, Integer>> statistic(@RequestParam(required = false) String liketype,
			@CurrentUser OikosUserDetails user) {
		return likes.statistic(liketype);
	}
}
