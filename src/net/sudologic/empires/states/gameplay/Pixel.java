package net.sudologic.empires.states.gameplay;

import java.awt.*;

public class Pixel {
    public static enum ColorMode{
        empire
    }

    private int x, y, scale;

    private double habitability;

    private float strength;
    private Empire empire;

    public Pixel(int x, int y, double habitability, int scale) {
        //System.out.println(habitability);
        if(habitability <= 0.5) {
            habitability = 0;
        }
        this.habitability = habitability;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public double getHabitability() {
        return habitability;
    }

    public void render(Graphics g, ColorMode colorMode) {
        g.setColor(getColor(colorMode));
        g.fillRect(x, y, scale, scale);
    }

    public void tick() {

    }

    public Color getColor(ColorMode colorMode) {
        switch(colorMode) {
            case empire:
                if(empire != null) {
                    return empire.getColor();
                }
            default:
                if(habitability == 0) {
                    return Color.BLUE;
                } else {
                    //System.out.println(habitability);
                    return new Color(0,(int) (habitability * 200), 0);
                }
        }
    }
}
