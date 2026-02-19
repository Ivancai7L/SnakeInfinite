package mi.proyecto;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MiJuegoPrincipal extends Game {

    public SpriteBatch batch;
    public Dificultad dificultadSeleccionada;
    private Music musica;
    private float volumenMusica = 0.5f;
    private boolean mute;

    @Override
    public void create() {
        try {
            System.out.println("MiJuegoPrincipal: create() iniciado");
            batch = new SpriteBatch();
            System.out.println("MiJuegoPrincipal: SpriteBatch creado");
            iniciarMusica();
            mostrarMenu();
            System.out.println("MiJuegoPrincipal: create() completado");
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en create()");
            e.printStackTrace();
        }
    }

    public void mostrarMenu() {
        try {
            System.out.println("MiJuegoPrincipal: mostrarMenu() llamado");
            setScreen(new Menu(this));
            System.out.println("MiJuegoPrincipal: Menu establecido");
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en mostrarMenu()");
            e.printStackTrace();
        }
    }

    public void mostrarSeleccionDificultad() {
        try {
            System.out.println("MiJuegoPrincipal: mostrarSeleccionDificultad() llamado");
            setScreen(new PantallaSeleccionDificultad(this));
            System.out.println("MiJuegoPrincipal: PantallaSeleccionDificultad establecida");
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en mostrarSeleccionDificultad()");
            e.printStackTrace();
        }
    }

    public void iniciarJuego() {
        try {
            System.out.println("MiJuegoPrincipal: iniciarJuego() llamado");
            System.out.println("MiJuegoPrincipal: Dificultad seleccionada: " + dificultadSeleccionada);
            setScreen(new JuegoScreen(this));
            System.out.println("MiJuegoPrincipal: JuegoScreen establecido");
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en iniciarJuego()");
            e.printStackTrace();
        }
    }

    public void mostrarJuego() {
        setScreen(new JuegoScreen(this));
    }

    public void mostrarMenuOnline() {
        try {
            setScreen(new OnlineMenuScreen(this));
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en mostrarMenuOnline()");
            e.printStackTrace();
        }
    }


    public void mostrarPantallaServidorOnline() {
        try {
            setScreen(new PantallaServidorOnline(this));
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en mostrarPantallaServidorOnline()");
            e.printStackTrace();
        }
    }

    public void mostrarPantallaClienteOnline() {
        try {
            setScreen(new PantallaClienteOnline(this));
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en mostrarPantallaClienteOnline()");
            e.printStackTrace();
        }
    }
    public void iniciarJuegoOnline(OnlineSession session, boolean host) {
        iniciarJuegoOnline(session, host, Dificultad.NORMAL);
    }

    public void iniciarJuegoOnline(OnlineSession session, boolean host, Dificultad dificultadOnline) {
        try {
            Dificultad dificultad = dificultadOnline == null ? Dificultad.NORMAL : dificultadOnline;
            setScreen(new OnlineJuegoScreen(this, session, host, dificultad));
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en iniciarJuegoOnline()");
            e.printStackTrace();
        }
    }

    private void iniciarMusica() {
        try {
            if (musica == null && Gdx.files.internal("Musicasnake.mp3").exists()) {
                musica = Gdx.audio.newMusic(Gdx.files.internal("Musicasnake.mp3"));
                musica.setLooping(true);
                musica.setVolume(volumenMusica);
                musica.play();
            }
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR al iniciar música");
            e.printStackTrace();
        }
    }

    public void setVolumenMusica(float volumen) {
        volumenMusica = Math.max(0f, Math.min(1f, volumen));
        if (!mute && musica != null) {
            musica.setVolume(volumenMusica);
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
    public void dispose() {
        try {
            System.out.println("MiJuegoPrincipal: dispose() llamado");
            batch.dispose();
            if (musica != null) {
                musica.dispose();
                musica = null;
            }
        } catch (Exception e) {
            System.err.println("MiJuegoPrincipal: ERROR en dispose()");
            e.printStackTrace();
        }
    }


}
