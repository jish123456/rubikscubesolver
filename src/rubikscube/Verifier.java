package rubikscube;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Verifier {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java rubikscube.Verifier <input_file> <solution_file>");
            return;
        }

        String inputFile = args[0];
        String solutionFile = args[1];

        try {
            // 1. Load Cube
            RubiksCube cube = new RubiksCube(inputFile);
            System.out.println("Initial State:");
            System.out.println(cube);

            // 2. Read Solution
            StringBuilder solutionBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(solutionFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    solutionBuilder.append(line).append(" ");
                }
            }
            String solution = solutionBuilder.toString().trim();
            System.out.println("Applying solution: " + solution);

            // 3. Apply Moves
            cube.applyMoves(solution);

            // 4. Check Solved
            if (cube.isSolved()) {
                System.out.println("VERIFICATION PASSED: Cube is solved.");
            } else {
                System.out.println("VERIFICATION FAILED: Cube is NOT solved.");
                System.out.println("Final State:");
                System.out.println(cube);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
