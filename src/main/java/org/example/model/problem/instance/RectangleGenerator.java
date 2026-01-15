package org.example.model.problem.instance;

import org.example.model.problem.model.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for generating a list of random rectangles.
 */
public class RectangleGenerator {
    /**
     * Generates a list of random rectangles.
     *
     * @param seed      the seed for random number generation
     * @param n         the number of rectangles to generate
     * @param minWidth  the minimum width of the rectangles
     * @param maxWidth  the maximum width of the rectangles
     * @param minHeight the minimum height of the rectangles
     * @param maxHeight the maximum height of the rectangles
     * @return a list of generated rectangles
     */
    public static List<Rectangle> generate(int seed, int n, int minWidth, int maxWidth, int minHeight, int maxHeight){
        Rectangle.resetIDCounter();
        List<Rectangle> rectangles = new ArrayList<>();
        Random rand = new Random(seed);

        for(int i=0; i<n; i++){
            int width = rand.nextInt(minWidth, maxWidth);
            int height = rand.nextInt(minHeight, maxHeight);
            rectangles.add(new Rectangle(width, height));
        }

        return rectangles;
    }
}
