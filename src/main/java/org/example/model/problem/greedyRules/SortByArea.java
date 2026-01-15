package org.example.model.problem.greedyRules;

import org.example.model.core.greedySearch.GreedyRule;
import org.example.model.problem.model.Rectangle;

import java.util.Comparator;
import java.util.List;

/**
 * Greedy rule that sorts rectangles by their area in descending order.
 */
public class SortByArea implements GreedyRule<Rectangle> {
    @Override
    public List<Rectangle> sort(List<Rectangle> initialOrder) {
        initialOrder.sort(Comparator.comparingInt(Rectangle::getArea).reversed());
        return initialOrder;
    }
}
