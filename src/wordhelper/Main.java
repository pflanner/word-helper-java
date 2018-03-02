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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

/**
 * Author: Pete
 * Date: 2/15/2018
 * Time: 6:14 AM
 */
public class Main {
    // TODO reduce human input. there is a lot of possibility for error manually inputting everything
    // TODO read in 2 wildcards
    // TODO permutations in Java
    // TODO save game board to file and read it back in
    // TODO clean up and make a git repo
    
    private static Set<String> dict = loadDictionary();
    
    public static void main(String[] args) throws Exception{
        String again = "y";
        while ("y".equals(again)) {
            String saveDir = "c:/users/pete/ideaprojects/wordhelper/resources/";
            String saveFile = "becki.gbd";
            GameBoard board = makeGameBoardFromFile(saveDir + saveFile);
            readWordFromStdIn(board);
            p(board.getPrettyPrint());

            Scanner sc = new Scanner(System.in);
            p("Compute best word?");
            String compute = sc.nextLine();

            if ("y".equals(compute.toLowerCase())) {
                System.out.println("Enter rack letters: ");
                String rack = sc.nextLine();
                // TODO handle 2 wildcards
                boolean wildcard = rack.contains("?");

                Result result = computeHighestScore(board, rack, wildcard);
                System.out.format("Score: %d%n", result.getScore());
                if (result.getTiles() != null && result.getTiles().size() > 0) {
                    board.clearNewTiles();
                    for (Tile tile : result.getTiles()) {
                        board.addNewTile(tile);
                    }
                    System.out.println(board.getPrettyPrint());
                }
            }
            saveGameBoard(board, saveDir + saveFile);
            
            p("Again?");
            again = sc.nextLine();
        }
    }
    
    private static void readWordFromStdIn(GameBoard gameBoard) {
        Scanner sc = new Scanner(System.in);
        p("Enter word: ");
        String word = sc.nextLine();
        p("Enter start row: ");
        int startRow = sc.nextInt();
        p("Enter start column: ");
        int startCol = sc.nextInt();
        p("Enter orientation (h or v): ");
        Orientation orientation = sc.next().equals("h") ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        
        List<Tile> tiles = makeRack(word);
        Location currentLocation = new Location(startRow, startCol);
        for (Tile tile : tiles) {
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
    
    public static GameBoard makeGameBoardBen() {
        GameBoard b = new GameBoard();
        b.addOldTile(new Tile('c', new Location(3, 7)));
        b.addOldTile(new Tile('o', new Location(4, 7)));
        b.addOldTile(new Tile('v', new Location(5, 7)));
        b.addOldTile(new Tile('e', new Location(6, 7)));
        b.addOldTile(new Tile('y', new Location(7, 7)));

        b.addOldTile(new Tile('h', new Location(7, 2)));
        b.addOldTile(new Tile('o', new Location(7, 3)));
        b.addOldTile(new Tile('o', new Location(7, 4)));
        b.addOldTile(new Tile('k', new Location(7, 5)));
        b.addOldTile(new Tile('e', new Location(7, 6)));

        b.addOldTile(new Tile('f', new Location(8, 4)));
        b.addOldTile(new Tile('i', new Location(8, 5)));
        b.addOldTile(new Tile('t', new Location(8, 6)));
        
        b.addOldTile(new Tile('p', new Location(3, 0), true));
        b.addOldTile(new Tile('r', new Location(3, 1)));
        b.addOldTile(new Tile('o', new Location(3, 2)));
        b.addOldTile(new Tile('s', new Location(3, 3)));
        b.addOldTile(new Tile('o', new Location(3, 4)));
        b.addOldTile(new Tile('d', new Location(3, 5)));
        b.addOldTile(new Tile('i', new Location(3, 6)));

        b.addOldTile(new Tile('s', new Location(9, 1)));
        b.addOldTile(new Tile('a', new Location(9, 2)));
        b.addOldTile(new Tile('l', new Location(9, 3)));
        b.addOldTile(new Tile('t', new Location(9, 4)));

        b.addOldTile(new Tile('a', new Location(10, 3)));
        b.addOldTile(new Tile('g', new Location(11, 3)));
        b.addOldTile(new Tile('u', new Location(12, 3)));
        b.addOldTile(new Tile('n', new Location(13, 3)));
        b.addOldTile(new Tile('a', new Location(14, 3)));

        b.addOldTile(new Tile('b', new Location(2, 1)));
        b.addOldTile(new Tile('a', new Location(4, 1)));
        b.addOldTile(new Tile('t', new Location(5, 1)));
        b.addOldTile(new Tile('s', new Location(6, 1)));

        b.addOldTile(new Tile('b', new Location(2, 4)));
        b.addOldTile(new Tile('o', new Location(2, 5)));
        b.addOldTile(new Tile('x', new Location(2, 6)));

        b.addOldTile(new Tile('v', new Location(11, 0)));
        b.addOldTile(new Tile('e', new Location(11, 1)));
        b.addOldTile(new Tile('r', new Location(11, 2)));
        b.addOldTile(new Tile('e', new Location(11, 4)));
        b.addOldTile(new Tile('d', new Location(11, 5)));

        b.addOldTile(new Tile('f', new Location(5, 5)));
        b.addOldTile(new Tile('e', new Location(5, 6)));
        b.addOldTile(new Tile('e', new Location(5, 8)));
        b.addOldTile(new Tile('r', new Location(5, 9)));
        
        
        b.addOldTile(new Tile('a', new Location(12, 5)));
        b.addOldTile(new Tile('w', new Location(13, 5)));
        b.addOldTile(new Tile('t', new Location(14, 5)));

        b.addOldTile(new Tile('h', new Location(13, 6)));
        b.addOldTile(new Tile('i', new Location(13, 7)));
        b.addOldTile(new Tile('n', new Location(13, 8)));
        b.addOldTile(new Tile('g', new Location(13, 9)));
        b.addOldTile(new Tile('e', new Location(13, 10)));

        b.addOldTile(new Tile('l', new Location(14, 10)));
        b.addOldTile(new Tile('a', new Location(14, 11)));
        b.addOldTile(new Tile('c', new Location(14, 12)));
        b.addOldTile(new Tile('e', new Location(14, 13)));

        b.addOldTile(new Tile('i', new Location(9, 14)));
        b.addOldTile(new Tile('n', new Location(10, 14)));
        b.addOldTile(new Tile('p', new Location(11, 14)));
        b.addOldTile(new Tile('o', new Location(12, 14)));
        b.addOldTile(new Tile('u', new Location(13, 14)));
        
        b.addOldTile(new Tile('j', new Location(8, 0)));
        b.addOldTile(new Tile('o', new Location(8, 1)));

        b.addOldTile(new Tile('l', new Location(5, 13)));
        b.addOldTile(new Tile('a', new Location(6, 13)));
        b.addOldTile(new Tile('r', new Location(7, 13)));
        b.addOldTile(new Tile('u', new Location(8, 13)));
        b.addOldTile(new Tile('m', new Location(9, 13)));

        b.addOldTile(new Tile('h', new Location(4, 14)));
        b.addOldTile(new Tile('i', new Location(5, 14)));
        b.addOldTile(new Tile('d', new Location(6, 14)));
        b.addOldTile(new Tile('e', new Location(7, 14)));

        b.addOldTile(new Tile('t', new Location(14, 5)));
        b.addOldTile(new Tile('i', new Location(14, 6)));
        b.addOldTile(new Tile('d', new Location(14, 7)));
        b.addOldTile(new Tile('e', new Location(14, 8)));

        b.addOldTile(new Tile('g', new Location(6, 12)));
        b.addOldTile(new Tile('e', new Location(7, 12)));
        b.addOldTile(new Tile('m', new Location(8, 12)));

        b.addOldTile(new Tile('w', new Location(9, 10)));
        b.addOldTile(new Tile('u', new Location(10, 10)));
        b.addOldTile(new Tile('r', new Location(11, 10)));
        b.addOldTile(new Tile('z', new Location(12, 10)));

        b.addOldTile(new Tile('p', new Location(7, 11)));
        b.addOldTile(new Tile('e', new Location(8, 11)));
        
        b.addOldTile(new Tile('y', new Location(4, 8)));
        b.addOldTile(new Tile('n', new Location(6, 8)));
        b.addOldTile(new Tile('s', new Location(7, 8)));

        b.addOldTile(new Tile('q', new Location(2, 10)));
        b.addOldTile(new Tile('a', new Location(3, 10)));
        b.addOldTile(new Tile('t', new Location(4, 10)));
        b.addOldTile(new Tile('s', new Location(5, 10)));

        b.addOldTile(new Tile('t', new Location(0, 11)));
        b.addOldTile(new Tile('a', new Location(1, 11)));
        b.addOldTile(new Tile('i', new Location(2, 11)));
        b.addOldTile(new Tile('n', new Location(3, 11)));
        
        return b;
    }

    public static GameBoard makeGameBoard() {
        GameBoard b = new GameBoard();
        b.addOldTile(new Tile('a', new Location(3, 7)));
        b.addOldTile(new Tile('d', new Location(4, 7), true));
        b.addOldTile(new Tile('d', new Location(5, 7)));
        b.addOldTile(new Tile('a', new Location(6, 7)));
        b.addOldTile(new Tile('x', new Location(7, 7)));


        b.addOldTile(new Tile('r', new Location(7, 5)));
        b.addOldTile(new Tile('a', new Location(7, 6)));


        b.addOldTile(new Tile('o', new Location(5, 8)));
        b.addOldTile(new Tile('r', new Location(5, 9)));
        b.addOldTile(new Tile('k', new Location(5, 10)));
        b.addOldTile(new Tile('i', new Location(5, 11)));
        b.addOldTile(new Tile('e', new Location(5, 12)));
        b.addOldTile(new Tile('r', new Location(5, 13)));


        b.addOldTile(new Tile('b', new Location(4, 4)));
        b.addOldTile(new Tile('u', new Location(4, 5)));
        b.addOldTile(new Tile('r', new Location(4, 6)));
        b.addOldTile(new Tile('s', new Location(4, 8)));

        b.addOldTile(new Tile('c', new Location(3, 11)));
        b.addOldTile(new Tile('l', new Location(4, 11)));
        b.addOldTile(new Tile('n', new Location(6, 11)));
        b.addOldTile(new Tile('g', new Location(7, 11)));

        b.addOldTile(new Tile('t', new Location(3, 9)));
        b.addOldTile(new Tile('e', new Location(3, 10)));
        b.addOldTile(new Tile('h', new Location(3, 12)));
        b.addOldTile(new Tile('n', new Location(3, 13)));
        b.addOldTile(new Tile('o', new Location(3, 14)));

        b.addOldTile(new Tile('e', new Location(8, 5)));
        b.addOldTile(new Tile('v', new Location(9, 5)));
        b.addOldTile(new Tile('i', new Location(10, 5)));
        b.addOldTile(new Tile('s', new Location(11, 5)));
        b.addOldTile(new Tile('o', new Location(12, 5)));
        b.addOldTile(new Tile('r', new Location(13, 5)));
        
        b.addOldTile(new Tile('v', new Location(14, 0)));
        b.addOldTile(new Tile('a', new Location(14, 1)));
        b.addOldTile(new Tile('l', new Location(14, 2)));
        b.addOldTile(new Tile('l', new Location(14, 3)));
        b.addOldTile(new Tile('e', new Location(14, 4)));
        b.addOldTile(new Tile('y', new Location(14, 5)));

        b.addOldTile(new Tile('j', new Location(11, 0)));
        b.addOldTile(new Tile('a', new Location(11, 1)));
        b.addOldTile(new Tile('u', new Location(11, 2)));
        b.addOldTile(new Tile('n', new Location(11, 3)));
        b.addOldTile(new Tile('t', new Location(11, 4)));

        b.addOldTile(new Tile('f', new Location(5, 0)));
        b.addOldTile(new Tile('a', new Location(5, 1)));
        b.addOldTile(new Tile('d', new Location(5, 2)));
        b.addOldTile(new Tile('g', new Location(5, 3)));
        b.addOldTile(new Tile('e', new Location(5, 4)));

        b.addOldTile(new Tile('w', new Location(2, 0)));
        b.addOldTile(new Tile('a', new Location(3, 0)));
        b.addOldTile(new Tile('i', new Location(4, 0)));
        b.addOldTile(new Tile('e', new Location(6, 0)));
        b.addOldTile(new Tile('d', new Location(7, 0)));

        b.addOldTile(new Tile('r', new Location(8, 2)));
        b.addOldTile(new Tile('o', new Location(9, 2)));
        b.addOldTile(new Tile('q', new Location(10, 2)));
        b.addOldTile(new Tile('e', new Location(12, 2)));

        b.addOldTile(new Tile('c', new Location(1, 13), true));
        b.addOldTile(new Tile('e', new Location(2, 13)));
        b.addOldTile(new Tile('t', new Location(4, 13)));
        b.addOldTile(new Tile('i', new Location(6, 13)));
        b.addOldTile(new Tile('o', new Location(7, 13)));
        b.addOldTile(new Tile('l', new Location(8, 13)));
        b.addOldTile(new Tile('e', new Location(9, 13)));

        b.addOldTile(new Tile('o', new Location(8, 14)));
        b.addOldTile(new Tile('n', new Location(9, 14)));
        b.addOldTile(new Tile('t', new Location(10, 14)));
        b.addOldTile(new Tile('o', new Location(11, 14)));


        b.addOldTile(new Tile('m', new Location(0, 11)));
        b.addOldTile(new Tile('u', new Location(1, 11)));
        b.addOldTile(new Tile('s', new Location(2, 11)));

        b.addOldTile(new Tile('h', new Location(12, 6)));
        b.addOldTile(new Tile('e', new Location(13, 6)));
        b.addOldTile(new Tile('s', new Location(14, 6)));


        b.addOldTile(new Tile('o', new Location(12, 0)));
        b.addOldTile(new Tile('b', new Location(12, 1)));
        b.addOldTile(new Tile('e', new Location(12, 2)));
        
        b.addOldTile(new Tile('f', new Location(7, 3)));
        b.addOldTile(new Tile('e', new Location(8, 3)));
        b.addOldTile(new Tile('w', new Location(9, 3)));

        b.addOldTile(new Tile('a', new Location(7, 1)));
        b.addOldTile(new Tile('a', new Location(8, 1)));
        b.addOldTile(new Tile('h', new Location(9, 1)));

        b.addOldTile(new Tile('p', new Location(13, 4)));
        b.addOldTile(new Tile('z', new Location(13, 7)));
        b.addOldTile(new Tile('e', new Location(13, 8)));
        b.addOldTile(new Tile('s', new Location(13, 9)));
        
        b.addOldTile(new Tile('d', new Location(14, 8)));
        b.addOldTile(new Tile('i', new Location(14, 9)));
        b.addOldTile(new Tile('t', new Location(14, 10)));
        b.addOldTile(new Tile('t', new Location(14, 11)));
        b.addOldTile(new Tile('y', new Location(14, 12)));
        
        b.addOldTile(new Tile('u', new Location(13, 11)));
        b.addOldTile(new Tile('m', new Location(13, 12)));
        b.addOldTile(new Tile('p', new Location(13, 13)));

        b.addOldTile(new Tile('i', new Location(6, 10)));
        b.addOldTile(new Tile('d', new Location(6, 12)));
        b.addOldTile(new Tile('e', new Location(6, 14)));

        b.addOldTile(new Tile('g', new Location(11, 13)));
        b.addOldTile(new Tile('i', new Location(12, 13)));

        return b;
    }
    
    private static GameBoard makeGameBoardFromFile(String fileName) {
        GameBoard gameBoard;
        Properties p = new Properties();
        
        try {
            p.load(new FileInputStream(fileName));

            String configClassName = p.getProperty("config", "wordhelper.StandardBoardConfig");
            Class clazz = Class.forName(configClassName);
            BoardConfig boardConfig = (BoardConfig)clazz.newInstance(); 

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
    
    public static GameBoard makeGameBoardEasy() {
        GameBoard b = new GameBoard(new EasyBoardConfig());
        
        b.addOldTile(new Tile('v', new Location(5, 4)));
        b.addOldTile(new Tile('e', new Location(5, 5)));
        b.addOldTile(new Tile('r', new Location(5, 6)));
        b.addOldTile(new Tile('i', new Location(5, 7)));
        b.addOldTile(new Tile('t', new Location(5, 8)));
        b.addOldTile(new Tile('e', new Location(5, 9)));

        b.addOldTile(new Tile('z', new Location(3, 7)));
        b.addOldTile(new Tile('e', new Location(4, 7)));
        b.addOldTile(new Tile('n', new Location(6, 7)));

        b.addOldTile(new Tile('s', new Location(5, 10)));
        b.addOldTile(new Tile('o', new Location(6, 10)));
        b.addOldTile(new Tile('c', new Location(7, 10)));
        b.addOldTile(new Tile('k', new Location(8, 10)));

        b.addOldTile(new Tile('m', new Location(0, 8)));
        b.addOldTile(new Tile('o', new Location(1, 8)));
        b.addOldTile(new Tile('x', new Location(2, 8)));
        b.addOldTile(new Tile('a', new Location(3, 8)));

        b.addOldTile(new Tile('f', new Location(1, 5)));
        b.addOldTile(new Tile('l', new Location(1, 6)));
        b.addOldTile(new Tile('o', new Location(1, 7)));
        b.addOldTile(new Tile('d', new Location(1, 9)));

        b.addOldTile(new Tile('n', new Location(0, 1)));
        b.addOldTile(new Tile('i', new Location(0, 2)));
        b.addOldTile(new Tile('t', new Location(0, 3)));
        b.addOldTile(new Tile('r', new Location(0, 4)));
        b.addOldTile(new Tile('e', new Location(0, 5)));

        b.addOldTile(new Tile('a', new Location(9, 5)));
        b.addOldTile(new Tile('i', new Location(9, 6)));
        b.addOldTile(new Tile('s', new Location(9, 7)));
        b.addOldTile(new Tile('l', new Location(9, 8)));
        b.addOldTile(new Tile('e', new Location(9, 9)));
        b.addOldTile(new Tile('s', new Location(9, 10)));

        b.addOldTile(new Tile('j', new Location(10, 2)));
        b.addOldTile(new Tile('a', new Location(10, 3)));
        b.addOldTile(new Tile('y', new Location(10, 4)));
        b.addOldTile(new Tile('s', new Location(10, 5)));

        b.addOldTile(new Tile('a', new Location(6, 8)));
        b.addOldTile(new Tile('p', new Location(7, 8)));
        b.addOldTile(new Tile('a', new Location(8, 8)));
        b.addOldTile(new Tile('o', new Location(10, 8)));
        
        return b;
    }
    
    private static List<Tile> makeRack(String strRack) {
        List<Tile> rack = new ArrayList<>(strRack.length());
        for (int i = 0; i < strRack.length(); i++) {
            char c = strRack.charAt(i);
            Tile tile = new Tile(c);
            if (c == '?') {
                tile.setLetter(strRack.charAt(++i));
                tile.setWildcard(true);
            }
            rack.add(tile);
        }
        
        return rack;
    }
    
    private static Set<String> loadDictionary() {
        Set<String> dict = new HashSet<>();
        File f = new File("c:/users/pete/pycharmprojects/wordhelper/resources/words.txt");
        try (FileInputStream fis = new FileInputStream(f)) {
            Scanner sc = new Scanner(fis);
            while (sc.hasNext()) {
                dict.add(sc.next());
            }
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException");
        } catch (IOException e) {
            System.out.println("IOException");
        }
        return dict;
    }
    
    private static Result computeHighestScore(GameBoard board, String rack, boolean wildcard) throws Exception {
        Result result = new Result();
        
        int count = 0;
        
        List<List<Tile>> permutations = generatePermutationsPython(rack, wildcard);
        for (List<Tile> permutation : permutations) {
            printProgress(count++, permutations.size());

            Result hResult = tryHorizontalPlacements(board, permutation);
            Result vResult = tryVerticalPlacements(board, permutation);
            Result curResult = hResult.getScore() > vResult.getScore() ? hResult : vResult;
            result = curResult.getScore() > result.getScore() ? curResult : result;
        }
        
        
        return result;
    }

    private static Result tryHorizontalPlacements(GameBoard board, List<Tile> permutation) {
        Result result = new Result();
        List<Tile> tiles = new ArrayList<>(permutation);

        for (int r = 0; r < board.getConfig().getSize(); r++) {
            for (int c = 0; c < board.getConfig().getSize(); c++) {
                board.clearNewTiles();
                Location curLoc = new Location(r, c);
                for (Tile tile : tiles) {
                    tile.setLocation(curLoc);
                    while(!board.addNewTile(tile) && tile.getLocation().getCol() < board.getConfig().getSize()) {
                        tile.getLocation().setRow(tile.getLocation().getRow());
                        tile.getLocation().setCol(tile.getLocation().getCol() + 1);
                    }
                    if (board.getNewTiles().containsKey(tile.getLocation())) {
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
        return result;
    }

    private static Result tryVerticalPlacements(GameBoard board, List<Tile> permutation) {
        Result result = new Result();
        List<Tile> tiles = new ArrayList<>(permutation);

        for (int r = 0; r < board.getConfig().getSize(); r++) {
            for (int c = 0; c < board.getConfig().getSize(); c++) {
                board.clearNewTiles();
                Location curLoc = new Location(r, c);
                for (Tile tile : tiles) {
                    tile.setLocation(curLoc);
                    while(!board.addNewTile(tile) && tile.getLocation().getRow() < board.getConfig().getSize()) {
                        tile.getLocation().setRow(tile.getLocation().getRow() + 1);
                        tile.getLocation().setCol(tile.getLocation().getCol());
                    }
                    if (board.getNewTiles().containsKey(tile.getLocation())) {
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
        return result;
    }
    
    public static int computeScore(GameBoard board) {
        if(!board.isValid()) {
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

    private static List<List<Tile>> generatePermutationsPython(String rack, boolean wildcard) throws Exception {
        // TODO using python itertools to generate permutations for now
        String pythonWildcard = wildcard ? "True" : "False";
        List<List<Tile>> permutations = new ArrayList<>();
        String python = "c:/programdata/anaconda3/python.exe";
        Runtime.getRuntime().exec(python + " c:/users/pete/pycharmprojects/wordhelper/generate_permutations.py \"" + rack + "\" " + pythonWildcard);
        Thread.sleep(1000);
        try (Scanner fileScanner = new Scanner(new FileInputStream("c:/users/pete/pycharmprojects/wordhelper/resources/permutations.txt"))) {
            while (fileScanner.hasNext()) {
                String s = fileScanner.next();
                permutations.add(makeRack(s));
            }
        }
        return permutations;
    }
    
    private static void printProgress(int count, int total) {
        if (total >= 100 && (count % (total / 100) == 0)) {
            int progress = (int)((float)count / (float)total * 100f);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < progress; i++) {
                sb.append('=');
            }
            System.out.format("|%-99s|%n", sb.toString());
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
