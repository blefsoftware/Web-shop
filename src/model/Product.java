package model;

public class Product {
    private static int ID = 0;
    private int pid;
    private String name;
    private double price;
    private String info;
    private Category category;

    public Product(String name, double price, String info, Category category) {
        this.pid = ++ID;
        this.name = name;
        this.price = price;
        this.info = info;
        this.category = category;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public static void setID(int ID) {
        Product.ID = ID;
    }

    @Override
    public String toString() {
        //return "[" + getPid() + ": " + getName() + ", " + getPrice() + ", " + getInfo() + ", (" + getCategory().getName() + ": " + getCategory().getCid() + ")]";
        return "[" + getName() + ", €" + getPrice() + "]";
    }
}
