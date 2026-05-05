package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BPiece;
import sopra.steria.helpers.Helpers;

public class GoodEvaluator implements Evaluator {
    private static final int PAWN_VALUE   = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE   = 500;
    private static final int QUEEN_VALUE  = 900;
    private static final int KING_VALUE   = 20000;

    @Override
    public int evaluate(BBoard board) {
        int score = 0;
        int[] pieces = board.getPieceBoards(); // length 64, one entry per square

        for (int sq= 0; sq < 64; sq++) {
            int piece = pieces[sq];
            if(piece == BPiece.none) continue;

            boolean isWhite = BPiece.isWhite(piece);
            int type = BPiece.getPieceType(piece);

            // Tables are from Black's perspective (index 0 = a8).
            // White squares must be mirrored (sq ^ 56) so rank 1 maps to index 56+.
            // Black squares are used as-is.
            int mirroredIndex = isWhite ? (sq ^ 56) : sq;

            score += isWhite
                    ? PstTables.PST[type][mirroredIndex]
                    : -PstTables.PST[type][mirroredIndex]; // PST
            score += Helpers.pieceValue(type); // Material Value
        }

        return board.isWhiteToMove() ? score : -score;
    }
}
