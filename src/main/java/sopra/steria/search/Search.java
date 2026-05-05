package sopra.steria.search;

import knight.clubbing.core.BBoard;
import knight.clubbing.core.BMove;
import knight.clubbing.core.BPiece;
import knight.clubbing.movegen.MoveGenerator;
import sopra.steria.evaluation.BadEvaluator;
import sopra.steria.evaluation.Evaluator;
import sopra.steria.ordering.BadMoveOrderer;
import sopra.steria.ordering.MoveOrderer;

import static sopra.steria.EngineConst.INF;
import static sopra.steria.EngineConst.MATE_SCORE;

public class Search {

    private static final int MAX_PLY = 64;

    private volatile boolean stop;
    private long startTime;
    private SearchSetting setting;
    private long nodes;

    private final Evaluator evaluator;
    private final MoveOrderer moveOrderer;
    private short[][] killerMoves;
    private int[][] historyMoves;

    public Search() {
        this.evaluator = new BadEvaluator();
        this.moveOrderer = new BadMoveOrderer();
    }

    public SearchResult bestMove(BBoard board, SearchSetting setting) {
        this.startTime = System.currentTimeMillis();
        this.setting = setting;
        this.stop = false;
        this.killerMoves = new short[MAX_PLY][2];
        this.historyMoves = new int[64][64];

        SearchResult bestResult = new SearchResult();
        bestResult.setScore(-INF);


        for (int depth = 1; depth <= setting.maxDepth(); depth++) {
            try {
                SearchResult result = searchDepth(board, depth);
                bestResult = result;

                checkStop();

                long elapsed = getTimeTakenMillis();
                String pv = result.getBestMove() != null ? result.getBestMove() : "";
                System.out.println("info depth " + depth + " score cp " + result.getScore() + " time " + elapsed + " pv " + pv);

                if (isDecisive(result)) break;
            } catch (SearchInterruptedException e) {
                break;
            }

        }

        bestResult.setTimeTakenMillis(getTimeTakenMillis());
        return bestResult;
    }

    private SearchResult searchDepth(BBoard board, int depth) {
        SearchResult bestResult = new SearchResult();
        bestResult.setScore(-INF);
        bestResult.setDepth(depth);
        int alpha = -INF;
        int beta = INF;
        this.nodes = 0;

        BMove[] moves = new MoveGenerator(board).generateMoves(false);
        moveOrderer.orderMoves(moves, board, killerMoves[0], historyMoves);

        for (BMove move : moves) {
            checkStop();

            board.makeMove(move, true);
            int score = -negamax(board, depth - 1, -beta, -alpha, 1);
            board.undoMove(move, true);

            if (score > bestResult.getScore()) {
                bestResult.setScore(score);
                bestResult.setBestMove(move.getUci());
            }

            alpha = Math.max(alpha, score);
        }

        bestResult.setNodesSearched(this.nodes);
        return bestResult;
    }

    private int negamax(BBoard board, int depth, int alpha, int beta, int ply) {
        nodes++;

        if (isNthNode(1023))
            checkStop();

        if (depth <= 0) return quiescence(board, alpha, beta, ply);

        if (board.isDrawByRepetition()) {
            return -50; // Contempt: strongly prefer to avoid draws
        }

        // Null move pruning: skip our turn and see if opponent can still beat beta
        if (depth >= 3 && !board.isInCheck()) {
            board.makeNullMove();
            int nullScore = -negamax(board, depth - 3, -beta, -beta + 1, ply + 1);
            board.undoNullMove();
            if (nullScore >= beta) return beta;
        }

        int bestScore = -INF;

        BMove[] nextMoves = new MoveGenerator(board).generateMoves(false);

        moveOrderer.orderMoves(nextMoves, board, killerMoves[ply], historyMoves);

        if (nextMoves.length == 0) {
            if (board.isInCheck())
                return -MATE_SCORE + ply;
            else
                return 0;
        }

        for (BMove move : nextMoves) {
            board.makeMove(move, true);
            int score = -negamax(board, depth - 1, -beta, -alpha, ply + 1);
            board.undoMove(move, true);

            bestScore = Math.max(bestScore, score);
            alpha = Math.max(alpha, score);

            if (alpha >= beta) {
                // Store killer move if it's a quiet move (not a capture)
                if (board.getPieceBoards()[move.targetSquare()] == BPiece.none
                        && move.moveFlag() != BMove.enPassantCaptureFlag) {
                    if (killerMoves[ply][0] != move.value()) {
                        killerMoves[ply][1] = killerMoves[ply][0];
                        killerMoves[ply][0] = move.value();
                    }
                    historyMoves[move.startSquare()][move.targetSquare()] += depth * depth;
                }
                break;
            }
        }

        return bestScore;
    }

    private int quiescence(BBoard board, int alpha, int beta, int ply) {
        nodes++;

        if (isNthNode(1023))
            checkStop();

        int standPat = evaluator.evaluate(board);

        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;

        BMove[] captures = new MoveGenerator(board).generateMoves(true);
        moveOrderer.orderMoves(captures, board, null, null);

        for (BMove move : captures) {
            board.makeMove(move, true);
            int score = -quiescence(board, -beta, -alpha, ply + 1);
            board.undoMove(move, true);

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }

        return alpha;
    }

    private boolean isNthNode(int n) {
        return (nodes & n) == 0;
    }

    private boolean isDecisive(SearchResult result) {
        return Math.abs(result.getScore()) >= MATE_SCORE - result.getDepth();
    }

    private long getTimeTakenMillis() {
        return System.currentTimeMillis() - startTime;
    }

    private void checkStop() {
        if (stop) throw new SearchInterruptedException();
        if (Thread.currentThread().isInterrupted()) {
            stop = true;
            throw new SearchInterruptedException();
        }

        if (setting.timeLimit() > 0 && getTimeTakenMillis() >= setting.timeLimit()) {
            stop = true;
            throw new SearchInterruptedException();
        }
    }
}
