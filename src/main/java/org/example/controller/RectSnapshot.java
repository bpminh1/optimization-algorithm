package org.example.controller;

/**
 * Represents a snapshot of a rectangle's state within a box.
 *
 * @param rectID   the unique identifier of the rectangle
 * @param boxID    the identifier of the box containing the rectangle
 * @param x        the x-coordinate of the rectangle's position
 * @param y        the y-coordinate of the rectangle's position
 * @param width    the width of the rectangle
 * @param height   the height of the rectangle
 * @param rotated  indicates whether the rectangle is rotated
 */
public record RectSnapshot (int rectID, int boxID, int x, int y, int width, int height, boolean rotated) {
    /** Compares this RectSnapshot with another for equality.
     *
     * @param other the other RectSnapshot to compare with
     * @return true if both snapshots are the same, false otherwise
     */
    boolean sameAs(RectSnapshot other){
        return other != null &&
                this.boxID == other.boxID &&
                this.x == other.x &&
                this.y == other.y &&
                this.width == other.width &&
                this.height == other.height &&
                this.rotated == other.rotated;
    }
}
