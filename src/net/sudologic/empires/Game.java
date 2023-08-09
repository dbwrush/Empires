package net.sudologic.empires;

import net.sudologic.empires.display.Display;
import net.sudologic.empires.input.KeyManager;
import net.sudologic.empires.input.MouseManager;
import net.sudologic.empires.states.gameplay.GameState;
import net.sudologic.empires.states.State;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class Game implements Runnable{
    private final double warThreshold;
    private int width, height, numEmpires, scale;
    private String title;

    private KeyManager keyManager;
    private MouseManager mouseManager;

    private Display display;

    private GameState gameState;

    private boolean running;

    private Thread thread;

    private BufferStrategy bs;
    private Graphics g;

    public Game(String title, int width, int height, int scale, int numEmpires, double warThreshold) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.numEmpires = numEmpires;
        this.warThreshold = warThreshold;
        this.scale = scale;
        keyManager = new KeyManager();
    }

    private void init() {
        display = new Display(title, width, height);
        display.getFrame().addKeyListener(keyManager);
        gameState = new GameState(this, width, height, scale, numEmpires, warThreshold,  keyManager);
        mouseManager = new MouseManager(display.getCanvas(), this);
        State.setCurrentState(gameState);
    }

    public Display getDisplay() {
        return display;
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public void run() {
        init();

        //game loop timer setup
        int desiredFps = 100;
        double timePerTick = 1000000000 / desiredFps;//timePerTick will be the number of nanoseconds per tick. 1 second is 1 billion nanoseconds.
        double delta = 0;
        long now;
        long lastTime = System.nanoTime();
        long timer = 0;
        int ticks = 0;

        while(running) {
            //game loop timer
            now = System.nanoTime();
            delta += (now - lastTime) / timePerTick;
            timer += now - lastTime;
            lastTime = now;
            if(delta >= 1) {
                tick();
                render();
                ticks++;
                delta--;
            }

            if(timer >= 1000000000) {
                System.out.println("fps: " + ticks);
                ticks = 0;
                timer = 0;
            }
        }

        stop();
    }

    public synchronized void start() {
        if(!running) {
            running = true;
            thread = new Thread(this);
            thread.start();//calls the run method
        }
    }

    public synchronized void stop() {
        if(running) {
            try {
                thread.join();
            } catch(InterruptedException e) {

            }
        }
    }

    public void tick() {
        State.getCurrentState().tick();
    }

    public void render() {
        bs = display.getCanvas().getBufferStrategy();
        if(bs == null) {
            display.getCanvas().createBufferStrategy(3);
            return;
        }
        g = bs.getDrawGraphics();
        //clear screen
        g.clearRect(0,0, width, height);
        //draw here

        if(State.getCurrentState() != null) {
            State.getCurrentState().render(g);
        }

        //end drawing
        bs.show();
        g.dispose();
    }

}
