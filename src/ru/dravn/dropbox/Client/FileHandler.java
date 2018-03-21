package ru.dravn.dropbox.Client;

import java.io.File;

public class FileHandler {


    public static String[] getFolderList(String folderName)
    {
       return new File(folderName).list();
    }




}
