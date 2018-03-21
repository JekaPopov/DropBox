package ru.dravn.dropbox.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;


public class Server {

    private Vector<ClientHandler> clients;

    public Server()
    {

        try (ServerSocket serverSocket = new ServerSocket(8189))
        {
            clients = new Vector<>();
            AuthService.connect();
            System.out.println("Server started... Waiting clients...");

            while(true)
            {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected" + socket.getInetAddress() + " " + socket.getPort() + " " + socket.getLocalPort());
                new ClientHandler(this, socket);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (SQLException | ClassNotFoundException e)
        {
            System.out.println(e);
            System.out.println("Не удалось запустить сервис авторизации");
        }
        finally
        {
            AuthService.disconnect();
        }
    }


    public void subscribe(ClientHandler clientHandler)
    {
        clients.add(clientHandler);
    }

    public void unSubscribe(ClientHandler clientHandler)
    {
        clients.remove(clientHandler);
    }

}
