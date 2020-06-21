package wordhelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainTest {
    @Test
    public void rackPermutations() {
        Tiles rack = new Tiles("aaaaa");
        Set<Tiles> racks = Main.generateRacksForPermutations(rack, 0);
        assertEquals(1, racks.size());

        racks = Main.generateRacksForPermutations(rack, 1);
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

    /**
     * Prevent regressions in max score computation with some benchmark cases.
     */
    @Test
    public void maxScore() {
        GameBoard board = Main.makeGameBoardFromFile("resources/easy-test.gbd");
        List<Result> results = Main.computeHighestScore(board, "enaocgi", 0);
        results.sort(Comparator.comparingInt(Result::getScore).reversed());
        Result firstResult = results.get(0);

        assertEquals(35, firstResult.getScore(), "max score calulation is not correct");
        assertNotNull(firstResult.getTiles(), "we didn't get a first result");
        assertNotEquals(0, firstResult.getTiles().size(), "first result was empty");

        Tiles expectedTiles = new Tiles();
        expectedTiles.add(new Tile('c', new Location(2, 4)));
        expectedTiles.add(new Tile('a', new Location(3, 4)));

        Set<Integer> foundIndices = new HashSet<>(2);

        for (Tile tile : firstResult.getTiles()) {
            for (int i = 0; i < expectedTiles.size(); i++) {
                if (tile.equals(expectedTiles.get(i))) {
                    foundIndices.add(i);
                }
            }
        }

        assertEquals(expectedTiles.size(), foundIndices.size(), "computed tiles did not match expectation");
    }
}
