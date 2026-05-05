package sopra.steria.ordering;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BBoardHelper;
import knight.clubbing.core.BMove;
import knight.clubbing.core.BPiece;
import sopra.steria.utilities.PieceUtilities;

public class GoodMoveOrderer implements MoveOrderer {

    @Override
    public void orderMoves(BMove[] moves, BBoard board) {
        int[] scores = new int[moves.length];

        for (int i = 0; i < moves.length; i++) {
            scores[i] = score(moves[i], board);
        }

        sortMovesByScore(moves, scores);
    }

    private int score(BMove move, BBoard board) {
        int targetPiece = board.getPieceBoards()[move.targetSquare()];

        // leeg? Dan waarde 0. Andere kleur ? dan waarde van stuk
        if (0==targetPiece) return 0;
        return PieceUtilities.getMaterialValue(BPiece.getPieceType(targetPiece));
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
