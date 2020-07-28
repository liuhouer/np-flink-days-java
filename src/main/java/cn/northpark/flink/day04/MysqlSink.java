package cn.northpark.flink.day04;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MysqlSink extends RichSinkFunction<ActivityBean> {

    private transient Connection connection = null;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        //创建MySQL连接
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bigdata?characterEncoding=UTF-8", "root", "123456");

    }

    @Override
    public void invoke(ActivityBean bean, Context context) throws Exception {

        PreparedStatement pstm = null;
        try {
            pstm = connection.prepareStatement(
                    "INSERT INTO t_activity_counts (aid, event_type, counts) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE counts = ?");

            pstm.setString(1, bean.aid);
            pstm.setInt(2, bean.eventType);
            pstm.setInt(3, bean.count);
            pstm.setInt(4, bean.count);

            pstm.executeUpdate();
        } finally {
            if(pstm != null) {
                pstm.close();
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        //关闭连接
        connection.close();
    }
}
