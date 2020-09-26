/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drowamp;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Enumeration;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * http://drow.today
 *
 * @author datampq
 */
public class ledMatrix extends JFrame implements SerialPortEventListener {

    String port;
    private BufferedReader input;
    private OutputStream output;
    private SerialPort serialPort;
    private final int TIME_OUT = 2000;
    private final int DATA_RATE = 57600;
    private String mode;
    private final JFrame frameRef;
    String ms;
    //sleep time = 10 min
    // int sleepTime = 600000;
    int sleepTime = 600000;
    private worker work;
    final main main;
    boolean portOpen = false;
    private Dimension dim;
    private JTextField portUrl;

    public ledMatrix(main m) {
        Runtime.getRuntime().addShutdownHook(new ProcessorHook());
        frameRef = this;
        main = m;
        setupFrame();
        add(content());
        pack();
        setVisible(true);
    }

    private void setupFrame() {
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dim = Toolkit.getDefaultToolkit().getScreenSize();
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
        content.add(bot());
        content.add(end());
        return content;
    }

    private JPanel top() {
        JPanel content = new JPanel();
        content.setPreferredSize(new Dimension(240, 60));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(main.bg);
        JLabel l1 = new JLabel("USB PORT: ");
        l1.setForeground(main.text);
        l1.setFont(main.mainFont);
        content.add(l1);
        portUrl = new JTextField("COM8");
        portUrl.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                port = portUrl.getText();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                port = portUrl.getText();
            }

        });
        portUrl.setBackground(main.bg);
        portUrl.setForeground(main.text);
        content.add(portUrl);
        return content;
    }

    private JPanel mid() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setPreferredSize(new Dimension(240, 60));
        content.setBackground(main.bg);
        JLabel l1 = new JLabel("Select mode: ");
        mode = "bands";
        l1.setForeground(main.text);
        l1.setFont(main.mainFont);
        content.add(l1);

        return content;
    }

    private JPanel bot() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(main.bg);
        JLabel l1 = new JLabel("Arduino code:");
        l1.setForeground(main.text);
        l1.setFont(main.mainFont);
        content.add(l1);

        return content;
    }

    private JPanel end() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.setBackground(main.bg);
        //button(String label, String action, int w, int h)
        // button b = new button("\uf0c7", "save", 40, 20);
        button b = new button("\uf060", "exit", 120, 40);
        button b1 = new button("\uf058", "activate", 120, 40);
        content.add(b);
        content.add(b1);
        return content;
    }

    private class worker implements Runnable {

        public worker() {
            if (t == null) {
                t = new Thread(this, "work");
                t.start();
                System.out.println("Starting thread");
            }
        }
        Thread t;

        @Override
        public void run() {
            while (true) {
                try {
                    String txt = "";
                    int[] bands = main.getBands();
                    byte[] toByteArray = toByteArray(bands);
                    if (mode.equals("title")) {
                        ms = main.getCurrentSongName();
                        txt = " " + ms + " |";
                    } else if (mode.equals("time")) {
                        ms = main.getTime();
                        txt = ms;
                    } else {

                        //TODO: process band data;
                        // ms = "bands";
                    }
                    //  String replaceAll = ms.replaceAll(System.getProperty("line.separator"), " ");
                    System.out.println(txt);
                    byte[] b = txt.getBytes();
                    int length = b.length * 10;
                    if (serialPort != null) {
                        if (mode.equals("title") || mode.equals("time")) {
                            output.write(b);
                        } else {
                            output.write(toByteArray);
                        }

                    } else {
                        System.out.println("Err, cant send data via serial");
                        //TOODO:handle exception
                    }
                    if (mode.equals("title") || mode.equals("time")) {
                        Thread.sleep(length);
                        System.out.println("writing, & sleeping: " + length);
                    } else {
                        int sl = bands.length * 200;
                        //100
                        Thread.sleep(50);
                    }

                } catch (Exception e) {

                }

            }
        }

    }

    private byte[] toByteArray(int[] data) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);
        byte[] array = byteBuffer.array();
        return array;
    }

    private int[] tointArray(float[] data) {
        int[] arr = new int[8];
        int a = 0;
        for (int i = 0; i < data.length - 15; i++) {
            float f = 0;

            for (int j = i; j < i + 16; j++) {
                f += data[j];
            }
            arr[a] = (int) (f);
            a++;
            i += 15;
        }
        return arr;
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
            if (a.equals("activate")) {

                if (!portOpen) {
                    if (initPort()) {
                        work = new worker();
                        portOpen = true;
                    } else {
                        close();
                        portOpen = false;
                        System.out.println("Unable to init port");
                    }
                } else {
                    close();
                    portOpen = false;
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

    public class ProcessorHook extends Thread {

        @Override
        public void run() {
            close();
        }
    }

    private boolean initPort() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();

            if (currPortId.getName().equals(port)) {
                portId = currPortId;
                break;
            }

        }
        if (portId == null) {
            return false;

        }
        try {
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {

        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

}
