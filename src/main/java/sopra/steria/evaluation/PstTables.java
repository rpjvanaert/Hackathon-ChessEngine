package sopra.steria.evaluation;

public class PstTables {

    // See https://www.chessprogramming.org/Simplified_Evaluation_Function at the PST part
    // Tables are from White's perspective.
    // Index 0 = a1 (bottom-left), index 63 = h8 (top-right).
    // Each row below represents one rank, rank 1 first.

    public static final int[] PAWN = {
    //   a    b    c    d    e    f    g    h
         0,   0,   0,   0,   0,   0,   0,   0,  // rank 1
         5,  10,  10, -20, -20,  10,  10,   5,  // rank 2
         5,  -5, -10,   0,   0, -10,  -5,   5,  // rank 3
         0,   0,   0,  20,  20,   0,   0,   0,  // rank 4
         5,   5,  10,  25,  25,  10,   5,   5,  // rank 5
        10,  10,  20,  30,  30,  20,  10,  10,  // rank 6
        50,  50,  50,  50,  50,  50,  50,  50,  // rank 7
         0,   0,   0,   0,   0,   0,   0,   0   // rank 8
    };

    public static final int[] KNIGHT = {
    //   a    b    c    d    e    f    g    h
       -50, -40, -30, -30, -30, -30, -40, -50,  // rank 1
       -40, -20,   0,   0,   0,   0, -20, -40,  // rank 2
       -30,   0,  10,  15,  15,  10,   0, -30,  // rank 3
       -30,   5,  15,  20,  20,  15,   5, -30,  // rank 4
       -30,   0,  15,  20,  20,  15,   0, -30,  // rank 5
       -30,   5,  10,  15,  15,  10,   5, -30,  // rank 6
       -40, -20,   0,   5,   5,   0, -20, -40,  // rank 7
       -50, -40, -30, -30, -30, -30, -40, -50   // rank 8
    };

    public static final int[] BISHOP = {
    //   a    b    c    d    e    f    g    h
       -20, -10, -10, -10, -10, -10, -10, -20,  // rank 1
       -10,   0,   0,   0,   0,   0,   0, -10,  // rank 2
       -10,   0,   5,  10,  10,   5,   0, -10,  // rank 3
       -10,   5,   5,  10,  10,   5,   5, -10,  // rank 4
       -10,   0,  10,  10,  10,  10,   0, -10,  // rank 5
       -10,  10,  10,  10,  10,  10,  10, -10,  // rank 6
       -10,   5,   0,   0,   0,   0,   5, -10,  // rank 7
       -20, -10, -10, -10, -10, -10, -10, -20   // rank 8
    };

    public static final int[] ROOK = {
    //   a    b    c    d    e    f    g    h
         0,   0,   0,   0,   0,   0,   0,   0,  // rank 1
         5,  10,  10,  10,  10,  10,  10,   5,  // rank 2
        -5,   0,   0,   0,   0,   0,   0,  -5,  // rank 3
        -5,   0,   0,   0,   0,   0,   0,  -5,  // rank 4
        -5,   0,   0,   0,   0,   0,   0,  -5,  // rank 5
        -5,   0,   0,   0,   0,   0,   0,  -5,  // rank 6
        -5,   0,   0,   0,   0,   0,   0,  -5,  // rank 7
         0,   0,   0,   5,   5,   0,   0,   0   // rank 8
    };

    public static final int[] QUEEN = {
    //   a    b    c    d    e    f    g    h
       -20, -10, -10,  -5,  -5, -10, -10, -20,  // rank 1
       -10,   0,   0,   0,   0,   0,   0, -10,  // rank 2
       -10,   0,   5,   5,   5,   5,   0, -10,  // rank 3
        -5,   0,   5,   5,   5,   5,   0,  -5,  // rank 4
         0,   0,   5,   5,   5,   5,   0,  -5,  // rank 5
       -10,   5,   5,   5,   5,   5,   0, -10,  // rank 6
       -10,   0,   5,   0,   0,   0,   0, -10,  // rank 7
       -20, -10, -10,  -5,  -5, -10, -10, -20   // rank 8
    };

    public static final int[] KING = {
    //   a    b    c    d    e    f    g    h
       -30, -40, -40, -50, -50, -40, -40, -30,  // rank 1
       -30, -40, -40, -50, -50, -40, -40, -30,  // rank 2
       -30, -40, -40, -50, -50, -40, -40, -30,  // rank 3
       -30, -40, -40, -50, -50, -40, -40, -30,  // rank 4
       -20, -30, -30, -40, -40, -30, -30, -20,  // rank 5
       -10, -20, -20, -20, -20, -20, -20, -10,  // rank 6
        20,  20,   0,   0,   0,   0,  20,  20,  // rank 7
        20,  30,  10,   0,   0,  10,  30,  20   // rank 8
    };


}
