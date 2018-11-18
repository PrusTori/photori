package com.viktoriia.photori.fxapp.app;


import com.jfoenix.controls.*;

import com.viktoriia.photori.db.DatabaseConnector;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import org.intellij.lang.annotations.Language;

public class ControllerPhotori {

    @FXML
    private JFXTextField textName, textPhone, textMail, textTime,
            textLocationAddress, textLocationName, textOverPhotoQuantity;

    @FXML
    private Label labelTypeInfo, labelRoomInfo, labelPrice;

    @FXML
    private JFXComboBox<String> comboType, comboRoom, comboPhotoset;

    @FXML
    private JFXDatePicker date;

    @FXML
    private JFXTimePicker time;

    @FXML
    private JFXButton buttonOrder;

    @FXML
    private AnchorPane anchor;

    private DatabaseConnector databaseConnector;

    private double priceType = 0,
            priceOverPhoto = 0,
            priceRoom = 0;

    String[][] type, room;

    public void initialize()
            throws SQLException, ClassNotFoundException {

        databaseConnector = new DatabaseConnector();

        comboRoom.setItems(setItemsCombo("select name from rooms"));
        comboType.setItems(setItemsCombo("select title from types"));

        comboRoom.setOnAction(event -> {

            if (comboRoom.getSelectionModel().isSelected(0)) {
                comboRoom.getSelectionModel().clearSelection();
                textLocationAddress.setDisable(false);
                textLocationName.setDisable(false);
                room = null;
            } else {
                textLocationAddress.setDisable(true);
                textLocationName.setDisable(true);

                room = databaseConnector.getSql(
                        "select * from rooms where name = '"
                                + comboRoom.getSelectionModel().getSelectedItem() + "'"
                );
            }
            calculatePriceTime();

        });
        comboType.setOnAction(event -> {

            if (comboType.getSelectionModel().isSelected(0)) {
                comboType.getSelectionModel().clearSelection();
                textOverPhotoQuantity.setVisible(false);
                type = null;
            } else {
                textOverPhotoQuantity.setVisible(true);

                type = databaseConnector.getSql(
                        "select * from types where title = '"
                                + comboType.getSelectionModel().getSelectedItem() + "'"
                );
            }
            calculatePriceTime();

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
                    || date.getValue() == null
                    || time.getValue() == null
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
                                date.getValue().toString() + " " + time.getValue().toString() +
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

        if (!textOverPhotoQuantity.getText().isEmpty()) {
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

}