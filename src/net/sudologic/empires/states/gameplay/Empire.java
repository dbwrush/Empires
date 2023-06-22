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

    private GameState gameState;

    private Color color;

    private Pixel capital;

    public Empire(GameState gameState) {
        ideology = new double[]{Math.random() * 255, Math.random() * 255, Math.random() * 255};
                                //CoopIso      AuthLib        LeftRight
        name = EmpireNameGenerator.generateEmpireName((int) ideology[0], (int) ideology[1], (int) ideology[2]);
        color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
        territory = new ArrayList<>();
        enemies = new ArrayList<>();
        allies = new ArrayList<>();
        technology = Math.random();
        this.gameState = gameState;
    }

    public void tick() {
        if(capital.getEmpire() != this && territory.size() > 0) {//capitulate!
            Pixel p = territory.get((int) (Math.random() * territory.size()));
            p.revolt();
            mergeInto(capital.getEmpire());
        }
        if(Math.random() < 0.01) {
            technology *= 1.1;
        }
        while(territory.contains(null)) {
            territory.remove(null);
        }
        if(maxSize > 0 && Math.random() > territory.size() / maxSize && territory.size() > 10) {
            Pixel p = territory.get((int) (Math.random() * territory.size()));
            if(p == null) {
                removeTerritory(null);
            } else {
                if(Math.random() < 0.1) {
                    p.revolt();
                } else if(Math.random() < 0.1) {
                    for(Empire e : allies) {
                        if(ideoDifference(e) < (getCoopIso() + e.getCoopIso()) * (6 * Math.random())) {
                            mergeInto(e);
                        }
                    }
                }
            }

        }
        if(territory.size() > maxSize) {
            maxSize = territory.size();
        }
        for(Empire e : allies) {
            //System.out.println(name + " considers merging into " + e.getName());
            if(ideoDifference(e) < (getCoopIso() + e.getCoopIso()) * (4 * Math.random())) {
                mergeInto(e);
            }
        }
    }

    public Pixel getCapital() {
        return capital;
    }

    public void setCapital(Pixel capital) {
        this.capital = capital;
    }

    public void removeTerritory(Pixel pixel) {
        if(territory.contains(pixel)) {
            if(pixel == null) {
                territory.remove(null);
                return;
            }
            if(pixel.getEmpire() == this) {
                pixel.setEmpire(null);
            }
            territory.remove(pixel);
        }
    }

    public void mergeInto(Empire e) {
        if(e.getTerritory().size() > territory.size()) {
            System.out.println(name + " is merging into " + e.getName());
            for(Pixel p : territory) {
                if(p != null && p.getEmpire() == this) {
                    p.setEmpire(e);
                }
            }
            territory.clear();
        } else {
            System.out.println(e.getName() + " is merging into " + name);
            for(Pixel p : e.getTerritory()) {
                if(p != null && p.getEmpire() == e) {
                    p.setEmpire(this);
                }
            }
            e.getTerritory().clear();
        }
    }

    public void render(Graphics g) {
        if(capital != null) {
            g.setColor(Color.white);
            g.drawString(name, (capital.getX() - name.length()) * gameState.getScale(), capital.getY() * gameState.getScale());
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
