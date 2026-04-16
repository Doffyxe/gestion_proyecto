package gestion_de_hoteles;

import javax.swing.JPanel;
import java.awt.Color;


public abstract class PanelBase extends JPanel {
    protected final Color COLOR_CREMA = new Color(0xFFF8F0);

    public PanelBase() {
        setBackground(COLOR_CREMA); 
    }

  
    public abstract void actualizarPanel();
}