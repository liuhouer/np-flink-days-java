package cn.northpark.flink.day04;

import cn.northpark.flink.utils.FlinkUtilsV1;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

public class QueryActivityLocation {

    public static void main(String[] args) throws Exception {

      DataStream<String> lines =  FlinkUtilsV1.createKafkaStream(args, new SimpleStringSchema());

        SingleOutputStreamOperator<ActivityBean> beans = lines.map(new GeoToActivityBeanFunction());

        beans.print();

        FlinkUtilsV1.getEnv().execute("QueryActivityName");

    }
}
