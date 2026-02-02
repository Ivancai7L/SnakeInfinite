package mi.proyecto;

public enum Dificultad {

    FACIL(2),
    NORMAL(4),
    DIFICIL(6),
    IMPOSIBLE(10);

    private final float velocidad;

    Dificultad(float velocidad) {
        this.velocidad = velocidad;
    }

    public float getVelocidad() {
        return velocidad;
    }
}
