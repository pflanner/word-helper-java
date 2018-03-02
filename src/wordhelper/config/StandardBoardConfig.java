package wordhelper.config;

/**
 * Author: Pete
 * Date: 2/15/2018
 * Time: 5:27 AM
 */
public class StandardBoardConfig extends BoardConfig {
    public StandardBoardConfig() {
        // Double Letters
        addDl(1, 2);
        addDl(1, 12);
        addDl(2, 1);
        addDl(2, 4);
        addDl(2, 10);
        addDl(2, 13);
        addDl(4, 2);
        addDl(4, 6);
        addDl(4, 8);
        addDl(4, 12);
        addDl(6, 4);
        addDl(6, 10);
        addDl(8, 4);
        addDl(8, 10);
        addDl(10, 2);
        addDl(10, 6);
        addDl(10, 8);
        addDl(10, 12);
        addDl(12, 1);
        addDl(12, 4);
        addDl(12, 10);
        addDl(12, 13);
        addDl(13, 2);
        addDl(13, 12);
        
        // Triple Letters
        addTl(0, 6);
        addTl(0, 8);
        addTl(3, 3);
        addTl(3, 11);
        addTl(5, 5);
        addTl(5, 9);
        addTl(6, 0);
        addTl(6, 14);
        addTl(8, 0);
        addTl(8, 14);
        addTl(9, 5);
        addTl(9, 9);
        addTl(11, 3);
        addTl(11, 11);
        addTl(14, 6);
        addTl(14, 8);
        
        // Double Words
        addDw(1, 5);
        addDw(1, 9);
        addDw(3, 7);
        addDw(5, 1);
        addDw(5, 13);
        addDw(7, 3);
        addDw(7, 11);
        addDw(9, 1);
        addDw(9, 13);
        addDw(11, 7);
        addDw(13, 5);
        addDw(13, 9);
        
        // Triple Words
        addTw(0, 3);
        addTw(0, 11);
        addTw(3, 0);
        addTw(3, 14);
        addTw(11, 0);
        addTw(11, 14);
        addTw(14, 3);
        addTw(14, 11);
    }
}
