package mi.proyecto.servidor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import mi.proyecto.OnlineSession;


//Thread que corre en segundo plano esperando la conexión de un cliente.
//Abre el ServerSocket, hace el handshake (saludo) y cuando todo está bien avisa via Listener para arrancar la partida.
public class HiloServidor extends Thread {

    private static final int ACCEPT_TIMEOUT_MS = 30000;
    private static final int HANDSHAKE_TIMEOUT_MS = 5000;

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

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(datosServidor.getPuerto()));
            serverSocket.setSoTimeout(ACCEPT_TIMEOUT_MS);

            datosServidor.setEstado("Esperando cliente...");
            listener.onEstado(datosServidor.getEstado());

            Socket socket = serverSocket.accept();
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setSoTimeout(HANDSHAKE_TIMEOUT_MS);

            OnlineSession session = new OnlineSession(socket);
            String saludo = session.recibirLinea();
            if (saludo == null || !saludo.startsWith("STATUS:CLIENTE_CONECTADO")) {
                session.cerrar();
                throw new IOException("Handshake inválido del cliente");
            }

            DatosClienteRemoto cliente = new DatosClienteRemoto(socket.getInetAddress().getHostAddress());
            cliente.setConectado(true);
            cliente.setEstado("conectado");
            datosServidor.setClienteRemoto(cliente);

            session.enviar("STATUS:SERVIDOR_LISTO");
            session.enviar("ROLE:CLIENT");
            session.enviar("DIFF:" + datosServidor.getDificultad().name());
            session.enviar("STATUS:INICIANDO_PARTIDA");

            socket.setSoTimeout(0);
            datosServidor.setEstado("Cliente conectado: " + cliente.getDireccionIp());
            listener.onEstado(datosServidor.getEstado());
            listener.onConectado(session, datosServidor);
        } catch (SocketTimeoutException e) {
            datosServidor.setEstado("timeout: no se pudo completar la conexión");
            listener.onError(datosServidor.getEstado());
        } catch (IOException e) {
            datosServidor.setEstado("error: " + e.getMessage());
            listener.onError(datosServidor.getEstado());
        }
    }
}
