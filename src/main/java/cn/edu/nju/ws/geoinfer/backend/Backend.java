package cn.edu.nju.ws.geoinfer.backend;

import cn.edu.nju.ws.geoinfer.seminaive.TablePointerRegistry;
import cn.edu.nju.ws.geoinfer.sql.SqlStorageEngine;
import cn.edu.nju.ws.geoinfer.utils.Initializer;
import cn.edu.nju.ws.geoinfer.utils.SimpleInferer;
import io.javalin.Context;
import io.javalin.Javalin;

import java.util.List;

public class Backend {
  public static final String PARAM_DB = "db";
  public static final String PARAM_PROGRAM = "program";

  private static synchronized void handleQuery(Context ctx) {
    String db = ctx.queryParam(PARAM_DB);
    String program = ctx.queryParam(PARAM_PROGRAM);

    SqlStorageEngine.getInstance()
        .initialize("jdbc:mysql://localhost:3306/?characterEncoding=utf8", "root", "dhf19700101");
    SqlStorageEngine.getInstance()
        .executeSql(
            String.format(
                "CREATE DATABASE IF NOT EXISTS `%s` character set UTF8 collate utf8_bin", db));
    SqlStorageEngine.getInstance().executeSql(String.format("USE `%s`", db));
    TablePointerRegistry.getInstance().initialize();
    LogCollector.getInstance().initialize();

    List<List<String>> result = SimpleInferer.infer(program, "query");
    ctx.json(new Response(LogCollector.getInstance().getLogs(), result));
  }

  public static void main(String args[]) {
    Initializer.registerBuiltins();

    Javalin app = Javalin.create().enableCorsForOrigin("*").start(7000);
    app.get("/", Backend::handleQuery);
  }
}
