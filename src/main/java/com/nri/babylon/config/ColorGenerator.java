package com.nri.babylon.config;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ColorGenerator {
    private Set<Integer> usedColors;

    public ColorGenerator() {
        usedColors = new HashSet<>();
    }

    public String getUniqueHexColor(String key) {
        int hashCode = key.hashCode();
        int color = (hashCode % 16) * 16; // To get "solid" colors, we restrict each RGB component to 16 distinct values (0x00, 0x10, ..., 0xf0).

        while (usedColors.contains(color)) { // Ensure color uniqueness
            color = (color + 16) % 256;
        }

        usedColors.add(color);

        return String.format("#%02x%02x%02x", color, color, color);
    }
}