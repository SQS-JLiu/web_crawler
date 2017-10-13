package edu.ecnu.web.crawler.utils;

/**
 * 该类是用于连接数据库的工具类，
 * 并且在该类里面定义了一个进行数据库操作的方法execSql(String sql, Object...args)，执行数据库操作时调用该方法即可
 * 另外关闭数据库的操作也在这里定义
 * @author xxc
 *
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlUtils {
    // 定义数据连接变量
    private Connection connection;

    // 定义参数
    private PreparedStatement preparedStatement;

    private final String sql_driver = "com.mysql.jdbc.Driver";

    private final String sql_url = "jdbc:mysql://127.0.0.1/FDroidDB?useUnicode=true&connectionCollation=utf8mb4_unicode_ci";

    private final String sql_name = "mysql";

    private final String sql_password = "mysql";

    /**
     * 初始化类 在初始化时获取为Connection
     */
    public MySqlUtils() {
        try {
            Class.forName(sql_driver);
            connection = DriverManager.getConnection(sql_url, sql_name, sql_password);
        }
        catch (Exception e) {
            System.out.println("数据库连接异常," + e.toString());
        }
    }

    public ResultSet execSql(String sql) throws SQLException {
        Statement state = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        state.executeQuery(sql);
        try {
            return state.getResultSet();
        }
        catch (Exception e) {
            System.out.println("execSql() --> 连接数据库并执行sql异常: " + e.toString());
            return state.getResultSet();
        }
    }

    // 执行sql操作的方法
    public ResultSet execSql(String sql, Object... args) {
        try {
            preparedStatement = connection.prepareStatement(sql);
            // 为PreparedStatement对象设置SQL参数
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            // 执行
            preparedStatement.execute();
            return preparedStatement.getResultSet();
        }
        catch (SQLException e) {
            System.out.println("execSql() --> Executing sql exception: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭相应的资源
     */
    public void closeConnPs() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            }
            catch (SQLException e) {
                System.out.println("closeConnPs() --> Connection Closing exception: " + e.toString());
                e.printStackTrace();
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
                preparedStatement = null;
            }
            catch (SQLException e) {
                System.out.println("closeConnPs() --> preparedStatement Closing exception:" + e.toString());
                e.printStackTrace();
            }
        }
    }
}
