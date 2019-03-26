package edu.stanford.cs108.tetris;

public class myBrain extends DefaultBrain {
    @Override
    public double rateBoard(Board board) {
        final int width = board.getWidth();
        final int maxHeight = board.getMaxHeight();

        int sumHeight = 0;
        int holes = 0;

        // Count the holes, and sum up the heights
        for (int x=0; x<width; x++) {
            final int colHeight = board.getColumnHeight(x);
            sumHeight += colHeight;

            int y = colHeight - 2;    // addr of first possible hole

            while (y>=0) {
                if  (!board.getGrid(x,y)) {
                    holes++;
                }
                y--;
            }
        }

        double avgHeight = ((double)sumHeight)/width;

        //System.out.println("Rated at: 8* " + maxHeight + "+ 40* " + avgHeight + "+ 1.25*" + holes);
        //System.out.println("Rated at: " + (8*maxHeight + 40*avgHeight + 1.25*holes));
        //board.printBoard(true);

        // Add up the counts to make an overall score
        // The weights, 8, 40, etc., are just made up numbers that appear to work
        return (8*maxHeight + 2*avgHeight + 10*holes);
    }

}
