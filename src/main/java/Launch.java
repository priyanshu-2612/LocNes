package main.java;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.input.*;
import javafx.event.EventHandler;


public class Launch extends Application {
    private GuiController controller;
    private Display display;
    private Canvas mainScreen;
    private Scene scene;
    private CPU cpu;
    private PPU ppu;
    public Tester t;

    public static void main(String[] args){
        launch(args);
    }

    public void launchGame(String path){

        t = new Tester(display, scene, cpu, ppu);
        t.setUpCartridge(path);
        t.readCartridge();
        t.setGuiController(controller);
        t.setUiDirty(true);
        t.runGame();

    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        BorderPane root = loader.load();

        controller = loader.getController();
        controller.setLauncher(this);

        controller.setStage(stage);

        MenuBar menuBar = controller.getMenuBar();
        mainScreen = controller.getMainScreen();
        scene = new Scene(root);
//        scene.setFill(Color.PEACHPUFF);


        stage.setScene(scene);
//        stage.getIcons().add(new Image("file:C:/Users/prash/Downloads/FFRK_Thunder_Dragon_FFIV.png"));
        stage.setTitle("NES Emulator");
        stage.setWidth(524.8);//760 //512
        stage.setHeight(((542.5 + menuBar.getHeight() - 7)));//552 //480
        stage.setResizable(false);


        ppu = new PPU();
        cpu = new CPU(ppu);
        display = new Display(controller, ppu);
        root.setTop(menuBar);
        stage.show();


        //TODO: Use gameloop

        //Timeline is slow and not suitable for NES

//        gameLoop.setCycleCount(Timeline.INDEFINITE);
//
//        KeyFrame kf = new KeyFrame(
//                Duration.seconds(0.0001),
//                actionEvent -> {
//                    try {
//                        double critical = 1790000.0 / 60.0;
//                        double cycles = 0;
//
//                        while (cycles < critical) {
//                            cycles += t.cycle();
//                        }
//                        //System.out.println(Arrays.toString(t.cpu.controller.controller_input));
//                        //System.out.println("SIZE OF CANVAS IS " + stage.getWidth() + " " + stage.getHeight());
//                    } catch (RuntimeException e) {
//                        System.out.println("GAME OVER");
//                        e.printStackTrace();
//                        gameLoop.stop();
//                    }
//                });
//
//        gameLoop.getKeyFrames().add(kf);
//
//        gameLoop.play();

    }

    public CPU getCPU(){
        return this.cpu;
    }
}
