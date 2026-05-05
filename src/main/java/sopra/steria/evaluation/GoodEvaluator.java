package sopra.steria.evaluation;

import static sopra.steria.utilities.PieceUtilities.getMaterialValue;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BPiece;

import java.util.function.Predicate;


public class GoodEvaluator implements Evaluator {

    @Override
    public int evaluate(BBoard board) {
        int score = 0;

        // Me take opponent material good
        int whiteMaterial = getScore(board, BPiece::isWhite);
        int blackMaterial = getScore(board, piece -> !BPiece.isWhite(piece));
        score += whiteMaterial - blackMaterial;

        return board.isWhiteToMove() ? score : -score;
    }

    private int getScore(BBoard board, Predicate<Integer> predicate) {
        int[] posities = board.getPieceBoards();
        int score = 0;
        for (int bezetting : posities) {
            if (bezetting > 0) {
                if (predicate.test(bezetting)) {
                    score += getMaterialValue(bezetting);
                }
            }
        }
        return score;
    }

}
