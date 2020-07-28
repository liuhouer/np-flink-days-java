package cn.northpark.flink.day05;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class MapWithStateV2 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(5000);

        env.setStateBackend(new FsStateBackend("file:///Users/star/Documents/dev/doit10/flink-java/chk002"));

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

            private transient ValueState<Integer> valueState;

            @Override
            public void open(Configuration parameters) throws Exception {

                //初始化状态或恢复历史状态
                //定义一个状态描述器
                ValueStateDescriptor<Integer> descriptor = new ValueStateDescriptor<Integer>(
                        "wc-keyed-state-v2",
                        //TypeInformation.of(new TypeHint<Integer>() {})
                        Types.INT
                );
                valueState = getRuntimeContext().getState(descriptor);



            }

            @Override
            public Tuple2<String, Integer> map(Tuple2<String, Integer> tp) throws Exception {
                //输入的K(单词)
                String word = tp.f0;
                //输入的V（次数）
                Integer count = tp.f1;

                //根据State获取历史数据
                Integer total = valueState.value();
                if(total == null) {
                    //跟新状态数据
                    valueState.update(count);
                    //返回结果
                    return Tuple2.of(word, count);
                } else {
                    //根据状态数据
                    total += count;
                    valueState.update(total);
                    //返回数据
                    return Tuple2.of(word, total);
                }
            }
        });

        summed.print();

        env.execute("MapWithState");
    }


}
