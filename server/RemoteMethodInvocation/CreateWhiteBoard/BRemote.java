import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the interface of the rmi of the project
 */
public interface BRemote extends Remote {
    public Boolean remoteRequest(String name) throws RemoteException;
}
