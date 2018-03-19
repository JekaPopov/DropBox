package ru.dravn.dropbox.Server;

import java.sql.*;

public class AuthService {
    private static Connection connect;
    private static Statement stmt;




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
        //stmt.execute("DROP TABLE IF EXISTS users");
        stmt.execute("CREATE TABLE if not exists 'users' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                " login text, pass text, folder text);");

        System.out.println("Таблица создана или уже существует.");
    }

    public static void WriteDB() throws SQLException
    {
        stmt.execute("INSERT INTO users (login, pass, folder) " +
                "VALUES ('log1', 'pass1', 'C:\\serv\\log1'); ");
        /*stmt.execute("INSERT INTO 'users' ('login', 'pass','folder')" +
                " VALUES ('log2', 'pass2','C:\\serv\\log2'); ");
        stmt.execute("INSERT INTO 'users' ('login', 'pass','folder')" +
                " VALUES ('log3', 'pass3','C:\\serv\\log3'); ");*/

        System.out.println("Таблица заполнена");
    }

    public void disconnect() {
        try {
            stmt.close();
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    public boolean login(String login, String pass){
        try {
            ResultSet rs = stmt.executeQuery("SELECT 'pass' FROM 'users' WHERE 'login' = " + login);
            if (rs.next()){
                System.out.println(rs.getString("pass"));
                return rs.getString("pass").equals(pass);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
