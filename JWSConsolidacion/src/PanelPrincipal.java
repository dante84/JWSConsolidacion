
// @author Daniel.Meza

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
 
public class PanelPrincipal extends JPanel {
     
       /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JFrame frame;                                           
       private JLabel eRutaExcel,eMes,eTExamen,eSTExamen,eExcel,eProcesando;
       private JPanel panelAplicacion,panelExcel,panelDown;              
       private JTextField cRuta,cExcel;
       private JButton bExaminar,bProcesarAplicaciones,bExcel,bSalvarDatos;
       private JFileChooser fileChooser;
       private ArrayList<Object> cve_instr,appDatMControlNoDat,alAplicacionesDatsErraticos = new ArrayList<>();  
       private Map<Object,Date> fechas;
       private Map<Object,String> registro,respuesta,instituciones,mapaTipoAplicacion,mapaSubtipoAplicacion;
       private Map<Object,Object> aplicaciones,imagEncR,imagEncS,mapaValoresMControlR,mapaValoresMControlS,mapaPosicionesRegistro = new HashMap<>(),
                                  mapaPosicionesRegistroBPM = new HashMap<>(),mapaPosicionesRegistroMcontrol = new HashMap<>(),
                                  mapaPosicionesRespuesta = new HashMap<>(),mapaPosicionesRespuestaBPM = new HashMap<>(),
                                  mapaPosicionesRespuestaMcontrol = new HashMap<>();
       private Map<Object,Object> mapaAplicacionesSinDatif = new HashMap<>(),aplicacionesInexistentes = new HashMap<>(),
                                  mapaAplicacionesPosicionesDesfazadas = new HashMap<>();
       private ArrayList<Object[]> alResultados = new ArrayList<>();
       private File aExcel;       
       private JComboBox<String> comboMes,comboTipoInstr,comboNombres_cortos;
       private JTable tresultados;
       private DefaultTableModel dftm;
       private JScrollPane sResultados;
       private GridBagConstraints gbc;      
       private SimpleDateFormat sdf;
       
       private int posicionesExcel = 0;
       private int numeroPosiciones = 0;    
       
       private ConexionBase conexionBase;
       private final String remoto = "172.16.50.14";
       
       private final String[] meses   = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
       private final String[] months  = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
       
       private final String[] tiposInstrumento = {"AC286","ACRESEC","ACRETSU","ACUERDO","ALI","CEAACES","CONALEP","DGESPE","ECCYPEC","ECELE","ECODEMS","EGAL",
                                                  "EGEL","EGETSU","EPROM","ESPECIALES","EUC","EUCCA","EXANI","EXTRA","IFE","LEPRE_LEPRI","MCEF","Metropolitano",                                                  
                                                  "MINNESOTA","OLIMPIADA","PILOTO","PREESCOLAR_BACH","PREESCOLAR_LIC","SEISP","SSP","TRIF","UPN"
                                                 };                                                             
       
       private PanelPrincipal() throws IOException,InvalidFormatException{                                   
               
               setLayout(new GridBagLayout());
               
               conexionBase = new ConexionBase();
               
               gbc = new GridBagConstraints();
               
               eProcesando = new JLabel("En espera");
               
               tresultados = new JTable(new DefaultTableModel());
               dftm = new DefaultTableModel();
               dftm.addColumn("No");
               dftm.addColumn("No aplicacion");
               dftm.addColumn("Existe");
               dftm.addColumn("Datif");
               dftm.addColumn("No Imag Reg");
               dftm.addColumn("No Imag Res");
               dftm.addColumn("No PReg");
               dftm.addColumn("No PReg BPM"); 
               dftm.addColumn("No PReg MControl");
               dftm.addColumn("No PRes");
               dftm.addColumn("No PRes BPM"); 
               dftm.addColumn("No PRes MControl");
               dftm.addColumn("Estado");
               dftm.addColumn("Observaciones");
               
               tresultados.setModel(dftm);                                      
               sResultados = new JScrollPane(tresultados);     
               
               gbc = new GridBagConstraints();         
               gbc.gridx = 0;
               gbc.gridy = 2;          
               gbc.weightx = 0.1;
               gbc.fill = GridBagConstraints.HORIZONTAL;
               gbc.insets = new Insets(5,5,5,5);                                                                                                                                    
               add(sResultados,gbc);
                                                     
               panelAplicacion = new JPanel(new GridBagLayout());
               panelExcel = new JPanel(new GridBagLayout());
               panelDown = new JPanel(new GridBagLayout());
               setSize(700,400);
               
               eRutaExcel = new JLabel("Ruta :");                              
               gbc.gridx = 0;
               gbc.gridy = 0;               
               gbc.weightx = 0.1;  
               gbc.anchor = GridBagConstraints.WEST;
               gbc.insets = new Insets(5,5,5,5);               
               panelAplicacion.add(eRutaExcel,gbc);
               
               cRuta = new JTextField(40);
               cRuta.setEditable(false);
               gbc = new GridBagConstraints();
               gbc.gridx = 1;
               gbc.gridy = 0;
               gbc.insets = new Insets(5,5,5,5);
               gbc.anchor = GridBagConstraints.WEST;
               gbc.gridwidth = 3;
               gbc.weightx = 0.2;               
               panelAplicacion.add(cRuta,gbc);                              
               
               bExaminar = new JButton("Examinar");
                              
               bExaminar.addActionListener(
                         new ActionListener() {
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 
                                    fileChooser = new JFileChooser();
                                    fileChooser.setMultiSelectionEnabled(false);
                                    
                                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                    
                                    int valor = fileChooser.showOpenDialog(null);
                                    
                                    if( valor == JFileChooser.APPROVE_OPTION ){
                                        File f = fileChooser.getSelectedFile();
                                        cRuta.setText(f.getAbsolutePath());
                                    }
                                    
                             }
                             
                       }
                      
               );
                                             
               gbc = new GridBagConstraints();
               gbc.gridx = 4;
               gbc.gridy = 0;         
               gbc.weightx = 0.1;               
               gbc.anchor = GridBagConstraints.WEST;
               gbc.insets = new Insets(5,5,5,5);
               panelAplicacion.add(bExaminar,gbc);     

               gbc = new GridBagConstraints();
               gbc.gridx = 0;
               gbc.gridy = 1;
               gbc.anchor = GridBagConstraints.WEST;
               gbc.insets = new Insets(5,5,5,5);
               eMes = new JLabel("Mes :");                                            
               panelAplicacion.add(eMes,gbc);
               
               gbc = new GridBagConstraints();
               gbc.gridx = 1;
               gbc.gridy = 1;
               gbc.anchor = GridBagConstraints.WEST;
               gbc.insets = new Insets(5,5,5,5);
               comboMes = new JComboBox<>(meses);
               panelAplicacion.add(comboMes,gbc);
              
               gbc = new GridBagConstraints();
               gbc.gridx = 2;
               gbc.gridy = 1;               
               gbc.anchor = GridBagConstraints.WEST;               
               gbc.insets = new Insets(5,5,5,5);
               eTExamen = new JLabel("Examen :");
               panelAplicacion.add(eTExamen,gbc);
                              
               comboTipoInstr = new JComboBox<>(tiposInstrumento);
               comboTipoInstr.setSelectedIndex(0);
               comboTipoInstr.addActionListener(
                            new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {                                                                                                                    	                            	                                          
                                                                                                              
                                       SwingWorker<Object, Object> swbe; 
                                       swbe = new SwingWorker<Object, Object>() {
                                       @Override
                                       protected Object doInBackground() throws Exception {
                                                                                             
                                                 panelAplicacion.remove(comboNombres_cortos);
                        
                                                 String itExamen = (String)comboTipoInstr.getSelectedItem();                                                
                        
                                                 gbc = new GridBagConstraints();
                        
                                                 comboNombres_cortos = new JComboBox<>(traeNombresCortos(itExamen));                                      
                                                 gbc = new GridBagConstraints();
                                                 gbc.gridx = 1;
                                                 gbc.gridy = 2;                                            
                                                 gbc.gridwidth = 3;
                                                 gbc.insets = new Insets(5,5,5,5);
                                                 gbc.anchor = GridBagConstraints.WEST;               
                                                 panelAplicacion.add(comboNombres_cortos,gbc);                                               
                                                 panelAplicacion.revalidate();
                                                 panelAplicacion.repaint();   
                                               
                                                 return null;
                              
                                       }
                                                                                                                                                        
                                       private String[] traeNombresCortos(String item){ 

                                               Connection c;
                                               Statement s;
                                               String[] nombres_cortosArray = null;

                                               try{
                                                      
                                                   //c = DriverManager.getConnection("jdbc:mysql://172.16.34.21:3306/replicasiipo","test","slipknot");
                                                   c = conexionBase.getC(remoto,"replicasiipo","test","slipknot");   
                                                   //c = conexionBase.getC(localhost,"replicasiipo","test","slipknot");
                                                   
                                                   String select = "select nom_corto from datos_examenes where tipo_instr = '" + item + "'";
                                                   
                                                   System.out.println(select);
                                                   
                                                   s = c.createStatement();
                                                   ResultSet rs = s.executeQuery(select);
                                         
                                                   List<String> nombres_cortos = new ArrayList<>();
                                                                                                                                                                                                   
                                                   while( rs.next() ){
                                                         
                                                          String nom_corto = rs.getString(1);
                                                          if( nom_corto != null ){
                                                              nombres_cortos.add(nom_corto);
                                                          }
          
                                                   }
                                                                                                                                                                                                                                            
                                                   nombres_cortosArray = new String[nombres_cortos.size()];
                                                   int i = 0;
                                                   for(Object o : nombres_cortos.toArray()){
                                                       nombres_cortosArray[i] = (String)o;
                                                       i++;
                                                   }      
                                                
                                                   s.close();
                                                   c.close();
                     
                                               }catch(Exception e){ e.printStackTrace(); }
                                              
                                               return nombres_cortosArray;       

                                       } 
                                    
                                     };
                                       
                                   swbe.execute();
                                       
                                }
                            
                            } 
                 
               );
                                 
               gbc = new GridBagConstraints();
               gbc.gridx = 2;
               gbc.gridy = 1;
               gbc.anchor = GridBagConstraints.CENTER;
               gbc.weightx = 0.1;
               gbc.insets = new Insets(5,5,5,5);
               panelAplicacion.add(comboTipoInstr,gbc);
                              
               gbc = new GridBagConstraints();
               gbc.gridx = 4;
               gbc.gridy = 1;                                            
               gbc.insets = new Insets(5,5,5,5);
               gbc.anchor = GridBagConstraints.WEST;                                                            
              
               eSTExamen = new JLabel("Subtipo :");
               gbc = new GridBagConstraints();
               gbc.gridx = 0;
               gbc.gridy = 2;
               gbc.anchor = GridBagConstraints.WEST;               
               gbc.insets = new Insets(5,5,5,5);               
               panelAplicacion.add(eSTExamen,gbc);
               
               comboNombres_cortos = new JComboBox<>();
               gbc = new GridBagConstraints();
               gbc.gridx = 1;
               gbc.gridy = 2;               
               gbc.insets = new Insets(5,5,5,5);
               gbc.anchor = GridBagConstraints.WEST;               
               panelAplicacion.add(comboNombres_cortos,gbc);  
                         
               eExcel = new JLabel("Excel :");
               gbc = new GridBagConstraints();
               gbc.gridx = 0;
               gbc.gridy = 0;               
               gbc.insets = new Insets(5,5,5,5);
               gbc.anchor = GridBagConstraints.WEST;               
               panelExcel.add(eExcel,gbc);
               
               cExcel = new JTextField(40);  
               cExcel.setEditable(false);
               gbc = new GridBagConstraints();
               gbc.gridx = 1;
               gbc.gridy = 0;
               gbc.gridwidth = 2;
               gbc.anchor = GridBagConstraints.WEST;               
               gbc.insets = new Insets(5,15,5,8);                                           
               panelExcel.add(cExcel,gbc);                             
               
               bExcel = new JButton("Examinar");                           
               bExcel.addActionListener(
                      new ActionListener() {
                          @Override
                          public void actionPerformed(ActionEvent e) {                                
                                 SwingWorker<?, ?> swbe = new SwingWorker<Object, Object>() {
                                             @Override
                                             protected Object doInBackground() throws Exception {
                                               
                                                       fileChooser = new JFileChooser();
                                                       fileChooser.setMultiSelectionEnabled(false);
                                    
                                                       fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                    
                                                       int valor = fileChooser.showOpenDialog(null);
                                     
                                                       if( valor == JFileChooser.APPROVE_OPTION){ 
                                                           
                                                           aExcel = (File)AccessController.doPrivileged(new PrivilegedAction<Object>() {
                                                                           @Override
                                                                           public Object run(){
                                                                                  File inputFile1 = fileChooser.getSelectedFile();
                                                                                  return inputFile1;
                                                                           }
                                                                           
                                                                        }
                                                                   
                                                                    );                                                                                                        
                                        
                                                           cExcel.setText(aExcel.getAbsolutePath());
                                        
                                                       }
                                                       
                                                       return null;
                                    
                                             }
                              
                                 }; 
                                 
                                 swbe.execute();
                                                                                         
                          }                                                   
                             
                      }
                     
               );                                                                  
               
               gbc = new GridBagConstraints();
               gbc.gridx = 3;
               gbc.gridy = 0;
               gbc.insets = new Insets(5,5,5,5);
               panelExcel.add(bExcel,gbc);                                                                                  
                              
               bProcesarAplicaciones = new JButton("Procesar Aplicaciones");
               bProcesarAplicaciones.addActionListener(new ActionListener() {

                       @Override
                       public void actionPerformed(ActionEvent e) {
                                                     
                              PanelPrincipal.this.bProcesarAplicaciones.setEnabled(false);
                              System.out.println("en el listener inicial");
                              SwingWorker<Void,Void> chamber;
                              chamber = new SwingWorker<Void,Void>() {
                                                                
                              Workbook wb;
                              
                              String cste = (String)comboNombres_cortos.getSelectedItem();   
                              
                              int h = 1;
                              
                              @Override
                              protected Void doInBackground() {                                                                                                                         
                                                     
                                        PanelPrincipal.this.eProcesando.setText("Procesando");
                                        
                                        System.out.println("En procesando");
                                        String rutaExcel = cExcel.getText().trim();
                                        String rutaDats = cRuta.getText().trim();
             
                                        aplicaciones          = new HashMap<>();
                                        fechas                = new HashMap<>();
                                        instituciones         = new HashMap<>();
                                        registro              = new HashMap<>();
                                        respuesta             = new HashMap<>();
                                        cve_instr             = new ArrayList<>();
                                        mapaTipoAplicacion    = new HashMap<>();
                                        mapaSubtipoAplicacion = new HashMap<>();
                          
                                        if( rutaExcel.equals("") ){
                                            JOptionPane.showMessageDialog(
                                            null,
                                            "Debes especificar un archivo excel.",
                                            "Especificar excel",
                                            JOptionPane.WARNING_MESSAGE);
                                            return null;
                                        }
                          
                                        if( rutaDats.equals("") ){
                                            JOptionPane.showMessageDialog(
                                            null,
                                            "La ruta es invalida, verifica.",
                                            "Ruta invalida",
                                            JOptionPane.WARNING_MESSAGE);
                                            return null;
                                        }                                                                                                       
                          
                                        try{
                                                                                        
                                            obtenDatos();
                                            cuentaImagenes();  
                                            cuentaPosiciones();
                                            
                                       }catch(Exception e){ e.printStackTrace(); }                                                                                                                                                                      
                                                                                                     
                                       return null; 
                       
                              }
                
                              private void obtenDatos() {
                       
                                      Connection con = null;                                      
                                      System.out.println("En obten datos antes de obtener la conexion");
                                      
                                      try{
                                          //con = conexionBase.getC(localhost,"replicasiipo","test","slipknot");
                                          con = conexionBase.getC(remoto,"replicasiipo","test","slipknot");
                                          wb = WorkbookFactory.create(aExcel); 
                                                                    
                                          System.out.println("En obten datos despues de obtener la conexion");
                                          Sheet hoja = wb.getSheetAt(0);      
                                                                    
                                          Iterator<Row> rowIt = hoja.rowIterator();                                    
                                          rowIt.next();
             
                                          sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);                    
                                                                                                                                          
                                          Statement statement = con.createStatement();//conectaBase();                        
                                          String select = "select cve_instr from datos_examenes";                                      
                                          
                                          select += " where nom_corto = '" + cste + "'" ;
                                           
                                    	  System.out.println("select de obtenDatos " + select);
                                          ResultSet rs = statement.executeQuery(select);
                                          
                                          while ( rs.next() ){
                                              
                                                  int datoCve_instr = rs.getInt(1);
                                               
                                                  for( Iterator<Row> it = rowIt; it.hasNext(); ){
  
                                                       Row r = (Row) it.next();
 
                                                       Cell cFechaInicio        = r.getCell(1); 
                                                       Cell cTipoAplicacion     = r.getCell(11);
                                                       Cell cSTipoAplcacion     = r.getCell(13);
                                                       Cell cInstitucion        = r.getCell(32);
                                                       Cell noRegistradosCell   = r.getCell(21);
                                                       Cell noRespuestaCell     = r.getCell(22);
                                                       Cell cClave_instr        = r.getCell(12);
                            
                                                       String scTipoAplicacion  = cTipoAplicacion.getStringCellValue().trim();                                                  
                                                       String scSTipoAplicacion = cSTipoAplcacion.getStringCellValue().trim();
                                                       String scInstitucion     = cInstitucion.getStringCellValue().trim();
                                                       int scClave_instr        = Integer.parseInt( cClave_instr.getStringCellValue());
                                                       double noRegistrados     = noRegistradosCell.getNumericCellValue();
                                                       double noRespuesta       = noRespuestaCell.getNumericCellValue();                                                                                  
                         
                                                       String valor = cFechaInicio.getStringCellValue().trim();                                                       
                              
                                                       if( valor.length() < 7 ){ continue; }
                                                       
                                                       String mes = valor.substring(3,6);
                                                       String month = "";
                                                       
                                                       for(int i = 0; i <= months.length - 1; i++){
                                                           if( months[i].equals(mes) ){
                                                               month = String.valueOf(i + 1);
                                                               if(month.length() - 1 == 0){
                                                                  month = "0" + month;
                                                               }
                                                           }
                                                       }
                                                       
                                                       String fecha = "20" + valor.substring(7,9) + "-" +  month  + "-" + valor.subSequence(0,2);                                                     
                                                       
                                                       Date fechaExcel = sdf.parse(fecha);
                                                       Calendar c = Calendar.getInstance();
                                                       c.setTime(fechaExcel);
                                                       int fem = c.get(Calendar.MONTH);                                                                                                                    
                                                       int cmi = comboMes.getSelectedIndex();
                                                                                                                                                                              
                                                       if( fem == cmi && scClave_instr == datoCve_instr ){                                                                                                                                  
                                               
                                                           Cell cApp   = r.getCell(0);
                                                           Object oapp = cApp.getStringCellValue();                                                                                                                                                                                            
                                                                                   
                                                           if( oapp != null ){                                 
                                                               
                                                               h++;
                                               
                                                               System.out.println( h + " " + oapp + " " + scClave_instr + " " + valor + " " + 
                                                                                   fecha + " " + fechaExcel);
                                                               aplicaciones.put(oapp,scClave_instr);   
                                                               fechas.put(oapp,fechaExcel);
                                                               registro.put(oapp,String.valueOf(noRegistrados));
                                                               respuesta.put(oapp,String.valueOf(noRespuesta));
                                                               instituciones.put(oapp,scInstitucion);
                                                               cve_instr.add(scClave_instr);                                                               
                                                               mapaTipoAplicacion.put(oapp,scTipoAplicacion);
                                                               mapaSubtipoAplicacion.put(oapp,scSTipoAplicacion);
                                                   
                                                           }
                                                                                                                                                                                                             
                                                       }                                                                                                                                     
                                  
                                                  }
                                                  
                                          }                                                                                    
                                                                                                                              
                                  }catch(Exception e){ e.printStackTrace(); }                                                                                                                                                   
                                      
                              }
                              
                              private void cuentaImagenes(){
                                     
                                      String ruta = cRuta.getText().trim();                                                                            
                                      imagEncR = new HashMap<>();
                                      imagEncS = new HashMap<>();
                                                                            
                                      int imagenesExistenR = 0;
                                      int imagenesExistenS = 0;
                                      int k = 1;
                                      
                                      Set<Object> ks = aplicaciones.keySet();
                                      Iterator<Object> it = ks.iterator();
                                      
                                      try{
                                                                                                                    
                                          while( it.hasNext() ){
                                          
                                                 PanelPrincipal.this.eProcesando.setText("Procesando imagenes - aplicacion " + k + " de " + h);
                                                 Object o = it.next();
                                                 String numeroAplicacion = (String)o;                                                  
                                                 
                                                 File appDir = new File(ruta + "\\" + numeroAplicacion);
                                                 boolean existe = appDir.exists();                                                                                                  
                                                 
                                                 if( !existe ){ aplicacionesInexistentes.put(o,o); }                                                 
                                                 else{
                                                      boolean esDir = appDir.isDirectory();                       	  
                                                      if( esDir ){                                                                                                                                  
                                                          File[] archivos = appDir.listFiles();                                                                                                                          
                                                          for( File f : archivos ){                                                                                                      
                                                               String nombreArchivo = f.getName();  
                                                           
                                                               if( nombreArchivo.matches("\\d{6}\\_[Rr]\\d{3}\\.[t][i][f]") ){                                                                    
                                                                   imagenesExistenR++; 
                                                               }                                                                                                                                                                                                                                                                                                                    
                                                               if( nombreArchivo.matches("\\d{6}\\_[Ss]\\d{3}\\.[t][i][f]") ){ 
                                                                   imagenesExistenS++; 
                                                               }                                                              
                                                              
                                                          }
                                                                                                                                                                             
                                                      }                                           
                                                                                      
                                                 }
                                                 
                                                 System.out.println(o  + " " + imagenesExistenR + " " + imagenesExistenS);
                                                 imagEncR.put(o,imagenesExistenR);
                                                 imagEncS.put(o,imagenesExistenS);                                                                                                                   
                                                 
                                                 imagenesExistenR = 0;
                                                 imagenesExistenS = 0;
                                     
                                                 k++;
                                                 
                                          }
                                          
                                      }catch(Exception e){ e.printStackTrace(); }    
                              
                              }
                              
                              private void cuentaPosiciones(){
                                                                       
                                      String rutaDatif = cRuta.getText().trim();  
                                      appDatMControlNoDat = new ArrayList<>();
                                      mapaValoresMControlR = new HashMap<>();
                                      mapaValoresMControlS = new HashMap<>();                                                                            
                                      
                                      Set<Object> ks = aplicaciones.keySet();
                                      Iterator<Object> it = ks.iterator();
                                      
                                      int k = 1;
                                      while( it.hasNext() ){
                                          
                                             PanelPrincipal.this.eProcesando.setText("Procesando dats - aplicacion " + k + " de " + h);
                                             Object o = it.next();                                                                                     
                                             
                                             if( aplicacionesInexistentes.containsKey(o) ){ continue; }                                             
                                             
                                             String aplicacion = (String)o;
                                             rutaDatif += "\\" + aplicacion + "\\DATIF";
                                             File datif = new File(rutaDatif);                                                                                          
                                           
                                             boolean existeDatif = datif.exists();                                                                                     
                                             
                                             if( existeDatif ){
                                                 datif.getAbsolutePath();                     
                                                 File[] archivos = datif.listFiles(                            		   
                         	 	                 new FileFilter() {
                     			                    @Override
                                                            public boolean accept(File pathname) {                                     
                                                                   if( pathname.getName().endsWith(".dat") ){ return true; }                                          
                                                                   return false;                                        
                                                            }
                   
                                                        }
                                                      
                                                 );                                                                                            
                                                                                            
                                                 int r = -1;
                                                 int S = -1;
                                                 int la = (archivos.length - 1);                          
                                                                                                                    
                                                 if( la == -1 && 
                                                     ( ( registro.containsKey(o)  && Double.valueOf( registro.get(o)  ) > 0  ) || 
                                                     ( ( respuesta.containsKey(o) && Double.valueOf( respuesta.get(o) ) > 0) ) ) ) {      
                                                     System.out.println("No hay dats " + o);
                                                     appDatMControlNoDat.add(o);
                                                     continue;
                                                 }
                                              
                                                 for( int m = 0; m <= la; m++ ){
                              
                                                      String nombreArchivo = archivos[m].getName();                                                                                                            
                                                      String subNombreArchivo = "";
                               
                                                      for( int i = 0; i <= nombreArchivo.length() - 5; i++ ){
                                                           subNombreArchivo += nombreArchivo.charAt(i);
                                                      }                                                                                                        
                                                              
                                                      char ci = nombreArchivo.charAt(0);
                                                                                  
                                                      if( subNombreArchivo.matches("[Rr]\\d{9}[Xx][_\\d]") || subNombreArchivo.matches("[Ss]\\d{9}[Xx][_\\d]") ){ 
                                   
                                                          String c = "";
                                                          c += ci;
                                      
                                                          if( c.matches("[RrSs]") ){                                                       
                                                              if( "r".equals(c) || "R".equals(c) ){                                                                 
                                                                  r++;                                 
                                                              }   
                                                              if( "s".equals(c) || "S".equals(c) ){                                                               
                                                                  S++;
                                                              }
                                                          }                                                                                          
                                   
                                                      }else{                                     
                                                            alAplicacionesDatsErraticos.add(o);
                                                            continue;
                                                      }   
                                                                                                                                     
                                                 }
                                                                 
                                                 System.out.println("Es " + respuesta.containsKey(o) +" S " + S + " " + respuesta.get(o) + " " + 
                                                                    (Double.valueOf( respuesta.get(o)) >= 0) );
                                                 if( respuesta.containsKey(o) && S == -1 && Double.valueOf( respuesta.get(o)) > 0 ){
                                                     System.out.println("No hay dats de respuestas en " + o);
                                                     appDatMControlNoDat.add(o);
                                                 }
                                                                                                  
                                                 System.out.println("Es " + registro.containsKey(o) +" r " + r + " " + registro.get(o) + " " + 
                                                                    (Double.valueOf( registro.get(o)) >= 0) );
                                                 if( registro.containsKey(o) && r == -1 && Double.valueOf(registro.get(o)) > 0){
                                                     System.out.println("No hay dats de registros en " + o);
                                                     appDatMControlNoDat.add(o);
                                                 }
                                                 
                                                 if( la == -1 ){ mapaAplicacionesSinDatif.put(o,o); }                     
                                                 else{    
                                                	 
                                                      int i = 0;
                                                      
                                                      while( i <=  r ){                               
                                                             String nombreArchivo = archivos[i].getName();                                                                                                       
                                                             mapaValoresMControlR.put(o,leeArchivo(nombreArchivo,rutaDatif,(String)o,i,r,"R"));
                                                             i++;
                                                      }                                                            
                              
                                                      while( i <= la ){                               
                                                             String nombreArchivo = archivos[i].getName();                                                                                                                                                                                    
                                                             mapaValoresMControlS.put(o,leeArchivo(nombreArchivo,rutaDatif,(String)o,i,la,"S"));
                                                             i++;
                                                      }
                              
                                                 }
                                              
                                           }else{ mapaAplicacionesSinDatif.put(o, o); }                                                                                     
                                                                                      
                                           rutaDatif = cRuta.getText().trim();
                                           k++;
                                                                                              
                                      }
                                   
                              }
                              
                              @SuppressWarnings("resource")
                              public int leeArchivo(String nombreArchivo,String rutaDatif,Object f,int i,int noArchivos,String tipo){               
        
                                     String linea = "";                                                 
                                     int temp;                                                                                                                                                                                                         
              
                                     try{         
                                                      
                                         File f1 = new File(rutaDatif + "\\" + nombreArchivo);                  
                                         FileInputStream fis = new FileInputStream(f1);                       
                     
                                         while(true){
                   
                                               temp = fis.read();                                                                   
                                               
                                               if( temp == -1 ){                              
                                                   break;
                                              }
                     
                                              int digitoSub;
                                              linea += (char)temp;                                                                                                          
                     
                                              if( temp == '\n' ){ 
                                                        
                                                  String sub = linea.substring(3,9);                            
                                                  digitoSub  = Integer.parseInt(sub);                               
                         
                                                  if( digitoSub == 0 ){ numeroPosiciones--; }
                         
                                                  numeroPosiciones++;                                                                                
                                                         
                                                  if( digitoSub != numeroPosiciones ){ 
                            	                      mapaAplicacionesPosicionesDesfazadas.put(f,f);
                                                  }
                                                                                         
                                                  linea = "";
                         
                                              }                                                        
                     
                                         }                                                                                                         
                                         
                                         posicionesExcel = mcExcelPosiciones((String)f, "2012",tipo);                                                                                          
                                      
                                         if( i == noArchivos ){ 
                   
                                             int posiciones = revisaBpmPosiciones((String)f, "2012",tipo);
                                                                                                                                                                           
                                             if( tipo.equals("R") ){                                   	 
                                                 mapaPosicionesRegistro.put(f,numeroPosiciones);
                                                 mapaPosicionesRegistroBPM.put(f,posiciones);
                                                 mapaPosicionesRegistroMcontrol.put(f,posicionesExcel);
                                             }else{
                                                   mapaPosicionesRespuesta.put(f, numeroPosiciones);
                                                   mapaPosicionesRespuestaBPM.put(f, posiciones);
                                                   mapaPosicionesRespuestaMcontrol.put(f, posicionesExcel);
                                             }  
                         
                                             numeroPosiciones = 0;
                                                    
                                         }                                   
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                     }catch(IOException | NumberFormatException e){ e.printStackTrace(); }                              
                                                  
                                     return posicionesExcel;
              
                              }
                              
                              public int mcExcelPosiciones(String app,String año,String tipo){ 
   
                                     int posiciones = 0;                                                                               
        
                                     try{
            	                                          
                                         Sheet hoja = wb.getSheetAt(0);                  
                                         Iterator<Row> rowIt = hoja.rowIterator();                                                 
                                         
                                         rowIt.next();
                                         for(Iterator<Row> it = rowIt; it.hasNext(); ){
                                             Row r = it.next();
                                             Cell cAplicacion = r.getCell(0);
                                             String cvc = cAplicacion.getStringCellValue().trim();                                             
                                             if( cvc.matches("^[0-9]+$") ){
                                                 if( Integer.parseInt(cvc) == Integer.parseInt(app) ){                                                     
                                                     Cell cPosiciones;
                                                     if( tipo.equals("R") ){ cPosiciones = r.getCell(21); }
                                                     else{ cPosiciones = r.getCell(22);}
                                                     posiciones += cPosiciones.getNumericCellValue();                     
                                                 }
                                             }
                                             
                                         }
         
                                     }catch(Exception e){ e.printStackTrace(); }                                          
                                     
                                     return posiciones;
     
                              }    
                              
                              public int revisaBpmPosiciones(String aplicacion,String año,String tipo){
     
                                     Connection c;
                                     Statement s;
                                     ResultSet rs;                                                        
     
                                     int posiciones = 0;
      
                                     try{
     
                                         Class.forName("oracle.jdbc.OracleDriver");                   
                                         c = DriverManager.getConnection("jdbc:oracle:thin:@10.10.2.10:1521:ceneval","dpoc","bpm_DPOC");
                        
                                         s = c.createStatement();
                  
                                         String select = "";
                  
                                         if( tipo.equals("R") ){ 
                                             select += "select \"Registrado_desglose\",\"Registrado\" from dpoc where NUM_APLIC = '" + 
                                             aplicacion + "' and extract(year from \"fecha_de_inicio\") ='" + año + "'";
                                         }else{
                	                        select += "select \"Aplicados_desglose\",\"Aplicados\" from dpoc where NUM_APLIC = '" + aplicacion + "' and " + 
                                                          " to_char(\"fecha_de_inicio\",'YYYY') = '" + año + "'";
                                         }
                  
                                         rs = s.executeQuery(select);
                                     
                                         int i = 0;                      
             
                                         while( rs.next() ){
                                                i++;
                                                if( i > 1 ){                             
                                                    posiciones =  rs.getInt(1);                             
                                                    break;
                                                }else{ posiciones = rs.getInt(2); }                             
                    
                                         }         
         
                                     }catch(ClassNotFoundException | SQLException e){ e.printStackTrace(); }
     
                                     return posiciones;
   
                              }

                              @Override
                              public void done(){                                                                                                                                                   
                                     
                                     Set<Object> ks = aplicaciones.keySet();
                                     Set<Object> setAne = aplicacionesInexistentes.keySet();    
                                     Set<Object> setAsd = mapaAplicacionesSinDatif.keySet();                  
                                     Iterator<Object> it = ks.iterator();
                                     Object[] aResultados;
                                     
                                     boolean s1 = false;
                                     boolean s2 = false;   
                                     
                                     boolean imagResDat = false;
                                     boolean imagRegDat = false;
                                                    
                                     int i = 0;
                                     while( it.hasNext() ){
            	                              
                                            i++;
       	                                    ArrayList<Object> ao =  new ArrayList<>();   
                                            ArrayList<Object> aoq = new ArrayList<>();
                                            Object o = it.next();
                                            ao.add(i);
       	                                    ao.add(o);   
                                            aoq.add(o);
                                            aoq.add(mapaTipoAplicacion.get(o));
                                            aoq.add(mapaSubtipoAplicacion.get(o));
                                            aoq.add(sdf.format(fechas.get(o)));
                                            aoq.add(sdf.format(new Date()));
                                           	               	         
        	                                int tamañoNoEncontradas = aplicacionesInexistentes.size() - 1;
       	                                    int tamañoSinDatif = mapaAplicacionesSinDatif.size() - 1;
       	                                    int tamañoCantidadImagenesR = imagEncR.size() - 1;
                                            int tamañoCantidadImagenesS = imagEncS.size() - 1;
       	                                    int tamañoPosicionesRegistro = mapaPosicionesRegistro.size() -1;
       	                                    int tamañoPosicionesRegistroBPM = mapaPosicionesRegistroBPM.size() -1;
       	                                    int tamañoPosicionesRegistroMControl = mapaPosicionesRegistroMcontrol.size() -1;
       	                                    int tamañoPosicionesRespuesta = mapaPosicionesRespuesta.size() - 1;
       	                                    int tamañoPosicionesRespuestaBPM = mapaPosicionesRespuesta.size() - 1;
       	                                    int tamañoPosicionesRespuestaMControl = mapaPosicionesRespuesta.size() - 1;
                                            ao.add(agregaDato(aplicacionesInexistentes, tamañoNoEncontradas,o,false,"Inexistentes"));        	 		
                                            if( !aplicacionesInexistentes.containsKey(o) ){
                                                Object osd = agregaDato(mapaAplicacionesSinDatif, tamañoSinDatif,o,false,"SinDatif");
           	                                    ao.add(osd);           	  	       	                                                                                              
                                                Object ir = agregaDato(imagEncR, tamañoCantidadImagenesR,o,true,"ImagenesR");
           	                                    ao.add(ir);           	  
                                                aoq.add(ir);
                                                Object is = agregaDato(imagEncS, tamañoCantidadImagenesS,o,true,"ImagenesS");
                                                ao.add(is);           	  
                                                aoq.add(is);
                                                Object pr = agregaDato(mapaPosicionesRegistro, tamañoPosicionesRegistro,o,true,"PR");
              	                                ao.add(pr);                              	 
                                                aoq.add(pr);
                                                Object prb = agregaDato(mapaPosicionesRegistroBPM, tamañoPosicionesRegistroBPM,o,true,"PRB");                                                
              	                                ao.add(prb);         	  	                	 
                                                aoq.add(prb);
                                                Object prm = agregaDato(mapaPosicionesRegistroMcontrol, tamañoPosicionesRegistroMControl,o,true,"PRM");
                                                ao.add(prm);                        	 
                                                aoq.add(prm);
                                                Object pres = agregaDato(mapaPosicionesRespuesta, tamañoPosicionesRespuesta, o, true,"PS");
                                                ao.add(pres);             	  		 
                                                aoq.add(pres);
                                                Object presb = agregaDato(mapaPosicionesRespuestaBPM, tamañoPosicionesRespuestaBPM, o, true,"PSB");
                                                ao.add(presb);                                    	       	                	    	                    	       	                	         
                                                aoq.add(presb);
                                                Object presm = agregaDato(mapaPosicionesRespuestaMcontrol, tamañoPosicionesRespuestaMControl, o, true,"PSM");
                                                ao.add(presm);                                                                  
                                                aoq.add(presm);                                                
                                                aoq.add(cRuta.getText().trim());
                                                aoq.add(instituciones.get(o));
       	        	                                                                                 
                                                if( mapaPosicionesRegistro.containsKey(o) && mapaPosicionesRegistroMcontrol.containsKey(o) ){                                             
                                                    
                                                    if( (int)mapaPosicionesRegistro.get(o) != (int)mapaPosicionesRegistroMcontrol.get(o) ){ 
                                                        s1 = true; 
              	                                    }                                                                                                                                    
                                                    
                                                }
                   	       	                    	     
                                                if( mapaPosicionesRespuesta.containsKey(o) && mapaPosicionesRespuestaMcontrol.containsKey(o) ){
                                                    if( (int)mapaPosicionesRespuesta.get(o) != (int)mapaPosicionesRespuestaMcontrol.get(o) ){
                                                        s2 = true; 
                                                    }
                                                }
                                                
                                                if( mapaPosicionesRegistro.containsKey(o) || mapaPosicionesRegistroMcontrol.containsKey(o) ){
                                                    if( (int)imagEncR.get(o) <= 0 ){
                                                        imagRegDat = true;       
                                                    }
                                                }                                             
                                                
                                                if( mapaPosicionesRespuesta.containsKey(o) || mapaPosicionesRespuestaMcontrol.containsKey(o) ){
                                                    if( (int)imagEncS.get(o) <= 0 ){
                                                        imagResDat = true;       
                                                    }
                                                }                                             
                                                
                                              
//                                                System.out.println(o + " " + setAne.contains(o) + " " + setAsd.contains(o) + " " +  s1 + " " + s2 + " " +
//                                                                   alAplicacionesDatsErraticos.contains(o) + " " + appDatMControlNoDat.contains(o));
                                                if( setAne.contains(o) || setAsd.contains(o) || s1 || s2 || alAplicacionesDatsErraticos.contains(o) || 
                                                    appDatMControlNoDat.contains(o) || imagRegDat || imagResDat ){                                                                           
                                                    ao.add("Verificar");                        
                                                    aoq.add("Verificar");
                                                }else{                                                                              
                                                      ao.add("Correcto");                          
                                                      aoq.add("Correcto");                          
                                                }                     
        	  	 
        	                                aResultados = ao.toArray();
        	                                alResultados.add(aoq.toArray());
        	                                dftm.addRow(aResultados);        	                                                                                                                                                                                                                                                                                         
                                                
                                            }else{
                                                  aoq.add(cRuta.getText().trim());
                                                  aoq.add(instituciones.get(o));
       	        	                    
                                                  if( mapaPosicionesRegistro.containsKey(o) && mapaPosicionesRegistroMcontrol.containsKey(o) ){                                             
                                                      if( (int)mapaPosicionesRegistro.get(o) != (int)mapaPosicionesRegistroMcontrol.get(o) ){ 
                                                          s1 = true; 
             	                                      }                       
                                                  }
                   	       	                    	     
                                                  if( mapaPosicionesRespuesta.containsKey(o) && mapaPosicionesRespuestaMcontrol.containsKey(o) ){
                                                      if( (int)mapaPosicionesRespuesta.get(o) != (int)mapaPosicionesRespuestaMcontrol.get(o) ){
                                                           s2 = true; 
                                                      }
                                                  }
                                                  
                                                  if( mapaPosicionesRegistro.containsKey(o) || mapaPosicionesRegistroMcontrol.containsKey(o) ){
                                                      if( (int)imagEncR.get(o) <= 0 ){
                                                          imagRegDat = true;       
                                                      }
                                                  }                                             
                                                
                                                  if( mapaPosicionesRespuesta.containsKey(o) || mapaPosicionesRespuestaMcontrol.containsKey(o) ){
                                                      if( (int)imagEncS.get(o) <= 0 ){
                                                          imagResDat = true;       
                                                      }
                                                  }
                                              
                                                  if( setAne.contains(o) || setAsd.contains(o) || s1 || s2 || alAplicacionesDatsErraticos.contains(o) || 
                                                      appDatMControlNoDat.contains(o) || imagRegDat || imagResDat){                                                                             
                                                      ao.add("Verificar");                        
                                                      aoq.add("Verificar");                        
                                                  }else{                                                                                
                                                        ao.add("Correcto");                          
                                                        aoq.add("Correcto");                          
                                                  }                
                                                
                                                  aResultados = ao.toArray();
                                                  alResultados.add(aoq.toArray());
        	                                      dftm.addRow(aResultados);        	                                                                                                                                                                                                
                                                
                                            }
                                            
                                            s1 = false;
                                            s2 = false;   
                                            imagRegDat = false;
                                            imagResDat = false;
        	     
                                      }    
                                                     
                                      tresultados.setModel(dftm);                                      
                                      
                                      tresultados.getColumn("Observaciones").setCellRenderer(new BotonRender());
                                      tresultados.getColumn("Observaciones").setCellEditor(new ButtonEditor(new JCheckBox(),frame));
 
                                      sResultados = new JScrollPane(tresultados);     
         
                                      gbc = new GridBagConstraints();         
                                      gbc.gridx = 0;
                                      gbc.gridy = 2;          
                                      gbc.weightx = 0.1;
                                      gbc.fill = GridBagConstraints.HORIZONTAL;
                                      gbc.insets = new Insets(5,5,5,5);                                      
                                                                                              
                                      PanelPrincipal.this.add(sResultados,gbc);
                                                                            
                                      PanelPrincipal.this.eProcesando.setText("Terminado");
                                      PanelPrincipal.this.bProcesarAplicaciones.setEnabled(true);
                                      PanelPrincipal.this.bSalvarDatos.setEnabled(true);
                                      
                                      PanelPrincipal.this.revalidate();
                                      PanelPrincipal.this.repaint();                                                           
                                                                        
                                 }                                                               
                                                               
                                 public Object agregaDato(Map<Object,Object> alObjetos,int tamaño,Object o,boolean real,String vieneDe){	        	            	    
    	                                       
    	                                Set<Object> set     = alObjetos.keySet();    	         	        
    	                                Iterator<Object> si = set.iterator();                                        
                                        
    	                                //System.out.println("Size mapa " + (alObjetos.size() - 1));
    	                                
    	                                if( alObjetos.size() == 0){
    	                                	
    	                                	Object objeto = alObjetos.get(o);
                                            boolean existe = alObjetos.containsKey(o);
                                               
                                            //System.out.println("El objetoso " + objeto + " " + vieneDe);
                                               
    	       	                            if( existe ){    	       	    	                                                   
    	       	     	                        if( real ){           
                                                    if( objeto != null ){ return objeto; }
                                                    else{ return 0; } 
                                                }else{ return "No"; }    	       	    	
    	       	                            }else{    	       	    	
     	       	                                  if( real ){ 
                                                      if( objeto != null ){ return objeto; }
                                                      else{ return 0; }
                                                  }
     	       	                                  else{ return "Si"; }     	       	    	
    	       	                            }
    	                                }else{
    	                                
    	                                      while( si.hasNext() ){
                                            
                                                     Object objeto = alObjetos.get(o);
                                                     boolean existe = si.next().equals(o);
                                               
                                                     //System.out.println("El objetoso " + objeto + " " + vieneDe);
                                               
    	          	                                 if( existe ){    	       	    	                                                   
    	       	     	                                 if( real ){           
                                                             if( objeto != null ){ return objeto; }
                                                             else{ return 0; } 
                                                         }else{ return "No"; }    	       	    	
    	       	                                     }else{    	       	    	
     	       	                                           if( real ){ 
                                                               if( objeto != null ){ return objeto; }
                                                               else{ return 0; }
                                                           }
     	       	                                           else{ return "Si"; }     	       	    	
    	       	                                     }
                                               
    	                                      }
    	                                      
    	                                }
    	         	    
    	                                return "Si";
	      
                                 }
                                                                  
                                                          
                              };
                                                                                                             
                              chamber.execute();
                              
                       }
                                                                               
                    }
                       
               );
               
               bSalvarDatos = new JButton("Guardar");
               bSalvarDatos.setEnabled(false);
               bSalvarDatos.addActionListener(new ActionListener() {

                   @Override
                   public void actionPerformed(ActionEvent e) {
                         
                          SwingWorker<?, ?> worker;
                          worker = new SwingWorker<Object, Object>() {

                                  @Override
                                  protected Void doInBackground(){                                                                                  
                                            
                                            //Connection c = conexionBase.getC(localhost,"ceneval","user","slipknot");
                                            Connection c = conexionBase.getC(remoto,"ceneval","test","slipknot");
                                            
                                            Statement  s = null;
                                                                                                                                 
                                            try{                                                                                                 
                                                                                                 
                                                String select =  "select no_aplicacion from viimagenes where no_aplicacion = '";
                                                for( Object[] datosArreglo : alResultados ){                                                                                                     
                                                     Object no_aplicacion = datosArreglo[0];
                                                     select += no_aplicacion + "'";
                                                     System.out.println(select);
                                                     s = c.createStatement();    
                                                     ResultSet  rs = s.executeQuery(select);                                                     
                                                     
                                                     if( !rs.isBeforeFirst() ){
                                                         
                                                         String insert = "insert into viimagenes(no_aplicacion,instrumento,nombre,fecha_alta," +
                                                                         "fecha_registro,imag_reg,imag_res,pregistro,pregistrobpm,pregistromcontrol," + 
                                                                         "prespuesta,prespuestabpm,prespuestamcontrol,ruta,institucion,estado) values('";
                                                                                            
                                                         int i = 0;
                                                         int longitud = datosArreglo.length - 1;
                                                         System.out.println("longitud " + longitud);
                                                         for( Object dato: datosArreglo ){
                                                          
                                                              if( i == longitud ){ insert += dato + "')"; }
                                                              else{ insert += dato + "','"; }
                                                              i++;                                                                                                                                                                                                                                              
                                                               
                                                         }                                                                                                                  
                                                           
                                                         System.out.println(insert);
                                                         s.executeUpdate(insert);                                                             
                                                         
                                                     }else{
                                                           
                                                           s  = c.createStatement();                                                               
                                                           
                                                           while( rs.next() ){
                                                                  System.out.println(no_aplicacion + " existe");
                                                                  String update = "update viimagenes set ";
                                                                  String[] campos = {"instrumento","nombre","fecha_alta","fecha_registro","imag_reg","imag_res",
                                                                                     "pregistro","pregistrobpm","pregistromcontrol","prespuesta","prespuestabpm",
                                                                                     "prespuestamcontrol","ruta","institucion","estado"};
                                                            
                                                                  int longitud = datosArreglo.length - 1;
                                                                  System.out.println("longitud " + longitud);
                                                                  for( int i = 1; i <= datosArreglo.length - 1; i++ ){
                                                                       if( i == longitud ){                                                                      
                                                                           update += campos[i - 1] + " = '" + datosArreglo[i] + "' where no_aplicacion = '" +
                                                                                     no_aplicacion + "'";                                                                                                                                                                                                                                                                                                                                                                                        
                                                                       }else{
                                                                             update += campos[i - 1] + " = '" + datosArreglo[i] + "',";
                                                                       }
                                                                  }
                                                             
                                                                  System.out.println(update);
                                                                  s.executeUpdate(update);                                                                                                                                                                                                                                             
                                                               
                                                           }                                                                                                                       
                                                                                                                                                                                 
                                                     }                                                                                                          
                                                                                                          
                                                     select = "select no_aplicacion from viimagenes where no_aplicacion = '";
                                                     rs.close();
                                                
                                                }                                                                                             
                                                                                                
                                                JOptionPane.showMessageDialog(
                                                            null,
                                                            "Datos salvados correctamente",
                                                            "Exito",
                                                            JOptionPane.INFORMATION_MESSAGE);
                                                
                                                
                                            }catch(SQLException e){ 
                                                
                                                   int codigoError = e.getErrorCode();
                                                           
                                                   e.printStackTrace();
                                                   
                                                   if( codigoError > 0 ){
                                                       JOptionPane.showMessageDialog(
                                                                   null,
                                                                   "Codigo de error " + codigoError + 
                                                                   ".Reportalo al administrador del sistema",
                                                                   "Error",
                                                                   JOptionPane.ERROR_MESSAGE);                                                                                                                                                                             
                                                   }
                                                   
                                            }
                                            
                                            finally{ 
                                                    try{                                                        
                                                        s.close();
                                                     c.close();
                                                    }catch(SQLException e){ e.printStackTrace(); }
                                            }
                                      
                                            return null;
                                      }
                                  
                                      @Override
                                      public void done(){
                                    	     PanelPrincipal.this.eProcesando.setText("En espera");
                                      }
                            
                                };
                                  
                                worker.execute();
                                  
                          };
                          
                          
                          
               });                                     
                             
               gbc = new GridBagConstraints();
               gbc.anchor = GridBagConstraints.SOUTHEAST;
               gbc.weightx = 0.1;
               gbc.weighty = 0.1;
               gbc.insets = new Insets(5,5,5,5);
               panelDown.add(eProcesando,gbc); 
               
               gbc = new GridBagConstraints();
               gbc.anchor = GridBagConstraints.SOUTHEAST;
               gbc.weightx = 0.1;
               gbc.weighty = 0.1;
               gbc.insets = new Insets(5,5,5,5);
               panelDown.add(bSalvarDatos,gbc);                              
                             
               gbc = new GridBagConstraints();
               gbc.gridx = 4;
               gbc.gridy = 2;
               gbc.weightx = 0.1;
               gbc.weighty = 0.1;
               gbc.insets = new Insets(5,5,5,5);
               panelAplicacion.add(bProcesarAplicaciones,gbc);
               
               JPanel panelUp = new JPanel();
               panelUp.add(panelExcel);
               panelUp.add(panelAplicacion);
               
               gbc = new GridBagConstraints();               
               gbc.anchor = GridBagConstraints.WEST;
               add(panelUp,gbc);                                                                           
               
               gbc = new GridBagConstraints();                    
               gbc.gridy = 4;
               gbc.anchor = GridBagConstraints.SOUTH;
               gbc.fill = GridBagConstraints.HORIZONTAL;               
               gbc.insets = new Insets(5,5,5,5);
               gbc.weighty = 0.5;
               add(panelDown,gbc);
                         
       }                
                 
       public static void main(String args[]){
             
              try{
                  
                  JTabbedPane tabPane = new JTabbedPane();                                                          
                                      
                  JPanel contenedor = new JPanel();
                  contenedor.setLayout(new GridBagLayout());
              
                  GridBagConstraints gbc = new GridBagConstraints();                                              
                  try {
			   		   contenedor.add(new PanelPrincipal(),gbc);
				  } catch (InvalidFormatException e) { e.printStackTrace(); }
                            
                  tabPane.addTab("Consolidacion",contenedor);
                  tabPane.addTab("Generar Reportes",new GenerarReportes(""));
                   
                  frame = new JFrame("Consolidacion de datos");
                  frame.setExtendedState(JFrame.MAXIMIZED_BOTH);              
                  frame.setContentPane(tabPane);                  
                  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                  
                  frame.setVisible(true);
                  
              }catch(IOException | HeadlessException e){ e.printStackTrace(); }
                                                  
       }
      
}
