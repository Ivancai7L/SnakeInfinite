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
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.ArrayList;
import java.util.List;
import mi.proyecto.cliente.HiloCliente;

public class PantallaClienteOnline implements Screen {

    private static final int PUERTO_SERVIDOR = 5050;

    private final MiJuegoPrincipal game;
    private Stage stage;
    private Texture fondo;
    private BitmapFont font;
    private BitmapFont fontTitulo;
    private Label estado;
    private TextField campoIp;
    private TextButton btnConectar;
    private TextButton btnVolver;
    private volatile boolean conectando;
    private final List<Texture> texturasUI = new ArrayList<>();

    public PantallaClienteOnline(MiJuegoPrincipal game) {
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

        TextField.TextFieldStyle ipStyle = new TextField.TextFieldStyle();
        ipStyle.font = font;
        ipStyle.fontColor = new Color(0.95f, 0.98f, 1f, 1f);
        ipStyle.cursor = new TextureRegionDrawable(crearTexturaUI(1, 1, 255, 255, 255, 255));
        ipStyle.background = new TextureRegionDrawable(crearTexturaUI(1, 1, 20, 28, 44, 230));
        ipStyle.messageFont = font;
        ipStyle.messageFontColor = new Color(0.72f, 0.80f, 0.92f, 1f);

        campoIp = new TextField("127.0.0.1", ipStyle);
        campoIp.setMessageText("IP del servidor");
        campoIp.setAlignment(Align.center);

        btnConectar = crearBoton("CONECTAR AL SERVIDOR", 70, 95, 190);
        btnVolver = crearBoton("VOLVER", 120, 120, 120);

        estado = new Label("Ingresá la IP del servidor y conectá", textStyle);
        estado.setAlignment(Align.center);

        btnConectar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String ip = campoIp.getText() == null ? "127.0.0.1" : campoIp.getText().trim();
                if (ip.isEmpty()) {
                    ip = "127.0.0.1";
                }
                conectarComoCliente(ip);
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

        table.add(new Label("MODO CLIENTE", titleStyle)).padBottom(16).width(640);
        table.row();
        table.add(new Label("Conectate al servidor abierto en otra ventana/PC", textStyle)).padBottom(12).width(640);
        table.row();
        table.add(new Label("Puerto: " + PUERTO_SERVIDOR, textStyle)).padBottom(8).width(640);
        table.row();
        table.add(campoIp).width(520).height(60).pad(8);
        table.row();
        table.add(btnConectar).width(520).height(90).pad(8);
        table.row();
        table.add(estado).padTop(12).width(640);
        table.row();
        table.add(btnVolver).width(520).height(90).padTop(20);

        stage.addActor(table);
    }

    private void conectarComoCliente(String ip) {
        if (conectando) {
            return;
        }
        conectando = true;
        btnConectar.setDisabled(true);
        campoIp.setDisabled(true);
        estado.setText("Conectando a " + ip + ":" + PUERTO_SERVIDOR + " ...");

        HiloCliente hiloCliente = new HiloCliente(ip, PUERTO_SERVIDOR, new HiloCliente.Listener() {
            @Override
            public void onEstado(String estadoTexto) {
                Gdx.app.postRunnable(() -> estado.setText(estadoTexto));
            }

            @Override
            public void onConectado(OnlineSession session, Dificultad dificultad) {
                Gdx.app.postRunnable(() -> {
                    conectando = false;
                    btnConectar.setDisabled(false);
                    campoIp.setDisabled(false);
                    game.iniciarJuegoOnline(session, false, dificultad);
                });
            }

            @Override
            public void onError(String mensaje) {
                Gdx.app.postRunnable(() -> {
                    conectando = false;
                    btnConectar.setDisabled(false);
                    campoIp.setDisabled(false);
                    estado.setText(mensaje);
                });
            }
        });
        hiloCliente.start();
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
