package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Frutas {

    public enum TipoFruta {
        MANZANA,
        BANANA,
        PERA
    }

    private static final float TAMANIO_BASE = 25f;
    private static final float ESCALA_PERA = 1.15f;

    private Texture texturaManzana;
    private Texture texturaBanana;
    private Texture texturaPera;
    private Vector2 posicion;
    private TipoFruta tipo;
    private float probBanana;
    private float probPera;

    public Frutas() {
        texturaManzana = cargarTexturaConFallback("Frutaimg.png", 220, 50, 50);
        texturaBanana = cargarTexturaConFallback("banana.png", 255, 225, 60);
        texturaPera = cargarTexturaConFallback("pera.png", 120, 220, 90);
        posicion = new Vector2();
        tipo = TipoFruta.MANZANA;
        probBanana = 0.10f;
        probPera = 0.08f;
        generarNuevaPosicion();
    }

    public void dibujar(SpriteBatch batch) {
        Texture textura = obtenerTexturaActual();
        float tam = obtenerTamanoActual();
        batch.draw(textura, posicion.x, posicion.y, tam, tam);
    }

    public void generarNuevaPosicion() {
        float tam = obtenerTamanoActual();
        int columnas = Math.max(1, (int) (Gdx.graphics.getWidth() / TAMANIO_BASE));
        int filas = Math.max(1, (int) (Gdx.graphics.getHeight() / TAMANIO_BASE));

        float x = MathUtils.random(0, columnas - 1) * TAMANIO_BASE;
        float y = MathUtils.random(0, filas - 1) * TAMANIO_BASE;

        x = Math.min(x, Gdx.graphics.getWidth() - tam);
        y = Math.min(y, Gdx.graphics.getHeight() - tam);
        posicion.set(x, y);

        sortearTipo();
    }

    private void sortearTipo() {
        float r = MathUtils.random();
        if (r < probPera) {
            tipo = TipoFruta.PERA;
        } else if (r < probPera + probBanana) {
            tipo = TipoFruta.BANANA;
        } else {
            tipo = TipoFruta.MANZANA;
        }
    }

    public void actualizarTipoPorPuntuacion(int puntuacion) {
        if (puntuacion >= 80) {
            probBanana = 0.20f;
            probPera = 0.16f;
        } else if (puntuacion >= 40) {
            probBanana = 0.16f;
            probPera = 0.12f;
        } else {
            probBanana = 0.10f;
            probPera = 0.08f;
        }
    }

    public void actualizarProbabilidades(Dificultad dificultad) {
        if (dificultad == null) {
            dificultad = Dificultad.NORMAL;
        }

        switch (dificultad) {
            case FACIL:
                probBanana = Math.max(probBanana, 0.12f);
                probPera = Math.max(probPera, 0.10f);
                break;
            case DIFICIL:
                probBanana = Math.max(probBanana, 0.18f);
                probPera = Math.max(probPera, 0.14f);
                break;
            case NORMAL:
            default:
                probBanana = Math.max(probBanana, 0.14f);
                probPera = Math.max(probPera, 0.11f);
                break;
        }
    }

    public Rectangle obtenerRectangulo() {
        float tam = obtenerTamanoActual();
        return new Rectangle(posicion.x, posicion.y, tam, tam);
    }

    public int getPuntos() {
        switch (tipo) {
            case BANANA:
                return 15;
            case PERA:
                return 20;
            case MANZANA:
            default:
                return 10;
        }
    }

    public TipoFruta getTipo() {
        return tipo;
    }

    public Vector2 getPosicion() {
        return posicion;
    }

    public void setPosicion(float x, float y) {
        float tam = obtenerTamanoActual();
        float clampedX = Math.max(0, Math.min(x, Gdx.graphics.getWidth() - tam));
        float clampedY = Math.max(0, Math.min(y, Gdx.graphics.getHeight() - tam));
        posicion.set(clampedX, clampedY);
    }

    public void setTipo(TipoFruta nuevoTipo) {
        if (nuevoTipo != null) {
            this.tipo = nuevoTipo;
        }
    }

    private float obtenerTamanoActual() {
        if (tipo == TipoFruta.PERA) {
            return TAMANIO_BASE * ESCALA_PERA;
        }
        return TAMANIO_BASE;
    }

    private Texture obtenerTexturaActual() {
        switch (tipo) {
            case BANANA:
                return texturaBanana != null ? texturaBanana : texturaManzana;
            case PERA:
                return texturaPera != null ? texturaPera : texturaManzana;
            case MANZANA:
            default:
                return texturaManzana;
        }
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
        if (texturaManzana != null) {
            texturaManzana.dispose();
            texturaManzana = null;
        }
        if (texturaBanana != null) {
            texturaBanana.dispose();
            texturaBanana = null;
        }
        if (texturaPera != null) {
            texturaPera.dispose();
            texturaPera = null;
        }
    }
}
