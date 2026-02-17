package mi.proyecto.servidor;

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
