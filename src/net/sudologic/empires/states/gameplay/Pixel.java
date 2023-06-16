package net.sudologic.empires.states.gameplay;

import java.awt.*;
import java.util.ArrayList;

public class Pixel {

    public enum ColorMode {
        empire,
        strength,
        ideology,
        need
    }

    private int x, y, scale;
    private float strength, borderFriction, habitability, need;
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

    public void setEmpire(Empire empire) {
        this.empire = empire;
        neighbors = gameState.getNeighbors(x, y);
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
            strength += habitability;
            if(strength > 255) {
                strength = 255;
            }
            friendlyNeighbors = new ArrayList<>(neighbors.size());
            float tneed = 1;
            for (Pixel p : neighbors) {
                if (p.isHabitable()) {
                    if (p.habitability < strength) {
                        if (p.empire == null) {
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
            default:
                if (habitability == 0) {
                    return new Color(0, 0, 100);
                } else {
                    return new Color(0, (int) (habitability * 200), 0);
                }
        }
    }
}
