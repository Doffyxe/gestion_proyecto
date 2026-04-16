package gestion_de_hoteles;


public abstract class Persona {
    protected String nombre;
    protected String cedula;

  
    public Persona(String nombre, String cedula) {
        this.nombre = nombre;
        this.cedula = cedula;
    }

   
    public abstract String getTipoPersona();

    public String getNombre() { return nombre; }
}