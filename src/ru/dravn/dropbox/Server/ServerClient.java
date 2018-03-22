package ru.dravn.dropbox.Server;

import java.io.File;

public class ServerClient {

    private String mLogin;
    private File mFolder;

    public ServerClient(String login, File folder)
    {
        this.mLogin = login;
        this.mFolder = folder;
    }

    public String getLogin() {
        return mLogin;
    }

    public File getFolder() {
        return mFolder;
    }
}
