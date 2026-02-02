package mi.proyecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;

public class JuegoScreen implements Screen {

    private final MiJuegoPrincipal juego;
    private Snake snake;
    private Frutas fruta;
    private BitmapFont font;

    private int puntuacion;
    private boolean juegoTerminado;

    public JuegoScreen(MiJuegoPrincipal juego) {
        System.out.println("JuegoScreen: Constructor llamado");
        this.juego = juego;
        System.out.println("JuegoScreen: Constructor completado");
    }

    @Override
    public void show() {
        try {
            // Obtener velocidad según dificultad seleccionada
            float velocidad = 100f; // velocidad por defecto

            // Verificar si hay dificultad seleccionada
            if (juego.dificultadSeleccionada != null) {
                velocidad = juego.dificultadSeleccionada.getVelocidad() * 50;
            } else {
                // Si no hay dificultad, usar NORMAL por defecto
                juego.dificultadSeleccionada = Dificultad.NORMAL;
                velocidad = Dificultad.NORMAL.getVelocidad() * 50;
                System.out.println("Advertencia: No se seleccionó dificultad. Usando NORMAL por defecto.");
            }

            // Crear la serpiente
            snake = new Snake(velocidad, 20f);
            System.out.println("Snake creada con velocidad: " + velocidad);

            // Crear la fruta
            fruta = new Frutas();
            System.out.println("Frutas creadas");

            // Inicializar fuente para mostrar puntuación
            font = new BitmapFont();
            font.setColor(Color.WHITE);
            font.getData().setScale(2);

            puntuacion = 0;
            juegoTerminado = false;

            System.out.println("JuegoScreen inicializado correctamente");

        } catch (Exception e) {
            System.err.println("ERROR al inicializar JuegoScreen:");
            e.printStackTrace();
            // Si hay error, volver al menú
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    juego.mostrarMenu();
                }
            });
        }
    }

    @Override
    public void render(float delta) {
        try {
            // Limpiar pantalla
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if (!juegoTerminado && snake != null) {
                // Manejar input del teclado
                manejarInput();

                // Actualizar serpiente
                snake.actualizar(delta);

                // Verificar colisión con fruta
                verificarColisionConFruta();

                // Verificar colisiones con bordes
                verificarColisionConBordes();
            }

            // Dibujar todo
            juego.batch.begin();

            // Dibujar fruta
            if (fruta != null) {
                fruta.dibujar(juego.batch);
            }

            // Dibujar serpiente
            if (snake != null) {
                snake.dibujar(juego.batch);
            }

            // Dibujar puntuación
            if (font != null) {
                font.draw(juego.batch, "Puntuacion: " + puntuacion, 20, Gdx.graphics.getHeight() - 20);
                font.draw(juego.batch, "ESC para volver al menu", 20, Gdx.graphics.getHeight() - 50);
            }

            // Si el juego terminó, mostrar mensaje
            if (juegoTerminado && font != null) {
                font.draw(juego.batch, "GAME OVER! Presiona ESPACIO para reiniciar",
                    Gdx.graphics.getWidth() / 2f - 400,
                    Gdx.graphics.getHeight() / 2f);
            }

            juego.batch.end();

            // Volver al menú si se presiona ESC
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                juego.mostrarMenu();
            }

            // Reiniciar si se presiona ESPACIO y el juego terminó
            if (juegoTerminado && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                show(); // Reiniciar el juego
            }

        } catch (Exception e) {
            System.err.println("ERROR en render de JuegoScreen:");
            e.printStackTrace();
        }
    }

    private void manejarInput() {
        // Controles con flechas
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            snake.cambiarDireccion(Direccion.ARRIBA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            snake.cambiarDireccion(Direccion.ABAJO);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            snake.cambiarDireccion(Direccion.IZQUIERDA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            snake.cambiarDireccion(Direccion.DERECHA);
        }

        // Controles alternativos con WASD
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            snake.cambiarDireccion(Direccion.ARRIBA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            snake.cambiarDireccion(Direccion.ABAJO);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            snake.cambiarDireccion(Direccion.IZQUIERDA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            snake.cambiarDireccion(Direccion.DERECHA);
        }
    }

    private void verificarColisionConFruta() {
        if (snake == null || fruta == null) return;

        // Obtener la cabeza de la serpiente
        Vector2 cabeza = snake.getCabeza();
        Vector2 posFruta = fruta.getPosicion();

        // Verificar si la cabeza está cerca de la fruta (colisión simple)
        float distancia = cabeza.dst(posFruta);

        if (distancia < 25f) { // 25 es aproximadamente el tamaño de la fruta
            // La serpiente comió la fruta
            snake.comer();
            fruta.regenerar();
            puntuacion += 10;
            System.out.println("Fruta comida! Puntuación: " + puntuacion);
        }
    }

    private void verificarColisionConBordes() {
        if (snake == null) return;

        Vector2 cabeza = snake.getCabeza();

        // Verificar si la serpiente salió de los límites
        if (cabeza.x < 0 || cabeza.x > Gdx.graphics.getWidth() ||
            cabeza.y < 0 || cabeza.y > Gdx.graphics.getHeight()) {
            juegoTerminado = true;
            System.out.println("Game Over! Puntuación final: " + puntuacion);
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        try {
            if (snake != null) snake.dispose();
            if (fruta != null) fruta.dispose();
            if (font != null) font.dispose();
        } catch (Exception e) {
            System.err.println("Error al hacer dispose: " + e.getMessage());
        }
    }
}
