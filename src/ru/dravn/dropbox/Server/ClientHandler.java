package ru.dravn.dropbox.Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler {


    private Server server;
    protected Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean onLine;
    private String mQuery;
    private String mFile;
    private ServerClient mClient;

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
                    if(authorization.runAuth(receiveMessage()))break;
                }

                while (onLine) {
                    Object request = in.readObject();

                    if (request instanceof String) {
                        String[] data = ((String) request).split("\\s");

                        switch (data[0])
                        {
                            case ("/getFile"):
                            {
                                sendFile(data[1]);
                                break;
                            }
                            case ("/receiveFile"):
                            {
                                mFile = data[1];
                                break;
                            }
                            case ("/end"):
                            {
                                stopConnection();
                                break;
                            }
                        }
                    }
                    else if(request instanceof File)
                    {
                        switch (mQuery)
                        {
                            case ("/deleteFile"):
                            {
                             break;
                            }
                        }
                    }
                    else if(request instanceof  byte[])
                    {
                        receiveFile((byte[]) request);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                stopConnection();
            }
        }).start();
    }



    private void sendFile(String fileName) throws IOException {

        System.out.println("send: "+ fileName);

        sendMessage("/sendFile " + fileName);

        FileInputStream fin = new FileInputStream(mClient.getFolder()+"\\"+fileName);

        byte[] buffer = new byte[fin.available()];

        System.out.println(buffer.length);

        fin.read(buffer, 0, fin.available());
        fin.close();
        out.writeObject(buffer);
        out.flush();

        deleteFile(fileName);

        sendFileList();
    }

    private void receiveFile(byte[] request) throws IOException
    {
        System.out.println("receive: " + mFile + " " + request.length);

        File file = new File(mClient.getFolder() + "\\" + mFile);
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file.getPath());
        try
        {
            fos.write(request, 0, request.length);
        }
        catch (IOException ex)
        {
            file.delete();
            System.out.println(ex.getMessage());
        }
        fos.close();
        mFile = null;
        sendFileList();
    }

    private void deleteFile(String fileName)
    {

        if(new File(mClient.getFolder() + "\\"+fileName).delete())
            System.out.println("удален");
        else
            System.out.println("не удален");

    }
    public void sendFileList()
    {
        sendMessage("/fileList");
        sendMessage(mClient.getFolder());
    }

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


    public Object receiveMessage()
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
        mClient = null;
        mQuery = null;
        server.unSubscribe(ClientHandler.this);
        try {
            onLine = false;
            System.out.println("ClientHandler.stopConnection");
            sendMessage("/end");
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
