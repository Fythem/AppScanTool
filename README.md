# AppScanTool

> App扫描工具（Android/iOS/HarmonyNext）
>
> ！！！为了简化工作！！！

AppScanTool 是一个简单的扫描工具。可对 ".apk"，".ipa"，".hap" 文件进行扫描。

## 目前实现的功能

Android / iOS / HarmonyNext 客户端的基本扫描

- Android
    - AndroidManifest配置（debuggable，allowbackup，权限保护等级）
    - 导出组件（Activity，Service，Receiver，Provider）
    - 应用签名问题（CVE-2017-13156）
    - 应用包内特殊信息（url，token，各类凭证）
- iOS
    - info.plist（ATS，Url Scheme，权限申请）
    - 可执行程序（PIE、栈Cookie保护、ARC）
    - 应用包内特殊信息（url，token，各类凭证）
- HarmonyNext
    - module.json配置（debug）
    - abc（mixmode.ALL，屏幕保护，SSL ERROR）
    - 应用包内特殊信息（url，token，各类凭证）

## 环境需求

- Java1.8
- 高版本 Java 需要配置 [javafx](https://gluonhq.com/products/javafx/) 运行参数

## 命令行

- Java 1.8

```shell
java -jar AppScanTool.jar
```

- Java 22

```shell
java --module-path /javafx-sdk-23.0.1/lib --add-modules javafx.controls,javafx.fxml -jar AppScanTool.jar
```

## 参考信息
> Android、iOS部分，根据平常的测试需要，输出攻击面或者信息。
>
>HarmonyNext部分，参考华为开发文档 [《应用安全编码实践》](https://developer.huawei.com/consumer/cn/doc/best-practices-V5/bpta-harmony-application-security-V5) 




