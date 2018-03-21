package ru.dravn.dropbox.Client;

import java.io.File;

public class Client {

    private final String FOLDERPATH = "C:\\_client\\";

    private String mLogin;
    private String mFolder;

    public Client(String login) {
        mLogin = login;
        mFolder = FOLDERPATH + login;
        createClientFolder();
    }

    public String getLogin() {
        return mLogin;
    }

    public String getFolder() {
        return mFolder;
    }

    private void createClientFolder()
    {
        new File(mFolder).mkdirs();
    }

    public String[] getFolderFileList()
    {
        return new File(mFolder).list();
    }
}
