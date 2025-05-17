package data;


/**
 * Représente un trajet
 *
 * @param tripId  identifiant unique du trajet
 * @param routeId identifiant de la ligne associée
 */
public record Trip(String tripId, String routeId) {}