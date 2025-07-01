package main.java;


import java.io.FileWriter;
import java.io.IOException;

public class CPU {
    PPU ppu;
    FileWriter cpu_logger;
    byte Accumulator;
    byte X;
    byte Y;
    byte Status=0; // NV1B DIZC
    byte SP = (byte) 0xFF;

    short PC;
    byte[] stack = new byte[256];
    byte[] cpu_memory = new byte[65536]; // 2 KB
    Controller controller;
    int[] shift_register_4021; // 1 byte each
    boolean testMode = false;

    public CPU(){
        SP = (byte) 0xFD;
        Status = 0x24;
        controller = new Controller();
        shift_register_4021 = new int[2];
//        try {
//            cpu_logger = new FileWriter("C:/Users/prash/Downloads/CPU_logger.txt");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void writeTo(int index, byte data){
        index = index & 0xffff;
        if(testMode){
            cpu_memory[index] = data;
            return;
        }
        if(index <= 0x1fff){
            cpu_memory[index&0x07ff] = data;
        }
        else if(index >= 0x2000 && index <= 0x3fff){
            index = 0x2000 + (index&0x0007);
            ppu.cpuWrite(index, data);
        }
        else if(index >= 0x4016 && index <= 0x4017){
            shift_register_4021[index & 0x0001] = (controller.controller_input[index & 0x0001]);
        }
        else{
            cpu_memory[index] = data; // for single tests
        }
    }

    public int getData(int index){
        index = index & 0xffff;
        if(testMode){
            return cpu_memory[index]&0xff;
        }
        if(index <=0x1fff) {
            index = (index&0x07ff);
            return (cpu_memory[index]&0xff);
        }
        else if(index <= 0x3fff && index >= 0x2000){
            index = 0x2000 + (index&0x0007);
            return ppu.cpuRead((short) ((index)&0xffff))&0xff;
        }
        else if(index >= 0x4016 && index <= 0x4017){
            int bit = (shift_register_4021[index&0x0001] & 0x80) != 0 ? 1 : 0;
            shift_register_4021[index&0x0001] = ((shift_register_4021[index&0x0001] << 1)&0xff);
            return bit;
        }
        else{
            return cpu_memory[index]&0xff;
        }
    }

    public void push(byte val){
        SP = (byte) (SP & 0xff);
        int address = 0x0100 + (SP&0xff);
        cpu_memory[address] = val;
        SP--;
        SP = (byte) (SP & 0xFF);
        //System.out.println("Value Pushed : " + Integer.toHexString(Byte.toUnsignedInt(val)) + " at 0x" + Integer.toHexString(address));
    }

    public byte pop(){
        SP = (byte)((SP & 0xff) + 1);
        int address = 0x0100 + (SP&0xff);
        return cpu_memory[address];
    }

    public byte getX() {
        return X;
    }

    public void setX(byte x) {
        X = x;
    }

    public byte getY() {
        return Y;
    }

    public void setY(byte y) {
        Y = y;
    }

    public byte getStatus() {
        return Status;
    }

    public void setStatus(byte status) {
        Status = status;
    }

    public byte getSP() {
        return SP;
    }

    public void setSP(byte SP) {
        this.SP = SP;
    }

    public short getPC() {
        return PC;
    }

    public void setPC(short PC) {
        this.PC = PC;
    }

    public byte getAccumulator() {
        return Accumulator;
    }

    public void setAccumulator(byte accumulator) {
        Accumulator = accumulator;
    }

    public PPU getPpu() {
        return ppu;
    }

    public void setPpu(PPU ppu) {
        this.ppu = ppu;
    }

    public void turnOnTestMode(){
        testMode = true;
    }

    public void turnOffTestMode(){
        testMode = false;
    }
}
