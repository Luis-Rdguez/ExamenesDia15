package examen.ext201802.item;

import examen.ext201802.*;

public abstract class ItemEnMapa {
	protected PuntoGPS punto;  // Lugar donde se encuentra el objeto en el mapa
	protected String nombre;   // Nombre del objeto
	
	/** Crea un �tem f�sico representable en una coordenada de mapa GPS
	 * @param punto	Punto GPS en el que se encuentra ese objeto
	 * @param nombre	Nombre del objeto
	 */
	public ItemEnMapa(PuntoGPS punto, String nombre) {
		super();
		this.punto = punto;
		this.nombre = nombre;
	}

	/**
	 * @return the punto
	 */
	public PuntoGPS getPunto() {
		return punto;
	}

	/**
	 * @param punto the punto to set
	 */
	public void setPunto(PuntoGPS punto) {
		this.punto = punto;
	}

	/**
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/** Dibuja el �tem en un mapa gr�fico de la ventana
	 * @param ventana	Ventana en la que dibujar
	 * @param pintaEnVentana	true si se quiere pintar inmediatamente en el mapa, false si se pinta en el objeto gr�fico pero no se muestra a�n en pantalla
	 */
	public abstract void dibuja( EdicionZonasGPS ventana, boolean pintaEnVentana );	
	
}
