package org.example.model.problem.model;

import java.util.List;

/**
 * PlacementOverlap extends Placement to include overlap handling between boxes.
 */
public class PlacementOverlap extends Placement{
    public double allowedOverlap;

    /** Constructor for PlacementOverlap.
     *
     * @param L the size of the placement area (L x L)
     * @param allowedOverlap the maximum allowed overlap ratio between boxes
     */
    public PlacementOverlap(int L, double allowedOverlap) {
        super(L);
        this.allowedOverlap = allowedOverlap;
    }

    @Override
    public void setAllowedOverlap(double allowedOverlap) {
        this.allowedOverlap = allowedOverlap;
        for(Box box : this.getBoxes()) {
            if(box instanceof BoxOverlap)
                ((BoxOverlap) box).setAllowedOverlap(allowedOverlap);
        }
    }

    /** Calculates the objective value considering overlaps.
     * The objective value is defined as the base objective value plus total overlap and total overlap penalty.
     *
     * @return the calculated objective value
     */
    @Override
    public double getObjectiveValue() {
        double penalty = 0;
        double overlap = 0;

        for(Box box : this.getBoxes()){
            if(box instanceof BoxOverlap) {
                List<Double> overlapData = ((BoxOverlap) box).getOverlapScore();
                penalty += overlapData.get(0);
                overlap += overlapData.get(1);
            }
        }
        return super.getObjectiveValue() + overlap * 10 + penalty * 1000;
    }

    /** Finds and returns the first box with a positive overlap score.
     *
     * @return a BoxOverlap instance with positive overlap score, or null if none found
     */
    public BoxOverlap findBoxWithOverlap(){
        for(Box box : super.getBoxes()){
            if(box instanceof BoxOverlap overlapBox){
                if(overlapBox.getOverlapScore().getFirst() > 0){
                    return overlapBox;
                }
            }
        }

        return null;
    }

    @Override
    public void addBox(){
        this.getBoxes().add(new BoxOverlap(super.getL(), this.allowedOverlap));
    }

    @Override
    public PlacementOverlap copy() {
        PlacementOverlap p = new PlacementOverlap(super.getL(), this.allowedOverlap);
        for(Box box : this.getBoxes()) {
            p.getBoxes().add(box.copy());
        }
        return p;
    }
}
