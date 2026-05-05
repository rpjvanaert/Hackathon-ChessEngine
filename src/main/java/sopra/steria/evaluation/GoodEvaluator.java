package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BPiece;
import sopra.steria.helpers.Helpers;

public class GoodEvaluator implements Evaluator {
    @Override
    public int evaluate(BBoard board) {
        int score = 0;
        int[] pieces = board.getPieceBoards(); // length 64, one entry per square

        for (int sq = 0; sq < 64; sq++) {
            int piece = pieces[sq];
            if (piece == BPiece.none) continue;

            boolean isWhite = BPiece.isWhite(piece);
            int type = BPiece.getPieceType(piece);

            // Tables are from Black's perspective (index 0 = a8).
            // White squares must be mirrored (sq ^ 56) so rank 1 maps to index 56+.
            // Black squares are used as-is.
            int mirroredIndex = isWhite ? (sq ^ 56) : sq;

            int materialValue = Helpers.pieceValue(type);
            score += (isWhite
                    ? PstTables.PST[type][mirroredIndex]
                    : -PstTables.PST[type][mirroredIndex])
                    * materialValue;
            score += materialValue;
        }

        return board.isWhiteToMove() ? score : -score;
    }
}
