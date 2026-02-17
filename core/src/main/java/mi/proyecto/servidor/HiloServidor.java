package mi.proyecto.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import mi.proyecto.OnlineSession;

public class HiloServidor extends Thread {

    public interface Listener {
        void onEstado(String estado);
        void onConectado(OnlineSession session, DatosServidor datosServidor);
        void onError(String mensaje);
    }

    private final DatosServidor datosServidor;
    private final Listener listener;

    public HiloServidor(DatosServidor datosServidor, Listener listener) {
        super("online-host-thread");
        this.datosServidor = datosServidor;
        this.listener = listener;
        setDaemon(true);
    }

    @Override
    public void run() {
        datosServidor.setEstado("Servidor escuchando en puerto " + datosServidor.getPuerto());
        listener.onEstado(datosServidor.getEstado());

        try (ServerSocket serverSocket = new ServerSocket(datosServidor.getPuerto())) {
            datosServidor.setEstado("Esperando cliente...");
            listener.onEstado(datosServidor.getEstado());

            Socket socket = serverSocket.accept();
            DatosClienteRemoto cliente = new DatosClienteRemoto(socket.getInetAddress().getHostAddress());
            cliente.setConectado(true);
            cliente.setEstado("conectado");
            datosServidor.setClienteRemoto(cliente);

            OnlineSession session = new OnlineSession(socket);
            session.enviar("STATUS:SERVIDOR_LISTO");
            session.enviar("ROLE:CLIENT");
            session.enviar("DIFF:" + datosServidor.getDificultad().name());
            session.enviar("STATUS:INICIANDO_PARTIDA");

            datosServidor.setEstado("Cliente conectado: " + cliente.getDireccionIp());
            listener.onEstado(datosServidor.getEstado());
            listener.onConectado(session, datosServidor);
        } catch (IOException e) {
            datosServidor.setEstado("error: " + e.getMessage());
            listener.onError(datosServidor.getEstado());
        }
    }
}
