package sopra.steria.helpers;

import knight.clubbing.core.BPiece;

public class Helpers {


    public static int pieceValue(int piece) {
        return switch (piece) {
            case BPiece.pawn -> 100;
            case BPiece.knight, BPiece.bishop -> 350;
            case BPiece.rook -> 525;
            case BPiece.queen -> 1000;
            default -> 0; // King is not considered in MVV-LVA
        };
    }

    /**
     *
     * @param victimValue captured piece
     * @param attackerValue attacking piece
     * Score is weighted based on that victim value is more important
     * @return move score
     */
    public static int scoreCapture(int victimValue, int attackerValue) {
        return victimValue * 10 - attackerValue; // Higher score for capturing more valuable pieces
    }
}
