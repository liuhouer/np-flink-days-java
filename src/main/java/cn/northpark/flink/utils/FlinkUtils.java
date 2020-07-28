package cn.northpark.flink.utils;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FlinkUtils {


    private static StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    public static <T> DataStream<T> createKafkaStream(ParameterTool parameters, Class<? extends DeserializationSchema<T>> clazz) throws Exception{

        //设置全局的参数
        env.getConfig().setGlobalJobParameters(parameters);

        //开启Chackpointing，同时开启重启策略
        env.enableCheckpointing(parameters.getLong("checkpoint.interval", 5000L), CheckpointingMode.EXACTLY_ONCE);

        //取消任务checkpoint不擅长
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        Properties props = new Properties();
        //指定Kafka的Broker地址
        props.setProperty("bootstrap.servers", parameters.getRequired("bootstrap.servers"));
        //指定组ID
        props.setProperty("group.id", parameters.getRequired("group.id"));
        //如果没有记录偏移量，第一次从最开始消费
        props.setProperty("auto.offset.reset", parameters.get("auto.offset.reset", "earliest"));
        //kafka的消费者不自动提交偏移量
        props.setProperty("enable.auto.commit", parameters.get("enable.auto.commit", "false"));

        String topics = parameters.getRequired("topics");

        List<String> topicList = Arrays.asList(topics.split(","));

        //KafkaSource
        FlinkKafkaConsumer<T> kafkaConsumer = new FlinkKafkaConsumer<T>(
                topicList,
                clazz.newInstance(),
                props);

        //kafkaConsumer.setCommitOffsetsOnCheckpoints(true);

        return env.addSource(kafkaConsumer);

    }

    public static StreamExecutionEnvironment getEnv() {
        return env;
    }
}
