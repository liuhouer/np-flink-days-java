package cn.northpark.flink.day02.transformations;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FoldDemo {

    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //直接输入的就是单词
        DataStreamSource<String> words = env.socketTextStream("localhost", 8888);

        SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = words.map(w -> Tuple2.of(w, 1))
                .returns(Types.TUPLE(Types.STRING, Types.INT));

        //在java,认为元组是一个特殊的集合，脚标是从0开始
        KeyedStream<Tuple2<String, Integer>, Tuple> keyed = wordAndOne.keyBy(0);

//        SingleOutputStreamOperator<Integer> folded = keyed.fold(1000, new FoldFunction<Tuple2<String, Integer>, Integer>() {
//            @Override
//            public Integer fold(Integer accumulator, Tuple2<String, Integer> value) throws Exception {
//                return accumulator + value.f1;
//            }
//        });

        SingleOutputStreamOperator<String> result = keyed.fold("", new FoldFunction<Tuple2<String, Integer>, String>() {
            @Override
            public String fold(String accumulator, Tuple2<String, Integer> value) throws Exception {
                String word = "";
                int sum = 0;
                if (accumulator.equals("")) {
                    word = value.f0;
                    sum += value.f1;
                } else {
                    String[] fields = accumulator.split("-");
                    word = fields[0];
                    sum = Integer.parseInt(fields[1]) + value.f1;
                }

                return word + "-" + sum;
            }
        });

        result.print();


        env.execute("FoldDemo");
    }
}
