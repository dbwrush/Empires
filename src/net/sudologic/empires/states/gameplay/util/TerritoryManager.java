package net.sudologic.empires.states.gameplay.util;

import net.sudologic.empires.states.gameplay.Empire;
import net.sudologic.empires.states.gameplay.Pixel;

import java.util.*;

public class TerritoryManager {
    private Map<Pixel, Empire> pixelToEmpireMap = new HashMap<>();
    private Map<Empire, List<Pixel>> empireToPixelsMap = new HashMap<>();

    public void addPixelToEmpire(Pixel pixel, Empire empire) {
        Empire currentEmpire = pixelToEmpireMap.get(pixel);

        if (currentEmpire != null && currentEmpire.equals(empire)) {
            return; // Pixel is already controlled by the desired empire
        }

        removePixelFromEmpire(pixel); // Ensure pixel is not controlled by any empire
        pixelToEmpireMap.put(pixel, empire);
        empireToPixelsMap.computeIfAbsent(empire, k -> new ArrayList<>()).add(pixel);
    }

    public void removePixelFromEmpire(Pixel pixel) {
        Empire empire = pixelToEmpireMap.remove(pixel);
        if (empire != null) {
            List<Pixel> pixelList = empireToPixelsMap.get(empire);
            if (pixelList != null) {
                pixelList.remove(pixel);
                if (pixelList.isEmpty()) {
                    empireToPixelsMap.remove(empire);
                }
            }
        }
    }

    public void transferPixelToEmpire(Pixel pixel, Empire newEmpire) {
        Empire currentEmpire = pixelToEmpireMap.get(pixel);
        if (currentEmpire != null) {
            if (currentEmpire.equals(newEmpire)) {
                return; // Pixel is already controlled by the new empire
            }
            removePixelFromEmpire(pixel);
        }
        addPixelToEmpire(pixel, newEmpire);
    }

    public Empire getEmpireForPixel(Pixel pixel) {
        return pixelToEmpireMap.get(pixel);
    }

    public List<Pixel> getPixelsForEmpire(Empire empire) {
        return empireToPixelsMap.getOrDefault(empire, Collections.emptyList());
    }

    public void addEmpire(Empire empire) {
        if (!empireToPixelsMap.containsKey(empire)) {
            empireToPixelsMap.put(empire, new ArrayList<>());
        }
    }

    public void removeEmpire(Empire empire) {
        List<Pixel> pixelList = empireToPixelsMap.remove(empire);
        if (pixelList != null) {
            for (Pixel pixel : pixelList) {
                pixelToEmpireMap.remove(pixel);
            }
        }
    }

    public List<Empire> getEmpires() {
        return new ArrayList<>(empireToPixelsMap.keySet());
    }
}
