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
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.async.TS_ThreadAsyncAwait;
import com.tugalsan.api.tuple.client.*;
import com.tugalsan.api.validator.client.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class Main {

    final private static TS_Log d = TS_Log.of(true, Main.class);

    //HOW TO EXECUTE
    //WHEN RUNNING IN NETBEANS, ALL DEPENDENCIES SHOULD HAVE TARGET FOLDER!
    //cd D:\git\gvm\com.tugalsan.gvm.cloud
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.gvm.cloud-1.0-SNAPSHOT-jar-with-dependencies.jar    
    public static void main(String[] args) {
        var killer = TS_ThreadSyncTrigger.of();
        var maxDuration = Duration.ofMinutes(10);
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
            //CHOOSE EXECUTOR FROM SERVLET NAME
            var servletName = request.url.path.fileOrServletName;
            var fileNameLabel = TGS_Coronator.ofStr()
                    .anoint(val -> TGS_FileUtilsTur.toSafe(servletName))
                    .anointIf(TGS_StringUtils::isNullOrEmpty, val -> "home")
                    .coronate();
            var filePath = TGS_Coronator.of(Path.class).coronateAs(val -> {
                if (!win) {
                    var sh = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".sh");
                    if (!TS_FileUtils.isExistFile(sh)) {
                        request.sendError404("ERROR: sh file not found", sh);
                        return null;
                    }
                    return null;
                }
                var bat = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".bat");
                if (TS_FileUtils.isExistFile(bat)) {
                    return bat;
                }
                var exe = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".exe");
                if (!TS_FileUtils.isExistFile(exe)) {
                    request.sendError404("ERROR: bat or exe file not found", bat, exe);
                    return null;
                }
                return null;
            });
            if (filePath == null) {
                return null;
            }
            //TODO DB OPS
            var rowId = 0L; //TODO push request.url.toString() to db, fetch row id
            //EXECUTE OPS
            var result = TS_ThreadAsyncAwait.callSingle(killer, maxDuration, kt -> {
                var process = TS_OsProcess.of(List.of(filePath.toString(), String.valueOf(rowId)));
                return process.exitValueOk() ? process.output : process.exception.toString();
            });
            if (result.hasError()) {
                request.sendError404("ERROR: execute", result.exceptionIfFailed.get());
                return null;
            }
            d.ci("main", "result.resultIfSuccessful", result.resultIfSuccessful.get());
            //TODO CREATING RETURN OBJ
            var type = "txt";
            var out = "todo: value at db:" + result.resultIfSuccessful.get();
            //RETURN
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
