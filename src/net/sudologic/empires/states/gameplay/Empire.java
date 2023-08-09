package net.sudologic.empires.states.gameplay;

import net.sudologic.empires.Game;
import net.sudologic.empires.states.gameplay.util.EmpireNameGenerator;
import net.sudologic.empires.states.gameplay.util.TerritoryManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Empire {
    private String name;
    private ArrayList<Empire> allies, enemies;
    private double[] ideology;

    private int maxSize = 0;
    private static float mergeDifficulty = 0.03f;
    private static float allianceDifficulty = 1.2f;

    public static TerritoryManager tm;

    private GameState gameState;

    private Color color;

    private Pixel capital;

    public Empire(GameState gameState) {
        ideology = new double[]{Math.random() * 255, Math.random() * 255, Math.random() * 255};
                                //CoopIso      AuthLib        LeftRight
        name = EmpireNameGenerator.generateEmpireName((int) ideology[0], (int) ideology[1], (int) ideology[2], null);
        color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
        tm = gameState.getTerritoryManager();
        tm.addEmpire(this);
        enemies = new ArrayList<>();
        allies = new ArrayList<>();
        this.gameState = gameState;
    }

    public Empire(GameState gameState, String oldName, double[] ideology) {
        this.ideology = ideology;
        //CoopIso      AuthLib        LeftRight
        String[] p = oldName.split(" ");
        this.name = EmpireNameGenerator.generateEmpireName((int) ideology[0], (int) ideology[1], (int) ideology[2], p[p.length - 1]);
        color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
        tm = gameState.getTerritoryManager();
        tm.addEmpire(this);
        enemies = new ArrayList<>();
        allies = new ArrayList<>();
        this.gameState = gameState;
    }

    public void tick() {
        if (getTerritory().size() == 0) {
            return;
        }
        if (enemies.size() == 0 && Math.random() < 0.01) {
            ideology[0] = ideology[0] * 0.9f;
        }
        if (Math.random() < 0.01 * allies.size()) {
            ideology[0] = ideology[0] * 1.1f;
            if(ideology[0] > 255) {
                ideology[0] = 255;
            }
        }
        if (tm.getEmpireForPixel(capital) != this) {
            if(Math.random() < 0.3 && getTerritory().size() > 0) {
                crisisChance();
            } else if(Math.random() < 0.7) {
                puppet(tm.getEmpireForPixel(capital));
            }
            if(getTerritory().size() > 0) {
                capital = getTerritory().get(0);
            }
        }
        while (getTerritory().contains(null)) {
            removeTerritory(null);
        }
        if(maxSize > 0 && Math.random() < (double) (maxSize - getTerritory().size()) / getTerritory().size() && getTerritory().size() > 0) {
        //if (maxSize > 0 && Math.random() > territory.size() / (maxSize * 0.66f) && territory.size() > 10) {
            crisisChance();
        }

        if (getTerritory().size() > maxSize) {
            maxSize = getTerritory().size();
        }
        for (Empire e : allies) {
            if (ideoDifference(e) < (getCoopIso() + e.getCoopIso()) * (4 * Math.random()) * mergeDifficulty) {
                if(getTerritory().size() > e.getTerritory().size()) {
                    e.mergeInto(this);
                } else {
                    mergeInto(e);
                }
            }
        }
        ArrayList<Empire> deadEmpires = new ArrayList<>();
        for (Empire e : enemies) {
            if (!gameState.getEmpires().contains(e)) {
                deadEmpires.add(e);
            }
            allies.remove(e);
            e.allies.remove(this);
            if(!e.enemies.contains(this)) {
                e.enemies.add(this);
            }
        }
        for(Empire e : allies) {
            if(!gameState.getEmpires().contains(e)) {
                deadEmpires.add(e);
            }
            if(!e.getAllies().contains(this)) {
                deadEmpires.add(e);
            }
            for(Empire enemy : e.getEnemies()) {
                if(allies.contains(enemy)) {
                    if(ideoDifference(e) < ideoDifference(enemy)) {
                        deadEmpires.add(enemy);
                        enemy.getAllies().remove(this);
                    } else {
                        deadEmpires.add(e);
                        e.getAllies().remove(this);
                    }
                }
            }
        }
        for (Empire e : deadEmpires) {
            enemies.remove(e);
            allies.remove(e);
        }
    }

    public double[] getIdeology() {
        return ideology;
    }

    public void crisisChance() {
        Pixel p = getTerritory().get((int) (Math.random() * getTerritory().size()));
        if (p == null) {
            removeTerritory(null);
        } else {
            if (Math.random() < 0.01) {
                setEnemy(p.revolt(), true, true);
            } else if (Math.random() < 0.1) {
                for (Empire e : allies) {
                    if (ideoDifference(e) < (getCoopIso() + e.getCoopIso()) * (4 * Math.random()) * mergeDifficulty) {
                        if(getTerritory().size() > e.getTerritory().size()) {
                            e.mergeInto(this);
                        } else {
                            mergeInto(e);
                        }

                        return;
                    }
                }
            }
        }
    }

    public void puppet(Empire e) {
        ideology[0] = (ideology[0] + e.ideology[0]) / 2;
        ideology[1] = (ideology[1] + e.ideology[1]) / 2;
        ideology[2] = (ideology[2] + e.ideology[2]) / 2;
        makePeace(e);
        setAlly(e);
    }

    public Pixel getCapital() {
        return capital;
    }

    public void setCapital(Pixel capital) {
        this.capital = capital;
    }

    public void removeTerritory(Pixel pixel) {
        if(tm.getPixelsForEmpire(this).contains(pixel)) {
            tm.removePixelFromEmpire(pixel);
        }
    }

    public void mergeInto(Empire e) {
        if(!gameState.getEmpires().contains(e) || e.getTerritory().size() == 0) {
            return;
        }
        //System.out.println(name + " is merging into " + e.getName());
        Pixel[] pixels = getTerritory().toArray(Pixel[]::new);
        for(Pixel p : pixels) {
            if(p != null && tm.getEmpireForPixel(p) == this) {
                e.addTerritory(p);
                p.setAge(0);
            }
        }
    }

    public void render(Graphics g) {
        g.setColor(Color.white);
        if(capital == null) {
            return;
        }
        int x = (int) (capital.getX() - (name.length() * 0.66f));
        int y = capital.getY();
        if(x < 2) {
            x = 2;
        }
        if(x > gameState.getWidth()) {
            x = (int) (gameState.getWidth() - (name.length() * 0.4f));
        }
        if(y < 2) {
            y = 2;
        }
        g.drawString(name, x * gameState.getScale(), y * gameState.getScale());
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

    public void setEnemy(Empire e, boolean recur, boolean log) {
        if(e == null || e == this) {
            return;
        }
        double coopIso = (float) ((ideology[0] + e.getCoopIso()) / 2);
        double ideoDiff = ideoDifference(e);
        if(allies.contains(e) && coopIso < ideoDiff * 0.16f * Math.random()) {
            allies.remove(e);
            e.allies.remove(this);
            setEnemy(e, false, false);
        }
        if(!enemies.contains(e)) {
            //System.out.println(name + " is now an enemy of " + e.getName());
            enemies.add(e);
            e.setEnemy(this, false, false);
            if(recur) {
                for(Empire a : allies) {
                    a.setEnemy(e, false, true);
                }
            }
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
        if(e == this) {
            return;
        }
        for(Empire a : allies) {
            if(a.getEnemies().contains(e)) {
                return;
            }
        }
        for(Empire a : e.getAllies()) {
            if(a.getEnemies().contains(this)) {
                return;
            }
        }
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
        tm.addPixelToEmpire(pixel,this);
    }

    public List<Pixel> getTerritory() {
        return tm.getPixelsForEmpire(this);
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
        double[] colors = ideology;
        for(int i = 0; i < colors.length; i++) {
            if(colors[i] > 255) {
                colors[i] = 255;
            }
        }
        return new Color((int) (colors[0]), (int) (colors[1]), (int) (colors[2]));
    }

    public ArrayList<Empire> getAllies() {
        return allies;
    }

    public static float getAllianceDifficulty() {
        return allianceDifficulty;
    }
}
