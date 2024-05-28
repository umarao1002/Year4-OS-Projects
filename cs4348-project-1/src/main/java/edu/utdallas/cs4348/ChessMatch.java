package edu.utdallas.cs4348;

import java.util.List;

public class ChessMatch {

    private volatile ChessMove umaMove;
    private volatile ChessMove raoMove;

    private class Uma implements Runnable {
        private final List<ChessMove> moves;
        private final MoveRecord record;

        public Uma(List<ChessMove> moves, MoveRecord record) {
            this.moves = moves;
            this.record = record;
        }

        /**
         * Executes Uma's sequence of moves in the chess match, waiting for Rao's move
         * before responding.
         */
        @Override
        public void run() {
            Thread.currentThread().setName("UmaThread");
            printThreadInfo("Initialized with moves.");

            for (ChessMove move : moves) { // Iterate through moves
                synchronized (ChessMatch.this) { // Ensure thread safety
                    umaMove = move; // Assign move
                    printThreadInfo("Makes move: " + move.toString());
                    ChessMatch.this.notifyAll(); // Notify Rao
                    while (raoMove == null) { // Wait for Rao's move
                        try {
                            printThreadInfo("Waiting for Rao."); // waiting
                            ChessMatch.this.wait(); // Wait for Rao's move
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // Handle interruption
                            printThreadInfo("Uma interrupted, exiting cleanly.");
                            return; // Exit stopping the thread's execution gracefully
                        }
                    }

                    printThreadInfo("Recorded Rao's move: " + raoMove.toString());
                    record.addMove(raoMove); // Record Rao's move
                    raoMove = null; // Reset Rao's move for next iteration.
                }
            }
            printThreadInfo("UMA FINISHED making all moves.");
        }

    }

    private class Rao implements Runnable {

        private final List<ChessMove> moves;
        private final MoveRecord record;

        public Rao(List<ChessMove> moves, MoveRecord record) {
            this.moves = moves;
            this.record = record;
        }

        /**
         * Executes Rao's sequence of moves in the chess match, responding to Uma's
         * moves.
         */
        @Override
        public void run() {
            Thread.currentThread().setName("RaoThread");
            printThreadInfo("Initialized with moves.");

            for (ChessMove move : moves) { // Iterate through moves
                synchronized (ChessMatch.this) { // Ensure thread safety

                    // Wait for Uma's move before sending own move
                    while (umaMove == null) {
                        try {
                            printThreadInfo("Waiting for Uma's move.");
                            ChessMatch.this.wait(); // Waits for notification from Uma's thread
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // Handles thread interruption
                            printThreadInfo("Rao interrupted, exiting cleanly.");
                            return; // Exit stopping the thread's execution gracefully
                        }
                    }
                    printThreadInfo("Rao records Uma's move: " + umaMove);
                    record.addMove(umaMove); // Records Uma's move
                    umaMove = null; // Resets Uma's move for the next round

                    raoMove = move; // Assigns Rao's current move
                    printThreadInfo("Makes move: " + move.toString());

                    ChessMatch.this.notifyAll(); // Notifies Uma's thread that Rao has made a move
                }
            }
            printThreadInfo("RAO FINISHED making all moves.");
        }
    }

    public MoveRecord playMoves(List<ChessMove> umaMoves, List<ChessMove> raoMoves) {
        Thread.currentThread().setName("ChessMatch");
        printThreadInfo("Starting a New chess match.");
        printThreadInfo("---------------------------");
        MoveRecord moveRecord = new MoveRecord();

        // Instantiate Uma and Rao on new threads with shared MoveRecord
        Thread umaThread = new Thread(new Uma(umaMoves, moveRecord), "UmaThread");
        Thread raoThread = new Thread(new Rao(raoMoves, moveRecord), "RaoThread");

        // Start the threads
        printThreadInfo("Starting Uma's and Rao's threads.");
        umaThread.start();
        raoThread.start();

        try {
            umaThread.join(2000); // Set Uma's thread threshold to 2000ms
            raoThread.join(2000); // Set Ajay's thread threshold to 2000ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Re-interrupt current thread for status preservation
            throw new RuntimeException("[ChessMatch]: Main thread interrupted during player thread wait.", e);
        }

        // Validate and log the state of threads if not completed and throw runtime
        // exception to stop the test
        if (umaThread.isAlive() || raoThread.isAlive()) {
            // Log threads' states if either or both threads are alive
            printThreadInfo("UmaThread state: " + umaThread.getState());
            printThreadInfo("RaoThread state: " + raoThread.getState());
            if (umaThread.isAlive() && raoThread.isAlive()) {
                throw new RuntimeException("[ChessMatch]: Both players' threads didn't finish on time.");
            } else if (umaThread.isAlive()) {
                throw new RuntimeException("[ChessMatch]: Uma's thread didn't finish on time.");
            } else if (raoThread.isAlive()) {
                throw new RuntimeException("[ChessMatch]: Rao's thread didn't finish on time.");
            }
        }
        printThreadInfo("Both players finished their moves. Match complete.");
        return moveRecord; // returns the record of moves made by the players Uma and Rao
    }

    private void printThreadInfo(String message) {
        System.out.printf("[%s-%d] : %s%n", Thread.currentThread().getName(),
                Thread.currentThread().getId(), message);
    }

}
