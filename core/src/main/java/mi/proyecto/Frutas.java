package mi.proyecto;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Frutas {
    private Texture texture;
    private int x, y;

    public Frutas() {
        texture = new Texture("Fruta.png"); // tu imagen de fruta
        x = 20;
        y = 20;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, 20, 20);
    }

    public void dispose() {
        texture.dispose();
    }
}
