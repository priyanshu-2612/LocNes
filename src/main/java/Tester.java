package main.java;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

import java.util.Random;


public class Tester {
    public Display display;
    private static AnimationTimer gameLoop;
    Decoder decoder;
    CPU cpu;
    PPU ppu;
    int[] game_code;
    Cartridge cartridge;
    Bus bus;
    Scene scene;
    long SystemCounter = 0;

    public Tester(Display display, Scene scene, CPU cpu, PPU ppu) {
        this.display = display;
        this.scene = scene;
        this.ppu = ppu;
        this.cpu = cpu;
        bus = new Bus(cpu,ppu);
        decoder = new Decoder(cpu,ppu);
    }

    public void setUpCartridge(String path){
        cartridge = new Cartridge(path);
        cartridge.InitCartridge();
        bus.insertCartridge(cartridge);
    }

    public void runGame(){

        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }

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
                        cycles += cycle();
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
    }

    public int cycle(){
            int cycles=0, cpu_cycles;
            if(cpu.dma_transfer){
                if(cpu.dma_dummy){
                    if(SystemCounter % 2 == 1){
                        cpu.dma_dummy = false;
                    }
                }
                else{
                    if(SystemCounter % 2 == 0){
                        int readAddress = (cpu.dma_page << 8) | (cpu.dma_address);
                        cpu.dma_data = cpu.getData(readAddress);
                    }
                    else{
                        ppu.writeToOAM(cpu.dma_address, (byte) cpu.dma_data);
                        cpu.dma_address = (cpu.dma_address + 1) & 0xFF;

                        if(cpu.dma_address == 0x00){
                            //dma transfer done
                            cpu.dma_transfer = false;
                            cpu.dma_dummy = true;
                        }

                    }
                }
            }
            else{
                cycles = decoder.run_one_cycle();
            }
            cpu_cycles = cycles;
            cycles *= 3;
            while (cycles-- > 0) {
                    ppu.cycle();
                if (ppu.nmi) {
                    ppu.nmi = false;
                    decoder.is.nmi();
                }
            }
            SystemCounter++;
            SystemCounter &= 0xFFFFFFFFl;


        return  cpu_cycles;
    }
    public void display_pattern_table(){
        int[][] sprite = new int[8][8];
        Random random = new Random();
        int seed = random.nextInt(0xb);
        for(int i=0 ; i< cartridge.vCHRMemory.length/16 ; i++){
            draw(sprite,i*16);
            int x = i%16, y = i/16;
            if(y>=16) y += 1;
            //+ ((i*16)/0x1000)*16 Pt Offset
            display.draw_chr_rom(sprite,x,y,seed);
        }
    }

    public void readCartridge(){
        for(int i=0 ; i + 0x8000 <= 0xffff ; i++){  //Load program data
            int size = cartridge.vPRGMemory.length;
            String s = Integer.toHexString(Byte.toUnsignedInt(cartridge.vPRGMemory[i%size]));
            cpu.cpu_memory[i + 0x8000] = cartridge.vPRGMemory[i%size];
        }

        int reset_vector = ((cpu.cpu_memory[0xfffd]<<8 & 0xff00 ) + (cpu.cpu_memory[0xfffc]&0x00ff));
        String s = Integer.toHexString(reset_vector);
        cpu.PC = (short) reset_vector;  //TODO: Uncomment THIS ONCE DONE USING NESTEST

        for(int j=0 ; j <= 0x1fff ; j++){
            int size = cartridge.vCHRMemory.length;
            if(size==0){
                break;
            }
            ppu.ppu_memory[j] = cartridge.vCHRMemory[j%size];
            ppu.patterntable[(j&0x1000)>>12][j&0xfff] = cartridge.vCHRMemory[j%size]&0xff;
        }

        ppu.display = display;

        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            int step=0;
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().toString().equals("Q")){
                    display_palette_ram();
                    display_nametable();
                    System.exit(99);
                }
            }
        });

        cpu.ppu = ppu;
        ppu.cpu = cpu;

        //TODO: for gameLoop


        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            int step=0;
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().toString().equals("SPACE")){
                    int c=1;
                    while(c-- > 0) {
                        int cycles;
                        cycles = decoder.run_one_cycle();
                        printFlags();
                        //System.out.println("The instruction took " + cycles + " to execute");
//                        cpu.ppu_registers_dump(ppu_Reg_values);
                        cycles *= 3;
                        while (cycles-- > 0) {
                            ppu.cycle();
                            if (ppu.nmi) {
                                ppu.nmi = false;
                                decoder.is.nmi();
                            }
                        }
                    }
                }
            }
        });

        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            int step=0;
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().toString().equals("L")){
                }
            }
        });
    }

    public void printFlags(){
        String paddedStatus = String.format("%8s", Integer.toBinaryString(cpu.Status & 0xff)).replace(' ', '0');
        for(int i=0 ; i<8 ; i++){
            //System.out.print(paddedStatus.charAt(i)+ " ");
        }
    }

    public void draw(int[][] sprite , int loc){
//        //System.out.println("Reading 2 bytes at " + Integer.toHexString(loc));
        for(int i=0 ; i<8 ; i++){
            int lower = ppu.ppuRead((loc+i)&0xffff) & 0xff;
            int higher = ppu.ppuRead((loc+8+i)&0xffff) & 0xff;
            String binLow = String.format("%8s", Integer.toBinaryString(lower)).replace(' ', '0');
            String binHigh = String.format("%8s", Integer.toBinaryString(higher)).replace(' ', '0');
//            //System.out.println("Str High : " + binHigh);
//            //System.out.println("Str Low : " + binLow);
            for(int j=0 ; j<8 ; j++){
                sprite[i][j] = (((binHigh.charAt(j)- '0')<<1) & 0x2) + binLow.charAt(j)-'0';
            }
        }
    }

    public void show(int[][] sprite){
        for(int i=0 ; i<8 ; i++){
            for(int j=0 ; j<8 ; j++){
                //System.out.print(sprite[i][j] + " ");
            }
            //System.out.println();
        }
    }

    public void step_draw(int i){
        int[][] sprite = new int[8][8];
        //for(int i=0 ; i< cartridge.vCHRMemory.length/16 ; i++){
            draw(sprite,i*16);
            show(sprite);
//            display.show(sprite , i );
            int x = i%16, y = i/16;
            display.draw_tile(sprite,x,y);
            //System.out.println("Block " + i + " drawn");
        //}
    }

    public void draw_nametable(){
        int[][] sprite = new int[8][8];
        for(int i=0 ; i< 0x400*4 ; i++) {
            draw(sprite , ppu.ppuRead((short) (0x2000+i)));
//            display_nametable();
            if(i<=0x400)
                display.show(sprite, i , 0 ,0);
            else if(i<=0x800)
                display.show(sprite, i , 1 , 0);
            else if(i<=0x400*3)
                display.show(sprite, i , 0 , 1);
            else
                display.show(sprite, i , 1 , 1);
//            //System.out.println("Block " + i + " drawn");
        }
    }

    public void display_nametable(){
        int c=0;
        for(int i=0 ; i< 0x400*4 ; i++) {
            if(i%0x400 == 0){
                System.out.println("\nNametable " + (i/0x400));
            }
            String s = Integer.toHexString(ppu.ppuRead( ((0x2000 + i)) )&0xff);
            System.out.print("0x" + Integer.toHexString(0x2000 + i) + " : " + s+ " ");
            c++;
            if(c==7){
                //System.out.println();/
                c=0;
            }
        }
    }

    public void display_palette_ram(){
        int c=0;
        for(int i=0; i<= 0x1f ; i++){
            String address = Integer.toHexString(0x3f00 + i);
            int value = ppu.ppuRead(0x3f00 + i);
            System.out.println(address + " : " + value);
            c++;
            if(c == 7){
                System.out.println();
                c = 0;
            }
        }
    }

    public void show_me(){
        int coarse_x = 0, coarse_y=0;
        int fine_x = 0, dine_y = 0;
        int nt_offset = 0x2000, tile_offset=0, nt_number=0;
        int[] nt_loc = {0x2000,0x2400,0x2800,0x2c00};
        int[][] tile = new int[8][8];

        for(int i=0; i<30*2 ; i++){
            for(int j=0; j<32*2 ; j++){
                int i_wrt_nametable = i%30, j_wrt_nametable = j%32;
                tile_offset = i_wrt_nametable*32 + j_wrt_nametable;
                nt_number = (((i/30)<<1)&0b10) + (j/32);
                /* *
                0 1
                2 3
                * */
                nt_offset = 0x2000 + 0x400*nt_number;
//              //System.out.print(Integer.toHexString(ppu.ppuRead((short) (nt_offset + tile_offset))&0x3ff) + " ");
//                //System.out.println("Drawing value at Pt index " + Integer.toHexString(ppu.ppuRead((nt_offset + (tile_offset&0x3ff) ))&0xff));
                draw_background(tile,ppu.ppuRead((nt_offset + (tile_offset&0x3ff) ))&0xff); //load tile
//                show(tile);
                display.draw_tile(tile,j,i);
            }
//            //System.out.println();
        }
        /* for attribute table 0
        for(int i=0; i < 8; i++){
            for(int j=0 ; j<8;j++){
                int nt_a = 0x23C0 + i*8 + j;
                //System.out.println("Value at " + Integer.toHexString(nt_a) + " is " + ppu.ppuRead(nt_a));
            }
        }
        */

    }

    private void draw_background(int[][] tile, int loc) {
        loc =  (loc<<4) + 0x1000; //bkg tiles are in pattern table 1
//        //System.out.println("Loc is " + Integer.toHexString(loc));
        for(int i=0 ; i<8 ; i++){
            int lower = ppu.ppuRead((loc+i)) & 0xff;
            int higher = ppu.ppuRead((loc+8+i)) & 0xff;
            String binLow = String.format("%8s", Integer.toBinaryString(lower)).replace(' ', '0');
            String binHigh = String.format("%8s", Integer.toBinaryString(higher)).replace(' ', '0');
            for(int j=0 ; j<8 ; j++){
                tile[i][j] = (((binHigh.charAt(j)- '0')<<1) & 0x2) + binLow.charAt(j)-'0';
            }
        }
    }

}
