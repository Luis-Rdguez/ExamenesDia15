package examen.ext201802;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

import examen.ext201802.item.Arbol;

public class UtilsGPSTest {

	// TAREA 1 - Escribe a continuaci�n en estos comentarios si has encontrado zonas err�neas
	//
	// Zonas 63 y 77 son las zonas erroneas que he encontrado.
	// 

	@Test
	public void areasErandioCorrectas() {
		// TAREA 1 - Comprobaci�n de areas > 0
	    GrupoZonas GZ = GrupoZonas.jardinesErandio;
	    Iterator<Zona> iz = GZ.getIteradorZonas();
	    while (iz.hasNext()) {
	    	Zona z = iz.next();
	    	double valArea = 0.0;
	    	for (ArrayList<PuntoGPS> subzona : z.getPuntosGPS()) {
	        	valArea += UtilsGPS.areaDePoligono( subzona );
	    	}
	    	assertTrue( "Zona " + z.getCodigoZona() + "area total = "+ valArea, valArea > 0 );
	    }
	}
	
	@Test
	public void interseccionesErandioCorrectas() {
		// TAREA 1 - Comprobaci�n de intersecciones == 0
		GrupoZonas g = GrupoZonas.jardinesErandio;
		Iterator<Zona> iz = g.getIteradorZonas();
		while (iz.hasNext()) {
			Zona zona = iz.next();
		    double interseccion = UtilsGPS.interseccionEntrePoligonos( zona.getPuntosGPS() );
		    assertTrue( "Zona " + zona.getCodigoZona() + " intersecci�n = " + interseccion, interseccion==0.0 );
		 }
	}
	
	@Test
	public void gpsDentroDePoligonoTest() {
		// Todos los �rboles est�n en zonas
		for (Arbol arbol : GrupoZonas.arbolesErandio) {
			assertTrue( puntoEstaEnAlgunaZona( arbol.getPunto() ) );
		}
		// Todos los bordes no est�n en zonas
		double lat1 = 43.323; double long1 = -2.990;
		double lat2 = 43.287; double long2 = -2.923;
		double incr = (lat2-lat1)/100;
		for (double lat = lat1; lat<lat2; lat += incr) {
			assertFalse( puntoEstaEnAlgunaZona( new PuntoGPS( lat, long1 ) ) );
			assertFalse( puntoEstaEnAlgunaZona( new PuntoGPS( lat, long2 ) ) );
		}
		incr = (long2-long1)/100;
		for (double lon = long1; lon<long2; lon += incr) {
			assertFalse( puntoEstaEnAlgunaZona( new PuntoGPS( lat1, lon ) ) );
			assertFalse( puntoEstaEnAlgunaZona( new PuntoGPS( lat2, lon ) ) );
		}
	}
		// M�todo para comprobar todas las zonas
		private boolean puntoEstaEnAlgunaZona( PuntoGPS punto ) {
			Iterator<Zona> itZona = GrupoZonas.jardinesErandio.getIteradorZonas();
			while (itZona.hasNext()) {
				Zona zona = itZona.next();
				for (ArrayList<PuntoGPS> subzona : zona.getPuntosGPS()) {
					if (UtilsGPS.gpsDentroDePoligono( punto, subzona )) return true;
				}
			}
			return false;
		}

}
