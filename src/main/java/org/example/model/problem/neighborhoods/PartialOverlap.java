package org.example.model.problem.neighborhoods;

import org.example.model.core.Solution;
import org.example.model.problem.model.Box;
import org.example.model.problem.model.BoxOverlap;
import org.example.model.problem.model.PlacementOverlap;
import org.example.model.problem.model.Rectangle;

import java.util.*;

/**
 * Neighborhood that generates neighbors by allowing partial overlap between rectangles.
 */
public class PartialOverlap extends GeometryBased{
    double currentAllowedOverlap;

    /** Constructor for PartialOverlap neighborhood.
     * Initializes the allowed overlap to 1.0 (100%).
     */
    public PartialOverlap(){
        super();
        this.currentAllowedOverlap = 1.0;
    }

    /**
     * Generates a neighboring solution by adjusting rectangle placements to reduce overlaps.
     *
     * @param currentSolution the current solution
     * @return a neighboring solution with reduced overlaps
     */
    @Override
    public Solution getNeighbor(Solution currentSolution){
        PlacementOverlap current = (PlacementOverlap) currentSolution.copy();
        current.setAllowedOverlap(currentAllowedOverlap);
        List<Box> boxes = current.getBoxes();

        if(boxes.isEmpty()) return current;

        BoxOverlap box = current.findBoxWithOverlap();
        if(box == null) {
            return super.getNeighbor(currentSolution);
        }

        List<Rectangle> overlappingRects = getOverlappingRects(box);
        for(Rectangle rect : overlappingRects) {
            box.remove(rect);

            if (box.place(rect)) {
                continue;
            }

            if (!tryPlacingInOtherBoxes(boxes, box, rect)) {
                current.addBox();
                current.getLastBox().place(rect);
            }
        }
        current.removeEmptyBox();
        return current;
    }

    /** Attempts to place the given rectangle in other boxes.
     *
     * @param boxes the list of boxes to try placing the rectangle in
     * @param box the original box from which the rectangle was removed
     * @param rect the rectangle to be placed
     * @return true if the rectangle was successfully placed in another box, false otherwise
     */
    private boolean tryPlacingInOtherBoxes(List<Box> boxes, BoxOverlap box, Rectangle rect){
        Collections.shuffle(boxes, random);
        int maxBoxesToTry = Math.min(3, boxes.size());
        for (int i =0; i < maxBoxesToTry; i++) {
            BoxOverlap other = (BoxOverlap) boxes.get(i);
            if (other.getId() != box.getId() && other.place(rect)) {
                return true;
            }
        }
        return false;
    }

    /** Retrieves a list of overlapping rectangles from the given box.
     * If the number of overlapping rectangles exceeds certain thresholds,
     * the list is truncated to reduce its size.
     *
     * @param box the box to analyze for overlapping rectangles
     * @return a list of overlapping rectangles
     */
    private List<Rectangle> getOverlappingRects(BoxOverlap box) {
        List<Rectangle> overlappingRects = findOverlappingRectangles(box);

        if (overlappingRects.size() > 80) {
            overlappingRects = overlappingRects.subList(0, overlappingRects.size() / 4);
        } else if (overlappingRects.size() > 40) {
            overlappingRects = overlappingRects.subList(0, overlappingRects.size() / 2);
        }

        return overlappingRects;
    }

    /** Finds and ranks overlapping rectangles in the given box based on their total overlap.
     *
     * @param box the box to analyze for overlapping rectangles
     * @return a list of rectangles sorted by their total overlap in descending order
     */
    private List<Rectangle> findOverlappingRectangles(BoxOverlap box){
        Map<List<Rectangle>, Double> overlaps = box.getOverlappedRectangles();
        Map<Rectangle, Double> score = new HashMap<>();

        for (Map.Entry<List<Rectangle>, Double> e : overlaps.entrySet()) {
            Rectangle r1 = e.getKey().get(0);
            Rectangle r2 = e.getKey().get(1);
            double overlap = e.getValue();

            score.merge(r1, overlap, Double::sum);
            score.merge(r2, overlap, Double::sum);
        }

        return score.entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }


    /** Updates the allowed overlap by decreasing it by 0.1 in each iteration. */
    @Override
    public void updateAllowedOverlap(){
        if(currentAllowedOverlap >= 0.1)
            currentAllowedOverlap -= 0.1;
        else
            currentAllowedOverlap = 0.0;
    }

    @Override
    public double getCurrentAllowedOverlap() {
        return currentAllowedOverlap;
    }
}
