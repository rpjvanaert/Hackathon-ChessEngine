package sopra.steria.utilities;

import knight.clubbing.core.BPiece;

public class PieceUtilities {
    public static int getMaterialValue(int piece) {
        int pieceType = BPiece.getPieceType(piece);
        return switch (pieceType) {
            case BPiece.pawn -> 1;
            case BPiece.knight -> 3;
            case BPiece.bishop -> 3;
            case BPiece.rook -> 5;
            case BPiece.queen -> 9;
            case BPiece.king -> 6;
            default -> throw new IllegalArgumentException("Invalid piece: " + pieceType);
        };
    }

}
