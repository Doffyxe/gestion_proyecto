package gestion_de_hoteles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;


public class InterfazStaff extends JDialog implements IFormulario {
    private final Color COLOR_CREMA = new Color(0xFFF8F0);
    private final Color COLOR_BRONCE = new Color(0xA97B50);
    
    private JTable tablaHabitaciones;
    private DefaultTableModel modelo;
    private VentanaPrincipal vPrincipal;
    private String rolActual;

    public InterfazStaff(VentanaPrincipal parent, String rol) {
        super(parent, "OPERACIONES DE CAMPO - " + rol, true);
        this.vPrincipal = parent;
        this.rolActual = rol.toUpperCase();
        
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_CREMA);

        cargarDatosIniciales(); 
    }

  
    @Override
    public void cargarDatosIniciales() {
        String[] columnas = {"ID", "N°", "Tipo", "Estado Actual"};
        modelo = new DefaultTableModel(columnas, 0);
        tablaHabitaciones = new JTable(modelo);
        tablaHabitaciones.setRowHeight(40);
        add(new JScrollPane(tablaHabitaciones), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panelAcciones.setBackground(COLOR_CREMA);

        JButton btnLimpiar = new JButton("🧹 MARCAR COMO LIMPIA");
        JButton btnReparar = new JButton("🛠 EN MANTENIMIENTO");
        JButton btnListo = new JButton("✅ LISTA / DISPONIBLE");

        styleButton(btnLimpiar, new Color(46, 125, 50));
        styleButton(btnReparar, new Color(198, 40, 40));
        styleButton(btnListo, COLOR_BRONCE);

    
        if (rolActual.equals("LIMPIEZA")) btnReparar.setEnabled(false);
        else if (rolActual.equals("MANTENIMIENTO")) btnLimpiar.setEnabled(false);

        panelAcciones.add(btnLimpiar);
        panelAcciones.add(btnReparar);
        panelAcciones.add(btnListo);
        add(panelAcciones, BorderLayout.SOUTH);

        btnLimpiar.addActionListener(e -> cambiarEstado("DISPONIBLE", "SUCIA"));
        btnReparar.addActionListener(e -> cambiarEstado("MANTENIMIENTO", "CUALQUIERA"));
        btnListo.addActionListener(e -> cambiarEstado("DISPONIBLE", "MANTENIMIENTO"));

        cargarDatos();
    }

    @Override
    public void limpiarCampos() {

        tablaHabitaciones.clearSelection();
    }

    private void cambiarEstado(String nuevoEstado, String estadoRequerido) {
        int fila = tablaHabitaciones.getSelectedRow();
        if (fila == -1) { 
            JOptionPane.showMessageDialog(this, "Selecciona una habitación de la lista."); 
            return; 
        }

        String id = modelo.getValueAt(fila, 0).toString();
        String numeroHab = modelo.getValueAt(fila, 1).toString();
        String estadoActual = modelo.getValueAt(fila, 3).toString();

       
        if (nuevoEstado.equals("DISPONIBLE") && !estadoActual.equals("SUCIA") && !estadoActual.equals("MANTENIMIENTO")) {
            JOptionPane.showMessageDialog(this, "La habitación " + numeroHab + " ya está operativa.");
            return;
        }

     
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement("UPDATE habitaciones SET estado = ? WHERE id = ?")) {
            pst.setString(1, nuevoEstado);
            pst.setString(2, id);
            pst.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Estado actualizado con éxito.");
            cargarDatos();
            vPrincipal.cargarDatosHabitaciones();
        } catch (SQLException e) { 
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        
        String sql = "SELECT id, numero, tipo, estado FROM habitaciones WHERE estado != 'OCUPADA' ORDER BY numero ASC";
        try (Connection con = Conexion.conectar();
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void styleButton(JButton btn, Color c) {
        btn.setBackground(c); 
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(250, 60)); 
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
    }
}