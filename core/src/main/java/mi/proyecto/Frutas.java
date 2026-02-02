package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;

public class Frutas {

    private static final float TAMANIO = 20f;
    private static final float MARGEN = 50f; // Margen desde los bordes

    private final Random random;

    private Texture textura;
    private float x;
    private float y;

    public Frutas() {
        random = new Random();

        // Intentar cargar textura, si falla crear una roja
        try {
            if (Gdx.files.internal("Frutaimg.png").exists()) {
                this.textura = new Texture("Frutaimg.png");
                System.out.println("Textura Frutaimg.png cargada");
            } else {
                // Crear textura roja usando Pixmap
                this.textura = crearTexturaColor(255, 0, 0); // Rojo
                System.out.println("Textura Frutaimg.png no encontrada, usando textura roja generada");
            }
        } catch (Exception e) {
            System.out.println("Error al cargar Frutaimg.png: " + e.getMessage());
            this.textura = crearTexturaColor(255, 0, 0); // Rojo
        }

        regenerar();
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

    public void regenerar() {
        // Generar posición aleatoria dentro de los límites de la ventana
        float anchoVentana = Gdx.graphics.getWidth();
        float altoVentana = Gdx.graphics.getHeight();

        // Generar entre el margen y el límite menos el margen y el tamaño de la fruta
        x = MARGEN + random.nextFloat() * (anchoVentana - 2 * MARGEN - TAMANIO);
        y = MARGEN + random.nextFloat() * (altoVentana - 2 * MARGEN - TAMANIO);
    }

    public void dibujar(SpriteBatch batch) {
        batch.draw(textura, x, y, TAMANIO, TAMANIO);
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, TAMANIO, TAMANIO);
    }

    public Vector2 getPosicion() {
        return new Vector2(x, y);
    }

    public void dispose() {
        if (textura != null) textura.dispose();
    }
}
