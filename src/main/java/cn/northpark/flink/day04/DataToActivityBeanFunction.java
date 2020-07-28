package cn.northpark.flink.day04;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DataToActivityBeanFunction extends RichMapFunction<String, ActivityBean> {


    private transient Connection connection = null;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        //创建MySQL连接
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bigdata?characterEncoding=UTF-8", "root", "123456");

    }

    @Override
    public ActivityBean map(String line) throws Exception {
        String[] fields = line.split(",");

        String uid = fields[0];
        String aid = fields[1];

        //根据aid作为查询条件查询出name
        PreparedStatement prepareStatement = connection.prepareStatement("SELECT name FROM t_activities WHERE id = ?");

        prepareStatement.setString(1, aid);

        ResultSet resultSet = prepareStatement.executeQuery();

        String name = null;

        while (resultSet.next()) {
            name = resultSet.getString(1);
        }

        prepareStatement.close();

        String time = fields[2];
        int eventType = Integer.parseInt(fields[3]);

        String province = fields[4];

        return ActivityBean.of(uid, aid, name, time, eventType, province);
    }

    @Override
    public void close() throws Exception {
        super.close();
        connection.close();
    }
}
