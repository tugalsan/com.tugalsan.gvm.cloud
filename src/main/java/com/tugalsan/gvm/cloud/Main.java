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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

public class Main {

    final private static TS_Log d = TS_Log.of(true, Main.class);

    //HOW TO EXECUTE
    //WHEN RUNNING IN NETBEANS, ALL DEPENDENCIES SHOULD HAVE TARGET FOLDER!
    //cd D:\git\gvm\com.tugalsan.gvm.cloud
    //java --enable-preview --add-modules jdk.incubator.vector -jar target/com.tugalsan.gvm.cloud-1.0-SNAPSHOT-jar-with-dependencies.jar    
    public static void main(String[] args) {
        var killer = TS_ThreadSyncTrigger.of();//not used for now
        var settings = Settings.of(Settings.pathDefault());
        TGS_ValidatorType1<TS_SHttpHandlerRequest> allow = request -> isAllowed(request);
        var handlerExecutor = createHandlerExecutor(killer, allow);//settings may be used here in the future
        startHttps(settings, allow, handlerExecutor);
    }

    @Deprecated //TODO ALLOW LOGIC
    private static boolean isAllowed(TS_SHttpHandlerRequest request) {
        if (!request.isLocal()) {
            request.sendError404("ERROR: Will work only localhost 😠");
            return false;
        }
        return true;
    }

    @Deprecated //TODO DB OPS
    private static TGS_Tuple2<TGS_FileTypes, String> createReply(long rowId, String outExecution) {
//        try {
//            Class.forName("org.hsqldb.jdbc.JDBCDriver");
//            try (var con = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "")) {
//                try (var stmt = con.createStatement()) {
//                    stmt.execute("TODO");
                    var outReply = outExecution + " and data of rowId";
                    var type = "txt";
                    return TGS_Tuple2.of(TGS_FileTypes.findByContenTypePrefix(type), outReply);
//                }
//            }
//        } catch (ClassNotFoundException | SQLException e) {
//            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
//            e.printStackTrace();
//            return 0L;
//        }
    }

    @Deprecated //TODO DB OPS
    private static Long pushUrl2DB_and_FetchRowId(TS_SHttpHandlerRequest request) {
//        try {
//            Class.forName("org.hsqldb.jdbc.JDBCDriver");
//            try (var con = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "")) {
//                try (var stmt = con.createStatement()) {
//                    stmt.execute("CREATE TABLE IF NOT EXISTS mytable (`col` VARCHAR(16) NOT NULL)");
                    return 0L; //TODO push request.url.toString() to db, fetch row id
//                }
//            }
//        } catch (ClassNotFoundException | SQLException e) {
//            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
//            e.printStackTrace();
//            return 0L;
//        }
    }

    private static TS_SHttpHandlerAbstract createHandlerExecutor(TS_ThreadSyncTrigger killer, TGS_ValidatorType1<TS_SHttpHandlerRequest> allow) {
        var maxExecutionDuration = Duration.ofMinutes(10);
        var isWindows = TS_OsPlatformUtils.isWindows();
        return TS_SHttpHandlerString.of("/", allow, request -> {
            var pathExecutor = chooseExecutor(isWindows, request);
            if (pathExecutor == null) {
                return null;
            }
            var rowId = pushUrl2DB_and_FetchRowId(request);
            if (rowId == null) {
                return null;
            }
            var outExecution = execute(killer, maxExecutionDuration, request, pathExecutor, rowId);
            if (outExecution == null) {
                return null;
            }
            return createReply(rowId, outExecution);
        });
    }

    private static void startHttps(Settings settings, TGS_ValidatorType1<TS_SHttpHandlerRequest> allow, TS_SHttpHandlerAbstract... customHandler) {
        TS_SHttpServer.of(
                TS_SHttpConfigNetwork.of(settings.ip, settings.sslPort),
                TS_SHttpConfigSSL.of(settings.sslPath, settings.sslPass, settings.redirectToSSL),
                TS_SHttpConfigHandlerFile.of(settings.fileHandlerServletName, allow, settings.fileHandlerRoot),
                customHandler
        );
    }

    private static String execute(TS_ThreadSyncTrigger killer, Duration maxExecutionDuration, TS_SHttpHandlerRequest request, Path pathExecutor, long rowId) {
        var result = TS_ThreadAsyncAwait.callSingle(killer, maxExecutionDuration, kt -> {
            return TS_OsProcess.of(List.of(pathExecutor.toString(), String.valueOf(rowId)));
        });
        if (result.hasError()) {
            request.sendError404("ERROR: execute", result.exceptionIfFailed.get());
            return null;
        }
        var process = result.resultIfSuccessful.get();
        if (!process.exitValueOk()) {
            request.sendError404("ERROR: execute.process", process.exception.toString());
            return null;
        }
        d.ci("main", "result.resultIfSuccessful", process.output);
        return process.output;
    }

    private static Path chooseExecutor(boolean isWindows, TS_SHttpHandlerRequest request) {
        var servletName = request.url.path.fileOrServletName;
        var fileNameLabel = TGS_Coronator.ofStr()
                .anoint(val -> TGS_FileUtilsTur.toSafe(servletName))
                .anointIf(TGS_StringUtils::isNullOrEmpty, val -> "home")
                .coronate();
        var filePath = TGS_Coronator.of(Path.class).coronateAs(val -> {
            if (!isWindows) {
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
        return filePath;
    }

}
