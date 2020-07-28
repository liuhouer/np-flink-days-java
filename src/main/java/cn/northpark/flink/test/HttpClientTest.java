package cn.northpark.flink.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientTest {

    public static void main(String[] args) throws Exception {

        double longitude = 116.311805;
        double latitude = 40.028572;

        String url = "https://restapi.amap.com/v3/geocode/regeo?key=4924f7ef5c86a278f5500851541cdcff&location=" + longitude + "," + latitude;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);

        try {
            int status = response.getStatusLine().getStatusCode();
            String province = null;

            if (status == 200) {
                //获取请求的json字符串
                String result = EntityUtils.toString(response.getEntity());

                System.out.println(result);
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

            System.out.println(province);

        } finally {
            response.close();
        }

    }
}
