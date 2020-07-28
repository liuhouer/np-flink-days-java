package cn.northpark.flink.day02.transformations;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class KeyByDemo2 {

    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //直接输入的就是单词
        DataStreamSource<String> words = env.socketTextStream("localhost", 8888);

        SingleOutputStreamOperator<WordCounts> wordAndOne = words.map(new MapFunction<String, WordCounts>() {

            @Override
            public WordCounts map(String value) throws Exception {
                return WordCounts.of(value, 1L);
            }
        });

        KeyedStream<WordCounts, Tuple> keyed = wordAndOne.keyBy("word");

        //聚合
        SingleOutputStreamOperator<WordCounts> summed = keyed.sum("counts");

        summed.print();

        env.execute("KeyByDemo1");
    }
}
