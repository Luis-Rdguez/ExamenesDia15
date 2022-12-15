package examen.ext201802;

/** Punto de coordenadas GPS est�ndar (utilizado, por ejemplo, por google maps)
 */
public class PuntoGPS {
    private double latitud;  // Latitud gps en forma est�ndar de valor double  (p ej 43.263 para la plaza Moy�a) (distancia angular al ecuador)
    private double longitud; // Longitud gps en forma est�ndar de valor double (p ej -2.935 para la plaza Moy�a) (distancia angular al meridiano de Greenwich)
    
    /**
     * @param latitud
     * @param longitud
     */
    public PuntoGPS(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }
    
    /**
	 * @return the latitud
	 */
	public double getLatitud() {
		return latitud;
	}

	/**
	 * @param latitud the latitud to set
	 */
	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}

	/**
	 * @return the longitud
	 */
	public double getLongitud() {
		return longitud;
	}

	/**
	 * @param longitud the longitud to set
	 */
	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	@Override
    public String toString() {
        return latitud + "," + longitud;
    }
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PuntoGPS) {
			PuntoGPS p2 = (PuntoGPS) obj;
			return latitud==p2.latitud && longitud==p2.longitud;
		}
		return false;
	}
}

