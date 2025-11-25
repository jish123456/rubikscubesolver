package rubikscube;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RubiksCube {
    private char[][][] cube;

    private static final int U = 0; // up
    private static final int D = 1; // down
    private static final int L = 2; // left
    private static final int R = 3; // right
    private static final int F = 4; // front
    private static final int B = 5; // back

    /**
     * Default constructor.
     * Creates a Rubik's Cube in an initial state:
     *
     *    OOO
     *    OOO
     *    OOO
     * GGGWWWBBBYYY
     * GGGWWWBBBYYY
     * GGGWWWBBBYYY
     *    RRR
     *    RRR
     *    RRR
     */
    public RubiksCube() {
        this.cube = new char[6][3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cube[U][i][j] = 'O';
                cube[D][i][j] = 'R';
                cube[L][i][j] = 'G';
                cube[R][i][j] = 'B';
                cube[F][i][j] = 'W';
                cube[B][i][j] = 'Y';
            }
        }
    }

    /**
     * Creates a Rubik's Cube from the description in fileName.
     *
     * @param fileName path to cube description
     * @throws IOException
     * @throws IncorrectFormatException if file format doesn't match expected net
     */
    public RubiksCube(String fileName) throws IOException, IncorrectFormatException {
        cube = new char[6][3][3];

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String[] lines = new String[9];
            for (int i = 0; i < 9; i++) {
                lines[i] = br.readLine();
                if (lines[i] == null) {
                    throw new IncorrectFormatException("File too short, expected 9 lines");
                }
            }

            // Basic length checks to avoid StringIndexOutOfBounds
            for (int i = 0; i < 3; i++) {
                if (lines[i].length() < 6) {
                    throw new IncorrectFormatException("Top face lines too short");
                }
            }
            for (int i = 3; i < 6; i++) {
                if (lines[i].length() < 12) {
                    throw new IncorrectFormatException("Middle strip lines too short");
                }
            }
            for (int i = 6; i < 9; i++) {
                if (lines[i].length() < 6) {
                    throw new IncorrectFormatException("Bottom face lines too short");
                }
            }

            // Top (U): lines 0–2, chars 3–5
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    cube[U][r][c] = lines[r].charAt(3 + c);
                }
            }

            // Middle strip (L, F, R, B): lines 3–5
            for (int r = 0; r < 3; r++) {
                int lineIndex = 3 + r;
                for (int c = 0; c < 3; c++) cube[L][r][c] = lines[lineIndex].charAt(c);
                for (int c = 0; c < 3; c++) cube[F][r][c] = lines[lineIndex].charAt(3 + c);
                for (int c = 0; c < 3; c++) cube[R][r][c] = lines[lineIndex].charAt(6 + c);
                for (int c = 0; c < 3; c++) cube[B][r][c] = lines[lineIndex].charAt(9 + c);
            }

            // Bottom (D): lines 6–8, chars 3–5
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    cube[D][r][c] = lines[6 + r].charAt(3 + c);
                }
            }
        }
    }

    /**
     * Copy constructor used by search algorithms to branch without mutating
     * the original cube instance.
     *
     * @param other cube to copy
     */
    public RubiksCube(RubiksCube other) {
        cube = new char[6][3][3];
        for (int face = 0; face < 6; face++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    cube[face][row][col] = other.cube[face][row][col];
                }
            }
        }
    }

    // --- Move helpers ---

    private void rotateFaceClockwise(int face) {
        char[][] temp = new char[3][3];

        // Rotate into temp
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                temp[j][2 - i] = cube[face][i][j];
            }
        }

        // Copy back to face
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cube[face][i][j] = temp[i][j];
            }
        }
    }

    private void turnF() {
        rotateFaceClockwise(F);

        char[] temp = new char[3];
        for (int i = 0; i < 3; i++) {
            temp[i] = cube[U][2][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[U][2][i] = cube[L][2 - i][2];
        }

        for (int i = 0; i < 3; i++) {
            cube[L][i][2] = cube[D][0][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[D][0][i] = cube[R][2 - i][0];
        }

        for (int i = 0; i < 3; i++) {
            cube[R][i][0] = temp[i];
        }
    }

    private void turnB() {
        rotateFaceClockwise(B);

        char[] temp = new char[3];
        for (int i = 0; i < 3; i++) {
            temp[i] = cube[U][0][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[U][0][i] = cube[R][i][2];
        }

        for (int i = 0; i < 3; i++) {
            cube[R][i][2] = cube[D][2][2 - i];
        }

        for (int i = 0; i < 3; i++) {
            cube[D][2][i] = cube[L][i][0];
        }

        for (int i = 0; i < 3; i++) {
            cube[L][i][0] = temp[2 - i];
        }
    }

    private void turnL() {
        rotateFaceClockwise(L);

        char[] temp = new char[3];
        for (int i = 0; i < 3; i++) {
            temp[i] = cube[U][i][0];
        }

        for (int i = 0; i < 3; i++) {
            cube[U][i][0] = cube[B][2 - i][2];
        }

        for (int i = 0; i < 3; i++) {
            cube[B][i][2] = cube[D][2 - i][0];
        }

        for (int i = 0; i < 3; i++) {
            cube[D][i][0] = cube[F][i][0];
        }

        for (int i = 0; i < 3; i++) {
            cube[F][i][0] = temp[i];
        }
    }

    private void turnR() {
        rotateFaceClockwise(R);

        char[] temp = new char[3];
        for (int i = 0; i < 3; i++) {
            temp[i] = cube[U][i][2];
        }

        for (int i = 0; i < 3; i++) {
            cube[U][i][2] = cube[F][i][2];
        }

        for (int i = 0; i < 3; i++) {
            cube[F][i][2] = cube[D][i][2];
        }

        for (int i = 0; i < 3; i++) {
            cube[D][i][2] = cube[B][2 - i][0];
        }

        for (int i = 0; i < 3; i++) {
            cube[B][i][0] = temp[2 - i];
        }
    }

    private void turnU() {
        rotateFaceClockwise(U);

        char[] temp = new char[3];
        for (int i = 0; i < 3; i++) {
            temp[i] = cube[F][0][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[F][0][i] = cube[R][0][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[R][0][i] = cube[B][0][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[B][0][i] = cube[L][0][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[L][0][i] = temp[i];
        }
    }

    private void turnD() {
        rotateFaceClockwise(D);

        char[] temp = new char[3];
        for (int i = 0; i < 3; i++) {
            temp[i] = cube[F][2][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[F][2][i] = cube[L][2][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[L][2][i] = cube[B][2][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[B][2][i] = cube[R][2][i];
        }

        for (int i = 0; i < 3; i++) {
            cube[R][2][i] = temp[i];
        }
    }

    /**
     * Applies the sequence of moves on the Rubik's Cube.
     * Valid moves: 'F', 'B', 'U', 'D', 'L', 'R'.
     */
    public void applyMoves(String moves) {
        for (char move : moves.toCharArray()) {
            switch (move) {
                case 'F': turnF(); break;
                case 'B': turnB(); break;
                case 'U': turnU(); break;
                case 'D': turnD(); break;
                case 'L': turnL(); break;
                case 'R': turnR(); break;
                default:
                    // Ignore unknown chars so Solver doesn't crash if something slips in
                    break;
            }
        }
    }

    /**
     * Returns true if the current state of the Cube is solved.
     */
    public boolean isSolved() {
        char[] expected = { 'O', 'R', 'G', 'B', 'W', 'Y' };
        for (int face = 0; face < 6; face++) {
            char colour = expected[face];
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (cube[face][row][col] != colour) return false;
                }
            }
        }
        return true;
    }

    /**
     * String representation of the cube in the same "net" format
     * used by the file constructor.
     *
     * This is what Solver uses as the state signature.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        // Top (U)
        for (int i = 0; i < 3; i++) {
            result.append("   ");
            for (int j = 0; j < 3; j++) {
                result.append(cube[U][i][j]);
            }
            result.append('\n');
        }

        // Middle strip (L F R B)
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) result.append(cube[L][r][c]);
            for (int c = 0; c < 3; c++) result.append(cube[F][r][c]);
            for (int c = 0; c < 3; c++) result.append(cube[R][r][c]);
            for (int c = 0; c < 3; c++) result.append(cube[B][r][c]);
            result.append('\n');
        }

        // Bottom (D)
        for (int i = 0; i < 3; i++) {
            result.append("   ");
            for (int j = 0; j < 3; j++) {
                result.append(cube[D][i][j]);
            }
            result.append('\n');
        }

        return result.toString();
    }

    /**
     * Compact signature for hashing: flatten faces in order U, D, L, R, F, B,
     * row-major per face. This avoids the multi-line formatting of toString().
     */
    public String signature() {
        char[] sig = new char[54];
        int idx = 0;
        for (int face = 0; face < 6; face++) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    sig[idx++] = cube[face][r][c];
                }
            }
        }
        return new String(sig);
    }

    /**
     * Order of a move sequence (number of times you must apply it to return to solved).
     */
    public static int order(String moves) {
        RubiksCube cube = new RubiksCube();
        int count = 0;
        do {
            cube.applyMoves(moves);
            count++;
        } while (!cube.isSolved());
        return count;
    }

    /**
     * Heuristic for A*: sum of how far every non-center sticker (all 20 movable cubies)
     * is from its home face. We approximate distance as the minimum number of face
     * quarter-turns between the current face and the target face, then scale down
     * because a single move repositions 8 stickers at once. We also take the max
     * with a simple misplaced-sticker count to give A* a bit more spread.
     */
    public int calculateAStarHeuristic() {
        int faceDistance = 0;
        int misplaced = 0;
        
        // Map colors to their solved face index
        // Based on constructor: U=O, D=R, L=G, R=B, F=W, B=Y
        // U=0, D=1, L=2, R=3, F=4, B=5
        char[] expected = { 'O', 'R', 'G', 'B', 'W', 'Y' };
        
        for (int face = 0; face < 6; face++) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    // Skip center pieces (1,1) as they don't move
                    if (r == 1 && c == 1) continue;

                    char color = cube[face][r][c];
                    int targetFace = getFaceForColor(color);
                    
                    // Add distance from current face to target face
                    faceDistance += getFaceDistance(face, targetFace);

                    // Misplaced sticker count
                    if (color != expected[face]) {
                        misplaced++;
                    }
                }
            }
        }
        
        // Normalize because a single face turn moves 8 stickers at once.
        int normalizedFace = faceDistance; // keep strong guidance; still optimistic-ish

        // Misplaced stickers: at most 8 can be corrected per move; divide by 4 to keep it aggressive but not wild
        int normalizedMisplaced = misplaced / 4;

        // Take the max to stay on the safe side for admissibility while giving better spread
        return Math.max(normalizedFace, normalizedMisplaced); 
    }

    private int getFaceForColor(char c) {
        switch (c) {
            case 'O': return U;
            case 'R': return D;
            case 'G': return L;
            case 'B': return R;
            case 'W': return F;
            case 'Y': return B;
            default: return 0;
        }
    }

    // Returns minimum moves to get a sticker from face1 to face2
    private int getFaceDistance(int f1, int f2) {
        if (f1 == f2) return 0;
        
        // Opposites are 2 moves away
        // U(0) <-> D(1)
        // L(2) <-> R(3)
        // F(4) <-> B(5)
        if ((f1 == 0 && f2 == 1) || (f1 == 1 && f2 == 0)) return 2;
        if ((f1 == 2 && f2 == 3) || (f1 == 3 && f2 == 2)) return 2;
        if ((f1 == 4 && f2 == 5) || (f1 == 5 && f2 == 4)) return 2;
        
        // Adjacent faces are 1 move away
        return 1;
    }
}
