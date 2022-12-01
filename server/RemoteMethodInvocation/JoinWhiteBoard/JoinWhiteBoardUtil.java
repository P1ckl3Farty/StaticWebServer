import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the utility class
 */
public class JoinWhiteBoardUtil {
    public final static int SERVER_ARGS = 3;
    public final static int PORT_LOWER_BOUND = 1024;
    public final static int PORT_HIGHER_BOUND = 65535;


    public final static int FRAME_WIDTH = 860;
    public final static int FRAME_HEIGHT = 600;

    public static final int COLOR_NUMBER = 16;
    public final static Color BLACK = Color.BLACK;
    public final static Color RED = Color.RED;
    public final static Color GREEN = Color.GREEN;
    public final static Color GRAY = Color.GRAY;
    public final static Color MAGENTA = Color.MAGENTA;
    public final static Color CYAN = Color.CYAN;
    public final static Color ORANGE = Color.ORANGE;
    public final static Color PINK = Color.PINK;
    public final static Color YELLOW = Color.YELLOW;
    public final static Color BLUE = Color.BLUE;
    public final static Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public final static Color TOMATO = new Color(255,99,71);
    public final static Color BISQUE = new Color(255,228,196);
    public final static Color PURPLE = new Color(128,0,128);
    public final static Color MAROON = new Color(128,0,0);
    public final static Color OLIVE = new Color(128,128,0);

    public final static int BOARD_PANEL_X = 30;
    public final static int BOARD_PANEL_Y = 20;
    public final static int BOARD_PANEL_WIDTH = 430;
    public final static int BOARD_PANEL_HEIGHT = 490;
    public final static int BOARD_LABEL_X = 200;
    public final static int BOARD_LABEL_Y = 510;
    public final static int BOARD_LABEL_WIDTH = 200;
    public final static int BOARD_LABEL_HEIGHT = 20;

    public final static int COLOR_BUTTON_X = 110;
    public final static int COLOR_BUTTON_Y = 540;
    public final static int COLOR_BUTTON_WIDTH = 640;
    public final static int COLOR_BUTTON_HEIGHT = 20;

    public final static int NOTIFICATION_PANEL_X = 470;
    public final static int NOTIFICATION_PANEL_Y = 110;
    public final static int NOTIFICATION_PANEL_WIDTH = 350;
    public final static int NOTIFICATION_PANEL_HEIGHT = 230;
    public final static int NOTIFICATION_LABEL_X = 470;
    public final static int NOTIFICATION_LABEL_Y = 85;
    public final static int NOTIFICATION_LABEL_WIDTH = 200;
    public final static int NOTIFICATION_LABEL_HEIGHT = 20;

    public final static int INPUT_FIELD_PANEL_X = 470;
    public final static int INPUT_FIELD_PANEL_Y = 360;
    public final static int INPUT_FIELD_PANEL_WIDTH = 350;
    public final static int INPUT_FIELD_PANEL_HEIGHT = 150;
    public final static int TEXT_LABEL_X = 470;
    public final static int TEXT_LABEL_Y = 335;
    public final static int TEXT_LABEL_WIDTH = 200;
    public final static int TEXT_LABEL_HEIGHT = 20;

    public final static int SEND_BUTTON_X = 605;
    public final static int SEND_BUTTON_Y = 510;
    public final static int SEND_BUTTON_WIDTH = 70;
    public final static int SEND_BUTTON_HEIGHT = 20;

    public final static int TEXT_SHAPE_X = 470;
    public final static int TEXT_SHAPE_Y = 55;
    public final static int TEXT_SHAPE_WIDTH = 350;
    public final static int TEXT_SHAPE_HEIGHT = 20;
    public final static int TEXT_SHAPE_LABEL_X = 470;
    public final static int TEXT_SHAPE_LABEL_Y = 35;
    public final static int TEXT_SHAPE_LABEL_WIDTH = 200;
    public final static int TEXT_SHAPE_LABEL_HEIGHT = 20;


    public final static int SHAPE_BOX_X = 470;
    public final static int SHAPE_BOX_Y = 10;
    public final static int SHAPE_BOX_WIDTH = 400;
    public final static int SHAPE_BOX_HEIGHT = 20;

    public static int TRIANGLE = 3;
    public final static Color[] COLORS = new Color[]{BLACK, RED, GREEN,
            GRAY, MAGENTA, CYAN, ORANGE, PINK, YELLOW,BLUE,LIGHT_GRAY, TOMATO,BISQUE, PURPLE, MAROON, OLIVE};
    public final static String[] COLOR_NAME = new String[]{"black", "red", "green",
            "gray", "magenta", "cyan", "orange", "pink", "yellow","blue","light gray", "tomato","bisque", "purple", "maroon","olive"};
    public final static HashMap<String, Color> COLOR_HASH_MAP = new HashMap(){{
        for(int i = 0; i <= COLOR_NUMBER - 1; i++) {
            put(COLOR_NAME[i], COLORS[i]);
        }
    }};

    /**
     * To setup the date format of the project
     * @return a string contains data format
     */
    public static String myDateFormat() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dataFormat.format(new Date());
        return time;
    }

    /**
     * This method is to trigger the system's quit
     */
    public static void serverQuitDialog() {
        JOptionPane.showMessageDialog(null,
                "Sorry, " +
                        "you've been removed or the server has been closed.\nThe app will be closed.");
        System.exit(0);
    }
}
