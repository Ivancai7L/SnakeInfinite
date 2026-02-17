package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import mi.proyecto.cliente.HiloCliente;
import mi.proyecto.servidor.DatosServidor;
import mi.proyecto.servidor.HiloServidor;


public class OnlineMenuScreen implements Screen {

    private final MiJuegoPrincipal game;
    private Stage stage;
    private Texture fondo;
    private BitmapFont font;
    private Label estado;
    private volatile boolean esperandoConexion;
    private volatile boolean conectandoCliente;
    private Dificultad dificultadOnline = Dificultad.NORMAL;
    private final List<Texture> texturasUI = new ArrayList<>();

    public OnlineMenuScreen(MiJuegoPrincipal game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        fondo = cargarTexturaConFallback("Fondosnake.jpg", 20, 20, 20);

        font = new BitmapFont();
        font.getData().setScale(2f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.ORANGE);

        TextButton btnHost = crearBoton("CREAR SERVIDOR", 50, 145, 50);
        TextButton btnJoinLocal = crearBoton("CLIENTE LOCAL (127.0.0.1)", 50, 95, 190);
        TextButton btnJoin = crearBoton("ENTRAR COMO CLIENTE (IP)", 70, 95, 190);
        TextButton btnBack = crearBoton("VOLVER AL MENU", 120, 120, 120);
        TextButton btnDificultad = crearBoton(textoDificultad(), 170, 120, 40);

        estado = new Label("Elegí una opción para empezar", style);

        btnHost.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                iniciarHost();
            }
        });

        btnJoinLocal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                conectarComoCliente("127.0.0.1");
            }
        });

        btnJoin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pedirIpYConectar();
            }
        });

        btnDificultad.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (esperandoConexion || conectandoCliente) {
                    estado.setText("No podés cambiar dificultad durante una conexión");
                    return;
                }
                dificultadOnline = siguienteDificultad(dificultadOnline);
                btnDificultad.setText(textoDificultad());
                estado.setText("Dificultad online: " + dificultadOnline.name());
            }
        });

        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.mostrarMenu();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.align(Align.center);

        table.add(new Label("ONLINE 2 JUGADORES", style)).padBottom(18);
        table.row();
        table.add(new Label("1) Jugador 1: elegí dificultad y click en CREAR SERVIDOR", style)).padBottom(8);
        table.row();
        table.add(new Label("2) Jugador 2: click en CLIENTE LOCAL (misma PC)", style)).padBottom(8);
        table.row();
        table.add(new Label("3) Si están en PCs distintas: ENTRAR COMO CLIENTE (IP)", style)).padBottom(16);

        table.row();
        table.add(btnDificultad).width(520).height(80).pad(8);
        table.row();
        table.add(btnHost).width(520).height(90).pad(10);
        table.row();
        table.add(btnJoinLocal).width(520).height(90).pad(10);
        table.row();
        table.add(btnJoin).width(520).height(90).pad(10);
        table.row();
        table.add(btnBack).width(520).height(90).pad(10);
        table.row();
        table.add(estado).padTop(20);

        stage.addActor(table);
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
        style.fontColor = Color.WHITE;

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

    private void iniciarHost() {
        if (esperandoConexion) {
            return;
        }
        esperandoConexion = true;
        estado.setText("Servidor abierto en puerto 5050 (esperando cliente)...");
        Thread t = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(5050)) {
                Socket socket = serverSocket.accept();
                OnlineSession session = new OnlineSession(socket);
                session.enviar("ROLE:CLIENT");
                session.enviar("DIFF:" + dificultadOnline.name());
                Dificultad dificultadElegida = dificultadOnline;
                Gdx.app.postRunnable(() -> game.iniciarJuegoOnline(session, true, dificultadElegida));
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> estado.setText("Error host: " + e.getMessage()));
            } finally {
                esperandoConexion = false;
            }
        }, "online-host-thread");
        t.setDaemon(true);
        t.start();
    }

    private void pedirIpYConectar() {
        if (conectandoCliente) return;
        estado.setText("Ingresá la IP del servidor (si es misma PC: 127.0.0.1)");
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                conectarComoCliente(text == null || text.isEmpty() ? "127.0.0.1" : text.trim());
            }

            @Override
            public void canceled() {
                estado.setText("Conexión cancelada");
            }
        }, "Conectar a IP", "127.0.0.1", "");
    }

    private void conectarComoCliente(String ip) {
        if (conectandoCliente) {
            return;
        }
        conectandoCliente = true;
        estado.setText("Conectando como cliente a " + ip + ":5050 ...");
        Thread t = new Thread(() -> {
            try {
                Socket socket = new Socket(ip, 5050);
                OnlineSession session = new OnlineSession(socket);
                String handshake = session.recibirLinea();
                if (!"ROLE:CLIENT".equals(handshake)) {
                    session.cerrar();
                    Gdx.app.postRunnable(() -> estado.setText("Conexión inválida: el host no está en modo servidor"));
                    return;
                }

                String diffMsg = session.recibirLinea();
                Dificultad dificultadRemota = Dificultad.NORMAL;
                if (diffMsg != null && diffMsg.startsWith("DIFF:")) {
                    String nombreDiff = diffMsg.substring(5).trim();
                    try {
                        dificultadRemota = Dificultad.valueOf(nombreDiff);
                    } catch (IllegalArgumentException ignored) {
                        dificultadRemota = Dificultad.NORMAL;
                    }
                }

                Dificultad dificultadFinal = dificultadRemota;
                Gdx.app.postRunnable(() -> game.iniciarJuegoOnline(session, false, dificultadFinal));
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> estado.setText("Error conexión: " + e.getMessage()));
            } finally {
                conectandoCliente = false;
            }
        }, "online-join-thread");
        t.setDaemon(true);
        t.start();
    }

    private String textoDificultad() {
        return "DIFICULTAD ONLINE: " + dificultadOnline.name();
    }

    private Dificultad siguienteDificultad(Dificultad actual) {
        if (actual == Dificultad.FACIL) return Dificultad.NORMAL;
        if (actual == Dificultad.NORMAL) return Dificultad.DIFICIL;
        return Dificultad.FACIL;
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
        if (stage != null) {
            stage.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (fondo != null) {
            fondo.dispose();
        }
        for (Texture texture : texturasUI) {
            texture.dispose();
        }
        texturasUI.clear();
    }
}
