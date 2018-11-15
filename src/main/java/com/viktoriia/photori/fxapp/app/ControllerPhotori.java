package com.viktoriia.photori.fxapp.app;

import com.jfoenix.controls.*;
import com.viktoriia.photori.db.DatabaseConnector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import javax.swing.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

import org.intellij.lang.annotations.Language;

public class ControllerPhotori {

    @FXML
    private JFXTextField textName, textPhone, textMail, textTime, textLocation, textQuantity;

    @FXML
    private Label label1, label2, label3, label4, label5, label6, labelPrice;

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

    public void initialize() throws SQLException, ClassNotFoundException {

        databaseConnector = new DatabaseConnector();

        comboType.setItems(setItemsCombo("select title from types"));
        comboRoom.setItems(setItemsCombo("select name from rooms"));

        buttonOrder.setOnAction(event -> {

            if (textName.getText().isEmpty()
                    || textPhone.getText().isEmpty()
                    || textMail.getText().isEmpty()
                    || date.getValue() == null
                    || time.getValue() == null
                    || textTime.getText().isEmpty()
                    || comboType.getSelectionModel().isEmpty()
                    || (comboRoom.getSelectionModel().isEmpty()
                    || textLocation.getText().isEmpty())
            ) {

                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "Заполните обязательные поля ввода *\n" +
                                "(ФИО, номер телефона, email, дату, время," +
                                "\nколичество часов, комнату или адресс, вид съемки)"
                );

            } else {

                databaseConnector.sql(
                        "select create_order(" + textMail.getText() + ", " +
                                comboType.getSelectionModel().getSelectedItem() + ", " +
                                date.getValue().toString() + " " + time.getValue().toString() + ", " +
                                textTime.getText() + ", " +
                                comboRoom.getSelectionModel().getSelectedItem() + ", " +
                                textLocation.getText() + ", " +
                                textQuantity.getText() + ", " +
                                labelPrice.getText() + ")");
            }

        });


    }

    private ObservableList<String> setItemsCombo(@Language("SQL") String sqlGenTable) {
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

}