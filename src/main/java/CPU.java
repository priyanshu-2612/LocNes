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

    public CPU(){
        SP = (byte) 0xFD;
        Status = 0x24;
        controller = new Controller();
        shift_register_4021 = new int[2];
        try {
            cpu_logger = new FileWriter("C:/Users/prash/Downloads/CPU_logger.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeTo(int index, byte data){
        index = index & 0xffff;
        if(index <= 0x07ff){
            //cpu memory
            System.out.println("Loading " + Integer.toHexString(Byte.toUnsignedInt(data)) + " in cpu Location 0x" + Integer.toHexString(index));
            cpu_memory[index] = data;
        }
        else if(index >= 0x2000 && index <= 0x2007){
            //These registers are also exposed to the ppu
            System.out.println("Attempting to write " + Integer.toHexString(data&0xff)+ " to 0x" + Integer.toHexString(index));
            try {
                cpu_logger.write("Writing " + Integer.toHexString(data&0xff) + " to $" + Integer.toHexString(index) + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            cpu_memory[index] = data;
            ppu.cpuWrite(index, data);
        }
        else if(index >= 0x4016 && index <= 0x4017){
            for(int i=0; i<8; i++){
                shift_register_4021[index & 0x0001] |= ((controller.controller_input[i]&0b01)<<(7-i));
            }
        }
    }

    public int getData(int index){
        index = index & 0xffff;
        if(index <=0x1fff) {
            index = (index&0x07ff);
            System.out.println("Returning " + Integer.toHexString(cpu_memory[index & 0xffff]&0xff));
            return (cpu_memory[index]&0xff);
        }
        else if(index <= 0x3fff && index >= 0x2000){
            index = 0x2000 + (index&0x0007);
            System.out.println("Attempting to read ppu register 0x" + Integer.toHexString(index));
            try {
                cpu_logger.write("Reading from $" + Integer.toHexString(index)+"\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            return ppu.cpuRead((short) ((index-0x2000)&0xffff));
            return ppu.cpuRead((short) ((index)&0xffff));
        }
        else if(index >= 0x4016 && index <= 0x4017){
            System.out.println("ATTEMPTING TO READ " + Integer.toHexString(index));
            int bit = (shift_register_4021[index&0x0001] & 0x80) != 0 ? 1 : 0;
            shift_register_4021[index&0x0001] = ((shift_register_4021[index&0x0001] << 1)&0xff);
            return bit;
        }
        else{
            System.out.println("At " + Integer.toHexString(index) + " we have " + Integer.toHexString(cpu_memory[index]&0xff));
//            throw new RuntimeException("Memory location : "+ Integer.toHexString(index) + " cannot be read");
            return cpu_memory[index]&0xff;
        }
    }

    public void push(byte val){
        SP = (byte) (SP & 0xff);
        int address = 0x0100 + Byte.toUnsignedInt(SP);
        cpu_memory[address] = val;
        SP--;
        SP = (byte) (SP & 0xFF);
        System.out.println("Value Pushed : " + Integer.toHexString(Byte.toUnsignedInt(val)) + " at 0x" + Integer.toHexString(address));
    }

    public byte pop(){
        SP = (byte)((SP & 0xff) + 1);
        int address = 0x0100 + Byte.toUnsignedInt(SP);
        return cpu_memory[address];
    }
}
