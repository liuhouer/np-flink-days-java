package cn.northpark.flink.day07;

import cn.northpark.flink.utils.DruidConnectionPool;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.base.VoidSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlTwoPhaseCommitSink extends TwoPhaseCommitSinkFunction<Tuple2<String, Integer>, MySqlTwoPhaseCommitSink.ConnectionState, Void> {


    public MySqlTwoPhaseCommitSink() {
        super(new KryoSerializer<>(MySqlTwoPhaseCommitSink.ConnectionState.class, new ExecutionConfig()), VoidSerializer.INSTANCE);
    }

    @Override
    protected MySqlTwoPhaseCommitSink.ConnectionState beginTransaction() throws Exception {

        System.out.println("=====> beginTransaction... ");
        //Class.forName("com.mysql.jdbc.Driver");
        //Connection conn = DriverManager.getConnection("jdbc:mysql://172.16.200.101:3306/bigdata?characterEncoding=UTF-8", "root", "123456");
        Connection connection = DruidConnectionPool.getConnection();
        connection.setAutoCommit(false);
        return new ConnectionState(connection);

    }


    @Override
    protected void invoke(MySqlTwoPhaseCommitSink.ConnectionState connectionState, Tuple2<String, Integer> value, Context context) throws Exception {
        Connection connection = connectionState.connection;
        PreparedStatement pstm = connection.prepareStatement("INSERT INTO t_wordcount (word, counts) VALUES (?, ?) ON DUPLICATE KEY UPDATE counts = ?");
        pstm.setString(1, value.f0);
        pstm.setInt(2, value.f1);
        pstm.setInt(3, value.f1);
        pstm.executeUpdate();
    }



    @Override
    protected void preCommit(MySqlTwoPhaseCommitSink.ConnectionState connectionState) throws Exception {
        System.out.println("=====> preCommit... " + connectionState);

    }

    @Override
    protected void commit(MySqlTwoPhaseCommitSink.ConnectionState connectionState) {
        System.out.println("=====> commit... ");
        Connection connection = connectionState.connection;
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("提交事物异常");
        }
    }

    @Override
    protected void abort(MySqlTwoPhaseCommitSink.ConnectionState connectionState) {
        System.out.println("=====> abort... ");
        Connection connection = connectionState.connection;
        try {
            connection.rollback();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("回滚事物异常");
        }
    }

    static class ConnectionState {

        private final transient Connection connection;

        ConnectionState(Connection connection) {
            this.connection = connection;
        }

    }


}