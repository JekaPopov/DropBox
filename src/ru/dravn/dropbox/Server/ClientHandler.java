package ru.dravn.dropbox.Server;

import ru.dravn.dropbox.Common.Command;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Command {


    private Server server;
    protected Socket socket;
    private ObjectInputStream in;
    public ObjectOutputStream out;
    private boolean onLine;
    public FileHandler fh;

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

                while (onLine)
                {
                    if(authorization.runAuth(receiveMessage()))break;
                }

                while (onLine)
                {
                    Object request = receiveMessage();

                    if (request instanceof String)
                    {
                        String[] data = ((String) request).split("\\s");

                        switch (data[0])
                        {
                            case GetFile:
                            {
                                fh.sendFile(data[1]);
                                break;
                            }
                            case ReceiveFile:
                            {
                                fh.setReciveFile(data[1]);
                                break;
                            }
                            case DeleteFile:
                            {
                                fh.deleteFile(data[1]);
                                break;
                            }
                            case Close_Connection:
                            {
                                stopConnection();
                                break;
                            }
                            default:
                            {
                                break;
                            }
                        }
                    }
                    else if(request instanceof  byte[])
                    {
                        fh.receiveFile((byte[]) request);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                stopConnection();
            }
        }).start();
    }


    public void sendMessage(Object msg) throws IOException {

        if(socket.isClosed()) throw new IOException();

            out.writeObject(msg);
            out.flush();


    }


    private Object receiveMessage() throws IOException, ClassNotFoundException {
        if(socket.isClosed()
                ||socket==null) throw new IOException();

        return in.readObject();

    }


    private void stopConnection()
    {
        server.unSubscribe(ClientHandler.this);
        try {
            onLine = false;
            System.out.println("ClientHandler.stopConnection");
            sendMessage(Close_Connection);
            in.close();
            out.close();
            socket.close();
            this.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Server getServer() {
        return server;
    }


}
