package oikos.app.common.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.FirstRunInitializer;
import oikos.app.common.configurations.AppProperties;
import oikos.app.common.exceptions.BaseException;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.exceptions.InternalServerError;
import oikos.app.common.models.Appointment;
import oikos.app.common.models.BienVendre;
import oikos.app.common.models.MyPropertyType;
import oikos.app.common.models.PropertyFile;
import oikos.app.common.models.Status;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.common.repos.PropertyFileRepo;
import oikos.app.common.request.BienaVendreResponse;
import oikos.app.common.request.PropertyEditRequest;
import oikos.app.common.request.PropertyRequest;
import oikos.app.common.responses.DoneResponse;
import oikos.app.common.utils.Authorizable;
import oikos.app.common.utils.NanoIDGenerator;
import oikos.app.common.utils.PdfGenaratorUtil;
import oikos.app.users.Role;
import oikos.app.users.User;
import oikos.app.users.UserRepo;

//@AllArgsConstructor
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PropertyService implements Authorizable<PropertyService.PropertyMethods> {
	private final BienaVendreRepo repo;
	private final PropertyFileRepo fileRepo;
	private final Path root = Paths.get("upload");
	private final UserRepo usersRepo;
	private final ModelMapper modelMapper;
	private PdfGenaratorUtil util;
	private final AppProperties appProperties;
	private Path pdfDir;
	private final AddTypeService serviceAdd;

	@ToString
	enum PropertyMethods {
		ADD_PROPERTY_WITHOUT_IMAGE(Names.ADD_PROPERTY_WITHOUT_IMAGE),
		ADD_PROPERTY_WITH_IMAGE(Names.ADD_PROPERTY_WITH_IMAGE),
		ADD_PROPERTY_WITH_IMAGES(Names.ADD_PROPERTY_WITH_IMAGES), DELETE_PROPERTY(Names.DELETE_PROPERTY),
		EDIT_PROPERTY(Names.EDIT_PROPERTY), GET_ONE_PROPERTY(Names.GET_ONE_PROPERTY),
		LIST_PROPERTY_FOR_OWNER(Names.LIST_PROPERTY_FOR_OWNER), FIND_IMAGES(Names.FIND_IMAGES),
		APPROVE_PROPERTY(Names.APPROVE_PROPERTY), REJECT_PROPETY(Names.REJECT_PROPETY),
		ADD_IMAGE_TO_PROPERTY(Names.ADD_IMAGE_TO_PROPERTY), ADD_IMAGES_TO_PROPERTY(Names.ADD_IMAGES_TO_PROPERTY),
		ESTIMER_PROPERTY(Names.ESTIMER_PROPERTY), GET_ALL(Names.GET_ALL), DELETE_IMAGE_PROP(Names.DELETE_IMAGE_PROP),
		ADD_LIKE(Names.ADD_LIKE), FIND_ALL_BY_PROPERTY(Names.FIND_ALL_BY_PROPERTY), MY_LIKES(Names.MY_LIKES),
		MOST_LIKE(Names.MOST_LIKE), STATISTIC(Names.STATISTIC), EDIT_IMAGES_TO_PROPERTY(Names.EDIT_IMAGES_TO_PROPERTY),
		EDIT_PICTURES_PROPERTY(Names.EDIT_PICTURES_PROPERTY),MY_PROPERTIES(Names.MY_PROPERTIES),
		GENERATE_PDF_WITH_PDF_RESPONSE(Names.GENERATE_PDF_WITH_PDF_RESPONSE),
		GENERATE_PDF_WITHOUT_PDF_RESPONSE(Names.GENERATE_PDF_WITHOUT_PDF_RESPONSE),
		GET_FILE_BY_NAME(Names.GET_FILE_BY_NAME),
		ADD_PROP_WITHOUT_PICTURES_RESPONSE(Names.ADD_PROP_WITHOUT_PICTURES_RESPONSE),
		GET_IMAGE_MEDIA_TYPE_RESPONSE(Names.GET_IMAGE_MEDIA_TYPE_RESPONSE),
		LIST_ALL_PDF_FILES(Names.LIST_ALL_PDF_FILES), DOWNLOAD_PDF(Names.DOWNLOAD_PDF),
		DOWNLOAD_FILE(Names.DOWNLOAD_FILE);

		private final String label;

		PropertyMethods(String label) {
			this.label = label;
		}

		public static class Names {
			public static final String ADD_PROPERTY_WITHOUT_IMAGE = "ADD_PROPERTY_WITHOUT_IMAGE";
			public static final String ADD_PROPERTY_WITH_IMAGE = "ADD_PROPERTY_WITH_IMAGE";
			public static final String ADD_PROPERTY_WITH_IMAGES = "ADD_PROPERTY_WITH_IMAGES";

			public static final String ADD_IMAGE_TO_PROPERTY = "ADD_IMAGE_TO_PROPERTY";
			public static final String ADD_IMAGES_TO_PROPERTY = "ADD_IMAGES_TO_PROPERTY";
			public static final String ESTIMER_PROPERTY = "ESTIMER_PROPERTY";

			public static final String DELETE_PROPERTY = "DELETE_PROPERTY";
			public static final String EDIT_PROPERTY = "EDIT_PROPERTY";
			public static final String MY_PROPERTIES = "MY_PROPERTIES";

			public static final String GET_ONE_PROPERTY = "GET_ONE_PROPERTY";
			public static final String GET_ALL = "GET_ALL";
			public static final String LIST_PROPERTY_FOR_OWNER = "LIST_PROPERTY_FOR_OWNER";
			public static final String FIND_IMAGES = "FIND_IMAGES";
			public static final String APPROVE_PROPERTY = "APPROVE_PROPERTY";
			public static final String REJECT_PROPETY = "REJECT_PROPETY";
			public static final String EDIT_PICTURES_PROPERTY = "EDIT_PICTURES_PROPERTY";
			private static final String DOWNLOAD_FILE = "DOWNLOAD_FILE";
			// NEW PDF
			private static final String GENERATE_PDF_WITH_PDF_RESPONSE = "GENERATE_PDF_WITH_RESPONSE";
			private static final String GENERATE_PDF_WITHOUT_PDF_RESPONSE = "GENERATE_PDF_WITHOUT_RESPONSE";
			private static final String GET_FILE_BY_NAME = "GET_FILE_BY_NAME";
			private static final String DOWNLOAD_PDF = "DOWNLOAD_PDF";
			private static final String LIST_ALL_PDF_FILES = "LIST_ALL_PDF_FILES";
			// nidhal
			private static final String DELETE_IMAGE_PROP = "DELETE_IMAGE_PROP";
			// old code
			private static final String ADD_PROP_WITHOUT_PICTURES_RESPONSE = "ADD_PROP_WITHOUT_PICTURES_RESPONSE";
			private static final String GET_IMAGE_MEDIA_TYPE_RESPONSE = "GET_IMAGE_MEDIA_TYPE_RESPONSE";
			// likes parts
			private static final String ADD_LIKE = "ADD_LIKE";
			private static final String FIND_ALL_BY_PROPERTY = "FIND_ALL_BY_PROPERTY";
			private static final String MY_LIKES = "MY_LIKES";
			private static final String MOST_LIKE = "MOST_LIKE";
			private static final String STATISTIC = "STATISTIC";
			private static final String EDIT_IMAGES_TO_PROPERTY = "EDIT_IMAGES_TO_PROPERTY";

			private Names() {
			}
		}

	}

	public boolean canDo(PropertyMethods methodName, String userID, String objectID) {
		try {
			return switch (methodName) {
			case MY_PROPERTIES,ADD_PROPERTY_WITH_IMAGE, ADD_PROPERTY_WITH_IMAGES, ADD_PROPERTY_WITHOUT_IMAGE, ADD_PROP_WITHOUT_PICTURES_RESPONSE -> usersRepo
					.getOne(userID).getRoles().stream().anyMatch(i -> i.equals(Role.SELLER));
			case EDIT_PROPERTY, DELETE_PROPERTY, ADD_IMAGE_TO_PROPERTY, LIST_PROPERTY_FOR_OWNER, ADD_IMAGES_TO_PROPERTY, EDIT_PICTURES_PROPERTY, ESTIMER_PROPERTY, DELETE_IMAGE_PROP, EDIT_IMAGES_TO_PROPERTY -> repo
					.getOne(objectID).getUserId().getId().equals(userID);
			case GET_ONE_PROPERTY, FIND_IMAGES, GET_ALL, GET_IMAGE_MEDIA_TYPE_RESPONSE, GET_FILE_BY_NAME, DOWNLOAD_FILE, DOWNLOAD_PDF, GENERATE_PDF_WITHOUT_PDF_RESPONSE, GENERATE_PDF_WITH_PDF_RESPONSE, ADD_LIKE, MY_LIKES, FIND_ALL_BY_PROPERTY, MOST_LIKE, STATISTIC -> true;
			case APPROVE_PROPERTY, REJECT_PROPETY -> usersRepo.getOne(userID).getRoles().stream()
					.anyMatch(i -> i.equals(Role.SECRETARY));
			case LIST_ALL_PDF_FILES -> CollectionUtils.containsAny(usersRepo.getOne(userID).getRoles(),
					List.of(Role.ADMIN));
			};
		} catch (javax.persistence.EntityNotFoundException e) {
			throw new EntityNotFoundException(BienVendre.class, objectID);
		}
	}

	public List<PropertyFile> getMyImages(String id) {
		List<PropertyFile> list = null;
		try {
			list = fileRepo.findByPropId(id);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Please enter a valid id for property", e);
		}
		return list;
	}

	public List<String> listallPdfFiles(Path pdfroot) {
		String res = "We could not find any pdf file";
		List<String> result = null;
		try (Stream<Path> walk = Files.walk(pdfroot)) {

			result = walk.filter(Files::isRegularFile).map(x -> x.getFileName().toString())
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new BaseException(res + e.getMessage());
		}
		return result;
	}

	public void deletePdfFileByName(Path pdfroot, String fileName) {
		if (!fileName.isEmpty()) {
			Path file = pdfroot.resolve(fileName);
			try {
				Files.delete(file);
			} catch (IOException e) {
				throw new BaseException("We couldn't delete this file " + fileName);
			}
		}
	}

	/*
	 * Methode delete to test
	 */
	public void deleteRecursively(Path path) {
		log.info("delete recursively files upload/pdf/...");
		try {

			FileSystemUtils.deleteRecursively(path);
			File file = path.toFile();
			file.mkdir();
		} catch (IOException e) {
			throw new BaseException("Error deleting pdf file ");
		}

	}

	public String generatePdfFile(Model model, String idbien) {
		BienVendre bien = getBienById(idbien);
		Map<Object, Object> data = new HashMap<>();
		data.put("users", bien);
		String filegenerated = null;
		try {
			filegenerated = util.createPdf("test", data, idbien);
		} catch (Exception e) {
			throw new BaseException("Could not create pdf file" + e);
		}
		model.addAttribute("message", "PDF Downloaded successfully..");
		return filegenerated;
	}

	public byte[] returnPdfMediaType(Path pdfroot, String fileName) {
		String res = "We could not find your pfd file please generate a new pdf file  ";
		Path file = pdfroot.resolve(fileName);
		try {
			Resource resource = new UrlResource(file.toUri());
			InputStream in = resource.getInputStream();
			return IOUtils.toByteArray(in);
		} catch (IOException e) {
			throw new BaseException(res + e.getMessage());
		}
	}

	public String findmypdffile(Path pdfroot, String idbien) {
		String res = "We could not find your pfd file please generate a pdf file for this property with id :" + idbien;
		// String res = null;
		try (Stream<Path> walk = Files.walk(pdfroot)) {
			res = walk.map(x -> x.getFileName().toString()).filter(f -> f.startsWith(idbien)).findAny().get();
			log.info("pdf file :: " + res);

		} catch (Exception e) {
			throw new BaseException(res);
		}
		return res;
	}

//	public List<BienVendre> getBiens() {
//		List<BienVendre> users = new ArrayList<BienVendre>();
//		List<BienVendre> lista = repo.findAll();
//		for (BienVendre bienVendre : lista) {
//			log.info(bienVendre.getId());
//			users.add(bienVendre);
//		}
//		return users;
//	}

	public List<BienVendre> getmesBien(User user) {
		List<BienVendre> list = null;
		try {
			list = repo.findPropByUser(user.getId());
		} catch (Exception e) {
			throw new BaseException("Please enter a valid id for property");
		}
		return list;
	}

	public List<BienVendre> getienByCity(String city) {
		List<BienVendre> list = null;
		try {
			list = repo.findPropByLiexuandStatus(city);
		} catch (Exception e) {
			throw new BaseException("Please enter a valid city for property"+e.getMessage());
		}
		return list;
	}
	
	public List<BienVendre> getienByStatus(String status) {
		 Status st = Status.Approved;
		List<BienVendre> listprop = null;
		try {
			  st = Status.valueOf(status);
			  listprop = repo.findPropByStatus(st);
		} catch (Exception e) {
			throw new BaseException("Please enter a valid status for property"+e.getMessage());
		}
		return listprop;
	}
	
	  public List<BienVendre> getmesBienStatus(User user, String status) {
		    List<BienVendre> list = null;
		    Status st = Status.Pending;
		    try {
		      st = Status.valueOf(status);
		    } catch (IllegalArgumentException e) {
		     throw new BaseException("Sorry we couldn't find any property  with this status");
		    }
		    list = repo.findPropByUserandStatus(user.getId(), st);
		    if (list.isEmpty()) {
		      throw new EntityNotFoundException(Appointment.class,
		        "You don't have any property with " + status + " Status ");
		    } else {
		      return list;
		    }
		  }
	  

	@Transactional
	public BienVendre createBienWithouPictures(PropertyRequest dto, User user) {
		BienVendre items = modelMapper.map(dto, BienVendre.class);
		items.setTypeofprop(FirstRunInitializer.typeof.get(dto.getTypepropInt()));
		items.setUserId(user);
		repo.save(items);
		return items;
	}

	@Transactional
	public BienaVendreResponse createBienWithouPicturesResponses(PropertyRequest dto, User user) {
		BienVendre items = modelMapper.map(dto, BienVendre.class);

		items.setTypeofprop(FirstRunInitializer.typeof.get(dto.getTypepropInt()));
		items.setUserId(user);
		BienVendre bien = repo.save(items);

		return this.modelMapper.map(bien, BienaVendreResponse.class);

	}

	@Transactional
	public BienVendre editBien(PropertyEditRequest dto, String id, User user) {

		var bien = repo.findById(id).orElseThrow(() -> new EntityNotFoundException(BienVendre.class, id));
		modelMapper.map(dto, bien);
		repo.save(bien);
		return bien;
	}

	@Transactional
	public BienVendre savewithFilesShort(PropertyRequest dto, MultipartFile file, User user) {
		BienVendre rest;
		try {

			BienVendre bien = createBienWithouPictures(dto, user);
			saveShort(file, bien);
			rest = repo.findById(bien.getId())
					.orElseThrow(() -> new EntityNotFoundException(BienVendre.class, bien.getId()));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid request", e);
		}
		return rest;
	}

	public Page<BienVendre> getAllBien(Integer pageNo, Integer pageSize, String sortBy) {
		Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

		return repo.findAll(paging);
	}

	public BienVendre getBienById(String id) {
		return repo.findById(id).orElseThrow(() -> new EntityNotFoundException(BienVendre.class, id));
	}

	@Transactional
	public ResponseEntity<DoneResponse> save(MultipartFile file, String id) {
		final var bien = repo.findById(id).orElseThrow(() -> new EntityNotFoundException(BienVendre.class, id));
		try {
			uploadPropFile(file, bien);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new DoneResponse("Uploaded the file successfully: " + file.getOriginalFilename()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(new DoneResponse("Failed to upload file!"));
		}
	}

	@Transactional
	public void saveShort(MultipartFile file, BienVendre bien) {
		// BienVendre bien = repo.findById(id).orElseThrow(() -> new
		// EntityNotFoundException(BienVendre.class, id));
		try {
			uploadPropFile(file, bien);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid request", e);
		}
	}

	@Transactional
	public BienVendre savePropWithFiles(MultipartFile[] files, PropertyRequest dto, User user) {

		try {

			BienVendre bien = createBienWithouPictures(dto, user);
			// List<String> OriginalfileNames = new ArrayList<>();

			Arrays.asList(files).forEach(file -> {
				try {
					uploadPropFile(file, bien);
				} catch (Exception e) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during file Upload!");
				}
			});
			return repo.findById(bien.getId())
					.orElseThrow(() -> new EntityNotFoundException(BienVendre.class, bien.getId()));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid request", e);
		}
	}

	@Transactional
	public ResponseEntity<DoneResponse> saveFiles(MultipartFile[] files, String id) {
		String message = "";

		BienVendre bien = repo.findById(id).orElseThrow(() -> new EntityNotFoundException(BienVendre.class, id));

		try {
			Arrays.asList(files).forEach(file -> {
				try {
					uploadPropFile(file, bien);
				} catch (IOException e) {

					e.printStackTrace();
				}
			});

			message = "Uploaded the files successfully: "
					+ Arrays.stream(files).map(MultipartFile::getOriginalFilename).collect(Collectors.joining(" , "));
			return ResponseEntity.status(HttpStatus.OK).body(new DoneResponse(message));
		} catch (Exception e) {
			message = "Fail to upload files!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new DoneResponse(message));
		}
	}

	////////////////////////////////////////////////////

	public Resource load(String repoId) {
		try {
			PropertyFile fileModel = fileRepo.findById(repoId)
					.orElseThrow(() -> new EntityNotFoundException(PropertyFile.class, repoId));
			log.info(fileModel.getFileName());
			Path file = root.resolve(fileModel.getFileName());
			Resource resource = new UrlResource(file.toUri());

			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("Could not read the file!");
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}

	public Resource loadPdf(String fileName, Path pathpdf) {
		try {

			Path file = pathpdf.resolve(fileName);
			Resource resource = new UrlResource(file.toUri());

			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("Could not read the file!");
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}

	public DoneResponse deletePicture(String id) {
		log.info("delete image for prop id " + id);
		try {
			fileRepo.deleteById(id);
		} catch (Exception ex) {
			throw new BaseException("sorry this is image with id : " + id + " doesn't exist");
		}
		return new DoneResponse("You have succefully delete image with id : " + id);
	}

	@Transactional
	public DoneResponse deleteSomePictureForProp(List<String> myIdList) {
		try {
		for (String idFile : myIdList) {
			fileRepo.deletePropById(idFile);
		}
		return new DoneResponse("You have succefully delete images of you property");
		
	}
		catch (Exception e) {
		throw new BaseException("Sorry we could delete your pictures please check if they already exist or try again ");
		}
	}
		
		
	@Transactional
	public DoneResponse deleteAllPictureForProp(String idProp) {
		var bien = repo.findById(idProp).orElseThrow(() -> new EntityNotFoundException(BienVendre.class, idProp));
		var myfile = bien.getFileBien();
		log.info("delete image for prop id " + idProp);
		for (PropertyFile propertyFile : myfile) {
			log.info("delete image " + propertyFile.getFileName());

			fileRepo.deletePropById(propertyFile.getId());

		}
		return new DoneResponse("You have succefully delete image with id : " + idProp);
	}

	public Stream<Path> loadAll() {
		try {

			return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
		} catch (IOException e) {
			throw new RuntimeException("Could not load the files!");
		}
	}

	private void uploadPropFile(MultipartFile file, BienVendre bien) throws IOException {

		final var originalName = StringUtils.cleanPath(file.getOriginalFilename());
		final var extension = FilenameUtils.getExtension(file.getOriginalFilename());
		final var fileId = NanoIDGenerator.generateID();
		final var newName = fileId + "." + extension;
		final var destination = root.resolve(newName);
		if (originalName.contains("..")) {
			throw new BaseException("Sorry! Filename contains invalid path sequence " + originalName);
		}
		var dbFile = PropertyFile.builder().bien(bien).fileName(newName).fileType(file.getContentType()).id(fileId)
				.originalName(originalName).size(file.getSize()).build();
		Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
		fileRepo.save(dbFile);
	}

	@PostConstruct
	private void initUserDir() {
		this.pdfDir = Paths.get(appProperties.getFiles().getPdf()).toAbsolutePath().normalize();
		try {
			if (!Files.exists(pdfDir)) {
				Files.createDirectory(pdfDir);
			}
		} catch (Exception exception) {
			log.error("userservice.initpdfDir", exception);
			throw new InternalServerError("Error creating pdf directory", exception);
		}
	}

}
