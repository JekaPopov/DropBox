package ru.dravn.dropbox.Server;

import java.io.File;
import java.util.Arrays;

public class Authorization {

    private ClientHandler mHandler;
    private long mTime;

    public Authorization(ClientHandler handler) {
        this.mHandler = handler;
    }

    public boolean runAuth(Object msg)
    {
        mTime = System.currentTimeMillis();
        if((msg!=null)&&(msg instanceof  String))
        {
            String[] data = ((String)msg).split(" ");

            System.out.println("in: "+ Arrays.toString(data));

            if(data[0].equals("/reg"))
            {
                if(registration(data)) return true;
            }
            else if(data[0].equals("/auth"))
            {
                if(authAnswer(data))  return true;
            }
        }
        return false;
    }

    private boolean registration(String[] data)
    {
        System.out.println("reg: "+ Arrays.toString(data));
        if(AuthService.registration(data[1], data[2]))
        {
            System.out.println("r_auth");
            return authAnswer(data);
        }
        else
        {
            mHandler.sendMsg("/alert Учетная запись занята");

        }
        return false;
    }


    private boolean authAnswer(String[] data)
    {
        if (AuthService.login(data[1], data[2]))
        {
            System.out.println("auth: "+ Arrays.toString(data));

            String folder = "C:\\serv\\"+data[1];
            mHandler.setNick(data[1]);
            mHandler.sendMsg("/authok " + data[1]);

            StringBuilder sb=new StringBuilder("/fileList ");

            for(String file :new File(folder).list())
            {
                sb.append(file + "\r");
            }
            mHandler.sendMsg(sb.toString());
            mHandler.getServer().subscribe(mHandler);

            mTime = 0;
            return true;
        }
        else
        {
            mHandler.sendMsg("/alert Hе верный логин или пароль");
        }
        return false;
    }
}
