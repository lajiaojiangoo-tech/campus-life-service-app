package cn.edu.gdpnc.jsjxy.mmt;

public class FoodItemModel {
    public final String id;
    public final String name;
    public final String desc;
    public final int price;
    public int count;

    public FoodItemModel(String id, String name, String desc, int price) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.price = price;
        this.count = 0;
    }

    // 兼容旧的无id构造方法
    public FoodItemModel(String name, String desc, int price) {
        this("", name, desc, price);
    }
}
