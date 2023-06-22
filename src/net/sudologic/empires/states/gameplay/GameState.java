package net.sudologic.empires.states.gameplay;

import net.sudologic.empires.Game;
import net.sudologic.empires.input.KeyManager;
import net.sudologic.empires.states.gameplay.util.PerlinNoiseGenerator;
import net.sudologic.empires.states.State;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameState extends State {
    private ArrayList<Empire> empires, dead, revolts;

    private ArrayList<Pixel> habitablePixels;

    private ArrayList<Boat> boats, remBoats;

    private Pixel[][] pixels;

    private int maxBoats, scale;

    private double warThreshold;

    private Pixel.ColorMode colorMode;

    private KeyManager km;

    public GameState(Game game, int width, int height, int scale, int numEmpires, double warThreshold, int maxBoats, KeyManager km) {
        super(game);
        System.out.println("Switched to GameState");
        this.km = km;
        this.warThreshold = warThreshold;
        this.scale = scale;
        genTerrain(width / scale, height / scale, scale);
        genEmpires(numEmpires);
        boats = new ArrayList<>();
        remBoats = new ArrayList<>();
        colorMode = Pixel.ColorMode.empire;
        this.maxBoats = maxBoats;
        revolts = new ArrayList<>();
    }

    private void genTerrain(int width, int height, int scale) {
        System.out.println("Generating Terrain");
        pixels = new Pixel[width][height];
        habitablePixels = new ArrayList<>();
        PerlinNoiseGenerator perlin = new PerlinNoiseGenerator(width, height, 0.005f, 4);
        float[][] habitability = perlin.getNoise();
        for(int x = 0; x < pixels.length; x++) {
            for(int y = 0; y < pixels[0].length; y++) {
                //System.out.println("X: " + x + " Y: " + y);
                float h = (habitability[x][y] + 1) / 2;
                if(h < 0.5) {
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

    private void genEmpires(int numEmpires) {
        empires = new ArrayList<>();
        dead = new ArrayList<>();
        while(empires.size() <= numEmpires) {
            Empire e = new Empire(this);
            System.out.println("Spawning " + e.getName());
            while(e.getCapital() == null) {
                Pixel p = pixels[(int) (Math.random() * pixels.length)][(int) (Math.random() * pixels[0].length)];
                if(p.getEmpire() == null && p.isHabitable()) {
                    e.addTerritory(p);
                    e.setCapital(p);
                }
            }
            empires.add(e);
        }
    }

    public void addEmpire(Empire empire) {
        revolts.add(empire);
    }

    public double getWarThreshold() {
        return warThreshold;
    }

    public void removeBoat(Boat b) {
        remBoats.add(b);
    }

    public ArrayList<Pixel> getNeighbors(int x, int y) {
        ArrayList<Pixel> neighbors = new ArrayList<>();

        if(x > 0 && y > 0) {
            neighbors.add(pixels[x - 1][y - 1]);
        }
        if(x > 0) {
            neighbors.add(pixels[x - 1][y]);
        }
        if(x > 0 && y < pixels[0].length - 1) {
            neighbors.add(pixels[x - 1][y + 1]);
        }
        if(y > 0) {
            neighbors.add(pixels[x][y - 1]);
        }
        if(y > 0 && x < pixels.length - 1) {
            neighbors.add(pixels[x + 1][y - 1]);
        }
        if(x < pixels.length - 1) {
            neighbors.add(pixels[x + 1][y]);
        }
        if(y < pixels[0].length - 1) {
            neighbors.add(pixels[x][y + 1]);
        }
        if(x < pixels.length - 1 && y < pixels[0].length - 1) {
            neighbors.add(pixels[x + 1][y + 1]);
        }

        return neighbors;
    }

    @Override
    public void tick() {
        if (warThreshold > 0 && Math.random() < 0.01) {
            warThreshold -= 1;
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

        Collections.shuffle(empires);
        for (Empire e : empires) {
            e.tick();
            if (e.getTerritory().size() == 0) {
                removeEmpire(e);
            }
        }
        for (Empire e : dead) {
            empires.remove(e);
        }
        dead = new ArrayList<>();
        for(Empire e : revolts) {
            empires.add(e);
        }
        revolts = new ArrayList<>();

        Collections.shuffle(habitablePixels);
        for (Pixel p : habitablePixels) {
            p.tick();
            if(boats.size() < maxBoats && Math.random() < 0.01) {
                p.spawnBoat();
            }
        }

        for (Boat b : boats) {
            b.tick();
        }

        for (Boat b : remBoats) {
            if (boats.contains(b)) {
                boats.remove(b);
            }
        }
        remBoats = new ArrayList<>();
    }

    public int getScale() {
        return scale;
    }

    public void removeEmpire(Empire e) {
        dead.add(e);
        System.out.println(e.getName() + " has been eliminated.");
    }

    public int getWidth() {
        return pixels.length;
    }

    public int getHeight() {
        return pixels[0].length;
    }

    public ArrayList<Empire> getEmpires() {
        return empires;
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
        for(Empire e : empires) {
            e.render(g);
        }
    }

    public boolean addBoat(Boat boat) {
        if(boats.size() < maxBoats) {
            boats.add(boat);
            return true;
        }
        return false;
    }
}
