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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import mi.proyecto.cliente.HiloCliente;
import mi.proyecto.servidor.DatosServidor;
import mi.proyecto.servidor.HiloServidor;

//Permite crear servidor, conectarse como cliente(local o por búsqueda automática), elegir dificultad y manejar el estado de conexión.

public class OnlineMenuScreen implements Screen {

    private static final int PUERTO_SERVIDOR = 5050;
    private static final String IP_LOCALHOST = "127.0.0.1";
    private static final int PUERTO_DESCUBRIMIENTO = 5051;
    private static final String BEACON_PREFIJO = "SNAKE_SERVER:";

    private final MiJuegoPrincipal game;
    private Stage stage;
    private Texture fondo;
    private BitmapFont font;
    private Label estado;
    private volatile boolean esperandoConexion;
    private volatile boolean conectandoCliente;
    private volatile boolean disposed;
    private Dificultad dificultadOnline = Dificultad.NORMAL;
    private final List<Texture> texturasUI = new ArrayList<>();
    private volatile boolean emitiendoBeacon;

    public OnlineMenuScreen(MiJuegoPrincipal game) {
        this.game = game;
    }

    @Override
    public void show() {
        disposed = false;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        fondo = cargarTexturaConFallback("Fondosnake.jpg", 20, 20, 20);

        font = new BitmapFont();
        font.getData().setScale(2f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.ORANGE);

        TextButton btnHost = crearBoton("CREAR SERVIDOR", 50, 145, 50);
        TextButton btnJoinLocal = crearBoton("CLIENTE LOCAL (127.0.0.1)", 50, 95, 190);
        TextButton btnJoin = crearBoton("ENTRAR COMO CLIENTE (AUTO)", 70, 95, 190);
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
                conectarComoCliente(IP_LOCALHOST);
            }
        });

        btnJoin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                conectarClienteAutomatico();
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
        table.add(new Label("3) Si están en PCs distintas: ENTRAR COMO CLIENTE (AUTO)", style)).padBottom(16);

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
        if (esperandoConexion || conectandoCliente) {
            return;
        }
        esperandoConexion = true;
        estado.setText("Servidor abierto en puerto " + PUERTO_SERVIDOR + " (esperando cliente)...");

        iniciarBeaconServidor();
        DatosServidor datosServidor = new DatosServidor(PUERTO_SERVIDOR, dificultadOnline);
        HiloServidor hiloServidor = new HiloServidor(datosServidor, new HiloServidor.Listener() {
            @Override
            public void onEstado(String estadoActual) {
                actualizarEstadoAsync(estadoActual);
            }

            @Override
            public void onConectado(OnlineSession session, DatosServidor datos) {
                Gdx.app.postRunnable(() -> {
                    if (disposed) {
                        session.cerrar();
                        return;
                    }
                    esperandoConexion = false;
                    detenerBeaconServidor();
                    game.iniciarJuegoOnline(session, true, datos.getDificultad());
                });
            }

            @Override
            public void onError(String mensaje) {
                Gdx.app.postRunnable(() -> {
                    esperandoConexion = false;
                    detenerBeaconServidor();
                    if (disposed) {
                        return;
                    }
                    estado.setText("Error host: " + mensaje);
                });
            }
        });
        hiloServidor.start();
    }

    private void conectarClienteAutomatico() {
        if (conectandoCliente || esperandoConexion) {
            return;
        }

        conectandoCliente = true;
        estado.setText("Buscando servidor en la red...");

        Thread detector = new Thread(() -> {
            String ipDescubierta = descubrirServidorLan();
            Gdx.app.postRunnable(() -> {
                if (disposed) {
                    conectandoCliente = false;
                    return;
                }
                if (ipDescubierta == null) {
                    conectandoCliente = false;
                    estado.setText("No se encontró servidor automáticamente. Verificá que el host esté en CREAR SERVIDOR");
                    return;
                }
                estado.setText("Servidor encontrado en " + ipDescubierta + ". Conectando...");
                conectandoCliente = false;
                conectarComoCliente(ipDescubierta);
            });
        }, "online-discovery-client-thread");
        detector.setDaemon(true);
        detector.start();
    }

    private String descubrirServidorLan() {
        byte[] buffer = new byte[128];
        try (DatagramSocket socket = new DatagramSocket(PUERTO_DESCUBRIMIENTO)) {
            socket.setSoTimeout(4500);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            if (!msg.startsWith(BEACON_PREFIJO)) {
                return null;
            }

            String puertoTexto = msg.substring(BEACON_PREFIJO.length()).trim();
            int puerto = Integer.parseInt(puertoTexto);
            if (puerto != PUERTO_SERVIDOR) {
                return null;
            }
            return packet.getAddress().getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }

    private void conectarComoCliente(String ip) {
        if (conectandoCliente) {
            return;
        }
        conectandoCliente = true;
        estado.setText("Conectando como cliente a " + ip + ":" + PUERTO_SERVIDOR + " ...");

        HiloCliente hiloCliente = new HiloCliente(ip, PUERTO_SERVIDOR, new HiloCliente.Listener() {
            @Override
            public void onEstado(String estadoActual) {
                actualizarEstadoAsync(estadoActual);
            }

            @Override
            public void onConectado(OnlineSession session, Dificultad dificultad) {
                Gdx.app.postRunnable(() -> {
                    if (disposed) {
                        session.cerrar();
                        return;
                    }
                    conectandoCliente = false;
                    game.iniciarJuegoOnline(session, false, dificultad);
                });
            }

            @Override
            public void onError(String mensaje) {
                Gdx.app.postRunnable(() -> {
                    conectandoCliente = false;
                    if (disposed) {
                        return;
                    }
                    estado.setText(mensaje);
                });
            }
        });
        hiloCliente.start();
    }

    private void iniciarBeaconServidor() {
        emitiendoBeacon = true;
        Thread beacon = new Thread(() -> {
            byte[] data = (BEACON_PREFIJO + PUERTO_SERVIDOR).getBytes(StandardCharsets.UTF_8);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName("255.255.255.255"),
                    PUERTO_DESCUBRIMIENTO
                );
                while (emitiendoBeacon && !disposed) {
                    socket.send(packet);
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }, "online-host-beacon-thread");
        beacon.setDaemon(true);
        beacon.start();
    }

    private void detenerBeaconServidor() {
        emitiendoBeacon = false;
    }

    private void actualizarEstadoAsync(String mensaje) {
        Gdx.app.postRunnable(() -> {
            if (disposed || estado == null) {
                return;
            }
            estado.setText(mensaje);
        });
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
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        disposed = true;
        esperandoConexion = false;
        conectandoCliente = false;
        detenerBeaconServidor();
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
