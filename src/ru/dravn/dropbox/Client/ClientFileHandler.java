package ru.dravn.dropbox.Client;

import ru.dravn.dropbox.Common.Command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClientFileHandler implements Command {

    private ClientController mController;
    private File mFolder;



    private String mReciveFile;


    public ClientFileHandler(ClientController controller, File folder) {
        mController = controller;
        mFolder = folder;
    }

    protected void receiveFile(byte[] request) throws IOException
    {
        System.out.println("receive: "+mReciveFile+" "+request.length);

        int i=1;
        while (new File(mFolder+"\\"+mReciveFile).exists())
        {
            String[] data = mReciveFile.split("\\.");
            String[] data1 = data[0].split("\\(");
            mReciveFile = data1[0]+"("+(i++)+")."+data[1];
        }

        File file = new File(mFolder+"\\"+mReciveFile);
        file.createNewFile();
        FileOutputStream fos=new FileOutputStream(file.getPath());
        try
        {
            fos.write(request, 0, request.length);
        }
        catch(IOException ex){
            file.delete();
            System.out.println(ex.getMessage());
        }

        fos.close();
        mReciveFile = null;
        mController.fillClientFileList();
        mController.fillServerFileList();
    }

    protected void sendFile(String fileName) throws IOException {
        mController.sendMessage(ReceiveFile + " "+fileName);

        FileInputStream fin = new FileInputStream(mFolder+"\\"+fileName);

        byte[] buffer = new byte[fin.available()];

        System.out.println("send: "+ fileName +" "+ buffer.length);

        fin.read(buffer, 0, fin.available());
        mController.out.writeObject(buffer);
        mController.out.flush();

        fin.close();
        deleteFile(fileName);
        mController.fillClientFileList();
        mController.fillServerFileList();

    }

    protected void deleteFile(String fileName)
    {
        if(new File(mFolder + "\\"+fileName).delete()) {
            mController.fillClientFileList();
            System.out.println(mFolder + "\\"+fileName +" удален");
        }
        else
            System.out.println(mFolder + "\\"+fileName + "не удален");
    }

    protected void loadFile(String selectedItem) {
        System.out.println(mController.mFileList +"\\"+ selectedItem);
        mController.sendMessage(GetFile+" "+selectedItem);
    }

    public void setReciveFile(String reciveFile) {
        mReciveFile = reciveFile;
    }

   
    public void properties(String selectedItem) {
    }

    public void rename(String selectedItem) {
    }
}
