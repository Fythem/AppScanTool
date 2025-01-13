package com.fythem.AppScanTool.entity;

import java.util.*;

public class ShellInfo {
    private static List<String> sPackageName = new ArrayList<>();
    private static Map<String, Type> sTypeMap = new HashMap<>();

    /**
     * 360加固
     */
    private static final String[] QI_HOO = {"com.stub.StubApp"};
    /**
     * 爱加密
     */
    private static final String[] AI_JIA_MI = {"s.h.e.l.l.S"};
    /**
     * 梆梆加固
     */
    private static final String[] BANG_BANG = {"com.secneo.apkwrapper.ApplicationWrapper", "com.secneo.apkwrapper"};
    /**
     * 腾讯加固
     */
    private static final String[] TENCENT = {"com.tencent.StubShell.TxAppEntry"};
    /**
     * 百度加固
     */
    private static final String[] BAI_DU = {"com.baidu.protect.StubApplication"};


    static {
        sPackageName.addAll(Arrays.asList(QI_HOO));
        sPackageName.addAll(Arrays.asList(AI_JIA_MI));
        sPackageName.addAll(Arrays.asList(BANG_BANG));
        sPackageName.addAll(Arrays.asList(TENCENT));
        sPackageName.addAll(Arrays.asList(BAI_DU));

        for (String s : QI_HOO) {
            sTypeMap.put(s, Type.QI_HOO);
        }
        for (String s : AI_JIA_MI) {
            sTypeMap.put(s, Type.AI_JIA_MI);
        }
        for (String s : BANG_BANG) {
            sTypeMap.put(s, Type.BANG_BANG);
        }
        for (String s : TENCENT) {
            sTypeMap.put(s, Type.TENCENT);
        }
        for (String s : BAI_DU) {
            sTypeMap.put(s, Type.BAI_DU);
        }

    }

    public static String find(String pkg_name) {
        String shell_name = Type.UNKNOWN.getName();
        for (String s : sPackageName) {
            if (pkg_name.startsWith(s)) {
                shell_name = getType(s).getName();
            }
        }
        return shell_name;
    }

    private static Type getType(String packageName) {
        return sTypeMap.get(packageName);
    }

    public enum Type {
        QI_HOO("360加固"),
        AI_JIA_MI("爱加密"),
        BANG_BANG("梆梆加固"),
        TENCENT("腾讯加固"),
        BAI_DU("百度加固"),
        UNKNOWN("未识别加固");

        String name;

        Type(String s) {
            name = s;
        }

        public String getName() {
            return name;
        }
    }
}