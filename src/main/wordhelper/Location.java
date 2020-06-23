package wordhelper;

/**
 * Author: Pete
 * Date: 2/15/2018
 * Time: 5:16 AM
 */
public class Location {
    private int row;
    private int col;
    
    public Location() {
        row = -1;
        col = -1;
    }
    
    public Location(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public Location(Location location) {
        this.row = location.getRow();
        this.col = location.getCol();
    }
    
    public Location oneRight() {
//        if (col >= 14) {
//            System.out.println("error col out of bounds");
//        }
        col++;
        return this;
    }
    
    public Location oneDown() {
//        if (row >= 14) {
//            System.out.println("error row out of bounds");
//        }
        row++;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Location)) return false;
        
        Location that = (Location) o;
        
        return this.row == that.row && this.col == that.col;
    }
    
    @Override
    public String toString() {
        return String.format("(%d, %d)", row, col);
    }
    
    @Override
    public int hashCode() {
        return 31 * (row ^ col);
    }
    
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
//        if (row >= 15) {
//            System.out.println("error, row=" + row);
//        }
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
//        if (col >= 15) {
//            System.out.println("error, col=" + col);
//        }
        this.col = col;
    }
}
