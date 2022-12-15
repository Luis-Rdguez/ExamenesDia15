package examen.ext201802;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import examen.ext201802.item.Arbol;

/** Clase principal ejecutable con ventana de visualizaci�n de zonas GPS. 
 * Inicializada para representar zonas ajardinadas de Erandio (m�s o menos realistas)
 * y una serie de �rboles de Erandio (ficticios)
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
@SuppressWarnings("serial")
public class EdicionZonasGPS extends JFrame {
	// Pinceles generales con los que dibujar
	/** Pincel de 4 p�xels de grosor */
	public static final Stroke stroke4 = new BasicStroke( 4.0f );
	/** Pincel de 2 p�xels y medio de grosor */
	public static final Stroke stroke2m = new BasicStroke( 2.5f );
	/** Pincel de 1 p�xel y medio de grosor */
	public static final Stroke stroke1m = new BasicStroke( 1.5f );

	// Ventana principal �nica creada por la ejecuci�n de esta clase como principal
	private static EdicionZonasGPS ventana;
	
	/** Devuelve la instancia �nica de ventana creada por esta clase (singleton)
	 * @return	Ventana ya creada - debe haberse ejecutado antes {@link #main(String[])}
	 */
	public static EdicionZonasGPS getVentana() {
		return ventana;
	}
	
	/** M�todo principal de la clase. Crea y muestra una ventana de zonas de Erandio
	 * @param args	No utilizado
	 */
	public static void main(String[] args) {
		ventana = new EdicionZonasGPS();
		ventana.setVisible( true );
	}
	
	// Constantes de la ventana
	private static final int ANCH_MAX = 3000;   // M�xima anchura de dibujado del objeto gr�fico
	private static final int ALT_MAX = 2000;    // M�xima altura de dibujado del objeto gr�fico
	// Componentes gr�ficos de la ventana
	JLabel lMensaje = new JLabel( " " );
	private JLabel lMensaje2 = new JLabel( " " );
	private ImageIcon mapa;
	private BufferedImage biImagen = new BufferedImage( ANCH_MAX, ALT_MAX, BufferedImage.TYPE_INT_RGB );
	private Graphics2D graphics;
	JCheckBox cbArboles;
	private JToggleButton tbMover = new JToggleButton( "Mover" );
	private JToggleButton tbZoom = new JToggleButton( "Zoom" );
	private JToggleButton tbGeoposicionar = new JToggleButton( "Geoposicionar" );
	private JToggleButton tbMoverArbol = new JToggleButton( "Mover �rbol" );
	private JButton bCentrar = new JButton( "Centrar" );
	DefaultListModel<Zona> mZonas = new DefaultListModel<>();
	JList<Zona> lZonas = new JList<Zona>( mZonas );
	private JPanel pDibujo;
	// Valores para el dibujado: escalado, desplazado, geolocalizaci�n
	private double zoomX = 1.0;         // A cu�ntos pixels de pantalla corresponden los pixels del gr�fico en horizontal. 2.0 = aumentado. 0.5 = disminuido
	private double zoomY = 1.0;         // En vertical
	private double offsetX = 0.0;       // Qu� p�xel x de pantalla marca el origen del gr�fico
	private double offsetY = 0.0;       // Qu� p�xel y de pantalla marca el origen del gr�fico
	private double xImagen1, yImagen1;  // Punto 1 en la imagen asociado a una coordenada GPS
	private double longGPS1, latiGPS1;        // Coordenada GPS asociada al punto 1  (x = longitud, y = latitud)
	private double xImagen2, yImagen2;  // Punto 1 en la imagen asociado a una coordenada GPS
	private double longGPS2, latiGPS2;        // Coordenada GPS asociada al punto 1  (x = longitud, y = latitud)
	double relGPSaGrafX;                // Relaci�n horizontal entre longitud (GPS) y x del gr�fico
	double relGPSaGrafY;                // Relaci�n vertical entre latitud (GPS) y x del gr�fico
	double origenXGPSEnGraf;            // Pixel x virtual del gr�fico en el que est� el origen de longitud GPS
	double origenYGPSEnGraf;            // Pixel y virtual del gr�fico en el que est� el origen de latitud GPS
	boolean dibujadoArboles;            // Si es true se dibujan los �rboles, si no no se dibujan
	
	private JTable tDatos;
	private DefaultTableModel mDatos;

	
	/** Construye una ventana de dibujado y edici�n de zonas GPS
	 */
	public EdicionZonasGPS() {
		// Acciones generales de ventana
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setSize( 1600, 1000 );
		setTitle( "Edici�n de zonas geoposicionadas (Erandio) - Examen resuelto" );
		// Creaci�n de objetos gr�ficos
		mapa = new ImageIcon( this.getClass().getResource( "img/mapa-erandio.jpg" ) );
		graphics = (Graphics2D) biImagen.getGraphics();
		// Componentes y contenedores visuales y asignaci�n de componentes a contenedores
		JPanel pSup = new JPanel();
		pSup.setLayout( new BorderLayout() );
		pSup.add( lMensaje, BorderLayout.WEST );
		pSup.add( lMensaje2, BorderLayout.EAST );
		add( pSup, BorderLayout.NORTH );
		JPanel p = new JPanel();
		tbZoom.setSelected( true );
		cbArboles = new JCheckBox( "Dibujar �rboles" ); cbArboles.setSelected( false );
		p.add( cbArboles ); p.add( tbZoom ); p.add( tbMover ); p.add( tbMoverArbol); /* p.add( tbGeoposicionar ); No interesa para el examen */ p.add( bCentrar );

		JButton bTarea2 = new JButton( "Tarea 2" ); p.add( bTarea2 ); // Llamada de tarea 2
		bTarea2.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { Tareas.tarea2(); } });
		
		JButton bTarea4 = new JButton( "Tarea 4" ); p.add( bTarea4 ); // Llamada de tarea 4
		bTarea4.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { Tareas.tarea4( EdicionZonasGPS.this ); } });
		
		add( p, BorderLayout.SOUTH );
		pDibujo = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				g.drawImage( biImagen, 0, 0, null );
			}
		};
		add( pDibujo, BorderLayout.CENTER );
		// Lista de zonas de la derecha de la ventana
		Iterator<Zona> itZona = GrupoZonas.jardinesErandio.getIteradorZonas();
		while (itZona.hasNext()) {
			Zona zona = itZona.next();
			mZonas.addElement( zona );
		}
		add( new JScrollPane( lZonas ), BorderLayout.EAST );
		// Eventos de los distintos componentes
		lZonas.addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && lZonas.getSelectedIndex()>=0) {
					clickEnZona();
				}
				
			}
		});
		lZonas.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (lZonas.getSelectedIndex()>=0) {
					clickEnZona();
				}
			}
		});
		pDibujo.addMouseListener( new MouseAdapter() {
			Point pressed;
			@Override
			public void mouseReleased(MouseEvent e) {
				if (pressed.equals(e.getPoint())) {
					clickEn( e.getPoint(), e.getButton()==MouseEvent.BUTTON1 ); 
				} else {
					dragEn( pressed, e.getPoint(), e.getButton()==MouseEvent.BUTTON1 );
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				pressed = e.getPoint();
			}
		});
		pDibujo.addMouseMotionListener( new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// TAREA 5
				String mensaje = "";
				PuntoGPS puntoRaton = new PuntoGPS( yPantallaAlatiGPS( e.getY() ), xPantallaAlongGPS( e.getX() ) );
				if (e.isControlDown()) {
				    GrupoZonas gZ = GrupoZonas.jardinesErandio;
				    Iterator<Zona> iz = gZ.getIteradorZonas();
				    boolean sigue = true;
				    while (iz.hasNext() && sigue) {
				    	Zona z = iz.next();
				    	for (ArrayList<PuntoGPS> subZona : z.getPuntosGPS()) {
			    			if (UtilsGPS.gpsDentroDePoligono( puntoRaton, subZona )) {
			    				mensaje = "En zona " + z.getCodigoZona() + " -";
			    				sigue = false;
			    				break;
			    			}
				    	}
				    }
				}
				lMensaje2.setText( String.format( mensaje + " Coordenadas GPS ratón: %1$.4f , %2$.4f", 
						puntoRaton.getLatitud(), puntoRaton.getLongitud() ) );
			}
			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
		tbZoom.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { tbMover.setSelected( false ); tbGeoposicionar.setSelected( false ); tbMoverArbol.setSelected( false ); } } );
		tbMover.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { tbGeoposicionar.setSelected( false ); tbZoom.setSelected( false ); tbMoverArbol.setSelected( false );} } );
		tbGeoposicionar.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { tbMover.setSelected( false ); tbZoom.setSelected( false ); tbMoverArbol.setSelected( false );} } );
		tbMoverArbol.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { tbMover.setSelected( false ); tbZoom.setSelected( false ); tbGeoposicionar.setSelected( false );} } );
		bCentrar.addActionListener( new ActionListener() { public void actionPerformed(ActionEvent e) { calcMoverACentroZonas(); calculaMapa(); } } );
		cbArboles.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dibujadoArboles = cbArboles.isSelected();
				calculaMapa();
			}
		});
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				guardaProperties();
				Tareas.finAplicacion();
			}
		});
		// Carga final de propiedades de configuraci�n (zoom y posici�n del mapa, referencias GPS, primer dibujado del mapa en ventana
		cargaProperties();

		// JTable izquierda
		mDatos = new DefaultTableModel() {
			{ setColumnIdentifiers( new Object[] { "Zona", "�rea", "Pto", "Latitud", "Longitud" } ); }  // Inicializaci�n para que salgan solo las cabeceras cuando la tabla est� vac�a al principio
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				if (column>2) {
					super.setValueAt(aValue, row, column);
					Zona zona = GrupoZonas.jardinesErandio.getZona( (String) mDatos.getValueAt( row, 0 ) );
					int subzona = (Integer) mDatos.getValueAt( row, 1 );
					int punto = (Integer) mDatos.getValueAt( row, 2 );
					if (column==3)
						zona.getPuntosGPS().get( subzona ).get( punto ).setLatitud( Double.parseDouble((String)aValue) );
					else
						zona.getPuntosGPS().get( subzona ).get( punto ).setLongitud( Double.parseDouble((String)aValue) );
					// Caso especial si es el primer punto hay que cambiar tambi�n el �ltimo
					if (punto==0) {
						if (column==3)
							zona.getPuntosGPS().get( subzona ).get( zona.getPuntosGPS().get( subzona ).size()-1 ).setLatitud( Double.parseDouble((String)aValue) );
						else
							zona.getPuntosGPS().get( subzona ).get( zona.getPuntosGPS().get( subzona ).size()-1 ).setLongitud( Double.parseDouble((String)aValue) );
					}
					calculaMapa();  // Redibuja con el cambio
				}
			}
		};
		tDatos = new JTable( mDatos );
		TableColumnModel modeloCol = tDatos.getColumnModel();
		modeloCol.getColumn(0).setPreferredWidth(40);
		modeloCol.getColumn(1).setPreferredWidth(30);
		modeloCol.getColumn(2).setPreferredWidth(30);
		modeloCol.getColumn(3).setPreferredWidth(120);
		modeloCol.getColumn(4).setPreferredWidth(120);
		JScrollPane scrollPane = new JScrollPane(tDatos);
		scrollPane.setPreferredSize(new Dimension(310, 100));
		add( scrollPane, BorderLayout.WEST );
		// TAREA 3 - Evento de dibujado de punto actual
		tDatos.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && tDatos.getSelectedRow() >= 0) {
					int row = tDatos.getSelectedRow();
					Zona z = GrupoZonas.jardinesErandio.getZona( "" + (mDatos.getValueAt( row, 0 )) );
					int subZona = (Integer) mDatos.getValueAt( row, 1 );
					int pt = (Integer) mDatos.getValueAt( row, 2 );
					PuntoGPS p = z.getPuntosGPS().get( subZona ).get( pt );
					calculaMapa();
					dibujaZona( row + 1, z, Color.CYAN );
					graphics.setColor( Color.BLACK );
					graphics.drawOval( (int) Math.round( longGPSaXPantalla( p.getLongitud() )) - 8, 
							(int) Math.round( latiGPSaYPantalla( p.getLatitud() )) - 8, 16, 16 );
					if (pDibujo!=null) pDibujo.repaint();
				}
			}
		});
		tDatos.setDefaultRenderer( Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				int subZona = (Integer) mDatos.getValueAt( row, 1 );
				if ((subZona % 2) == 0) comp.setBackground( Color.WHITE ); else comp.setBackground( Color.LIGHT_GRAY );
				return comp;
			}
		});
		tDatos.addKeyListener( new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_DELETE && tDatos.getSelectedRow()>=0) {
					int filaABorrar = tDatos.getSelectedRow();
					Zona z = GrupoZonas.jardinesErandio.getZona( "" + (mDatos.getValueAt( filaABorrar, 0 )) );
					int subZona = (Integer) mDatos.getValueAt( filaABorrar, 1 );
					int punto = (Integer) mDatos.getValueAt( filaABorrar, 2 );
					if (punto == 0 || punto == z.getPuntosGPS().get(subZona).size()-1) {
						return; 
					}
					mDatos.removeRow( filaABorrar );
					z.getPuntosGPS().get( subZona ).remove( punto );
					calculaMapa();
					clickEnZona();
				}
			}
		});
	}
	
		/** Dibuja un c�rculo negro en el mapa
		 * @param redibujaPant	Redibuja la pantalla antes de pintar el c�rculo (si es true) o pinta encima de lo que haya (false)
		 * @param zona	Zona que se quiere resaltar en cyan (si no se quiere resaltar ninguna zona, null)
		 * @param longitud	Longitud GPS del punto a pintar en el mapa
		 * @param latitud	Latitud GPS del punto a pintar en el mapa
		 */
		public void dibujaCirculoNegro( boolean redibujaPant, Zona zona, double longitud, double latitud ) {
			if (redibujaPant) calculaMapa();  // Vuelve a dibujar
			if (zona!=null) dibujaZona( 0, zona, Color.CYAN );
			graphics.setColor( Color.BLACK );
			graphics.drawOval( (int) Math.round( longGPSaXPantalla( longitud )) - 8, (int) Math.round( latiGPSaYPantalla( latitud )) - 8, 16, 16 );
			if (pDibujo!=null) pDibujo.repaint();
		}
	
		private void clickEnZona() {
			int val = lZonas.getSelectedIndex();
			ArrayList<String> zonasErandio = GrupoZonas.jardinesErandio.getCodZonas();
			Zona zona = GrupoZonas.jardinesErandio.getZona( zonasErandio.get(val) );
			calcMoverACentroZona( zona );
			calculaMapa();
			dibujaZona( val+1, zona, Color.CYAN );
			lMensaje.setText( "Zona " + (val+1) + " tiene " + zona.getNumSubzonas() + " subzonas y " + zona.getNumPuntos() + " puntos diferentes." );
			Object[][] datos = new Object[ zona.getNumPuntos() + zona.getPuntosGPS().size() ][5];
			int numArea = 0;
			int numPunto = 0;
			for (ArrayList<PuntoGPS> subzona : zona.getPuntosGPS()) {
				int numOrden = 0;
				for (int i=0; i<subzona.size(); i++) {
					PuntoGPS punto = subzona.get(i);
					datos[numPunto][0] = zona.getCodigoZona();
					datos[numPunto][1] = numArea;
					datos[numPunto][2] = numOrden;
					datos[numPunto][3] = punto.getLatitud();
					datos[numPunto][4] = punto.getLongitud();
					numPunto++;
					numOrden++;
				}
				numArea++;
			}
			mDatos.setDataVector( datos, new Object[] { "Zona", "�rea", "Pto", "Latitud", "Longitud" } );
			TableColumnModel modeloCol = tDatos.getColumnModel();
			modeloCol.getColumn(0).setPreferredWidth(40);
			modeloCol.getColumn(1).setPreferredWidth(30);
			modeloCol.getColumn(2).setPreferredWidth(30);
			modeloCol.getColumn(3).setPreferredWidth(120);
			modeloCol.getColumn(4).setPreferredWidth(120);
		}
	
		// Gesti�n de eventos de click de rat�n, dependiendo del bot�n que est� seleccionado
		private boolean inicioGeopos = false;
		private void clickEn( Point p, boolean boton1 ) {
			if (tbZoom.isSelected()) {  // Click con zoom: si es bot�n izquierdo acerca, derecho aleja. Adem�s entendemos que al hacer click se hace una traslaci�n, por eso se duplica el c�digo de traslaci�n
				double zoom = 1 / 1.5; if (boton1) zoom = 1.5;  // Bot�n izquierdo aumenta, derecho u otro disminuye
				zoomX *= zoom; zoomY *= zoom;
				calcMoverCentroAlPunto( p.x, p.y );
				calcZoomManteniendoCentro( zoom );
				calculaMapa();
				lMensaje.setText( "Zoom actual: " + String.format( "%.2f", zoomX ) + "%" );
			} else if (tbMover.isSelected()) {
				calcMoverCentroAlPunto( p.x, p.y );
				lMensaje.setText( "Movido el mapa al centro indicado." );
				calculaMapa();
			} else if (tbGeoposicionar.isSelected()) {  // Primer click de un geoposicionamiento
				lMensaje.setText( "Introducida coordenada de posicionamiento. Introduce GPS correspondiente..." );
				String dato = JOptionPane.showInputDialog( ventana, "Introduce coordenada GPS (latitud,longitud):", "Geolocalizaci�n", JOptionPane.QUESTION_MESSAGE );
				if (!inicioGeopos) {
					inicioGeopos = true;
					calcGeoposicion( p.x, p.y, dato, 1 );
				} else {
					inicioGeopos = false;
					calcGeoposicion( p.x, p.y, dato, 2 );
				}
			}
		}
		
		// Gesti�n de eventos de drag de rat�n, dependiendo del bot�n que est� seleccionado
		private void dragEn( Point p1, Point p2, boolean boton1  ) {
			if (tbZoom.isSelected()) {  // Zoom sobre drag tiene dos partes: mover al centro y escalar al rect�ngulo del drag (si no es demasiado peque�o)
				double anchoDrag = Math.abs( p1.getX() - p2.getX() );
				double altoDrag = Math.abs( p1.getY() - p2.getY() );
				if (altoDrag < 10 || anchoDrag < 10) return;  // Si alguna dimensi�n del drag es menor de 10 p�xels no se hace nada (para evitar zooms demasiado grandes o peque�os)
				double relAnchura = pDibujo.getWidth() / anchoDrag;
				double relAltura = pDibujo.getHeight() / altoDrag;
				double relZoom = Math.min( relAnchura, relAltura );  // No queremos cambiar la rel de aspecto con lo cual nos limitamos a la menos cambiada de las dos dimensiones
				if (!boton1) relZoom = 1 / relZoom;  // Si el bot�n no es el izquierdo se reduce el zoom en lugar de ampliar
				int medioX = (p1.x + p2.x) / 2;
				int medioY = (p1.y + p2.y) / 2;
				zoomX *= relZoom; zoomY *= relZoom;
				calcMoverCentroAlPunto( medioX, medioY );
				calcZoomManteniendoCentro( relZoom );
				calculaMapa();
				lMensaje.setText( "Zoom actual: " + zoomX );
			} else if (tbMover.isSelected()) {
				calcMoverRelativo( p2.x - p1.x, p2.y - p1.y );
				calculaMapa();
				lMensaje.setText( "Movido el mapa seg�n el vector indicado." );
			} else if (tbMoverArbol.isSelected()) {  // Mueve un �rbol seg�n el drag
				double longOrigen = xPantallaAlongGPS( p1.getX() );
				double latOrigen = yPantallaAlatiGPS( p1.getY() );
				double longDestino = xPantallaAlongGPS( p2.getX() );
				double latDestino = yPantallaAlatiGPS( p2.getY() );
				Arbol arbolCerca = null;
				double distMenor = Double.MAX_VALUE;
				for (Arbol arbol : GrupoZonas.arbolesErandio) {  // Buscamos el �rbol m�s cercano
					double dist = UtilsGPS.distanciaEntrePuntos( arbol.getPunto(), new PuntoGPS( latOrigen, longOrigen ));
					if (arbolCerca==null || dist < distMenor) {
						arbolCerca = arbol;
						distMenor = dist;
					}
				}
				if (distMenor < 0.00025/zoomX) {  // Si el �rbol m�s cercano est� suficienmente cerca del picking se mueve
					arbolCerca.getPunto().setLatitud( latDestino );
					arbolCerca.getPunto().setLongitud( longDestino );
					calculaMapa();  // Redibuja el mapa
				}
				
				// TAREA 6 - Parte 1. Calcular dos puntos m�s cercanos a arbolCerca - c�lculos previos a la llamada recursiva
				PuntoGPS puntoMasCercano = null;
				double distanciaMenor = Double.MAX_VALUE;
				PuntoGPS puntoMasCercano2 = null;
				double distanciaMenor2 = Double.MAX_VALUE;
				Iterator<Zona> itZ = GrupoZonas.jardinesErandio.getIteradorZonas();
				while (itZ.hasNext()) {
					Zona zona = itZ.next();
					for (ArrayList<PuntoGPS> subZona : zona.getPuntosGPS()) {
						for (PuntoGPS p : subZona) {
							double dist = UtilsGPS.distanciaEntrePuntos( p, arbolCerca.getPunto() );
							if (puntoMasCercano==null || dist < distanciaMenor) {
								distanciaMenor2 = distanciaMenor;
								puntoMasCercano2 = puntoMasCercano;
								distanciaMenor = dist;
								puntoMasCercano = p;
							} else if (puntoMasCercano2==null || dist < distanciaMenor2) {
								distanciaMenor2 = dist;
								puntoMasCercano2 = p;
							}
						}
					}
				}
				// TAREA 6 - Parte 2. llamada recursiva (m�todo a continuaci�n)
				if (puntoMasCercano2 != null) {
					mostrarPuntoMasCercano( arbolCerca.getPunto().getLongitud(), arbolCerca.getPunto().getLatitud(), 
							puntoMasCercano.getLongitud(), puntoMasCercano.getLatitud(),
							puntoMasCercano2.getLongitud(), puntoMasCercano2.getLatitud(), 10 );
					dibujaCirculoNegro( false, null, puntoMasCercano.getLongitud(), puntoMasCercano.getLatitud() );  // Si se quieren ver los extremos
					dibujaCirculoNegro( false, null, puntoMasCercano2.getLongitud(), puntoMasCercano2.getLatitud() );
				}
			}
		}
			// TAREA 6 - Parte 2. Rutina recursiva
		private void mostrarPuntoMasCercano( double lonP, double latP, double lon1, double lat1, double lon2, double lat2, int numLlams ) {  // Por completar
			double lonMed = (lon1+lon2)/2;
			double latMed = (lat1+lat2)/2;
			if (numLlams == 0) {
				System.out.println( UtilsGPS.distanciaEntrePuntos( lonP, latP, lonMed, latMed ) );
				dibujaCirculoNegro( false, null, lonMed, latMed );
			} else {
				double dist1 = UtilsGPS.distanciaEntrePuntos( lonP, latP, lon1, lat1 );
				double dist2 = UtilsGPS.distanciaEntrePuntos( lonP, latP, lon2, lat2 );
				if (dist1 < dist2) {
					mostrarPuntoMasCercano( lonP, latP, lon1, lat1, lonMed, latMed, numLlams-1 );
				} else {
					mostrarPuntoMasCercano( lonP, latP, lonMed, latMed, lon2, lat2, numLlams-1 );
				}
			}
		}
		
		// Rutinas de rec�lculo de zoom
		private void calcZoomManteniendoCentro( double relZoom ) {
			offsetX = pDibujo.getWidth()/2 - relZoom * (pDibujo.getWidth()/2 - offsetX);
			offsetY = pDibujo.getHeight()/2 - relZoom * (pDibujo.getHeight()/2 - offsetY);
			if (zoomX > 20.001) { double relZ2 = 20.0/zoomX; zoomX = 20.0; zoomY = 20.0; calcZoomManteniendoCentro( relZ2 ); }
			else if (zoomX < 0.099) { double relZ2 = 0.10/zoomX; zoomX = 0.10; zoomY = 0.10; calcZoomManteniendoCentro( relZ2 ); }
		}
		// Rutinas de rec�lculo de movimiento
		private void calcMoverRelativo( int difX, int difY ) {
			offsetX += difX;
			offsetY += difY;
		}
		private void calcMoverCentroAlPunto( int x, int y ) {
			offsetX += (pDibujo.getWidth()/2 - x);  // Mover al centro
			offsetY += (pDibujo.getHeight()/2 - y);
		}
		// Mueve el centro de la pantalla al centro del rect�ngulo que engloba todos los puntos de zona, y pone un 55% de Zoom
		void calcMoverACentroZonas() {
			Iterator<Zona> itZona = GrupoZonas.jardinesErandio.getIteradorZonas();
			double longMin = Double.MAX_VALUE; double longMax = -Double.MAX_VALUE; double latiMin = Double.MAX_VALUE; double latiMax = -Double.MAX_VALUE;
			while (itZona.hasNext()) {
				Zona zona = itZona.next();
				for (ArrayList<PuntoGPS> subzona : zona.getPuntosGPS()) {
					for (PuntoGPS punto : subzona) {
						if (punto.getLatitud()>latiMax) latiMax = punto.getLatitud();
						if (punto.getLatitud()<latiMin) latiMin = punto.getLatitud();
						if (punto.getLongitud()>longMax) longMax = punto.getLongitud();
						if (punto.getLongitud()<longMin) longMin = punto.getLongitud();
					}
				}
			}
			calcMoverCentroAlPunto( xGraficoAPantalla( longGPSaXGrafico( (longMax+longMin)/2.0 ) ), yGraficoAPantalla( latiGPSaYGrafico( (latiMax+latiMin)/2.0 ) ) );
			double zoom = 0.55/zoomX;
			zoomX *= zoom; zoomY *= zoom;
			calcZoomManteniendoCentro( zoom );
			lMensaje.setText( "Centrado el mapa en la vista de todas las zonas." );
		}
		// Mueve el centro de la pantalla a la media aritm�tica de los puntos de la zona
		void calcMoverACentroZona( Zona zona ) {
			double sumaLong = 0; double sumaLati = 0;
			for (ArrayList<PuntoGPS> lp : zona.getPuntosGPS())
				for (int i=0; i<lp.size()-1; i++) {  // No se coge el �ltimo que repite siempre al primero (cierra la subzona)
					PuntoGPS p = lp.get(i);
					sumaLong += p.getLongitud();
					sumaLati += p.getLatitud();
				}
			sumaLati /= zona.getNumPuntos();  sumaLong /= zona.getNumPuntos();  // Medias aritm�ticas de los puntos de la zona
			int medioX = xGraficoAPantalla( longGPSaXGrafico( sumaLong ) );
			int medioY = yGraficoAPantalla( latiGPSaYGrafico( sumaLati ) );
			calcMoverCentroAlPunto( medioX, medioY );
		}
	
	// Calcula y redibuja todo el mapa visual en la ventana
	void calculaMapa() {
		graphics.setColor( Color.WHITE );
		graphics.fillRect( 0, 0, ANCH_MAX, ALT_MAX );
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR); // Configuraci�n para mejor calidad del gr�fico escalado
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);	
		graphics.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.6f ) ); // Pintar con transparencia de 60%
		graphics.drawImage( mapa.getImage(), 
				(int) Math.round(offsetX), (int) Math.round(offsetY), 
				(int) Math.round(mapa.getIconWidth()*zoomX), (int) Math.round(mapa.getIconHeight()*zoomY), null );
		graphics.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 1f ));  // Restaurar no transparencia
		dibujaZonas();
		if (pDibujo!=null) pDibujo.repaint();
	}
	
	// Relaciona un punto de pantalla (x,y) a una coordenada GPS (gps) latitud,longitud. Si hay dos puntos correctos, recalcula la relaci�n del gr�fico con el geoposicionamiento y redibuja
	private void calcGeoposicion( int x, int y, String gps, int numPunto ) {
		if (gps==null || gps.isEmpty()) { lMensaje.setText( "Introducci�n GPS incorrecta." ); inicioGeopos = false; return; }
		double grafX = xPantallaAGrafico( x );
		double grafY = yPantallaAGrafico( y );
		try {
			int coma = gps.indexOf( ',' );
			double lati = Double.parseDouble( gps.substring( 0, coma ) );
			double longi = Double.parseDouble( gps.substring( coma+1 ) );
			if (numPunto==1) {
				xImagen1 = grafX;
				yImagen1 = grafY;
				longGPS1 = longi;
				latiGPS1 = lati;
			} else {  // punto 2
				xImagen2 = grafX;
				yImagen2 = grafY;
				longGPS2 = longi;
				latiGPS2 = lati;
			}
			calcGPSBase();
			calculaMapa();
		} catch (Exception e) {
		}
	}

	// Dibuja las zonas de Erandio y los �rboles (si el checkbox lo indica) en el objeto gr�fico de pantalla
	private void dibujaZonas() {
		if (longGPS1!=0.0) {  // Marca de asociaci�n GPS de punto 1
			int x = xGraficoAPantalla( xImagen1 );
			int y = yGraficoAPantalla( yImagen1 );
			graphics.setColor( Color.BLACK );
			graphics.setStroke( stroke4 );
			graphics.drawOval( x-6, y-6, 12, 12 );
			if (longGPS2!=0.0) {  // Marca de asociaci�n GPS de punto 2 - si no est� no se puede correlacionar GPS con el gr�fico y por tanto no se dibujan zonas ni puntos
				int x2 = xGraficoAPantalla( xImagen2 );
				int y2 = yGraficoAPantalla( yImagen2 );
				graphics.setColor( Color.RED );
				graphics.setStroke( stroke4 );
				graphics.drawOval( x2-6, y2-6, 12, 12 );
				int nZ = 1;
				graphics.setStroke( stroke2m );
				Iterator<Zona> itZona = GrupoZonas.jardinesErandio.getIteradorZonas();
				while (itZona.hasNext()) {
					Zona zona = itZona.next();
					dibujaZona( nZ, zona );
					nZ++;
				}
				if (dibujadoArboles) {
					for (Arbol arbol : GrupoZonas.arbolesErandio) {
						arbol.dibuja( this, false );
					}
				}
			}
		}
	}
	
	// Dibuja las zona indicada con l�neas y puntos, en el color indicado (si no se indica, se alterna magenta y azul), en el objeto gr�fico de pantalla
	private void dibujaZona( int numZona, Zona zona, Color... colorOpcional ) {
		if (zona.getNumSubzonas()>0) {
			if (numZona%2==0) graphics.setColor( Color.BLUE ); else graphics.setColor( Color.MAGENTA );
			if (colorOpcional.length>0) { graphics.setColor( colorOpcional[0] ); graphics.setStroke( stroke4 ); }
			Color color = graphics.getColor();
			Stroke stroke = graphics.getStroke();
			for (ArrayList<PuntoGPS> subzona : zona.getPuntosGPS()) {
				PuntoGPS p1 = subzona.get(0);
				for (int i=1; i<subzona.size(); i++) {
					PuntoGPS p2 = subzona.get(i);
					dibujaLinea( p1.getLongitud(), p1.getLatitud(), p2.getLongitud(), p2.getLatitud(), null, null, false );
					dibujaCirculo( p1.getLongitud(), p1.getLatitud(), 3, Color.YELLOW, stroke1m, false );  // Punto amarillo en cada v�rtice de la zona
					graphics.setColor( color );
					graphics.setStroke( stroke );
					p1 = p2;
				}
				dibujaCirculo( p1.getLongitud(), p1.getLatitud(), 4, null, null, false );  // Punto gordo para el inicial de la lista de puntos
			}
		}
	}
	
	/** Dibuja un c�rculo en el mapa gr�fico de la ventana
	 * @param longitud	Coordenada longitud (horizontal) donde dibujar el centro del c�rculo
	 * @param latitud  Coordenada latitud (vertical) donde dibujar el centro del c�rculo
	 * @param radio	P�xels de pantalla de radio del c�rculo a dibujar
	 * @param color	Color del c�rculo (si es null utiliza el color actual de dibujado)
	 * @param stroke Pincel de dibujado del c�rculo (grosor) (si es null utiliza el stroke actual de dibujado)
	 * @param pintaEnVentana	true si se quiere pintar inmediatamente en el mapa, false si se pinta en el objeto gr�fico pero no se muestra a�n en pantalla
	 */
	public void dibujaCirculo( double longitud, double latitud, double radio, Color color, Stroke stroke, boolean pintaEnVentana ) {
		if (color!=null) graphics.setColor( color );
		if (stroke!=null) graphics.setStroke( stroke );
		graphics.drawOval( (int) Math.round( longGPSaXPantalla( longitud ) - radio), (int) Math.round( latiGPSaYPantalla( latitud ) - radio),
				(int) Math.round(radio*2), (int) Math.round(radio*2) );
		if (pintaEnVentana && pDibujo!=null) pDibujo.repaint();
	}

	/** Dibuja una cruz en el mapa gr�fico de la ventana
	 * @param longitud	Coordenada longitud (horizontal) donde dibujar el centro de la cruz
	 * @param latitud  Coordenada latitud (vertical) donde dibujar el centro de la cruz
	 * @param tamanyo	P�xels de pantalla de anchura y altura de la cruz a dibujar
	 * @param color	Color de la cruz (si es null utiliza el color actual de dibujado)
	 * @param stroke Pincel de dibujado de la cruz (grosor) (si es null utiliza el stroke actual de dibujado)
	 * @param pintaEnVentana	true si se quiere pintar inmediatamente en el mapa, false si se pinta en el objeto gr�fico pero no se muestra a�n en pantalla
	 */
	public void dibujaCruz( double longitud, double latitud, double tamanyo, Color color, Stroke stroke, boolean pintaEnVentana ) {
		if (color!=null) graphics.setColor( color );
		if (stroke!=null) graphics.setStroke( stroke );
		graphics.drawLine( (int) Math.round(longGPSaXPantalla( longitud )), (int) (Math.round(latiGPSaYPantalla(latitud))-tamanyo/2),
				(int) Math.round(longGPSaXPantalla( longitud )), (int) (Math.round(latiGPSaYPantalla(latitud))+tamanyo/2) );
		graphics.drawLine( (int) (Math.round(longGPSaXPantalla( longitud ))-tamanyo/2), (int) Math.round(latiGPSaYPantalla(latitud)),
				(int) (Math.round(longGPSaXPantalla( longitud ))+tamanyo/2), (int) Math.round(latiGPSaYPantalla(latitud)) );
		if (pintaEnVentana && pDibujo!=null) pDibujo.repaint();
	}

	/** Dibuja una l�nea en el mapa gr�fico de la ventana
	 * @param longitud1	Coordenada longitud (horizontal) del punto inicial
	 * @param latitud1  Coordenada latitud (vertical) del punto inicial
	 * @param longitud2	Coordenada longitud (horizontal) del punto inicial
	 * @param latitud2  Coordenada latitud (vertical) del punto inicial
	 * @param color	Color de la l�nea (si es null utiliza el color actual de dibujado)
	 * @param stroke Pincel de dibujado de la l�nea (grosor) (si es null utiliza el stroke actual de dibujado)
	 * @param pintaEnVentana	true si se quiere pintar inmediatamente en el mapa, false si se pinta en el objeto gr�fico pero no se muestra a�n en pantalla
	 */
	public void dibujaLinea( double longitud1, double latitud1, double longitud2, double latitud2, Color color, Stroke stroke, boolean pintaEnVentana ) {
		if (color!=null) graphics.setColor( color );
		if (stroke!=null) graphics.setStroke( stroke );
		graphics.drawLine( (int) Math.round(longGPSaXPantalla( longitud1 )), (int) Math.round(latiGPSaYPantalla(latitud1)),
				(int) Math.round(longGPSaXPantalla( longitud2 )), (int) Math.round(latiGPSaYPantalla(latitud2)) );
		if (pintaEnVentana && pDibujo!=null) pDibujo.repaint();
	}
	
	
	// M�todos de conversi�n entre x,y de pantalla,  x,y de p�xels del gr�fico de fondo (mapa),  long,lati de la coordenada GPS equivalente
	public double xPantallaAGrafico( double x ) { return (x - offsetX) / zoomX; }
	public double yPantallaAGrafico( double y ) { return (y - offsetY) / zoomY; }
	public double xPantallaAlongGPS( double x ) { return xGraficoAlongGPS( xPantallaAGrafico( x ) ); }
	public double yPantallaAlatiGPS( double y ) { return yGraficoAlatiGPS( yPantallaAGrafico( y ) ); }
	public int xGraficoAPantalla( double x ) { return (int) Math.round( zoomX * x + offsetX ); }
	public int yGraficoAPantalla( double y ) { return (int) Math.round( zoomY * y + offsetY ); }
	public double longGPSaXGrafico( double longi ) {
		return origenXGPSEnGraf + longi*relGPSaGrafX;
	}
	public int longGPSaXPantalla( double longitud ) { return xGraficoAPantalla( longGPSaXGrafico( longitud ) ); }
	public double latiGPSaYGrafico( double lati ) {
		return origenYGPSEnGraf + lati*relGPSaGrafY;
	}
	public int latiGPSaYPantalla( double latitud ) { return yGraficoAPantalla( latiGPSaYGrafico( latitud ) ); }
	public double xGraficoAlongGPS( double x ) {
		return (x - origenXGPSEnGraf) / relGPSaGrafX;
	}
	public double yGraficoAlatiGPS( double y ) {
		return (y - origenYGPSEnGraf) / relGPSaGrafY;
	}
	
	public JPanel getPanelDibujo() {
		return pDibujo;
	}
	

	// M�todo de c�lculo de los coeficientes de referencia para convertir coordenadas de pantalla y mapa en GPS georeferenciadas
	// (dependiendo de dos puntos suministrados de forma expl�cita para interpolar partiendo de ellos)
	private void calcGPSBase() {
		if (longGPS1!=0.0 && longGPS2!=0.0) {  // Marca de asociaci�n realizada en los dos puntos GPS
			double difX = xImagen2 - xImagen1;
			double difY = yImagen2 - yImagen1;
			double difLong = longGPS2 - longGPS1;
			double difLati = latiGPS2 - latiGPS1;
			relGPSaGrafX = difX / difLong;
			relGPSaGrafY = difY / difLati;
			origenXGPSEnGraf = xImagen1 - longGPS1 * relGPSaGrafX;
			origenYGPSEnGraf = yImagen1 - latiGPS1 * relGPSaGrafY;
			// System.out.println( "Origen GPS en el gr�fico: " + origenXGPSEnGraf + " , " + origenYGPSEnGraf );
			// System.out.println( "Escala de imagen en graf: " + relGPSaGrafX  + " , " + relGPSaGrafY );
		}
	}

	// Atributo de propiedades de configuraci�n
	private Properties properties;
	/**	Carga los valores de configuraci�n: zoom y posici�n del mapa, referencias GPS
	 * Si el fichero no existe, los inicializa con valores por defecto
	 * Hace el c�lculo de cooordenadas GPS y realiza el primer dibujado del mapa en ventana
	 */
	private void cargaProperties() {
		properties = new Properties();
		try {
			properties.loadFromXML( new FileInputStream( "utmgps.ini" ) );
			xImagen1 = Double.parseDouble( properties.getProperty( "xImagen1" ) );
			yImagen1 = Double.parseDouble( properties.getProperty( "yImagen1" ) );
			xImagen2 = Double.parseDouble( properties.getProperty( "xImagen2" ) );
			yImagen2 = Double.parseDouble( properties.getProperty( "yImagen2" ) );
			longGPS1 = Double.parseDouble( properties.getProperty( "longGPS1" ) );
			latiGPS1 = Double.parseDouble( properties.getProperty( "latiGPS1" ) );
			longGPS2 = Double.parseDouble( properties.getProperty( "longGPS2" ) );
			latiGPS2 = Double.parseDouble( properties.getProperty( "latiGPS2" ) );
			offsetX = Double.parseDouble( properties.getProperty( "offsetX" ) );
			offsetY = Double.parseDouble( properties.getProperty( "offsetY" ) );
			zoomX = Double.parseDouble( properties.getProperty( "zoomX" ) );
			zoomY = Double.parseDouble( properties.getProperty( "zoomY" ) );
		} catch (Exception e) {  // Fichero no existe o error en alg�n dato
			// longGPS2 = 0.0;  // Marcar�a que no hay asociaci�n GPS
			// TODO - Esto habr�a que cambiarlo si el mapa fuera otro. Est� configurado para el mapa y los valores de ejemplo de Erandio (ver GrupoZonas)
			longGPS1 = -2.98926;
			longGPS2 = -2.953024;
			latiGPS1 = 43.313283;
			latiGPS2 = 43.294753;
			xImagen1 = 130.16049382715826;
			xImagen2 = 1264.0216049382686;
			yImagen1 = 1601.4228395061725;
			yImagen2 = 2394.3672839506157;
		}
		calcGPSBase();
		calculaMapa();
		System.out.println( "Coordenada GPS correspondiente a la esquina superior izquierda del gr�fico: " + yGraficoAlatiGPS(0) + " , " + xGraficoAlongGPS(0) );
		System.out.println( "Coordenada GPS correspondiente a la esquina inferior derecha del gr�fico: " + yGraficoAlatiGPS(mapa.getIconHeight()) + " , " + xGraficoAlongGPS(mapa.getIconWidth()) );
	}
	
	/**	Guarda en fichero los valores de configuraci�n
	 */
	private void guardaProperties() {
		properties.setProperty( "xImagen1", ""+xImagen1 );
		properties.setProperty( "yImagen1", ""+yImagen1 );
		properties.setProperty( "xImagen2", ""+xImagen2 );
		properties.setProperty( "yImagen2", ""+yImagen2 );
		properties.setProperty( "longGPS1", ""+longGPS1 );
		properties.setProperty( "latiGPS1", ""+latiGPS1 );
		properties.setProperty( "longGPS2", ""+longGPS2 );
		properties.setProperty( "latiGPS2", ""+latiGPS2 );
		properties.setProperty( "offsetX", ""+offsetX );
		properties.setProperty( "offsetY", ""+offsetY );
		properties.setProperty( "zoomX", ""+zoomX );
		properties.setProperty( "zoomY", ""+zoomY );
		try {
			properties.storeToXML( new FileOutputStream( "utmgps.ini" ), "datos de RevisionZonasUTMGPS" );
		} catch (Exception e) {
		}
	}
	
}
