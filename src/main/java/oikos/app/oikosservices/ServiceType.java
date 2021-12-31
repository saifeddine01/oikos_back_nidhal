package oikos.app.oikosservices;

/** Created by Mohamed Haamdi on 31/01/2021 */
public enum ServiceType {
  RECEPTION_APPELS("Reception d'appels"),
  SEANCE_PHOTO("Séance photo"),
  VISITE_VIRTUELLE("Visite virtuelle"),
  HOME_STAGING("Home staging"),
  DIAGNOSTIC("Diagnostic"),
  BOOSTEUR_ANNONCE("Boosteur d'annonces"),
  NEGOCIATEUR("Négociateur"),
  NOTAIRE("Notaire")
  ;
  private final String name;

  ServiceType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
