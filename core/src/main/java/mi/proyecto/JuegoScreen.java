package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.List;

public class JuegoScreen implements Screen {

    private static final float VELOCIDAD_BASE = 50f;
    private static final float TAMANIO_SERPIENTE = 25f;
    private static final float ESCALA_FONT = 2f;
    private static final float DURACION_MULTIPLICADOR = 8f;

    private final MiJuegoPrincipal juego;
    private Snake snake;
    private Frutas fruta;
    private final List<Piedra> piedras = new ArrayList<>();
    private BitmapFont font;
    private Texture imagenGameOver;
    private Texture fondoJuego;
    private Texture fondoGameOver;
    private Texture texturaBarraVolumen;
    private Texture texturaPerillaVolumen;
    private Stage pausaStage;
    private Slider sliderVolumenPausa;
    private boolean inputPausaActivo;
    private Sound sonidoGameOver;

    private int puntuacion;
    private int puntuacionRecord;
    private boolean juegoTerminado;
    private boolean juegoPausado;
    private int multiplicadorPuntuacion;
    private float tiempoMultiplicador;
    private Preferences preferencias;
    private int siguientePiedraPuntuacion;
    private int intervaloPiedraPuntuacion;
    private int maxPiedras;
    private boolean sonidoGameOverReproducido;

    public JuegoScreen(MiJuegoPrincipal juego) {
        System.out.println("JuegoScreen: Constructor llamado");
        this.juego = juego;
        System.out.println("JuegoScreen: Constructor completado");
    }

    @Override
    public void show() {
        try {
            if (juego.dificultadSeleccionada == null) {
                juego.dificultadSeleccionada = Dificultad.NORMAL;
                System.out.println("Advertencia: No se seleccionó dificultad. Usando NORMAL por defecto.");
            }

            if (fondoJuego == null) {
                fondoJuego = cargarTexturaConFallback("tierrafondo.png", 28, 45, 30);
            }

            float velocidad = juego.dificultadSeleccionada.getVelocidad() * VELOCIDAD_BASE;

            preferencias = Gdx.app.getPreferences("snake-record");
            puntuacionRecord = preferencias.getInteger("puntuacionRecord", 0);

            inicializarPartida(velocidad);

            System.out.println("JuegoScreen inicializado correctamente");

        } catch (Exception e) {
            System.err.println("ERROR al inicializar JuegoScreen:");
            e.printStackTrace();
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    juego.mostrarMenu();
                }
            });
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!juegoTerminado && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (juegoPausado) {
                juego.mostrarMenu();
            } else {
                juegoPausado = true;
            }
        }

        if (!juegoTerminado && Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            juegoPausado = !juegoPausado;
        }

        if (!juegoTerminado && juegoPausado) {
            manejarVolumenEnPausa();
        }

        if (!juegoTerminado && !juegoPausado && snake != null) {
            manejarInput();
            snake.actualizar(delta);
            verificarColisionConFruta();
            verificarColisionConPiedra();
            verificarColisionConBordes();
            verificarColisionConCuerpo();
        }

        if (!juegoTerminado && !juegoPausado && multiplicadorPuntuacion > 1) {
            tiempoMultiplicador -= delta;
            if (tiempoMultiplicador <= 0f) {
                multiplicadorPuntuacion = 1;
                tiempoMultiplicador = 0f;
            }
        }

        juego.batch.begin();

        if (fondoJuego != null) {
            juego.batch.draw(fondoJuego, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        if (fruta != null) {
            fruta.dibujar(juego.batch);
        }
        for (Piedra piedra : piedras) {
            piedra.dibujar(juego.batch);
        }

        if (snake != null) {
            snake.dibujar(juego.batch);
        }

        if (font != null) {
            font.draw(juego.batch, "Puntuación: " + puntuacion, 20, Gdx.graphics.getHeight() - 20);
            font.draw(juego.batch, "Record: " + puntuacionRecord, 20, Gdx.graphics.getHeight() - 50);
            font.draw(juego.batch, "ESC o P para pausar", 20, Gdx.graphics.getHeight() - 80);
            if (multiplicadorPuntuacion > 1) {
                int segundosRestantes = (int) Math.ceil(tiempoMultiplicador);
                font.draw(juego.batch, "x2: " + segundosRestantes + "s", 20, Gdx.graphics.getHeight() - 110);
            }
        }

        if (juegoTerminado && font != null) {
            if (fondoGameOver != null) {
                juego.batch.draw(fondoGameOver, 0, 0,
                    Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
            if (imagenGameOver != null) {
                float ancho = Gdx.graphics.getWidth() * 0.62f;
                float alto = Gdx.graphics.getHeight() * 0.36f;
                float x = (Gdx.graphics.getWidth() - ancho) / 2f;
                float y = (Gdx.graphics.getHeight() - alto) / 2f;
                juego.batch.draw(imagenGameOver, x, y, ancho, alto);
            }
            font.draw(juego.batch, "Puntuación final: " + puntuacion,
                Gdx.graphics.getWidth() / 2f - 220,
                Gdx.graphics.getHeight() / 2f - 80f);
            font.draw(juego.batch, "Presione ESPACIO para reiniciar o ESC para volver al menu",
                Gdx.graphics.getWidth() / 2f - 520,
                Gdx.graphics.getHeight() / 2f - 120f);
        }

        if (juegoPausado && !juegoTerminado && font != null) {
            float centroX = Gdx.graphics.getWidth() / 2f;
            float centroY = Gdx.graphics.getHeight() / 2f;
            dibujarTextoCentrado("PAUSA - Presiona P para continuar", centroX, centroY + 80f);
            dibujarTextoCentrado("ESC para volver al menu", centroX, centroY + 40f);
            dibujarTextoCentrado(
                "Volumen: " + (int) (juego.getVolumenMusica() * 100) + "%"
                    + (juego.isMusicaMute() ? " (MUTE)" : ""),
                centroX,
                centroY
            );
            dibujarTextoCentrado("M para mutear / desmutear", centroX, centroY - 95f);
        }

        juego.batch.end();

        if (juegoPausado && !juegoTerminado) {
            activarInputPausa();
            if (sliderVolumenPausa != null && Math.abs(sliderVolumenPausa.getValue() - juego.getVolumenMusica()) > 0.001f) {
                sliderVolumenPausa.setValue(juego.getVolumenMusica());
            }
            if (pausaStage != null) {
                pausaStage.act(delta);
                pausaStage.draw();
            }
        } else {
            desactivarInputPausa();
        }

        if (juegoTerminado && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            reiniciarPartida();
        }
        if (juegoTerminado && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            juego.mostrarMenu();
        }
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

    private void dibujarTextoCentrado(String texto, float centroX, float y) {
        if (font == null) {
            return;
        }
        GlyphLayout layout = new GlyphLayout(font, texto);
        font.draw(juego.batch, texto, centroX - (layout.width / 2f), y);
    }

    private void inicializarPartida(float velocidad) {
        liberarRecursosPartida();

        snake = new Snake(velocidad, TAMANIO_SERPIENTE);
        System.out.println("Snake creada con velocidad: " + velocidad);

        if (font == null) {
            font = new BitmapFont();
            font.setColor(Color.ORANGE);
            font.getData().setScale(ESCALA_FONT);
        }

        puntuacion = 0;
        juegoTerminado = false;
        juegoPausado = false;
        multiplicadorPuntuacion = 1;
        tiempoMultiplicador = 0f;
        configurarDificultad();

        fruta = new Frutas();
        fruta.actualizarProbabilidades(juego.dificultadSeleccionada);
        fruta.actualizarTipoPorPuntuacion(puntuacion);
        regenerarFruta();
        System.out.println("Frutas creadas");
        piedras.clear();
        sonidoGameOverReproducido = false;

        if (imagenGameOver == null) {
            try {
                if (Gdx.files.internal("gameover.png").exists()) {
                    imagenGameOver = new Texture("gameover.png");
                } else {
                    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                    pixmap.setColor(0.55f, 0f, 0f, 0.75f);
                    pixmap.fill();
                    imagenGameOver = new Texture(pixmap);
                    pixmap.dispose();
                }
            } catch (Exception e) {
                System.out.println("Error al cargar imagen de game over: " + e.getMessage());
            }
        }

        if (fondoGameOver == null) {
            Pixmap pixmapFondo = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmapFondo.setColor(0f, 0f, 0f, 0.6f);
            pixmapFondo.fill();
            fondoGameOver = new Texture(pixmapFondo);
            pixmapFondo.dispose();
        }

        if (texturaBarraVolumen == null) {
            if (Gdx.files.internal("barra.png").exists()) {
                texturaBarraVolumen = new Texture("barra.png");
            } else {
                Pixmap pixmapBarra = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmapBarra.setColor(0.35f, 0.35f, 0.35f, 1f);
                pixmapBarra.fill();
                texturaBarraVolumen = new Texture(pixmapBarra);
                pixmapBarra.dispose();
            }
        }

        if (texturaPerillaVolumen == null) {
            if (Gdx.files.internal("perilla.png").exists()) {
                texturaPerillaVolumen = new Texture("perilla.png");
            } else {
                Pixmap pixmapPerilla = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmapPerilla.setColor(0.85f, 0.85f, 0.85f, 1f);
                pixmapPerilla.fill();
                texturaPerillaVolumen = new Texture(pixmapPerilla);
                pixmapPerilla.dispose();
            }
        }

        if (pausaStage == null) {
            pausaStage = new Stage(new ScreenViewport());
        }

        if (sliderVolumenPausa == null && texturaBarraVolumen != null && texturaPerillaVolumen != null) {
            Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
            TextureRegionDrawable barraDrawable = new TextureRegionDrawable(texturaBarraVolumen);
            barraDrawable.setMinWidth(1500);
            barraDrawable.setMinHeight(150);
            sliderStyle.background = barraDrawable;

            TextureRegionDrawable perillaDrawable = new TextureRegionDrawable(texturaPerillaVolumen);
            perillaDrawable.setMinWidth(50);
            perillaDrawable.setMinHeight(50);
            sliderStyle.knob = perillaDrawable;

            sliderVolumenPausa = new Slider(0f, 1f, 0.01f, false, sliderStyle);
            sliderVolumenPausa.setBounds(
                Gdx.graphics.getWidth() / 2f - 150f,
                Gdx.graphics.getHeight() / 2f - 70f,
                300f,
                40f
            );
            sliderVolumenPausa.setValue(juego.getVolumenMusica());
            sliderVolumenPausa.addListener(event -> {
                juego.setVolumenMusica(sliderVolumenPausa.getValue());
                return false;
            });
            pausaStage.addActor(sliderVolumenPausa);
        }

        if (sonidoGameOver == null) {
            try {
                if (Gdx.files.internal("gameoversonido.wav").exists()) {
                    sonidoGameOver = Gdx.audio.newSound(Gdx.files.internal("gameoversonido.wav"));
                }
            } catch (Exception e) {
                System.out.println("Error al cargar sonido de game over: " + e.getMessage());
            }
        }
    }

    private void reiniciarPartida() {
        if (juego.dificultadSeleccionada == null) {
            juego.dificultadSeleccionada = Dificultad.NORMAL;
        }
        float velocidad = juego.dificultadSeleccionada.getVelocidad() * VELOCIDAD_BASE;
        inicializarPartida(velocidad);
    }

    private void configurarDificultad() {
        Dificultad dificultad = juego.dificultadSeleccionada;
        if (dificultad == null) {
            dificultad = Dificultad.NORMAL;
        }

        switch (dificultad) {
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

    private void manejarInput() {
        if (snake == null) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            snake.cambiarDireccion(Direccion.ARRIBA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            snake.cambiarDireccion(Direccion.ABAJO);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            snake.cambiarDireccion(Direccion.IZQUIERDA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            snake.cambiarDireccion(Direccion.DERECHA);
        }
    }

    private void manejarVolumenEnPausa() {
        float ajuste = 0.01f;
        boolean cambioVolumen = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            juego.ajustarVolumenMusica(-ajuste);
            cambioVolumen = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            juego.ajustarVolumenMusica(ajuste);
            cambioVolumen = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            juego.alternarMuteMusica();
            cambioVolumen = true;
        }

        if (cambioVolumen && sliderVolumenPausa != null) {
            sliderVolumenPausa.setValue(juego.getVolumenMusica());
        }
    }

    private void activarInputPausa() {
        if (!inputPausaActivo && pausaStage != null) {
            Gdx.input.setInputProcessor(pausaStage);
            inputPausaActivo = true;
        }
    }

    private void desactivarInputPausa() {
        if (inputPausaActivo) {
            Gdx.input.setInputProcessor(null);
            inputPausaActivo = false;
        }
    }

    private void verificarColisionConFruta() {
        if (snake == null || fruta == null) return;

        Rectangle cabezaRect = new Rectangle(
            snake.getCabeza().x,
            snake.getCabeza().y,
            TAMANIO_SERPIENTE,
            TAMANIO_SERPIENTE
        );

        Rectangle frutaRect = fruta.obtenerRectangulo();

        if (cabezaRect.overlaps(frutaRect)) {
            snake.comer();

            int puntosGanados = fruta.getPuntos();
            if (fruta.getTipo() == Frutas.TipoFruta.PERA) {
                multiplicadorPuntuacion = 2;
                tiempoMultiplicador = DURACION_MULTIPLICADOR;
            }
            puntuacion += puntosGanados * multiplicadorPuntuacion;

            if (puntuacion > puntuacionRecord) {
                puntuacionRecord = puntuacion;
                if (preferencias != null) {
                    preferencias.putInteger("puntuacionRecord", puntuacionRecord);
                    preferencias.flush();
                }
            }

            fruta.actualizarTipoPorPuntuacion(puntuacion);
            fruta.actualizarProbabilidades(juego.dificultadSeleccionada);

            generarPiedrasSiCorresponde();

            regenerarFruta();
        }
    }

    private void generarPiedrasSiCorresponde() {
        if (piedras.size() >= maxPiedras) {
            return;
        }
        while (puntuacion >= siguientePiedraPuntuacion && piedras.size() < maxPiedras) {
            Piedra nueva = new Piedra(TAMANIO_SERPIENTE, 10f);
            int intentos = 0;
            while (intentos < 40 && posicionOcupada(nueva.obtenerRectangulo())) {
                nueva.generarNuevaPosicion();
                intentos++;
            }
            piedras.add(nueva);
            siguientePiedraPuntuacion += intervaloPiedraPuntuacion;
        }
    }

    private void regenerarFruta() {
        if (fruta == null) return;

        fruta.generarNuevaPosicion();
        int intentos = 0;
        while (intentos < 60 && posicionOcupada(fruta.obtenerRectangulo())) {
            fruta.generarNuevaPosicion();
            intentos++;
        }
    }

    private boolean posicionOcupada(Rectangle rect) {
        if (snake != null) {
            float tamano = snake.getTamano();
            for (Vector2 seg : snake.getCuerpo()) {
                Rectangle segmentoRect = new Rectangle(seg.x, seg.y, tamano, tamano);
                if (segmentoRect.overlaps(rect)) {
                    return true;
                }
            }
        }

        for (Piedra piedra : piedras) {
            if (piedra.obtenerRectangulo().overlaps(rect)) {
                return true;
            }
        }
        return false;
    }

    private void verificarColisionConPiedra() {
        if (snake == null) return;

        Rectangle cabezaRect = new Rectangle(
            snake.getCabeza().x,
            snake.getCabeza().y,
            TAMANIO_SERPIENTE,
            TAMANIO_SERPIENTE
        );

        for (Piedra piedra : piedras) {
            if (cabezaRect.overlaps(piedra.obtenerRectangulo())) {
                marcarGameOver();
                return;
            }
        }
    }

    private void verificarColisionConBordes() {
        if (snake == null) return;

        Vector2 cabeza = snake.getCabeza();
        float maxX = Gdx.graphics.getWidth() - TAMANIO_SERPIENTE;
        float maxY = Gdx.graphics.getHeight() - TAMANIO_SERPIENTE;

        if (cabeza.x < 0 || cabeza.y < 0 || cabeza.x > maxX || cabeza.y > maxY) {
            marcarGameOver();
        }
    }

    private void verificarColisionConCuerpo() {
        if (snake == null) return;

        if (snake.getCuerpo().size() < 8) {
            return;
        }

        Vector2 cabeza = snake.getCabeza();
        float tamano = snake.getTamano();
        float umbralColision = tamano * 0.28f;
        float umbralColisionCuadrado = umbralColision * umbralColision;
        boolean esCabeza = true;
        int segmentosIgnorados = 0;
        for (Vector2 segmento : snake.getCuerpo()) {
            if (esCabeza) {
                esCabeza = false;
                continue;
            }
            if (segmentosIgnorados < 8) {
                segmentosIgnorados++;
                continue;
            }
            float dx = cabeza.x - segmento.x;
            float dy = cabeza.y - segmento.y;
            if (dx * dx + dy * dy <= umbralColisionCuadrado) {
                marcarGameOver();
                return;
            }
        }
    }

    private void marcarGameOver() {
        if (juegoTerminado) {
            return;
        }
        juegoTerminado = true;
        if (!sonidoGameOverReproducido && sonidoGameOver != null) {
            sonidoGameOver.play(0.7f);
            sonidoGameOverReproducido = true;
        }
        System.out.println("Game Over! Puntuación final: " + puntuacion);
    }

    @Override
    public void resize(int width, int height) {
        if (pausaStage != null) {
            pausaStage.getViewport().update(width, height, true);
        }
        if (sliderVolumenPausa != null) {
            sliderVolumenPausa.setBounds(width / 2f - 150f, height / 2f - 70f, 300f, 40f);
        }
    }

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
        try {
            liberarRecursosPartida();
            if (font != null) {
                font.dispose();
                font = null;
            }
            if (imagenGameOver != null) {
                imagenGameOver.dispose();
                imagenGameOver = null;
            }
            if (fondoJuego != null) {
                fondoJuego.dispose();
                fondoJuego = null;
            }
            if (fondoGameOver != null) {
                fondoGameOver.dispose();
                fondoGameOver = null;
            }
            if (texturaBarraVolumen != null) {
                texturaBarraVolumen.dispose();
                texturaBarraVolumen = null;
            }
            if (texturaPerillaVolumen != null) {
                texturaPerillaVolumen.dispose();
                texturaPerillaVolumen = null;
            }
            if (sonidoGameOver != null) {
                sonidoGameOver.dispose();
                sonidoGameOver = null;
            }
            if (pausaStage != null) {
                pausaStage.dispose();
                pausaStage = null;
                sliderVolumenPausa = null;
            }
            desactivarInputPausa();
        } catch (Exception e) {
            System.err.println("Error al hacer dispose: " + e.getMessage());
        }
    }

    private void liberarRecursosPartida() {
        if (snake != null) {
            snake.dispose();
            snake = null;
        }
        if (fruta != null) {
            fruta.dispose();
            fruta = null;
        }
        for (Piedra piedra : piedras) {
            piedra.dispose();
        }
        piedras.clear();
    }
}
