package com.tugalsan.gvm.cloud;

import com.tugalsan.api.coronator.client.TGS_Coronator;
import com.tugalsan.api.file.client.*;
import com.tugalsan.api.file.server.TS_FileUtils;
import com.tugalsan.api.file.server.TS_PathUtils;
import com.tugalsan.api.log.server.*;
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
import com.tugalsan.api.validator.client.*;
import com.tugalsan.lib.license.server.TS_LibLicenseFileUtils;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class Main {//extended from com.tugalsan.tst.servlet.http.Main

    final private static TS_Log d = TS_Log.of(true, Main.class);
    final private static Duration maxExecutionDuration = Duration.ofMinutes(10);

    //HOW TO EXECUTE
    //WHEN RUNNING IN NETBEANS, ALL DEPENDENCIES SHOULD HAVE TARGET FOLDER!
    //cd D:\git\gvm\com.tugalsan.gvm.cloud
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.gvm.cloud-1.0-SNAPSHOT-jar-with-dependencies.jar    
    public static void main(String[] args) {
        if (!TS_LibLicenseFileUtils.check(Main.class)) {
            TS_LibLicenseFileUtils.request(Main.class);
//            TS_LibLicenseFileUtils.giveTo(TS_LibLicenseFileUtils.fileReq(Main.class));
            d.ce("main", "ERROR: Not licensed yet");
            return;
        }
        var killer = TS_ThreadSyncTrigger.of();//not used for now
        startRowCleanUp(killer);
        var settings = Settings.of(Settings.pathDefault());
        TGS_ValidatorType1<TS_SHttpHandlerRequest> allowCommon = request -> true;
        var handlerExecutor = createHandlerNativeCaller(killer, allowCommon, settings);
        startHttps(settings, allowCommon, handlerExecutor);
    }

    private static void startRowCleanUp(TS_ThreadSyncTrigger killer) {
        TS_ThreadAsyncScheduled.every(killer, true, maxExecutionDuration, kt -> {
            var ago = TGS_Time.ofMinutesAgo((int) maxExecutionDuration.toMinutes());
            rows.removeAll(row -> ago.hasGreater(row.value2));
            d.ci("startRowCleanUp", "INFO rows.size()", rows.size());
        });
    }

    private static void startHttps(Settings settings, TGS_ValidatorType1<TS_SHttpHandlerRequest> allow, TS_SHttpHandlerAbstract... customHandler) {
        TS_SHttpServer.of(
                TS_SHttpConfigNetwork.of(settings.ip, settings.sslPort),
                TS_SHttpConfigSSL.of(settings.sslPath, settings.sslPass, settings.redirectToSSL),
                TS_SHttpConfigHandlerFile.of(settings.fileHandlerServletName, allow, settings.fileHandlerRoot, settings.onHandlerFile_filterUrlsWithHiddenChars),
                customHandler
        );
    }

    private static TS_SHttpHandlerAbstract createHandlerNativeCaller(TS_ThreadSyncTrigger killer, TGS_ValidatorType1<TS_SHttpHandlerRequest> allow, Settings settings) {
        return TS_SHttpHandlerString.of("/", allow, request -> {
            return switch (request.url.path.fileOrServletName) {
                case "native_supply" ->
                    createHandlerNativeCaller_doSupply(request);
                default ->
                    createHandlerNativeCaller_doCall(request, killer);
            };
        }, settings.onHandlerString_removeHiddenChars);
    }
    final static private TS_ThreadSyncLst<TGS_Tuple3<String, String, TGS_Time>> rows = new TS_ThreadSyncLst();

    private static TGS_Tuple2<TGS_FileTypes, String> createHandlerNativeCaller_doSupply(TS_SHttpHandlerRequest request) {
        if (!request.isLocal()) {
            return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, "ERROR: !request.isLocal() @ " + request.url);
        }
        var pRowHash = request.url.quary.getParameterByName("rowHash");
        if (pRowHash == null) {
            return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, "ERROR: pRowHash == null @ " + request.url);
        }
        var row = rows.findFirst(r -> Objects.equals(r.value0, pRowHash));
        if (row == null) {
            return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, "ERROR: row == null @ " + request.url);
        }
        return TGS_Tuple2.of(TGS_FileTypes.txt_utf8, row.value1);
    }

    private static TGS_Tuple2<TGS_FileTypes, String> createHandlerNativeCaller_doCall(TS_SHttpHandlerRequest request, TS_ThreadSyncTrigger killer) {
        var isWindows = TS_OsPlatformUtils.isWindows();
        d.ci("createHandlerNativeCaller_doCall", "hello");
        var pathExecutor = createHandlerNativeCaller_doCall_pathExecutor(isWindows, request);
        d.ci("createHandlerNativeCaller_doCall", "pathExecutor", pathExecutor);
        if (pathExecutor == null) {
            return null;
        }
        var row = TGS_Tuple3.of(
                TGS_RandomUtils.nextString(10, true, true, true, false, null),
                request.url.toString(), TGS_Time.of()
        );
        rows.add(row);
        var outExecution = createHandlerNativeCaller_doCall_sub(killer, maxExecutionDuration, request, pathExecutor, row.value0);
        rows.removeFirst(row);
        if (outExecution == null) {
            return null;
        }
        d.ci("createHandlerNativeCaller_doCall", "outExecution", outExecution);
        var type = TGS_FileTypes.txt_utf8;
        if (outExecution.startsWith("ERROR") || outExecution.startsWith("ERROR") || outExecution.startsWith("HATA") || outExecution.startsWith("hata")) {
            d.ce("createHandlerNativeCaller_doCall", outExecution);
            return TGS_Tuple2.of(type, outExecution);
        }
        var firstSpaceIndex = outExecution.indexOf(" ");
        if (firstSpaceIndex != -1) {
            var typeStr = outExecution.substring(0, firstSpaceIndex);
            var typeNew = TGS_FileTypes.findByContenTypePrefix(typeStr);
            if (typeNew == null) {
                d.ce("createHandlerNativeCaller_doCall", "typeNew != null", outExecution);
                return TGS_Tuple2.of(type, "ERROR: typeNew == null");
            }
            type = typeNew;
            outExecution = outExecution.substring(firstSpaceIndex + 1);
        }
        return TGS_Tuple2.of(type, outExecution);
    }

    private static String createHandlerNativeCaller_doCall_sub(TS_ThreadSyncTrigger killer, Duration maxExecutionDuration, TS_SHttpHandlerRequest request, Path pathExecutor, String rowHash) {
        var result = TS_ThreadAsyncAwait.callSingle(killer, maxExecutionDuration, kt -> {
            var cmdList = pathExecutor.toString().endsWith("jar")
                    ? List.of("java", "--enable-preview", "--add-modules", "jdk.incubator.vector", "-jar", pathExecutor.toString(), rowHash)
                    : List.of(pathExecutor.toString(), rowHash);
            return TS_OsProcess.of(cmdList);
        });
        if (result.hasError()) {
            request.sendError404("ERROR: execute", result.exceptionIfFailed.get().toString());
            return null;
        }
        var process = result.resultIfSuccessful.get();
        if (process.exception != null) {
            request.sendError404("ERROR: execute.process", "exitValue=" + process.exitValue + " | exception=" + process.exception);
            return null;
        }
        d.ci("createHandlerNativeCaller_doCall_sub", "process.exitValue", process.exitValue, "process.output", process.output);
        return process.output;
    }

    private static Path createHandlerNativeCaller_doCall_pathExecutor(boolean isWindows, TS_SHttpHandlerRequest request) {
        var servletName = request.url.path.fileOrServletName;
        var fileNameLabel = TGS_Coronator.ofStr()
                .anoint(val -> servletName)
                .anointIf(TGS_StringUtils::isNullOrEmpty, val -> "home")
                .anoint(val -> TGS_FileUtilsTur.toSafe(val))
                .coronate();
        var filePath = TGS_Coronator.of(Path.class).coronateAs(val -> {
            if (!isWindows) {
                var sh = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".sh");
                if (TS_FileUtils.isExistFile(sh)) {
                    d.ci("createHandlerNativeCaller_doCall_pathExecutor", "picked", sh);
                    return sh;
                }
                request.sendError404("ERROR: sh file not found", sh.toString());
                return null;
            }
            if (isWindows) {
                var bat = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".bat");
                if (TS_FileUtils.isExistFile(bat)) {
                    d.ci("createHandlerNativeCaller_doCall_pathExecutor", "picked", bat);
                    return bat;
                }
                var exe = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".exe");
                if (TS_FileUtils.isExistFile(exe)) {
                    d.ci("createHandlerNativeCaller_doCall_pathExecutor", "picked", exe);
                    return exe;
                }
            }
            var jar = TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".jar");
            if (TS_FileUtils.isExistFile(jar)) {
                d.ci("createHandlerNativeCaller_doCall_pathExecutor", "picked", jar);
                return jar;
            }
            request.sendError404("ERROR: bat or exe file not found", TS_PathUtils.getPathCurrent_nio(fileNameLabel + ".???").toString());
            return null;
        });
        return filePath;
    }

}
