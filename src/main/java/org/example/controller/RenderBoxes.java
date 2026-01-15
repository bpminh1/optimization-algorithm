package org.example.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.model.core.Solution;
import org.example.model.problem.instance.PackingProblem;
import org.example.model.problem.model.Box;
import org.example.model.problem.model.Placement;
import org.example.model.problem.model.Rectangle;

import java.util.*;

/**
 * Class responsible for rendering boxes and their rectangles in a grid layout.
 */
public class RenderBoxes {
    private final GridPane grid;
    private final PackingProblem problem;
    private List<Map<Integer, RectSnapshot>> iterationSnapshots;
    private int numCols;

    /** Constructor for RenderBoxes.
     *
     * @param grid               the GridPane to render boxes into
     * @param problem            the packing problem instance
     * @param iterationSnapshots list of snapshots for each iteration
     */
    public RenderBoxes(
            GridPane grid,
            PackingProblem problem,
            List<Map<Integer, RectSnapshot>> iterationSnapshots
    ){
        this.grid = grid;
        this.problem = problem;
        this.iterationSnapshots = iterationSnapshots;
    }

    /** Sets the iteration snapshots.
     *
     * @param iterationSnapshots list of snapshots for each iteration
     */
    public void setIterationSnapshots(List<Map<Integer, RectSnapshot>> iterationSnapshots){
        this.iterationSnapshots = iterationSnapshots;
    }

    /** Renders boxes based on the provided solution.
     *
     * @param solution the solution containing box placements
     */
    public void renderBoxes(Solution solution){
        renderBoxes(solution, null);
    }

    /** Renders boxes based on the provided solution and previous snapshot.
     * The previous snapshot is used to highlight changes in rectangle placements.
     *
     * @param solution     the solution containing box placements
     * @param prevSnapshot the previous snapshot of rectangles for comparison
     */
    public void renderBoxes(Solution solution, Map<Integer, RectSnapshot> prevSnapshot){
        removeOldGrid();

        int numBoxes = ((Placement) solution).getNumBoxes();
        List<Double> boxCellSize = computeBoxCell(numBoxes);
        List<Box> boxes = ((Placement) solution).getBoxes();
        boxes.sort(Comparator.comparingInt(Box::getId));

        Map<Integer, List<Pane>> boxChangedRectangles = new HashMap<>();

        for(int i = 0; i < numBoxes; i++){
            Box box = boxes.get(i);
            int row = i / numCols;
            int col = i % numCols;

            Node boxCell = createBoxCell(
                    box, boxCellSize,
                    prevSnapshot,
                    boxChangedRectangles
            );

            grid.add(boxCell, col, row);
        }
        drawPreviousPositions(boxChangedRectangles);
    }

    /** Draws rectangles in their previous positions to indicate changes.
     *
     * @param boxChangedRectangles mapping of box IDs to lists of rectangle panes that changed
     */
    private void drawPreviousPositions(Map<Integer, List<Pane>> boxChangedRectangles) {
        for(Map.Entry<Integer, List<Pane>> entry : boxChangedRectangles.entrySet()){
            int boxID = entry.getKey();
            List<Pane> rectangles = entry.getValue();

            Node boxNode = grid.lookup("#box-" + boxID);
            if(boxNode instanceof StackPane stack){
                Pane borderPane = (Pane) stack.getUserData();
                Pane contentPane = (Pane) stack.getChildren().getFirst();

                for(Pane rectPane : rectangles){
                    contentPane.getChildren().add(rectPane);
                }

                borderPane.setBorder(new Border(new BorderStroke(
                        Color.DODGERBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2.0))));
            }
        }
    }

    /** Removes the old grid content and constraints. */
    private void removeOldGrid(){
        grid.getChildren().clear();
        grid.getRowConstraints().clear();
        grid.getColumnConstraints().clear();
    }

    /** Computes the size of each box cell in the grid layout.
     *
     * @param numBoxes the total number of boxes to render
     * @return a list containing box size, cell width, cell height, and rectangle scale
     */
    private List<Double> computeBoxCell(int numBoxes){
        numCols = (int) Math.ceil(Math.sqrt(numBoxes));
        int numRows = (int) Math.ceil((double) numBoxes / numCols);

        setGridConstraints(numRows);

        double gridWidth = grid.getWidth() > 0? grid.getWidth() : grid.getPrefWidth();
        double gridHeight = grid.getHeight() > 0? grid.getHeight() : grid.getPrefHeight();

        double cellWidth = gridWidth / numCols;
        double cellHeight = gridHeight / numRows;

        double boxSize = Math.min(cellWidth, cellHeight) - 5; // margin
        double rectScale = boxSize / problem.getL();

        return List.of(boxSize, cellWidth, cellHeight, rectScale);
    }

    /** Sets the row and column constraints for the grid layout.
     *
     * @param numRows the number of rows in the grid
     */
    private void setGridConstraints(int numRows) {
        for (int i = 0; i < numRows; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < numCols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }
    }

    /** Creates a cell for a box, including its rectangles and information.
     *
     * @param box                  the box to render
     * @param boxCellSize          list containing box size, cell width, cell height, and rectangle scale
     * @param prevSnapshot         the previous snapshot of rectangles for comparison
     * @param boxChangedRectangles mapping of box IDs to lists of rectangle panes that changed
     * @return a Node representing the box cell
     */
    private Node createBoxCell(
            Box box, List<Double> boxCellSize,
            Map<Integer, RectSnapshot> prevSnapshot,
            Map<Integer, List<Pane>> boxChangedRectangles
    ){
        double boxSize = boxCellSize.get(0);
        double cellWidth = boxCellSize.get(1);
        double cellHeight = boxCellSize.get(2);
        double rectScale = boxCellSize.get(3);

        Node boxNode = createBoxNode(box, boxSize);
        Text boxInfo = createBoxInfo(box);

        renderRetangles(
                boxNode, box.getRects(),
                rectScale, boxSize,
                prevSnapshot,
                boxChangedRectangles
        );

        // Decide layout based on cell dimensions
        if(cellHeight < cellWidth){ // Horizontal layout
            HBox hBox = new HBox(2);
            hBox.getChildren().addAll(boxInfo, boxNode);
            hBox.setAlignment(Pos.TOP_LEFT);
            return hBox;
        }else{ // Vertical layout
            VBox vBox = new VBox(2);
            vBox.getChildren().addAll(boxInfo, boxNode);
            vBox.setAlignment(Pos.TOP_CENTER);
            return vBox;
        }
    }

    /** Creates a visual representation of a box.
     *
     * @param box     the box to render
     * @param boxSize the size of the box
     * @return a Node representing the box
     */
    private Node createBoxNode(Box box, double boxSize){
        // Use StackPane so that we can have a border over the content pane
        StackPane root = new StackPane();
        root.setPrefSize(boxSize, boxSize);
        root.setMinSize(boxSize, boxSize);
        root.setMaxSize(boxSize, boxSize);

        Pane contentPane = new Pane();
        contentPane.setPrefSize(boxSize, boxSize);
        contentPane.setMinSize(boxSize, boxSize);
        contentPane.setMaxSize(boxSize, boxSize);
        contentPane.setBackground(new Background(new BackgroundFill(
                Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY)));

        Pane borderPane = new Pane();
        borderPane.setPrefSize(boxSize, boxSize);
        borderPane.setMinSize(boxSize, boxSize);
        borderPane.setMaxSize(boxSize, boxSize);
        borderPane.setMouseTransparent(true);
        borderPane.setBorder(new Border(new BorderStroke(
                Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        root.getChildren().addAll(contentPane, borderPane);
        root.setId("box-" + box.getId());
        root.setUserData(borderPane);

        return root;
    }

    /** Creates a text node containing information about the box.
     *
     * @param box the box to get information from
     * @return a Text node with box information
     */
    private Text createBoxInfo(Box box){
        Text info = new Text("Box id: " + box.getId() + "\n" +
                "#rects: " + box.getNumRects());
        info.setFill(Color.BLACK);
        return info;
    }

    /** Renders rectangles within a box node.
     *
     * @param boxNode              the Node representing the box
     * @param rects                the list of rectangles to render
     * @param rectScale            the scale factor for rectangle dimensions
     * @param boxSize              the size of the box
     * @param prevSnapshot         the previous snapshot of rectangles for comparison
     * @param boxChangedRectangles mapping of box IDs to lists of rectangle panes that changed
     */
    private void renderRetangles(
            Node boxNode, List<Rectangle> rects,
            double rectScale, double boxSize,
            Map<Integer, RectSnapshot> prevSnapshot,
            Map<Integer, List<Pane>> boxChangedRectangles
    ){
        boolean hasChangedRectangles = false;

        for(Rectangle rect : rects){
            double width = rect.getWidth() * rectScale;
            double height = rect.getHeight() * rectScale;
            double x = rect.getX() * rectScale;
            double y = boxSize - rect.getY() * rectScale;

            // Default colors
            Color fillColor = Color.LIGHTGREY;
            Color strokeColor = Color.DARKGREY;
            double strokeWidth = 0.5;

            if(prevSnapshot != null){
                RectSnapshot prevRect = prevSnapshot.get(rect.getRectID());

                if(prevRect == null){
                    // New rectangle
                    fillColor = Color.CORNFLOWERBLUE;
                    strokeColor = Color.ROYALBLUE;
                    strokeWidth = 1.0;
                } else if(!prevRect.sameAs(
                        new RectSnapshot(
                                rect.getRectID(), rect.getBoxID(),
                                rect.getX(), rect.getY(),
                                rect.getWidth(), rect.getHeight(),
                                rect.isRotated()))){
                    // Changed rectangle
                    fillColor = Color.CORNFLOWERBLUE;
                    strokeColor = Color.ROYALBLUE;
                    strokeWidth = 1.0;

                    createPreviousPositionPane(rectScale, boxSize, boxChangedRectangles, prevRect);
                    hasChangedRectangles = true;
                }
            }

            Pane currRectanglePane = createRectanglePane(
                    width, height,
                    x, y,
                    strokeColor, BorderStrokeStyle.SOLID, strokeWidth,
                    fillColor
            );
            Pane contentPane = (Pane) ((StackPane) boxNode).getChildren().getFirst();
            contentPane.getChildren().add(currRectanglePane);

            if(hasChangedRectangles){ // Highlight box if it has changed rectangles
                Pane borderPane = (Pane) ((StackPane) boxNode).getUserData();
                borderPane.setBorder(new Border(new BorderStroke(
                        Color.DODGERBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2.0))));
            }
        }
    }

    /** Creates a pane for the previous position of a rectangle and adds it to the changed rectangles map.
     *
     * @param rectScale            the scale factor for rectangle dimensions
     * @param boxSize              the size of the box
     * @param boxChangedRectangles mapping of box IDs to lists of rectangle panes that changed
     * @param prevRect             the previous snapshot of the rectangle
     */
    private void createPreviousPositionPane(double rectScale, double boxSize, Map<Integer, List<Pane>> boxChangedRectangles, RectSnapshot prevRect) {
        Pane prevRectanglePane = createRectanglePane(
                prevRect.width() * rectScale, prevRect.height() * rectScale,
                prevRect.x() * rectScale, boxSize - prevRect.y() * rectScale,
                Color.DODGERBLUE, BorderStrokeStyle.DASHED, 1.0,
                Color.LIGHTBLUE
        );

        int prevBoxID = prevRect.boxID();
        if(boxChangedRectangles.containsKey(prevBoxID))
            boxChangedRectangles.get(prevBoxID).add(prevRectanglePane);
        else {
            List<Pane> paneList = new ArrayList<>();
            paneList.add(prevRectanglePane);
            boxChangedRectangles.put(prevBoxID, paneList);
        }
    }

    /** Creates a pane representing a rectangle with specified properties.
     *
     * @param width        the width of the rectangle
     * @param height       the height of the rectangle
     * @param x            the x-coordinate of the rectangle
     * @param y            the y-coordinate of the rectangle
     * @param strokeColor  the color of the rectangle's border
     * @param strokeStyle  the style of the rectangle's border
     * @param strokeWidth  the width of the rectangle's border
     * @param fillColor    the fill color of the rectangle
     * @return a Pane representing the rectangle
     */
    private Pane createRectanglePane(double width, double height,
                                     double x, double y,
                                     Color strokeColor, BorderStrokeStyle strokeStyle, double strokeWidth,
                                     Color fillColor
    ){
        Pane pane = new Pane();
        pane.setPrefSize(width, height);
        pane.setMinSize(width, height);
        pane.setMaxSize(width, height);
        pane.setLayoutX(x);
        pane.setLayoutY(y - height); // javafx y-coordinate is in the top-left corner

        pane.setBorder(new Border(new BorderStroke(
                strokeColor, strokeStyle, CornerRadii.EMPTY, new BorderWidths(strokeWidth))));
        pane.setBackground(new Background(new BackgroundFill(
                fillColor, CornerRadii.EMPTY, Insets.EMPTY)));

        return pane;
    }
}
