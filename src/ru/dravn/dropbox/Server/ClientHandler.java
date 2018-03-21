package ru.dravn.dropbox.Server;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler {


    private Server server;
    protected Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nick;
    private boolean onLine;

    ClientHandler(Server server, Socket socket)
    {
        this.server = server;
        this.socket = socket;
        this.onLine = true;
        Authorization authorization = new Authorization(this);

        new Thread(() -> {
            try {

                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                while (onLine) {
                    if(authorization.runAuth(receiveMsg()))break;
                }

                while (onLine) {
                    Object request = in.readObject();
                    if (request instanceof String) {
                        String[] data = ((String) request).split("\\s");



                        switch (data[0])
                        {
                            case ("/loadFile"):
                            {
                                System.out.println(nick + ": " + Arrays.toString(data));
                                File file = new File("C:\\_serv\\"+nick+"\\"+data[1]);
                                out.writeObject(file);
                                break;
                            }
                            case ("/end"):
                            {
                                stopConnection();
                                break;
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                stopConnection();
            }
        }).start();
    }

    public Server getServer() {
        return server;
    }

    public String getNick()
    {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }


    public void sendMsg(String msg)
    {
        if(socket.isClosed())return;
        try
        {
            out.writeObject(msg);
            System.out.println("out"+msg);
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Object receiveMsg()
    {
        if(socket.isClosed()
                ||socket==null)return null;
            try {
                return in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        return null;
    }


    public void stopConnection()
    {
        nick = null;
        server.unSubscribe(ClientHandler.this);
        try {
            onLine = false;
            System.out.println("ClientHandler.stopConnection");
            sendMsg("/end");
            in.close();
            out.close();
            socket.close();
            this.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
