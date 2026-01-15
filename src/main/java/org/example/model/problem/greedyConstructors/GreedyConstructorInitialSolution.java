package org.example.model.problem.greedyConstructors;

import org.example.model.core.Solution;
import org.example.model.core.greedySearch.GreedyConstructor;
import org.example.model.problem.model.Placement;
import org.example.model.problem.model.Rectangle;

/**
 * Greedy constructor that builds an initial solution for the rectangle placement problem.
 * It places rectangles into boxes, creating new boxes as needed based on the specified limit.
 */
public class GreedyConstructorInitialSolution implements GreedyConstructor<Rectangle> {
    int L;

    /** Constructor for GreedyConstructorInitialSolution.
     *
     * @param L the size of each box (L x L)
     */
    public GreedyConstructorInitialSolution(int L){
        this.L = L;
    }

    @Override
    public Solution build(Solution previousSolution, Rectangle item) {
        Placement solution = (Placement) previousSolution;

        if(solution.getLastBox().getRects().size() >= this.L / 4 || !solution.getLastBox().place(item)){
            solution.addBox();
            solution.getLastBox().place(item);
        }

        solution.removeEmptyBox();
        return solution;
    }
}


