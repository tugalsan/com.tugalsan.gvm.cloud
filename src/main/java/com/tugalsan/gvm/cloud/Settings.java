package com.tugalsan.gvm.cloud;

import com.tugalsan.api.file.properties.server.TS_FilePropertiesUtils;
import com.tugalsan.api.file.server.TS_FileUtils;
import com.tugalsan.api.file.server.TS_PathUtils;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.nio.file.Path;
import java.util.Properties;

public class Settings {

    final private static TS_Log d = TS_Log.of(true, Settings.class);

    public static Path pathDefault() {
        return TS_PathUtils.getPathCurrent_nio(Settings.class.getName() + ".properties");
    }

    private Settings(Path propsFile) {
        var propsExists = TS_FileUtils.isExistFile(propsFile);
        var props = propsExists ? TS_FilePropertiesUtils.read(propsFile) : new Properties();
        ip = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.cloud.Main_ip", "localhost");
        d.ci("construtor", "ip", ip);
        var redirectToSSLStr = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.cloud.Main_sslRedirect", "true");
        d.ci("construtor", "redirectToSSLStr", redirectToSSLStr);
        redirectToSSL = TGS_UnSafe.call((() -> Boolean.valueOf(redirectToSSLStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslRedirectStr: Cannot convert String to Boolean: " + redirectToSSLStr)));
        var sslPortStr = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.cloud.Main_sslPort", "8081");
        d.ci("construtor", "sslPortStr", sslPortStr);
        sslPort = TGS_UnSafe.call((() -> Integer.valueOf(sslPortStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslPortStr: Cannot convert String to Integer: " + sslPortStr)));
        var sslPathStr = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.cloud.Main_sslPath", "D:/xampp_data/SSL/tomcat.p12");
        d.ci("construtor", "sslPathStr", sslPathStr);
        sslPath = TGS_UnSafe.call((() -> Path.of(sslPathStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslPathStr: Cannot convert String to Path: " + sslPathStr)));
        sslPass = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.cloud.Main_sslPass", "MyPass");
        d.ci("construtor", "sslPass", sslPass);
        fileHandlerServletName = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.cloud.Main_fileHandlerServletName", "/file/");
        d.ci("construtor", "fileHandlerServletName", fileHandlerServletName);
        var fileHandlerRootStr = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.cloud.Main_fileHandlerRoot", "D:/file");
        d.ci("construtor", "fileHandlerRoot", fileHandlerRootStr);
        fileHandlerRoot = TGS_UnSafe.call((() -> Path.of(fileHandlerRootStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for fileHandlerRootStr: Cannot convert String to Path: " + fileHandlerRootStr)));
        if (!propsExists) {
            TS_FilePropertiesUtils.write(props, propsFile);
        }
    }
    final public boolean redirectToSSL;
    final public int sslPort;
    final public Path sslPath, fileHandlerRoot;
    final public String ip, sslPass, fileHandlerServletName;

    public static Settings of(Path propsFile) {
        return new Settings(propsFile);
    }

}
