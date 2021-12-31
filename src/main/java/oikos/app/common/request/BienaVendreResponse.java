package oikos.app.common.request;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import oikos.app.common.models.LikedProperty;
import oikos.app.common.models.Location;
import oikos.app.common.models.PiecesOfProperty;
import oikos.app.common.models.PropExport;
import oikos.app.common.models.PropertyAddress;
import oikos.app.common.models.PropertyFile;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import oikos.app.users.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BienaVendreResponse {
//	private String id;
//
//	private boolean isApproved;
//
//	
//	private String status;
//
//	private String surface;
//	
//
//	private	Address addresse;
//	private String description;
//	private String keyPoints;
//	private int nbChambres;
//	private int nbPieces;
//	private int nbWC;
//	private int nbStationnements;
//	private int nbEtages;
//	private int anneeConstruction;
//	private int surfaceHabitable;
//	private String mainPic;
//	private int price;
//	 private Set<File> set = new HashSet<>();
//	 private String userId;
//	 
//	 
	 
	 /*
	  * New Response
	  */
	 
	 
	  String id;

	  private Instant createdAt;
	  private Instant updatedAt;

	  
	  private String status;

	  
	  private String typeofprop;

	  
	  private String vueProp;

	  private String propLocation;

	 
	  private String propStanding;
	  private String propId;

	  private boolean isOwner ;
	  private double allArea;
	  private double homeArea;
	  private double livingArea;
	  @Embedded private Location location;
	  @Embedded private PropertyAddress address;

	  @Embedded private PiecesOfProperty piecesOfProperty;
	  @Embedded private PropExport propExport;

	  private boolean hasDependancy;
	  // mitoyen
	  private boolean isAdjoining;

	  private String description;
	  private String keyPoints;
	  private int nbBedrooms;
	  private int nbRooms;
	  // private int nbWC;
	  private int nbParkingPlaces;
	  private boolean hasPlannedWork;
	  private int nbFloors;
	  private String yearConstruction;
	  private String mainPic;
	  private int price;
	 private String userId;

	  
	 
	  private Set<PropertyFile> fileBien = new HashSet<>();
	 
	  private Set<LikedProperty> proplikes = new HashSet<>();
}
