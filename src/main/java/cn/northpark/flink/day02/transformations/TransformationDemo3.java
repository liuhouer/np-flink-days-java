package cn.northpark.flink.day02.transformations;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;

/**
 * 对DataStream进行操作，返回一个新的DataStream
 */
public class TransformationDemo3 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Integer> nums = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9);

        SingleOutputStreamOperator<Integer> odd = nums.filter(new FilterFunction<Integer>() {
            @Override
            public boolean filter(Integer value) throws Exception {
                return value % 2 != 0;
            }
        });

        //lambda表达式
        //SingleOutputStreamOperator<Integer> filtered = nums.filter(i -> i >= 5);

        SingleOutputStreamOperator<Integer> filtered = nums.filter(i -> {
           return i >= 5;
        });


        filtered.print();

        env.execute("TransformationDemo3");

    }
}
