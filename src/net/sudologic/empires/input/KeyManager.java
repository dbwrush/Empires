package net.sudologic.empires.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyManager implements KeyListener {

    private boolean[] keys;

    public KeyManager() {
        keys = new boolean[256];
    }

    public void tick() {

    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    public boolean isKeyPressed(int keyCode) {
        if(keyCode >= 0 && keyCode < keys.length) {
            return keys[keyCode];
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        keys[keyEvent.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        keys[keyEvent.getKeyCode()] = false;
    }
}
