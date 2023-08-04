package net.sudologic.empires.states.gameplay;

import net.sudologic.empires.states.gameplay.util.EmpireNameGenerator;
import net.sudologic.empires.states.gameplay.util.TerritoryManager;

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
        friction,
        alliance,
        perspective,
        habitability
    }

    private int x, y, scale;
    private static int maxAge = 2048;
    private static TerritoryManager tm;
    private float strength, borderFriction, habitability, need, age;
    private GameState gameState;
    private ArrayList<Pixel> neighbors, friendlyNeighbors;

    public Pixel(int x, int y, float habitability, int scale, GameState gameState) {
        this.gameState = gameState;
        this.habitability = Math.max(habitability, 0);
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.need = 0;
        tm = gameState.getTerritoryManager();
    }

    public Empire revolt() {
        Empire old = tm.getEmpireForPixel(this);

        Empire e = new Empire(gameState, old.getName());
        e.addTerritory(this);
        this.age = 0;
        strength = habitability * 20;
        e.setEnemy(old, true, true);
        old.setEnemy(e, true, true);
        e.setCapital(this);
        //System.out.println("Revolt in " + old.getName() + ", " + empire.getName() + " has formed.");
        return e;
    }

    public Empire getEmpire() {
        return tm.getEmpireForPixel(this);
    }

    public double getHabitability() {
        return habitability;
    }

    public void setHabitability(float habitability) {
        this.habitability = habitability;
    }

    public void render(Graphics g, ColorMode colorMode) {
        age += 0.5f;
        g.setColor(getColor(colorMode));
        g.fillRect(x * scale, y * scale, scale, scale);
    }

    public void tick() {
        friendlyNeighbors = new ArrayList<>();
        if (tm.getEmpireForPixel(this) != null) {
            Empire empire = tm.getEmpireForPixel(this);
            borderFriction = 0;
            strength += habitability;
            if(strength > 255) {
                strength = 255;
            }
            float tneed = 0;
            if(neighbors == null) {
                neighbors = gameState.getNeighbors(x, y);
            }
            if(empire.getCapital() == this) {
                tneed += 4;
            }
            for (Pixel p : neighbors) {
                Empire pEmpire = tm.getEmpireForPixel(p);
                if (p.isHabitable()) {
                    if (( 1 - p.habitability) * 3 < strength) {
                        if (pEmpire == null) {
                            //System.out.println("Empire is null!");
                            tneed += 40f;
                            empire.addTerritory(p);
                            p.setAge(0);
                            strength -= p.habitability;
                        } else if (empire.getEnemies().contains(pEmpire) && p.strength * 3 < strength) {
                            empire.addTerritory(p);
                            p.setAge(0);
                            strength -= p.strength;
                            p.strength = strength / 2;
                            strength /= 2;
                        }
                    }
                    if (pEmpire != null) {
                        if (pEmpire != empire) {
                            if(empire.getAllies().contains(pEmpire)) {
                                borderFriction += (strength + p.strength) / 5 * habitability;
                            } else {
                                borderFriction += (strength + p.strength) / 2 * habitability;
                            }
                            float ideoDiff = (float) empire.ideoDifference(pEmpire);
                            float coopIso = (float) ((empire.getCoopIso() + pEmpire.getCoopIso()) / 4);
                            if (ideoDiff < coopIso * Empire.getAllianceDifficulty()) {
                                empire.setAlly(pEmpire);
                            }
                            if (borderFriction > gameState.getWarThreshold() && coopIso < ideoDiff * 0.33f * Math.random() && !empire.getEnemies().contains(pEmpire)) {
                                empire.setEnemy(pEmpire, true, true);
                            }
                            if (empire.getEnemies().contains(pEmpire) && ((ideoDiff + (borderFriction / 5)) * 2 < gameState.getWarThreshold())) {
                                empire.makePeace(pEmpire);
                            }
                            if (!empire.getAllies().contains(pEmpire)) {
                                tneed += 10f;
                            } else {
                                tneed += 1f;
                            }
                        }
                        if (empire.getEnemies().contains(pEmpire)) {
                            tneed += 255f;
                        }
                        if (pEmpire == empire || empire.getAllies().contains(pEmpire)) {
                            friendlyNeighbors.add(p);
                        }
                    }
                } else {
                    tneed += 1f;
                }
                tneed -= strength;
                if(tneed <= 0) {
                    tneed = 0;
                }
            }
            float totalNeed = tneed;
            float maxNeed = 0;
            for (Pixel p : friendlyNeighbors) {
                totalNeed += p.need;
                if(p.need > maxNeed) {
                    maxNeed = p.need;
                }
            }
            if (!friendlyNeighbors.isEmpty()) {
                float factor = strength / totalNeed;
                for (Pixel p : friendlyNeighbors) {
                    p.strength += p.need * factor;
                }
                strength *= tneed / totalNeed;
            }
            
            if(tneed < maxNeed * 0.9) {
                need = maxNeed * 0.9f;
            } else {
                need = tneed;
            }
        }
    }

    public void spawnBoat() {
        if (tm.getEmpireForPixel(this) == null) {
            return;
        }
        for (Pixel p : neighbors) {
            if (!p.isHabitable()) {
                gameState.addBoat(new Boat(tm.getEmpireForPixel(this), strength / 2, p.getX(), p.getY(), gameState, Math.random() * 8));
                strength = strength / 2;
                //System.out.println("Successfully spawned boat");
                return;
            }
        }
    }

    public void spawnMissile() {
        if(tm.getEmpireForPixel(this) == null) {
            return;
        }
        gameState.addMissile(new Missile(tm.getEmpireForPixel(this), x, y, gameState));
    }

    public void spawnParatrooer() {
        if(tm.getEmpireForPixel(this) == null) {
            return;
        }
        gameState.addParatrooper(new Paratrooper(tm.getEmpireForPixel(this), x, y, gameState, strength * 0.9));
        strength *= 0.1;
    }

    public ArrayList<Pixel> getNeighbors() {
        return neighbors;
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
        Empire empire = tm.getEmpireForPixel(this);
        if(empire != null && empire.getCapital() == this) {
            return empire.getColor();
        }
        switch (colorMode) {
            case empire:
                if (empire != null) {
                    return empire.getColor();
                }
            case strength:
                if (empire != null) {
                    int s = (int) strength;
                    if(friendlyNeighbors != null) {
                        for(Pixel p : friendlyNeighbors) {
                            s += p.getStrength();
                        }
                        if(friendlyNeighbors.size() > 0) {
                            s /= friendlyNeighbors.size();
                        }
                    }
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
                    if(a > maxAge) {
                        a = maxAge;
                    }
                    float hue = (float) a / maxAge * 120;  // 120 degrees covers the range from red to green
                    float saturation = 1.0f;
                    float brightness = 1.0f;

                    return Color.getHSBColor(hue / 360, saturation, brightness);
                }
            case friction:
                if(empire != null) {
                    int f = (int) (borderFriction - empire.getCoopIso()) * 3;
                    if(f > 255) {
                        f = 255;
                    }
                    if(f < 0) {
                        f = 0;
                    }
                    return new Color(f, 0, 0);
                }
            case alliance:
                if(empire != null) {
                    float g = 0;
                    if(friendlyNeighbors != null) {
                        g = friendlyNeighbors.size();
                    }
                    float r = 8 - g;
                    g = (g/8);
                    r = (r/8);

                    return new Color(r, g, 0);
                }
            case perspective:
                if(empire != null) {
                    int r = (8 - friendlyNeighbors.size()) * 255;
                    r = (r/8);
                    int g = 255 - ((int) empire.ideoDifference(gameState.getPerspectiveEmpire()) / 3);
                    Color base = new Color(r, g, 0);
                    if(gameState.getPerspectiveEmpire() == empire) {
                        base = Color.YELLOW;
                    }
                    if(gameState.getPerspectiveEmpire().getEnemies().contains(empire)) {
                        base = Color.RED;
                    }
                    if(gameState.getPerspectiveEmpire().getAllies().contains(empire)) {
                        base = Color.CYAN;
                    }

                    return new Color((base.getRed() + r) / 2,(base.getGreen() + g) / 2, base.getBlue());
                }
            case habitability:
                if(habitability == 0) {
                    return new Color(0, 0, 100);
                }
                return new Color(0, (int) (habitability * 200), 0);
            default:
                if (habitability == 0) {
                    return new Color(0, 0, 100);
                } else {
                    return new Color(0, (int) (habitability * 200), 0);
                }
        }
    }

    public float getAge() {
        return age;
    }

    public void setAge(float age) {
        this.age = age;
    }
}
