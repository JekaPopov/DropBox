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
import ru.dravn.dropbox.Common.Command;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;


public class ClientController implements Initializable, Command {

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
    protected ObjectOutputStream out;
    private ObjectInputStream in;
    private Client mClient;
    private boolean onLine;
    private String mFile;
    private ClientFileHandler mFileHandler;
    private String mQuery;
    protected File mFileList;

    private ObservableList<String> serverFileList;
    private ObservableList<String> clientFileList;

    private final String SERVER_IP = "localhost";
    private final int SERVER_PORT = 8189;
    private String chosenFile;


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
                                case(AuthSuccessful):
                                {
                                    setAuthorized(true);
                                    mClient = new Client(data[1]);
                                    mFileHandler = new ClientFileHandler(this, mClient.getFolder());
                                    fillClientFileList();
                                    break;
                                }
                                case (FileList):
                                {
                                    mQuery = FileList;
                                    break;
                                }
                                case (SendFile):
                                {
                                    mFileHandler.setReciveFile(((String) request).replace(SendFile+" ", ""));
                                    break;
                                }
                                case (AlertMessage):
                                {
                                    showAlert(((String) request).replace(AlertMessage, ""));
                                    break;
                                }
                                case(Close_Connection):
                                {
                                    stopConnection();
                                    break;
                                }
                                default:
                                {
                                    break;
                                }
                            }
                        }
                        else if(request instanceof File)
                        {
                            mFileList = (File)request;
                            fillServerFileList();
                            mQuery = null;
                        }
                        else if(request instanceof  byte[])
                        {
                            mFileHandler.receiveFile((byte[]) request);
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
        if(mouseEvent.getClickCount() == 1)
        {
            showDialog(clientFileViewList.getSelectionModel().getSelectedItem(), "client");
        }
        else if (mouseEvent.getClickCount() == 2)
        {
            try
            {
                mFileHandler.sendFile(clientFileViewList.getSelectionModel().getSelectedItem());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void serverFileListClicked(MouseEvent mouseEvent)
    {
        if(mouseEvent.getClickCount() == 1)
        {
            showDialog(serverFileViewList.getSelectionModel().getSelectedItem(),"server");
        }
        else if ((mouseEvent.getClickCount() == 2))
        {
            mFileHandler.loadFile(serverFileViewList.getSelectionModel().getSelectedItem());
        }
    }


    protected void fillServerFileList() {
        Platform.runLater(() -> {
            serverFileList.clear();

            if(mFileList.list()!=null)
            {
                serverFileList.addAll(mFileList.list());
            }
        });
    }

    protected void fillClientFileList() {
        Platform.runLater(() -> {
            clientFileList.clear();

            if (mClient.getFolder().list() != null)
            {
                clientFileList.addAll(mClient.getFolder().list());
            }
        });
    }


    protected void sendMessage(Object msg)
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
            showAlert("Ошибка приема сообщения");
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
            sendMessage(Reg +" "+ loginField.getText() + " " + passField.getText());
        }
        else
        {
            sendMessage(Auth + " "+loginField.getText() + " " + passField.getText());
        }
        loginField.clear();
        passField.clear();
    }

    protected void showAlert(String msg)
    {
        Platform.runLater(() -> {
            Alert alert =new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Возникли проблемы");
            alert.setHeaderText(null);

            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void showDialog(String selectedItem, String type)
    {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Выберите действие с файлом");
            alert.setHeaderText("Выберите действие с файлом "+ selectedItem);
            alert.setContentText(null);


            ButtonType buttonSend = new ButtonType("Передать");
            ButtonType buttonDelete = new ButtonType("Удалить");
            ButtonType buttonRename = new ButtonType("Переименовать");
            ButtonType buttonProperties = new ButtonType("Свойства");
            ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonSend, buttonDelete, buttonRename,buttonProperties, buttonTypeCancel);


            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSend)
            {
                if(type.equals("server"))
                {
                    mFileHandler.loadFile(selectedItem);
                }
                else
                {
                    try {
                        mFileHandler.sendFile(clientFileViewList.getSelectionModel().getSelectedItem());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (result.get() == buttonDelete)
            {
               if(type.equals("server"))
               {
                   sendMessage(DeleteFile + " "+selectedItem);
               }
               else
               {
                   mFileHandler.deleteFile(selectedItem);
               }
            }
            else if (result.get() == buttonRename) {
                showRenameDialog(selectedItem, type);
            }
            else if (result.get() == buttonProperties) {
                showPropertiesDialog(selectedItem, type);
            } else {
                alert.close();
            }

        });
    }

    private void showRenameDialog(String oldName, String type)
    {
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog(oldName);
            dialog.setTitle("Преименование файла");
            dialog.setHeaderText("Переименование файла "+ oldName);
            dialog.setContentText("Введите новое имя:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(newName -> {
                if(type.equals("server"))
                {
                    sendMessage(RenameFile + " "+oldName + " "+ newName);
                }
                else
                {
                    mFileHandler.rename(oldName, newName);
                }
            });
        });
    }

    private void showPropertiesDialog(String name, String type)
    {
        Platform.runLater(() -> {
            File file;
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle("Свойства файла");
            dialog.setHeaderText("Свойства файла " + name);

            if (type.equals("server")) {
                file = new File(mFileList, name);
            } else {
                file = new File(mClient.getFolder(), name);
            }

            DateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            dialog.setContentText("Расположение: " + file.getAbsolutePath() + "\n" +
                    "Размер: " + humanReadableByteCount(file.length(),true) + "\n" +
                    "Создан: " + TIMESTAMP.format(file.lastModified()));
            dialog.showAndWait();
        });
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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


    public void exit()
    {
        sendMessage(Close_Connection);
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


