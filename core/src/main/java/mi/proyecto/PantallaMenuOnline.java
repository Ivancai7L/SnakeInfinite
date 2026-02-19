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

public class PantallaMenuOnline implements Screen {

    private final MiJuegoPrincipal game;
    private Stage stage;
    private Texture fondo;
    private BitmapFont font;
    private BitmapFont fontTitulo;
    private BitmapFont fontAyuda;
    private final List<Texture> texturasUI = new ArrayList<>();

    public PantallaMenuOnline(MiJuegoPrincipal game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        fondo = cargarTexturaConFallback("Fondosnake.jpg", 20, 20, 20);

        font = new BitmapFont();
        font.getData().setScale(1.85f);

        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(2.8f);

        fontAyuda = new BitmapFont();
        fontAyuda.getData().setScale(1.45f);

        Label.LabelStyle styleTitulo = new Label.LabelStyle(fontTitulo, new Color(1f, 0.86f, 0.34f, 1f));
        Label.LabelStyle styleAyuda = new Label.LabelStyle(fontAyuda, new Color(0.92f, 0.95f, 1f, 1f));

        TextButton btnServidor = crearBoton("ABRIR SERVIDOR", 50, 145, 50);
        TextButton btnCliente = crearBoton("ABRIR CLIENTE", 70, 95, 190);
        TextButton btnBack = crearBoton("VOLVER AL MENU", 120, 120, 120);

        btnServidor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.mostrarPantallaServidorOnline();
            }
        });

        btnCliente.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.mostrarPantallaClienteOnline();
            }
        });

        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.mostrarMenu();
            }
        });

        Label ayuda1 = new Label("1) En una PC abrí ABRIR SERVIDOR", styleAyuda);
        Label ayuda2 = new Label("2) En la misma u otra PC abrí ABRIR CLIENTE", styleAyuda);
        Label ayuda3 = new Label("3) El juego es de 2: servidor + 1 cliente", styleAyuda);
        ayuda1.setWrap(true);
        ayuda2.setWrap(true);
        ayuda3.setWrap(true);
        ayuda1.setAlignment(Align.center);
        ayuda2.setAlignment(Align.center);
        ayuda3.setAlignment(Align.center);

        Table table = new Table();
        table.setFillParent(true);
        table.align(Align.center);
        table.pad(24f);
        table.background(new TextureRegionDrawable(crearTexturaUI(1, 1, 8, 12, 22, 190)));

        table.add(new Label("ONLINE 2 JUGADORES", styleTitulo)).padBottom(18).width(640);
        table.row();
        table.add(ayuda1).padBottom(8).width(640);
        table.row();
        table.add(ayuda2).padBottom(8).width(640);
        table.row();
        table.add(ayuda3).padBottom(18).width(640);

        table.row();
        table.add(btnServidor).width(520).height(90).pad(10);
        table.row();
        table.add(btnCliente).width(520).height(90).pad(10);
        table.row();
        table.add(btnBack).width(520).height(90).pad(10);

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
        style.fontColor = new Color(0.96f, 0.99f, 1f, 1f);
        style.overFontColor = Color.WHITE;

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
        if (stage != null) {
            stage.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (fontTitulo != null) {
            fontTitulo.dispose();
        }
        if (fontAyuda != null) {
            fontAyuda.dispose();
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
