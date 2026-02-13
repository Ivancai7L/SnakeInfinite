package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Piedra {

    private final float tamano;
    private final float margen;
    private Texture textura;
    private final Vector2 posicion = new Vector2();

    public Piedra(float tamano, float margen) {
        this.tamano = tamano;
        this.margen = margen;
        this.textura = cargarTexturaConFallback("piedra.png", 120, 120, 120);
        generarNuevaPosicion();
    }

    public void generarNuevaPosicion() {
        float minX = margen;
        float minY = margen;
        float maxX = Math.max(minX, Gdx.graphics.getWidth() - tamano - margen);
        float maxY = Math.max(minY, Gdx.graphics.getHeight() - tamano - margen);

        int columnas = Math.max(1, (int) ((maxX - minX) / tamano));
        int filas = Math.max(1, (int) ((maxY - minY) / tamano));

        float x = minX + MathUtils.random(0, columnas - 1) * tamano;
        float y = minY + MathUtils.random(0, filas - 1) * tamano;

        x = Math.min(x, maxX);
        y = Math.min(y, maxY);
        posicion.set(x, y);
    }

    public void dibujar(SpriteBatch batch) {
        if (textura != null) {
            batch.draw(textura, posicion.x, posicion.y, tamano, tamano);
        }
    }

    public Rectangle obtenerRectangulo() {
        return new Rectangle(posicion.x, posicion.y, tamano, tamano);
    }

    public Vector2 getPosicion() {
        return posicion;
    }

    public float getTamano() {
        return tamano;
    }

    private Texture cargarTexturaConFallback(String ruta, int r, int g, int b) {
        try {
            if (Gdx.files.internal(ruta).exists()) {
                return new Texture(ruta);
            }
        } catch (Exception ignored) {
        }

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r / 255f, g / 255f, b / 255f, 1f);
        pixmap.fill();
        Texture fallback = new Texture(pixmap);
        pixmap.dispose();
        return fallback;
    }

    public void dispose() {
        if (textura != null) {
            textura.dispose();
            textura = null;
        }
    }
}
