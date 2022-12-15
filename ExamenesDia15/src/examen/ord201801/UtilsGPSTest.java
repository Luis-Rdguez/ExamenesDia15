package examen.ord201801;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

import examen.ord201801.item.Arbol;

public class UtilsGPSTest {

	@Test
	public void gpsDentroDePoligonoTest() {
		// TAREA 1
		for (Arbol a : GrupoZonas.arbolesErandio) {
			assertTrue( puntoEnAlgunaZona( a.getPunto() ) );
		}
		double lat1 = 43.323; double long1 = -2.990;
		double lat2 = 43.287; double long2 = -2.923;
		double incremeto = (lat2 - lat1)/100;
		for (double l = lat1; l < lat2; l += incremeto) {
			assertFalse( puntoEnAlgunaZona( new PuntoGPS( l, long1 ) ) );
			assertFalse( puntoEnAlgunaZona( new PuntoGPS( l, long2 ) ) );
		}
		incremeto = (long2 - long1)/100;
		for (double lg = long1; lg < long2; lg += incremeto) {
			assertFalse( puntoEnAlgunaZona( new PuntoGPS( lat1, lg ) ) );
			assertFalse( puntoEnAlgunaZona( new PuntoGPS( lat2, lg ) ) );
		}
	}
	
	private boolean puntoEnAlgunaZona( PuntoGPS punto ) {
		Iterator<Zona> itZ = GrupoZonas.jardinesErandio.getIteradorZonas();
		while (itZ.hasNext()) {
			Zona z = itZ.next();
			for (ArrayList<PuntoGPS> subZona : z.getPuntosGPS()) {
				if (UtilsGPS.gpsDentroDePoligono( punto, subZona )) {
					return true;
				}
			}
		}
		return false;
	}

}
