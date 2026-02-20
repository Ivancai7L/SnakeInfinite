package mi.proyecto.servidor;

import mi.proyecto.Dificultad;
//Guarda los datos del puerto y la dificultad
public class DatosServidor {

    private final int puerto;
    private final Dificultad dificultad;
    private volatile String estado;
    private volatile DatosClienteRemoto clienteRemoto;

    public DatosServidor(int puerto, Dificultad dificultad) {
        this.puerto = puerto;
        this.dificultad = dificultad;
        this.estado = "inicializando";
    }

    public int getPuerto() {
        return puerto;
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public DatosClienteRemoto getClienteRemoto() {
        return clienteRemoto;
    }

    public void setClienteRemoto(DatosClienteRemoto clienteRemoto) {
        this.clienteRemoto = clienteRemoto;
    }
}
