package ru.dravn.dropbox.Client;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class ClientController implements Initializable {

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
    private Client client;
    private boolean onLine;

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
                        Object request = receiveMsg();
                        if(request instanceof String)
                        {
                            String[] data =((String) request).split("\\s");
                            switch (data[0])
                            {
                                case("/authok"):
                                {
                                    setAuthorized(true);
                                    client = new Client(data[1]);

                                    fillClientFileList();
                                    break;
                                }
                                case ("/fileList"):
                                {
                                    fillServerFileList(data);
                                    break;
                                }
                                case ("/alert"):
                                {
                                    showAlert(data);
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
                            System.out.println(((File) request).getName() + " " + ((File) request).getAbsolutePath());

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

    private void fillServerFileList(String[] data) {
        Platform.runLater(() -> {
            serverFileList.clear();
            for (int i = 1; i < data.length; i++) {
                serverFileList.addAll(data[i]);
            }
        });
    }

    private void fillClientFileList() {
        if(FileHandler.getFolderList(client.getFolder())!=null)
        {
            Platform.runLater(() -> {
                clientFileList.clear();
                clientFileList.addAll(FileHandler.getFolderList(client.getFolder()));
            });
        }
    }


    private void sendMessage(String msg)
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

    public Object receiveMsg()
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
        clientFileViewList.setManaged(authorized);
        serverFileViewList.setManaged(authorized);
    }

    public void clientFileListClicked(MouseEvent mouseEvent)
    {
        if ((mouseEvent.getClickCount() == 2))
        {
            sendMessage("/file " + clientFileViewList.getSelectionModel().getSelectedItem() + " ");
        }
    }

    public void serverFileListClicked(MouseEvent mouseEvent)
    {
        if ((mouseEvent.getClickCount() == 2))
        {
            loadFile(serverFileViewList.getSelectionModel().getSelectedItem());
            //sendMessage("/file " + serverFileViewList.getSelectionModel().getSelectedItem() + " ");
        }
    }

    private void loadFile(String selectedItem) {
        sendMessage("/loadFile "+selectedItem);
    }

    public void exit()
    {
        sendMessage("/end");
    }

    public void stopConnection()
    {
        setAuthorized(false);
        client = null;
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


