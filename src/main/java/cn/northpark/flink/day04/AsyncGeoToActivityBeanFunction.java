package cn.northpark.flink.day04;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class AsyncGeoToActivityBeanFunction extends RichAsyncFunction<String, ActivityBean> {

    private transient CloseableHttpAsyncClient httpclient = null;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        //初始化异步的HttpClient
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000)
                .build();
        httpclient = HttpAsyncClients.custom()
                .setMaxConnTotal(20)
                .setDefaultRequestConfig(requestConfig).build();
        httpclient.start();

    }

    @Override
    public void asyncInvoke(String line, ResultFuture<ActivityBean> resultFuture) throws Exception {

        String[] fields = line.split(",");
        String uid = fields[0];
        String aid = fields[1];
        String time = fields[2];
        int eventType = Integer.parseInt(fields[3]);
        String longitude = fields[4];
        String latitude = fields[5];

        String url = "https://restapi.amap.com/v3/geocode/regeo?key=4924f7ef5c86a278f5500851541cdcff&location=" + longitude + "," + latitude;

        HttpGet httpGet = new HttpGet(url);

        Future<HttpResponse> future = httpclient.execute(httpGet, null);

        CompletableFuture.supplyAsync(new Supplier<String>() {

            @Override
            public String get() {
                try {
                    HttpResponse response = future.get();
                    String province = null;
                    if (response.getStatusLine().getStatusCode() == 200) {
                        //获取请求的json字符串
                        String result = EntityUtils.toString(response.getEntity());
                        //System.out.println(result);
                        //转成json对象
                        JSONObject jsonObj = JSON.parseObject(result);
                        //获取位置信息
                        JSONObject regeocode = jsonObj.getJSONObject("regeocode");
                        if (regeocode != null && !regeocode.isEmpty()) {
                            JSONObject address = regeocode.getJSONObject("addressComponent");
                            //获取省市区
                            province = address.getString("province");
                            //String city = address.getString("city");
                            //String businessAreas = address.getString("businessAreas");
                        }
                    }
                    return province;
                } catch (Exception e) {
                    return null;
                }
            }

        }).thenAccept((String province) -> {
            resultFuture.complete(Collections.singleton(ActivityBean.of(uid, aid, null, time, eventType, province)));
        });

    }

    @Override
    public void close() throws Exception {
        super.close();

        httpclient.close();
    }
}
