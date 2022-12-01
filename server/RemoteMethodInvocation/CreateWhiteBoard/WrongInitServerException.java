/**
 * @author Boyang Zhang 1184097
 * @email boyazhang1@student.unimelb.edu.au
 * @version 1.0
 */
public class WrongInitServerException extends Exception {
    public WrongInitServerException() {
        super("Error: Invalid initiation of the server!" + "\nYou should enter three parameters.");
    }
}
