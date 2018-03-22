package ru.dravn.dropbox.Client;

import java.io.File;

public class Client {

    protected String FOLDERPATH = "C:\\_client\\";

    private String mLogin;
    private File mFolder;

    public Client(String login) {
        mLogin = login;
        mFolder = new File(FOLDERPATH + login+"\\");
        createClientFolder();
    }

    public String getLogin() {
        return mLogin;
    }

    public File getFolder() {
        return mFolder;
    }

    private void createClientFolder()
    {
        mFolder.mkdirs();
    }

}
