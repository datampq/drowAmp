/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drowamp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/**
 * http://drow.today
 *
 * @author datampq
 */
public class seek extends JPanel implements MouseListener {

    private final main main;
    private int width;
    private int height;
    private int _current;

    public seek(main m, Dimension d) {
        this.setPreferredSize(d);
        width = d.width;
        height = d.height;
        main = m;
        setBackground(main.bg);
        addMouseListener(this);
    }
    private boolean blocked = false;

    @Override
    public int getWidth() {
        return width;
    }

    public void setBlocked(int i) {
        _current = i;
        repaint();
    }

    public void setCurrent(int i) {
        if (!blocked) {
            _current = i;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(main.button);
        g.fillRect(0, 0, width, height);
        g.setColor(main.bg);
        g.fillRect(_current, 0, width, height);
        if (blocked) {
            blocked = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        double a = (double) me.getX();
        main.setSeekLocation(a);
        blocked = true;
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

}
