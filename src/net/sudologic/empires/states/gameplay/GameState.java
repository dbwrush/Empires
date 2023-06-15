package net.sudologic.empires.states.gameplay;

import net.sudologic.empires.Game;
import net.sudologic.empires.PerlinNoiseGenerator;
import net.sudologic.empires.states.State;

import java.awt.*;
import java.util.ArrayList;

public class GameState extends State {
    public ArrayList<Empire> empires;

    public Pixel[][] pixels;

    public GameState(Game game, int width, int height, int scale, int numEmpires) {
        super(game);
        System.out.println("Switched to GameState");
        genTerrain(width / scale, height / scale, scale);
        empires = new ArrayList<>();
        while(empires.size() <= numEmpires) {
            empires.add(new Empire());
        }
    }

    private void genTerrain(int width, int height, int scale) {
        System.out.println("Generating Terrain");
        pixels = new Pixel[width][height];
        PerlinNoiseGenerator perlin = new PerlinNoiseGenerator(width, height, 0.05f, 4);
        float[][] habitability = perlin.getNoise();
        for(int x = 0; x < pixels.length; x++) {
            for(int y = 0; y < pixels[0].length; y++) {
                //System.out.println("X: " + x + " Y: " + y);
                pixels[x][y] = new Pixel(x * scale, y * scale, (habitability[x][y] + 1) / 2, scale);
            }
        }
        System.out.println("Finished Generating Terrain");
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(Graphics g) {
        for(Pixel[] pa : pixels) {
            for(Pixel p : pa) {
                p.render(g, Pixel.ColorMode.empire);
            }
        }
    }
}
