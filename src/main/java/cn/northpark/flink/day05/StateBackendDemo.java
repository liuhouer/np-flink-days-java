package cn.northpark.flink.day05;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


//bin/flink run -m node-1.51doit.cn:8081 -c cn._51doit.flink.day05.StateBackendDemo -p 4 -s hdfs://node-1.51doit.cn:9000/checkdoit/bcf86ffb60ebbc23153ee5112d50a332/chk-76 /root/flink-java-1.0-SNAPSHOT.jar node-1.51doit.cn
public class StateBackendDemo {

    public static void main(String[] args) throws Exception{


        //System.setProperty("HADOOP_USER_NAME", "root");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //只有开启了checkpointing，才会有重启策略
        env.enableCheckpointing(5000);

        //设置重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 2000));

        //设置状态数据存储的后端
        //env.setStateBackend(new FsStateBackend("file:///Users/star/Documents/dev/doit10/flink-java/backend"));
        //env.setStateBackend(new FsStateBackend(args[0]));

        //程序异常退出或人为cancel掉，不删除checkpoint的数据
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        DataStreamSource<String> lines = env.socketTextStream(args[0], 8888);

        SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = lines.map(new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String word) throws Exception {

                if (word.startsWith("laoduan")) {
                    //int i = 1 / 0;
                    throw new RuntimeException("老段来了，程序挂了！！！");
                }
                return Tuple2.of(word, 1);
            }
        });

        SingleOutputStreamOperator<Tuple2<String, Integer>> result = wordAndOne.keyBy(0).sum(1);

        result.print();

        env.execute("RestartStrategiesDemo");
    }
}
