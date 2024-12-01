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
            int value = random.nextInt(1000); // value2 is any number less than 1000
            drSum += value;

            // Create a new entry for "Dr"
            List<String> values = new ArrayList<>();
            values.add(String.valueOf(value));
            values.add("Dr");
            jeEntries.add(new AbstractMap.SimpleEntry<>(selectedEntries.get(i).getKey(), values));
        }

        // Now process "Cr" entries with adjusted values
        for (int i = halfSize; i < selectedEntries.size() - 1; i++) {
            int value = random.nextInt(1000);
            crSum += value;

            // Create a new entry for "Cr"
            List<String> values = new ArrayList<>();
            values.add(String.valueOf(value));
            values.add("Cr");
            jeEntries.add(new AbstractMap.SimpleEntry<>(selectedEntries.get(i).getKey(), values));
        }

        // Adjust last Cr value to match total sum of Dr
        int lastCrValue = drSum - crSum;
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(lastCrValue));
        values.add("Cr");
        jeEntries.add(new AbstractMap.SimpleEntry<>(selectedEntries.get(selectedEntries.size() - 1).getKey(), values));

        return jeEntries;
    }

}
