package mi.proyecto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//Representa la conexión entre los dos jugadores.
//Envuelve el socket TCP y expone dos métodos simples: enviar() para mandar un mensaje y recibirLinea() para leer uno.

public class OnlineSession {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private volatile boolean cerrada;

    public OnlineSession(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public synchronized void enviar(String mensaje) {
        if (cerrada) {
            return;
        }
        out.println(mensaje);
        out.flush();
    }

    public String recibirLinea() throws IOException {
        return in.readLine();
    }

    public boolean estaCerrada() {
        return cerrada || socket.isClosed();
    }

    public synchronized void cerrar() {
        if (cerrada) {
            return;
        }
        cerrada = true;

        try {
            in.close();
        } catch (IOException ignored) {
        }

        out.close();

        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
