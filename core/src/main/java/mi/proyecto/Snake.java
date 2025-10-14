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

    public Snake(float x, float y, float speed) {
        this.x = x;
        this.y = y;
        this.size = 20;
        this.speed = speed;
        shapeRenderer = new ShapeRenderer();
    }

    public Snake() {
        this(100, 100, 200); // velocidad por defecto
    }

    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            y += speed * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            y -= speed * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= speed * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += speed * delta;
        }

        // Limitar al área de la pantalla
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > Gdx.graphics.getWidth() - size) x = Gdx.graphics.getWidth() - size;
        if (y > Gdx.graphics.getHeight() - size) y = Gdx.graphics.getHeight() - size;
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

    // Getters para colisiones
    public float getX() { return x; }
    public float getY() { return y; }
    public float getSize() { return size; }
}
