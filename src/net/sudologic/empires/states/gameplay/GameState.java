package net.sudologic.empires.states.gameplay;

import net.sudologic.empires.Game;
import net.sudologic.empires.input.KeyManager;
import net.sudologic.empires.states.gameplay.util.PerlinNoiseGenerator;
import net.sudologic.empires.states.State;
import net.sudologic.empires.states.gameplay.util.TerritoryManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

public class GameState extends State {

    private ArrayList<Pixel> habitablePixels;

    private TerritoryManager tm;

    private ArrayList<Boat> boats, remBoats;

    private ArrayList<Missile> missiles, remMissiles;
    private ArrayList<Paratrooper> paratroopers, remParatroopers;

    private Pixel[][] pixels;

    private int scale;

    private Empire perspectiveEmpire;

    private double warThreshold;

    private Pixel.ColorMode colorMode;

    private KeyManager km;



    public GameState(Game game, int width, int height, int scale, int numEmpires, double warThreshold, KeyManager km) {
        super(game);
        System.out.println("Switched to GameState");
        this.km = km;
        this.warThreshold = warThreshold;
        this.scale = scale;
        tm = new TerritoryManager();
        habitablePixels = new ArrayList<>();
        while(habitablePixels.size() < ((width / scale) * (height / scale)) / 2) {
            habitablePixels = new ArrayList<>();
            genTerrain(width / scale, height / scale, scale);
        }
        genEmpires(numEmpires);
        boats = new ArrayList<>();
        remBoats = new ArrayList<>();
        missiles = new ArrayList<>();
        remMissiles = new ArrayList<>();
        paratroopers = new ArrayList<>();
        remParatroopers = new ArrayList<>();
        colorMode = Pixel.ColorMode.empire;
        Collections.shuffle(habitablePixels);
        perspectiveEmpire = getEmpires().get(0);
    }

    private void genTerrain(int width, int height, int scale) {
        System.out.println("Generating Terrain");
        pixels = new Pixel[width][height];
        PerlinNoiseGenerator perlin = new PerlinNoiseGenerator(width, height, 0.01f, 4);
        float[][] habitability = perlin.getNoise();
        for(int x = 0; x < pixels.length; x++) {
            for(int y = 0; y < pixels[0].length; y++) {
                //System.out.println("X: " + x + " Y: " + y);
                float h = (habitability[x][y] + 1) / 2;
                if(h < 0.4) {
                    h = 0;
                }
                Pixel p = new Pixel(x, y, h, scale, this);
                if(h > 0) {
                    habitablePixels.add(p);
                }
                pixels[x][y] = p;
            }
        }
        System.out.println("Finished Generating Terrain");
    }

    public ArrayList<Pixel> getHabitablePixels() {
        return habitablePixels;
    }

    public Empire getPerspectiveEmpire() {
        return perspectiveEmpire;
    }

    private void genEmpires(int numEmpires) {
        ArrayList<Empire> empires = (ArrayList<Empire>) tm.getEmpires();
        while(empires.size() <= numEmpires) {
            Empire e = new Empire(this);
            System.out.println("Spawning " + e.getName());
            while(e.getCapital() == null) {
                Pixel p = pixels[(int) (Math.random() * pixels.length)][(int) (Math.random() * pixels[0].length)];
                if(tm.getEmpireForPixel(p) == null && p.isHabitable()) {
                    e.addTerritory(p);
                    e.setCapital(p);
                    e.getCapital().setStrength(2);
                }
            }
            empires.add(e);
        }
    }

    public double getWarThreshold() {
        return warThreshold;
    }

    public void removeBoat(Boat b) {
        remBoats.add(b);
    }

    public void removeMissile(Missile m) {
        remMissiles.add(m);
    }

    public void removeParatrooper(Paratrooper p) {
        remParatroopers.add(p);
    }

    public ArrayList<Pixel> getNeighbors(int x, int y) {
        ArrayList<Pixel> neighbors = new ArrayList<>();

        int leftOne = x - 1;
        int rightOne = x + 1;

        if(leftOne < 0) {
            leftOne = pixels.length - 1;
        }
        if(rightOne > pixels.length - 1) {
            rightOne = 0;
        }

        if(y > 0) {
            neighbors.add(pixels[leftOne][y - 1]);
            neighbors.add(pixels[x][y - 1]);
            neighbors.add(pixels[rightOne][y - 1]);
        }
        neighbors.add(pixels[rightOne][y]);
        if(y < pixels[0].length - 1) {
            neighbors.add(pixels[rightOne][y + 1]);
            neighbors.add(pixels[x][y + 1]);
            neighbors.add(pixels[leftOne][y + 1]);
        }
        neighbors.add(pixels[leftOne][y]);

        return neighbors;
    }

    @Override
    public void tick() {
        if (warThreshold > 0 && Math.random() < 0.01) {
            warThreshold -= 1;
        }
        if(!tm.getEmpires().contains(perspectiveEmpire)) {
            perspectiveEmpire = tm.getEmpires().get(0);
        }
        if (km.isKeyPressed(KeyEvent.VK_1)) {
            colorMode = Pixel.ColorMode.empire;
        }
        if (km.isKeyPressed(KeyEvent.VK_2)) {
            colorMode = Pixel.ColorMode.strength;
        }
        if (km.isKeyPressed(KeyEvent.VK_3)) {
            colorMode = Pixel.ColorMode.ideology;
        }
        if (km.isKeyPressed(KeyEvent.VK_4)) {
            colorMode = Pixel.ColorMode.need;
        }
        if (km.isKeyPressed(KeyEvent.VK_5)) {
            colorMode = Pixel.ColorMode.age;
        }
        if (km.isKeyPressed(KeyEvent.VK_6)) {
            colorMode = Pixel.ColorMode.friction;
        }
        if (km.isKeyPressed(KeyEvent.VK_7)) {
            colorMode = Pixel.ColorMode.alliance;
        }
        if(km.isKeyPressed(KeyEvent.VK_8)) {
            colorMode = Pixel.ColorMode.perspective;
        }
        if(km.isKeyPressed(KeyEvent.VK_9)) {
            colorMode = Pixel.ColorMode.habitability;
        }
        ArrayList<Empire> empires = (ArrayList<Empire>) tm.getEmpires();
        Collections.shuffle(empires);
        for (Empire e : empires) {
            e.tick();
            if (e.getTerritory().size() == 0) {
                tm.removeEmpire(e);
            }
        }

        tickPixels();

        for (Boat b : boats) {
            b.tick();
        }

        for (Boat b : remBoats) {
            boats.remove(b);
        }
        remBoats = new ArrayList<>();

        for (Missile m : missiles) {
            m.tick();
        }

        for(Missile m : remMissiles) {
            missiles.remove(m);
        }
        remMissiles = new ArrayList<>();

        for (Paratrooper p : paratroopers) {
            p.tick();
        }

        for(Paratrooper p : remParatroopers) {
            paratroopers.remove(p);
        }
        remParatroopers = new ArrayList<>();
    }

    private void tickPixels() {
        Collections.shuffle(habitablePixels);
        for (Pixel p : habitablePixels) {
            p.strengthPhase();
            if(Math.random() < 0.01) {
                p.spawnBoat();
            }
            if(Math.random() < 0.001) {
                p.spawnMissile();
            }
            if(Math.random() < 0.0001) {
                p.spawnParatrooer();
            }
        }
        for(Pixel p : habitablePixels) {
            p.attackPhase();
        }
        for(Pixel p : habitablePixels) {
            p.needPhase();
        }
        for(Pixel p : habitablePixels) {
            p.needSpreadPhase();
        }
        for(Pixel p : habitablePixels) {
            p.resourcePhase();
        }
    }
    public int getScale() {
        return scale;
    }

    public int getWidth() {
        return pixels.length;
    }

    public int getHeight() {
        return pixels[0].length;
    }

    public ArrayList<Empire> getEmpires() {
        return (ArrayList<Empire>) tm.getEmpires();
    }

    public void setColorMode(Pixel.ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    @Override
    public void render(Graphics g) {
        for(Pixel[] pa : pixels) {
            for(Pixel p : pa) {
                p.render(g, colorMode);
            }
        }
        for(Boat b : boats) {
            b.render(g, scale);
        }
        for(Empire e : getEmpires()) {
            e.render(g);
        }
        for(Missile m : missiles) {
            m.render(g, scale);
        }
        for(Paratrooper p : paratroopers) {
            p.render(g, scale);
        }
    }

    public void mouseClicked(Point point) {
        int x = point.x / scale;
        int y = point.y / scale;

        if(tm.getEmpireForPixel(pixels[x][y]) != null) {
            perspectiveEmpire = tm.getEmpireForPixel(pixels[x][y]);
        }
    }

    public void addBoat(Boat boat) {
        boats.add(boat);
    }

    public void addMissile(Missile missile) {
        missiles.add(missile);
    }

    public void addParatrooper(Paratrooper paratrooper) {
        paratroopers.add(paratrooper);
    }

    public TerritoryManager getTerritoryManager() {
        return tm;
    }

}
