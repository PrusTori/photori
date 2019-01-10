package com.viktoriia.photori.fxapp.app;


import com.jfoenix.controls.*;

import com.viktoriia.photori.db.DatabaseBackup;
import com.viktoriia.photori.db.DatabaseConnector;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import javax.swing.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.apache.commons.codec.digest.DigestUtils;

import org.intellij.lang.annotations.Language;


public class ControllerPhotori {

    @FXML
    private Label labelTypeInfo, labelRoomInfo, labelPrice;

    @FXML
    private JFXTextField textName, textPhone, textMail, textTime,
            textLocationAddress, textLocationName, textOverPhotoQuantity,
            textAdd1, textAdd2, textAdd3, textAdd4, textSearch;

    @FXML
    private JFXComboBox<String> comboType, comboRoom, comboPhotoset, comboTable,
            comboBoxAdd1, comboBoxAdd2, comboBoxAdd3, comboBoxAdd4;

    @FXML
    private JFXButton buttonOrder, buttonAdd, buttonDelete,
            buttonLogin, buttonBackup, buttonRestore;

    @FXML
    private JFXDatePicker datePicker, datePickerAdd;

    @FXML
    private JFXTimePicker timePicker, timePickerAdd;

    @FXML
    private ImageView imageViewAdd;

    @FXML
    private VBox vboxScrollPhotosets;

    @FXML
    private TableView<String[]> tableView;

    @FXML
    private Tab tabAdmin;

    private ObservableList<String[]> observableListTableView;

    private DatabaseConnector databaseConnector;

    private double priceType = 0,
            priceOverPhoto = 0,
            priceRoom = 0;

    @Language("SQL")
    private String sqlGetTable, tableName;

    private String[] columnNamesTableView, promptTextFieldsAdd, promptComboBoxesAdd;
    String[][] type, room, photosets, photos, columnNames;

    private JFXTextField[] textFieldsAdd;
    private JFXComboBox[] comboBoxesAdd;
    private ObservableList<String>[] comboBoxesInTable;
    private int[] columnComboBoxesInTable, indexTextFieldsAdd, indexComboBoxesAdd;

    private boolean adminAccess = false;

    public void initialize()
            throws SQLException, ClassNotFoundException {

        databaseConnector = new DatabaseConnector();

        setItemsCombo(comboRoom, "select name from rooms order by name");
        setItemsCombo(comboType, "select title from types order by title");
        setItemsCombo(comboPhotoset, "select title from photosets order by title");
        setItemsCombo(comboTable,
                "Заказы",
                "Галерея",
                "Локации",
                "Клиенты",
                "Комнаты",
                "Типы фотосессий",
                "Фотоотчеты"
        );

        textOverPhotoQuantity.setOnKeyReleased(event -> {
            calculatePriceTime();
        });
        textTime.setOnKeyReleased(event -> {
            calculatePrice();
        });
        textSearch.setOnKeyReleased(event -> {
            if (!tableView.isDisable()) {

                ObservableList<String[]> observableListSearchResult = FXCollections.observableArrayList();

                for (int i = 0; i < observableListTableView.size(); i++) {
                    for (int j = 0; j < observableListTableView.get(0).length; j++) {
                        try {
                            if (observableListTableView.get(i)[j].toLowerCase()
                                    .contains(textSearch.getText().toLowerCase())) {
                                observableListSearchResult.add(observableListTableView.get(i));
                                System.out.println(Arrays.toString(observableListTableView.get(i)));
                            }
                        } catch (NullPointerException e) {
                            tableView.getItems().clear();
                        }
                    }
                }

                if (textSearch.getText().equals("")) {
                    tableView.setItems(observableListTableView);
                } else {
                    tableView.setItems(observableListSearchResult);
                }

            }
        });

        comboRoom.setOnAction(event -> {

            if (comboRoom.getSelectionModel().isSelected(0)) {
                comboRoom.getSelectionModel().clearSelection();
                textLocationAddress.setDisable(false);
                textLocationName.setDisable(false);
                room = null;
                labelRoomInfo.setText(". . .");
            } else {
                textLocationAddress.setDisable(true);
                textLocationName.setDisable(true);

                room = databaseConnector.getSql(
                        "select * from rooms where name = '"
                                + comboRoom.getSelectionModel().getSelectedItem() + "'"
                );
                labelRoomInfo.setText("Размеры комнаты: " + room[1][2]
                        + "\nЦена за час: " + room[1][3]
                        + (room[1][4] != null ? "\nЗаметки: " + room[1][4] : ""));
            }
            calculatePriceTime();

        });
        comboType.setOnAction(event -> {

            if (comboType.getSelectionModel().isSelected(0)) {
                comboType.getSelectionModel().clearSelection();
                textOverPhotoQuantity.setVisible(false);
                type = null;
                labelTypeInfo.setText(". . .");
            } else {
                textOverPhotoQuantity.setVisible(true);

                type = databaseConnector.getSql(
                        "select * from types where title = '"
                                + comboType.getSelectionModel().getSelectedItem() + "'"
                );
                labelTypeInfo.setText("\nЦена за час: " + type[1][2]
                        + "\nКол-во фотографий, входящих в пакет: " + type[1][3]
                        + "\nЦена за каждую последующую фотографию: " + type[1][4]);
            }
            calculatePriceTime();

        });
        comboPhotoset.setOnAction(event -> {
            ObservableList<Node> children = vboxScrollPhotosets.getChildren();
            vboxScrollPhotosets.getChildren().removeAll(children);
            if (comboPhotoset.getSelectionModel().isSelected(0)) {
                comboPhotoset.getSelectionModel().clearSelection();
            } else {
                selectPhotoset();
            }
        });

        comboTable.setOnAction(event -> {

            textFieldsAdd = new JFXTextField[]{
                    textAdd1, textAdd2, textAdd3, textAdd4
            };
            comboBoxesAdd = new JFXComboBox[]{
                    comboBoxAdd1, comboBoxAdd2, comboBoxAdd3, comboBoxAdd4
            };

            indexComboBoxesAdd = null;
            promptComboBoxesAdd = null;
            comboBoxesInTable = null;
            columnComboBoxesInTable = null;

            clearAndHide();
            tableView.setDisable(false);

            switch (comboTable.getSelectionModel().getSelectedIndex()) {

                case -1:
                case 0:
                    tableView.setDisable(true);
                    tableView.getColumns().clear();
                    tableView.getItems().clear();
                    comboTable.getSelectionModel().clearSelection();
                    return;

                case 1:
                    sqlGetTable = "select o.id_order, c.\"e-mail\", t.title, o.date, " +
                            "o.time, r.name, l.name, o.quantity_photos, o.price " +
                            "from orders o, clients c, types t, rooms r, locations l " +
                            "where o.id_client = c.id_client " +
                            "and o.id_type = t.id_type " +
                            "and (o.id_room = r.id_room " +
                            "or o.id_location = l.id_location) " +
                            "order by o.date";
                    columnNamesTableView =
                            new String[]{
                                    "orders", "Email клиента", "Тип фотосессии", "Дата",
                                    "Время", "Комната", "Локация", "Кол-во фотографий", "Сумма"
                            };

                    comboBoxesInTable =
                            new ObservableList[]{
                                    setItemsCombo("select \"e-mail\" from clients order by \"e-mail\""),
                                    setItemsCombo("select title from types order by title"),
                                    setItemsCombo("select name from rooms order by name"),
                                    setItemsCombo("select name from locations order by name")
                            };
                    comboBoxesInTable[0].remove(0);
                    comboBoxesInTable[1].remove(0);
                    comboBoxesInTable[2].remove(0);
                    comboBoxesInTable[3].remove(0);
                    columnComboBoxesInTable = new int[]{1, 2, 5, 6};

                    textFieldsAdd = new JFXTextField[]{textAdd1, textAdd2, textAdd3};
                    indexTextFieldsAdd = new int[]{4, 7, 8};
                    promptTextFieldsAdd = new String[]{"Время", "Кол-во фотографий", "Сумма"};

                    comboBoxesAdd = new JFXComboBox[]{comboBoxAdd1, comboBoxAdd2, comboBoxAdd3, comboBoxAdd4};
                    indexComboBoxesAdd = new int[]{1, 2, 5, 6};
                    promptComboBoxesAdd = new String[]{"Клиент", "Тип фотосессии", "Комната", "Локация"};

                    setItemsCombo(comboBoxAdd1, "select \"e-mail\" from clients order by \"e-mail\"");
                    setItemsCombo(comboBoxAdd2, "select title from types order by title");
                    setItemsCombo(comboBoxAdd3, "select name from rooms order by name");
                    setItemsCombo(comboBoxAdd4, "select name from locations order by name");
                    comboBoxAdd1.getItems().remove(0);
                    comboBoxAdd2.getItems().remove(0);
                    comboBoxAdd3.getItems().remove(0);
                    comboBoxAdd4.getItems().remove(0);
                    break;

                case 2:
                    sqlGetTable = "select p.id_photo, ps.title, p.path, p.title " +
                            "from photos p, photosets ps " +
                            "where p.id_photoset = ps.id_photoset " +
                            "order by ps.title, p.path ";
                    columnNamesTableView = new String[]{"photos", "Фотоотчет", "Путь", "Название"};

                    comboBoxesInTable = new ObservableList[]{setItemsCombo("select title from photosets order by title")};
                    comboBoxesInTable[0].remove(0);
                    columnComboBoxesInTable = new int[]{1};

                    textFieldsAdd = new JFXTextField[]{textAdd1, textAdd2};
                    indexTextFieldsAdd = new int[]{2, 3};
                    promptTextFieldsAdd = new String[]{"Путь", "Название"};

                    comboBoxesAdd = new JFXComboBox[]{comboBoxAdd1};
                    indexComboBoxesAdd = new int[]{1};
                    promptComboBoxesAdd = new String[]{"Фотоотчет"};

                    setItemsCombo(comboBoxAdd1, "select title from photosets order by title");
                    comboBoxAdd1.getItems().remove(0);

                    break;

                case 3:
                    sqlGetTable = "select * from locations order by address";
                    columnNamesTableView = new String[]{"locations", "Адрес", "Название"};
                    textFieldsAdd = new JFXTextField[]{textAdd1, textAdd2};
                    indexTextFieldsAdd = new int[]{1, 2};
                    promptTextFieldsAdd = new String[]{"Адрес", "Название"};
                    comboBoxesAdd = null;
                    break;

                case 4:
                    sqlGetTable = "select * from clients order by full_name";
                    columnNamesTableView = new String[]{"clients", "ФИО", "Номер телефона", "Email"};
                    textFieldsAdd = new JFXTextField[]{textAdd1, textAdd2, textAdd3};
                    indexTextFieldsAdd = new int[]{1, 2, 3};
                    promptTextFieldsAdd = new String[]{"ФИО", "Телефон", "Email"};
                    comboBoxesAdd = null;
                    break;

                case 5:
                    sqlGetTable = "select * from rooms order by name";
                    columnNamesTableView = new String[]{"rooms", "Название", "Площадь", "Цена за час", "Заметки"};
                    textFieldsAdd = new JFXTextField[]{textAdd1, textAdd2, textAdd3, textAdd4};
                    indexTextFieldsAdd = new int[]{1, 2, 3, 4};
                    promptTextFieldsAdd = new String[]{"Название", "Площадь", "Цена за час", "Заметки"};
                    comboBoxesAdd = null;
                    break;

                case 6:
                    sqlGetTable = "select * from types order by title";
                    columnNamesTableView = new String[]{
                            "types", "Название", "Цена за час", "Кол-во фотографий", "Цена за доп. фотографию"
                    };
                    textFieldsAdd = new JFXTextField[]{textAdd1, textAdd2, textAdd3, textAdd4};
                    indexTextFieldsAdd = new int[]{1, 2, 3, 4};
                    promptTextFieldsAdd = new String[]{
                            "Название", "Цена за час", "Кол-во фотографий", "Цена за доп. фотографию"
                    };
                    comboBoxesAdd = null;
                    break;

                case 7:
                    sqlGetTable = "select * from photosets order by title";
                    columnNamesTableView = new String[]{"photosets", "Дата", "Описание", "Название"};
                    textFieldsAdd = new JFXTextField[]{textAdd1, textAdd2};
                    indexTextFieldsAdd = new int[]{2, 3};
                    promptTextFieldsAdd = new String[]{"Описание", "Название"};
                    comboBoxesAdd = null;
                    break;

            }

            setTable();
            tableName = tableView.getColumns().get(0).getText();
            showAndSetValue();
            setDisableButton();
            columnNames = databaseConnector.getSql("select * from " + tableName + " limit 0");
            for (int i = 0; i < columnNames[0].length; i++) {
                columnNames[0][i] = "\"" + columnNames[0][i] + "\"";
            }
            observableListTableView = tableView.getItems();

            addRecord();
            editCellTable();
            deleteRecord();

        });

        buttonOrder.setOnAction(event -> {

            if ((textName.getText().isEmpty()
                    || textPhone.getText().isEmpty()
                    || textMail.getText().isEmpty()
                    || datePicker.getValue() == null
                    || timePicker.getValue() == null
                    || textTime.getText().isEmpty()
                    || comboType.getSelectionModel().isEmpty())
                    && (comboRoom.getSelectionModel().isEmpty()
                    || (textLocationAddress.getText().isEmpty()
                    && textLocationName.getText().isEmpty()))
            ) {
                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "Заполните обязательные поля ввода *\n" +
                                "(ФИО, номер телефона, email, дату, время," +
                                "\nколичество часов, вид съемки, комнату или адресс)"
                );
            } else {

                databaseConnector.sql(
                        "select create_order('" + textName.getText() + "', '"
                                + textPhone.getText() + "', '"
                                + textMail.getText() + "', '" +
                                comboType.getSelectionModel().getSelectedItem() + "', " +
                                "to_timestamp('" +
                                datePicker.getValue().toString() + " " + timePicker.getValue().toString() +
                                "', 'YYYY-MM-DD HH24:MI'), " +
                                textTime.getText() + ", " +
                                (comboRoom.getSelectionModel().getSelectedIndex() == 0
                                        ? null
                                        : "'" + comboRoom.getSelectionModel().getSelectedItem() + "'") + ", " +
                                (textLocationAddress.isDisabled() || !comboRoom.getSelectionModel().isEmpty()
                                        ? null
                                        : "'" + textLocationAddress.getText() + "'") + ", " +
                                (textLocationName.isDisabled() || !comboRoom.getSelectionModel().isEmpty()
                                        ? null
                                        : "'" + textLocationName.getText() + "'") + ", " +
                                (textOverPhotoQuantity.getText().isEmpty()
                                        ? "0"
                                        : Integer.parseInt(textOverPhotoQuantity.getText())) + ", " +
                                labelPrice.getText() + ")"
                );

                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "Спасибо за проявленный Вами интерес к нашей фотостудии!\n" +
                                "Мы свяжемся с Вами по указанным контактным данным."
                );
            }

        });
        buttonLogin.setOnAction(event -> {

            if (!adminAccess) {

                try {
                    adminAccess = Boolean.parseBoolean(
                            databaseConnector.getSql(
                                    "select * from check_available_admin_access('" +
                                            DigestUtils.sha1Hex(
                                                    JOptionPane.showInputDialog("Введите пароль:")
                                            ) + "')")[1][0]
                                    .replace("t", "true")
                                    .replace("f", "false"));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            JOptionPane.getRootFrame(),
                            "Неверный пароль!"
                    );
                }
                if (adminAccess) {
                    tabAdmin.setDisable(false);
                    tabAdmin.setText("Администрирование");
                    buttonLogin.setText("Выход");
                }
            } else {
                adminAccess = false;
                tabAdmin.setDisable(true);
                tabAdmin.setText("");
                buttonLogin.setText("Логин");
            }

        });
        buttonBackup.setOnAction(event -> DatabaseBackup.executeCommand("backup"));
        buttonRestore.setOnAction(event -> DatabaseBackup.executeCommand("restore"));

    }

    private void calculatePriceTime() {
        if (!textTime.getText().isEmpty()) {
            calculatePrice();
        } else {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Укажите необходимое количество часов"
            );
        }
    }

    private void calculatePrice() {

        if (!comboType.getSelectionModel().isEmpty()) {
            priceType =
                    Integer.parseInt(textTime.getText())
                            * Double.parseDouble(type[1][2]);
        } else {
            priceType = 0;
        }

        if (!textOverPhotoQuantity.getText().isEmpty()
                && !comboType.getSelectionModel().isEmpty()) {
            try {
                priceOverPhoto =
                        (type[1][3].equals("-1")
                                ? 0
                                : Integer.parseInt(textOverPhotoQuantity.getText()) * Double.parseDouble(type[1][4])
                        );
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                priceOverPhoto = 0;
            }
        } else {
            priceOverPhoto = 0;
        }

        if (!comboRoom.getSelectionModel().isEmpty()) {
            priceRoom = Integer.parseInt(textTime.getText())
                    * Double.parseDouble(room[1][3]);
        } else {
            priceRoom = 0;
        }
        labelPrice.setText(Double.toString(priceType + priceOverPhoto + priceRoom));
    }

    private void selectPhotoset() {

        photos = databaseConnector.getSql(
                "select * from photos where id_photoset = " +
                        "(select id_photoset from photosets where title = '" +
                        comboPhotoset.getSelectionModel().getSelectedItem() + "') " +
                        "order by path"
        );

        /*gridPhotos = new GridPane();
        gridPhotos.setAlignment(Pos.CENTER);
        gridPhotos.setPrefWidth(vboxScrollPhotosets.getPrefWidth());*/

        for (int i = 1, j = 0, k = 0; i < photos.length; i++) {

            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);

            ImageView imageViewPhoto = new ImageView(photos[i][2]);

//            imageViewPhoto.setFitHeight(250);
//            imageViewPhoto.setFitWidth(230);

            imageViewPhoto.setOpacity(0.8);
            imageViewPhoto.setOnMouseEntered(event -> imageViewPhoto.setOpacity(1));
            imageViewPhoto.setOnMouseExited(event -> imageViewPhoto.setOpacity(0.8));

            imageViewPhoto.setFitWidth(imageViewPhoto.getFitWidth() / 2);
            imageViewPhoto.setFitHeight(imageViewPhoto.getFitHeight() / 2);

            /*if (j == 1) {
                j = 0;
                k++;
            }
            gridPhotos.add(imageViewPhoto, j++, k);*/

            hBox.getChildren().add(imageViewPhoto);
            vboxScrollPhotosets.getChildren().add(hBox);
        }
        vboxScrollPhotosets.setSpacing(5);

//        vboxScrollPhotosets.getChildren().addAll(gridPhotos);
//        vboxScrollPhotosets.getChildren().get(0).setLayoutX(5.5);
//        vboxScrollPhotosets.setPrefHeight(gridPhotos.getPrefHeight());

    }

    private void showAndSetValue() {

        textSearch.setDisable(false);

        boolean isTextField = textFieldsAdd != null,
                isComboBox = comboBoxesAdd != null,
                isTimestamp = false,
                isImageView = false;

        if (isTextField) {
            int i = 0;
            for (JFXTextField textField : textFieldsAdd) {
                textField.setDisable(false);
                textField.setVisible(true);
                textField.setPromptText(promptTextFieldsAdd[i++]);
            }
        }

        if (isComboBox) {
            int i = 0;
            for (JFXComboBox comboBox : comboBoxesAdd) {
                comboBox.setDisable(false);
                comboBox.setVisible(true);
                comboBox.setPromptText(promptComboBoxesAdd[i++]);
            }
        }

        if (tableName.equals("orders")
                || tableName.equals("photosets")) {
            isTimestamp = true;
            datePickerAdd.setDisable(false);
            datePickerAdd.setVisible(true);
            timePickerAdd.setDisable(false);
            timePickerAdd.setVisible(true);
        }

        if (tableName.equals("photos")) {
            isImageView = true;
            imageViewAdd.setDisable(false);
            imageViewAdd.setVisible(true);
        }

        boolean finalIsTimestamp = isTimestamp;
        boolean finalIsImageView = isImageView;
        tableView.setOnMouseClicked(action -> {

            if (!tableView.getSelectionModel().isEmpty()) {
                buttonAdd.setDisable(false);
                buttonDelete.setDisable(false);

                if (isTextField) {

                    for (int t = 0; t < textFieldsAdd.length; t++) {
                        for (int j = 0; j < tableView.getColumns().size(); j++) {
                            if (j == indexTextFieldsAdd[t]) {
                                textFieldsAdd[t].setText(tableView.getSelectionModel().getSelectedItem()[j]);
                            }
                        }
                    }

                } else {
                    buttonAdd.setDisable(true);
                }

                if (isComboBox) {

                    for (int c = 0; c < comboBoxesAdd.length; c++) {
                        for (int j = 0; j < tableView.getColumns().size(); j++) {
                            if (j == indexComboBoxesAdd[c]) {
                                comboBoxesAdd[c].setValue(tableView.getSelectionModel().getSelectedItem()[j]);
                            }
                        }
                    }

                }

                if (finalIsTimestamp) {
                    if (tableName.equals("photosets")) {
                        datePickerAdd.setValue(LocalDate.parse(
                                tableView.getSelectionModel().getSelectedItem()[1]
                                        .substring(0, 10)
                                )
                        );
                        timePickerAdd.setValue(LocalTime.parse(
                                tableView.getSelectionModel().getSelectedItem()[1]
                                        .substring(11, 19)
                                )
                        );
                    }
                    if (tableName.equals("orders")) {
                        datePickerAdd.setValue(LocalDate.parse(
                                tableView.getSelectionModel().getSelectedItem()[3]
                                        .substring(0, 10)
                                )
                        );
                        timePickerAdd.setValue(LocalTime.parse(
                                tableView.getSelectionModel().getSelectedItem()[3]
                                        .substring(11, 19)
                                )
                        );
                    }
                }

                if (finalIsImageView) {
                    if (tableName.equals("photos")) {
                        imageViewAdd.setImage(
                                new Image(tableView.getSelectionModel().getSelectedItem()[2])
                        );
                    }
                }

            }

        });
    }

    private void setDisableButton() {

        if (textFieldsAdd != null) {
            for (JFXTextField textField1 : textFieldsAdd) {
                textField1.setOnKeyReleased(event -> {

                    for (JFXTextField textField2 : textFieldsAdd) {
                        if (textField2.getText().isEmpty()) {
                            buttonAdd.setDisable(true);
                            break;
                        }
                        buttonAdd.setDisable(false);
                    }

                });
            }
        }

    }

    private void clearAndHide() {
        textSearch.clear();
        textSearch.setDisable(true);
        buttonAdd.setDisable(true);
        buttonDelete.setDisable(true);
        for (JFXComboBox comboBox : comboBoxesAdd) {
            comboBox.getItems().clear();
            comboBox.setPromptText("");
            comboBox.setDisable(true);
            comboBox.setVisible(false);
        }
        for (JFXTextField textField : textFieldsAdd) {
            textField.clear();
            textField.setPromptText("");
            textField.setDisable(true);
            textField.setVisible(false);
        }
        datePickerAdd.setValue(null);
        timePickerAdd.setValue(null);
        datePickerAdd.setDisable(true);
        datePickerAdd.setVisible(false);
        timePickerAdd.setDisable(true);
        timePickerAdd.setVisible(false);
        imageViewAdd.setImage(null);
        imageViewAdd.setDisable(true);
        imageViewAdd.setVisible(false);
    }

    private void setTable() {

        tableView.getColumns().clear();
        tableView.getItems().clear();

        String[][] records = databaseConnector.getSql(sqlGetTable);

        for (int i = 0, j = 0; i < records[0].length; i++) {
            TableColumn<String[], String> tableColumn = new TableColumn<>(columnNamesTableView[i]);
            final int col = i;
            tableColumn.setCellValueFactory(
                    (TableColumn.CellDataFeatures<String[], String> param) -> new SimpleStringProperty(param.getValue()[col]));

            boolean combo = false;

            if (comboBoxesInTable == null && columnComboBoxesInTable == null) {
                tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                tableColumn.setEditable(true);
            } else {
                for (int indexColumn : columnComboBoxesInTable)
                    if (i == indexColumn) {
                        combo = true;
                        break;
                    }
            }
            if (combo) {
                tableColumn.setCellFactory(ComboBoxTableCell.forTableColumn(comboBoxesInTable[j++]));
            } else {
                tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            }
            tableView.getColumns().add(tableColumn);
        }

        ObservableList<String[]> items = FXCollections.observableArrayList(records);

        tableView.setItems(items);
        tableView.getItems().remove(0);
        tableView.getColumns().get(0).setVisible(false);
        tableView.setEditable(true);

    }

    private void setItemsCombo(
            JFXComboBox<String> comboBox,
            @Language("SQL") String sqlGetColumn
    ) {

        ObservableList<String[]> observableList =
                FXCollections.observableArrayList(
                        databaseConnector.getSql(sqlGetColumn)
                );

        comboBox.getItems().add("- выбор -");
        for (int i = 1; i < observableList.size(); i++) {
            comboBox.getItems().add(observableList.get(i)[0]);
        }

    }

    private void setItemsCombo(
            JFXComboBox<String> comboBox,
            String... values
    ) {

        comboBox.getItems().add("- выбор -");
        for (String value : values) {
            comboBox.getItems().add(value);
        }

    }

    private ObservableList<String> setItemsCombo(@Language("SQL") String sql) {
        String[][] records = databaseConnector.getSql(sql);
        ObservableList<String> result = FXCollections.observableArrayList();

        for (String[] record : records) {
            result.add(record[0]);
        }

        return result;
    }

    private void addRecord() {
        buttonAdd.setOnAction(event -> {
            String values = "";
            if (comboBoxesAdd != null) {
                for (JFXComboBox comboBoxAdd : comboBoxesAdd) {

                    switch (comboBoxAdd.getPromptText()) {
                        case "Клиент":
                            values = values.concat(
                                    "(select \"id_client\" from clients where \"e-mail\" = '" +
                                            comboBoxAdd.getSelectionModel().getSelectedItem() + "'), ' ");
                            break;
                        case "Тип фотосессии":
                            values = values.concat(
                                    "(select \"id_type\" from types where \"title\" = '" +
                                            comboBoxAdd.getSelectionModel().getSelectedItem() + "'), ' ");
                            break;
                        case "Комната":
                            values = values.concat(
                                    "(select \"id_room\" from rooms where \"name\" = '" +
                                            comboBoxAdd.getSelectionModel().getSelectedItem() + "'), ' ");
                            break;
                        case "Локация":
                            values = values.concat(
                                    "(select \"id_location\" from locations where \"name\" = '" +
                                            comboBoxAdd.getSelectionModel().getSelectedItem() + "'), ' ");
                            break;
                        case "Фотоотчет":
                            values = values.concat(
                                    "(select \"id_photoset\" from photosets where \"title\" = '" +
                                            comboBoxAdd.getSelectionModel().getSelectedItem() + "'), ' ");
                            break;
                    }

                }
            }
            if (datePickerAdd.getValue() != null
                    && timePickerAdd.getValue() != null) {
                values = values.concat(
                        datePickerAdd.getValue().toString() + " " +
                                timePickerAdd.getValue().toString() + "', ' "
                );
            }
            if (textFieldsAdd != null) {
                for (JFXTextField textFieldAdd : textFieldsAdd) {
                    values = values.concat(textFieldAdd.getText() + "', '");
                }
            }
            if (!values.isEmpty()) {
                values = values.substring(0, values.length() - 4);
                String columns;
                if (tableName.equals("orders")) {
                    columns = "\"id_client\", \"id_type\", \"date\", \"time\", \"id_room\", " +
                            "\"id_location\", \"quantity_photos\", \"price\"";
                } else {
                    columns = Arrays.toString(columnNames[0]);
                }
                String sql =
                        "insert into " + tableName
                                + "(" + columns.substring(columns.indexOf(" ") + 1, columns.length() - 1) + ")"
                                + " values('" + values
                                .replaceAll("'null'", "null")
                                .replaceAll("''", "null")
                                + "')";

                if (comboBoxesAdd != null) {
                    sql = sql.replace("values('", "values(");
                }

                databaseConnector.sql(sql);

                switch (tableName) {
                    case "photos":
                        tableView.getItems().add(
                                new String[]{
                                        databaseConnector.getSql(
                                                "select max(id_photo) from photos"
                                        )[0][0],
                                        comboBoxAdd1.getSelectionModel().getSelectedItem(),
                                        textAdd1.getText(),
                                        textAdd2.getText()
                                }
                        );
                        tableView.refresh();
                        break;
                    case "orders":
                        tableView.getItems().add(
                                new String[]{
                                        databaseConnector.getSql(
                                                "select max(id_order) from orders"
                                        )[0][0],
                                        comboBoxAdd1.getSelectionModel().getSelectedItem(),
                                        comboBoxAdd2.getSelectionModel().getSelectedItem(),
                                        datePickerAdd.getValue().toString() + " " +
                                                timePickerAdd.getValue().toString(),
                                        textAdd1.getText(),
                                        comboBoxAdd3.getSelectionModel().getSelectedItem(),
                                        comboBoxAdd4.getSelectionModel().getSelectedItem(),
                                        textAdd2.getText(),
                                        textAdd3.getText()
                                }
                        );
                        tableView.refresh();
                        break;
                    default:
                        tableView.getItems().add(
                                databaseConnector.getSql(
                                        "select * from " + tableName
                                                + " order by " + columnNamesTableView[0] + " desc limit 1"
                                )[1]
                        );
                        break;
                }


                if (textFieldsAdd != null) {
                    for (JFXTextField textFieldAdd : textFieldsAdd) {
                        textFieldAdd.clear();
                    }
                }
                if (comboBoxesAdd != null) {
                    for (JFXComboBox comboBoxAdd : comboBoxesAdd) {
                        comboBoxAdd.getSelectionModel().clearSelection();
                    }
                }
                if (textFieldsAdd != null) {
                    datePickerAdd.setValue(null);
                    timePickerAdd.setValue(null);
                }

            }

        });
    }

    private void editCellTable() {
        for (int i = 1; i < tableView.getColumns().size(); i++) {
            int finalI = i;
            tableView.getColumns().get(i).setOnEditCommit(actionEdit -> {
                tableView.getFocusModel().getFocusedItem()[finalI] = String.valueOf(actionEdit.getNewValue());
                String value = "'" + tableView.getFocusModel().getFocusedItem()[finalI] + "'";
                switch (columnNamesTableView[0]) {

                    case "photos":
                        value = new String[]{
                                value,
                                "(select id_photoset from photosets " +
                                        "where \"title\" = '"
                                        + tableView.getFocusModel().getFocusedItem()[finalI]
                                        + "')",
                                value,
                                value
                        }[finalI];
                        break;

                    case "orders":
                        value = new String[]{
                                value,
                                "(select id_client from clients " +
                                        "where \"e-mail\" = '"
                                        + tableView.getFocusModel().getFocusedItem()[finalI]
                                        + "')",
                                "(select id_type from types " +
                                        "where title = '"
                                        + tableView.getFocusModel().getFocusedItem()[finalI]
                                        + "')",
                                value,
                                value,
                                "(select id_room from rooms " +
                                        "where name = '"
                                        + tableView.getFocusModel().getFocusedItem()[finalI]
                                        + "')",
                                "(select id_location from locations " +
                                        "where name = '"
                                        + tableView.getFocusModel().getFocusedItem()[finalI]
                                        + "')",
                                value,
                                value
                        }[finalI];
                        break;

                    default:
                        value = "'" + tableView.getFocusModel().getFocusedItem()[finalI] + "'";
                        break;
                }
                databaseConnector.sql("update " + tableName
                        + " set " + columnNames[0][finalI] + " = " + value
                        + " where " + columnNames[0][0] + " = " + tableView.getFocusModel().getFocusedItem()[0]);
            });
        }
    }

    private void deleteRecord() {
        buttonDelete.setOnAction(eventDelete -> {
            if (!tableView.getSelectionModel().isEmpty()) {
                databaseConnector.sql(
                        "delete from " + tableName
                                + " where " + columnNames[0][0]
                                + " = " + tableView.getSelectionModel().getSelectedItem()[0]);
                tableView.getItems().remove(tableView.getSelectionModel().getSelectedIndex());
                tableView.getSelectionModel().clearSelection();
                buttonDelete.setDisable(true);
            }
        });
    }

}
