/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drowamp;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * http://drow.today
 *
 * @author datampq
 */
public class settings extends JFrame {

    private ledMatrix leds;
    private final main main;
    private final settings frameRef;

    public settings(main m) {
        main = m;
        frameRef = this;
        setupFrame();
        add(content());
        pack();
        setVisible(true);
    }

    private void setupFrame() {
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);

    }

    private JPanel content() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(main.bg);
        content.add(top());
        content.add(mid());
        //content.add(bot());
        // content.add(end());
        return content;
    }

    private JPanel top() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.setPreferredSize(new Dimension(240,60));
        content.setBackground(main.bg);
        JLabel l1 = new JLabel("Settings");
        l1.setForeground(main.text);
        l1.setFont(main.mainFont);
        content.add(l1);

        return content;
    }

    private JPanel mid() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(main.bg);
        button b = new button("\uf060", "exit", 240, 40);
        button b1 = new button("\uf0eb", "leds", 240, 40);
        content.add(b);
        content.add(b1);
        return content;
    }

    public class button extends JPanel implements MouseListener {

        private final String a;
        private final JLabel l;
        private boolean active = false;

        public button(String label, String action, int w, int h) {
            a = action;
            l = new JLabel(label);
            l.setForeground(main.button);
            l.setFont(main.fa);
            add(l);
            this.setBackground(main.bg);
            this.setPreferredSize(new Dimension(w, h));
            addMouseListener(this);
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            if (a.equals("exit")) {
                frameRef.setVisible(false);
            }
            if (a.equals("leds")) {
                if (leds == null) {
                    leds = new ledMatrix(main);
                } else {
                    leds.setVisible(true);
                }
            }
            //removeAll()
        }

        @Override
        public void mousePressed(MouseEvent me) {
        }

        @Override
        public void mouseReleased(MouseEvent me) {
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            l.setForeground(main.buttonHover);
        }

        @Override
        public void mouseExited(MouseEvent me) {
            if (active) {
                l.setForeground(main.buttonHover);
            } else {
                l.setForeground(main.button);
            }

        }
    }

}
