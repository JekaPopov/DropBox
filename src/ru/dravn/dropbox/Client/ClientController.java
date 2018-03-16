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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class ClientController implements Initializable {

    @FXML
    GridPane gameField;
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
    private DataOutputStream out;
    private DataInputStream in;
    private String myNick;
    private String controlKey;

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
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

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
                try
                {
                    while(true)
                    {
                        String s = null;
                        s=in.readUTF();
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
                }
                finally
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
                out.writeUTF("/w "+data[1]+" "+data[2]);
            }
            else
            {
                out.writeUTF(textField.getText());
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
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
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

    public void gameMenu(ContextMenuEvent contextMenuEvent) {

        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                List<String> choices = new ArrayList<>();
                choices.add("3х3");
                choices.add("4х4");
                choices.add("5х5");
                choices.add("6х6");

                ChoiceDialog<String> settings = new ChoiceDialog<>("3х3", choices);

                settings.setTitle("Настройки игры крестики нолики");
                settings.setHeaderText("Выберите параметры");
                settings.setContentText("Выберите размер поля: ");


                Optional<String> fieldRes = settings.showAndWait();




                if (fieldRes.isPresent())
                {

                    System.out.println("Your choice: " + fieldRes.get());
                    int fieldSize=0;

                    switch (fieldRes.get())
                    {
                        case"3х3" :
                            fieldSize=3;
                            break;
                        case"4х4" :
                            fieldSize=4;
                            break;
                        case"5х5" :
                            fieldSize=5;
                            break;
                        case"6х6" :
                            fieldSize=6;
                            break;
                    }




                    System.out.println(fieldSize);

                    settings = new ChoiceDialog<>("X","X","0");

                    settings.setTitle("Настройки игры крестики нолики");
                    settings.setHeaderText("Выберите параметры");
                    settings.setContentText("Выберите X или O: ");

                    fieldRes = settings.showAndWait();
                    if (fieldRes.isPresent()){
                        System.out.println("Your choice: " + fieldRes.get());
                        gameField(fieldSize);
                    }
                }

        }
        });
    }

    private void gameField(int size) {
        for (int i = 0; i <size ; i++) {
            for (int j = 0; j <size ; j++) {
                Text rankTitle = new Text(i+" "+j );
                gameField.add(rankTitle, i,j);
            }

        }
    //gameField.setVisible(true);

    }
}


