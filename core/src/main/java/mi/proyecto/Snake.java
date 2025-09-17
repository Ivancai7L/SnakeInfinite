package mi.proyecto;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Snake {
    private float x, y;
    private float size;
    private ShapeRenderer shapeRenderer;

    public Snake() {
        x = 100;
        y = 100;
        size = 20;
        shapeRenderer = new ShapeRenderer();
    }

    public void update() {
        // por ahora la serpiente no se mueve, después agregamos controles
    }

    public void draw(SpriteBatch batch) {
        // Como ShapeRenderer no usa SpriteBatch, lo dibujamos aparte
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y, size, size);
        shapeRenderer.end();

        batch.begin();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
