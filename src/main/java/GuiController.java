package main.java;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.debug.CpuDumpController;
import main.java.debug.NameTableController;
import main.java.debug.PatternTableController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.util.ResourceBundle;

public class GuiController implements Initializable {
    @FXML
    private BorderPane root;
    @FXML
    private MenuBar menuBar;
    @FXML
    public Canvas mainScreen;

    private Stage stage;
    private Launch launcher;
    private PatternTableController ptController;
    private NameTableController nametController;
    private CpuDumpController cpuDumpController;

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
    }

    public Canvas getMainScreen() {
        return mainScreen;
    }

    public void setMainScreen(Canvas mainScreen) {
        this.mainScreen = mainScreen;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BorderPane.setAlignment(mainScreen, Pos.TOP_LEFT);
        mainScreen.widthProperty().bind(root.widthProperty());
        mainScreen.heightProperty().bind(
                root.heightProperty().subtract(menuBar.heightProperty()));
        Platform.runLater(() -> {
            GraphicsContext gc = mainScreen.getGraphicsContext2D();
            gc.setFill(Color.web("#2e2e2e")); // dark gray
            gc.fillRect(0, 0, mainScreen.getWidth(), mainScreen.getHeight());
        });
    }

    public void openNesFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open NES ROM");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("NES files (*.nes)", "*.nes"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Set default directory to Downloads
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (downloadsDir.exists()) {
            fileChooser.setInitialDirectory(downloadsDir);
        }

        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());

        if (selectedFile != null) {
            System.out.println("Selected ROM: " + selectedFile.getAbsolutePath());
            launcher.launchGame(selectedFile.getAbsolutePath());
        }
    }

    public void showPatternTables(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("debug/pt_table.fxml"));
            Parent root = loader.load();

            ptController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Pattern Tables");
            Scene scene = new Scene(root);

            scene.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.Q) {
                    launcher.t.display.palette_num = (launcher.t.display.palette_num + 1) % 4;
                    ptController.showPatternTables();
                }
            });

            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(menuBar.getScene().getWindow()); // optional
            stage.show();

            ptController.setLauncher(launcher);
            ptController.showPatternTables();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showNameTables(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("debug/name_table.fxml"));
            Parent root = loader.load();

            nametController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Name Tables");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initOwner(menuBar.getScene().getWindow()); // optional
            stage.show();

            nametController.setLauncher(launcher);
            nametController.showNameTables();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seeCPU(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("debug/cpu_debug.fxml"));
            Parent root = loader.load();

            cpuDumpController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("CPU Dissassembly");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initOwner(menuBar.getScene().getWindow()); // optional
            stage.show();

            cpuDumpController.setLauncher(launcher);
            cpuDumpController.setCPU(launcher.getCPU());
            launcher.t.setDebugController(cpuDumpController);

            stage.setOnCloseRequest(e -> {
                launcher.t.onClose();
                cpuDumpController.markClosed();
            });

            stage.getScene().setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case P -> {
                        launcher.t.togglePause();
                        cpuDumpController.setPausedFlag();
                    }
                    case R -> {
                        launcher.t.pause();
                        cpuDumpController.setPausedFlag();
                        launcher.t.singleStep();
                        cpuDumpController.showCpuDump();
                    }
                }
            });


            cpuDumpController.showCpuDump();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void clearScreens() {
        if(cpuDumpController!=null)
            cpuDumpController.showCpuDump();
        if(nametController != null)
            nametController.showNameTables();
    }

    public void setLauncher(Launch launch){
        this.launcher = launch;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
