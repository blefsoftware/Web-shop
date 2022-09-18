package model;

public class User {
    private static int ID = 0;
    private int uid;
    private String username;
    private String password;
    private String status;

    public User(String username, String password, String status) {
        this.uid = ++ID;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static void setID(int ID) {
        User.ID = ID;
    }

    @Override
    public String toString() {
        return getUid() + ": " + getUsername() + " - " + getStatus();
    }
}
