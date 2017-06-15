/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.util.Collections;
import java.util.Random;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

//enum BoardState {UNTOUCHED, FLAGGED, DUG};

/**
 * TODO: Specification
 */
public class Board {
    
    // some predefined constants to be used as board states:
    public static final int UNTOUCHED = -1;
    public static final int FLAGGED = -2;
    public static final int BOMB = -3;
    public static final int NOP = -4;
    
    // the size of the board specified.
    private final int sizeX;
    private final int sizeY;
    
    // number of bombs generated for this board.
    private int numBombs;
    
    // number of squares "untouched"
    private int numUntouched;
    
    // number of squares "flagged"
    private int numFlagged;
    
    // number of squares "dug"
    private int numDug;
    
    // indicates where a bomb is planted
    private boolean[][] minesField;
    
    // current state of this board. Each square may be in one of the states {UNTOUCHED, FLAGGED} or 
    // a number between 0..8 for a dug empty square with number of neighboring squares containing a bomb.
    private int[][] board;
     
    // rep invariant:
    //   1. sizeX > 0, sizeY > 0
    //   2. numBombs <= sizeX * sizeY
    //   3. numUntouched + numFlagged + numDug == sizeX * sizeY
    //
    // abstraction function:
    //    represents the state of a game of Minesweeper's board. 
    //
    // All reps except the state are private so no rep exposure risk.
    //    the states are all constants so are safe from being mutated.
    //
    // Thread safety argument:
    //   all accesses to board happen within Board's methods,
    //   which are all guarded by Board's lock.
       
    // TODO: Specify, test, and implement in problem 2
    /**
     * constructor
     * @param int sizeX - the width of the board (X axis)
     * @param int sizeY - the length of the board (Y axis)
     * 
     * construct a board with dimension sizeX x sizeY, each square is randomly assigned with a bomb 
     * (with a probability of 0.25) or not.
     */
    public Board (int sizeX, int sizeY) {
        // initialize all instance variables
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        numUntouched = sizeX * sizeY;
        numFlagged = 0;
        numDug = 0;
        
        minesField = new boolean[sizeX][sizeY];
        board = new int[sizeX][sizeY];
        
        Random rand = new Random();
        int i,j;
        boolean assign;
        
        this.numBombs=0;
    
        // initialize the board and minesField
        for (i=0; i < sizeX; i++) {
            for (j=0; j < sizeY; j++) {
                
                board[i][j] = UNTOUCHED;
                
                // random generator will generate a random integer between 0..3
                // the probability of having a 0 is 1/4, when this happens
                // we will assign the square (i,j) with a bomb.
                assign = rand.nextInt(4)==0;
                if (assign) {
                    minesField[i][j] = true; // assign a bomb
                    this.numBombs++;
                } else {
                    minesField[i][j] = false;
                }
            }
        }
    }
    
    /**
     * constructor
     * @param File file - the text file contains the board to initialize. The first line of the file
     *   specifies the board size, and it must be followed by exactly Y lines, where each line 
     *   must contain exactly X values. If the file is properly formatted, the Board should be 
     *   instantiated such that square i,j has a bomb if and only if the i¡¦th VAL in LINE j of 
     *   the input is 1. If the file is improperly formatted, the program should throw an 
     *   unchecked exception (RuntimeException). 
     *   
     * The grammar of the file:
     * 
     * FILE ::= BOARD LINE+
     * BOARD := X SPACE Y NEWLINE
     * LINE ::= (VAL SPACE)* VAL NEWLINE
     * VAL ::= 0 | 1
     * X ::= INT
     * Y ::= INT
     * SPACE ::= " "
     * NEWLINE ::= "\n" | "\r" "\n"?
     * INT ::= [0-9]+
     * 
     */
    public Board (File file) throws IOException {
        int sizeX;
        int sizeY;
        
        /* for debug \/
        if (file.exists() && file.isFile() && file.canRead()) {
            System.out.println("File can be read!");
        }
        System.out.println("Board is attempting to read from file in: "+file.getCanonicalPath());
        */
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
                        
            // the first line should contain the board size
            String line = reader.readLine();
            if (line == null) {
                //empty file
                throw new RuntimeException("empty file!");
            } else {
                String[] sizes = line.split(" ");

                sizeX = Integer.parseInt(sizes[0]);
                sizeY = Integer.parseInt(sizes[1]);
                
                if ((sizeX <= 0) || (sizeY <= 0)) {
                    // if either size is illegal
                    throw new RuntimeException("invalid board size, x="+sizeX+",y="+sizeY);
                }
            }
            // initialize all instance variables
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            numUntouched = sizeX * sizeY;
            numFlagged = 0;
            numDug = 0;
            numBombs = 0;
            minesField = new boolean[sizeX][sizeY];
            board = new int[sizeX][sizeY];
            
            // continue to process the 2nd line...
            for (int i = 0; i < sizeY; i++) {
                line = reader.readLine();
                if (line == null) {
                    //not enough rows in file
                    throw new RuntimeException("not enough rows in file!");
                }
                    
                String[] mines = line.split(" ");
                int arraySize = mines.length;
                if (arraySize != sizeX)
                    throw new RuntimeException("invalid row read! length="+arraySize);
                    
                //System.out.println();  //debug
                //System.out.print("(y="+i+"):");  //debug
                for (int j = 0; j < sizeX; j++) {    
                    board[j][i] = UNTOUCHED;
                        
                    int n = Integer.parseInt(mines[j]);
                    //System.out.print(n+" ");  //debug
                    if (n == 1) {
                        minesField[j][i] = true; // assign a bomb
                        this.numBombs++;
                    } else if (n == 0) {
                        minesField[j][i] = false;
                    } else {
                        throw new RuntimeException("invalid number read ("+n+")");
                    }
                }
            }
            //System.out.println();  //debug
            reader.close();
        }   
    }
    
    // assert the rep invariant¡G
    // 
    //   1. sizeX > 0, sizeY > 0
    //   2. numBombs <= sizeX * sizeY
    //   3. numUntouched + numFlagged + numDug == sizeX * sizeY
    private void checkRep() {
        if ((sizeX <= 0) || (sizeY <= 0)) 
            throw new RuntimeException("Board sizes must greater than zero (sizeX = "+sizeX+", sizeY = "+sizeY+")");
        if (numBombs > sizeX*sizeY)
            throw new RuntimeException("numBombs too big (numBombs = "+numBombs+", sizeX = "+sizeX+", sizeY = "+sizeY+")");
        if (numUntouched + numFlagged + numDug != sizeX * sizeY) 
            throw new RuntimeException("number of cells don't match! numUntouched = "+numUntouched+
                                       ",numFlagged = "+numFlagged+",numDug = "+numDug+
                                       "sizeX = "+sizeX+",sizeY = "+sizeY);
    }
    
    /**
     * getBoardSizeX
     * return the width (X) of the board
     * 
     */
    public synchronized int getBoardSizeX(){
        return this.sizeX;
    }
    
    /**
     * getBoardSizeY
     * return the length (Y) of the board
     * 
     */
    public synchronized int getBoardSizeY(){
        return this.sizeY;
    }
    
    /**
     * getBombs
     * return the number of bombs in the board
     * 
     */
    public synchronized int getBombs(){
        return this.numBombs;
    }
    
    /**
     * getFlagged
     * return the number of squares currently flagged in the board
     * 
     */
    public synchronized int getFlagged(){
        return this.numFlagged;
    }
    
    /**
     * getUntouched
     * return the number of squares currently untouched in the board
     * 
     */
    public synchronized int getUntouched(){
        return this.numUntouched;
    }
    
    /**
     * flag
     * to mark an untouched square to indicate it potentially contains a bomb.
     * if it's not in UNTOUCHED state then do nothing.
     * @param int x - the x coordinate of the square to be marked.
     *                0 <= x < sizeX
     * @param int y - the y coordinate of the square to be marked.
     *                0 <= y < sizeY
     *
     */
    public synchronized void flag (int x, int y) {
        
        if ((x < 0) || (x >= sizeX))
            return;
        if ((y < 0) || (y >= sizeY))
            return;
        

        if (board[x][y] == UNTOUCHED) {
            board[x][y] = FLAGGED;
            numFlagged++;
            numUntouched--;
        }

        checkRep();
    }
    
    
    /**
     * deflag
     * to unmark an flagged square if it has been flagged before.
     * if it's not in UNTOUCHED state then do nothing.
     * @param int x - the x coordinate of the square to be unmarked.
     *                0 <= x < sizeX
     * @param int y - the y coordinate of the square to be unmarked.
     *                0 <= y < sizeY
     *
     */
    public synchronized void deflag (int x, int y) {
        
        if ((x < 0) || (x >= sizeX))
            return;
        if ((y < 0) || (y >= sizeY))
            return;
        
        synchronized (this) {
            if (board[x][y] == FLAGGED) {
                board[x][y] = UNTOUCHED;
                numFlagged--;
                numUntouched++;
            }
        }
        checkRep();
    }
    /**
     * dig
     * to reveal an untouched square and return the state of that square.
     * @param int x - the x coordinate of the square to be revealed.
     *                0 <= x < sizeX
     * @param int y - the y coordinate of the square to be revealed.
     *                0 <= y < sizeY
     * @returns the state of the square 
     *          - BOMB if the square contains a bomb. Also, this square will contain no more bomb and
     *            this square and its surrounding cells' bomb count will be updated accordingly.
     *          - 0..8 if the square is an empty cell and number of neighboring cells containing bomb.
     *
     */
    public synchronized int dig (int x, int y) {
        
        if ((x < 0) || (x >= sizeX))
            return NOP;
        if ((y < 0) || (y >= sizeY))
            return NOP;
     
        int state = board[x][y];
        
        switch (state) {
        case FLAGGED:
            // do nothing.
            break;

        case UNTOUCHED:
            numUntouched--;
            if (minesField[x][y]) {
                state = BOMB;
                
                // change the square to contain no bomb
                minesField[x][y] = false;
                numBombs--;
                
                // update the neighboring square's bomb count (if already dug)
                updateNeighbors(x,y);
                board[x][y] = checkNeighbors(x,y);
            } else {
                // no bomb in this square, check how many bombs in the surrounding cells
                board[x][y] = checkNeighbors(x,y);
                state = board[x][y];
            }
            numDug++;
            if (board[x][y] == 0) {
                // no bomb in neighbor squares, dig more...
                digMore(x,y);
            }

            break;
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
           // already dug, so nothing needs to be done here.
           break;
        default:
           throw new RuntimeException("unexpected board state!");
        }

        checkRep();
        return state;
    }
    
    /**
     * digMore
     * to uncover the contents of more squares around the square (x, y).  
     * For each of the square surrounding (x, y):
     *    - if the square is UNTOUCHED and it does *not* contain a bomb then
     *      the contents of the square is revealed.
     *    - further, if the revealed square is empty and has no neighboring bombs,
     *      digMore() is called recursively on it.
     * @param int x - the x coordinate of the square to be started.
     *                0 <= x < sizeX
     * @param int y - the y coordinate of the square to be started.
     *                0 <= y < sizeY
     */
    public synchronized void digMore(int x, int y) {
        int lowerBoundX, lowerBoundY, upperBoundX, upperBoundY;
         
        // Don't check outside the edges of the board
        lowerBoundX = (x <= 0 ? 0 : x - 1);
        lowerBoundY = (y <= 0 ? 0 : y - 1);
        upperBoundX = (x >= sizeX - 1 ? sizeX : x + 2);
        upperBoundY = (y >= sizeY - 1 ? sizeY : y + 2);
         
        // Loop over all surrounding cells
        for (int i = lowerBoundX; i < upperBoundX; i++) {
            for (int j = lowerBoundY; j < upperBoundY; j++) {
                if (!minesField[i][j] && (board[i][j] == UNTOUCHED)) {
                    dig(i, j);
                    // after the square is dug, we will continue to explore its
                    // neighboring square if its state is 0 (no neighbor contains bomb)
                    if (board[i][j] == 0) {
                        digMore(i, j);
                    }
                }
            }
        }
        checkRep();
    }
    /**
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
     */
    public synchronized String draw () {
        StringBuilder builder = new StringBuilder();
        
        // draw row by row
        for (int i = 0; i < sizeY; i++) {     
            for (int j = 0; j < sizeX; j++) {
                switch (board[j][i]) {
                case UNTOUCHED:
                    builder.append("- ");
                    continue;
                case FLAGGED:
                    builder.append("F ");
                    continue;
                case 0:
                    builder.append("  ");
                    continue;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    builder.append(board[j][i]).append(" ");
                    continue;
                default:
                    throw new RuntimeException("unexpected state!");
                }
            }
            builder.deleteCharAt(2*(i+1)*sizeX - 1); //get rid of the last space.
            builder.append("\n"); //append a new line at the end of each row.
        }
        builder.deleteCharAt(builder.length() - 1); //get rid of the last new line.
        // System.out.println("board draw:\n"+builder.toString()); // debug
        return builder.toString();
    }
    /**
     * checkNeighbors
     * to count how many the neighboring cells of (x,y) contains bomb and return the total count
     */
    private synchronized int checkNeighbors(int x, int y) {
        int lowerBoundX, lowerBoundY, upperBoundX, upperBoundY;
        int result = 0;
     
        // Don't check outside the edges of the board
        lowerBoundX = (x <= 0 ? 0 : x - 1);
        lowerBoundY = (y <= 0 ? 0 : y - 1);
        upperBoundX = (x >= sizeX - 1 ? sizeX : x + 2);
        upperBoundY = (y >= sizeY - 1 ? sizeY : y + 2);
     
         // Check all immediate neighbors
         for (int i = lowerBoundX; i < upperBoundX; i++) {
             for (int j = lowerBoundY; j < upperBoundY; j++) {
                 if (minesField[i][j]) {
                     result++;
                 }
             }
         }
         
         return result;
    }
    
    /**
     * updateNeighbors
     * once the bomb has been removed from the square (x, y), we need to update all its
     * surrounding cell's bomb count (the DUG ones).
     * 
     */
    private synchronized void updateNeighbors(int x, int y) {
        int lowerBoundX, lowerBoundY, upperBoundX, upperBoundY;
     
        // Don't check outside the edges of the board
        lowerBoundX = (x <= 0 ? 0 : x - 1);
        lowerBoundY = (y <= 0 ? 0 : y - 1);
        upperBoundX = (x >= sizeX - 1 ? sizeX : x + 2);
        upperBoundY = (y >= sizeY - 1 ? sizeY : y + 2);
     
         // Check all immediate neighbors
         for (int i = lowerBoundX; i < upperBoundX; i++) {
             for (int j = lowerBoundY; j < upperBoundY; j++) {
                 if (board[i][j] > 0) {
                     board[i][j]--;
                 }
             }
         }
    }
    
}
