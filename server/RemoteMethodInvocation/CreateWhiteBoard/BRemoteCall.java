

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the exact class of the rmi registry of the project
 */
public class BRemoteCall extends UnicastRemoteObject implements BRemote{
    public BRemoteCall() throws RemoteException {
    }

    @Override
    public Boolean remoteRequest(String name) throws RemoteException {
        Boolean ans = false;
        int option = JOptionPane.showConfirmDialog(null,"User: " +
                name + " want to join the white board", "New Request", JOptionPane.YES_NO_OPTION);
        if(option == 0) {
            if(CreateWhiteBoardMain.userMap.contains(name)) {
                ans = false;
                JOptionPane.showMessageDialog(null,"This user name is duplicated, the request is " +
                        "automatically refused.");
            } else {
                ans = true;
                CreateWhiteBoardMain.serialId++;
                CreateWhiteBoardMain.userMap.put(CreateWhiteBoardMain.serialId,name);
            }
        }
        return ans;
    }
}
