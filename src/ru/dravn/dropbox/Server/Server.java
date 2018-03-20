package ru.dravn.dropbox.Server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;


public class Server {
    private Vector<ClientHandler> clients;
    AuthService authService;

    public AuthService getAuthService()
    {
        return authService;
    }

    public Server()
    {

        try (ServerSocket serverSocket = new ServerSocket(8189))
        {
            clients = new Vector<>();
            authService = new AuthService();
            authService.connect();
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
            authService.disconnect();
        }
    }

    public void subscribe(ClientHandler clientHandler)
    {
        clients.add(clientHandler);
        //broadcastClientList();
    }

    public void unSubscribe(ClientHandler clientHandler)
    {
        clients.remove(clientHandler);
        //broadcastClientList();
    }


    public boolean isNickBusy(String nick)
    {
        for (ClientHandler o: clients)
        {
            if (o.getNick().equals(nick))
                return true;
        }
        return false;
    }

    public void broadcastMsg(String msg)
    {
        for (ClientHandler o: clients)
        {
            o.sendMsg(msg);
        }
    }


    public void privateMsg(String msg, ClientHandler from, String to )
    {
        for (ClientHandler o: clients)
        {
            if(o.getNick().equals(to))
            {
                o.sendMsg("от "+from.getNick()+" (лично): "+msg);
                from.sendMsg(o.getNick()+" (лично): "+msg);
                return;
            }
        }
        from.sendMsg("Пользоватьель с ником "+to+ " не найден");
    }

     public void broadcastClientList()
     {
         StringBuilder sb=new StringBuilder("/clientslist ");
         for (ClientHandler o: clients)
         {
            sb.append(o.getNick()+ "\r");
         }
         broadcastMsg(sb.toString());
     }


}
