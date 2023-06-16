package net.sudologic.empires.states.gameplay.util;

import java.util.Random;

public class EmpireNameGenerator {
    private static final String[] PREFIXES = { "United", "Republic", "Federal", "Democratic", "Kingdom", "People's" };
    private static final String[] SUFFIXES = { "States", "Union", "Republic", "Kingdom", "Federation" };
    private static final String[] ADJECTIVES = { "Great", "New", "Northern", "Southern", "Eastern", "Western" };
    private static final String[] NOUNS = { "Land", "Island", "Nation", "Country", "Territory" };

    private static final Random random = new Random();

    public static String generateEmpireName() {
        StringBuilder empireName = new StringBuilder();

        // Add prefix (optional)
        if (random.nextBoolean()) {
            empireName.append(getRandomElement(PREFIXES)).append(" ");
        }

        // Add adjective (optional)
        if (random.nextBoolean()) {
            empireName.append(getRandomElement(ADJECTIVES)).append(" ");
        }

        // Add noun
        empireName.append(getRandomElement(NOUNS));

        // Add suffix (optional)
        if (random.nextBoolean()) {
            empireName.append(" ").append(getRandomElement(SUFFIXES));
        }

        return empireName.toString();
    }

    private static String getRandomElement(String[] array) {
        int index = random.nextInt(array.length);
        return array[index];
    }
}