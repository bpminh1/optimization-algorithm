package org.example.model.core.localSearch;

import org.example.model.core.IterationInfo;
import org.example.model.core.IterationListener;
import org.example.model.core.Search;
import org.example.model.core.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementation of a local search algorithm for optimization problems.
 * The algorithm uses Metropolis criterion to accept or reject neighboring solutions.
 */
public class LocalSearch implements Search {
    // Components of the local search
    Neighborhood neighborhood;
    Random random;

    // Components for visualization
    private final List<IterationInfo> history = new ArrayList<>();
    private IterationListener iterationListener;

    // Parameters for termination conditions
    long start;
    long limit;
    double epsilon;
    int noImprovementCount;
    int threshold;

    /** Constructor for LocalSearch.
     *
     * @param neighborhood the neighborhood structure used to explore solutions
     */
    public LocalSearch(Neighborhood neighborhood){
        this.neighborhood = neighborhood;
        this.random = new Random(42);
    }

    @Override
    public Solution run(Solution initialSolution){
        history.clear();
        setParameters();

        // Iteration counter for visualization
        int iteration = 0;

        Solution current = initialSolution.copy();
        Solution best = initialSolution.copy();

        recordIteration(iteration, neighborhood.getCurrentAllowedOverlap(), current);

        do {
            iteration++;
            neighborhood.updateAllowedOverlap();

            Solution neighbor = this.neighborhood.getNeighbor(current);

            // Ensure neighbor and current, best have the same allowed overlap for fair comparison
            best.setAllowedOverlap(neighborhood.getCurrentAllowedOverlap());
            current.setAllowedOverlap(neighborhood.getCurrentAllowedOverlap());

            // If accepted, update current and best solutions
            if (accept(current, neighbor)) {
                current = neighbor.copy();
                if (current.getObjectiveValue() < best.getObjectiveValue()) {
                    best = current.copy();
                }
                recordIteration(iteration, neighborhood.getCurrentAllowedOverlap(), current);
            }

        } while (!checkTermination(current, best));

        return best;
    }

    /** Sets the parameters for the local search algorithm.
     * Time limit is set to 10 seconds and no improvement threshold to 5000 iterations.
     */
    private void setParameters(){
        start = System.nanoTime();
        limit = 10_000_000_000L;

        epsilon = 0.05;
        noImprovementCount = 0;
        threshold = 5000;
    }

    /** Determines whether to accept the neighboring solution based on Metropolis criterion.
     *
     * @param current  the current solution
     * @param neighbor the neighboring solution
     * @return true if the neighbor is accepted, false otherwise
     */
    private boolean accept(Solution current, Solution neighbor){
        double delta = neighbor.getObjectiveValue() - current.getObjectiveValue();
        boolean accept = false;

        if(delta < 0){
            accept = true;
        }else{
            double prob = Math.exp(-delta);
            if(random.nextDouble() < prob){
                accept = true;
            }
        }

        return accept;
    }

    /** Checks whether the termination conditions are met.
     *
     * @param current the current solution
     * @param best    the best solution found so far
     * @return true if termination conditions are met, false otherwise
     */
    private boolean checkTermination(Solution current, Solution best){
        double improvement = Math.abs(best.getObjectiveValue() - current.getObjectiveValue());
        if(improvement < epsilon){
            noImprovementCount++;
        }else{
            noImprovementCount = 0;
        }

        long elapsed = System.nanoTime() - start;
        return elapsed >= limit || noImprovementCount >= threshold;
    }

    /** Records the current iteration information.
     *
     * @param iteration      the current iteration number
     * @param allowedOverlap the current allowed overlap value
     * @param current        the current solution
     */
    private void recordIteration(int iteration, double allowedOverlap, Solution current) {
        Solution snapshot = current.copy();
        IterationInfo info = new IterationInfo(iteration, snapshot.getObjectiveValue(), allowedOverlap, snapshot);
        history.add(info);
        if (iterationListener != null) {
            iterationListener.onIteration(info);
        }
    }

    @Override
    public void setIterationListener(IterationListener listener) {
        this.iterationListener = listener;
    }

    @Override
    public List<IterationInfo> getHistory() {
        return List.copyOf(history);
    }
}
