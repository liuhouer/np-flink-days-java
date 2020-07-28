package cn.northpark.flink.day06;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class OperatorStateDemo {

    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(2);

        env.enableCheckpointing(5000);

        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(2, 2000));

        env.setStateBackend(new FsStateBackend("file:////Users/star/Documents/dev/doit10/flink-java/chk003"));

        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        //控制在什么时候出现异常
        DataStreamSource<String> lines = env.socketTextStream("localhost", 8888);

        lines.map(new MapFunction<String, String>() {
            @Override
            public String map(String line) throws Exception {
                if(line.startsWith("doit")) {
                    System.out.println(1 / 0);
                }
                return line;
            }
        }).print();

        DataStreamSource<Tuple2<String, String>> tp = env.addSource(new MyExactlyOnceParFileSource("/Users/star/Desktop/data"));

        tp.print();

        env.execute("OperatorStateDemo");

    }
}
