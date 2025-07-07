package main.java.help;

import javafx.scene.Parent;

import java.net.URI;

public class AboutController{

    public void handleGitHubLink() {
        try {
            java.awt.Desktop.getDesktop().browse(new URI("https://github.com/priyanshu-2612/NEMUlator"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
