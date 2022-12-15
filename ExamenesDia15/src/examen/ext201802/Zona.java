package examen.ext201802;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Zona implements Comparable<Zona> {
	
    protected String codigoZona;  // Clave principal de zona (parte 1)
    protected String numDistrito;    // Clave principal de zona (parte 2)
    protected String nombreDistrito;
    protected String numZonaEnDistrito;
    protected String nombreZona;
    protected int colorRed;
    protected int colorGreen;
    protected int colorBlue;
    protected ArrayList<ArrayList<PuntoGPS>> puntosGPS; // Lista de listas de puntos GPS delimitantes de la zona  (hay varias porque puede haber m�s de una subzona independiente)

    /** Construye una zona geogr�fica nueva
     * @param codZona   	C�digo de zona (c�digo �nico)
     * @param numZona   	N�mero del distrito
     * @param nombreZona    Nombre del distrito
     * @param numArea   	N�mero de zona (dentro del distrito)
     * @param nombreArea    Nombre de la zona
     * @param colorRed		C�digo de color - componente rojo (R)
     * @param colorGreen	C�digo de color - componente rojo (G)
     * @param colorBlue		C�digo de color - componente rojo (B)
     * @param puntosGPS 	Lista cerrada de puntos GPS que delimitan la zona en formato array { { lg, lt, lg, lt ... }, { lg, lt, lg, lt ... } ... }
     */
    public Zona(String codZona, String numZona, String nombreZona, String numArea, String nombreArea, int colorRed, int colorGreen, int colorBlue, String puntosGPS ) {
        this.codigoZona = codZona;
        this.colorBlue = colorBlue;
        this.colorGreen = colorGreen;
        this.colorRed = colorRed;
        this.nombreDistrito = nombreZona;
        this.numDistrito = numZona;
        this.nombreZona = nombreArea;
        this.numZonaEnDistrito = numArea;
        setPuntosGPS( puntosGPS );
    }

	/**
	 * @return the numZona
	 */
	public String getNumZona() {
		return numDistrito;
	}

	/**
	 * @param numZona the numZona to set
	 */
	public void setNumZona(String numZona) {
		this.numDistrito = numZona;
	}

	/**
	 * @return the colorRed
	 */
	public int getColorRed() {
		return colorRed;
	}

	/**
	 * @return the numDistrito
	 */
	public String getNumDistrito() {
		return numDistrito;
	}

	/**
	 * @param numDistrito the numDistrito to set
	 */
	public void setNumDistrito(String numDistrito) {
		this.numDistrito = numDistrito;
	}

	/**
	 * @return the nombreDistrito
	 */
	public String getNombreDistrito() {
		return nombreDistrito;
	}

	/**
	 * @param nombreDistrito the nombreDistrito to set
	 */
	public void setNombreDistrito(String nombreDistrito) {
		this.nombreDistrito = nombreDistrito;
	}

	/**
	 * @return the numZonaEnDistrito
	 */
	public String getNumZonaEnDistrito() {
		return numZonaEnDistrito;
	}

	/**
	 * @param numZonaEnDistrito the numZonaEnDistrito to set
	 */
	public void setNumZonaEnDistrito(String numZonaEnDistrito) {
		this.numZonaEnDistrito = numZonaEnDistrito;
	}

	/**
	 * @return the nombreZona
	 */
	public String getNombreZona() {
		return nombreZona;
	}

	/**
	 * @param nombreZona the nombreZona to set
	 */
	public void setNombreZona(String nombreZona) {
		this.nombreZona = nombreZona;
	}

	/**
	 * @param colorRed the colorRed to set
	 */
	public void setColorRed(int colorRed) {
		this.colorRed = colorRed;
	}

	/**
	 * @return the colorGreen
	 */
	public int getColorGreen() {
		return colorGreen;
	}

	/**
	 * @param colorGreen the colorGreen to set
	 */
	public void setColorGreen(int colorGreen) {
		this.colorGreen = colorGreen;
	}

	/**
	 * @return the colorBlue
	 */
	public int getColorBlue() {
		return colorBlue;
	}

	/**
	 * @param colorBlue the colorBlue to set
	 */
	public void setColorBlue(int colorBlue) {
		this.colorBlue = colorBlue;
	}

	/**
	 * @return the codigoZona
	 */
	public String getCodigoZona() {
		return codigoZona;
	}
	
	/**
	 * @return the puntosGPS
	 */
	public ArrayList<ArrayList<PuntoGPS>> getPuntosGPS() {
		return puntosGPS;
	}
	
	/** Devuelve el n�mero de subzonas cerradas dentro de la zona
	 * @return	N�mero de subzonas
	 */
	public int getNumSubzonas() {
		return puntosGPS.size();
	}

    /** Calcula los puntos GPS de la zona partiendo de un array formateado del siguiente modo:
	 * { { long, lat, long, lat ... }, { long, lat, long, lat ... }, ... }
	 * siendo cada una de las series internas una subzona cerrada.
     * @param puntosGPS	Puntos GPS en el formato correcto
     */
    public void setPuntosGPS( String puntosGPS ) {
        this.puntosGPS = new ArrayList<>();
        StringTokenizer st = new StringTokenizer( puntosGPS, "{}" );
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (!token.isEmpty() && !token.equals(",")) {
                ArrayList<PuntoGPS> lP = new ArrayList<>();
                // System.out.println( "-- " + token );
                StringTokenizer st2 = new StringTokenizer( token, "," );
                while (st2.hasMoreTokens()) {
                    String longitud = st2.nextToken();
                    if (st2.hasMoreTokens()) {
                        String latitud = st2.nextToken();
                        try {
                            double longi = Double.parseDouble( latitud );
                            double lati = Double.parseDouble( longitud );
                            lP.add( new PuntoGPS( lati, longi ) );
                            // System.out.println( "  -> " + "( " + longi + "," + lati + ")  -  " + longitud + " , " + latitud );
                        } catch (Exception e) {
                        }
                    }
                }
                // System.out.println( "Fin subzona" );
                if (!lP.isEmpty()) this.puntosGPS.add( lP );
            }
        }
    }

	

    /** Devuelve el n�mero de puntos relevantes de toda la zona (no incluye los puntos de cierre en el conteo)
     * @return	N�mero de puntos diferentes de la zona
     */
    public int getNumPuntos() {
        int numPuntos = 0;
        for (ArrayList<PuntoGPS> l : puntosGPS) numPuntos += (l.size() - 1);
        return numPuntos;
    }
    
    
    @Override
    public int compareTo(Zona o) {
        return codigoZona.compareTo( o.codigoZona );
    }

    @Override
    public String toString() {
    	return codigoZona + " - " + nombreZona;
    }
	
	
}
