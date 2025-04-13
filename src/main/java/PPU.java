package main.java;
import javafx.scene.transform.Affine;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.awt.Color;
import java.io.IOException;

public class PPU {
    Display display;
    byte[] ppu_memory = new byte[65536];
    byte[] ppu_registers = new byte[9];
    int Controller_Address = 0x2000;
    int PPU_Read_Buffer=0;
    int read_location=0;
    boolean horizontal_mirroring = true;
    int Mask = 0x2001;
    int Status = 0x2002;
    int OAM_Address = 0x2003;
    int OAM_Data = 0x2004;
    int Scroll = 0x2005;
    int Address = 0x2006;
    int Data = 0x2007;
    int OAM_DMA = 0x4014;
    int address_latch = 0;
    Cartridge cartridge;
    byte[][] nametable = new byte[2][1024];
    byte[] palette = new byte[32];
    int[][] patterntable = new int[2][4096];
    Color[] palScreen = new Color[0x40];
    int cycle=0 , scanline=-1;
    int V;
    int T;  //15 bits
    int X; //3 bits
    int write_toggle = 0; // 1 bit
    int bg_shifter_pattern_lo=0,bg_shifter_pattern_hi=0,bg_shifter_attrib_lo=0,bg_shifter_attrib_hi=0; //16 bits
    int bg_next_tile_lsb=0 , bg_next_tile_msb = 0,bg_next_tile_attrib=0;
    int bg_next_tile_id=0;
    boolean nmi = false;
    CPU cpu;

    public void cycle(){

        int nametableByte = 0;
        int palette=0;

        if(scanline >= -1 && scanline < 240){

            if (scanline == 0 && cycle == 0)
            {
                // Skipped
                cycle = 1;
            }

            if(scanline == -1 && cycle == 1){
                clearVBlank();
            }

            if((cycle >= 2 && cycle < 258)||(cycle >= 321 && cycle < 338)){

                UpdateShifters();
                switch((cycle-1)%8){
                    case 0:
                        LoadBackgroundShifters();
                        int nt_offset = 0;
                        nt_offset = 0x2000 | (V & 0x0fff);
                        //System.out.println("Accessing nametable byte at address " +Integer.toHexString(nt_offset));
                        nametableByte = ppuRead(nt_offset)&0xff;
                        //System.out.println("Read " + Integer.toHexString(nametableByte) + " from there");
//                        nametableByte <<= 4;
                        bg_next_tile_id = nametableByte;
                        break;
                    case 2:
                        //System.out.println("V is " + Integer.toBinaryString(V));
                        int coarse_x = V & 0x1f , coarse_y = (V & 0x03e0) >> 5;
                        //System.out.println("At coordinates x: " + coarse_x +" y: " + coarse_y);
                        int blockx = ((coarse_x >> 2)& 0x7) , blocky = ((coarse_y >> 2)& 0x7);  //each is of 3 bits
                        int attributeOffset = ((blocky << 3)&0x38) + blockx;
                        int nt = ((V & 0x0c00) >> 10);
                        //System.out.println("In nametable " + Integer.toBinaryString(nt));
                        bg_next_tile_attrib = ppuRead(0x23C0 | nt<<10 | attributeOffset);
                        //System.out.println("Attribute address is " + Integer.toHexString(0x23C0 | nt<<10 | blockx));
                        //System.out.println("Attribute byte is " + Integer.toHexString(bg_next_tile_attrib));
                        if ((coarse_y & 0x02)!=0) bg_next_tile_attrib >>= 4;
                        if ((coarse_x & 0x02)!=0) bg_next_tile_attrib >>= 2;
                        bg_next_tile_attrib &= 0x03;
                        break;
                    case 4:
                        int fine_y = (V & 0x7000) >> 12;
                        fine_y = fine_y & 0x7;
                        //System.out.println("PPUControl is " + Integer.toBinaryString(ppu_registers[0]&0xff));
                        int address = ( (((ppu_registers[Controller_Address-0x2000]&0x10)>>4)<<12)&0x1000) + (bg_next_tile_id << 4) + fine_y ;
//                        int address = ( 0x1000 + (bg_next_tile_id << 4) + fine_y) ;
                        //System.out.println("Loading sprite at " + Integer.toHexString(address) + " in ppu memory");
                        bg_next_tile_lsb = ppuRead(address)&0xff;
                        break;
                    case 6:
                        int fine_y2 = (V & 0x7000) >> 12;
                        fine_y2 = fine_y2&0x7;
                        int address2 = ( (((ppu_registers[Controller_Address-0x2000]&0x10)>>4)<<12)&0x1000) + (bg_next_tile_id << 4) + fine_y2 ;
//                        int address2 = ( 0x1000 + (bg_next_tile_id << 4) + fine_y2) ;
                        bg_next_tile_msb = ppuRead(address2+8)&0xff;
                        break;
                    case 7:
                        IncrementScrollX();
                        break;
                }
            }

            // End of a visible scanline, so increment downwards...
            if (cycle == 256)
            {
                IncrementScrollY();
            }

            //...and reset the x position
            if (cycle == 257)
            {
                LoadBackgroundShifters();
                transfer_addressx();
            }

            // Superfluous reads of tile id at end of scanline
            if (cycle == 338 || cycle == 340)
            {
                bg_next_tile_id = ppuRead((0x2000 | (V & 0x0FFF)));
            }

            if (scanline == -1 && cycle >= 280 && cycle < 305)
            {
                // End of vertical blank period so reset the Y address ready for rendering
                transfer_addressy();
            }

        }

        if (scanline == 240)
        {
            // Post Render Scanline - Do Nothing!
        }

        if (scanline >= 241 && scanline < 261)
        {
            if (scanline == 241 && cycle == 1)
            {
                setVBlank();
                //System.out.println("VBlank is now set");

                //System.out.println("Controller is " + Byte.toUnsignedInt(ppu_registers[Controller_Address-0x2000]));
                if (getNMIenable()==1) {
                    //System.out.println("NMI is now set");
                    nmi = true;
                }
            }
        }

        // Composition - We now have background pixel information for this cycle
        // At this point we are only interested in background

        byte bg_pixel = 0x00;   // The 2-bit pixel to be rendered
        byte bg_palette = 0x00; // The 3-bit index of the palette the pixel indexes

        // We only render backgrounds if the PPU is enabled to do so. Note if
        // background rendering is disabled, the pixel and palette combine
        // to form 0x00. This will fall through the colour tables to yield
        // the current background colour in effect
        if (backgRenderingEnabled())
        {
            // Handle Pixel Selection by selecting the relevant bit
            // depending upon fine x scrolling. This has the effect of
            // offsetting ALL background rendering by a set number
            // of pixels, permitting smooth scrolling
            int fine_x = X;
            //System.out.println("V : " + Integer.toBinaryString(V));
            int bit_mux =  ((0x8000 >> fine_x) & 0xffff); // 16 bits

            // Select Plane pixels by extracting from the shifter
            // at the required location.
            int p0_pixel = (byte) ((bg_shifter_pattern_lo & bit_mux) != 0 ? 1 : 0); // 8 bits
            int p1_pixel = (byte) ((bg_shifter_pattern_hi & bit_mux) != 0 ? 1 : 0); // 8 bits

            // Combine to form pixel index
            bg_pixel = (byte) ((p1_pixel << 1) | p0_pixel);
            //System.out.println("Background pixel is " + bg_pixel);

            // Get palette
              byte bg_pal0 = (byte) ((bg_shifter_attrib_lo & bit_mux) != 0 ? 1 : 0);
              byte bg_pal1 = (byte) ((bg_shifter_attrib_hi & bit_mux) != 0 ? 1 : 0);
              bg_palette = (byte) ((bg_pal1 << 1) | bg_pal0);
            //System.out.println("Palette is " + Integer.toHexString(Byte.toUnsignedInt(bg_palette)) );
        }


        // Now we have a final pixel colour, and a palette for this cycle
        // of the current scanline. Lets at long last, draw that ^&%*er :P

        //sprScreen->SetPixel(cycle - 1, scanline, GetColourFromPaletteRam(bg_palette, bg_pixel));
        display.setPixel(cycle-1 , scanline, getColor(bg_palette, bg_pixel));

        // Advance renderer - it never stops, it's relentless

        cycle++;
        if (cycle >= 341)
        {
            cycle = 0;
            scanline++;
            if (scanline >= 261)
            {
                scanline = -1;
            }
        }
    }


    public int getColor(int palette , int color){
        int offset = palette* 4 + color;
        //return (ppu_memory[0x3f00 + offset] & 0xff);
        return (ppuRead( (0x3f00 + offset)) & 0xff);
    }

    public int getVBlank(){
//        if( (cpu.cpu_memory[Status]&0x80) > 0) return 1;
        if( (ppu_registers[Status-0x2000]&0x80) > 0) return 1;
        else return 0;
    }

    public void setVBlank(){
//        cpu.cpu_memory[Status] = (byte) ((cpu.cpu_memory[Status] | 0x80) & 0xff);
//        ppuWrite((short) Status, (byte) ((ppuRead((short) Status) | 0x80) & 0xff));
        ppu_registers[Status-0x2000] = (byte) ((ppu_registers[Status-0x2000] | 0x80) & 0xff);
    }

    public int getNMIenable(){
        if((ppu_registers[Controller_Address-0x2000] & 0x80) != 0) return 1;
        else return 0;
    }

    public void setNMIenable(){
        ppu_memory[Controller_Address] = (byte) ((ppu_memory[Controller_Address] | 0x80) & 0xff);
    }

    public void UpdateShifters(){
        if(backgRenderingEnabled()) {
            // Shifting background tile pattern row
            bg_shifter_pattern_lo <<= 1;
            bg_shifter_pattern_lo &= 0xffff;
            bg_shifter_pattern_hi <<= 1;
            bg_shifter_pattern_hi &= 0xffff;

            // Shifting palette attributes by 1
            bg_shifter_attrib_lo <<= 1;
            bg_shifter_attrib_lo &= 0xffff;
            bg_shifter_attrib_hi <<= 1;
            bg_shifter_attrib_hi &= 0xffff;
        }
    }

    public void LoadBackgroundShifters(){
        bg_shifter_pattern_lo =  ((bg_shifter_pattern_lo & 0xFF00) | bg_next_tile_lsb)&0xffff;
        bg_shifter_pattern_hi = ((bg_shifter_pattern_hi & 0xFF00) | bg_next_tile_msb)&0xffff;

        bg_shifter_attrib_lo  =  ((bg_shifter_attrib_lo & 0xFF00) | (((bg_next_tile_attrib & 0b01)==1) ? 0xFF : 0x00))&0xffff;
        bg_shifter_attrib_hi  =  ((bg_shifter_attrib_hi & 0xFF00) | (((bg_next_tile_attrib & 0b10)==2) ? 0xFF : 0x00))&0xffff;

        //System.out.println("BG_SHIFTER_PATTERN_HI : "+ Integer.toBinaryString(bg_shifter_pattern_hi));
        //System.out.println("BG_SHIFTER_PATTERN_LO : "+ Integer.toBinaryString(bg_shifter_pattern_lo));

        //System.out.println("BG_SHIFTER_ATTRIBUTE_HI : "+ Integer.toBinaryString(bg_shifter_attrib_hi));
        //System.out.println("BG_SHIFTER_ATTRIBUTE_LO : "+ Integer.toBinaryString(bg_shifter_attrib_lo));
    }

    public void IncrementScrollX(){
        int coarse_x = V & 0x1f;
        if(backgRenderingEnabled() || spriteRenderingEnabled()) {
            if (coarse_x == 31) {
                V = ((V & 0xffe0) & 0xffff); //coarse_x = 0
                flip_nametablex_bit();
            } else {
                coarse_x++;
                V = (((V & 0xffe0) | coarse_x) & 0xffff);
            }
        }
    }

    public void IncrementScrollY(){
        int fine_y = (((V&0x7000) >> 12)&0x7);
        int coarse_y = (((V & 0x3e0)>>5) & 0x1f);
        //System.out.println("Coarse y is " +coarse_y);
        if(backgRenderingEnabled() || spriteRenderingEnabled()) {
            if (fine_y < 7) {
                fine_y++;
            }
            else{
                fine_y = 0;
                if (coarse_y == 29) {
                    coarse_y = 0;
                    flip_nametabley_bit();
                } else if (coarse_y == 31) {
                    coarse_y = 0;
                } else coarse_y++;
            }
            V = ((V&0x0fff)| ((fine_y<<12)&0x7000) );  //set fine_y
            V = ((V&0x7c1f)| ((coarse_y<<5)&0x03e0) );  //set coarse_y
        }
        //System.out.println("Coarse y updated to " +coarse_y);
        //System.out.println("Incremented Scroll Y");
    }

    public void flip_nametablex_bit(){
        V =  ((V ^ 0x400) & 0xffff) & 0xffff;
    }
    public void flip_nametabley_bit(){
        V =  ((V ^ 0x800) & 0xffff) & 0xffff;
    }

    public void transfer_addressx(){
        if(backgRenderingEnabled() || spriteRenderingEnabled()) {
            V = (((V & 0xfbff) | (T & 0x400)) & 0xffff) & 0xffff; //ntx
            V =  (((V & 0xffe0) | (T & 0x1f)) & 0xffff) & 0xffff; // coarsex
        }
    }

    public void transfer_addressy(){
        if(backgRenderingEnabled() || spriteRenderingEnabled()) {
              V = (V & 0x041F) | (T & 0x7BE0);
        }
    }

    public void clearVBlank(){
        ppu_registers[Status-0x2000] = (byte) ((ppu_registers[Status-0x2000] & 0x7f) & 0xff);
    }

    public int ppuRead(int addr){
        int addr_value = addr;
        addr_value  &= 0xffff;
//        //System.out.println("PPU Read Address is " + Integer.toHexString(addr));
        if(addr_value <= 0x1fff){
            int pt_num = (addr & 0x1000) >> 12;
            int pt_offset = addr_value & 0x0fff;
//            //System.out.println("Returning value from Pt " + pt_num + " at address " + Integer.toHexString(pt_offset));
//            //System.out.println("Value returned is " + Integer.toHexString(patterntable[(addr_value & 0x1000) >> 12][addr_value & 0x0fff]));
            return patterntable[pt_num][addr_value & 0x0fff]&0xff;
        }
        else if(addr_value <= 0x2fff){
            addr_value  &= 0x0fff;
            if (!horizontal_mirroring)
            {
                // Vertical
                if (addr_value  >= 0x0000 && addr_value  <= 0x03FF)
                    return nametable[0][addr_value  & 0x03FF];
                if (addr_value  >= 0x0400 && addr_value  <= 0x07FF)
                    return nametable[1][addr_value  & 0x03FF];
                if (addr_value  >= 0x0800 && addr_value  <= 0x0BFF)
                    return nametable[0][addr_value  & 0x03FF];
                if (addr_value >= 0x0C00 && addr_value  <= 0x0FFF)
                    return nametable[1][addr_value  & 0x03FF];
            }
            else
            {
                // Horizontal
                if (addr_value  >= 0x0000 && addr_value  <= 0x03FF)
                    return nametable[0][addr_value  & 0x03FF];
                if (addr_value  >= 0x0400 && addr_value  <= 0x07FF)
                    return nametable[0][addr_value  & 0x03FF];
                if (addr_value  >= 0x0800 && addr_value  <= 0x0BFF)
                    return nametable[1][addr_value  & 0x03FF];
                if (addr_value  >= 0x0C00 && addr <= 0x0FFF)
                    return  nametable[1][addr_value  & 0x03FF];
            }
        }
        else if(addr_value >= 0x3f00 && addr_value <= 0x3fff){
            addr_value &= 0x001F;
            if (addr_value == 0x0010) addr_value = 0x0000;
            if (addr_value == 0x0014) addr_value = 0x0004;
            if (addr_value == 0x0018) addr_value = 0x0008;
            if (addr_value == 0x001C) addr_value = 0x000C;
            return palette[addr_value];
        }
        return 0;
    }

    public void ppuWrite(short addr , byte data){
        int addr_value = Short.toUnsignedInt(addr);
        addr_value &= 0x3fff;
        //System.out.println("PPU Write address is " + Integer.toHexString(addr_value));
        if(addr_value <= 0x1fff){
            patterntable[addr_value & 0x1000 >> 12][addr_value & 0x0fff] = data;
        }
        else if(addr_value <= 0x2fff){
            addr_value &= 0x0fff;
            if (!horizontal_mirroring)
            {
                // Vertical
                if (addr_value >= 0x0000 && addr_value <= 0x03FF)
                    nametable[0][addr_value & 0x03FF] = data;
                if (addr_value >= 0x0400 && addr_value <= 0x07FF)
                    nametable[1][addr_value & 0x03FF] = data;
                if (addr_value >= 0x0800 && addr_value <= 0x0BFF)
                    nametable[0][addr_value & 0x03FF] = data;
                if (addr_value >= 0x0C00 && addr_value <= 0x0FFF)
                    nametable[1][addr_value & 0x03FF] = data;
            }
            else
            {
                // Horizontal
                if (addr_value >= 0x0000 && addr_value <= 0x03FF)
                    nametable[0][addr_value & 0x03FF] = data;
                if (addr_value >= 0x0400 && addr_value <= 0x07FF)
                    nametable[0][addr_value & 0x03FF] = data;
                if (addr_value >= 0x0800 && addr_value <= 0x0BFF)
                    nametable[1][addr_value & 0x03FF] = data;
                if (addr_value >= 0x0C00 && addr_value <= 0x0FFF)
                    nametable[1][addr_value & 0x03FF] = data;
            }
        }
        else if(addr_value >= 0x3f00 && addr_value <= 0x3fff){
            addr_value &= 0x001F;
            if (addr_value == 0x0010) addr_value = 0x0000;
            if (addr_value == 0x0014) addr_value = 0x0004;
            if (addr_value == 0x0018) addr_value = 0x0008;
            if (addr_value == 0x001C) addr_value = 0x000C;
            palette[addr_value] = data;
        }
    }

    public byte cpuRead(int addr){
        //For when CPU wants to read the exposed registers
        int addr_value = addr & 0xffff;
        switch(addr_value){

            case 0x2000:  //control
                return ppu_registers[addr_value-0x2000];
            case 0x2001:  //mask
                return ppu_registers[addr_value-0x2000];
            case 0x2002:  //status
                write_toggle = 0;
                return (byte) read_Status();
            case 0x2003:  //OAM address
                return ppu_registers[addr_value-0x2000];
            case 0x2004:  //OAM data
                return ppu_registers[addr_value-0x2000];
            case 0x2005:  //scroll
                break;
            case 0x2006:  //ppu address
                break;
            case 0x2007:  //ppu data
                return (byte) read_from_Data();
        }

        return 0;
    }

    public void cpuWrite(int addr , byte data){
        //For when cpu wants to write to the exposed registers
        int addr_value = addr&0xffff;
        switch(addr_value){

            case 0x2000:  //control
                ppu_registers[Controller_Address-0x2000] = data;
                T =  (((T&0xf3ff) | ((data & 0x3) << 10)) & 0x7fff) & 0xffff;
                //System.out.println("T is " + Integer.toHexString(T));
                //System.out.println("PPUControl is " + Integer.toHexString(Byte.toUnsignedInt(ppu_registers[0])));
                break;
            case 0x2001:  //mask
                ppu_registers[1] = data;
                break;
            case 0x2002:  //status
                break;
            case 0x2003:  //OAM address
                break;
            case 0x2004:  //OAM data
                break;
            case 0x2005:  //scroll
                int d = data & 0xff;
                if(write_toggle==0){
                    T = ( ((T&0xffe0) | ((d >> 3)&0x1f) ) & 0x7fff) & 0xffff;
                    X =  (d & 0x07) & 0xff;
                    //System.out.println("X changed to  " + Integer.toHexString(X));
                    write_toggle = 1;
                }
                else{
                    T =  (( (T&0x8fff) | ((d&0x07)<<12)) & 0x7fff) &0xffff;
                    T =  (( (T&0xfc1f) | (((d>>3)&0x1f)<<5)) & 0x7fff) &0xffff;
                    write_toggle = 0;
                }
                break;

            case 0x2006:  //ppu address
//                write_to_address_reg(data);
                int dad = data & 0xff;
                if(write_toggle==0){
                    T =  (( (T&0xc0ff) | ((dad & 0b00111111) << 8)) & 0x7fff)&0xffff;
                    T =  (T & 0xbfff) & 0xffff;
                    write_toggle = 1;
                }
                else{
                    T =  (( (T&0xff00) | ((dad&0xff))) & 0x7fff)&0xffff;
                    V = T;
                    write_toggle = 0;
                }
                break;

            case 0x2007:  //ppu data
                write_to_Data(data);
                //System.out.println("PPUControl is " + Integer.toHexString(ppu_registers[0]));
                if((ppu_registers[Controller_Address-0x2000] & 0x04) != 0){
//                        read_location += 32;
                          V += 32;
                }
                else {
//                        read_location += 1;
                    V += 1;
                }
                break;
        }
    }


    public int read_Status(){
        int data = ppu_registers[Status-0x2000] & 0xE0;
//        data = (data | PPU_Read_Buffer & 0x1f); //Some games may require this erroneous behaviour
//        int data = cpuRead((short) Status) & 0xE0;
//        ppu_registers[Status-0x2000] &= (byte) 0x80; //clear the V-Blank bit
//        cpuWrite((short) Status, (byte) (ppuRead((short) Status) & 0x80));
        ppu_registers[Status-0x2000] = (byte) (ppu_registers[Status-0x2000] & 0x7f); //clear the V-Blank bit
        address_latch = 0;
        return data;
    }

    public void write_to_address_reg(byte value){
        ppu_registers[Address-0x2000] = (byte) (value & 0xff);
        if(address_latch==0) {
            read_location = ((value & 0xff)<<8)&0xff00;
            //System.out.println("Read Location High is set to " + Integer.toHexString(read_location & 0xfffff));
            address_latch = 1;
        }
        else {
            read_location += value & 0xff;
            //System.out.println("Read Low is set to " + Integer.toHexString(read_location & 0xffff));
            address_latch = 0;
        }
//        update_status();
    }

    public void update_status(){
        if(getI()){
            ppu_registers[Status-0x2000] = (byte) ((ppu_registers[Controller_Address-0x2000]+ 32)&0xff);
            //System.out.println("PPUControl is " + Integer.toHexString(ppu_registers[Controller_Address-0x2000]));
        }
        else{
            ppu_registers[Status-0x2000] = (byte) ((ppu_registers[Controller_Address-0x2000]+1)&0xff);
            //System.out.println("PPUControl is " + Integer.toHexString(ppu_registers[Controller_Address-0x2000]));
        }
    }

    public boolean getI(){
        return (((ppu_registers[Controller_Address-0x2000]) & 0x04) != 0);
//        return (((ppuRead((short) Controller_Address) >> 2) & 0x01) == 1);
    }

    public int read_from_Data(){
        int return_value;
        return_value = PPU_Read_Buffer & 0xff;
//        PPU_Read_Buffer = ppuRead(read_location)&0xff;
        PPU_Read_Buffer = ppuRead(V)&0xff;
//        update_status();
        // However, if the address was in the palette range, the
        // data is not delayed, so it returns immediately
        if (V >= 0x3F00) return_value = PPU_Read_Buffer;
        if((ppu_registers[Controller_Address-0x2000] & 0x04) != 0){
            V += 32;
        }
        else{
            V += 1;
        }
        return return_value;
    }

    public void write_to_Data(byte data){
//        int write_location = read_location;
        int write_location = V;
        ppuWrite((short) (write_location), (byte) (data & 0xff));
        //System.out.println("Wrote " + Integer.toHexString((data & 0xff)) + " to 0x" + Integer.toHexString(write_location));
    }


    public boolean backgRenderingEnabled(){
        //System.out.println("PPUMask : "+ Integer.toBinaryString(ppu_registers[Mask-0x2000]));
        if((ppu_registers[Mask-0x2000] & 0x8) != 0){
            //System.out.println("Background Rendering is ON");
        }
        else{
            //System.out.println("Background Rendering is OFF");
        }
        return ((ppu_registers[Mask-0x2000] & 0x8) != 0);
    }

    public boolean spriteRenderingEnabled(){
        //System.out.println("PPUMask : "+ Integer.toBinaryString(ppu_registers[Mask-0x2000]));
        if((ppu_registers[Mask-0x2000] & 0x10) != 0){
            //System.out.println("Sprite Rendering is ON");
        }
        else{
            //System.out.println("Sprite Rendering is OFF");
        }
        return ((ppu_registers[Mask-0x2000] & 0x10) != 0);
    }


    public int get_mirror(short addr){
        int loc = Short.toUnsignedInt(addr);
        if(loc>=0x2000 && loc<=0x3f00){
            if(horizontal_mirroring){
                if(loc<=0x27ff)
                    loc = (loc-0x2000)%0x400 + 0x2000;
                else
                    loc = (loc-0x2400)%0x400 + 0x2000;
            }
            else{
                if(loc<=0x23ff || (loc>=0x2800 && loc<=0x2fff))
                    loc = (loc-0x2000)%0x400 + 0x2000;
                else
                    loc = (loc-0x2400)%0x400 + 0x2000;
            }
        }
        return loc;
    }

    PPU(){
        palScreen[0x00] = new Color(84, 84, 84);
        palScreen[0x01] = new Color(0, 30, 116);
        palScreen[0x02] = new Color(8, 16, 144);
        palScreen[0x03] = new Color(48, 0, 136);
        palScreen[0x04] = new Color(68, 0, 100);
        palScreen[0x05] = new Color(92, 0, 48);
        palScreen[0x06] = new Color(84, 4, 0);
        palScreen[0x07] = new Color(60, 24, 0);
        palScreen[0x08] = new Color(32, 42, 0);
        palScreen[0x09] = new Color(8, 58, 0);
        palScreen[0x0A] = new Color(0, 64, 0);
        palScreen[0x0B] = new Color(0, 60, 0);
        palScreen[0x0C] = new Color(0, 50, 60);
        palScreen[0x0D] = new Color(0, 0, 0);
        palScreen[0x0E] = new Color(0, 0, 0);
        palScreen[0x0F] = new Color(0, 0, 0);

        palScreen[0x10] = new Color(152, 150, 152);
        palScreen[0x11] = new Color(8, 76, 196);
        palScreen[0x12] = new Color(48, 50, 236);
        palScreen[0x13] = new Color(92, 30, 228);
        palScreen[0x14] = new Color(136, 20, 176);
        palScreen[0x15] = new Color(160, 20, 100);
        palScreen[0x16] = new Color(152, 34, 32);
        palScreen[0x17] = new Color(120, 60, 0);
        palScreen[0x18] = new Color(84, 90, 0);
        palScreen[0x19] = new Color(40, 114, 0);
        palScreen[0x1A] = new Color(8, 124, 0);
        palScreen[0x1B] = new Color(0, 118, 40);
        palScreen[0x1C] = new Color(0, 102, 120);
        palScreen[0x1D] = new Color(0, 0, 0);
        palScreen[0x1E] = new Color(0, 0, 0);
        palScreen[0x1F] = new Color(0, 0, 0);

        palScreen[0x20] = new Color(236, 238, 236);
        palScreen[0x21] = new Color(76, 154, 236);
        palScreen[0x22] = new Color(120, 124, 236);
        palScreen[0x23] = new Color(176, 98, 236);
        palScreen[0x24] = new Color(228, 84, 236);
        palScreen[0x25] = new Color(236, 88, 180);
        palScreen[0x26] = new Color(236, 106, 100);
        palScreen[0x27] = new Color(212, 136, 32);
        palScreen[0x28] = new Color(160, 170, 0);
        palScreen[0x29] = new Color(116, 196, 0);
        palScreen[0x2A] = new Color(76, 208, 32);
        palScreen[0x2B] = new Color(56, 204, 108);
        palScreen[0x2C] = new Color(56, 180, 204);
        palScreen[0x2D] = new Color(60, 60, 60);
        palScreen[0x2E] = new Color(0, 0, 0);
        palScreen[0x2F] = new Color(0, 0, 0);

        palScreen[0x30] = new Color(236, 238, 236);
        palScreen[0x31] = new Color(168, 204, 236);
        palScreen[0x32] = new Color(188, 188, 236);
        palScreen[0x33] = new Color(212, 178, 236);
        palScreen[0x34] = new Color(236, 174, 236);
        palScreen[0x35] = new Color(236, 174, 212);
        palScreen[0x36] = new Color(236, 180, 176);
        palScreen[0x37] = new Color(228, 196, 144);
        palScreen[0x38] = new Color(204, 210, 120);
        palScreen[0x39] = new Color(180, 222, 120);
        palScreen[0x3A] = new Color(168, 226, 144);
        palScreen[0x3B] = new Color(152, 226, 180);
        palScreen[0x3C] = new Color(160, 214, 228);
        palScreen[0x3D] = new Color(160, 162, 160);
        palScreen[0x3E] = new Color(0, 0, 0);
        palScreen[0x3F] = new Color(0, 0, 0);
    }

    public int add(byte a , byte b){
        return Byte.toUnsignedInt(a) + Byte.toUnsignedInt(b);
    }
    public int add(int a , byte b){
        return a + Byte.toUnsignedInt(b);
    }

    public int add(byte a , int b){
        return b + Byte.toUnsignedInt(a);
    }

}
