package sopra.steria.ordering;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BMove;
import sopra.steria.evaluation.Evaluator;

import java.util.HashMap;
import java.util.Map;

public class MvvLvaOrderer implements MoveOrderer {

    public MvvLvaOrderer() {
        Map<String, Integer> pieceValues = new HashMap<String, Integer>();
        pieceValues.put("P", 100);
        pieceValues.put("N", 350);
        pieceValues.put("B", 350);
        pieceValues.put("R", 525);
        pieceValues.put("Q", 1000);
    }

    @Override
    public void orderMoves(BMove[] moves, BBoard board) {

        for (int i = 0; i < moves.length; i++) {

        }
    }

    /**
     *
     * @param victimValue captured piece
     * @param attackerValue attacking piece
     * Score is weighted based on that victim value is more important
     * @return move score
     */
    private int scoreCapture(int victimValue, int attackerValue) {
        return victimValue * 10 - attackerValue; // Higher score for capturing more valuable pieces
    }
}
