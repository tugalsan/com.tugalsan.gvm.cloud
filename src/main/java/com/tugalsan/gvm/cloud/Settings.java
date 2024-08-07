package com.tugalsan.gvm.cloud;

import com.tugalsan.api.file.properties.server.TS_FilePropertiesUtils;
import com.tugalsan.api.file.server.TS_FileUtils;
import com.tugalsan.api.file.server.TS_PathUtils;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.nio.file.Path;
import java.util.Properties;

public class Settings {

    final private static TS_Log d = TS_Log.of(false, Settings.class);

    public static Path pathDefault() {
        return TS_PathUtils.getPathCurrent_nio(Settings.class.getPackageName() + ".properties");
    }

    private Settings(Path propsFile) {
        var propsExists = TS_FileUtils.isExistFile(propsFile);
        var props = propsExists ? TS_FilePropertiesUtils.read(propsFile) : new Properties();

        var onHandlerFile_filterUrlsWithHiddenCharsStr = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_onHandlerFile_filterUrlsWithHiddenChars", "true");
        d.ci("construtor", "onHandlerFile_filterUrlsWithHiddenChars", onHandlerFile_filterUrlsWithHiddenCharsStr);
        onHandlerFile_filterUrlsWithHiddenChars = TGS_UnSafe.call((() -> Boolean.valueOf(onHandlerFile_filterUrlsWithHiddenCharsStr)), e -> TGS_UnSafe.thrw(new RuntimeException("ERROR for onHandlerFile_filterUrlsWithHiddenCharsStr: Cannot convert String to Boolean: " + onHandlerFile_filterUrlsWithHiddenCharsStr)));

        var onHandlerString_removeHiddenCharsStr = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_onHandlerString_removeHiddenChars", "true");
        d.ci("construtor", "onHandlerString_removeHiddenCharsStr", onHandlerString_removeHiddenCharsStr);
        onHandlerString_removeHiddenChars = TGS_UnSafe.call((() -> Boolean.valueOf(onHandlerString_removeHiddenCharsStr)), e -> TGS_UnSafe.thrw(new RuntimeException("ERROR for onHandlerString_removeHiddenCharsStr: Cannot convert String to Boolean: " + onHandlerString_removeHiddenCharsStr)));

        ip = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_ip", "localhost");
        d.ci("construtor", "ip", ip);

        var redirectToSSLStr = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_sslRedirect", "true");
        d.ci("construtor", "redirectToSSLStr", redirectToSSLStr);
        redirectToSSL = TGS_UnSafe.call((() -> Boolean.valueOf(redirectToSSLStr)), e -> TGS_UnSafe.thrw(new RuntimeException("ERROR for sslRedirectStr: Cannot convert String to Boolean: " + redirectToSSLStr)));

        var sslPortStr = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_sslPort", "7443");
        d.ci("construtor", "sslPortStr", sslPortStr);
        sslPort = TGS_UnSafe.call((() -> Integer.valueOf(sslPortStr)), e -> TGS_UnSafe.thrw(new RuntimeException("ERROR for sslPortStr: Cannot convert String to Integer: " + sslPortStr)));

        var sslPathStr = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_sslPath", "D:/xampp_data/SSL/tomcat.p12");
        d.ci("construtor", "sslPathStr", sslPathStr);
        sslPath = TGS_UnSafe.call((() -> Path.of(sslPathStr)), e -> TGS_UnSafe.thrw(new RuntimeException("ERROR for sslPathStr: Cannot convert String to Path: " + sslPathStr)));

        sslPass = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_sslPass", "MyPass");
        d.ci("construtor", "sslPass", sslPass);

        fileHandlerServletName = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_fileHandlerServletName", "/file/");
        d.ci("construtor", "fileHandlerServletName", fileHandlerServletName);

        var fileHandlerRootStr = TS_FilePropertiesUtils.getValue(props, Settings.class.getPackageName() + "_fileHandlerRoot", "D:/file");
        d.ci("construtor", "fileHandlerRoot", fileHandlerRootStr);
        fileHandlerRoot = TGS_UnSafe.call((() -> Path.of(fileHandlerRootStr)), e -> TGS_UnSafe.thrw(new RuntimeException("ERROR for fileHandlerRootStr: Cannot convert String to Path: " + fileHandlerRootStr)));

        if (!propsExists) {
            TS_FilePropertiesUtils.write(props, propsFile);
        } else if (!props.equals(TS_FilePropertiesUtils.read(propsFile))) {
            TS_FilePropertiesUtils.write(props, propsFile);
        }
    }
    final public boolean redirectToSSL, onHandlerString_removeHiddenChars, onHandlerFile_filterUrlsWithHiddenChars;
    final public int sslPort;
    final public Path sslPath, fileHandlerRoot;
    final public String ip, sslPass, fileHandlerServletName;

    public static Settings of(Path propsFile) {
        return new Settings(propsFile);
    }

    @Override
    public String toString() {
        return d.className + "{" + "redirectToSSL=" + redirectToSSL + ", onHandlerString_removeHiddenChars=" + onHandlerString_removeHiddenChars + ", onHandlerFile_filterUrlsWithHiddenChars=" + onHandlerFile_filterUrlsWithHiddenChars + ", sslPort=" + sslPort + ", sslPath=" + sslPath + ", fileHandlerRoot=" + fileHandlerRoot + ", ip=" + ip + ", sslPass=" + sslPass + ", fileHandlerServletName=" + fileHandlerServletName + '}';
    }

}
