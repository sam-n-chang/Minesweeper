/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class BoardTest {
    private static final String BOARDS_PKG = "test/minesweeper/server/";
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    /**
     * @return a fresh instance of a board
     */
    public Board makeBoard(int x, int y) {
        try {
            /*
            Class<?> cls = Class.forName("minesweeper.Board");
            return (Board) cls.newInstance();
            */
            Class<?> cls = Class.forName("minesweeper.Board");
            Constructor constructor = cls.getDeclaredConstructor(new Class[] {int.class, int.class});
            return (Board) constructor.newInstance(new Object[]{x,y});

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Board makeBoardFromFile (String fileName) {
        try {
            File boradFile = new File(BOARDS_PKG + fileName);
            
            Class<?> cls = Class.forName("minesweeper.Board");
            Constructor constructor = cls.getDeclaredConstructor(new Class[] {File.class});
            return (Board) constructor.newInstance(new Object[]{boradFile});

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    /*
     * Testing strategy
     * ==================
     * 
     *
     * Build a new board.
     * 
     * test the board size X and Y
     * test various counters
     * 
     * Partition the inputs as follows:
     * 1. board size 1 x 1
     * 2. board size 1000 x 1000
     * 3. board size 10 x 20
     * 
     * Cover each part testing coverage.
     */
    
    @Test
    public void testBuildMinBoard() {
        int x=1;
        int y=1;
        
        Board bd = makeBoard(x, y);
        
        assertTrue("Board size X checking", bd.getBoardSizeX() == x);
        assertTrue("Board size Y checking", bd.getBoardSizeY() == y);
        
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
        assertTrue("Counter numUntouched checked", bd.getUntouched() == x*y);
        
        for (int i=0; i < x; i++) {
            for (int j=0; j < y; j++) {
                assertTrue("Square state checking", bd.getState(i,j) == Board.UNTOUCHED);
            }
        }
    }
   
    
    @Test
    public void testBuildMaxBoard() {
        int x=1000;
        int y=1000;
        
        Board bd = makeBoard(x, y);
        
        assertTrue("Board size X checking", bd.getBoardSizeX() == x);
        assertTrue("Board size Y checking", bd.getBoardSizeY() == y);
        
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
        assertTrue("Counter numUntouched checked", bd.getUntouched() == x*y);
        
        for (int i=0; i < x; i++) {
            for (int j=0; j < y; j++) {
                assertTrue("Square state checking", bd.getState(i,j) == Board.UNTOUCHED);
            }
        }
        
        //as the prob. of having bomb in each square is ~0.25
        //so the total number of bombs should be within the range of 20%-30%
        assertTrue("Counter numUntouched checked, lowerbound", bd.getBombs() > x*y*0.2);
        assertTrue("Counter numUntouched checked, upperbound", bd.getBombs() < x*y*0.3);
    }
    
    @Test
    public void testBuildBoard() {
        int x=10;
        int y=20;
        
        Board bd = makeBoard(x, y);
        
        assertTrue("Board size X checking", bd.getBoardSizeX() == x);
        assertTrue("Board size Y checking", bd.getBoardSizeY() == y);
        
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
        assertTrue("Counter numUntouched checked", bd.getUntouched() == x*y);
        
        for (int i=0; i < x; i++) {
            for (int j=0; j < y; j++) {
                assertTrue("Square state checking", bd.getState(i,j) == Board.UNTOUCHED);
            }
        }
        
        //as the prob. of having bomb in each square is ~0.25
        //so the total number of bombs should be within the range of 20%-30%
        assertTrue("Counter numUntouched checked, lowerbound", bd.getBombs() > x*y*0.15);
        assertTrue("Counter numUntouched checked, upperbound", bd.getBombs() < x*y*0.35);
    }
    
    /*
     * Testing strategy
     * ==================
     * 
     *
     * Build a new board from reading a file.
     * 
     * test the board size X and Y
     * test various counters
     * 
     * Partition the inputs as follows:
     * the board_file_1.txt:
     * 7 7
     * 0 0 0 0 0 0 0
     * 0 0 0 0 1 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 1 0 0 0 0 0 0
     * 
     * Cover each part testing coverage.
     */
    
    @Test
    public void testBuildBoardFile() {
        Board bd = makeBoardFromFile("board_file_1.txt");
        
        assertTrue("Board size X checking", bd.getBoardSizeX() == 7);
        assertTrue("Board size Y checking", bd.getBoardSizeY() == 7);
        
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
        assertTrue("Counter numUntouched checked", bd.getUntouched() == 7*7);
        
        assertTrue("Counter numUntouched checked", bd.getBombs() == 2);
        
        for (int i=0; i < 7; i++) {
            for (int j=0; j < 7; j++) {
                assertTrue("Square state checking", bd.getState(i,j) == Board.UNTOUCHED);
            }
        }

    }
    /*
     * Testing strategy
     * ==================
     * 
     *
     * Build a new board from reading a file.
     *
     * Partition the inputs as follows:
     * the board_file_4.txt:
     * 4 4
     * 0 0 0 0
     * 0 1 0 0
     * 0 0 0 0
     * 0 0 1 0
     * 
     * test command "dig"
     * 1. dig a "untouched" cell - 
     *    a) if there's a bomb - becomes "bomb"
     *    b) if there's no bomb - number of bombs contained by its neighboring squares,
     *       and if count is 0, uncover all neighboring squares. Repeat a)-b).
     * 2. dig a "flagged" cell - remains "flagged"
     * 3. dig a "dug" cell - remains "dug"
     * 
     * Cover each part testing coverage.
     */
    
    @Test
    public void testCommandDig() {
        Board bd = makeBoardFromFile("board_file_4.txt");
        
        assertTrue("Counter numBombs checked", bd.getBombs() == 2);
        
        assertTrue("Square state checking", bd.getState(3,0) == Board.UNTOUCHED);
        
        bd.dig(3,0);
        /*
         * - - 1 0
         * - - 1 0
         * - - 2 1
         * - - - -
         */
        assertTrue("Square state checking", bd.getState(2,0) == 1);
        assertTrue("Square state checking", bd.getState(3,0) == 0);
        assertTrue("Square state checking", bd.getState(2,1) == 1);
        assertTrue("Square state checking", bd.getState(3,1) == 0);
        assertTrue("Square state checking", bd.getState(2,2) == 2);
        assertTrue("Square state checking", bd.getState(3,2) == 1);
        
        bd.dig(1,1); // bomb!
        /*
         * 0 0 0 0
         * 0 0 0 0
         * 0 1 1 1
         * 0 1 - -
         */
        assertTrue("Square state checking", bd.getState(1,1) == 0); // state changed from BOMB to count
        assertTrue("Square state checking", bd.getState(1,2) == 1);
        assertTrue("Square state checking", bd.getState(1,3) == 1);
        assertTrue("Square state checking", bd.getState(2,0) == 0);
        assertTrue("Square state checking", bd.getState(2,1) == 0);
        assertTrue("Square state checking", bd.getState(2,2) == 1);
        
        bd.flag(2, 3);
        assertTrue("Square state checking", bd.getState(2,3) == Board.FLAGGED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 1);
        bd.dig(2, 3);
        assertTrue("Square state checking", bd.getState(2,3) == Board.FLAGGED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 1);
        
        bd.dig(2, 2);
        assertTrue("Square state checking", bd.getState(2,2) == 1);
    }
    /*
     * Testing strategy
     * ==================
     * 
     *
     * Build a new board from reading a file.
     *
     * Partition the inputs as follows:
     * the board_file_1.txt:
     * 7 7
     * 0 0 0 0 0 0 0
     * 0 0 0 0 1 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 1 0 0 0 0 0 0
     * 
     * test command "flag"
     * 1. flag a "untouched" cell - becomes "flagged"
     * 2. flag a "flagged" cell - remains "flagged"
     * 3. flag a "dug" cell - remains "dug"
     * 
     * Cover each part testing coverage.
     */
    
    @Test
    public void testCommandFlag() {
        Board bd = makeBoardFromFile("board_file_1.txt");
        
        assertTrue("Square state checking", bd.getState(3,3) == Board.UNTOUCHED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
        
        bd.flag(3, 3);
        assertTrue("Square state checking", bd.getState(3,3) == Board.FLAGGED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 1);
        
        bd.flag(3, 3);
        assertTrue("Square state checking", bd.getState(3,3) == Board.FLAGGED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 1);
        
        bd.dig(2, 2);
        assertTrue("Square state checking", bd.getState(2,2) == 0);
        bd.flag(2, 2);
        assertTrue("Square state checking", bd.getState(2,2) == 0);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 1);
    }
    /*
     * Testing strategy
     * ==================
     * 
     *
     * Build a new board from reading a file.
     *
     * Partition the inputs as follows:
     * the board_file_1.txt:
     * 7 7
     * 0 0 0 0 0 0 0
     * 0 0 0 0 1 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 0 0 0 0 0 0 0
     * 1 0 0 0 0 0 0
     * 
     * test command "deflag"
     * 1. deflag a "flagged" cell - becomes "untouched"
     * 2. deflag a "untouched" cell - remains "untouched"
     * 3. deflag a "dug" cell - remains "dug"
     * 
     * Cover each part testing coverage.
     */
    
    @Test
    public void testCommandDeflag() {
        Board bd = makeBoardFromFile("board_file_1.txt");
        
        assertTrue("Square state checking", bd.getState(3,3) == Board.UNTOUCHED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
        
        bd.flag(3, 3);
        assertTrue("Square state checking", bd.getState(3,3) == Board.FLAGGED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 1);
        
        bd.deflag(3, 3);
        assertTrue("Square state checking", bd.getState(3,3) == Board.UNTOUCHED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
        
        bd.deflag(3, 3);
        assertTrue("Square state checking", bd.getState(3,3) == Board.UNTOUCHED);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
        
        bd.dig(2, 2);
        assertTrue("Square state checking", bd.getState(2,2) == 0);
        bd.deflag(2, 2);
        assertTrue("Square state checking", bd.getState(2,2) == 0);
        assertTrue("Counter numFlagged checked", bd.getFlagged() == 0);
    }
    /*
     * Testing strategy
     * ==================
     * 
     *
     * Build a new board from reading a file.
     *
     * Partition the inputs as follows:
     * the board_file_4.txt:
     * 4 4
     * 0 0 0 0
     * 0 1 0 0
     * 0 0 0 0
     * 0 0 1 0
     * 
     * test command "draw"
     * draw
     * to draw a representation of the current state of the board in a readable way:
     * if the square is "untouched", place "-"
     *                  "flagged", place "F"
     *                  0 (dug, and no surrounding square has bomb), place " "
     *                  1~8, place the number 1~8 respectively.
     * each square is separated by a " ", except the last one in a row, instead, it'll be 
     * a new line "\n".
     * @return String which represents the drawing of the board.
     * 
     * Cover each part testing coverage.
     */
    
    @Test
    public void testCommandDraw() {
        String separator = System.lineSeparator();
        Board bd = makeBoardFromFile("board_file_4.txt");
        
        String textBoard = bd.draw();
        String[] str = textBoard.split(separator);
        for (int i=0; i < 4; i++) {
            assertEquals("- - - -", str[i]);
        }
        
        bd.dig(3,0);
        /*
         * - - 1 0
         * - - 1 0
         * - - 2 1
         * - - - -
         */
        str = bd.draw().split(separator);
        assertEquals("- - 1  ", str[0]);
        assertEquals("- - 1  ", str[1]);
        assertEquals("- - 2 1", str[2]);
        assertEquals("- - - -", str[3]);
        
        bd.dig(1,1); // bomb!
        /*
         * 0 0 0 0
         * 0 0 0 0
         * 0 1 1 1
         * 0 1 - -
         */
        str = bd.draw().split(separator);
        assertEquals("       ", str[0]);
        assertEquals("       ", str[1]);
        assertEquals("  1 1 1", str[2]);
        assertEquals("  1 - -", str[3]);
        
        bd.flag(2, 3);
        /*
         * 0 0 0 0
         * 0 0 0 0
         * 0 1 1 1
         * 0 1 F -
         */
        str = bd.draw().split(separator);
        assertEquals("       ", str[0]);
        assertEquals("       ", str[1]);
        assertEquals("  1 1 1", str[2]);
        assertEquals("  1 F -", str[3]);
        
        bd.deflag(2, 3);
        /*
         * 0 0 0 0
         * 0 0 0 0
         * 0 1 1 1
         * 0 1 - -
         */
        str = bd.draw().split(separator);
        assertEquals("       ", str[0]);
        assertEquals("       ", str[1]);
        assertEquals("  1 1 1", str[2]);
        assertEquals("  1 - -", str[3]);
    }
    /*
     * Testing strategy
     * ==================
     * 
     *
     * Build a new board from reading a file.
     *
     * Partition the inputs as follows:
     * the board_file_4.txt:
     * 4 4
     * 0 0 0 0
     * 0 1 0 0
     * 0 0 0 0
     * 0 0 1 0
     * 
     * test invalid command syntax
     * dig / flag / deflag with invalid cell index
     * 
     * Cover each part testing coverage.
     */
    
    @Test
    public void testCommandSyntax() {
        String separator = System.lineSeparator();
        Board bd = makeBoardFromFile("board_file_4.txt");
        
        String textBoard = bd.draw();
        String[] str = textBoard.split(separator);
        for (int i=0; i < 4; i++) {
            assertEquals("- - - -", str[i]);
        }
        
        // first make sure board is ok
        bd.dig(3,0);
        /*
         * - - 1 0
         * - - 1 0
         * - - 2 1
         * - - - -
         */
        str = bd.draw().split(separator);
        assertEquals("- - 1  ", str[0]);
        assertEquals("- - 1  ", str[1]);
        assertEquals("- - 2 1", str[2]);
        assertEquals("- - - -", str[3]);
        
        bd.dig(-1,-3); // test negative numbers
        /*
         * - - 1 0
         * - - 1 0
         * - - 2 1
         * - - - -
         */
        str = bd.draw().split(separator);
        assertEquals("- - 1  ", str[0]);
        assertEquals("- - 1  ", str[1]);
        assertEquals("- - 2 1", str[2]);
        assertEquals("- - - -", str[3]);
        
        bd.flag(2, 3);
        /*
         * - - 1 0
         * - - 1 0
         * - - 2 1
         * - - F -
         */
        str = bd.draw().split(separator);
        assertEquals("- - 1  ", str[0]);
        assertEquals("- - 1  ", str[1]);
        assertEquals("- - 2 1", str[2]);
        assertEquals("- - F -", str[3]);
        
        bd.flag(5, 2); // out of bound index
        /*
         * - - 1 0
         * - - 1 0
         * - - 2 1
         * - - F -
         */
        str = bd.draw().split(separator);
        assertEquals("- - 1  ", str[0]);
        assertEquals("- - 1  ", str[1]);
        assertEquals("- - 2 1", str[2]);
        assertEquals("- - F -", str[3]);
        
        bd.deflag(2, 30);  // out of bound index
        /*
         * - - 1 0
         * - - 1 0
         * - - 2 1
         * - - F -
         */
        str = bd.draw().split(separator);
        assertEquals("- - 1  ", str[0]);
        assertEquals("- - 1  ", str[1]);
        assertEquals("- - 2 1", str[2]);
        assertEquals("- - F -", str[3]);
    }
    
}
