package com.tugalsan.gvm.http;

import com.tugalsan.api.file.properties.server.TS_FilePropertiesUtils;
import com.tugalsan.api.file.server.TS_FileUtils;
import com.tugalsan.api.file.server.TS_PathUtils;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.nio.file.Path;
import java.util.Properties;

public class ApplicationProperties {

    final private static TS_Log d = TS_Log.of(true, ApplicationProperties.class);

    public static Path pathDefault() {
        return TS_PathUtils.getPathCurrent_nio(ApplicationProperties.class.getName() + ".properties");
    }

    private ApplicationProperties(Path propsFile) {
        var propsExists = TS_FileUtils.isExistFile(propsFile);
        var props = propsExists ? TS_FilePropertiesUtils.read(propsFile) : new Properties();
        ip = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.http.Main_ip", "localhost");
        d.ci("construtor", "ip", ip);
        var redirectToSSLStr = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.http.Main_sslRedirect", "true");
        d.ci("construtor", "redirectToSSLStr", redirectToSSLStr);
        redirectToSSL = TGS_UnSafe.call((() -> Boolean.valueOf(redirectToSSLStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslRedirectStr: Cannot convert String to Boolean: " + redirectToSSLStr)));
        var sslPortStr = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.http.Main_sslPort", "8081");
        d.ci("construtor", "sslPortStr", sslPortStr);
        sslPort = TGS_UnSafe.call((() -> Integer.valueOf(sslPortStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslPortStr: Cannot convert String to Integer: " + sslPortStr)));
        var sslPathStr = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.http.Main_sslPath", "D:/xampp_data/SSL/tomcat.p12");
        d.ci("construtor", "sslPathStr", sslPathStr);
        sslPath = TGS_UnSafe.call((() -> Path.of(sslPathStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslPathStr: Cannot convert String to Path: " + sslPathStr)));
        sslPass = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.http.Main_sslPass", "MyPass");
        d.ci("construtor", "sslPass", sslPass);
        var pathFileServerStr = TS_FilePropertiesUtils.getValue(props, "com.tugalsan.gvm.http.Main_pathFileServer", "D:/file");
        d.ci("construtor", "pathFileServerStr", pathFileServerStr);
        pathFileServer = TGS_UnSafe.call((() -> Path.of(pathFileServerStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for pathFileServerStr: Cannot convert String to Path: " + pathFileServerStr)));
        if (!propsExists) {
            TS_FilePropertiesUtils.write(props, propsFile);
        }
    }
    final public boolean redirectToSSL;
    final public int sslPort;
    final public Path sslPath, pathFileServer;
    final public String ip, sslPass;

    public static ApplicationProperties of(Path propsFile) {
        return new ApplicationProperties(propsFile);
    }

}
