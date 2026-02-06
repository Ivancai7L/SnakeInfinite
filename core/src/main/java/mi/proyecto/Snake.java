package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Snake {

    private final LinkedList<Vector2> cuerpo;
    private Direccion direccion;
    private final float velocidad;
    private final float tamano;
    private Texture textura;
    private int crecimientoPendiente;

    public Snake(float velocidad, float tamano) {
        this.velocidad = velocidad;
        this.tamano = tamano;

        try {
            if (Gdx.files.internal("Snakeimg.png").exists()) {
                this.textura = new Texture("Snakeimg.png");
                System.out.println("Textura Snakeimg.png cargada");
            } else {
                this.textura = crearTexturaColor(0, 255, 0);
                System.out.println("Textura Snakeimg.png no encontrada, usando textura verde generada");
            }
        } catch (Exception e) {
            System.out.println("Error al cargar Snakeimg.png: " + e.getMessage());
            this.textura = crearTexturaColor(0, 255, 0);
        }

        cuerpo = new LinkedList<>();
        float centroX = Gdx.graphics.getWidth() / 2f;
        float centroY = Gdx.graphics.getHeight() / 2f;
        cuerpo.add(new Vector2(centroX, centroY));
        direccion = Direccion.DERECHA;
        crecimientoPendiente = 0;
    }

    private Texture crearTexturaColor(int r, int g, int b) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pixmap.setColor(r / 255f, g / 255f, b / 255f, 1f);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    public void cambiarDireccion(Direccion nueva) {
        if (nueva == Direccion.ARRIBA && direccion == Direccion.ABAJO) return;
        if (nueva == Direccion.ABAJO && direccion == Direccion.ARRIBA) return;
        if (nueva == Direccion.IZQUIERDA && direccion == Direccion.DERECHA) return;
        if (nueva == Direccion.DERECHA && direccion == Direccion.IZQUIERDA) return;
        direccion = nueva;
    }

    public void actualizar(float delta) {
        mover(delta);
    }

    private void mover(float delta) {
        Vector2 cabeza = cuerpo.getFirst();
        Vector2 nueva = new Vector2(cabeza);

        switch (direccion) {
            case ARRIBA:
                nueva.y += velocidad * delta;
                break;
            case ABAJO:
                nueva.y -= velocidad * delta;
                break;
            case IZQUIERDA:
                nueva.x -= velocidad * delta;
                break;
            case DERECHA:
                nueva.x += velocidad * delta;
                break;
        }

        cuerpo.addFirst(nueva);
        if (crecimientoPendiente > 0) {
            crecimientoPendiente--;
        } else {
            cuerpo.removeLast();
        }
    }

    public void comer() {
        crecimientoPendiente++;
    }

    public void dibujar(SpriteBatch batch) {
        for (Vector2 p : cuerpo) {
            batch.draw(textura, p.x, p.y, tamano, tamano);
        }
    }

    public Vector2 getCabeza() {
        return cuerpo.getFirst();
    }

    public List<Vector2> getCuerpo() {
        return Collections.unmodifiableList(cuerpo);
    }

    public float getTamano() {
        return tamano;
    }

    public void dispose() {
        if (textura != null) textura.dispose();
    }
}
