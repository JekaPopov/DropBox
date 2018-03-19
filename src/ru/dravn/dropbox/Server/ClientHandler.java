package ru.dravn.dropbox.Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    protected Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private String nick;
    private long time;

    ClientHandler(Server server, Socket socket)
    {
        this.server = server;
        this.socket = socket;

        new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    in = new ObjectInputStream(socket.getInputStream());
                    out = new ObjectOutputStream(socket.getOutputStream());

                    authorization();

                    while (true) {
                        Object request = in.readObject();
                        if (request instanceof String) {
                            String msg = request.toString();
                            System.out.println(nick + ": " + msg);

                            if (msg.startsWith("/w ")) {
                                String[] data = msg.split("\\s", 3);
                                server.privateMsg(data[2], ClientHandler.this, data[1]);
                            } else if (msg.startsWith("/File ")) {
                                //String[] data = msg.split("\\s", 2);
                                File file = new File("C:\\serv\\nick1\\test.txt");
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
            }
        }).start();
    }






    String getNick()
    {
        return nick;
    }

    void sendMsg(String msg)
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

    private void authorization() throws IOException, ClassNotFoundException {
        stopTimer();

        while (true) {
            Object request = in.readObject();

            if (request instanceof String) {
                String msg = request.toString();
                System.out.println(msg);
                if (msg.startsWith("/auth ")) {
                    String[] data = msg.split("\\s");


                    if (server.getAuthService().login(data[1], data[2])) {
                        if (!server.isNickBusy(data[1])) {
                            nick = data[1];
                            ClientHandler.this.sendMsg("/authok " + data[1]);
                            server.subscribe(ClientHandler.this);
                            break;
                        } else {
                            ClientHandler.this.sendMsg("/alert Учетная запись занята");
                            time = System.currentTimeMillis();
                        }
                    } else {
                        ClientHandler.this.sendMsg("/alert Hе верный логин или пароль");
                        time = System.currentTimeMillis();
                    }
                }
            }
        }
    }


    public void stopTimer()
        {
            new Thread(() -> {
                while (true)
                {
                    if(time==0)break;
                    if ((System.currentTimeMillis() - time) > 1200)
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
        }

}
