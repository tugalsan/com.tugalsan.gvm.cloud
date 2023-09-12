package com.tugalsan.gvm.cloud;

import com.tugalsan.api.coronator.client.TGS_Coronator;
import com.tugalsan.api.file.client.*;
import com.tugalsan.api.file.server.TS_FileUtils;
import com.tugalsan.api.file.server.TS_PathUtils;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.os.server.TS_OsPlatformUtils;
import com.tugalsan.api.os.server.TS_OsProcess;
import com.tugalsan.api.servlet.http.server.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.tuple.client.*;
import com.tugalsan.api.url.client.TGS_Url;
import com.tugalsan.api.url.client.TGS_UrlUtils;
import com.tugalsan.api.validator.client.*;
import java.nio.file.Path;
import java.util.List;

public class Main {

    final private static TS_Log d = TS_Log.of(true, Main.class);

    //HOW TO EXECUTE
    //WHEN RUNNING IN NETBEANS, ALL DEPENDENCIES SHOULD HAVE TARGET FOLDER!
    //cd D:\git\gvm\com.tugalsan.gvm.cloud
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.gvm.cloud-1.0-SNAPSHOT-jar-with-dependencies.jar    
    public static void main(String[] args) {
        var win = TS_OsPlatformUtils.isWindows();
        var settings = Settings.of(Settings.pathDefault());
        TGS_ValidatorType1<TS_SHttpHandlerRequest> allow = request -> {
            if (!request.isLocal()) {
                request.sendError404("ERROR: Will work only localhost 😠");
                return false;
            }
            return true;
        };
        var stringHandler = TS_SHttpHandlerString.of("/", allow, request -> {
            var servletName = request.url.path.fileOrServletName;
            if (TGS_UrlUtils.isHackedUrl(TGS_Url.of(servletName))) {
                request.sendError404("ERROR: hack detected", servletName);
                return null;
            }
            var fileNameLabel = TGS_Coronator.ofStr()
                    .anoint(val -> TGS_FileUtilsTur.toSafe(servletName))
                    .anointIf(TGS_StringUtils::isNullOrEmpty, val -> "home")
                    .coronate();
            var filePath = TGS_Coronator.of(Path.class).coronateAs(val -> {
                if (!win) {
                    return TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".sh");
                }
                var bat = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".bat");
                if (TS_FileUtils.isExistFile(bat)) {
                    return bat;
                }
                return TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".exe");
            });
            if (!TS_FileUtils.isExistFile(filePath)) {
                request.sendError404("ERROR: File not found", filePath);
                return null;
            }
            var process = TS_OsProcess.of(List.of(filePath.toString(), request.url.toString()));
            var out = process.exitValueOk() ? process.output : process.exception.toString();
            var type = out.substring(0, 3);
            return TGS_Tuple2.of(TGS_FileTypes.findByContenTypePrefix(type), out);
        });
        TS_SHttpServer.of(
                TS_SHttpConfigNetwork.of(settings.ip, settings.sslPort),
                TS_SHttpConfigSSL.of(settings.sslPath, settings.sslPass, settings.redirectToSSL),
                TS_SHttpConfigHandlerFile.of(settings.fileHandlerServletName, allow, settings.fileHandlerRoot),
                stringHandler
        );
    }

}
