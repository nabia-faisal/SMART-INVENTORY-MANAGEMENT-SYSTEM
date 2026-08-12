package dao;

public class TestConnection {
    public static void main(String[] args) {
        var con = ConnectionProvider.getCon();
        if (con != null)
            System.out.println("Connection successful!");
        else
            System.out.println("Connection failed!");
    }
}
