package gestion_de_hoteles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class InterfazAdmin extends JDialog implements IFormulario {
    private final Color COLOR_CREMA = new Color(0xFFF8F0);
    private final Color COLOR_BRONCE = new Color(0xA97B50);
    private final Color COLOR_CAFE_OSCURO = new Color(0x563A2D);
    private final Color COLOR_BLANCO = Color.WHITE;

    private JLabel lblValorIngresos, lblValorOcupacion, lblValorPersonal;
    private VentanaPrincipal vPrincipal;
    private JTable tablaLista, tablaUsuarios;
    private DefaultTableModel modeloLista, modeloUsuarios;

    private JTextField txtBusqueda, txtIdCli, txtIdHab, txtIn, txtOut;

    public InterfazAdmin(VentanaPrincipal parent) {
        super(parent, "DASHBOARD GERENCIAL - E.M.J LOGISTIC", true);
        this.vPrincipal = parent;
        
        setSize(1100, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_CREMA);

       
        cargarDatosIniciales();
    }

 
    @Override
    public void cargarDatosIniciales() {
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("Segoe UI", Font.PLAIN, 14));

      
        pestañas.addTab("📊 Dashboard", crearPanelDashboardReal());
        pestañas.addTab("📋 Historial & Check-Out", crearPanelListaClientes());
        pestañas.addTab("🔑 Reservas", crearPanelReservas());
        pestañas.addTab("🛏 Habitaciones", crearPanelHabitaciones());
        pestañas.addTab("👥 Personal", crearPanelUsuarios());
        pestañas.addTab("👤 Clientes", crearPanelClientes());

        add(pestañas, BorderLayout.CENTER);
        actualizarEstadisticas();
    }

    @Override
    public void limpiarCampos() {
       
        if(txtIdCli != null) txtIdCli.setText("");
        if(txtIdHab != null) txtIdHab.setText("");
        if(txtBusqueda != null) txtBusqueda.setText("");
    }

   
    private JPanel crearPanelDashboardReal() {
        JPanel panelBase = new JPanel(new BorderLayout());
        panelBase.setBackground(COLOR_CREMA);
        JPanel panelTarjetas = new JPanel(new GridLayout(1, 3, 20, 0));
        panelTarjetas.setBackground(COLOR_CREMA);
        panelTarjetas.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        lblValorIngresos = new JLabel("$0.00");
        lblValorOcupacion = new JLabel("0%");
        lblValorPersonal = new JLabel("0");

        panelTarjetas.add(crearTarjetaDashboard("INGRESOS TOTALES", lblValorIngresos, "Habitaciones ocupadas"));
        panelTarjetas.add(crearTarjetaDashboard("OCUPACIÓN ACTUAL", lblValorOcupacion, "Huéspedes activos"));
        panelTarjetas.add(crearTarjetaDashboard("PERSONAL ACTIVO", lblValorPersonal, "Usuarios en sistema"));

        panelBase.add(panelTarjetas, BorderLayout.NORTH);
        JButton btnRefrescar = new JButton("🔄 Sincronizar Datos en Tiempo Real");
        btnRefrescar.addActionListener(e -> actualizarEstadisticas());
        styleButton(btnRefrescar);
        
        JPanel pnlBoton = new JPanel(); pnlBoton.setBackground(COLOR_CREMA); pnlBoton.add(btnRefrescar);
        panelBase.add(pnlBoton, BorderLayout.SOUTH);
        return panelBase;
    }

   
    private JPanel crearPanelListaClientes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(COLOR_CREMA);
        JPanel panelFiltros = new JPanel(new BorderLayout());
        panelFiltros.setBackground(COLOR_CAFE_OSCURO); 
        panelFiltros.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("HISTORIAL DE HUÉSPEDES & OPERACIONES", SwingConstants.CENTER);
        lblHeader.setForeground(COLOR_CREMA); lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panelFiltros.add(lblHeader, BorderLayout.NORTH);

        JPanel panelInputs = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelInputs.setOpaque(false);
        txtBusqueda = new JTextField(15);
        txtBusqueda.setForeground(Color.WHITE); txtBusqueda.setBackground(new Color(60, 45, 35));
        txtBusqueda.setCaretColor(Color.WHITE);
        txtBusqueda.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_CREMA), "Nombre Huésped", 0, 0, null, COLOR_CREMA));

        JButton btnSearch = new JButton("Filtrar / Recargar");
        styleButton(btnSearch); btnSearch.setPreferredSize(new Dimension(180, 40));
        btnSearch.addActionListener(e -> cargarHistorial(txtBusqueda.getText()));

        panelInputs.add(txtBusqueda); panelInputs.add(btnSearch);
        panelFiltros.add(panelInputs, BorderLayout.CENTER);

        modeloLista = new DefaultTableModel(new String[]{"ID Res", "Hab", "Huésped", "Cédula", "Estado", "Entrada", "Salida", "Total Pagado"}, 0);
        tablaLista = new JTable(modeloLista); tablaLista.setRowHeight(35);
        JScrollPane scroll = new JScrollPane(tablaLista);

        JButton btnCheckOut = new JButton("🔔 REALIZAR CHECK-OUT SELECCIONADO");
        styleButton(btnCheckOut); btnCheckOut.setBackground(new Color(46, 125, 50)); 
        
        btnCheckOut.addActionListener(e -> {
            int fila = tablaLista.getSelectedRow();
            if (fila == -1) { JOptionPane.showMessageDialog(this, "Selecciona una reserva."); return; }
            int idReserva = (int) modeloLista.getValueAt(fila, 0);
            String numHab = (String) modeloLista.getValueAt(fila, 1);
            String estado = (String) modeloLista.getValueAt(fila, 4);

            if (!estado.equalsIgnoreCase("ACTIVA")) { JOptionPane.showMessageDialog(this, "Esta reserva ya fue finalizada."); return; }
            if (JOptionPane.showConfirmDialog(this, "¿Finalizar estancia en habitación " + numHab + "?") == JOptionPane.YES_OPTION) {
                realizarCheckOut(idReserva, numHab); cargarHistorial("");
            }
        });

        JPanel pnlSur = new JPanel(); pnlSur.setBackground(COLOR_CREMA); pnlSur.add(btnCheckOut);
        panelPrincipal.add(panelFiltros, BorderLayout.NORTH); panelPrincipal.add(scroll, BorderLayout.CENTER); panelPrincipal.add(pnlSur, BorderLayout.SOUTH);
        cargarHistorial(""); return panelPrincipal;
    }

    private void realizarCheckOut(int idReserva, String numHab) {
        String sql1 = "UPDATE reservas SET estado_reserva = 'COMPLETADA', fecha_salida = CURDATE() WHERE id = ?";
        String sql2 = "UPDATE habitaciones SET estado = 'SUCIA' WHERE numero = ?";
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try (PreparedStatement pst1 = con.prepareStatement(sql1);
                 PreparedStatement pst2 = con.prepareStatement(sql2)) {
                pst1.setInt(1, idReserva); pst1.executeUpdate();
                pst2.setString(1, numHab); pst2.executeUpdate();
                con.commit();
                JOptionPane.showMessageDialog(this, "Check-out procesado exitosamente.");
                actualizarEstadisticas(); vPrincipal.cargarDatosHabitaciones();
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void cargarHistorial(String filtro) {
        modeloLista.setRowCount(0);
        String sql = "SELECT r.id, h.numero, c.nombre, c.cedula, r.estado_reserva, r.fecha_entrada, r.fecha_salida, " +
                     "(h.precio * GREATEST(DATEDIFF(IFNULL(r.fecha_salida, CURDATE()), r.fecha_entrada), 1)) as total " +
                     "FROM reservas r JOIN clientes c ON r.id_cliente = c.id " +
                     "JOIN habitaciones h ON r.id_habitacion = h.id " +
                     "WHERE c.nombre LIKE ? ORDER BY r.id DESC";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "%" + filtro + "%"); ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                modeloLista.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), "$" + rs.getDouble("total")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

  
    private JPanel crearPanelReservas() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBackground(COLOR_CREMA); panel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        txtIdCli = new JTextField(); txtIdHab = new JTextField();
        String hoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        txtIn = new JTextField(hoy); txtOut = new JTextField("2026-04-20");
        JButton btn = new JButton("Confirmar Reserva"); styleButton(btn);

        panel.add(new JLabel("ID Cliente:")); panel.add(txtIdCli);
        panel.add(new JLabel("ID Habitación:")); panel.add(txtIdHab);
        panel.add(new JLabel("Entrada (YYYY-MM-DD):")); panel.add(txtIn);
        panel.add(new JLabel("Salida (YYYY-MM-DD):")); panel.add(txtOut);
        panel.add(new JLabel("")); panel.add(btn);

        btn.addActionListener(e -> {
            try (Connection con = Conexion.conectar()) {
                con.setAutoCommit(false);
                String fEntrada = txtIn.getText().trim(); String fActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                PreparedStatement ps1 = con.prepareStatement("INSERT INTO reservas (id_cliente, id_habitacion, fecha_entrada, fecha_salida, estado_reserva) VALUES (?,?,?,?,'ACTIVA')");
                ps1.setInt(1, Integer.parseInt(txtIdCli.getText())); ps1.setInt(2, Integer.parseInt(txtIdHab.getText()));
                ps1.setString(3, fEntrada); ps1.setString(4, txtOut.getText()); ps1.executeUpdate();
                
                if (fEntrada.equals(fActual)) {
                    PreparedStatement ps2 = con.prepareStatement("UPDATE habitaciones SET estado = 'OCUPADA' WHERE id = ?");
                    ps2.setInt(1, Integer.parseInt(txtIdHab.getText())); ps2.executeUpdate();
                }
                con.commit(); JOptionPane.showMessageDialog(this, "Reserva registrada.");
                limpiarCampos(); 
                vPrincipal.cargarDatosHabitaciones(); actualizarEstadisticas(); cargarHistorial("");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });
        return panel;
    }


    private JPanel crearPanelHabitaciones() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(COLOR_CREMA); panel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        JTextField txtN = new JTextField(); JTextField txtP = new JTextField();
        JComboBox<String> cbT = new JComboBox<>(new String[]{"SIMPLE", "DOBLE", "SUITE"});
        JButton btnG = new JButton("Guardar Habitación"); styleButton(btnG);
        panel.add(new JLabel("Número:")); panel.add(txtN);
        panel.add(new JLabel("Tipo:")); panel.add(cbT);
        panel.add(new JLabel("Precio:")); panel.add(txtP);
        panel.add(new JLabel("")); panel.add(btnG);
        btnG.addActionListener(e -> {
            try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement("INSERT INTO habitaciones (numero, tipo, precio, estado) VALUES (?,?,?,'DISPONIBLE')")) {
                ps.setString(1, txtN.getText()); ps.setString(2, cbT.getSelectedItem().toString());
                ps.setDouble(3, Double.parseDouble(txtP.getText())); ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Habitación registrada."); vPrincipal.cargarDatosHabitaciones(); actualizarEstadisticas();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        return panel;
    }

   
    private JPanel crearPanelUsuarios() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(COLOR_CREMA);
        JPanel panelForm = new JPanel(new GridLayout(4, 2, 10, 10));
        panelForm.setBackground(COLOR_CREMA); panelForm.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField txtU = new JTextField(); JTextField txtPa = new JTextField();
        JComboBox<String> cbR = new JComboBox<>(new String[]{"ADMIN", "RECEPCION", "LIMPIEZA", "MANTENIMIENTO"});
        JButton btnC = new JButton("Crear Usuario"); styleButton(btnC);

        panelForm.add(new JLabel("Usuario (Email):")); panelForm.add(txtU);
        panelForm.add(new JLabel("Contraseña:")); panelForm.add(txtPa);
        panelForm.add(new JLabel("Rol:")); panelForm.add(cbR);
        panelForm.add(new JLabel("")); panelForm.add(btnC);

        modeloUsuarios = new DefaultTableModel(new String[]{"ID", "Usuario", "Rol"}, 0);
        tablaUsuarios = new JTable(modeloUsuarios); JScrollPane scroll = new JScrollPane(tablaUsuarios);

        JButton btnBorrar = new JButton("🗑 ELIMINAR USUARIO SELECCIONADO");
        styleButton(btnBorrar); btnBorrar.setBackground(new Color(183, 28, 28));

        btnC.addActionListener(e -> {
            try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement("INSERT INTO usuarios (usuario, password, rol) VALUES (?,?,?)")) {
                ps.setString(1, txtU.getText()); ps.setString(2, txtPa.getText());
                ps.setString(3, cbR.getSelectedItem().toString()); ps.executeUpdate();
                cargarTablaUsuarios(); actualizarEstadisticas();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnBorrar.addActionListener(e -> {
            int fila = tablaUsuarios.getSelectedRow();
            if (fila == -1) return;
            String user = (String) modeloUsuarios.getValueAt(fila, 1);
            
            if (user.equalsIgnoreCase("admin") || user.equalsIgnoreCase("mario@emj.com")) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar al usuario raíz: " + user, "ERROR DE SEGURIDAD", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (JOptionPane.showConfirmDialog(this, "¿Borrar usuario " + user + "?") == JOptionPane.YES_OPTION) {
                try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
                    ps.setInt(1, (int) modeloUsuarios.getValueAt(fila, 0));
                    ps.executeUpdate(); cargarTablaUsuarios(); actualizarEstadisticas();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        panelPrincipal.add(panelForm, BorderLayout.NORTH); panelPrincipal.add(scroll, BorderLayout.CENTER); panelPrincipal.add(btnBorrar, BorderLayout.SOUTH);
        cargarTablaUsuarios(); return panelPrincipal;
    }

    private void cargarTablaUsuarios() {
        if (modeloUsuarios == null) return; modeloUsuarios.setRowCount(0);
        try (Connection con = Conexion.conectar(); ResultSet rs = con.createStatement().executeQuery("SELECT id, usuario, rol FROM usuarios")) {
            while (rs.next()) { modeloUsuarios.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)}); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

  
    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(COLOR_CREMA); panel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        JTextField txtNo = new JTextField(); JTextField txtCe = new JTextField(); JTextField txtTe = new JTextField();
        JButton btnR = new JButton("Registrar Cliente"); styleButton(btnR);
        panel.add(new JLabel("Nombre:")); panel.add(txtNo);
        panel.add(new JLabel("Cédula:")); panel.add(txtCe);
        panel.add(new JLabel("Teléfono:")); panel.add(txtTe);
        panel.add(new JLabel("")); panel.add(btnR);
        btnR.addActionListener(e -> {
            try (Connection con = Conexion.conectar(); PreparedStatement ps = con.prepareStatement("INSERT INTO clientes (nombre, cedula, telefono) VALUES (?,?,?)")) {
                ps.setString(1, txtNo.getText()); ps.setString(2, txtCe.getText()); ps.setString(3, txtTe.getText());
                ps.executeUpdate(); JOptionPane.showMessageDialog(this, "Cliente registrado.");
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        return panel;
    }

    private void actualizarEstadisticas() {
        try (Connection con = Conexion.conectar()) {
            if (con != null) {
                ResultSet rs1 = con.createStatement().executeQuery("SELECT SUM(precio) FROM habitaciones WHERE estado = 'OCUPADA'");
                if (rs1.next()) lblValorIngresos.setText(new DecimalFormat("$#,##0.00").format(rs1.getDouble(1)));
                ResultSet rsT = con.createStatement().executeQuery("SELECT COUNT(*) FROM habitaciones");
                rsT.next(); int tot = rsT.getInt(1);
                ResultSet rsO = con.createStatement().executeQuery("SELECT COUNT(*) FROM habitaciones WHERE estado = 'OCUPADA'");
                rsO.next(); int ocu = rsO.getInt(1);
                if (tot > 0) lblValorOcupacion.setText(((ocu * 100) / tot) + "% (" + ocu + "/" + tot + ")");
                ResultSet rsP = con.createStatement().executeQuery("SELECT COUNT(*) FROM usuarios");
                if (rsP.next()) lblValorPersonal.setText(String.valueOf(rsP.getInt(1)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private JPanel crearTarjetaDashboard(String t, JLabel v, String s) {
        JPanel card = new JPanel(new GridLayout(3, 1)); card.setBackground(COLOR_BLANCO);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)), BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        JLabel lblT = new JLabel(t); lblT.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblT.setForeground(COLOR_BRONCE);
        v.setFont(new Font("Segoe UI", Font.BOLD, 35)); v.setForeground(COLOR_CAFE_OSCURO);
        JLabel lblS = new JLabel(s); lblS.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lblS.setForeground(Color.GRAY);
        card.add(lblT); card.add(v); card.add(lblS); return card;
    }

    private void styleButton(JButton b) {
        b.setBackground(COLOR_BRONCE); b.setForeground(COLOR_BLANCO); b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14)); b.setPreferredSize(new Dimension(280, 45)); b.setBorder(BorderFactory.createEmptyBorder());
    }
}