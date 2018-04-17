package ru.dravn.dropbox.Common;

public interface Command
{

    String Close_Connection = "/end";
    String Auth = "/auth";
    String Reg = "/reg";
    String AuthSuccessful = "/authok";
    String AlertMessage = "/alert";

    String GetFile = "/getFile";
    String ReceiveFile = "/receiveFile";
    String DeleteFile = "/deleteFile";
    String SendFile = "/sendFile";
    String FileList = "/fileList";
}
