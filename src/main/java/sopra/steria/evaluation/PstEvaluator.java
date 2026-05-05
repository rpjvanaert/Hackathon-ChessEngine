package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BBoardHelper;
import knight.clubbing.core.BPiece;

public class PstEvaluator implements Evaluator {

//    private static int PAWN = 100;
//    private static int KNIGHT = 320;
//    private static int BISHOP = 500;
//    private static int ROOK = 500;
//    private static int QUEEN = 900;
//    private static int KING = 20000;



    @Override
    public int evaluate(BBoard board) {
        int score = 0;
        int[] pieces = board.getPieceBoards(); // length 64, one entry per square

        for (int sq= 0; sq < 64; sq++) {
            int piece = pieces[sq];
            if(piece == BPiece.none) continue;

            boolean isWhite = BPiece.isWhite(piece);
            int type = BPiece.getPieceType(piece);

            // Mirror square for black so we reuse white-perspective tables
//            int tableSq = isWhite ? sq : (56 - (sq / 8) * 8 + (sq % 8));
            int mirroredIndex =  isWhite ? BBoardHelper.mirrorSquare(sq) : sq;

            int bonus = switch (type) {
                case BPiece.pawn -> PstTables.PAWN[mirroredIndex];
                case BPiece.knight -> PstTables.KNIGHT[mirroredIndex];
                case BPiece.bishop -> PstTables.BISHOP[mirroredIndex];
                case BPiece.rook -> PstTables.ROOK[mirroredIndex];
                case BPiece.queen -> PstTables.QUEEN[mirroredIndex];
                case BPiece.king -> PstTables.KING[mirroredIndex];
                default -> 0;
            };

            score += isWhite ? bonus : -bonus;
        }
        return board.isWhiteToMove() ? score : -score;
    }

//
}
