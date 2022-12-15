package examen.ext201802;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;

import examen.ext201802.item.Arbol;

/** Clase con m�todos est�ticos para implementar algunas de las tareas del examen
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class Tareas {

	// TAREA 2
	public static void tarea2() {
		  TreeMap<String, TreeMap<String, Integer>> conteoArboles = new TreeMap<>();
		  for ( Arbol arbol : GrupoZonas.arbolesErandio ) {
			  Iterator<Zona> itZona = GrupoZonas.jardinesErandio.getIteradorZonas();
			  boolean enc = false;
			  while (!enc && itZona.hasNext()) {
				  Zona zona = itZona.next();
				  for ( ArrayList<PuntoGPS> subzona : zona.getPuntosGPS() ) {
					  if (UtilsGPS.gpsDentroDePoligono( arbol.getPunto(), subzona ) ) {
						  if (!conteoArboles.containsKey( zona.getCodigoZona())) conteoArboles.put( zona.getCodigoZona(), new TreeMap<>() );
						  TreeMap<String,Integer> arbolesEnZona = conteoArboles.get( zona.getCodigoZona() );
						  if (arbolesEnZona.containsKey( arbol.getNombre() )) {
							  arbolesEnZona.put( arbol.getNombre(), arbolesEnZona.get( arbol.getNombre() ) + 1 );
						  } else {
							  arbolesEnZona.put( arbol.getNombre(), 1 );
						  }
						  enc = true;
						  break;
					  }
				  }
			  }
		  }
		  for (String cz : conteoArboles.keySet()) {
			  TreeMap<String,Integer> arboles = conteoArboles.get( cz );
			  for (String tipoArbol : arboles.keySet()) {
				  int conteo = arboles.get( tipoArbol );
				  System.out.println( "Zona " + cz + " - " + conteo + " arboles " + tipoArbol );
			  }
		  }
	}
	
	// TAREA 4
	public static void tarea4( EdicionZonasGPS ventanaQueLlama ) {
		try {
		    Class.forName("org.sqlite.JDBC");
		    Connection con = DriverManager.getConnection("jdbc:sqlite:puntospantalla.bd" );
			Statement s = con.createStatement();
			s.setQueryTimeout(30);
			s.executeUpdate( "CREATE TABLE if not exists punto (zona string, latitud double, longitud double)" );
			s.executeUpdate( "DELETE from punto" );
			
			double loMin = ventanaQueLlama.xPantallaAlongGPS( 0 );
			double laMin = ventanaQueLlama.yPantallaAlatiGPS( 0 );
			double loMax = ventanaQueLlama.xPantallaAlongGPS( ventanaQueLlama.getPanelDibujo().getWidth() );
			double laMax = ventanaQueLlama.yPantallaAlatiGPS( ventanaQueLlama.getPanelDibujo().getHeight() );
		    GrupoZonas gZ = GrupoZonas.jardinesErandio;
		    Iterator<Zona> iZ = gZ.getIteradorZonas();
		    while (iZ.hasNext()) {
		    	Zona z = iZ.next();
		    	for (ArrayList<PuntoGPS> subzona : z.getPuntosGPS()) {
		    		for (PuntoGPS p : subzona) {
		    			if (p.getLongitud()>=loMin && p.getLongitud() <= loMax && p.getLatitud() <= laMin && p.getLatitud() >= laMax) {
		    				s.executeUpdate( "insert into punto values( '" + z.getCodigoZona() + "', " 
		    						+ p.getLatitud() + ", " + p.getLongitud() + ")" );
		    			}
		    		}
		    	}
		    }
			s.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// Base de datos de examen convocatoria ordinaria
	private static Connection conn = null;
	private static Statement stat = null;
	public static void tarea4ExamenOrdinaria() {
		ventana = EdicionZonasGPS.getVentana();  // Atributo de acceso a la ventana
		// 1.- Conexi�n de base de datos (que se conecte solo la primera vez)
		if (conn==null) {
			conn = BD.initBD( "arboles.bd" );
			stat = BD.usarCrearTablasBD( conn );
		}
		// 2.- Actualizaci�n de �rboles de zona seleccionada
		if (ventana.lZonas.getSelectedIndex()!=-1) {
			Zona zona = ventana.lZonas.getSelectedValue();
			// Cargar las dos listas - base de datos y mapa
			ArrayList<Arbol> arbolesDeZonaEnBD = BD.arbolSelect( stat, zona.getCodigoZona() );
			ArrayList<Arbol> arbolesDeZonaEnMapa = new ArrayList<>();
			for (Arbol arbol : GrupoZonas.arbolesErandio) {
				for (ArrayList<PuntoGPS> subzona : zona.getPuntosGPS()) {
					if (UtilsGPS.gpsDentroDePoligono( arbol.getPunto(), subzona )) {
						arbolesDeZonaEnMapa.add( arbol );
						break;
					}
				}
			}
			// Quitar las que est�n en los dos sitios
			for (int i=arbolesDeZonaEnBD.size()-1; i>=0; i--) {
				Arbol arbolEnBD = arbolesDeZonaEnBD.get( i );
				int enMapa = arbolesDeZonaEnMapa.indexOf( arbolEnBD );
				if (enMapa!=-1) {  // Si est� se quita de los dos sitios
					arbolesDeZonaEnBD.remove( i );
					arbolesDeZonaEnMapa.remove( enMapa );
				}
			}
			// Borrar los que est�n en BD, insertar los que est�n en mapa
			for (Arbol a : arbolesDeZonaEnBD) {
				BD.arbolDelete( stat, zona.getCodigoZona(), a.getPunto().getLatitud(), a.getPunto().getLongitud() );
			}
			for (Arbol a : arbolesDeZonaEnMapa) {
				BD.arbolInsert( stat, zona.getCodigoZona(), a );
			}
		}
		ventana.lMensaje.setText( "Finalizado proceso de BD." );
	}
	
	public static void finAplicacion() {
		if (conn!=null) {
			BD.cerrarBD( conn, stat );
		}
	}
	
	
	private static EdicionZonasGPS ventana;
	private static PuntoGPS[] centroide;
	public static void tarea5ExamenOrdinaria() {
		// 1.- Reajusta la ventana para que se vean las zonas completas y para que se vean los �rboles
		ventana = EdicionZonasGPS.getVentana();
		if (!ventana.cbArboles.isSelected()) {
			ventana.cbArboles.doClick();  // Activa la selecci�n de �rboles para que se vean
		}
		ventana.calcMoverACentroZonas();
		ventana.calculaMapa();
		// 2.- Plantea 3 centroides (en lugar de aleatorios -que es lo habitual- se ponen de forma arbitraria para que se pueda probar el algoritmo siempre con los mismos valores
		centroide = new PuntoGPS[] { new PuntoGPS( 43.319, -2.964 ), new PuntoGPS( 43.304, -2.979 ), new PuntoGPS( 43.298, -2.958 ) };
		// 3.- Llama al algoritmo propio para que esos centroides se vayan recalculando
		(new Thread() { public void run() { calculoCentroides( 0 ); ventana.lMensaje.setText( "Finalizado algoritmo de centroides." ); }}).start();
	}
		private static void calculoCentroides( int numPasoRecursivo ) {
			ventana.lMensaje.setText( "Inicio de paso " + numPasoRecursivo );
			Color[] color = { Color.RED, Color.BLUE, Color.GREEN };  // Array de colores asociados a cada centroide
			// 1.- Colorear los puntos dependiendo del centroide m�s cercano:
			//   centroide1 - Rojo, centroide2 - Azul, centroide3 - Verde
			int numCambios = 0;
			for (Arbol arbol : GrupoZonas.arbolesErandio) {
				double[] dist = new double[3];
				for (int i=0; i<3; i++) dist[i] = UtilsGPS.distanciaEntrePuntos( arbol.getPunto(), centroide[i] );
				if (dist[0]<=dist[1] && dist[0]<=dist[2]) {
					if (arbol.getColor() != color[0]) {
						numCambios++; arbol.setColor( color[0] );
					}
				} else if (dist[1]<=dist[0] && dist[1]<=dist[2]) {
					if (arbol.getColor() != color[1]) {
						numCambios++; arbol.setColor( color[1] );
					}
				} else {  // Es el centroide[2] -->  if (dist[2]<=dist[0] && dist[2]<=dist[1]) {
					if (arbol.getColor() != color[2]) {
						numCambios++; arbol.setColor( color[2] );
					}
				}
			}
			// 2.- Redibujar la pantalla con los �rboles recoloreados
			ventana.calculaMapa();
			// 3.- Dibujar centroides actuales
			for (int i=0; i<3; i++) {
				ventana.dibujaCirculo( centroide[i].getLongitud(), centroide[i].getLatitud(), 8, color[i], EdicionZonasGPS.stroke4, true );
				ventana.dibujaCruz( centroide[i].getLongitud(), centroide[i].getLatitud(), 30, Color.BLACK, EdicionZonasGPS.stroke2m, true );
			}
			if (numCambios==0) return; // Caso base
			// 4.- Esperar un par de segundos
			try { Thread.sleep( 2000 ); } catch (Exception e) {}
			// 5.- Calcular medias de cada uno de los grupos (colores de �rboles)
			double[] sumaLat = new double[3];
			double[] sumaLong = new double[3];
			int[] num = new int[3];
			for (Arbol arbol : GrupoZonas.arbolesErandio) {
				int numGrupo = Arrays.asList( color ).indexOf( arbol.getColor() );
				sumaLat[numGrupo] += arbol.getPunto().getLatitud(); 
				sumaLong[numGrupo] += arbol.getPunto().getLongitud();
				num[numGrupo]++;
			}
			// 6.- Recalcular los centroides con las medias
			for (int i=0; i<3; i++) {
				centroide[i].setLatitud( sumaLat[i]/num[i] );
				centroide[i].setLongitud( sumaLong[i]/num[i] );
			}
			// 7.- Completar recursividad...
			calculoCentroides( numPasoRecursivo + 1);
		}
}
