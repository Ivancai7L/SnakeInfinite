package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.LinkedList;

public class Snake {

    private LinkedList<Vector2> cuerpo;
    private Direccion direccion;
    private float velocidad;
    private float tamano;
    private Texture textura;

    public Snake(float velocidad, float tamano) {
        this.velocidad = velocidad;
        this.tamano = tamano;

        // Intentar cargar textura, si falla crear una verde
        try {
            if (Gdx.files.internal("Snakeimg.png").exists()) {
                this.textura = new Texture("Snakeimg.png");
                System.out.println("Textura Snakeimg.png cargada");
            } else {
                // Crear textura verde usando Pixmap
                this.textura = crearTexturaColor(0, 255, 0); // Verde
                System.out.println("Textura Snakeimg.png no encontrada, usando textura verde generada");
            }
        } catch (Exception e) {
            System.out.println("Error al cargar Snakeimg.png: " + e.getMessage());
            this.textura = crearTexturaColor(0, 255, 0); // Verde
        }

        cuerpo = new LinkedList<>();
        // Posición inicial centrada en la pantalla
        float centroX = Gdx.graphics.getWidth() / 2f;
        float centroY = Gdx.graphics.getHeight() / 2f;
        cuerpo.add(new Vector2(centroX, centroY));
        direccion = Direccion.DERECHA;
    }

    /**
     * Crea una textura de un solo color usando Pixmap
     */
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
            case ARRIBA:    nueva.y += velocidad * delta; break;
            case ABAJO:     nueva.y -= velocidad * delta; break;
            case IZQUIERDA: nueva.x -= velocidad * delta; break;
            case DERECHA:   nueva.x += velocidad * delta; break;
        }

        cuerpo.addFirst(nueva);
        cuerpo.removeLast();
    }

    public void comer() {
        Vector2 ultima = cuerpo.getLast();
        cuerpo.add(new Vector2(ultima));
    }

    public void dibujar(SpriteBatch batch) {
        for (Vector2 p : cuerpo) {
            batch.draw(textura, p.x, p.y, tamano, tamano);
        }
    }

    /**
     * Obtiene la posición de la cabeza de la serpiente
     * @return Vector2 con la posición de la cabeza
     */
    public Vector2 getCabeza() {
        return cuerpo.getFirst();
    }

    /**
     * Obtiene el cuerpo completo de la serpiente
     * @return LinkedList con todas las posiciones del cuerpo
     */
    public LinkedList<Vector2> getCuerpo() {
        return cuerpo;
    }

    public void dispose() {
        if (textura != null) textura.dispose();
    }
}
