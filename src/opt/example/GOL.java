package opt.example;

public class GOL {
    /**
     * x,y offsets of the various neighbors of a cell.
     */
    private static int[][] neighborOffsets = {
        {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1},  {-1, 1}, {-1, 0}, {-1, -1}
    };
    
    /** The board's width */
    private int boardWidth;
    /** The board's height */
    private int boardHeight;
    /** The current game board */
    private boolean[][] board;
    private boolean[][] oldBoard; // previously used board, saved for memory optimization reasons
    
    /**
     * Constructs a game board for play.
     * @param rules The ruleset to use
     * @param width The board width
     * @param height The board height
     */
    public GOL(boolean[][] board) {
        boardWidth = board.length;
        boardHeight = board[0].length;
        this.board = board;
        oldBoard = new boolean[boardWidth][boardHeight];
    }
    
    public boolean getCell(int x, int y) {
        return board[x][y];
    }
    
    /**
     * Set the value of a particular cell.
     * @param x The X-coordinate of the cell.
     * @param y The Y-coordinate of the cell.
     * @param state The new state of the cell.
     */
    public void setCell(int x, int y, boolean state) {
        board[x][y] = state;
    }
    
    /**
     * Computes the next state of the game board.
     */
    public void next() {
        // Allocate a new board to hold the next state
        boolean[][] nextBoard = oldBoard;
        oldBoard = board;
        
        // Ask the ruleset to compute the next board based on the neighborhood
        // of each cell in the current board.
        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                int neighbors = countLivingNeighbors(x, y);
                nextBoard[x][y] = applyRules(getCell(x, y), neighbors);
            }
        }
        
        // Replace the current board with the new board
        board = nextBoard;
    }
    
    /**
     * Counts the living neighbors of a cell.  The edge of the board is
     * considered to be dead.
     * @param x The X-coordinate of the cell of interest.
     * @param y The Y-coordinate of the cell of interest.
     * @return The number of living neighbors of the cell (x,y).
     */
    int countLivingNeighbors(int x, int y) {
        int n = 0;
        
        // Loop over the neighborhood, counting neighbors.
        for (int i = 0; i < 8; i++) {
            int nx = x + neighborOffsets[i][0];
            int ny = y + neighborOffsets[i][1];
            try {
                if (board[nx][ny])
                    n++;
            } catch (ArrayIndexOutOfBoundsException e) {
                // This block will be reached if (nx,ny) is not a valid
                // coordinate pair in the board.  In this case, do nothing.
            }
        }
        
        return n;
    }
    
    /**
     * Applies the rules of Conway's Game of Life.
     *
     * @param isAlive       The value of the current cell (true = alive).
     * @param neighborCount The number of living neighbors of the cell.
     * @return true if the cell should be alive in the next generation.
     */
    public boolean applyRules(boolean isAlive, int neighborCount) {
        if (isAlive) {
            return (neighborCount == 2 || neighborCount == 3) ;
        } else {
            return (neighborCount == 3);
        }
    }

	public int getBoardWidth() {
		return boardWidth;
	}

	public int getBoardHeight() {
		return boardHeight;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < boardWidth; x++) {
			for (int y = 0; y < boardHeight; y++) {
				sb.append(getCell(x, y) ? 'O' : '.');
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}
