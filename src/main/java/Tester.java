package main.java;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

import java.util.Random;


public class Tester {
    public Display display;
    Decoder decoder;
    CPU cpu;
    PPU ppu;
    int[] game_code;
    Cartridge cartridge;
    Bus bus;
    Scene scene;

    Tester(){
        cartridge = new Cartridge("C:/Users/prash/Downloads/donkey kong.nes");
//        cartridge = new Cartridge("C:/Users/prash/Downloads/nestest.nes");
//        cartridge = new Cartridge("C:/Users/prash/Downloads/Balloon_fight.nes");
//        cartridge = new Cartridge("C:/Users/prash/Downloads/vram_access.nes");
//        cartridge = new Cartridge("C:/Users/prash/Downloads/Ice_hockey.nes");
//        cartridge = new Cartridge("C:/Users/prash/Downloads/Super_mario_brothers.nes");
        cartridge.InitCartridge();
        cpu = new CPU();
        ppu = new PPU();
        bus = new Bus(cpu,ppu);
        bus.insertCartridge(cartridge);
        decoder = new Decoder(cpu,ppu);
    }

    public int cycle(){
            int cycles, cpu_cycles;
            cycles = decoder.run_one_cycle();
            printFlags();
            //System.out.println("The instruction took " + cycles + " to execute");
//                        cpu.ppu_registers_dump(ppu_Reg_values);
            cpu_cycles = cycles;
            cycles *= 3;
//            //System.out.println("$4016 has " + (cpu.cpu_memory[0x4016]&0xff));
            while (cycles-- > 0) {
                    ppu.cycle();
                if (ppu.nmi) {
                    ppu.nmi = false;
                    //System.out.println("LALALALLALALLALAALALLALALLALALALALALLALALALALALLALALA");
                    decoder.is.nmi();
                }
            }
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

    public void runCode(){
        //System.out.println("The code will now run");
        for(int i=0 ; i + 0x8000 <= 0xffff ; i++){  //Load program data
            int size = cartridge.vPRGMemory.length;
            String s = Integer.toHexString(Byte.toUnsignedInt(cartridge.vPRGMemory[i%size]));
            cpu.cpu_memory[i + 0x8000] = cartridge.vPRGMemory[i%size];
            //System.out.println("0x"+Integer.toHexString(i+0x8000) + " : 0x" + s);
        }

        //System.out.println("Higher byte is " + Integer.toHexString(cpu.cpu_memory[0xfffd]<<8 & 0xff00));
        //System.out.println("Lower byte is " + Integer.toHexString(cpu.cpu_memory[0xfffc]&0x00ff));
        int reset_vector = ((cpu.cpu_memory[0xfffd]<<8 & 0xff00 ) + (cpu.cpu_memory[0xfffc]&0x00ff));
        String s = Integer.toHexString(reset_vector);
        //System.out.println("Setting Program Counter to 0x" + s);
        cpu.PC = (short) reset_vector;  //TODO: Uncomment THIS ONCE DONE USING NESTEST
//        cpu.PC = (short) 0xc000;
        //System.out.println("Program Counter is " + Integer.toHexString(Short.toUnsignedInt(cpu.PC)));

        //System.out.println("Now outputting CHRROM");
        for(int j=0 ; j <= 0x1fff ; j++){
            int size = cartridge.vCHRMemory.length;
            ppu.ppu_memory[j] = cartridge.vCHRMemory[j%size];
            ppu.patterntable[(j&0x1000)>>12][j&0xfff] = cartridge.vCHRMemory[j%size]&0xff; // shift by 12 shifts 3 digits in hex
            //System.out.println("At 0x" + Integer.toHexString(j) + " : " + Integer.toHexString(ppu.patterntable[(j&0x1000)>>12][j&0xfff]));
        }

        ppu.display = display;

//        decoder.run_one_cycle();
//
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            int step=0;
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().toString().equals("Q")){
                    step_draw(step);
                    if(step<cartridge.vCHRMemory.length/16)
                        step++;
                    else System.exit(0);
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
                    int c=230;
                    while(c-- > 0) {
                        int cycles;
                        cycles = decoder.run_one_cycle();
                        printFlags();
                        //System.out.println("The instruction took " + cycles + " to execute");
//                      cpu.ppu_registers_dump(ppu_Reg_values);
                        cycles *= 3;
//                        if(Integer.toHexString(Short.toUnsignedInt(cpu.PC)).equals("cdf2") ){
//                            draw_nametable();
//                            display_nametable();
//                        }
                        while (cycles-- > 0) {
                            ppu.cycle();
                            if (ppu.nmi) {
                                //System.out.println("NMI routine will follow");
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
                if(event.getCode().toString().equals("S")){
                    draw_nametable();
                      display_nametable();
//                    show_me();
                }
            }
        });

        /*int[][] sprite = new int[8][8];
        for(int i=0 ; i< cartridge.vCHRMemory.length/16 ; i++){
        draw(sprite,i*16);
        show(sprite);
        display.show(sprite , i);
        //System.out.println("Block " + i + " drawn");
        }*/


    }

    public void printFlags(){
        String paddedStatus = String.format("%8s", Integer.toBinaryString(cpu.Status & 0xff)).replace(' ', '0');
        for(int i=0 ; i<8 ; i++){
            //System.out.print(paddedStatus.charAt(i)+ " ");
        }
        //System.out.println();
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
                //System.out.println("\nNametable " + (i/0x400));
            }
            String s = Integer.toHexString(ppu.ppuRead( ((0x2000 + i)) )&0xff);
            //System.out.print("0x" + Integer.toHexString(0x2000 + i) + " : " + s+ " ");
            c++;
            if(c==7){
                //System.out.println();
                c=0;
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
//            //System.out.println("Str High : " + Integer.toHexString(higher));
//            //System.out.println("Str Low : " + Integer.toHexString(lower));
            for(int j=0 ; j<8 ; j++){
                tile[i][j] = (((binHigh.charAt(j)- '0')<<1) & 0x2) + binLow.charAt(j)-'0';
            }
        }
    }

}
