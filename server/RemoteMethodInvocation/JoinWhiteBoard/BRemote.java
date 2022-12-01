import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the rmi interface
 */
public interface BRemote extends Remote {
    public Boolean remoteRequest(String name) throws RemoteException;
}
