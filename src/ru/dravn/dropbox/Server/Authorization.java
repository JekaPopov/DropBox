package ru.dravn.dropbox.Server;

import java.io.File;

public class Authorization {

    private ClientHandler mHandler;
    private ServerClient mClient;
    private long mTime;

    public Authorization(ClientHandler handler) {
        this.mHandler = handler;
    }

    public boolean runAuth(Object msg)
    {
        mTime = System.currentTimeMillis();
        if((msg!=null)&&(msg instanceof  String))
        {
            String[] data = ((String)msg).split("\\s");

            if(data[0].equals("/reg"))
            {
                if(registration(data[1],data[2])) return true;
            }
            else if(data[0].equals("/auth"))
            {
                if(authAnswer(data[1], data[2]))  return true;
            }
        }
        return false;
    }

    private boolean registration(String login , String pass)
    {
        System.out.println("reg: "+ login +" "+ pass);
        if(AuthService.registration(login, pass))
        {
            if(authAnswer(login,pass))
            {
                mClient.getFolder().mkdirs();
                return true;
            }
        }
        else
        {
            mHandler.sendMessage("/alert Учетная запись занята");
        }
        return false;
    }


    private boolean authAnswer(String login, String pass)
    {
        if (AuthService.login(login, pass))
        {
            File folder = new File(AuthService.getFolder(login));
            mClient = new ServerClient(login, folder);
            mHandler.setClient(mClient);
            mHandler.sendMessage("/authok " + mClient.getLogin());
            mHandler.sendFileList();
            mHandler.getServer().subscribe(mHandler);

            mTime = 0;
            return true;
        }
        else
        {
            mHandler.sendMessage("/alert Hе верный логин или пароль");
        }
        return false;
    }
}
