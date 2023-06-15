package net.sudologic.empires.states.gameplay;

import java.awt.*;
import java.util.ArrayList;

public class Empire {
    public String name;
    public ArrayList<Empire> allies, enemies;

    private Color color;

    public Empire() {

    }

    public Color getColor() {
        return color;
    }


}
