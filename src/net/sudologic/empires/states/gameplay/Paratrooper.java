package net.sudologic.empires.states.gameplay;

import java.awt.*;

public class Paratrooper {
    private Empire empire;
    private double xRate, yRate, dist, strength;
    private Pixel target;
    private double x, y;
    private GameState gs;


    public Paratrooper(Empire empire, int x, int y, GameState gs, double strength) {
        //System.out.println(empire.getName() + " launched a missile!");
        this.empire = empire;
        this.x = x;
        this.y = y;
        this.gs = gs;
        this.strength = strength;

        if(empire.getEnemies().size() == 0) {
            gs.removeParatrooper(this);
            return;
        }
        Empire enemy = empire.getEnemies().get((int) (Math.random() * empire.getEnemies().size()));

        if(enemy.getTerritory().size() == 0) {
            gs.removeParatrooper(this);
            return;
        }

        pickTarget(enemy);
    }

    public void pickTarget(Empire enemy) {
        int size = enemy.getTerritory().size();
        int firstpart = (int) (size * 0.00);
        int secondpart = (int) (size * 0.01);
        target = enemy.getTerritory().get((int) (secondpart * Math.random()) + firstpart);

        int xDist = (int) (target.getX() - x);
        int yDist = (int) (target.getY() - y);

        dist = Math.sqrt((xDist * xDist) + (yDist *yDist));

        if(dist > strength) {
            gs.removeParatrooper(this);
        }

        xRate = xDist / dist;
        yRate = yDist / dist;
    }

    public void tick() {
        x += xRate;
        y += yRate;

        if(target == null || target.getEmpire() == null) {
            gs.removeParatrooper(this);
            return;
        }

        int xDist = (int) (target.getX() - x);
        int yDist = (int) (target.getY() - y);

        dist = Math.sqrt((xDist * xDist) + (yDist *yDist));

        if(dist <= 0.5) {
            if(target.getStrength() * 3< strength) {
                empire.addTerritory(target);
                target.setAge(0);
                target.setStrength((float) strength);
            } else {
                target.setStrength((float) (target.getStrength() - strength));
            }
            gs.removeParatrooper(this);
        }
    }

    public void render(Graphics g, int scale) {
        //System.out.println("Rendered missile!");
        g.setColor(Color.GREEN);
        g.fillRect((int) (x * scale), (int) (y * scale), scale, scale);
    }
}
