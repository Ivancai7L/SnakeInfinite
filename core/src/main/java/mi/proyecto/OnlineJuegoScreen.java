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


//Maneja todo lo que pasa durante la partida: dibuja las dos serpientes, la fruta y las piedras
//envía la posición local al rival 20 veces por segundo, y si es el servidor también decide colisiones, puntaje y cuándo termina la partida.
// Al finalizar muestra el perdedor o ganador según el resultado.

public class OnlineJuegoScreen implements Screen {

    private static final float VELOCIDAD_BASE = 60f;
    private static final float TAMANIO = 25f;
    private static final float INTERVALO_ENVIO_ESTADO = 1f / 20f;
    private static final float HITBOX_INSET_SNAKE = 4f;
    private static final float HITBOX_INSET_FRUTA = 3f;
    private static final float HITBOX_INSET_PIEDRA = 2f;

    private final MiJuegoPrincipal game;
    private final OnlineSession session;
    private final boolean servidor;
    private final Dificultad dificultadPartida;

    private Snake snakeLocal;
    private final List<Vector2> cuerpoRemoto = new ArrayList<>();
    private final List<Vector2> piedrasOnline = new ArrayList<>();
    private BitmapFont font;
    private Texture fondoJuego;
    private Texture texturaRemota;
    private Texture texturaPiedra;
    private Texture texturaGameOver;
    private Texture texturaWinner;
    private boolean localGano;
    private Texture texturaFrutaManzana;
    private Texture texturaFrutaBanana;
    private Texture texturaFrutaPera;
    private final Vector2 frutaPos = new Vector2(300, 300);
    private Frutas.TipoFruta frutaTipoActual = Frutas.TipoFruta.MANZANA;
    private int frutaIdActual;
    private int ultimaFrutaComidaPredicha = -1;

    private volatile boolean corriendo;
    private volatile String estadoConexion = "Conectado";
    private boolean juegoTerminado;
    private int puntajeServidor;
    private int puntajeCliente;
    private int siguientePiedraPuntuacion;
    private int intervaloPiedraPuntuacion;
    private int maxPiedras;
    private float acumuladorEnvioEstado;
    private volatile boolean cierreSesionSolicitado;
    private boolean reinicioSolicitado;
    private boolean rivalListoParaReiniciar;

    public OnlineJuegoScreen(MiJuegoPrincipal game, OnlineSession session, boolean servidor, Dificultad dificultadPartida) {
        this.game = game;
        this.session = session;
        this.servidor = servidor;
        this.dificultadPartida = dificultadPartida == null ? Dificultad.NORMAL : dificultadPartida;
    }

    @Override
    public void show() {
        float velocidad = VELOCIDAD_BASE * dificultadPartida.getVelocidad();
        String texturaLocal = servidor ? "Snakeimg.png" : "Snakeimg2.png";
        snakeLocal = new Snake(velocidad, TAMANIO, texturaLocal);
        posicionarSnakeInicial();

        fondoJuego = cargarTexturaConFallback("tierrafondo.png", 28, 45, 30);
        texturaRemota = servidor
            ? cargarTexturaConFallback("Snakeimg2.png", 70, 160, 255)
            : cargarTexturaConFallback("Snakeimg.png", 40, 220, 40);
        texturaPiedra = cargarTexturaConFallback("piedra.png", 120, 120, 120);
        texturaGameOver = cargarTexturaConFallback("gameover.png", 180, 30, 30);
        texturaWinner = cargarTexturaConFallback("winner.png", 30, 180, 60);
        texturaFrutaManzana = cargarTexturaConFallback("Frutaimg.png", 220, 50, 50);
        texturaFrutaBanana = cargarTexturaConFallback("banana.png", 255, 225, 60);
        texturaFrutaPera = cargarTexturaConFallback("pera.png", 120, 220, 90);

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.ORANGE);

        configurarDificultadPiedras();

        if (servidor) {
            regenerarFrutaServidor();
            enviarScore();
            enviarPiedras();
        }

        reinicioSolicitado = false;
        rivalListoParaReiniciar = false;
        iniciarReceptor();
    }

    private void configurarDificultadPiedras() {
        switch (dificultadPartida) {
            case FACIL:
                intervaloPiedraPuntuacion = 40;
                maxPiedras = 3;
                break;
            case DIFICIL:
                intervaloPiedraPuntuacion = 20;
                maxPiedras = 7;
                break;
            case NORMAL:
            default:
                intervaloPiedraPuntuacion = 30;
                maxPiedras = 5;
                break;
        }
        siguientePiedraPuntuacion = intervaloPiedraPuntuacion;
    }

    private void posicionarSnakeInicial() {
        if (snakeLocal == null || snakeLocal.getCuerpo().isEmpty()) {
            return;
        }
        float y = Gdx.graphics.getHeight() / 2f;
        float x = servidor ? Gdx.graphics.getWidth() * 0.25f : Gdx.graphics.getWidth() * 0.75f;
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
                        corriendo = false;
                        break;
                    }
                    procesarMensaje(linea);
                } catch (IOException e) {
                    estadoConexion = "Error de red";
                    juegoTerminado = true;
                    corriendo = false;
                    break;
                }
            }
        }, "online-recv-thread");
        t.setDaemon(true);
        t.start();
    }

    private void procesarMensaje(String msg) {
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
            }
            return;
        }

        if (msg.startsWith("FRUIT:")) {
            String data = msg.substring(6);
            String[] parts = data.split(":", 3);
            if (parts.length == 3) {
                try {
                    frutaIdActual = Integer.parseInt(parts[0]);
                } catch (NumberFormatException ignored) {
                }
                try {
                    frutaTipoActual = Frutas.TipoFruta.valueOf(parts[1]);
                } catch (IllegalArgumentException ignored) {
                    frutaTipoActual = Frutas.TipoFruta.MANZANA;
                }
                data = parts[2];
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

        if (msg.startsWith("STONES:")) {
            String data = msg.substring(7);
            List<Vector2> nuevas = new ArrayList<>();
            if (!data.isEmpty()) {
                String[] stones = data.split(";");
                for (String stone : stones) {
                    String[] xy = stone.split(",");
                    if (xy.length == 2) {
                        try {
                            nuevas.add(new Vector2(Float.parseFloat(xy[0]), Float.parseFloat(xy[1])));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            synchronized (piedrasOnline) {
                piedrasOnline.clear();
                piedrasOnline.addAll(nuevas);
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

        if (msg.startsWith("RESTART_REQ:")) {
            if (servidor && juegoTerminado) {
                rivalListoParaReiniciar = true;
                if (reinicioSolicitado) {
                    Gdx.app.postRunnable(this::reiniciarPartidaOnline);
                    session.enviar("RESTART");
                } else {
                    estadoConexion = "El jugador 2 está listo. Presioná ESPACIO para reiniciar";
                }
            }
            return;
        }

        if (msg.equals("RESTART")) {
            Gdx.app.postRunnable(this::reiniciarPartidaOnline);
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
                    localGano = false;
                } else {
                    localGano = (servidor && "SERVIDOR".equals(ganador)) || (!servidor && "CLIENTE".equals(ganador));
                    estadoConexion = (localGano ? "Ganaste" : "Perdiste") + " - " + motivo;
                }
            }
            juegoTerminado = true;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!juegoTerminado) {
            manejarInput();
            snakeLocal.actualizar(delta);
            acumuladorEnvioEstado += delta;
            if (acumuladorEnvioEstado >= INTERVALO_ENVIO_ESTADO) {
                enviarEstadoLocal();
                acumuladorEnvioEstado -= INTERVALO_ENVIO_ESTADO;
            }
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
        dibujarPiedras(game.batch);
        dibujarFruta(game.batch);
        snakeLocal.dibujar(game.batch);
        dibujarRemoto(game.batch);

        String rol = servidor ? "SERVIDOR" : "CLIENTE";
        int miPuntaje = servidor ? puntajeServidor : puntajeCliente;
        int puntajeRival = servidor ? puntajeCliente : puntajeServidor;

        font.draw(game.batch, "Online 2P - " + rol + " | " + dificultadPartida.name(), 20, Gdx.graphics.getHeight() - 20);
        font.draw(game.batch, "Mi puntaje: " + miPuntaje + " | Rival: " + puntajeRival, 20, Gdx.graphics.getHeight() - 55);
        font.draw(game.batch, "Mueve: WASD/Flechas | ESC: menu", 20, Gdx.graphics.getHeight() - 90);

        if (juegoTerminado) {
            int screenW = Gdx.graphics.getWidth();
            int screenH = Gdx.graphics.getHeight();
            float imgW = screenW * 0.6f;
            float imgH = screenH * 0.35f;
            float imgX = (screenW - imgW) / 2f;
            float imgY = (screenH - imgH) / 2f + 60f;

            Texture imagenFin = localGano ? texturaWinner : texturaGameOver;
            if (imagenFin != null) {
                game.batch.draw(imagenFin, imgX, imgY, imgW, imgH);
            }

            font.draw(game.batch, estadoConexion, 20, imgY - 10f);
            font.draw(game.batch, "ESPACIO: volver a jugar  |  ESC: salir al menu", 20, imgY - 50f);
        }

        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            salirAlMenu();
        }
        if (juegoTerminado && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            manejarReinicioConEspacio();
        }
    }

    private void dibujarFruta(SpriteBatch batch) {
        Texture fruta = texturaFrutaManzana;
        if (frutaTipoActual == Frutas.TipoFruta.BANANA) {
            fruta = texturaFrutaBanana != null ? texturaFrutaBanana : texturaFrutaManzana;
        } else if (frutaTipoActual == Frutas.TipoFruta.PERA) {
            fruta = texturaFrutaPera != null ? texturaFrutaPera : texturaFrutaManzana;
        }
        float tam = frutaTipoActual == Frutas.TipoFruta.PERA ? TAMANIO * 1.2f : TAMANIO;
        batch.draw(fruta, frutaPos.x, frutaPos.y, tam, tam);
    }

    private void dibujarPiedras(SpriteBatch batch) {
        synchronized (piedrasOnline) {
            for (Vector2 piedraPos : piedrasOnline) {
                batch.draw(texturaPiedra, piedraPos.x, piedraPos.y, TAMANIO, TAMANIO);
            }
        }
    }

    private void predecirComidaCliente() {
        if (frutaIdActual == ultimaFrutaComidaPredicha) {
            return;
        }
        Rectangle frutaRect = rectFrutaActual();
        Rectangle cabezaLocal = crearHitboxSnake(snakeLocal.getCabeza());
        if (cabezaLocal.overlaps(frutaRect)) {
            snakeLocal.comer();
            ultimaFrutaComidaPredicha = frutaIdActual;
        }
    }

    private Rectangle rectFrutaActual() {
        float tam = frutaTipoActual == Frutas.TipoFruta.PERA ? TAMANIO * 1.2f : TAMANIO;
        float inset = Math.min(HITBOX_INSET_FRUTA, tam * 0.25f);
        return new Rectangle(frutaPos.x + inset, frutaPos.y + inset, tam - inset * 2f, tam - inset * 2f);
    }

    private void actualizarFrutaServidor() {
        Rectangle frutaRect = rectFrutaActual();

        Rectangle cabezaLocal = crearHitboxSnake(snakeLocal.getCabeza());
        if (cabezaLocal.overlaps(frutaRect)) {
            snakeLocal.comer();
            puntajeServidor += puntosPorFruta(frutaTipoActual);
            enviarScore();
            regenerarFrutaServidor();
            actualizarPiedrasServidor();
            return;
        }

        synchronized (cuerpoRemoto) {
            if (!cuerpoRemoto.isEmpty()) {
                Vector2 cabezaRemota = cuerpoRemoto.get(0);
                Rectangle cabezaRemotaRect = crearHitboxSnake(cabezaRemota);
                if (cabezaRemotaRect.overlaps(frutaRect)) {
                    puntajeCliente += puntosPorFruta(frutaTipoActual);
                    enviarScore();
                    regenerarFrutaServidor();
                    actualizarPiedrasServidor();
                }
            }
        }
    }

    private void actualizarPiedrasServidor() {
        if (!servidor) {
            return;
        }
        int totalPuntaje = puntajeServidor + puntajeCliente;
        while (piedrasOnline.size() < maxPiedras && totalPuntaje >= siguientePiedraPuntuacion) {
            Vector2 nueva = generarPosicionPiedraLibre();
            if (nueva == null) {
                break;
            }
            piedrasOnline.add(nueva);
            siguientePiedraPuntuacion += intervaloPiedraPuntuacion;
        }
        enviarPiedras();
    }

    private Vector2 generarPosicionPiedraLibre() {
        int columnas = Math.max(1, (int) (Gdx.graphics.getWidth() / TAMANIO));
        int filas = Math.max(1, (int) (Gdx.graphics.getHeight() / TAMANIO));

        for (int intento = 0; intento < 120; intento++) {
            float x = MathUtils.random(0, columnas - 1) * TAMANIO;
            float y = MathUtils.random(0, filas - 1) * TAMANIO;
            Rectangle posible = new Rectangle(x, y, TAMANIO, TAMANIO);

            if (posible.overlaps(rectFrutaActual())) {
                continue;
            }
            if (intersectaConSnake(posible, snakeLocal.getCuerpo(), 0)) {
                continue;
            }
            synchronized (cuerpoRemoto) {
                if (intersectaConSnake(posible, cuerpoRemoto, 0)) {
                    continue;
                }
            }
            synchronized (piedrasOnline) {
                if (intersectaConPiedras(posible)) {
                    continue;
                }
            }
            return new Vector2(x, y);
        }
        return null;
    }

    private boolean intersectaConPiedras(Rectangle rect) {
        for (Vector2 pos : piedrasOnline) {
            Rectangle piedraRect = new Rectangle(pos.x, pos.y, TAMANIO, TAMANIO);
            if (rect.overlaps(piedraRect)) {
                return true;
            }
        }
        return false;
    }

    private void enviarPiedras() {
        if (!servidor) {
            return;
        }
        StringBuilder sb = new StringBuilder("STONES:");
        synchronized (piedrasOnline) {
            for (int i = 0; i < piedrasOnline.size(); i++) {
                Vector2 p = piedrasOnline.get(i);
                if (i > 0) {
                    sb.append(';');
                }
                sb.append(p.x).append(',').append(p.y);
            }
        }
        session.enviar(sb.toString());
    }

    private void regenerarFrutaServidor() {
        int columnas = Math.max(1, (int) (Gdx.graphics.getWidth() / TAMANIO));
        int filas = Math.max(1, (int) (Gdx.graphics.getHeight() / TAMANIO));

        for (int intento = 0; intento < 120; intento++) {
            float x = MathUtils.random(0, columnas - 1) * TAMANIO;
            float y = MathUtils.random(0, filas - 1) * TAMANIO;
            Frutas.TipoFruta tipo = sortearTipoFruta();
            Rectangle posible = rectFruta(tipo, x, y);
            if (intersectaConSnake(posible, snakeLocal.getCuerpo(), 0)) {
                continue;
            }
            synchronized (cuerpoRemoto) {
                if (intersectaConSnake(posible, cuerpoRemoto, 0)) {
                    continue;
                }
            }
            synchronized (piedrasOnline) {
                if (intersectaConPiedras(posible)) {
                    continue;
                }
            }
            frutaPos.set(x, y);
            frutaTipoActual = tipo;
            frutaIdActual++;
            session.enviar("FRUIT:" + frutaIdActual + ":" + frutaTipoActual.name() + ":" + frutaPos.x + "," + frutaPos.y);
            return;
        }

        frutaPos.set(TAMANIO * 3f, TAMANIO * 3f);
        frutaTipoActual = Frutas.TipoFruta.MANZANA;
        frutaIdActual++;
        session.enviar("FRUIT:" + frutaIdActual + ":" + frutaTipoActual.name() + ":" + frutaPos.x + "," + frutaPos.y);
    }

    private Frutas.TipoFruta sortearTipoFruta() {
        float r = MathUtils.random();
        switch (dificultadPartida) {
            case FACIL:
                if (r < 0.25f) return Frutas.TipoFruta.PERA;
                if (r < 0.55f) return Frutas.TipoFruta.BANANA;
                return Frutas.TipoFruta.MANZANA;
            case DIFICIL:
                if (r < 0.20f) return Frutas.TipoFruta.PERA;
                if (r < 0.45f) return Frutas.TipoFruta.BANANA;
                return Frutas.TipoFruta.MANZANA;
            case NORMAL:
            default:
                if (r < 0.15f) return Frutas.TipoFruta.PERA;
                if (r < 0.35f) return Frutas.TipoFruta.BANANA;
                return Frutas.TipoFruta.MANZANA;
        }
    }

    private Rectangle rectFruta(Frutas.TipoFruta tipo, float x, float y) {
        float tam = tipo == Frutas.TipoFruta.PERA ? TAMANIO * 1.2f : TAMANIO;
        float inset = Math.min(HITBOX_INSET_FRUTA, tam * 0.25f);
        return new Rectangle(x + inset, y + inset, tam - inset * 2f, tam - inset * 2f);
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

    private int puntosPorFruta(Frutas.TipoFruta tipo) {
        switch (tipo) {
            case BANANA:
                return 15;
            case PERA:
                return 20;
            case MANZANA:
            default:
                return 10;
        }
    }

    private void manejarInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            snakeLocal.cambiarDireccion(Direccion.ARRIBA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            snakeLocal.cambiarDireccion(Direccion.ABAJO);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            snakeLocal.cambiarDireccion(Direccion.IZQUIERDA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            snakeLocal.cambiarDireccion(Direccion.DERECHA);
        }
    }

    private void enviarEstadoLocal() {
        StringBuilder sb = new StringBuilder("STATE:");
        boolean first = true;
        for (Vector2 p : snakeLocal.getCuerpo()) {
            if (!first) {
                sb.append(';');
            }
            first = false;
            sb.append(p.x).append(',').append(p.y);
        }
        session.enviar(sb.toString());
    }

    private void evaluarFinPartidaServidor() {
        boolean servidorMuere = tocaBorde(snakeLocal.getCabeza())
            || colisionConPropioCuerpo(snakeLocal.getCuerpo())
            || colisionConPiedras(snakeLocal.getCabeza());

        boolean clienteMuere = false;
        Vector2 cabezaCliente = null;
        synchronized (cuerpoRemoto) {
            if (!cuerpoRemoto.isEmpty()) {
                cabezaCliente = cuerpoRemoto.get(0);
                clienteMuere = tocaBorde(cabezaCliente)
                    || colisionConPropioCuerpo(cuerpoRemoto)
                    || colisionConPiedras(cabezaCliente);
            }
        }

        if (cabezaCliente != null) {
            Rectangle headServidor = crearHitboxSnake(snakeLocal.getCabeza());
            Rectangle headCliente = crearHitboxSnake(cabezaCliente);

            if (headServidor.overlaps(headCliente)) {
                finalizarPorPuntaje("Choque de cabezas");
                return;
            }

            synchronized (cuerpoRemoto) {
                Rectangle bodyServidorRect = crearHitboxSnake(snakeLocal.getCabeza());
                if (intersectaConSnake(bodyServidorRect, cuerpoRemoto, 1)) {
                    finalizarConGanador("CLIENTE", "Colisión de serpientes");
                    return;
                }

                Rectangle bodyClienteRect = crearHitboxSnake(cabezaCliente);
                if (intersectaConSnake(bodyClienteRect, snakeLocal.getCuerpo(), 1)) {
                    finalizarConGanador("SERVIDOR", "Colisión de serpientes");
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

    private boolean colisionConPiedras(Vector2 cabeza) {
        Rectangle headRect = crearHitboxSnake(cabeza);
        synchronized (piedrasOnline) {
            for (Vector2 piedra : piedrasOnline) {
                Rectangle piedraRect = crearHitboxPiedra(piedra);
                if (headRect.overlaps(piedraRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tocaBorde(Vector2 cabeza) {
        return cabeza.x < 0
            || cabeza.y < 0
            || cabeza.x > Gdx.graphics.getWidth() - TAMANIO
            || cabeza.y > Gdx.graphics.getHeight() - TAMANIO;
    }

    private boolean colisionConPropioCuerpo(List<Vector2> cuerpo) {
        if (cuerpo.size() < 8) {
            return false;
        }
        Vector2 cabeza = cuerpo.get(0);
        Rectangle head = crearHitboxSnake(cabeza);
        for (int i = 5; i < cuerpo.size(); i++) {
            Vector2 seg = cuerpo.get(i);
            Rectangle other = crearHitboxSnake(seg);
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
        if (juegoTerminado) {
            return;
        }
        juegoTerminado = true;

        if ("EMPATE".equals(ganador)) {
            estadoConexion = "Empate - " + motivo;
            localGano = false;
        } else {
            localGano = (servidor && "SERVIDOR".equals(ganador)) || (!servidor && "CLIENTE".equals(ganador));
            estadoConexion = (localGano ? "Ganaste" : "Perdiste") + " - " + motivo;
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

    private Rectangle crearHitboxSnake(Vector2 posicion) {
        float inset = Math.min(HITBOX_INSET_SNAKE, TAMANIO * 0.25f);
        return new Rectangle(posicion.x + inset, posicion.y + inset, TAMANIO - inset * 2f, TAMANIO - inset * 2f);
    }

    private Rectangle crearHitboxPiedra(Vector2 posicion) {
        float inset = Math.min(HITBOX_INSET_PIEDRA, TAMANIO * 0.25f);
        return new Rectangle(posicion.x + inset, posicion.y + inset, TAMANIO - inset * 2f, TAMANIO - inset * 2f);
    }

    private void manejarReinicioConEspacio() {
        if (reinicioSolicitado) {
            return;
        }
        reinicioSolicitado = true;

        if (servidor) {
            if (rivalListoParaReiniciar) {
                reiniciarPartidaOnline();
                session.enviar("RESTART");
            } else {
                estadoConexion = "Esperando al jugador 2 para reiniciar";
            }
            return;
        }

        estadoConexion = "Esperando al jugador 1 para reiniciar";
        session.enviar("RESTART_REQ:CLIENTE");
    }

    private void reiniciarPartidaOnline() {
        float velocidad = VELOCIDAD_BASE * dificultadPartida.getVelocidad();
        if (snakeLocal != null) {
            snakeLocal.dispose();
        }
        String texturaLocal = servidor ? "Snakeimg.png" : "Snakeimg2.png";
        snakeLocal = new Snake(velocidad, TAMANIO, texturaLocal);
        posicionarSnakeInicial();

        synchronized (cuerpoRemoto) {
            cuerpoRemoto.clear();
        }
        if (servidor) {
            puntajeServidor = 0;
            puntajeCliente = 0;
            piedrasOnline.clear();
            siguientePiedraPuntuacion = intervaloPiedraPuntuacion;
            regenerarFrutaServidor();
            enviarPiedras();
            enviarScore();
        }

        acumuladorEnvioEstado = 0f;
        juegoTerminado = false;
        localGano = false;
        estadoConexion = "Conectado";
        reinicioSolicitado = false;
        rivalListoParaReiniciar = false;
    }

    private void salirAlMenu() {
        corriendo = false;
        cerrarSesionAsync();
        game.mostrarMenu();
    }

    private void cerrarSesionAsync() {
        if (cierreSesionSolicitado) {
            return;
        }
        cierreSesionSolicitado = true;
        Thread cierre = new Thread(() -> {
            try {
                session.cerrar();
            } catch (Exception ignored) {
            }
        }, "online-session-close-thread");
        cierre.setDaemon(true);
        cierre.start();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        corriendo = false;
        if (snakeLocal != null) {
            snakeLocal.dispose();
            snakeLocal = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
        if (fondoJuego != null) {
            fondoJuego.dispose();
            fondoJuego = null;
        }
        if (texturaRemota != null) {
            texturaRemota.dispose();
            texturaRemota = null;
        }
        if (texturaPiedra != null) {
            texturaPiedra.dispose();
            texturaPiedra = null;
        }
        if (texturaGameOver != null) {
            texturaGameOver.dispose();
            texturaGameOver = null;
        }
        if (texturaWinner != null) {
            texturaWinner.dispose();
            texturaWinner = null;
        }
        if (texturaFrutaManzana != null) {
            texturaFrutaManzana.dispose();
            texturaFrutaManzana = null;
        }
        if (texturaFrutaBanana != null) {
            texturaFrutaBanana.dispose();
            texturaFrutaBanana = null;
        }
        if (texturaFrutaPera != null) {
            texturaFrutaPera.dispose();
            texturaFrutaPera = null;
        }
        cerrarSesionAsync();
    }
}
