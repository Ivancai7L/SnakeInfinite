package mi.proyecto;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Frutas {
    private Texture textura;
    private float x, y;
    private float size;

    // Constructor con posición y tamaño personalizado
    public Frutas(float x, float y, float size) {
        textura = new Texture("Frutaimg.png");
        this.x = x;
        this.y = y;
        this.size = size;
    }

    // Constructor por defecto: posición aleatoria y tamaño 20
    public Frutas() {
        textura = new Texture("Frutaimg.png");
        this.size = 30;
        regenerar();
    }

    // Mueve la fruta a una nueva posición aleatoria
    public void regenerar() {
        x = MathUtils.random(0, com.badlogic.gdx.Gdx.graphics.getWidth() - size);
        y = MathUtils.random(0, com.badlogic.gdx.Gdx.graphics.getHeight() - size);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(textura, x, y, size, size);
    }

    public void dispose() {
        textura.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getSize() { return size; }
}
