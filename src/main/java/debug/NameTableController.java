package main.java.debug;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import main.java.Launch;

import java.util.concurrent.TimeoutException;

public class NameTableController {

    @FXML public Canvas nameTable0;
    @FXML public Canvas nameTable1;
    @FXML public Canvas nameTable2;
    @FXML public Canvas nameTable3;
    private Launch launcher;

    public void showNameTables() {
        GraphicsContext gc1 = nameTable0.getGraphicsContext2D();
        GraphicsContext gc2 = nameTable1.getGraphicsContext2D();
        GraphicsContext gc3 = nameTable2.getGraphicsContext2D();
        GraphicsContext gc4 = nameTable3.getGraphicsContext2D();
        launcher.t.draw_nametable(gc1, gc2, gc3, gc4);
//        fill(nameTable0.getGraphicsContext2D(), Color.LIGHTGRAY);
//        fill(nameTable1.getGraphicsContext2D(), Color.BEIGE);
//        fill(nameTable2.getGraphicsContext2D(), Color.LIGHTBLUE);
//        fill(nameTable3.getGraphicsContext2D(), Color.LIGHTGREEN);
    }

    private void fill(GraphicsContext gc, Color c) {
        gc.setFill(c);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    public void setLauncher(Launch launch){
        this.launcher = launch;
    }
}
