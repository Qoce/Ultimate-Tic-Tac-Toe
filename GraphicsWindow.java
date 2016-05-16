package main;


import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import javax.swing.*;


public class GraphicsWindow extends Applet implements MouseListener
{
	private static final long serialVersionUID = 0x4550a73265cc2120L;
    public JFrame frame;
    public JPanel panel;
    public UTTT game;
    public volatile boolean playersTurn; 
    public Color colors[] = {
        new Color(255, 255, 255, 255), new Color(255, 0, 0, 255), new Color(0, 255, 0, 255), new Color(0, 0, 255, 255)
    };
    public GraphicsWindow()
    {
        playersTurn = false;
    }

    public void init()
    {
        frame = new JFrame();
        frame.setSize(720, 720);
        panel = (JPanel)frame.getContentPane();
        panel.addMouseListener(this);
        frame.setVisible(true);
        game = new UTTT();
        drawBoard();
        doComputerMove(null);
        drawBoard();
        playersTurn = true;
        System.out.println("I'm Done");
    }

    public void doComputerMove(Point p)
    {
        game.doNextMove(p);
    }

    public void drawBoard()
    {
        JLabel boardLabel = new JLabel();
        int boardStatus[][] = game.squares;
        BufferedImage image = new BufferedImage(720, 720, 5);
        Graphics g = image.getGraphics();
        for(int i = 0; i < 9; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                g.setColor(colors[boardStatus[i][j]]);
                g.fillRect((i * 720) / 9 + 1, (j * 720) / 9 + 1, 79, 79);
            }

        }

        if(game.playerOptions != null)
        {
            for(int i = 0; i < game.playerOptions.size(); i++)
            {
                g.setColor(colors[3]);
                g.fillRect((((Point)game.playerOptions.get(i)).x * 720) / 9 + 1, (((Point)game.playerOptions.get(i)).y * 720) / 9 + 1, 79, 79);
            }

        }
        g.setColor(Color.black);
        for(int i = 1; i < 3; i++)
        {
            g.fillRect(240 * i - 3, 0, 6, 720);
            g.fillRect(0, 240 * i - 3, 720, 6);
        }

        boardLabel.setIcon(new ImageIcon(image));
        boardLabel.setBounds(0, 0, 720, 720);
        boardLabel.setVisible(true);
        panel.removeAll();
        panel.add(boardLabel);
        panel.repaint();
    }

    public boolean isPointOption(Point p)
    {
        for(Iterator<Point> iterator = game.playerOptions.iterator(); iterator.hasNext();)
        {
            Point i = (Point)iterator.next();
            if(i.x == p.x && i.y == p.y)
                return true;
        }

        return false;
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public void mouseEntered(MouseEvent mouseevent)
    {
    }

    public void mouseExited(MouseEvent mouseevent)
    {
    }

    public void mousePressed(MouseEvent arg0)
    {
        double x = arg0.getX();
        double y = arg0.getY();
        x /= 720D;
        y -= 10D;
        y /= 720D;
        y *= 9D;
        x *= 9D;
        if(isPointOption(new Point((int)x, (int)y)) && playersTurn)
        {
            playersTurn = false;
            int temp = game.placeSquare(game.ID_PLAYER, new Point((int)x, (int)y), game.squares);
            game.score += temp;
            game.squareScores[(int)x / 3][(int)y / 3] += temp;
            game.tilesInSquare[(int)x / 3][(int)y / 3]++;
            drawBoard();
            doComputerMove(new Point((int)x, (int)y));
            drawBoard();
            playersTurn = true;
        }
    }

    public void mouseReleased(MouseEvent mouseevent)
    {
    }
}
