package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Snake {
    private float x, y;
    private float size;
    private float speed;
    private ShapeRenderer shapeRenderer;

    public Snake() {
        x = 100;
        y = 100;
        size = 20;
        speed = 100;
        shapeRenderer = new ShapeRenderer();
    }

    public void update() {
        // Tiempo delta para movimiento suave (independiente de los FPS)
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            y += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            y -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += speed * delta;
        }
    }

    public void draw(SpriteBatch batch) {
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
