package com.fythem.AppScanTool.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class ApkInfoV2 extends BassAppInfo {

    /**
     * 以 AndroidManifest 内的信息为主
     * 1.应用包名
     * 2.应用版本号
     * 3.自定义的权限
     * 4.使用的权限
     * 5.应用加固信息（application name）
     * 6.是否可备份
     * 7.是否可调试
     * 8.组件信息（Activity，Service，Receiver，Provider）
     * 9.签名版本（Janus签名漏洞）
     * 10.资源文件内的敏感信息
     */
    String package_name;
    String app_version;
    ArrayList<String> uses_permission_list;
    HashMap<String, String> permissions_map;
    String shell_name;
    Boolean is_debuggable;
    Boolean is_allow_backup;
    String network_config;
    String icon;
    String app_name;

    HashMap<String, String> activity_list;
    HashMap<String, String> service_list;
    HashMap<String, String> receiver_list;
    HashMap<String, String> provider_list;

    String sign_version;

    HashMap<String, ArrayList<String>> sens_info;

    public ApkInfoV2() {
        this.package_name = "";
        this.app_version = "";
        this.uses_permission_list = new ArrayList<String>();
        this.permissions_map = new HashMap<>();
        this.shell_name = "";
        this.is_debuggable = false;
        this.is_allow_backup = false;
        this.network_config = "default";
        this.activity_list = new HashMap<>();
        this.service_list = new HashMap<>();
        this.receiver_list = new HashMap<>();
        this.provider_list = new HashMap<>();
        this.sign_version = "";
        this.icon = "";
        this.app_name = "";
        this.sens_info = new HashMap<>();
    }

    @Override
    public void showAppInfo() {
        System.out.println("[showAppInfo] PackageName: " + this.package_name);
        System.out.println("[showAppInfo] Version: " + this.app_version);
        System.out.println("[showAppInfo] Uses-Permissions: " + this.uses_permission_list);
        System.out.println("[showAppInfo] Permission: " + this.permissions_map);
        System.out.println("[showAppInfo] Shell: " + this.shell_name);
        System.out.println("[showAppInfo] Debuggable: " + this.is_debuggable);
        System.out.println("[showAppInfo] AllowBackup: " + this.is_allow_backup);
        System.out.println("[showAppInfo] NetworkConfig: " + this.network_config);
        System.out.println("[showAppInfo] Activity: " + this.activity_list);
        System.out.println("[showAppInfo] Service: " + this.service_list);
        System.out.println("[showAppInfo] Receiver: " + this.receiver_list);
        System.out.println("[showAppInfo] Provider: " + this.provider_list);
        System.out.println("[showAppInfo] SignVersion: " + this.sign_version);
        System.out.println("[showAppInfo] Icon: " + this.icon);
        System.out.println("[showAppInfo] AppName: " + this.app_name);
        System.out.println("[showAppInfo] SensInfo: " + this.sens_info);
    }

    public void setPackageName(String packageName) {
        this.package_name = packageName;
    }

    public void setVersion(String version) {
        this.app_version = version;
    }

    public void setUsesPermission(ArrayList<String> usesPermissionList) {
        this.uses_permission_list = usesPermissionList;
    }

    public void setPermissions(HashMap<String, String> permissionMap) {
        this.permissions_map = permissionMap;
    }

    public void setShellName(String shellName) {
        this.shell_name = shellName;
    }

    public void setIsAllowBackup(String allowBackup) {
        this.is_allow_backup = Boolean.valueOf(allowBackup);
    }

    public Boolean getIsDebuggable() {
        return this.is_debuggable;
    }

    public Boolean getIsAllowBackup() {
        return this.is_allow_backup;
    }

    public void setIsDebuggable(String debuggable) {
        this.is_debuggable = Boolean.valueOf(debuggable);
    }

    public void setNetworkSecurityConfig(String networkSecurityConfig) {
        if (networkSecurityConfig.isEmpty()) {
            return;
        }
        this.network_config = networkSecurityConfig;
    }


    public void setActivityList(HashMap<String, String> activityMap) {
        this.activity_list = activityMap;
    }

    public void setServiceList(HashMap<String, String> serviceMap) {
        this.service_list = serviceMap;
    }

    public void setReceiverList(HashMap<String, String> receiverMap) {
        this.receiver_list = receiverMap;
    }

    public void setProviderList(HashMap<String, String> providerMap) {
        this.provider_list = providerMap;
    }

    public void setSignVersion(String signVersion) {
        this.sign_version = signVersion;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setAppName(String appName) {
        this.app_name = appName;
    }

    public void setSensInfo(HashMap<String, ArrayList<String>> sensInfo) {
        this.sens_info = sensInfo;
    }

    public String getAppName() {
        return this.app_name;
    }

    public String getAppVersion() {
        return this.app_version;
    }

    public String getPackageName() {
        return this.package_name;
    }

    public String getShellName() {
        return this.shell_name;
    }

    public HashMap<String, String> getPermissions() {
        return this.permissions_map;
    }

    public HashMap<String, String> getActivities() {
        return this.activity_list;
    }

    public HashMap<String, String> getServices() {
        return this.service_list;
    }

    public HashMap<String, String> getReceivers() {
        return this.receiver_list;
    }

    public HashMap<String, String> getProviders() {
        return this.provider_list;
    }

    public String getSignVersion() {
        return this.sign_version;
    }

    public HashMap<String, ArrayList<String>> getSensInfo() {
        return sens_info;
    }
}
