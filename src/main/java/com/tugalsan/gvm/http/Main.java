package com.tugalsan.gvm.http;

import com.tugalsan.api.file.client.TGS_FileTypes;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.servlet.http.server.TS_SHttpConfigNetwork;
import com.tugalsan.api.servlet.http.server.TS_SHttpConfigSSL;
import com.tugalsan.api.servlet.http.server.TS_SHttpHandlerText;
import com.tugalsan.api.servlet.http.server.TS_SHttpServer;
import com.tugalsan.api.servlet.http.server.TS_SHttpUtils;
import com.tugalsan.api.tuple.client.TGS_Tuple2;
import com.tugalsan.api.url.client.TGS_Url;
import com.tugalsan.api.url.client.parser.TGS_UrlParser;
import com.tugalsan.api.validator.client.TGS_ValidatorType1;

public class Main {

    final private static TS_Log d = TS_Log.of(true, Main.class);

    //HOW TO EXECUTE
    //WHEN RUNNING IN NETBEANS, ALL DEPENDENCIES SHOULD HAVE TARGET FOLDER!
    //cd D:\git\gvm\com.tugalsan.gvm.http
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.gvm.http-1.0-SNAPSHOT-jar-with-dependencies.jar    
    public static void main(String[] args) {
        var props = ApplicationProperties.of(ApplicationProperties.pathDefault());
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
        var network = TS_SHttpConfigNetwork.of(props.ip, props.sslPort);
        var ssl = TS_SHttpConfigSSL.of(props.sslPath, props.sslPass, props.redirectToSSL);
        TS_SHttpServer.startHttpsServlet(network, ssl, allow, props.pathFileServer, customTextHandler);
    }

}
