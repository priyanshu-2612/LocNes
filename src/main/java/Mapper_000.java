package main.java;

public class Mapper_000 implements Mapper {

    int prgBanks=0 , chrBanks=0;

    Mapper_000(int prgBanks , int chrBanks){
        this.prgBanks = prgBanks;
        this.chrBanks = chrBanks;
    }
    @Override
    public boolean cpuMapRead(short addr) {
        int addr_value = Short.toUnsignedInt(addr);
        if(addr_value >= 0x8000 && addr_value <= 0xffff){

            int mapped_addr = addr & (prgBanks>1 ? 0x7fff : 0x3fff);
            return true;
        }

        return false;
    }

    public short getMappedAddr(short addr){
        return (short) (addr & (prgBanks>1 ? 0x7fff : 0x3fff));
    }

    @Override
    public boolean cpuMapWrite(short addr) {
        int addr_value = Short.toUnsignedInt(addr);
        if(addr_value >= 0x8000 && addr_value <= 0xffff){
            return true;
        }

        return false;
    }

    @Override
    public boolean ppuMapRead(short addr) {
        int addr_value = Short.toUnsignedInt(addr);
        if(addr_value >= 0x8000 && addr_value <= 0x1fff){
            int mapped_addr = addr;
            return true;
        }

        return false;
    }

    @Override
    public boolean ppuMapWrite(short addr) {
        return false;
    }
}
