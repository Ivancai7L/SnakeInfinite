package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Menu implements Screen {
    private final MiJuegoPrincipal game;
    private Stage stage;
    private Texture fondo;
    private Music musica;

    // 🔊 Sonidos
    private Sound hoverSound, clickSound;

    // Botones principales
    private ImageButton btnJugar, btnOpciones, btnSalir;

    // Submenú
    private Table tablaOpciones;
    private boolean mostrandoOpciones = false;

    public Menu(MiJuegoPrincipal game) {
        this.game = game;
    }

    @Override
    public void show() {
        fondo = new Texture("Fondosnake.jpg");

        // Música de fondo
        musica = Gdx.audio.newMusic(Gdx.files.internal("Musicasnake.mp3"));
        musica.setLooping(true);
        musica.setVolume(0.5f);
        musica.play();

        // Sonidos
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("hover.wav"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("sonidobotones.wav"));

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Botones principales
        TextureRegionDrawable imgJugar = new TextureRegionDrawable(new Texture("jugar.png"));
        TextureRegionDrawable imgOpciones = new TextureRegionDrawable(new Texture("opciones.png"));
        TextureRegionDrawable imgSalir = new TextureRegionDrawable(new Texture("Salir.png"));
        TextureRegionDrawable imgVolver = new TextureRegionDrawable(new Texture("volver.png")); // para el submenú

        btnJugar = new ImageButton(imgJugar);
        btnOpciones = new ImageButton(imgOpciones);
        btnSalir = new ImageButton(imgSalir);

        // Efectos hover y clic
        addHoverEffect(btnJugar);
        addHoverEffect(btnOpciones);
        addHoverEffect(btnSalir);

        // Acciones
        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                musica.stop();
                game.mostrarJuego();
            }
        });

        btnOpciones.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                mostrarOpciones(true);
            }
        });

        btnSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                musica.stop();
                Gdx.app.exit();
            }
        });

        // Tabla principal
        Table tablaPrincipal = new Table();
        tablaPrincipal.setFillParent(true);
        tablaPrincipal.align(Align.center);
        tablaPrincipal.add(btnJugar).width(250).height(100).pad(15);
        tablaPrincipal.row();
        tablaPrincipal.add(btnOpciones).width(250).height(100).pad(15);
        tablaPrincipal.row();
        tablaPrincipal.add(btnSalir).width(250).height(100).pad(15);
        stage.addActor(tablaPrincipal);

        // Submenú Opciones
        ImageButton btnVolver = new ImageButton(imgVolver);
        addHoverEffect(btnVolver);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play(0.8f);
                mostrarOpciones(false);
            }
        });

        tablaOpciones = new Table();
        tablaOpciones.setFillParent(true);
        tablaOpciones.align(Align.center);
        tablaOpciones.add(btnVolver).width(250).height(100).pad(15);
        tablaOpciones.setVisible(false);
        stage.addActor(tablaOpciones);
    }

    private void addHoverEffect(ImageButton button) {
        // 🔹 Habilitar transformaciones para permitir escala
        button.setTransform(true);
        button.setOrigin(Align.center);

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                hoverSound.play(0.5f);
                button.clearActions();
                button.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f)); // animación suave
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                button.clearActions();
                button.addAction(Actions.scaleTo(1f, 1f, 0.1f)); // volver al tamaño normal
            }
        });
    }

    private void mostrarOpciones(boolean mostrar) {
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : stage.getActors()) {
            actor.setVisible(!mostrar);
        }
        tablaOpciones.setVisible(mostrar);
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

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        fondo.dispose();
        musica.dispose();
        stage.dispose();
        hoverSound.dispose();
        clickSound.dispose();
    }
}
