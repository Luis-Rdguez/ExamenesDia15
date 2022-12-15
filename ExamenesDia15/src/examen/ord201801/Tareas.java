package examen.ord201801;

import java.awt.Color;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import examen.ord201801.item.Arbol;


/** Clase con m�todos est�ticos para implementar algunas de las tareas del examen
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class Tareas {
	
	// TAREA 4 - Atributos necesarios para la tarea 4
	private static Connection con = null;
	private static Statement s = null;
	
	public static void tarea4() {
		ventana = EdicionZonasGPS.getVentana();  // Atributo de acceso a la ventana
		// TAREA 4
		if (con == null) {
			con = BD.initBD( "arboles.bd" );
			s = BD.usarCrearTablasBD( con );
		}
		if (ventana.lZonas.getSelectedIndex()!=-1) {
			Zona z = ventana.lZonas.getSelectedValue();
			ArrayList<Arbol> arbolesDeZonaEnBD = BD.arbolSelect( s, z.getCodigoZona() );
			ArrayList<Arbol> arbolesDeZonaEnMapa = new ArrayList<>();
			for (Arbol a : GrupoZonas.arbolesErandio) {
				for (ArrayList<PuntoGPS> subZona : z.getPuntosGPS()) {
					if (UtilsGPS.gpsDentroDePoligono( a.getPunto(), subZona )) {
						arbolesDeZonaEnMapa.add( a );
						break;
					}
				}
			}
			for (int i=arbolesDeZonaEnBD.size()-1; i>=0; i--) {
				Arbol arbolEnBD = arbolesDeZonaEnBD.get( i );
				int enMapa = arbolesDeZonaEnMapa.indexOf( arbolEnBD );
				if (enMapa!=-1) {
					arbolesDeZonaEnBD.remove( i );
					arbolesDeZonaEnMapa.remove( enMapa );
				}
			}
			for (Arbol a : arbolesDeZonaEnBD) {
				BD.arbolDelete( s, z.getCodigoZona(), a.getPunto().getLatitud(), a.getPunto().getLongitud() );
			}
			for (Arbol a : arbolesDeZonaEnMapa) {
				BD.arbolInsert( s, z.getCodigoZona(), a );
			}
		}
		ventana.lMensaje.setText( "Finalizado proceso de BD." );
	}
	
	public static void finAplicacion() {
		// TAREA 4 - Cierre al final de la aplicaci�n
		if ( con != null ) {
			BD.cerrarBD( con, s );
		}
	}
	
	
	// Atributos y m�todos de la tarea 5
	private static EdicionZonasGPS ventana;
	private static PuntoGPS[] centroide;
	public static void tarea5() {
		// 1.- Reajusta la ventana para que se vean las zonas completas y para que se vean los �rboles
		ventana = EdicionZonasGPS.getVentana();
		if (!ventana.cbArboles.isSelected()) {
			ventana.cbArboles.doClick();  // Activa la selecci�n de �rboles para que se vean
		}
		ventana.calcMoverACentroZonas();
		ventana.calculaMapa();
		// 2.- Plantea 3 centroides (en lugar de aleatorios -que es lo habitual- se ponen de forma arbitraria para que se pueda probar el algoritmo siempre con los mismos valores
		centroide = new PuntoGPS[] { new PuntoGPS( 43.319, -2.964 ), new PuntoGPS( 43.304, -2.979 ), new PuntoGPS( 43.298, -2.958 ) };
		// 3.- Llama al algoritmo propio de la tarea 5 para que esos centroides se vayan recalculando
		(new Thread() { public void run() { calculoCentroides( 0 ); ventana.lMensaje.setText( "Finalizado algoritmo de centroides." ); }}).start();
	}
	// TAREA 5 - Codificar este m�todo de forma recursiva completando los pasos sin codificar
	private static void calculoCentroides( int numPasoRecursivo ) {
		ventana.lMensaje.setText( "Inicio de paso " + numPasoRecursivo );
		Color[] color = { Color.RED, Color.BLUE, Color.GREEN };  // Array de colores asociados a cada centroide
		// 1.- Colorear los puntos dependiendo del centroide m�s cercano:
		int numCambios = 0;
		for (Arbol a : GrupoZonas.arbolesErandio) {
			double[] dist = new double[3];
			for (int i = 0; i < 3; i++) dist[i] = UtilsGPS.distanciaEntrePuntos( a.getPunto(), centroide[i] );
			if (dist[0] <= dist[1] && dist[0] <= dist[2]) {
				if (a.getColor() != color[0]) {
					numCambios++; 
					a.setColor( color[0] );
				}
			} else if (dist[1]<=dist[0] && dist[1]<=dist[2]) {
				if (a.getColor() != color[1]) {
					numCambios++; 
					a.setColor( color[1] );
				}
			} else {
				if (a.getColor() != color[2]) {
					numCambios++; 
					a.setColor( color[2] );
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
		if (numCambios==0) {
			return;
		}
		// 4.- Esperar un par de segundos
		try { 
			Thread.sleep( 2000 ); 
		} catch (Exception e) {
			
		}
		// 5.- Calcular medias de cada uno de los grupos (colores de �rboles)
		double[] sumaLt = new double[3];
		double[] sumaLg = new double[3];
		int[] num = new int[3];
		for (Arbol a : GrupoZonas.arbolesErandio) {
			int numGrupo = Arrays.asList( color ).indexOf( a.getColor() );
			sumaLt[numGrupo] += a.getPunto().getLatitud(); 
			sumaLg[numGrupo] += a.getPunto().getLongitud();
			num[numGrupo]++;
		}
		// 6.- Recalcular los centroides con las medias
		for (int i=0; i<3; i++) {
			centroide[i].setLatitud( sumaLt[i]/num[i] );
			centroide[i].setLongitud( sumaLg[i]/num[i] );
		}
		// 7.- Completar recursividad...
		calculoCentroides( numPasoRecursivo + 1);
	}
}
