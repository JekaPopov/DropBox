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
    HBox field;
    @FXML
    HBox msgPanel;
    @FXML
    HBox authorizedPanel;
    @FXML
    TextArea textArea;
    @FXML
    TextField textField;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passField;
    @FXML
    ListView<String> clientViewList;


    public boolean authorized;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String myNick;

    private ObservableList<String> clientList;

    final String SERVER_IP = "localhost";
    final int SERVER_PORT = 8189;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        setAuthorized(false);
    }

    public void connect()
    {
        try
        {
            socket = new Socket(SERVER_IP, SERVER_PORT);

            out = new ObjectOutputStream(socket.getOutputStream());

            clientList= FXCollections.observableArrayList();
            clientViewList.setItems(clientList);

            clientViewList.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
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
                                if (item.equals(myNick))
                                {
                                    setStyle("-fx-font-weight: bold");
                                }
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
                try {
                    in = new ObjectInputStream(socket.getInputStream());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try
                {
                    while(true)
                    {
                        Object o = in.readObject();
                        String s = o.toString();

                        System.out.println(s);
                        if(s.startsWith("/"))
                        {
                            if (s.startsWith("/authok "))
                            {
                            setAuthorized(true);
                            myNick = s.split("\\s")[1];
                            }
                            else if (s.startsWith("/clientslist "))
                            {
                                String[] data = s.split("\\s");
                                Platform.runLater(() ->
                                {
                                    clientList.clear();

                                    for (int i = 1; i < data.length; i++)
                                    {
                                        clientList.addAll(data[i]);
                                    }
                                });
                            }
                            else if(s.startsWith("/alert "))
                            {
                                String data[]=s.split("\\s",2);
                                showAlert(data[1]);
                            }
                        }
                        else
                        {
                        textArea.appendText(s + "\n");
                        }
                    }

                }
                catch (IOException e)
                {
                    showAlert("Сервер перестал отвечать");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally
                {
                    setAuthorized(false);
                    try
                    {
                        socket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
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


    public void SendMessage()
    {
        try
        {
            if(textField.getText().startsWith("Лично "))
            {
                String[] data = textField.getText().split("\\s", 4);
                out.writeObject("/w "+data[1]+" "+data[2]);
                out.flush();
            }
            else
            {
                out.writeObject(textField.getText());
                out.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showAlert("Ошибка отправки сообщения");
        }
        textArea.setWrapText(true);
        textField.clear();
        textField.requestFocus();
    }

    public void sendAuthMsg()
    {
        if(passField.getText().isEmpty()||loginField.getText().isEmpty())
        {
            showAlert("Введены неверные данные");
            return;
        }

        if (socket == null || socket.isClosed()){
            connect();
        }
        try {// /auth login pass
            /*ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(s);
            outputStream.flush();*/

            String s = "/auth " + loginField.getText() + " " + passField.getText();
            System.out.println(s);
            out.writeObject("/auth " + loginField.getText() + " " + passField.getText());
            out.flush();
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка авторизации");
        }
    }



    private void showAlert(String msg)
    {
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                Alert alert =new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Возникли проблемы");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            }
        });
    }


    private void setAuthorized(boolean authorized)
    {
            this.authorized=authorized;
            authorizedPanel.setVisible(!authorized);
            authorizedPanel.setManaged(!authorized);
            msgPanel.setVisible(authorized);
            msgPanel.setManaged(authorized);
            clientViewList.setVisible(authorized);
            clientViewList.setManaged(authorized);
    }

    public void clientsListClicked(MouseEvent mouseEvent)
    {
        if ((mouseEvent.getClickCount() == 2)&&(!clientViewList.getSelectionModel().getSelectedItem().equals(myNick)))
        {
            textField.setText("Лично " + clientViewList.getSelectionModel().getSelectedItem() + " ");
            textField.requestFocus();
            textField.selectEnd();
        }
    }
}


