package ru.dravn.dropbox.Server;

import ru.dravn.dropbox.Common.Command;

import java.io.File;

public class Authorization implements Command{

    private ClientHandler mHandler;
    private ServerClient mClient;

    public Authorization(ClientHandler handler)
    {
        this.mHandler = handler;
    }

    public boolean runAuth(Object msg)
    {
        if((msg!=null)&&(msg instanceof  String))
        {
            String[] data = ((String)msg).split("\\s");

            if(data[0].equals(Reg))
            {
                if(registration(data[1],data[2])) return true;
            }
            else if(data[0].equals(Auth))
            {
                if(authAnswer(data[1], data[2]))  return true;
            }
        }
        return false;
    }

    private boolean registration(String login , String pass)
    {
        System.out.println(Reg + login +" "+ pass);
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
            mHandler.sendMessage(AlertMessage + " Учетная запись занята");
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
            mHandler.sendMessage(AuthSuccessful + " "+mClient.getLogin());


            mHandler.fh = new FileHandler(mHandler, folder);
            mHandler.fh.sendFileList();
            mHandler.getServer().subscribe(mHandler);

            return true;
        }
        else
        {
            mHandler.sendMessage(AlertMessage + " Hе верный логин или пароль");
        }
        return false;
    }
}
