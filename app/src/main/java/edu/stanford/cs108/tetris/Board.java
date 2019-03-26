// Board.java
package edu.stanford.cs108.tetris;

import java.util.Arrays;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
 */
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean DEBUG = true;
	boolean committed;

	public int[] heights;
	private int[] widths;
	private int maxHeight;
	private int[] xHeights;
	private int[] xWidths;
	private int xMaxHeight;
	private boolean [][] xGrid;
	// Here a few trivial methods are provided:

	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		committed = true;

		// more variables
		this.heights = new int[width];
		this.widths = new int[height];
		this.maxHeight = 0;

		// initialize backup
		this.xWidths = new int[height];
		this.xHeights = new int[width];
		xGrid = new boolean[width][height];
		xMaxHeight = 0;
	}


	/**
	 Returns the width of the board in blocks.
	 */
	public int getWidth() {
		return width;
	}


	/**
	 Returns the height of the board in blocks.
	 */
	public int getHeight() {
		return height;
	}


	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	 */
	public int getMaxHeight() {
		// YOUR CODE HERE
		for(int eachHeight : heights) {
			maxHeight = Math.max(eachHeight, maxHeight);
		}
		return maxHeight;
	}


	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	 */
	public void sanityCheck() {
		if(!DEBUG) {
			return ;
		}
		int[] checkWidth = new int[height];
		int[] checkHeight = new int[width];
		int checkMaxHeight = 0;
		if (DEBUG) {
			// YOUR CODE HERE
			for(int i=0;i<width;i++) {
				for(int j=0;j<height;j++) {
					if(grid[i][j] == true) {
						checkWidth[j]++;
						checkHeight[i] = j+1;
					}
					checkMaxHeight = Math.max(checkMaxHeight, checkHeight[i]);
				}
			}
		}
		if(!Arrays.equals(widths, checkWidth)) {
			throw new RuntimeException("widths is not correct");
		}
		if(!Arrays.equals(heights, checkHeight)) {
			System.out.println(heights[0]);
			System.out.println(heights[1]);
			System.out.println(heights[2]);
			System.out.println(checkHeight[0]);
			System.out.println(checkHeight[1]);
			System.out.println(checkHeight[2]);
			throw new RuntimeException("heights is not correct");
		}
		if(checkMaxHeight != maxHeight) {
			throw new RuntimeException("maxHeight is not correct");
		}
	}


	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.

	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {
		// YOUR CODE HERE
		int[] thisSkirt = piece.getSkirt();
		int dropY = 0;
		for(int i=0;i<thisSkirt.length;i++) {
			dropY = Math.max(heights[x+i]-thisSkirt[i], dropY);
		}
		return dropY;
	}


	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
		// YOUR CODE HERE
		return heights[x];
	}


	/**
	 Returns the number of filled blocks in
	 the given row.
	 */
	public int getRowWidth(int y) {
		// YOUR CODE HERE
		return widths[y];
	}


	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	 */
	public boolean getGrid(int x, int y) {
		// YOUR CODE HERE
		if(x<0 || x>=width || y<0 || y>=height) {
			return true;
		}
		if(grid[x][y] == true) {
			// this position has been filled
			return true;
		}
		return false;
	}


	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.

	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	 */

	public int place(Piece piece, int x, int y) {

		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");

		backUp();
		// YOUR CODE HERE
		int result = PLACE_OK;

		TPoint[] body = piece.getBody();
		for(int i=0;i<body.length;i++) {

			int xPosition = x+body[i].x;
			int yPosition = y+body[i].y;

			// check if this point of a piece are in bound
			if(xPosition>=width || xPosition<0
					|| yPosition>=height || yPosition <0) {
				result = PLACE_OUT_BOUNDS;
				break;
			}

			// check if this block has been filled
			if(getGrid(xPosition, yPosition)) {
				result = PLACE_BAD;
				break;
			}

			// if it's legal, place the piece
			grid[xPosition][yPosition] = true;

			// update status
			widths[yPosition] ++;
			if(widths[yPosition] == width) {
				result = PLACE_ROW_FILLED;
			}
			heights[xPosition] = Math.max(yPosition+1, heights[xPosition]);
			maxHeight = getMaxHeight();
		}

		// it fine to place
		committed = false;
		sanityCheck();
		return result;
	}

	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	 */
	public int clearRows() {
		// 1.delete the full rows in the grid
		// 2.shifting the rows above down and
		// 3.adding empty rows at the top
		// note that we should rotate the tetris left 90 degree
		if(committed) {
			backUp();
			committed = false;
		}

		int rowsCleared = 0;
		// copy the rows that are not full from bottom to top
		int from = -1;
		for (int j = 0; j < maxHeight - rowsCleared; j++) {
			from++;
			while (widths[from] == width) {
				from++;
				rowsCleared++;
			}
			for (int i = 0; i < width; i++) {
				grid[i][j] = grid[i][from];
				widths[j] = widths[from];
			}
		}

		// fill the upper empty rows
		for (int j = maxHeight - rowsCleared; j < maxHeight ; j++) {
			for (int i = 0; i < width; i++) {
				grid[i][j] = false;
				widths[j] = 0;
			}
		}

		// update heights[]
		maxHeight = 0;
		for (int i = 0; i < width; i++) {
			heights[i] = 0;
			for (int j = 0; j < height; j++) {
				if (grid[i][j] == true) {
					heights[i] = Math.max(j + 1, heights[i]);
				}
			}
			maxHeight = Math.max(maxHeight, heights[i]);
		}
		// YOUR CODE HERE
		sanityCheck();
		return rowsCleared;
	}

	/*copy the data of the current status and put it to backup data*/
	private void backUp() {
		System.arraycopy(widths, 0, xWidths, 0, widths.length);
		System.arraycopy(heights, 0, xHeights, 0, heights.length);
		xMaxHeight = maxHeight;
		for(int i=0;i<width;i++) {
			System.arraycopy(grid[i], 0, xGrid[i], 0, height);
		}
	}

	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	 */
	public void undo() {
		// in committed status
		if(committed) {
			return ;
		}

		// extract data from backup
		System.arraycopy(xWidths, 0, widths, 0, widths.length);
		System.arraycopy(xHeights, 0, heights, 0, heights.length);
		maxHeight = xMaxHeight;

		for(int i=0;i<grid.length;i++) {
			System.arraycopy(xGrid[i], 0, grid[i], 0, height);
		}
		committed = true;
		sanityCheck();
	}

	/**
	 Puts the board in the committed state.
	 */
	public void commit() {
		committed = true;
	}

	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


