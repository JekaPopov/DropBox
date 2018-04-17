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
    private String mQuery;
    private String mFile;
    private ServerClient mClient;
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
                    Object request = in.readObject();

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
                            case Close_Connection:
                            {
                                stopConnection();
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



//    private void sendFile(String fileName) throws IOException {
//
//        System.out.println("send: "+ fileName);
//
//        sendMessage(SendFile + fileName);
//
//        FileInputStream fin = new FileInputStream(mClient.getFolder()+"\\"+fileName);
//
//        byte[] buffer = new byte[fin.available()];
//
//        System.out.println(buffer.length);
//
//        fin.read(buffer, 0, fin.available());
//        fin.close();
//        out.writeObject(buffer);
//        out.flush();
//
//        deleteFile(fileName);
//
//        sendFileList();
//    }



//    private void deleteFile(String fileName)
//    {
//
//        if(new File(mClient.getFolder() + "\\"+fileName).delete())
//            System.out.println("удален");
//        else
//            System.out.println("не удален");
//
//    }
//
//
//    public void sendFileList()
//    {
//        sendMessage(FileList);
//        sendMessage(mClient.getFolder());
//    }

    public void sendMessage(Object msg)
    {
        if(socket.isClosed())return;
        try
        {
            out.writeObject(msg);
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private Object receiveMessage()
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


    private void stopConnection()
    {
        mClient = null;
        mQuery = null;
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

    public void setClient(ServerClient client) {
        mClient = client;
    }
}
