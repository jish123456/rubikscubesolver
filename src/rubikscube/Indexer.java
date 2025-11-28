package rubikscube;

public class Indexer {

    // Pre-computed factorials for permutation ranking
    private static final int[] FACTORIALS = { 1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800 };

    /**
     * MASTER FUNCTION FOR CORNERS
     * Combines Permutation and Orientation into a single unique index.
     * Range: [0 to 88,179,839]
     * 
     * @param p: Array of size 8 containing corner positions (0-7)
     * @param o: Array of size 8 containing corner orientations (0-2)
     */
    public static int getCornerIndex(byte[] p, byte[] o) {
        int permIndex = rankPermutation(p, 8);
        int orientIndex = rankCornerOrientation(o);

        // Combine them: (PermutationIndex * 3^7) + OrientationIndex
        // 2187 is 3^7
        return (permIndex * 2187) + orientIndex;
    }

    /**
     * RANK PERMUTATION (Lehmer Code)
     * Maps an arrangement of numbers to a unique integer index.
     * Works for both Corners (n=8) and Edges (n=12, or subsets like n=6).
     */
    public static int rankPermutation(byte[] p, int n) {
        int index = 0;

        // Loop through the positions
        for (int i = 0; i < n - 1; i++) {
            int countSmaller = 0;

            // Count how many pieces to the right of 'i' are smaller than p[i]
            for (int j = i + 1; j < n; j++) {
                if (p[j] < p[i]) {
                    countSmaller++;
                }
            }

            // Add to index based on the factorial of remaining positions
            index += countSmaller * FACTORIALS[n - 1 - i];
        }
        return index;
    }

    /**
     * RANK CORNER ORIENTATION
     * Maps the orientation of the first 7 corners to a number [0 to 2186].
     * (The 8th corner's orientation is fixed by the other 7, so we ignore it).
     */
    public static int rankCornerOrientation(byte[] o) {
        int index = 0;

        // Treat 'o' as a Base-3 number
        for (int i = 0; i < 7; i++) {
            index = (index * 3) + o[i];
        }
        return index;
    }

    // Pre-computed factorials/products for partial ranking
    // 12P6 calculation helpers
    // 11P5, 10P4, 9P3, 8P2, 7P1, 6P0
    private static final int[] PARTIAL_FACTORIALS = {
            11 * 10 * 9 * 8 * 7, // 55440
            10 * 9 * 8 * 7, // 5040
            9 * 8 * 7, // 504
            8 * 7, // 56
            7, // 7
            1 // 1
    };

    /**
     * RANK PARTIAL PERMUTATION
     * Maps the positions of K specific pieces (e.g. 0-5) in N slots (12).
     * 
     * @param ep:      The edge permutation array (size 12).
     * @param targets: The piece IDs to track (e.g. {0,1,2,3,4,5}).
     */
    public static int rankPartialPermutation(byte[] ep, int[] targets) {
        int n = ep.length; // 12
        int k = targets.length; // 6

        // Find positions of targets
        int[] positions = new int[k];
        for (int i = 0; i < k; i++) {
            int target = targets[i];
            for (int j = 0; j < n; j++) {
                if (ep[j] == target) {
                    positions[i] = j;
                    break;
                }
            }
        }

        // Rank the positions sequence
        int index = 0;
        // Available slots mask (or boolean array)
        boolean[] used = new boolean[n];

        for (int i = 0; i < k; i++) {
            int pos = positions[i];
            int countSmaller = 0;
            for (int j = 0; j < pos; j++) {
                if (!used[j]) {
                    countSmaller++;
                }
            }

            index += countSmaller * PARTIAL_FACTORIALS[i];
            used[pos] = true;
        }
        return index;
    }

    /**
     * RANK EDGE ORIENTATION (subset)
     * Maps orientations of the K specific pieces.
     * We need to find the orientation of piece 0, piece 1... piece 5.
     * NOT the orientation at slot 0, slot 1...
     */
    public static int rankEdgeOrientation(byte[] eo, byte[] ep, int[] targets) {
        int index = 0;
        for (int t : targets) {
            // Find where piece t is
            int pos = -1;
            for (int i = 0; i < ep.length; i++) {
                if (ep[i] == t) {
                    pos = i;
                    break;
                }
            }
            index = (index * 2) + eo[pos];
        }
        return index;
    }

    public static int getFirst6EdgesIndex(byte[] ep, byte[] eo) {
        int[] targets = { 0, 1, 2, 3, 4, 5 };
        int permIndex = rankPartialPermutation(ep, targets);
        int orientIndex = rankEdgeOrientation(eo, ep, targets);
        return (permIndex * 64) + orientIndex;
    }

    public static int getSecond6EdgesIndex(byte[] ep, byte[] eo) {
        int[] targets = { 6, 7, 8, 9, 10, 11 };
        int permIndex = rankPartialPermutation(ep, targets);
        int orientIndex = rankEdgeOrientation(eo, ep, targets);
        return (permIndex * 64) + orientIndex;
    }
}
