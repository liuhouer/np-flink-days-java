package cn.northpark.flink.day05;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class MapWithState {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(5000);

        env.setStateBackend(new FsStateBackend(args[0]));

        DataStreamSource<String> lines = env.socketTextStream("localhost", 8888);

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

        //将单词和一组合
        SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = words.map(new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String word) throws Exception {

                if("storm".equals(word)) {
                    System.out.println(1 / 0);
                }
                return Tuple2.of(word, 1);
            }
        });

        //为了保证程序出现问题可以继续累加，要记录分组聚合的中间结果
        KeyedStream<Tuple2<String, Integer>, Tuple> keyed = wordAndOne.keyBy(0);

        SingleOutputStreamOperator<Tuple2<String, Integer>> summed = keyed.map(new RichMapFunction<Tuple2<String, Integer>, Tuple2<String, Integer>>() {

            //transient
            private transient ValueState<Tuple2<String, Integer>> valueState;

            @Override
            public void open(Configuration parameters) throws Exception {

                //初始化状态或恢复历史状态
                //定义一个状态描述器
                ValueStateDescriptor<Tuple2<String, Integer>> descriptor = new ValueStateDescriptor<Tuple2<String, Integer>>(
                        "wc-keyed-state",
                        TypeInformation.of(new TypeHint<Tuple2<String, Integer>>() {})
                        //Types.TUPLE(Types.STRING, Types.INT)
                );
                valueState = getRuntimeContext().getState(descriptor);



            }

            @Override
            public Tuple2<String, Integer> map(Tuple2<String, Integer> value) throws Exception {

                String word = value.f0;
                Integer count = value.f1;

                Tuple2<String, Integer> historyKV = valueState.value();

                if (historyKV != null) {
                    historyKV.f1 += value.f1;
                    valueState.update(historyKV);
                    return historyKV;
                } else {
                    valueState.update(value);
                    return value;
                }
            }
        });

        summed.print();

        env.execute("MapWithState");
    }


}
