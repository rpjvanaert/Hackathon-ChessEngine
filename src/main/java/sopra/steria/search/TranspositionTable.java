package sopra.steria.search;

public class TranspositionTable {

    public static final int FLAG_EXACT = 0;
    public static final int FLAG_ALPHA = 1; // upper bound
    public static final int FLAG_BETA = 2;  // lower bound

    private final long[] keys;
    private final long[] data; // packed: score (16 bits) | depth (8 bits) | flag (2 bits) | bestMove (16 bits)

    private final int size;

    public TranspositionTable(int sizeMB) {
        // Each entry = 16 bytes (key + data)
        this.size = (sizeMB * 1024 * 1024) / 16;
        this.keys = new long[size];
        this.data = new long[size];
    }

    public void clear() {
        java.util.Arrays.fill(keys, 0);
        java.util.Arrays.fill(data, 0);
    }

    public void store(long zobristKey, int depth, int score, int flag, short bestMove) {
        int index = index(zobristKey);

        // Always replace (simple replacement scheme)
        keys[index] = zobristKey;
        data[index] = pack(score, depth, flag, bestMove);
    }

    public long probe(long zobristKey) {
        int index = index(zobristKey);
        if (keys[index] == zobristKey) {
            return data[index];
        }
        return 0;
    }

    public boolean hit(long zobristKey) {
        return keys[index(zobristKey)] == zobristKey;
    }

    private int index(long zobristKey) {
        return (int) (Long.remainderUnsigned(zobristKey, size));
    }

    private static long pack(int score, int depth, int flag, short bestMove) {
        return ((long)(score & 0xFFFF) << 26)
             | ((long)(depth & 0xFF) << 18)
             | ((long)(flag & 0x3) << 16)
             | ((long)(bestMove & 0xFFFF));
    }

    public static int getScore(long data) {
        int raw = (int)((data >> 26) & 0xFFFF);
        // Sign extend from 16 bits
        if (raw >= 32768) raw -= 65536;
        return raw;
    }

    public static int getDepth(long data) {
        return (int)((data >> 18) & 0xFF);
    }

    public static int getFlag(long data) {
        return (int)((data >> 16) & 0x3);
    }

    public static short getBestMove(long data) {
        return (short)(data & 0xFFFF);
    }
}
