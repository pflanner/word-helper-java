package wordhelper;

import wordhelper.config.BoardConfig;
import wordhelper.config.EasyBoardConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Pete
 * Date: 2/15/2018
 * Time: 6:14 AM
 */
public class Main {
    // TODO reduce human input. there is a lot of possibility for error manually inputting everything

    private static final ExecutorService executorService = Executors.newFixedThreadPool(12);
    private static final Properties config = loadConfig();
    private static final Set<String> dict = loadDictionary();
    private static final TrieNode trieDict = loadTrieDictionary();
    private static final boolean showAdditionalWords = Boolean.parseBoolean(config.getProperty("showAdditionalWords"));

    public static void main(String[] args) throws Exception {
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

    private static void playRegularGame() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
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

    private static void playDuel() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String saveDir = config.getProperty("saveDir");
        if (!saveDir.endsWith(File.separator)) {
            saveDir += File.separator;
        }

        Scanner sc = new Scanner(System.in);
        int boardIndex = 0;

        while (true) {
            GameBoard board;
            String boardPath = saveDir + "duel-" + (boardIndex + 1) + ".gbd";
            File f = new File(boardPath);
            if (!f.exists()) {
                board = new GameBoard(new EasyBoardConfig());
            } else {
                board = makeGameBoardFromFile(boardPath);
            }

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
                p("Board #" + (boardIndex + 1));
                p(board.getPrettyPrint());
            }

            saveGameBoard(board, boardPath);
            boardIndex = (boardIndex + 1) % 3;
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
                } else {
                    currentLocation.setRow(currentLocation.getRow() + 1);
                }
            }
        }
    }

    static GameBoard makeGameBoardFromFile(String fileName) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
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
            throw new IOException("IOException while reading properties file: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("ClassNotFoundException while reading properties file: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException while reading properties file: " + e.getMessage());
        } catch (InstantiationException e) {
            p("InstantiationException while reading properties file: " + e.getMessage());
            throw new InstantiationException("InstantiationException while reading properties file: " + e.getMessage());
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

            Result horizontalResult = tryHorizontalPlacements(board, permutation);
            Result verticalResult = tryVerticalPlacements(board, permutation);
            if (horizontalResult.getScore() > 0) {
                results.add(horizontalResult);
            }
            if (verticalResult.getScore() > 0) {
                results.add(verticalResult);
            }
        }

        if (results.size() > 0) {
            results.sort(Comparator.comparingInt(Result::getScore).reversed());
            Result firstResult = results.get(0);
            Location curLoc = new Location(firstResult.getStartLocation());
            for (Tile tile : firstResult.getTiles()) {
                tile.getLocation().setRow(curLoc.getRow());
                tile.getLocation().setCol(curLoc.getCol());

                while(!board.addNewTile(tile)) {
                    if (firstResult.getOrientation() == Orientation.HORIZONTAL) {
                        tile.getLocation().oneRight();
                    } else {
                        tile.getLocation().oneDown();
                    }
                    curLoc.setRow(tile.getLocation().getRow());
                    curLoc.setCol(tile.getLocation().getCol());
                }
            }
        }

        return results;
    }

    static Result tryHorizontalPlacements(GameBoard board, List<Tile> permutation) {
        Result result = new Result();
        List<Tile> tiles = new ArrayList<>(permutation);
        int boardSize = board.getConfig().getSize();
        final CountDownLatch latch = new CountDownLatch(boardSize * boardSize);

        for (int r = 0; r < board.getConfig().getSize(); r++) {
            for (int c = 0; c < board.getConfig().getSize(); c++) {
                final int row = r;
                final int col = c;
                executorService.execute(() -> {
                    horizontalPlacementsHelper(board, row, col, tiles, result);
                    latch.countDown();
                });
            }
        }
        result.setOrientation(Orientation.HORIZONTAL);

        try {
            latch.await();
        } catch (InterruptedException e) {
            p("InterruptedException while trying horizontal placements");
        }

        return result;
    }

    private static void horizontalPlacementsHelper(GameBoard board, int r, int c, List<Tile> tiles, final Result result) {
        TrieNode cur = trieDict;
        Location curLoc = new Location(r, c);
        Map<Location,Tile> newTiles = new HashMap<>(tiles.size());

        TRY_POS:
        for (Tile tile : tiles) {
            while (board.getOldTiles().containsKey(curLoc) && curLoc.getCol() < board.getConfig().getSize()) {
                char curLetter = board.getOldTiles().get(curLoc).getLetter();
                if (!cur.getChildren().containsKey(curLetter)) {
                    break TRY_POS;
                }
                cur = cur.getChildren().get(curLetter);
                curLoc.oneRight();
            }

            if (curLoc.getCol() < board.getConfig().getSize()) {
                char curLetter = tile.getLetter();
                if (!cur.getChildren().containsKey(curLetter)) {
                    break;
                }

                newTiles.put(new Location(curLoc), tile);

                cur = cur.getChildren().get(curLetter);
                curLoc.oneRight();
            } else {
                break;
            }
        }
        if (newTiles.size() == tiles.size()) {
            int score = computeScore(newTiles,  board, Orientation.HORIZONTAL);

            synchronized (result) {
                if (score > result.getScore()) {
                    result.setScore(score);
                    result.setTiles(tiles);
                    result.setStartLocation(new Location(r, c));
                }
            }
        }
    }

    static Result tryVerticalPlacements(GameBoard board, List<Tile> permutation) {
        Result result = new Result();
        List<Tile> tiles = new ArrayList<>(permutation);

        for (int r = 0; r < board.getConfig().getSize(); r++) {
            for (int c = 0; c < board.getConfig().getSize(); c++) {
                board.clearNewTiles();
                TrieNode cur = trieDict;
                Location curLoc = new Location(r, c);
                Map<Location,Tile> newTiles = new HashMap<>(tiles.size());

                TRY_POS:
                for (Tile tile : tiles) {
                    while (board.getOldTiles().containsKey(curLoc) && curLoc.getRow() < board.getConfig().getSize()) {
                        char curLetter = board.getOldTiles().get(curLoc).getLetter();
                        if (!cur.getChildren().containsKey((curLetter))) {
                            break TRY_POS;
                        }
                        cur = cur.getChildren().get(curLetter);
                        curLoc.oneDown();
                    }

                    if (curLoc.getRow() < board.getConfig().getSize()) {
                        char curLetter = tile.getLetter();
                        if (!cur.getChildren().containsKey(curLetter)) {
                            break;
                        }

                        newTiles.put(new Location(curLoc), tile);

                        cur = cur.getChildren().get(curLetter);
                        curLoc.oneDown();
                    } else {
                        break;
                    }
                }
                if (newTiles.size() == permutation.size()) {
                    int score = computeScore(newTiles, board, Orientation.VERTICAL);

                    if (score > result.getScore()) {
                        result.setScore(score);
                        result.setTiles(tiles);
                        result.setStartLocation(new Location(r, c));
                    }
                }
            }
        }
        result.setOrientation(Orientation.VERTICAL);
        return result;
    }

    public static int computeScore(Map<Location,Tile> newTiles, GameBoard board, Orientation orientation) {
        if (!board.isValid(newTiles, orientation)) {
            return 0;
        }

        BoardConfig config = board.getConfig();
        int score = 0;
        Set<List<Tile>> words = findNewWords(newTiles, board, orientation);
        for (List<Tile> word : words) {
            int wordScore = 0;
            int wordMultiplier = 1;

            for (Tile tile : word) {
                int tileScore = 0;
                if (!tile.isWildcard()) {
                    tileScore = config.getLetterPoints().get(tile.getLetter());
                    if (newTiles.containsKey(tile.getLocation())) {
                        if (config.getDls().contains(tile.getLocation())) {
                            tileScore *= 2;
                        } else if (config.getTls().contains(tile.getLocation())) {
                            tileScore *= 3;
                        }
                    }
                }

                if (newTiles.containsKey(tile.getLocation())) {
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
        if (words.size() > 0 && newTiles.size() == config.getRackSize()) {
            score += config.getAllTileBonus();
        }
        return score;
    }

    public static Set<List<Tile>> findNewWords(Map<Location,Tile> newTiles, GameBoard board, Orientation orientation) {
        Set<List<Tile>> words = new HashSet<>();
        int verticalWordCount = 0;
        int horizontalWordCount = 0;

        for (Location tileLoc : newTiles.keySet()) {
            List<Tile> word = new ArrayList<>();
            Location curLoc = new Location(tileLoc);

            // Vertical
            // find the start
            do {
                curLoc.oneUp();
            }
            while (board.getOldTiles().containsKey(curLoc) || newTiles.containsKey(curLoc));
            curLoc.oneDown();

            // add the tiles to the word
            while (board.getOldTiles().containsKey(curLoc) || newTiles.containsKey(curLoc)) {
                Tile tileToAdd = board.getOldTiles().getOrDefault(curLoc, newTiles.get(curLoc)).getCopy();
                tileToAdd.setLocation(new Location(curLoc));
                word.add(tileToAdd);
                curLoc.oneDown();
            }

            String strWord = listToString(word);

            boolean isValidWord = dict.contains(strWord);
            boolean isLoneWord = board.getOldTiles().isEmpty();

            if (isValidWord && (isLoneWord || word.size() > 1)) {
                words.add(word);
                verticalWordCount++;
            } else if (!isValidWord) {
                if (orientation == Orientation.VERTICAL) {
                    words.clear();
                    return words;
                }

                if (orientation == Orientation.HORIZONTAL && word.size() > 1) {
                    words.clear();
                    return words;
                }

                if (orientation == Orientation.SINGLE && word.size() > 1) {
                    words.clear();
                    return words;
                }
            }

            word = new ArrayList<>();
            curLoc.setRow(tileLoc.getRow());
            curLoc.setCol(tileLoc.getCol());

            // Horizontal
            // find the start
            do {
                curLoc.oneLeft();
            }
            while (board.getOldTiles().containsKey(curLoc) || newTiles.containsKey(curLoc));
            curLoc.oneRight();

            // add the tiles to the word
            while (board.getOldTiles().containsKey(curLoc) || newTiles.containsKey(curLoc)) {
                Tile tileToAdd = board.getOldTiles().getOrDefault(curLoc, newTiles.get(curLoc)).getCopy();
                tileToAdd.setLocation(new Location(curLoc));
                word.add(tileToAdd);

                curLoc.oneRight();
            }

            strWord = listToString(word);

            isValidWord = dict.contains(strWord);

            if (isValidWord) {
                if (isLoneWord || word.size() > 1) {
                    words.add(word);
                    horizontalWordCount++;
                }
            } else {
                if (orientation == Orientation.HORIZONTAL) {
                    words.clear();
                    return words;
                }

                if (orientation == Orientation.VERTICAL && word.size() > 1) {
                    words.clear();
                    return words;
                }

                if (orientation == Orientation.SINGLE && word.size() > 1) {
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

    private static void p(Integer i) {
        System.out.println(i);
    }
}
