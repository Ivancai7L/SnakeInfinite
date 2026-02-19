package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.ArrayList;
import java.util.List;
import mi.proyecto.servidor.DatosServidor;
import mi.proyecto.servidor.HiloServidor;

public class PantallaServidorOnline implements Screen {

    private static final int PUERTO_SERVIDOR = 5050;

    private final MiJuegoPrincipal game;
    private Stage stage;
    private Texture fondo;
    private BitmapFont font;
    private BitmapFont fontTitulo;
    private Label lblEstado;
    private Label lblJugadores;
    private TextButton btnDificultad;
    private TextButton btnIniciarServidor;
    private TextButton btnVolver;
    private Dificultad dificultadOnline = Dificultad.NORMAL;
    private volatile boolean esperandoConexion;
    private final List<Texture> texturasUI = new ArrayList<>();

    public PantallaServidorOnline(MiJuegoPrincipal game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        fondo = cargarTexturaConFallback("Fondosnake.jpg", 20, 20, 20);

        font = new BitmapFont();
        font.getData().setScale(1.8f);
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(2.5f);

        Label.LabelStyle titleStyle = new Label.LabelStyle(fontTitulo, Color.GOLD);
        Label.LabelStyle textStyle = new Label.LabelStyle(font, Color.WHITE);

        btnIniciarServidor = crearBoton("INICIAR SERVIDOR", 50, 145, 50);
        btnDificultad = crearBoton(textoDificultad(), 170, 120, 40);
        btnVolver = crearBoton("VOLVER", 120, 120, 120);

        lblEstado = new Label("Servidor apagado", textStyle);
        lblEstado.setAlignment(Align.center);
        lblJugadores = new Label("Jugadores conectados: 1/2 (host)", textStyle);
        lblJugadores.setAlignment(Align.center);

        btnIniciarServidor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                iniciarServidor();
            }
        });

        btnDificultad.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (esperandoConexion) {
                    lblEstado.setText("No podés cambiar dificultad mientras esperás cliente");
                    return;
                }
                dificultadOnline = siguienteDificultad(dificultadOnline);
                btnDificultad.setText(textoDificultad());
            }
        });

        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.mostrarMenuOnline();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.align(Align.center);
        table.pad(24f);
        table.background(new TextureRegionDrawable(crearTexturaUI(1, 1, 8, 12, 22, 190)));

        table.add(new Label("MODO SERVIDOR", titleStyle)).padBottom(16).width(640);
        table.row();
        table.add(new Label("Al iniciar, queda esperando 1 cliente para jugar.", textStyle)).padBottom(10).width(640);
        table.row();
        table.add(new Label("Puerto: " + PUERTO_SERVIDOR, textStyle)).padBottom(16).width(640);
        table.row();
        table.add(btnDificultad).width(520).height(80).pad(8);
        table.row();
        table.add(btnIniciarServidor).width(520).height(90).pad(8);
        table.row();
        table.add(lblEstado).padTop(12).width(640);
        table.row();
        table.add(lblJugadores).padTop(8).width(640);
        table.row();
        table.add(btnVolver).width(520).height(90).padTop(20);

        stage.addActor(table);
    }

    private void iniciarServidor() {
        if (esperandoConexion) {
            return;
        }
        esperandoConexion = true;
        btnIniciarServidor.setDisabled(true);
        btnDificultad.setDisabled(true);
        btnVolver.setDisabled(true);
        lblEstado.setText("Esperando jugadores...");
        lblJugadores.setText("Jugadores conectados: 1/2 (host)");

        DatosServidor datos = new DatosServidor(PUERTO_SERVIDOR, dificultadOnline);
        HiloServidor hiloServidor = new HiloServidor(datos, new HiloServidor.Listener() {
            @Override
            public void onEstado(String estado) {
                Gdx.app.postRunnable(() -> {
                    lblEstado.setText(estado);
                    if (estado.toLowerCase().contains("esperando")) {
                        lblJugadores.setText("Jugadores conectados: 1/2 (host)");
                    }
                });
            }

            @Override
            public void onConectado(OnlineSession session, DatosServidor datosServidor) {
                Dificultad dificultad = datosServidor.getDificultad();
                Gdx.app.postRunnable(() -> {
                    lblEstado.setText("Cliente conectado. Iniciando partida...");
                    lblJugadores.setText("Jugadores conectados: 2/2");
                    esperandoConexion = false;
                    game.iniciarJuegoOnline(session, true, dificultad);
                });
            }

            @Override
            public void onError(String mensaje) {
                Gdx.app.postRunnable(() -> {
                    lblEstado.setText("Error servidor: " + mensaje);
                    lblJugadores.setText("Jugadores conectados: 1/2 (host)");
                    esperandoConexion = false;
                    btnIniciarServidor.setDisabled(false);
                    btnDificultad.setDisabled(false);
                    btnVolver.setDisabled(false);
                });
            }
        });
        hiloServidor.start();
    }

    private String textoDificultad() {
        return "DIFICULTAD: " + dificultadOnline.name();
    }

    private Dificultad siguienteDificultad(Dificultad actual) {
        if (actual == Dificultad.FACIL) return Dificultad.NORMAL;
        if (actual == Dificultad.NORMAL) return Dificultad.DIFICIL;
        return Dificultad.FACIL;
    }

    private TextButton crearBoton(String texto, int r, int g, int b) {
        Texture base = crearTexturaUI(1, 1, r, g, b, 255);
        Texture hover = crearTexturaUI(1, 1,
            Math.min(255, r + 25),
            Math.min(255, g + 25),
            Math.min(255, b + 25),
            255);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = new TextureRegionDrawable(base);
        style.down = new TextureRegionDrawable(hover);
        style.over = new TextureRegionDrawable(hover);
        style.font = font;
        style.fontColor = new Color(0.96f, 0.99f, 1f, 1f);
        return new TextButton(texto, style);
    }

    private Texture crearTexturaUI(int w, int h, int r, int g, int b, int a) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(r / 255f, g / 255f, b / 255f, a / 255f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texturasUI.add(texture);
        return texture;
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (font != null) font.dispose();
        if (fontTitulo != null) fontTitulo.dispose();
        if (fondo != null) fondo.dispose();
        for (Texture texture : texturasUI) {
            texture.dispose();
        }
        texturasUI.clear();
    }
}
