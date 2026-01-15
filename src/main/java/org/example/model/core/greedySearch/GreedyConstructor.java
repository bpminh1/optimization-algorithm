package org.example.model.core.greedySearch;

import org.example.model.core.Solution;

/**
 * Interface for constructing a new solution by adding an item to a previous solution in a greedy manner.
 *
 * @param <T> the type of items to be added to the solution
 */
public interface GreedyConstructor<T> {
    /**
     * Builds a new solution by adding the specified item to the previous solution.
     *
     * @param previousSolution the previous solution
     * @param item             the item to be added
     * @return the new solution after adding the item
     */
    Solution build(Solution previousSolution, T item);
}
