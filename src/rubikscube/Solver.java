package rubikscube;

import java.lang.invoke.MethodHandles;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class Solver {

    // Allowed face moves. Make sure these match what RubiksCube.applyMoves(...) understands.
    private static final char[] MOVES = { 'F', 'B', 'U', 'D', 'L', 'R' };

    private static class Node {
        final RubiksCube state;
        final String path;

        Node(RubiksCube state, String path) {
            this.state = state;
            this.path = path;
        }
    }

    /**
     * Inverse of a quarter-turn move: XXX = 3 * 90° clockwise = 90° counterclockwise.
     */
    private static String invertMove(char move) {
        return "" + move + move + move;
    }

    /**
     * Expands one BFS "layer" from either the start side or the goal side.
     *
     * @param frontier   queue of nodes to expand from this side
     * @param ownPaths   map of signatures -> path from this side's root
     * @param otherPaths map of signatures -> path from the other side's root
     * @param fromGoal   true if expanding from goal side, false if from start
     * @return full solution string if a meeting point is found, otherwise null
     */
    private static String expandFrontier(Queue<Node> frontier,
                                         Map<String, String> ownPaths,
                                         Map<String, String> otherPaths,
                                         boolean fromGoal) {
        int layerSize = frontier.size();
        while (layerSize-- > 0) {
            Node current = frontier.poll();
            if (current == null) {
                // Shouldn't happen if layerSize was taken from frontier.size(),
                // but this is a safe-guard.
                continue;
            }

            for (char move : MOVES) {
                // Make a copy of the cube and apply a single move
                RubiksCube next = new RubiksCube(current.state);
                // If you DON'T have a copy constructor, replace the above with an alternative
                // e.g. `RubiksCube next = new RubiksCube(current.state.toString());`
                // depending on how your RubiksCube constructors are defined.

                next.applyMoves(String.valueOf(move));
                String signature = next.toString();

                if (ownPaths.containsKey(signature)) {
                    // Already visited from this side
                    continue;
                }

                String nextPath = fromGoal
                        ? invertMove(move) + current.path  // building path from intersection to goal
                        : current.path + move;             // building path from start to intersection

                // If the other side has already seen this state, we have a full solution!
                if (otherPaths.containsKey(signature)) {
                    return fromGoal
                            ? otherPaths.get(signature) + nextPath
                            : nextPath + otherPaths.get(signature);
                }

                ownPaths.put(signature, nextPath);
                frontier.offer(new Node(next, nextPath));
            }
        }

        return null;
    }

    /**
     * Solve a scrambled cube using bidirectional BFS.
     *
     * @param scrambledCube the starting cube state
     * @return a string of moves that solves the cube, or empty string if already solved, or null if no solution found
     */
    public static String solve(RubiksCube scrambledCube) {
        RubiksCube start = new RubiksCube(scrambledCube);
        RubiksCube goal = new RubiksCube(); // assumes default constructor = solved cube

        Queue<Node> forward = new ArrayDeque<>();
        Queue<Node> backward = new ArrayDeque<>();
        Map<String, String> forwardPaths = new HashMap<>();
        Map<String, String> backwardPaths = new HashMap<>();

        String startSignature = start.toString();
        String goalSignature = goal.toString();

        forward.offer(new Node(start, ""));
        backward.offer(new Node(goal, ""));
        forwardPaths.put(startSignature, "");
        backwardPaths.put(goalSignature, "");

        // Already solved
        if (startSignature.equals(goalSignature)) {
            return "";
        }

        // Bidirectional BFS
        while (!forward.isEmpty() && !backward.isEmpty()) {
            // Expand from start side
            String solution = expandFrontier(forward, forwardPaths, backwardPaths, false);
            if (solution != null) {
                return solution;
            }

            // Expand from goal side
            solution = expandFrontier(backward, backwardPaths, forwardPaths, true);
            if (solution != null) {
                return solution;
            }
        }

        // No solution found (shouldn't happen for valid cubes but safer than returning "")
        return null;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("File names are not specified");
            System.out.println("usage: java " 
                    + MethodHandles.lookup().lookupClass().getName()
                    + " input_file output_file");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];

        try {
            RubiksCube cube = new RubiksCube(inputPath); // assumes this reads from a file

            String solution = solve(cube);

            if (solution == null) {
                System.out.println("No solution found for cube in " + inputPath);
                return;
            }

            try (PrintWriter writer = new PrintWriter(outputPath)) {
                writer.println(solution);
            }

            System.out.println("Solved cube. Moves written to " + outputPath);
        } catch (IOException | IncorrectFormatException e) {
            System.out.println("Failed to solve cube from " + inputPath + ": " + e.getMessage());
        }
    }
}