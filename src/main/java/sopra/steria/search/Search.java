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
    private final TranspositionTable tt;
    private short[][] killerMoves;
    private int[][] historyMoves;

    public Search() {
        this.evaluator = new BadEvaluator();
        this.moveOrderer = new BadMoveOrderer();
        this.tt = new TranspositionTable(16); // 16 MB
    }

    public SearchResult bestMove(BBoard board, SearchSetting setting) {
        this.startTime = System.currentTimeMillis();
        this.setting = setting;
        this.stop = false;
        this.killerMoves = new short[MAX_PLY][2];
        this.historyMoves = new int[64][64];

        SearchResult bestResult = new SearchResult();
        bestResult.setScore(-INF);

        int previousScore = 0;

        for (int depth = 1; depth <= setting.maxDepth(); depth++) {
            // Soft time management: don't start new depth if >40% of time used
            if (setting.timeLimit() > 0 && getTimeTakenMillis() > setting.timeLimit() * 4 / 10) {
                break;
            }

            try {
                SearchResult result;

                // Aspiration windows: use narrow window around previous score
                if (depth >= 4) {
                    int window = 50;
                    int alphaWindow = previousScore - window;
                    int betaWindow = previousScore + window;
                    result = searchDepth(board, depth, alphaWindow, betaWindow);

                    // Re-search with full window if result is outside bounds
                    if (result.getScore() <= alphaWindow || result.getScore() >= betaWindow) {
                        result = searchDepth(board, depth, -INF, INF);
                    }
                } else {
                    result = searchDepth(board, depth, -INF, INF);
                }

                bestResult = result;
                previousScore = result.getScore();

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

    private SearchResult searchDepth(BBoard board, int depth, int alpha, int beta) {
        SearchResult bestResult = new SearchResult();
        bestResult.setScore(-INF);
        bestResult.setDepth(depth);
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

        if (isNthNode(127))
            checkStop();

        if (depth <= 0) return quiescence(board, alpha, beta, ply);

        if (board.isDrawByRepetition()) {
            return -50; // Contempt: strongly prefer to avoid draws
        }

        // Transposition table probe
        long zobristKey = board.getState().getZobristKey();
        short ttBestMove = 0;
        long ttEntry = tt.probe(zobristKey);
        if (ttEntry != 0) {
            int ttDepth = TranspositionTable.getDepth(ttEntry);
            if (ttDepth >= depth) {
                int ttScore = TranspositionTable.getScore(ttEntry);
                int ttFlag = TranspositionTable.getFlag(ttEntry);
                if (ttFlag == TranspositionTable.FLAG_EXACT) return ttScore;
                if (ttFlag == TranspositionTable.FLAG_BETA && ttScore >= beta) return beta;
                if (ttFlag == TranspositionTable.FLAG_ALPHA && ttScore <= alpha) return alpha;
            }
            ttBestMove = TranspositionTable.getBestMove(ttEntry);
        }

        // Null move pruning: skip our turn and see if opponent can still beat beta
        if (depth >= 3 && !board.isInCheck()) {
            board.makeNullMove();
            int nullScore = -negamax(board, depth - 3, -beta, -beta + 1, ply + 1);
            board.undoNullMove();
            if (nullScore >= beta) return beta;
        }

        int bestScore = -INF;
        short bestMove = 0;

        BMove[] nextMoves = new MoveGenerator(board).generateMoves(false);

        moveOrderer.orderMoves(nextMoves, board, killerMoves[ply], historyMoves);

        // PV move ordering: put TT best move first
        if (ttBestMove != 0) {
            for (int i = 0; i < nextMoves.length; i++) {
                if (nextMoves[i].value() == ttBestMove) {
                    BMove temp = nextMoves[0];
                    nextMoves[0] = nextMoves[i];
                    nextMoves[i] = temp;
                    break;
                }
            }
        }

        if (nextMoves.length == 0) {
            if (board.isInCheck())
                return -MATE_SCORE + ply;
            else
                return 0;
        }

        boolean inCheck = board.isInCheck();

        for (int i = 0; i < nextMoves.length; i++) {
            BMove move = nextMoves[i];
            boolean isCapture = board.getPieceBoards()[move.targetSquare()] != BPiece.none;
            boolean isPromotion = move.isPromotion();

            board.makeMove(move, true);

            int score;
            // LMR: reduce depth for late quiet moves
            if (i >= 4 && depth >= 3 && !inCheck && !isCapture && !isPromotion && !board.isInCheck()) {
                score = -negamax(board, depth - 2, -beta, -alpha, ply + 1);
                if (score > alpha) {
                    score = -negamax(board, depth - 1, -beta, -alpha, ply + 1);
                }
            } else {
                score = -negamax(board, depth - 1, -beta, -alpha, ply + 1);
            }

            board.undoMove(move, true);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move.value();
            }
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

        // Store in transposition table
        int flag;
        if (bestScore <= alpha) flag = TranspositionTable.FLAG_ALPHA;
        else if (bestScore >= beta) flag = TranspositionTable.FLAG_BETA;
        else flag = TranspositionTable.FLAG_EXACT;
        tt.store(zobristKey, depth, bestScore, flag, bestMove);

        return bestScore;
    }

    private static final int[] DELTA_PIECE_VALUES = {0, 100, 320, 330, 500, 900, 0};
    private static final int DELTA_MARGIN = 200;

    private static final int MAX_QSEARCH_DEPTH = 8;

    private int quiescence(BBoard board, int alpha, int beta, int ply) {
        nodes++;

        if (isNthNode(127))
            checkStop();

        int standPat = evaluator.evaluate(board);

        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;

        // Hard limit on quiescence depth
        if (ply >= MAX_PLY - 1) return standPat;

        BMove[] captures = new MoveGenerator(board).generateMoves(true);
        moveOrderer.orderMoves(captures, board, null, null);

        for (BMove move : captures) {
            // Delta pruning: skip if capture can't possibly raise alpha
            int capturedPieceType = BPiece.getPieceType(board.getPieceBoards()[move.targetSquare()]);
            if (standPat + DELTA_PIECE_VALUES[capturedPieceType] + DELTA_MARGIN < alpha) {
                continue;
            }

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
