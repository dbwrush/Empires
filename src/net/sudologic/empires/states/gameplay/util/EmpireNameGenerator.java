package net.sudologic.empires.states.gameplay.util;

import java.util.Random;

public class EmpireNameGenerator {
    private static final String[] LEFT_NAMES = {"Socialist", "Soviet", "People's", "Democratic", "Workers'", "Communist", "Collective"};
    private static final String[] RIGHT_NAMES = {"Capitalist", "Free", "Liberal", "Market", "Democratic", "Individual"};
    private static final String[] AUTH_NAMES = {"Empire", "Dominion", "State", "Union", "Federation", "Kingdom"};
    private static final String[] LIB_NAMES = {"Republic", "Commonwealth", "Alliance", "Federation", "League", "Union"};
    private static final String[] ISO_NAMES = {"Isolationist", "Solitary", "Secluded", "Reclusive", "Hermit", "Independent"};
    private static final String[] COOP_NAMES = {"Cooperative", "Allied", "United", "Collaborative", "Concordant", "Joint"};

    private static final String[] GOVT_NAMES = {"Nation", "Realm", "Republic", "Country", "Territory", "Land"};
    private static final String[] PLACE_NAMES = {
            "France", "Germany", "Britain", "America", "Russia", "China", "Brazil", "India", "Canada",
            "Australia", "Japan", "Italy", "Spain", "Mexico", "Egypt", "Argentina", "Netherlands",
            "Sweden", "Greece", "Turkey", "Thailand", "Antarctica", "Chile", "Iceland", "Austria", "Kenya", "Indonesia",
            "Denmark", "England", "Scotland", "Wales", "Ireland", "Norway", "Finland", "Belgium", "Portugal", "Singapore", "Korea",
            "Czechia", "Israel", "Switzerland", "Colombia", "Peru", "Cuba", "Ukraine", "Vietnam", "Jamaica",
            "Hawaii", "Mongolia", "Belarus", "Madagascar", "Liberia", "Libya", "Algeria", "Pakistan", "Iran", "Afghanistan",
            "Indonesia", "Brazil", "Bolivia", "Congo"
    };

    public static String generateEmpireName(int isoCoop, int authLib, int leftRight, String placeName) {
        StringBuilder name = new StringBuilder();

        if (isoCoop < 96 || isoCoop > 160) {
            if (isoCoop < 96) {
                int index = new Random().nextInt(ISO_NAMES.length);
                name.append(ISO_NAMES[index]).append(" ");
            } else {
                int index = new Random().nextInt(COOP_NAMES.length);
                name.append(COOP_NAMES[index]).append(" ");
            }
        }

        if (leftRight < 96 || leftRight > 160) {
            if (leftRight < 96) {
                int index = new Random().nextInt(LEFT_NAMES.length);
                name.append(LEFT_NAMES[index]).append(" ");
            } else {
                int index = new Random().nextInt(RIGHT_NAMES.length);
                name.append(RIGHT_NAMES[index]).append(" ");
            }
        }

        if (authLib < 96 || authLib > 160) {
            if (authLib < 96) {
                int index = new Random().nextInt(AUTH_NAMES.length);
                name.append(AUTH_NAMES[index]).append(" ");
            } else {
                int index = new Random().nextInt(LIB_NAMES.length);
                name.append(LIB_NAMES[index]).append(" ");
            }
        } else {
            int index = new Random().nextInt(GOVT_NAMES.length);
            name.append(GOVT_NAMES[index]).append(" ");
        }

        if(placeName == null) {
            int placeIndex = new Random().nextInt(PLACE_NAMES.length);
            name.append("of " + PLACE_NAMES[placeIndex]);
        } else {
            name.append("of " + placeName);
        }

        return name.toString().trim();
    }
}
