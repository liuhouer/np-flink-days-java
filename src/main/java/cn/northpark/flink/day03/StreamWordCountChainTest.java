package cn.northpark.flink.day03;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class StreamWordCountChainTest {

    public static void main(String[] args) throws Exception {

        //创建一个flink stream 程序的执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //使用StreamExecutionEnvironment创建DataStream
        //Source
        DataStream<String> lines = env.socketTextStream(args[0], Integer.parseInt(args[1]));

        //Transformation开始

        //调用DataStream上的方法Transformation（s）
        SingleOutputStreamOperator<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String line, Collector<String> out) throws Exception {
                //切分
                String[] words = line.split(" ");
                for (String word : words) {
                    //输出
                    out.collect(word);
                }
            }
        });

        SingleOutputStreamOperator<String> filtered = words.filter(new FilterFunction<String>() {
            @Override
            public boolean filter(String value) throws Exception {
                return value.startsWith("h");
            }
        });
        //.disableChaining(); //将这个算子单独划分处理，生成一个Task，跟其他的算子不再有Operator Chain
        //.startNewChain(); //从该算子开始，开启一个新的链，从这个算子之前，发生redistributing

        //将单词和一组合
        SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = filtered.map(new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String word) throws Exception {
                return Tuple2.of(word, 1);
            }
        });


        SingleOutputStreamOperator<Tuple2<String, Integer>> summed = wordAndOne.keyBy(0).sum(1);

        //Transformation结束

        // 调用Sink (Sink必须调用)
        summed.print();

        //启动
        env.execute("StreamWordCount");

    }
}
