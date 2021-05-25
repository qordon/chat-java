package main.Database;

import main.Connection.Message;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class SQLService {
    private static final String URL_CONNECTION_DATABASE = "jdbc:sqlite:src/main/Database/usersDatabase.db";
    private static final String DRIVER = "org.sqlite.JDBC";
    private static SQLService instance;
    private static Connection connection;

    private static PreparedStatement preparedStatementGetNicknameByLoginAndPassword;
    private static PreparedStatement preparedStatementGetNickname;
    private static PreparedStatement preparedStatementRegistration;
    private static PreparedStatement preparedStatementSaveInformation;

    private static boolean isConnected = false;

    private SQLService() {
        try {
            loadDriver();
            loadConnection();

            prepareAllStatements();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static boolean isConnected() {
        return isConnected;
    }

    public static SQLService getInstance() {
        if (instance == null) {
            synchronized (SQLService.class) {
                if (instance == null) {
                    instance = new SQLService();
                    isConnected = true;
                }
            }
        }
        return instance;
    }

    private static void loadConnection() throws SQLException {
        connection = DriverManager.getConnection(URL_CONNECTION_DATABASE);
    }

    private static void loadDriver() throws ClassNotFoundException {
        Class.forName(DRIVER);
    }

    private static void prepareAllStatements() throws SQLException {
        preparedStatementGetNicknameByLoginAndPassword = connection.prepareStatement("SELECT nickname FROM users WHERE nickname = ? AND password = ?;");
        preparedStatementGetNickname = connection.prepareStatement("SELECT nickname FROM users WHERE nickname = ?");
        preparedStatementRegistration = connection.prepareStatement("INSERT INTO users (nickname, password) VALUES (?, ?);");
        preparedStatementSaveInformation = connection.prepareStatement("INSERT INTO messages (from_nick, to_nick, message, time) VALUES (?, ?, ?, ?);");
    }

    public static String getNickname(String nickname) throws SQLException {
        String nick = null;
        preparedStatementGetNickname.setString(1, nickname);
        ResultSet resultSet = preparedStatementGetNickname.executeQuery();
        if (resultSet.next()) {
            nick = resultSet.getString(1);
        }
        resultSet.close();
        return nick;
    }

    public static String getNicknameByLoginAndPassword(String login, String password) throws SQLException {
        String nick = null;
        preparedStatementGetNicknameByLoginAndPassword.setString(1, login);
        preparedStatementGetNicknameByLoginAndPassword.setString(2, password);
        ResultSet rs = preparedStatementGetNicknameByLoginAndPassword.executeQuery();
        if (rs.next()) {
            nick = rs.getString(1);
        }
        rs.close();
        return nick;
    }

    public static boolean registration(String nickname, String password) throws SQLException {
        preparedStatementRegistration.setString(1, nickname);
        preparedStatementRegistration.setString(2, password);
        preparedStatementRegistration.executeUpdate();
        return true;
    }


    public static void savingUserMessages(Message message) throws SQLException {
        preparedStatementSaveInformation.setString(1, message.getFrom());
        preparedStatementSaveInformation.setString(2, message.getTo());
        preparedStatementSaveInformation.setString(3, message.getTextMessage());
        preparedStatementSaveInformation.setString(4, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        preparedStatementSaveInformation.executeUpdate();
    }

    public static void closeConnection() throws SQLException {
        preparedStatementRegistration.close();
        preparedStatementGetNicknameByLoginAndPassword.close();
        connection.close();
        connection = null;
    }
}
