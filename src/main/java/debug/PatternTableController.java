package main.java.debug;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import main.java.Launch;

public class PatternTableController {

    @FXML private Canvas patternScreen1;
    @FXML private Canvas patternScreen2;
    private Launch launcher;

    public void showPatternTables() {

        GraphicsContext gc1 = patternScreen1.getGraphicsContext2D();
        GraphicsContext gc2 = patternScreen2.getGraphicsContext2D();

        // TODO: replace with real pattern‑table draw logic
//        gc1.setFill(Color.GRAY);
//        gc1.fillRect(0, 0, 200, 200);
//
//        gc2.setFill(Color.DARKGRAY);
//        gc2.fillRect(0, 0, 200, 200);

        launcher.t.display_pattern_table(gc1, gc2);
    }

    public void setLauncher(Launch launch){
        this.launcher = launch;
    }
}
