package wordhelper.config;

import wordhelper.Location;

/**
 * Author: Pete
 * Date: 2/19/2018
 * Time: 4:25 PM
 */
public class EasyBoardConfig extends BoardConfig {
    public EasyBoardConfig() {
        setSize(11);
        setCenter(new Location(5, 5));

        // Double Letters
        addDl(2, 2);
        addDl(2, 4);
        addDl(2, 6);
        addDl(2, 8);
        addDl(4, 2);
        addDl(4, 8);
        addDl(6, 2);
        addDl(6, 8);
        addDl(8, 2);
        addDl(8, 4);
        addDl(8, 6);
        addDl(8, 8);

        // Triple Letters
        addTl(0, 0);
        addTl(0, 10);
        addTl(3, 3);
        addTl(3, 7);
        addTl(7, 3);
        addTl(7, 7);
        addTl(10, 0);
        addTl(10, 10);

        // Double Words
        addDw(1, 1);
        addDw(1, 5);
        addDw(1, 9);
        addDw(5, 1);
        addDw(5, 9);
        addDw(9, 1);
        addDw(9, 5);
        addDw(9, 9);

        // Triple Words
        addTw(0, 2);
        addTw(0, 8);
        addTw(2, 0);
        addTw(2, 10);
        addTw(8, 0);
        addTw(8, 10);
        addTw(10, 2);
        addTw(10, 8);
    }
}
