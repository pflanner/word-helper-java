package wordhelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
        class TestCase {
            TestCase(String gameBoardPath, String rack, int expectedScore, Tiles expectedTiles) {
                this.gameBoardPath = gameBoardPath;
                this.rack = rack;
                this.expectedScore = expectedScore;
                this.expectedTiles = expectedTiles;
            }

            final String gameBoardPath;
            final String rack;
            final int expectedScore;
            final Tiles expectedTiles;
        }

        List<TestCase> testCases = new ArrayList<>();
        testCases.add(new TestCase(
                "resources/easy-test.gbd",
                "enaogci",
                35,
                getTilesFor(
                        new Tile('c', new Location(2, 4)),
                        new Tile('a', new Location(3, 4))
                )
        ));
        testCases.add(new TestCase(
                "resources/easy-test2.gbd",
                "jasiwpv",
                50,
                getTilesFor(
                        new Tile('j', new Location(0, 1)),
                        new Tile('a', new Location(0, 2))
                )
        ));

        for (TestCase tc : testCases) {
            GameBoard board = Main.makeGameBoardFromFile(tc.gameBoardPath);
            List<Result> results = Main.computeHighestScore(board, tc.rack, 0);
            results.sort(Comparator.comparingInt(Result::getScore).reversed());
            Result firstResult = results.get(0);

            assertEquals(tc.expectedScore, firstResult.getScore(), "max score calulation is not correct");
            assertNotNull(firstResult.getTiles(), "we didn't get tiles for the first result");

            Set<Integer> foundIndices = new HashSet<>(2);

            for (Tile tile : firstResult.getTiles()) {
                for (int i = 0; i < tc.expectedTiles.size(); i++) {
                    if (tile.equals(tc.expectedTiles.get(i))) {
                        foundIndices.add(i);
                    }
                }
            }

            assertEquals(tc.expectedTiles.size(), foundIndices.size(), "computed tiles did not match expectation");
        }
    }

    private Tiles getTilesFor(Tile... tiles) {
        return new Tiles(Arrays.asList(tiles));
    }
}
