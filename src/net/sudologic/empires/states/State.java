package net.sudologic.empires.states;

import net.sudologic.empires.Game;

import java.awt.*;

public abstract class State {

    private static State currentState = null;

    protected Game game;

    public State(Game game) {
        this.game = game;
    }

    public abstract void tick();

    public abstract void render(Graphics g);

    public static void setCurrentState(State state) {
        currentState = state;
    }

    public static State getCurrentState() {
        return currentState;
    }

}
