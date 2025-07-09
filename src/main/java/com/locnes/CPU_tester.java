package com.locnes;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CPU_tester {
    CPU cpu;
    Scene scene;
    Decoder decoder;

    public CPU_tester(Scene scene) {
        this.scene = scene;
        PPU ppu = new PPU();
        cpu = new CPU(ppu);
        decoder = new Decoder(cpu , ppu);
    }

    public void test() {
        byte[] file;
        try {
            file = Files.readAllBytes(Path.of("C:/Users/prash/Downloads/nestest.nes"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(int i=0 ; i+0x8000 < 0xffff ; i++){
            cpu.cpu_memory[0x8000+i] = file[i];
        }

        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            int step=0;
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().toString().equals("SPACE")){
                    decoder.run_one_cycle();
                }
            }
        });
    }
}
