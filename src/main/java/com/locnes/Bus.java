package com.locnes;

public class Bus {

    CPU cpu;
    PPU ppu;
    Cartridge cartridge;

    Bus(CPU cpu, PPU ppu){
        this.cpu = cpu;
        this.ppu = ppu;
    }


    public void insertCartridge(Cartridge cartridge){
        this.cartridge = cartridge;
        ppu.cartridge = cartridge;
        ppu.horizontal_mirroring = !cartridge.verticalMirroring;
    }

}
