
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

/**
 * @author Boyang Zhang boyazhang1@student.unimelb.edu.au
 * @version 1.0
 * This class is the gui
 */
public class CreateWhiteBoardGUI {
    //Main area
    private JFrame jFrame;
    //Main container
    private JPanel mainPanel;
    //White board container
    private JPanel boardPanel;
    //Notification Board
    private JTextArea notificationField = new JTextArea();
    //Input Board
    private JTextArea inputField  = new JTextArea();
    //Text shape
    private JTextArea usersTextField = new JTextArea();
    private JButton kickOutButton = new JButton("kick-out");
    private JButton sendButton  = new JButton("send");

    private JButton save = new JButton("save");
    private JButton saveAs = new JButton("saveAs");
    private JButton open = new JButton("open");
    private JButton close = new JButton("close");
    private JButton newOne = new JButton("new");
    private Graphics graph;

    //Shape picker
    //shape picker container default color is black
    private Box shapeBox = Box.createHorizontalBox();
    private ButtonGroup shapeGroup = new ButtonGroup();
    private JRadioButton lineButton;
    private JRadioButton circleButton;
    private JRadioButton ovalButton;
    private JRadioButton triButton;
    private JRadioButton rectangleButton;
    private JRadioButton textButton;

    private JPanel colorPanel;
    private JButton[] colorButtons = new JButton[ManagerUtil.COLOR_NUMBER];


    private JTextArea textField = new JTextArea();

    //For painting, location, shape, texture and color
    public int[] presslocations = new int[2];
    public int[] relealocations = new int[2];
    private String shape;
    private String texture = "";
    private String currentColor = "black";

    public CreateWhiteBoardGUI() {
        jFrame = new JFrame("Distributed WhiteBoard Manager");
        jFrame.setSize(ManagerUtil.FRAME_WIDTH, ManagerUtil.FRAME_HEIGHT);
        jFrame.setResizable(false);

        WindowListener exitListener = new WindowAdapter() {
            //Remind for closing
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are You Sure to Close the Server?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == JOptionPane.YES_NO_OPTION) {
                    System.exit(0);
                }
            }
        };
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowListener(exitListener);

        setMainPanel();
        jFrame.add(mainPanel);
        jFrame.setVisible(true);
        graph = boardPanel.getGraphics();
    }

    /**
     * Set the location of the whiteboard, including:
     * itself
     * its label
     */
    private void setBoardPanel() {
        boardPanel = new JPanel();
        boardPanel.setBounds(ManagerUtil.BOARD_PANEL_X,ManagerUtil.BOARD_PANEL_Y,
                ManagerUtil.BOARD_PANEL_WIDTH, ManagerUtil.BOARD_PANEL_HEIGHT);
        boardPanel.setBackground(Color.WHITE);

        JLabel boardLabel = new JLabel("White Board");
        boardLabel.setBounds(ManagerUtil.BOARD_LABEL_X,ManagerUtil.BOARD_LABEL_Y,
                ManagerUtil.BOARD_LABEL_WIDTH, ManagerUtil.BOARD_LABEL_HEIGHT);
        mainPanel.add(boardLabel);
        addColorGroup();
    }

    /**
     * Set the main panel to contain all sub panels
     */
    private void setMainPanel() {
        mainPanel = new JPanel(null);
        setBoardPanel();
        mainPanel.add(boardPanel);
        setNotification(notificationField);
        setTypeIn(inputField);
        setUsersTextField(usersTextField);
        setTextShape(textField);
        addActionGroup();
        addMenuActions();
    }

    /**
     * This method add color selector buttons to the GUI
     */
    private void addColorGroup() {
        colorPanel = new JPanel();
        colorPanel.setBounds(ManagerUtil.COLOR_BUTTON_X,ManagerUtil.COLOR_BUTTON_Y,
                ManagerUtil.COLOR_BUTTON_WIDTH,ManagerUtil.COLOR_BUTTON_HEIGHT);
        for(int i = 0; i < ManagerUtil.COLOR_NUMBER; i++) {
            colorButtons[i] = new JButton();
            colorButtons[i].setActionCommand(ManagerUtil.COLOR_NAME[i]);
            colorButtons[i].setBackground(ManagerUtil.COLORS[i]);
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


        scrollPane.setBounds(ManagerUtil.NOTIFICATION_PANEL_X ,ManagerUtil.NOTIFICATION_PANEL_Y,
                ManagerUtil.NOTIFICATION_PANEL_WIDTH,ManagerUtil.NOTIFICATION_PANEL_HEIGHT);
        JLabel notificationLabel = new JLabel("Notification Board");
        notificationLabel.setBounds(ManagerUtil.NOTIFICATION_LABEL_X ,ManagerUtil.NOTIFICATION_LABEL_Y,
                ManagerUtil.NOTIFICATION_LABEL_WIDTH,ManagerUtil.NOTIFICATION_LABEL_HEIGHT);

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

        scrollPane.setBounds(ManagerUtil.INPUT_FIELD_PANEL_X,ManagerUtil.INPUT_FIELD_PANEL_Y,
                ManagerUtil.INPUT_FIELD_PANEL_WIDTH,ManagerUtil.INPUT_FIELD_PANEL_HEIGHT);
        JLabel textLabel = new JLabel("Send Message Board");
        textLabel.setBounds(ManagerUtil.TEXT_LABEL_X,ManagerUtil.TEXT_LABEL_Y,
                ManagerUtil.TEXT_LABEL_WIDTH,ManagerUtil.TEXT_LABEL_HEIGHT);

        sendButton.setBounds(ManagerUtil.SEND_BUTTON_X,ManagerUtil.SEND_BUTTON_Y,
                ManagerUtil.SEND_BUTTON_WIDTH,ManagerUtil.SEND_BUTTON_HEIGHT);

        mainPanel.add(scrollPane);
        mainPanel.add(textLabel);
        mainPanel.add(sendButton);
    }

    /**
     * set the action group for user to choose
     */
    private void addActionGroup() {
        shapeBox.setBounds(ManagerUtil.SHAPE_BOX_X,ManagerUtil.SHAPE_BOX_Y,
                ManagerUtil.SHAPE_BOX_WIDTH,ManagerUtil.SHAPE_BOX_HEIGHT);
        //draw lines
        lineButton = new JRadioButton("Line");
        shapeBox.add(lineButton);
        shapeGroup.add(lineButton);
        lineButton.setActionCommand("line");
        lineButton.addActionListener(e -> {
            if(lineButton.isSelected()) {
                textField.setEditable(false);
                textField.setText("");
                this.shape = lineButton.getActionCommand();
            }
        });
        //draw Circle
        circleButton = new JRadioButton("Circle");
        shapeBox.add(circleButton);
        shapeGroup.add(circleButton);
        circleButton.setActionCommand("circle");
        circleButton.addActionListener(e -> {
            if(circleButton.isSelected()) {
                textField.setEditable(false);
                textField.setText("");
                this.shape = circleButton.getActionCommand();
            }
        });
        //draw oval
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
        //draw triangle
        triButton = new JRadioButton("Triangle");
        shapeBox.add(triButton);
        shapeGroup.add(triButton);
        triButton.setActionCommand("triangle");
        triButton.addActionListener(e -> {
            if(triButton.isSelected()) {
                textField.setEditable(false);
                textField.setText("");
                this.shape = triButton.getActionCommand();
            }
        });

        //draw Rectangle
        rectangleButton = new JRadioButton("Rectangle");
        shapeBox.add(rectangleButton);
        shapeGroup.add(rectangleButton);
        rectangleButton.setActionCommand("rectangle");
        rectangleButton.addActionListener(e -> {
            if(rectangleButton.isSelected()) {
                textField.setEditable(false);
                textField.setText("");
                this.shape = rectangleButton.getActionCommand();
            }
        });
        //draw Text
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
     * This method add action buttons to the gui
     */
    private void addMenuActions() {
        save.setBounds(ManagerUtil.SAVE_BUTTON_X,ManagerUtil.SAVE_BUTTON_Y,ManagerUtil.
                SAVE_BUTTON_WIDTH,ManagerUtil.SAVE_BUTTON_HEIGHT);
        mainPanel.add(save);
        saveAs.setBounds(ManagerUtil.SAVEAS_BUTTON_X,ManagerUtil.SAVEAS_BUTTON_Y,ManagerUtil.
                SAVEAS_BUTTON_WIDTH,ManagerUtil.SAVEAS_BUTTON_HEIGHT);
        mainPanel.add(saveAs);
        newOne.setBounds(ManagerUtil.NEWONE_BUTTON_X,ManagerUtil.NEWONE_BUTTON_Y,ManagerUtil.
                NEWONE_BUTTON_WIDTH,ManagerUtil.NEWONE_BUTTON_HEIGHT);
        mainPanel.add(newOne);
        close.setBounds(ManagerUtil.CLOSE_BUTTON_X,ManagerUtil.CLOSE_BUTTON_Y,ManagerUtil.
                CLOSE_BUTTON_WIDTH,ManagerUtil.CLOSE_BUTTON_HEIGHT);
        mainPanel.add(close);
        open.setBounds(ManagerUtil.OPEN_BUTTON_X,ManagerUtil.OPEN_BUTTON_Y,ManagerUtil.
                OPEN_BUTTON_WIDTH,ManagerUtil.OPEN_BUTTON_HEIGHT);
        mainPanel.add(open);
    }

    /**
     * Set the text field for manager to lookup current user list
     * @param usersTextField is the user list field
     */
    private void setUsersTextField(JTextArea usersTextField) {
        this.usersTextField = usersTextField;
        usersTextField.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(usersTextField);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.setBounds(ManagerUtil.USER_TEXTFIELD_X,ManagerUtil.USER_TEXTFIELD_Y,
                ManagerUtil.USER_TEXTFIELD_WIDTH,ManagerUtil.USER_TEXTFIELD_HEIGHT);
        JLabel textLabel = new JLabel("User list");
        textLabel.setBounds(ManagerUtil.USER_LABEL_X,ManagerUtil.USER_LABEL_Y,
                ManagerUtil.USER_LABEL_WIDTH,ManagerUtil.USER_LABEL_HEIGHT);

        kickOutButton.setBounds(ManagerUtil.KICK_BUTTON_X,ManagerUtil.KICK_BUTTON_Y,
                ManagerUtil.KICK_BUTTON_WIDTH,ManagerUtil.KICK_BUTTON_HEIGHT);

        mainPanel.add(scrollPane);
        mainPanel.add(textLabel);
        mainPanel.add(kickOutButton);
    }

    /**
     * Set the text field for user to input text for drawing if choosing text
     * @param textShape is the text field for textShape
     */
    private void setTextShape(JTextArea textShape) {
        this.textField = textShape;
        JLabel textShapeLabel = new JLabel("Type text if choosing text shape:");
        textShapeLabel.setBounds(ManagerUtil.TEXT_SHAPE_LABEL_X,ManagerUtil.TEXT_SHAPE_LABEL_Y,
                ManagerUtil.TEXT_SHAPE_LABEL_WIDTH,ManagerUtil.TEXT_SHAPE_LABEL_HEIGHT);

        textShape.setVisible(true);
        textShape.setEditable(false);
        textShape.setBounds(ManagerUtil.TEXT_SHAPE_X,ManagerUtil.TEXT_SHAPE_Y,
                ManagerUtil.TEXT_SHAPE_WIDTH,ManagerUtil.TEXT_SHAPE_HEIGHT);

        mainPanel.add(textShape);
        mainPanel.add(textShapeLabel);
    }

    /**
     * Show the user list
     * @param ID is the generated ID of users
     * @param name is the name of the users
     */
    public synchronized void showUserList(String ID, String name) {
        usersTextField.append("ID: " + ID + "   Name: " + name + "\n");
    }

    /**
     * This method return the shape String
     * @return shape in string
     */
    public String getShape() {
        for (Enumeration<AbstractButton> buttons = shapeGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            button.addActionListener(e -> {
                if (button.getActionCommand() == "text") {
                    textField.setEditable(true);
                } else {
                    textField.setEditable(false);
                    textField.setText("");
                }
                this.shape = button.getActionCommand();
            });
        }
        return shape;
    }

    public String getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(String currentColor) {
        this.currentColor = currentColor;
    }

    public void showInfo(String str) {
        notificationField.append(str + "\n");
    }
    public void clearTextField() {
        textField.setText("");
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

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public Graphics getGraph() {
        return this.graph;
    }

    public void setGraph(Graphics graph) {
        this.graph = graph;
    }

    public void clearUserList() {
        this.usersTextField.setText("");
    }
    public JPanel getBoardPanel() {
        return boardPanel;
    }

    public String getTextField() {
        return textField.getText();
    }

    public JTextArea getTField() {
        return textField;
    }

    public JTextArea getInputField() {
        return inputField;
    }
    public JButton getSendButton() {
        return this.sendButton;
    }

    public JButton getKickOutButton() {
        return this.kickOutButton;
    }

    public JTextArea getUsersTextField() {
        return usersTextField;
    }

    public JButton getSave() {
        return save;
    }

    public JButton getSaveAs() {
        return saveAs;
    }

    public JButton getOpen() {
        return open;
    }

    public JButton getClose() {
        return close;
    }

    public JButton getNewOne() {
        return newOne;
    }

    public ButtonGroup getShapeGroup() {
        return shapeGroup;
    }


    /**
     * reset the gui, reset the color, text.
     */
    public void resetGui() {
        getGraph().setColor(Color.WHITE);
        getGraph().fillRect(ManagerUtil.BOARD_PANEL_X,ManagerUtil.BOARD_PANEL_Y, ManagerUtil.BOARD_PANEL_WIDTH,ManagerUtil.BOARD_PANEL_HEIGHT);
        getShapeGroup().clearSelection();
        setTexture("");
        getTField().setEditable(false);
        getTField().setText("");
        setShape(null);
        setCurrentColor("black");
        colorButtons[0].setSelected(true);
    }



}
