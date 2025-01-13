package com.fythem.AppScanTool.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class HapInfoV1 extends BassAppInfo {

    String bundle_Name;
    String debug;
    String label;
    String version;
    String abc_file;
    String out_abc;
    ArrayList<String> mix_mode;
    Boolean window_privacy;
    Boolean ssl_pass;
    HashMap<String, ArrayList<String>> sens_info;


    public HapInfoV1() {
        this.bundle_Name = "";
        this.debug = "";
        this.label = "";
        this.version = "";
        this.abc_file = "";
        this.out_abc = "";
        this.mix_mode = new ArrayList<String>();
        this.window_privacy = false;    //true 表示有截屏保护
        this.ssl_pass = false;  //true 表示存在ssl error不处理,存在风险。开发时默认cancel请求
        this.sens_info = new HashMap<>();
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getDebug() {
        return this.debug;
    }

    public void setWindowPrivacy(Boolean is_privacy) {
        this.window_privacy = is_privacy;
    }

    public Boolean getWindowPrivacy() {
        return this.window_privacy;
    }

    public void setSSLError(Boolean ssl_pass) {
        this.ssl_pass = ssl_pass;
    }

    public boolean getSSLError() {
        return this.ssl_pass;
    }

    public void setBundle_Name(String bundle_Name) {
        this.bundle_Name = bundle_Name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void showAppInfo() {
        System.out.println("[showHapInfo] BundleName: " + this.bundle_Name);
        System.out.println("[showHapInfo] Label: " + this.label);
        System.out.println("[showHapInfo] Version: " + this.version);
        System.out.println("[showHapInfo] Debug: " + this.debug);
        System.out.println("[showHapInfo] .abc: " + this.abc_file);
        System.out.println("[showHapInfo] ark_disasm.abc: " + this.out_abc);
        System.out.println("[showHapInfo] mix_mode: " + this.mix_mode);
        System.out.println("[showHapInfo] window_privacy: " + this.window_privacy);
        System.out.println("[showHapInfo] ssl_pass: " + this.ssl_pass);
        System.out.println("[showHapInfo] SensInfo: " + this.sens_info);

    }


    public void setOutAbc(String out_abc) {
        this.out_abc = out_abc;
    }

    public String getOutAbc() {
        return this.out_abc;
    }

    public void setAbc_file(String abc_file) {
        this.abc_file = abc_file;
    }

    public String getAbc_file() {
        return this.abc_file;
    }

    public String getBundleName() {
        return this.bundle_Name;
    }

    public String getLabel() {
        return this.label;
    }

    public String getVersion() {
        return this.version;
    }

    public void addMixedMode(String code) {
        this.mix_mode.add(code);
    }

    public ArrayList<String> getMixMode() {
        return this.mix_mode;
    }

    public void setSensInfo(HashMap<String, ArrayList<String>> sensInfo) {
        this.sens_info = sensInfo;
    }

    public HashMap<String, ArrayList<String>> getSensInfo() {
        return sens_info;
    }

}
