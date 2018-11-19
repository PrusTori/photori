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
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import javafx.scene.layout.GridPane;
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
    private AnchorPane anchorScrollPhotosets;

    private DatabaseConnector databaseConnector;

    private double priceType = 0,
            priceOverPhoto = 0,
            priceRoom = 0;

    String[][] type, room, photosets, photos;

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

                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "Спасибо за проявленный Вами интерес к нашей фотостудии!\n" +
                                "Мы свяжемся с Вами по указанным контактным данным."
                );
            }

        });

        photosets = databaseConnector.getSql(
                "select * from photosets"
        );

        photos = databaseConnector.getSql(
                "select * from photos"
        );

        Label labelPhotosetTitle1 = new Label(photosets[1][3]);
        ImageView imageViewPhoto1 = new ImageView(photos[1][2]);
        imageViewPhoto1.setFitHeight(150);
        imageViewPhoto1.setFitWidth(230);
        imageViewPhoto1.setOpacity(0.75);
        imageViewPhoto1.setOnMouseEntered(event -> imageViewPhoto1.setOpacity(1));
        imageViewPhoto1.setOnMouseExited(event -> imageViewPhoto1.setOpacity(0.75));
        ImageView imageViewPhoto2 = new ImageView(photos[2][2]);
        imageViewPhoto2.setFitHeight(150);
        imageViewPhoto2.setFitWidth(230);
        imageViewPhoto2.setOpacity(0.75);
        imageViewPhoto2.setOnMouseEntered(event -> imageViewPhoto2.setOpacity(1));
        imageViewPhoto2.setOnMouseExited(event -> imageViewPhoto2.setOpacity(0.75));
        ImageView imageViewPhoto3 = new ImageView(photos[3][2]);
        imageViewPhoto3.setFitHeight(150);
        imageViewPhoto3.setFitWidth(230);
        imageViewPhoto3.setOpacity(0.75);
        imageViewPhoto3.setOnMouseEntered(event -> imageViewPhoto3.setOpacity(1));
        imageViewPhoto3.setOnMouseExited(event -> imageViewPhoto3.setOpacity(0.75));
        GridPane gridPhotos = new GridPane();
        gridPhotos.add(labelPhotosetTitle1, 1, 0);
        gridPhotos.add(imageViewPhoto1, 0, 1);
        gridPhotos.add(imageViewPhoto2, 1, 1);
        gridPhotos.add(imageViewPhoto3, 2, 1);
//        gridPhotos.getChildren().get(0).setLayoutX(100);

        anchorScrollPhotosets.getChildren().addAll(gridPhotos);
        anchorScrollPhotosets.getChildren().get(0).setLayoutX(5);

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

}