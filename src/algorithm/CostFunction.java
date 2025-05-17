package algorithm;

import algorithm.graph.Edge;

/**
 * Interface pour calculer le coût d'un arc,
 * en fonction de l'arc courant et de l'arc précédent pour gérer les changements.
 */
@FunctionalInterface
public interface CostFunction {
    /**
     * @param edge        l'arc actuel
     * @param previous    l'arc précédent
     * @return coût entier à ajouter (en secondes)
     */
    int cost(Edge edge, Edge previous);
}
