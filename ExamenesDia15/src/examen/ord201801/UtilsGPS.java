package examen.ord201801;

import java.util.ArrayList;

/** Algunas utilidades para c�lculo y gesti�n de puntos GPS
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class UtilsGPS {

    /** Calcula si un punto es o no interior a un pol�gono cerrado
     * @param p	Punto GPS a chequear
     * @param puntos	Lista de puntos GPS que conforman un pol�gono cerrado (el primero es igual al �ltimo)
     * @return	true si el punto es interior al pol�gono, false en caso contrario 
     */
    public static boolean gpsDentroDePoligono( PuntoGPS p, ArrayList<PuntoGPS> puntos ) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = puntos.size()-1; i < puntos.size(); j = i++) {
            if ((puntos.get(i).getLatitud() > p.getLatitud()) != (puntos.get(j).getLatitud() > p.getLatitud()) &&
                    (p.getLongitud() < (puntos.get(j).getLongitud() - puntos.get(i).getLongitud()) * (p.getLatitud() - puntos.get(i).getLatitud()) / (puntos.get(j).getLatitud()-puntos.get(i).getLatitud()) + puntos.get(i).getLongitud())) {
                result = !result;
            }
        }
        return result;
    }
    
    /** Devuelve la distancia en dimensi�n GPS entre dos puntos
     * @param p1	Punto 1
     * @param p2	Punto 2
     * @return	Distancia pitag�rica entre los dos puntos (siempre positiva o cero)
     */
    public static double distanciaEntrePuntos( PuntoGPS p1, PuntoGPS p2 ) {
    	return Math.sqrt( (p1.getLatitud()-p2.getLatitud())*(p1.getLatitud()-p2.getLatitud()) + (p1.getLongitud()-p2.getLongitud())*(p1.getLongitud()-p2.getLongitud()) ); 
    }

}
