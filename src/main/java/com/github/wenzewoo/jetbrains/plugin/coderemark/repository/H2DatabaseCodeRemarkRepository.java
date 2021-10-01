//package com.github.wenzewoo.jetbrains.plugin.coderemark.repository;
//
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.db.Db;
//import cn.hutool.db.Entity;
//import cn.hutool.db.ds.pooled.DbConfig;
//import cn.hutool.db.ds.pooled.PooledDataSource;
//import com.github.wenzewoo.jetbrains.plugin.coderemark.Utils;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class H2DatabaseCodeRemarkRepository implements CodeRemarkRepository {
//    private static Db DB;
//    private final static String TABLE_NAME = "CODE_REMARKS";
//
//    public H2DatabaseCodeRemarkRepository() {
//    }
//
//    static {
//        try { // init datasource, tables
//
//            // jdbc:h2:mem:default
//            // jdbc:h2:~/.code-remark/repository
//            final DbConfig dbConfig = new DbConfig(
//                    "jdbc:h2:~/.code-remark/repository", "sa", "");
//            dbConfig.setMinIdle(1);
//            dbConfig.setMaxActive(2);
//            dbConfig.setMaxWait(20);
//            dbConfig.setInitialSize(1);
//            DB = Db.use(new PooledDataSource(dbConfig));
//
//
//            DB.execute("CREATE TABLE IF NOT EXISTS CODE_REMARKS\n" +
//                    "(\n" +
//                    "    ID          INT AUTO_INCREMENT,\n" +
//                    "    HASH_ID     VARCHAR(32)   NOT NULL,\n" +
//                    "    FILE_PATH   VARCHAR(1024) NOT NULL,\n" +
//                    "    LINE_NUMBER INT           NOT NULL,\n" +
//                    "    SUMMARY     VARCHAR(20)   NOT NULL,\n" +
//                    "    TEXT        VARCHAR(1024) NOT NULL,\n" +
//                    "    CONSTRAINT CODE_REMARKS_PK\n" +
//                    "        PRIMARY KEY (ID)\n" +
//                    ");\n" +
//                    "\n" +
//                    "CREATE INDEX IF NOT EXISTS CODE_REMARKS_HASH_ID_INDEX\n" +
//                    "    ON CODE_REMARKS (HASH_ID);\n" +
//                    "\n" +
//                    "CREATE UNIQUE INDEX IF NOT EXISTS CODE_REMARKS_HASH_ID_LINE_NUMBER_UINDEX\n" +
//                    "    ON CODE_REMARKS (HASH_ID, LINE_NUMBER);\n" +
//                    "\n" +
//                    "CREATE INDEX IF NOT EXISTS CODE_REMARKS_LINE_NUMBER_INDEX\n" +
//                    "    ON CODE_REMARKS (LINE_NUMBER);\n" +
//                    "\n");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @Override
//    public List<Integer> lines(String filePath) {
//        final List<Integer> lines = new ArrayList<>();
//        final String sql = "SELECT LINE_NUMBER FROM CODE_REMARKS WHERE HASH_ID = ?";
//        try (final Connection connection = DB.getConnection();
//             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
//            pstmt.setString(1, Utils.hashMD5(filePath));
//
//            final ResultSet resultSet = pstmt.executeQuery();
//            while (resultSet.next()) lines.add(resultSet.getInt("LINE_NUMBER"));
//        } catch (SQLException e) {
//            return lines;
//        }
//        return lines;
//    }
//
//    @Override
//    public Boolean exist(String filePath) {
//        try {
//            final Entity condition = Entity.create(TABLE_NAME)
//                    .set("HASH_ID", Utils.hashMD5(filePath));
//
//            return DB.count(condition) > 0;
//        } catch (SQLException e) {
//            return false;
//        }
//    }
//
//    @Override
//    public Boolean exist(String filePath, int lineNumber) {
//        try {
//            final Entity condition = Entity.create(TABLE_NAME)
//                    .set("LINE_NUMBER", lineNumber)
//                    .set("HASH_ID", Utils.hashMD5(filePath));
//
//            return DB.count(condition) > 0;
//        } catch (SQLException e) {
//            return false;
//        }
//    }
//
//
//    private String queryField(String sql, String fieldName) {
//        try (final Connection connection = DB.getConnection();
//             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
//
//            final ResultSet resultSet = pstmt.executeQuery();
//            if (resultSet.next()) return resultSet.getString(fieldName);
//        } catch (SQLException e) {
//            return null;
//        }
//        return null;
//    }
//
//    @Override
//    public String getSummary(String filePath, int lineNumber) {
//        return this.queryField("SELECT SUMMARY FROM CODE_REMARKS WHERE LINE_NUMBER = "
//                + lineNumber + " AND HASH_ID = '" + Utils.hashMD5(filePath) + "'", "SUMMARY");
//    }
//
//    @Override
//    public String getText(String filePath, int lineNumber) {
//        return this.queryField("SELECT TEXT FROM CODE_REMARKS WHERE LINE_NUMBER = "
//                + lineNumber + " AND HASH_ID = '" + Utils.hashMD5(filePath) + "'", "TEXT");
//    }
//
//    @Override
//    public void save(String filePath, int lineNumber, String text) {
//        try {
//            final Entity record = Entity.create(TABLE_NAME)
//                    .set("TEXT", text)
//                    .set("LINE_NUMBER", lineNumber)
//                    .set("FILE_PATH", filePath)
//                    .set("SUMMARY", StrUtil.maxLength(text, 17))
//                    .set("HASH_ID", Utils.hashMD5(filePath));
//
//            DB.insert(record);\
//        } catch (SQLException ignored) {
//        }
//    }
//
//    @Override
//    public void update(String filePath, int lineNumber, String text) {
//        try {
//            final Entity record = Entity.create()
//                    .set("TEXT", text)
//                    .set("SUMMARY", StrUtil.maxLength(text, 17));
//
//            final Entity condition = Entity.create(TABLE_NAME)
//                    .set("LINE_NUMBER", lineNumber)
//                    .set("HASH_ID", Utils.hashMD5(filePath));
//
//            DB.update(record, condition);
//        } catch (SQLException ignored) {
//        }
//    }
//
//    @Override
//    public void delete(String filePath, int lineNumber) {
//        try {
//            final Entity condition = Entity.create(TABLE_NAME)
//                    .set("LINE_NUMBER", lineNumber)
//                    .set("HASH_ID", Utils.hashMD5(filePath));
//
//            DB.del(condition);
//        } catch (SQLException ignored) {
//        }
//    }
//
//    @Override
//    public void delete(String filePath) {
//        try {
//            final Entity condition = Entity.create(TABLE_NAME)
//                    .set("HASH_ID", Utils.hashMD5(filePath));
//
//            DB.del(condition);
//        } catch (SQLException ignored) {
//        }
//    }
//}
