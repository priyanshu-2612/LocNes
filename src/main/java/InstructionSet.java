package main.java;

import javax.swing.plaf.synth.SynthOptionPaneUI;

public class InstructionSet {

    CPU cpu;
    PPU ppu;

    InstructionSet(CPU cpu, PPU ppu){
        this.cpu = cpu;
        this.ppu = ppu;
    }
    public int adc(addressingMode A, byte op){
        int sum , loc , addr;
        boolean[] flags = {true , true , true , true};
        switch (A) {
            case Immediate:
                sum = add(op , (byte) (cpu.Status%2)); //op+C
                sum = add(sum , cpu.Accumulator); // op+C+Alda
                setFlags(sum , flags);
                checkAndSet(add(cpu.Accumulator,0),add(op,0) , sum);
                cpu.Accumulator = (byte)sum;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                dump_at(0);
                System.out.println("Added value is " + Integer.toHexString(cpu.getData(Byte.toUnsignedInt(op))));
                sum = add((cpu.getData(Byte.toUnsignedInt(op)) & 0xff), (byte) (Byte.toUnsignedInt(cpu.Status)%2));// here 2
//                sum = add(cpu.cpu_memory[Byte.toUnsignedInt(op)] , Byte.toUnsignedInt(cpu.Status)%2); //changing
                sum = add(sum, cpu.Accumulator);
                setFlags(sum , flags);
                checkAndSet(add(cpu.Accumulator,0), cpu.cpu_memory[Byte.toUnsignedInt(op)]&0xff , sum);//changing
                System.out.println("Sum is " + sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
//                int val = cpu.cpu_memory[add(op , cpu.X)]; //changing
                int val = cpu.getData(add(op , cpu.X));
                sum = add(val , (byte) (cpu.Status%2));
                sum = add(sum, cpu.Accumulator);
                setFlags(sum, flags);
                checkAndSet(add(cpu.Accumulator,0), val&0xff , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                loc = add(op , cpu.X);
                loc = loc&0xff;
                addr = ( (cpu.cpu_memory[(loc+1)&0xff]<<8)&0xff00) + (cpu.cpu_memory[loc]&0x00ff); // converting into little endian
                sum = add(cpu.cpu_memory[addr&0xffff] , (byte) (cpu.Status%2));
//                sum = add(cpu.getData(addr&0xfff) , (byte) (cpu.Status%2));
                sum = add(sum, cpu.Accumulator);
                setFlags(sum , flags);
                checkAndSet(add(cpu.Accumulator,0),add(cpu.cpu_memory[addr], 0) , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                addr = (cpu.cpu_memory[(Byte.toUnsignedInt(op)+1)&0xff]<<8) + cpu.cpu_memory[Byte.toUnsignedInt(op)&0xff];
                addr = add(cpu.Y, addr);
                sum = add(cpu.cpu_memory[addr], cpu.Status%2);
                sum = add(sum, cpu.Accumulator);
                setFlags(sum , flags);
                checkAndSet(add(cpu.Accumulator,0),add(cpu.cpu_memory[addr], 0) , sum);
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
                sum = add(cpu.cpu_memory[Short.toUnsignedInt(op)], (byte) (cpu.Status%2));
                sum = add(sum, cpu.Accumulator);
                setFlags(sum ,flags);
                checkAndSet(add(cpu.Accumulator,0),add(cpu.cpu_memory[Short.toUnsignedInt(op)], 0) , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                loc = add(op, cpu.X);
                sum = add(cpu.cpu_memory[loc] , cpu.Status%2);
                sum = add(sum, cpu.Accumulator);
                setFlags(sum, flags);
                checkAndSet(add(cpu.Accumulator,0),add(cpu.cpu_memory[loc], 0) , sum);
                cpu.Accumulator = (byte) sum;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                loc = add(op, cpu.Y);
                sum = add(cpu.cpu_memory[loc] , cpu.Status%2);
                sum = add(sum, cpu.Accumulator);
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
                System.out.println("0x" + Integer.toHexString(ac) + " & 0x" + Integer.toHexString(opint));
                System.out.println("Result of AND is "+ Integer.toBinaryString(val));
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.cpu_memory[Byte.toUnsignedInt(op)] & cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                val = cpu.cpu_memory[cpu.X+Byte.toUnsignedInt(op)] & cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                loc = add(op , cpu.X);
                loc = loc&0xff;
                addr = (cpu.cpu_memory[(loc+1)&0xff]<<8) + cpu.cpu_memory[loc];
                val = cpu.Accumulator & cpu.cpu_memory[addr];
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                addr = (cpu.cpu_memory[(Byte.toUnsignedInt(op)+1)&0xff]<<8) + cpu.cpu_memory[(Byte.toUnsignedInt(op))&0xff];
                addr = add(cpu.Y, addr);
                val = cpu.cpu_memory[addr] & cpu.Accumulator;
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
                val = cpu.cpu_memory[Short.toUnsignedInt(op)] & cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.cpu_memory[add(op , cpu.X)] & cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.cpu_memory[add(op , cpu.Y)] & cpu.Accumulator;
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
                val = cpu.cpu_memory[Byte.toUnsignedInt(op)]&0xff;
                System.out.println("Shifting 0x" + Integer.toHexString(val) + " one left");
                carry = ((val&0x80) == 0x80) ? 1: 0;
                System.out.println("Carry condition is "+ (val&0x80) + " and " + ((val&0x80)==0x80));
                val = (val << 1)&0xff;
                setFlags(val, flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[Byte.toUnsignedInt(op)] = (byte) val;
                cpu.writeTo(Byte.toUnsignedInt(op),(byte) val);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                val = cpu.cpu_memory[add(op, cpu.X)]&0xff;
                carry = ((val&0x80) == 0x80) ? 1: 0;
                val = (val << 1)&0xff;
                setFlags(val, flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[add(op, cpu.X)] = (byte) val;
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
//                addr = (cpu.cpu_memory[Short.toUnsignedInt(op)+1] << 8) + cpu.cpu_memory[Short.toUnsignedInt(op)];
                addr = Short.toUnsignedInt(op);
                val = cpu.cpu_memory[addr]&0xff;
                System.out.println("Shifting 0x" + Integer.toHexString(val) + " one left");
                carry = ((val & 0x80) == 0x80) ? 1 : 0;
                System.out.println("Carry condition is "+ (val&0x80) + " and " + ((val&0x80)==0x80));
                val = (val << 1) & 0xff;
                setFlags(val, flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[addr] = (byte) val;
                cpu.writeTo(addr,(byte)val);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                addr = add(op, cpu.X);
                val = cpu.cpu_memory[addr]&0xff;
                carry = ((val & 0x80) == 0x80) ? 1 : 0;
                val = (val << 1) & 0xff;
                setFlags(val, flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[addr] = (byte) val;
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
            System.out.println("Displacing by " + Integer.toHexString(Byte.toUnsignedInt(op)));
            System.out.println("Branching to 0x" + Integer.toHexString((cpu.PC+op)&0xffff));
//          cpu.PC = (short) add(cpu.PC, op);
            cpu.PC += op;  //signed addition
            System.out.println("PC is now " + Byte.toUnsignedInt(op));
        }
        return 2;
    }

    public int bit(addressingMode A , byte op) {

        int acc = Byte.toUnsignedInt((byte) (cpu.Accumulator & 0xff)), mem, val;
        mem = Byte.toUnsignedInt((byte) (cpu.cpu_memory[Byte.toUnsignedInt(op)] & 0xff));
        val = mem & acc;
        System.out.println("Memory value is " + Integer.toHexString(mem));
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
        mem = Byte.toUnsignedInt((byte) (cpu.cpu_memory[Short.toUnsignedInt(op)] & 0xff));
        val = mem & acc;
        System.out.println("Memory value is " + Integer.toHexString(mem));
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
//        cpu.stack[Byte.toUnsignedInt(cpu.SP)] = (byte) ((cpu.PC >> 8) & 0x00ff);
//        cpu.SP--;
        cpu.push((byte) ((cpu.PC >> 8) & 0x00ff));

//        cpu.stack[Byte.toUnsignedInt(cpu.SP)] = (byte) (cpu.PC & 0x00ff);
//        cpu.SP--;
        cpu.push((byte) (cpu.PC & 0x00ff));

        cpu.Status |= 0x10;  //set the B flag
        cpu.Status |= 0x4;  //set Interrupt disable

//        cpu.stack[Byte.toUnsignedInt(cpu.SP)] = cpu.Status;
//        cpu.SP--;
        cpu.push(cpu.Status);

        cpu.PC = (short) ((((cpu.cpu_memory[0xfffa+1]<<8)& 0xff00) + (cpu.cpu_memory[0xfffa])) & 0xffff);
        System.out.println("PC is now 0x" + Integer.toHexString(Short.toUnsignedInt(cpu.PC)));
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
//        cpu.stack[Byte.toUnsignedInt(cpu.SP)] = (byte) (cpu.PC >>> 8); //PC High
//        cpu.SP = (byte) (sub(cpu.SP , 1) & 0xff);
        cpu.push((byte) ((cpu.PC >> 8)&0xff));

//        cpu.stack[Byte.toUnsignedInt(cpu.SP)] = (byte) (cpu.PC); //PC Low
//        cpu.SP = (byte) (sub(cpu.SP , 1) & 0xff);
        cpu.push((byte) ((cpu.PC)&0xff));

        setBreak();

//        cpu.stack[Byte.toUnsignedInt(cpu.SP)] = cpu.Status;
//        cpu.SP = (byte) (sub(cpu.SP , 1) & 0xff);
        cpu.push(cpu.Status);

        int loc = (cpu.cpu_memory[0xffff] << 8) + cpu.cpu_memory[0xfffe];
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
        int res = r-op;
        System.out.println("Result is " + Integer.toHexString(res)+ " " + res);
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
//              val = Byte.toUnsignedInt(op);
//                val = op; //signed
//                if(val==acc) {
//                    setCarry();
//                    setZero();
//                }
//
//                if(acc<val) setNegative();
//                else clearNegative();
//
//                if(val <= acc) setCarry();
//                else clearCarry();
//
//
//                System.out.println("Comparing 0x" + Integer.toHexString(val) + " and 0x" + Integer.toHexString(acc));
//                System.out.println("Zero Flag is " + getZero());
//                System.out.println("Negative Flag is " + getNegative());
//                System.out.println("Carry Flag is " + getCarry());

                compare_set_flags(cpu.Accumulator,op);
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.cpu_memory[Byte.toUnsignedInt(op)];
//                if(val==acc) setZero();
//                else clearZero();
//
//                if(acc<val) setNegative();
//                else clearNegative();
//
//                if(val <= acc) setCarry();
//                else clearCarry();

                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                val = cpu.cpu_memory[add(cpu.X, op)];
//                if(val==acc) setZero();
//                else clearZero();
//
//                if(acc<val) setNegative();
//                else clearNegative();
//
//                if(val <= acc) setCarry();
//                else clearCarry();

                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                loc = add(op , cpu.X);
                loc = loc&0xff;
                addr = (cpu.cpu_memory[(loc+1)&0xff]<<8) + cpu.cpu_memory[loc];
                val = cpu.cpu_memory[addr];
//                if(val==acc) setZero();
//                else clearZero();
//
//                if(acc<val) setNegative();
//                else clearNegative();
//
//                if(val <= acc) setCarry();
//                else clearCarry();

                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                loc = (cpu.cpu_memory[(Byte.toUnsignedInt(op)+1)&0xff]<<8 )+ cpu.cpu_memory[Byte.toUnsignedInt(op)&0xff];
                val = cpu.cpu_memory[add(loc , cpu.Y)];
                System.out.println("Address is " + Integer.toHexString(loc) + " ");
//                if(val==acc) setZero();
//                else clearZero();
//
//                if(acc<val) setNegative();
//                else clearNegative();
//
//                if(val <= acc) setCarry();
//                else clearCarry();

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
                val = cpu.cpu_memory[Short.toUnsignedInt(op)];
//                if(val==acc) setZero();
//                else clearZero();
//
//                if(acc<val) setNegative();
//                else clearNegative();
//
//                if(val <= acc) setCarry();
//                else clearCarry();

                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.cpu_memory[add(cpu.X, op)];
//                if(val==acc) setZero();
//                else clearZero();
//
//                if(acc<val) setNegative();
//                else clearNegative();
//
//                if(val <= acc) setCarry();
//                else clearCarry();

                compare_set_flags(cpu.Accumulator,val);
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.cpu_memory[add(cpu.Y, op)];
//                if(val==acc) setZero();
//                else clearZero();
//
//                if(acc<val) setNegative();
//                else clearNegative();
//
//                if(val <= acc) setCarry();
//                else clearCarry();

                compare_set_flags(cpu.Accumulator,val);
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
                val = cpu.cpu_memory[Byte.toUnsignedInt(op)];

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
                return 3;

            default:
                throw new OpcodeException();
        }
    }

    public int cpx(addressingMode A,short op){
        int loc , val , x = cpu.X;
        val = cpu.cpu_memory[Short.toUnsignedInt(op)];

//        if(x==val) setZero();
//        else clearZero();
//
//        if(x >= val) setCarry();
//        else clearCarry();
//
//        if(x<val) setNegative();
//        else clearNegative();

        compare_set_flags(cpu.X,val);
        cpu.PC += 3;
        return 4;
    }

    public int cpy(addressingMode A , byte op){
        int loc , val , y = cpu.Y;
        switch(A){
            case Immediate:
                val = op;

//                if(y==val) setZero();
//                else clearZero();
//
//                if(y >= val) setCarry();
//                else clearCarry();
//
//                if(y<val) setNegative();
//                else clearNegative();

                compare_set_flags(cpu.Y,val);
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = cpu.cpu_memory[Byte.toUnsignedInt(op)];

//                if(y==val) setZero();
//                else clearZero();
//
//                if(y >= val) setCarry();
//                else clearCarry();
//
//                if(y<val) setNegative();
//                else clearNegative();

                compare_set_flags(cpu.Y,val);
                cpu.PC += 2;
                return 3;

            default:
                throw new OpcodeException();
        }
    }

    public int cpy(addressingMode A, short op){
        int loc , val , y = cpu.Y;
        val = cpu.cpu_memory[Short.toUnsignedInt(op)];

//        if(y==val) setZero();
//        else clearZero();
//
//        if(y >= val) setCarry();
//        else clearCarry();
//
//        if(y<val) setNegative();
//        else clearNegative();

        compare_set_flags(cpu.Y,val);
        cpu.PC += 3;
        return 4;
    }

    public int dec(addressingMode A , byte op){
        int val , loc;
        boolean[] flags = {false , false, true , true};
        switch(A){
            case ZeropageAbs:
//                loc = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                loc = Byte.toUnsignedInt(op);
                System.out.println("The val at " + loc + " is " + Byte.toUnsignedInt(cpu.cpu_memory[loc]));
//                cpu.cpu_memory[loc] = (byte) sub(cpu.cpu_memory[loc],1);
                cpu.writeTo(loc, (byte) sub(cpu.cpu_memory[loc],1) );
                System.out.println("And now it is " + Byte.toUnsignedInt(cpu.cpu_memory[loc]));
                setFlags(add(cpu.cpu_memory[loc],0) , flags);
                cpu.PC += 2;
                dump_at(0);
                return 5;

            case ZeropageIndexed:
//                loc = Byte.toUnsignedInt(cpu.cpu_memory[add(op, cpu.X)]);
                loc = add(op,cpu.X);
//                cpu.cpu_memory[loc] = (byte) sub(cpu.cpu_memory[loc],1);
                cpu.writeTo(loc,(byte) sub(cpu.cpu_memory[loc],1) );
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
//                loc = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op)]);
                loc = Short.toUnsignedInt(op);
//                cpu.cpu_memory[loc] = (byte) sub(cpu.cpu_memory[loc],1);
                cpu.writeTo(loc, (byte) sub(cpu.cpu_memory[loc],1));
                setFlags(add(cpu.cpu_memory[loc],0) , flags);
                cpu.PC += 3;
                dump_at(0);
                return 6;

            case AbsoluteX:
//                loc = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op) + Byte.toUnsignedInt(cpu.X)]);
                loc = Short.toUnsignedInt(op) + Byte.toUnsignedInt(cpu.X);
//                cpu.cpu_memory[loc] = (byte) sub(cpu.cpu_memory[loc],1);
                cpu.writeTo(loc,(byte) sub(cpu.cpu_memory[loc],1));
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
        System.out.println("X is " + Byte.toUnsignedInt(cpu.X));
        cpu.X = (byte) sub(cpu.X, 1);
        System.out.println("X is now " + Byte.toUnsignedInt(cpu.X));

        setFlags(Byte.toUnsignedInt(cpu.X) , flags);
        cpu.PC += 1;
        return 2;
    }

    public int dey(addressingMode A){
        boolean[] flags = {false , false, true , true};
        System.out.println("Y is " + Byte.toUnsignedInt(cpu.Y));
        cpu.Y = (byte) sub(cpu.Y, 1);
        System.out.println("Y is now " + Byte.toUnsignedInt(cpu.Y));

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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op , cpu.X)]);
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                int addr = add(cpu.X,op) & 0xff;
                loc = (cpu.cpu_memory[(addr+1)&0xff] <<8) + cpu.cpu_memory[addr];
                val = Byte.toUnsignedInt(cpu.cpu_memory[loc]);
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                loc = (cpu.cpu_memory[(Byte.toUnsignedInt(op)+1)&0xff]<<8) + cpu.cpu_memory[Byte.toUnsignedInt(op)&0xff];
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(loc, cpu.Y)]);
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
                val = cpu.cpu_memory[Short.toUnsignedInt(op)];
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.cpu_memory[add(Short.toUnsignedInt(op) , cpu.X)];
                res = cpu.Accumulator ^ val;
                cpu.Accumulator = (byte) (res);
                setFlags(res, flags);
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.cpu_memory[add(Short.toUnsignedInt(op) , cpu.Y)];
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
                val = cpu.cpu_memory[Byte.toUnsignedInt(op)]&0xff;
                res = (val + 1)&0xff;
                setFlags(res, flags);
                System.out.println("Setting 0x" + Integer.toHexString(Byte.toUnsignedInt(op)) + " to " + Integer.toHexString(val));
//                cpu.cpu_memory[Byte.toUnsignedInt(op)] = (byte)res;
                cpu.writeTo(Byte.toUnsignedInt(op) , (byte) res);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                val = cpu.cpu_memory[add(op, cpu.X)];
                res = (val + 1)&0xff;
                setFlags(res, flags);
//                cpu.cpu_memory[add(op, cpu.X)] = (byte)res;
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
                val = cpu.cpu_memory[Short.toUnsignedInt(op)];
                res = (val + 1)&0xff;
                setFlags(res, flags);
//                cpu.cpu_memory[Short.toUnsignedInt(op)] = (byte)res;
                cpu.writeTo(Short.toUnsignedInt(op), (byte) res);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                val = cpu.cpu_memory[add(Short.toUnsignedInt(op) , cpu.X)];
                res = (val+1)&0xff;
                setFlags(res, flags);
//                cpu.cpu_memory[add(Short.toUnsignedInt(op) , cpu.X)] = (byte)res;
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
        System.out.println("X is now " + res);
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
                val_high = (cpu.cpu_memory[op_high + ((op_low+1)&0xff)]<<8)&0xff00;
                val_low = cpu.cpu_memory[Short.toUnsignedInt(op)]&0xff;
                val = val_high + val_low;
                dump_at(((op_value+1)&op_value)-5);
                System.out.println("Op+1 is " + ((op_value+1)&op_value));
                cpu.PC = (short) val;
                return 5;

            default:
                throw new OpcodeException();
        }
    }

    public int jsr(addressingMode A , short op){
        System.out.println("Setting PC to " + Integer.toHexString(Short.toUnsignedInt(op)));
        System.out.println("Stack Pointer is "+ Integer.toHexString(cpu.SP));
        cpu.PC += 2;

        cpu.push((byte) ((cpu.PC>>8)&0xff));

        cpu.push((byte) (((cpu.PC)& 0xff)&0xff));
        cpu.PC += 3;

        System.out.println("HIGH BYTE IS "+ Integer.toHexString(cpu.PC>>8));
        System.out.println("LOW BYTE IS "+ Integer.toHexString(cpu.PC & 0xff));
        cpu.PC = op;
        System.out.println("PC is now " + Integer.toHexString(Short.toUnsignedInt(cpu.PC)));
        return 6;
    }

    private void ram_dump() {
        System.out.println("RAM DUMP:");
        for(int y=-5 ; y<10 ;y++) {
            String pc = Integer.toHexString(Short.toUnsignedInt(cpu.PC)+y);
            String inst = Integer.toHexString(Byte.toUnsignedInt(cpu.cpu_memory[y+Short.toUnsignedInt(cpu.PC)]));
            System.out.println("0x" + pc + " : 0x" +inst);
        }
    }

    public int lda(addressingMode A, byte op){
        int val , loc = Byte.toUnsignedInt(op) , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);
                //val = Integer.parseInt(Integer.toString(Byte.toUnsignedInt(op)),16);
                dump_at(0x0180-5);
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
//                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                val = cpu.getData(Byte.toUnsignedInt(op));
                dump_at(Byte.toUnsignedInt(op));
                System.out.println("Loading value " + val+ " in accumulator");
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                loc = add(loc , cpu.X);
//                if(0x2000 <= loc && loc <= 0x2007){
//                    cpu.Accumulator = ppu.cpuRead((short) (loc & 0xffff));
//                    setFlags(Byte.toUnsignedInt(cpu.Accumulator),  flags);
//                    cpu.PC += 2;
//                    return 4;
//                }
//                val = cpu.cpu_memory[add(cpu.X, op)];
                val = cpu.getData(add(cpu.X,op));
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                addr = add(cpu.X, op);
                addr = addr & 0xff;
                System.out.println("Address is " + Integer.toHexString(addr));
                dump_at(0xff-5);
                System.out.println("At 0x100 : " + Integer.toHexString(cpu.cpu_memory[0x100]));
//                int loc_high = ((cpu.cpu_memory[(addr+1)&0xff] << 8)&0xff00);
//                int loc_low = cpu.cpu_memory[addr]&0x00ff; //changing
                int loc_high = ((cpu.getData((addr+1)&0xff) << 8)&0xff00);
                int loc_low = cpu.getData(addr)&0x00ff;

                loc = ( loc_high + loc_low );
//                if(0x2000 <= loc && loc <= 0x2007){
//                    cpu.Accumulator = ppu.cpuRead((short) (loc & 0xffff));
//                    setFlags(Byte.toUnsignedInt(cpu.Accumulator),  flags);
//                    cpu.PC += 2;
//                    return 6;
//                }
//                val = cpu.cpu_memory[loc];
                val = cpu.getData(loc);
                System.out.println("Loc_High " + Integer.toHexString(loc_high) + " Loc_low "+ Integer.toHexString(loc_low));
                System.out.println("Value at "+ Integer.toHexString(loc) +" is " + Integer.toHexString(val));
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                addr = Byte.toUnsignedInt(op);
                dump_at(addr-5);
                loc_high = (cpu.cpu_memory[(addr+1)&0xff] << 8)&0xff00;
                loc_low = cpu.cpu_memory[addr&0xff]&0x00ff;
                loc = (loc_high+ loc_low);
                System.out.println("Loc_High is " + Integer.toHexString(loc_high) +" Loc_Low is " + Integer.toHexString(loc_low));
                loc = loc & 0xffff;
//                val = cpu.cpu_memory[add(loc , cpu.Y)&0xffff]&0xff; // here
                val = cpu.getData(add(loc , cpu.Y));
                dump_at(loc+cpu.Y-5);
                System.out.println("Loading data from " + Integer.toHexString(add(loc , cpu.Y)));
                System.out.println("Stored "  + Integer.toHexString(val) +" in Accumulator");
//                loc = add(loc , cpu.Y);
//                if(0x2000 <= loc && loc <= 0x2007){
//                    cpu.Accumulator = ppu.cpuRead((short) (loc & 0xffff));
//                    setFlags(Byte.toUnsignedInt(cpu.Accumulator),  flags);
//                    cpu.PC += 2;
//                    return 5;
//                }
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
//                if(0x2000 <= loc && loc <= 0x2007){
//                    cpu.Accumulator = ppu.cpuRead(op);
//                    setFlags(Byte.toUnsignedInt(cpu.Accumulator),  flags);
//                    cpu.PC += 3;
//                    return 4;
//                }
                dump_at(0x0180-5);
//                val = cpu.cpu_memory[Short.toUnsignedInt(op)]; // here
                val = cpu.getData(Short.toUnsignedInt(op));
                System.out.println("Loading value " + Integer.toHexString(val)+ " in accumulator");
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                loc = add(loc,cpu.X);
//                if(0x2000 <= loc && loc <= 0x2007){
//                    cpu.Accumulator = ppu.cpuRead((short) (loc & 0xffff));
//                    setFlags(Byte.toUnsignedInt(cpu.Accumulator),  flags);
//                    setFlags(Byte.toUnsignedInt(cpu.Accumulator),  flags);
//                    cpu.PC += 3;
//                    return 4;
//                }
//                val = cpu.cpu_memory[add(cpu.X, loc)];
//                val = cpu.getData(add(cpu.X, loc));
                val = cpu.getData(loc);
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                loc = add(loc, cpu.Y);
//                if(0x2000 <= loc && loc <= 0x2007){
//                    cpu.Accumulator = ppu.cpuRead((short) (loc & 0xffff));
//                    setFlags(Byte.toUnsignedInt(cpu.Accumulator),  flags);
//                    cpu.PC += 3;
//                    return 4;
//                }
//                val = cpu.cpu_memory[add(cpu.Y, op)&0xffff]; // here
//                val = cpu.getData(add(cpu.Y, op));
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
                //val = Integer.parseInt(Integer.toString(Byte.toUnsignedInt(op)),16);
                setFlags(val, flags);
                cpu.X = (byte) val;
                System.out.println("Loading value " + val + " in X : " + Byte.toUnsignedInt(cpu.X));
//                ram_dump();
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                setFlags(val, flags);
                cpu.X = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(cpu.Y, op)]);
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op)]);
                setFlags(val, flags);
                cpu.X = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(cpu.Y, op)]);
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
                //val = Integer.parseInt(Integer.toString(Byte.toUnsignedInt(op)),16);
                setFlags(val, flags);
                cpu.Y = (byte) val;
                System.out.println("Loading value " + val + " in Y : " + Byte.toUnsignedInt(cpu.Y));
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                setFlags(val, flags);
                cpu.Y = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(cpu.X, op)]);
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op)]);
                setFlags(val, flags);
                cpu.Y = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(cpu.X, op)]);
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                carry = val%2;
                val >>= 1;
                setFlags(val , flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[Byte.toUnsignedInt(op)] = (byte)val;
                cpu.writeTo(Byte.toUnsignedInt(op), (byte)val);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op, cpu.X)]);
                carry = val%2;
                val >>= 1;
                setFlags(val , flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[add(cpu.X, op)] = (byte)val;
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op)]);
                carry = val%2;
                val >>= 1;
                setFlags(val , flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[Short.toUnsignedInt(op)] = (byte)val;
                cpu.writeTo(Short.toUnsignedInt(op) , (byte) val);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op ,cpu.X)]);
                carry = val%2;
                val >>= 1;
                setFlags(val , flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[add(op , cpu.X)] = (byte)val;
                cpu.writeTo(add(op , cpu.X), (byte) val);
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]) | cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                val = Byte.toUnsignedInt(cpu.cpu_memory[cpu.X+Byte.toUnsignedInt(op)]) | cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                loc = add(op , cpu.X);
                loc = loc&0xff;
                addr = (cpu.cpu_memory[(loc+1)&0xff]<<8) + cpu.cpu_memory[loc];
                val = cpu.Accumulator | cpu.cpu_memory[addr];
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                addr = (cpu.cpu_memory[(Byte.toUnsignedInt(op)+1)&0xff]<<8) + cpu.cpu_memory[Byte.toUnsignedInt(op)&0xff];
                addr = add(cpu.Y, addr);
                val = Byte.toUnsignedInt(cpu.cpu_memory[addr]) | cpu.Accumulator;
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op)]) | cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op , cpu.X)]) | cpu.Accumulator;
                setFlags(val , flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op , cpu.Y)]) | cpu.Accumulator;
                setFlags(val, flags);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int pha(addressingMode A){
//        cpu.stack[Byte.toUnsignedInt(cpu.SP)] = cpu.Accumulator;
//        cpu.SP = (byte) (sub(cpu.SP,1) & 0xff);
        cpu.push(cpu.Accumulator);
        cpu.PC += 1;
        System.out.println("Running PHA");
        return 3;
    }

    public int php(addressingMode A){
//        cpu.stack[Byte.toUnsignedInt(cpu.SP)] = cpu.Status;
//        cpu.SP = (byte) (sub(cpu.SP,1) & 0xff);
        cpu.push(cpu.Status);
        cpu.PC += 1;
        return 3;
    }

    public int pla(addressingMode A){
        System.out.println("SP is " + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)));
//        cpu.SP++;
//        cpu.Accumulator = (byte) (cpu.stack[Byte.toUnsignedInt(cpu.SP)] & 0xff);
        dump_at(0x100+ Byte.toUnsignedInt(cpu.SP)-5);
        cpu.Accumulator = cpu.pop();
        System.out.println("Value at SP was 0x" + Integer.toHexString(Byte.toUnsignedInt(cpu.stack[Byte.toUnsignedInt(cpu.SP)])));
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                carry = ((val & 0x80) != 0) ? 1 : 0;
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[Byte.toUnsignedInt(op)] = (byte) val;
                cpu.writeTo(Byte.toUnsignedInt(op), (byte) val);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op , cpu.X)]);
                carry = ((val & 0x80) != 0) ? 1 : 0;
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[add(cpu.X,op)] = (byte) val;
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op)]);
                carry = ((val & 0x80) != 0) ? 1 : 0;
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[Short.toUnsignedInt(op)] = (byte) val;
                cpu.writeTo(Short.toUnsignedInt(op), (byte) val);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op , cpu.X)]);
                carry = ((val & 0x80) != 0) ? 1 : 0;
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[add(op , cpu.X)] = (byte) val;
                cpu.writeTo(add(op , cpu.X), (byte) val);
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                carry = val%2;
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[Byte.toUnsignedInt(op)] = (byte) val;
                cpu.writeTo(Byte.toUnsignedInt(op), (byte) val);
                cpu.PC += 2;
                return 5;

            case ZeropageIndexed:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op , cpu.X)]);
                carry = val%2;
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[add(cpu.X,op)] = (byte) val;
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
                val = Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op)]);
                carry = val%2;
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[Short.toUnsignedInt(op)] = (byte) val;
                cpu.writeTo(Short.toUnsignedInt(op), (byte) val);
                cpu.PC += 3;
                return 6;

            case AbsoluteX:
                val = Byte.toUnsignedInt(cpu.cpu_memory[add(op , cpu.X)]);
                carry = val%2;
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                cpu.Status |= (byte) carry;
//                cpu.cpu_memory[add(op , cpu.X)] = (byte) val;
                cpu.writeTo(add(op , cpu.X) , (byte) val);
                cpu.PC += 3;
                return 7;

            default:
                throw new OpcodeException();
        }
    }

    public int rti(addressingMode A){

        System.out.println("SP is " + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)));
//        cpu.SP++;
//        cpu.Status = cpu.stack[Byte.toUnsignedInt(cpu.SP)];
        dump_at(0x0100 + Byte.toUnsignedInt(cpu.SP) - 5);
        cpu.Status = cpu.pop();

//        cpu.SP++;
//        cpu.PC = (short) ( (cpu.stack[Byte.toUnsignedInt(cpu.SP)]<<8) + cpu.stack[add(cpu.SP,1)]);
//        cpu.SP++;

        cpu.PC = (short) (Byte.toUnsignedInt(cpu.pop()) + (Byte.toUnsignedInt(cpu.pop())<<8));
//        cpu.PC += 1;
        return 6;
    }

    public int rts(addressingMode A){
//        cpu.PC += 2;
//        cpu.SP += 1;
//        System.out.println("Stack Pointer is :" + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)+1));
//        cpu.PC = (short) (cpu.stack[Byte.toUnsignedInt(cpu.SP)] + Byte.toUnsignedInt(cpu.stack[add(cpu.SP,1)])<<8 );

       int low  = ((cpu.pop()&0xff));
       int high =  (((cpu.pop())<<8)&0xff00);

//        System.out.println("Value at " + Integer.toHexString(cpu.SP) + " " + Integer.toHexString(cpu.stack[Byte.toUnsignedInt(cpu.SP)]));
//        System.out.println("Returning back to " + Integer.toHexString(cpu.stack[add(cpu.SP,1)]<<8));

        System.out.println("Value at " + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)-1) + " " + Integer.toHexString(cpu.stack[Byte.toUnsignedInt((byte) (cpu.SP-1))]));
        System.out.println("Returning back to " + Integer.toHexString(cpu.stack[Byte.toUnsignedInt(cpu.SP)]<<8));

//        cpu.SP += 1;
//        int high = cpu.stack[Byte.toUnsignedInt(cpu.SP)]<<8;
//        int low = cpu.stack[sub(cpu.SP,1)]&0xff;


        System.out.println("Low 0x" + Integer.toHexString(low) + " High 0x"+ Integer.toHexString(high));
        System.out.println("Returning to " + Integer.toHexString(low+high+1));
        cpu.PC = (short) (low+high);
        cpu.PC += 1;

        System.out.println("Returning to " + Integer.toHexString(cpu.PC&0xffff));
//        cpu.PC += 2;
        System.out.println("Stack Pointer is :" + Integer.toHexString(Byte.toUnsignedInt(cpu.SP)));
        return 6;
    }

    public void calculateBorrow(int a , int memory, int carry){
            int b = memory +carry;
            System.out.println("M + ~C is " + Integer.toHexString(memory));
            int m = (a & 0x80) != 0 ? 1 : 0;
            int n = (b & 0x80) != 0 ? 1 : 0;
            System.out.println("M is "+ m +" N is "+ n);
            int c = a-b;
            System.out.println("C is " + c);
            int s = (c & 0x80) != 0 ? 1 : 0;
            System.out.println("S is "+ s);
            System.out.println("Status is " + Integer.toHexString(cpu.Status));

            if(m==0 && n==0 && s==1)
                cpu.Status &= (byte) 0xfe;
            else
                cpu.Status |= 1;

            if( ( ((a ^ c)&0x80) != 0) && ( ((a ^ memory)&0x80) != 0 ))
                setOverflow();
            else
                clearOverflow();

            System.out.println("Status is " + Integer.toHexString(cpu.Status));

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
                System.out.println("Val is "+ val);
                cpu.PC += 2;
                return 2;

            case ZeropageAbs:
                val = Byte.toUnsignedInt(cpu.Accumulator);
                val -= Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[Byte.toUnsignedInt(op)],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
                val = Byte.toUnsignedInt(cpu.Accumulator);
                val -= Byte.toUnsignedInt(cpu.cpu_memory[add(cpu.X, op)]);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[add(cpu.X, op)],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 4;

            case PreIndirectX:
                int addr = add(cpu.X, op) & 0xff;
                loc = cpu.cpu_memory[(addr+1)&0xff] << 8 + (cpu.cpu_memory[addr] & 0xff);
                val = Byte.toUnsignedInt(cpu.Accumulator);
                val -= Byte.toUnsignedInt(cpu.cpu_memory[loc]);
                System.out.println("Value at memory is " + Integer.toHexString(cpu.cpu_memory[loc]));
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[loc],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 2;
                return 6;

            case PostIndirectY:
                loc = cpu.cpu_memory[(Byte.toUnsignedInt(op)+1)&0xff] << 8 + (cpu.cpu_memory[Byte.toUnsignedInt(op)&0xff] & 0xff);
                val = cpu.Accumulator;
                val -= Byte.toUnsignedInt(cpu.cpu_memory[add(loc , cpu.Y)]);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[add(loc , cpu.Y)],carry);
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
                val -= Byte.toUnsignedInt(cpu.cpu_memory[Short.toUnsignedInt(op)]);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[Short.toUnsignedInt(op)],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteX:
                val = cpu.Accumulator;
                val -= Byte.toUnsignedInt(cpu.cpu_memory[add(cpu.X, Short.toUnsignedInt(op))]);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator, cpu.cpu_memory[add(cpu.X, Short.toUnsignedInt(op))],carry);
                cpu.Accumulator = (byte) val;
                cpu.PC += 3;
                return 4;

            case AbsoluteY:
                val = cpu.Accumulator;
                val -= Byte.toUnsignedInt(cpu.cpu_memory[add(cpu.Y, Short.toUnsignedInt(op))]);
                if(!getCarry())
                    val--;
                setFlags(val , flags);
                calculateBorrow(cpu.Accumulator,  cpu.cpu_memory[add(cpu.Y, Short.toUnsignedInt(op))],carry);
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
//              loc = Byte.toUnsignedInt((byte) (cpu.cpu_memory[Byte.toUnsignedInt(op)] & 0xff));
                loc = Byte.toUnsignedInt(op);
//                cpu.cpu_memory[loc] = cpu.Accumulator;
                cpu.writeTo(loc, cpu.Accumulator);
                dump_at(0);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
//              loc = Byte.toUnsignedInt((byte) (cpu.cpu_memory[add(op , cpu.X)] & 0xff)) ;
                loc = add(op,cpu.X);
//                if(0x2000 <= loc && loc <= 0x2007){
//                    ppu.cpuWrite((short) loc, cpu.Accumulator);
//                    cpu.PC += 2;
//                    return 4;
//                }
//                cpu.cpu_memory[loc] = cpu.Accumulator;
                cpu.writeTo(loc, cpu.Accumulator);
                cpu.PC += 2;
                dump_at(0);
                return 4;

            case PreIndirectX:
                int addr = add(op , cpu.X) & 0xff;
                System.out.println("Address is " +Integer.toHexString(addr));
                int loc_high = ((cpu.cpu_memory[(addr+1)&0xff]<<8)&0xff00);
                int loc_low = (cpu.cpu_memory[addr] & 0xff);
                loc = ( loc_high + loc_low);
                System.out.println("Loc is " + Integer.toHexString(loc));
//                val = Byte.toUnsignedInt((byte) (cpu.cpu_memory[loc] & 0xff));
//                cpu.cpu_memory[val] = cpu.Accumulator;
//                if(0x2000 <= loc && loc <= 0x2007){
//                    ppu.cpuWrite((short) loc, cpu.Accumulator);
//                    cpu.PC += 2;
//                    return 6;
//                }
//                cpu.cpu_memory[loc] = cpu.Accumulator;
                cpu.writeTo(loc, cpu.Accumulator);
                cpu.PC += 2;
                dump_at(loc-5);
                return 6;

            case PostIndirectY:
                loc = (cpu.cpu_memory[(Byte.toUnsignedInt(op)+1)&0xff]<<8 + (cpu.cpu_memory[Byte.toUnsignedInt(op)&0xff] & 0xff));
//                val = Byte.toUnsignedInt((byte) (cpu.cpu_memory[add(loc, cpu.X)] & 0xff));
//                cpu.cpu_memory[val] = cpu.Accumulator;
                loc = add(loc,cpu.Y);
                System.out.println("Setting 0x" + Integer.toHexString(loc) +" to " + Byte.toUnsignedInt(cpu.Accumulator));
//                if(0x2000 <= loc && loc <= 0x2007){
//                    ppu.cpuWrite((short) loc, cpu.Accumulator);
//                    cpu.PC += 2;
//                    return 6;
//                }
//                cpu.cpu_memory[loc] = cpu.Accumulator;
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
//                cpu.cpu_memory[Short.toUnsignedInt(op)] = cpu.Accumulator;
                cpu.writeTo(Short.toUnsignedInt(op), cpu.Accumulator);
                cpu.PC += 3;
                dump_at(Short.toUnsignedInt(op)-5);
                return 4;

            case AbsoluteX:
                loc = add(op , cpu.X);
//                cpu.cpu_memory[add(op , cpu.X)] = cpu.Accumulator;
                cpu.writeTo(add(op , cpu.X) , cpu.Accumulator);
                cpu.PC += 3;
                dump_at(0);
                return 5;

            case AbsoluteY:
                loc = add(op , cpu.Y);
//                if(0x2000 <= loc && loc <= 0x2007){
//                    ppu.cpuWrite(op, cpu.Accumulator);
//                    cpu.PC += 3;
//                    return 5;
//                }
//                cpu.cpu_memory[add(op , cpu.Y)] = cpu.Accumulator;
                cpu.writeTo(add(op , cpu.Y),cpu.Accumulator);
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
//                cpu.cpu_memory[Byte.toUnsignedInt(op)] = cpu.X;
                cpu.writeTo(Byte.toUnsignedInt(op), cpu.X);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
//                cpu.cpu_memory[add(op , cpu.X)] = cpu.X;
                cpu.writeTo(add(op , cpu.X), cpu.X);
                cpu.PC += 2;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int stx(addressingMode A , short op){
//        cpu.cpu_memory[Short.toUnsignedInt(op)] = cpu.X;
        cpu.writeTo(Short.toUnsignedInt(op), cpu.X);
        cpu.PC += 3;
        return 4;
    }

    public int sty(addressingMode A , byte op){
        int loc ,val;
        switch(A){
            case ZeropageAbs:
                val = Byte.toUnsignedInt(cpu.cpu_memory[Byte.toUnsignedInt(op)]);
//                cpu.cpu_memory[Byte.toUnsignedInt(op)] = cpu.Y;
                cpu.writeTo(Byte.toUnsignedInt(op), cpu.Y);
                String yval = Integer.toHexString(Byte.toUnsignedInt(cpu.Y)) , valLoc = Integer.toHexString(Byte.toUnsignedInt(op));
                System.out.println("Loading " + yval + " at Location " + val);
                cpu.PC += 2;
                return 3;

            case ZeropageIndexed:
//                cpu.cpu_memory[add(op , cpu.X)] = cpu.Y;
                cpu.writeTo(add(op , cpu.X), cpu.Y);
                cpu.PC += 2;
                return 4;

            default:
                throw new OpcodeException();
        }
    }

    public int sty(addressingMode A ,short op){
        int loc ,val;
//        cpu.cpu_memory[Short.toUnsignedInt(op)] = cpu.Y;
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
        System.out.println("Setting X : " + Integer.toHexString(cpu.SP));
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
        return ((Byte.toUnsignedInt(a) + Byte.toUnsignedInt(b)));
    }
    public int  add(int a , byte b){
        return ((a + Byte.toUnsignedInt(b)));
    }

    public int add(byte a , int b){
        return ((b + Byte.toUnsignedInt(a)));
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
//      int a = Byte.toUnsignedInt(cpu.Status);
//      if((a>>6)%2==0) cpu.Status = (byte) add(0b01000000 , cpu.Status);
        System.out.println("Setting V flag");
        cpu.Status = (byte) ((cpu.Status | 0x40) & 0xff);
    }
    public void setNegative(){
        //int a = Byte.toUnsignedInt(cpu.Status);
        //if((a>>7)%2==0) cpu.Status = (byte) add(cpu.Status, 128);
        System.out.println("Setting N flag");
        System.out.println("Status is "+ Integer.toBinaryString(Byte.toUnsignedInt(cpu.Status)));
        cpu.Status = (byte) ((cpu.Status | 0x80) & 0xff);
        System.out.println("Status is "+ Integer.toBinaryString(Byte.toUnsignedInt(cpu.Status)));
    }
    private void setBreak() {
//        int a = Byte.toUnsignedInt(cpu.Status);
//        if((a>>2)%2==0) cpu.Status = (byte) add(0b00000100 , cpu.Status);
          cpu.Status = (byte) ((cpu.Status | 0x10) & 0xff);
    }
    public void setZero(){
//      int a = Byte.toUnsignedInt(cpu.Status);
//      if((a>>1)%2==0) cpu.Status = (byte) add(0b00000010 , cpu.Status);
        System.out.println("Setting Z flag");
        cpu.Status = (byte) ((cpu.Status | 0x2) & 0xff);
    }
    private void setDecimal() {
//        int a = Byte.toUnsignedInt(cpu.Status);
//        if((a>>3)%2==0) cpu.Status = (byte) add(cpu.Status, 0b00001000);
          cpu.Status = (byte) ((cpu.Status | 0x8) & 0xff);
    }
    public void setCarry(){
//        int a = Byte.toUnsignedInt(cpu.Status);
//        if(a%2==0) cpu.Status = (byte) add(0b00000001, cpu.Status);
        System.out.println("Setting D flag");
        cpu.Status = (byte) ((cpu.Status | 0x1) & 0xff);
    }
    public void clearOverflow(){
//        int a = Byte.toUnsignedInt(cpu.Status);
//        if((a>>6)%2==1) cpu.Status = (byte) sub(cpu.Status,0b01000000 );
        System.out.println("Clearing overflow flag");
        cpu.Status = (byte) (cpu.Status & 0xbf);
    }
    public void clearNegative(){
        int a = Byte.toUnsignedInt(cpu.Status);
        if((a>>7)%2==1) cpu.Status = (byte) sub(cpu.Status, 128);
    }
    public void clearZero(){
//        int a = Byte.toUnsignedInt(cpu.Status);
//        if((a>>1)%2==1) cpu.Status = (byte) sub(cpu.Status , 0b00000010);
//        a = Byte.toUnsignedInt(cpu.Status);
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
        System.out.println("OP1 : " + a +" OP2 : " +b + " Res : " +c);
        int s1 = (a>>7) & 0x01 , s2 = (b>>7) & 0x01;
        int sign = (c>>7) & 0x01;
        System.out.println("M7 : " +s1 +" N7 : " + s2 +  " R7 : " + sign);
        if(s1==s2){
            if(s1!=sign) setOverflow();
            else clearOverflow();
        }
        else clearOverflow();
    }

    private void setFlags(int sum, boolean[] flags) {
        System.out.println("In FlagSetter , Sum is " + sum);
        if(flags[0] && sum>255 && !getCarry()) setCarry();
        if(flags[1] && (sum>127 || sum<-127) && !getOverflow()) setOverflow();
        if(flags[3] && (sum==0 || sum==256) && !getZero()) setZero();

        if(flags[0] && sum<=255 && getCarry()) clearCarry();
        if(flags[1] && (sum<=127 && sum>=-127) && getOverflow()) clearOverflow();
        if(flags[3] && (sum!=0 && sum!=256) && getZero()) clearZero();

        if(((sum & 0x80) >> 7)==1) setNegative();
        else clearNegative();
    }

    public void dump_at(int a){
        for(int i=0 ; i<10 ; i++){
            System.out.println(Integer.toHexString((a+i)&0xffff) + " : 0x" + Integer.toHexString(Byte.toUnsignedInt((byte) (cpu.cpu_memory[(a+i)&0xffff] & 0xff))));
        }
    }

}
