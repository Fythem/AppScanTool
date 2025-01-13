package com.fythem.AppScanTool;

import com.fythem.AppScanTool.controllers.ScanAppControllerV2;
import com.fythem.AppScanTool.utils.ExportAppResultCSV;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class MainView {
    @FXML
    private AnchorPane root;

    @FXML
    public TextArea appBaseInfoTA;

    @FXML
    public TableView appScanResTV;

    @FXML
    private TableColumn<?, ?> appScanResItemCol;

    @FXML
    private TableColumn<?, ?> appScanResInfoCol;

    @FXML
    private TableColumn<?, ?> appScanResRiskCol;

    @FXML
    private Button appScanBtn;

    @FXML
    private ProgressBar appScanPB;

    @FXML
    private Text appScanPBText;


    ScanAppControllerV2 scanAppControllerV2 = new ScanAppControllerV2();

    @FXML
    void exportAppScanResult(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save AppScanResult as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", new String[]{"*.csv"}));
        File file = fileChooser.showSaveDialog(this.appScanResTV.getScene().getWindow());
        if (file != null) (new ExportAppResultCSV()).exportAppResToCSV(this.appScanResTV, file.getAbsolutePath());
    }

    public void appScanImportV2(ActionEvent actionEvent) {
        File app_file = appChooseV2();
        if (app_file.exists()) {
            scanAppControllerV2.startScan(app_file, this.appBaseInfoTA);
        }
    }

    public void appScanV2(ActionEvent actionEvent) {
        if (this.appBaseInfoTA.getText().trim().isEmpty()) return;
        scanAppControllerV2.start(this.appBaseInfoTA, 1, this.appScanResTV, this.appScanPB, this.appScanPBText, appScanBtn, appScanResItemCol, appScanResInfoCol, appScanResRiskCol);
    }

    private File appChooseV2() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("App files", new String[]{"*.apk", "*.ipa", "*.hap"});
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(this.root.getScene().getWindow());
        return file;
    }
}
