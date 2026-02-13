package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Menu implements Screen {
    private final MiJuegoPrincipal game;
    private Stage stage;
    private Texture fondo;

    private Sound hoverSound;
    private Sound clickSound;

    private Texture texJugar;
    private Texture texOpciones;
    private Texture texControles;
    private Texture texSalir;
    private Texture texVolver;
    private Texture texBarra;
    private Texture texPerilla;
    private Texture texFlechitas;
    private Texture texP;
    private Texture texOnline;

    private ImageButton btnJugar;
    private ImageButton btnOpciones;
    private ImageButton btnControles;
    private ImageButton btnSalir;
    private ImageButton btnVolverOpciones;
    private ImageButton btnVolverControles;
    private ImageButton btnOnline;

    private Slider sliderVolumen;
    private Label lblMuteHint;
    private Table tablaPrincipal;
    private Table tablaOpciones;
    private Table tablaControles;

    public Menu(MiJuegoPrincipal game) {
        this.game = game;
    }

    @Override
    public void show() {
        fondo = new Texture("Fondosnake.jpg");

        hoverSound = Gdx.audio.newSound(Gdx.files.internal("hover.wav"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("sonidobotones.wav"));

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        texJugar = cargarTexturaConFallback("jugar.png", 40, 180, 40);
        texOnline = cargarTexturaConFallback("online.png", 40, 200, 200);
        texOpciones = cargarTexturaConFallback("opciones.png", 60, 120, 220);
        texControles = cargarTexturaConFallback("controles.png", 220, 150, 40);
        texSalir = cargarTexturaConFallback("Salir.png", 200, 60, 60);
        texVolver = cargarTexturaConFallback("volver.png", 170, 170, 170);
        texBarra = cargarTexturaConFallback("barra.png", 120, 120, 120);
        texPerilla = cargarTexturaConFallback("perilla.png", 220, 220, 220);
        texFlechitas = cargarTexturaConFallback("flechitas.png", 80, 160, 220);
        texP = cargarTexturaConFallback("p.png", 200, 80, 180);


        btnJugar = new ImageButton(new TextureRegionDrawable(texJugar));
        btnOpciones = new ImageButton(new TextureRegionDrawable(texOpciones));
        btnControles = new ImageButton(new TextureRegionDrawable(texControles));
        btnSalir = new ImageButton(new TextureRegionDrawable(texSalir));
        btnVolverOpciones = new ImageButton(new TextureRegionDrawable(texVolver));
        btnVolverControles = new ImageButton(new TextureRegionDrawable(texVolver));
        btnOnline = new ImageButton(new TextureRegionDrawable(texOnline));

        addHoverEffect(btnJugar);
        addHoverEffect(btnOpciones);
        addHoverEffect(btnControles);
        addHoverEffect(btnSalir);
        addHoverEffect(btnVolverOpciones);
        addHoverEffect(btnVolverControles);
        addHoverEffect(btnOnline);

        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                game.mostrarSeleccionDificultad();
            }
        });

        btnOpciones.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                mostrarSeccion(SeccionMenu.OPCIONES);
            }
        });

        btnControles.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                mostrarSeccion(SeccionMenu.CONTROLES);
            }
        });

        btnOnline.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                game.mostrarMenuOnline();
            }
        });

        btnSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                Gdx.app.exit();
            }
        });

        btnVolverOpciones.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                mostrarSeccion(SeccionMenu.PRINCIPAL);
            }
        });

        btnVolverControles.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                mostrarSeccion(SeccionMenu.PRINCIPAL);
            }
        });

        crearTablaPrincipal();
        crearTablaOpciones();
        crearTablaControles();
        mostrarSeccion(SeccionMenu.PRINCIPAL);
    }

    private void crearTablaPrincipal() {
        tablaPrincipal = new Table();
        tablaPrincipal.setFillParent(true);
        tablaPrincipal.align(Align.center);
        tablaPrincipal.add(btnJugar).width(250).height(100).pad(12);
        tablaPrincipal.row();
        tablaPrincipal.add(btnOpciones).width(250).height(100).pad(12);
        tablaPrincipal.row();
        tablaPrincipal.add(btnControles).width(250).height(100).pad(12);
        tablaPrincipal.row();
        tablaPrincipal.add(btnOnline).width(250).height(100).pad(12);
        tablaPrincipal.row();
        tablaPrincipal.add(btnSalir).width(250).height(100).pad(12);
        stage.addActor(tablaPrincipal);
    }

    private void crearTablaOpciones() {
        tablaOpciones = new Table();
        tablaOpciones.setFillParent(true);
        tablaOpciones.align(Align.center);

        BitmapFont fontVolumen = new BitmapFont();
        fontVolumen.getData().setScale(3f);

        Label.LabelStyle estiloTexto = new Label.LabelStyle(fontVolumen, Color.ORANGE);
        Label lblVolumen = new Label("Volumen", estiloTexto);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        TextureRegionDrawable barraDrawable = new TextureRegionDrawable(texBarra);
        barraDrawable.setMinWidth(1500);
        barraDrawable.setMinHeight(150);
        sliderStyle.background = barraDrawable;

        TextureRegionDrawable perillaDrawable = new TextureRegionDrawable(texPerilla);
        perillaDrawable.setMinWidth(50);
        perillaDrawable.setMinHeight(50);
        sliderStyle.knob = perillaDrawable;

        sliderVolumen = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        sliderVolumen.setValue(game.getVolumenMusica());
        sliderVolumen.addListener(event -> {
            game.setVolumenMusica(sliderVolumen.getValue());
            return false;
        });

        Label lblMute = new Label("M para mutear / desmutear", estiloTexto);
        lblMuteHint = new Label("Estado: " + (game.isMusicaMute() ? "MUTE" : "SONANDO"), estiloTexto);

        tablaOpciones.add(lblVolumen).padBottom(20);
        tablaOpciones.row();
        tablaOpciones.add(sliderVolumen).width(300).height(40).pad(10);
        tablaOpciones.row();
        tablaOpciones.add(lblMute).padTop(8).padBottom(8);
        tablaOpciones.row();
        tablaOpciones.add(lblMuteHint).padBottom(10);
        tablaOpciones.row();
        tablaOpciones.add(btnVolverOpciones).width(250).height(100).pad(15);
        stage.addActor(tablaOpciones);
    }

    private void crearTablaControles() {
        tablaControles = new Table();
        tablaControles.setFillParent(true);
        tablaControles.align(Align.center);

        BitmapFont fontControles = new BitmapFont();
        fontControles.getData().setScale(2.2f);
        Label.LabelStyle estilo = new Label.LabelStyle(fontControles, Color.WHITE);
        Label titulo = new Label("Controles del juego", estilo);

        Image imagenFlechas = new Image(texFlechitas);
        Image imagenP = new Image(texP);

        Label textoMovimiento = new Label("Mover snake", estilo);
        Label textoPausa = new Label("Pausar / reanudar", estilo);

        tablaControles.add(titulo).padBottom(30);
        tablaControles.row();
        tablaControles.add(imagenFlechas).width(260).height(140).padBottom(8);
        tablaControles.row();
        tablaControles.add(textoMovimiento).padBottom(20);
        tablaControles.row();
        tablaControles.add(imagenP).width(120).height(120).padBottom(8);
        tablaControles.row();
        tablaControles.add(textoPausa).padBottom(20);
        tablaControles.row();
        tablaControles.add(btnVolverControles).width(250).height(100).pad(15);

        stage.addActor(tablaControles);
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

    private void addHoverEffect(ImageButton button) {
        button.setTransform(true);
        button.setOrigin(Align.center);

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                              com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                hoverSound.play(0.5f);
                button.clearActions();
                button.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                             com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                button.clearActions();
                button.addAction(Actions.scaleTo(1f, 1f, 0.1f));
            }
        });
    }

    private void mostrarSeccion(SeccionMenu seccion) {
        tablaPrincipal.setVisible(seccion == SeccionMenu.PRINCIPAL);
        tablaOpciones.setVisible(seccion == SeccionMenu.OPCIONES);
        tablaControles.setVisible(seccion == SeccionMenu.CONTROLES);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.M)) {
            game.alternarMuteMusica();
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (sliderVolumen != null && Math.abs(sliderVolumen.getValue() - game.getVolumenMusica()) > 0.001f) {
            sliderVolumen.setValue(game.getVolumenMusica());
        }
        if (lblMuteHint != null) {
            lblMuteHint.setText("Estado: " + (game.isMusicaMute() ? "MUTE" : "SONANDO"));
        }

        game.batch.begin();
        game.batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        fondo.dispose();
        stage.dispose();
        hoverSound.dispose();
        clickSound.dispose();

        texJugar.dispose();
        texOpciones.dispose();
        texControles.dispose();
        texSalir.dispose();
        texVolver.dispose();
        texBarra.dispose();
        texPerilla.dispose();
        texFlechitas.dispose();
        texP.dispose();
        texOnline.dispose();
    }

    private enum SeccionMenu {
        PRINCIPAL,
        OPCIONES,
        CONTROLES
    }
}
