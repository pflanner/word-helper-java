package wordhelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class MainTest {
    @Test
    public void rackPermutations() {
        Tiles rack = new Tiles("aaaaa");
        Set<Tiles> racks = Main.generateRacksForPermutations(rack, 0);
        assertEquals(1, racks.size());

        racks = Main.generateRacksForPermutations(rack,1);
        assertEquals(26, racks.size());

        racks = Main.generateRacksForPermutations(rack, 2);
        assertEquals(26 * 26, racks.size());
    }

    @Test
    public void permutations() {
        String rack = "aaaaa";
        Set<Tiles> permutations = Main.generatePermutations(rack, 2);
        assertEquals(35707, permutations.size());
    }

}
