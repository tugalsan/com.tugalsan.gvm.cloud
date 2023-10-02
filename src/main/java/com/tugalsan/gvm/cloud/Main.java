package com.tugalsan.gvm.cloud;

import com.tugalsan.api.coronator.client.TGS_Coronator;
import com.tugalsan.api.file.client.*;
import com.tugalsan.api.file.server.TS_FileUtils;
import com.tugalsan.api.file.server.TS_PathUtils;
import com.tugalsan.api.file.txt.server.TS_FileTxtUtils;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.network.server.TS_NetworkSSLUtils;
import com.tugalsan.api.os.server.TS_OsPlatformUtils;
import com.tugalsan.api.os.server.TS_OsProcess;
import com.tugalsan.api.random.client.TGS_RandomUtils;
import com.tugalsan.api.servlet.http.server.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.async.TS_ThreadAsyncAwait;
import com.tugalsan.api.thread.server.async.TS_ThreadAsyncScheduled;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncLst;
import com.tugalsan.api.time.client.TGS_Time;
import com.tugalsan.api.tuple.client.*;
import com.tugalsan.lib.cloud.client.TGS_LibCloudUtils;
import com.tugalsan.lib.license.server.TS_LibLicenseFileUtils;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class Main {//extended from com.tugalsan.tst.servlet.http.Main

    final private static TS_Log d = TS_Log.of(false, Main.class);
    final private static TS_Log d_thread = TS_Log.of(false, Main.class);
    final private static TS_Log d_caller = TS_Log.of(false, Main.class);
    final private static Duration maxExecutionDuration = Duration.ofMinutes(10);

    //HOW TO EXECUTE
    //WHEN RUNNING IN NETBEANS, ALL DEPENDENCIES SHOULD HAVE TARGET FOLDER!
    //cd D:\git\gvm\com.tugalsan.gvm.cloud
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.gvm.cloud-1.0-SNAPSHOT-jar-with-dependencies.jar    
    public static void main(String[] args) {
        //PREREQUESTS
        TS_NetworkSSLUtils.disableCertificateValidation();
        var kill = TS_ThreadSyncTrigger.of();
        var settings = Settings.of(Settings.pathDefault());
        if (settings == null) {
            d.ce("main", "ERROR: settings == null");
            return;
        }

        //LICENSE
        if (!TS_LibLicenseFileUtils.checkLicenseFromLicenseFile(Main.class)) {
            TS_LibLicenseFileUtils.createLicenseFileFromServer(Main.class);
        }
        if (!TS_LibLicenseFileUtils.checkLicenseFromLicenseFile(Main.class)) {
            TS_LibLicenseFileUtils.createRequestFile(Main.class);
            d.ce("main", "ERROR: license server cannot be reached", "request file created instead");
            return;
        }

        startRowCleanUp(kill);
        var nativeSupplier = nativeSupplier(settings);
        var nativeCaller = nativeCaller(kill, settings);
        startHttps(settings, nativeSupplier, nativeCaller);
        startRowCleanUp(kill);
        if (d_thread.infoEnable) {
            startRowInfo(kill, Duration.ofSeconds(10));
        }
        d.cr("main", "https://" + settings.ip + ":" + settings.sslPort);
    }

    private static void startRowInfo(TS_ThreadSyncTrigger kill, Duration durInfo) {
        TS_ThreadAsyncScheduled.every(kill, true, durInfo, kt -> {
            d_thread.ci("startRowInfo", "rows.size()", rows.size());
            rows.forEach(row -> d_thread.ci("startRowInfo", row));
        });
    }

    private static void startRowCleanUp(TS_ThreadSyncTrigger kill) {
        TS_ThreadAsyncScheduled.every(kill, true, maxExecutionDuration, kt -> {
            var ago = TGS_Time.ofMinutesAgo((int) maxExecutionDuration.toMinutes());
            d_thread.ci("startRowCleanUp", "will clean before", ago.toString_dateOnly(), ago.toString_timeOnly_simplified());
            d_thread.ci("startRowCleanUp", "before", "rows.size()", rows.size());
            rows.removeAll(row -> ago.hasGreater(row.time));
            d_thread.ci("startRowCleanUp", "after", "rows.size()", rows.size());
        });
    }

    private static void startHttps(Settings settings, TS_SHttpHandlerAbstract... customHandler) {
        TS_SHttpServer.of(
                TS_SHttpConfigNetwork.of(settings.ip, settings.sslPort),
                TS_SHttpConfigSSL.of(settings.sslPath, settings.sslPass, settings.redirectToSSL),
                TS_SHttpConfigHandlerFile.of(settings.fileHandlerServletName, r -> true, settings.fileHandlerRoot, settings.onHandlerFile_filterUrlsWithHiddenChars),
                customHandler
        );
    }

    private static TS_SHttpHandlerAbstract nativeSupplier(Settings settings) {
        return TS_SHttpHandlerString.of("/" + TGS_LibCloudUtils.SERVLET_NAME_NATIVE_SUPPLY, r -> true, request -> {
            d_caller.ci("nativeSupplier", "request.url", request.url);
            if (!request.isLocalClient()) {
                return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, "ERROR: !request.isLocalClient() @ " + request.url);
            }
            d_caller.ci("nativeSupplier", "passed local");
            var pRowHash = request.url.quary.getParameterByName(TGS_LibCloudUtils.SERVLET_PARAM_ROW_HASH);
            if (pRowHash == null) {
                return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, "ERROR: pRowHash == null @ " + request.url);
            }
            d_caller.ci("nativeSupplier", "pRowHash", pRowHash);
            var row = rows.findFirst(r -> Objects.equals(r.hash, pRowHash.valueUrlSafe));
            if (row == null) {
                return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, "ERROR: row == null @ " + request.url);
            }
            d_caller.ci("nativeSupplier", "SUCCESS", "row.url", row.url);
            return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, row.url);

        }, settings.onHandlerString_removeHiddenChars);
    }

    private static TS_SHttpHandlerAbstract nativeCaller(TS_ThreadSyncTrigger kill, Settings settings) {
        return TS_SHttpHandlerString.of("/", r -> true, request -> {
            var isWindows = TS_OsPlatformUtils.isWindows();
            d_caller.ci("nativeCaller", "hello");
            var pathExecutor = nativeCaller_pick(isWindows, request);
            d_caller.ci("nativeCaller", "pathExecutor", pathExecutor);
            if (pathExecutor == null) {
                request.sendError404("nativeCaller", "ERROR: pathExecutor == null");
                return null;
            }
            var nameType = TS_FileUtils.getNameType(pathExecutor);
            d_caller.ci("nativeCaller", "nameType",nameType);
            if (nameType.equals("htm")) {
                return TGS_Tuple2.of(TGS_FileTypes.htm_utf8, TS_FileTxtUtils.toString(pathExecutor));
            }
            if (nameType.equals("txt")) {
                return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, TS_FileTxtUtils.toString(pathExecutor));
            }
            var row = Row.of(
                    TGS_RandomUtils.nextString(20, true, true, true, false, null),
                    request.url.toString(), TGS_Time.of()
            );
            rows.add(row);
            var outExecution = nativeCaller_call(kill, maxExecutionDuration, request, pathExecutor, row.hash);
            rows.removeFirst(r -> Objects.equals(r.hash, row.hash));
            if (outExecution == null) {
                request.sendError404("nativeCaller", "ERROR: outExecution == null");
                return null;
            }
            d_caller.ci("nativeCaller", "outExecution", outExecution);
            var type = TGS_FileTypes.txt_utf8;
            var firstSpaceIndex = outExecution.indexOf(" ");
            if (firstSpaceIndex != -1) {
                var typeStr = outExecution.substring(0, firstSpaceIndex);
                var typeNew = TGS_FileTypes.findByContenTypePrefix(typeStr);
                if (typeNew != null) {
                    type = typeNew;
                    outExecution = outExecution.substring(firstSpaceIndex + 1);
                }
            }
            return TGS_Tuple2.of(type, outExecution);
        }, settings.onHandlerString_removeHiddenChars);
    }
    final static private TS_ThreadSyncLst<Row> rows = new TS_ThreadSyncLst();

    private static String nativeCaller_call(TS_ThreadSyncTrigger kill, Duration maxExecutionDuration, TS_SHttpHandlerRequest request, Path pathExecutor, String rowHash) {
        var await = TS_ThreadAsyncAwait.callSingle(kill, maxExecutionDuration, kt -> {
            var cmdList = pathExecutor.toString().endsWith("jar")
                    ? List.of("java", "--enable-preview", "--add-modules", "jdk.incubator.vector", "-jar", pathExecutor.toString(), rowHash)
                    : List.of(pathExecutor.toString(), rowHash);
            return TS_OsProcess.of(cmdList);
        });
        if (await.hasError()) {
            request.sendError404("nativeCaller_call", "ERROR: await.exception: " + await.exceptionIfFailed.get().toString());
            return null;
        }
        var process = await.resultIfSuccessful.get();
        if (process.exception != null) {
            request.sendError404("nativeCaller_call", "ERROR: process.exception != null -> " + process.exception);
            return null;
        }
        return process.output;
    }

    private static Path nativeCaller_pick(boolean isWindows, TS_SHttpHandlerRequest request) {
        var servletName = request.url.path.fileOrServletName;
        d_caller.ci("nativeCaller_pick", "servletName", servletName, "url", request.url);
        var fileNameLabel = TGS_Coronator.ofStr()
                .anoint(val -> servletName)
                .anointIf(TGS_StringUtils::isNullOrEmpty, val -> "home")
                .anoint(val -> TGS_FileUtilsTur.toSafe(val))
                .coronate();
        d_caller.ci("nativeCaller_pick", "fileNameLabel", fileNameLabel);
        var filePath = TGS_Coronator.of(Path.class).coronateAs(val -> {
            if (!isWindows) {
                var sh = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".sh");
                if (TS_FileUtils.isExistFile(sh)) {
                    d.ci("nativeCaller_pick", "picked", sh);
                    return sh;
                }
                request.sendError404("ERROR: sh file not found", sh.toString());
                return null;
            }
            if (isWindows) {
                var bat = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".bat");
                if (TS_FileUtils.isExistFile(bat)) {
                    d.ci("nativeCaller_pick", "picked", bat);
                    return bat;
                }
                var exe = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".exe");
                if (TS_FileUtils.isExistFile(exe)) {
                    d.ci("nativeCaller_pick", "picked", exe);
                    return exe;
                }
            }
            var jar = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".jar");
            if (TS_FileUtils.isExistFile(jar)) {
                d.ci("nativeCaller_pick", "picked", jar);
                return jar;
            }
            var htm = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".htm");
            if (TS_FileUtils.isExistFile(htm)) {
                d.ci("nativeCaller_pick", "picked", htm);
                return htm;
            }
            var txt = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".txt");
            if (TS_FileUtils.isExistFile(txt)) {
                d.ci("nativeCaller_pick", "picked", txt);
                return txt;
            }
            request.sendError404("ERROR: bat or exe file not found", TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".xxx").toString());
            return null;
        });
        return filePath;
    }

}
