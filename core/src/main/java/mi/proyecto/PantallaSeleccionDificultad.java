package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


//Pantalla donde el jugador elige la dificultad antes de empezar.
//Muestra tres botones (Fácil, Normal, Difícil) y al hacer click guarda la dificultad elegida e inicia el juego.


public class PantallaSeleccionDificultad implements Screen {

    private final MiJuegoPrincipal juego;
    private Stage stage;
    private Texture texturaTitulo;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Texture texturaFacil;
    private Texture texturaNormal;
    private Texture texturaDificil;
    private Sound hoverSound;
    private Sound clickSound;
    private ImageButton btnFacil;
    private ImageButton btnNormal;
    private ImageButton btnDificil;

    public PantallaSeleccionDificultad(MiJuegoPrincipal juego) {
        this.juego = juego;
        System.out.println("PantallaSeleccionDificultad: Constructor llamado");
    }

    @Override
    public void show() {
        try {
            System.out.println("PantallaSeleccionDificultad: show() iniciado");
            stage = new Stage(new ScreenViewport());
            Gdx.input.setInputProcessor(stage);

            texturaTitulo = new Texture("selecciondificultad.png");
            texturaFacil = new Texture("facil.png");
            texturaNormal = new Texture("normal.png");
            texturaDificil = new Texture("dificil.png");
            hoverSound = Gdx.audio.newSound(Gdx.files.internal("hover.wav"));
            clickSound = Gdx.audio.newSound(Gdx.files.internal("sonidobotones.wav"));

            btnFacil = new ImageButton(new TextureRegionDrawable(texturaFacil));
            btnNormal = new ImageButton(new TextureRegionDrawable(texturaNormal));
            btnDificil = new ImageButton(new TextureRegionDrawable(texturaDificil));

            addHoverEffect(btnFacil);
            addHoverEffect(btnNormal);
            addHoverEffect(btnDificil);

            btnFacil.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    clickSound.play(0.8f);
                    juego.dificultadSeleccionada = Dificultad.FACIL;
                    juego.iniciarJuego();
                }
            });

            btnNormal.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    clickSound.play(0.8f);
                    juego.dificultadSeleccionada = Dificultad.NORMAL;
                    juego.iniciarJuego();
                }
            });

            btnDificil.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    clickSound.play(0.8f);
                    juego.dificultadSeleccionada = Dificultad.DIFICIL;
                    juego.iniciarJuego();
                }
            });

            Image titulo = new Image(texturaTitulo);

            Table tabla = new Table();
            tabla.setFillParent(true);
            tabla.align(Align.center);
            tabla.add(titulo).padBottom(40);
            tabla.row();
            tabla.add(btnFacil).width(260).height(90).padBottom(20);
            tabla.row();
            tabla.add(btnNormal).width(260).height(90).padBottom(20);
            tabla.row();
            tabla.add(btnDificil).width(260).height(90);
            stage.addActor(tabla);

            System.out.println("PantallaSeleccionDificultad: show() completado");

        } catch (Exception e) {
            System.err.println("PantallaSeleccionDificultad: ERROR en show()");
            e.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        try {
            Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if (stage == null) {
                show();
                return;
            }

            stage.act(delta);
            stage.draw();

        } catch (Exception e) {
            System.err.println("PantallaSeleccionDificultad: ERROR en render()");
            e.printStackTrace();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        System.out.println("PantallaSeleccionDificultad: hide() llamado");
        dispose();
    }

    @Override
    public void dispose() {
        try {
            System.out.println("PantallaSeleccionDificultad: dispose() llamado");
            if (stage != null) stage.dispose();
            if (texturaFacil != null) texturaFacil.dispose();
            if (texturaNormal != null) texturaNormal.dispose();
            if (texturaDificil != null) texturaDificil.dispose();
            if (texturaTitulo != null) texturaTitulo.dispose();
            if (hoverSound != null) hoverSound.dispose();
            if (clickSound != null) clickSound.dispose();
        } catch (Exception e) {
            System.err.println("PantallaSeleccionDificultad: ERROR en dispose()");
            e.printStackTrace();
        }
    }

    private void addHoverEffect(ImageButton button) {
        button.setTransform(true);
        button.setOrigin(Align.center);

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (hoverSound != null) {
                    hoverSound.play(0.5f);
                }
                button.clearActions();
                button.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                button.clearActions();
                button.addAction(Actions.scaleTo(1f, 1f, 0.1f));
            }
        });
    }
}
