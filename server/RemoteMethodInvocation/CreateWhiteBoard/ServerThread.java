import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the customised thread class of the project
 * each client socket will have one.
 */
public class ServerThread extends Thread {
    private Socket socket;
    private CreateWhiteBoardGUI createWhiteBoardGUI;
    private String clientName;
    private BufferedReader bufferedReader;
    private BufferedWriter out;
    private Integer clientID;

    public ServerThread(Socket socket, CreateWhiteBoardGUI createWhiteBoardGUI,Integer clientID, String clientName) {
        this.socket = socket;
        this.createWhiteBoardGUI = createWhiteBoardGUI;
        this.clientID = clientID;
        this.clientName = clientName;
    }

    @Override
    public void run(){

        InputStream byteIn = null;
        try {
            byteIn = socket.getInputStream();
        } catch (IOException e) {
            try {
                disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        InputStreamReader charIn = new InputStreamReader(byteIn, StandardCharsets.UTF_8);
        this.bufferedReader = new BufferedReader(charIn);

        OutputStream byteOut = null;
        try {
            byteOut = socket.getOutputStream();
        } catch (IOException e) {
            try {
                closeAll();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        OutputStreamWriter charOut = new OutputStreamWriter(byteOut, StandardCharsets.UTF_8);
        this.out = new BufferedWriter(charOut);
        CreateWhiteBoardMain.outsMap.put(this.clientID, this.out);
        for(String graphHistory : CreateWhiteBoardMain.graphHistorys) {
            try {
                this.out.write(graphHistory + "\n");
                this.out.flush();
            } catch (IOException e) {
                try {
                    disconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        String message;
        while (true){
            try {
                while (((message = bufferedReader.readLine()) != null)){
                    if(message.contains("message")) {
                        synShowMsg(message, createWhiteBoardGUI);
                    } else {
                        CreateWhiteBoardMain.graphHistorys.add(message +"&" + "history");;
                        synShowGraph(message, createWhiteBoardGUI);
                    }
                }
            } catch (IOException e) {
                try {
                    disconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }
            }
        }
    }

    /**
     * show logout on notification board
     */
//    private synchronized void showLogout(){
//        this.createWhiteBoardGUI.showInfo(ManagerUtil.myDateFormat());
//        this.createWhiteBoardGUI.showInfo("The user: " + this.clientName + " has disconnected.");
//    }

    /**
     * show the graph when received a passed graph
     * @param message is from client
     * @param createWhiteBoardGUI is the gui of the project
     */
    private synchronized void synShowGraph(String message, CreateWhiteBoardGUI createWhiteBoardGUI) {
        CreateWhiteBoardMain.userShowGraph(message, createWhiteBoardGUI);
        sendToAll(message);
    }

    /**
     * When IOException happens, close offline users
     * @throws IOException is thrown when a bufferedwriter is closes.
     */
    private void disconnect() throws IOException {
        if(bufferedReader != null & !socket.isClosed()){
            bufferedReader.close();
            closeAll();
        }
        for(BufferedWriter out : CreateWhiteBoardMain.outsMap.values()) {
            if(out != null & !socket.isClosed()) {
                out.flush();
                out.close();
            }
        }
        if(this.socket != null & !socket.isClosed()){
            closeAll();
        }

    }

    /**
     * Close a clients' socket, in and output stream
     * @throws IOException when some of them are closed before this function finally executed
     */
    private void closeAll() throws IOException {
//        showLogout();
        int ID = this.clientID;
        String name = this.clientName;

        CreateWhiteBoardMain.outsMap.get(ID).flush();
        CreateWhiteBoardMain.outsMap.get(ID).close();
        CreateWhiteBoardMain.outsMap.remove(ID);

        this.bufferedReader.close();

        CreateWhiteBoardMain.socketsMap.get(ID).close();
        CreateWhiteBoardMain.socketsMap.remove(ID);

        CreateWhiteBoardMain.userMap.remove(ID, name);
        createWhiteBoardGUI.clearUserList();
        for(Map.Entry entry : CreateWhiteBoardMain.userMap.entrySet()) {
            Integer userID = (Integer) entry.getKey();
            this.createWhiteBoardGUI.showUserList("" + userID, (String)entry.getValue());
        }
        Thread.currentThread().interrupt();
    }

    /**
     * to show the message on notification board
     * @param message is clients' input stream
     * @param createWhiteBoardGUI is the gui of the project
     */
    private synchronized void synShowMsg(String message, CreateWhiteBoardGUI createWhiteBoardGUI) {
        showMsg(message, createWhiteBoardGUI);
        sendToAll(message);
    }

    /**
     * send all messages and draw commands to other clients
     * @param message is the message waited for sending
     */
    private void sendToAll(String message) {
        for(Map.Entry entry : CreateWhiteBoardMain.outsMap.entrySet()) {
            if(!entry.getKey().equals(this.clientID)) {
                BufferedWriter currentOut = (BufferedWriter) entry.getValue();
                try {
                    currentOut.write(message + "\n");
                    currentOut.flush();
                } catch (IOException e) {
                    System.out.println("sentToAll is wrong");
                }
            }
        }
    }

    /**
     * this function is for show message on notification board
     * @param message is the messages needed to be shown on notification board
     * @param createWhiteBoardGUI is the gui of the app
     */
    private void showMsg(String message, CreateWhiteBoardGUI createWhiteBoardGUI) {
        String[] messages = message.split("&");
        String speaker = messages[1];
        String words = messages[2];
        createWhiteBoardGUI.showInfo(ManagerUtil.myDateFormat());
        createWhiteBoardGUI.showInfo("User: " + speaker + " said: ");
        createWhiteBoardGUI.showInfo(words);
    }

}
