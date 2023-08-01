package net.sudologic.empires.states.gameplay;

import java.awt.*;
import java.util.ArrayList;

public class Missile {
    private Empire empire;
    private double xRate, yRate, dist;
    private Pixel target;
    private double x, y;
    private GameState gs;

    public Missile(Empire empire, double range, int x, int y, GameState gs) {
        //System.out.println(empire.getName() + " launched a missile!");
        this.empire = empire;
        this.x = x;
        this.y = y;
        this.gs = gs;

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

        for(int i = 0; i < 5; i++) {
            if(dist > range) {
                pickTarget(enemy);
            } else {
                break;
            }
        }
    }

    public void pickTarget(Empire enemy) {
        target = enemy.getTerritory().get((int) (enemy.getTerritory().size() * Math.random()));

        int xDist = (int) (target.getX() - x);
        int yDist = (int) (target.getY() - y);

        dist = Math.sqrt((xDist * xDist) + (yDist *yDist));

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
            target.setStrength((float) (target.getStrength() * 0.1));
            target.setHabitability((float) (target.getHabitability() * 0.9));
            if(target.getStrength() < 25) {
                target.getEmpire().removeTerritory(target);
            }
            for(Pixel p : target.getNeighbors()) {
                if(p.getEmpire() == target.getEmpire()) {
                    p.setStrength((float)(p.getStrength() * 0.2));
                    target.setHabitability((float) (target.getHabitability() * 0.95));
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
