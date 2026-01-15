package org.example.model.problem.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BoxOverlap class extends Box to handle rectangle placements with allowed overlaps.
 */
public class BoxOverlap extends Box{
    private double allowedOverlap;

    /** Constructor for BoxOverlap.
     *
     * @param L the size of the box (L x L)
     * @param allowedOverlap the maximum allowed overlap ratio between rectangles
     */
    public BoxOverlap(int L, double allowedOverlap) {
        super(L);
        this.allowedOverlap = allowedOverlap;
    }

    /** Copy constructor for BoxOverlap.
     *
     * @param other the BoxOverlap instance to copy
     */
    protected BoxOverlap(BoxOverlap other){
        super(other);
        this.allowedOverlap = other.allowedOverlap;
    }

    /** Sets the allowed overlap ratio.
     *
     * @param allowedOverlap the maximum allowed overlap ratio between rectangles
     */
    public void setAllowedOverlap(double allowedOverlap) {
        this.allowedOverlap = allowedOverlap;
    }

    /** Calculates the overlap ratio between two rectangles.
     *
     * @param r1 the first rectangle
     * @param r2 the second rectangle
     * @return the overlap ratio (intersection area / max area of the two rectangles)
     */
    public double calculateOverlapRatio(Rectangle r1, Rectangle r2) {
        int xOverlap = Math.max(
                0,
                Math.min(
                        r1.getX() + r1.getWidth(),
                        r2.getX() + r2.getWidth()
                ) - Math.max(
                        r1.getX(),
                        r2.getX()));

        int yOverlap = Math.max(
                0,
                Math.min(
                        r1.getY() + r1.getHeight(),
                        r2.getY() + r2.getHeight()
                ) - Math.max(
                        r1.getY(),
                        r2.getY()));

        int intersectionArea = xOverlap * yOverlap;

        if(intersectionArea == 0) {
            return 0;
        }

        int maxArea = Math.max(r1.getArea(), r2.getArea());
        if(maxArea == 0) return 1;
        return (double) intersectionArea / maxArea;
    }

    /** Gets the map of overlapped rectangle pairs and their overlap penalties.
     * Only pairs with overlap greater than allowedOverlap are included.
     *
     * @return a map where keys are lists of overlapped rectangles and values are their overlap penalties
     */
    public Map<List<Rectangle>, Double> getOverlappedRectangles() {
        List<Rectangle> rectangles = this.getRects();
        Map<List<Rectangle>, Double> overlappedMap = new LinkedHashMap<>();

        for(int i = 0; i < rectangles.size(); i++) {
            for (int j = i + 1; j < rectangles.size(); j++) {
                double overlap = calculateOverlapRatio(rectangles.get(i), rectangles.get(j));
                if (overlap > allowedOverlap) {
                    List<Rectangle> overlappedPair = new ArrayList<>();
                    overlappedPair.add(rectangles.get(i));
                    overlappedPair.add(rectangles.get(j));
                    overlappedMap.put(overlappedPair, overlap - allowedOverlap);
                }
            }
        }

        return overlappedMap;
    }

    /** Calculates the total overlap penalty and total overlap in the box.
     *
     * @return a list containing two elements: [totalPenalty, totalOverlap]
     */
    public List<Double> getOverlapScore() {
        double totalPenalty = 0;
        double totalOverlap = 0;

        Map<List<Rectangle>, Double> overlappedMap = getOverlappedRectangles();
        for(Double penalty : overlappedMap.values()) {
            totalPenalty += penalty;
            totalOverlap += penalty + allowedOverlap;
        }
        return List.of(totalPenalty, totalOverlap);
    }

    @Override
    public boolean canPlace(int x, int y, Rectangle rect){
        if(x < 0 || y < 0 || x + rect.getWidth() > getL() || y + rect.getHeight() > getL()) return false;

        // Create a temporary rectangle at the desired position
        Rectangle tmpRect = rect.copy();
        tmpRect.place(x, y, this.getId());

        // Check overlap with existing rectangles
        for(Rectangle rectangle : this.getRects()) {
            double overlap = calculateOverlapRatio(tmpRect, rectangle);
            if(overlap > allowedOverlap) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Box copy(){
        return new BoxOverlap(this);
    }
}
