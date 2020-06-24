package wordhelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    public void maxScore() throws Exception {
        class TestCase {
            TestCase(String name, String gameBoardPath, String rack, int expectedScore, Tiles expectedTiles) {
                this.name = name;
                this.gameBoardPath = gameBoardPath;
                this.rack = rack;
                this.expectedScore = expectedScore;
                this.expectedTiles = expectedTiles;
            }

            final String name;
            final String gameBoardPath;
            final String rack;
            final int expectedScore;
            final Tiles expectedTiles;
        }

        List<TestCase> testCases = new ArrayList<>();
        testCases.add(new TestCase(
                "CA - vertical starting at (2, 4)",
                "resources/easy-test.gbd",
                "enaogci",
                35,
                getTilesFor(
                        new Tile('c', new Location(2, 4)),
                        new Tile('a', new Location(3, 4))
                )
        ));
        testCases.add(new TestCase(
                "JA - horizontal starting at (0, 1)",
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

            System.out.println(board.getPrettyPrint());

            assertEquals(tc.expectedScore, firstResult.getScore(), String.format("max score calulation is not correct – new tiles: %s", firstResult.getTiles()));
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

    @Test
    public void computeScore() throws Exception {
        class TestCase {
            TestCase(String name, String gameBoardPath, Map<Location, Tile> tileMap, Orientation orientation, int expectedScore) {
                this.name = name;
                this.gameBoardPath = gameBoardPath;
                this.tileMap = tileMap;
                this.orientation = orientation;
                this.expectedScore = expectedScore;
            }

            final String name;
            final String gameBoardPath;
            final Map<Location, Tile> tileMap;
            final Orientation orientation;
            final int expectedScore;
        }

        List<TestCase> testCases = Arrays.asList(
                new TestCase(
                        "Invalid word placement",
                        "resources/easy-test.gbd",
                        getTileMapFor(
                                new Tile('c', new Location(2, 9)),
                                new Tile('o', new Location(3, 9)),
                                new Tile('i', new Location(4, 9)),
                                new Tile('n', new Location(5, 9)),
                                new Tile('a', new Location(6, 9)),
                                new Tile('g', new Location(7, 9)),
                                new Tile('e', new Location(8, 9))
                        ),
                        Orientation.VERTICAL,
                        0
                ),
                new TestCase(
                        "Invalid word placement 2",
                        "resources/easy-test.gbd",
                        getTileMapFor(
                                new Tile('c', new Location(9, 0))
                        ),
                        Orientation.VERTICAL,
                        0
                ),
                new TestCase(
                        "Good",
                        "resources/easy-test.gbd",
                        getTileMapFor(
                                new Tile('c', new Location(2, 4)),
                                new Tile('a', new Location(3, 4))
                        ),
                        Orientation.VERTICAL,
                        35
                )
        );

        for (TestCase tc : testCases) {
            GameBoard board = Main.makeGameBoardFromFile(tc.gameBoardPath);
            int score = Main.computeScore(tc.tileMap, board, tc.orientation);

            assertEquals(tc.expectedScore, score, String.format("%s: didn't get expected score", tc.name));
        }
    }

    @Test
    public void tryVerticalPlacements() throws Exception {
        class TestCase {
            TestCase(String name, String gameBoardPath, Tiles tiles, int expectedScore) {
                this.name = name;
                this.gameBoardPath = gameBoardPath;
                this.tiles = tiles;
                this.expectedScore = expectedScore;
            }

            final String name;
            final String gameBoardPath;
            final Tiles tiles;
            final int expectedScore;
        }

        List<TestCase> testCases = Arrays.asList(
                new TestCase(
                        "ZA, ATE - 14 points",
                        "resources/easy-test.gbd",
                        getTilesFor(new Tile('a', new Location(9, 0))),
                        14
                ),
                new TestCase(
                        "PAC, ZA, CATE - 35 points",
                        "resources/easy-test.gbd",
                        getTilesFor(
                                new Tile('c', new Location(0, 0)),
                                new Tile('a', new Location(0, 0))
                        ),
                        35
                )
        );

        for (TestCase tc : testCases) {
            GameBoard board = Main.makeGameBoardFromFile(tc.gameBoardPath);
            Result result = Main.tryVerticalPlacements(board, tc.tiles);

            assertEquals(tc.expectedScore, result.getScore(), String.format("%s: didn't get expected score", tc.name));
        }
    }

    @Test
    public void tryHorizontalPlacements() throws Exception {
        GameBoard board = Main.makeGameBoardFromFile("resources/easy-test.gbd");
        Tiles tiles = getTilesFor(
                new Tile('a', new Location(3, 6)),
                new Tile('g', new Location(3, 7)),
                new Tile('o', new Location(3, 8)),
                new Tile('n', new Location(3, 9)),
                new Tile('e', new Location(3, 10))
        );
        Result result = Main.tryHorizontalPlacements(board, tiles);

        assertEquals(16, result.getScore());
    }

    @Test
    public void invalidBoard() throws Exception {
        GameBoard board = Main.makeGameBoardFromFile("resources/easy-test.gbd");
        Map<Location, Tile> tileMap = getTileMapFor(
                new Tile('c', new Location(10, 10)),
                new Tile('o', new Location(11, 6)),
                new Tile('i', new Location(11, 4)),
                new Tile('n', new Location(10, 9)),
                new Tile('a', new Location(10, 9)),
                new Tile('g', new Location(10, 9)),
                new Tile('e', new Location(10, 9))
        );

        assertFalse(board.isValid(tileMap, Orientation.HORIZONTAL), "Invalid board was marked valid");
    }

    @Test
    public void performance() throws Exception{
        for (int i = 0; i < 100; i++) {
            long startTime = System.nanoTime();
            for (int j = 0; j < 5; j++) {
                tryHorizontalPlacements();
            }
            System.out.println((System.nanoTime() - startTime) / 1000);
        }
    }

    private Tiles getTilesFor(Tile... tiles) {
        return new Tiles(Arrays.asList(tiles));
    }

    private Map<Location, Tile> getTileMapFor(Tile... tiles) {
        Map<Location, Tile> tileMap = new HashMap<>(tiles.length);

        for (Tile tile : tiles) {
            tileMap.put(new Location(tile.getLocation()), tile);
            tile.setLocation(new Location());
        }

        return tileMap;
    }
}
