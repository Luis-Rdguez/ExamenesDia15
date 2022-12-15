package examen.ext201802.item;

import java.awt.Color;

import examen.ext201802.*;

public class Arbol extends ItemEnMapa {
	int edadAproximadaEnAnyos;
	Color color;  // Color de dibujado de �rbol (gris por defecto)

	/** Crea un �rbol representable en un mapa GPS
	 * @param punto	Punto GPS del �rbol
	 * @param nombre	Especie del �rbol
	 * @param edadAproximada	Edad aproximada del �rbol en a�os
	 */
	public Arbol(PuntoGPS punto, String nombre, int edadAproximada ) {
		super(punto, nombre);
		this.edadAproximadaEnAnyos = edadAproximada;
		color = Color.GRAY;
	}

	/** Devuelve la edad del �rbol
	 * @return La edad aproximada del �rbol en a�os
	 */
	public int getEdadAproximada() {
		return edadAproximadaEnAnyos;
	}

	/**	Cambia la edad del �rbol
	 * @param edadAproximada La edad del �rbol aproximada en a�os
	 */
	public void setEdadAproximada(int edadAproximada ) {
		this.edadAproximadaEnAnyos = edadAproximada;
	}
	
	/** Devuelve el color utilizado para dibujar el �rbol
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/** Cambia el color de dibujado de �rbol
	 * @param color nuevo color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/** Dibuja el �tem en un mapa gr�fico de la ventana
	 * @param ventana	Ventana en la que dibujar
	 * @param pintaEnVentana	true si se quiere pintar inmediatamente en el mapa, false si se pinta en el objeto gr�fico pero no se muestra a�n en pantalla
	 */
	@Override
	public void dibuja(EdicionZonasGPS ventana, boolean pintaEnVentana) {
		ventana.dibujaCirculo( punto.getLongitud(), punto.getLatitud(), 4, color, EdicionZonasGPS.stroke4, true );
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Arbol) {
			Arbol a2 = (Arbol) obj;
			return punto.equals( a2.getPunto() ) && nombre.equals( a2.getNombre() );
		}
		return false;
	}
	
}
