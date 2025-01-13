public class InstructionSet {

    Memory memory;
    public void adc(addressingMode A, byte op){
        int sum , loc , addr;
        boolean[] flags = {true , true , true , true};
        switch (A) {
            case Immediate:
                sum = add(op , (byte) (memory.Status%2)); //op+C
                sum = add(sum , memory.Accumulator); // op+C+A
                setFlags(sum , flags);
                memory.Accumulator = (byte)sum;
                memory.PC += 2;
                break;

            case ZeropageAbs:
                sum = add(memory.ram[Byte.toUnsignedInt(op)], (byte) (memory.Status%2));
                sum = add(sum, memory.Accumulator);
                setFlags(sum , flags);
                memory.Accumulator = (byte) sum;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                byte val = memory.ram[add(op , memory.X)];
                sum = add(val , (byte) (memory.Status%2));
                sum = add(sum, memory.Accumulator);
                setFlags(sum, flags);
                memory.Accumulator = (byte) sum;
                memory.PC += 2;
                break;

            case PreIndirectX:
                loc = add(op , memory.X);
                addr = memory.ram[loc+1]<<8 + memory.ram[loc]; // converting into little endian
                sum = add(memory.ram[addr] , (byte) (memory.Status%2));
                sum = add(sum, memory.Accumulator);
                setFlags(sum , flags);
                memory.Accumulator = (byte) sum;
                memory.PC += 2;
                break;

            case PostIndirectY:
                addr = memory.ram[Byte.toUnsignedInt(op)+1]<<8 + memory.ram[Byte.toUnsignedInt(op)];
                addr = add(memory.Y, addr);
                sum = add(memory.ram[addr], memory.Status%2);
                sum = add(sum, memory.Accumulator);
                setFlags(sum , flags);
                memory.Accumulator = (byte) sum;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void adc(addressingMode A , short op){
        int sum , loc;
        boolean[] flags = {true , true , true , true};
        switch(A){
            case Absolute:
                sum = add(memory.ram[Short.toUnsignedInt(op)], (byte) (memory.Status%2));
                sum = add(sum, memory.Accumulator);
                setFlags(sum ,flags);
                memory.Accumulator = (byte) sum;
                memory.PC += 2;
                break;

            case AbsoluteX:
                loc = add(op, memory.X);
                sum = add(memory.ram[loc] , memory.Status%2);
                sum = add(sum, memory.Accumulator);
                setFlags(sum, flags);
                memory.Accumulator = (byte) sum;
                memory.PC += 2;
                break;

            case AbsoluteY:
                loc = add(op, memory.Y);
                sum = add(memory.ram[loc] , memory.Status%2);
                sum = add(sum, memory.Accumulator);
                setFlags(sum , flags);
                memory.Accumulator = (byte) sum;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void and(addressingMode A , byte op){
        int val , loc ,addr;

        boolean[] flags = {true , true , true , true};
        switch(A){
            case Immediate:
                val = memory.Accumulator & op;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)] & memory.Accumulator;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[memory.X+Byte.toUnsignedInt(op)] & memory.Accumulator;
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case PreIndirectX:
                loc = add(op , memory.X);
                addr = memory.ram[loc+1]<<8 + memory.ram[loc];
                val = memory.Accumulator & memory.ram[addr];
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case PostIndirectY:
                addr = memory.ram[Byte.toUnsignedInt(op)+1]<<8 + memory.ram[Byte.toUnsignedInt(op)];
                addr = add(memory.Y, addr);
                val = memory.ram[addr] & memory.Accumulator;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;
        }
    }

    public void and(addressingMode A , short op){
        int val;
        boolean[] flags = {true , true , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)] & memory.Accumulator;
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteX:
                val = memory.ram[add(op , memory.X)] & memory.Accumulator;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteY:
                val = memory.ram[add(op , memory.Y)] & memory.Accumulator;
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;
        }
    }

    public void asl(addressingMode A){
        boolean[] flags = {true, false, true , true};
        if(A==addressingMode.Accumulator){
            int a = memory.Accumulator << 1;
            setFlags(a, flags);
            memory.Accumulator = (byte) a;
            memory.PC += 2;
        }
        else throw new OpcodeException();
    }

    public void asl(addressingMode A , byte op){
        int val , loc , addr;
        boolean[] flags = {true , true , true , true};
        switch(A){
            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)]<<1;
                setFlags(val, flags);
                memory.ram[Byte.toUnsignedInt(op)] = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(op, memory.X)]<<1;
                setFlags(val, flags);
                memory.ram[add(op, memory.X)] = (byte) val;
                memory.PC += 2;
                break;

            case Absolute:
                addr = memory.ram[add(op,1)]<<8 + memory.ram[Byte.toUnsignedInt(op)];
                val = memory.ram[addr]<<1;
                setFlags(val , flags);
                memory.ram[addr] = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteX:
                addr = add(op, memory.X);
                val = memory.ram[addr]<<1;
                setFlags(val, flags);
                memory.ram[addr] = (byte) val;
                memory.PC += 2;
                break;
        }
    }

    public void bcc(byte op){
        memory.PC += 2;
        if(!getCarry())memory.PC += op;  //signed addition
    }

    public void bcs(byte op){
        memory.PC += 2;
        if(getCarry())memory.PC += op;
    }

    public void beq(byte op){
        memory.PC += 2;
        if(getZero())memory.PC += op;
    }

    public void bit(addressingMode A , byte op){

        if(!getNegative()){
            if(((memory.ram[Byte.toUnsignedInt(op)]>>7) & 1) == 1) setNegative();
        }
        if(!getOverflow()){
            if(((memory.ram[Byte.toUnsignedInt(op)]>>6) & 1) == 1) setOverflow();
        }
        if(!getZero()){
            if((memory.Accumulator&memory.ram[Byte.toUnsignedInt(op)]) == 0) setZero();
        }
        memory.PC += 2;

    }

    public void bmi(byte op){
        memory.PC += 2;
        if(getNegative()) memory.PC = (short) add(memory.PC , op);
    }

    public void bne(byte op){
        memory.PC += 2;
        if(!getZero()) memory.PC = (short) add(memory.PC , op);
    }

    public void bpl(byte op){
        memory.PC += 2;
        if(!getNegative()) memory.PC = (short) add(memory.PC , op);
    }

    public void brk(){
        memory.stack[memory.SP++] = (byte) (memory.PC >>> 8); //PC High
        memory.stack[memory.SP++] = (byte) (memory.PC); //PC Low
        setBreak();
        memory.stack[memory.SP++] = memory.Status;

        int loc = memory.ram[0xffff] << 8 + memory.ram[0xfffe];
        memory.PC = (short) loc;
    }

    public void bvc(byte op){
        memory.PC += 2;
        if(!getOverflow()) memory.PC = (short) add(memory.PC , op);
    }

    public void bvs(byte op){
        memory.PC += 2;
        if(getOverflow()) memory.PC = (short) add(memory.PC , op);
    }

    public void clc(byte op){
        clearCarry();
        memory.PC += 2;
    }

    public void cld(byte op){
        clearDecimal();
        memory.PC += 2;
    }

    public void cli(byte op){
        clearInterrupt();
        memory.PC += 2;
    }

    public void clv(byte op){
        clearOverflow();
        memory.PC += 2;
    }

    public void cmp(addressingMode A , byte op){
        int loc ,addr, val , acc = Byte.toUnsignedInt(memory.Accumulator);
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);
                if(val==acc) setZero();
                else clearZero();

                if(acc<val) setNegative();
                else clearNegative();

                if(val <= acc) setCarry();
                else clearCarry();

                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                if(val==acc) setZero();
                else clearZero();

                if(acc<val) setNegative();
                else clearNegative();

                if(val <= acc) setCarry();
                else clearCarry();

                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(memory.X, op)];
                if(val==acc) setZero();
                else clearZero();

                if(acc<val) setNegative();
                else clearNegative();

                if(val <= acc) setCarry();
                else clearCarry();

                memory.PC += 2;
                break;

            case PreIndirectX:
                loc = add(op , memory.X);
                addr = memory.ram[loc+1]<<8 + memory.ram[loc];
                val = memory.ram[addr];
                if(val==acc) setZero();
                else clearZero();

                if(acc<val) setNegative();
                else clearNegative();

                if(val <= acc) setCarry();
                else clearCarry();

                memory.PC += 2;
                break;

            case PostIndirectY:
                loc = memory.ram[Byte.toUnsignedInt(op)+1]<<8 + memory.ram[Byte.toUnsignedInt(op)];
                val = memory.ram[add(loc , memory.X)];
                if(val==acc) setZero();
                else clearZero();

                if(acc<val) setNegative();
                else clearNegative();

                if(val <= acc) setCarry();
                else clearCarry();

                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void cmp(addressingMode A, short op){
        int loc , val , acc = Byte.toUnsignedInt(memory.Accumulator);
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)] ;
                if(val==acc) setZero();
                else clearZero();

                if(acc<val) setNegative();
                else clearNegative();

                if(val <= acc) setCarry();
                else clearCarry();

                memory.PC += 2;
                break;

            case AbsoluteX:
                val = memory.ram[add(memory.X, op)];
                if(val==acc) setZero();
                else clearZero();

                if(acc<val) setNegative();
                else clearNegative();

                if(val <= acc) setCarry();
                else clearCarry();

                memory.PC += 2;
                break;

            case AbsoluteY:
                val = memory.ram[add(memory.Y, op)];
                if(val==acc) setZero();
                else clearZero();

                if(acc<val) setNegative();
                else clearNegative();

                if(val <= acc) setCarry();
                else clearCarry();

                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void cpx(addressingMode A , byte op){
        int loc , val , x = Byte.toUnsignedInt(memory.X);
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);

                if(x==val) setZero();
                else clearZero();

                if(x >= val) setCarry();
                else clearCarry();

                if(x<val) setNegative();
                else clearNegative();

                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];

                if(x==val) setZero();
                else clearZero();

                if(x >= val) setCarry();
                else clearCarry();

                if(x<val) setNegative();
                else clearNegative();

                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void cpx(short op){
        int loc , val , x = Byte.toUnsignedInt(memory.X);
        val = memory.ram[Short.toUnsignedInt(op)];

        if(x==val) setZero();
        else clearZero();

        if(x >= val) setCarry();
        else clearCarry();

        if(x<val) setNegative();
        else clearNegative();

        memory.PC += 2;
    }

    public void cpy(addressingMode A , byte op){
        int loc , val , y = Byte.toUnsignedInt(memory.Y);
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);

                if(y==val) setZero();
                else clearZero();

                if(y >= val) setCarry();
                else clearCarry();

                if(y<val) setNegative();
                else clearNegative();

                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];

                if(y==val) setZero();
                else clearZero();

                if(y >= val) setCarry();
                else clearCarry();

                if(y<val) setNegative();
                else clearNegative();

                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void cpy(short op){
        int loc , val , y = Byte.toUnsignedInt(memory.Y);
        val = memory.ram[Short.toUnsignedInt(op)];

        if(y==val) setZero();
        else clearZero();

        if(y >= val) setCarry();
        else clearCarry();

        if(y<val) setNegative();
        else clearNegative();

        memory.PC += 2;
    }

    public void dec(addressingMode A , byte op){
        int val , loc;
        boolean[] flags = {false , false, true , true};
        switch(A){
            case ZeropageAbs:
                loc = memory.ram[Byte.toUnsignedInt(op)];
                memory.ram[loc] = (byte) sub(memory.ram[loc],1);
                setFlags(sub(memory.ram[loc],1) , flags);
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                loc = memory.ram[add(op, memory.X)];
                memory.ram[loc] = (byte) sub(memory.ram[loc],1);
                setFlags(sub(memory.ram[loc],1) , flags);
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void dec(addressingMode A , short op){
        int loc , val;
        boolean[] flags = {false , false, true , true};
        switch(A){
            case Absolute:
                loc = memory.ram[Short.toUnsignedInt(op)];
                memory.ram[loc] = (byte) sub(memory.ram[loc],1);
                setFlags(sub(memory.ram[loc],1) , flags);
                memory.PC += 2;
                break;

            case AbsoluteX:
                loc = memory.ram[Short.toUnsignedInt(op) + Byte.toUnsignedInt(memory.X)];
                memory.ram[loc] = (byte) sub(memory.ram[loc],1);
                setFlags(sub(memory.ram[loc],1) , flags);
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void dex(){
        boolean[] flags = {false , false, true , true};
        memory.X = (byte) sub(memory.X, 1);

        setFlags(sub(memory.X, 1) , flags);
        memory.PC += 2;
    }

    public void dey(){
        boolean[] flags = {false , false, true , true};
        memory.Y = (byte) sub(memory.Y, 1);

        setFlags(sub(memory.Y, 1) , flags);
        memory.PC += 2;
    }

    public void eor(addressingMode A , byte op){
        int val , loc, res;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                res = memory.Accumulator ^ op;
                memory.Accumulator = (byte) (res);
                setFlags(res, flags);
                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                res = memory.Accumulator ^ val;
                memory.Accumulator = (byte) (res);
                setFlags(res, flags);
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(op , memory.X)];
                res = memory.Accumulator ^ val;
                memory.Accumulator = (byte) (res);
                setFlags(res, flags);
                memory.PC += 2;
                break;

            case PreIndirectX:
                loc = memory.ram[add(memory.X, op)+1] <<8 + memory.ram[add(memory.X, op)];
                val = memory.ram[loc];
                res = memory.Accumulator ^ val;
                memory.Accumulator = (byte) (res);
                setFlags(res, flags);
                memory.PC += 2;
                break;

            case PostIndirectY:
                loc = memory.ram[Byte.toUnsignedInt(op)+1]<<8 + memory.ram[Byte.toUnsignedInt(op)];
                val = memory.ram[add(loc, memory.Y)];
                res = memory.Accumulator ^ val;
                memory.Accumulator = (byte) (res);
                setFlags(res, flags);
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void eor(addressingMode A , short op){
        int loc , val , res;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)];
                res = memory.Accumulator ^ val;
                memory.Accumulator = (byte) (res);
                setFlags(res, flags);
                memory.PC += 2;
                break;

            case AbsoluteX:
                val = memory.ram[add(Short.toUnsignedInt(op) , memory.X)];
                res = memory.Accumulator ^ val;
                memory.Accumulator = (byte) (res);
                setFlags(res, flags);
                memory.PC += 2;
                break;

            case AbsoluteY:
                val = memory.ram[add(Short.toUnsignedInt(op) , memory.Y)];
                res = memory.Accumulator ^ val;
                memory.Accumulator = (byte) (res);
                setFlags(res, flags);
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void inc(addressingMode A , byte op){
        int val , loc , res;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                res = add(memory.ram[val] , 1);
                setFlags(res, flags);
                memory.ram[val] = (byte)res;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(op, memory.X)];
                res = add(memory.ram[val] , 1);
                setFlags(res, flags);
                memory.ram[val] = (byte)res;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void inc(addressingMode A , short op){
        int loc , res , val;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)];
                res = add(memory.ram[val] , 1);
                setFlags(res, flags);
                memory.ram[val] = (byte)res;
                memory.PC += 2;
                break;

            case AbsoluteX:
                val = memory.ram[add(Short.toUnsignedInt(op) , memory.X)];
                res = add(memory.ram[val] , 1);
                setFlags(res, flags);
                memory.ram[val] = (byte)res;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void inx(){
        int res;
        boolean[] flags = {false, false , true , true};

        res = add(memory.X, 1);
        setFlags(res , flags);
        memory.X = (byte) res;
        memory.PC += 2;
    }

    public void iny(){
        int res;
        boolean[] flags = {false, false , true , true};

        res = add(memory.Y, 1);
        setFlags(res , flags);
        memory.Y = (byte) res;
        memory.PC += 2;
    }

    public void jmp(addressingMode A , short op){
        int loc , val;
        switch(A){
            case Absolute:
                memory.PC += 2;
                memory.PC = (short)(op<<8 + op>>8);
                break;

            case Indirect:
                memory.PC += 2;
                val = memory.ram[Short.toUnsignedInt(op)];
                memory.PC = (short)(val<<8 + val>>8);
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void jsr(short op){
        memory.PC += 2;
        memory.stack[memory.SP++] = (byte) (memory.PC>>8);
        memory.stack[memory.SP++] = (byte) (memory.PC);

        memory.PC = (short)(op<<8 + op>>8);
    }

    public void lda(addressingMode A, byte op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(memory.X, op)];
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case PreIndirectX:
                addr = add(memory.X, op);
                loc = memory.ram[addr+1] << 8 + memory.ram[addr];
                val = memory.ram[loc];
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case PostIndirectY:
                addr = Byte.toUnsignedInt(op);
                loc = memory.ram[addr+1] << 8 + memory.ram[addr];
                val = memory.ram[add(loc , memory.Y)];
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void lda(addressingMode A , short op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)];
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteX:
                val = memory.ram[add(memory.X, op)];
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteY:
                val = memory.ram[add(memory.Y, op)];
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void ldx(addressingMode A , byte op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);
                setFlags(val, flags);
                memory.X = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                setFlags(val, flags);
                memory.X = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(memory.Y, op)];
                setFlags(val, flags);
                memory.X = (byte) val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void ldx(addressingMode A , short op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)];
                setFlags(val, flags);
                memory.X = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteY:
                val = memory.ram[add(memory.Y, op)];
                setFlags(val, flags);
                memory.X = (byte) val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void ldy(addressingMode A , byte op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Immediate:
                val = Byte.toUnsignedInt(op);
                setFlags(val, flags);
                memory.Y = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                setFlags(val, flags);
                memory.Y = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(memory.X, op)];
                setFlags(val, flags);
                memory.Y = (byte) val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void ldy(addressingMode A , short op){
        int val , loc , addr;
        boolean[] flags = {false, false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)];
                setFlags(val, flags);
                memory.Y = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteX:
                val = memory.ram[add(memory.X, op)];
                setFlags(val, flags);
                memory.Y = (byte) val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void lsr(addressingMode A , byte op){
        int loc , val;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                val >>= 1;
                setFlags(val , flags);
                memory.ram[Byte.toUnsignedInt(op)] = (byte)val;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(op, memory.X)];
                val >>= 1;
                setFlags(val , flags);
                memory.ram[add(memory.X, op)] = (byte)val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void lsr(addressingMode A , short op){
        int loc , val;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)];
                val >>= 1;
                setFlags(val , flags);
                memory.ram[Short.toUnsignedInt(op)] = (byte)val;
                memory.PC += 2;
                break;

            case AbsoluteX:
                val = memory.ram[add(op ,memory.X)];
                val >>= 1;
                setFlags(val , flags);
                memory.ram[add(op , memory.X)] = (byte)val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void nop(){
        memory.PC += 2;
    }

    public void ora(addressingMode A , byte op){
        int val , loc ,addr;

        boolean[] flags = {false, false, true , true};
        switch(A){
            case Immediate:
                val = memory.Accumulator | op;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)] | memory.Accumulator;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[memory.X+Byte.toUnsignedInt(op)] | memory.Accumulator;
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case PreIndirectX:
                loc = add(op , memory.X);
                addr = memory.ram[loc+1]<<8 + memory.ram[loc];
                val = memory.Accumulator | memory.ram[addr];
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case PostIndirectY:
                addr = memory.ram[Byte.toUnsignedInt(op)+1]<<8 + memory.ram[Byte.toUnsignedInt(op)];
                addr = add(memory.Y, addr);
                val = memory.ram[addr] | memory.Accumulator;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;
        }
    }

    public void ora(addressingMode A , short op){
        int val;
        boolean[] flags = {false , false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)] | memory.Accumulator;
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteX:
                val = memory.ram[add(op , memory.X)] | memory.Accumulator;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case AbsoluteY:
                val = memory.ram[add(op , memory.Y)] | memory.Accumulator;
                setFlags(val, flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;
        }
    }

    public void pha(addressingMode A , byte op){
        memory.stack[memory.SP++] = memory.Accumulator;
        memory.PC += 2;
    }

    public void php(addressingMode A , byte op){
        memory.stack[memory.SP++] = memory.Status;
        memory.PC += 2;
    }

    public void pla(addressingMode A , byte op){
        memory.SP--;
        memory.Accumulator = memory.stack[memory.SP];
        memory.PC += 2;
    }

    public void plp(addressingMode A , byte op){
        memory.SP--;
        memory.Status = memory.stack[memory.SP];
        memory.PC += 2;
    }

    public void rol(){
        int val;
        boolean[] flags = {true , false , true , true};

        val = memory.Accumulator;
        val <<= 1;
        if(getCarry())
            val |= 1 ;
        else
            val |= 0;

        setFlags(val,flags);
        memory.Accumulator = (byte) val;
        memory.PC += 2 ;
    }

    public void rol(addressingMode A , byte op){
        int loc , val;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                memory.ram[Byte.toUnsignedInt(op)] = (byte) val;
                memory.PC += 2 ;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(op , memory.X)];
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                memory.ram[add(memory.X,op)] = (byte) val;
                memory.PC += 2 ;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void rol(addressingMode A , short op){
        int val , loc;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)];
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                memory.ram[Short.toUnsignedInt(op)] = (byte) val;
                memory.PC += 2 ;
                break;

            case AbsoluteX:
                val = memory.ram[add(op , memory.X)];
                val <<= 1;
                if(getCarry())
                    val |= 1 ;
                else
                    val |= 0;

                setFlags(val,flags);
                memory.ram[add(op , memory.X)] = (byte) val;
                memory.PC += 2 ;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void ror(){
        int val;
        boolean[] flags = {true , false , true , true};

        val = memory.Accumulator;
        val >>= 1;
        if(getCarry())
            val |= 0b10000000 ;
        else
            val |= 0;

        setFlags(val,flags);
        memory.Accumulator = (byte) val;
        memory.PC += 2 ;
    }

    public void ror(addressingMode A , byte op){
        int loc , val;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                memory.ram[Byte.toUnsignedInt(op)] = (byte) val;
                memory.PC += 2 ;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(op , memory.X)];
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                memory.ram[add(memory.X,op)] = (byte) val;
                memory.PC += 2 ;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void ror(addressingMode A , short op){
        int val , loc;
        boolean[] flags = {true , false , true , true};
        switch(A){
            case Absolute:
                val = memory.ram[Short.toUnsignedInt(op)];
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                memory.ram[Short.toUnsignedInt(op)] = (byte) val;
                memory.PC += 2 ;
                break;

            case AbsoluteX:
                val = memory.ram[add(op , memory.X)];
                val >>= 1;
                if(getCarry())
                    val |= 0b10000000 ;
                else
                    val |= 0;

                setFlags(val,flags);
                memory.ram[add(op , memory.X)] = (byte) val;
                memory.PC += 2 ;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void rti(){
        memory.SP--;
        memory.Status = memory.stack[memory.SP];
        memory.SP--;
        memory.PC = (short) (memory.stack[memory.SP]<<8 + memory.stack[memory.SP-1]);
        memory.SP--;

        memory.PC += 1;
    }

    public void rts(addressingMode A , byte op){
        memory.SP--;
        memory.PC = (short) (memory.stack[memory.SP]<<8 + memory.stack[memory.SP-1]);
        memory.SP--;

        memory.PC += 1;
    }

    public void sbc(addressingMode A , byte op){
        int val , loc;
        boolean[] flags = {true , true , true , true};
        switch(A){
            case Immediate:
                val = memory.Accumulator;
                val -= Byte.toUnsignedInt(op);
                if(getCarry())
                    val--;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageAbs:
                val = memory.Accumulator;
                val -= memory.ram[Byte.toUnsignedInt(op)];
                if(getCarry())
                    val--;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.Accumulator;
                val -= memory.ram[add(memory.X, op)];
                if(getCarry())
                    val--;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case PreIndirectX:
                loc = memory.ram[add(memory.X, op)+1] << 8 + memory.ram[add(memory.X, op)];
                val = memory.Accumulator;
                val -= memory.ram[loc];
                if(getCarry())
                    val--;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            case PostIndirectY:
                loc = memory.ram[Byte.toUnsignedInt(op)+1] << 8 + memory.ram[Byte.toUnsignedInt(op)];
                val = memory.Accumulator;
                val -= memory.ram[add(loc , memory.X)];
                if(getCarry())
                    val--;
                setFlags(val , flags);
                memory.Accumulator = (byte) val;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void sec(){
        setCarry();
        memory.PC += 2;
    }

    public void sed(){
        setDecimal();
        memory.PC += 2;
    }

    public void sei(){
        memory.Status |= 0b00000100;
        memory.PC += 2;
    }

    public void sta(addressingMode A , byte op){
        int val , loc;
        switch(A){
            case Immediate:
                memory.ram[Byte.toUnsignedInt(op)] = memory.Accumulator;
                memory.PC += 2;
                break;

            case ZeropageAbs:
                loc = memory.ram[Byte.toUnsignedInt(op)];
                memory.ram[loc] = memory.Accumulator;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                loc = memory.ram[add(op , memory.X)];
                memory.ram[loc] = memory.Accumulator;
                memory.PC += 2;
                break;

            case PreIndirectX:
                loc = memory.ram[add(op , memory.X)+1]<<8 + memory.ram[add(op , memory.X)];
                val = memory.ram[loc];
                memory.ram[val] = memory.Accumulator;
                memory.PC += 2;
                break;

            case PostIndirectY:
                loc = memory.ram[Byte.toUnsignedInt(op)+1]<<8 + memory.ram[Byte.toUnsignedInt(op)];
                val = memory.ram[add(loc, memory.X)];
                memory.ram[val] = memory.Accumulator;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void sta(addressingMode A , short op){
        int val , loc;
        switch(A){
            case Absolute:
                memory.ram[Short.toUnsignedInt(op)] = memory.Accumulator;
                memory.PC += 2;
                break;

            case AbsoluteX:
                memory.ram[add(op , memory.X)] = memory.Accumulator;
                memory.PC += 2;
                break;

            case AbsoluteY:
                memory.ram[add(op , memory.Y)] = memory.Accumulator;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void stx(addressingMode A , byte op){
        int loc ,val;
        switch(A){
            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                memory.ram[val] = memory.X;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(op , memory.X)];
                memory.ram[val] = memory.X;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void stx(short op){
        int loc ,val;
        val = memory.ram[Short.toUnsignedInt(op)];
        memory.ram[val] = memory.X;
        memory.PC += 2;
    }

    public void sty(addressingMode A , byte op){
        int loc ,val;
        switch(A){
            case ZeropageAbs:
                val = memory.ram[Byte.toUnsignedInt(op)];
                memory.ram[val] = memory.Y;
                memory.PC += 2;
                break;

            case ZeropageIndexed:
                val = memory.ram[add(op , memory.X)];
                memory.ram[val] = memory.Y;
                memory.PC += 2;
                break;

            default:
                throw new OpcodeException();
        }
    }

    public void sty(short op){
        int loc ,val;
        val = memory.ram[Short.toUnsignedInt(op)];
        memory.ram[val] = memory.Y;
        memory.PC += 2;
    }

    public void tax(){
        boolean[] flags = {false , false, true , true};
        setFlags(memory.Accumulator, flags);
        memory.X = memory.Accumulator;
        memory.PC += 2;
    }

    public void tay(){
        boolean[] flags = {false , false, true , true};
        setFlags(memory.Accumulator, flags);
        memory.Y = memory.Accumulator;
        memory.PC += 2;
    }

    public void tsx(){
        boolean[] flags = {false , false, true , true};
        setFlags(memory.Status, flags);
        memory.X = memory.Status;
        memory.PC += 2;
    }

    public void txa(){
        boolean[] flags = {false , false, true , true};
        setFlags(memory.X, flags);
        memory.Accumulator = memory.X;
        memory.PC += 2;
    }

    public void txs(){
        memory.Status = memory.X;
        memory.PC += 2;
    }

    public void tya(){
        boolean[] flags = {false , false, true , true};
        setFlags(memory.Y, flags);
        memory.Accumulator = memory.Y;
        memory.PC += 2;
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
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>6)%2==0) memory.Status = (byte) add(0b01000000 , memory.Status);
    }
    public void setNegative(){
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>7)%2==0) memory.Status = (byte) add(memory.Status, 128);
    }
    private void setBreak() {
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>2)%2==0) memory.Status = (byte) add(0b00000100 , memory.Status);
    }
    public void setZero(){
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>1)%2==0) memory.Status = (byte) add(0b00000010 , memory.Status);
    }
    private void setDecimal() {
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>3)%2==0) memory.Status = (byte) add(memory.Status, 0b00001000);
    }
    public void setCarry(){
        int a = Byte.toUnsignedInt(memory.Status);
        if(a%2==0) memory.Status = (byte) add(0b00000001, memory.Status);
    }
    public void clearOverflow(){
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>6)%2==0) memory.Status = (byte) sub(memory.Status,0b01000000 );
    }
    public void clearNegative(){
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>7)%2==0) memory.Status = (byte) sub(memory.Status, 128);
    }
    public void clearZero(){
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>1)%2==0) memory.Status = (byte) sub(memory.Status , 0b00000010);
    }
    private void clearDecimal() {
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>3)%2==0) memory.Status = (byte) sub(memory.Status, 0b00001000);
    }
    public void clearCarry(){
        int a = Byte.toUnsignedInt(memory.Status);
        if(a%2==0) memory.Status = (byte) sub(memory.Status, 0b00000001);
    }

    private void clearInterrupt() {
        int a = Byte.toUnsignedInt(memory.Status);
        if((a>>2)%2==0) memory.Status = (byte) sub(memory.Status, 0b00000100);
    }
    public boolean getOverflow(){
        int a = Byte.toUnsignedInt(memory.Status);
        return ((a>>6)%2==1);
    }
    public boolean getNegative(){
        int a = Byte.toUnsignedInt(memory.Status);
        return((a>>7)%2==1);
    }
    public boolean getZero(){
        int a = Byte.toUnsignedInt(memory.Status);
        return((a>>1)%2==1);
    }
    public boolean getCarry(){
        int a = Byte.toUnsignedInt(memory.Status);
        return(a%2==1);
    }

    private void setFlags(int sum, boolean[] flags) {
        if(flags[0] && sum>255 && !getCarry()) setCarry();
        if(flags[1] && sum>255 && !getOverflow()) setOverflow();
        if(flags[2] && sum<0 && !getNegative()) setNegative();
        if(flags[3] && (byte)sum==0 && !getZero()) setZero();

        if(flags[0] && sum<255 && getCarry()) clearCarry();
        if(flags[1] && sum<255 && getOverflow()) clearOverflow();
        if(flags[2] && (byte)sum>0 && getNegative()) clearNegative();
        if(flags[3] && sum!=0 && getZero()) clearZero();
    }


}
