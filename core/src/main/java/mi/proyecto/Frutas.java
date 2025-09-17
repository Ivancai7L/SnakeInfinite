package mi.proyecto;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Frutas {
    private Texture texture;
    private int x, y;

    public Frutas() {
        texture = new Texture("Fruta.png"); // tu imagen de fruta
        x = 200;
        y = 200;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }

    public void dispose() {
        texture.dispose();
    }
}
