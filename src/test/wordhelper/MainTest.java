package wordhelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MainTest {
    @Test
    public void rackPermuatations() {
        List<Tile> rack = Main.makeRack("aaaaa");
        List<List<Tile>> racks = Main.generateRacksForPermutations(rack, 0);
        assertEquals(1, racks.size());

        racks = Main.generateRacksForPermutations(rack,1);
        assertEquals(26, racks.size());

        racks = Main.generateRacksForPermutations(rack, 2);
        assertEquals(26 * 26, racks.size());
    }

    @Test
    public void permutations() {
        String rack = "aaaaa";
        List<List<Tile>> permutations = Main.generatePermutations(rack, 2);
        assertEquals(9260524, permutations.size());
    }

}
