package gestion_de_hoteles;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import java.io.File;
import javax.imageio.ImageIO;


public class GSTH_Hotel_Login_Pro {

    private JFrame frame;
    private JTextField txtUser;
    private JPasswordField txtPass;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            try {
                GSTH_Hotel_Login_Pro window = new GSTH_Hotel_Login_Pro();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public GSTH_Hotel_Login_Pro() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("E.M.J Logistic - Login Premium");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

      
        BackgroundPanel mainPanel = new BackgroundPanel("C:/Users/mario/Downloads/hoteles.png");
        mainPanel.setLayout(new GridBagLayout());
        frame.setContentPane(mainPanel);

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.gridx = 0;
        gbcMain.fill = GridBagConstraints.CENTER;

     
        JLabel lblEmpresa = new JLabel("E.M.J Logistic");
        lblEmpresa.setFont(new Font("Serif", Font.BOLD, 70));
        lblEmpresa.setForeground(new Color(0xFFF8F0));
        
        gbcMain.gridy = 0;
        gbcMain.insets = new Insets(0, 0, 30, 0); 
        mainPanel.add(lblEmpresa, gbcMain);

       
        JPanel loginCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 235)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
            }
        };
        loginCard.setOpaque(false);
        loginCard.setPreferredSize(new Dimension(450, 500));
        loginCard.setLayout(new GridBagLayout());
        
        GridBagConstraints gbcCard = new GridBagConstraints();
        gbcCard.insets = new Insets(10, 50, 10, 50);
        gbcCard.fill = GridBagConstraints.HORIZONTAL;
        gbcCard.gridx = 0;

        JLabel lblAcceso = new JLabel("System Access");
        lblAcceso.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblAcceso.setForeground(new Color(0x563A2D));
        lblAcceso.setHorizontalAlignment(SwingConstants.CENTER);
        gbcCard.gridy = 0;
        gbcCard.insets = new Insets(20, 50, 30, 50);
        loginCard.add(lblAcceso, gbcCard);

        txtUser = createStyledField("Username / Email");
        gbcCard.gridy = 1;
        gbcCard.insets = new Insets(10, 50, 10, 50);
        loginCard.add(txtUser, gbcCard);

        txtPass = new JPasswordField();
        txtPass.setPreferredSize(new Dimension(300, 55));
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPass.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0xA97B50)), "Password"
        ));
        gbcCard.gridy = 2;
        loginCard.add(txtPass, gbcCard);

        JButton btnLogin = new JButton("Login Now");
        btnLogin.setBackground(new Color(0xA97B50));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setPreferredSize(new Dimension(300, 55));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBorder(null);

        gbcCard.gridy = 3;
        gbcCard.insets = new Insets(40, 50, 40, 50);
        loginCard.add(btnLogin, gbcCard);

   
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            if(user.isEmpty() || pass.isEmpty()){
                JOptionPane.showMessageDialog(frame, "Please fill all fields", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection con = Conexion.conectar()) {
                if (con != null) {
                    String sql = "SELECT rol FROM usuarios WHERE usuario = ? AND password = ?";
                    PreparedStatement pst = con.prepareStatement(sql);
                    pst.setString(1, user);
                    pst.setString(2, pass);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        String rol = rs.getString("rol");
                        new VentanaPrincipal(rol).setVisible(true);
                        frame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid credentials", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Database Connection Error", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbcMain.gridy = 1;
        mainPanel.add(loginCard, gbcMain);
    }

    private JTextField createStyledField(String title) {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(300, 55));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0xA97B50)), title
        ));
        return tf;
    }

    class BackgroundPanel extends JPanel {
        private Image img;
        public BackgroundPanel(String path) {
            try { 
                File f = new File(path);
                if(f.exists()) img = ImageIO.read(f); 
            } catch (Exception e) {
                System.out.println("Fondo no encontrado, usando color sólido.");
            }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(0x563A2D)); 
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public void setVisible(boolean b) { frame.setVisible(b); }
}