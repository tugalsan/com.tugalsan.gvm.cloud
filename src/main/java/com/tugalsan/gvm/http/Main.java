package com.tugalsan.gvm.http;

import com.tugalsan.api.file.client.TGS_FileTypes;
import com.tugalsan.api.file.properties.server.TS_FilePropertiesUtils;
import com.tugalsan.api.file.server.TS_FileUtils;
import com.tugalsan.api.file.server.TS_PathUtils;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.servlet.http.server.TS_SHttpConfigNetwork;
import com.tugalsan.api.servlet.http.server.TS_SHttpConfigSSL;
import com.tugalsan.api.servlet.http.server.TS_SHttpHandlerText;
import com.tugalsan.api.servlet.http.server.TS_SHttpServer;
import com.tugalsan.api.servlet.http.server.TS_SHttpUtils;
import com.tugalsan.api.tuple.client.TGS_Tuple2;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import com.tugalsan.api.url.client.TGS_Url;
import com.tugalsan.api.url.client.parser.TGS_UrlParser;
import com.tugalsan.api.validator.client.TGS_ValidatorType1;
import java.nio.file.Path;
import java.util.Properties;

public class Main {

    final private static TS_Log d = TS_Log.of(true, Main.class);

    //HOW TO EXECUTE
    //WHEN RUNNING IN NETBEANS, ALL DEPENDENCIES SHOULD HAVE TARGET FOLDER!
    //cd D:\git\gvm\com.tugalsan.gvm.http
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.gvm.http-1.0-SNAPSHOT-jar-with-dependencies.jar    
    public static void main(String[] args) {

        var propsFile = TS_PathUtils.getPathCurrent_nio().getParent().resolve("application.properties");
        var propsExists = TS_FileUtils.isExistFile(propsFile);
        var props = propsExists ? TS_FilePropertiesUtils.read(propsFile) : new Properties();
        var ip = props.getProperty("com.tugalsan.gvm.http.Main_ip", "localhost");
        var sslRedirectStr = props.getProperty("com.tugalsan.gvm.http.Main_sslRedirect", "true");
        var sslPortStr = props.getProperty("com.tugalsan.gvm.http.Main_sslPort", "8081");
        var sslPathStr = props.getProperty("com.tugalsan.gvm.http.Main_sslPath", "D:\\xampp_data\\SSL\\tomcat.p12");
        var sslPass = props.getProperty("com.tugalsan.gvm.http.Main_sslPass", "MyPass");
        var pathFileServer = props.getProperty("com.tugalsan.gvm.http.Main_pathFileServer", "D:\\file");

        var sslRedirect = TGS_UnSafe.call((() -> Boolean.parseBoolean(sslRedirectStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslRedirectStr: Cannot convert String to Boolean: " + sslRedirectStr)));
        var sslPort = TGS_UnSafe.call((() -> Integer.parseInt(sslPortStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslPortStr: Cannot convert String to Integer: " + sslPortStr)));
        var sslPath = TGS_UnSafe.call((() -> Path.of(sslPathStr)), e -> TGS_UnSafe.thrwReturns(new RuntimeException("ERROR for sslPathStr: Cannot convert String to Path: " + sslPathStr)));

        TGS_ValidatorType1<TGS_UrlParser> allow = parser -> true;
        var customTextHandler = TS_SHttpHandlerText.of("/", allow, httpExchange -> {
            var uri = TS_SHttpUtils.getURI(httpExchange).orElse(null);
            if (uri == null) {
                d.ce("main", "ERROR sniff url from httpExchange is null");
                TS_SHttpUtils.sendError404(httpExchange);
                return null;
            }
            var parser = TGS_UrlParser.of(TGS_Url.of(uri.toString()));
            return TGS_Tuple2.of(TGS_FileTypes.htm_utf8, uri.toString() + "<br>" + parser.toString());
        });
        var network = TS_SHttpConfigNetwork.of("localhost", 8081);
        var ssl = TS_SHttpConfigSSL.of(Path.of("D:\\xampp_data\\SSL\\tomcat.p12"), "MyPass", true);
        var folder = Path.of("D:\\file");
        TS_SHttpServer.startHttpsServlet(network, ssl, allow, folder, customTextHandler);
    }

}
