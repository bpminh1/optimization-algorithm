package org.example.model.problem.greedyConstructors;

import org.example.model.core.Solution;
import org.example.model.core.greedySearch.GreedyConstructor;
import org.example.model.problem.model.Placement;
import org.example.model.problem.model.Rectangle;

/**
 * Greedy constructor that builds a solution for the rectangle placement problem
 * by placing rectangles into boxes.
 */
public class GreedyConstructorPlace implements GreedyConstructor<Rectangle> {
    int L;

    /** Constructor for GreedyConstructorPlace.
     *
     * @param L the size of each box (L x L)
     */
    public GreedyConstructorPlace(int L){
        this.L = L;
    }

    @Override
    public Solution build(Solution previousSolution, Rectangle item) {
        Placement solution = (Placement) previousSolution;

        if(!solution.getLastBox().place(item)){
            solution.addBox();
            solution.getLastBox().place(item);
        }

        solution.removeEmptyBox();
        return solution;
    }
}
