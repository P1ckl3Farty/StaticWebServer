/**
 * @author Boyang Zhang 1184097
 * @email boyazhang1@student.unimelb.edu.au
 * @version 1.0
 */
public class WrongPortNumberException extends Exception{
    public WrongPortNumberException() {
        super("Error: Invalid initiation of the server!" + "\nYour second parameter should be an integer " +
                "larger than 1024 and smaller than 65535.");
    }
}
