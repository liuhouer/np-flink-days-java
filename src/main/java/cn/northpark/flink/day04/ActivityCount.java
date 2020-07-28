package cn.northpark.flink.day04;

import cn.northpark.flink.utils.FlinkUtilsV1;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

import java.util.concurrent.TimeUnit;

public class ActivityCount {

    public static void main(String[] args) throws Exception {

        //创建Env和添加Source
        DataStream<String> lines = FlinkUtilsV1.createKafkaStream(args, new SimpleStringSchema());

        //Transformation
        SingleOutputStreamOperator<ActivityBean> beans = AsyncDataStream.unorderedWait(lines, new AsyncGeoToActivityBeanFunction(), 0,
                TimeUnit.MILLISECONDS, 10
        );
        //Transformation
        //SingleOutputStreamOperator<ActivityBean> summed1 = beans.keyBy("aid", "eventType").sum("count");
        SingleOutputStreamOperator<ActivityBean> summed2 = beans.keyBy("aid", "eventType", "province").sum("count");

        //调用Sink
        //summed1.addSink(new MysqlSink());

        //创建一个Redis Conf

        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("node-2.51doit.cn").setPassword("123456").setDatabase(3).build();

        summed2.addSink(new RedisSink<ActivityBean>(conf, new RedisActivityBeanMapper()));

        //执行
        FlinkUtilsV1.getEnv().execute("ActivityCount");

    }

    public static class RedisActivityBeanMapper implements RedisMapper<ActivityBean> {

        //调用Redis的写入方法
        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.HSET, "ACT_COUNT");
        }

        //写入Redis中key
        @Override
        public String getKeyFromData(ActivityBean data) {
            return data.aid + "_" + data.eventType + "_" + data.province ;
        }

        //Redis中的Value
        @Override
        public String getValueFromData(ActivityBean data) {
            return String.valueOf(data.count);
        }
    }

}
