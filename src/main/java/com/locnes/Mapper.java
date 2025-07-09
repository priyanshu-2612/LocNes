package com.locnes;

public interface Mapper {

    boolean cpuMapRead(short addr);
    boolean cpuMapWrite(short addr);
    boolean ppuMapRead(short addr);
    boolean ppuMapWrite(short addr);
}
