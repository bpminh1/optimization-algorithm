package org.example.model.core.greedySearch;

import org.example.model.core.IterationInfo;
import org.example.model.core.IterationListener;
import org.example.model.core.Search;
import org.example.model.core.Solution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Greedy search algorithm implementation.
 *
 * @param <T> the type of items to be processed by the greedy algorithm
 */
public class GreedySearch<T> implements Search {
    // Components of the greedy search
    GreedyRule<T> rule;
    GreedyConstructor<T> constructor;
    List<T> items;

    // Components for visualization
    List<IterationInfo> history = new ArrayList<>();
    IterationListener iterationListener = null;
    boolean recordHistory = true;

    /** Constructor for GreedySearch.
     *
     * @param rule        the greedy rule to sort items
     * @param constructor the greedy constructor to build solutions
     * @param items       the list of items to be processed
     */
    public GreedySearch(GreedyRule<T> rule, GreedyConstructor<T> constructor, List<T> items){
        this.rule = rule;
        this.constructor = constructor;
        this.items = items;
    }

    @Override
    public Solution run(Solution initialSolution){
        history.clear();

        List<T> itemsCopy = new LinkedList<>(this.rule.sort(items));
        Solution currentSolution = initialSolution.copy();

        int iteration = 0;
        recordIteration(iteration, currentSolution);

        while(!itemsCopy.isEmpty()){
            iteration++;
            currentSolution = constructor.build(currentSolution, itemsCopy.removeFirst());
            recordIteration(iteration, currentSolution);
        }

        return currentSolution;
    }

    /** Records the current iteration information if history recording is enabled.
     *
     * @param iteration the current iteration number
     * @param current   the current solution
     */
    private void recordIteration(int iteration, Solution current) {
        if(!recordHistory) return;

        Solution snapshot = current.copy();
        IterationInfo info = new IterationInfo(iteration, snapshot.getObjectiveValue(), 0, snapshot);
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

    /** Sets whether to record the history of iterations.
     *
     * @param recordHistory true to record history, false otherwise
     */
    public void setRecordHistory(boolean recordHistory) {
        this.recordHistory = recordHistory;
    }
}
