package net.sudologic.empires.states.gameplay;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Pixel {

    public enum ColorMode {
        empire,
        strength,
        ideology,
        need,
        age,
        friction
    }

    private int x, y, scale;
    private static int maxAge = 255;
    private float strength, borderFriction, habitability, need, age;
    private Empire empire;
    private GameState gameState;
    private ArrayList<Pixel> neighbors, friendlyNeighbors;

    public Pixel(int x, int y, float habitability, int scale, GameState gameState) {
        this.gameState = gameState;
        this.habitability = Math.max(habitability, 0);
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.need = 0;
    }

    public void revolt() {
        Empire old = empire;
        //System.out.println("Revolt in " + empire.getName());
        empire.setTechnology(empire.getTechnology() / 5);
        empire.removeTerritory(this);
        setEmpire(new Empire(gameState));
        empire.addTerritory(this);
        strength = habitability * 20;
        empire.setEnemy(old);
    }

    public void setEmpire(Empire empire) {
        this.empire = empire;
        age = 0;
        neighbors = gameState.getNeighbors(x, y);
        Collections.shuffle(neighbors);
    }

    public Empire getEmpire() {
        return empire;
    }

    public double getHabitability() {
        return habitability;
    }

    public void render(Graphics g, ColorMode colorMode) {
        g.setColor(getColor(colorMode));
        g.fillRect(x * scale, y * scale, scale, scale);
    }

    public void tick() {
        if (empire != null) {
            borderFriction = 0;
            age += 0.5f;
            strength += habitability * empire.getTechnology();
            if(strength > 255 * empire.getTechnology()) {
                strength = (float) (255 * empire.getTechnology());
            }
            friendlyNeighbors = new ArrayList<>();
            float tneed = 1;
            if(neighbors == null) {
                neighbors = gameState.getNeighbors(x, y);
            }
            for (Pixel p : neighbors) {
                if (p.isHabitable()) {
                    if (p.habitability < strength) {
                        if (p.empire == null) {
                            //System.out.println("Empire is null!");
                            tneed = 40f;
                            empire.addTerritory(p);
                            strength -= p.habitability;
                        } else if (empire.getEnemies().contains(p.empire) && p.strength < strength) {
                            empire.addTerritory(p);
                            strength -= p.strength;
                            tneed = 255f;
                            p.strength = strength / 2;
                            strength /= 2;
                        }
                    }
                    if (p.empire != null) {
                        if (p.empire != empire) {
                            float ideoDiff = (float) empire.ideoDifference(p.empire);
                            float coopIso = (float) ((empire.getCoopIso() + p.empire.getCoopIso()) / 4);
                            if (ideoDiff < coopIso) {
                                empire.setAlly(p.empire);
                            }
                            if (borderFriction > gameState.getWarThreshold() && coopIso < ideoDiff && !empire.getEnemies().contains(p.empire)) {
                                empire.setEnemy(p.empire);
                            }
                            if (empire.getEnemies().contains(p.empire) && ((ideoDiff + (borderFriction / 5)) * 2 < gameState.getWarThreshold())) {
                                empire.makePeace(p.empire);
                            }
                            if (!empire.getAllies().contains(p.empire)) {
                                tneed = 20f;
                            } else {
                                tneed = 10f;
                            }
                            borderFriction = (strength + p.strength) / 2;
                        }
                        if (empire.getEnemies().contains(p.empire)) {
                            tneed = 255f;
                        }
                        if (p.empire == empire || empire.getAllies().contains(p.empire)) {
                            friendlyNeighbors.add(p);
                        }
                    }
                } else {
                    tneed = 10f;
                }
            }
            float totalNeed = tneed;
            for (Pixel p : friendlyNeighbors) {
                totalNeed += p.need;
            }
            if (!friendlyNeighbors.isEmpty()) {
                float factor = strength / totalNeed;
                for (Pixel p : friendlyNeighbors) {
                    p.strength += p.need * factor;
                }
                strength *= tneed / totalNeed;
            }
            float avgNeed = totalNeed / (friendlyNeighbors.size() + 1) * 0.999f;
            need = tneed;
            if (avgNeed > tneed) {
                need = avgNeed;
            }
        }
    }

    public void spawnBoat() {
        if (empire == null) {
            return;
        }
        for (Pixel p : neighbors) {
            if (!p.isHabitable() && gameState.addBoat(new Boat(empire, strength, p.getX(), p.getY(), gameState))) {
                strength = 0;
                return;
            }
        }
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public double getStrength() {
        return strength;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isHabitable() {
        return habitability > 0;
    }

    public Color getColor(ColorMode colorMode) {
        switch (colorMode) {
            case empire:
                if (empire != null) {
                    return empire.getColor();
                }
            case strength:
                if (empire != null) {
                    int s = (int) strength;
                    if (s > 255) {
                        s = 255;
                    }
                    if(s < 0) {
                        s = 0;
                    }
                    return new Color(s, 0, 0);
                }
            case ideology:
                if (empire != null) {
                    return empire.getIdeologyColor();
                }
            case need:
                if (empire != null) {
                    int n = (int) need;
                    if (n > 255) {
                        n = 255;
                    }
                    return new Color(n, 0, 0);
                }
            case age:
                if(empire != null) {
                    int a = (int) age;
                    if(a > 255) {
                        a = 255;
                    }
                    float hue = (float) a / maxAge * 120;  // 120 degrees covers the range from red to green
                    float saturation = 1.0f;
                    float brightness = 1.0f;

                    return Color.getHSBColor(hue / 360, saturation, brightness);
                }
            case friction:
                if(empire != null) {
                    int f = (int) (borderFriction - empire.getCoopIso());
                    if(f > 255) {
                        f = 255;
                    }
                    if(f < 0) {
                        f = 0;
                    }
                    return new Color(f, 0, 0);
                }
            default:
                if (habitability == 0) {
                    return new Color(0, 0, 100);
                } else {
                    return new Color(0, (int) (habitability * 200), 0);
                }
        }
    }
}
