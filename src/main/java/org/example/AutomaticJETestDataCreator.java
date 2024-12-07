package org.example;

import java.util.*;

public class AutomaticJETestDataCreator {

    // Function to generate key-value pairs with equal "Dr" and "Cr" counts and sum
    public static List<Map.Entry<String, List<String>>> generateKeyValuePairs(List<Map.Entry<String, List<String>>> selectedEntries) {
        // To store the result
        List<Map.Entry<String, List<String>>> jeEntries = new ArrayList<>();

        // Split the list equally for Dr and Cr
        int halfSize = selectedEntries.size() / 2;
        Random random = new Random();

        // Initialize sums for Dr and Cr
        int drSum = 0, crSum = 0;

        // First, process "Dr" entries
        for (int i = 0; i < halfSize; i++) {
            int value = random.nextInt(1000) + 1; // Ensure value > 0
            drSum += value;

            // Create a new entry for "Dr"
            List<String> values = new ArrayList<>();
            values.add(String.valueOf(value));
            values.add("Dr");
            jeEntries.add(new AbstractMap.SimpleEntry<>(selectedEntries.get(i).getKey(), values));
        }

        // Now process "Cr" entries
        for (int i = halfSize; i < selectedEntries.size() - 1; i++) {
            int value = random.nextInt(1000) + 1; // Ensure value > 0
            crSum += value;

            // Create a new entry for "Cr"
            List<String> values = new ArrayList<>();
            values.add(String.valueOf(value));
            values.add("Cr");
            jeEntries.add(new AbstractMap.SimpleEntry<>(selectedEntries.get(i).getKey(), values));
        }

        // Calculate the remaining Cr value needed to balance the sums
        int remainingCrValue = drSum - crSum;

        if (remainingCrValue <= 0) {
            // Distribute the difference across all Cr entries proportionally
            int deficit = Math.abs(remainingCrValue);
            for (int i = halfSize; i < jeEntries.size(); i++) {
                Map.Entry<String, List<String>> entry = jeEntries.get(i);
                int currentValue = Integer.parseInt(entry.getValue().get(0));
                int adjustment = Math.min(deficit, currentValue - 1); // Ensure value remains > 0
                deficit -= adjustment;
                entry.getValue().set(0, String.valueOf(currentValue - adjustment));
                if (deficit == 0) break;
            }
        } else {
            // Assign the remaining positive Cr value to the last entry
            List<String> values = new ArrayList<>();
            values.add(String.valueOf(remainingCrValue));
            values.add("Cr");
            jeEntries.add(new AbstractMap.SimpleEntry<>(selectedEntries.get(selectedEntries.size() - 1).getKey(), values));
        }

        return jeEntries;
    }

}
