package main.java;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.input.*;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.Arrays;


public class Launch extends Application {
    private static Timeline gameLoop;
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        AnchorPane root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root);


        stage.setScene(scene);
        stage.setTitle("NES Emulator");
        stage.setWidth(760);
        stage.setHeight(552);
        stage.setResizable(true);


        Display display = new Display();
        root.getChildren().add(display.mainScreen);
        root.getChildren().add(display.patternScreen);
        stage.show();

        Tester t = new Tester();
        t.display = display;
        t.scene = scene;              //for running roms

        t.runCode();
        t.display_pattern_table();

        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                System.out.println("Key is " + keyEvent.getCode().toString());
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
                System.out.println("Key is " + keyEvent.getCode().toString());
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

        //TODO: Use gameloop
        gameLoop = new Timeline();
        gameLoop.setCycleCount(Timeline.INDEFINITE);

        KeyFrame kf = new KeyFrame(
                Duration.seconds(0.0001),
                actionEvent -> {
                    try {
                        t.cycle();
                        System.out.println(Arrays.toString(t.cpu.controller.controller_input));
                        System.out.println("SIZE OF CANVAS IS " + stage.getWidth() + " " + stage.getHeight());
                    } catch (RuntimeException e) {
                        System.out.println("GAME OVER");
                        e.printStackTrace();
                        gameLoop.stop();
                    }
                });

        gameLoop.getKeyFrames().add(kf);

        gameLoop.play();

    }
}
