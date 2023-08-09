package net.sudologic.empires.states.gameplay;

import net.sudologic.empires.states.gameplay.util.TerritoryManager;

import java.awt.*;
import java.util.ArrayList;

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
    private static int maxAge = 0;
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
        for(Pixel p : neighbors) {
            if(p.getEmpire() == old && Math.random() < 0.5) {
                e.addTerritory(p);
                p.setStrength((float) (p.getHabitability() * 20));
            }
        }
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
        age += 1f;
        if(age > maxAge) {
            maxAge = (int) Math.ceil(age);
        }
        g.setColor(getColor(colorMode));
        g.fillRect(x * scale, y * scale, scale, scale);
    }

    public void strengthPhase() {
        if (neighbors == null) {
            neighbors = gameState.getNeighbors(x, y);
        }
        if (getEmpire() != null) {
            strength += habitability;
            strength *= 0.99;
        }
    }

    public void attackPhase() {
        borderFriction = 0;
        if(getEmpire() != null) {
            Pixel target = null;
            float bestStrength = 0;
            Empire empire = tm.getEmpireForPixel(this);
            for (Pixel p : neighbors) {
                Empire pEmpire = tm.getEmpireForPixel(p);
                if (p.isHabitable()) {
                    if (pEmpire == null && (strength - ((1 - p.habitability) * 3)) > bestStrength) {
                        //System.out.println("Empire is null!");
                        target = p;
                        bestStrength = strength - ((1 - p.habitability) * 3);
                    } else if (empire.getEnemies().contains(pEmpire) && (strength - (p.strength * 3)) > bestStrength) {
                        target = p;
                        bestStrength = strength - (p.strength * 3);
                    }
                    if (pEmpire != null) {
                        if (pEmpire != empire) {
                            if(empire.getAllies().contains(pEmpire)) {
                                borderFriction += (Math.abs(strength - p.strength) / 5) * ((255 - empire.getCoopIso()) / 255);
                            } else {
                                borderFriction += Math.abs(strength - p.strength) * ((255 - empire.getCoopIso()) / 255);
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
                        }
                    }
                }
            }
            if(target != null) {
                if(bestStrength > 0) {
                    if(target.getEmpire() != null) {
                        strength -= target.getStrength();
                    } else {
                        strength -= 1 - target.getHabitability();
                    }
                    getEmpire().addTerritory(target);
                    target.setStrength(strength * 0.5f);
                    strength *= 0.5f;
                    target.setAge(0);
                } else {
                    target.setStrength((float) (target.getStrength() - (0.5 * strength)));
                    strength = (float) (strength - (target.getStrength() * 0.5));
                }
            }
        }
    }

    public void needPhase() {
        friendlyNeighbors = new ArrayList<>();
        need *= 0.9;
        if(getEmpire() != null) {
            for(Pixel p : neighbors) {
                if(p.getEmpire() == getEmpire() || getEmpire().getAllies().contains(p.getEmpire())) {
                    friendlyNeighbors.add(p);
                } else {
                    if(p.isHabitable()) {
                        if(getEmpire().getEnemies().contains(p.getEmpire())) {
                            need += 63;
                        } else {
                            need += 7;
                        }
                    } else {
                        need += 3;
                    }
                }
            }
            if(getEmpire().getCapital() == this) {
                need += 7;
            }
            if(need > 255) {
                need = 255;
            }
        }
    }

    public void needSpreadPhase() {
        float maxNeed = need;
        for(Pixel p : friendlyNeighbors) {
            if(p.need > maxNeed) {
                maxNeed = p.need;
            }
        }
        if(maxNeed * 0.9f > need) {
            need = maxNeed * 0.9f;
        }
    }

    public void resourcePhase() {
        float totalNeed = need;
        float maxNeed = need;
        for(Pixel p : friendlyNeighbors) {
            totalNeed += p.need;
            if(p.need > maxNeed) {
                maxNeed = p.need;
            }
        }
        if(!friendlyNeighbors.isEmpty() && maxNeed > need) {
            float factor = strength / totalNeed;
            for(Pixel p : friendlyNeighbors) {
                p.strength += p.need * factor;
            }
            strength *= need / totalNeed;
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
        gameState.addMissile(new Missile(tm.getEmpireForPixel(this), x, y, gameState, strength));
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
                    int r = (empire.getColor().getRed() * s) / 255;
                    if (r > 255)
                        r = 255;
                    if (r < 0)
                        r = 0;
                    int g = (empire.getColor().getGreen() * s) / 255;
                    if (g > 255)
                        g = 255;
                    if (g < 0)
                        g = 0;
                    int b = (empire.getColor().getBlue() * s) / 255;
                    if (b > 255)
                        b = 255;
                    if (b < 0)
                        b = 0;
                    return new Color(r, g, b);
                }
            case ideology:
                if (empire != null) {
                    return empire.getIdeologyColor();
                }
            case need:
                if (empire != null) {
                    int n = (int) need;
                    int r = (empire.getColor().getRed() * n) / 255;
                    if (r > 255)
                        r = 255;
                    int g = (empire.getColor().getGreen() * n) / 255;
                    if (g > 255)
                        g = 255;
                    int b = (empire.getColor().getBlue() * n) / 255;
                    if (b > 255)
                        b = 255;
                    return new Color(r, g, b);
                }
            case age:
                if(empire != null) {
                    int a = (int) age;
                    if(a > maxAge && maxAge < 2048) {
                        a = maxAge;
                    }
                    float hue = (float) a / maxAge * 120;  // 120 degrees covers the range from red to green
                    float saturation = 1.0f;
                    float brightness = 1.0f;

                    return Color.getHSBColor(hue / 360, saturation, brightness);
                }
            case friction:
                if(empire != null) {
                    int f = (int) borderFriction;
                    int r = (empire.getColor().getRed() * f) / 255;
                    if (r > 255)
                        r = 255;
                    int g = (empire.getColor().getGreen() * f) / 255;
                    if (g > 255)
                        g = 255;
                    int b = (empire.getColor().getBlue() * f) / 255;
                    if (b > 255)
                        b = 255;
                    return new Color(r, g, b);
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
