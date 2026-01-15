package org.example.model.problem.model;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a square box of size L x L that can hold rectangles.
 * Coordinates start at (0,0) in the bottom-left corner.
 */
public class Box {

    // Unique identifier for the box
    private final int L;
    private static int ID_COUNTER = 0;
    private final int id;

    boolean[][] occupied; // Spatial grid to track occupied cells
    List<Rectangle> rects; // List of rectangles placed in the box

    /** Constructor for Box.
     *
     * @param L the size of the box (L x L)
     */
    public Box(int L){
        this.L = L;
        this.id = ID_COUNTER++;

        this.occupied = new boolean[L][L];
        this.rects = new ArrayList<>();
    }

    /** Copy constructor for Box.
     *
     * @param other the box to copy
     */
    protected Box(Box other){
        this.L = other.L;
        this.id = other.id;

        this.occupied = new boolean[L][L];
        for(int i = 0; i < L; i++){
            System.arraycopy(other.occupied[i], 0, this.occupied[i], 0, L);
        }

        this.rects = new ArrayList<>();
        for(Rectangle r : other.rects){
            this.rects.add(r.copy());
        }
    }

    /** Resets the ID counter for boxes. */
    public static void resetIDCounter(){
        ID_COUNTER = 0;
    }

    /** Gets the size of the smallest rectangle in the box.
     *
     * @return an array with two elements: [minWidth, minHeight]
     */
    public int[] getSmallestRectangleSize(){
        int minWidth = Integer.MAX_VALUE;
        int minHeight = Integer.MAX_VALUE;

        for(Rectangle rect : rects){
            if(rect.getHeight() < minHeight)
                minHeight = rect.getHeight();
            if(rect.getWidth() < minWidth)
                minWidth = rect.getWidth();
        }

        return new int[]{minWidth, minHeight};
    }

    /** Attempts to place a rectangle in the box.
     *
     * @param rect the rectangle to place
     * @return true if the rectangle was placed, false otherwise
     */
    public boolean place(Rectangle rect){
        for(int x = 0; x + rect.getWidth() <= L; x++){
            for(int y = 0; y + rect.getHeight() <= L; y++){
                if(canPlace(x, y, rect)){
                    place(x, y, rect);
                    return true;
                }
            }
        }
        return false;
    }

    /** Places a rectangle at the specified coordinates.
     *
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @param rect the rectangle to place
     */
    public void place(int x, int y, Rectangle rect){
        for(int i = x; i < x + rect.getWidth(); i++){
            for(int j = y; j < y + rect.getHeight(); j++){
                occupied[i][j] = true;
            }
        }
        rects.add(rect);
        rect.place(x, y, this.id);
    }

    /** Checks if a rectangle can be placed at the given coordinates.
     *
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @param rect the rectangle to place
     * @return true if the rectangle can be placed, false otherwise
     */
    public boolean canPlace(int x, int y, Rectangle rect){
        if(x < 0 || y < 0 || x + rect.getWidth() > L || y + rect.getHeight() > L) return false;

        for(int i = x; i < x + rect.getWidth(); i++){
            for(int j = y; j < y + rect.getHeight(); j++){
                if(occupied[i][j]) return false;
            }
        }

        return true;
    }

    /** Removes a rectangle from the box.
     *
     * @param rect the rectangle to remove
     */
    public void remove(Rectangle rect){
        for(int i = rect.getX(); i < rect.getX() + rect.getWidth(); i++){
            for(int j = rect.getY(); j < rect.getY() + rect.getHeight(); j++){
                occupied[i][j] = false;
            }
        }
        rects.remove(rect);
    }

    /** Creates a copy of the box.
     *
     * @return a new Box object that is a copy of this box
     */
    public Box copy(){
        return new Box(this);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Box{" +
                "id=" + this.id + ' ' +
                "numRects=" + this.rects.size() + ' ' +
                "rects{");

        for(Rectangle r : this.rects){
            result.append(r.toString());
            result.append(' ');
        }

        result.append("}");

        return result.toString();
    }

    // Getters
    public List<Rectangle> getRects(){ return this.rects; }
    public int getNumRects(){ return this.rects.size(); }
    public int getL(){ return this.L; }
    public int getId(){ return this.id; }
}