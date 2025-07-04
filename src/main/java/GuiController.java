package main.java;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {
    @FXML
    private BorderPane root;
    @FXML
    private MenuBar menuBar;
    @FXML
    public Canvas mainScreen;

    private MenuBar instantiateMenuBar() {
        Menu fileMenu = new Menu("file");
        MenuItem open = new MenuItem("Open NES...");
        fileMenu.getItems().add(open);

        Menu debugMenu = new Menu("Debug");
        MenuItem patternTableMenuItem = new MenuItem("Show Pattern Table");
        debugMenu.getItems().add(patternTableMenuItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, debugMenu);
        return  menuBar;
    }

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
        menuBar = instantiateMenuBar();
//        mainScreen.setWidth(528);
//        mainScreen.setHeight(517);
        BorderPane.setAlignment(mainScreen, Pos.TOP_LEFT);
        mainScreen.widthProperty().bind(root.widthProperty());

        // height = full window height minus menu‑bar height
        mainScreen.heightProperty().bind(
                root.heightProperty().subtract(menuBar.heightProperty()));
    }
}
