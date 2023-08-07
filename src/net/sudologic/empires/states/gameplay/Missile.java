package net.sudologic.empires.states.gameplay;

import java.awt.*;
import java.util.ArrayList;

public class Missile {
    private Empire empire;
    private double xRate, yRate, dist, strength;
    private Pixel target;
    private double x, y;
    private GameState gs;

    public Missile(Empire empire, int x, int y, GameState gs, double strength) {
        //System.out.println(empire.getName() + " launched a missile!");
        this.empire = empire;
        this.x = x;
        this.y = y;
        this.gs = gs;
        this.strength = strength;

        if(empire.getEnemies().size() == 0) {
            gs.removeMissile(this);
            return;
        }
        Empire enemy = empire.getEnemies().get((int) (Math.random() * empire.getEnemies().size()));

        if(enemy.getTerritory().size() == 0) {
            gs.removeMissile(this);
            return;
        }

        pickTarget(enemy);
    }

    public void pickTarget(Empire enemy) {
        int size = enemy.getTerritory().size();
        int firstpart = (int) (size * 0.90);
        int secondpart = (int) (size * 0.01);
        if(Math.random() < 0.2) {
            firstpart = (int) (size * 0.3);
        }

        target = enemy.getTerritory().get((int) (secondpart * Math.random()) + firstpart);

        int xDist = (int) (target.getX() - x);
        int yDist = (int) (target.getY() - y);

        dist = Math.sqrt((xDist * xDist) + (yDist *yDist));

        if(dist > strength) {
            gs.removeMissile(this);
        }

        xRate = xDist / dist;
        yRate = yDist / dist;
    }

    public void tick() {
        x += xRate;
        y += yRate;

        if(target == null || target.getEmpire() == null) {
            gs.removeMissile(this);
            return;
        }

        int xDist = (int) (target.getX() - x);
        int yDist = (int) (target.getY() - y);

        dist = Math.sqrt((xDist * xDist) + (yDist *yDist));

        if(dist <= 0.5) {
            target.setStrength((float) (target.getStrength() * 0.01));
            //target.setHabitability((float) (target.getHabitability() * 0.9));
            for(Pixel p : target.getNeighbors()) {
                if(p.getEmpire() == target.getEmpire()) {
                    p.setStrength((float)(p.getStrength() * 0.02));
                    //target.setHabitability((float) (target.getHabitability() * 0.95));
                }
            }
            gs.removeMissile(this);
            return;
        }
    }

    public void render(Graphics g, int scale) {
        //System.out.println("Rendered missile!");
        g.setColor(Color.RED);
        g.fillRect((int) (x * scale), (int) (y * scale), scale, scale);
    }
}
