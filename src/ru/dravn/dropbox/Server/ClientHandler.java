package ru.dravn.dropbox.Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler {


    private Server server;
    protected Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nick;

    ClientHandler(Server server, Socket socket)
    {
        this.server = server;
        this.socket = socket;

        new Thread(() -> {
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                //authorization();

                new Authorization(ClientHandler.this);


                while (true) {
                    Object request = in.readObject();
                    if (request instanceof String) {
                        String msg = request.toString();
                        System.out.println(nick + ": " + msg);

                        if (msg.startsWith("/w ")) {
                            String[] data = msg.split("\\s");
                            server.privateMsg(data[2], ClientHandler.this, data[1]);
                        } else if (msg.startsWith("/File ")) {
                            String[] data = msg.split("\\s");
                            File file = new File("C:\\serv\\"+nick+"\\"+data[1]);
                            out.writeObject(file);

                        } else if (msg.equals("/end")) break;
                        else server.broadcastMsg(nick + ": " + msg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {

                e.printStackTrace();
            } finally {
                nick = null;
                server.unSubscribe(ClientHandler.this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        try
        {
           return in.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

   /* private void authorization() throws IOException, ClassNotFoundException {
        time = System.currentTimeMillis();
        //stopTimer();

        while (true) {
            Object request = in.readObject();

            if (request instanceof String) {
                String msg = request.toString();

                String[] data = msg.split("\\s");
                if (msg.startsWith("/auth ")) {

                    System.out.println("auth: "+msg);

                    if (server.getAuthService().login(data[1], data[2])) {
                        if (!server.isNickBusy(data[1])) {
                            authAnswer(data);
                            break;
                        } else {
                            ClientHandler.this.sendMsg("/alert Учетная запись занята");
                            time = System.currentTimeMillis();
                        }
                    }

                    else {
                        ClientHandler.this.sendMsg("/alert Hе верный логин или пароль");
                        time = System.currentTimeMillis();
                    }
                }
                else if(msg.startsWith("/reg "))
                {
                    System.out.println("reg: "+msg);
                    if(server.getAuthService().registration(data[1], data[2]))
                    {
                        System.out.println("r_auth: "+msg);
                        authAnswer(data);
                        break;
                    }
                    else
                    {
                        ClientHandler.this.sendMsg("/alert Учетная запись занята");
                        time = System.currentTimeMillis();
                    }

                }
            }
        }
    }

    private void authAnswer(String[] data) {
        nick = data[1];
        String folder = "C:\\serv\\"+data[1];
        ClientHandler.this.sendMsg("/authok " + data[1]);

        StringBuilder sb=new StringBuilder("/clientslist ");
        for(String file :new File(folder).list())
        {
            sb.append(file + "\r");
        }
        ClientHandler.this.sendMsg(sb.toString());
        server.subscribe(ClientHandler.this);
        time=0;
    }


    public void stopTimer()
        {
            new Thread(() -> {
                while (true)
                {
                    if(time==0)break;
                    if ((System.currentTimeMillis() - time) > 12000)
                    {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }).start();
        }*/

}
