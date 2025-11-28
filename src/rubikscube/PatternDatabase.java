package rubikscube;

import java.io.*;
import java.util.*;

public class PatternDatabase {

    public static final int TYPE_CORNER = 0;
    public static final int TYPE_EDGE1 = 1;
    public static final int TYPE_EDGE2 = 2;

    private int type;
    private String filename;
    private byte[] table;
    private int size;

    public PatternDatabase(int type, String filename) {
        this.type = type;
        this.filename = filename;

        // Calculate sizes
        // Corners: 8! * 3^7 = 88,179,840
        // Edges (subset 6): 12P6 * 2^6 = 665,280 * 64 = 42,577,920
        if (type == TYPE_CORNER) {
            this.size = 88179840;
        } else {
            this.size = 42577920;
        }
        this.table = new byte[this.size];
        Arrays.fill(this.table, (byte) -1); // -1 indicates unvisited
    }

    public boolean load() {
        File f = new File(filename);
        if (!f.exists())
            return false;
        try (FileInputStream fis = new FileInputStream(f)) {
            fis.read(table);
            System.out.println("Loaded PDB: " + filename);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void generate() {
        System.out.println("Generating PDB: " + filename + " (This may take a moment...)");
        Queue<SmartCube> queue = new ArrayDeque<>();
        SmartCube solved = new SmartCube(); // Starts solved

        queue.add(solved);
        int index = getIndex(solved);
        table[index] = 0;

        int count = 0;
        int depth = 0;
        int nodesAtCurrentDepth = 1;
        int nodesAtNextDepth = 0;

        while (!queue.isEmpty()) {
            SmartCube current = queue.poll();
            int curDist = table[getIndex(current)];

            // Limit depth to prevent running out of memory/time during generation
            // Depth 7 is usually enough for a good heuristic
            if (curDist >= 7 && type != TYPE_CORNER)
                break;
            if (curDist >= 9 && type == TYPE_CORNER)
                break;

            for (int face = 0; face < 6; face++) {
                // Optimization: Don't twist same face twice in BFS generation (simple pruning)
                for (int power = 1; power <= 3; power++) {
                    // Make a copy and move
                    SmartCube next = new SmartCube(current);
                    next.move(face, power);

                    int nextIdx = getIndex(next);
                    if (table[nextIdx] == -1) {
                        table[nextIdx] = (byte) (curDist + 1);
                        queue.add(next);
                        nodesAtNextDepth++;
                    }
                }
            }
        }

        // Save to file so we don't calculate next time
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(table);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Generated PDB " + filename);
    }

    public int getEstimate(SmartCube cube) {
        int idx = getIndex(cube);
        // If we haven't visited this state (table[idx] == -1), return a safe high
        // number
        // or the max depth we searched.
        if (table[idx] == -1)
            return 8; // Fallback
        return table[idx];
    }

    private int getIndex(SmartCube cube) {
        if (type == TYPE_CORNER) {
            return Indexer.getCornerIndex(cube.cp, cube.co);
        } else if (type == TYPE_EDGE1) {
            // First 6 edges (0-5)
            // You need to adapt Indexer to handle specific edge subsets
            // For now, mapping logic would go here.
            return Indexer.getFirst6EdgesIndex(cube.ep, cube.eo);
        } else {
            // Second 6 edges (6-11)
            // Need to shift indices for the second set or map them to 0-5 range
            // This requires a specialized indexer method for "Edges 6-11"
            return Indexer.getSecond6EdgesIndex(cube.ep, cube.eo);
        }
    }

}
