package cn.northpark.flink.day04;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class GeoToActivityBeanFunction extends RichMapFunction<String, ActivityBean> {

    private CloseableHttpClient httpclient = null;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        httpclient = HttpClients.createDefault();
    }

    @Override
    public ActivityBean map(String line) throws Exception {

        String[] fields = line.split(",");

        String uid = fields[0];
        String aid = fields[1];

        String time = fields[2];
        int eventType = Integer.parseInt(fields[3]);
        String longitude = fields[4];
        String latitude = fields[5];

        String url = "https://restapi.amap.com/v3/geocode/regeo?key=4924f7ef5c86a278f5500851541cdcff&location=" + longitude + "," + latitude;

        String province = null;

        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);

        try {
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
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
        } finally {
            response.close();
        }

        return ActivityBean.of(uid, aid, "null", time, eventType, Double.parseDouble(longitude), Double.parseDouble(latitude), province);
    }

    @Override
    public void close() throws Exception {
        super.close();
        httpclient.close();
    }
}
