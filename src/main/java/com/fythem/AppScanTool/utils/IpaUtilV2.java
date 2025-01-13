package com.fythem.AppScanTool.utils;

import com.dd.plist.*;
import com.fythem.AppScanTool.entity.IpaInfoV2;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class IpaUtilV2 {
    String otool_path = "";
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

    public IpaUtilV2() {
        this.otool_path = Paths.get(root_path, "lib/arm64-apple-darwin20.0.0-otool").toString();
    }

    /**
     * unzip the ipa file
     * param ipa_file
     * return ipa_out
     */
    public String unzip(File ipa_file) throws IOException {
        System.out.println("[IpaUtilV2] [unzip]");
        if (!ipa_file.exists()) {
            ipa_file.mkdirs();
        }
        String ipa_name = ipa_file.getName().replace(".ipa", "");
        String unzipIpa_out = Paths.get(out_dir, ipa_name).toString();

        FileInputStream fis = new FileInputStream(ipa_file);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry entry = zis.getNextEntry();

        while (entry != null) {
            String filePath = unzipIpa_out + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // 如果是文件则解压
                extractFile(zis, filePath);
            } else {
                // 如果是目录则创建
                File dirEntry = new File(filePath);
                dirEntry.mkdirs();
            }
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
        zis.close();
        fis.close();

        System.out.println("[IpaUtilV2] [unzip] finished");
        return unzipIpa_out;
    }

    private void extractFile(ZipInputStream zis, String filePath) throws IOException {
        File newFile = new File(filePath);
        new File(newFile.getParent()).mkdirs();
//
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[1024];
        int read;
        while ((read = zis.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public IpaInfoV2 analyse_InfoPlist(String out_dir, IpaInfoV2 ipa_info) {
        System.out.println("[IpaUtilV2] [analyse_InfoPlist]");

        File out_file = new File(out_dir);
        if (!out_file.isDirectory()) {
            return ipa_info;
        }

        String infoPlist_path = find_InfoPlist(out_dir);

        File info_plist = new File(infoPlist_path);
        if (info_plist.exists()) {
            ipa_info.setInfoPlist(infoPlist_path);
        } else {
            ipa_info.setInfoPlist("Not Found");
        }

        try {
            NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(info_plist);

            /**
             * "CFBundleDisplayName","CFBundleName" : bundle_name（应用名）
             * "CFBundleIdentifier" : pkg_name（包名,应用ID）
             * "CFBundleShortVersionString" : main_version（主版本号）
             * "CFBundleVersion" : build_version（构建版本号）
             * "MinimumOSVersion" : min_sys（最低支持系统版本）
             * "CFBundleExecutable" : exe_file（可执行文件）
             */

            /**
             * app name
             */
            String bundle_name = get_PlistValue(rootDict, "CFBundleName").toString();
            String display_name = get_PlistValue(rootDict, "CFBundleDisplayName").toString();
            if (bundle_name.isEmpty()) {
                bundle_name = display_name;
            } else {
                bundle_name = bundle_name + "(" + display_name + ")";
            }
            ipa_info.setBundleName(bundle_name);

            /**
             * ipa version
             */
            String bundle_version = get_PlistValue(rootDict, "CFBundleVersion").toString();
            String main_version = get_PlistValue(rootDict, "CFBundleShortVersionString").toString();
            String ipa_version = main_version + "(" + bundle_version + ")";
            if (ipa_version.length() > 2) {
                ipa_info.setIpaVersion(ipa_version);
            } else {
                ipa_info.setIpaVersion("0.0(0.0.0.0)");
            }

            /**
             * executable file
             */
            String file_name = get_PlistValue(rootDict, "CFBundleExecutable").toString();
            String exe_file = find_TargetFile(out_dir, file_name);
            ipa_info.setExeFile(exe_file);

            /**
             * package name
             */
            String pkg_name = get_PlistValue(rootDict, "CFBundleIdentifier").toString();
            ipa_info.setPackageName(pkg_name);

            /**
             * ATS
             */
            boolean ats = false;
            try {
                ats = Boolean.parseBoolean(get_PlistValue(rootDict, "NSAppTransportSecurity.NSAllowsArbitraryLoads").toString());
                NSDictionary special_domains = (NSDictionary) get_PlistValue(rootDict, "NSAppTransportSecurity.NSExceptionDomains");
                if (special_domains == null) {
                    ipa_info.setSpecial_domains(new HashSet<>());
                }
                ipa_info.setSpecial_domains(special_domains.keySet());
            } catch (Exception e) {
                //not find ats, use default false
//                e.printStackTrace();
            }
            ipa_info.setATS(ats);


            /**
             * Url Scheme
             */
            Set<String> url_schemes = new HashSet<>();
            NSArray array = (NSArray) rootDict.get("CFBundleURLTypes");
            for (NSObject nsObject : array.getArray()) {
                NSDictionary aaa = (NSDictionary) nsObject;
                NSArray bbb = (NSArray) aaa.get("CFBundleURLSchemes");
                for (NSObject nsObject1 : bbb.getArray()) {
                    url_schemes.add(nsObject1.toString() + "://");
                }
            }
            ipa_info.setUrlScheme(url_schemes);


            /**
             * Permission Description
             */
            Set<String> mPermissions = new HashSet<>();
            for (Map.Entry<String, NSObject> key : rootDict.entrySet()) {
                String pKey = key.getKey();
                if (pKey.startsWith("NS") && (pKey.contains("UsageDescription"))) {
                    String kk = key.getKey().replace("NS", "").replace("UsageDescription", "");
                    mPermissions.add(kk + "(" + key.getValue().toString() + ")");
                }
            }
            ipa_info.setPermissions(mPermissions);

            /**
             * demo code
             * 获取所有key-value
             */
//            for (Map.Entry<String, NSObject> key : rootDict.entrySet()) {
//                System.out.println("---> " + key.getKey() + " | " + key.getValue());
//                if (key.getValue() instanceof NSDictionary) {
//                    System.out.println("DDDDDDDDDDDDDDDDDD");
//                    NSDictionary a = (NSDictionary) key.getValue();
//                    System.out.println(a.toJavaObject());
//                    System.out.println(a);
//                }
//                if (key.getValue() instanceof NSArray) {
//                    System.out.println("AAAAAAAAAAAAAAAAAA");
//                    NSObject[] a = ((NSArray) key.getValue()).getArray();
//                    for (NSObject nsObject : a) {
//                        System.out.println(nsObject);
//                    }
//                }
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("[IpaUtilV2] [analyse_InfoPlist] finished");
        return ipa_info;
    }

    public String find_InfoPlist(String dir_path) {
        File dir = new File(dir_path);
        File[] all_file = dir.listFiles();
        for (File file : all_file) {
            String absolutePath = file.getAbsolutePath();
            if (file.isDirectory()) {
                absolutePath = find_InfoPlist(absolutePath);
                if (absolutePath != null) {
                    return absolutePath;
                }
            } else {
                if (absolutePath.endsWith(".app/Info.plist")) {
                    return absolutePath;
                }
            }
        }
        return null;
    }

    public String find_TargetFile(String dir_path, String target_file) {
        File dir = new File(dir_path);
        File[] all_file = dir.listFiles();
        for (File file : all_file) {
            String absolutePath = file.getAbsolutePath();
            if (file.isDirectory()) {
                absolutePath = find_TargetFile(absolutePath, target_file);
                if (absolutePath != null) {
                    return absolutePath;
                }
            } else {
                if (absolutePath.endsWith(target_file)) {
                    return absolutePath;
                }
            }
        }
        return null;
    }


    public NSObject get_PlistValue(NSDictionary ns_dic, String key_path) {
        String[] values = key_path.split("\\.");
        try {
            if (values.length > 0) {
                int i = 0;
                NSObject value = null;
                NSDictionary dictionary = ns_dic;
                while (i < values.length) {
                    value = dictionary.objectForKey(values[i]);
                    if (value instanceof NSDictionary) {
                        dictionary = (NSDictionary) value;
                    }
                    i++;
                }
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public IpaInfoV2 analyse_ExeFile(String out_path, IpaInfoV2 ipaInfoV2) {
        System.out.println("[IpaUtilV2] [analyse_ExeFile]");
        File exe_file = new File(ipaInfoV2.getExe_file());
        File otool_out_file = new File(Paths.get(out_path, "otool_out_for_stack_and_arc.txt").toString());
        if (!exe_file.exists()) {
            System.out.println("[IpaUtilV2] [analyse_ExeFile] exe_file not exist");
            return ipaInfoV2;
        }
        try {
            String line;

            // PIE
            ProcessBuilder processBuilder_pie = new ProcessBuilder(otool_path, "-Vh", exe_file.getAbsolutePath());
            Process process_pie = processBuilder_pie.start();
            BufferedReader reader_pie = new BufferedReader(new InputStreamReader(process_pie.getInputStream()));
            while ((line = reader_pie.readLine()) != null) {
                if (line.contains("PIE")) {
                    System.out.println("[IpaUtilV2] [analyse_ExeFile] pie -->" + line);
                    ipaInfoV2.setPIE(true);
                }
            }
            process_pie.waitFor();
            System.out.println("[IpaUtilV2] [analyse_ExeFile] analyse ExeFile PIE finished: " + process_pie.waitFor());

            ProcessBuilder processBuilder_stack_ARC = new ProcessBuilder(otool_path, "-Iv", exe_file.getAbsolutePath());
            processBuilder_stack_ARC.redirectOutput(otool_out_file);
            Process process_stack = processBuilder_stack_ARC.start();
            process_stack.waitFor();

            if (!otool_out_file.exists()) {
                System.out.println("[IpaUtilV2] [analyse_ExeFile] Error! otool_out_for_stack_and_arc.txt not find!");
            }

            String line2;
            BufferedReader br = new BufferedReader(new FileReader(otool_out_file));
            while ((line2 = br.readLine()) != null) {
                if (line2.contains("stack")) {
                    // Stack Canary, 栈cookie
                    System.out.println("[IpaUtilV2] [analyse_ExeFile] stack --> " + line2);
                    ipaInfoV2.setStack(true);
                }
                if (line2.contains("_objc_release")) {
                    // ARC
                    System.out.println("[IpaUtilV2] [analyse_ExeFile] arc --> " + line2);
                    ipaInfoV2.setARC(true);
                }
            }
            System.out.println("[IpaUtilV2] [analyse_ExeFile] analyse ExeFile stack & ARC finished: " + process_stack.waitFor());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("[IpaUtilV2] [analyse_ExeFile] finished");
        return ipaInfoV2;
    }

    public IpaInfoV2 find_SensInfo(String dir_path, IpaInfoV2 ipaInfoV2) {
        System.out.println("[IpaUtilV2] [find_SensInfo]");

        File out_dir = new File(dir_path);
        HashMap<String, ArrayList<String>> sens_info = new HashMap<>();
        ArrayList<File> target_file = new ArrayList<File>();

        if (out_dir.isDirectory()) {
            find_AllFile(out_dir, target_file);
        }
        PatternUtilV2 patternUtilV2 = new PatternUtilV2();
        sens_info = patternUtilV2.start_match(target_file);
        ipaInfoV2.setSensInfo(sens_info);

        System.out.println("[IpaUtilV2] [find_SensInfo] finished");
        return ipaInfoV2;
    }

    public void find_AllFile(File allFileDir, ArrayList<File> target) {
        File[] files = allFileDir.listFiles();
        for (File file : files) {
            String file_path = file.getPath();
            if (file.isDirectory()) {
                if (file_path.contains("Payload")
                        || file_path.contains("Frameworks")) {
                    find_AllFile(file, target);
                } else {
                    continue;  //跳过无用的目录
                }
            } else {
                if (file_path.endsWith(".plist")
                        || file_path.endsWith(".xml")
                        || file_path.endsWith(".txt")
                        || file_path.endsWith(".so")
                        || file_path.endsWith(".html")
                        || file_path.endsWith(".htm")
                        || file_path.endsWith(".json")
                        || file_path.endsWith(".js")) {
                    // 目标文件
                    target.add(file);
                } else if (!file.getName().contains(".")) {
                    // 可执行程序
                    target.add(file);
                } else if (file.getName().endsWith(".png")
                        || file.getName().endsWith(".gif")
                        || file.getName().endsWith(".jpg")
                        || file.getName().endsWith(".ico")
                        || file.getName().endsWith(".svg")
                        || file.getName().endsWith(".css")
                        || file.getName().endsWith(".bcmap")
                        || file.getName().endsWith(".pem")
                        || file.getName().endsWith(".cer")
                        || file.getName().endsWith(".mp3")
                        || file.getName().endsWith(".zip")
                        || file.getName().endsWith(".wav")) {
                    // 过滤无用的资源文件
                    continue;
                }
                target.add(file);

            }
        }

    }
}
