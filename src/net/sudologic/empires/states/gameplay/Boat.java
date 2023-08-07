package net.sudologic.empires.states.gameplay;

import java.awt.*;
import java.util.ArrayList;

public class Boat {
    private Empire empire;
    private double strength, direction;
    private int x, y;

    private GameState gs;

    public Boat(Empire empire, double strength, int x, int y, GameState gs, double direction) {
        //System.out.println(empire.getName() + " launched a boat!");
        this.empire = empire;
        this.strength = strength;
        this.x = x;
        this.y = y;
        this.gs = gs;
        this.direction = direction;
    }

    public void tick() {
        //System.out.println("Ticking boat!");
        if(empire.getTerritory().size() == 0) {
            gs.removeBoat(this);
        }

        ArrayList<Pixel> neighbors = gs.getNeighbors(x, y);
        int t = (int) (direction + (Math.random() - 0.5));
        if(t > neighbors.size() - 1) {
            t = neighbors.size() - 1;
        }
        if(neighbors.size() <= 5) {
            strength = 0;
        }
        if(t < 0) {
            t += 8;
        }
        Pixel target = neighbors.get(t);

        if(!target.isHabitable()) {
            x = target.getX();
            y = target.getY();
        } else {
            if(target.getEmpire() == null) {
                if(target.getHabitability() * 3 < strength) {
                    //System.out.println("Boat discovered territory for " + empire.getName());
                    empire.addTerritory(target);
                    target.setAge(0);
                    target.setStrength((float) (strength - target.getHabitability()));
                }
                //System.out.println("Boat landed");
            } else if(empire.getEnemies().contains(target.getEmpire())) {
                if(target.getStrength() * 3 < strength || Math.random() < 0.3) {
                    //System.out.println("Boat captured territory for " + empire.getName());
                    target.setStrength((float) strength);
                    empire.addTerritory(target);
                    target.setAge(0);
                } else {
                    target.setStrength((float) (target.getStrength() - strength));
                }
                //System.out.println("Boat landed");
            } else {
                if(target.getEmpire() != empire) {
                    float ideoDiff = (float) empire.ideoDifference(target.getEmpire());
                    float coopIso = (float) ((empire.getCoopIso() + target.getEmpire().getCoopIso()) / 4);
                    float borderFriction = (float) ((strength + target.getStrength()) / 2);
                    if (ideoDiff < coopIso * Empire.getAllianceDifficulty()) {
                        empire.setAlly(target.getEmpire());
                    } else if(borderFriction > gs.getWarThreshold() && coopIso < ideoDiff && !empire.getEnemies().contains(target.getEmpire())) {
                        empire.setEnemy(target.getEmpire(), true, true);
                    } else if(empire.getEnemies().contains(target.getEmpire()) && ((ideoDiff + (borderFriction / 5)) * 2 < gs.getWarThreshold())) {
                        empire.makePeace(target.getEmpire());
                    }
                }
                target.setStrength((float) (target.getStrength() + strength));
            }
            gs.removeBoat(this);
        }
        if(strength < 1) {
            //System.out.println("Boat died");
            gs.removeBoat(this);
        }
    }

    public void render(Graphics g, int scale) {
        //System.out.println("Rendered boat!");
        g.setColor(empire.getColor());
        g.fillRect(x * scale, y * scale, scale, scale);
    }
}
