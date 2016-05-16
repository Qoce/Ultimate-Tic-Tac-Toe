package main;

import java.awt.Point;
import java.util.ArrayList;
/**
 * The "AI" of the Ultimate tick tac toe game. 
 * @author Adam
 */
public class UTTT 
{
    public static final int ID_COMPUTER = 1; //The number stored in the board array which indicates the computer owns the tile
    public int ID_PLAYER = 2; //The number stored to indicate the player owns the tile
    public int squares[][]; //Array of squares of the board, stores either 0 for blank, or ID_PLAYER, or ID_COMPUTER
    public boolean isMyTurn; //True if it's the computers turn
    public static final int VICTORY = 10000; // TODO Value of victory
    public ArrayList<Point> playerOptions; //array list of options that the player can play. Used by graphics window class to show the player's options 
    public int score;
    public int squareScores[][]; //Used to determine board score for each individual "Square".  Each "Square" refers to a specific board within the large baord.
    public int tilesInSquare[][]; //Refers to the number of tiles in each small board
    public static final int NUM_ITERATIONS = 7; //How far the game looks into the future.  
    
    public UTTT()
    {
        squares = new int[9][9];
        isMyTurn = true;
        squareScores = new int[3][3];
        tilesInSquare = new int[3][3];
    }
    /**
     * Returns the location of the next move
     * @param p, point of the previous move, used to determine possible moves
     * @return the point of the move that the computer choses.
     */
    public Point doNextMove(Point p)
    {
    	playerOptions = null;  //Options for the next move, stored as the index of the square
        Point output; 
        if(p == null) //Happens on first move, allows playing on any blank square. then calculates best move
            output = calculateNextMove(null);
        else //Calls function to determine where the future possible moves are, then calculates best move
            output = calculateNextMove(getPossibleMoves(p, squares)); 
        if(output.x > -1) //This is false if there is no output, otherwise it places square onto board and modifies the score to account for the action
        {
            int temp = placeSquare(ID_COMPUTER, output, squares);
            score += temp;
            squareScores[output.x / 3][output.y / 3] += temp;
            tilesInSquare[output.x / 3][output.y / 3]++;
            playerOptions = getPossibleMoves(output, squares);
        }
        return output;
    }
    /**
     * Calculates the best move out of a given list of possible moves.  
     * @param possibleMoves list of possible moves
     * @return chosen move
     */
    public Point calculateNextMove(ArrayList<Point> possibleMoves)
    {
        if(possibleMoves == null)
            possibleMoves = getFullBoard(); 
        int bestOutComeValue = Integer.MIN_VALUE; 
        int bestOutComeLocation = -1;
        if(possibleMoves.size() == 0)//Occurs once game is over
            return new Point(-1, -1);
        for(int i = 0; i < possibleMoves.size(); i++) //Places the move, calculates move score, then removes the move.  Maintains the value of the best move chosen.
        {
            Point p = (Point)possibleMoves.get(i);
            int temp = placeSquare(ID_COMPUTER, p, squares);
            score += temp;
            squareScores[p.x / 3][p.y / 3] += temp;
            tilesInSquare[p.x / 3][p.y / 3]++;
            int j = calculateMoveScore(squares, p, NUM_ITERATIONS, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            score -= temp;
            squareScores[p.x / 3][p.y / 3] -= temp;
            tilesInSquare[p.x / 3][p.y / 3]--;
            squares[p.x][p.y] = 0;
            if(j > bestOutComeValue)
            {
                bestOutComeValue = j;
                bestOutComeLocation = i;
            }
        }
        //Prints the index of the chosen move and the best possible value into the console.
        System.out.println((new StringBuilder(String.valueOf(bestOutComeValue))).append("--").append(bestOutComeLocation).toString());
        return (Point)possibleMoves.get(bestOutComeLocation);
    }
    /**
     * Uses recursion to check the value of a move.  Uses alpha beta pruning to determine best possible move by searching through all possibilities 
     * @param board position of the board after the move is placed
     * @param move location of the move that was just placed
     * @param movesAhead depth of the search, value is decreased by one with each recursive call of the function, until a value of 0
     * @param isOurMove true if it is the computers move.  Alternates at every level of searching.
     * @param min 
     * @param max
     * @return Evaluation of the move in terms of how favorable board position that it results in is.
     */
    public int calculateMoveScore(int board[][], Point move, int movesAhead, boolean isOurMove, int min, int max)
    {
        if(movesAhead == 0)
            return score;
        ArrayList<Point> moves = getPossibleMoves(move, board); //Gets possible moves for next level of seraching
        if(moves.size() == 0) //If there are none, then the game is done so the function stops
            return score;
        //One of the bounds for the minimax function. The score is positive when the board position is in the computers favor
        //The evaluation assumes the player will play the move that decreases the score as much as possible,  while the computer will play the move that increases it
        int minMax; 
        if(isOurMove)
            minMax = Integer.MIN_VALUE;
        else
            minMax = Integer.MAX_VALUE;
        for(int i = 0; i < moves.size(); i++)
        {
            Point p = (Point)moves.get(i);
            int temp; //Change in score
            if(isOurMove)
                temp = placeSquare(ID_COMPUTER, p, board);
            else
                temp = placeSquare(ID_PLAYER, p, board);
            score += temp;
            int smallx = p.x / 3; //Used to determine which small board the move was in
            int smally = p.y / 3;
            tilesInSquare[smallx][smally]++;
            squareScores[smallx][smally] += temp;
            int small;
            int big;
            if(isOurMove) //Sets the range of the next tree search, this ranged is used for alpha beta pruning
            {
                small = minMax;
                big = max;
            } else
            {
                big = minMax;
                small = min;
            }//Determines the score resulting from the previous move by calling the function again, then removes the changes the loop made to the board
            int next = calculateMoveScore(board, p, movesAhead - 1, !isOurMove, small, big);
            board[p.x][p.y] = 0;
            score -= temp;
            tilesInSquare[smallx][smally]--;
            squareScores[smallx][smally] -= temp;
            if(isOurMove)//Alpha-beta pruning.  Stops iterating if the score will be rejected.  
            {
                if(next > minMax)
                    minMax = next;
                if(minMax >= max)
                    break;
            }
            else
            {
            	if(next < minMax)
            		minMax = next;
            	if(minMax <= min)
            		break;
            }
        }

        return minMax;
    }
    /**
     * Returns the possible list of moves based on the previous move.
     * The board which a player is allowed to place their next move in is determined by 
     * the position within the small board that the previous player played in.
     * If the board that the player is to play in is full, or already won than the player can play anywhere.
     * @param lastMove location of previous move
     * @param board position of the board after the move has been placed
     * @return
     */
    public ArrayList<Point> getPossibleMoves(Point lastMove, int board[][])
    {
        int x = lastMove.x % 3;
        int y = lastMove.y % 3;
        ArrayList<Point> points = new ArrayList<Point>();
        if(!isSquareDone(x % 3, y % 3, board))
        {
            for(int i = 0; i < 3; i++)
            {
                for(int j = 0; j < 3; j++)
                    if(board[i + 3 * x][j + y * 3] == 0)
                        points.add(new Point(i + x * 3, j + y * 3));
            }
        } else
        {
            for(int i = 0; i < 9; i++)
            {
                for(int j = 0; j < 9; j++)
                    if(board[i][j] == 0 && !isSquareDone(i / 3, j / 3, board))
                        points.add(new Point(i, j));
            }
        }
        return points;
    }
    /**
     * Returns true if the square is no longer playable in, which happens if the square has been won (somebody got 3 in a row)
     * Or if all 9 cells are filled. (It IS actually possible to be sent to a full board, but only if that board was played on during the first move)  
     * Such an occurrence is very rare, but accounted for
     * Used only by getPossibleMoves()
     * @param x Position of square
     * @param y Position of square
     * @param board Layout of board
     * @return true if the board is not playable
     */
    public boolean isSquareDone(int x, int y, int board[][])
    {
        if(Math.abs(squareScores[x][y]) > 5000)
            return true;
        return tilesInSquare[x][y] >= 9;
    }
    /**
     * @deprecated
     * Obsolete, used by the evaluation function, which is no longer called because it was replaced by dynamic board evaluation
     * Creates a 3 by 3 array of the values of the cells in a small board, used to evaluate board score
     * @param x Position of board
     * @param y
     * @param board Board Layout
     * @return A 3 by 3 array that contains the values of the cells in the square
     */
    public int[][] makeSquare(int x, int y, int board[][])
    {
        x *= 3;
        y *= 3;
        int newSquare[][] = new int[3][3];
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
                newSquare[i][j] = board[x + i][y + j];

        }

        return newSquare;
    }
    /**
     * @deprecated
     * @param board
     * Determines the score of the full board.  Not used because the board score is now evaluated based on the change each move induces,
     *  rather than recalculating the entire board score
     * @return score of the board
     */
    public int getBoardScore(int board[][])
    {
        int score = 0;
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
                score += getSquareScore(makeSquare(i, j, board));

        }

        return score;
    }
    /**
     * @deprecated
     * @param board 
     * @return determines the score of an individual small board, which estimates the likelihood of winning the board.
     */
    public int getSquareScore(int board[][])
    {
        int values[] = {
            getDiagonalScore(new int[] {
                board[0][0], board[0][1], board[0][2]
            }), getDiagonalScore(new int[] {
                board[1][0], board[1][1], board[1][2]
            }), getDiagonalScore(new int[] {
                board[2][0], board[2][1], board[2][2]
            }), getDiagonalScore(new int[] {
                board[0][0], board[1][0], board[2][0]
            }), getDiagonalScore(new int[] {
                board[0][1], board[1][1], board[2][1]
            }), getDiagonalScore(new int[] {
                board[0][2], board[1][2], board[2][2]
            }), getDiagonalScore(new int[] {
                board[0][0], board[1][1], board[2][2]
            }), getDiagonalScore(new int[] {
                board[0][2], board[1][1], board[2][0]
            })
        };
        int value = 0;
        for(int i = 0; i < values.length; i++)
            value += values[i];

        return value;
    }
    /**
     * Determines the score of a single group of three cells that form either a row, diagonal, or column in a 
     * @param numbers
     * @return A score based on evaluation.  10,000 if the diagonal is filled by the same player, 3 if two are 
     * filled by the same payer and one is blank, 1 if one is filled and two are blank, 0 if all are blank, or if
     * both players have at least 1 of the three tiles filled.  Used for the small boards and can be used on the big board if the
     * win condition is changed to winning three small boards in a row.  This is not implemented.
     * 
     * A positive score means the computer has the advantage 
     */
    public int getDiagonalScore(int numbers[])
    {
        int numM = 0;
        int numY = 0;
        for(int i = 0; i < numbers.length; i++)
        {
            int j = numbers[i];
            if(j == ID_PLAYER)
                numY++;
            else
            if(j == ID_COMPUTER)
                numM++;
        }

        if(numM == 3)
            return 10000;
        if(numM == 2)
            return numY != 0 ? 0 : 3;
        if(numM == 1)
            return numY != 0 ? 0 : 1;
        if(numY == 1)
            return -1;
        if(numY == 2)
            return -3;
        return numY != 3 ? 0 : -10000;
    }
    /**
     * Adds a square to the board, and determines the change in score
     * @param team, who placed the move, player or computer
     * @param p location of the move
     * @param board
     * @return The change in score the moved caused, based on immediate evaluation of diagonals, not minimax
     */
    public int placeSquare(int team, Point p, int board[][])
    {
        int prevScore = getPointScore(p, board);
        board[p.x][p.y] = team;
        return getPointScore(p, board) - prevScore;
    }
    /**
     * Determines the change in immediate score based on a single move by only evaluating the squares near the new move
     * @param p
     * @param board
     * @return
     */
    public int getPointScore(Point p, int board[][])
    {
        int smallX = p.x % 3;
        int smallY = p.y % 3;
        int squareX = (p.x / 3) * 3;
        int squareY = (p.y / 3) * 3;
        int score = 0;
        int colum[] = new int[3];
        int row[] = new int[3];
        for(int i = 0; i < 3; i++)
        {
            row[i] = board[squareX + i][p.y];
            colum[i] = board[p.x][squareY + i];
        }

        score += getDiagonalScore(row);
        score += getDiagonalScore(colum);
        if(smallX == smallY)
        {
            for(int i = 0; i < 3; i++)
                row[i] = board[squareX + i][squareY + i];

            score += getDiagonalScore(row);
        }
        if(2 - smallX == smallY)
        {
            for(int i = 0; i < 3; i++)
                row[i] = board[(squareX + 2) - i][squareY + i];

            score += getDiagonalScore(row);
        }
        if(score > 10000)
            score = 10000;
        else
        if(score < -10000)
            score = -10000;
        return score;
    }
    /**
     * @return  a list of points containing every position on the board.  Used for the first move. 
     */
    public ArrayList<Point> getFullBoard()
    {
        ArrayList<Point> points = new ArrayList<Point>();
        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
                points.add(new Point(i, j));

        }

        return points;
    }
}
