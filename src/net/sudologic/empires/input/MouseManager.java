package net.sudologic.empires.input;

import net.sudologic.empires.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseManager {
    private Canvas canvas;
    private Game game;
    private Point clickPosition;


    public MouseManager(Canvas canvas, Game game) {
        this.canvas = canvas;
        this.game = game;
        this.clickPosition = new Point(0, 0);

        // Add a mouse listener to the canvas
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the mouse click position relative to the canvas
                clickPosition = e.getPoint();
                SwingUtilities.convertPointToScreen(clickPosition, canvas);
                SwingUtilities.convertPointFromScreen(clickPosition, canvas);

                // Handle the click event, e.g., call a method to process the click
                handleMouseClick(clickPosition);
            }
        });
    }

    // Method to process the mouse click event
    private void handleMouseClick(Point clickPosition) {
        game.getGameState().mouseClicked(clickPosition);
    }
}