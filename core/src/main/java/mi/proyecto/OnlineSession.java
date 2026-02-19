package mi.proyecto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class OnlineSession {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public OnlineSession(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    public void enviar(String mensaje) {
        out.println(mensaje);
    }

    public String recibirLinea() throws IOException {
        return in.readLine();
    }

    public boolean estaCerrada() {
        return socket == null || socket.isClosed();
    }

    public synchronized void cerrar() {
        if (estaCerrada()) {
            return;
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {
        }

        if (out != null) {
            out.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
