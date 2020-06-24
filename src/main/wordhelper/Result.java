package wordhelper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Author: Pete
 * Date: 2/16/2018
 * Time: 6:53 AM
 */
public class Result {
    private int score;
    private Collection<Tile> tiles;
    private Orientation orientation;
    private Location startLocation;
    
    public Result() { }
    
    public Result(int score, Collection<Tile> tiles) {
        this.score = score;
        this.tiles = tiles;
    }
    
    public void addTile(Tile tile) {
        if (tiles == null) {
            tiles = new ArrayList<>();
        }
        
        tiles.add(tile);
    }

    /********** Accessors and Mutators *********/
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Collection<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(Collection<Tile> tiles) {
        this.tiles = tiles;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }
}
