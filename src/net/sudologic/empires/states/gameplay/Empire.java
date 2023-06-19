package net.sudologic.empires.states.gameplay;

import net.sudologic.empires.states.gameplay.util.EmpireNameGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class Empire {
    private String name;
    private ArrayList<Empire> allies, enemies;

    private ArrayList<Pixel> territory;
    private double[] ideology;

    private double technology;

    private int maxSize = 0;

    private Color color;

    public Empire() {
        name = EmpireNameGenerator.generateEmpireName();
        ideology = new double[]{Math.random() * 255, Math.random() * 255, Math.random() * 255};
                                //CoopIso      AuthLib        LeftRight
        color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
        territory = new ArrayList<>();
        enemies = new ArrayList<>();
        allies = new ArrayList<>();
        technology = Math.random();
    }

    public void tick() {
        if(Math.random() < 0.01) {
            technology *= 1.1;
        }
        if(maxSize > 0 && Math.random() > territory.size() / maxSize && territory.size() > 0) {
            Pixel p = territory.get((int) (Math.random() * territory.size()));
            p.revolt();
        }
        if(territory.size() > maxSize) {
            maxSize = territory.size();
        }
    }

    public void removeTerritory(Pixel pixel) {
        if(territory.contains(pixel)) {
            territory.remove(pixel);
        }
    }

    public void setTechnology(double technology) {
        this.technology = technology;
    }

    public double getTechnology() {
        return technology;
    }

    public double getCoopIso() {
        return ideology[0];
    }

    public double getAuthLib() {
        return ideology[1];
    }

    public double getLeftRight() {
        return ideology[2];
    }

    public void setEnemy(Empire e) {
        if(allies.contains(e)) {
            allies.remove(e);
            e.allies.remove(this);
        }
        if(!enemies.contains(e)) {
            //System.out.println(name + " is now an enemy of " + e.getName());
            enemies.add(e);
            e.enemies.add(this);
        }
    }

    public void makePeace(Empire e) {
        if(enemies.contains(e)) {
            enemies.remove(e);
            e.enemies.remove(this);
            //System.out.println(name + " made peace with " + e.getName());
        }
    }

    public void setAlly(Empire e) {
        makePeace(e);
        if(allies.contains(e)) {
            return;
        }
        allies.add(e);
        e.allies.add(this);
        //System.out.println(name + " is now allied with " + e.getName());
    }

    public double ideoDifference(Empire e) {
        double total = 0;
        for(int i = 0; i < ideology.length; i++) {
            total += Math.abs(ideology[i] - e.ideology[i]);
        }
        return total;
    }

    public void addTerritory(Pixel pixel) {
        //System.out.println(name + " gained territory!");
        if(!territory.contains(pixel)) {
            territory.add(pixel);
            if(pixel.getEmpire() != null) {
                pixel.getEmpire().removeTerritory(pixel);
            }
            pixel.setEmpire(this);
        }
    }

    public ArrayList<Pixel> getTerritory() {
        return territory;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }


    public ArrayList<Empire> getEnemies() {
        return enemies;
    }

    public Color getIdeologyColor() {
        return new Color((int) (ideology[0]), (int) (ideology[1]), (int) (ideology[2]));
    }

    public ArrayList<Empire> getAllies() {
        return allies;
    }
}
