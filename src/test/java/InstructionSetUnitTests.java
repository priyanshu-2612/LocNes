package test.java;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.Assert.*;
import main.java.CPU;
import main.java.Decoder;
import main.java.InstructionSet;
import main.java.PPU;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InstructionSetUnitTests {

    CPU cpu = new CPU();
    PPU ppu = new PPU();
    InstructionSet instructionSet = new InstructionSet(cpu,ppu);
    Decoder decoder = new Decoder(instructionSet);
    File jsonFile;
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void test() throws IOException {

        cpu.setPpu(ppu);

        int counter=0;
        String hex, path;
        while(counter <= 0xff){
            //String hex = String.format("%02X", value); also works
            hex = Integer.toHexString(counter);
            hex = String.format("%2s", hex).replace(" ", "0");
            System.out.println(hex);
            path = "C:/Users/prash/Downloads/ProcessorTests-main/ProcessorTests-main/nes6502/v1/" + hex + ".json";
            System.out.println("Opening " + path);
            jsonFile = new File(path);
            List<Test6502Format> testcases = objectMapper.readValue(jsonFile,
                    new TypeReference<List<Test6502Format>>() {});

            for(Test6502Format tc : testcases) {
                initialize_ram(tc.initial);
                System.out.println(tc.name);
                String[] instruction = tc.name.split(" ");
                System.out.println(Arrays.toString(instruction));

                decoder.run_one_cpu_cycle(instruction);

                CpuState csFinal = tc.getFinalState();
                CpuState csMyFinal = getFinalStateAfterExecution();

                assertEquals("PC should be equal", csFinal.getPc(), csMyFinal.getPc());
                assertEquals("S should be equal", csFinal.getS(), csMyFinal.getS());
                assertEquals("A should be equal", csFinal.getA(), csMyFinal.getA());
                assertEquals("X should be equal", csFinal.getX(), csMyFinal.getX());
                assertEquals("Y should be equal", csFinal.getY(), csMyFinal.getY());
//                assertEquals("P should be equal", csFinal.getP(), csMyFinal.getP());

                for(int j=0; j<csFinal.ram.size() ; j++){
                   int address = csFinal.ram.get(j).get(0);
                   int value = csFinal.ram.get(j).get(1);

                   int valAtTheEnd = cpu.getData(address);
                   int valThatShouldBe = csFinal.ram.get(j).get(1);

                   assertEquals("At location " + address + " should be " + value, valAtTheEnd, valThatShouldBe );
                }
            }
        }
    }

    public void initialize_ram(CpuState cpuState){
        cpu.setPC((short) cpuState.pc);
        System.out.println("Set PC to " + cpuState.pc);
        cpu.setX((byte) cpuState.x);
        cpu.setY((byte) cpuState.y);
        cpu.setAccumulator((byte) cpuState.a);
        cpu.setSP((byte) cpuState.s);
        cpu.setStatus((byte) cpuState.p);
        for(int i=0; i<cpuState.ram.size() ; i++){
//            cpu.writeTo(cpuState.ram.get(i).get(0), (byte)((int)cpuState.ram.get(i).get(1) & 0xff) );
            System.out.println("WROTE " + ((int)cpuState.ram.get(i).get(1) & 0xff) + " at " + cpuState.ram.get(i).get(0));
            System.out.println("VALUE READ IS " + cpu.getData(cpuState.ram.get(i).get(0)));
        }
    }

    public CpuState getFinalStateAfterExecution(){
        CpuState cs = new CpuState();
        cs.setPc(cpu.getPC()&0xffff);
        cs.setX(cpu.getX()&0xff);
        cs.setY(cpu.getY() & 0xff);
        cs.setA(cpu.getAccumulator() & 0xff);
        cs.setS(cpu.getSP() & 0xff);
        cs.setP(cpu.getStatus() & 0xff);

        return cs;
    }
}
