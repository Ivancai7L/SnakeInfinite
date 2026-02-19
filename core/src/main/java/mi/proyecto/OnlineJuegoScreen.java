package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OnlineJuegoScreen implements Screen {

    private static final float VELOCIDAD_BASE = 60f;
    private static final float TAMANIO = 25f;
    private static final float MAX_DELTA_SEGUNDOS = 1f / 20f;
    private static final float CAMPO_ANCHO = 1080f;
    private static final float CAMPO_ALTO = 720f;
    private static final String TEXTURA_JUGADOR_1 = "Snakeimg.png";
    private static final String TEXTURA_JUGADOR_2 = "Snakeimg2.png";
    private static final String TEXTURA_FRUTA = "Frutaimg.png";

    private final MiJuegoPrincipal game;
    private final OnlineSession session;
    private final boolean servidor;
    private final Dificultad dificultadPartida;

    private Snake snakeLocal;
    private final List<Vector2> cuerpoRemoto = new ArrayList<>();
    private BitmapFont font;
    private Texture fondoJuego;
    private Texture texturaRemota;
    private Texture texturaFruta;
    private final Vector2 frutaPos = new Vector2(300, 300);
    private int frutaIdActual;
    private int ultimaFrutaComidaPredicha = -1;

    private volatile boolean corriendo;
    private volatile String estadoConexion = "Conectado";
    private boolean juegoTerminado;
    private int puntajeServidor;
    private int puntajeCliente;
    private boolean enMenu;
    private volatile boolean estadoRemotoRecibido;

    public OnlineJuegoScreen(MiJuegoPrincipal game, OnlineSession session, boolean servidor, Dificultad dificultadPartida) {
        this.game = game;
        this.session = session;
        this.servidor = servidor;
        this.dificultadPartida = dificultadPartida == null ? Dificultad.NORMAL : dificultadPartida;
    }

    @Override
    public void show() {
        float velocidad = VELOCIDAD_BASE * dificultadPartida.getVelocidad();
        snakeLocal = new Snake(velocidad, TAMANIO, servidor ? TEXTURA_JUGADOR_1 : TEXTURA_JUGADOR_2);
        posicionarSnakeInicial();

        fondoJuego = cargarTexturaConFallback("tierrafondo.png", 28, 45, 30);

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.ORANGE);
        texturaRemota = cargarTexturaConFallback(servidor ? TEXTURA_JUGADOR_2 : TEXTURA_JUGADOR_1, 70, 160, 255);
        texturaFruta = cargarTexturaConFallback(TEXTURA_FRUTA, 255, 90, 90);

        if (servidor) {
            regenerarFrutaServidor();
            enviarScore();
        }

        iniciarReceptor();
        session.enviar("STATUS:" + (servidor ? "SERVIDOR_EN_PARTIDA" : "CLIENTE_EN_PARTIDA"));
    }

    private void posicionarSnakeInicial() {
        if (snakeLocal == null || snakeLocal.getCuerpo().isEmpty()) return;
        float y = CAMPO_ALTO / 2f;
        float x = servidor ? CAMPO_ANCHO * 0.25f : CAMPO_ANCHO * 0.75f;
        snakeLocal.getCabeza().set(x, y);
    }

    private Texture cargarTexturaConFallback(String ruta, int r, int g, int b) {
        if (Gdx.files.internal(ruta).exists()) {
            return new Texture(ruta);
        }
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r / 255f, g / 255f, b / 255f, 1f);
        pixmap.fill();
        Texture fallback = new Texture(pixmap);
        pixmap.dispose();
        return fallback;
    }

    private void iniciarReceptor() {
        corriendo = true;
        Thread t = new Thread(() -> {
            while (corriendo && !session.estaCerrada()) {
                try {
                    String linea = session.recibirLinea();
                    if (linea == null) {
                        estadoConexion = "Conexión cerrada";
                        juegoTerminado = true;
                        break;
                    }
                    procesarMensaje(linea);
                } catch (IOException e) {
                    estadoConexion = "Error de red";
                    juegoTerminado = true;
                    break;
                }
            }
        }, "online-recv-thread");
        t.setDaemon(true);
        t.start();
    }

    private void procesarMensaje(String msg) {
        if (msg.startsWith("DIR:")) {
            return;
        }

        if (msg.startsWith("STATE:")) {
            String data = msg.substring(6);
            List<Vector2> nuevo = new ArrayList<>();
            if (!data.isEmpty()) {
                String[] puntos = data.split(";");
                for (String p : puntos) {
                    String[] xy = p.split(",");
                    if (xy.length == 2) {
                        try {
                            nuevo.add(new Vector2(Float.parseFloat(xy[0]), Float.parseFloat(xy[1])));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            synchronized (cuerpoRemoto) {
                cuerpoRemoto.clear();
                cuerpoRemoto.addAll(nuevo);
                estadoRemotoRecibido = !cuerpoRemoto.isEmpty();
            }
            return;
        }

        if (msg.startsWith("FRUIT:")) {
            String data = msg.substring(6);
            String[] parts = data.split(":");
            if (parts.length == 2) {
                try {
                    frutaIdActual = Integer.parseInt(parts[0]);
                } catch (NumberFormatException ignored) {
                }
                data = parts[1];
            }
            String[] xy = data.split(",");
            if (xy.length == 2) {
                try {
                    frutaPos.set(Float.parseFloat(xy[0]), Float.parseFloat(xy[1]));
                } catch (NumberFormatException ignored) {
                }
            }
            return;
        }

        if (msg.startsWith("SCORE:")) {
            String data = msg.substring(6);
            String[] values = data.split(",");
            if (values.length == 2) {
                try {
                    puntajeServidor = Integer.parseInt(values[0]);
                    puntajeCliente = Integer.parseInt(values[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            return;
        }

        if (msg.startsWith("STATUS:")) {
            String estadoRed = msg.substring(7).replace('_', ' ');
            if (!juegoTerminado) {
                estadoConexion = estadoRed;
            }
            return;
        }

        if (msg.startsWith("END:")) {
            String[] values = msg.split(":", 5);
            if (values.length >= 5) {
                try {
                    puntajeServidor = Integer.parseInt(values[3]);
                    puntajeCliente = Integer.parseInt(values[4]);
                } catch (NumberFormatException ignored) {
                }
                String ganador = values[1];
                String motivo = values[2];
                if ("EMPATE".equals(ganador)) {
                    estadoConexion = "Empate - " + motivo;
                } else {
                    boolean gane = (servidor && "SERVIDOR".equals(ganador)) || (!servidor && "CLIENTE".equals(ganador));
                    estadoConexion = (gane ? "Ganaste" : "Perdiste") + " - " + motivo;
                }
            }
            juegoTerminado = true;
            return;
        }

        if (msg.startsWith("LOSE")) {
            estadoConexion = "Ganaste la partida";
            juegoTerminado = true;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!juegoTerminado) {
            delta = Math.min(delta, MAX_DELTA_SEGUNDOS);
            manejarInput();
            snakeLocal.actualizar(delta);
            enviarEstadoLocal();
            if (servidor) {
                actualizarFrutaServidor();
                evaluarFinPartidaServidor();
            } else {
                predecirComidaCliente();
            }
        }

        game.batch.begin();
        if (fondoJuego != null) {
            game.batch.draw(fondoJuego, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (texturaFruta != null) {
            game.batch.draw(texturaFruta, frutaPos.x, frutaPos.y, TAMANIO, TAMANIO);
        }
        snakeLocal.dibujar(game.batch);
        dibujarRemoto(game.batch);

        dibujarTextoConSombra("Online 2P | " + dificultadPartida.name(), 20, Gdx.graphics.getHeight() - 20, Color.GOLD);
        dibujarTextoConSombra("Jugador 1 puntos: " + puntajeServidor + " | Jugador 2 puntos: " + puntajeCliente, 20, Gdx.graphics.getHeight() - 55, Color.WHITE);
        dibujarTextoConSombra("Mueve: WASD/Flechas | ESC: menu", 20, Gdx.graphics.getHeight() - 90, new Color(0.80f, 0.95f, 1f, 1f));
        dibujarTextoConSombra(estadoConexion, 20, Gdx.graphics.getHeight() - 125, Color.ORANGE);
        if (juegoTerminado) {
            dibujarTextoConSombra("Fin de partida - ESPACIO para reintentar", 20, Gdx.graphics.getHeight() - 160, Color.SCARLET);
        }
        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            salirAlMenu();
        }
        if (juegoTerminado && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            salirAlMenu();
        }
    }


    private void dibujarTextoConSombra(String texto, float x, float y, Color color) {
        if (font == null) return;

        font.setColor(0f, 0f, 0f, 0.75f);
        font.draw(game.batch, texto, x + 2f, y - 2f);

        font.setColor(color);
        font.draw(game.batch, texto, x, y);
    }

    private void predecirComidaCliente() {
        if (frutaIdActual == ultimaFrutaComidaPredicha) return;
        Rectangle frutaRect = new Rectangle(frutaPos.x, frutaPos.y, TAMANIO, TAMANIO);
        Rectangle cabezaLocal = new Rectangle(snakeLocal.getCabeza().x, snakeLocal.getCabeza().y, TAMANIO, TAMANIO);
        if (cabezaLocal.overlaps(frutaRect)) {
            snakeLocal.comer();
            ultimaFrutaComidaPredicha = frutaIdActual;
        }
    }

    private void actualizarFrutaServidor() {
        Rectangle frutaRect = new Rectangle(frutaPos.x, frutaPos.y, TAMANIO, TAMANIO);

        Rectangle cabezaLocal = new Rectangle(snakeLocal.getCabeza().x, snakeLocal.getCabeza().y, TAMANIO, TAMANIO);
        if (cabezaLocal.overlaps(frutaRect)) {
            snakeLocal.comer();
            puntajeServidor += puntosPorFruta();
            enviarScore();
            regenerarFrutaServidor();
            return;
        }

        synchronized (cuerpoRemoto) {
            if (!cuerpoRemoto.isEmpty()) {
                Vector2 cabezaRemota = cuerpoRemoto.get(0);
                Rectangle cabezaRemotaRect = new Rectangle(cabezaRemota.x, cabezaRemota.y, TAMANIO, TAMANIO);
                if (cabezaRemotaRect.overlaps(frutaRect)) {
                    puntajeCliente += puntosPorFruta();
                    enviarScore();
                    regenerarFrutaServidor();
                }
            }
        }
    }

    private void regenerarFrutaServidor() {
        int columnas = Math.max(1, (int) (CAMPO_ANCHO / TAMANIO));
        int filas = Math.max(1, (int) (CAMPO_ALTO / TAMANIO));

        for (int intento = 0; intento < 120; intento++) {
            float x = MathUtils.random(0, columnas - 1) * TAMANIO;
            float y = MathUtils.random(0, filas - 1) * TAMANIO;
            Rectangle posible = new Rectangle(x, y, TAMANIO, TAMANIO);
            if (intersectaConSnake(posible, snakeLocal.getCuerpo(), 0)) {
                continue;
            }
            synchronized (cuerpoRemoto) {
                if (intersectaConSnake(posible, cuerpoRemoto, 0)) {
                    continue;
                }
            }
            frutaPos.set(x, y);
            frutaIdActual++;
            session.enviar("FRUIT:" + frutaIdActual + ":" + frutaPos.x + "," + frutaPos.y);
            return;
        }

        frutaPos.set(TAMANIO * 3f, TAMANIO * 3f);
        frutaIdActual++;
        session.enviar("FRUIT:" + frutaIdActual + ":" + frutaPos.x + "," + frutaPos.y);
    }

    private boolean intersectaConSnake(Rectangle rect, List<Vector2> cuerpo, int startIndex) {
        for (int i = startIndex; i < cuerpo.size(); i++) {
            Vector2 parte = cuerpo.get(i);
            Rectangle seg = new Rectangle(parte.x, parte.y, TAMANIO, TAMANIO);
            if (rect.overlaps(seg)) {
                return true;
            }
        }
        return false;
    }

    private void enviarScore() {
        session.enviar("SCORE:" + puntajeServidor + "," + puntajeCliente);
    }

    private int puntosPorFruta() {
        switch (dificultadPartida) {
            case FACIL:
                return 8;
            case DIFICIL:
                return 14;
            case NORMAL:
            default:
                return 10;
        }
    }

    private void manejarInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            snakeLocal.cambiarDireccion(Direccion.ARRIBA);
            session.enviar("DIR:UP");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            snakeLocal.cambiarDireccion(Direccion.ABAJO);
            session.enviar("DIR:DOWN");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            snakeLocal.cambiarDireccion(Direccion.IZQUIERDA);
            session.enviar("DIR:LEFT");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            snakeLocal.cambiarDireccion(Direccion.DERECHA);
            session.enviar("DIR:RIGHT");
        }
    }

    private void enviarEstadoLocal() {
        StringBuilder sb = new StringBuilder("STATE:");
        boolean first = true;
        for (Vector2 p : snakeLocal.getCuerpo()) {
            if (!first) sb.append(';');
            first = false;
            sb.append(p.x).append(',').append(p.y);
        }
        session.enviar(sb.toString());
    }

    private void evaluarFinPartidaServidor() {
        boolean servidorMuere = colisionConPropioCuerpo(snakeLocal.getCuerpo());

        boolean clienteMuere = false;
        Vector2 cabezaCliente = null;
        if (estadoRemotoRecibido) {
            synchronized (cuerpoRemoto) {
                if (!cuerpoRemoto.isEmpty()) {
                    cabezaCliente = cuerpoRemoto.get(0);
                    if (cabezaCliente != null && Float.isFinite(cabezaCliente.x) && Float.isFinite(cabezaCliente.y)) {
                        clienteMuere = colisionConPropioCuerpo(cuerpoRemoto);
                    }
                }
            }
        }

        if (cabezaCliente != null) {
            Rectangle headServidor = new Rectangle(snakeLocal.getCabeza().x, snakeLocal.getCabeza().y, TAMANIO, TAMANIO);
            Rectangle headCliente = new Rectangle(cabezaCliente.x, cabezaCliente.y, TAMANIO, TAMANIO);

            if (headServidor.overlaps(headCliente)) {
                finalizarPorPuntaje("Choque de cabezas");
                return;
            }

            synchronized (cuerpoRemoto) {
                Rectangle bodyServidorRect = new Rectangle(snakeLocal.getCabeza().x, snakeLocal.getCabeza().y, TAMANIO, TAMANIO);
                if (intersectaConSnake(bodyServidorRect, cuerpoRemoto, 1)) {
                    finalizarConGanador("CLIENTE", "El cliente te comió");
                    return;
                }

                Rectangle bodyClienteRect = new Rectangle(cabezaCliente.x, cabezaCliente.y, TAMANIO, TAMANIO);
                if (intersectaConSnake(bodyClienteRect, snakeLocal.getCuerpo(), 1)) {
                    finalizarConGanador("SERVIDOR", "Comiste al cliente");
                    return;
                }
            }
        }

        if (servidorMuere && clienteMuere) {
            finalizarPorPuntaje("Ambos colisionaron");
            return;
        }
        if (servidorMuere) {
            finalizarConGanador("CLIENTE", "Servidor colisionó");
            return;
        }
        if (clienteMuere) {
            finalizarConGanador("SERVIDOR", "Cliente colisionó");
        }
    }

    private boolean colisionConPropioCuerpo(List<Vector2> cuerpo) {
        if (cuerpo.size() < 8) return false;
        Vector2 cabeza = cuerpo.get(0);
        Rectangle head = new Rectangle(cabeza.x, cabeza.y, TAMANIO, TAMANIO);
        for (int i = 5; i < cuerpo.size(); i++) {
            Vector2 seg = cuerpo.get(i);
            Rectangle other = new Rectangle(seg.x, seg.y, TAMANIO, TAMANIO);
            if (head.overlaps(other)) {
                return true;
            }
        }
        return false;
    }

    private void finalizarPorPuntaje(String motivo) {
        if (puntajeServidor > puntajeCliente) {
            finalizarConGanador("SERVIDOR", motivo + " (gana por puntaje)");
        } else if (puntajeCliente > puntajeServidor) {
            finalizarConGanador("CLIENTE", motivo + " (gana por puntaje)");
        } else {
            finalizarConGanador("EMPATE", motivo + " (empate)");
        }
    }

    private void finalizarConGanador(String ganador, String motivo) {
        if (juegoTerminado) return;
        juegoTerminado = true;

        if ("EMPATE".equals(ganador)) {
            estadoConexion = "Empate - " + motivo;
        } else {
            boolean gane = (servidor && "SERVIDOR".equals(ganador)) || (!servidor && "CLIENTE".equals(ganador));
            estadoConexion = (gane ? "Ganaste" : "Perdiste") + " - " + motivo;
        }

        session.enviar("END:" + ganador + ":" + motivo + ":" + puntajeServidor + ":" + puntajeCliente);
    }

    private void dibujarRemoto(SpriteBatch batch) {
        synchronized (cuerpoRemoto) {
            for (Vector2 p : cuerpoRemoto) {
                batch.draw(texturaRemota, p.x, p.y, TAMANIO, TAMANIO);
            }
        }
    }

    private void salirAlMenu() {
        if (enMenu) {
            return;
        }
        enMenu = true;
        corriendo = false;
        session.cerrar();
        game.mostrarMenuOnline();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        corriendo = false;
        if (snakeLocal != null) {
            snakeLocal.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (fondoJuego != null) {
            fondoJuego.dispose();
            fondoJuego = null;
        }
        if (texturaRemota != null) {
            texturaRemota.dispose();
        }
        if (texturaFruta != null) {
            texturaFruta.dispose();
        }
        if (session != null) {
            session.cerrar();
        }
    }
}
