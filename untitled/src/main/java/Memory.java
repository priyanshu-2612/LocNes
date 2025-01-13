public class Memory {
    byte Accumulator;
    byte X;
    byte Y;
    byte Status; // NV1B DIZC
    byte SP;
    short PC;
    byte[] stack = new byte[256];
    byte[] ram = new byte[2048]; // 2 KB
}
