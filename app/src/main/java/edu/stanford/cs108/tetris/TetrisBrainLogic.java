package edu.stanford.cs108.tetris;

public class TetrisBrainLogic extends TetrisLogic {
	
	private myBrain defaultBrain;
	private boolean brainMode;
	private Brain.Move bestmove;

	public TetrisBrainLogic(TetrisUIInterface uiInterface) {
		super(uiInterface);
		bestmove = null;
        bestmove = new Brain.Move();
	}
	
	@Override
	protected void tick(int verb) {
		if(!brainMode){
			super.tick(verb);
		} else {
			if(verb == DOWN) {
				if (!gameOn) return;
				if (currentPiece != null) {
					board.undo();	// remove the piece from its old position
					bestmove = defaultBrain.bestMove(board, currentPiece, HEIGHT, bestmove);
				}
				if (bestmove != null) {

					if (!currentPiece.equals(bestmove.piece)) {
						super.tick(ROTATE);
					}
					if (bestmove.x < currentX) {
						super.tick(LEFT);
					} else if (bestmove.x > currentX) {
						super.tick(RIGHT);
					}
				}
			}
			super.tick(verb);
		}
	}
	
	// client choose whether use this Brain
	public void userSetBrain(boolean userChoose) {
		brainMode = userChoose;
		if (brainMode) {
			defaultBrain = new myBrain();
		}
	}
	
}
