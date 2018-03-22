package ru.dravn.dropbox.Server;

import java.io.File;
import java.sql.*;

public class AuthService {
    private static Connection connect;
    private static Statement stmt;
    private static String folder =  "C:\\_serv\\";

    public static void connect() throws ClassNotFoundException, SQLException
    {
        Class.forName("org.sqlite.JDBC");
        connect = DriverManager.getConnection("jdbc:sqlite:main.db");
        CreateDB();
        WriteDB();
    }

    public static void CreateDB() throws ClassNotFoundException, SQLException
    {

        stmt = connect.createStatement();
        stmt.execute("DROP TABLE IF EXISTS USERS");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " +
                "USERS " +
                "(ID INTEGER  PRIMARY KEY AUTOINCREMENT," +
                " LOGIN           TEXT    NOT NULL, " +
                " PASS            TEXT     NOT NULL, " +
                " FOLDER          TEXT  NOT NULL)");

        System.out.println("Таблица создана или уже существует.");
    }

    public static void WriteDB() throws SQLException
    {
        stmt.execute("INSERT INTO USERS (LOGIN, PASS, FOLDER) " +
                "VALUES ('log1', 'pass1', 'C:\\_serv\\log1'); ");
        stmt.execute("INSERT INTO USERS (LOGIN, PASS, FOLDER) " +
                " VALUES ('log2', 'pass2','C:\\_serv\\log2'); ");
        stmt.execute("INSERT INTO USERS (LOGIN, PASS, FOLDER) " +
                " VALUES ('log3', 'pass3','C:\\_serv\\log3'); ");

        //System.out.println("Таблица заполнена");
    }


    public static boolean login(String login, String pass){
        try {
            ResultSet rs = stmt.executeQuery("SELECT PASS FROM USERS WHERE LOGIN ='" + login+"'");
            if (rs.next()){
                return rs.getString("PASS").equals(pass);
            }
            else return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean registration(String login, String pass) {
        try
        {
            if(checkLogin(login))
            {
                stmt.execute("INSERT INTO USERS (LOGIN, PASS, FOLDER) " +
                        "VALUES ('" + login + "', '" + pass + "','" + folder + login + "'); ");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean checkLogin(String login) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT LOGIN FROM USERS WHERE  LOGIN ='" + login+"'");
        return !rs.next();
    }

    public static void disconnect() {
        try {
            stmt.close();
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getFolder(String login) {
        try
        {
            ResultSet rs = stmt.executeQuery("SELECT FOLDER FROM USERS WHERE LOGIN ='" + login+"'");
            if (rs.next())
            {
                return rs.getString("FOLDER");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
