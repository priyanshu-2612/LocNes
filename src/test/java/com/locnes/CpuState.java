package com.locnes;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CpuState {
    int pc; // 2bytes
    int s, a, x, y, p;
    List<List<Integer>> ram;
}
