package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BPiece;
import knight.clubbing.core.PopLsbResult;
import sopra.steria.helpers.Helpers;

public class GoodEvaluator implements Evaluator {
    @Override
    public int evaluate(BBoard board) {
        final int whiteScore = materialScore(board, BBoard.whiteIndex);
        final int blackScore = materialScore(board, BBoard.blackIndex);
        final int score = whiteScore - blackScore;

        return board.isWhiteToMove() ? score : -score;
    }

    private int materialScore(final BBoard board, final int color) {
        int score = 0;

        long bitboard = board.getColorBitboard(color);
        while (bitboard != 0L) {
            PopLsbResult result = PopLsbResult.popLsb(bitboard);
            int squareIndex = result.index;

            score += Helpers.pieceValue(BPiece.getPieceType(board.getPieceBoards()[squareIndex]));

            bitboard = result.remaining;
        }

        return score;
    }
}
