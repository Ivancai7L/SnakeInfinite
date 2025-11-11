package mi.proyecto;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class Frutas {
    private Texture textura;
    private float x, y;
    private float tamaño;
    private Random random;

    private static final int MAPA_ANCHO = 4000;
    private static final int MAPA_ALTO = 4000;

    public Frutas() {
        textura = new Texture("Frutaimg.png");
        tamaño = 20f;
        random = new Random();
        regenerar();
    }

    public void regenerar() {
        x = random.nextInt(MAPA_ANCHO - (int) tamaño);
        y = random.nextInt(MAPA_ALTO - (int) tamaño);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(textura, x, y, tamaño, tamaño);
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, tamaño, tamaño);
    }

    public void dispose() {
        textura.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
