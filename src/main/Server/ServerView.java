package main.Server;

import main.Database.SQLService;
import main.Config.Config;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ServerView extends JFrame {

    private final ServerController server;

    private JButton buttonStartServer;
    private JButton buttonStopServer;
    private JScrollPane scrollPanel;
    private JTextArea textAreaLog;

    public ServerView(ServerController server) {
        this.server = server;

        SQLService.getInstance();

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {

        }
    }

    protected void initComponents() {
        buttonStartServer = new JButton();
        buttonStopServer = new JButton();
        scrollPanel = new JScrollPane();
        textAreaLog = new JTextArea();

        setTitle(Config.SERVER_TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(Config.SERVER_SIZE_WIDTH, Config.SERVER_SIZE_HEIGHT));
        setPreferredSize(new java.awt.Dimension(Config.SERVER_SIZE_WIDTH, Config.SERVER_SIZE_HEIGHT));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.stopServer();
                System.exit(0);
            }
        });
        setLocationRelativeTo(null);

        try {
            setIconImage(ImageIO.read(new File(Config.SERVER_ICON_IMAGE)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonStartServer.setIcon(new ImageIcon(Config.IMAGE_ICON_START_SERVER));
        buttonStartServer.setText("Start server");
        buttonStartServer.addActionListener(e -> {
            server.startServer(getPortFromOptionPane());
            if (server.isServerStart()) {
                buttonStartServer.setEnabled(false);
                buttonStopServer.setEnabled(true);
            }
        });

        buttonStopServer.setIcon(new ImageIcon(Config.IMAGE_ICON_STOP_SERVER));
        buttonStopServer.setText("Stop server");
        buttonStopServer.setEnabled(false);
        buttonStopServer.addActionListener(e -> {
            server.stopServer();
            if (!server.isServerStart()) {
                buttonStartServer.setEnabled(true);
                buttonStopServer.setEnabled(false);
            }
        });

        textAreaLog.setEditable(false);
        textAreaLog.setColumns(Config.TEXT_AREA_LOG_SERVER_COLUMNS);
        textAreaLog.setRows(Config.TEXT_AREA_LOG_SERVER_ROWS);
        scrollPanel.setViewportView(textAreaLog);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(buttonStartServer, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                                                .addGap(0, 0, 0)
                                                .addComponent(buttonStopServer, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                                                .addGap(0, 0, 0))
                                        .addComponent(scrollPanel))
                                .addGap(5, 5, 5))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(scrollPanel, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonStartServer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buttonStopServer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(5, 5, 5))
        );

        pack();
        setVisible(true);
    }

    public void refreshDialogWindowServer(String serviceMessage) {
        textAreaLog.append(serviceMessage);
    }


    protected int getPortFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(this, "Enter the server port:", "Server port input", JOptionPane.QUESTION_MESSAGE);
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Incorrect server port entered. Try again.", "Server port input error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
