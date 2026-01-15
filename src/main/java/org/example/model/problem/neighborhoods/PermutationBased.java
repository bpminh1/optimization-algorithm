package org.example.model.problem.neighborhoods;

import org.example.model.core.Solution;
import org.example.model.core.greedySearch.GreedyConstructor;
import org.example.model.core.greedySearch.GreedyRule;
import org.example.model.core.greedySearch.GreedySearch;
import org.example.model.core.localSearch.Neighborhood;
import org.example.model.problem.greedyConstructors.GreedyConstructorPlace;
import org.example.model.problem.model.Box;
import org.example.model.problem.model.Placement;
import org.example.model.problem.model.Rectangle;

import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

/**
 * A neighborhood structure that generates neighbors by permuting the order of rectangles
 * and constructing a new placement using a greedy algorithm.
 */
public class PermutationBased implements Neighborhood {
    Random random;
    GreedySearch<Rectangle> greedySearch;
    int L;

    /** Constructor for PermutationBased neighborhood.
     *
     * @param L               the size of the boxes (L x L)
     * @param initialSolution the initial solution to base permutations on
     */
    public PermutationBased(int L, Solution initialSolution) {
        random = new Random(42);
        this.L = L;

        GreedyConstructor<Rectangle> constructor = new GreedyConstructorPlace(L);
        GreedyRule<Rectangle> rule = (initialOrder) -> {
            shuffle(initialOrder, random);
            return initialOrder;
        };

        Placement current = (Placement) initialSolution.copy();
        List<Rectangle> rectangles = current.getRectanglesInOrder();
        this.greedySearch = new GreedySearch<>(rule, constructor, rectangles);
        greedySearch.setRecordHistory(false);
    }

    /**
     * Generates a neighboring solution by permuting the order of rectangles
     * and constructing a new placement using a greedy algorithm.
     *
     * @param currentSolution the current solution
     * @return a neighboring solution
     */
    @Override
    public Solution getNeighbor(Solution currentSolution) {
        Placement emptyPlacement = new Placement(L);
        Box.resetIDCounter();
        emptyPlacement.addBox();

        return greedySearch.run(emptyPlacement);
    }

    @Override
    public double getCurrentAllowedOverlap() {
        // Overlap is not allowed in this neighborhood
        return 0;
    }

    @Override
    public void updateAllowedOverlap() {
        // Overlap is not allowed in this neighborhood
    }
}
