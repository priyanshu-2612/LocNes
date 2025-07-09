package com.locnes.debug;

public class DisassemblyRow {
    private final String addr;
    private final String bytes;
    private final String instr;

    public DisassemblyRow(String addr, String bytes, String instr) {
        this.addr = addr;
        this.bytes = bytes;
        this.instr = instr;
    }

    public String getAddr() {
        return addr;
    }

    public String getBytes() {
        return bytes;
    }

    public String getInstr() {
        return instr;
    }
}
