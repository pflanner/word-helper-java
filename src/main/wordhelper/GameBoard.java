package wordhelper;

import org.apache.commons.lang3.StringUtils;
import wordhelper.config.BoardConfig;
import wordhelper.config.StandardBoardConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Author: Pete
 * Date: 2/15/2018
 * Time: 5:21 AM
 */
public class GameBoard {
    private Map<Location, Tile> oldTiles;
    private Map<Location, Tile> newTiles;
    private BoardConfig config;
    private Orientation orientation;

    public GameBoard() {
        oldTiles = new HashMap<>();
        newTiles = new HashMap<>();
        config = new StandardBoardConfig();
        orientation = Orientation.NONE;
    }

    public GameBoard(BoardConfig config) {
        this();
        this.config = config;
    }

    public boolean addOldTile(Tile tile) {
        Location loc = tile.getLocation();
        if (!oldTiles.containsKey(loc) && !newTiles.containsKey(loc)) {
            neighborify(tile);
            oldTiles.put(loc, tile);
            return true;
        } else {
            return false;
        }
    }

    public boolean addNewTile(Tile tile) {
        Location loc = tile.getLocation();
        if (!oldTiles.containsKey(loc) && !newTiles.containsKey(loc) && isInBounds(tile)) {
            if (orientation == Orientation.NONE) {
                orientation = Orientation.SINGLE;
            } else if (orientation == Orientation.SINGLE) {
                Tile firstNewTile = newTiles.values().iterator().next();
                if (firstNewTile.getLocation().getRow() == tile.getLocation().getRow()) {
                    orientation = Orientation.HORIZONTAL;
                } else if (firstNewTile.getLocation().getCol() == tile.getLocation().getCol()) {
                    orientation = Orientation.VERTICAL;
                } else {
                    return false;
                }
            }

            neighborify(tile);
            newTiles.put(loc, tile);

            return true;
        }

        return false;
    }

    public void clearNewTiles() {
        for (Tile tile : newTiles.values()) {
            if (tile.getUpper() != null)
                tile.getUpper().setLower(null);
            if (tile.getLower() != null)
                tile.getLower().setUpper(null);
            if (tile.getLeft() != null)
                tile.getLeft().setRight(null);
            if (tile.getRight() != null)
                tile.getRight().setLeft(null);
        }
        newTiles.clear();
        orientation = Orientation.NONE;
    }

    public boolean isValid() {
        if (orientation == Orientation.HORIZONTAL) {
            // tiles must be contiguous in the direction we're adding them
            // we're testing this by going from the leftmost new tile to the rightmost new tile
            // one position at a time and making sure every position in between has a tile
            Iterator<Tile> i = newTiles.values().iterator();
            if (i.hasNext()) {
                Tile first = i.next();
                Tile leftmost = first, righmost = first;
                for (Tile tile : newTiles.values()) {
                    if (tile.getLocation().getCol() < leftmost.getLocation().getCol()) {
                        leftmost = tile;
                    }
                    if (tile.getLocation().getCol() > righmost.getLocation().getCol()) {
                        righmost = tile;
                    }
                }

                Tile cur = leftmost;
                while (cur.getRight() != null) {
                    cur = cur.getRight();
                }

                boolean isContiguous = cur.getLocation().getCol() >= righmost.getLocation().getCol();

                // if the board has old tiles, at least one new tile must have at least one old tile neighbor
                // otherwise, at least one new tile must be on the center square
                boolean isTouchingCenter = false;
                boolean isNeighboringOldTile = false;
                for (Tile tile : newTiles.values()) {
                    if (tile.getUpper() != null && oldTiles.containsKey(tile.getUpper().getLocation()) ||
                            tile.getLower() != null && oldTiles.containsKey(tile.getLower().getLocation()) ||
                            tile.getLeft() != null && oldTiles.containsKey(tile.getLeft().getLocation()) ||
                            tile.getRight() != null && oldTiles.containsKey(tile.getRight().getLocation())) {
                        isNeighboringOldTile = true;
                    }
                    if (config.getCenter().equals(tile.getLocation())) {
                        isTouchingCenter = true;
                    }
                }

                return isContiguous && (isNeighboringOldTile || isTouchingCenter);
            } else {
                return false;
            }
        } else if (orientation == Orientation.VERTICAL) {
            // must be contiguous
            Iterator<Tile> i = newTiles.values().iterator();
            if (i.hasNext()) {
                Tile first = i.next();
                Tile uppermost = first, lowermost = first;
                for (Tile tile : newTiles.values()) {
                    if (tile.getLocation().getRow() < uppermost.getLocation().getRow()) {
                        uppermost = tile;
                    }
                    if (tile.getLocation().getRow() > lowermost.getLocation().getRow()) {
                        lowermost = tile;
                    }
                }

                Tile cur = uppermost;
                while (cur.getLower() != null) {
                    cur = cur.getLower();
                }

                boolean isContiguous = cur.getLocation().getRow() >= lowermost.getLocation().getRow();

                // if the board has old tiles, at least one new tile must have at least one old tile neighbor
                // otherwise, at least one new tile must be on the center square
                boolean isTouchingCenter = false;
                boolean isNeighboringOldTile = false;
                for (Tile tile : newTiles.values()) {
                    if (tile.getUpper() != null && oldTiles.containsKey(tile.getUpper().getLocation()) ||
                            tile.getLower() != null && oldTiles.containsKey(tile.getLower().getLocation()) ||
                            tile.getLeft() != null && oldTiles.containsKey(tile.getLeft().getLocation()) ||
                            tile.getRight() != null && oldTiles.containsKey(tile.getRight().getLocation())) {
                        isNeighboringOldTile = true;
                    }
                    if (config.getCenter().equals(tile.getLocation())) {
                        isTouchingCenter = true;
                    }
                }

                return isContiguous && (isNeighboringOldTile || isTouchingCenter);
            } else {
                return false;
            }
        } else if (orientation == Orientation.SINGLE) {
            Tile firstNewTile = newTiles.values().iterator().next();
            return firstNewTile.hasNeighbor() || oldTiles.size() == 0;
        } else {
            return false;
        }
    }

    public boolean isValid(Map<Location,Tile> tilesToAdd, Orientation orientation) {
        if (tilesToAdd == null || tilesToAdd.isEmpty()) {
            return false;
        }

        if (orientation == Orientation.HORIZONTAL) {
            // if the board has old tiles, at least one new tile must have at least one old tile neighbor
            // otherwise, at least one new tile must be on the center square
            boolean isTouchingCenter = false;
            boolean isNeighboringOldTile = false;
            boolean first = true;
            Location prevLoc = new Location();
            for (Location curLoc : tilesToAdd.keySet()) {
                if (oldTiles.containsKey(curLoc)) {
                    return false;
                }

                if (curLoc.getCol() >= getConfig().getSize()) {
                    // we're off the board
                    return false;
                }

                if (first) {
                    first = false;
                } else {
                    if (curLoc.getRow() != prevLoc.getRow() || curLoc.getCol() == prevLoc.getCol()) {
                        return false;
                    }
                }

                if (oldTiles.containsKey(new Location(curLoc.getRow() - 1, curLoc.getCol())) ||
                        oldTiles.containsKey(new Location(curLoc.getRow() + 1, curLoc.getCol())) ||
                        oldTiles.containsKey(new Location(curLoc.getRow(), curLoc.getCol() - 1)) ||
                        oldTiles.containsKey(new Location(curLoc.getRow(), curLoc.getCol() + 1))
                ) {
                    isNeighboringOldTile = true;
                }

                if (config.getCenter().equals(curLoc)) {
                    isTouchingCenter = true;
                }

                prevLoc = curLoc;
            }

            return isNeighboringOldTile || isTouchingCenter;
        } else if (orientation == Orientation.VERTICAL) {
            // if the board has old tiles, at least one new tile must have at least one old tile neighbor
            // otherwise, at least one new tile must be on the center square
            boolean isTouchingCenter = false;
            boolean isNeighboringOldTile = false;
            boolean first = true;
            Location prevLoc = new Location();

            for (Location curLoc : tilesToAdd.keySet()) {
                if (oldTiles.containsKey(curLoc)) {
                    return false;
                }

                if (curLoc.getRow() >= getConfig().getSize()) {
                    // we're off the board
                    return false;
                }

                if (first) {
                    first = false;
                } else {
                    if (curLoc.getCol() != prevLoc.getCol() || curLoc.getRow() == prevLoc.getRow()) {
                        return false;
                    }
                }

                if (oldTiles.containsKey(new Location(curLoc.getRow() - 1, curLoc.getCol())) ||
                        oldTiles.containsKey(new Location(curLoc.getRow() + 1, curLoc.getCol())) ||
                        oldTiles.containsKey(new Location(curLoc.getRow(), curLoc.getCol() - 1)) ||
                        oldTiles.containsKey(new Location(curLoc.getRow(), curLoc.getCol() + 1))
                ) {
                    isNeighboringOldTile = true;
                    break;
                }

                if (config.getCenter().equals(curLoc)) {
                    isTouchingCenter = true;
                    break;
                }

                prevLoc = curLoc;
            }

            return isNeighboringOldTile || isTouchingCenter;
        } else {
            return false;
        }
    }

    private void neighborify(Tile tile) {
        Location loc = tile.getLocation();

        Tile oldUpper = oldTiles.get(new Location(loc.getRow() - 1, loc.getCol()));
        Tile oldLower = oldTiles.get(new Location(loc.getRow() + 1, loc.getCol()));
        Tile oldLeft = oldTiles.get(new Location(loc.getRow(), loc.getCol() - 1));
        Tile oldRight = oldTiles.get(new Location(loc.getRow(), loc.getCol() + 1));

        Tile newUpper = newTiles.get(new Location(loc.getRow() - 1, loc.getCol()));
        Tile newLower = newTiles.get(new Location(loc.getRow() + 1, loc.getCol()));
        Tile newLeft = newTiles.get(new Location(loc.getRow(), loc.getCol() - 1));
        Tile newRight = newTiles.get(new Location(loc.getRow(), loc.getCol() + 1));

        tile.setUpper(oldUpper != null ? oldUpper : newUpper);
        tile.setLower(oldLower != null ? oldLower : newLower);
        tile.setLeft(oldLeft != null ? oldLeft : newLeft);
        tile.setRight(oldRight != null ? oldRight : newRight);

        if (tile.getUpper() != null)
            tile.getUpper().setLower(tile);
        if (tile.getLower() != null)
            tile.getLower().setUpper(tile);
        if (tile.getLeft() != null)
            tile.getLeft().setRight(tile);
        if (tile.getRight() != null)
            tile.getRight().setLeft(tile);
    }

    private boolean isInBounds(Tile tile) {
        Location loc = tile.getLocation();
        return loc != null && loc.getRow() >= 0 && loc.getRow() < config.getSize()
                && loc.getCol() >= 0 && loc.getCol() < config.getSize();
    }

    public String getPrettyPrint() {
        int columnSize = 5;
        StringBuilder sb = new StringBuilder();
        String cellFormat = "%-" + columnSize + "s";
        sb.append("-".repeat(config.getSize() * (columnSize + 1)));
        sb.append(String.format("%n"));
        for (int i = 0; i < config.getSize(); i++) {
            for (int j = 0; j < config.getSize(); j++) {
                Location loc = new Location(i, j);
                String cellValue = "";
                if (oldTiles.containsKey(loc)) {
                    Tile tile = oldTiles.get(loc);
                    cellValue = tile.getLetter().toString();
                    if (tile.isWildcard()) {
                        cellValue = "-" + cellValue + "-";
                    }
                } else if (newTiles.containsKey(loc)) {
                    cellValue = "*" + newTiles.get(loc).getLetter().toString() + "*";
                }
                sb.append(String.format(cellFormat, StringUtils.center(cellValue, columnSize)));
                if (j < config.getSize() - 1) {
                    sb.append("|");
                }
            }
            sb.append(String.format("%n"));
            sb.append("-".repeat(config.getSize() * (columnSize + 1)));
            sb.append(String.format("%n"));
        }
        return sb.toString();
    }

    public String getEmptyBoardLayout() {
        int columnSize = 6;
        StringBuilder sb = new StringBuilder();
        String cellFormat = "%-" + columnSize + "s";
        sb.append("-".repeat(config.getSize() * (columnSize + 1)));
        sb.append(String.format("%n"));
        for (int i = 0; i < config.getSize(); i++) {
            for (int j = 0; j < config.getSize(); j++) {
                Location loc = new Location(i, j);
                if (config.getDls().contains(loc)) {
                    sb.append(String.format(cellFormat, StringUtils.center("DL", columnSize)));
                } else if (config.getTls().contains(loc)) {
                    sb.append(String.format(cellFormat, StringUtils.center("TL", columnSize)));
                } else if (config.getDws().contains(loc)) {
                    sb.append(String.format(cellFormat, StringUtils.center("DW", columnSize)));
                } else if (config.getTws().contains(loc)) {
                    sb.append(String.format(cellFormat, StringUtils.center("TW", columnSize)));
                } else {
                    sb.append(String.format(cellFormat, ""));
                }
                if (j < config.getSize() - 1) {
                    sb.append("|");
                }
            }
            sb.append(String.format("%n"));
            sb.append("-".repeat(config.getSize() * (columnSize + 1)));
            sb.append(String.format("%n"));
        }
        return sb.toString();
    }

    /*********** Accessors and Mutators ***********/
    public Map<Location, Tile> getOldTiles() {
        return oldTiles;
    }

    public void setOldTiles(Map<Location, Tile> oldTiles) {
        this.oldTiles = oldTiles;
    }

    public Map<Location, Tile> getNewTiles() {
        return newTiles;
    }

    public void setNewTiles(Map<Location, Tile> newTiles) {
        this.newTiles = newTiles;
    }

    public BoardConfig getConfig() {
        return config;
    }

    public void setConfig(BoardConfig config) {
        this.config = config;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }
}
