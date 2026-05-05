package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BMove;
import knight.clubbing.core.BPiece;
import knight.clubbing.movegen.MoveGenerator;
import sopra.steria.helpers.Helpers;

public class GoodEvaluator implements Evaluator {
    private static final int PAWN_VALUE   = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE   = 500;
    private static final int QUEEN_VALUE  = 900;
    private static final int KING_VALUE   = 20000;

    // Mobility scoring (in centipawns per move)
    private static final int MOBILITY_WEIGHT = 1; // +1cp per extra move

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

            int bonus = switch (type) {
                case BPiece.pawn   -> PAWN_VALUE   + PstTables.PAWN[mirroredIndex];
                case BPiece.knight -> KNIGHT_VALUE + PstTables.KNIGHT[mirroredIndex];
                case BPiece.bishop -> BISHOP_VALUE + PstTables.BISHOP[mirroredIndex];
                case BPiece.rook   -> ROOK_VALUE   + PstTables.ROOK[mirroredIndex];
                case BPiece.queen  -> QUEEN_VALUE  + PstTables.QUEEN[mirroredIndex];
                case BPiece.king   -> KING_VALUE   + PstTables.KING[mirroredIndex];
                default -> 0;
            };

            // Mobility bonus


            score += isWhite ? bonus : -bonus; // PST
            score += Helpers.pieceValue(type); // Material Value
        }
        int mobilityScore = evaluateMobilityApprox(board);
        score += mobilityScore;

        return board.isWhiteToMove() ? score : -score;
    }

    private int evaluateMobilityApprox(BBoard board) {
        int[] pieces = board.getPieceBoards();
        int whiteActivity = 0;
        int blackActivity = 0;

        // Count active pieces for each side
        for (int sq = 0; sq < 64; sq++) {
            int piece = pieces[sq];
            if (piece == BPiece.none) continue;

            boolean isWhite = BPiece.isWhite(piece);
            int type = BPiece.getPieceType(piece);

            // Weight by piece type
            int weight = switch (type) {
                case BPiece.pawn -> 1;     // Pawns: limited mobility
                case BPiece.knight -> 3;   // Knights: high activity
                case BPiece.bishop -> 2;   // Bishops: medium activity
                case BPiece.rook -> 3;     // Rooks: high activity
                case BPiece.queen -> 4;    // Queens: highest activity
                case BPiece.king -> 1;     // Kings: limited in middlegame
                default -> 0;
            };

            if (isWhite) {
                whiteActivity += weight;
            } else {
                blackActivity += weight;
            }
        }

        return (whiteActivity - blackActivity) * MOBILITY_WEIGHT;
    }
}
