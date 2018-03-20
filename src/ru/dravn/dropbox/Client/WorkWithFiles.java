package ru.dravn.dropbox.Client;

import java.io.File;

public class WorkWithFiles {

    public boolean createFolder(String name)
    {
        return new File(name).mkdir();
    }

    public String[] dirList(String folder)
    {
        return new File(folder).list();
    }
}
