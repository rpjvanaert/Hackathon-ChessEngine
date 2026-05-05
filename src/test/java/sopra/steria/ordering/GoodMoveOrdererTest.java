package sopra.steria.ordering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BMove;
import knight.clubbing.core.BPiece;

class GoodMoveOrdererTest {
    public static final int POS_B_NONE = 40;
    public static final int POS_B_PAWN = 49;
    public static final int POS_B_BISHOP = 44;
    public static final int POS_W_ROOK = 41;


    @Test
    void test() {
        // https://lichess.org/editor/8/1p6/1R2b3/8/8/1N6/8/8_w_-_-_0_1?color=white
        // witte toren met twee andere zwarte stukken
        BBoard bord = new BBoard("8/1p6/1R2b3/8/8/1N6/8/8 w - - 0 1");

        int[] pieces = bord.getPieceBoards();
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i]== BPiece.whiteRook) {
                assertThat(i).isEqualTo(POS_W_ROOK);
            }
            if (pieces[i]== BPiece.blackPawn) {
                assertThat(i).isEqualTo(POS_B_PAWN);
            }
            if (pieces[i]== BPiece.blackBishop) {
                assertThat(i).isEqualTo(POS_B_BISHOP);
            }
        }

        BMove hitBishop = new BMove(POS_W_ROOK, POS_B_BISHOP);
        BMove hitPawn = new BMove(POS_W_ROOK, POS_B_PAWN);
        BMove noHit = new BMove(POS_W_ROOK, POS_B_NONE);

        BMove[] actualMoves = {hitBishop, noHit, hitPawn};
        BMove[] expectedMoves = {hitBishop, hitPawn, noHit};

        new GoodMoveOrderer().orderMoves(actualMoves, bord);

        for (int i = 0; i < actualMoves.length; i++) {
            assertThat(actualMoves[i]).isEqualTo(expectedMoves[i]);
        }
    }

}