package ru.dravn.dropbox.Client;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class ClientController implements Initializable {

    @FXML
    HBox title;
    @FXML
    Button exitButton;
    @FXML
    HBox field;
    @FXML
    HBox authorizedPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passField;
    @FXML
    ListView<String> serverFileViewList;
    @FXML
    ListView<String> clientFileViewList;
    @FXML
    CheckBox regCheck;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Client mClient;
    private boolean onLine;
    private String mFile;
    private String mQuery;
    private File mFileList;

    private ObservableList<String> serverFileList;
    private ObservableList<String> clientFileList;

    private final String SERVER_IP = "localhost";
    private final int SERVER_PORT = 8189;


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        setAuthorized(false);
        this.onLine = true;
    }

    public void connect()
    {
        try
        {
            onLine = true;
            socket = new Socket(SERVER_IP, SERVER_PORT);

            out = new ObjectOutputStream(socket.getOutputStream());

            serverFileList = FXCollections.observableArrayList();
            serverFileViewList.setItems(serverFileList);
            serverFileViewList.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
            {
                @Override
                public ListCell<String> call(ListView<String> param)
                {
                    return new ListCell<String>()
                    {
                        @Override
                        protected void updateItem(String item, boolean empty)
                        {
                            super.updateItem(item, empty);
                            if (!empty)
                            {
                                setText(item);
                            }
                            else
                            {
                                setGraphic(null);
                                setText(null);
                            }
                        }
                    };
                }
            });

            clientFileList = FXCollections.observableArrayList();
            clientFileViewList.setItems(clientFileList);
            clientFileViewList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                @Override
                public ListCell<String> call(ListView<String> param)
                {
                    return new ListCell<String>()
                    {
                        @Override
                        protected void updateItem(String item, boolean empty)
                        {
                            super.updateItem(item, empty);
                            if (!empty)
                            {
                                setText(item);
                            }
                            else
                            {
                                setGraphic(null);
                                setText(null);
                            }
                        }
                    };
                }
            });


            Thread t = new Thread(() ->
            {
                try
                {
                    in = new ObjectInputStream(socket.getInputStream());

                    while(onLine)
                    {
                        Object request = receiveMessage();
                        if(request instanceof String)
                        {
                            String[] data =((String) request).split("\\s");
                            switch (data[0])
                            {
                                case("/authok"):
                                {
                                    setAuthorized(true);
                                    mClient = new Client(data[1]);
                                    fillClientFileList();
                                    break;
                                }
                                case ("/fileList"):
                                {
                                    mQuery = "/fileList";
                                    break;
                                }
                                case ("/alert"):
                                {
                                    showAlert(data);
                                    break;
                                }
                                case ("/sendFile"):
                                {
                                    mFile = data[1];
                                    break;
                                }
                                case("/end"):
                                {
                                    stopConnection();
                                    break;
                                }
                            }
                        }
                        else if(request instanceof File)
                        {
                            switch(mQuery)
                            {
                                case("/fileList"):
                                {
                                    mFileList = (File)request;
                                    fillServerFileList();
                                    mQuery = null;
                                    break;
                                }

                            }
                        }
                        else if(request instanceof  byte[])
                        {
                            receiveFile((byte[]) request);
                        }
                    }
                }
                catch (IOException e)
                {
                    showAlert("Сервер перестал отвечать");
                }
                finally
                {
                    stopConnection();
                }
            });
            t.setDaemon(true);
            t.start();
        }
        catch (IOException e)
        {
            showAlert("Не удалось подключиться к серверу. Проверьте сетевое соединение.");
        }
    }


    public void clientFileListClicked(MouseEvent mouseEvent)
    {

        if(mouseEvent.isSecondaryButtonDown())
        {
            System.out.println("вторая");
        }
        else if ((mouseEvent.getClickCount() == 2))
        {
            try
            {
                sendFile(clientFileViewList.getSelectionModel().getSelectedItem());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void serverFileListClicked(MouseEvent mouseEvent)
    {
        if ((mouseEvent.getClickCount() == 2))
        {

            loadFile(serverFileViewList.getSelectionModel().getSelectedItem());
        }
        else if(mouseEvent.getButton().name().equals("SECONDARY"))
        {
            System.out.println("вторая");
        }
    }

    private void receiveFile(byte[] request) throws IOException
    {
        System.out.println("receive: "+mFile+" "+request.length);

        File file = new File(mClient.getFolder()+"\\"+mFile);
        file.createNewFile();
        FileOutputStream fos=new FileOutputStream(file.getPath());
        try
        {
            fos.write(request, 0, request.length);
        }
        catch(IOException ex){
            file.delete();
            System.out.println(ex.getMessage());
        }

        fos.close();
        mFile = null;
        fillClientFileList();
        fillServerFileList();
    }

    private void sendFile(String fileName) throws IOException {
        sendMessage("/receiveFile " + fileName);

        FileInputStream fin = new FileInputStream(mClient.getFolder()+"\\"+fileName);

        byte[] buffer = new byte[fin.available()];

        System.out.println("send: "+ fileName +" "+ buffer.length);

        fin.read(buffer, 0, fin.available());
        out.writeObject(buffer);
        out.flush();

        fin.close();
        deleteFile(fileName);
        fillClientFileList();
        fillServerFileList();
        mQuery = null;
    }

    private void deleteFile(String fileName)
    {
        if(new File(mClient.getFolder() + "\\"+fileName).delete())
            System.out.println("удален");
        else
            System.out.println("не удален");
    }

    private void fillServerFileList() {
        Platform.runLater(() -> {
            serverFileList.clear();

            if(mFileList.list()!=null)
            {
                serverFileList.addAll(mFileList.list());
            }
            });
    }

    private void fillClientFileList() {
        Platform.runLater(() -> {
            clientFileList.clear();

            if (mClient.getFolder().list() != null)
            {
                clientFileList.addAll(mClient.getFolder().list());
            }
        });
    }


    private void sendMessage(Object msg)
    {
        try
        {
            if (socket == null
                    || socket.isClosed())
            {
                connect();
            }

            out.writeObject(msg);
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showAlert("Ошибка отправки сообщения");
        }
    }

    public Object receiveMessage()
    {
        if(socket.isClosed()||socket==null)return null;

        try {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void authorizationAndRegistration()
    {
        if(passField.getText().isEmpty()
                ||loginField.getText().isEmpty())
        {
            showAlert("Введены неверные данные");
            return;
        }

        if(regCheck.isSelected())
        {
            sendMessage("/reg " + loginField.getText() + " " + passField.getText());
        }
        else
        {
            sendMessage("/auth " + loginField.getText() + " " + passField.getText());
        }
        loginField.clear();
        passField.clear();
    }

    private void showAlert(String ... msg)
    {
        for (int i = 2; i <msg.length ; i++) {
            msg[1]+=msg[i]+" ";
        }
        Platform.runLater(() -> {
            Alert alert =new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Возникли проблемы");
            alert.setHeaderText(null);
            alert.setContentText(msg[1]);
            alert.showAndWait();
        });
    }

    private void setAuthorized(boolean authorized)
    {
        authorizedPanel.setVisible(!authorized);
        authorizedPanel.setManaged(!authorized);
        regCheck.setSelected(authorized);

        exitButton.setVisible(authorized);
        field.setVisible(authorized);
        title.setVisible(authorized);
        clientFileViewList.setManaged(authorized);
        serverFileViewList.setManaged(authorized);
    }



    private void loadFile(String selectedItem) {
        System.out.println(mFileList +"\\"+ selectedItem);
        sendMessage("/getFile "+selectedItem);
        //sendMessage(new File(mFileList +"\\"+ selectedItem) );
    }

    public void exit()
    {
        sendMessage("/end");
    }

    public void stopConnection()
    {
        setAuthorized(false);
        mClient = null;
        onLine = false;
        loginField.clear();
        passField.clear();

        try
        {
            in.close();
            out.close();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}


