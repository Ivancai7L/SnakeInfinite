package mi.proyecto.cliente;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import mi.proyecto.Dificultad;
import mi.proyecto.OnlineSession;

public class HiloCliente extends Thread {

    private static final int CONNECT_TIMEOUT_MS = 4000;

    public interface Listener {
        void onEstado(String estado);
        void onConectado(OnlineSession session, Dificultad dificultad);
        void onError(String mensaje);
    }

    private final String ip;
    private final int puerto;
    private final Listener listener;

    public HiloCliente(String ip, int puerto, Listener listener) {
        super("online-join-thread");
        this.ip = ip;
        this.puerto = puerto;
        this.listener = listener;
        setDaemon(true);
    }

    @Override
    public void run() {
        listener.onEstado("Conectando a " + ip + ":" + puerto + "...");

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, puerto), CONNECT_TIMEOUT_MS);
            OnlineSession session = new OnlineSession(socket);
            session.enviar("STATUS:CLIENTE_CONECTADO");

            String handshake = leerSiguienteNoStatus(session);
            if (handshake == null) {
                session.cerrar();
                listener.onError("Conexión cerrada por el servidor antes del inicio");
                return;
            }
            if (!"ROLE:CLIENT".equals(handshake)) {
                session.cerrar();
                listener.onError("Conexión inválida: servidor no respondió ROLE:CLIENT");
                return;
            }

            String diffMsg = leerSiguienteNoStatus(session);
            Dificultad dificultadRemota = Dificultad.NORMAL;
            if (diffMsg != null && diffMsg.startsWith("DIFF:")) {
                String nombreDiff = diffMsg.substring(5).trim();
                try {
                    dificultadRemota = Dificultad.valueOf(nombreDiff);
                } catch (IllegalArgumentException ignored) {
                    dificultadRemota = Dificultad.NORMAL;
                }
            }

            listener.onEstado("Conectado. Dificultad recibida: " + dificultadRemota.name());
            listener.onConectado(session, dificultadRemota);
        } catch (IOException e) {
            listener.onError("Error conexión: " + e.getMessage());
        }
    }

    private String leerSiguienteNoStatus(OnlineSession session) throws IOException {
        String msg;
        while ((msg = session.recibirLinea()) != null) {
            if (msg.startsWith("STATUS:")) {
                String texto = msg.substring(7).replace('_', ' ');
                listener.onEstado(texto);
                continue;
            }
            return msg;
        }
        return null;
    }
}
