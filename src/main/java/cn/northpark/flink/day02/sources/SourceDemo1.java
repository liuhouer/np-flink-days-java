package cn.northpark.flink.day02.sources;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;

/**
 * 并行度为1的Source
 */
public class SourceDemo1 {

    public static void main(String[] args) throws Exception {


        //实时计算，创建一个实现的执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //创建抽象的数据集【创建原始的抽象数据集的方法：Source】
        DataStream<String> socketTextStream = env.socketTextStream("localhost", 8888);

        int parallelism2 = socketTextStream.getParallelism();

        System.out.println("++++++++++++++++++ > " + parallelism2);

        //将客户端的集合并行化成一个抽象的数据集，通常是用来做测试和实验
        //fromElements是一个有界的数据量，虽然是一个实时计算程序，但是数据处理完，程序就会推出
        //DataStream<Integer> nums = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9);

        //并行度为1的Source
        DataStreamSource<Integer> nums = env.fromCollection(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        //获取这个DataStream的并行度
        int parallelism = nums.getParallelism();

        System.out.println("==================> " + parallelism);


        DataStream<Integer> filtered = nums.filter(new FilterFunction<Integer>() {
            @Override
            public boolean filter(Integer value) throws Exception {
                return value % 2 == 0;
            }
        }).setParallelism(3);

        int parallelism1 = filtered.getParallelism();

        System.out.println("&&&&&&&&&&&&&&&&&&> " + parallelism1);


        filtered.print();

        env.execute("SourceDemo1");


    }
}
