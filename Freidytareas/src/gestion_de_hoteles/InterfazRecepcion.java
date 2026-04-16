package gestion_de_hoteles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class InterfazRecepcion extends JDialog implements IFormulario {
    private final Color COLOR_CREMA = new Color(0xFFF8F0);
    private final Color COLOR_BRONCE = new Color(0xA97B50);
    private final Color COLOR_CAFE_OSCURO = new Color(0x563A2D);
    private final Color COLOR_BLANCO = Color.WHITE;

    private VentanaPrincipal vPrincipal;
    private DefaultTableModel modeloHuespedes;
    private DefaultTableModel modeloBusquedaClientes;
    
  
    private JTextField txtNom, txtCed, txtTel, txtIdCli, txtIdHab;

    public InterfazRecepcion(VentanaPrincipal parent) {
        super(parent, "RECEPCIÓN: OPERACIONES DIARIAS", true);
        this.vPrincipal = parent;
        setSize(1000, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_CREMA);

        cargarDatosIniciales(); 
    }

   
    @Override
    public void cargarDatosIniciales() {
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pestañas.addTab("🔑 Nueva Reserva", crearPanelReservar());
        pestañas.addTab("👤 Registro Clientes", crearPanelRegistrarCliente());
        pestañas.addTab("📋 Lista de Huéspedes", crearPanelListaHuespedes());

        add(pestañas, BorderLayout.CENTER);
        cargarHuespedes();
        cargarTablaClientes();
    }

    @Override
    public void limpiarCampos() {
        if (txtNom != null) txtNom.setText("");
        if (txtCed != null) txtCed.setText("");
        if (txtTel != null) txtTel.setText("");
        if (txtIdCli != null) txtIdCli.setText("");
        if (txtIdHab != null) txtIdHab.setText("");
    }

  
    private JPanel crearPanelRegistrarCliente() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(COLOR_CREMA);

        JPanel panelForm = new JPanel(new GridLayout(4, 2, 10, 10));
        panelForm.setBackground(COLOR_CREMA);
        panelForm.setBorder(BorderFactory.createTitledBorder("Nuevo Registro de Huésped"));

        txtNom = new JTextField();
        txtCed = new JTextField();
        txtTel = new JTextField();
        JButton btnGuardar = new JButton("Guardar y Ver ID");
        styleButton(btnGuardar);

        panelForm.add(new JLabel("Nombre Completo:")); panelForm.add(txtNom);
        panelForm.add(new JLabel("Cédula:")); panelForm.add(txtCed);
        panelForm.add(new JLabel("Teléfono:")); panelForm.add(txtTel);
        panelForm.add(new JLabel("")); panelForm.add(btnGuardar);

        modeloBusquedaClientes = new DefaultTableModel(new String[]{"ID", "Nombre", "Cédula", "Teléfono"}, 0);
        JTable tablaClientes = new JTable(modeloBusquedaClientes);
        JScrollPane scroll = new JScrollPane(tablaClientes);

        btnGuardar.addActionListener(e -> {
            if (txtNom.getText().isEmpty() || txtCed.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y Cédula obligatorios.");
                return;
            }
            try (Connection con = Conexion.conectar();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO clientes (nombre, cedula, telefono) VALUES (?,?,?)")) {
                ps.setString(1, txtNom.getText());
                ps.setString(2, txtCed.getText());
                ps.setString(3, txtTel.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Cliente registrado.");
                limpiarCampos();
                cargarTablaClientes();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        panelPrincipal.add(panelForm, BorderLayout.NORTH);
        panelPrincipal.add(scroll, BorderLayout.CENTER);
        return panelPrincipal;
    }


    private JPanel crearPanelReservar() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 15, 15));
        panel.setBackground(COLOR_CREMA);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        txtIdCli = new JTextField();
        txtIdHab = new JTextField();
        String hoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        JTextField txtIn = new JTextField(hoy);
        JTextField txtOut = new JTextField("2026-04-20");
        JButton btnConfirmar = new JButton("Confirmar Check-in");
        styleButton(btnConfirmar);

        panel.add(new JLabel("ID Cliente:")); panel.add(txtIdCli);
        panel.add(new JLabel("ID Habitación:")); panel.add(txtIdHab);
        panel.add(new JLabel("Entrada (YYYY-MM-DD):")); panel.add(txtIn);
        panel.add(new JLabel("Salida (YYYY-MM-DD):")); panel.add(txtOut);
        panel.add(new JLabel("")); panel.add(btnConfirmar);

        btnConfirmar.addActionListener(e -> {
            try (Connection con = Conexion.conectar()) {
                con.setAutoCommit(false);
                String fEntrada = txtIn.getText().trim();
                String fActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

               
                PreparedStatement ps1 = con.prepareStatement("INSERT INTO reservas (id_cliente, id_habitacion, fecha_entrada, fecha_salida, estado_reserva) VALUES (?,?,?,?,'ACTIVA')");
                ps1.setInt(1, Integer.parseInt(txtIdCli.getText()));
                ps1.setInt(2, Integer.parseInt(txtIdHab.getText()));
                ps1.setString(3, fEntrada);
                ps1.setString(4, txtOut.getText());
                ps1.executeUpdate();

                if (fEntrada.equals(fActual)) {
                    PreparedStatement ps2 = con.prepareStatement("UPDATE habitaciones SET estado = 'OCUPADA' WHERE id = ?");
                    ps2.setInt(1, Integer.parseInt(txtIdHab.getText()));
                    ps2.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(this, "✅ Check-in exitoso.");
                limpiarCampos();
                cargarHuespedes();
                vPrincipal.cargarDatosHabitaciones();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error en reserva."); }
        });
        return panel;
    }


    private JPanel crearPanelListaHuespedes() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_CREMA);
        modeloHuespedes = new DefaultTableModel(new String[]{"ID Res", "Hab", "Nombre", "Estado", "Entrada"}, 0);
        JTable tabla = new JTable(modeloHuespedes);
        JScrollPane scroll = new JScrollPane(tabla);
        
        JButton btnOut = new JButton("REALIZAR CHECK-OUT");
        styleButton(btnOut);
        btnOut.setBackground(new Color(46, 125, 50));

        btnOut.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila != -1) {
                realizarCheckOut((int) modeloHuespedes.getValueAt(fila, 0), modeloHuespedes.getValueAt(fila, 1).toString());
            } else { JOptionPane.showMessageDialog(this, "Seleccione un huésped."); }
        });

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnOut, BorderLayout.SOUTH);
        return panel;
    }

    private void realizarCheckOut(int idReserva, String numHab) {
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
         
            PreparedStatement pst1 = con.prepareStatement("UPDATE reservas SET estado_reserva = 'COMPLETADA', fecha_salida = CURDATE() WHERE id = ?");
            pst1.setInt(1, idReserva); pst1.executeUpdate();

            PreparedStatement pst2 = con.prepareStatement("UPDATE habitaciones SET estado = 'SUCIA' WHERE numero = ?");
            pst2.setString(1, numHab); pst2.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this, "Check-out exitoso.");
            cargarHuespedes();
            vPrincipal.cargarDatosHabitaciones();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void cargarTablaClientes() {
        if (modeloBusquedaClientes == null) return;
        modeloBusquedaClientes.setRowCount(0);
        try (Connection con = Conexion.conectar();
             ResultSet rs = con.createStatement().executeQuery("SELECT * FROM clientes ORDER BY id DESC")) {
            while (rs.next()) {
                modeloBusquedaClientes.addRow(new Object[]{rs.getInt("id"), rs.getString("nombre"), rs.getString("cedula"), rs.getString("telefono")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void cargarHuespedes() {
        if (modeloHuespedes == null) return;
        modeloHuespedes.setRowCount(0);
        String sql = "SELECT r.id, h.numero, c.nombre, r.estado_reserva, r.fecha_entrada " +
                     "FROM reservas r JOIN clientes c ON r.id_cliente = c.id " +
                     "JOIN habitaciones h ON r.id_habitacion = h.id WHERE r.estado_reserva = 'ACTIVA'";
        try (Connection con = Conexion.conectar(); ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                modeloHuespedes.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void styleButton(JButton b) {
        b.setBackground(COLOR_BRONCE); b.setForeground(COLOR_BLANCO);
        b.setFocusPainted(false); b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setPreferredSize(new Dimension(280, 45)); b.setBorder(BorderFactory.createEmptyBorder());
    }
}