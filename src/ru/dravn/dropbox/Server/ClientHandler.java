package ru.dravn.dropbox.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    protected Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    protected long time;


    ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            time = System.currentTimeMillis();
            stopTimer();

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        while (true)
                        {
                            String msg = in.readUTF();

                            if (msg.startsWith("/auth "))
                            {
                                String[] data = msg.split("\\s");
                                String newNick = server.getAuthService().getNickByLoginAndPass(data[1], data[2]);


                                if (newNick != null)
                                {
                                    if (!server.isNickBusy(newNick))
                                    {
                                        nick = newNick;
                                        ClientHandler.this.sendMsg("/authok " + newNick);
                                        server.subscribe(ClientHandler.this);
                                        time = 0;
                                        break;
                                    }
                                    else
                                    {
                                        ClientHandler.this.sendMsg("/alert Учетная запись занята");
                                        time = System.currentTimeMillis();
                                    }
                                }
                                else
                                {
                                    ClientHandler.this.sendMsg("/alert Hе верный логин или пароль");
                                    time = System.currentTimeMillis();
                                }
                            }
                        }

                        while (true)
                        {
                            String msg = in.readUTF();
                            System.out.println(nick + ": " + msg);
                            if (msg.startsWith("/w "))
                            {
                                String[] data = msg.split("\\s", 3);
                                server.privateMsg(data[2], ClientHandler.this, data[1]);
                            }
                            else if (msg.equals("/end")) break;
                            else server.broadcastMsg(nick + ": " + msg);
                        }
                    }
                    catch (IOException e)
                    {

                        e.printStackTrace();
                    }
                    finally
                    {
                        nick = null;
                        server.unsubscribe(ClientHandler.this);
                        try
                        {
                            socket.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String getNick()
    {
        return nick;
    }

    void sendMsg(String msg)
    {
        try
        {
            out.writeUTF(msg);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stopTimer()
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    while (true)
                    {
                        if(time==0)break;

                        if ((System.currentTimeMillis() - time) > 120000)
                        {
                            try
                            {
                                socket.close();
                                break;
                            }
                            catch (IOException e)
                            {

                                e.printStackTrace();
                            }
                        }
                    }

                }
            });
            thread.start();
        }
}
