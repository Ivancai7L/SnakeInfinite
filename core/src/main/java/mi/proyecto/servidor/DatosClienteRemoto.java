package mi.proyecto.servidor;
//Guarda la ip y muestra el estado de si esta conectado o no
public class DatosClienteRemoto {

    private final String direccionIp;
    private volatile boolean conectado;
    private volatile String estado;

    public DatosClienteRemoto(String direccionIp) {
        this.direccionIp = direccionIp;
        this.estado = "sin conectar";
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
