package mi.proyecto;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Frutas {
    Texture texFruta;
    private int x, y;

    public Frutas() {
        texFruta = new Texture("Frutaimg.png");
        x = 20;
        y = 20;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texFruta, x, y, 30, 30);
    }

    public void dispose() {
        texFruta.dispose();
    }
}
