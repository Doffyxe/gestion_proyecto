package gestion_de_hoteles;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;


public class VentanaPrincipal extends JFrame implements IFormulario {

    private JTable tablaHabitaciones;
    private DefaultTableModel modelo;
    private JPanel panelContenido;
    private String rolGlobal;

    public VentanaPrincipal(String rolUsuario) {
        this.rolGlobal = rolUsuario.toUpperCase();
        
        setTitle("E.M.J Logistic - Hotel Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIStyles.COLOR_FONDO);
        setLayout(new BorderLayout());

   
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIStyles.COLOR_TEXTO_OSCURO);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel lblLogo = new JLabel("E.M.J Logistic");
        lblLogo.setForeground(UIStyles.COLOR_FONDO);
        lblLogo.setFont(UIStyles.FONT_TITULO);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(30, 20, 40, 20));
        sidebar.add(lblLogo);

     
        agregarBotonesPermisos(sidebar);

        add(sidebar, BorderLayout.WEST);

   
        panelContenido = new JPanel(new BorderLayout(20, 20));
        panelContenido.setBackground(UIStyles.COLOR_FONDO);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitulo = new JLabel("Estado de Habitaciones");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitulo.setForeground(UIStyles.COLOR_TEXTO_OSCURO);
        
        JLabel lblUser = new JLabel("Sesión: " + rolUsuario + "  👤");
        lblUser.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        header.add(lblTitulo, BorderLayout.WEST);
        header.add(lblUser, BorderLayout.EAST);
        
        panelContenido.add(header, BorderLayout.NORTH);

        
        configurarTabla();

        add(panelContenido, BorderLayout.CENTER);
        cargarDatosIniciales(); // Implementación de IFormulario
    }

    private void agregarBotonesPermisos(JPanel sidebar) {
        sidebar.add(crearBotonMenu("🏠  Vista General"));
        
      
        if (rolGlobal.equals("ADMIN") || rolGlobal.equals("RECEPCION")) {
            JButton btnRecepcion = crearBotonMenu("🧑‍💼  Recepción");
            btnRecepcion.addActionListener(e -> new InterfazRecepcion(this).setVisible(true));
            sidebar.add(btnRecepcion);
        }

        if (!rolGlobal.equals("RECEPCION")) {
            JButton btnStaff = crearBotonMenu("🧹🛠  Operaciones Staff");
            btnStaff.addActionListener(e -> new InterfazStaff(this, rolGlobal).setVisible(true));
            sidebar.add(btnStaff);
        }

      
        if (rolGlobal.equals("ADMIN")) {
            JButton btnAdmin = crearBotonMenu("⚙  Panel de Gerencia");
            btnAdmin.addActionListener(e -> new InterfazAdmin(this).setVisible(true));
            sidebar.add(btnAdmin);
        }
    }

    private void configurarTabla() {
        String[] columnas = {"ID", "N° Habitación", "Tipo", "Precio", "Estado"};
        modelo = new DefaultTableModel(columnas, 0);
        tablaHabitaciones = new JTable(modelo);
        tablaHabitaciones.setRowHeight(45);
        tablaHabitaciones.getTableHeader().setBackground(UIStyles.COLOR_PRIMARIO);
        tablaHabitaciones.getTableHeader().setForeground(UIStyles.COLOR_TEXTO_BLANCO);

        JScrollPane scroll = new JScrollPane(tablaHabitaciones);
        panelContenido.add(scroll, BorderLayout.CENTER);
    }

  
    @Override
    public void cargarDatosIniciales() {
        cargarDatosHabitaciones();
    }

    @Override
    public void limpiarCampos() {
        
    }

    public void cargarDatosHabitaciones() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM habitaciones ORDER BY numero ASC")) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("numero"), rs.getString("tipo"),
                    "$" + rs.getDouble("precio"), rs.getString("estado")
                });
            }
            aplicarRenderColores();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void aplicarRenderColores() {
        tablaHabitaciones.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (value != null) ? value.toString() : "";
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));

              
                if (estado.equals("DISPONIBLE")) { c.setBackground(new Color(200, 230, 201)); c.setForeground(new Color(46, 125, 50)); }
                else if (estado.equals("OCUPADA")) { c.setBackground(new Color(255, 205, 210)); c.setForeground(new Color(198, 40, 40)); }
                else if (estado.equals("SUCIA")) { c.setBackground(new Color(255, 249, 196)); c.setForeground(new Color(245, 127, 23)); }
                
                if (isSelected) { c.setBackground(table.getSelectionBackground()); c.setForeground(table.getSelectionForeground()); }
                return c;
            }
        });
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(280, 55));
        btn.setForeground(UIStyles.COLOR_FONDO);
        btn.setBackground(UIStyles.COLOR_TEXTO_OSCURO);
        btn.setFont(UIStyles.FONT_TEXTO);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(UIStyles.COLOR_PRIMARIO); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(UIStyles.COLOR_TEXTO_OSCURO); }
        });
        
        return btn;
    }
}