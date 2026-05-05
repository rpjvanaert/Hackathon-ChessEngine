package sopra.steria.evaluation;

import knight.clubbing.core.BBoard;

import java.util.HashMap;
import java.util.Map;

public class MvvLvaEvaluator implements Evaluator{

    private static final Map<String ,Integer> pieceValues =  new HashMap<String, Integer>();

    public MvvLvaEvaluator() {
        this.pieceValues.put("P", 100);
        this.pieceValues.put("N", 350);
        this.pieceValues.put("B", 350);
        this.pieceValues.put("R", 525);
        this.pieceValues.put("Q", 1000);
    }


    @Override
    public int evaluate(BBoard board) {
        return 0;
    }
}
