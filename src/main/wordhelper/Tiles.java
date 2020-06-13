package wordhelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for an ordered collection of Tile with equals and hashCode methods implemented to make it easy
 * to test for membership specific arrangements of tiles in maps and sets
 */
public class Tiles extends ArrayList<Tile> {
    public Tiles() {
        super();
    }

    public Tiles(List<Tile> tiles)  {
        super(tiles);
    }

    public Tiles(String tileString) {
        super();

        tileString = tileString.toLowerCase();
        for (Character c : tileString.toCharArray()) {
            if (Constants.ALPHABET_SET.contains(c)) {
                Tile tile = new Tile(c);
                add(tile);
            }
        }
    }

    public String letters() {
        String letters = "";
        for (Tile t : this) {
            letters += t.getLetter();
        }
        return letters;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tiles)) {
            return false;
        }

        Tiles that = (Tiles)o;

        if (this.size() != that.size()) {
            return false;
        }

        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).getLetter().equals(that.get(i).getLetter())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        String stringValue = "";
        for (Tile tile : this) {
            stringValue += tile.getLetter();
        }
        return stringValue.hashCode();
    }
}
