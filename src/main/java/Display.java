package main.java;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

import java.nio.IntBuffer;
import java.util.Random;

public class Display {

    GraphicsContext gc, gc_pt;
//    Canvas mainScreen = new Canvas(256*12,256*12);
//    Canvas mainScreen = new Canvas(528,517); //528,517
    Canvas mainScreen;
    Canvas patternScreen = new Canvas(256*3,128*3);
    PPU ppu;
    java.awt.Color[] greyscale;
    int palette_num =0;
    float SCALE = 1.5F;

    Display(GuiController controller, PPU ppu){
//        super(256*12,256*12);
        mainScreen = controller.getMainScreen();
        gc = mainScreen.getGraphicsContext2D();
//        patternScreen.setLayoutX(538);
//        patternScreen.setLayoutY(10);
//        gc_pt = patternScreen.getGraphicsContext2D();
        this.ppu = ppu;
        greyscale = new java.awt.Color[4];
        greyscale[0] = new java.awt.Color(64, 64, 64);   // Dark grey
        greyscale[1] = new java.awt.Color(128, 128, 128); // Medium grey
        greyscale[2] = new java.awt.Color(192, 192, 192); // Light grey
        greyscale[3] = new java.awt.Color(255, 255, 255); // White (brightest grey)
    }

    public void setPixel(int x , int y , int paletteID){
        gc.setFill(getFXColor(ppu.palScreen[paletteID]));
        int scale = 2;
        gc.fillRect(x*scale , y*scale , scale ,scale);
    }

    public void render(){
        WritableImage wi = new WritableImage(256 , 256);
        PixelWriter pw = wi.getPixelWriter();
        WritablePixelFormat<IntBuffer> pf = WritablePixelFormat.getIntArgbInstance();
        Random rand = new Random();
        for(int i=0 ; i<256 ; i++){
            for(int j=0 ; j<256 ; j++){
                int n = rand.nextInt(2);
                javafx.scene.paint.Color c;
                if(n==0) c = javafx.scene.paint.Color.BLACK;
                else c = Color.WHITE;
                int scale = 1;
                gc.setFill(c);
                gc.fillRect(i*scale, (j*scale), scale, scale);
            }
        }
    }

    public void drawTile(GraphicsContext gc, int[][] sprite, int col, int row){
        Color c;
        float scale = 1;
        double baseX = (col) * 8 * scale;
        double baseY = (row) * 8 * scale;
        for(int i=0 ; i<8 ; i++){
        for(int j=0 ; j<8 ; j++){
            c = getFXColor(greyscale[sprite[i][j]]);
            gc.setFill(c);
            gc.fillRect(baseX + j*scale, baseY + i*scale, scale, scale);
            }
        }
    }

    public void draw_tile(int[][] tile, int x, int y){
        Color c;
        float scale = 1.5f;
        x *= 8*scale;
        y *= 8*scale;
        for(int i=0 ; i<8 ; i++){
            for(int j=0 ; j<8 ; j++){
                c = getFXColor(greyscale[tile[i][j]]);
                    gc.setFill(c);
                    gc.fillRect(x+j*scale,y+i*scale,scale,scale);

            }
        }
    }

    public void draw_chr_rom(GraphicsContext gc_pt, int[][] tile, int x, int y){
        Color c;
        float scale = 1.5f;
        x *= 8*scale;
        y *= 8*scale;
        for(int i=0 ; i<8 ; i++){
            for(int j=0 ; j<8 ; j++){
                int paletteID = ppu.getColor(palette_num , tile[i][j]);
                c = (getFXColor(ppu.palScreen[paletteID]));
                    gc_pt.setFill(c);
                    gc_pt.fillRect(x+j*scale,y+i*scale,scale,scale);
            }
        }
    }
    public Color getFXColor(java.awt.Color awtColor){
        int r = awtColor.getRed();
        int g = awtColor.getGreen();
        int b = awtColor.getBlue();
        int a = awtColor.getAlpha();
        double opacity = a / 255.0 ;
        javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(r, g, b, opacity);
        return fxColor;
    }
}
