package rubikscube;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Solver {

    public static final long DEFAULT_SOLVE_TIME_LIMIT_NANOS = 300_000_000_000L; // 300 seconds (5 minutes)

    // PDBs
    private static PatternDatabase cornerDB;
    private static PatternDatabase edgeDB1;
    private static PatternDatabase edgeDB2;

    // Initialize PDBs
    private static void initPDBs() {
        if (cornerDB == null) {
            cornerDB = new PatternDatabase(PatternDatabase.TYPE_CORNER, "corner.pdb");
            if (!cornerDB.load())
                cornerDB.generate();

            edgeDB1 = new PatternDatabase(PatternDatabase.TYPE_EDGE1, "edge1.pdb");
            if (!edgeDB1.load())
                edgeDB1.generate();

            edgeDB2 = new PatternDatabase(PatternDatabase.TYPE_EDGE2, "edge2.pdb");
            if (!edgeDB2.load())
                edgeDB2.generate();
        }
    }

    public static String solve(RubiksCube scrambledCube) {
        return solve(scrambledCube, null, DEFAULT_SOLVE_TIME_LIMIT_NANOS);
    }

    // Faces: 0=U, 1=L, 2=F, 3=R, 4=B, 5=D
    private static final String[] MOVE_NAMES = {
            "U", "L", "F", "R", "B", "D"
    };
    private static final String[] SUFFIXES = { "", "2", "'" };

    // Beam Search (Legacy constants, used for weight)
    private static final float WEIGHT = 3.0f;

    private static long nodesCount = 0;

    private static String solutionPath = null;

    public static String solve(RubiksCube startCube, String inputName, long timeoutNanos) {
        initPDBs();
        SmartCube start = new SmartCube(startCube);
        // System.out.println("Start Cube State:\n" + start);
        solutionPath = null; // Reset solution path for each solve call
        nodesCount = 0;

        // Initial heuristic
        int h = getHeuristic(start);
        int threshold = (int) (h * WEIGHT);

        long deadline = System.nanoTime() + timeoutNanos;

        // IDA* loop
        try {
            while (true) {
                // Double check time before starting a new depth
                if (System.nanoTime() >= deadline)
                    throw new TimeoutException();

                // System.out.println("Searching with threshold: " + threshold);
                int nextThreshold = search(start, 0, threshold, -1, deadline);
                if (nextThreshold == -1) { // -1 signals solution found
                    return solutionPath;
                }
                if (nextThreshold == Integer.MAX_VALUE) {
                    return "Solution not found"; // No solution within reasonable bounds
                }
                threshold = nextThreshold;
            }
        } catch (TimeoutException e) {
            return "TIMEOUT";
        }
    }

    // Returns next threshold (min f > threshold) or -1 if found (and sets
    // solutionPath)
    private static int search(SmartCube cube, int g, int threshold, int lastFace, long deadline) {
        // Check timeout every 1000 nodes
        nodesCount++;
        if ((nodesCount & 1023) == 0) {
            if (System.nanoTime() >= deadline) {
                throw new TimeoutException();
            }
        }

        int h = getHeuristic(cube);
        int f = g + (int) (h * WEIGHT); // Weighted f

        if (f > threshold) {
            return f;
        }

        if (h == 0) {
            // Solved!
            return -1; // -1 signals FOUND
        }

        int min = Integer.MAX_VALUE;

        for (int face = 0; face < 6; face++) {
            // Pruning
            if (lastFace != -1) {
                if (face == lastFace)
                    continue;
                // Commutative pruning: Enforce an order for opposite faces to avoid duplicates.
                // We allow U then D, but not D then U.
                // U (0) <-> D (5)
                // L (1) <-> R (3)
                // F (2) <-> B (4)
                if (lastFace == 5 && face == 0)
                    continue; // Prune D then U
                if (lastFace == 3 && face == 1)
                    continue; // Prune R then L
                if (lastFace == 4 && face == 2)
                    continue; // Prune B then F
            }

            for (int power = 1; power <= 3; power++) {
                // Apply move
                cube.move(face, power);

                int res = search(cube, g + 1, threshold, face, deadline);

                if (res == -1) {
                    // Found! Append this move to solution.
                    // User Request: No prime moves, U2 -> U U
                    String name = MOVE_NAMES[face];
                    String moveString = "";
                    if (power == 1) {
                        moveString = name;
                    } else if (power == 2) {
                        moveString = name + name;
                    } else if (power == 3) {
                        moveString = name + name + name;
                    }

                    if (solutionPath == null)
                        solutionPath = moveString;
                    else
                        solutionPath = moveString + solutionPath;
                    return -1;
                }

                if (res < min) {
                    min = res;
                }

                // Backtrack (inverse move)
                // Inverse of power 1 is 3, 2 is 2, 3 is 1.
                int invPower = 4 - power;
                cube.move(face, invPower);
            }
        }

        return min;
    }

    private static int getHeuristic(SmartCube cube) {
        int h1 = cornerDB.getEstimate(cube);
        int h2 = edgeDB1.getEstimate(cube);
        int h3 = edgeDB2.getEstimate(cube);
        return Math.max(h1, Math.max(h2, h3));
    }

    public static void main(String[] args) throws IncorrectFormatException, IOException {
        // Project Requirement: Command line arguments
        if (args.length < 2) {
            System.out.println("Usage: java rubikscube.Solver <input_file> <output_file>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            // 1. Read Input
            RubiksCube cube = new RubiksCube(inputFile);

            // 2. Solve
            long startTime = System.currentTimeMillis();
            String sol = solve(cube, inputFile, DEFAULT_SOLVE_TIME_LIMIT_NANOS);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 3. Write Output
            try (PrintWriter w = new PrintWriter(outputFile)) {
                w.println(sol);
            }

            System.out.println("Solution written to " + outputFile);
            System.out.println("Execution Time: " + (duration / 1000.0) + " seconds");
            System.out.println("Solution: " + sol);

            if (sol != null && !sol.equals("TIMEOUT") && !sol.equals("Solution not found")) {
                cube.applyMoves(sol);
                if (cube.isSolved()) {
                    System.out.println("Verification: SOLVED");
                } else {
                    System.out.println("Verification: NOT SOLVED");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 40; i++) {
            // Make scramble cube
            RubiksCube scramble = new RubiksCube(args[2 * i]);
            String outputPath = args[2 * i + 1];
            // Set timer
            long start = System.nanoTime();
            String solution = solve(scramble, null, 60_000_000_000L);

            // CHECK FOR TIMEOUT
            if (solution.equals("TIMEOUT")) {
                System.out.println("❌ Scramble " + (i + 1) + " TIMED OUT (>60s). Skipping to next...");
                System.out.println(" ");
                try (PrintWriter w = new PrintWriter(outputPath)) {
                    w.println("");
                } // Write empty file
                continue; // SKIP to the next iteration of the for-loop
            }

            System.out.println("Solution: " + solution);
            if (solution != null) {
                long end = System.nanoTime();
                double seconds = (end - start) / 1_000_000_000.0;

                // End timer
                scramble.applyMoves(solution);
                if (scramble.isSolved() && seconds < 10) {
                    System.out.println("Scramble " + (i + 1) + " SOLVED IN REQ TIME");
                    System.out.println("Solve took: " + seconds + " seconds");
                    System.out.println("");
                } else if (scramble.isSolved() && seconds > 10) {
                    System.out.println("Scramble " + (i + 1) + " SOLVED IN TOO SLOW TIME");
                    System.out.println("Solve took: " + seconds + " seconds");
                    System.out.println(" ");
                } else {
                    System.out.println("Scramble " + (i + 1) + " NOT Solved");
                    System.out.println(" ");
                }
            }
        }
    }

    // ADD THIS AT THE BOTTOM OF THE CLASS
    private static class TimeoutException extends RuntimeException {
    }

}
