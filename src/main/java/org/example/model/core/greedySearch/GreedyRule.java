package org.example.model.core.greedySearch;

import java.util.List;

/**
 * Interface for defining a greedy rule to sort items for greedy algorithms.
 *
 * @param <T> the type of items to be sorted
 */
public interface GreedyRule<T> {
    /**
     * Sorts the given list of items according to the greedy rule.
     *
     * @param initialOrder the initial order of items
     * @return the sorted list of items
     */
    List<T> sort(List<T> initialOrder);
}
