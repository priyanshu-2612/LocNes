package main.java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Cartridge {

    byte[] PRGMemory , CHRMemory;
    String filePath;
    byte[] vPRGMemory , vCHRMemory;
    boolean verticalMirroring;
    int MapperID=0 , PRGBanks=0 , CHRBanks=0;
    int header_len = 16; //16bytes
    int prg_rom_chunks = 4 , chr_rom_chunks = 5 , mapper1 = 6 , mapper2 = 7;

    Cartridge(String filePath){
        this.filePath = filePath;
    }

    void InitCartridge(){
        byte[] file_data;
        try {
            file_data = Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(file_data);

        int offset=0 , t=header_len;
        byte[] header = new byte[header_len];
        while(offset<header_len){
            offset += in.read(header,offset,t);
            t -= offset;
        }

        System.out.println("Read " + offset + " bytes of data");

        verticalMirroring = (header[6] & 0x01) != 0;

        if((header[mapper1] & 0x04) > 0) {

            byte[] junk = new byte[512];
            t = 512;
            offset = 0;
            while (t > 0) {
                int bytes_read = in.read(junk, offset, t);
                offset += bytes_read;
                t -= bytes_read;
            }

            System.out.println("Read " + offset + " bytes of data");
        }

        MapperID = ((header[mapper2] >> 4) << 4) | (header[mapper1] >> 4);

        int file_type = 1;
        int prgsize , chrsize;

        System.out.println("Mapper used is " + MapperID);

        switch(file_type){
            case 0:
                break;
            case 1:
                PRGBanks = header[prg_rom_chunks];
                prgsize = PRGBanks * 16384;
                vPRGMemory = new byte[prgsize];
                t=prgsize;
                offset = 0;
                while(t>0){
                    int bytes_read = in.read(vPRGMemory, offset,t);
                    offset += bytes_read;
                    t -= bytes_read;
                }

                System.out.println("Read " + offset + " bytes of data");

                CHRBanks = header[chr_rom_chunks];
                chrsize = CHRBanks * 8192;
                vCHRMemory = new byte[chrsize];
                System.out.println("CHRBanks is "+ CHRBanks);
                t=chrsize;
                offset = 0;
                while(t>0){
                    int bytes_read = in.read(vCHRMemory, offset,t);
                    offset += bytes_read;
                    t -= bytes_read;
                }

                System.out.println("Read " + offset + " bytes of data");

                break;
            case 2:
                break;
        }
    }
}
