package main.java;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.input.*;
import javafx.event.EventHandler;
import javafx.util.Duration;

import javax.swing.*;
import java.util.Arrays;


public class Launch extends Application {
    private static AnimationTimer gameLoop;
    private GuiController controller;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        BorderPane root = loader.load();

        controller = loader.getController();

        MenuBar menuBar = controller.getMenuBar();
        Canvas mainScreen = controller.getMainScreen();
        Scene scene = new Scene(root);
        scene.setFill(Color.PEACHPUFF);


        stage.setScene(scene);
        stage.setTitle("NES Emulator");
        stage.setWidth(Math.floor(524.8));//760 //512
        stage.setHeight(((542.5 + menuBar.getHeight())));//552 //480
        stage.setResizable(false);


        Display display = new Display(controller);
        root.setTop(menuBar);
//        root.setCenter(mainScreen);
        stage.show();

//        root.getChildren().add(menuBar);

        Tester t = new Tester();
        t.display = display;
        t.display.ppu = t.ppu;
        t.scene = scene;              //for running roms

        t.runCode();
//        t.display_pattern_table();

        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
//                System.out.println("Key is " + keyEvent.getCode().toString());
                switch(keyEvent.getCode().toString()){
                    case "X":
                        t.cpu.controller.controller_input[0] |= 0x80;
                        break;
                    case "Z":
                        t.cpu.controller.controller_input[0] |= 0x40;
                        break;
                    case "BACK_SPACE":
                        t.cpu.controller.controller_input[0] |= 0x20;
                        break;
                    case "ENTER":
                        t.cpu.controller.controller_input[0] |= 0x10;
                        break;
                    case "UP":
                        t.cpu.controller.controller_input[0] |= 0x08;
                        break;
                    case "DOWN":
                        t.cpu.controller.controller_input[0] |= 0x04;
                        break;
                    case "LEFT":
                        t.cpu.controller.controller_input[0] |= 0x02;
                        break;
                    case "RIGHT":
                        t.cpu.controller.controller_input[0] |= 0x01;
                        break;
                }
            }
        });
        scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
//                System.out.println("Key is " + keyEvent.getCode().toString());
                switch(keyEvent.getCode().toString()){
                    case "X":
                        t.cpu.controller.controller_input[0] ^= 0x80;
                        break;
                    case "Z":
                        t.cpu.controller.controller_input[0] ^= 0x40;
                        break;
                    case "BACK_SPACE":
                        t.cpu.controller.controller_input[0] ^= 0x20;
                        break;
                    case "ENTER":
                        t.cpu.controller.controller_input[0] ^= 0x10;
                        break;
                    case "UP":
                        t.cpu.controller.controller_input[0] ^= 0x08;
                        break;
                    case "DOWN":
                        t.cpu.controller.controller_input[0] ^= 0x04;
                        break;
                    case "LEFT":
                        t.cpu.controller.controller_input[0] ^= 0x02;
                        break;
                    case "RIGHT":
                        t.cpu.controller.controller_input[0] ^= 0x01;
                        break;
                }
            }
        });

        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().toString().equals("L"))
                    System.out.println("W : " + root.getWidth() + " H : " + root.getHeight());
            }
        });

        //TODO: Use gameloop
        gameLoop = new AnimationTimer() {
            private static double critical = 1790000.0 / 60.0;
            private static final double FRAME_DURATION_NS = 1_000_000_000.0 / 60.0; // ~16.67ms in nanoseconds
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                long start = System.nanoTime();
//                long start = now;

                try {
                    double cycles = 0;
                    while (cycles < critical) {
                        cycles += t.cycle();
                    }
                }
                catch (RuntimeException e) {
                    System.out.println("GAME OVER");
                    e.printStackTrace();
                    this.stop();
                    return;
                }
//                t.display_pattern_table();
                long elapsed = System.nanoTime() - start;
//                long elapsed = now - start;
//                System.out.println("Elapsed: " + (elapsed / 1_000_000.0) + " ms"); //for checking fps

                long sleepTimeNs = (long)(FRAME_DURATION_NS - elapsed);

                if (sleepTimeNs > 0) {
                    try {
                        Thread.sleep(sleepTimeNs / 1_000_000, (int)(sleepTimeNs % 1_000_000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                lastTime = now;
            }
        };
        gameLoop.start();

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
}
