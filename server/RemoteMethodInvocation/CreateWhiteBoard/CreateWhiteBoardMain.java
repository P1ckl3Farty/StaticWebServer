import sun.security.util.ArrayUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the main class
 */
public class CreateWhiteBoardMain {
    //java CreateWhiteBoard <serverIPAddress> <serverPort> username
    private static int port;
    private static String username;
    private static String serverIP;
    private static int rmiPort;
    public static ConcurrentHashMap<Integer, String> userMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Socket> socketsMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, BufferedWriter> outsMap = new ConcurrentHashMap<>();
    public static List<String> graphHistorys = new CopyOnWriteArrayList<>();
    public static List<String> open = new CopyOnWriteArrayList<>();
    private static ServerSocket serverSocket;
    public static int serialId = 0;
    private static Drawing drawing;

    public static void main(String[] args) {
        setup(args);
        rmiVerify(rmiPort);
        CreateWhiteBoardGUI createWhiteBoardGUI = new CreateWhiteBoardGUI();
        kickUser(createWhiteBoardGUI);
        sendMsg(createWhiteBoardGUI);
        menuActions(createWhiteBoardGUI);
        paint(createWhiteBoardGUI);
        drawing = new Drawing(createWhiteBoardGUI);
        drawing.start();
        listen(createWhiteBoardGUI);
        sendMsg(createWhiteBoardGUI);
    }

    /**
     * handle arguments
     * @param args are input arguments from cmd
     */
    public static void setup(String[] args) {
        try {
            checkServerSetUp(args);
        } catch (WrongPortNumberException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        } catch (WrongInitServerException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        rmiPort = port - 1;
        username = args[1].trim();

    }

    /**
     * Helper function of setup
     * @param args arguments
     * @throws WrongPortNumberException enter wrong port numbers
     * @throws WrongInitServerException enter less or more arguments than expected
     */
    private static void checkServerSetUp(String[] args)
            throws WrongPortNumberException, WrongInitServerException {
        if(!legalArgNum(args.length)) {
            throw new WrongInitServerException();
        }
        try{
            port = Integer.parseInt(args[1].replaceAll(" ", ""));
            serverIP = args[0].replaceAll(" ", "");
        } catch (NumberFormatException e) {
            throw new WrongPortNumberException();
        }
        if(!legalPort(port)) {
            throw new WrongPortNumberException();
        }
    }
    private static boolean legalArgNum(int args) {
        return args == ManagerUtil.SERVER_ARGS;
    }
    private static boolean legalPort(int port) {
        return !(port < ManagerUtil.PORT_LOWER_BOUND || port > ManagerUtil.PORT_HIGHER_BOUND);
    }

    /**
     * rmi registry
     * @param rmiPort is the port number of rmi
     */
    public static void rmiVerify(int rmiPort) {
        try {
            BRemoteCall bRemoteCall = new BRemoteCall();
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registry.rebind("get", bRemoteCall);
        } catch (RemoteException e) {
            System.out.println("Error: RMI port has been occupied or the server port has been occupied.");
            System.exit(0);
        }
    }

    /**
     * Start the server to listen if there is any client connect in
     * Per client per thread
     * @param createWhiteBoardGUI is the gui of the application
     */
    public static void listen(CreateWhiteBoardGUI createWhiteBoardGUI) {
        try {
            serverSocket = new ServerSocket(port);
            while(true) {
                Socket client = serverSocket.accept();
                socketsMap.put(serialId, client);
                String currentUser = userMap.get(serialId);
                createWhiteBoardGUI.showUserList("" + serialId, currentUser);
                ServerThread serverThread = new ServerThread(client, createWhiteBoardGUI, serialId, currentUser);
                serverThread.start();
            }
        } catch (IOException e) {
            System.out.println("Error: The I/O stream is down, please try it later.");
            System.exit(0);
        }
    }

    /**
     * This method is for the manager to paint by mouse
     * @param cwbg is the GUI
     */
    public static void paint(CreateWhiteBoardGUI cwbg) {
        cwbg.getBoardPanel().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (cwbg.getShape() == null) {
                    JOptionPane.showMessageDialog(null, "Warning: You must choose a shape." );
                }
                cwbg.setPresslocations(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    cwbg.setRelealocations(e.getX(), e.getY());
                    String shape = cwbg.getShape();
                    Color color = ManagerUtil.COLOR_HASH_MAP.get(cwbg.getCurrentColor());
                    cwbg.setGraph(cwbg.getBoardPanel().getGraphics());
                    cwbg.getGraph().setColor(color);
                    switch (shape) {
                        case "line":
                            cwbg.getGraph().drawLine(cwbg.getPresslocations()[0], cwbg.getPresslocations()[1],
                                    cwbg.getRelealocations()[0], cwbg.getRelealocations()[1]);
                            synMessage(shape, cwbg);
                            break;
                        case "circle":
                            //4 parameters
                            int xci = Math.min(cwbg.getPresslocations()[0], cwbg.getRelealocations()[0]);
                            int yci = Math.min(cwbg.getPresslocations()[1], cwbg.getRelealocations()[1]);
                            int widthCi = Math.abs(cwbg.getRelealocations()[0] - cwbg.getPresslocations()[0]);
                            int heightCi = Math.abs(cwbg.getRelealocations()[1] - cwbg.getPresslocations()[1]);
                            cwbg.getGraph().drawArc(xci, yci, Math.max(heightCi, widthCi), Math.max(heightCi, widthCi), 0,360);
                            synMessage(shape, cwbg);
                            break;
                        case "oval":
                            //4 parameters
                            int xo = Math.min(cwbg.getPresslocations()[0], cwbg.getRelealocations()[0]);
                            int yo = Math.min(cwbg.getPresslocations()[1], cwbg.getRelealocations()[1]);
                            int widthO = Math.abs(cwbg.getRelealocations()[0] - cwbg.getPresslocations()[0]);
                            int heightO = Math.abs(cwbg.getRelealocations()[1] - cwbg.getPresslocations()[1]);
                            cwbg.getGraph().drawOval(xo, yo,widthO,heightO);
                            synMessage(shape, cwbg);
                            break;
                        case "triangle":
                            int[] xs = {cwbg.getPresslocations()[0], cwbg.getPresslocations()[0] - 1, cwbg.getRelealocations()[0]};
                            int[] ys = {cwbg.getRelealocations()[0], cwbg.getPresslocations()[1], cwbg.getRelealocations()[1]};
                            cwbg.getGraph().drawPolygon(xs, ys, ManagerUtil.TRIANGLE);
                            synMessage(shape, cwbg);
                            break;
                        case "rectangle":
                            //4 parameters
                            int xre = Math.min(cwbg.getPresslocations()[0], cwbg.getRelealocations()[0]);
                            int yre = Math.min(cwbg.getPresslocations()[1], cwbg.getRelealocations()[1]);
                            int widthRe = Math.abs(cwbg.getRelealocations()[0] - cwbg.getPresslocations()[0]);
                            int heightRe = Math.abs(cwbg.getRelealocations()[1] - cwbg.getPresslocations()[1]);
                            cwbg.getGraph().drawRect(xre, yre, widthRe, heightRe);
                            synMessage(shape, cwbg);
                            break;
                        case "text":
                            //5 parameters
                            cwbg.setTexture(cwbg.getTextField().trim());
                            if(cwbg.getTexture().equals("") || cwbg.getTexture().contains("&")) {
                                JOptionPane.showMessageDialog(null,"You must enter something non-empty without '&'");
                                cwbg.clearTextField();
                            } else {
                                int xte = Math.min(cwbg.getPresslocations()[0], cwbg.getRelealocations()[0]);
                                int yte = Math.min(cwbg.getPresslocations()[1], cwbg.getRelealocations()[1]);
                                cwbg.getGraph().drawString(cwbg.getTexture(), xte, yte);
                                synMessage(shape, cwbg);
                            }
                            break;
                    }
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
     * This method is for user to paint on the paint board.
     * @param message is the user input messages
     * @param createWhiteBoardGUI is the gui paint board
     */
    public static void userShowGraph(String message, CreateWhiteBoardGUI createWhiteBoardGUI) {
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
        Color color = ManagerUtil.COLOR_HASH_MAP.get(colorStr);
        Graphics graphics = createWhiteBoardGUI.getBoardPanel().getGraphics();
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
                graphics.drawPolygon(xs, ys, ManagerUtil.TRIANGLE);
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
     * It is for sending a message to clients after the manager send a message or paint by hand
     * @param cwbg is the server's gui
     */
    public static void sendMsg(CreateWhiteBoardGUI cwbg) {

        cwbg.getSendButton().addActionListener(e -> {
            String text = cwbg.getInputField().getText().replaceAll("\n"," ").trim();
            if(outsMap.size() == 0) {
                JOptionPane.showMessageDialog(null,
                        "There is no user online! You cannot send anything.");
                cwbg.getInputField().setText("");
            } else {
                if("".equals(text) || text.contains("&")) {
                    JOptionPane.showMessageDialog(null,
                            "You must enter something non-empty without '&'.");
                    cwbg.getInputField().setText("");
                } else {
                    cwbg.showInfo(ManagerUtil.myDateFormat());
                    cwbg.showInfo("You: \n" + text);
                    synMessage("message", cwbg);
                    cwbg.getInputField().setText("");
                }
            }
        });
    }

    /**
     * It is for modify message format for communication
     * @param operation is the first argument. a string as a shape, or send a message or actions from the menu
     * @param cwbg is the server's gui
     */
    public static void synMessage(String operation, CreateWhiteBoardGUI cwbg) {
        String synMsg = "";
        if("message".equals(operation)) {
            synMsg = operation + "&" + "Manager" + "&" + cwbg.getInputField().getText().replaceAll("\n"," ").trim();
        } else if("text".equals(operation)){
            int x1 = cwbg.getPresslocations()[0];
            int y1 = cwbg.getPresslocations()[1];
            int x2 = cwbg.getRelealocations()[0];
            int y2 = cwbg.getRelealocations()[1];
            String colorStr = cwbg.getCurrentColor();
            synMsg = operation + "&" + "Manager" + "&" + colorStr + "&" + x1 + "&" + y1 + "&" + x2 + "&" + y2 + "&" + cwbg.getTexture();
        } else {
            int x1 = cwbg.getPresslocations()[0];
            int y1 = cwbg.getPresslocations()[1];
            int x2 = cwbg.getRelealocations()[0];
            int y2 = cwbg.getRelealocations()[1];
            String colorStr = cwbg.getCurrentColor();
            synMsg = operation + "&" + "Manager" + "&" + colorStr + "&" + x1 + "&" + y1 + "&" + x2 + "&" + y2;
        }
        if(!synMsg.contains("message")) {
            graphHistorys.add(synMsg +"&" + "history");
        }
        if(outsMap.size() != 0) {
            Integer id = -1;
            for(Map.Entry entry : outsMap.entrySet()) {
                try {
                    id = (Integer) entry.getKey();
                    BufferedWriter out = (BufferedWriter) entry.getValue();
                    if(synMsg.contains("message") && synMsg.contains("history")) {
                        continue;
                    }
                    out.write(synMsg + "\n");
                    out.flush();
                } catch (IOException ex) {
                    try {
                        outsMap.get(id).close();
                        outsMap.remove(id);
                        socketsMap.get(id).close();
                        socketsMap.remove(id);
                        userMap.remove(id);
                    } catch (IOException exc) {
                        System.out.println("synMessage wrong error");
                    }
                }
            }
        }
    }

    /**
     * Is the kick user function
     * @param cwbg
     */
    public static void kickUser(CreateWhiteBoardGUI cwbg) {
        cwbg.getKickOutButton().addActionListener(e -> {
            if(userMap.size() == 0) {
                JOptionPane.showMessageDialog(null,"No user is online.");
                return;
            }
            String kickIDStr = " ";
            Integer kickID = -1;
            try{
                kickIDStr = JOptionPane.showInputDialog("Please enter the one's ID you want to kick-out").trim();
                kickID = Integer.parseInt(kickIDStr);
            } catch (NullPointerException ne){
                return;
            }
            catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null,"Warning: Invalid input format! Please check on the user list");
                return;
            }
            if(!userMap.containsKey(kickID)) {
                JOptionPane.showMessageDialog(null,"Warning: Invalid Input! We don't have this user online.");
                return;
            } else {
                try {
                    outsMap.get(kickID).write("exit\n");
                    outsMap.get(kickID).flush();
                    outsMap.get(kickID).close();
                    outsMap.remove(kickID);
                    socketsMap.get(kickID).close();
                    socketsMap.remove(kickID);
                    userMap.remove(kickID);
                    if(userMap.size() == 0) {
                        cwbg.getUsersTextField().setText("");
                    } else {
                        cwbg.getUsersTextField().setText("");
                        for(Map.Entry entry : userMap.entrySet()) {
                            Integer userID = (Integer) entry.getKey();
                            cwbg.showUserList("" + userID, (String)entry.getValue());
                        }
                    }

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null,"Warning: The user has logout before you kick");
                }
            }
        });
    }

    /**
     * Is for menu actions including new, open, save, saveAs and close
     * @param createWhiteBoardGUI is the gui
     */
    public static synchronized void menuActions(CreateWhiteBoardGUI createWhiteBoardGUI) {
        //Open action
        createWhiteBoardGUI.getOpen().addActionListener(e -> {
            String fileName = "";
            try{
                fileName = JOptionPane.showInputDialog("The file name: ").trim();
            } catch (NullPointerException npe) {
                if(!fileName.isEmpty()) {
                    JOptionPane.showMessageDialog(null,"Failed to open the file!\n" +
                            "Please check the name and try it later!");
                }
                return;
            }

            if(!fileName.equals("")) {
                try{
                    FileInputStream fileInputStream = new FileInputStream(fileName);
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    open = (CopyOnWriteArrayList<String>) objectInputStream.readObject();
                    objectInputStream.close();
                    fileInputStream.close();
                } catch (IOException | ClassNotFoundException exception) {
                    JOptionPane.showMessageDialog(null,"Failed to open the file!\n" +
                            "Please check the name and try it later!");
                    open = null;
                    return;
                }
                drawing.interrupt();
                createWhiteBoardGUI.getGraph().setColor(Color.WHITE);
                createWhiteBoardGUI.getGraph().fillRect(0,0, ManagerUtil.BOARD_PANEL_WIDTH,ManagerUtil.BOARD_PANEL_HEIGHT);
                try{
                    graphHistorys = open;
                    if(outsMap.size() != 0) {
                        notifyAll("open\n");
                    }
                    Iterator<String> i2 = graphHistorys.iterator();
                    while (i2.hasNext()) {
                        sleep(50);
                        String str = i2.next();
                        userShowGraph(str, createWhiteBoardGUI);
                        if(outsMap.size() != 0) {
                            notifyAll(str);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    drawing = new Drawing(createWhiteBoardGUI);
                    drawing.start();
                }
            } else {
                JOptionPane.showMessageDialog(null,"Failed to open the file!\n" +
                        "Please check the name and try it later!");
            }
        });

        //new action
        createWhiteBoardGUI.getNewOne().addActionListener(e -> {
            if(graphHistorys.size() == 0) {
                JOptionPane.showMessageDialog(null,"Failed: the board is clean");
                return;
            }
            createWhiteBoardGUI.getGraph().setColor(Color.WHITE);
            createWhiteBoardGUI.getGraph().fillRect(0,0, ManagerUtil.BOARD_PANEL_WIDTH,ManagerUtil.BOARD_PANEL_HEIGHT);
            createWhiteBoardGUI.resetGui();
            drawing.interrupt();

            if(userMap.size() == 0) {
            } else {
                notifyAll("new\n");
            }
            graphHistorys = new CopyOnWriteArrayList<>();
            createWhiteBoardGUI.getGraph().setColor(Color.WHITE);
            createWhiteBoardGUI.getGraph().fillRect(0,0, ManagerUtil.BOARD_PANEL_WIDTH,ManagerUtil.BOARD_PANEL_HEIGHT);
            drawing = new Drawing(createWhiteBoardGUI);
            drawing.start();
        });
        //save action
        createWhiteBoardGUI.getSave().addActionListener(e -> {
            if(graphHistorys.size() == 0) {
                JOptionPane.showMessageDialog(null,"Warning: the board is clean");
                return;
            }
            String fileName = "Record";
            try{
                if(!fileName.equals("")) {
                    JOptionPane.showMessageDialog(null,"saving...");
                    File file = new File(fileName);
                    if(!file.exists()) {
                        file.createNewFile();
                    }
                    drawing.interrupt();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeObject(graphHistorys);
                    objectOutputStream.flush();
                    objectOutputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    JOptionPane.showMessageDialog(null,"Success, The file Record has been successfully saved!");
                }
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(null,"Warning: The board has not been saved successfully.");
                return;
            } finally {
                drawing = new Drawing(createWhiteBoardGUI);
                drawing.start();
            }
        });
        //saveAs action
        createWhiteBoardGUI.getSaveAs().addActionListener(e -> {
            if(graphHistorys.size() == 0) {
                JOptionPane.showMessageDialog(null,"Warning: the board is clean");
                return;
            }
            int choice = JOptionPane.showOptionDialog(null,"Pick your desired saving format. Note: the picture " +
                    "can only be opened by yourself in directory","Select save file format:",JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,null,ManagerUtil.option, ManagerUtil.option[0]);
            if(choice == 0) {
                BufferedImage image = new BufferedImage(ManagerUtil.BOARD_PANEL_WIDTH, ManagerUtil.BOARD_PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
                String fileName = "";
                try{
                    fileName = JOptionPane.showInputDialog("The file name: ").trim();
                    if(fileName.equals("")) {
                        fileName = " ";
                        throw new NullPointerException();
                    }
                } catch (NullPointerException npe) {
                    if(fileName.equals(" ")) {
                        JOptionPane.showMessageDialog(null,"Failed to save the file!\n" +
                                "Please check the name and try it later!");
                    }
                    return;
                }

                if(fileName.contains(".")) {
                    fileName = fileName.replaceAll(".","");
                }
                File file = new File(fileName + ".png");
                JOptionPane.showMessageDialog(null,"saving...");
                try{
                    drawing.interrupt();
                    FileOutputStream fos = new FileOutputStream(file);
                    image.createGraphics();
                    Graphics graphics = image.getGraphics();
                    graphics.fillRect(0,0, ManagerUtil.BOARD_PANEL_WIDTH,ManagerUtil.BOARD_PANEL_HEIGHT);
                    Iterator ite = graphHistorys.iterator();
                    while(ite.hasNext()) {
                        String str = ite.next().toString();
                        sleep(100);
                        drawByGraphics(str, graphics);
                    }

                    javax.imageio.ImageIO.write(image, "PNG", fos);
                    fos.close();
                    JOptionPane.showMessageDialog(null,"Success, The file has been successfully saved!");
                } catch (Exception exxx){
                    JOptionPane.showMessageDialog(null,"Warning: The board has not been saved successfully.");
                }  finally {
                    drawing = new Drawing(createWhiteBoardGUI);
                    drawing.start();
                }
            } else if(choice == 1) {
                String fileName = "";
                try{
                    fileName = JOptionPane.showInputDialog("The file name: ").trim();
                    if(fileName.equals("")) {
                        fileName = " ";
                        throw new NullPointerException();
                    }
                } catch (NullPointerException npe) {
                    if(fileName.equals(" ")) {
                        JOptionPane.showMessageDialog(null,"Failed to save the file!\n" +
                                "Please check the name and try it later!");
                    }
                    return;
                }
                try{
                    if(!fileName.equals("")) {
                        File file = new File(fileName);
                        if(!file.exists()) {
                            file.createNewFile();
                        }
                        drawing.interrupt();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(graphHistorys);
                        objectOutputStream.flush();
                        objectOutputStream.close();
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        JOptionPane.showMessageDialog(null,"Success, The file has been successfully saved!");

                    }
                } catch (IOException ioException) {
                    JOptionPane.showMessageDialog(null,"Warning: The board has not been saved successfully.");
                } finally {
                    drawing = new Drawing(createWhiteBoardGUI);
                    drawing.start();
                }
            }
        });
        //close action
        createWhiteBoardGUI.getClose().addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(null,
                    "Do you want to close the app?","EXIT", JOptionPane.YES_NO_OPTION);
            if(option == 0) {
                notifyAll("exit\n");
                JOptionPane.showMessageDialog(null,"Goodbye!");
                System.exit(0);
            }
        });

    }


    /**
     * This function is to notify all online clients.
     * @param str is the message wanted to be sent to all clients
     */
    public static void notifyAll(String str) {
        Integer id = -1;
        for(Map.Entry entry : outsMap.entrySet()) {
            try {
                id = (Integer) entry.getKey();
                BufferedWriter out = (BufferedWriter) entry.getValue();
                out.write(str + "\n");
                out.flush();
            } catch (IOException ex) {
                try {
                    outsMap.get(id).close();
                    outsMap.remove(id);
                    socketsMap.get(id).close();
                    socketsMap.remove(id);
                    userMap.remove(id);
                } catch (IOException exc) {
                }
            }
        }
    }

    /**
     * This method is to handle input client's messages including drawing command and message command
     * @param message is the client's message
     * @param graphics is the graphic of the gui boardPanel
     */
    public static synchronized void drawByGraphics(String message, Graphics graphics) {
        String[] messages = message.split("&");
        if(messages.length < 2) {
            return;
        }
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
        Color color = ManagerUtil.COLOR_HASH_MAP.get(colorStr);
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
                graphics.drawPolygon(xs, ys, ManagerUtil.TRIANGLE);
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
     * This method is to promise painting not disappear.
     * @param createWhiteBoardGUI is the gui
     * @throws InterruptedException is ignored.
     */
    public synchronized static void drawings(CreateWhiteBoardGUI createWhiteBoardGUI) throws InterruptedException {
        while(true) {
            sleep(100);
            if(graphHistorys.size() != 0) {
                Iterator<String> itera = graphHistorys.iterator();
                while (itera.hasNext()) {
                    sleep(20);
                    String e = itera.next();
                    if(e.contains("history")) {
                        drawByGraphics(e, createWhiteBoardGUI.getGraph());
                    }
                }
            }
        }
    }
}
