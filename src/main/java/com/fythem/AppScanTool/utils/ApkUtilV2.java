package com.fythem.AppScanTool.utils;

import com.fythem.AppScanTool.entity.ApkInfoV2;
import com.fythem.AppScanTool.entity.ShellInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ApkUtilV2 {
    String apktool_path = "";
    String root_path = System.getProperty("user.dir");
    String out_dir = root_path + "/apps";

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private static ThreadLocal<DocumentBuilder> documentBuilderThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    });

    /**
     * init ApkUtil_v2
     * set apktool_path
     */
    public ApkUtilV2() {
        this.apktool_path = Paths.get(root_path, "lib/apktool_2.9.3.jar").toString();
    }


    /**
     * decompile apk
     * param: apk file
     * return: decompile out directory
     */
    public String decompile(File apk_file) throws IOException {
        System.out.println("[ApkUtilV2] [decompile]");
        String apk_name = apk_file.getName().replace(".apk", "");
        String decompile_out = Paths.get(out_dir, apk_name).toString();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", apktool_path, "d", "-f", apk_file.getAbsolutePath(), "-o", decompile_out);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
            int exitCode = process.waitFor();
            System.out.println("[ApkUtilV2] [decompile] decompile Apk finished: " + exitCode);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        System.out.println("[ApkUtilV2] [decompile] finished");
        return decompile_out;
    }

    /**
     * analyse AndroidManifest.xml
     * param1: AndroidManifest.xml file
     * param2: apkInfoV2
     * return: ApkInfoV2.class
     */
    public ApkInfoV2 analyse_AndroidManifest(String out_dir, ApkInfoV2 apk_info) {
        System.out.println("[ApkUtilV2] [analyse_AndroidManifest]");
        File manifest_file = new File(Paths.get(out_dir, "AndroidManifest.xml").toString());
        if (!manifest_file.exists()) {
            System.out.println("[ApkUtilV2] [analyse_AndroidManifest] Error. AndroidManifest.xml not exists.");
            return apk_info;
        }

        Document AM_doc = null;
        try {
            AM_doc = documentBuilderThreadLocal.get().parse(manifest_file);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        Element root = AM_doc.getDocumentElement();

        /*
        get package_name
         */
        String package_name = root.getAttribute("package");
        apk_info.setPackageName(package_name);

        /*
        get app version
         */
        String version_name = "";
        String version_code = "";

        File apktool_yml = new File(Paths.get(out_dir, "apktool.yml").toString());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(apktool_yml));
            String runline = "";
            while ((runline = reader.readLine()) != null) {
                runline = runline.replace(" ", "");
                if (runline.startsWith("versionCode")) {
                    version_code = runline.split(":")[1];
                } else if (runline.startsWith("versionName")) {
                    version_name = runline.split(":")[1];
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("[ApkUtilV2] [analyse_AndroidManifest] Read apktool.yml error: " + e.getMessage());
        }

        if (root.hasAttribute("android:versionName")) {
            version_name = root.getAttribute("android:versionName");
        }
        if (root.hasAttribute("android:versionCode")) {
            version_code = root.getAttribute("android:versionCode");
        }

        String version = version_name + "(" + version_code + ")";
        apk_info.setVersion(version);

        /*
        get uses-permissions
         */
        NodeList uses_permission = root.getElementsByTagName("uses-permission");
        ArrayList<String> uses_permission_list = new ArrayList<String>();
        for (int i = 0; i < uses_permission.getLength(); i++) {
            Element element = (Element) uses_permission.item(i);
            uses_permission_list.add(element.getAttribute("android:name"));
        }
        apk_info.setUsesPermission(uses_permission_list);

        /*
        get permissions
         */
        NodeList permission = root.getElementsByTagName("permission");
        HashMap<String, String> permission_map = new HashMap<>();
        for (int i = 0; i < permission.getLength(); i++) {
            Element element = (Element) permission.item(i);
            permission_map.put(element.getAttribute("android:name"), element.getAttribute("android:protectionLevel"));
        }
        apk_info.setPermissions(permission_map);

        /*
        get application info
         */
        Element application = (Element) (root.getElementsByTagName("application").item(0));
        String applicationName = application.getAttribute("android:name");
        String allowBackup = application.getAttribute("android:allowBackup");
        String debuggable = application.getAttribute("android:debuggable");
        String networkSecurityConfig = application.getAttribute("android:networkSecurityConfig");
        String icon = application.getAttribute("android:icon");
        String appName = application.getAttribute("android:label");

        // 获取 application 内的所有信息
//        NamedNodeMap attributes = application.getAttributes();
//        for (int i = 0; i < attributes.getLength(); i++) {
//            Node node = attributes.item(i);
//            System.out.println("<application> " + node.getNodeName() + " : " + node.getTextContent());
//        }

        apk_info.setShellName(ShellInfo.find(applicationName));
        apk_info.setIsAllowBackup(allowBackup);
        apk_info.setIsDebuggable(debuggable);
        apk_info.setNetworkSecurityConfig(networkSecurityConfig);
        apk_info.setIcon(icon);
        apk_info.setAppName(appName);

        /*
        get Activity（此处仅判断是否导出）
         */
        NodeList activities = root.getElementsByTagName("activity");
        HashMap<String, String> activity_map = new HashMap<>();
        for (int i = 0; i < activities.getLength(); i++) {
            Element activity_element = (Element) activities.item(i);
            String name = activity_element.getAttribute("android:name");
            String exported = activity_element.getAttribute("android:exported");

            if (exported.isEmpty()) {
                // check intent-filter
                NodeList intent_filter_list = activity_element.getElementsByTagName("intent-filter");
                if (intent_filter_list.getLength() > 0) {
                    activity_map.put(name, "true");
                } else {
                    activity_map.put(name, "false");
                }
            } else {
                activity_map.put(name, exported);
            }
        }
        apk_info.setActivityList(activity_map);

        /*
        get Service（此处仅判断是否导出）
         */
        NodeList services = root.getElementsByTagName("service");
        HashMap<String, String> service_map = new HashMap<>();
        for (int i = 0; i < services.getLength(); i++) {
            Element service_element = (Element) services.item(i);
            String name = service_element.getAttribute("android:name");
            String exported = service_element.getAttribute("android:exported");

            if (exported.isEmpty()) {
                // check intent-filter
                NodeList intent_filter_list = service_element.getElementsByTagName("intent-filter");
                if (intent_filter_list.getLength() > 0) {
                    service_map.put(name, "true");
                } else {
                    service_map.put(name, "false");
                }
            } else {
                service_map.put(name, exported);
            }
        }
        apk_info.setServiceList(service_map);

        /*
        get Receiver（此处仅判断是否导出）
         */
        NodeList receivers = root.getElementsByTagName("receiver");
        HashMap<String, String> receiver_map = new HashMap<>();
        for (int i = 0; i < receivers.getLength(); i++) {
            Element receiver_element = (Element) receivers.item(i);
            String name = receiver_element.getAttribute("android:name");
            String exported = receiver_element.getAttribute("android:exported");

            if (exported.isEmpty()) {
                // check intent-filter
                NodeList intent_filter_list = receiver_element.getElementsByTagName("intent-filter");
                if (intent_filter_list.getLength() > 0) {
                    receiver_map.put(name, "true");
                } else {
                    receiver_map.put(name, "false");
                }
            } else {
                receiver_map.put(name, exported);
            }
        }
        apk_info.setReceiverList(receiver_map);

         /*
        get Provider（此处仅判断是否导出）
         */
        NodeList providers = root.getElementsByTagName("provider");
        HashMap<String, String> provider_map = new HashMap<>();
        for (int i = 0; i < providers.getLength(); i++) {
            Element provider_element = (Element) providers.item(i);
            String name = provider_element.getAttribute("android:name");
            String exported = provider_element.getAttribute("android:exported");

            if (exported.isEmpty()) {
                // check intent-filter
                NodeList intent_filter_list = provider_element.getElementsByTagName("intent-filter");
                if (intent_filter_list.getLength() > 0) {
                    provider_map.put(name, "true");
                } else {
                    provider_map.put(name, "false");
                }
            } else {
                provider_map.put(name, exported);
            }
        }
        apk_info.setProviderList(provider_map);

        System.out.println("[ApkUtilV2] [analyse_AndroidManifest] finished");
        return apk_info;
    }


    /**
     * analyse "xxx.SF" for CVE-2017-13156
     * param1: "xxx.SF" file
     * param2: apkInfoV2
     * return: ApkInfoV2.class
     */
    public ApkInfoV2 analyse_Signature(String out_dir, ApkInfoV2 apkInfoV2) {
        System.out.println("[ApkUtilV2] [analyse_Signature]");
        File sign_dir = new File(Paths.get(out_dir, "original", "META-INF").toString());
        String sign_version = "X-Android-APK-Signed: 1"; //默认v1签名，沿用 X-Android-APK-Signed 写法

        if (!sign_dir.exists()) {
            System.out.println("[ApkUtilV2] [analyse_Signature] Error. CERT.SF not exists.");
            apkInfoV2.setSignVersion(sign_version);
            return apkInfoV2;
        }
        if (sign_dir.isDirectory()) {
            File[] files = sign_dir.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".SF")) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String runline;
                        while ((runline = reader.readLine()) != null) {
                            if (runline.contains("X-Android-APK-Signed")) {
                                sign_version = runline;
                                break;
                            }
                        }
                        reader.close();
                    } catch (IOException e) {
                        System.out.println("[ApkUtilV2] [analyse_Signature] Read .SF error: " + e.getMessage());
                    }
                }
            }
            sign_version = sign_version.split(": ")[1];
            apkInfoV2.setSignVersion(sign_version);
        }

        System.out.println("[ApkUtilV2] [analyse_Signature] finished");
        return apkInfoV2;
    }

    /**
     * analyse AppName
     * param1: appName from AndroidManifest.xml
     * param2: decompileOut path
     * param3: apkInfoV2
     * return: ApkInfoV2.class
     */
    public ApkInfoV2 analyse_AppName(String appName, String decompileOut, ApkInfoV2 apkInfoV2) {
        System.out.println("[ApkUtilV2] [analyse_AppName]");
        if (appName.equals("@string/app_name")) {
            File file = new File(Paths.get(decompileOut, "res/values/strings.xml").toString());
            if (file.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String runline;
                    while ((runline = reader.readLine()) != null) {
                        if (runline.contains("\"app_name\"")) {
                            appName = runline.substring(runline.indexOf("<string name=\"app_name\">") + 24, runline.indexOf("</string>"));
                            break;
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    System.out.println("[ApkUtilV2] [analyse_AppName] Read app_name error: " + e.getMessage());
                }

                apkInfoV2.setAppName(appName);
            }
        }
        System.out.println("[ApkUtilV2] [analyse_AppName] finished");
        return apkInfoV2;
    }


    /**
     * find SensInfo
     * param1: decompileOut path
     * param2: apkInfoV2
     * return: ApkInfoV2.class
     */
    public ApkInfoV2 find_SensInfo(String decompileOut, ApkInfoV2 apkInfoV2) {
        System.out.println("[ApkUtilV2] [find_SensInfo]");
        File out_dir = new File(decompileOut);
        HashMap<String, ArrayList<String>> sens_info = new HashMap<>();
        ArrayList<File> target_file = new ArrayList<File>();

        if (out_dir.isDirectory()) {
            find_AllFile(out_dir, target_file);
        }
        PatternUtilV2 patternUtilV2 = new PatternUtilV2();
        sens_info = patternUtilV2.start_match(target_file); // return what???
        apkInfoV2.setSensInfo(sens_info);

        System.out.println("[ApkUtilV2] [find_SensInfo] finished");
        return apkInfoV2;

    }

    /**
     * Get target files
     * save in param2
     */
    public void find_AllFile(File allFileDir, ArrayList<File> target) {
        File[] files = allFileDir.listFiles();
        for (File file : files) {
            String file_path = file.getPath();
            if (file.isDirectory()) {
                if (file_path.contains("assets")
                        || file_path.contains("smali")
                        || file_path.contains("res")
                        || file_path.contains("lib")) {
                    find_AllFile(file, target);
                } else {
                    continue;  //跳过无用的目录
                }
            } else {
                if (file_path.contains("/res/color")
                        || file_path.contains("/res/drawable")
                        || file_path.contains("/res/layout")
                        || file_path.contains("/res/anim")
                        || file_path.contains("/res/font")
                        || file_path.contains("/res/menu")) {
                    continue;  //跳过无用的资源文件
                } else if (file_path.endsWith(".smali") || file_path.endsWith(".xml") || file_path.endsWith(".html") || file_path.endsWith(".json") || file_path.endsWith(".js")) {
                    target.add(file);
                }
            }
        }
    }


}
