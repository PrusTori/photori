package com.viktoriia.photori.fxapp.app;


import com.jfoenix.controls.*;

import com.viktoriia.photori.db.DatabaseConnector;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import org.intellij.lang.annotations.Language;


public class ControllerPhotori {

    @FXML
    private Label labelTypeInfo, labelRoomInfo, labelPrice;

    @FXML
    private JFXTextField textName, textPhone, textMail, textTime,
            textLocationAddress, textLocationName, textOverPhotoQuantity,
            textAdd1, textAdd2, textAdd3, textAdd4;

    @FXML
    private JFXComboBox<String> comboType, comboRoom, comboPhotoset, comboTable;

    @FXML
    private JFXDatePicker datePicker;

    @FXML
    private JFXTimePicker timePicker;

    @FXML
    private JFXButton buttonOrder, buttonAdd, buttonDelete;

    @FXML
    private VBox vboxScrollPhotosets;

    @FXML
    private GridPane gridPhotos;

    @FXML
    private TableView<String[]> tableView;

    private DatabaseConnector databaseConnector;

    private double priceType = 0,
            priceOverPhoto = 0,
            priceRoom = 0;

    String[][] type, room, photosets, photos;

    public void initialize()
            throws SQLException, ClassNotFoundException {

        databaseConnector = new DatabaseConnector();

        comboRoom.setItems(setItemsCombo("select name from rooms order by name"));
        comboType.setItems(setItemsCombo("select title from types order by title"));
        comboPhotoset.setItems(setItemsCombo("select title from photosets order by title"));

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
//            anchorScrollPhotosets.getChildren().removeAll(gridPhotos);
            if (comboPhotoset.getSelectionModel().isSelected(0)) {
                comboPhotoset.getSelectionModel().clearSelection();
            } else {
                selectPhotoset();
            }
        });

        textOverPhotoQuantity.setOnKeyReleased(event -> {
            calculatePriceTime();
        });
        textTime.setOnKeyReleased(event -> {
            calculatePrice();
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

        comboTable.setItems(
                FXCollections.observableArrayList(
                        "- выбор -",
                        "Клиенты",
                        "Локации",
                        "Комнаты",
                        "Типы фотосетов",
                        "Фотосеты",
                        "Фотографии",
                        "Заказы"
                )
        );
        comboTable.setOnAction(event -> {

            switch (comboTable.getSelectionModel().getSelectedIndex()) {

                case 0:

                    break;

                case 1:

                    setTable(
                            "select * from clients order by full_name",
                            new String[]{"id_client", "full_name", "phone", "e-mail"},
                            null, null
                    );
                    showAndSetValue(
                            new JFXTextField[]{textAdd1, textAdd2, textAdd3},
                            new int[]{1, 2, 3},
                            new String[]{"ФИО", "Телефон", "Email"},
                            null, null, null
                    );

                    break;

                case 2:

                    break;

                case 3:

                    break;

                case 4:

                    break;

                case 5:

                    break;

                case 6:

                    break;

                case 7:

                    break;

            }

        });


    }

    private ObservableList<String> setItemsCombo(
            @Language("SQL") String sqlGenTable
    ) {

        String[] values = new String[FXCollections.observableArrayList(
                databaseConnector.getSql(sqlGenTable)
        ).size() - 1];

        for (int i = 1; i <= values.length; i++)
            values[i - 1] = FXCollections.observableArrayList(
                    databaseConnector.getSql(sqlGenTable)
            ).get(i)[0];

        ObservableList<String> list = FXCollections.observableArrayList("- выбор -");
        list.addAll(FXCollections.observableArrayList(
                new HashSet<String>(Arrays.asList(values))
        ).sorted());

        return list;
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

        /*photos = databaseConnector.getSql(
                "select * from photos where id_photoset = " +
                        "(select id_photoset from photosets where title = '" +
                        comboPhotoset.getSelectionModel().getSelectedItem() + "') " +
                        "order by path"
        );

        gridPhotos = new GridPane();
        gridPhotos.setAlignment(Pos.CENTER);
        gridPhotos.setPrefWidth(anchorScrollPhotosets.getPrefWidth());

        for (int i = 1, j = 0, k = 0; i < photos.length; i++) {

            ImageView imageViewPhoto = new ImageView(photos[i][2]);

//            imageViewPhoto.setFitHeight(250);
//            imageViewPhoto.setFitWidth(230);

            imageViewPhoto.setOpacity(0.8);
            imageViewPhoto.setOnMouseEntered(event -> imageViewPhoto.setOpacity(1));
            imageViewPhoto.setOnMouseExited(event -> imageViewPhoto.setOpacity(0.8));

            if (j == 1) {
                j = 0;
                k++;
            }
            gridPhotos.add(imageViewPhoto, j++, k);

        }

        anchorScrollPhotosets.getChildren().addAll(gridPhotos);
        anchorScrollPhotosets.getChildren().get(0).setLayoutX(5.5);
        anchorScrollPhotosets.setPrefHeight(gridPhotos.getPrefHeight());*/

    }

    private void setOrientation() {

    }


    private void setTable(@Language("SQL") String sql, String[] colName,
                          ObservableList<String>[] comboItems, int[] indexColumnCombo) {

        tableView.getColumns().clear();
        tableView.getItems().clear();

        String[][] records = databaseConnector.getSql(sql);

        for (int i = 0, j = 0; i < records[0].length; i++) {
            TableColumn<String[], String> tableColumn = new TableColumn<>(colName[i]);
            final int col = i;
            tableColumn.setCellValueFactory(
                    (TableColumn.CellDataFeatures<String[], String> param) -> new SimpleStringProperty(param.getValue()[col]));

            boolean combo = false;

            if (comboItems == null && indexColumnCombo == null) {
                tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                tableColumn.setEditable(true);
            } else {
                for (int indexColumn : indexColumnCombo)
                    if (i == indexColumn) {
                        combo = true;
                        break;
                    }
            }
            if (combo) {
                tableColumn.setCellFactory(ComboBoxTableCell.forTableColumn(comboItems[j++]));
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


    private void showAndSetValue(JFXTextField[] textFields, int[] indexFields, String[] promptFields,
                                 JFXComboBox[] comboBoxes, int[] indexCombos, String[] promptCombos) {

        boolean isTextField = textFields != null && indexFields != null,
                isCombo = comboBoxes != null && indexCombos != null;

        if (isTextField) {
            int i = 0;
            for (JFXTextField textField : textFields) {
                textField.setDisable(false);
                textField.setVisible(true);
                textField.setPromptText(promptFields[i++]);
            }
        }

        if (isCombo) {
            int i = 0;
            for (JFXComboBox comboBox : comboBoxes) {
                comboBox.setDisable(false);
                comboBox.setVisible(true);
                comboBox.setPromptText(promptCombos[i++]);
            }
        }

        tableView.setOnMouseClicked(action -> {

            if (!tableView.getSelectionModel().isEmpty()) {
                buttonAdd.setDisable(false);
                buttonDelete.setDisable(false);

                if (isTextField) {

                    for (int t = 0; t < textFields.length; t++) {
                        for (int j = 0; j < tableView.getColumns().size(); j++) {
                            if (j == indexFields[t]) {
                                textFields[t].setText(tableView.getSelectionModel().getSelectedItem()[j]);
                            }
                        }
                    }

                } else {
                    buttonAdd.setDisable(true);
                }

                if (isCombo) {

                    for (int c = 0; c < comboBoxes.length; c++) {
                        for (int j = 0; j < tableView.getColumns().size(); j++) {
                            if (j == indexCombos[c]) {
                                comboBoxes[c].setValue(tableView.getSelectionModel().getSelectedItem()[j]);
                            }
                        }
                    }

                }

            }

        });
    }


    private void editCellTable(String tableName) {

        for (int i = 1; i < tableView.getColumns().size(); i++) {
            int finalI = i;
            tableView.getColumns().get(i).setOnEditCommit(actionEdit -> {
                tableView.getFocusModel().getFocusedItem()[finalI] = String.valueOf(actionEdit.getNewValue());

                /*boolean update = databaseConnector.sql("update " + tableName
                        + "," + finalI + "," + tableView.getFocusModel().getFocusedItem()[finalI]
                        + "," + tableView.getFocusModel().getFocusedItem()[0]);
*/
            });

        }
    }


}