package gestion_de_hoteles;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class UIStyles {
   
    public static final Color COLOR_FONDO = new Color(0xFFF8F0);      // Crema suave
    public static final Color COLOR_TARJETA = Color.WHITE;             // Tarjetas blancas
    public static final Color COLOR_PRIMARIO = new Color(0xA97B50);   // Bronceado (Botones)
    public static final Color COLOR_TEXTO_OSCURO = new Color(0x563A2D); // Marrón oscuro
    public static final Color COLOR_TEXTO_BLANCO = Color.WHITE;

  
    public static final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TEXTO = new Font("Segoe UI", Font.PLAIN, 14);
    
   
    public static final Border BORDE_TARJETA = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0xE0E0E0), 1),
        BorderFactory.createEmptyBorder(15, 15, 15, 15)
    );
}