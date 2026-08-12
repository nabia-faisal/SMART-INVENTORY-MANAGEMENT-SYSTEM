package dao;

import java.sql.Connection;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class tables {

public static void main(String[] args) {
    // Try-with-resources automatically closes Connection and Statement
    try (Connection con = ConnectionProvider.getCon();
         Statement st = con.createStatement()) {

        // Create tables if they don't exist
        st.executeUpdate("CREATE TABLE IF NOT EXISTS appuser(" +
                "appuser_pk INT AUTO_INCREMENT PRIMARY KEY," +
                "userRole VARCHAR(50)," +
                "name VARCHAR(200)," +
                "mobileNumber VARCHAR(50)," +
                "email VARCHAR(200)," +
                "password VARCHAR(50)," +
                "address VARCHAR(200)," +
                "status VARCHAR(50))");

        st.executeUpdate("CREATE TABLE IF NOT EXISTS category(" +
                "category_pk INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(200))");

        st.executeUpdate("CREATE TABLE IF NOT EXISTS warehouse(" +
                "warehouse_pk INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(150)," +
                "city VARCHAR(100)," +
                "address VARCHAR(200))");

        st.executeUpdate("CREATE TABLE IF NOT EXISTS shelf(" +
                "shelf_pk INT AUTO_INCREMENT PRIMARY KEY," +
                "code VARCHAR(20)," +
                "warehouse_fk INT," +
                "FOREIGN KEY (warehouse_fk) REFERENCES warehouse(warehouse_pk) ON DELETE CASCADE)");

        st.executeUpdate("CREATE TABLE IF NOT EXISTS supplier(" +
                "supplier_pk INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(150)," +
                "contactPerson VARCHAR(100)," +
                "phone VARCHAR(50)," +
                "email VARCHAR(150)," +
                "address VARCHAR(200))");

        st.executeUpdate("CREATE TABLE IF NOT EXISTS supplier_warehouse(" +
                "supplier_fk INT," +
                "warehouse_fk INT," +
                "sinceDate DATE," +
                "PRIMARY KEY (supplier_fk, warehouse_fk)," +
                "FOREIGN KEY (supplier_fk) REFERENCES supplier(supplier_pk) ON DELETE CASCADE," +
                "FOREIGN KEY (warehouse_fk) REFERENCES warehouse(warehouse_pk) ON DELETE CASCADE)");

        st.executeUpdate("CREATE TABLE IF NOT EXISTS product(" +
                "product_pk INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(200)," +
                "quantity INT," +
                "price INT," +
                "description VARCHAR(500)," +
                "category_fk INT," +
                "shelf_fk INT," +
                "supplier_fk INT," +
                "FOREIGN KEY (shelf_fk) REFERENCES shelf(shelf_pk) ON DELETE SET NULL," +
                "FOREIGN KEY (supplier_fk) REFERENCES supplier(supplier_pk) ON DELETE SET NULL)");

        st.executeUpdate("CREATE TABLE IF NOT EXISTS customer(" +
                "customer_pk INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(200)," +
                "mobileNumber VARCHAR(50)," +
                "email VARCHAR(200))");

        st.executeUpdate("CREATE TABLE IF NOT EXISTS orderDetail(" +
                "order_pk INT AUTO_INCREMENT PRIMARY KEY," +
                "orderId VARCHAR(200)," +
                "customer_fk INT," +
                "orderDate VARCHAR(200)," +
                "totalPaid INT)");

        // Insert default SuperAdmin if not already exists
        st.executeUpdate("INSERT INTO appuser(userRole,name,mobileNumber,email,password,address,status) " +
                "SELECT 'SuperAdmin','SuperAdmin','12345','superadmin@testemail.com','admin','Pakistan','Active' " +
                "WHERE NOT EXISTS (SELECT 1 FROM appuser WHERE userRole='SuperAdmin')");

        JOptionPane.showMessageDialog(null, "Tables created and SuperAdmin inserted successfully!");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e);
    }
}

}
