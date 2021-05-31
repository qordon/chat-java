package main.Client;

import main.Authorization.Login;
import main.Authorization.Registration;
import main.Database.SQLService;
import main.Config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class ClientView extends JFrame {

    private final ClientController client;

    private JButton buttonSignIn;
    private JButton buttonSignOut;
    private JButton buttonRegistration;
    private JButton buttonSend;
    private JList<String> listUserOnline;
    private JScrollPane scrollPanelForChatLog;
    private JScrollPane scrollPanelForUserListOnline;
    private JTextArea textAreaChatLog;
    private JTextField textFieldUserInputMessage;

    public ClientView(ClientController clientController) {
        this.client = clientController;

        SQLService.getInstance();

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    protected void initComponents() {
        buttonSend = new JButton();
        textFieldUserInputMessage = new JTextField();
        scrollPanelForUserListOnline = new JScrollPane();
        listUserOnline = new JList<>();
        buttonSignIn = new JButton();
        scrollPanelForChatLog = new JScrollPane();
        textAreaChatLog = new JTextArea();
        buttonRegistration = new JButton();
        buttonSignOut = new JButton();

        setTitle(Config.CLIENT_TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(Config.CLIENT_SIZE_WIDTH, Config.CLIENT_SIZE_HEIGHT));
        setPreferredSize(new java.awt.Dimension(Config.CLIENT_SIZE_WIDTH, Config.CLIENT_SIZE_HEIGHT));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isClientConnected()) {
                    client.disableClient();
                }
                try {
                    if (SQLService.isConnected()) {
                        SQLService.closeConnection();
                    }
                } catch (SQLException sqlException) {
                    errorDialogWindow(sqlException.getMessage());
                }
                System.exit(0);
            }
        });
        setLocationRelativeTo(null);

        buttonSend.setText("Send");
        buttonSend.setToolTipText("Send message");
        buttonSend.setEnabled(false);
        buttonSend.addActionListener(e -> {
            if (!textFieldUserInputMessage.getText().equals("")) {
                    if (listUserOnline.isSelectedIndex(listUserOnline.getSelectedIndex())) {
                        client.sendPrivateMessageOnServer(listUserOnline.getSelectedValue(), textFieldUserInputMessage.getText());
                    } else {
                        errorDialogWindow("Please select a user from the list, otherwise you will not be able to send a private message");
                    }
                textFieldUserInputMessage.setText("");
            }
        });

        textFieldUserInputMessage.setToolTipText("Input message");
        textFieldUserInputMessage.setEnabled(false);
        textFieldUserInputMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!textFieldUserInputMessage.getText().equals("") && e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (listUserOnline.isSelectedIndex(listUserOnline.getSelectedIndex())) {

                            client.sendPrivateMessageOnServer(listUserOnline.getSelectedValue(), textFieldUserInputMessage.getText());
                        } else {
                            errorDialogWindow("Please select a user from the list, otherwise you will not be able to send a private message");
                        }
                    textFieldUserInputMessage.setText("");
                }
            }
        });

        listUserOnline.setToolTipText("User list online");
        listUserOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPanelForUserListOnline.setViewportView(listUserOnline);
        listUserOnline.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JList list = (JList)e.getSource();
                System.out.println(list.getSelectedValue());
                client.getHistory((String)list.getSelectedValue());
            }
        });

        textAreaChatLog.setEditable(false);
        textAreaChatLog.setColumns(Config.TEXT_AREA_CHAT_LOG_COLUMNS);
        textAreaChatLog.setRows(Config.TEXT_AREA_CHAT_LOG_ROWS);
        textAreaChatLog.setToolTipText("Chat");
        textAreaChatLog.setFont(new Font(Config.TEXT_AREA_CHAT_LOG_FONT_NAME, Font.PLAIN, Config.TEXT_AREA_CHAT_LOG_FONT_SIZE));
        scrollPanelForChatLog.setViewportView(textAreaChatLog);

        buttonRegistration.setText("Registration");
        buttonRegistration.setToolTipText("Registration");
        buttonRegistration.addActionListener(e -> {
            if (!client.isDatabaseConnected()) {
                Registration registration = new Registration(this);
                registration.setVisible(true);
                if (registration.isSucceeded()) {
                    client.setNickname(registration.getNickname());
                }
            }
        });

        buttonSignIn.setText("Log in");
        buttonSignIn.setToolTipText("Log in");
        buttonSignIn.setEnabled(true);
        buttonSignIn.addActionListener(e -> {
            if (!client.isDatabaseConnected()) {
                Login loginDialog = new Login(this);
                loginDialog.setVisible(true);
                if (loginDialog.isSucceeded()) {
                    client.setNickname(loginDialog.getNickname());
                    client.setDatabaseConnected(true);

                    client.connectClient();
                    if (client.isClientConnected()) {
                        buttonSignOut.setEnabled(true);
                        buttonSignIn.setEnabled(false);
                        textFieldUserInputMessage.setEnabled(true);
                        buttonSend.setEnabled(true);
                        buttonRegistration.setEnabled(false);
                    }
                }
            }
        });

        buttonSignOut.setText("Log out");
        buttonSignOut.setToolTipText("Log out");
        buttonSignOut.setEnabled(false);
        buttonSignOut.addActionListener(e -> {
            if (client.isClientConnected()) {
                client.disableClient();
                buttonSignIn.setEnabled(true);
                buttonSignOut.setEnabled(false);
                textFieldUserInputMessage.setEnabled(false);
                buttonSend.setEnabled(false);
                if (client.isDatabaseConnected()) {
                    client.setDatabaseConnected(false);
                    buttonRegistration.setEnabled(true);
                }
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(buttonSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(textFieldUserInputMessage)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()

                                                .addGap(5, 5, 5)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(buttonSignOut, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(buttonSignIn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(buttonRegistration, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(scrollPanelForUserListOnline, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))
                                                .addComponent(scrollPanelForChatLog)))
                                .addGap(5, 5, 5))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(buttonSignIn, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonSignOut)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonRegistration)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(scrollPanelForUserListOnline, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                                        .addComponent(scrollPanelForChatLog))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldUserInputMessage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(buttonSend)
                                .addGap(5, 5, 5))
        );
        pack();
        setVisible(true);
    }

    protected void addMessage(String text) {
        textAreaChatLog.append("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + text);
    }

    protected void addHistory(String text){
        textAreaChatLog.setText(null);
        textAreaChatLog.append(text);
    }

    protected void refreshListUsers(Set<String> allUserNicknames) {
        StringBuilder text = new StringBuilder();
        for (String user : allUserNicknames) {
            text.append(user).append("\n");
        }
        String[] strings = text.toString().split("\n");
        listUserOnline.setModel(new AbstractListModel<>() {
            public int getSize() {
                return allUserNicknames.size();
            }

            public String getElementAt(int i) {
                return strings[i];
            }
        });
    }

    protected String getServerAddress() {
        return Config.HOST;
    }

    protected int getPort() {
        return Config.PORT;
    }

    protected void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void disableClient() {
        setTitle(Config.CLIENT_TITLE);
        textAreaChatLog.setText("");
    }
}

