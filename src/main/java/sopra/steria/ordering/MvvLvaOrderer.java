package sopra.steria.ordering;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BMove;
import knight.clubbing.core.BPiece;

import static sopra.steria.helpers.Helpers.pieceValue;
import static sopra.steria.helpers.Helpers.scoreCapture;

public class MvvLvaOrderer implements MoveOrderer {

    @Override
    public void orderMoves(BMove[] moves, BBoard board) {
        int[] scores = new int[moves.length];

        for (int i = 0; i < moves.length; i++) {
            scores[i] = score(moves[i], board);
        }

        sortMovesByScore(moves, scores);
    }

    private int score(BMove move, BBoard board) {
        int[] pieceBoards = board.getPieceBoards();
        int attacker = BPiece.getPieceType(pieceBoards[move.startSquare()]);
        int victim = BPiece.getPieceType(pieceBoards[move.targetSquare()]);
        int attackerVal = pieceValue(attacker);
        int victimVal = pieceValue(victim);

        if (victim == BPiece.none) {
            return 0; // don't penalize quiet moves
        }
        return scoreCapture(victimVal, attackerVal);
    }

    private void sortMovesByScore(BMove[] moves, int[] scores) {
        for (int i = 0; i < moves.length - 1; i++) {
            for (int j = i + 1; j < moves.length; j++) {
                if (scores[j] > scores[i]) {
                    // Swap moves
                    BMove tempMove = moves[i];
                    moves[i] = moves[j];
                    moves[j] = tempMove;

                    // Swap scores
                    int tempScore = scores[i];
                    scores[i] = scores[j];
                    scores[j] = tempScore;
                }
            }
        }
    }
}
