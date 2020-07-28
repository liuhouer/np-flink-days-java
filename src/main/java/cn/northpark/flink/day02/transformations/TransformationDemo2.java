package cn.northpark.flink.day02.transformations;

import jdk.nashorn.internal.codegen.types.Type;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;

/**
 * 对DataStream进行操作，返回一个新的DataStream
 */
public class TransformationDemo2 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> lines = env.fromElements("spark flink hadoop", "spark flink hadoop hbase");

//        SingleOutputStreamOperator<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
//            @Override
//            public void flatMap(String line, Collector<String> out) throws Exception {
//
//                //                String[] words = line.split(" ");
//                //                for (String word : words) {
//                //                    out.collect(word);
//                //                }
//                //Arrays.asList(line.split(" ")).forEach(w -> out.collect(w));
//                Arrays.stream(line.split(" ")).forEach(out::collect);
//            }
//        });

        SingleOutputStreamOperator<String> words2 = lines.flatMap((String line,Collector<String> out) ->
                Arrays.stream(line.split(" ")).forEach(out::collect)).returns(Types.STRING);


        //flatMap方法还可以传入RichFlatMapFunction

        words2.print();

        env.execute("TransformationDemo2");

    }
}
