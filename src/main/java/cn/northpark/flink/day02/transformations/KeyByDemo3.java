package cn.northpark.flink.day02.transformations;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 按照多个字段进行分组
 */
public class KeyByDemo3 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //辽宁,沈阳,1000
        //山东,青岛,2000
        //山东,青岛,2000
        //山东,烟台,1000
        DataStreamSource<String> lines = env.socketTextStream("localhost", 8888);

        //        SingleOutputStreamOperator<Tuple3<String, String, Double>> provinceCityAndMoney = lines.map(new MapFunction<String, Tuple3<String, String, Double>>() {
        //            @Override
        //            public Tuple3<String, String, Double> map(String line) throws Exception {
        //                String[] fields = line.split(",");
        //                String province = fields[0];
        //                String city = fields[1];
        //                double money = Double.parseDouble(fields[2]);
        //                return Tuple3.of(province, city, money);
        //            }
        //        });

        // 元组keyBy可以用角标
        // KeyedStream<Tuple3<String, String, Double>, Tuple> keyed = provinceCityAndMoney.keyBy(0, 1);

        //SingleOutputStreamOperator<Tuple3<String, String, Double>> summed = keyed.sum(2);

        SingleOutputStreamOperator<OrderBean> provinceCityAndMoney = lines.map(new MapFunction<String, OrderBean>() {

            @Override
            public OrderBean map(String line) throws Exception {
                String[] fields = line.split(",");
                String province = fields[0];
                String city = fields[1];
                double money = Double.parseDouble(fields[2]);
                return OrderBean.of(province, city, money);
            }
        });
        KeyedStream<OrderBean, Tuple> keyed = provinceCityAndMoney.keyBy("province", "city");

        SingleOutputStreamOperator<OrderBean> summed = keyed.sum("money");

        summed.print();

        env.execute("KeyByDemo3");
    }
}
