package com.viktoriia.photori.fxapp.admin;

import com.viktoriia.photori.db.DatabaseConnector;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.intellij.lang.annotations.Language;

import javax.swing.*;

public class ControllerAdmin {

    private DatabaseConnector databaseConnector;

    public void initialize(){

    }

    private void createTable(
            TableView<String[]> table,
            @Language("SQL") String sqlResultTable,
            String[] collName,
            String errorMessage
    ) {
        table.getColumns().remove(0, table.getColumns().size());
        int length = databaseConnector.getSql(sqlResultTable)[0].length;
        System.out.println(length);
        for (int i = 1; i < length; i++) {
            TableColumn<String[], String> tableColumn = new TableColumn<>(collName[i]);
            final int coll = i;
            tableColumn.setCellValueFactory(
                    (TableColumn.CellDataFeatures<String[], String> param) ->
                            new SimpleStringProperty(param.getValue()[coll])
            );
            table.getColumns().add(tableColumn);
        }
        table.setItems(FXCollections.observableArrayList(databaseConnector.getSql(sqlResultTable)));
        table.getItems().remove(0);
        if (table.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), errorMessage);
        }
    }
}
