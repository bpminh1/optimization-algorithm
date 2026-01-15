package org.example.model.problem.neighborhoods;

import org.example.model.core.Solution;
import org.example.model.core.localSearch.Neighborhood;
import org.example.model.problem.model.Box;
import org.example.model.problem.model.Placement;
import org.example.model.problem.model.Rectangle;

import java.util.*;

/**
 * Neighborhood structure for local search based on geometric operations
 * such as shifting, snapping, and rotating rectangles within boxes.
 */
public class GeometryBased implements Neighborhood {
    Random random;

    /** Constructor for GeometryBased neighborhood. */
    public GeometryBased(){
        random = new Random(42);
    }

    /**
     * Generates a neighboring solution by applying geometric operations
     * on rectangles within boxes.
     *
     * @param currentSolution the current solution
     * @return a neighboring solution
     */
    @Override
    public Solution getNeighbor(Solution currentSolution) {
        Placement current = (Placement) currentSolution.copy();
        current.sortBoxes();
        List<Box> boxes = current.getBoxes();

        if(boxes.size() <= 1) return current;

        boolean newSolution = false;
        int attempts = 0;
        int attemptsLimit = 50;

        while(!newSolution && attempts < attemptsLimit) {
            attempts++;
            double prob = random.nextDouble();

            /*
            Strategy selection based on probability:
            - 40%: Consolidate sparse box
            - 30%: Shift rectangle
            - 15%: Snap rectangle
            - 15%: Rotate rectangle
             */
            if (prob < 0.4) newSolution = consolidateSparseBox(boxes.getLast(), boxes);
            else {
                Box box = boxes.get(random.nextInt(boxes.size()));
                List<Rectangle> rects = box.getRects();

                if (!rects.isEmpty()) {
                    List<Rectangle> sampled = sampleRectangles(rects);

                    if (prob < 0.7) newSolution = shiftRectangle(box, sampled);
                    else if (prob < 0.85) newSolution = snapRectangle(box, sampled);
                    else newSolution = rotateRectangle(box, sampled);
                }
            }
        }

        current.removeEmptyBox();
        return current;
    }

    /**
     * Attempts to consolidate rectangles from the sparsest box into other boxes.
     *
     * @param sparseBox the box with the fewest rectangles
     * @param boxes the list of all boxes
     * @return true if consolidation was successful, false otherwise
     */
    private boolean consolidateSparseBox(Box sparseBox, List<Box> boxes){
        if(boxes.size() <= 1) return false;

        List<Rectangle> rectsToMove = sparseBox.getRects();
        if(rectsToMove.isEmpty()) return false;

        Rectangle rect = rectsToMove.get(random.nextInt(rectsToMove.size()));
        List<Box> candidateBoxes = new ArrayList<>(boxes.subList(0, boxes.size() - 1));
        Collections.shuffle(candidateBoxes, random);

        int maxAttempts = Math.min(5, candidateBoxes.size());
        for(int i = 0; i < maxAttempts; i++){
            Box box = candidateBoxes.get(i);
            if(box.getId() == sparseBox.getId()) continue;

            if(box.place(rect)){
                sparseBox.remove(rect);
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to shift rectangles within a box to new positions.
     *
     * @param box the box containing rectangles
     * @param sampled the list of sampled rectangles to shift
     * @return true if a rectangle was successfully shifted, false otherwise
     */
    private boolean shiftRectangle(Box box, List<Rectangle> sampled){
        int[] smallestShift = box.getSmallestRectangleSize();
        int L = box.getL();

        for(Rectangle rect : sampled){
            int x = rect.getX();
            int y = rect.getY();
            int h = rect.getHeight();
            int w = rect.getWidth();

            box.remove(rect);

            List<Integer>[] options = generateShiftOptions(smallestShift, w, h, L);
            List<Integer> dxOptions = options[0];
            List<Integer> dyOptions = options[1];

            if(tryShiftRectangle(dxOptions, dyOptions, box, rect, x, y)){
                return true;
            }

            box.place(x, y, rect);
        }

        return false;
    }

    /**
     * Tries to shift a rectangle within a box using provided shift options in all four directions.
     *
     * @param dxOptions the list of possible x-shifts
     * @param dyOptions the list of possible y-shifts
     * @param box the box containing the rectangle
     * @param rect the rectangle to be shifted
     * @param x the current x-coordinate of the rectangle
     * @param y the current y-coordinate of the rectangle
     * @return true if the rectangle was successfully shifted, false otherwise
     */
    private boolean tryShiftRectangle(List<Integer> dxOptions, List<Integer> dyOptions, Box box, Rectangle rect, int x, int y){
        for(int dx : dxOptions){
            for(int dy : dyOptions){
                List<int[]> directions = Arrays.asList(
                        new int[]{dx, 0}, new int[]{-dx, 0},
                        new int[]{0, dy}, new int[]{0, -dy});
                Collections.shuffle(directions, random);

                for(int[] dir : directions){
                    int newX = x + dir[0];
                    int newY = y + dir[1];

                    if(box.canPlace(newX, newY, rect)){
                        box.place(newX, newY, rect);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Attempts to snap rectangles within a box to nearby aligned positions.
     *
     * @param box the box containing rectangles
     * @param sampled the list of sampled rectangles to snap
     * @return true if a rectangle was successfully snapped, false otherwise
     */
    private boolean snapRectangle(Box box, List<Rectangle> sampled){
        int L = box.getL();

        for(Rectangle rect : sampled){
            int x = rect.getX();
            int y = rect.getY();
            int h = rect.getHeight();
            int w = rect.getWidth();

            box.remove(rect);

            List<int[]> candidates = generateSnapPositions(box, L, rect, x, y, w, h);

            if(candidates.isEmpty()){
                box.place(x, y, rect);
                continue;
            }

            int chosenIndex = chooseSnapPosition(candidates, x, y);
            int[] chosen = candidates.get(chosenIndex);
            box.place(chosen[0], chosen[1], rect);
            return true;
        }

        return false;
    }

    /**
     * Chooses a snap position from the list of candidates based on weighted probabilities.
     * The weights favor positions that move the rectangle closer to the origin (0,0).
     *
     * @param candidates the list of candidate positions
     * @param x the current x-coordinate of the rectangle
     * @param y the current y-coordinate of the rectangle
     * @return the index of the chosen candidate position
     */
    private int chooseSnapPosition(List<int[]> candidates, int x, int y){
        int totalWeight = 0;
        List<Integer> weights = new ArrayList<>();
        for(int[] c : candidates){
            int weight = 1;
            if(x > c[0]) weight += 2;
            if(y > c[1]) weight += 2;
            totalWeight += weight;
            weights.add(weight);
        }

        int r = random.nextInt(totalWeight);
        int chosenIndex = 0;
        for(int i = 0; i < weights.size(); i++){
            r -= weights.get(i);
            if(r < 0){
                chosenIndex = i;
                break;
            }
        }

        return chosenIndex;
    }

    /**
     * Attempts to rotate rectangles within a box by 90˚.
     *
     * @param box the box containing rectangles
     * @param sampled the list of sampled rectangles to rotate
     * @return true if a rectangle was successfully rotated, false otherwise
     */
    private boolean rotateRectangle(Box box, List<Rectangle> sampled){
        int L = box.getL();

        for(Rectangle rect : sampled){
            if(rect.getWidth() == rect.getHeight()) continue;

            int x = rect.getX();
            int y = rect.getY();

            box.remove(rect);

            rect.rotate();
            int w = rect.getWidth();
            int h = rect.getHeight();

            List<int[]> positions = generateRotatePositions(box, L, rect, x, y, w, h);

            if(positions.isEmpty()){
                rect.rotate();
                box.place(x, y, rect);
                continue;
            }

            int[] position = positions.get(random.nextInt(positions.size()));
            box.place(position[0], position[1], rect);
            return true;
        }

        return false;
    }

    /**
     * Samples up to 5 rectangles randomly from the provided list.
     *
     * @param rects the list of rectangles to sample from
     * @return a list of sampled rectangles
     */
    public List<Rectangle> sampleRectangles(List<Rectangle> rects){
        List<Rectangle> copy = new ArrayList<>(rects);
        Collections.shuffle(copy, random);
        return copy.subList(0, Math.min(rects.size(), 5));
    }

    /**
     * Generates shift options based on the smallest rectangle size in the box
     * and the to be shifted rectangle size.
     *
     * @param smallestShift the dimensions of the smallest rectangle in the box
     * @param w the width of the rectangle to be shifted
     * @param h the height of the rectangle to be shifted
     * @param L the size of the box
     * @return an array containing two lists: possible x-shifts and y-shifts
     */
    private List<Integer>[] generateShiftOptions(int[] smallestShift, int w, int h, int L){
        // Add options to shift by 1, by smallest rectangle size, and by full width/height
        List<Integer> dxOptions = new ArrayList<>(Arrays.asList(1, smallestShift[0], w));
        List<Integer> dyOptions = new ArrayList<>(Arrays.asList(1, smallestShift[1], h));

        // The lower bound for shifting is the maximum between the smallest rectangle size and rectangle size
        int lowerBoundX = Math.max(smallestShift[0], w);
        int lowerBoundY = Math.max(smallestShift[1], h);
        // The upper bound is the last possible position that the rectangle can be placed after shifting
        int upperBoundX = L - w + 1 - lowerBoundX;
        int upperBoundY = L - h + 1 - lowerBoundY;
        
        for(int i = 0; i < 3; i++){
            if(upperBoundX <= 0 || upperBoundY <= 0) break;
            dxOptions.add(lowerBoundX + Math.max(0, random.nextInt(upperBoundX)));
            dyOptions.add(lowerBoundY + Math.max(0, random.nextInt(upperBoundY)));
        }

        Collections.shuffle(dxOptions, random);
        Collections.shuffle(dyOptions, random);

        return new List[]{dxOptions, dyOptions};
    }

    /** Generates snap positions for a rectangle within a box
     * by exploring vertical, horizontal, and diagonal movements.
     *
     * @param box the box containing the rectangle
     * @param L the size of the box
     * @param rect the rectangle to be snapped
     * @param x the current x-coordinate of the rectangle
     * @param y the current y-coordinate of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @return a list of candidate snap positions
     */
    private List<int[]> generateSnapPositions(Box box, int L, Rectangle rect, int x, int y, int w, int h){
        List<int[]> candidates = new ArrayList<>();

        generateSnapPositionsVertical(box, L, candidates, rect, x, y, w);
        generateSnapPositionsHorizontal(box, L, candidates, rect, x, y, h);
        generateSnapPositionsDiagonal(box, L, candidates, rect, x, y, w, h);

        Collections.shuffle(candidates, random);
        return candidates;
    }

    /** Generates snap positions by moving vertically until an obstacle is encountered.
     *
     * @param box the box containing the rectangle
     * @param L the size of the box
     * @param candidates the list to store candidate snap positions
     * @param rect the rectangle to be snapped
     * @param x the current x-coordinate of the rectangle
     * @param y the current y-coordinate of the rectangle
     * @param w the width of the rectangle
     */
    private void generateSnapPositionsVertical(Box box, int L, List<int[]> candidates, Rectangle rect, int x, int y, int w){
        int newX = x;
        while(newX > 0 && box.canPlace(newX - 1, y, rect)) newX--;
        if(newX != x) candidates.add(new int[]{newX, y});

        newX = x;
        while(newX < L - w && box.canPlace(newX + 1, y, rect)) newX++;
        if(newX != x) candidates.add(new int[]{newX, y});
    }

    /** Generates snap positions by moving horizontally until an obstacle is encountered.
     *
     * @param box the box containing the rectangle
     * @param L the size of the box
     * @param candidates the list to store candidate snap positions
     * @param rect the rectangle to be snapped
     * @param x the current x-coordinate of the rectangle
     * @param y the current y-coordinate of the rectangle
     * @param h the height of the rectangle
     */
    private void generateSnapPositionsHorizontal(Box box, int L, List<int[]> candidates, Rectangle rect, int x, int y, int h){
        int newY = y;
        while(newY > 0 && box.canPlace(x, newY - 1, rect)) newY--;
        if(newY != y) candidates.add(new int[]{x, newY});

        newY = y;
        while(newY < L - h && box.canPlace(x, newY + 1, rect)) newY++;
        if(newY != y) candidates.add(new int[]{x, newY});
    }

    /** Generates snap positions by moving diagonally until an obstacle is encountered.
     *
     * @param box the box containing the rectangle
     * @param L the size of the box
     * @param candidates the list to store candidate snap positions
     * @param rect the rectangle to be snapped
     * @param x the current x-coordinate of the rectangle
     * @param y the current y-coordinate of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     */
    private void generateSnapPositionsDiagonal(Box box, int L, List<int[]> candidates, Rectangle rect, int x, int y, int w, int h){
        int newX = x;
        int newY = y;
        // Move down-left
        while(newX > 0 && newY > 0 && box.canPlace(newX - 1, newY - 1, rect)){
            newX--; newY--;
        }
        if(newX != x || newY != y) candidates.add(new int[]{newX, newY});

        newX = x;
        newY = y;
        // Move up-left
        while(newX > 0 && newY < L - h && box.canPlace(newX - 1, newY + 1, rect)){
            newX--; newY++;
        }
        if(newX != x || newY != y) candidates.add(new int[]{newX, newY});

        newX = x;
        newY = y;
        // Move down-right
        while(newX < L - w && newY > 0 && box.canPlace(newX + 1, newY - 1, rect)){
            newX++; newY--;
        }
        if(newX != x || newY != y) candidates.add(new int[]{newX, newY});

        newX = x;
        newY = y;
        // Move up-right
        while(newX < L - w && newY < L - h && box.canPlace(newX + 1, newY + 1, rect)){
            newX++; newY++;
        }
        if(newX != x || newY != y) candidates.add(new int[]{newX, newY});
    }

    /** Generates possible positions for a rotated rectangle within a box
     * by exploring a square area around its original position.
     *
     * @param box the box containing the rectangle
     * @param L the size of the box
     * @param rect the rectangle to be rotated
     * @param x the current x-coordinate of the rectangle
     * @param y the current y-coordinate of the rectangle
     * @param w the width of the rotated rectangle
     * @param h the height of the rotated rectangle
     * @return a list of possible positions for the rotated rectangle
     */
    private List<int[]> generateRotatePositions(Box box, int L, Rectangle rect, int x, int y, int w, int h){
        List<int[]> positions = new ArrayList<>();

        int r = Math.max(w, h);

        int down = Math.max(0, y - r);
        int up = Math.min(y + r, L - h);
        int left = Math.max(0, x - r);
        int right = Math.min(x + r, L - w);

        for(int newY = down; newY <= up; newY++){
            for(int newX = left; newX <= right; newX++){
                if(box.canPlace(newX, newY, rect)){
                    positions.add(new int[]{newX, newY});
                }
            }
        }

        Collections.shuffle(positions, random);
        return positions;
    }

    @Override
    public double getCurrentAllowedOverlap() {
        // Overlap is not allowed in this neighborhood
        return 0;
    }

    @Override
    public void updateAllowedOverlap() {
        // Overlap is not allowed in this neighborhood
    }
}