package cn.northpark.flink.day02.transformations;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 对DataStream进行操作，返回一个新的DataStream
 */
public class TransformationDemo1 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Integer> nums = env.fromElements(1, 2, 3, 4, 5);

        //map方法是一个Transformation，功能：做映射
        SingleOutputStreamOperator<Integer> res1 = nums.map(new MapFunction<Integer, Integer>() {
            @Override
            public Integer map(Integer value) throws Exception {
                return value * 2;
            }
        });

        SingleOutputStreamOperator<Integer> res2 = nums.map(i -> i * 2).returns(Types.INT);//.returns(Integer.class);


        //传入功能更加强大的RichMapFunction
        nums.map(new RichMapFunction<Integer, Integer>() {

            //open，在构造方法之后，map方法执行之前，执行一次，Configuration可以拿到全局配置
            //用来出事化一下连接，或者初始化或恢复state
            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
            }

            //销毁之前，执行一次，通常是做资源释放
            @Override
            public void close() throws Exception {
                super.close();
            }

            @Override
            public Integer map(Integer value) throws Exception {
                return value * 10;
            }

            //close
        });


        //Sink
        res2.print();

        env.execute("TransformationDemo1");

    }
}
