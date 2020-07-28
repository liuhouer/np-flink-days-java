package cn.northpark.flink.day01;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class BatchWordCount {

    public static void main(String[] args) throws Exception {

        //离线批处理使用的执行换行是ExecutionEnvironment，少了Stream
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //使用ExecutionEnvironment创建DataSet
        DataSet<String> lines = env.readTextFile(args[0]);

        //切分压平
        FlatMapOperator<String, Tuple2<String, Integer>> wordAndOne = lines.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String line, Collector<Tuple2<String, Integer>> out) throws Exception {
                String[] words = line.split(" ");
                for (String word : words) {
                    out.collect(Tuple2.of(word, 1));
                }
            }
        });

        //离线计算实现的是分组聚合，调用的是groupBy
        AggregateOperator<Tuple2<String, Integer>> summed = wordAndOne.groupBy(0).sum(1);

        //将结果保存到HDFS
        summed.writeAsText(args[1]).setParallelism(2);

        env.execute("BatchWordCount");
    }
}
