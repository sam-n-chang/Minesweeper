/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper.server;

//public class MinesweeperServerTest {

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import org.junit.Test;

import minesweeper.server.MinesweeperServer;

/**
 * Tests basic LOOK and DIG commands and X,Y directions.
 */
public class MinesweeperServerTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final int PORT = 4000 + new Random().nextInt(1 << 15);

    private static final int MAX_CONNECTION_ATTEMPTS = 10;

    //private static final String BOARDS_PKG = "minesweeper.server/";
    private static final String BOARDS_PKG = "test/minesweeper/server/";

    /**
     * Start a MinesweeperServerFile in debug mode with a board file from BOARDS_PKG.
     * 
     * @param boardFile board to load
     * @return thread running the server
     * @throws IOException if the board file cannot be found
     */
    private static Thread startMinesweeperServerFile(String boardFile) throws IOException, ClassNotFoundException {
        /* for debug 
        File myFile = new File(BOARDS_PKG + boardFile);
        System.out.println("Attempting to read from file in: "+myFile.getCanonicalPath());
        // list all files in the directory
        File file = new File(".");
        for(String fileNames : file.list()) System.out.println(fileNames);
        */
        
        //final URL boardURL = ClassLoader.getSystemClassLoader().getResource(BOARDS_PKG + boardFile);
        File boradFile = new File(BOARDS_PKG + boardFile);
        final String boardPath = boradFile.getAbsolutePath();
        System.out.println("startMinesweeperServerFile is reading from file: "+boardPath+", port="+PORT);
        final String[] args = new String[] {
                "--debug",
                "--port", Integer.toString(PORT),
                "--file", boardPath
        };
        
        /*
        if (boardURL == null) {
            throw new IOException("Failed to locate resource " + boardFile);
        }
        final String boardPath;
        try {
            boardPath = new File(boardURL.toURI()).getAbsolutePath();
        } catch (URISyntaxException urise) {
            throw new IOException("Invalid URL " + boardURL, urise);
        }
        final String[] args = new String[] {
                "--debug",
                "--port", Integer.toString(PORT),
                "--file", boardPath
        };
        */
        // System.out.println("port="+PORT+", path="+boardPath);
        Thread serverThread = new Thread(() -> MinesweeperServer.main(args));
        serverThread.start();
        return serverThread;
    }

    /**
     * Start a MinesweeperServerCommand in debug mode with various option flag.
     * 
     * @param args command line flags to be used to start the minesweeper server.
     * @return thread running the server
     * @throws IOException if the board file cannot be found
     */
    private static Thread startMinesweeperServerCommand(String[] args) throws IOException, ClassNotFoundException {
        
        System.out.println("startMinesweeperServerCommand: "+args.toString());
        
        Thread serverThread = new Thread(() -> MinesweeperServer.main(args));
        serverThread.start();
        return serverThread;
    }
    /**
     * Connect to a MinesweeperServer and return the connected socket.
     *      
     * @param server abort connection attempts if the server thread dies
     * @return socket connected to the server
     * @throws IOException if the connection fails
     */
    private static Socket connectToMinesweeperServer(Thread server) throws IOException {
        int attempts = 0;
        while (true) {
            try {
                //client tries to connect to server and return the socket if success.
                Socket socket = new Socket(LOCALHOST, PORT);
                socket.setSoTimeout(3000);
                System.out.println("connectToMinesweeperServer: socket="+socket.toString()); //debug
                return socket;
            } catch (ConnectException ce) {
                if ( ! server.isAlive()) {
                    throw new IOException("Server thread not running");
                }
                if (++attempts > MAX_CONNECTION_ATTEMPTS) {
                    throw new IOException("Exceeded max connection attempts", ce);
                }
                try { Thread.sleep(attempts * 10); } catch (InterruptedException ie) { }
            }
        }
    }
    /*
     * Test connection of multiple clients
     */
    @Test(timeout = 10000)
    // no file/command line option (except the default port no) used to start the server
    // 
    public void clientConnectionTest() throws IOException, ClassNotFoundException {
        final String[] args = new String[] {
                "--port", Integer.toString(PORT)
        };
        
        // thread that the server is running.
        Thread thread = startMinesweeperServerCommand(args);

        // first client's socket connected to server
        Socket socket1 = connectToMinesweeperServer(thread);

        BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
        PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
        
        /*
        System.out.println("client socket="+socket1.toString()); //debug
        System.out.println("Received HELLO msg="+in1.readLine()); //debug
        */
        
        String msg = in1.readLine();
        assertTrue("expected HELLO message: ", msg.startsWith("Welcome"));
        
        // second client's socket connected to server
        Socket socket2 = connectToMinesweeperServer(thread);

        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
        
        msg = in2.readLine();
        String[] word = msg.split(" ");
        assertEquals("Players:", word[3]);
        
        //check the number of clients connected
        assertEquals(2, Integer.parseInt(word[4]));
               
        out1.println("bye");
        out2.println("bye");
        
        // close server socket and stop its thread
        socket1.close();
        socket2.close();
        MinesweeperServer.stopMinesweeperServer(thread);
    }
    /*
     * Tests related to board are conducted in two options:
     * 1. Start minesweeper server by loading a pre-configured board file, 
     *    with "debug" and "port" flags.
     * 2. Start minesweeper server by using a command line with various flags. 
     */

    /***************************************************************** 
     * Start minesweeper server by loading a pre-configured board file
     * ***************************************************************
     */
    @Test(timeout = 10000)
    // The following tests are done in this test:
    // 1. test with an input file with x = y = 7
    // 2. server thread started
    // 3. client socket created after connecting to server
    // 4. input / output streams from / to the socket
    // 5. "Welcome" message from server after the connection is set up
    // 6. "help" message
    // 7. "look" command
    // 8. "dig" command - with bomb and w/o bomb in square
    // 9. "bye" command
    // 
    public void publishedTest() throws IOException, ClassNotFoundException {

        // thread that the server is running.
        Thread thread = startMinesweeperServerFile("board_file_1.txt");

        // client's socket connected to server
        Socket socket = connectToMinesweeperServer(thread);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        /*
        System.out.println("client socket="+socket.toString()); //debug
        System.out.println("Received HELLO msg="+in.readLine()); //debug
        */
        
        assertTrue("expected HELLO message: ", in.readLine().startsWith("Welcome"));
        
        out.println("help");
        assertTrue("expected HELP message: ", in.readLine().startsWith("Command"));

        out.println("look");

        for (int i=0; i < 7; i++) {
            String s = in.readLine();
            /* System.out.println(i+" readLine="+in.readLine()); */
            /*
            System.out.print("i("+i+"):"+s);
            System.out.println();
            */
            assertEquals("- - - - - - -", s);
        }
        /*
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        */

        out.println("dig 3 1");
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - 1 - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());

        out.println("dig 4 1");
        assertEquals("BOOM!", in.readLine());

        out.println("look"); // debug mode is on
        assertEquals("             ", in.readLine());
        assertEquals("             ", in.readLine());
        assertEquals("             ", in.readLine());
        assertEquals("             ", in.readLine());
        assertEquals("             ", in.readLine());
        assertEquals("1 1          ", in.readLine());
        assertEquals("- 1          ", in.readLine());

        out.println("bye");
        
        // close server socket and stop its thread
        socket.close();
        MinesweeperServer.stopMinesweeperServer(thread);
    }
    
    @Test(timeout = 10000)
    // test an input file with different x (=8) and y (=6)
    // test dig operations
    //
    public void MinesweeperServerTestReadFile1() throws IOException, ClassNotFoundException {

        Thread thread = startMinesweeperServerFile("board_file_2.txt");

        Socket socket = connectToMinesweeperServer(thread);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        assertTrue("expected HELLO message", in.readLine().startsWith("Welcome"));

        out.println("dig 5 1");
        
        assertEquals("- - - - - - - -", in.readLine());
        assertEquals("- - - - - 1 - -", in.readLine());
        assertEquals("- - - - - - - -", in.readLine());
        assertEquals("- - - - - - - -", in.readLine());
        assertEquals("- - - - - - - -", in.readLine());
        assertEquals("- - - - - - - -", in.readLine());

        out.println("dig 2 1");
        //out.println("look"); // debug mode is on
        assertEquals("      1 - - - -", in.readLine());
        assertEquals("      1 - 1 - -", in.readLine());
        assertEquals("  1 2 3 - - - -", in.readLine());
        assertEquals("  2 - - - - - -", in.readLine());
        assertEquals("  2 - - - - - -", in.readLine());
        assertEquals("  1 - - - - - -", in.readLine());

        out.println("dig 2 1");
        //out.println("look"); // debug mode is on
        assertEquals("      1 - - - -", in.readLine());
        assertEquals("      1 - 1 - -", in.readLine());
        assertEquals("  1 2 3 - - - -", in.readLine());
        assertEquals("  2 - - - - - -", in.readLine());
        assertEquals("  2 - - - - - -", in.readLine());
        assertEquals("  1 - - - - - -", in.readLine());

        out.println("bye");
        socket.close();
        MinesweeperServer.stopMinesweeperServer(thread);
    }
    

    @Test(timeout = 10000)
    // test an input file with different x (=4) and y (=6)
    // test flag/deflag operations
    //
    public void MinesweeperServerTestReadFile2() throws IOException, ClassNotFoundException {

        Thread thread = startMinesweeperServerFile("board_file_3.txt");

        Socket socket = connectToMinesweeperServer(thread);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        //retrieve the welcome message
        in.readLine();

        out.println("flag 2 2");
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - F -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        
        out.println("dig 2 2");
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - F -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());

        out.println("deflag 2 2");
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());

        out.println("flag 3 5");
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - F", in.readLine());

        out.println("flag 3 5");
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - F", in.readLine());

        out.println("deflag 3 5");
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());

        out.println("deflag 3 5");
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        assertEquals("- - - -", in.readLine());
        
        out.println("bye");
        socket.close();
        MinesweeperServer.stopMinesweeperServer(thread);
    }
    /***************************************************************** 
     * Start minesweeper server by using command line
     * ***************************************************************
     */
    @Test(timeout = 10000)
    // The following tests are done in this test:
    // 1. test with flag "debug" and "size" and "port"
    // 2. server thread started
    // 3. client socket created after connecting to server
    // 4. input / output streams from / to the socket
    // 5. "Welcome" message from server after the connection is set up
    // 6. "help" message
    // 7. "look" command
    // 8. "flag" command 
    // 9. "deflag" command
    // 10."bye" command
    // 
    public void commandLineTest() throws IOException, ClassNotFoundException {
        final String[] args = new String[] {
                "--debug",
                "--size", "5,5",
                "--port", Integer.toString(PORT)
        };
        
        // thread that the server is running.
        Thread thread = startMinesweeperServerCommand(args);

        // client's socket connected to server
        Socket socket = connectToMinesweeperServer(thread);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        /*
        System.out.println("client socket="+socket.toString()); //debug
        System.out.println("Received HELLO msg="+in.readLine()); //debug
        */
        
        assertTrue("expected HELLO message: ", in.readLine().startsWith("Welcome"));
        
        out.println("help");
        assertTrue("expected HELP message: ", in.readLine().startsWith("Command"));

        out.println("look");

        for (int i=0; i < 5; i++) {
            String s = in.readLine();
            /* System.out.println(i+" readLine="+in.readLine()); */
            /*
            System.out.print("i("+i+"):"+s);
            System.out.println();
            */
            assertEquals("- - - - -", s);
        }
        
        out.println("flag 2 2");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - F - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        
        out.println("dig 2 2");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - F - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());

        out.println("deflag 2 2");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());

        out.println("flag 4 4");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - F", in.readLine());

        out.println("flag 4 4");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - F", in.readLine());

        out.println("deflag 4 4");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());

        out.println("deflag 4 4");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        
        out.println("bye");
        
        // close server socket and stop its thread
        socket.close();
        MinesweeperServer.stopMinesweeperServer(thread);
    }
    @Test(timeout = 10000)
    // The following tests are done in this test:
    // 1. test with flag "port" only - 
    //    this will create a board with default size of 10
    // 2. server thread started
    // 3. client socket created after connecting to server
    // 4. input / output streams from / to the socket
    // 5. "Welcome" message from server after the connection is set up
    // 6. "help" message
    // 7. "look" command
    // 8."bye" command
    // 
    public void commandLineTest2() throws IOException, ClassNotFoundException {
        final String[] args = new String[] {
                "--port", Integer.toString(PORT)
        };
        
        // thread that the server is running.
        Thread thread = startMinesweeperServerCommand(args);

        // client's socket connected to server
        Socket socket = connectToMinesweeperServer(thread);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        /*
        System.out.println("client socket="+socket.toString()); //debug
        System.out.println("Received HELLO msg="+in.readLine()); //debug
        */
        
        assertTrue("expected HELLO message: ", in.readLine().startsWith("Welcome"));
        
        out.println("help");
        assertTrue("expected HELP message: ", in.readLine().startsWith("Command"));

        out.println("look");

        for (int i=0; i < 10; i++) {
            String s = in.readLine();
            /* System.out.println(i+" readLine="+in.readLine()); */
            /*
            System.out.print("i("+i+"):"+s);
            System.out.println();
            */
            assertEquals("- - - - - - - - - -", s);
        }
        
        out.println("flag 2 2");
        assertEquals("- - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - -", in.readLine());
        assertEquals("- - F - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - -", in.readLine());
               
        out.println("bye");
        
        // close server socket and stop its thread
        socket.close();
        MinesweeperServer.stopMinesweeperServer(thread);
    }
}