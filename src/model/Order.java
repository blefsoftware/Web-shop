package model;

import java.util.ArrayList;

public class Order {
    private static int ID = 0;
    private int oid;
    private User user;
    private int quantity;
    private ArrayList<Product> products;
    private double cost;
    private boolean paid;

    public Order(User user, ArrayList<Product> products) {
        this.oid = ++ID;
        this.user = user;
        this.products = products;
        this.paid = false;
        this.setQuantity(products.size());
        this.setCost(products);
    }

    public int getOid() {
        return oid;
    }

    public void setOid(int oid) {
        this.oid = oid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(ArrayList<Product> products) {
        for (Product product : products) {
            this.cost += product.getPrice();
        }
    }

    public static void setID(int ID) {
        Order.ID = ID;
    }

    @Override
    public String toString() {
        StringBuilder orderString = new StringBuilder("Order: " + getOid() + ". - " + getUser().getUsername() + " - quantity: " + getQuantity() + " {");
        for (Product product : getProducts()) {
            orderString.append(product).append(";");
        }
        orderString.append("}, cost: ").append(getCost()).append(", paid: ").append(isPaid());
        return orderString.toString();
    }
}
