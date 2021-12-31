package oikos.app.common.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.entityResponses.LikesResponse;
import oikos.app.common.exceptions.EntityNotFoundException;
import oikos.app.common.models.BienVendre;
import oikos.app.common.models.LikedProperty;
import oikos.app.common.models.Likes;
import oikos.app.notifications.CreateNotificationRequest;
import oikos.app.notifications.NotificationService;
import oikos.app.common.repos.BienaVendreRepo;
import oikos.app.common.repos.PropertyLikesRepo;
import oikos.app.common.request.LikesRequest;
import oikos.app.users.User;
import oikos.app.users.UserRepo;

@Service
@AllArgsConstructor
@Slf4j
public class PropertyLikesService {
	private final PropertyLikesRepo repo;
	private final BienaVendreRepo proprepo;
	private final NotificationService notif;
	private final UserRepo userRepo;
	

	@ToString
	enum PropertyLikeMethods {
		ADD_LIKE(Names.ADD_LIKE),
		GET_LIKE_OR_DISLIKE_BY_USER(Names.GET_LIKE_OR_DISLIKE_BY_USER),
		MY_LIKES(Names.MY_LIKES), 
		FIND_ALL_BY_PROPERTY(Names.FIND_ALL_BY_PROPERTY),
		FIND_ALL_BY_ID(Names.FIND_ALL_BY_ID),
		GET_ALL_LIKES(Names.GET_ALL_LIKES),
		MOST_LIKE(Names.MOST_LIKE),
		STATISTIC(Names.STATISTIC);
		

		private final String label;

		PropertyLikeMethods(String label) {
			this.label = label;
		}

		public static class Names {
			public static final String ADD_LIKE = "ADD_LIKE";
			public static final String GET_LIKE_OR_DISLIKE_BY_USER = "GET_LIKE_OR_DISLIKE_BY_USER";
			public static final String MY_LIKES = "MY_LIKES";

			public static final String FIND_ALL_BY_PROPERTY = "FIND_ALL_BY_PROPERTY";
			public static final String FIND_ALL_BY_ID = "FIND_ALL_BY_ID";
			public static final String GET_ALL_LIKES="GET_ALL_LIKES";
			public static final String MOST_LIKE="MOST_LIKE";
			public static final String STATISTIC="STATISTIC";


			private Names() {
			}
		}
	}

	public boolean canDo(PropertyLikeMethods methodName, String userID, String objectID) {
		try {
			return switch (methodName) {
//			case ADD_LIKE, GET_LIKE_OR_DISLIKE_BY_USER, MY_LIKES -> userRepo.getOne(userID)
//					.getRoles().stream().anyMatch(i -> i.equals(Role.BUYER));
			case ADD_LIKE,MY_LIKES,FIND_ALL_BY_PROPERTY, MOST_LIKE, STATISTIC, GET_LIKE_OR_DISLIKE_BY_USER,FIND_ALL_BY_ID,GET_ALL_LIKES-> true;
					
			default -> throw new IllegalArgumentException("Unexpected value: " + methodName);
			};
		} catch (javax.persistence.EntityNotFoundException e) {
			throw new EntityNotFoundException(BienVendre.class, objectID);
		}
	}

	// SAIF: Send mail Someone Liked your property
	public LikesResponse addLike(LikesRequest likes, User user) {
		BienVendre bien = proprepo.findById(likes.getPropId())
				.orElseThrow(() -> new EntityNotFoundException(getClass(), likes.getPropId()));
		LikedProperty property = new LikedProperty(user.getId(), bien, likes.getLike());
		if(likes.getLike().equals(Likes.LIKE)) {
		CreateNotificationRequest notifreq = CreateNotificationRequest.builder()
				.content(user.getFirstName() + " "+user.getLastName()+ " Liked your property").userId(bien.getUserId().getId()).lien("www.google.com")
				.build();
		notif.addNotification(notifreq);
		}
		if (!repo.checkLike(user.getId(), likes.getPropId())) {
			property = repo.save(property);
			return LikesResponse.builder().propertyId(property.getProp().getId()).status(property.getStatus())
					.userId(property.getUserId()).build();
		} else {
			LikedProperty prop = repo.getLikeOrDislikeByUser(likes.getPropId(), user.getId());
			prop.setStatus(likes.getLike());
			repo.save(prop);
			return LikesResponse.builder().propertyId(prop.getProp().getId()).status(prop.getStatus())
					.createdAt(prop.getCreatedAt()).updatedAt(prop.getUpdatedAt()).userId(prop.getUserId()).build();
		}
	}

	public LikesResponse getLikeOrDislikeByUser(String idProp, User user) {
		LikedProperty prop = repo.getLikeOrDislikeByUser(idProp, user.getId());
		return LikesResponse.builder().propertyId(prop.getProp().getId()).status(prop.getStatus()).userId(user.getId())
				.id(prop.getId()).build();
	}

	public List<LikesResponse> myLikes(String userId, String status) {
		Likes lk = Likes.LIKE;
		lk = Likes.valueOf(status);
		LikesResponse en;
		List<LikesResponse> list = new ArrayList<LikesResponse>();
		List<LikedProperty> prop = repo.myLikes(userId, lk);
		if (prop.isEmpty()) {
			return list;
		} else if (!prop.isEmpty()) {

			for (LikedProperty likedProperty : prop) {
				en = LikesResponse.builder().propertyId(likedProperty.getProp().getId())
						.status(likedProperty.getStatus()).userId(likedProperty.getId()).id(likedProperty.getId())
						.updatedAt(likedProperty.getUpdatedAt()).createdAt(likedProperty.getCreatedAt()).build();
				list.add(en);
			}
		}
		return list;
	}

	public List<LikesResponse> findallByProp(String propId) {
		LikesResponse en;
		List<LikesResponse> list = new ArrayList<LikesResponse>();
		List<LikedProperty> prop = repo.getLikesByPrOP(propId);
		if (prop.isEmpty()) {
			return list;
		} else if (!prop.isEmpty()) {

			for (LikedProperty likedProperty : prop) {
				en = LikesResponse.builder().propertyId(likedProperty.getProp().getId())
						.status(likedProperty.getStatus()).userId(likedProperty.getId()).id(likedProperty.getId())
						.updatedAt(likedProperty.getUpdatedAt()).createdAt(likedProperty.getCreatedAt()).build();
				list.add(en);
			}
		}
		return list;
	}

	public LikesResponse findFindById(String id) {
		LikedProperty prop = repo.findById(id).orElseThrow(() -> new EntityNotFoundException(getClass(), id));
		return LikesResponse.builder().propertyId(prop.getProp().getId()).status(prop.getStatus()).userId(prop.getId())
				.id(prop.getId()).updatedAt(prop.getUpdatedAt()).createdAt(prop.getCreatedAt()).build();
	}

	public Page<LikedProperty> getAllLikes(Integer pageNo, Integer pageSize, String sortBy) {
		Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
		return repo.findAll(paging);
	}

	public List<String> mostlike(String status) {
		Likes lk = Likes.LIKE;
		lk = Likes.valueOf(status);
		return repo.mostlikedprop(lk);
	}

	public List<Map<String, Integer>> statistic(String status) {

		Likes lk = Likes.LIKE;
		lk = Likes.valueOf(status);
		List<String> list = repo.stats(lk);

//		Map<String, String> map = new HashMap<>();
//		for (String str : list) {
//			Stream.of(str.split(",")).map(elem -> new String(elem)).collect(Collectors.toMap(null, null));
//		}
//		}
//		List<String> items = Arrays.asList(str.split("\\s*,\\s*"));
//
//		Map<String, String> map = list.stream().collect(Collectors.toMap(, s->s));
//		map.forEach((x, y) -> System.out.println("Key: " + x +", value: "+ y));
//		return 0;
		return list.stream().map(s -> s.split(",", 2)).map(a -> Collections.singletonMap(a[0], Integer.parseInt(a[1])))
				.collect(Collectors.toList());

	}
}
