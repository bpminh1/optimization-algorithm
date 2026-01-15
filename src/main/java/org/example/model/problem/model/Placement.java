package org.example.model.problem.model;
import org.example.model.core.Solution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a placement solution consisting of multiple boxes.
 */
public class Placement implements Solution {
    private final int L;
    private List<Box> boxes;

    /** Constructor for Placement.
     *
     * @param L the size of each box (L x L)
     */
    public Placement(int L){
        this.L = L;
        this.boxes = new ArrayList<>();
    }

    /**
     * Gets the last box in the placement.
     *
     * @return the last Box object
     */
    public Box getLastBox(){
        return this.boxes.getLast();
    }

    /** Adds a new empty box to the placement. */
    public void addBox(){
        this.boxes.add(new Box(this.L));
    }

    /** Sorts the boxes in descending order based on the number of rectangles they contain. */
    public void sortBoxes(){
        removeEmptyBox();
        this.boxes.sort(Comparator.comparingInt((Box b) -> b.getRects().size()).reversed());
    }

    /** Removes boxes that do not contain any rectangles. */
    public void removeEmptyBox(){
        boxes.removeIf(box -> box.getRects().isEmpty());
    }

    /** Retrieves all rectangles in the placement in order of their boxes.
     *
     * @return a list of rectangles
     */
    public List<Rectangle> getRectanglesInOrder(){
        List<Rectangle> rectangles = new ArrayList<>();
        for(Box box : this.boxes){
            rectangles.addAll(box.getRects());
        }
        return rectangles;
    }

    /** Calculates the penalty score for a given box.
     * The penalty score is defined as the inverse of the number of rectangles in the box.
     *
     * @param box the box to evaluate
     * @return the penalty score
     */
    public double getPenaltyScore(Box box){
        int numRect = box.getRects().size();
        return numRect == 0? 0 : 1.0 / numRect;
    }

    /** Calculates the objective value of the placement.
     * The objective value is defined as the number of boxes plus the average penalty score across all boxes.
     *
     * @return the objective value
     */
    @Override
    public double getObjectiveValue() {
        int numBox = this.boxes.size();

        double penalty = 0;
        for(Box box : this.boxes){
            penalty += getPenaltyScore(box);
        }

        return numBox + penalty / numBox;
    }

    @Override
    public void setAllowedOverlap(double overlap) {
        // No overlap handling in this class
    }

    @Override
    public Solution copy() {
        Placement p = new Placement(this.L);

        List<Box> b = new ArrayList<>();
        for(Box box : this.boxes){
            b.add(box.copy());
        }
        p.boxes = b;

        return p;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Placement{" + ' ' +
                "L=" + this.L + ' ' +
                "numBox=" + this.boxes.size() + ' ' +
                "boxes{");

        for(Box box : this.boxes){
            result.append(box.toString());
            result.append(' ');
        }

        result.append("}");

        return result.toString();
    }

    // Getters
    public List<Box> getBoxes(){ return this.boxes; }
    public int getNumBoxes(){ return this.boxes.size(); }
    public int getL() {
        return L;
    }
}