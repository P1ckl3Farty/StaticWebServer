
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the thread to handle the client sending mission
 */
public class ReceiveThread extends Thread{
    private BufferedReader in;
    private String name;
    private JoinWhiteBoardGUI joinWhiteBoardGUI;
    public ReceiveThread(BufferedReader in, String name, JoinWhiteBoardGUI joinWhiteBoardGUI) {
        this.in = in;
        this.name = name;
        this.joinWhiteBoardGUI = joinWhiteBoardGUI;
    }

    @Override
    public void run() {
        String message;
        try{
            while((message = in.readLine()) != null) {
                if(message.contains("message") && !message.contains("history")) {
                    synShowMsg(message, joinWhiteBoardGUI);
                } else if(message.equals("exit")) {
                    JOptionPane.showMessageDialog(null,"Sorry, " +
                            "you've been removed or the server has been closed.\nThe app will be closed.");
                    System.exit(0);
                } else if(message.equals("new")) {
                    JoinWhiteBoardMain.drawing.interrupt();
                    JOptionPane.showMessageDialog(null, "The manager opens a new white board.");
                    joinWhiteBoardGUI.setGraphics(joinWhiteBoardGUI.getBoardPanel().getGraphics());
                    joinWhiteBoardGUI.getGraph().setColor(Color.WHITE);
                    joinWhiteBoardGUI.getGraph().fillRect(0,0,
                            JoinWhiteBoardUtil.BOARD_PANEL_WIDTH,JoinWhiteBoardUtil.BOARD_PANEL_HEIGHT);
                    JoinWhiteBoardMain.graphHistorys = new CopyOnWriteArrayList<>();
                    joinWhiteBoardGUI.resetGui();
                    JoinWhiteBoardMain.drawing = new Drawing(joinWhiteBoardGUI);
                    JoinWhiteBoardMain.drawing.start();
                } else if(message.equals("open")) {
                    JOptionPane.showMessageDialog(null, "The manager opens a recorded white board.");
                    JoinWhiteBoardMain.drawing.interrupt();
                    joinWhiteBoardGUI.setGraphics(joinWhiteBoardGUI.getBoardPanel().getGraphics());
                    JoinWhiteBoardMain.graphHistorys = new CopyOnWriteArrayList<>();
                    joinWhiteBoardGUI.getGraph().setColor(Color.WHITE);
                    joinWhiteBoardGUI.getGraph().fillRect(0,0, JoinWhiteBoardUtil.BOARD_PANEL_WIDTH,JoinWhiteBoardUtil.BOARD_PANEL_HEIGHT);
                    JoinWhiteBoardMain.drawing = new Drawing(joinWhiteBoardGUI);
                    JoinWhiteBoardMain.drawing.start();
                }
                else {
                    String[] temp = message.split("&");
                    if(temp[temp.length - 1].equals("history")) {
                        sleep(100);
                        userShowGraph(message, joinWhiteBoardGUI);
                        JoinWhiteBoardMain.graphHistorys.add(message);
                    } else {
                        synShowGraph(message, joinWhiteBoardGUI);
                        JoinWhiteBoardMain.graphHistorys.add(message + "&history");
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            JoinWhiteBoardUtil.serverQuitDialog();
        }
    }
    private void synShowMsg(String message, JoinWhiteBoardGUI jwbg) {
        showMsg(message, jwbg);
    }

    /**
     * This function is to show who is painting now
     * @param message is the coming message from the server
     * @param jwbg is the gui
     */
    private void synShowGraph(String message, JoinWhiteBoardGUI jwbg) {
        if(message.split("&").length == 1) {
            return;
        }
        userShowGraph(message, jwbg);
    }
    /**
     * This method is for user to paint on the paint board.
     * @param message is the user input messages
     * @param jwbg is the gui paint board
     */
    public static synchronized void userShowGraph(String message, JoinWhiteBoardGUI jwbg) {

        String[] messages = message.split("&");
        String shape = messages[0];
        String colorStr = messages[2];
        int x1 = Integer.parseInt(messages[3]);
        int y1 = Integer.parseInt(messages[4]);
        int x2 = Integer.parseInt(messages[5]);
        int y2 = Integer.parseInt(messages[6]);
        String text = "";
        if(message.contains("text")) {
            text = messages[7];
        }
        Color color = JoinWhiteBoardUtil.COLOR_HASH_MAP.get(colorStr);
        Graphics graphics = jwbg.getBoardPanel().getGraphics();
        graphics.setColor(color);

        switch(shape) {
            case "line":
                graphics.drawLine(x1, y1, x2, y2);
                break;
            case "circle":
                //4 parameters
                int xci = Math.min(x1, x2);
                int yci = Math.min(y1, y2);
                int widthCi = Math.abs(x1 - x2);
                int heightCi = Math.abs(y1 - y2);
                graphics.drawArc(xci, yci, Math.max(heightCi, widthCi), Math.max(heightCi, widthCi), 0,360);
                break;
            case "oval":
                //4 parameters
                int xo = Math.min(x1, x2);
                int yo = Math.min(y1, y2);
                int widthO = Math.abs(x1 - x2);
                int heightO = Math.abs(y1 - y2);
                graphics.drawOval(xo, yo, widthO, heightO);
                break;
            case "triangle":
                int[] xs = {x1, x1 - 1, x2};
                int[] ys = {x2, y1, y2};
                graphics.drawPolygon(xs, ys, JoinWhiteBoardUtil.TRIANGLE);
                break;
            case "rectangle":
                //4 parameters
                int xre = Math.min(x1, x2);
                int yre = Math.min(y1, y2);
                int widthRe = Math.abs(x1 - x2);
                int heightRe = Math.abs(y1 - y2);
                graphics.drawRect(xre, yre, widthRe, heightRe);
                break;
            case "text":
                int xte = Math.min(x1, x2);
                int yte = Math.min(y1, y2);
                graphics.drawString(text, xte, yte);
                break;
        }
    }

    /**
     * This method is to show the incoming messgae on the notification board.
     * @param message is the incoming message
     * @param jwbg is the gui
     */
    private void showMsg(String message, JoinWhiteBoardGUI jwbg) {
        String[] messages = message.split("&");
        String speaker = messages[1];
        String words = messages[2];
        jwbg.showInfo(JoinWhiteBoardUtil.myDateFormat());
        if(speaker.equals("Manager")) {
            jwbg.showInfo(speaker + " said: ");
        } else {
            jwbg.showInfo("User: " + speaker + " said: ");
        }
        jwbg.showInfo(words);
    }
}
