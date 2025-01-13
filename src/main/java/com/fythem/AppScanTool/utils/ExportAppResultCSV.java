package com.fythem.AppScanTool.utils;

import com.fythem.AppScanTool.entity.PentestInfo;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ExportAppResultCSV {
    public void exportResToCSV(TableView tableView, String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        if (getOSType() == 2)
            osw = new OutputStreamWriter(fos, "GBK");
        StringBuilder sb = new StringBuilder();
        sb.append("Item").append(",");
        sb.append("Info").append(",");
        sb.append("Risk").append(",");
        sb.append("\n");
        ObservableList<TableColumn<PentestInfo, ?>> columns = tableView.getColumns();
        ObservableList<PentestInfo> data = tableView.getItems();
        for (PentestInfo item : data) {
            for (TableColumn<PentestInfo, ?> column : columns) {
                Object cellValue = column.getCellData(item);
                sb.append(cellValue.toString().replace(",", "")).append(",");
            }
            sb.append("\n");
        }
        osw.write(sb.toString());
        osw.close();
    }

    public void exportAppResToCSV(TableView tableView, String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        if (getOSType() == 2)
            osw = new OutputStreamWriter(fos, "GBK");
        StringBuilder sb = new StringBuilder();
        sb.append("Item").append(",");
        sb.append("Info").append(",");
        sb.append("Risk").append(",");
        sb.append("\n");
        ObservableList<TableColumn<PentestInfo, ?>> columns = tableView.getColumns();
        ObservableList<PentestInfo> data = tableView.getItems();
        for (PentestInfo item : data) {
            for (TableColumn<PentestInfo, ?> column : columns) {
                Object cellValue = column.getCellData(item);
                sb.append(cellValue.toString().replace(",", "][")).append(",");
            }
            sb.append("\n");
        }
        osw.write(sb.toString());
        osw.close();
    }

    public static int getOSType() {
        String OS_NAME = System.getProperties().getProperty("os.name").toUpperCase();
        if (OS_NAME.contains("WINDOWS"))
            return 1;
        if (OS_NAME.contains("MAC"))
            return 2;
        return -1;
    }
}
