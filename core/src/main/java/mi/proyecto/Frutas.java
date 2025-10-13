package mi.proyecto;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import java.util.Random;

public class Frutas {
    Texture texFruta;
    private int x, y;

    public Frutas() {
        texFruta = new Texture("Frutaimg.png");
        Random random = new Random();
        int maxX = Gdx.graphics.getWidth() - 30;
        int maxY = Gdx.graphics.getHeight() - 30;
        x = random.nextInt(maxX);
        y = random.nextInt(maxY);
    }

    public void regenerar() {
        Random random = new Random();
        int maxX = Gdx.graphics.getWidth() - 30;
        int maxY = Gdx.graphics.getHeight() - 30;
        x = random.nextInt(maxX);
        y = random.nextInt(maxY);
    }


    public void draw(SpriteBatch batch) {
        batch.draw(texFruta, x, y, 30, 30);
    }

    public void dispose() {
        texFruta.dispose();
    }
}
