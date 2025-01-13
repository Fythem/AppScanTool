package com.fythem.AppScanTool.utils;


import com.alibaba.fastjson.JSONObject;
import com.fythem.AppScanTool.entity.HapInfoV1;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HapUtilV1 {
    String ark_disasm_path = "";
    String root_path = System.getProperty("user.dir");
    String out_dir = root_path + "/apps";

    public HapUtilV1() {
        this.ark_disasm_path = Paths.get(root_path, "lib/ark_disasm").toString();
    }

    public String unzip(File hap_file) throws IOException {
        System.out.println("[HapUtilV1] [unzip]");

        if (!hap_file.exists()) {
            hap_file.mkdirs();
        }
        String hap_name = hap_file.getName().replace(".hap", "");
        String unzipHap_out = Paths.get(out_dir, hap_name).toString();

        FileInputStream fis = new FileInputStream(hap_file);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry entry = zis.getNextEntry();

        while (entry != null) {
            String filePath = unzipHap_out + File.separator + entry.getName();

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

        System.out.println("[HapUtilV1] [unzip] finished");

        return unzipHap_out;
    }

    private void extractFile(ZipInputStream zis, String filePath) throws IOException {
        File newFile = new File(filePath);
        new File(newFile.getParent()).mkdirs();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[1024];
        int read;
        while ((read = zis.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public HapInfoV1 analyse_ModuleJson(String hapOut, HapInfoV1 hapInfoV1) {
        System.out.println("[HapUtilV1] [analyse_ModuleJson]");
        File module_file = new File(Paths.get(hapOut, "module.json").toString());
        if (!module_file.exists()) {
            System.out.println("[HapUtilV1] [analyse_ModuleJson] module.json not found.");
            return hapInfoV1;
        }
        hapInfoV1.setOutAbc(Paths.get(hapOut, "out_abc.txt").toString());

        StringBuilder module_json = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(module_file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                module_json.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JSONObject module_obj = JSONObject.parseObject(module_json.toString());
        JSONObject app_obj = module_obj.getJSONObject("app");

        /**
         * bundle name
         * 包名，不可缺省
         */
        String bundle_name = (String) app_obj.get("bundleName");
        hapInfoV1.setBundle_Name(bundle_name);

        /**
         * label
         * 应用名，不可缺省
         */
        String label = (String) app_obj.get("label");
        hapInfoV1.setLabel(label);

        /**
         * version
         * versionName + versionCode
         * 版本号，不可缺省
         */
        String version = (String) app_obj.get("versionName") + "(" + app_obj.get("versionCode") + ")";
        hapInfoV1.setVersion(version);

        /**
         * 应用是否可被调试，默认false
         */
        String debug = "false";
        if (app_obj.containsKey("debug")) {
            debug = String.valueOf(app_obj.get("debug"));
        }
        hapInfoV1.setDebug(debug);


        /**
         * module.abc 应用主代码
         */
        String abc_file = find_TargetFile(out_dir, "modules.abc");
        System.out.println("[HapUtilV1] [analyse_ModuleJson] find .abc file: " + abc_file);
        hapInfoV1.setAbc_file(abc_file);

        System.out.println("[HapUtilV1] [analyse_ModuleJson] finished");
        return hapInfoV1;
    }

    public HapInfoV1 analyse_abc(HapInfoV1 hapInfoV1) {
        System.out.println("[HapUtilV1] [analyse_abc]");
        File abc_file = new File(hapInfoV1.getAbc_file());
        if (!abc_file.exists()) {
            System.out.println("[HapUtilV1] [analyse_abc] analyse_abc file not exist");
            return hapInfoV1;
        }
        try {
            ProcessBuilder processBuilder_abc = new ProcessBuilder(ark_disasm_path, abc_file.getAbsolutePath(), hapInfoV1.getOutAbc());
            Process process_abc = processBuilder_abc.start();
            process_abc.waitFor();


            String abc_line;
            File out_abc = new File(hapInfoV1.getOutAbc());
            System.out.println("[HapUtilV1] [analyse_abc] decompile .abc file " + out_abc);

            if (!out_abc.exists()) {
                System.out.println("[HapUtilV1] [analyse_abc] outabc not exist ");
            }
            BufferedReader br = new BufferedReader(new FileReader(out_abc));
            while ((abc_line = br.readLine()) != null) {
                /**
                 * MixedMode.All
                 */
                if (abc_line.contains("MixedMode.All")) {
                    hapInfoV1.addMixedMode(abc_line);
                }

                /**
                 * 防截屏
                 * setWindowPrivacyMode
                 */
                if (abc_line.contains("setWindowPrivacyMode")) {
                    hapInfoV1.setWindowPrivacy(true);
                }

                /**
                 * SSL校验出错
                 * handleConfirm
                 */
                if (abc_line.contains("handleConfirm()")) {
                    hapInfoV1.setSSLError(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("[HapUtilV1] [analyse_abc] analyse abc finished");
        return hapInfoV1;
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


    public HapInfoV1 find_SensInfo(String dir_path, HapInfoV1 hapInfoV1) {
        System.out.println("[HapUtilV2] [find_SensInfo]");

        File out_dir = new File(dir_path);
        HashMap<String, ArrayList<String>> sens_info = new HashMap<>();
        ArrayList<File> target_file = new ArrayList<File>();

        if (out_dir.isDirectory()) {
            find_AllFile(out_dir, target_file);
        }

        PatternUtilV2 patternUtilV2 = new PatternUtilV2();
        sens_info = patternUtilV2.start_match(target_file); // return what???
        hapInfoV1.setSensInfo(sens_info);
        System.out.println("[HapUtilV2] [find_SensInfo] finished");
        return hapInfoV1;
    }

    public void find_AllFile(File allFileDir, ArrayList<File> target) {
        File[] files = allFileDir.listFiles();
        for (File file : files) {
            String file_path = file.getPath();
            if (file.isDirectory()) {
                find_AllFile(file, target);

//                if (file_path.contains("media")) {
//                    find_AllFile(file, target);
//                } else {
//                    continue;  //跳过无用的目录
//                }
            } else {
                if (file_path.endsWith(".txt")
                        || file_path.endsWith(".abc")
                        || file_path.endsWith(".so")
                        || file_path.endsWith(".xml")
                        || file_path.endsWith(".html")
                        || file_path.endsWith(".json")
                        || file_path.endsWith(".js")) {
                    // 目标文件
                    target.add(file);
                    continue;
                } else if (!file.getName().contains(".")) {
                    // 可执行程序
                    target.add(file);
                    continue;
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
                System.out.println("add-> " + file.getName());
                target.add(file);
            }
        }

    }
}
