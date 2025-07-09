package com.locnes.debug;
import com.locnes.Launch;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.concurrent.TimeoutException;

public class NameTableController {

    @FXML public Canvas nameTable0;
    @FXML public Canvas nameTable1;
    @FXML public Canvas nameTable2;
    @FXML public Canvas nameTable3;
    private Launch launcher;
    private GraphicsContext gc1, gc2, gc3, gc4;

    private AnimationTimer renderLoop;

    public void initialize() {
        gc1 = nameTable0.getGraphicsContext2D();
        gc2 = nameTable1.getGraphicsContext2D();
        gc3 = nameTable2.getGraphicsContext2D();
        gc4 = nameTable3.getGraphicsContext2D();
        renderLoop = new AnimationTimer() {
            long last = 0;
            public void handle(long now) {
                if (now - last >= 100_000_000) { // every 100ms = 10 FPS
                    showNameTables();
                    last = now;
                }
            }
        };
    }

    public void startRendering() {
        if (renderLoop != null) renderLoop.start();
    }

    public void stopRendering() {
        if (renderLoop != null) renderLoop.stop();
    }

    public void showNameTables() {
        launcher.t.draw_nametable(gc1, 0x2000);
        launcher.t.draw_nametable(gc2, 0x2400);
        launcher.t.draw_nametable(gc3, 0x2800);
        launcher.t.draw_nametable(gc4, 0x2c00);
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
