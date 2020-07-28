package cn.northpark.flink.day02.sources;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.NumberSequenceIterator;

import java.util.Arrays;

/**
 * 可以并行的Source，即并行度大于1的Source
 */
public class SourceDemo2 {

    public static void main(String[] args) throws Exception {


        //实时计算，创建一个实现的执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //DataStreamSource<Long> nums = env.fromParallelCollection(new NumberSequenceIterator(1, 10), Long.class);

        //DataStreamSource<Long> nums = env.fromParallelCollection(new NumberSequenceIterator(1, 10), TypeInformation.of(Long.TYPE));

        DataStreamSource<Long> nums = env.generateSequence(1, 100);

        int parallelism = nums.getParallelism();

        System.out.println("+++++++++++++++++> " + parallelism);

        DataStream<Long> filtered = nums.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long value) throws Exception {
                return value % 2 == 0;
            }
        }).setParallelism(3);

        int parallelism1 = filtered.getParallelism();

        System.out.println("&&&&&&&&&&&&&&&&&&> " + parallelism1);


        filtered.print();

        env.execute("SourceDemo2");


    }
}
