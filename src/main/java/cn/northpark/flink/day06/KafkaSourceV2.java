package cn.northpark.flink.day06;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

public class KafkaSourceV2 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //开启Chackpointing，同时开启重启策略
        env.enableCheckpointing(5000);
        //设置StateBackend
        env.setStateBackend(new FsStateBackend("file:///Users/star/Documents/dev/doit10/flink-java/chk004"));
        //取消任务checkpoint不擅长
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //设置checkpoint的模式
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        Properties props = new Properties();
        //指定Kafka的Broker地址
        props.setProperty("bootstrap.servers", "node-1.51doit.cn:9092,node-2.51doit.cn:9092,node-3.51doit.cn:9092");
        //指定组ID
        props.setProperty("group.id", "mytest");
        //如果没有记录偏移量，第一次从最开始消费
        props.setProperty("auto.offset.reset", "earliest");
        //kafka的消费者不自动提交偏移量
        props.setProperty("enable.auto.commit", "false");

        //KafkaSource
        FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>("mytest10",
                new SimpleStringSchema(),
                props);

        //Flink CheckPoint成功后还要向Kafka特殊的topic中写入偏移量
        kafkaSource.setCommitOffsetsOnCheckpoints(false);


        DataStreamSource<String> words = env.socketTextStream("localhost", 8888);
        words.map(new MapFunction<String, String>() {
            @Override
            public String map(String value) throws Exception {
                if(value.startsWith("doit")) {
                    System.out.println(1 / 0);
                }
                return value;
            }
        }).print();

        //Source
        DataStream<String> lines = env.addSource(kafkaSource);


        //Sink
        lines.print();

        env.execute("KafkaSource");

    }
}
