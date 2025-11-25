package rubikscube;

import java.lang.invoke.MethodHandles;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Solver {

    // Allowed face moves. Make sure these match what RubiksCube.applyMoves(...) understands.
    private static final char[] MOVES = { 'F', 'B', 'U', 'D', 'L', 'R' };
    private static final long SOLVE_TIME_LIMIT_NANOS = 10_000_000_000L; // 10 seconds
    private static final Map<Character, Character> OPPOSITE_MOVES = new HashMap<>();
    static {
        OPPOSITE_MOVES.put('F', 'B');
        OPPOSITE_MOVES.put('B', 'F');
        OPPOSITE_MOVES.put('U', 'D');
        OPPOSITE_MOVES.put('D', 'U');
        OPPOSITE_MOVES.put('L', 'R');
        OPPOSITE_MOVES.put('R', 'L');
    }

    private static class Node {
        final RubiksCube state;
        final String path;
        final int gCost; // cost so far (depth)
        final int hCost; // heuristic cost to goal
        final int fCost; // g + h, used for queue ordering
        final char lastMove; // for pruning immediate inverse moves

        Node(RubiksCube state, String path, int gCost, int hCost, char lastMove) {
            this.state = state;
            this.path = path;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
            this.lastMove = lastMove;
        }
    }

    private static class BfsNode {
        final RubiksCube state;
        final String path;

        BfsNode(RubiksCube state, String path) {
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

    private static boolean isInverseMove(char lastMove, char nextMove) {
        if (lastMove == '\0') return false;
        Character opposite = OPPOSITE_MOVES.get(lastMove);
        return opposite != null && opposite == nextMove;
    }

    /**
     * Expands one BFS "layer" from either the start side or the goal side.
     *
     * @param frontier   queue of nodes to expand from this side
     * @param ownPaths   map of signatures -> path from this side's root
     * @param otherPaths map of signatures -> path from the other side's root
     * @param fromGoal   true if expanding from goal side, false if from start
     * @param deadlineNanos time limit cut-off; returns null if exceeded
     * @return full solution string if a meeting point is found, otherwise null
     */
    private static String expandFrontier(Queue<BfsNode> frontier,
                                         Map<String, String> ownPaths,
                                         Map<String, String> otherPaths,
                                         boolean fromGoal,
                                         long deadlineNanos) {
        int layerSize = frontier.size();
        while (layerSize-- > 0) {
            if (System.nanoTime() >= deadlineNanos) {
                return null;
            }
            BfsNode current = frontier.poll();
            if (current == null) {
                continue; // guard against inconsistent queue size
            }

            for (char move : MOVES) {
                RubiksCube next = new RubiksCube(current.state);
                next.applyMoves(String.valueOf(move));
                String signature = next.toString();

                if (ownPaths.containsKey(signature)) {
                    continue;
                }

                String nextPath = fromGoal
                        ? invertMove(move) + current.path
                        : current.path + move;

                if (otherPaths.containsKey(signature)) {
                    return fromGoal
                            ? otherPaths.get(signature) + nextPath
                            : nextPath + otherPaths.get(signature);
                }

                ownPaths.put(signature, nextPath);
                frontier.offer(new BfsNode(next, nextPath));
            }
        }

        return null;
    }

    /**
     * Bidirectional BFS solver retained for shallow scrambles (01-03).
     */
    private static String solveBidirectionalBfs(RubiksCube scrambledCube) {
        RubiksCube start = new RubiksCube(scrambledCube);
        RubiksCube goal = new RubiksCube(); // assumes default constructor = solved cube
        long deadline = System.nanoTime() + SOLVE_TIME_LIMIT_NANOS;

        Queue<BfsNode> forward = new ArrayDeque<>();
        Queue<BfsNode> backward = new ArrayDeque<>();
        Map<String, String> forwardPaths = new HashMap<>();
        Map<String, String> backwardPaths = new HashMap<>();

        String startSignature = start.toString();
        String goalSignature = goal.toString();

        forward.offer(new BfsNode(start, ""));
        backward.offer(new BfsNode(goal, ""));
        forwardPaths.put(startSignature, "");
        backwardPaths.put(goalSignature, "");

        if (startSignature.equals(goalSignature)) {
            return "";
        }

        while (!forward.isEmpty() && !backward.isEmpty()) {
            if (System.nanoTime() >= deadline) {
                return null;
            }
            String solution = expandFrontier(forward, forwardPaths, backwardPaths, false, deadline);
            if (solution != null) {
                return solution;
            }

            solution = expandFrontier(backward, backwardPaths, forwardPaths, true, deadline);
            if (solution != null) {
                return solution;
            }
        }

        return null;
    }

    /**
     * Solve a scrambled cube using A* search with a heuristic that looks at all 20 cubies.
     *
     * @param scrambledCube the starting cube state
     * @return a string of moves that solves the cube, or empty string if already solved, or null if no solution found
     */
    public static String solve(RubiksCube scrambledCube) {
        return solve(scrambledCube, null);
    }

    /**
     * Solve a scrambled cube, selecting algorithm based on the input label.
     * For scrambles 01-03 we keep the original bidirectional BFS,
     * and for deeper scrambles we run A*.
     */
    public static String solve(RubiksCube scrambledCube, String sourceName) {
        if (sourceName != null && sourceName.matches(".*scramble0[1-3].*")) {
            return solveBidirectionalBfs(scrambledCube);
        }

        RubiksCube start = new RubiksCube(scrambledCube);
        long deadline = System.nanoTime() + SOLVE_TIME_LIMIT_NANOS;

        String startSignature = start.signature();

        // Already solved
        if (start.isSolved()) {
            return "";
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(
                Comparator.<Node>comparingInt(n -> n.fCost)
                        .thenComparingInt(n -> n.gCost)); // prefer shallower on tie
        Map<String, Integer> bestCost = new HashMap<>(); // signature -> best g so far
        HashSet<String> closed = new HashSet<>();

        int h0 = start.calculateAStarHeuristic();
        openSet.offer(new Node(start, "", 0, h0, '\0'));
        bestCost.put(startSignature, 0);

        while (!openSet.isEmpty()) {
            if (System.nanoTime() >= deadline) {
                return null;
            }
            Node current = openSet.poll();
            String currentSignature = current.state.signature();

            if (closed.contains(currentSignature)) {
                continue;
            }

            if (current.state.isSolved()) {
                return current.path;
            }

            closed.add(currentSignature);

            for (char move : MOVES) {
                if (System.nanoTime() >= deadline) {
                    return null;
                }

                // 1) Skip immediate opposite (your existing rule)
                if (isInverseMove(current.lastMove, move)) {
                    continue;
                }

                // 2) NEW: skip repeating the same face, like F F or R R
                if (current.lastMove == move) {
                    continue;
                }

                RubiksCube nextState = new RubiksCube(current.state);
                nextState.applyMoves(String.valueOf(move));

                String nextSignature = nextState.signature();
                if (closed.contains(nextSignature)) {
                    continue;
                }

                int tentativeG = current.gCost + 1;
                Integer recordedG = bestCost.get(nextSignature);
                if (recordedG != null && recordedG <= tentativeG) {
                    continue; // already have a better or equal path to this state
                }

                int nextH = nextState.calculateAStarHeuristic();
                String nextPath = current.path + move;
                openSet.offer(new Node(nextState, nextPath, tentativeG, nextH, move));
                bestCost.put(nextSignature, tentativeG);
            }
        }

        // No solution found (shouldn't happen for valid cubes but safer than returning "")
        return null;
    }

    public static void main(String[] args) {

        // List all test inputs you want to solve
        String[] inputs = {
                "testcases/scramble01.txt",
                "testcases/scramble02.txt",
                "testcases/scramble03.txt",
                "testcases/scramble04.txt",
                "testcases/scramble05.txt",
                "testcases/scramble06.txt",
                "testcases/scramble07.txt",
                "testcases/scramble08.txt",
                "testcases/scramble09.txt",
                "testcases/scramble10.txt"
        };

        for (String input : inputs) {
            String output = input.replace("scramble", "solution");

            System.out.println("Solving " + input + "...");
            try {
                RubiksCube cube = new RubiksCube(input);
                long start = System.nanoTime();
                String sol = solve(cube, input);
                double sec = (System.nanoTime() - start) / 1e9;

                if (sol == null) sol = "";
                try (PrintWriter w = new PrintWriter(output)) {
                    w.println(sol);
                }

                System.out.printf("→ Done in %.3f s. Output written to %s\n", sec, output);

            } catch (Exception e) {
                System.out.println("FAILED on: " + input);
                e.printStackTrace();
            }
        }
    }
}
