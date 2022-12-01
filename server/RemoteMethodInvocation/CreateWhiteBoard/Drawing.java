/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is to promise paint not disappear
 */
public class Drawing extends Thread{
    private CreateWhiteBoardGUI createWhiteBoardGUI;

    public Drawing(CreateWhiteBoardGUI createWhiteBoardGUI) {
        this.createWhiteBoardGUI = createWhiteBoardGUI;
    }

    @Override
    public void run() {
        try {
            CreateWhiteBoardMain.drawings(createWhiteBoardGUI);
        } catch (InterruptedException e) {
            //Ignore
        }
    }
}
