package com.tugalsan.gvm.http;

import com.tugalsan.api.file.client.TGS_FileTypes;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.servlet.http.server.TS_SHttpConfigNetwork;
import com.tugalsan.api.servlet.http.server.TS_SHttpConfigSSL;
import com.tugalsan.api.servlet.http.server.TS_SHttpHandlerRequest;
import com.tugalsan.api.servlet.http.server.TS_SHttpHandlerText;
import com.tugalsan.api.servlet.http.server.TS_SHttpServer;
import com.tugalsan.api.tuple.client.TGS_Tuple2;
import com.tugalsan.api.validator.client.TGS_ValidatorType1;

public class Main {

    final private static TS_Log d = TS_Log.of(true, Main.class);

    //HOW TO EXECUTE
    //WHEN RUNNING IN NETBEANS, ALL DEPENDENCIES SHOULD HAVE TARGET FOLDER!
    //cd D:\git\gvm\com.tugalsan.gvm.http
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.gvm.http-1.0-SNAPSHOT-jar-with-dependencies.jar    
    public static void main(String[] args) {
        var settings = Settings.of(Settings.pathDefault());
        TGS_ValidatorType1<TS_SHttpHandlerRequest> allow = request -> {
            if (!request.isLocal()) {
                request.sendError404("ERROR: i am grumpy, and will work only localhost 😠");
                return false;
            }
            return true;
        };
        var customTextHandler = TS_SHttpHandlerText.of("/", allow, request -> {
            return TGS_Tuple2.of(TGS_FileTypes.htm_utf8,
                    "<html><head><script>location.reload();</script></head><body>"
                    + request.url
                    + "<body></html>"
            );
        });
        var network = TS_SHttpConfigNetwork.of(settings.ip, settings.sslPort);
        var ssl = TS_SHttpConfigSSL.of(settings.sslPath, settings.sslPass, settings.redirectToSSL);
        TS_SHttpServer.startHttpsServlet(network, ssl, allow, settings.pathFileServer, customTextHandler);
    }

}
