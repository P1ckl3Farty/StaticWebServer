import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the client gui
 */
public class JoinWhiteBoardGUI {

    //Main area
    private JFrame jFrame;
    //Main container
    private JPanel mainPanel;
    //White board container
    private JPanel boardPanel = new JPanel();
    //Notification Board
    private JTextArea notificationField = new JTextArea();
    //Input Board
    private JTextArea inputField  = new JTextArea();
    //Text shape
    private JTextArea textField = new JTextArea();

    //default shape is line
    private String shape;
    private String texture = "";
    private String currentColor = "black";

    //For painting, location
    public int[] presslocations = new int[2];
    public int[] relealocations = new int[2];

    private JButton send = new JButton("send");
    //Shape picker
    private ButtonGroup shapeGroup = new ButtonGroup();
    private JRadioButton lineButton;
    private JRadioButton circleButton;
    private JRadioButton ovalButton;
    private JRadioButton triButton;
    private JRadioButton rectangleButton;
    private JRadioButton textButton;


    private JPanel colorPanel;
    private JButton[] colorButtons = new JButton[JoinWhiteBoardUtil.COLOR_NUMBER];


    //shape picker container default color is black
    private Box shapeBox = Box.createHorizontalBox();

    private JPanel colorShowBoard;

    private Graphics graphics;

    public JoinWhiteBoardGUI() {
        jFrame = new JFrame("Distributed WhiteBoard User");
        jFrame.setSize(JoinWhiteBoardUtil.FRAME_WIDTH, JoinWhiteBoardUtil.FRAME_HEIGHT);
        jFrame.setResizable(false);

        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        setMainPanel();
        jFrame.add(mainPanel);

        jFrame.setVisible(true);
    }

    /**
     * set board panel
     */
    private void setBoardPanel() {
        boardPanel = new JPanel();
        boardPanel.setBounds(JoinWhiteBoardUtil.BOARD_PANEL_X,JoinWhiteBoardUtil.BOARD_PANEL_Y,
                JoinWhiteBoardUtil.BOARD_PANEL_WIDTH, JoinWhiteBoardUtil.BOARD_PANEL_HEIGHT);
        boardPanel.setBackground(Color.WHITE);

        JLabel boardLabel = new JLabel("White Board");
        boardLabel.setBounds(JoinWhiteBoardUtil.BOARD_LABEL_X,JoinWhiteBoardUtil.BOARD_LABEL_Y,
                JoinWhiteBoardUtil.BOARD_LABEL_WIDTH, JoinWhiteBoardUtil.BOARD_LABEL_HEIGHT);

        mainPanel.add(boardLabel);
    }

    /**
     * set notification area
     * @param notificationField is the notification area
     */
    private void setNotification(JTextArea notificationField) {

        this.notificationField = notificationField;
        notificationField.setEditable(false);
        notificationField.setCaretColor(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(notificationField);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


        scrollPane.setBounds(JoinWhiteBoardUtil.NOTIFICATION_PANEL_X ,JoinWhiteBoardUtil.NOTIFICATION_PANEL_Y,
                JoinWhiteBoardUtil.NOTIFICATION_PANEL_WIDTH,JoinWhiteBoardUtil.NOTIFICATION_PANEL_HEIGHT);
        JLabel notificationLabel = new JLabel("Notification Board");
        notificationLabel.setBounds(JoinWhiteBoardUtil.NOTIFICATION_LABEL_X ,JoinWhiteBoardUtil.NOTIFICATION_LABEL_Y,
                JoinWhiteBoardUtil.NOTIFICATION_LABEL_WIDTH,JoinWhiteBoardUtil.NOTIFICATION_LABEL_HEIGHT);

        mainPanel.add(scrollPane);
        mainPanel.add(notificationLabel);
    }

    /**
     * set typeIn area
     * @param inputField is the typeIn area
     */
    private void setTypeIn(JTextArea inputField) {
        this.inputField = inputField;
        JScrollPane scrollPane = new JScrollPane(inputField);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.setBounds(JoinWhiteBoardUtil.INPUT_FIELD_PANEL_X,JoinWhiteBoardUtil.INPUT_FIELD_PANEL_Y,
                JoinWhiteBoardUtil.INPUT_FIELD_PANEL_WIDTH,JoinWhiteBoardUtil.INPUT_FIELD_PANEL_HEIGHT);
        JLabel textLabel = new JLabel("Send Message Board");
        textLabel.setBounds(JoinWhiteBoardUtil.TEXT_LABEL_X,JoinWhiteBoardUtil.TEXT_LABEL_Y,
                JoinWhiteBoardUtil.TEXT_LABEL_WIDTH,JoinWhiteBoardUtil.TEXT_LABEL_HEIGHT);

        send.setBounds(JoinWhiteBoardUtil.SEND_BUTTON_X,JoinWhiteBoardUtil.SEND_BUTTON_Y,
                JoinWhiteBoardUtil.SEND_BUTTON_WIDTH,JoinWhiteBoardUtil.SEND_BUTTON_HEIGHT);

        mainPanel.add(scrollPane);
        mainPanel.add(textLabel);
        mainPanel.add(send);
    }

    /**
     * This function add
     */
    private void addColorGroup() {
        colorPanel = new JPanel();
        colorPanel.setBounds(JoinWhiteBoardUtil.COLOR_BUTTON_X,JoinWhiteBoardUtil.COLOR_BUTTON_Y,
                JoinWhiteBoardUtil.COLOR_BUTTON_WIDTH,JoinWhiteBoardUtil.COLOR_BUTTON_HEIGHT);
        for(int i = 0; i < JoinWhiteBoardUtil.COLOR_NUMBER; i++) {
            colorButtons[i] = new JButton();
            colorButtons[i].setActionCommand(JoinWhiteBoardUtil.COLOR_NAME[i]);
            colorButtons[i].setBackground(JoinWhiteBoardUtil.COLORS[i]);
            colorButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentColor = e.getActionCommand();
                }
            });
            colorButtons[0].setSelected(true);
            colorPanel.add(colorButtons[i]);
            mainPanel.add(colorPanel);
        }
    }

    /**
     * To set area to set text shape
     * @param textShape is the input area for text
     */
    private void setTextShape(JTextArea textShape) {
        this.textField = textShape;
        JLabel textShapeLabel = new JLabel("Type text if choosing text shape:");
        textShapeLabel.setBounds(JoinWhiteBoardUtil.TEXT_SHAPE_LABEL_X,JoinWhiteBoardUtil.TEXT_SHAPE_LABEL_Y,
                JoinWhiteBoardUtil.TEXT_SHAPE_LABEL_WIDTH,JoinWhiteBoardUtil.TEXT_SHAPE_LABEL_HEIGHT);

        textShape.setVisible(true);
        textShape.setEditable(false);
        textShape.setBounds(JoinWhiteBoardUtil.TEXT_SHAPE_X,JoinWhiteBoardUtil.TEXT_SHAPE_Y,
                JoinWhiteBoardUtil.TEXT_SHAPE_WIDTH,JoinWhiteBoardUtil.TEXT_SHAPE_HEIGHT);

        mainPanel.add(textShape);
        mainPanel.add(textShapeLabel);
    }

    public String getTextField() {
        return textField.getText();
    }

    public void setGraphics(Graphics graphics) {
        this.graphics = graphics;
    }

    public Graphics getGraph() {
        return graphics;
    }

    /**
     * main panel contains all needed panels
     */
    private void setMainPanel() {
        mainPanel = new JPanel(null);
        setBoardPanel();
        setNotification(notificationField);
        mainPanel.add(boardPanel);
        setTypeIn(inputField);
        setTextShape(textField);
        addActionGroup();
        addColorGroup();
    }


    /**
     * This function is to setup action button group
     */
    private void addActionGroup() {
        shapeBox.setBounds(JoinWhiteBoardUtil.SHAPE_BOX_X,JoinWhiteBoardUtil.SHAPE_BOX_Y,
                JoinWhiteBoardUtil.SHAPE_BOX_WIDTH,JoinWhiteBoardUtil.SHAPE_BOX_HEIGHT);

        lineButton = new JRadioButton("Line");
        shapeBox.add(lineButton);
        shapeGroup.add(lineButton);
        lineButton.setActionCommand("line");
        lineButton.addActionListener(e -> {
            textField.setEditable(false);
            textField.setText("");
            if(lineButton.isSelected()) {
                this.shape = lineButton.getActionCommand();
            }
        });
        
        circleButton = new JRadioButton("Circle");
        shapeBox.add(circleButton);
        shapeGroup.add(circleButton);
        circleButton.setActionCommand("circle");
        circleButton.addActionListener(e -> {
            textField.setEditable(false);
            textField.setText("");
            if(circleButton.isSelected()) {
                this.shape = circleButton.getActionCommand();
            }
        });

        ovalButton = new JRadioButton("Oval");
        shapeBox.add(ovalButton);
        shapeGroup.add(ovalButton);
        ovalButton.setActionCommand("oval");
        ovalButton.addActionListener(e -> {
            if(ovalButton.isSelected()) {
                textField.setEditable(false);
                textField.setText("");
                this.shape = ovalButton.getActionCommand();
            }
        });

        triButton = new JRadioButton("Triangle");
        shapeBox.add(triButton);
        shapeGroup.add(triButton);
        triButton.setActionCommand("triangle");
        triButton.addActionListener(e -> {
            textField.setEditable(false);
            textField.setText("");
            if(triButton.isSelected()) {
                this.shape = triButton.getActionCommand();
            }
        });

        rectangleButton = new JRadioButton("Rectangle");
        shapeBox.add(rectangleButton);
        shapeGroup.add(rectangleButton);
        rectangleButton.setActionCommand("rectangle");
        rectangleButton.addActionListener(e -> {
            textField.setEditable(false);
            textField.setText("");
            if(rectangleButton.isSelected()) {
                this.shape = rectangleButton.getActionCommand();
            }
        });

        textButton = new JRadioButton("Text");
        shapeBox.add(textButton);
        shapeGroup.add(textButton);
        textButton.setActionCommand("text");
        textButton.addActionListener(e -> {
            if(textButton.isSelected()) {
                textField.setEditable(true);
                this.shape = textButton.getActionCommand();
            }
        });
        
        mainPanel.add(shapeBox);
    }

    /**
     * Get the shape when user select a shape
     */
    public String getShape() {
        return this.shape;
    }


    public ButtonGroup getShapeGroup() {
        return shapeGroup;
    }

    public JPanel getBoardPanel() {
        return this.boardPanel;
    }


    public String getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(String currentColor) {
        this.currentColor = currentColor;
    }


    public void setShape(String shape) {
        this.shape = shape;
    }


    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public int[] getPresslocations() {
        return presslocations;
    }

    public void setPresslocations(int x, int y) {
        this.presslocations[0] = x;
        this.presslocations[1] = y;
    }

    public int[] getRelealocations() {
        return relealocations;
    }

    public void setRelealocations(int x, int y) {
        this.relealocations[0] = x;
        this.relealocations[1] = y;
    }

    public JButton getSend() {
        return send;
    }

    public JTextArea getInputField() {
        return inputField;
    }

    /**
     * show information on notification area
     * @param str is the desired notification area.
     */
    public void showInfo(String str) {
        notificationField.append(str + "\n");
    }
    public void clearTextField() {
        textField.setText("");
    }

    /**
     * reset the gui, reset the color, text.
     */
    public void resetGui() {
        getShapeGroup().clearSelection();
        setTexture("");
        getTField().setEditable(false);
        getTField().setText("");
        setShape(null);

        setCurrentColor("black");
        colorButtons[0].setSelected(true);

    }

    public JFrame getjFrame() {
        return jFrame;
    }

    public JTextArea getTField() {
        return textField;
    }

}
