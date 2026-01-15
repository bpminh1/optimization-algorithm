package org.example.model.problem.model;

/**
 * Class representing a rectangle with properties such as width, height, position, and rotation state.
 */
public class Rectangle {
    private static int ID_COUNTER = 0;

    private final int rectID;
    private int boxID; // The ID of the box where the rectangle is placed
    private int height;
    private int width;

    private int x;
    private int y;
    private boolean rotated;

    /** Constructor for Rectangle.
     *
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     */
    public Rectangle(int width, int height){
        this.rectID = ID_COUNTER++;
        this.width = width;
        this.height = height;
        this.x = -1;
        this.y = -1;
        this.rotated = false;
        this.boxID = -1;
    }

    /** Copy constructor for Rectangle.
     *
     * @param other the rectangle to copy
     */
    protected Rectangle(Rectangle other){
        this.rectID = other.rectID;
        this.boxID = other.boxID;
        this.width = other.width;
        this.height = other.height;
        this.x = other.x;
        this.y = other.y;
        this.rotated = other.rotated;
    }

    /** Resets the ID counter for rectangles. */
    public static void resetIDCounter(){
        ID_COUNTER = 0;
    }

    /** Places the rectangle at the specified coordinates within a box.
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param boxID the ID of the box where the rectangle is placed
     */
    public void place(int x, int y, int boxID){
        this.boxID = boxID;
        this.x = x;
        this.y = y;
    }

    /** Rotates the rectangle by swapping its width and height. */
    public void rotate(){
        int oldH = this.height;
        this.height = this.width;
        this.width = oldH;
        this.rotated = !this.rotated;
    }

    /** Creates a copy of the rectangle.
     *
     * @return a new Rectangle instance that is a copy of this rectangle
     */
    public Rectangle copy(){
        return new Rectangle(this);
    }

    @Override
    public String toString() {
        return "Rectangle{" + ' ' +
                "id=" + this.rectID + ' ' +
                "width=" + this.width + ' ' +
                "height=" + this.height + ' ' +
                "x=" + this.x + ' ' +
                "y=" + this.y + '}';
    }

    // Getters
    public int getHeight(){
        return this.height;
    }
    public int getWidth(){
        return this.width;
    }
    public int getX(){ return this.x; }
    public int getY(){ return this.y; }
    public int getArea(){ return this.width * this.height; }
    public int getRectID(){ return this.rectID; }
    public int getBoxID(){ return this.boxID; }
    public boolean isRotated(){ return this.rotated; }
}
