/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drowamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import static javax.swing.BorderFactory.createEmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 * http://drow.today
 *
 * @author datampq
 */
public class main {

    public Dimension dim;
    public manager man;
    public encoder encoder;
    public playlist playlist;
    public pluginContainer plugins;
    public canvas canvas;
    public Font fa;
    public Font mainFont;
    private final float fontSize = 12f;
    private final float faSize = 16f;
    private final String faName = "fa-regular-400.ttf";
    private final String fontName = "Montserrat-ExtraLight.ttf";
    public Color bg = Color.decode("0x181e23");
    public Color text = Color.decode("0xffffff");
    public Color button = Color.decode("0x5e81ac");
    public Color buttonHover = Color.decode("0xffffff");
    private String time = "00:00 / 00:00";
    private JLabel timeLabel;
    public JPanel playListContainer;
    public JPanel playlistPanel;
    public double volume = 0.1;
    private JLabel volLabel;
    public LinkedList<playListItem> playListItems;
    public JLabel drowTit;
    public seek seek;
    public JLabel numItems;
    public settings settings;
    public main ref;

    public main() {
        ref = this;
        playListItems = new LinkedList();
        seek = new seek(this, new Dimension(1260, 10));
        canvas = new canvas(this);
        man = new manager(this);
    }

    public void setTime(String s) {
        time = s;
        timeLabel.setText(time);
    }

    public String getCurrentSongName() {
        if (drowTit.getText().isEmpty()) {
            return "drowAmp";
        } else {
            return drowTit.getText();
        }
    }

    public String getTime() {
        return timeLabel.getText();
    }

    public int[] getBands() {
        //String dat="";
        int[] normalized = new int[encoder.correctedMagnitude.length];
        for (int i = 0; i < normalized.length; i++) {
            normalized[i] = (80 + (int) encoder.correctedMagnitude[i]) / 2;
            if (normalized[i] > 32) {
                normalized[i] = 32;
            }
             if (normalized[i] <0) {
                normalized[i] = 0;
            }
            // dat=dat+ normalized[i]+",";
           // System.out.println(normalized[i]);
        }
        //System.out.println(dat);
        return normalized;
    }

    public void setVolume() {
        int vol = (int) Math.round(volume * 100);
        volLabel.setText(vol + " %");
    }

    public void setSeekLocation(double i) {
        double percent = i / 1260;
        encoder.setSeek(percent);
        seek.setBlocked((int) i);
    }

    public void updateSeek(double d) {
        seek.setCurrent((int) d);
    }

    public class canvas extends JFrame {

        main main;

        public canvas(main main) {
            this.main = main;
            setupFrame();
            setupFont();
            add(content());
            pack();
            setVisible(true);
        }

        private void setupFrame() {
            this.setUndecorated(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dim = Toolkit.getDefaultToolkit().getScreenSize();
            FrameDragListener frameDragListener = new FrameDragListener(this);
            this.addMouseListener(frameDragListener);
            this.addMouseMotionListener(frameDragListener);
            this.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int notches = e.getWheelRotation();
                    if (notches < 0) {
                        //up
                        if (volume < 1) {
                            volume += 0.02;
                        }
                    } else {
                        if (volume > 0.02) {
                            volume -= 0.02;
                        }
                    }
                    setVolume();
                }
            });
        }

        private void setupFont() {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(faName)));
                fa = Font.createFont(Font.TRUETYPE_FONT, new File(faName)).deriveFont(faSize);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FontFormatException e) {
                e.printStackTrace();
            }

            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(fontName)));
                mainFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontName)).deriveFont(fontSize);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FontFormatException e) {
                e.printStackTrace();
            }
        }

        private JPanel content() {
            JPanel content = new JPanel();
            content.setDropTarget(new DropTarget() {
                public synchronized void drop(DropTargetDropEvent evt) {
                    try {
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : droppedFiles) {
                            man.addFile(file);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(statusBar());
            content.add(mainContent());
            content.add(botContent());
            content.add(seek);
            return content;
        }

        private JPanel mainContent() {
            JPanel content = new JPanel();
            content.setPreferredSize(new Dimension(1260, 540));
            content.setBackground(bg);
            content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
            content.add(previewPane());
            content.add(playlistView());
            return content;
        }

        private JPanel botContent() {
            JPanel content = new JPanel();
            content.setPreferredSize(new Dimension(1260, 30));
            content.setBackground(bg);
            content.setLayout(new GridLayout(1, 2));
            //left time
            //  content.add(timePane(), BorderLayout.LINE_START);
            //mid controls
            JPanel contenta = new JPanel();
            contenta.setBackground(bg);
            contenta.setLayout(new BorderLayout());
            contenta.add(timePane(), BorderLayout.LINE_START);
            contenta.add(controlPane(), BorderLayout.CENTER);
            contenta.add(volumePane(), BorderLayout.LINE_END);
            content.add(contenta);
            //left volume
            content.add(setPane());
            //bot seek
            return content;
        }

        private JPanel setPane() {
            JPanel content = new JPanel();
            content.setPreferredSize(new Dimension(300, 20));
            content.setBackground(bg);
            content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
            content.setLayout(new BorderLayout());

            JPanel contenta = new JPanel();
            contenta.setBackground(bg);
            contenta.setLayout(new BoxLayout(contenta, BoxLayout.X_AXIS));

            numItems = new JLabel();
            numItems.setForeground(button);
            numItems.setFont(mainFont);

            numItems.setText(playListItems.size() + "");
            contenta.add(numItems);

            button b = new button("\uf0c7", "save", 40, 20);
            contenta.add(b);
            button b1 = new button("\uf1f8", "reset", 40, 20);
            contenta.add(b1);
            button b2 = new button("\uf15d", "sort", 40, 20);
            contenta.add(b2);

            button b3 = new button("\uf013", "set", 40, 20);
            contenta.add(b3);

            content.add(contenta, BorderLayout.LINE_END);

            return content;

        }

        private JPanel timePane() {
            JPanel content = new JPanel();
            content.setPreferredSize(new Dimension(100, 20));
            content.setBackground(bg);
            content.setLayout(new BorderLayout());
            timeLabel = new JLabel(time);
            timeLabel.setForeground(text);
            timeLabel.setFont(mainFont);
            content.add(timeLabel, BorderLayout.CENTER);
            return content;
        }

        private JPanel volumePane() {
            JPanel content = new JPanel();
            content.setPreferredSize(new Dimension(100, 20));
            content.setBackground(bg);
            content.setLayout(new GridLayout(1, 1));
            int vol = (int) Math.round(volume * 100);
            volLabel = new JLabel(vol + " %");
            volLabel.setForeground(text);
            volLabel.setFont(mainFont);
            content.add(volLabel);
            return content;
        }

        private JPanel controlPane() {
            JPanel content = new JPanel();
            content.setPreferredSize(new Dimension(400, 30));
            content.setBackground(bg);
            content.setLayout(new GridLayout(1, 6));

            button b = new button("\uf060", "prev", 40, 20);
            content.add(b);
            button b1 = new button("\uf144", "play", 40, 20);
            content.add(b1);
            button b2 = new button("\uf28d", "stop", 40, 20);
            content.add(b2);
            button b3 = new button("\uf061", "next", 40, 20);
            content.add(b3);
            button b4 = new button("\uf074", "shuffle", 40, 20);
            content.add(b4);
            button b5 = new button("\uf01e", "repeat", 40, 20);
            content.add(b5);
            return content;
        }

        private JPanel previewPane() {
            JPanel content = new JPanel();
            content.setPreferredSize(new Dimension(960, 540));
            content.setBackground(bg);
            content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
            encoder = new encoder(main, 960, 540);
            content.add(encoder);
            return content;
        }

        private JPanel playlistView() {
            playListContainer = new JPanel();
            playListContainer.setBackground(bg);
            playListContainer.setPreferredSize(new Dimension(300, 540));
            playListContainer.setLayout(new BoxLayout(playListContainer, BoxLayout.Y_AXIS));
            playlistPanel = new JPanel();
            playlistPanel.setBackground(bg);

            JScrollPane content = new JScrollPane(playlistPanel);
            JScrollBar sb = content.getVerticalScrollBar();
            content.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            sb.setUI(new scrollUI(button, bg));
            sb.setUnitIncrement(10);

            content.setPreferredSize(new Dimension(300, 540));
            content.setBackground(bg);
            playlistPanel.setLayout(new BoxLayout(playlistPanel, BoxLayout.Y_AXIS));

            content.setBorder(createEmptyBorder());
            playListContainer.add(content);
            return playListContainer;
        }

        private JPanel statusBar() {
            JPanel content = new JPanel();
            content.setPreferredSize(new Dimension(968, 40));
            content.setBackground(bg);
            content.setLayout(new GridLayout(1, 3));
            JLabel l = new JLabel("drowAmp");
            l.setForeground(button);
            l.setFont(mainFont);
            drowTit = new JLabel("");
            drowTit.setForeground(text);
            drowTit.setFont(mainFont);
            content.add(l);
            content.add(drowTit);
            button b = new button("\uf00d", "exit", 30, 30);
            JPanel contenta = new JPanel();
            contenta.setBackground(bg);
            contenta.setLayout(new BorderLayout());
            contenta.add(b, BorderLayout.LINE_END);
            content.add(contenta);
            return content;
        }

    }

    public void removeAllItems() {
        playlistPanel.removeAll();
        playListItems = new LinkedList();
        numItems.setText(playListItems.size() + "");
        canvas.repaint();
        canvas.revalidate();
    }

    public void sortItems() {
        playlistPanel.removeAll();
        Collections.sort(playListItems, (playListItem o1, playListItem o2) -> {
            //System.out.println("comparing" + o2.name);
            return o1.name.substring(0, 3).compareTo(o2.name.substring(0, 3));
        });
        for (playListItem item : playListItems) {
            playlistPanel.add(item);
        }
        numItems.setText(playListItems.size() + "");
        canvas.repaint();
        canvas.revalidate();

    }

    public void addItemToPlaylist(String file) {
        playListItem m = new playListItem(file, 0, new Dimension(200, 25));
        playListItems.add(m);
        playlistPanel.add(m);
        numItems.setText(playListItems.size() + "");
        canvas.repaint();
        canvas.revalidate();
    }

    public void deselectAll() {
        for (playListItem item : playListItems) {
            item.selected = false;
            item.setBackground(bg);
        }
        canvas.repaint();
        canvas.revalidate();
    }

    public void select(String f) {
        drowTit.setText(f);
        for (playListItem item : playListItems) {
            if (item.name.equals(f)) {
                item.selected = true;
                item.setBackground(button);
            } else {
                item.selected = false;
            }
        }
        canvas.repaint();
        canvas.revalidate();
    }

    public class playListItem extends JPanel implements MouseListener {

        public boolean selected = false;
        public final String name;
        public int index;

        public playListItem(String name, int index, Dimension d) {
            setBackground(bg);
            setLayout(new BorderLayout());
            this.setPreferredSize(d);
            this.index = index;
            this.name = name;
            JLabel l = new JLabel(" " + name);
            l.setFont(mainFont);
            l.setForeground(text);
            add(l, BorderLayout.PAGE_START);
            addMouseListener(this);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            man.PlayFile(name);
            deselectAll();
            selected = true;
        }

        @Override
        public void mousePressed(MouseEvent me) {
        }

        @Override
        public void mouseReleased(MouseEvent me) {
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            setBackground(button);
        }

        @Override
        public void mouseExited(MouseEvent me) {
            if (selected) {
                setBackground(button);
            } else {
                setBackground(bg);
            }
        }
    }

    public class button extends JPanel implements MouseListener {

        private final String a;
        private final JLabel l;
        private boolean active = false;

        public button(String label, String action, int w, int h) {
            a = action;
            l = new JLabel(label);
            l.setForeground(button);
            l.setFont(fa);
            add(l);
            this.setBackground(bg);
            this.setPreferredSize(new Dimension(w, h));
            addMouseListener(this);
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            if (a.equals("exit")) {
                System.exit(0);
            }
            if (!playListItems.isEmpty()) {
                if (a.equals("play")) {
                    man.getPlay();
                }
                if (a.equals("next")) {
                    man.getNext();
                }
                if (a.equals("prev")) {
                    man.getPrev();
                }
                if (a.equals("stop")) {
                    man.getStop();
                }
            }

            if (a.equals("repeat")) {
                man.setRepeat();
                active = !active;
            }
            if (a.equals("shuffle")) {
                man.setShuffle();
                active = !active;
            }

            if (a.equals("sort")) {
                sortItems();
            }
            if (a.equals("reset")) {
                removeAllItems();
            }

            if (a.equals("set")) {
                if (settings == null) {
                    settings = new settings(ref);
                } else {
                    settings.setVisible(true);
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
            l.setForeground(buttonHover);
        }

        @Override
        public void mouseExited(MouseEvent me) {
            if (active) {
                l.setForeground(buttonHover);
            } else {
                l.setForeground(button);
            }

        }
    }

}
