package wordhelper;

import wordhelper.config.BoardConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

/**
 * Author: Pete
 * Date: 2/15/2018
 * Time: 6:14 AM
 */
public class Main {
    // TODO reduce human input. there is a lot of possibility for error manually inputting everything

    private static final Properties config = loadConfig();
    private static final Set<String> dict = loadDictionary();
    private static final TrieNode trieDict = loadTrieDictionary();
    private static final boolean showAdditionalWords = Boolean.parseBoolean(config.getProperty("showAdditionalWords"));

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String choice = "";

        while (!choice.equals("r") && !choice.equals("d")) {
            p("(R)egular game or (D)uel?");
            choice = sc.nextLine();

            if (choice.toLowerCase().equals("r")) {
                playRegularGame();
            } else if (choice.toLowerCase().equals("d")) {
                playDuel();
            } else {
                p(String.format("Sorry, I didn't understand your response: %s", choice));
            }
        }
    }

    private static void playRegularGame() {
        String again = "y";
        while ("y".equalsIgnoreCase(again)) {
            String saveDir = config.getProperty("saveDir");
            String saveFile = config.getProperty("saveFile");
            if (!saveDir.endsWith(File.separator)) {
                saveDir += File.separator;
            }
            GameBoard board = makeGameBoardFromFile(saveDir + saveFile);
            readWordFromStdIn(board);
            p(board.getPrettyPrint());

            Scanner sc = new Scanner(System.in);
            p("Compute best word?");
            String compute = sc.nextLine();

            if ("y".equalsIgnoreCase(compute)) {
                System.out.println("Enter rack letters: ");
                String rack = sc.nextLine();

                int wildcard = 0;
                String adjustedRack = "";
                for (char c : rack.toCharArray()) {
                    if (c == '?') {
                        wildcard++;
                    } else {
                        adjustedRack += c;
                    }
                }

                List<Result> results = computeHighestScore(board, adjustedRack, wildcard);
                results.sort(Comparator.comparingInt(Result::getScore).reversed());
                Result firstResult = results.get(0);
                System.out.format("Score: %d%n", firstResult.getScore());
                if (firstResult.getTiles() != null && firstResult.getTiles().size() > 0) {
                    board.clearNewTiles();
                    for (Tile tile : firstResult.getTiles()) {
                        board.addNewTile(tile);
                    }
                    System.out.println(board.getPrettyPrint());
                }

                if (showAdditionalWords) {
                    for (Result r : results) {
                        if (r.getTiles() != null && r.getTiles().size() > 0) {
                            Collection<Tile> tiles = r.getTiles();
                            Tile firstTile = null;

                            Queue<Tile> minHeap;
                            if (r.getOrientation() == Orientation.HORIZONTAL) {
                                minHeap = new PriorityQueue<>(tiles.size(), Comparator.comparingInt((tile) -> tile.getLocation().getCol()));
                            } else {
                                minHeap = new PriorityQueue<>(tiles.size(), Comparator.comparingInt((tile) -> tile.getLocation().getRow()));
                            }

                            for (Tile t : tiles) {
                                minHeap.offer(t);
                            }

                            String word = "";
                            while (!minHeap.isEmpty()) {
                                Tile tile = minHeap.poll();
                                if (firstTile == null) {
                                    firstTile = tile;
                                }
                                word += tile.getLetter();
                            }

                            if (firstTile == null) {
                                firstTile = new Tile();
                            }

                            System.out.format("Score: %d Word: %s Start: %s Orientation: %s%n", r.getScore(), word, firstTile.getLocation(), r.getOrientation());
                        }

                    }
                }
            }

            saveGameBoard(board, saveDir + saveFile);

            p("Again?");
            again = sc.nextLine();
        }
    }

    public static void playDuel() {
        String saveDir = config.getProperty("saveDir");
        String saveFile = config.getProperty("saveFile");
        if (!saveDir.endsWith(File.separator)) {
            saveDir += File.separator;
        }

        Scanner sc = new Scanner(System.in);
        GameBoard board = makeGameBoardFromFile(saveDir + saveFile);

        while (true) {
            System.out.println("Enter rack letters: ");
            String rack = sc.nextLine();

            int wildcard = 0;
            String adjustedRack = "";
            for (char c : rack.toCharArray()) {
                if (c == '?') {
                    wildcard++;
                } else {
                    adjustedRack += c;
                }
            }

            List<Result> results = computeHighestScore(board, adjustedRack, wildcard);
            results.sort(Comparator.comparingInt(Result::getScore).reversed());
            Result firstResult = results.get(0);
            System.out.format("Score: %d%n", firstResult.getScore());
            if (firstResult.getTiles() != null && firstResult.getTiles().size() > 0) {
                board.clearNewTiles();
                for (Tile tile : firstResult.getTiles()) {
                    board.addNewTile(tile);
                }
                System.out.println(board.getPrettyPrint());
            }
        }
    }

    private static void readWordFromStdIn(GameBoard gameBoard) {
        Scanner sc = new Scanner(System.in);
        p("Enter word: ");
        String word = sc.nextLine().toLowerCase();
        p("Enter start row: ");
        int startRow = sc.nextInt();
        p("Enter start column: ");
        int startCol = sc.nextInt();
        p("Enter orientation (h or v): ");
        Orientation orientation = sc.next().equalsIgnoreCase("h") ? Orientation.HORIZONTAL : Orientation.VERTICAL;

        Location currentLocation = new Location(startRow, startCol);
        for (Tile tile : new Tiles(word)) {
            if (!gameBoard.getOldTiles().containsKey(tile.getLocation())) {
                tile.setLocation(new Location(currentLocation));
                gameBoard.addOldTile(tile);
                if (orientation == Orientation.HORIZONTAL) {
                    currentLocation.setCol(currentLocation.getCol() + 1);
                } else if (orientation == Orientation.VERTICAL) {
                    currentLocation.setRow(currentLocation.getRow() + 1);
                }
            }
        }
    }

    static GameBoard makeGameBoardFromFile(String fileName) {
        GameBoard gameBoard;
        Properties p = new Properties();

        try {
            p.load(new FileInputStream(fileName));

            String configClassName = p.getProperty("config", "wordhelper.config.StandardBoardConfig");
            Class clazz = Class.forName(configClassName);
            BoardConfig boardConfig = (BoardConfig) clazz.newInstance();

            gameBoard = new GameBoard(boardConfig);
            for (int r = 0; r < boardConfig.getSize(); r++) {
                for (int c = 0; c < boardConfig.getSize(); c++) {
                    String key = makeTileKey(r, c);
                    if (p.containsKey(key)) {
                        String tileDescription = p.getProperty(key);
                        try (Scanner sc = new Scanner(tileDescription)) {
                            sc.useDelimiter(" ");
                            Character letter = tileDescription.charAt(0);
                            sc.next();
                            int row = sc.nextInt();
                            int col = sc.nextInt();
                            boolean isWildcard = sc.nextBoolean();

                            Tile tile = new Tile(letter, new Location(row, col), isWildcard);
                            gameBoard.addOldTile(tile);
                        }
                    }
                }
            }

        } catch (IOException e) {
            p("IOException while reading properties file: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            p("ClassNotFoundException while reading properties file: " + e.getMessage());
            return null;
        } catch (IllegalAccessException e) {
            p("IllegalAccessException while reading properties file: " + e.getMessage());
            return null;
        } catch (InstantiationException e) {
            p("InstantiationException while reading properties file: " + e.getMessage());
            return null;
        }
        return gameBoard;
    }

    private static String makeTileKey(int r, int c) {
        return "tile" + r + "," + c;
    }

    private static Set<String> loadDictionary() {
        Set<String> dict = new HashSet<>();
        File f = new File(config.getProperty("dictionaryPath"));
        try (FileInputStream fis = new FileInputStream(f)) {
            Scanner sc = new Scanner(fis);
            while (sc.hasNext()) {
                dict.add(sc.next());
            }
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException when loading dictionary");
        } catch (IOException e) {
            System.out.println("IOException when loading dictionary");
        }
        return dict;
    }

    private static TrieNode loadTrieDictionary() {
        TrieNode root = new TrieNode();
        File f = new File(config.getProperty("dictionaryPath"));
        try (FileInputStream fis = new FileInputStream(f)) {
            Scanner sc = new Scanner(fis);
            while (sc.hasNext()) {
                String word = sc.next();
                TrieNode cur = root;
                for (int i = 0; i < word.length(); i++) {
                    cur = cur.addChild(word.charAt(i));
                }
                cur.setWord(true);
            }
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException when loading dictionary");
        } catch (IOException e) {
            System.out.println("IOException when loading dictionary");
        }
        return root;
    }

    private static Properties loadConfig() {
        Properties config = new Properties();

        File f = new File("resources/config.properties");
        try (FileInputStream fis = new FileInputStream(f)) {
            config.load(fis);
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException when loading config");
        } catch (IOException e) {
            System.out.println("IOException when loading config");
        }
        return config;
    }

    static List<Result> computeHighestScore(GameBoard board, String rack, int wildcard) {
        List<Result> results = new ArrayList<>();

        int count = 0;

        Set<Tiles> permutations = generatePermutations(rack, wildcard);
        for (List<Tile> permutation : permutations) {
            printProgress(count++, permutations.size());

            results.add(tryHorizontalPlacements(board, permutation));
            results.add(tryVerticalPlacements(board, permutation));
        }


        return results;
    }

    private static Result tryHorizontalPlacements(GameBoard board, List<Tile> permutation) {
        Result result = new Result();
        List<Tile> tiles = new ArrayList<>(permutation);

        for (int r = 0; r < board.getConfig().getSize(); r++) {
            TRY_POS:
            for (int c = 0; c < board.getConfig().getSize(); c++) {
                board.clearNewTiles();
                TrieNode cur = trieDict;
                Location curLoc = new Location(r, c);
                for (Tile tile : tiles) {
                    tile.setLocation(curLoc);
                    while (!board.addNewTile(tile) && tile.getLocation().getCol() < board.getConfig().getSize()) {
                        char curLetter = board.getOldTiles().get(curLoc).getLetter();
                        if (!cur.getChildren().containsKey(curLetter)) {
                            break TRY_POS;
                        }
                        cur = cur.getChildren().get(curLetter);
                        curLoc = tile.getLocation().oneRight();
                        tile.setLocation(curLoc);
                    }

                    if (board.getNewTiles().containsKey(tile.getLocation())) {
                        char curLetter = board.getNewTiles().get(curLoc).getLetter();
                        if (!cur.getChildren().containsKey(curLetter)) {
                            break;
                        }
                        cur = cur.getChildren().get(curLetter);
                        curLoc = tile.getLocation().oneRight();
                    } else {
                        break;
                    }
                }
                if (board.getNewTiles().size() == permutation.size()) {
                    int score = computeScore(board);
                    // deep copy the list
                    List<Tile> newTiles = new ArrayList<>(board.getNewTiles().size());
                    for (Tile tile : board.getNewTiles().values()) {
                        newTiles.add(tile.getCopy());
                    }
                    Result newResult = new Result(score, newTiles);
                    if (newResult.getScore() > result.getScore()) {
                        result = newResult;
                    }
                }
            }
        }
        result.setOrientation(Orientation.HORIZONTAL);
        return result;
    }

    private static Result tryVerticalPlacements(GameBoard board, List<Tile> permutation) {
        Result result = new Result();
        List<Tile> tiles = new ArrayList<>(permutation);

        for (int r = 0; r < board.getConfig().getSize(); r++) {
            for (int c = 0; c < board.getConfig().getSize(); c++) {
                board.clearNewTiles();
                TrieNode cur = trieDict;
                Location curLoc = new Location(r, c);
                TRY_POS:
                for (Tile tile : tiles) {
                    tile.setLocation(curLoc);
                    while (!board.addNewTile(tile) && tile.getLocation().getRow() < board.getConfig().getSize()) {
                        char curLetter = board.getOldTiles().get(curLoc).getLetter();
                        if (!cur.getChildren().containsKey((curLetter))) {
                            break TRY_POS;
                        }
                        cur = cur.getChildren().get(curLetter);
                        curLoc = tile.getLocation().oneDown();
                        tile.setLocation(curLoc);
                    }

                    if (board.getNewTiles().containsKey(tile.getLocation())) {
                        char curLetter = board.getNewTiles().get(curLoc).getLetter();
                        if (!cur.getChildren().containsKey(curLetter)) {
                            break;
                        }
                        cur = cur.getChildren().get(curLetter);
                        curLoc = tile.getLocation().oneDown();
                    } else {
                        break;
                    }
                }
                if (board.getNewTiles().size() == permutation.size()) {
                    int score = computeScore(board);
                    // deep copy the list
                    List<Tile> newTiles = new ArrayList<>(board.getNewTiles().size());
                    for (Tile tile : board.getNewTiles().values()) {
                        newTiles.add(tile.getCopy());
                    }
                    Result newResult = new Result(score, newTiles);
                    if (newResult.getScore() > result.getScore()) {
                        result = newResult;
                    }
                }
            }
        }
        result.setOrientation(Orientation.VERTICAL);
        return result;
    }

    public static int computeScore(GameBoard board) {
        if (!board.isValid()) {
            return 0;
        }

        BoardConfig config = board.getConfig();
        int score = 0;
        Set<List<Tile>> words = findNewWords(board);
        for (List<Tile> word : words) {
            int wordScore = 0;
            int wordMultiplier = 1;

            for (Tile tile : word) {
                int tileScore = 0;
                if (!tile.isWildcard()) {
                    tileScore = config.getLetterPoints().get(tile.getLetter());
                    if (board.getNewTiles().containsKey(tile.getLocation())) {
                        if (config.getDls().contains(tile.getLocation())) {
                            tileScore *= 2;
                        } else if (config.getTls().contains(tile.getLocation())) {
                            tileScore *= 3;
                        }
                    }
                }

                if (board.getNewTiles().containsKey(tile.getLocation())) {
                    if (config.getDws().contains(tile.getLocation())) {
                        wordMultiplier *= 2;
                    } else if (config.getTws().contains(tile.getLocation())) {
                        wordMultiplier *= 3;
                    }
                }

                wordScore += tileScore;
            }

            score += wordScore * wordMultiplier;


        }
        if (words.size() > 0 && board.getNewTiles().size() == config.getRackSize()) {
            score += config.getAllTileBonus();
        }
        return score;
    }

    public static Set<List<Tile>> findNewWords(GameBoard board) {
        Set<List<Tile>> words = new HashSet<>();
        int verticalWordCount = 0;
        int horizontalWordCount = 0;

        for (Tile tile : board.getNewTiles().values()) {
            List<Tile> word = new ArrayList<>();

            // Vertical
            word.add(tile);
            Tile cur = tile;
            while (cur.getUpper() != null) {
                cur = cur.getUpper();
                word.add(0, cur);
            }

            cur = tile;
            while (cur.getLower() != null) {
                cur = cur.getLower();
                word.add(cur);
            }

            String strWord = listToString(word);

            boolean isValidWord = dict.contains(strWord);
            boolean isLoneWord = board.getOldTiles().isEmpty();

            if (isValidWord && (isLoneWord || word.size() > 1)) {
                words.add(word);
                verticalWordCount++;
            } else if (!isValidWord) {
                if (board.getOrientation() == Orientation.VERTICAL && word.size() == 1) {
                    words.clear();
                    return words;
                }

                if (board.getOrientation() == Orientation.HORIZONTAL && word.size() > 1) {
                    words.clear();
                    return words;
                }

                if (board.getOrientation() == Orientation.SINGLE && word.size() > 1) {
                    words.clear();
                    return words;
                }
            }

            word = new ArrayList<>();

            // Horizontal
            word.add(tile);
            cur = tile;
            while (cur.getLeft() != null) {
                cur = cur.getLeft();
                word.add(0, cur);
            }

            cur = tile;
            while (cur.getRight() != null) {
                cur = cur.getRight();
                word.add(cur);
            }

            strWord = listToString(word);

            isValidWord = dict.contains(strWord);

            if (isValidWord) {
                if (isLoneWord || word.size() > 1) {
                    words.add(word);
                    horizontalWordCount++;
                }
            } else {
                if (board.getOrientation() == Orientation.HORIZONTAL && word.size() == 1) {
                    words.clear();
                    return words;
                }

                if (board.getOrientation() == Orientation.VERTICAL && word.size() > 1) {
                    words.clear();
                    return words;
                }

                if (board.getOrientation() == Orientation.SINGLE && word.size() > 1) {
                    words.clear();
                    return words;
                }
            }
        }

        if (board.getOrientation() == Orientation.VERTICAL && verticalWordCount < 1) {
            words.clear();
        }
        if (board.getOrientation() == Orientation.HORIZONTAL && horizontalWordCount < 1) {
            words.clear();
        }

        return words;
    }

    private static String listToString(List<Tile> word) {
        StringBuilder sb = new StringBuilder();
        for (Tile tile : word) {
            sb.append(tile.getLetter());
        }
        return sb.toString();
    }

    static Set<Tiles> generatePermutations(String rack, int wildcards) {
        Set<Tiles> permutations = new HashSet<>();

        for (Tiles r : generateRacksForPermutations(new Tiles(rack), wildcards)) {
            for (int i = 1; i <= rack.length() + wildcards; i++) {
                generatePermutationsHelper(r, new Tiles(), i, permutations);
            }
        }

        return permutations;
    }

    private static void generatePermutationsHelper(Tiles rack, Tiles permutation, int limit, Set<Tiles> permutations) {
        if (permutation.size() == limit) {
            permutations.add(permutation);
        } else {
            for (int i = 0; i < rack.size(); i++) {
                Tile t = rack.get(i);
                // Do we need to make a copy of the tile?
                Tile tCopy = t.getCopy();

                Tiles rackCopy = copyRack(rack);
                Tiles permutationCopy = copyRack(permutation);
                permutationCopy.add(tCopy);
                rackCopy.remove(i);
                generatePermutationsHelper(rackCopy, permutationCopy, limit, permutations);
            }
        }
    }

    static Set<Tiles> generateRacksForPermutations(Tiles rack, int wildcards) {
        Set<Tiles> racks = new HashSet<>();
        if (wildcards == 0) {
            racks.add(rack);
        } else {
            for (char c : "abcdefghijlkmnopqrstuvwxyz".toCharArray()) {
                Tiles rackCopy = copyRack(rack);
                Tile t = new Tile(c);
                t.setWildcard(true);
                rackCopy.add(t);
                racks.addAll(generateRacksForPermutations(rackCopy, wildcards - 1));
            }
        }

        return racks;
    }

    static Tiles copyRack(Tiles rack) {
        if (rack == null) {
            return null;
        }

        return new Tiles(rack);
    }

    private static void printProgress(int count, int total) {
        if (total >= 100 && (count % (total / 100) == 0)) {
            int progress = (int) ((float) count / (float) total * 100f);
            System.out.format("|%-99s|%n", "=".repeat(progress));
        }
    }

    private static void saveGameBoard(GameBoard gameBoard, String fileName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            Properties p = new Properties();
            p.setProperty("config", gameBoard.getConfig().getClass().getCanonicalName());

            setTileProperties(gameBoard.getNewTiles().values(), p);
            setTileProperties(gameBoard.getOldTiles().values(), p);

            p.store(fileOutputStream, null);
            p("Saved game board to " + fileName);
        } catch (IOException e) {
            p("IOException opening file for write: " + e.getMessage());
        }
    }

    private static void setTileProperties(Collection<Tile> tiles, Properties p) {
        StringBuilder sb = new StringBuilder();
        for (Tile tile : tiles) {
            sb.append(tile.getLetter());
            sb.append(" ");
            sb.append(tile.getLocation().getRow());
            sb.append(" ");
            sb.append(tile.getLocation().getCol());
            sb.append(" ");
            sb.append(tile.isWildcard());

            p.setProperty(makeTileKey(tile.getLocation().getRow(), tile.getLocation().getCol()), sb.toString());
            sb.setLength(0);
        }
    }

    private static void p(String s) {
        System.out.println(s);
    }
}
