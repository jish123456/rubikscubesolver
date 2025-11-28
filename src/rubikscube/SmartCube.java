package rubikscube;

import java.util.Arrays;

public class SmartCube {

    // Arrays to store the state of the cube
    // Permutations (p): 0-7 for corners, 0-11 for edges
    // Orientations (o): 0-2 for corners, 0-1 for edges
    public byte[] cp = { 0, 1, 2, 3, 4, 5, 6, 7 };
    public byte[] co = { 0, 0, 0, 0, 0, 0, 0, 0 };
    public byte[] ep = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    public byte[] eo = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    // Face indices (Internal SmartCube)
    private static final int U = 0, L = 1, F = 2, R = 3, B = 4, D = 5;

    // Face indices (RubiksCube class)
    private static final int RC_U = 0, RC_D = 1, RC_L = 2, RC_R = 3, RC_F = 4, RC_B = 5;

    // Colors
    private static final char COL_U = 'O';
    private static final char COL_D = 'R';
    private static final char COL_L = 'G';
    private static final char COL_R = 'B';
    private static final char COL_F = 'W';
    private static final char COL_B = 'Y';

    // Constructor: Copy from another SmartCube
    public SmartCube(SmartCube other) {
        this.cp = Arrays.copyOf(other.cp, 8);
        this.co = Arrays.copyOf(other.co, 8);
        this.ep = Arrays.copyOf(other.ep, 12);
        this.eo = Arrays.copyOf(other.eo, 12);
    }

    // Default solved state
    public SmartCube() {
    }

    // Constructor: Create from RubiksCube
    public SmartCube(RubiksCube inputCube) {
        // Map corners
        // 0: UBL (U00, L00, B02)
        setCorner(0, inputCube, RC_U, 0, 0, RC_L, 0, 0, RC_B, 0, 2);
        // 1: UBR (U02, B00, R02)
        setCorner(1, inputCube, RC_U, 0, 2, RC_B, 0, 0, RC_R, 0, 2);
        // 2: UFR (U22, R00, F02)
        setCorner(2, inputCube, RC_U, 2, 2, RC_R, 0, 0, RC_F, 0, 2);
        // 3: UFL (U20, F00, L02)
        setCorner(3, inputCube, RC_U, 2, 0, RC_F, 0, 0, RC_L, 0, 2);
        // 4: DFL (D00, L22, F20)
        setCorner(4, inputCube, RC_D, 0, 0, RC_L, 2, 2, RC_F, 2, 0);
        // 5: DFR (D02, F22, R20)
        setCorner(5, inputCube, RC_D, 0, 2, RC_F, 2, 2, RC_R, 2, 0);
        // 6: DBR (D22, R22, B20)
        setCorner(6, inputCube, RC_D, 2, 2, RC_R, 2, 2, RC_B, 2, 0);
        // 7: DBL (D20, B22, L20)
        setCorner(7, inputCube, RC_D, 2, 0, RC_B, 2, 2, RC_L, 2, 0);

        // Map edges
        // 0: UB (U01, B01)
        setEdge(0, inputCube, RC_U, 0, 1, RC_B, 0, 1);
        // 1: UR (U12, R01)
        setEdge(1, inputCube, RC_U, 1, 2, RC_R, 0, 1);
        // 2: UF (U21, F01)
        setEdge(2, inputCube, RC_U, 2, 1, RC_F, 0, 1);
        // 3: UL (U10, L01)
        setEdge(3, inputCube, RC_U, 1, 0, RC_L, 0, 1);
        // 4: FL (F10, L12)
        setEdge(4, inputCube, RC_F, 1, 0, RC_L, 1, 2);
        // 5: FR (F12, R10)
        setEdge(5, inputCube, RC_F, 1, 2, RC_R, 1, 0);
        // 6: BR (B10, R12)
        setEdge(6, inputCube, RC_B, 1, 0, RC_R, 1, 2);
        // 7: BL (B12, L10)
        setEdge(7, inputCube, RC_B, 1, 2, RC_L, 1, 0);
        // 8: DF (D01, F21)
        setEdge(8, inputCube, RC_D, 0, 1, RC_F, 2, 1);
        // 9: DR (D12, R21)
        setEdge(9, inputCube, RC_D, 1, 2, RC_R, 2, 1);
        // 10: DB (D21, B21)
        setEdge(10, inputCube, RC_D, 2, 1, RC_B, 2, 1);
        // 11: DL (D10, L21)
        setEdge(11, inputCube, RC_D, 1, 0, RC_L, 2, 1);
    }

    private void setCorner(int idx, RubiksCube rc, int f1, int r1, int c1, int f2, int r2, int c2, int f3, int r3,
            int c3) {
        char cA = rc.getColor(f1, r1, c1);
        char cB = rc.getColor(f2, r2, c2);
        char cC = rc.getColor(f3, r3, c3);

        // Identify corner
        int p = identifyCorner(cA, cB, cC);
        cp[idx] = (byte) p;

        // Identify orientation
        // 0 if U/D color is on U/D face (f1)
        // 1 if U/D color is on f2 (clockwise twist)
        // 2 if U/D color is on f3 (counter-clockwise twist)
        if (isUD(cA))
            co[idx] = 0;
        else if (isUD(cB))
            co[idx] = 1;
        else if (isUD(cC))
            co[idx] = 2;
    }

    private void setEdge(int idx, RubiksCube rc, int f1, int r1, int c1, int f2, int r2, int c2) {
        char cA = rc.getColor(f1, r1, c1);
        char cB = rc.getColor(f2, r2, c2);

        int p = identifyEdge(cA, cB);
        ep[idx] = (byte) p;

        // Orientation: 0 if correctly oriented, 1 if flipped
        // This is simplified; assumes standard color scheme and "good" edges
        // For U/D edges: 0 if U/D color is on U/D face
        // For middle edges: 0 if F/B color is on F/B face

        // Let's use a robust check based on the piece ID
        // If piece is from U/D layer (0-3, 8-11):
        // Oriented if U/D color is on U/D face (f1)
        // If piece is from middle layer (4-7):
        // Oriented if F/B color is on F/B face

        // Actually, simpler:
        // Check if the sticker at f1 matches the "primary" color of the piece at that
        // slot
        // But we don't know if it's in the right slot.
        // We know WHICH piece it is (p).
        // We know where it is (idx).

        // Standard definition:
        // Edge is oriented if it can be solved using only R, L, U, D, F2, B2.
        // F/B moves flip edges.

        // Let's stick to the color rule:
        // A piece has a "primary" facet.
        // U/D edges: U/D color is primary.
        // Middle edges: F/B color is primary.

        boolean isPrimaryA = isPrimary(p, cA);
        eo[idx] = (byte) (isPrimaryA ? 0 : 1);
    }

    private boolean isPrimary(int pieceIdx, char color) {
        // Pieces 0-3 (U layer): Primary is U (Orange)
        if (pieceIdx >= 0 && pieceIdx <= 3)
            return color == COL_U;
        // Pieces 8-11 (D layer): Primary is D (Red)
        if (pieceIdx >= 8 && pieceIdx <= 11)
            return color == COL_D;
        // Pieces 4-7 (Middle): Primary is F (White) or B (Yellow)
        // FL(4), FR(5): Primary F
        // BR(6), BL(7): Primary B
        if (pieceIdx == 4 || pieceIdx == 5)
            return color == COL_F;
        if (pieceIdx == 6 || pieceIdx == 7)
            return color == COL_B;
        return false;
    }

    private boolean isUD(char c) {
        return c == COL_U || c == COL_D;
    }

    private int identifyCorner(char c1, char c2, char c3) {
        String s = sort(c1, c2, c3);
        if (s.equals(sort(COL_U, COL_L, COL_B)))
            return 0;
        if (s.equals(sort(COL_U, COL_B, COL_R)))
            return 1;
        if (s.equals(sort(COL_U, COL_R, COL_F)))
            return 2;
        if (s.equals(sort(COL_U, COL_F, COL_L)))
            return 3;
        if (s.equals(sort(COL_D, COL_F, COL_L)))
            return 4;
        if (s.equals(sort(COL_D, COL_F, COL_R)))
            return 5;
        if (s.equals(sort(COL_D, COL_B, COL_R)))
            return 6;
        if (s.equals(sort(COL_D, COL_B, COL_L)))
            return 7;
        return 0; // Should not happen
    }

    private int identifyEdge(char c1, char c2) {
        String s = sort(c1, c2);
        if (s.equals(sort(COL_U, COL_B)))
            return 0;
        if (s.equals(sort(COL_U, COL_R)))
            return 1;
        if (s.equals(sort(COL_U, COL_F)))
            return 2;
        if (s.equals(sort(COL_U, COL_L)))
            return 3;
        if (s.equals(sort(COL_F, COL_L)))
            return 4;
        if (s.equals(sort(COL_F, COL_R)))
            return 5;
        if (s.equals(sort(COL_B, COL_R)))
            return 6;
        if (s.equals(sort(COL_B, COL_L)))
            return 7;
        if (s.equals(sort(COL_D, COL_F)))
            return 8;
        if (s.equals(sort(COL_D, COL_R)))
            return 9;
        if (s.equals(sort(COL_D, COL_B)))
            return 10;
        if (s.equals(sort(COL_D, COL_L)))
            return 11;
        return 0;
    }

    private String sort(char... chars) {
        Arrays.sort(chars);
        return new String(chars);
    }

    /**
     * MOVE LOGIC
     * Face Indices: 0:U, 1:L, 2:F, 3:R, 4:B, 5:D
     */
    public void move(int face, int power) {
        for (int i = 0; i < power; i++) {
            rotateOnce(face);
        }
    }

    private void rotateOnce(int face) {
        switch (face) {
            case 0:
                moveU();
                break;
            case 1:
                moveL();
                break;
            case 2:
                moveF();
                break;
            case 3:
                moveR();
                break;
            case 4:
                moveB();
                break;
            case 5:
                moveD();
                break;
        }
    }

    // --- Basic Moves (Cycles) ---
    // Using standard numbering:
    // Corners: UBL, UBR, UFR, UFL, DFL, DFR, DBR, DBL
    // Edges: UB, UR, UF, UL, FL, FR, BR, BL, DF, DR, DB, DL

    private void moveU() {
        cycleCorners(0, 1, 2, 3);
        cycleEdges(0, 1, 2, 3);
        // U move affects no orientation changes
    }

    private void moveD() {
        cycleCorners(4, 5, 6, 7);
        cycleEdges(8, 9, 10, 11);
        // D move affects no orientation changes
    }

    private void moveF() {
        cycleCorners(3, 2, 5, 4);
        cycleEdges(2, 5, 8, 4);
        // F move affects Corner Orientation (+1, -1, +1, -1)
        // Fix: Rotate args to achieve (+2, +1, +2, +1) relative to original order
        twistCorners(2, 5, 4, 3);
        // F move flips Edge Orientation
        flipEdges(2, 5, 8, 4);
    }

    private void moveB() {
        cycleCorners(1, 0, 7, 6);
        cycleEdges(0, 7, 10, 6);
        // Fix: Rotate args
        twistCorners(0, 7, 6, 1);
        flipEdges(0, 7, 10, 6);
    }

    private void moveL() {
        cycleCorners(0, 3, 4, 7);
        cycleEdges(3, 4, 11, 7);
        // Fix: Rotate args
        twistCorners(3, 4, 7, 0);
        // L/R moves do NOT flip edges in this scheme
    }

    private void moveR() {
        cycleCorners(2, 1, 6, 5);
        cycleEdges(1, 6, 9, 5);
        // Fix: Rotate args
        twistCorners(1, 6, 5, 2);
    }

    // --- Helpers ---
    private void cycleCorners(int a, int b, int c, int d) {
        byte temp = cp[d];
        cp[d] = cp[c];
        cp[c] = cp[b];
        cp[b] = cp[a];
        cp[a] = temp;
        byte tempO = co[d];
        co[d] = co[c];
        co[c] = co[b];
        co[b] = co[a];
        co[a] = tempO;
    }

    private void cycleEdges(int a, int b, int c, int d) {
        byte temp = ep[d];
        ep[d] = ep[c];
        ep[c] = ep[b];
        ep[b] = ep[a];
        ep[a] = temp;
        byte tempO = eo[d];
        eo[d] = eo[c];
        eo[c] = eo[b];
        eo[b] = eo[a];
        eo[a] = tempO;
    }

    private void twistCorners(int a, int b, int c, int d) {
        // 0->1->2->0 map: +1 clockwise, +2 counter-clockwise
        co[a] = (byte) ((co[a] + 1) % 3);
        co[b] = (byte) ((co[b] + 2) % 3);
        co[c] = (byte) ((co[c] + 1) % 3);
        co[d] = (byte) ((co[d] + 2) % 3);
    }

    private void flipEdges(int a, int b, int c, int d) {
        eo[a] ^= 1;
        eo[b] ^= 1;
        eo[c] ^= 1;
        eo[d] ^= 1;
    }

    @Override
    public String toString() {
        return "CP: " + Arrays.toString(cp) + "\n" +
                "CO: " + Arrays.toString(co) + "\n" +
                "EP: " + Arrays.toString(ep) + "\n" +
                "EO: " + Arrays.toString(eo);
    }
}
