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


    // Author: Jishith Guntamadugu
    /**
     * default constructor
     * Creates a Rubik's Cube in an initial state:
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
     * @param fileName
     * @throws IOException
     * @throws IncorrectFormatException
     * Creates a Rubik's Cube from the description in fileName
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

            // Top U: lines 0 - 2 , chars 3 - 5
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) cube[U][r][c] = lines[r].charAt(3 + c);
            }

            // Middle strip L F R B: lines[3 - 5]
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) cube[L][r][c] = lines[3 + r].charAt(c);
                for (int c = 0; c < 3; c++) cube[F][r][c] = lines[3 + r].charAt(3 + c);
                for (int c = 0; c < 3; c++) cube[R][r][c] = lines[3 + r].charAt(6 + c);
                for (int c = 0; c < 3; c++) cube[B][r][c] = lines[3 + r].charAt(9 + c);
            }

            // Bottom (D): lines[6..8], chars 3..5
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) cube[D][r][c] = lines[6 + r].charAt(3 + c);
            }
        }
    }

    // helpers
    private char[] getRow(int face, int row) {
        char[] res = new char[3];
        for (int c = 0; c < 3; c++) res[c] = cube[face][row][c];
        return res;
    }
    private void setRow(int face, int row, char[] values) {
        for (int c = 0; c < 3; c++) cube[face][row][c] = values[c];
    }
    private char[] getCol(int face, int col) {
        char[] res = new char[3];
        for (int r = 0; r < 3; r++) res[r] = cube[face][r][col];
        return res;
    }
    private void setCol(int face, int col, char[] values) {
        for (int r = 0; r < 3; r++) cube[face][r][col] = values[r];
    }
    private char[] rev(char[] a) { return new char[] { a[2], a[1], a[0] }; }

    private void rotateFaceClockwise(int face) {
        char[][] temp = new char[3][3];

        // Copy face to temp with rotation
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                temp[j][2 - i] = cube[face][i][j];
            }
        }

        // Copy back
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
     * @param moves
     * Applies the sequence of moves on the Rubik's Cube
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
            }
        }
    }

    /**
     * returns true if the current state of the Cube is solved,
     * i.e., it is in this state:
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
    public boolean isSolved() {
        char[] expected = {'O', 'R', 'G', 'B', 'W', 'Y'};
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

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < 3; i++) {
            result += "   ";
            for (int j = 0; j < 3; j++) result += cube[U][i][j];
            result += "\n";
        }
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) result += cube[L][r][c];
            for (int c = 0; c < 3; c++) result += cube[F][r][c];
            for (int c = 0; c < 3; c++) result += cube[R][r][c];
            for (int c = 0; c < 3; c++) result += cube[B][r][c];
            result += "\n";
        }
        for (int i = 0; i < 3; i++) {
            result += "   ";
            for (int j = 0; j < 3; j++) result += cube[D][i][j];
            result += "\n";
        }
        return result;
    }
    

    /**
     *
     * @param moves
     * @return the order of the sequence of moves
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

    public int calculateHeuristic(){
        int misplaced = 0;
        char[] expected ={'O','R', 'G', 'B', 'W', 'Y'};
        for(int face = 0; face < 6; face++){
            char colour = expected[face];
            for(int row = 0; row < 3; row++){
                for(int col =0; col < 3; col++){
                    if(cube[face][row][col] != colour){
                        misplaced++;
                    }
                }
            }
           
        }
        return misplaced;

    }
}