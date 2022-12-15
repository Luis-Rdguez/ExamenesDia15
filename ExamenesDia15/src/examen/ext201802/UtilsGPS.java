package examen.ext201802;

import static java.lang.Double.NaN;
import java.awt.geom.*;
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
    
    /** Devuelve la distancia en dimensi�n GPS entre dos puntos
     * @param long1	Longitud de punto 1
     * @param lat1	Latitud de punto 1
     * @param long2	Longitud de punto 2
     * @param lat2	Latitud de punto 2
     * @return	Distancia pitag�rica entre los dos puntos (siempre positiva o cero)
     */
    public static double distanciaEntrePuntos( double long1, double lat1, double long2, double lat2 ) {
    	return Math.sqrt( (lat1-lat2)*(lat1-lat2) + (long1-long2)*(long1-long2) ); 
    }
    
    /** Devuelve el �rea aproximada de una zona GPS marcada por sus puntos exteriores
     * @param subzonaGPS	Lista de puntos de la zona
     * @return	�rea aproximada, en la escala de los puntos GPS
     */
    public static double areaDePoligono( ArrayList<PuntoGPS> subzonaGPS ) {
    	Path2D.Double path = null;
    	for (PuntoGPS punto : subzonaGPS) {
    		if (path==null) {
    			path = new Path2D.Double();
    			path.moveTo( punto.getLongitud(), punto.getLatitud() );
    		} else {
    			path.lineTo( punto.getLongitud(),  punto.getLatitud() );
    		}
    	}
        Area area = new Area( path );
        return approxAreaSinCurvas( area );
    }
    
    /** Devuelve el �rea aproximada de la interseccion de subzonas GPS marcadas por sus puntos exteriores
     * @param zonasGPS	Lista de listas de puntos (subzonas gps)
     * @return	�rea aproximada del �rea com�n de esas subzonas. Si no hay coincidencia, devuelve 0.0
     */
    public static double interseccionEntrePoligonos( ArrayList<ArrayList<PuntoGPS>> zonasGPS ) {
        ArrayList<Path2D.Double> l = new ArrayList<>();
    	for (ArrayList<PuntoGPS> subzona : zonasGPS) {
        	Path2D.Double path = null;
        	for (PuntoGPS punto : subzona) {
	    		if (path==null) {
	    			path = new Path2D.Double();
	    			path.moveTo( punto.getLongitud(), punto.getLatitud() );
	    		} else {
	    			path.lineTo( punto.getLongitud(),  punto.getLatitud() );
	    		}
        	}
        	l.add( path );
    	}
    	double interseccion = 0.0;
    	for (int i=0; i<l.size(); i++) {
    		Path2D.Double path = l.get(i);
    		for (int j=i+1; j<l.size(); j++) {
        		Path2D.Double path2 = l.get(j);
    	    	Area intersec = new Area( path );
    	    	intersec.intersect( new Area( path2 ) );
    	    	interseccion += approxAreaSinCurvas( intersec );
    		}
    	}
    	return interseccion;
    }

    	// M�todos privados para el c�lculo de �reas
	    private static double approxAreaSinCurvas(Area area) {
	    	PathIterator i = area.getPathIterator(identity);
	    	return approxArea(i);
	    }
	    private static double approxArea(PathIterator i) {
	    	double a = 0.0;
	    	double[] coords = new double[6];
	    	double startX = NaN, startY = NaN;
	    	Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);
	    	while (! i.isDone()) {
	    		int segType = i.currentSegment(coords);
	    		double x = coords[0], y = coords[1];
	    		switch (segType) {
	    		case PathIterator.SEG_CLOSE:
	    			segment.setLine(segment.getX2(), segment.getY2(), startX, startY);
	    			a += hexArea(segment);
	    			startX = startY = NaN;
	    			segment.setLine(NaN, NaN, NaN, NaN);
	    			break;
	    		case PathIterator.SEG_LINETO:
	    			segment.setLine(segment.getX2(), segment.getY2(), x, y);
	    			a += hexArea(segment);
	    			break;
	    		case PathIterator.SEG_MOVETO:
	    			startX = x;
	    			startY = y;
	    			segment.setLine(NaN, NaN, x, y);
	    			break;
	    		default:
	    			throw new IllegalArgumentException("PathIterator contiene segmentos curvos");
	    		}
	    		i.next();
	    	}
	    	if (java.lang.Double.isNaN(a)) {
	    		throw new IllegalArgumentException("PathIterator contiene un path abierto");
	    	} else {
	    		return 0.5 * Math.abs(a);
	    	}
	    }
	    private static double hexArea(Line2D seg) {
	    	return seg.getX1() * seg.getY2() - seg.getX2() * seg.getY1();
	    }
	    private static final AffineTransform identity = AffineTransform.getQuadrantRotateInstance(0);

}
