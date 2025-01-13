package com.fythem.AppScanTool.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class IpaInfoV2 extends BassAppInfo {
    boolean PIE;
    boolean stack;
    boolean ARC;
    String info_plist;
    String bundle_name;
    String package_name;
    boolean ats;
    Set<String> special_domains;
    Set<String> permissions;
    Set<String> url_schemes;
    String ipa_version;
    String exe_file;
    HashMap<String, ArrayList<String>> sens_info;


    public IpaInfoV2() {
        this.PIE = false;
        this.stack = false;
        this.ARC = false;
        this.bundle_name = "";
        this.info_plist = "";
        this.package_name = "";
        this.ats = false;
        this.special_domains = new HashSet<>();
        this.permissions = new HashSet<>();
        this.url_schemes = new HashSet<>();
        this.ipa_version = "";
        this.exe_file = "";
        this.sens_info = new HashMap<>();
    }

    @Override
    public void showAppInfo() {
        System.out.println("[showIpaInfo] PIE: " + this.PIE);
        System.out.println("[showIpaInfo] stack: " + this.stack);
        System.out.println("[showIpaInfo] ARC: " + this.stack);
        System.out.println("[showIpaInfo] Info.plist: " + this.info_plist);
        System.out.println("[showIpaInfo] ExeFile: " + this.exe_file);
        System.out.println("[showIpaInfo] BundleName: " + this.bundle_name);
        System.out.println("[showIpaInfo] PackageName: " + this.package_name);
        System.out.println("[showIpaInfo] Version: " + this.ipa_version);
        System.out.println("[showIpaInfo] ATS: " + this.ats);
        System.out.println("[showIpaInfo] SpecialDomains: " + this.special_domains);
        System.out.println("[showIpaInfo] Permissions: " + this.permissions);
        System.out.println("[showIpaInfo] Url Schemes: " + this.url_schemes);
        System.out.println("[showIpaInfo] SensInfo: " + this.sens_info);
    }

    public String getBundleName() {
        return this.bundle_name;
    }

    public String getPackageName() {
        return this.package_name;
    }

    public String getVersion() {
        return this.ipa_version;
    }

    public void setBundleName(String bundleName) {
        this.bundle_name = bundleName;
    }

    public void setInfoPlist(String infoPlistPath) {
        this.info_plist = infoPlistPath;
    }

    public String getInfo_plist() {
        return this.info_plist;
    }

    public void setPackageName(String pkgName) {
        this.package_name = pkgName;
    }

    public void setIpaVersion(String ipa_version) {
        this.ipa_version = ipa_version;
    }

    public void setATS(Boolean ats) {
        if (ats != null) {
            this.ats = ats;
        }
    }

    public void setSpecial_domains(Set<String> special_domains) {
        this.special_domains = special_domains;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public void setUrlScheme(Set<String> url_schemes) {
        this.url_schemes = url_schemes;
    }

    public void setExeFile(String exe_file) {
        this.exe_file = exe_file;
    }

    public void setSensInfo(HashMap<String, ArrayList<String>> sensInfo) {
        this.sens_info = sensInfo;
    }

    public void setPIE(boolean pie) {
        this.PIE = pie;
    }


    public String getExe_file() {
        return this.exe_file;
    }

    public Boolean getATS() {
        return this.ats;
    }

    public Set<String> getUrlSchemes() {
        return this.url_schemes;
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public Boolean getPIE() {
        return this.PIE;
    }

    public void setStack(boolean stack) {
        this.stack = stack;
    }

    public Boolean getStack() {
        return this.stack;
    }

    public void setARC(boolean arc) {
        this.ARC = arc;
    }

    public Boolean getARC() {
        return this.ARC;
    }

    public HashMap<String, ArrayList<String>> getSensInfo() {
        return this.sens_info;
    }
}
