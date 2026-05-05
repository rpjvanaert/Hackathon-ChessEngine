package sopra.steria.ordering;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BMove;
import knight.clubbing.core.BPiece;

public class BadMoveOrderer implements MoveOrderer {

    private static final int KILLER_SCORE_1 = 900;
    private static final int KILLER_SCORE_2 = 800;
    private static final int MVV_LVA_OFFSET = 10000;

    // MVV-LVA: victim value indexed by piece type (none, pawn, knight, bishop, rook, queen, king)
    private static final int[] VICTIM_SCORE = {0, 100, 320, 330, 500, 900, 0};

    @Override
    public void orderMoves(BMove[] moves, BBoard board, short[] killers) {
        int[] scores = new int[moves.length];

        for (int i = 0; i < moves.length; i++) {
            scores[i] = score(moves[i], board, killers);
        }

        sortMovesByScore(moves, scores);
    }

    private int score(BMove move, BBoard board, short[] killers) {
        int targetPiece = board.getPieceBoards()[move.targetSquare()];

        // Captures: MVV-LVA (most valuable victim - least valuable attacker)
        if (targetPiece != BPiece.none) {
            int movingPiece = board.getPieceBoards()[move.startSquare()];
            int victimValue = VICTIM_SCORE[BPiece.getPieceType(targetPiece)];
            int attackerValue = VICTIM_SCORE[BPiece.getPieceType(movingPiece)];
            return MVV_LVA_OFFSET + victimValue - attackerValue / 100;
        }

        if (move.isPromotion() && move.promotionPieceType() == BPiece.queen) {
            return 9500; // Just below captures, well above killers
        } else if (move.isPromotion()) {
            return -100; // Underpromotions are rarely useful
        }

        // Killer moves
        if (killers != null) {
            if (move.value() == killers[0]) return KILLER_SCORE_1;
            if (move.value() == killers[1]) return KILLER_SCORE_2;
        }

        return 0;
    }

    private void sortMovesByScore(BMove[] moves, int[] scores) {
        for (int i = 0; i < moves.length - 1; i++) {
            for (int j = i + 1; j < moves.length; j++) {
                if (scores[j] > scores[i]) {
                    BMove tempMove = moves[i];
                    moves[i] = moves[j];
                    moves[j] = tempMove;

                    int tempScore = scores[i];
                    scores[i] = scores[j];
                    scores[j] = tempScore;
                }
            }
        }
    }
}
