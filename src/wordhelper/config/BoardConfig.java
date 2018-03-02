package wordhelper.config;

import wordhelper.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Pete
 * Date: 2/15/2018
 * Time: 5:23 AM
 */
public class BoardConfig {
    private Set<Location> dls = new HashSet<>();
    private Set<Location> tls = new HashSet<>();
    private Set<Location> dws = new HashSet<>();
    private Set<Location> tws = new HashSet<>();
    private Map<Character,Integer> letterPoints = new HashMap<>(26);
    private Location center = new Location(7, 7);
    private int size = 15;
    private int rackSize = 7;
    private int allTileBonus = 35;
    
    public BoardConfig() {
        initLetterPoints();
    }

    /********* Acessors and Mutators ************/
    public Set<Location> getDls() {
        return dls;
    }

    public void addDl(int row, int col) {
        dls.add(new Location(row, col));
    }

    public Set<Location> getTls() {
        return tls;
    }

    public void addTl(int row, int col) {
        tls.add(new Location(row, col));
    }

    public Set<Location> getDws() {
        return dws;
    }

    public void addDw(int row, int col) {
        dws.add(new Location(row, col));
    }

    public Set<Location> getTws() {
        return tws;
    }

    public void addTw(int row, int col) {
        tws.add(new Location(row, col));
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
    public Map<Character,Integer> getLetterPoints() {
        return letterPoints;
    }
    
    private void initLetterPoints() {
        letterPoints.put('a', 1);
        letterPoints.put('b', 4);
        letterPoints.put('c', 4);
        letterPoints.put('d', 2);
        letterPoints.put('e', 1);
        letterPoints.put('f', 4);
        letterPoints.put('g', 3);
        letterPoints.put('h', 3);
        letterPoints.put('i', 1);
        letterPoints.put('j', 10);
        letterPoints.put('k', 5);
        letterPoints.put('l', 2);
        letterPoints.put('m', 4);
        letterPoints.put('n', 2);
        letterPoints.put('o', 1);
        letterPoints.put('p', 4);
        letterPoints.put('q', 10);
        letterPoints.put('r', 1);
        letterPoints.put('s', 1);
        letterPoints.put('t', 1);
        letterPoints.put('u', 2);
        letterPoints.put('v', 5);
        letterPoints.put('w', 4);
        letterPoints.put('x', 8);
        letterPoints.put('y', 3);
        letterPoints.put('z', 10);
    }

    public int getRackSize() {
        return rackSize;
    }

    public void setRackSize(int rackSize) {
        this.rackSize = rackSize;
    }

    public int getAllTileBonus() {
        return allTileBonus;
    }

    public void setAllTileBonus(int allTileBonus) {
        this.allTileBonus = allTileBonus;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(Location center) {
        this.center = center;
    }
}
