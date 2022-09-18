package model;

public class Category {
    private static int ID = 0;
    private int cid;
    private String name;

    public Category(String name) {
        this.cid = ++ID;
        this.name = name;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void setID(int ID) {
        Category.ID = ID;
    }

    @Override
    public String toString() {
        return getCid() + ". " + getName();
    }
}
