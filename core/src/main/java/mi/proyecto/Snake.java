package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

public class Snake {
    private final float size = 20f;              // Tamaño de cada bloque
    private final float moveDelay = 0.08f;       // Velocidad (tiempo entre pasos)
    private final ShapeRenderer shapeRenderer;
    private final ArrayList<float[]> cuerpo;     // Cada segmento es [x, y]
    private int dirX = 1, dirY = 0;              // Dirección inicial (derecha)
    private float timer = 0f;
    private int growCount = 0;                   // Segs por crecer

    public Snake() {
        shapeRenderer = new ShapeRenderer();
        cuerpo = new ArrayList<>();
        cuerpo.add(new float[]{100f, 100f});     // Posición inicial
    }

    // Actualiza movimiento
    public void update() {
        handleInput();
        timer += Gdx.graphics.getDeltaTime();
        if (timer >= moveDelay) {
            timer = 0f;
            step();
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && dirY == 0) {
            dirX = 0; dirY = 1;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && dirY == 0) {
            dirX = 0; dirY = -1;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && dirX == 0) {
            dirX = -1; dirY = 0;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && dirX == 0) {
            dirX = 1; dirY = 0;
        }
    }

    private void step() {
        float[] head = cuerpo.get(0);
        float newX = head[0] + dirX * size;
        float newY = head[1] + dirY * size;

        // Movimiento infinito (pasa bordes)
        if (newX < 0) newX = Gdx.graphics.getWidth() - size;
        if (newX >= Gdx.graphics.getWidth()) newX = 0;
        if (newY < 0) newY = Gdx.graphics.getHeight() - size;
        if (newY >= Gdx.graphics.getHeight()) newY = 0;

        // Nueva cabeza al frente
        cuerpo.add(0, new float[]{newX, newY});

        // Si no hay crecimiento, eliminar cola
        if (growCount > 0) growCount--;
        else cuerpo.remove(cuerpo.size() - 1);
    }

    // Crece 1 bloque
    public void crecer() { growCount++; }

    // Dibujo
    public void draw(SpriteBatch batch) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        for (float[] seg : cuerpo) {
            shapeRenderer.rect(seg[0], seg[1], size, size);
        }
        shapeRenderer.end();
        batch.begin();
    }

    // Colisión real
    public boolean colisionaConCuerpo() {
        float[] cabeza = cuerpo.get(0);
        for (int i = 1; i < cuerpo.size(); i++) {
            float[] seg = cuerpo.get(i);
            if (cabeza[0] == seg[0] && cabeza[1] == seg[1]) return true;
        }
        return false;
    }

    // Rect de colisión
    public Rectangle getRect() {
        float[] head = cuerpo.get(0);
        return new Rectangle(head[0], head[1], size, size);
    }

    // Datos útiles
    public float getX() { return cuerpo.get(0)[0]; }
    public float getY() { return cuerpo.get(0)[1]; }
    public float getSize() { return size; }
    public int getSegmentCount() { return cuerpo.size(); }
    public float getSegmentX(int i) { return cuerpo.get(i)[0]; }
    public float getSegmentY(int i) { return cuerpo.get(i)[1]; }

    public void dispose() { shapeRenderer.dispose(); }
}
