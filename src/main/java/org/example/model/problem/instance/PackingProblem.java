package org.example.model.problem.instance;

import org.example.model.core.Problem;
import org.example.model.core.Solution;
import org.example.model.core.greedySearch.GreedySearch;
import org.example.model.problem.greedyConstructors.GreedyConstructorInitialSolution;
import org.example.model.problem.model.Box;
import org.example.model.problem.model.Placement;
import org.example.model.problem.model.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a packing problem instance with a set of rectangles to be packed into boxes of size L x L.
 */
public class PackingProblem implements Problem<Rectangle> {
    int L;
    List<Rectangle> rectangles;
    Solution initialSolution;

    /** Constructor for PackingProblem.
     *
     * @param seed       the seed for random rectangle generation
     * @param L          the size of the boxes (L x L)
     * @param n          the number of rectangles to generate
     * @param minWidth   the minimum width of the rectangles
     * @param maxWidth   the maximum width of the rectangles
     * @param minHeight  the minimum height of the rectangles
     * @param maxHeight  the maximum height of the rectangles
     */
    public PackingProblem(int seed, int L, int n, int minWidth, int maxWidth, int minHeight, int maxHeight){
        this.L = L;
        if(L <= 0 || minHeight <= 0 || minWidth <= 0){
            throw new IllegalArgumentException("L, minHeight and minWidth must be greater than 0");
        }
        if(L < maxHeight || L < maxWidth){
            throw new IllegalArgumentException("L must be greater than or equal to maxWidth and maxHeight");
        }
        if(minWidth > maxWidth){
            throw new IllegalArgumentException("maxWidth must be greater than or equal to minWidth");
        }
        if(minHeight > maxHeight){
            throw new IllegalArgumentException("maxHeight must be greater than or equal to minHeight");
        }
        rectangles = RectangleGenerator.generate(seed, n, minWidth, maxWidth, minHeight, maxHeight);
    }

    /**
     * Retrieves a copy of the list of rectangles involved in the packing problem.
     *
     * @return a list of rectangle copies
     */
    @Override
    public List<Rectangle> getItems() {
        List<Rectangle> itemsCopy = new ArrayList<>();
        for(Rectangle rectangle : rectangles) {
            itemsCopy.add(rectangle.copy());
        }
        return itemsCopy;
    }

    /**
     * Retrieves the initial solution for the packing problem using a greedy construction method.
     *
     * @return a copy of the initial solution
     */
    @Override
    public Solution getInitialSolution() {
        if(initialSolution == null) {
            GreedyConstructorInitialSolution constructor = new GreedyConstructorInitialSolution(this.L);
            GreedySearch<Rectangle> greedySearch = new GreedySearch<>(
                    items -> items, constructor, getItems());

            Placement emptyPlacement = new Placement(getL());
            Box.resetIDCounter();
            emptyPlacement.addBox();

            initialSolution = greedySearch.run(emptyPlacement);
        }

        return initialSolution.copy();
    }

    @Override
    public String toString() {
        return "PackingProblem{" +
                "L=" + L +
                ", number of rectangles=" + rectangles.size() +
                '}';
    }

    // Getters
    public int getL() {
        return L;
    }
}
