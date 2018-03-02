package wordhelper;

/**
 * Author: Pete
 * Date: 2/15/2018
 * Time: 5:10 AM
 */
public class Tile {
    private Tile upper;
    private Tile lower;
    private Tile left;
    private Tile right;
    private Character letter;
    private boolean isWildcard = false;
    private Location location;
    
    public Tile() {
        
    }
    
    public Tile(Character letter) {
        this.letter = letter;
    }
    
    public Tile(Character letter, Location location) {
        this.letter = letter;
        this.location = location;
    }

    public Tile(Character letter, Location location, boolean isWildcard) {
        this.letter = letter;
        this.location = location;
        this.isWildcard = isWildcard;
    }

    public boolean hasNeighbor() {
        return upper != null || lower != null || left != null || right != null;
    }
    
    public Tile getCopy() {
        return new Tile(this.letter, new Location(this.location.getRow(), this.location.getCol()));
    }
    
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Tile)) return false;
        
        Tile that = (Tile) o;
        
        boolean letterEquals = this.letter == null ? that.getLetter() == null : this.letter.equals(that.letter);
        boolean wildcardEquals = this.isWildcard == that.isWildcard;
        boolean locationEquals = this.location == null ? that.location == null : this.location.equals(that.location);
        
        return letterEquals && wildcardEquals && locationEquals;
        
//        return this.letter == null ? that.getLetter() == null : this.letter.equals(that.letter) &&
//               this.isWildcard == that.isWildcard &&
//               this.location == null ? that.location == null : this.location.equals(that.location);
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + (letter == null ? 0 : letter.hashCode());
        hash = hash * 31 + (isWildcard ? 13 : 19);
        hash = hash * 11 + (location == null ? 0 : location.hashCode());
        return hash;
    }
    
    @Override
    public String toString() {
        return String.format("{%s:%s}", location.toString(), letter.toString());
    }
    
    /********** Accessor and Mutators **************/
    
    public Tile getUpper() {
        return upper;
    }

    public void setUpper(Tile upper) {
        this.upper = upper;
    }

    public Tile getLower() {
        return lower;
    }

    public void setLower(Tile lower) {
        this.lower = lower;
    }

    public Tile getLeft() {
        return left;
    }

    public void setLeft(Tile left) {
        this.left = left;
    }

    public Tile getRight() {
        return right;
    }

    public void setRight(Tile right) {
        this.right = right;
    }

    public Character getLetter() {
        return letter;
    }

    public void setLetter(Character letter) {
        this.letter = letter;
    }

    public boolean isWildcard() {
        return isWildcard;
    }

    public void setWildcard(boolean wildcard) {
        isWildcard = wildcard;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        if (this.location == null) {
            this.location = new Location();
        }
        this.location.setRow(location.getRow());
        this.location.setCol(location.getCol());
    }
}
