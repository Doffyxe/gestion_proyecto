package gestion_de_hoteles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;


public class Conexion {
    private static final String URL = "jdbc:mysql://localhost:3306/emj_logistic_hotel";
    private static final String USER = "root"; 
    private static final String PASS = "fernanfloo@123"; 

    private static Connection con = null;

    public static Connection conectar() {
        try {
           
            if (con == null || con.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("✅ Conexión exitosa a MySQL - EMJ LOGISTIC");
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error: Driver no encontrado. Verifique el Build Path.", "Error de Librería", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de MySQL: " + e.getMessage(), "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
        }
        return con;
    }
}