import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the main class
 */
public class JoinWhiteBoardMain {
    private static int port;
    private static int rmiPort;
    private static String server;
    private static String username;
    private static BRemote remote;
    private static Socket client;
    private static BufferedWriter out;
    private static BufferedReader in;
    private static InputStream inputStream;
    public static List<String> graphHistorys = new CopyOnWriteArrayList<>();
    public static Drawing drawing;
    public static void main(String[] args) {
        JoinWhiteBoardMain.setup(args);

        JoinWhiteBoardMain.sendJoinRequest(rmiPort, server, username);

        JoinWhiteBoardGUI joinWhiteBoardGUI = new JoinWhiteBoardGUI();
        closeTheApp(joinWhiteBoardGUI);
        joinWhiteBoardGUI.showInfo("Welcome to use the distributed whiteboard, dear " + username);
        try {
            client = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("Error: The I/O stream is down, please try it later.");
            System.exit(0);
        }
        JoinWhiteBoardMain.openIO(client);
        JoinWhiteBoardMain.sendMsg(joinWhiteBoardGUI);
        JoinWhiteBoardMain.paint(joinWhiteBoardGUI);
        drawing = new Drawing(joinWhiteBoardGUI);
        drawing.start();

        ReceiveThread receiveThread = new ReceiveThread(in, username, joinWhiteBoardGUI);
        receiveThread.start();
    }

    /**
     * To setup server socket, rmi port and the username of the client
     * @param args is the arguments
     */
    private static void setup(String[] args) {
        try {
            checkServerSetUp(args);
            rmiPort = port - 1;
            server = args[0].trim();
            username = args[2].trim();
        } catch (NumberFormatException | WrongPortNumberException | WrongInitServerException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
     * To check whether the server port number is valid.
     * @param args is the input arguments
     * @throws WrongPortNumberException is to handle wrong port number
     * @throws WrongInitServerException is to handle wrong arguments number
     */
    private static void checkServerSetUp(String[] args)
            throws WrongPortNumberException, WrongInitServerException {
        if(!legalArgNum(args.length)) {
            throw new WrongInitServerException();
        }
        try{
            port = Integer.parseInt(args[1].replaceAll(" ", ""));
        } catch (NumberFormatException e) {
            throw new WrongPortNumberException();
        }
        if(!legalPort(port)) {
            throw new WrongPortNumberException();
        }
    }
    private static boolean legalArgNum(int args) {
        return args == JoinWhiteBoardUtil.SERVER_ARGS;
    }
    private static boolean legalPort(int port) {
        return !(port < JoinWhiteBoardUtil.PORT_LOWER_BOUND || port > JoinWhiteBoardUtil.PORT_HIGHER_BOUND);
    }

    /**
     * This function is to send the join request to the manager
     * @param rmiPort is the rmi port number
     * @param server is the server address
     * @param username is the client's username
     */
    private static void sendJoinRequest(int rmiPort, String server, String username) {
        try {
            Registry registry = LocateRegistry.getRegistry(server,rmiPort);
            remote = (BRemote) registry.lookup("get");
            Boolean ans = remote.remoteRequest(username);
            if(!ans) {
                System.out.println("Error: The manager refused your request. Please resubmit it later.");
                System.exit(0);
            } else {
                System.out.println("Welcome to use the distributed whiteboard, dear " + username);
            }
        } catch (RemoteException e) {
            System.out.println("Error: The RMI port is wrong or the server is not working.");
            System.exit(0);
        } catch (NotBoundException e) {
            System.out.println(e.getClass().toString() + "\n" + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * This function is to let the client to draw on the gui and send each operation to the server
     * @param jwbg is the gui
     */
    public static void paint(JoinWhiteBoardGUI jwbg) {
        jwbg.getBoardPanel().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (jwbg.getShape() == null) {
                    JOptionPane.showMessageDialog(null, "Warning: You must choose a shape." );
                }
                jwbg.setPresslocations(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                jwbg.setRelealocations(e.getX(), e.getY());
                String shape = jwbg.getShape();
                Color color = JoinWhiteBoardUtil.COLOR_HASH_MAP.get(jwbg.getCurrentColor());
                jwbg.setGraphics(jwbg.getBoardPanel().getGraphics());
                jwbg.getGraph().setColor(color);
                switch (shape) {
                    case "line":
                        jwbg.getGraph().drawLine(jwbg.getPresslocations()[0], jwbg.getPresslocations()[1],
                                jwbg.getRelealocations()[0], jwbg.getRelealocations()[1]);
                        synMessage(shape, jwbg);
                        break;
                    case "circle":
                        //4 parameters
                        int xci = Math.min(jwbg.getPresslocations()[0], jwbg.getRelealocations()[0]);
                        int yci = Math.min(jwbg.getPresslocations()[1], jwbg.getRelealocations()[1]);
                        int widthCi = Math.abs(jwbg.getRelealocations()[0] - jwbg.getPresslocations()[0]);
                        int heightCi = Math.abs(jwbg.getRelealocations()[1] - jwbg.getPresslocations()[1]);
                        jwbg.getGraph().drawArc(xci, yci, Math.max(heightCi, widthCi), Math.max(heightCi, widthCi), 0,360);
                        synMessage(shape, jwbg);
                        break;
                    case "oval":
                        //4 parameters
                        int xo = Math.min(jwbg.getPresslocations()[0], jwbg.getRelealocations()[0]);
                        int yo = Math.min(jwbg.getPresslocations()[1], jwbg.getRelealocations()[1]);
                        int widthO = Math.abs(jwbg.getRelealocations()[0] - jwbg.getPresslocations()[0]);
                        int heightO = Math.abs(jwbg.getRelealocations()[1] - jwbg.getPresslocations()[1]);
                        jwbg.getGraph().drawOval(xo, yo, widthO, heightO);
                        synMessage(shape, jwbg);
                        break;
                    case "triangle":
                        int[] xs = {jwbg.getPresslocations()[0], jwbg.getPresslocations()[0] - 1, jwbg.getRelealocations()[0]};
                        int[] ys = {jwbg.getRelealocations()[0], jwbg.getPresslocations()[1], jwbg.getRelealocations()[1]};
                        jwbg.getGraph().drawPolygon(xs, ys, JoinWhiteBoardUtil.TRIANGLE);
                        synMessage(shape, jwbg);
                        break;
                    case "rectangle":
                        //4 parameters
                        int xre = Math.min(jwbg.getPresslocations()[0], jwbg.getRelealocations()[0]);
                        int yre = Math.min(jwbg.getPresslocations()[1], jwbg.getRelealocations()[1]);
                        int widthRe = Math.abs(jwbg.getRelealocations()[0] - jwbg.getPresslocations()[0]);
                        int heightRe = Math.abs(jwbg.getRelealocations()[1] - jwbg.getPresslocations()[1]);
                        synMessage(shape, jwbg);
                        jwbg.getGraph().drawRect(xre, yre, widthRe, heightRe);
                        break;
                    case "text":
                        //5 parameters
                        jwbg.setTexture(jwbg.getTextField().trim());
                        if(jwbg.getTexture().equals("") || jwbg.getTexture().contains("&")) {
                            JOptionPane.showMessageDialog(null,"You must enter something non-empty without '&'");
                            jwbg.clearTextField();
                        } else {
                            int xte = Math.min(jwbg.getPresslocations()[0], jwbg.getRelealocations()[0]);
                            int yte = Math.min(jwbg.getPresslocations()[1], jwbg.getRelealocations()[1]);
                            jwbg.getGraph().drawString(jwbg.getTexture(), xte, yte);
                            synMessage(shape, jwbg);
                        }
                        break;
                }

            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

    }

    /**
     * This function is to send each operation to the server
     * @param jwbg is the gui
     */
    public static void sendMsg(JoinWhiteBoardGUI jwbg) {
        jwbg.getSend().addActionListener(e -> {
            String text = jwbg.getInputField().getText().replaceAll("\n"," ").trim();
            if("".equals(text) || text.contains("&")) {
                JOptionPane.showMessageDialog(null,
                        "You must enter something non-empty without '&'.");
                jwbg.getInputField().setText("");
            } else {
                jwbg.showInfo(JoinWhiteBoardUtil.myDateFormat());
                jwbg.showInfo("You: \n" + text);
                synMessage("message", jwbg);
                jwbg.getInputField().setText("");
            }
        });
    }

    /**
     * This function is to connect IO streams between the server and clients
     * @param client is the client's socket
     */
    public static void openIO(Socket client) {

        try {
            inputStream = client.getInputStream();
            InputStreamReader charIn = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            in = new BufferedReader(charIn);

            OutputStream byteOut = client.getOutputStream();
            OutputStreamWriter charOut = new OutputStreamWriter(byteOut, StandardCharsets.UTF_8);
            out = new BufferedWriter(charOut);
        } catch (IOException e) {
            JoinWhiteBoardUtil.serverQuitDialog();
        }
    }

    /**
     * This function is to encode the operation to
     * @param operation is the operation command
     * @param jwbg is the gui
     */
    public static void synMessage(String operation, JoinWhiteBoardGUI jwbg) {
        String synMsg = "";
        if("message".equals(operation)) {
            synMsg = operation + "&" + username + "&" + jwbg.getInputField().getText().replaceAll("\n"," ").trim();
        } else if("text".equals(operation)){
            int x1 = jwbg.getPresslocations()[0];
            int y1 = jwbg.getPresslocations()[1];
            int x2 = jwbg.getRelealocations()[0];
            int y2 = jwbg.getRelealocations()[1];
            String colorStr = jwbg.getCurrentColor();
            synMsg = operation + "&" + username + "&" + colorStr + "&" + x1 + "&" + y1 + "&" + x2 + "&" + y2 + "&" + jwbg.getTexture();
            graphHistorys.add(synMsg +"&" + "history");
        } else {
            int x1 = jwbg.getPresslocations()[0];
            int y1 = jwbg.getPresslocations()[1];
            int x2 = jwbg.getRelealocations()[0];
            int y2 = jwbg.getRelealocations()[1];
            String colorStr = jwbg.getCurrentColor();
            synMsg = operation + "&" + username + "&" + colorStr + "&" + x1 + "&" + y1 + "&" + x2 + "&" + y2;
            graphHistorys.add(synMsg +"&" + "history");
        }

        try {
            out.write(synMsg + "\n");
            out.flush();
        } catch (IOException e) {
            JoinWhiteBoardUtil.serverQuitDialog();
        }
    }

    /**
     * This function is to close the app by click quit icon of the gui
     * @param joinWhiteBoardGUI is the ui of the system
     */
    public static void closeTheApp(JoinWhiteBoardGUI joinWhiteBoardGUI) {
        joinWhiteBoardGUI.getjFrame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        WindowListener exitListener = new WindowAdapter() {
            //Remind for closing
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are You Sure to Close the app?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == JOptionPane.YES_NO_OPTION) {
                    System.exit(0);
                }
            }
        };
        joinWhiteBoardGUI.getjFrame().addWindowListener(exitListener);
    }

    /**
     * This method is to promise painting not disappear
     * @param joinWhiteBoardGUI is the gui
     * @throws InterruptedException is ignored
     */
    public synchronized static void drawings(JoinWhiteBoardGUI joinWhiteBoardGUI) throws InterruptedException {
        while(true) {
            sleep(100);
            if(graphHistorys.size() != 0) {
                Iterator<String> itera = graphHistorys.iterator();
                while(itera.hasNext()) {
                    sleep(20);
                    String element = itera.next();
                    if(element.equals("&history")) {
                    } else {
                        userShowGraph(element, joinWhiteBoardGUI);
                    }
                }
            }
        }
    }

    /**
     * This method is to show graph as a helper function for the drawings
     * @param message is the messages containing painting information
     * @param jwbg is the gui
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

}
