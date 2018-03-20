package ru.dravn.dropbox.Server;

import java.io.File;
import java.util.Arrays;

public class Authorization {

    private ClientHandler mHandler;

    public Authorization(ClientHandler handler) {
        mHandler = handler;
        while (true)
        {
            if(handler.receiveMsg() instanceof  String)
            {
               String[] data = ((String) handler.receiveMsg()).split(" ");

                System.out.println("in: "+ Arrays.toString(data));

                if(data[0].equals("/reg"))
                {
                    registration(data);
                    break;
                }
                else if(data[0].equals("/auth"))
                {
                    authAnswer(data);
                    break;
                }
            }

        }
    }

    private void registration(String[] data)
    {
        System.out.println("reg: "+ Arrays.toString(data));
        if(mHandler.getServer().getAuthService().registration(data[1], data[2]))
        {
            System.out.println("r_auth");
            authAnswer(data);
        }
        else
        {
            mHandler.sendMsg("/alert Учетная запись занята");
        }
    }

    private void authAnswer(String[] data)
    {
        if (mHandler.getServer().getAuthService().login(data[1], data[2]))
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

        }
        else
        {
            mHandler.sendMsg("/alert Hе верный логин или пароль");
        }
    }

}
