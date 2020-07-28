package cn.northpark.flink.day02.transformations;

public class OrderBean {

    public String province;

    public String city;

    public Double money;

    public OrderBean() {}

    public OrderBean(String province, String city, Double money) {
        this.province = province;
        this.city = city;
        this.money = money;
    }

    public static OrderBean of(String province, String city, Double money) {
        return new OrderBean(province, city, money);
    }

    @Override
    public String toString() {
        return "OrderBean{" + "province='" + province + '\'' + ", city='" + city + '\'' + ", money=" + money + '}';
    }
}
