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
        int mobilityScore = evaluatePieceActivity(board);
        score += mobilityScore;

        return board.isWhiteToMove() ? score : -score;
    }

    private int evaluatePieceActivity(BBoard board) {
        int[] pieces = board.getPieceBoards();
        int score = 0;

        for (int sq = 0; sq < 64; sq++) {
            int piece = pieces[sq];
            if (piece == BPiece.none) continue;

            boolean isWhite = BPiece.isWhite(piece);
            int type = BPiece.getPieceType(piece);
            int file = sq & 7;
            int rank = sq >> 3;

            int bonus = 0;

            // Center control bonus (d4, e4, d5, e5 = 27, 28, 35, 36)
            if ((file >= 3 && file <= 4) && (rank >= 3 && rank <= 4)) {
                bonus += switch (type) {
                    case BPiece.pawn -> 10;
                    case BPiece.knight -> 25;
                    case BPiece.bishop -> 15;
                    case BPiece.rook -> 10;
                    case BPiece.queen -> 20;
                    case BPiece.king -> 0;
                    default -> 0;
                };
            }

            // Starting square penalty (knights/bishops trapped)
            if (type == BPiece.knight && ((isWhite && sq == 1) || (!isWhite && sq == 62))) {
                bonus -= 15; // Knight on b1/g8 is trapped
            }
            if (type == BPiece.bishop && ((isWhite && (sq == 2 || sq == 5)) || (!isWhite && (sq == 57 || sq == 62)))) {
                bonus -= 10; // Bishop on starting square usually blocked
            }
            // Rank advancement bonus for minor pieces
            if (type == BPiece.knight || type == BPiece.bishop) {
                int rankDist = isWhite ? rank : 7 - rank;
                bonus += rankDist * 2; // Bonus for advanced pieces
            }

            score += isWhite ? bonus : -bonus;
        }
        return score;
    }
}
