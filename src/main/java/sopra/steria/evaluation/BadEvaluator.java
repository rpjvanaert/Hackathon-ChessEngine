package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BPiece;
import knight.clubbing.movegen.PrecomputedMoveData;

public class BadEvaluator implements Evaluator {
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int MOBILITY_WEIGHT = 4;

    private static final long[] KNIGHT_ATTACKS = PrecomputedMoveData.getInstance().getKnightAttackBitboards();

    // Piece-Square Tables (from white's perspective, index 0 = a1, index 63 = h8)
    private static final int[] PAWN_PST = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10,-20,-20, 10, 10,  5,
         5, -5,-10,  0,  0,-10, -5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5,  5, 10, 25, 25, 10,  5,  5,
        10, 10, 20, 30, 30, 20, 10, 10,
        50, 50, 50, 50, 50, 50, 50, 50,
         0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] KNIGHT_PST = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] BISHOP_PST = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] ROOK_PST = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] QUEEN_PST = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] KING_PST = {
         20, 30, 10,  0,  0, 10, 30, 20,
         20, 20,  0,  0,  0,  0, 20, 20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30
    };

    @Override
    public int evaluate(BBoard board) {
        int whiteMaterial = countMaterial(board, BPiece.white);
        int blackMaterial = countMaterial(board, BPiece.black);

        int whitePst = countPST(board, BPiece.white);
        int blackPst = countPST(board, BPiece.black);

        int whiteMobility = countMobility(board, BBoard.whiteIndex);
        int blackMobility = countMobility(board, BBoard.blackIndex);

        int whiteBishopPair = Long.bitCount(board.getBitboard(BPiece.whiteBishop)) >= 2 ? 30 : 0;
        int blackBishopPair = Long.bitCount(board.getBitboard(BPiece.blackBishop)) >= 2 ? 30 : 0;

        int whitePawnStructure = evaluatePawnStructure(board, BPiece.white);
        int blackPawnStructure = evaluatePawnStructure(board, BPiece.black);

        int whiteKingSafety = evaluateKingSafety(board, BBoard.whiteIndex);
        int blackKingSafety = evaluateKingSafety(board, BBoard.blackIndex);

        int score = (whiteMaterial + whitePst + whiteMobility + whiteBishopPair + whitePawnStructure + whiteKingSafety)
                  - (blackMaterial + blackPst + blackMobility + blackBishopPair + blackPawnStructure + blackKingSafety);

        return board.isWhiteToMove() ? score : -score;
    }

    private int countMaterial(BBoard board, int color) {
        int material = 0;
        material += PAWN_VALUE * Long.bitCount(board.getBitboard(BPiece.pawn | color));
        material += KNIGHT_VALUE * Long.bitCount(board.getBitboard(BPiece.knight | color));
        material += BISHOP_VALUE * Long.bitCount(board.getBitboard(BPiece.bishop | color));
        material += ROOK_VALUE * Long.bitCount(board.getBitboard(BPiece.rook | color));
        material += QUEEN_VALUE * Long.bitCount(board.getBitboard(BPiece.queen | color));
        return material;
    }

    private int countPST(BBoard board, int color) {
        int score = 0;
        boolean isWhite = color == BPiece.white;
        score += evaluatePiecePST(board.getBitboard(BPiece.pawn | color), PAWN_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.knight | color), KNIGHT_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.bishop | color), BISHOP_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.rook | color), ROOK_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.queen | color), QUEEN_PST, isWhite);
        score += evaluatePiecePST(board.getBitboard(BPiece.king | color), KING_PST, isWhite);
        return score;
    }

    private int evaluatePiecePST(long bitboard, int[] table, boolean isWhite) {
        int score = 0;
        while (bitboard != 0) {
            int square = Long.numberOfTrailingZeros(bitboard);
            score += table[isWhite ? square : square ^ 56];
            bitboard &= bitboard - 1;
        }
        return score;
    }

    private int countMobility(BBoard board, int colorIndex) {
        int color = colorIndex == BBoard.whiteIndex ? BPiece.white : BPiece.black;
        long friendly = board.getColorBitboard(colorIndex);
        int mobility = 0;

        // Knight mobility (uses precomputed table - very fast)
        long knights = board.getBitboard(BPiece.knight | color);
        while (knights != 0) {
            int sq = Long.numberOfTrailingZeros(knights);
            mobility += Long.bitCount(KNIGHT_ATTACKS[sq] & ~friendly);
            knights &= knights - 1;
        }

        return mobility * MOBILITY_WEIGHT;
    }

    private int evaluatePawnStructure(BBoard board, int color) {
        int score = 0;
        long pawns = board.getBitboard(BPiece.pawn | color);
        long enemyPawns = board.getBitboard(BPiece.pawn | (color == BPiece.white ? BPiece.black : BPiece.white));
        boolean isWhite = color == BPiece.white;

        long pawnsCopy = pawns;
        while (pawnsCopy != 0) {
            int sq = Long.numberOfTrailingZeros(pawnsCopy);
            int file = sq & 7;
            int rank = sq >> 3;

            // Doubled pawns: another pawn on the same file
            long fileMask = FILE_MASKS[file];
            if (Long.bitCount(pawns & fileMask) > 1) {
                score -= 15;
            }

            // Isolated pawns: no friendly pawns on adjacent files
            long adjacentFiles = 0;
            if (file > 0) adjacentFiles |= FILE_MASKS[file - 1];
            if (file < 7) adjacentFiles |= FILE_MASKS[file + 1];
            if ((pawns & adjacentFiles) == 0) {
                score -= 20;
            }

            // Passed pawns: no enemy pawns on same or adjacent files ahead
            long passedMask = adjacentFiles | fileMask;
            long aheadMask = isWhite ? ~((1L << ((rank + 1) * 8)) - 1) : ((1L << (rank * 8)) - 1);
            if ((enemyPawns & passedMask & aheadMask) == 0) {
                int bonus = isWhite ? rank : (7 - rank);
                score += bonus * 10;
            }

            pawnsCopy &= pawnsCopy - 1;
        }
        return score;
    }

    private int evaluateKingSafety(BBoard board, int colorIndex) {
        int color = colorIndex == BBoard.whiteIndex ? BPiece.white : BPiece.black;
        int kingSquare = board.getKingSquare(colorIndex);
        int kingFile = kingSquare & 7;
        long pawns = board.getBitboard(BPiece.pawn | color);
        int score = 0;

        // Penalize open files near the king (no friendly pawns shielding)
        for (int f = Math.max(0, kingFile - 1); f <= Math.min(7, kingFile + 1); f++) {
            if ((pawns & FILE_MASKS[f]) == 0) {
                score -= 15;
            }
        }

        return score;
    }

    // File masks for pawn structure
    private static final long[] FILE_MASKS = {
        0x0101010101010101L,       // a-file
        0x0202020202020202L,       // b-file
        0x0404040404040404L,       // c-file
        0x0808080808080808L,       // d-file
        0x1010101010101010L,       // e-file
        0x2020202020202020L,       // f-file
        0x4040404040404040L,       // g-file
        0x8080808080808080L        // h-file
    };
}
