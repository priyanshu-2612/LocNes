package com.locnes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class InstructionSetUnitTests {
    String testsDirectoryPath = "C:/Users/prash/Downloads/ProcessorTests-main/ProcessorTests-main/nes6502/v1/";
    PPU ppu = new PPU();
    CPU cpu = new CPU(ppu);
    InstructionSet instructionSet = new InstructionSet(cpu,ppu);
    Decoder decoder = new Decoder(instructionSet);
    File jsonFile;
    ObjectMapper objectMapper = new ObjectMapper();

    static Stream<String> Opcodes() {
        return DocumentedOpcodes.LEGAL_OPCODES
                .stream()
                .map(op -> String.format("%02X", op));
    }

    @ParameterizedTest(name = "Opcode 0x{0}")
    @MethodSource("Opcodes")
    void opcodeSuite(String hex) throws IOException {

        ppu.turnOnTestMode();
        cpu.turnOnTestMode();
        cpu.setPpu(ppu);

        String path;
            System.out.println(hex);
            path = testsDirectoryPath + hex + ".json";
            System.out.println("Opening " + path);
            jsonFile = new File(path);
            List<Test6502Format> testcases = objectMapper.readValue(jsonFile,
                    new TypeReference<List<Test6502Format>>() {});

            for(Test6502Format tc : testcases) {
                initialize_ram(tc.initial);
                System.out.println(tc.name);
                String[] instruction = tc.name.split(" ");
                System.out.println(Arrays.toString(instruction));

                cpu.setPC((short) tc.getInitial().pc);
                decoder.run_one_cycle();

                CpuState csFinal = tc.getFinalState();
                CpuState csMyFinal = getFinalStateAfterExecution();

                assertEquals(csFinal.getPc(), csMyFinal.getPc(), "PC should be equal");
                assertEquals(csFinal.getS(), csMyFinal.getS(), "S should be equal");
                assertEquals(csFinal.getA(), csMyFinal.getA(), "A should be equal");
                assertEquals(csFinal.getX(), csMyFinal.getX(), "X should be equal");
                assertEquals(csFinal.getY(), csMyFinal.getY(), "Y should be equal");
                assertEquals(csFinal.getP(), csMyFinal.getP(), "P should be equal");

                for(int j=0; j<csFinal.ram.size() ; j++){
                   int address = csFinal.ram.get(j).get(0);

                   int valAtTheEnd = cpu.getData(address);
                   int valThatShouldBe = csFinal.ram.get(j).get(1);

                   assertEquals(valThatShouldBe, valAtTheEnd, "At location " + address + " should be " + valThatShouldBe);
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
            cpu.writeTo(cpuState.ram.get(i).get(0), (byte)((int)cpuState.ram.get(i).get(1) & 0xff) );
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
