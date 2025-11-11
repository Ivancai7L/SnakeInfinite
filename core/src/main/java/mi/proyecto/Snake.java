package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Snake {
    private Texture textura;
    private ArrayList<Vector2> cuerpo;
    private float velocidad;
    private float tamaño;
    private float rotacion;
    private int crecimiento;

    public Snake() {
        textura = new Texture("Snakeimg.png");
        tamaño = 20f;
        velocidad = 160f;
        rotacion = 0f;
        crecimiento = 0;
        cuerpo = new ArrayList<>();
        cuerpo.add(new Vector2(100, 100));
    }

    public void update(float delta) {
        // Rotación con flechas izquierda/derecha
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) rotacion += 180 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) rotacion -= 180 * delta;

        // Mover cabeza hacia adelante constantemente
        Vector2 cabeza = cuerpo.get(0);
        Vector2 nuevaPos = new Vector2(
            cabeza.x + (float) Math.cos(Math.toRadians(rotacion)) * velocidad * delta,
            cabeza.y + (float) Math.sin(Math.toRadians(rotacion)) * velocidad * delta
        );

        // Agregar nueva cabeza al principio
        cuerpo.add(0, nuevaPos);

        // Limitar longitud del cuerpo (elimina último segmento)
        while (cuerpo.size() > getLongitudDeseada()) {
            cuerpo.remove(cuerpo.size() - 1);
        }

        // Teletransporte en bordes del mapa
        if (nuevaPos.x < 0) nuevaPos.x = Gdx.graphics.getWidth();
        if (nuevaPos.x > Gdx.graphics.getWidth()) nuevaPos.x = 0;
        if (nuevaPos.y < 0) nuevaPos.y = Gdx.graphics.getHeight();
        if (nuevaPos.y > Gdx.graphics.getHeight()) nuevaPos.y = 0;
    }

    private int getLongitudDeseada() {
        return 20 + crecimiento * 15;
    }

    public void crecer() {
        crecimiento++;
    }

    public void draw(SpriteBatch batch) {
        for (Vector2 seg : cuerpo) {
            batch.draw(textura, seg.x, seg.y, tamaño, tamaño);
        }
    }

    public Vector2 getCabeza() {
        return cuerpo.get(0);
    }

    public ArrayList<Vector2> getCuerpo() {
        return cuerpo;
    }

    public void dispose() {
        textura.dispose();
    }
}
