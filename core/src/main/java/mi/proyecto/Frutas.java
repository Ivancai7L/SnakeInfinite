package mi.proyecto;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Frutas {
    private Texture textura;
    private float x, y;
    private float size = 20f;

    public Frutas() {
        textura = new Texture("Frutaimg.png");
        regenerar(null);
    }

    // Regenera en posición aleatoria (sin tocar serpiente)
    public void regenerar(Snake snake) {
        boolean sobreSerpiente;
        do {
            sobreSerpiente = false;
            x = MathUtils.floor(MathUtils.random(0, (float)Math.floor(com.badlogic.gdx.Gdx.graphics.getWidth() / size))) * size;
            y = MathUtils.floor(MathUtils.random(0, (float)Math.floor(com.badlogic.gdx.Gdx.graphics.getHeight() / size))) * size;

            if (snake != null) {
                for (int i = 0; i < snake.getSegmentCount(); i++) {
                    if (snake.getSegmentX(i) == x && snake.getSegmentY(i) == y) {
                        sobreSerpiente = true;
                        break;
                    }
                }
            }
        } while (sobreSerpiente);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(textura, x, y, size, size);
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, size, size);
    }

    public void dispose() {
        textura.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getSize() { return size; }
}
