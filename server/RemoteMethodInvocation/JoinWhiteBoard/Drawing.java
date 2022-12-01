/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the to make sure painting from disappearing
 */
public class Drawing extends Thread{
    private JoinWhiteBoardGUI joinWhiteBoardGUI;

    public Drawing(JoinWhiteBoardGUI joinWhiteBoardGUI) {
        this.joinWhiteBoardGUI = joinWhiteBoardGUI;
    }

    public Drawing() {
    }

    @Override
    public void run() {
        try {
            JoinWhiteBoardMain.drawings(joinWhiteBoardGUI);
        } catch (InterruptedException e) {
            //ignore
        }
    }
}
