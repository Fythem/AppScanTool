package com.fythem.AppScanTool.controllers;

import com.fythem.AppScanTool.entity.ApkInfoV2;
import com.fythem.AppScanTool.entity.HapInfoV1;
import com.fythem.AppScanTool.entity.IpaInfoV2;
import com.fythem.AppScanTool.entity.PentestInfo;
import com.fythem.AppScanTool.utils.ApkUtilV2;
import com.fythem.AppScanTool.utils.HapUtilV1;
import com.fythem.AppScanTool.utils.IpaUtilV2;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanAppControllerV2 {

    double completed = 0.0D;
    double taskNum = 0.0D;


    private DoubleProperty progress;

    String Android = "Android";
    String iOS = "iOS";
    String HarmonyNext = "HarmonyNext";

    List<String> black_files_tail = Arrays.asList(".png", ".jpg", ".gif", ".mp3", ".mp4");

    private ObservableList<PentestInfo> data = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    private static TableView appScanResTV;

    public class WrapTableCell<S, T> extends TableCell<S, T> {
        private final Text text;

        public WrapTableCell() {
            this.text = new Text();
            this.text.wrappingWidthProperty().bind(this.widthProperty());   // 自动换行
            this.text.setTextAlignment(TextAlignment.CENTER);   // 居中
            this.text.textProperty().bind(this.itemProperty().asString());
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setGraphic(null);
            } else {
                setGraphic(text);
            }
        }
    }


    public void startScan(File app_file, TextArea appBaseInfoTA) {
        System.out.println("[ScanAppControllerV2_startScan]");
        String fileName = app_file.getName();
        String type = fileName.substring(fileName.lastIndexOf("."));

        appBaseInfoTA.setText("文件名: " + app_file.getName());

        switch (type) {
            case ".apk":
                System.out.println("[ScanAppControllerV2_startScan] " + fileName + " for Android");
                try {
                    ApkUtilV2 apkUtilV2 = new ApkUtilV2();
                    String apk_out = apkUtilV2.decompile(app_file);
                    System.out.println("[ScanAppControllerV2_startScan] apk_out = " + apk_out);
                    appBaseInfoTA.appendText("\n应用类型: " + Android);
                    appBaseInfoTA.appendText("\n解包路径: " + apk_out + "\n");
                } catch (Exception e) {
                    System.out.println("[ScanAppControllerV2_startScan] " + fileName + " Scan Error");
                    e.printStackTrace();
                }
                break;
            case ".ipa":
                System.out.println("[ScanAppControllerV2_startScan] " + fileName + " for iOS");
                try {
                    IpaUtilV2 ipaUtilV2 = new IpaUtilV2();
                    String ipa_out = ipaUtilV2.unzip(app_file);
                    System.out.println("[ScanAppControllerV2_startScan] ipa_out = " + ipa_out);

                    appBaseInfoTA.appendText("\n应用类型: " + iOS);
                    appBaseInfoTA.appendText("\n解包路径: " + ipa_out + "\n");
                } catch (Exception e) {
                    System.out.println("[ScanAppControllerV2_startScan] " + fileName + " Scan Error");
                    e.printStackTrace();
                }
                break;
            case ".hap":
                System.out.println("[ScanAppControllerV2_startScan] " + fileName + " for HarmonyNext");
                try {
                    HapUtilV1 hapUtilV1 = new HapUtilV1();
                    String hap_out = hapUtilV1.unzip(app_file);
                    System.out.println("[ScanAppControllerV2_startScan] hap_out = " + hap_out);
                    appBaseInfoTA.appendText("\n应用类型: " + HarmonyNext);
                    appBaseInfoTA.appendText("\n解包路径: " + hap_out + "\n");

                } catch (Exception e) {
                    System.out.println("[ScanAppControllerV2_startScan] " + fileName + " Scan Error");
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("[ScanAppControllerV2_startScan] Unknown file type");
                break;
        }
    }

    public void start(TextArea appBaseInfoTA, int threads, TableView appScanResTV, ProgressBar progressBar, Text text, Button button, TableColumn<?, ?> appScanResItemCol, TableColumn<?, ?> appScanResInfoCol, TableColumn<?, ?> appScanResRiskCol) {
        System.out.println("[ScanAppControllerV2_start]");

        appScanResTV.getItems().clear();
        this.data.clear();

        appScanResItemCol.setCellValueFactory(new PropertyValueFactory<>("item"));
        appScanResInfoCol.setCellValueFactory(new PropertyValueFactory<>("info"));
        appScanResInfoCol.setCellFactory(cell -> new WrapTableCell());
        appScanResRiskCol.setCellValueFactory(new PropertyValueFactory<>("risk"));

        this.appScanResTV = appScanResTV;

        button.setDisable(true);
        this.taskNum = 0.0D;
        this.completed = 0.0D;
        this.progress = new SimpleDoubleProperty(0.0D);

        //根据传入的应用类型，创建扫描任务
        String BaseInfo = appBaseInfoTA.getText();
        String[] baseOut = BaseInfo.split("\n");

        Scanner scanner = new Scanner(BaseInfo);
        int lineNum = 0;
        StringBuilder newBaseInfo = new StringBuilder();
        while (scanner.hasNextLine() && lineNum < 3) {
            String line = scanner.nextLine();
            newBaseInfo.append(line).append("\n");
            lineNum++;
        }
        BaseInfo = newBaseInfo.deleteCharAt(newBaseInfo.length() - 1).toString();

        String app_type = baseOut[1].split(":")[1].trim();
        String app_out = baseOut[2].split(":")[1].trim();
        System.out.println("[ScanAppControllerV2_start] Type -->" + app_type);
        System.out.println("[ScanAppControllerV2_start] OutPath -->" + app_out);

        List<Task<Void>> tasks = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        ApkInfoV2 apkInfoV2 = new ApkInfoV2();
        IpaInfoV2 ipaInfoV2 = new IpaInfoV2();
        HapInfoV1 hapInfoV1 = new HapInfoV1();

        switch (app_type) {
            case "Android":
                ApkUtilV2 apkUtilV2 = new ApkUtilV2();
                tasks.add(analyse_AndroidManifest(app_out, apkInfoV2, apkUtilV2));
                tasks.add(analyse_Signature(app_out, apkInfoV2, apkUtilV2));
                tasks.add(analyse_AppName(app_out, apkInfoV2, apkUtilV2));
                tasks.add(find_SensInfo(app_out, apkInfoV2, apkUtilV2));
                break;
            case "iOS":
                IpaUtilV2 ipaUtilV2 = new IpaUtilV2();
                tasks.add(analyse_InfoPlist(app_out, ipaInfoV2, ipaUtilV2));
                tasks.add(analyse_ExeFile(app_out, ipaInfoV2, ipaUtilV2));
                tasks.add(find_SensInfo(app_out, ipaInfoV2, ipaUtilV2));
                break;
            case "HarmonyNext":
                HapUtilV1 hapUtilV1 = new HapUtilV1();
                tasks.add(analyse_ModuleJson(app_out, hapInfoV1, hapUtilV1));
                tasks.add(analyse_ABC(hapInfoV1, hapUtilV1));
                tasks.add(analysisSensInfoResult(app_out, hapInfoV1, hapUtilV1));
                break;
            default:
                break;
        }

        this.taskNum = tasks.size();

        for (Task<?> task : tasks) {
            executorService.submit(task);
        }
        for (Task<?> task : tasks) {
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    this.completed += 1.0d;
                    double newProgress = this.completed / this.taskNum;
                    this.progress.set(newProgress);
                    text.setText(String.format("%.1f", Double.valueOf((this.completed / this.taskNum) * 100.0d)) + FXMLLoader.RESOURCE_KEY_PREFIX);
                });
            });
        }
        executorService.shutdown();

        appBaseInfoTA.setText(BaseInfo);
//        System.out.println("------>setText   " + BaseInfo);

        this.progress.addListener((observable, oldValue, newValue) -> {
            progressBar.setProgress(newValue.doubleValue());
//            System.out.println("completed = " + this.completed);
//            System.out.println("taskNum   = " + this.taskNum);

            if (this.completed == this.taskNum) {
                // 扫描完成，展示信息
                if (app_type.equals(Android)) {
                    apkInfoV2.showAppInfo();
                    appBaseInfoTA.appendText("\n应用名: " + apkInfoV2.getAppName());
                    appBaseInfoTA.appendText("\n版本号: " + apkInfoV2.getAppVersion());
                    appBaseInfoTA.appendText("\n应用包名: " + apkInfoV2.getPackageName());
                    appBaseInfoTA.appendText("\n加固类型: " + apkInfoV2.getShellName());
                } else if (app_type.equals(iOS)) {
                    ipaInfoV2.showAppInfo();
                    appBaseInfoTA.appendText("\n应用名: " + ipaInfoV2.getBundleName());
                    appBaseInfoTA.appendText("\n版本号: " + ipaInfoV2.getVersion());
                    appBaseInfoTA.appendText("\n应用包名: " + ipaInfoV2.getPackageName());
                } else if (app_type.equals(HarmonyNext)) {
                    hapInfoV1.showAppInfo();
                    appBaseInfoTA.appendText("\n应用名: " + hapInfoV1.getLabel());
                    appBaseInfoTA.appendText("\n版本号: " + hapInfoV1.getVersion());
                    appBaseInfoTA.appendText("\n应用包名: " + hapInfoV1.getBundleName());
                }

                button.setDisable(false);
            }

        });


    }

    private Task<Void> analyse_AndroidManifest(final String apk_out, final ApkInfoV2 apkInfoV2, final ApkUtilV2 apkUtilV2) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    apkUtilV2.analyse_AndroidManifest(apk_out, apkInfoV2);
                    analysisAndroidManifestResult(apkInfoV2);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }


    private Task<Void> analyse_Signature(final String apk_out, final ApkInfoV2 apkInfoV2, final ApkUtilV2 apkUtilV2) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    apkUtilV2.analyse_Signature(apk_out, apkInfoV2);
                    analysisSignatureResult(apkInfoV2);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Task<Void> analyse_AppName(final String apk_out, final ApkInfoV2 apkInfoV2, final ApkUtilV2 apkUtilV2) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    apkUtilV2.analyse_AppName(apkInfoV2.getAppName(), apk_out, apkInfoV2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Task<Void> find_SensInfo(final String apk_out, final ApkInfoV2 apkInfoV2, final ApkUtilV2 apkUtilV2) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    apkUtilV2.find_SensInfo(apk_out, apkInfoV2);
                    analysisSensInfoResult(apkInfoV2);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Task<Void> analyse_InfoPlist(final String ipa_out, final IpaInfoV2 ipaInfoV2, final IpaUtilV2 ipaUtilV2) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ipaUtilV2.analyse_InfoPlist(ipa_out, ipaInfoV2);
                    analyseInfoPlist(ipaInfoV2);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Task<Void> analyse_ExeFile(final String out_path, final IpaInfoV2 ipaInfoV2, final IpaUtilV2 ipaUtilV2) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ipaUtilV2.analyse_ExeFile(out_path, ipaInfoV2);
                    analyseExeFile(ipaInfoV2);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Task<Void> find_SensInfo(final String ipa_out, final IpaInfoV2 ipaInfoV2, final IpaUtilV2 ipaUtilV2) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ipaUtilV2.find_SensInfo(ipa_out, ipaInfoV2);
                    analysisSensInfoResult(ipaInfoV2);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Task<Void> analyse_ModuleJson(final String hap_out, final HapInfoV1 hapInfoV1, final HapUtilV1 hapUtilV1) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    hapUtilV1.analyse_ModuleJson(hap_out, hapInfoV1);
                    analyseModuleJson(hapInfoV1);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Task<Void> analyse_ABC(final HapInfoV1 hapInfoV1, final HapUtilV1 hapUtilV1) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    hapUtilV1.analyse_abc(hapInfoV1);
                    analyseABC(hapInfoV1);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Task<Void> analysisSensInfoResult(final String out_path, final HapInfoV1 hapInfoV1, final HapUtilV1 hapUtilV1) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    hapUtilV1.find_SensInfo(out_path, hapInfoV1);
                    analyseSensInfo(hapInfoV1);
                    freshTV();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }


    public void analysisAndroidManifestResult(ApkInfoV2 apkInfoV2) {
        System.out.println("[ScanAppControllerV2_analysisAndroidManifestResult] Android");
        // 应用可备份
        if (apkInfoV2.getIsAllowBackup()) {
            this.data.add(new PentestInfo("应用是否可备份", "android:allowBackup = true", "高风险"));
        } else {
            this.data.add(new PentestInfo("应用是否可备份", "android:allowBackup = false", "无"));
        }

        // 应用可调试
        if (apkInfoV2.getIsDebuggable()) {
            this.data.add(new PentestInfo("应用是否可调试", "android:debuggable = true", "高风险"));
        } else {
            this.data.add(new PentestInfo("应用是否可调试", "android:debuggable = false", "无"));
        }


        // 申请权限等级
        HashMap<String, String> permissions = apkInfoV2.getPermissions();
        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            String permission_name = entry.getKey();
            String permission_level = entry.getValue();
            if (permission_level.equals("normal")) {
                this.data.add(new PentestInfo("声明权限的保护等级", permission_name + " 权限保护等级为 normal ,需要进一步检测受该权限保护的组件是否能访问到敏感信息", "低风险"));
            } else {
                this.data.add(new PentestInfo("声明权限的保护等级", permission_name + " 权限保护等级为 " + permission_level, "无"));
            }
        }

        // 导出组件
        HashMap<String, String> activities = apkInfoV2.getActivities();
        for (Map.Entry<String, String> entry : activities.entrySet()) {
            String activity_name = entry.getKey();
            String activity_exported = entry.getValue();
            if (activity_exported.equals("true")) {
                this.data.add(new PentestInfo("Activity组件导出", activity_name + " 组件导出，需构造 Intent 进一步测试", "具体分析"));
            } else {
                this.data.add(new PentestInfo("Activity组件导出", activity_name + " 组件未导出", "无"));
            }
        }
        HashMap<String, String> services = apkInfoV2.getServices();
        for (Map.Entry<String, String> entry : services.entrySet()) {
            String service_name = entry.getKey();
            String service_exported = entry.getValue();
            if (service_exported.equals("true")) {
                this.data.add(new PentestInfo("Service组件导出", service_name + " 组件导出，需构造 Intent 进一步测试", "具体分析"));
            } else {
                this.data.add(new PentestInfo("Service组件导出", service_name + " 组件未导出", "无"));
            }
        }
        HashMap<String, String> receivers = apkInfoV2.getReceivers();
        for (Map.Entry<String, String> entry : receivers.entrySet()) {
            String receiver_name = entry.getKey();
            String receiver_exported = entry.getValue();
            if (receiver_exported.equals("true")) {
                this.data.add(new PentestInfo("Receiver组件导出", receiver_name + " 组件导出，需构造 Intent 进一步测试", "具体分析"));
            } else {
                this.data.add(new PentestInfo("Receiver组件导出", receiver_name + " 组件未导出", "无"));
            }
        }
        HashMap<String, String> providers = apkInfoV2.getProviders();
        for (Map.Entry<String, String> entry : providers.entrySet()) {
            String provider_name = entry.getKey();
            String provider_exported = entry.getValue();
            if (provider_exported.equals("true")) {
                this.data.add(new PentestInfo("Provider组件导出", provider_name + " 组件导出, 需构造 Intent 进一步测试", "具体分析"));
            } else {
                this.data.add(new PentestInfo("Provider组件导出", provider_name + " 组件未导出", "无"));
            }
        }
    }

    public void analysisSignatureResult(ApkInfoV2 apkInfoV2) {
        System.out.println("[ScanAppControllerV2_analysisSignatureResult] Android");
        String signature = apkInfoV2.getSignVersion();
        if (signature.equals("1")) {
            this.data.add(new PentestInfo("Janus签名漏洞", "应用使用 V" + signature + " 签名, 易受 CVE-2017-13156 攻击", "高风险"));
        } else {
            this.data.add(new PentestInfo("Janus签名漏洞", "应用使用 V" + signature + " 签名", "无"));
        }
    }

    public void analysisSensInfoResult(ApkInfoV2 apkInfoV2) {
        System.out.println("[ScanAppControllerV2_analysisSensInfoResult] Android");
        HashMap<String, ArrayList<String>> sensInfos = apkInfoV2.getSensInfo();
        boolean hasSensInfo = false;

        List<String> diff_list = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : sensInfos.entrySet()) {
            String sens_item = entry.getKey();
            ArrayList<String> sens_info = entry.getValue();
            if (!sens_info.isEmpty()) {
                hasSensInfo = true;
                for (String s : sens_info) {
                    if (diff_list.contains(s)) {
                        continue; // 去除重复信息
                    } else if (black_files_tail.contains(s.substring(s.lastIndexOf(".")))) {
                        continue;
                    }
                    diff_list.add(s);
                    this.data.add(new PentestInfo("应用包内敏感信息", "存在 " + sens_item + " : " + s, "具体分析"));
                }
            }
        }
        if (!hasSensInfo) {
            this.data.add(new PentestInfo("应用包内敏感信息", "未在应用包内发现敏感信息", "无"));
        }
    }

    public void analyseInfoPlist(IpaInfoV2 ipaInfoV2) {
        System.out.println("[ScanAppControllerV2_analyseInfoPlist] iOS");
        Boolean ats = ipaInfoV2.getATS();
        Set<String> scheme_urls = ipaInfoV2.getUrlSchemes();
        Set<String> permissions = ipaInfoV2.getPermissions();
        if (ats) {
            this.data.add(new PentestInfo("ATS配置安全", "ATS 配置为 true, 允许非 SSL 载入", "中风险"));
        } else {
            this.data.add(new PentestInfo("ATS配置安全", "ATS 配置为 false, 不允许非 SSL 载入", "无"));
        }

        for (String url : scheme_urls) {
            this.data.add(new PentestInfo("Url Scheme", "应用可通过 Safari 浏览器访问 " + url + " 调起, 需进一步测试", "具体分析"));
        }

        for (String permission : permissions) {
            this.data.add(new PentestInfo("应用权限检查", "应用申请权限 \"" + permission + "\"", "无"));
        }
    }

    public void analyseExeFile(IpaInfoV2 ipaInfoV2) {
        System.out.println("[ScanAppControllerV2_analyseExeFile] iOS");
        Boolean PIE = ipaInfoV2.getPIE();
        Boolean stack = ipaInfoV2.getStack();
        Boolean ARC = ipaInfoV2.getARC();
        if (PIE) {
            this.data.add(new PentestInfo("PIE 地址随机化", "应用可执行程序未开启地址随机化", "高风险"));
        } else {
            this.data.add(new PentestInfo("PIE 地址随机化", "应用可执行程序已开启地址随机化", "无"));
        }
        if (stack) {
            this.data.add(new PentestInfo("栈 cookie 保护", "应用可执行程序标志位未启用栈 cookie 保护", "高风险"));
        } else {
            this.data.add(new PentestInfo("栈 cookie 保护", "应用可执行程序标志位已启用栈 cookie 保护", "无"));
        }
        if (ARC) {
            this.data.add(new PentestInfo("ARC 管理对象", "应用可执行程序未启用 Automatic Reference Counting 管理对象", "高风险"));
        } else {
            this.data.add(new PentestInfo("ARC 管理对象", "应用可执行程序已启用 Automatic Reference Counting 管理对象", "无"));
        }
    }

    public void analysisSensInfoResult(IpaInfoV2 ipaInfoV2) {
        System.out.println("[ScanAppControllerV2_analysisSensInfoResult] iOS");
        HashMap<String, ArrayList<String>> sensInfos = ipaInfoV2.getSensInfo();
        boolean hasSensInfo = false;
        List<String> diff_list = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : sensInfos.entrySet()) {
            String sens_item = entry.getKey();
            ArrayList<String> sens_info = entry.getValue();
            if (!sens_info.isEmpty()) {
                hasSensInfo = true;
                for (String s : sens_info) {
                    if (diff_list.contains(s)) {
                        continue; // 去除重复信息
                    } else if (black_files_tail.contains(s.substring(s.lastIndexOf(".")))) {
                        continue;
                    }
                    diff_list.add(s);
                    this.data.add(new PentestInfo("应用包内敏感信息", "存在 " + sens_item + " : " + s, "具体分析"));
                }
            }
        }
        if (!hasSensInfo) {
            this.data.add(new PentestInfo("应用包内敏感信息", "未在应用包内发现敏感信息", "无"));
        }
    }

    public void analyseModuleJson(HapInfoV1 hapInfoV1) {
        System.out.println("[ScanAppControllerV2_analyseModuleJson] HarmonyNext");

        String debug = hapInfoV1.getDebug();

        if (debug.equals("true")) {
            this.data.add(new PentestInfo("应用是否可调试", "\"debug\": true", "低风险"));
        } else {
            this.data.add(new PentestInfo("应用是否可调试", "\"debug\": false", "无"));
        }

    }

    public void analyseABC(HapInfoV1 hapInfoV1) {
        System.out.println("[ScanAppControllerV2_analyseABC] HarmonyNext");

        ArrayList<String> mixMode = hapInfoV1.getMixMode();
        boolean windowPrivacy = hapInfoV1.getWindowPrivacy();
        boolean sslError = hapInfoV1.getSSLError();
        if (!mixMode.isEmpty()) {
            for (String code_line : mixMode) {
                this.data.add(new PentestInfo("HTTP/HTTPS 混合内容", "存在 HTTP/HTTPS 混合内容: " + code_line, "低风险"));
            }
        } else {
            this.data.add(new PentestInfo("HTTP/HTTPS 混合内容", "未发现使用 HTTP/HTTPS 混合内容", "无"));
        }

        if (!windowPrivacy) {
            this.data.add(new PentestInfo("是否可被截屏或录屏", "应用界面允许被截屏或录屏", "低风险"));
        } else {
            this.data.add(new PentestInfo("是否可被截屏或录屏", "应用界面不允许被截屏或录屏", "无"));
        }
        if (sslError) {
            this.data.add(new PentestInfo("SSL校验出错继续加载", "SSL校验出错时继续加载页面", "低风险"));
        } else {
            this.data.add(new PentestInfo("SSL校验出错继续加载", "SSL校验出错时不允许继续加载页面", "无"));
        }
    }

    public void analyseSensInfo(HapInfoV1 hapInfoV1) {
        System.out.println("[ScanAppControllerV2_analyseSensInfo] HarmonyNext");

        HashMap<String, ArrayList<String>> sensInfos = hapInfoV1.getSensInfo();
        boolean hasSensInfo = false;
        List<String> diff_list = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : sensInfos.entrySet()) {
            String sens_item = entry.getKey();
            ArrayList<String> sens_info = entry.getValue();
            if (!sens_info.isEmpty()) {
                hasSensInfo = true;
                for (String s : sens_info) {
//                    System.out.println("[ScanAppControllerV2_analyseSensInfo]  " + s.substring(s.lastIndexOf(".")));
                    if (diff_list.contains(s)) {
                        continue;   // 去除重复信息
                    } else if (black_files_tail.contains(s.substring(s.lastIndexOf(".")))) {
                        continue;   // 去除资源信息
                    }
                    diff_list.add(s);
                    this.data.add(new PentestInfo("应用包内敏感信息", "存在 " + sens_item + " : " + s, "具体分析"));

                }
            }
        }
        if (!hasSensInfo) {
            this.data.add(new PentestInfo("应用包内敏感信息", "未在应用包内发现敏感信息", "无"));
        }

    }


    public void freshTV() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            try {
                System.out.println("[ScanAppControllerV2_freshTV] this.data in TableView = " + this.data.size());
                this.appScanResTV.setItems(this.data);

//                Platform.runLater(() -> {
//                    System.out.println("freshTV data size = " + this.data.size());
//                    System.out.println(this.data.get(0).getInfo());
//                    this.appScanResTV.setItems(this.data);
//                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        executorService.shutdown();
    }


}
