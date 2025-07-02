package main.java;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class InstructionSet {

    CPU cpu;
    PPU ppu;

    public InstructionSet(CPU cpu, PPU ppu){
        this.cpu = cpu;
        this.ppu = ppu;
    }
    public int adc(addressingMode A, byte op){
        int sum , loc , addr;
        boolean[] flags = {true , true , true , true};
        switch (A) {
            case Immediate:
                sum = add(op , (byte) (cpu.Status & 0x01)); //op+C
                sum = add(sum , cpu.Accumulator); // op+C+Alda
//                sum = sum & 0xff;
                setFlags(sum , flags);
                checkAndSet((cpu.Accumulator & 0xff), (op & 0xff) , sum);
                cpu.Accumulator = (byte)sum;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                sum = (cpu.getData(op&0xff)&0xff) +  (cpu.Status & 0x01);
                sum = add(sum, cpu.Accumulator);
//                sum = sum&0xff;
                setFlags(sum , flags);
                checkAndSet((cpu.Accumulator & 0xff), cpu.getData(op & 0xff) , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op , cpu.X)&0xff;
                int val = cpu.getData(address);
                sum = add(val , (byte) (cpu.Status & 0x01));
                sum = add(sum, cpu.Accumulator);
//                sum = sum & 0xff;
                setFlags(sum, flags);
                checkAndSet((cpu.Accumulator & 0xff), val&0xff , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                loc = add(op , cpu.X);
                loc = loc&0xff;
                int lowByte = cpu.getData(loc & 0xFF) & 0xFF;
                int highByte = cpu.getData((loc + 1) & 0xFF) & 0xFF;
                addr = (highByte << 8) | lowByte;

                sum = add(cpu.getData(addr) , (byte) (cpu.Status & 0x01));
                sum = add(sum, cpu.Accumulator);
                setFlags(sum , flags);
                checkAndSet((cpu.Accumulator & 0xff),(cpu.cpu_memory[addr] & 0xff) , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                lowByte = cpu.getData(op & 0xFF) & 0xFF;
                highByte = cpu.getData((op + 1) & 0xFF) & 0xFF;
                addr = (highByte << 8) | lowByte;

                addr = add(addr, cpu.Y);
                addr = addr & 0xffff;
                sum = (cpu.getData(addr)&0xff +  cpu.Status&0x01);
                sum = add(sum, cpu.Accumulator);
                setFlags(sum , flags);
                checkAndSet(cpu.Accumulator & 0xff ,cpu.cpu_memory[addr]&0xff , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 2;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int adc(addressingMode A , short op){
        int sum , loc, carry;
        if(!getCarry())
            carry = 1;
        else
            carry = 0;
        boolean[] flags = {true , true , true , true};
        switch(A){
            case Absolute:
                sum = add(cpu.getData(op), (byte) (cpu.Status & 0x01));
                sum = add(sum, cpu.Accumulator);
//                sum = sum & 0xff;
                setFlags(sum ,flags);
                checkAndSet(add(cpu.Accumulator,0),add(cpu.cpu_memory[Short.toUnsignedInt(op)], 0) , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                loc = addShort(op, cpu.X);
                sum = cpu.getData(loc) + (cpu.Status & 0x01) + (cpu.Accumulator & 0xff);

                setFlags(sum, flags);
                checkAndSet((cpu.Accumulator & 0xff),cpu.getData(loc) , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:

                loc = addShort(op, cpu.Y);
                sum = cpu.getData(loc) + (cpu.Status & 0x01) + (cpu.Accumulator & 0xff);
//                sum = sum&0xff;
                setFlags(sum , flags);
                checkAndSet(add(cpu.Accumulator,0),add(cpu.cpu_memory[loc], 0) , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int and(addressingMode A , byte op){
        int val , loc ,addr;
        int operand;
        boolean[] flags = {false , false, true , true};
        switch(A){
            case Immediate:
                operand = Integer.parseInt(Integer.toString(Byte.toUnsignedInt(op)),16);
                val = cpu.Accumulator & op;
                int ac = Byte.toUnsignedInt(cpu.Accumulator) , opint = Byte.toUnsignedInt(op);
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(op&0xff) & cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = cpu.X+Byte.toUnsignedInt(op);
                address &= 0xff;
                val = cpu.getData(address) & cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                loc = add(op , cpu.X);
                loc = loc&0xff;
                int lowByte = cpu.getData(loc & 0xFF) & 0xFF;
                int highByte = cpu.getData((loc + 1) & 0xFF) & 0xFF;
                addr = (highByte << 8) | lowByte;

                val = cpu.Accumulator & cpu.getData(addr);
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                lowByte = cpu.getData(op & 0xFF) & 0xFF;
                highByte = cpu.getData((op + 1) & 0xFF) & 0xFF;
                addr = (highByte << 8) | lowByte;

                addr = (addr + (cpu.Y & 0xff)) & 0xffff;

                val = cpu.getData(addr) & cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int and(addressingMode A , short op){
        int val;
        boolean[] flags = {false , false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op) & cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.getData((op & 0xffff) + (cpu.X & 0xff)) & cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.getData((op & 0xffff) + (cpu.Y & 0xff)) & cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int asl(addressingMode A){
        boolean[] flags = {true, false, true , true};
            int a = cpu.Accumulator&0xff;
            int carry = ((a&0x80) == 0x80) ? 1: 0;
            a = (a<<1)&0xff ;
            setFlags(a, flags);
            cpu.Status |= (byte) carry;
            cpu.Accumulator = (byte) a;
            cpu.PC += 1;
            return 2;

    }

    public int asl(addressingMode A , byte op){
        int val , loc , addr, carry;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                carry = ((val&0x80) == 0x80) ? 1: 0;
                val = (val << 1)&0xff;
                setFlags(val, flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(Byte.toUnsignedInt(op),(byte) val);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                val = cpu.getData(address);
                carry = ((val&0x80) == 0x80) ? 1: 0;
                val = (val << 1)&0xff;
                setFlags(val, flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(add(op, cpu.X), (byte) val);
                cpu.PC += 2;
                return 6;

            default:
                throw new OpcodeException();
        }
    }

    public int asl(addressingMode A, short op){
        int val , loc , addr, carry;
        boolean[] flags = {true , false , true , true};
        switch(A) {
            case Absolute:
                addr = Short.toUnsignedInt(op);
                val = cpu.getData(addr);
                carry = ((val & 0x80) == 0x80) ? 1 : 0;
                val = (val << 1) & 0xff;
                setFlags(val, flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(addr,(byte)val);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                addr = addShort(op, cpu.X);
                val = cpu.getData(addr);
                carry = ((val & 0x80) == 0x80) ? 1 : 0;
                val = (val << 1) & 0xff;
                setFlags(val, flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(addr,(byte)val);
                cpu.PC += 3;
                return 7;

            default:
                throw new OpcodeException();
        }
    }

    public int bcc(addressingMode A ,byte op){
        cpu.PC += 2;
        if(!getCarry())cpu.PC += op;  //signed addition
        return 2;
    }

    public int bcs(addressingMode A ,byte op){
        cpu.PC += 2;
        if(getCarry())cpu.PC += op;
        return 2;
    }

    public int beq(addressingMode A ,byte op){
        cpu.PC += 2;
        if(getZero()) {
//            //System.out.println("Displacing by " + Integer.toHexString(Byte.toUnsignedInt(op)));
//            //System.out.println("Branching to 0x" + Integer.toHexString((cpu.PC+op)&0xffff));
//          cpu.PC = (short) add(cpu.PC, op);
            cpu.PC += op;  //signed addition
//            //System.out.println("PC is now " + Byte.toUnsignedInt(op));
        }
        return 2;
    }

    public int bit(addressingMode A , byte op) {

        int acc = Byte.toUnsignedInt((byte) (cpu.Accumulator & 0xff)), mem, val;
        mem = cpu.getData(op & 0xff);
        val = mem & acc;
        if ((val & 0xff) == 0){
                setZero();
        }
        else{
            clearZero();
        }

        if(((mem & 0x80) >>7) ==1) setNegative();
        else clearNegative();

        if(((mem & 0x40) >>6) ==1) setOverflow();
        else clearOverflow();

        cpu.PC += 2;
        return 2;

//        cpu.PC += 2;
//        return 3;
    }

    public int bit(addressingMode A, short op){

        int acc = Byte.toUnsignedInt((byte) (cpu.Accumulator & 0xff)), mem , val;
        mem = cpu.getData(op);
        val = mem & acc;
        if ((val & 0xff) == 0){
            setZero();
        }
        else{
            clearZero();
        }

        if(((mem & 0x80) >>7) ==1) setNegative();
        else clearNegative();

        if(((mem & 0x40) >>6) ==1) setOverflow();
        else clearOverflow();

        cpu.PC += 3;
        return 4;
    }

    public int nmi(){
        cpu.push((byte) ((cpu.PC >> 8) & 0x00ff));

        cpu.push((byte) (cpu.PC & 0x00ff));

        cpu.Status |= 0x10;  //set the B flag
        cpu.Status |= 0x4;  //set Interrupt disable

        cpu.push(cpu.Status);

        int PC_HIGH = ((cpu.cpu_memory[0xfffa+1]<<8) & 0xff00);
        int PC_LOW = ((cpu.cpu_memory[0xfffa]) & 0x00ff);

        cpu.PC = (short) (PC_HIGH + PC_LOW);
        return 8;
    }

    public int bmi(addressingMode A ,byte op){
        cpu.PC += 2;
//        if(getNegative()) cpu.PC = (short) add(cpu.PC , op);
        if(getNegative()) cpu.PC += op;  //signed addition
        return 2;
    }

    public int bne(addressingMode A , byte op){
        cpu.PC += 2;
//        if(!getZero()) cpu.PC = (short) add(cpu.PC , op);
        if(!getZero()) cpu.PC += op;  //signed addition
        return 2;
    }

    public int bpl(addressingMode A ,byte op){
        cpu.PC += 2;
//        if(!getNegative()) cpu.PC = (short) add(cpu.PC , op);
        if(!getNegative()) cpu.PC += op;  //signed addition
        return 2;
    }

    public int brk(addressingMode A){
        cpu.PC  += 2; // guesss so
        cpu.push((byte) ((cpu.PC >> 8)&0xff));

        cpu.push((byte) ((cpu.PC)&0xff));

        setBreak();

        cpu.push(cpu.Status);

        int lo = cpu.cpu_memory[0xFFFE] & 0xFF;
        int hi = cpu.cpu_memory[0xFFFF] & 0xFF;
        int loc = (hi << 8) | lo;

        cpu.PC = (short) loc;
        return 7;
    }

    public int bvc(addressingMode A ,byte op){
        cpu.PC += 2;
//        if(!getOverflow()) cpu.PC = (short) add(cpu.PC , op);
        if(!getOverflow()) cpu.PC += op;  //signed addition
        return 2;
    }

    public int bvs(addressingMode A , byte op){
        cpu.PC += 2;
//        if(getOverflow()) cpu.PC = (short) add(cpu.PC , op);
        if(getOverflow()) cpu.PC += op;  //signed addition
        return 2;
    }

    public int clc(addressingMode A){
        clearCarry();
        cpu.PC += 1;
        return 2;
    }

    public int cld(addressingMode A){
        clearDecimal();
        cpu.PC += 1;
        return 2;
    }

    public int cli(addressingMode A){
        clearInterrupt();
        cpu.PC += 1;
        return 2;
    }

    public int clv(addressingMode A){
        clearOverflow();
        cpu.PC += 1;
        return 2;
    }

    public void compare_set_flags(int r, int op){
        r = r&0xff;
        op = op&0xff;
        int res = r-op;
//        //System.out.println("Result is " + Integer.toHexString(res)+ " " + res);
        if(r < op){
            clearZero();
        }
        else if(r == op){
            setZero();
        }
        else{
            clearZero();
        }
        if(((res & 0x80) >> 7)==1) setNegative();
        else clearNegative();

        if(Byte.toUnsignedInt((byte) r) >= Byte.toUnsignedInt((byte) op))
            setCarry();
        else
            clearCarry();
    }

    public int cmp(addressingMode A , byte op){
        int loc ,addr, val , acc = cpu.Accumulator, src;
        switch(A){
            case Immediate:

                compare_set_flags(cpu.Accumulator,op);
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                val = cpu.getData(op & 0xff);
                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                loc = add(op , cpu.X);
                loc = loc&0xff;
                int lowByte = cpu.getData(loc & 0xFF) & 0xFF;
                int highByte = cpu.getData((loc + 1) & 0xFF) & 0xFF;
                addr = (highByte << 8) | lowByte;

                val = cpu.getData(addr);

                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                lowByte  = cpu.getData(op & 0xFF) & 0xFF;
                highByte = cpu.getData((op + 1) & 0xFF) & 0xFF;
                loc = (highByte << 8) | lowByte;
                val = cpu.getData(loc + (cpu.Y & 0xff));
                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 2;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int cmp(addressingMode A, short op){
        int loc , val , acc = cpu.Accumulator;
        switch(A){
            case Absolute:
                val = cpu.getData(op);
                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.getData((cpu.X&0xff) + (op&0xffff));
                compare_set_flags(cpu.Accumulator & 0xff,val);
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.getData((cpu.Y&0xff) + (op&0xffff));
                compare_set_flags(cpu.Accumulator & 0xff,val);
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int cpx(addressingMode A , byte op){
        int loc , val , x = cpu.X;
        switch(A){
            case Immediate:
                val = op;

//                if(x==val) setZero();
//                else clearZero();
//
//                if(x >= val) setCarry();
//                else clearCarry();
//
//                if(x<val) setNegative();
//                else clearNegative();

                compare_set_flags(cpu.X,val);
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(op & 0xff);

                compare_set_flags(cpu.X,val);
                cpu.PC += 2;
                return 3;

            default:
                throw new OpcodeException();
        }
    }

    public int cpx(addressingMode A,short op){
        int loc , val , x = cpu.X;
        val = cpu.getData(op);

        compare_set_flags(cpu.X,val);
        cpu.PC += 3;
        return 4;
    }

    public int cpy(addressingMode A , byte op){
        int loc , val , y = cpu.Y;
        switch(A){
            case Immediate:
                val = op;

                compare_set_flags(cpu.Y,val);
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(op & 0xff);

                compare_set_flags(cpu.Y,val);
                cpu.PC += 2;
                return 3;

            default:
                throw new OpcodeException();
        }
    }

    public int cpy(addressingMode A, short op){
        int loc , val , y = cpu.Y;
        val = cpu.getData(op);

        compare_set_flags(cpu.Y,val);
        cpu.PC += 3;
        return 4;
    }

    public int dec(addressingMode A , byte op){
        int val , loc;
        boolean[] flags = {false , false, true , true};
        switch(A){
            case ZeropageAbs:
                loc = Byte.toUnsignedInt(op);

                cpu.writeTo(loc, (byte) ((cpu.getData(loc) - 1) & 0xff));

                setFlags(add(cpu.cpu_memory[loc],0) , flags);
                cpu.PC += 2;
                dump_at(0);
                return 5;

            case ZeropageIndexed:
                loc = add(op,cpu.X)&0xff;
                cpu.writeTo(loc,(byte) ((cpu.getData(loc) - 1) & 0xff) );
                setFlags(add(cpu.cpu_memory[loc],0) , flags);
                cpu.PC += 2;
                dump_at(0);
                return 6;

            default:
                throw new OpcodeException();
        }
    }

    public int dec(addressingMode A , short op){
        int loc , val;
        boolean[] flags = {false , false, true , true};
        switch(A){
            case Absolute:
                loc = Short.toUnsignedInt(op);
                cpu.writeTo(loc, (byte) ((cpu.getData(loc) - 1) & 0xff));
                setFlags(add(cpu.cpu_memory[loc],0) , flags);
                cpu.PC += 3;
                dump_at(0);
                return 6;

            case AbsoluteX:
                loc = addShort(op, cpu.X);
                cpu.writeTo(loc,(byte) ((cpu.getData(loc) - 1) & 0xff));
                setFlags(add(cpu.cpu_memory[loc],0) , flags);
                cpu.PC += 3;
                dump_at(0);
                return 7;

            default:
                throw new OpcodeException();
        }
    }

    public int dex(addressingMode A){
        boolean[] flags = {false , false, true , true};
//        //System.out.println("X is " + Byte.toUnsignedInt(cpu.X));
//        cpu.X = (byte) sub(cpu.X, 1);
        cpu.X = (byte) ((cpu.X&0xff)-1);
//        //System.out.println("X is now " + Byte.toUnsignedInt(cpu.X));

        setFlags(Byte.toUnsignedInt(cpu.X) , flags);
//        setFlags((cpu.X&0xff) , flags);
        cpu.PC += 1;
        return 2;
    }

    public int dey(addressingMode A){
        boolean[] flags = {false , false, true , true};
//        //System.out.println("Y is " + Byte.toUnsignedInt(cpu.Y));
        cpu.Y = (byte) sub(cpu.Y, 1);
//        //System.out.println("Y is now " + Byte.toUnsignedInt(cpu.Y));

        setFlags(Byte.toUnsignedInt(cpu.Y), flags);
        cpu.PC += 1;
        return 2;
    }

    public int eor(addressingMode A , byte op){
        int val , loc, res;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                res = cpu.Accumulator ^ op;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                val = cpu.getData(address);
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                int addr = add(cpu.X,op) & 0xff;
                int low = cpu.getData(addr & 0xFF) & 0xFF;
                int high = cpu.getData((addr + 1) & 0xFF) & 0xFF;
                loc = (high << 8) | low;
                val = cpu.getData(loc);
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                high = (cpu.getData((op + 1) & 0xFF) & 0xFF) << 8;
                low  = cpu.getData(op & 0xFF) & 0xFF;
                loc  = high | low;

                val = cpu.getData(loc + (cpu.Y & 0xff));
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 2;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int eor(addressingMode A , short op){
        int loc , val , res;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op);
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.getData(addShort(op, cpu.X));
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.getData(addShort(op , cpu.Y));
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int inc(addressingMode A , byte op){
        int val , loc , res;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                res = (val + 1)&0xff;
                setFlags(res, flags);
                cpu.writeTo(Byte.toUnsignedInt(op) , (byte) res);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                val = cpu.getData(address);
                res = (val + 1)&0xff;
                setFlags(res, flags);
                cpu.writeTo(add(op, cpu.X), (byte)res);
                cpu.PC += 2;
                return 6;

            default:
                throw new OpcodeException();
        }
    }

    public int inc(addressingMode A , short op){
        int loc , res , val;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op);
                res = (val + 1)&0xff;
                setFlags(res, flags);
                cpu.writeTo(Short.toUnsignedInt(op), (byte) res);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                val = cpu.getData(addShort(op , cpu.X));
                res = (val+1)&0xff;
                setFlags(res, flags);
                cpu.writeTo(add(Short.toUnsignedInt(op) , cpu.X) , (byte) res);
                cpu.PC += 3;
                return 7;

            default:
                throw new OpcodeException();
        }
    }

    public int inx(addressingMode A){
        int res;
        boolean[] flags = {false, false , true , true};

        res = add(cpu.X, 1)&0xff;
//        if(res>255) res=0;
//        //System.out.println("X is now " + res);
        setFlags(res , flags);
        cpu.X = (byte) res;
        cpu.PC += 1;
        return 2;
    }

    public int iny(addressingMode A){
        int res;
        boolean[] flags = {false, false , true , true};

        res = add(cpu.Y, 1)&0xff;
        setFlags(res , flags);
        cpu.Y = (byte) res;
        cpu.PC += 1;
        return 2;
    }

    public int jmp(addressingMode A , short op){
        int loc , val,val_high,val_low;
        switch(A){
            case Absolute:
                cpu.PC += 3;
                //cpu.PC = (short)(op<<8 + op>>8);
                cpu.PC = op;
                return 3;

            case Indirect:
                cpu.PC += 3;
                int op_value = Short.toUnsignedInt(op);
                int op_high = op_value&0xff00;
                int op_low = op_value&0xff;
                int index = (op_low + 1) & 0xFF;
                val_high = (cpu.getData((op_high + index) & 0xFFFF) & 0xFF) << 8;
                val_low = cpu.getData(op);
                val = val_high + val_low;
                dump_at(((op_value+1)&op_value)-5);
                cpu.PC = (short) val;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int jsr(addressingMode A , short op){
//        //System.out.println("Setting PC to " + Integer.toHexString(Short.toUnsignedInt(op)));
//        //System.out.println("Stack Pointer is "+ Integer.toHexString(cpu.SP));
        cpu.PC += 2;

        cpu.push((byte) ((cpu.PC>>8)&0xff));

        cpu.push((byte) (((cpu.PC)& 0xff)&0xff));
        cpu.PC += 3;

//        //System.out.println("HIGH BYTE IS "+ Integer.toHexString(cpu.PC>>8));
//        //System.out.println("LOW BYTE IS "+ Integer.toHexString(cpu.PC & 0xff));
        cpu.PC = op;
//        //System.out.println("PC is now " + Integer.toHexString(Short.toUnsignedInt(cpu.PC)));
        return 6;
    }

    private void ram_dump() {
//        //System.out.println("RAM DUMP:");
        for(int y=-5 ; y<10 ;y++) {
            String pc = Integer.toHexString(Short.toUnsignedInt(cpu.PC)+y);
            String inst = Integer.toHexString(Byte.toUnsignedInt(cpu.cpu_memory[y+Short.toUnsignedInt(cpu.PC)]));
//            //System.out.println("0x" + pc + " : 0x" +inst);
        }
    }

    public int lda(addressingMode A, byte op){
        int val , loc = Byte.toUnsignedInt(op) , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);
                dump_at(0x0180-5);
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(Byte.toUnsignedInt(op));
                dump_at(Byte.toUnsignedInt(op));
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                loc = add(loc , cpu.X);
                int address = add(op&0xff, cpu.X)&0xff;
                val = cpu.getData(address);
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                addr = add(cpu.X, op);
                addr = addr & 0xff;
                dump_at(0xff-5);
                int loc_high = cpu.getData((addr + 1) & 0xFF) & 0xFF;
                int loc_low  = cpu.getData(addr & 0xFF) & 0xFF;
                loc  = (loc_high << 8) | loc_low;

                val = cpu.getData(loc);
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                addr = Byte.toUnsignedInt(op);
                dump_at(addr-5);

                int highByte = cpu.getData((addr + 1) & 0xFF) & 0xFF;
                int lowByte  = cpu.getData(addr & 0xFF) & 0xFF;

                loc = (highByte << 8) | lowByte;

                loc = loc & 0xffff;
                val = cpu.getData(add(loc , cpu.Y));
                dump_at(loc+cpu.Y-5);
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int lda(addressingMode A , short op){
        int val , loc = Short.toUnsignedInt(op), addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                dump_at(0x0180-5);
                val = cpu.getData(Short.toUnsignedInt(op));
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                loc = add(loc,cpu.X);
                val = cpu.getData(loc);
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                loc = add(loc, cpu.Y);
                val = cpu.getData(loc);
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int ldx(addressingMode A , byte op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);
                setFlags(val, flags);
                cpu.X = (byte) val;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                setFlags(val, flags);
                cpu.X = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op&0xff, cpu.Y)&0xff;
                val = cpu.getData(address);
                setFlags(val, flags);
                cpu.X = (byte) val;
                cpu.PC += 2;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int ldx(addressingMode A , short op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op);
                setFlags(val, flags);
                cpu.X = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.getData(addShort(op, cpu.Y));
                setFlags(val, flags);
                cpu.X = (byte) val;
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int ldy(addressingMode A , byte op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);
                setFlags(val, flags);
                cpu.Y = (byte) val;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                setFlags(val, flags);
                cpu.Y = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op&0xff, cpu.X)&0xff;
                val = cpu.getData(address);
                setFlags(val, flags);
                cpu.Y = (byte) val;
                cpu.PC += 2;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int ldy(addressingMode A , short op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op);
                setFlags(val, flags);
                cpu.Y = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.getData(addShort(op, cpu.X));
                setFlags(val, flags);
                cpu.Y = (byte) val;
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int lsr(addressingMode A){
        boolean[] flags = {true , false , true , true};
        int val;
        val = Byte.toUnsignedInt(cpu.Accumulator);
        int carry = val%2;
        val >>= 1;
        setFlags(val , flags);
        cpu.Status |= (byte) carry;
        cpu.Accumulator = (byte)val;
        cpu.PC += 1;
        return 2;
    }

    public int lsr(addressingMode A , byte op){
        int loc , val, carry;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                carry = val%2;
                val >>= 1;
                setFlags(val , flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(Byte.toUnsignedInt(op), (byte)val);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                int address = add(op&0xff, cpu.X)&0xff;
                val = cpu.getData(address);
                carry = val%2;
                val >>= 1;
                setFlags(val , flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(add(cpu.X, op), (byte) val);
                cpu.PC += 2;
                return 6;

            default:
                throw new OpcodeException();
        }
    }

    public int lsr(addressingMode A , short op){
        int loc , val, carry;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op);
                carry = val & 0x01;
                val >>= 1;
                setFlags(val , flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(Short.toUnsignedInt(op) , (byte) val);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                val = cpu.getData(addShort(op ,cpu.X));
                carry = val%2;
                val >>= 1;
                setFlags(val , flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(addShort(op , cpu.X), (byte) val);
                cpu.PC += 3;
                return 7;

            default:
                throw new OpcodeException();
        }
    }

    public int nop(addressingMode A){
        cpu.PC += 1;
        return 2;
    }

    public int ora(addressingMode A , byte op){
        int val , loc ,addr;

        boolean[] flags = {false, false, true , true};
        switch(A){
            case Immediate:
                val = cpu.Accumulator | op;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.getData(op & 0xff) | cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                val = cpu.getData(address) | cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                loc = add(op , cpu.X);
                loc = loc&0xff;
                int low  = cpu.getData(loc & 0xFF) & 0xFF;
                int high = cpu.getData((loc + 1) & 0xFF) & 0xFF;
                addr = (high << 8) + low;
                val = cpu.Accumulator | cpu.getData(addr);
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                low = cpu.getData(op & 0xFF) & 0xFF;
                high = cpu.getData((op + 1) & 0xFF) & 0xFF;
                addr = (high << 8) + low;

                addr = (addr+(cpu.Y & 0xff))&0xffff;

                val = cpu.getData(addr) | (cpu.Accumulator & 0xff);
                val = val&0xff;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int ora(addressingMode A , short op){
        int val;
        boolean[] flags = {false , false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op) | cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.getData(addShort(op , cpu.X)) | cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.getData(addShort(op , cpu.Y)) | cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int pha(addressingMode A){
        cpu.push(cpu.Accumulator);
        cpu.PC += 1;
        return 3;
    }

    public int php(addressingMode A){
        cpu.push((byte) (cpu.Status | 0x30));
        cpu.PC += 1;
        return 3;
    }

    public int pla(addressingMode A){
//        //System.out.println("SP is " + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)));
//        cpu.SP++;
//        cpu.Accumulator = (byte) (cpu.stack[Byte.toUnsignedInt(cpu.SP)] & 0xff);
        dump_at(0x100+ Byte.toUnsignedInt(cpu.SP)-5);
        cpu.Accumulator = cpu.pop();
//        //System.out.println("Value at SP was 0x" + Integer.toHexString(Byte.toUnsignedInt(cpu.stack[Byte.toUnsignedInt(cpu.SP)])));
        boolean[] flags= {false , false , true , true};
        setFlags(Byte.toUnsignedInt(cpu.Accumulator) & 0xff ,flags);
        cpu.PC += 1;
        return 4;
    }

    public int plp(addressingMode A){
//        cpu.SP++;
//        cpu.Status = (byte) (cpu.stack[Byte.toUnsignedInt(cpu.SP)] & 0xff);
        cpu.Status = cpu.pop();
        cpu.Status |= 0x20;
        cpu.PC += 1;
        return 4;
    }

    public int rol(addressingMode A){
        int val, carry;
        boolean[] flags = {true , false , true , true};

        val = Byte.toUnsignedInt(cpu.Accumulator);
        carry = ((val & 0x80) != 0) ? 1 : 0;
        val <<= 1;
        if(getCarry())
            val |= 1 ;
        else
            val |= 0;

        setFlags(val,flags);
        cpu.Status |= (byte) carry;
        cpu.Accumulator = (byte) val;
        cpu.PC += 1;
        return 2;
    }

    public int rol(addressingMode A , byte op){
        int loc , val, carry;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                carry = ((val & 0x80) != 0) ? 1 : 0;
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(Byte.toUnsignedInt(op), (byte) val);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                val = cpu.getData(address);
                carry = ((val & 0x80) != 0) ? 1 : 0;
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(add(cpu.X,op), (byte) val);
                cpu.PC += 2;
                return 6;

            default:
                throw new OpcodeException();
        }
    }

    public int rol(addressingMode A , short op){
        int val , loc, carry;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op);
                carry = ((val & 0x80) != 0) ? 1 : 0;
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(Short.toUnsignedInt(op), (byte) val);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                val = cpu.getData(addShort(op, cpu.X));
                carry = ((val & 0x80) != 0) ? 1 : 0;
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(addShort(op , cpu.X), (byte) val);
                cpu.PC += 3;
                return 7;

            default:
                throw new OpcodeException();
        }
    }

    public int ror(addressingMode A){
        int val, carry;
        boolean[] flags = {true , false , true , true};

        val = Byte.toUnsignedInt(cpu.Accumulator);
        carry = val%2;
        val >>= 1;
        if(getCarry())
            val |= 0b10000000 ;
        else
            val |= 0;

        setFlags(val,flags);
        cpu.Status |= (byte) carry;
        cpu.Accumulator = (byte) val;
        cpu.PC += 1;
        return 2;
    }

    public int ror(addressingMode A , byte op){
        int loc , val, carry;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                carry = val%2;
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(Byte.toUnsignedInt(op), (byte) val);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                val = cpu.getData(address);
                carry = val%2;
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(add(cpu.X,op), (byte) val);
                cpu.PC += 2;
                return 6;

            default:
                throw new OpcodeException();
        }
    }

    public int ror(addressingMode A , short op){
        int val , loc, carry;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case Absolute:
                val = cpu.getData(op);
                carry = val%2;
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(Short.toUnsignedInt(op), (byte) val);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                val = cpu.getData(addShort(op , cpu.X));
                carry = val%2;
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
                cpu.writeTo(addShort(op , cpu.X) , (byte) val);
                cpu.PC += 3;
                return 7;

            default:
                throw new OpcodeException();
        }
    }

    public int rti(addressingMode A){

        dump_at(0x0100 + Byte.toUnsignedInt(cpu.SP) - 5);
        cpu.Status = cpu.pop();

//        cpu.PC = (short) (Byte.toUnsignedInt(cpu.pop()) + (Byte.toUnsignedInt(cpu.pop())<<8));
        int low = cpu.pop()&0xff;
        int high = ((cpu.pop()&0xff) << 8);
        cpu.PC = (short) ((high & 0xff00) | low);
        return 6;
    }

    public int rts(addressingMode A){
//        cpu.PC += 2;
//        cpu.SP += 1;
////        //System.out.println("Stack Pointer is :" + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)+1));
//        cpu.PC = (short) (cpu.stack[Byte.toUnsignedInt(cpu.SP)] + Byte.toUnsignedInt(cpu.stack[add(cpu.SP,1)])<<8 );

       int low  = ((cpu.pop()&0xff));
       int high =  (((cpu.pop())<<8)&0xff00);

////        //System.out.println("Value at " + Integer.toHexString(cpu.SP) + " " + Integer.toHexString(cpu.stack[Byte.toUnsignedInt(cpu.SP)]));
////        //System.out.println("Returning back to " + Integer.toHexString(cpu.stack[add(cpu.SP,1)]<<8));

//        //System.out.println("Value at " + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)-1) + " " + Integer.toHexString(cpu.stack[Byte.toUnsignedInt((byte) (cpu.SP-1))]));
//        //System.out.println("Returning back to " + Integer.toHexString(cpu.stack[Byte.toUnsignedInt(cpu.SP)]<<8));

//        cpu.SP += 1;
//        int high = cpu.stack[Byte.toUnsignedInt(cpu.SP)]<<8;
//        int low = cpu.stack[sub(cpu.SP,1)]&0xff;


//        //System.out.println("Low 0x" + Integer.toHexString(low) + " High 0x"+ Integer.toHexString(high));
//        //System.out.println("Returning to " + Integer.toHexString(low+high+1));
        cpu.PC = (short) (low+high);
        cpu.PC += 1;

//        //System.out.println("Returning to " + Integer.toHexString(cpu.PC&0xffff));
//        cpu.PC += 2;
//        //System.out.println("Stack Pointer is :" + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)));
        return 6;
    }

    public void calculateBorrow(int a , int memory, int carry){
            int b = memory +carry;
//            //System.out.println("M + ~C is " + Integer.toHexString(memory));
            int m = (a & 0x80) != 0 ? 1 : 0;
            int n = (b & 0x80) != 0 ? 1 : 0;
//            //System.out.println("M is "+ m +" N is "+ n);
            int c = a-b;
//            //System.out.println("C is " + c);
            int s = (c & 0x80) != 0 ? 1 : 0;
//            //System.out.println("S is "+ s);
//            //System.out.println("Status is " + Integer.toHexString(cpu.Status));

            if(m==0 && n==0 && s==1)
                cpu.Status &= (byte) 0xfe;
            else
                cpu.Status |= 1;

            if( ( ((a ^ c)&0x80) != 0) && ( ((a ^ memory)&0x80) != 0 ))
                setOverflow();
            else
                clearOverflow();

//            //System.out.println("Status is " + Integer.toHexString(cpu.Status));

    }

    public int sbc(addressingMode A , byte op){
        int val , loc , carry;
        if(!getCarry())
            carry = 1;
        else
            carry = 0;
        boolean[] flags = {false , true, true , true};
        switch(A){

            case Immediate:
                val = cpu.Accumulator;
                val -= op;
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, op,carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = Byte.toUnsignedInt(cpu.Accumulator);
                val -= cpu.getData(op & 0xff);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[Byte.toUnsignedInt(op)],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                val = Byte.toUnsignedInt(cpu.Accumulator);
                val -= cpu.getData(address);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[add(cpu.X, op)],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                int addr = add(cpu.X, op) & 0xff;
                int highByte = cpu.getData((addr + 1) & 0xFF) & 0xFF;
                int lowByte  = cpu.getData(addr & 0xFF) & 0xFF;

                loc = (highByte << 8) | lowByte;

                val = Byte.toUnsignedInt(cpu.Accumulator);
                val -= cpu.getData(loc);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[loc],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                highByte = cpu.cpu_memory[(Byte.toUnsignedInt(op) + 1) & 0xFF] & 0xFF;
                lowByte  = cpu.cpu_memory[Byte.toUnsignedInt(op) & 0xFF] & 0xFF;

                loc = (highByte << 8) | lowByte;

                val = cpu.Accumulator;
                val -= cpu.getData(add(loc , cpu.Y));
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.getData(add(loc , cpu.Y)),carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int sbc(addressingMode A , short op){
        int val , loc , carry;
        if(!getCarry())
            carry = 1;
        else
            carry = 0;
        boolean[] flags = {false , true , true , true};
        switch(A){
            case Absolute:
                val = cpu.Accumulator;
                val -= cpu.getData(op);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[Short.toUnsignedInt(op)],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.Accumulator;
                val -= cpu.getData(addShort(op, cpu.X));
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.getData(addShort(op, cpu.X)),carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.Accumulator & 0xff;
                val -= cpu.getData(addShort(op, cpu.Y));
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator,  cpu.getData(addShort(op, cpu.Y)) ,carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int sec(addressingMode A){
        setCarry();
        cpu.PC += 1;
        return 2;
    }

    public int sed(addressingMode A){
        setDecimal();
        cpu.PC += 1;
        return 2;
    }

    public int sei(addressingMode A){
        cpu.Status |= 0b00000100;
        cpu.PC += 1;
        return 2;
    }

    public int sta(addressingMode A , byte op){
        int val , loc;
        switch(A){
            case ZeropageAbs:
                loc = Byte.toUnsignedInt(op);
                cpu.writeTo(loc, cpu.Accumulator);
                dump_at(0);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                loc = add(op,cpu.X)&0xff;
                cpu.writeTo(loc, cpu.Accumulator);
                cpu.PC += 2;
                dump_at(0);
                return 4;

            case PreIndirectX:
                int addr = add(op , cpu.X) & 0xff;
//                int loc_high = ((cpu.cpu_memory[(addr+1)&0xff]<<8)&0xff00);
//                int loc_low = (cpu.cpu_memory[addr] & 0xff);

                int loc_high = ((cpu.getData((addr+1)&0xff)<<8)&0xff00);
                int loc_low = (cpu.getData(addr) & 0xff);

                loc = ( loc_high + loc_low);
                cpu.writeTo(loc, cpu.Accumulator);
                cpu.PC += 2;
                dump_at(loc-5);
                return 6;

            case PostIndirectY:
//                int low  = cpu.cpu_memory[op & 0xFF] & 0xFF;
//                int high = cpu.cpu_memory[(op + 1) & 0xFF] & 0xFF;

                int low  = cpu.getData(op & 0xFF) & 0xFF;
                int high = cpu.getData((op + 1) & 0xFF) & 0xFF;

                loc = (high << 8) + low;

                loc = add(loc,cpu.Y);
                cpu.writeTo(loc, cpu.Accumulator);
                cpu.PC += 2;
                dump_at(0);
                return 6;

            default:
                throw new OpcodeException();
        }
    }

    public int sta(addressingMode A , short op){
        int val , loc = Short.toUnsignedInt(op);
        switch(A){
            case Absolute:
                cpu.writeTo(Short.toUnsignedInt(op), cpu.Accumulator);
                cpu.PC += 3;
                dump_at(Short.toUnsignedInt(op)-5);
                return 4;

            case AbsoluteX:
                loc = addShort(op , cpu.X);
                cpu.writeTo(loc , cpu.Accumulator);
                cpu.PC += 3;
                dump_at(0);
                return 5;

            case AbsoluteY:
                loc = addShort(op , cpu.Y);
                cpu.writeTo(addShort(op , cpu.Y),cpu.Accumulator);
                cpu.PC += 3;
                dump_at(0);
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int stx(addressingMode A , byte op){
        int loc ,val;
        switch(A){
            case ZeropageAbs:
                cpu.writeTo(Byte.toUnsignedInt(op), cpu.X);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op, cpu.Y)&0xff;
                cpu.writeTo(address, cpu.X);
                cpu.PC += 2;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int stx(addressingMode A , short op){
        cpu.writeTo(Short.toUnsignedInt(op), cpu.X);
        cpu.PC += 3;
        return 4;
    }

    public int sty(addressingMode A , byte op){
        int loc ,val;
        switch(A){
            case ZeropageAbs:
                val = cpu.getData(op & 0xff);
                cpu.writeTo(Byte.toUnsignedInt(op), cpu.Y);
                String yval = Integer.toHexString(Byte.toUnsignedInt(cpu.Y)) , valLoc = Integer.toHexString(Byte.toUnsignedInt(op));
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                int address = add(op, cpu.X)&0xff;
                cpu.writeTo(address, cpu.Y);
                cpu.PC += 2;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int sty(addressingMode A ,short op){
        int loc ,val;
        cpu.writeTo(Short.toUnsignedInt(op), cpu.Y);
        cpu.PC += 3;
        return 4;
    }

    public int tax(addressingMode A){
        boolean[] flags = {false , false, true , true};
        setFlags(cpu.Accumulator, flags);
        cpu.X = cpu.Accumulator;
        cpu.PC += 1;
        return 2;
    }

    public int tay(addressingMode A){
        boolean[] flags = {false , false, true , true};
        setFlags(cpu.Accumulator, flags);
        cpu.Y = cpu.Accumulator;
        cpu.PC += 1;
        return 2;
    }

    public int tsx(addressingMode A){
        boolean[] flags = {false , false, true , true};
        setFlags(cpu.SP, flags);
//        //System.out.println("Setting X : " + Integer.toHexString(cpu.SP));
        cpu.X = cpu.SP;
        cpu.PC += 1;
        return 2;
    }

    public int txa(addressingMode A){
        boolean[] flags = {false , false, true , true};
        setFlags(cpu.X, flags);
        cpu.Accumulator = cpu.X;
        cpu.PC += 1;
        return 2;
    }

    public int txs(addressingMode A){
        cpu.SP = cpu.X;
        cpu.PC += 1;
        return 2;
    }

    public int tya(addressingMode A){
        boolean[] flags = {false , false, true , true};
        setFlags(cpu.Y, flags);
        cpu.Accumulator = cpu.Y;
        cpu.PC += 1;
        return 2;
    }

    public int add(byte a , byte b){
        return ((Byte.toUnsignedInt(a) + Byte.toUnsignedInt(b)))&0xff;
    }
    public int  add(int a , byte b){
        return ((a + Byte.toUnsignedInt(b)));
    }

    public int add(byte a , int b){
        return ((b + Byte.toUnsignedInt(a)))&0xff;
    }

    public int addShort(short a, byte b){
        return ((a&0xffff) + (b&0xff)) & 0xffff;
    }
    public int sub(byte a , byte b){
        return Byte.toUnsignedInt(a) - Byte.toUnsignedInt(b);
    }
    public int sub(int a , byte b){
        return a - Byte.toUnsignedInt(b);
    }

    public int sub(byte a , int b){
        return Byte.toUnsignedInt(a) - b;
    }
    public short littleEndian(short A){
        return (short) ((int) A <<8 + (int) A >>8); //addition promotes shorts/bytes to int
    }
    public void setOverflow(){
        cpu.Status = (byte) ((cpu.Status | 0x40) & 0xff);
    }
    public void setNegative(){
        cpu.Status = (byte) ((cpu.Status | 0x80) & 0xff);
    }
    private void setBreak() {
          cpu.Status = (byte) ((cpu.Status | 0x10) & 0xff);
    }
    public void setZero(){
        cpu.Status = (byte) ((cpu.Status | 0x2) & 0xff);
    }
    private void setDecimal() {
          cpu.Status = (byte) ((cpu.Status | 0x8) & 0xff);
    }
    public void setCarry(){
        cpu.Status = (byte) ((cpu.Status | 0x1) & 0xff);
    }
    public void clearOverflow(){
        cpu.Status = (byte) (cpu.Status & 0xbf);
    }
    public void clearNegative(){
        int a = Byte.toUnsignedInt(cpu.Status);
        if((a>>7)%2==1) cpu.Status = (byte) sub(cpu.Status, 128);
    }
    public void clearZero(){
        cpu.Status = (byte) (cpu.Status & 0xfd);
    }
    private void clearDecimal() {
        int a = Byte.toUnsignedInt(cpu.Status);
        if((a>>3)%2==1) cpu.Status = (byte) sub(cpu.Status, 0b00001000);
    }
    public void clearCarry(){
        int a = Byte.toUnsignedInt(cpu.Status);
        if(a%2==1) cpu.Status = (byte) sub(cpu.Status, 0b00000001);
    }

    private void clearInterrupt() {
        int a = Byte.toUnsignedInt(cpu.Status);
        if((a>>2)%2==1) cpu.Status = (byte) sub(cpu.Status, 0b00000100);
    }
    public boolean getOverflow(){
        int a = Byte.toUnsignedInt(cpu.Status);
        return ((a>>6)%2==1);
    }
    public boolean getNegative(){
        int a = Byte.toUnsignedInt(cpu.Status);
        return((a>>7)%2==1);
    }
    public boolean getZero(){
        int a = Byte.toUnsignedInt(cpu.Status);
        return((a>>1)%2==1);
    }
    public boolean getCarry(){
        int a = Byte.toUnsignedInt(cpu.Status);
        return(a%2==1);
    }

    public void checkAndSet(int a , int b , int c){
        a = a&0xff;
        b = b&0xff;
        c = c&0xff;
//        //System.out.println("OP1 : " + a +" OP2 : " +b + " Res : " +c);
        int s1 = (a>>7) & 0x01 , s2 = (b>>7) & 0x01;
        int sign = (c>>7) & 0x01;
//        //System.out.println("M7 : " +s1 +" N7 : " + s2 +  " R7 : " + sign);
        if(s1==s2){
            if(s1!=sign) setOverflow();
            else clearOverflow();
        }
        else clearOverflow();
    }

    private void setFlags(int sum, boolean[] flags) {
//        //System.out.println("In FlagSetter , Sum is " + sum);
        if(flags[0] && sum>255 && !getCarry()) setCarry();
        if(flags[1] && (sum>127 || sum<-127) && !getOverflow()) setOverflow();
        if(flags[3] && (sum==0 || sum==256) && !getZero()) setZero();

        if(flags[0] && sum<=255 && getCarry()) clearCarry();
        if(flags[1] && (sum<=127 && sum>=-127) && getOverflow()) clearOverflow();
        if(flags[3] && (sum!=0 && sum!=256) && getZero()) clearZero();

        if(flags[2]){
            if(((sum & 0x80) >> 7)==1) setNegative();
            else clearNegative();
        }
    }

    public void dump_at(int a){
        for(int i=0 ; i<10 ; i++){
//            //System.out.println(Integer.toHexString((a+i)&0xffff) + " : 0x" + Integer.toHexString(Byte.toUnsignedInt((byte) (cpu.cpu_memory[(a+i)&0xffff] & 0xff))));
        }
    }

}
