/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper.server;

import java.io.*;
import java.net.*;
import java.util.*;

import minesweeper.Board;

/**
 * Multiplayer Minesweeper server.
 */
public class MinesweeperServer {

    // System thread safety argument

    //   all accesses to board happen within Board's methods which
    //   is guaranteed to be thread safe.

    /** Default server port. */
    private static final int DEFAULT_PORT = 4444;
    /** Maximum port number as defined by ServerSocket. */
    private static final int MAXIMUM_PORT = 65535;
    
    /** Default square board size. */
    public static final int DEFAULT_SIZE = 10;
    
    // various messages
    private static enum MessageType {HELLO, BOARD, BOOM, DISCONNECT, HELP};
    private static final String BOOM_MSG = "BOOM!";
    private static final String HELP_MSG = "Command syntax: [look], [dig x y], [flag x y], [deflag x y], [help], [bye] where x y are board size.";
    private static final String DISCONNECT_MSG = "Bye";
    private static final String HELLO_MSG = "Welcome to Minesweeper. Players: %1$d including you. Board: %2$d columns by %3$d rows. Type 'help' for help.";

    /** Socket for receiving incoming connections. */
    private static ServerSocket serverSocket;
    /** True if the server should *not* disconnect a client after a BOOM message. */
    private final boolean debug;
    
    // total number of client threads running
    private int numPlayers = 0;
    
    // board instance to play
    private static Board board;

    // rep invariant:
    //    none.
    //
    // abstraction function:
    //    MinesweeperServer creates a thread and starts the game with each client
    //    connection request it receives. 
    //
    // All reps are private so no rep exposure risk.
    //
    
    /**
     * Make a MinesweeperServer that listens for connections on port.
     * 
     * @throws IOException if an error occurs opening the server socket
     */
    MinesweeperServer() throws IOException {
        serverSocket = new ServerSocket(DEFAULT_PORT);
        this.debug = false;
    }
    /**
     * Make a MinesweeperServer that listens for connections on port.
     * 
     * @param port port number, requires 0 <= port <= 65535
     * @param debug debug mode flag
     * @throws IOException if an error occurs opening the server socket
     */
    public MinesweeperServer(int port, boolean debug) throws IOException {
        serverSocket = new ServerSocket(port);
        this.debug = debug;
    }

    /**
     * Run the server, listening for client connections and run a separate thread handling
     * each new client connection.
     * Server keeps listening on the server socket and never returns unless an exception is thrown.
     * 
     * @throws IOException if the main server socket is broken
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        /*
         * inner class to accept a parameter socket for its
         * run method to handle the connection.
         * Each connection will be run in a different thread,
         * so thread safety must be taken care of.
         */
        class ClientThread implements Runnable {
            private final Socket socket;
            
            ClientThread(Socket s) { 
                socket=s; 
            }
            
            public void run() {
                try {
                    numPlayers++;
                    System.out.println("client run starting, numPlayers="+numPlayers); //debug
                    handleConnection(socket);
                    numPlayers--;
                    System.out.println("client run ending, numPlayers="+numPlayers); //debug
                } catch (IOException ioe) {
                    ioe.printStackTrace(); // but don't terminate serve()
                }
            }
        }
        
        Socket socket=null;
        
        // Server keeps listening on the server socket and never returns unless an exception is thrown.
        while (!Thread.interrupted()) {
            try {
                // block until a client connects or the socket is closed
                socket = serverSocket.accept();
                    
                // new thread for a client connection
                Thread t = new Thread(new ClientThread(socket));
                t.start();
                
            } catch (SocketException e) {
                System.out.println("Server socket closed: " + e);
                break;
            } catch (IOException e) {
                System.out.println("Error on accepting socket: " + e);
                break;
            }
        }
        System.out.println("Server shutting down, thread:"+Thread.currentThread().getName()); //debug
    }
    
    /**
     * Handle a single client connection. Returns when client disconnects.
     * This is the main method to process the request from each client, and
     * it must be thread-safe!
     * 
     * @param socket socket where the client is connected
     * @throws IOException if the connection encounters an error or terminates unexpectedly
     * 
     * Thread safety argument:
     *   its call to handleRequest handles all accesses to board which happen within Board's methods,
     *   which are all guarded by Board's lock.
     */
    private void handleConnection(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        //System.out.println("Server client socket="+socket.toString()); //debug

        // send a HELLO message to the client right after the connection is set up
        System.out.println("Hello msg to client:"+buildMessage(MessageType.HELLO)); // debug
        out.println(buildMessage(MessageType.HELLO));

        try {
            // keep processing the requests from the client until no more input (Ctrl-C)
            //
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                
                if (output != null) {
                    // TODO: Consider improving spec of handleRequest to avoid use of null
                    System.out.println("result sent to client:\n"+output); // debug
                    
                    out.println(output);
                    // either a "bye" from client or a bomb was dug during a "dig",
                    // we disconnect.
                    if (output.equals(DISCONNECT_MSG))
                        break;
                    if (output.equals(BOOM_MSG) && !debug)
                        break;
                }
            }
        } finally {
            // some cleanups before we finish this client thread!
            out.close();
            in.close();
            socket.close();
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client, or DISCONNECT_MSG if client wants to disconnect
     */
    private String handleRequest(String input) {
        String regex = "(look)|(help)|(bye)|"
                     + "(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|(deflag -?\\d+ -?\\d+)";
        if ( ! input.matches(regex)) {
            // invalid input - send a help message to client!
            System.out.println("invalid input:"+input); //debug
            return buildMessage(MessageType.HELP);
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            // 'look' request - send a representation of the board's current state
            System.out.println("handle LOOK request"); //debug
            return buildMessage(MessageType.BOARD);
        } else if (tokens[0].equals("help")) {
            // 'help' request - send a help message
            System.out.println("handle HELP request"); //debug
            return buildMessage(MessageType.HELP);
        } else if (tokens[0].equals("bye")) {
            // 'bye' request - disconnect the client
            System.out.println("handle BYE request"); //debug
            return buildMessage(MessageType.DISCONNECT);
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                // if a bomb is dug, return a 'boom' msg and disconnect (if debug flag is missing
                //   otherwise keep the connection).
                // if no bomb was dug, return the new board state
                System.out.println("handle DIG request, x="+x+", y="+y); //debug
                if (board.dig(x, y) == Board.BOMB) {
                    return buildMessage(MessageType.BOOM);
                } else {
                    return buildMessage(MessageType.BOARD);
                }
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request
                System.out.println("handle FLAG request, x="+x+", y="+y); //debug
                board.flag(x, y);
                return buildMessage(MessageType.BOARD);
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request
                System.out.println("handle DEFLAG request, x="+x+", y="+y); //debug
                board.deflag(x, y);
                return buildMessage(MessageType.BOARD);
            }
        }
        // TODO: Should never get here, make sure to return in each of the cases above
        throw new UnsupportedOperationException();
    }
    
    /**
     * Build a server message according to the message type passed in.
     * 
     * @param msgType - message type based to build a message string
     * @return message string, or null if none
     */
    private String buildMessage (MessageType msgType) {
        switch (msgType) {
        // HELLO, BOARD, BOOM, DISCONNECT, HELP
        case BOARD:
            return board.draw();
            
        case HELP:
            return HELP_MSG;
            
        case HELLO:
            int x = board.getBoardSizeX(), y = board.getBoardSizeY();

            return String.format(HELLO_MSG, numPlayers, x, y);
    
        case BOOM:
            return BOOM_MSG;
            
        case DISCONNECT:
            return DISCONNECT_MSG;
            
        default:
            return null;
        }
    }

    /**
     * Start a MinesweeperServer using the given arguments.
     * 
     * <br> Usage:
     *      MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]
     * 
     * <br> The --debug argument means the server should run in debug mode. The server should disconnect a
     *      client after a BOOM message if and only if the --debug flag was NOT given.
     *      Using --no-debug is the same as using no flag at all.
     * <br> E.g. "MinesweeperServer --debug" starts the server in debug mode.
     * 
     * <br> PORT is an optional integer in the range 0 to 65535 inclusive, specifying the port the server
     *      should be listening on for incoming connections.
     * <br> E.g. "MinesweeperServer --port 1234" starts the server listening on port 1234.
     * 
     * <br> SIZE_X and SIZE_Y are optional positive integer arguments, specifying that a random board of size
     *      SIZE_X*SIZE_Y should be generated.
     * <br> E.g. "MinesweeperServer --size 42,58" starts the server initialized with a random board of size
     *      42*58.
     * 
     * <br> FILE is an optional argument specifying a file pathname where a board has been stored. If this
     *      argument is given, the stored board should be loaded as the starting board.
     * <br> E.g. "MinesweeperServer --file boardfile.txt" starts the server initialized with the board stored
     *      in boardfile.txt.
     * 
     * <br> The board file format, for use with the "--file" option, is specified by the following grammar:
     * <pre>
     *   FILE ::= BOARD LINE+
     *   BOARD ::= X SPACE Y NEWLINE
     *   LINE ::= (VAL SPACE)* VAL NEWLINE
     *   VAL ::= 0 | 1
     *   X ::= INT
     *   Y ::= INT
     *   SPACE ::= " "
     *   NEWLINE ::= "\n" | "\r" "\n"?
     *   INT ::= [0-9]+
     * </pre>
     * 
     * <br> If neither --file nor --size is given, generate a random board of size 10x10.
     * 
     * <br> Note that --file and --size may not be specified simultaneously.
     * 
     * @param args arguments as described
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        boolean debug = false;
        int port = DEFAULT_PORT;
        int sizeX = DEFAULT_SIZE;
        int sizeY = DEFAULT_SIZE;
        Optional<File> file = Optional.empty();

        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--debug")) {
                        //System.out.println("debug flag is set"); //debug
                        debug = true;
                    } else if (flag.equals("--no-debug")) {
                        //System.out.println("debug flag is not set"); //debug
                        debug = false;
                    } else if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        //System.out.println("port = "+port); //debug
                        if (port < 0 || port > MAXIMUM_PORT) {
                            throw new IllegalArgumentException("port " + port + " out of range");
                        }
                    } else if (flag.equals("--size")) {
                        String[] sizes = arguments.remove().split(",");
                        sizeX = Integer.parseInt(sizes[0]);
                        sizeY = Integer.parseInt(sizes[1]);
                        //System.out.println("sizeX = "+sizeX+", sizeY = "+sizeY); //debug
                        file = Optional.empty();
                    } else if (flag.equals("--file")) {
                        sizeX = -1;
                        sizeY = -1;
                        file = Optional.of(new File(arguments.remove()));
                        if ( ! file.get().isFile()) {
                            throw new IllegalArgumentException("file not found: \"" + file.get() + "\"");
                        }
                        //System.out.println("file name:"+file.get().getAbsolutePath()); //debug
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]");
            return;
        }

        try {
            runMinesweeperServer(debug, file, sizeX, sizeY, port);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file.
     * 
     * @param debug The server will disconnect a client after a BOOM message if and only if debug is false.
     * @param file If file.isPresent(), start with a board loaded from the specified file,
     *             according to the input file format defined in the documentation for main(..).
     * @param sizeX If (!file.isPresent()), start with a random board with width sizeX
     *              (and require sizeX > 0).
     * @param sizeY If (!file.isPresent()), start with a random board with height sizeY
     *              (and require sizeY > 0).
     * @param port The network port on which the server should listen, requires 0 <= port <= 65535.
     * @throws IOException if a network error occurs
     */
    public static void runMinesweeperServer(boolean debug, Optional<File> file, int sizeX, int sizeY, int port) throws IOException {
        
        // if !file.isPresent() then we check if sizeX > 0 and sizeY > 0 
        //   if so, we should generate a random board with these sizes passed in.
        // 
        if (!file.isPresent()) {
            if ((sizeX > 0) && (sizeY > 0)) {
                board = new Board(sizeX, sizeY);
            } else {
                // sizes are not legal, use default sizes
                board = new Board(DEFAULT_SIZE, DEFAULT_SIZE);
            }
        } else {
            // use the file to configure the board
            /*
            final String boardPath = file.get().getAbsolutePath(); // debug
            System.out.println("runMinesweeperServer is attempting to read from file in: "+boardPath); // debug
            */
            board = new Board(file.get());
            // board.draw(); // debug
        }
        
        MinesweeperServer server = new MinesweeperServer(port, debug);
        server.serve();
    }
    
    /*
     * stopMinesweeperServer
     * stop this server thread and close the server socket it opened.
     * 
     * @param thread - the server thread to be stopped.
     */
    public static void stopMinesweeperServer(Thread thread) throws IOException {
        if (thread != null) {
            System.out.println("interrupting server thread:"+thread.getName()); //debug
            thread.interrupt();
            serverSocket.close();
        }      
    }
}
