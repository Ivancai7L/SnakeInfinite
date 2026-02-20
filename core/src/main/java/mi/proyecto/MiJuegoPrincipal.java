package mi.proyecto;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Logger;


//Maneja el SpriteBatch, la música, el estado de mute/volumen y la navegación entre pantallas (menús, juego, online).
//También centraliza el inicio de la música y la gestión del ciclo de vida general del juego.

public class MiJuegoPrincipal extends Game {

    private static final Logger LOGGER = new Logger(MiJuegoPrincipal.class.getSimpleName(), Logger.INFO);
    private static final String MUSICA_RUTA = "Musicasnake.mp3";
    private static final float VOLUMEN_MIN = 0f;
    private static final float VOLUMEN_MAX = 1f;

    public SpriteBatch batch;
    public Dificultad dificultadSeleccionada;

    private Music musica;
    private float volumenMusica = 0.5f;
    private boolean mute;

    @Override
    public void create() {
        LOGGER.info("create() iniciado");

        batch = new SpriteBatch();
        actualizarProyeccionBatch(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        iniciarMusica();
        mostrarMenu();

        LOGGER.info("create() completado");
    }

    public void mostrarMenu() {
        cambiarPantalla(new Menu(this), "mostrarMenu");
    }

    public void mostrarSeleccionDificultad() {
        cambiarPantalla(new PantallaSeleccionDificultad(this), "mostrarSeleccionDificultad");
    }

    public void iniciarJuego() {
        LOGGER.info("iniciarJuego() - dificultad: " + dificultadSeleccionada);
        cambiarPantalla(new JuegoScreen(this), "iniciarJuego");
    }

    public void mostrarJuego() {
        cambiarPantalla(new JuegoScreen(this), "mostrarJuego");
    }

    public void mostrarMenuOnline() {
        cambiarPantalla(new OnlineMenuScreen(this), "mostrarMenuOnline");
    }

    public void iniciarJuegoOnline(OnlineSession session, boolean host) {
        iniciarJuegoOnline(session, host, Dificultad.NORMAL);
    }

    public void iniciarJuegoOnline(OnlineSession session, boolean host, Dificultad dificultadOnline) {
        Dificultad dificultad = dificultadOnline == null ? Dificultad.NORMAL : dificultadOnline;
        cambiarPantalla(new OnlineJuegoScreen(this, session, host, dificultad), "iniciarJuegoOnline");
    }

    private void iniciarMusica() {
        try {
            if (!Gdx.files.internal(MUSICA_RUTA).exists()) {
                LOGGER.info("No se encontró el archivo de música: " + MUSICA_RUTA);
                return;
            }

            if (musica == null) {
                musica = Gdx.audio.newMusic(Gdx.files.internal(MUSICA_RUTA));
            }

            musica.setLooping(true);
            musica.setVolume(mute ? 0f : volumenMusica);
            musica.play();
        } catch (Exception e) {
            LOGGER.error("Error al iniciar música", e);
        }
    }

    public void setVolumenMusica(float volumen) {
        volumenMusica = Math.max(VOLUMEN_MIN, Math.min(VOLUMEN_MAX, volumen));
        if (musica != null) {
            musica.setVolume(mute ? 0f : volumenMusica);
        }
    }

    public float getVolumenMusica() {
        return volumenMusica;
    }

    public void alternarMuteMusica() {
        mute = !mute;
        if (musica != null) {
            musica.setVolume(mute ? 0f : volumenMusica);
        }
    }

    public boolean isMusicaMute() {
        return mute;
    }

    public void ajustarVolumenMusica(float delta) {
        setVolumenMusica(volumenMusica + delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        actualizarProyeccionBatch(width, height);
    }

    @Override
    public void dispose() {
        LOGGER.info("dispose() llamado");

        if (batch != null) {
            batch.dispose();
            batch = null;
        }

        if (musica != null) {
            musica.dispose();
            musica = null;
        }
    }

    private void actualizarProyeccionBatch(int width, int height) {
        if (batch == null || width <= 0 || height <= 0) {
            return;
        }
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    private void cambiarPantalla(Screen pantalla, String origen) {
        try {
            setScreen(pantalla);
            actualizarProyeccionBatch(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        } catch (Exception e) {
            LOGGER.error("Error al cambiar pantalla desde " + origen, e);
        }
    }
}
