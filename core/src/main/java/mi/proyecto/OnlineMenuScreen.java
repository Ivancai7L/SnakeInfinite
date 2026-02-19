package mi.proyecto;

/**
 * Bridge de compatibilidad para conservar el nombre histórico de la pantalla online.
 * Mantiene funcionando cualquier referencia existente a OnlineMenuScreen.
 */
public class OnlineMenuScreen extends PantallaMenuOnline {

    public OnlineMenuScreen(MiJuegoPrincipal game) {
        super(game);
    }
}
