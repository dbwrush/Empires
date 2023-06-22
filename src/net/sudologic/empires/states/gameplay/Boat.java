package net.sudologic.empires.states.gameplay;

import java.awt.*;
import java.util.ArrayList;

public class Boat {
    private Empire empire;
    private double strength;
    private int x, y;

    private GameState gs;

    public Boat(Empire empire, double strength, int x, int y, GameState gs) {
        //System.out.println(empire.getName() + " launched a boat!");
        this.empire = empire;
        this.strength = strength;
        this.x = x;
        this.y = y;
        this.gs = gs;
    }

    public void tick() {
        strength *= 0.999;
        if(empire.getTerritory().size() == 0) {
            gs.removeBoat(this);
        }

        ArrayList<Pixel> neighbors = (ArrayList<Pixel>) gs.getNeighbors(x, y);
        Pixel target = neighbors.get((int) (Math.random() * neighbors.size()));
        if(!target.isHabitable()) {
            x = target.getX();
            y = target.getY();
        } else {
            if(target.getEmpire() == null) {
                if(target.getHabitability() < strength) {
                    //System.out.println("Boat discovered territory for " + empire.getName());
                    empire.addTerritory(target);
                    target.setStrength((float) (strength - target.getHabitability()));
                }
                //System.out.println("Boat landed");
            } else if(empire.getEnemies().contains(target.getEmpire())) {
                if(target.getStrength() < strength || Math.random() < 0.3) {
                    //System.out.println("Boat captured territory for " + empire.getName());
                    target.setStrength((float) strength);
                    empire.addTerritory(target);
                }
                //System.out.println("Boat landed");
                empire.addTerritory(target);
            } else {
                target.setStrength((float) (target.getStrength() + strength));
            }
            gs.removeBoat(this);
        }
        if(strength <= 10) {
            //System.out.println("Boat died");
            gs.removeBoat(this);
        }
    }

    public void render(Graphics g, int scale) {
        g.setColor(empire.getColor());
        g.fillRect(x * scale, y * scale, scale, scale);
    }
}
