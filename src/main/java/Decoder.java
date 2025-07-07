package main.java;

import main.java.debug.DisassemblyRow;
import main.java.debug.Instruction;

import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static main.java.CPU.hex;

public class Decoder {
    FileWriter logger;
    InstructionSet is;
    CPU cpu;
    HashMap<Integer , String> opcodeMap = new HashMap<>();
    HashMap<Integer , String> addressingmodeMap = new HashMap<>();
    Map<Integer, Instruction> instrCache = new HashMap<>();

    public Decoder(CPU cpu, PPU ppu){
        initModes();
        initOpcodes();
        this.cpu = cpu;
        is = new InstructionSet(cpu, ppu);
//        try {
//            logger = new FileWriter("C:/Users/prash/Downloads/NesLog.txt.txt");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public Decoder(InstructionSet instructionSet){
        initModes();
        initOpcodes();
        this.cpu = instructionSet.cpu;
        is = instructionSet;
    }

    public String instruction_name(int n){
        if(opcodeMap.containsKey(n))
            return opcodeMap.get(n);
        else return null;
    }

    public void initModes(){
        for(int i=0; i<addressingMode.values().length ; i++)
            addressingmodeMap.put(i,addressingMode.values()[i].name());
    }
    public int run_one_cycle(){
            int instruction = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)]);
            String task = opcodeMap.get(instruction);
            int addressingType = Integer.parseInt(task.substring(3));
            Method method = null;
            byte operand;
            short operandShort;
            int clock_cycles = 0;
//            String PCvalue = Integer.toHexString((Short.toUnsignedInt(cpu.PC))&0xffff).toUpperCase()
//                    , Instr = task.substring(0,3).toUpperCase()
//                    , InstOpcode = Integer.toHexString(instruction).toUpperCase()
//                    , A = Integer.toHexString((Byte.toUnsignedInt(cpu.Accumulator))&0xff).toUpperCase()
//                    , X = Integer.toHexString((Byte.toUnsignedInt(cpu.X))&0xff).toUpperCase()
//                    , Y =Integer.toHexString((Byte.toUnsignedInt(cpu.Y))&0xff).toUpperCase()
//                    , P = Integer.toHexString((Byte.toUnsignedInt(cpu.Status))&0xff).toUpperCase()
//                    , SP = Integer.toHexString(cpu.SP&0xff).toUpperCase();
            String operandValue;
            String operandShortValue , operandShortValueHigh , operandShortValueLow;
            cpu.Status = (byte) (cpu.Status & 0xef); //clearing out B bit
            cpu.Status = (byte) (cpu.Status | 0x20); // this bit is always 1
            switch(addressingType){
                case 0:
//                operand = (byte) cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1];
                operand = (byte) cpu.getData((cpu.PC +1)&0xffff);
//                operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                if(!cpu.testMode) {
//                    try {
//                        logger.write(PCvalue + "  " + InstOpcode + " " + operandValue + " " + Instr + "                  " + "A: " + A + " X: " + X
//                                + " Y: " + Y + " P: " + P + " SP: " + SP);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
                }
                    try {
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                    } catch (SecurityException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    try {
                        clock_cycles = (int) method.invoke(is,addressingMode.Immediate,operand);
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    break;

                case 4:
//                    operand = (byte) cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1];
                    operand = (byte) cpu.getData((cpu.PC +1)&0xffff);
//                    operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                    if(!cpu.testMode){
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandValue + " " + Instr + "                 "+ "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP );
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try {
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                    } catch (SecurityException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    try {
                        clock_cycles = (int) method.invoke(is,addressingMode.PreIndirectX,operand);
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    break;

                case 5:
//                    operand = (byte) cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1];
                    operand = (byte) cpu.getData((cpu.PC +1)&0xffff);
//                    operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandValue + " " + Instr + "                 " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try {
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                    } catch (SecurityException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    try {
                        clock_cycles = (int) method.invoke(is,addressingMode.PostIndirectY,operand);
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;

                case 6:
//                    operand = (byte) cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1];
                    operand = (byte) cpu.getData((cpu.PC +1)&0xffff);
//                    operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandValue + " " + Instr + "                  " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try {
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                    } catch (SecurityException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    try {
                        clock_cycles = (int) method.invoke(is,addressingMode.ZeropageAbs,operand);
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    break;

                case 9:
//                    operand = (byte) cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1];
                    operand = (byte) cpu.getData((cpu.PC +1)&0xffff);
//                    operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandValue + " " + Instr + "                  " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try {
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                    } catch (SecurityException | NoSuchMethodException e) {}

                    try {
                        clock_cycles = (int) method.invoke(is,addressingMode.ZeropageIndexed,operand);
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    break;

                case 1:
//                    operandShort = (short) ( (cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+2]<<8) + (cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1]&0xff) );
//                    operandShort = (short) get_mirror(operandShort);
                    operandShort = (short) ( ((cpu.getData(cpu.PC+2)<<8)&0xff00) + (cpu.getData(cpu.PC+1)&0xff) );
                    if(!cpu.testMode)
                        operandShort = (short) get_mirror(operandShort);
//                    operandShortValue = Integer.toHexString((Short.toUnsignedInt(operandShort))&0xffff);
//                    operandShortValue = String.format("%4s" , operandShortValue).replace(" " , "0");
//                    operandShortValueHigh = operandShortValue.substring(0,2);
//                    operandShortValueLow = operandShortValue.substring(2);
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandShortValueHigh + " " + operandShortValueLow + " " + Instr + "              " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try{
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , short.class);
                    }
                    catch (SecurityException | NoSuchMethodException e) {}

                    try{
                        clock_cycles = (int) method.invoke(is,addressingMode.Absolute,operandShort);
                    }
                    catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                        e.printStackTrace();
                        System.exit(0);
                    }

                    break;

                case 2:
//                    operandShort = (short) ((cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+2]<<8) + (cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1] & 0xff) );
//                    operandShort = (short) get_mirror(operandShort);
                    operandShort = (short) ( ((cpu.getData(cpu.PC+2)<<8)&0xff00) + (cpu.getData(cpu.PC+1)&0xff) );
                    if(!cpu.testMode)
                        operandShort = (short) get_mirror(operandShort);
//                    operandShortValue = Integer.toHexString((Short.toUnsignedInt(operandShort))&0xffff);
//                    operandShortValue = String.format("%4s" , operandShortValue).replace(" " , "0");
//                    operandShortValueHigh = operandShortValue.substring(0,2);
//                    operandShortValueLow = operandShortValue.substring(2);
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandShortValueHigh + " " + operandShortValueLow + " " + Instr + "              " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try{
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , short.class);
                    }
                    catch (SecurityException | NoSuchMethodException e) {}

                    try{
                        clock_cycles = (int) method.invoke(is,addressingMode.AbsoluteX,operandShort);
                    }
                    catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                        e.printStackTrace();
                    }
                    break;

                case 3:
//                    operandShort = (short) ((cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+2]<<8) + (cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1] & 0xff) );
//                    operandShort = (short) get_mirror(operandShort);
                    operandShort = (short) ( ((cpu.getData(cpu.PC+2)<<8)&0xff00) + (cpu.getData(cpu.PC+1)&0xff) );
                    if(!cpu.testMode)
                        operandShort = (short) get_mirror(operandShort);
//                    operandShortValue = Integer.toHexString((Short.toUnsignedInt(operandShort))&0xffff);
//                    operandShortValue = String.format("%4s" , operandShortValue).replace(" " , "0");
//                    operandShortValueHigh = operandShortValue.substring(0,2);
//                    operandShortValueLow = operandShortValue.substring(2);
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandShortValueHigh + " " + operandShortValueLow + " " + Instr + "              " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try{
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , short.class);
                    }
                    catch (SecurityException | NoSuchMethodException e) {}

                    try{
                        clock_cycles = (int) method.invoke(is,addressingMode.AbsoluteY,operandShort);
                    }
                    catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                        e.printStackTrace();
                        System.exit(0);
                    }

                    break;

                case 8:
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + Instr + "                    " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try{
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class);
                    }
                    catch(SecurityException | NoSuchMethodException e){}

                    try{
                        //System.out.println("Calling method " + task.substring(0,3));
                        assert method != null;
                        clock_cycles = (int) method.invoke(is,addressingMode.Implied);
                    }
                    catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                        e.printStackTrace();
                    }

                    break;

                case 10:
//                    operand = (byte) cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1];
                    operand = (byte) cpu.getData((cpu.PC +1)&0xffff);
//                    operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xffff);
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandValue + " " + Instr + "                 " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try {
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                    } catch (SecurityException | NoSuchMethodException e) {}

                    try {
                        clock_cycles = (int) method.invoke(is,addressingMode.Relative,operand);
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    break;

                case 7:
//                    operandShort = (short) ( (cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+2]<<8) + (cpu.cpu_memory[Short.toUnsignedInt(cpu.PC)+1]&0xff) );
//                    operandShort = (short) get_mirror(operandShort);
                    operandShort = (short) ( ((cpu.getData(cpu.PC+2)<<8)&0xff00) + (cpu.getData(cpu.PC+1)&0xff) );
                    if(!cpu.testMode)
                        operandShort = (short) get_mirror(operandShort);
//                    operandShortValue = Integer.toHexString((Short.toUnsignedInt(operandShort))&0xffff);
//                    operandShortValue = String.format("%4s" , operandShortValue).replace(" " , "0");
//                    operandShortValueHigh = operandShortValue.substring(0,2);
//                    operandShortValueLow = operandShortValue.substring(2);
                    if(!cpu.testMode) {
//                        try {
//                            logger.write(PCvalue + "  " + InstOpcode + " " + operandShortValueHigh + " " + operandShortValueLow + " " + Instr + "              " + "A: " + A + " X: " + X
//                                    + " Y: " + Y + " P: " + P + " SP: " + SP);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                    try{
                        method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , short.class);
                    }
                    catch (SecurityException | NoSuchMethodException e) {}

                    try{
                        clock_cycles = (int) method.invoke(is,addressingMode.Indirect, operandShort);
                    }
                    catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                        e.printStackTrace();
                    }

                    break;

                default:
                    //System.out.println("NO SUCH INSTRUCTION FOUND.. OPCODE : " + instruction + " " + task);
                    throw new RuntimeException();
            }
        if(!cpu.testMode) {
//            try {
//                logger.write("\n");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }
        cpu.setPC((short) (cpu.getPC()&0xffff));
        return clock_cycles;
    }

    public int run_one_cpu_cycle(String[] inst){
        int instruction = Integer.parseInt(inst[0], 16);
        String task = opcodeMap.get(instruction);

//        System.out.println("Executing " + task + " pc: " + Integer.toHexString((Short.toUnsignedInt(cpu.PC))&0xffff) );
        int addressingType = Integer.parseInt(task.substring(3));
        Method method = null;
        byte operand;
        short operandShort;
        int clock_cycles = 0;
        String operandValue;
        String operandShortValue , operandShortValueHigh , operandShortValueLow;
        cpu.Status = (byte) (cpu.Status & 0xef); //clearing out B bit
        cpu.Status = (byte) (cpu.Status | 0x20); // this bit is always 1
        switch(addressingType){
            case 0:
                operand = (byte) (Integer.parseInt(inst[1], 16)&0xff);
                operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                try {
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                } catch (SecurityException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

                try {
                    clock_cycles = (int) method.invoke(is,addressingMode.Immediate,operand);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;

            case 4:
                operand = (byte) (Integer.parseInt(inst[1],16)&0xff);
                operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                try {
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                } catch (SecurityException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

                try {
                    clock_cycles = (int) method.invoke(is,addressingMode.PreIndirectX,operand);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;

            case 5:
                operand = (byte) (Integer.parseInt(inst[1],16)&0xff);
                operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                try {
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                } catch (SecurityException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

                try {
                    clock_cycles = (int) method.invoke(is,addressingMode.PostIndirectY,operand);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;

            case 6:
                operand = (byte) (Integer.parseInt(inst[1],16)&0xff);
                operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                try {
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                } catch (SecurityException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

                try {
                    clock_cycles = (int) method.invoke(is,addressingMode.ZeropageAbs,operand);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;

            case 9:
                operand = (byte) (Integer.parseInt(inst[1],16)&0xff);
                operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xff);
                try {
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                } catch (SecurityException | NoSuchMethodException e) {}

                try {
                    clock_cycles = (int) method.invoke(is,addressingMode.ZeropageIndexed,operand);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;

            case 1:
                operandShort = (short) ((Integer.parseInt(inst[1],16)<<8) + (Integer.parseInt(inst[2],16)));
                operandShort = (short) get_mirror(operandShort);
                operandShortValue = Integer.toHexString((Short.toUnsignedInt(operandShort))&0xffff);
                operandShortValue = String.format("%4s" , operandShortValue).replace(" " , "0");
                operandShortValueHigh = operandShortValue.substring(0,2);
                operandShortValueLow = operandShortValue.substring(2);
                try{
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , short.class);
                }
                catch (SecurityException | NoSuchMethodException e) {}

                try{
                    clock_cycles = (int) method.invoke(is,addressingMode.Absolute,operandShort);
                }
                catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                    e.printStackTrace();
                }

                break;

            case 2:
                operandShort = (short) ((Integer.parseInt(inst[1],16)<<8) + (Integer.parseInt(inst[2],16)));
                operandShort = (short) get_mirror(operandShort);
                operandShortValue = Integer.toHexString((Short.toUnsignedInt(operandShort))&0xffff);
                operandShortValue = String.format("%4s" , operandShortValue).replace(" " , "0");
                operandShortValueHigh = operandShortValue.substring(0,2);
                operandShortValueLow = operandShortValue.substring(2);
                try{
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , short.class);
                }
                catch (SecurityException | NoSuchMethodException e) {}

                try{
                    clock_cycles = (int) method.invoke(is,addressingMode.AbsoluteX,operandShort);
                }
                catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                    e.printStackTrace();
                }
                break;

            case 3:
                operandShort = (short) ((Integer.parseInt(inst[1],16)<<8) + (Integer.parseInt(inst[2],16)));
                operandShort = (short) get_mirror(operandShort);
                operandShortValue = Integer.toHexString((Short.toUnsignedInt(operandShort))&0xffff);
                operandShortValue = String.format("%4s" , operandShortValue).replace(" " , "0");
                operandShortValueHigh = operandShortValue.substring(0,2);
                operandShortValueLow = operandShortValue.substring(2);
                try{
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , short.class);
                }
                catch (SecurityException | NoSuchMethodException e) {}

                try{
                    clock_cycles = (int) method.invoke(is,addressingMode.AbsoluteY,operandShort);
                }
                catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                    e.printStackTrace();
                    System.exit(0);
                }

                break;

            case 8:
                try{
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class);
                }
                catch(SecurityException | NoSuchMethodException e){}

                try{
                    //System.out.println("Calling method " + task.substring(0,3));
                    assert method != null;
                    clock_cycles = (int) method.invoke(is,addressingMode.Implied);
                }
                catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                    e.printStackTrace();
                }

                break;

            case 10:
                operand = (byte) (Integer.parseInt(inst[1],16)&0xff);
                operandValue = Integer.toHexString((Byte.toUnsignedInt(operand))&0xffff);
                try {
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , byte.class);
                } catch (SecurityException | NoSuchMethodException e) {}

                try {
                    clock_cycles = (int) method.invoke(is,addressingMode.Relative,operand);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;

            case 7:
                operandShort = (short) ((Integer.parseInt(inst[1],16)<<8) + (Integer.parseInt(inst[2],16)));
                operandShort = (short) get_mirror(operandShort);
                operandShortValue = Integer.toHexString((Short.toUnsignedInt(operandShort))&0xffff);
                operandShortValue = String.format("%4s" , operandShortValue).replace(" " , "0");
                operandShortValueHigh = operandShortValue.substring(0,2);
                operandShortValueLow = operandShortValue.substring(2);
                try{
                    method = is.getClass().getMethod(task.substring(0,3) , addressingMode.class , short.class);
                }
                catch (SecurityException | NoSuchMethodException e) {}

                try{
                    clock_cycles = (int) method.invoke(is,addressingMode.Indirect, operandShort);
                }
                catch(IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                    e.printStackTrace();
                }

                break;

            default:
                //System.out.println("NO SUCH INSTRUCTION FOUND.. OPCODE : " + instruction + " " + task);
                throw new RuntimeException();
        }
        return clock_cycles;
    }

    public int get_mirror(short addr){
        int val = Short.toUnsignedInt(addr);
        if(val>=0x800 && val <= 0x1fff)
            val %= 0x800;
        else if(val>=0x2008 && val<=0x3fff)
            val = (val-0x2000)%0x8 + 0x2000;

        return val;
    }

    public List<DisassemblyRow> disassemble(int count) {
        List<DisassemblyRow> rows = new ArrayList<>();
        int pc = cpu.getPC();
        int ptr = pc;

        for (int i = 0; i < count; i++) {
            int op = cpu.getData(ptr) & 0xFF;
            Instruction ins = decode(op);
            if (ins == null) {
                rows.add(newRow(ptr, op, 1, "???"));
                ptr += 1;
                continue;
            }

            int size = ins.size();
            String bytes = hex(op, 2);
            for (int j = 1; j < size; j++) {
                bytes += " " + hex(cpu.getData(ptr + j), 2);
            }

            String operand = formatOperand(ins.mode(), ptr + 1);
            rows.add(new DisassemblyRow(hex(ptr, 4), bytes, ins.name() + " " + operand.trim()));
            ptr += size;
        }

        return rows;
    }

    private DisassemblyRow newRow(int addr, int op, int size, String line) {
        StringBuilder b = new StringBuilder(hex(op, 2));
        for (int j = 1; j < size; j++) {
            b.append(" ").append(hex(cpu.getData(addr + j), 2));
        }
        return new DisassemblyRow(hex(addr, 4), b.toString(), line.trim());
    }


    private String formatOperand(String mode, int addr) {
        int lo = cpu.getData(addr) & 0xFF;
        int hi = cpu.getData(addr + 1) & 0xFF;
        int word = (hi << 8) | lo;

        return switch (mode) {
            case "IMM" -> "#$" + hex(lo, 2);
            case "ZP"  -> "$" + hex(lo, 2);
            case "ZPX" -> "$" + hex(lo, 2) + ",X";
            case "ZPY" -> "$" + hex(lo, 2) + ",Y";
            case "ABS" -> "$" + hex(word, 4);
            case "ABX" -> "$" + hex(word, 4) + ",X";
            case "ABY" -> "$" + hex(word, 4) + ",Y";
            case "IND" -> "($" + hex(word, 4) + ")";
            case "IDX" -> "($" + hex(lo, 2) + ",X)";
            case "IDY" -> "($" + hex(lo, 2) + "),Y";
            case "REL" -> {
                int offset = (byte) lo; // sign-extend
                int target = (addr + 1 + offset) & 0xFFFF;
                yield "$" + hex(target, 4);
            }
            default -> "";
        };
    }

    private Instruction decode(int opcode) {
        return instrCache.computeIfAbsent(opcode, op -> {
            String tag = opcodeMap.get(op);
            if (tag == null) return null;           // unknown / unimplemented

            // split "lda0" → mnemonic="lda", idx=0
            int split = tag.length() - 1;
            while (split > 0 && Character.isDigit(tag.charAt(split-1))) split--;
            String mnemonic = tag.substring(0, split).toUpperCase();   // LDA, TAX …
            int idx         = Integer.parseInt(tag.substring(split));  // 0,6,9…

            return switch (idx) {          // idx → mode / size
                case 0  -> new Instruction(mnemonic, "IMM", 2);   // #$nn
                case 1  -> new Instruction(mnemonic, "ABS", 3);   // $nnnn
                case 2  -> new Instruction(mnemonic, "ABX", 3);   // $nnnn,X
                case 3  -> new Instruction(mnemonic, "ABY", 3);   // $nnnn,Y
                case 4  -> new Instruction(mnemonic, "IDX", 2);   // ($nn,X)
                case 5  -> new Instruction(mnemonic, "IDY", 2);   // ($nn),Y
                case 6  -> new Instruction(mnemonic, "ZP",  2);   // $nn
                case 8  -> new Instruction(mnemonic, "IMP", 1);   // implied / accumulator
                case 9  -> new Instruction(mnemonic, "ZPX", 2);   // $nn,X
                case 10 -> new Instruction(mnemonic, "REL",  2);
                default -> null;              // add more if you invent new codes
            };
        });
    }

    public void initOpcodes(){

        /*
        Immediate - 0
        Absolute - 1
        AbsoluteX - 2
        AbsoluteY - 3
        PreIndirectX - 4
        PostIndirectY - 5
        ZeropageAbs - 6
        Indirect - 7
        Implied - 8
        ZeropageIndexed - 9
        Relative - 10

        * */
        // LDA (Load Accumulator)
        opcodeMap.put(0xA9, "lda0"); // Immediate
        opcodeMap.put(0xA5, "lda6"); // ZeropageAbs
        opcodeMap.put(0xB5, "lda9"); // ZeropageIndexed
        opcodeMap.put(0xAD, "lda1"); // Absolute
        opcodeMap.put(0xBD, "lda2"); // AbsoluteX
        opcodeMap.put(0xB9, "lda3"); // AbsoluteY
        opcodeMap.put(0xA1, "lda4"); // PreIndirectX
        opcodeMap.put(0xB1, "lda5"); // PostIndirectY

        // TAX (Transfer Accumulator to X)
        opcodeMap.put(0xAA, "tax8"); // Implied

        // PHA (Push Accumulator)
        opcodeMap.put(0x48, "pha8"); // Implied

        // ASL (Arithmetic Shift Left)
        opcodeMap.put(0x0A, "asl8"); // Accumulator
        opcodeMap.put(0x06, "asl6"); // ZeropageAbs
        opcodeMap.put(0x16, "asl9"); // ZeropageIndexed
        opcodeMap.put(0x0E, "asl1"); // Absolute
        opcodeMap.put(0x1E, "asl2"); // AbsoluteX

        // AND (Logical AND)
        opcodeMap.put(0x29, "and0"); // Immediate
        opcodeMap.put(0x25, "and6"); // ZeropageAbs
        opcodeMap.put(0x35, "and9"); // ZeropageIndexed
        opcodeMap.put(0x2D, "and1"); // Absolute
        opcodeMap.put(0x3D, "and2"); // AbsoluteX
        opcodeMap.put(0x39, "and3"); // AbsoluteY
        opcodeMap.put(0x21, "and4"); // PreIndirectX
        opcodeMap.put(0x31, "and5"); // PostIndirectY

        // ADC (Add with Carry)
        opcodeMap.put(0x69, "adc0"); // Immediate
        opcodeMap.put(0x65, "adc6"); // ZeropageAbs
        opcodeMap.put(0x75, "adc9"); // ZeropageIndexed
        opcodeMap.put(0x6D, "adc1"); // Absolute
        opcodeMap.put(0x7D, "adc2"); // AbsoluteX
        opcodeMap.put(0x79, "adc3"); // AbsoluteY
        opcodeMap.put(0x61, "adc4"); // PreIndirectX
        opcodeMap.put(0x71, "adc5"); // PostIndirectY

        // DEC (Decrement cpu)
        opcodeMap.put(0xC6, "dec6"); // ZeropageAbs
        opcodeMap.put(0xD6, "dec9"); // ZeropageIndexed
        opcodeMap.put(0xCE, "dec1"); // Absolute
        opcodeMap.put(0xDE, "dec2"); // AbsoluteX

        // BRK (Force Interrupt)
        opcodeMap.put(0x00, "brk8"); // Implied

        // BCC (Branch if Carry Clear)
        opcodeMap.put(0x90, "bcc10"); // Relative

        // CLC (Clear Carry Flag)
        opcodeMap.put(0x18, "clc8"); // Implied

        // NOP (No Operation)
        opcodeMap.put(0xEA, "nop8"); // Implied

        // LDX (Load X Register)
        opcodeMap.put(0xA2, "ldx0"); // Immediate
        opcodeMap.put(0xA6, "ldx6"); // ZeropageAbs
        opcodeMap.put(0xB6, "ldx9"); // ZeropageIndexedY
        opcodeMap.put(0xAE, "ldx1"); // Absolute
        opcodeMap.put(0xBE, "ldx3"); // AbsoluteY

        // TAY (Transfer Accumulator to Y)
        opcodeMap.put(0xA8, "tay8"); // Implied

        // PHP (Push Processor Status)
        opcodeMap.put(0x08, "php8"); // Implied

        // LSR (Logical Shift Right)
        opcodeMap.put(0x4A, "lsr8"); // Accumulator
        opcodeMap.put(0x46, "lsr6"); // ZeropageAbs
        opcodeMap.put(0x56, "lsr9"); // ZeropageIndexed
        opcodeMap.put(0x4E, "lsr1"); // Absolute
        opcodeMap.put(0x5E, "lsr2"); // AbsoluteX

        // BIT (Test Bits in cpu with Accumulator)
        opcodeMap.put(0x24, "bit6"); // ZeropageAbs
        opcodeMap.put(0x2C, "bit1"); // Absolute

        // CMP (Compare Accumulator)
        opcodeMap.put(0xC9, "cmp0"); // Immediate
        opcodeMap.put(0xC5, "cmp6"); // ZeropageAbs
        opcodeMap.put(0xD5, "cmp9"); // ZeropageIndexed
        opcodeMap.put(0xCD, "cmp1"); // Absolute
        opcodeMap.put(0xDD, "cmp2"); // AbsoluteX
        opcodeMap.put(0xD9, "cmp3"); // AbsoluteY
        opcodeMap.put(0xC1, "cmp4"); // PreIndirectX
        opcodeMap.put(0xD1, "cmp5"); // PostIndirectY

        // DEX (Decrement X Register)
        opcodeMap.put(0xCA, "dex8"); // Implied

        // JMP (Jump)
        opcodeMap.put(0x4C, "jmp1"); // Absolute
        opcodeMap.put(0x6C, "jmp7"); // Indirect

        // BCS (Branch if Carry Set)
        opcodeMap.put(0xB0, "bcs10"); // Relative

        // CLD (Clear Decimal Mode)
        opcodeMap.put(0xD8, "cld8"); // Implied

        // LDY (Load Y Register)
        opcodeMap.put(0xA0, "ldy0"); // Immediate
        opcodeMap.put(0xA4, "ldy6"); // ZeropageAbs
        opcodeMap.put(0xB4, "ldy9"); // ZeropageIndexedX
        opcodeMap.put(0xAC, "ldy1"); // Absolute
        opcodeMap.put(0xBC, "ldy2"); // AbsoluteX

        // TSX (Transfer Stack Pointer to X)
        opcodeMap.put(0xBA, "tsx8"); // Implied

        // PLA (Pull Accumulator)
        opcodeMap.put(0x68, "pla8"); // Implied

        // ROL (Rotate Left)
        opcodeMap.put(0x2A, "rol8"); // Accumulator
        opcodeMap.put(0x26, "rol6"); // ZeropageAbs
        opcodeMap.put(0x36, "rol9"); // ZeropageIndexed
        opcodeMap.put(0x2E, "rol1"); // Absolute
        opcodeMap.put(0x3E, "rol2"); // AbsoluteX

        // EOR (Exclusive OR)
        opcodeMap.put(0x49, "eor0"); // Immediate
        opcodeMap.put(0x45, "eor6"); // ZeropageAbs
        opcodeMap.put(0x55, "eor9"); // ZeropageIndexed
        opcodeMap.put(0x4D, "eor1"); // Absolute
        opcodeMap.put(0x5D, "eor2"); // AbsoluteX
        opcodeMap.put(0x59, "eor3"); // AbsoluteY
        opcodeMap.put(0x41, "eor4"); // PreIndirectX
        opcodeMap.put(0x51, "eor5"); // PostIndirectY

        // CPX (Compare X Register)
        opcodeMap.put(0xE0, "cpx0"); // Immediate
        opcodeMap.put(0xE4, "cpx6"); // ZeropageAbs
        opcodeMap.put(0xEC, "cpx1"); // Absolute

        // DEY (Decrement Y Register)
        opcodeMap.put(0x88, "dey8"); // Implied

        // JSR (Jump to Subroutine)
        opcodeMap.put(0x20, "jsr1"); // Absolute

        // BEQ (Branch if Equal)
        opcodeMap.put(0xF0, "beq10"); // Relative

        // CLI (Clear Interrupt Disable)
        opcodeMap.put(0x58, "cli8"); // Implied

        // STA (Store Accumulator)
        opcodeMap.put(0x85, "sta6"); // ZeropageAbs
        opcodeMap.put(0x95, "sta9"); // ZeropageIndexed
        opcodeMap.put(0x8D, "sta1"); // Absolute
        opcodeMap.put(0x9D, "sta2"); // AbsoluteX
        opcodeMap.put(0x99, "sta3"); // AbsoluteY
        opcodeMap.put(0x81, "sta4"); // PreIndirectX
        opcodeMap.put(0x91, "sta5"); // PostIndirectY

        // TXA (Transfer X to Accumulator)
        opcodeMap.put(0x8A, "txa8"); // Implied

        // PLP (Pull Processor Status)
        opcodeMap.put(0x28, "plp8"); // Implied

        // ROR (Rotate Right)
        opcodeMap.put(0x6A, "ror8"); // Accumulator
        opcodeMap.put(0x66, "ror6"); // ZeropageAbs
        opcodeMap.put(0x76, "ror9"); // ZeropageIndexed
        opcodeMap.put(0x6E, "ror1"); // Absolute
        opcodeMap.put(0x7E, "ror2"); // AbsoluteX

        // ORA (Logical Inclusive OR)
        opcodeMap.put(0x09, "ora0"); // Immediate
        opcodeMap.put(0x05, "ora6"); // ZeropageAbs
        opcodeMap.put(0x15, "ora9"); // ZeropageIndexed
        opcodeMap.put(0x0D, "ora1"); // Absolute
        opcodeMap.put(0x1D, "ora2"); // AbsoluteX
        opcodeMap.put(0x19, "ora3"); // AbsoluteY
        opcodeMap.put(0x01, "ora4"); // PreIndirectX
        opcodeMap.put(0x11, "ora5"); // PostIndirectY

        // CPY (Compare Y Register)
        opcodeMap.put(0xC0, "cpy0"); // Immediate
        opcodeMap.put(0xC4, "cpy6"); // ZeropageAbs
        opcodeMap.put(0xCC, "cpy1"); // Absolute

        // INC (Increment cpu)
        opcodeMap.put(0xE6, "inc6"); // ZeropageAbs
        opcodeMap.put(0xF6, "inc9"); // ZeropageIndexed
        opcodeMap.put(0xEE, "inc1"); // Absolute
        opcodeMap.put(0xFE, "inc2"); // AbsoluteX

        // RTI (Return from Interrupt)
        opcodeMap.put(0x40, "rti8"); // Implied

        // BMI (Branch if Minus)
        opcodeMap.put(0x30, "bmi10"); // Relative

        // CLV (Clear Overflow Flag)
        opcodeMap.put(0xB8, "clv8"); // Implied

        // STX (Store X Register)
        opcodeMap.put(0x86, "stx6"); // ZeropageAbs
        opcodeMap.put(0x96, "stx9"); // ZeropageIndexedY
        opcodeMap.put(0x8E, "stx1"); // Absolute

        // TXS (Transfer X to Stack Pointer)
        opcodeMap.put(0x9A, "txs8"); // Implied

        // SBC (Subtract with Carry)
        opcodeMap.put(0xE9, "sbc0"); // Immediate
        opcodeMap.put(0xE5, "sbc6"); // ZeropageAbs
        opcodeMap.put(0xF5, "sbc9"); // ZeropageIndexed
        opcodeMap.put(0xED, "sbc1"); // Absolute
        opcodeMap.put(0xFD, "sbc2"); // AbsoluteX
        opcodeMap.put(0xF9, "sbc3"); // AbsoluteY
        opcodeMap.put(0xE1, "sbc4"); // PreIndirectX
        opcodeMap.put(0xF1, "sbc5"); // PostIndirectY

        // INX (Increment X Register)
        opcodeMap.put(0xE8, "inx8"); // Implied

        // RTS (Return from Subroutine)
        opcodeMap.put(0x60, "rts8"); // Implied

        // BNE (Branch if Not Equal)
        opcodeMap.put(0xD0, "bne10"); // Relative

        // SEC (Set Carry Flag)
        opcodeMap.put(0x38, "sec8"); // Implied

        // STY (Store Y Register)
        opcodeMap.put(0x84, "sty6"); // ZeropageAbs
        opcodeMap.put(0x94, "sty9"); // ZeropageIndexed
        opcodeMap.put(0x8C, "sty1"); // Absolute

        // TYA (Transfer Y to Accumulator)
        opcodeMap.put(0x98, "tya8"); // Implied

        // INY (Increment Y Register)
        opcodeMap.put(0xC8, "iny8"); // Implied

        // BPL (Branch if Positive)
        opcodeMap.put(0x10, "bpl10"); // Relative

        // SED (Set Decimal Flag)
        opcodeMap.put(0xF8, "sed8"); // Implied

        // BVC (Branch if Overflow Clear)
        opcodeMap.put(0x50, "bvc10"); // Relative

        // SEI (Set Interrupt Disable)
        opcodeMap.put(0x78, "sei8"); // Implied

        // BVS (Branch if Overflow Set)
        opcodeMap.put(0x70, "bvs10"); // Relative
    }
}
