package net.sudologic.empires.states.gameplay.util;

import java.util.Random;

public class PerlinNoiseGenerator {
    private int width;
    private int height;
    private float frequency;
    private int octaves;
    private double[][] noise;

    public PerlinNoiseGenerator(int width, int height, float frequency, int octaves) {
        this.width = width;
        this.height = height;
        this.frequency = frequency;
        this.octaves = octaves;
        this.noise = generateNoise();
    }

    public double[][] getNoise() {
        return noise;
    }

    private double[][] generateNoise() {
        double[][] noise = new double[width][height];
        Random random = new Random();

        // Generate random gradients
        float[][][] gradients = new float[width][height][2];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float angle = random.nextFloat() * 2 * (float) Math.PI;
                gradients[x][y][0] = (float) Math.cos(angle);
                gradients[x][y][1] = (float) Math.sin(angle);
            }
        }

        // Generate Perlin noise
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float noiseValue = 0;
                float frequency = this.frequency;
                float amplitude = 1;

                for (int octave = 0; octave < octaves; octave++) {
                    float sampleX = x * frequency;
                    float sampleY = y * frequency;

                    int x0 = (int) sampleX;
                    int x1 = (x0 + 1) % width;
                    int y0 = (int) sampleY;
                    int y1 = (y0 + 1) % height;

                    float dx = sampleX - x0;
                    float dy = sampleY - y0;

                    float dot0 = dotProduct(gradients[x0][y0], dx, dy);
                    float dot1 = dotProduct(gradients[x1][y0], dx - 1, dy);
                    float dot2 = dotProduct(gradients[x0][y1], dx, dy - 1);
                    float dot3 = dotProduct(gradients[x1][y1], dx - 1, dy - 1);

                    float interpX0 = interpolate(dot0, dot1, smoothStep(dx));
                    float interpX1 = interpolate(dot2, dot3, smoothStep(dx));
                    float interpY = interpolate(interpX0, interpX1, smoothStep(dy));

                    noiseValue += interpY * amplitude;

                    frequency *= 2;
                    amplitude *= 0.5;
                }

                noise[x][y] = noiseValue;
            }
        }

        return noise;
    }

    private float dotProduct(float[] gradient, float x, float y) {
        return gradient[0] * x + gradient[1] * y;
    }

    private float interpolate(float a, float b, float t) {
        return a * (1 - t) + b * t;
    }

    private float smoothStep(float t) {
        return t * t * (3 - 2 * t);
    }
}
