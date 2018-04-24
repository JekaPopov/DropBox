package ru.dravn.dropbox.Server;

import ru.dravn.dropbox.Common.Command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler implements Command
{
    private File mFolder;
    private ClientHandler mClientHandler;



    private String mReciveFile;

    public FileHandler( ClientHandler clientHandler,File folder) {
        mFolder = folder;
        mClientHandler = clientHandler;
    }


    public void sendFile(String fileName) throws IOException {

        System.out.println("send: "+ fileName);

        mClientHandler.sendMessage(SendFile +" "+fileName);

        FileInputStream fin = new FileInputStream(mFolder+"\\"+fileName);

        byte[] buffer = new byte[fin.available()];

        System.out.println(buffer.length);

        fin.read(buffer, 0, fin.available());
        fin.close();
        mClientHandler.out.writeObject(buffer);
        mClientHandler.out.flush();

        deleteFile(fileName);

        sendFileList();
    }


    public void receiveFile(byte[] request) throws IOException
    {
        System.out.println("receive: " + mReciveFile + " " + request.length);

        int i=1;
        while (new File(mFolder+"\\"+mReciveFile).exists())
        {
            String[] data = mReciveFile.split("\\.");
            String[] data1 = data[0].split("\\(");
            mReciveFile = data1[0]+"("+(i++)+")."+data[1];
        }


        if(mReciveFile!=null)
        {
            File file = new File(mFolder + "\\" + mReciveFile);
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
        }
        mReciveFile = null;
        sendFileList();
    }


    private void deleteFile(String fileName)
    {

        if(new File(mFolder + "\\"+fileName).delete())
            System.out.println("удален");
        else
            System.out.println("не удален");

    }


    public void sendFileList() throws IOException {
        mClientHandler.sendMessage(FileList);
        mClientHandler.sendMessage(mFolder);
    }

    public void setReciveFile(String reciveFile) {
        mReciveFile = reciveFile;
    }

}
