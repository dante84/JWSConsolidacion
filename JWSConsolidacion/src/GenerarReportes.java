
//@author Daniel.Meza
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Row;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.draw.VerticalPositionMark;

import java.awt.HeadlessException;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.LocalDate;
import org.joda.time.chrono.ISOChronology;
 
public class GenerarReportes extends JPanel{
    
       private static final long serialVersionUID = 1L;
   	   
       private JLabel etiquetaInstrumento,etiquetaSubAplicacion,etiquetaNoAplicacion,etiquetaFi,etiquetaDiaFi,etiquetaMesFi,etiquetaAñoFi,
                      etiquetaFf,etiquetaDiaFf,etiquetaMesFf,etiquetaAñoFf,etiquetaFiltros;                           
       private JTextField campoNoAplicacion;
       private JButton botonGenerarvistaPreviaReporte,botonGenerarReporte;        
       private JScrollPane panelTabla;
       private static JPanel panelFiltros,panelFiltroAplicacion,panelFiltroInstrumento;
       private JTable tabla;                          
       private GridBagConstraints gbc;
       private JComboBox<String> comboTipoInstr,comboNombres_cortos,comboDiaFi,comboMesFi,comboAñoFi,comboDiaFf,comboMesFf,comboAñoFf,
                                 comboFiltros;
       private ArrayList<Object[]> datosReporte;     
              
       private final String[] tiposInstrumento = {"AC286","ACRESEC","ACRETSU","ACUERDO","ALI","CEAACES","CONALEP","DGESPE","ECCYPEC","ECELE","ECODEMS","EGAL",
                                                  "EGEL","EGETSU","EPROM","ESPECIALES","EUC","EUCCA","EXANI","EXTRA","IFE","LEPRE_LEPRI","MCEF","Metropolitano",                                                  
                                                  "MINNESOTA","OLIMPIADA","PILOTO","PREESCOLAR_BACH","PREESCOLAR_LIC","SEISP","SSP","TRIF","UPN"
                                                 };
                  
       private final String[] nombresColumnas = {"No.","Aplicacion","Examen","Fecha App","Fecha de Proc","Imag Reg","Imag Res","Reg","Reg mc",
                                                 "Aplicados","Aplicados mc","Ruta","Estado","Observacion"};
              
       private final String[] años    = {"2012","2013"}; 
       private final String[] meses   = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
       private final String[] filtros = {"Numero de aplicacion","Tipo de instrumento"};
       
       private String name,añoFiSeleccionado,añoFfSeleccionado;
       private boolean estadoFiltroAplicacion = true,estadoFiltroInstrumento = false,estadoFiltroFechas = false;
       final int nombresCantidad = nombresColumnas.length - 1;
       private String short_name = "";
       private ConexionBase conexionBase;
       int year,month;
       private final String remoto = "172.16.50.14";
       private final String localhost = "127.0.0.1";
              
       public GenerarReportes(String nombre){
             
    	      name = nombre;
    	      
    	      setLayout(new GridBagLayout());                            
    	      
              conexionBase = new ConexionBase();
              
              gbc = new GridBagConstraints();
              
              panelFiltros = new JPanel();
              
              etiquetaFiltros = new JLabel("Filtro por : ");              
              comboFiltros = new JComboBox<>(filtros);
              comboFiltros.addActionListener(new ActionListener() {

                           @Override
                           public void actionPerformed(ActionEvent e) {
                                
                                  SwingWorker<Void,Void> sw;
                                  sw = new SwingWorker<Void, Void>() {

                                      @Override
                                      protected Void doInBackground() throws Exception {
                                          
                                                try{
                                                    
                                                    String comando = GenerarReportes.this.comboFiltros.getActionCommand();
                                                    
                                                    if( comando.equals("comboBoxChanged") ){ 
                                                        
                                                        String filtroSeleccionado = (String)GenerarReportes.this.comboFiltros.getSelectedItem();
                                                        
                                                        if( filtroSeleccionado.equals(filtros[0]) ){
                                                        
                                                            GenerarReportes.this.remove(panelFiltroInstrumento);
                                                            GenerarReportes.this.add(panelFiltroAplicacion);
                                                        
                                                            estadoFiltroInstrumento = false;
                                                            estadoFiltroAplicacion = true;
                                                        }
                                                        
                                                        if( filtroSeleccionado.equals(filtros[1]) ){
                                                            GenerarReportes.this.remove(panelFiltroAplicacion);                                                        
                                                            GenerarReportes.this.add(panelFiltroInstrumento);                                                            
                                                            estadoFiltroInstrumento = true;
                                                            estadoFiltroAplicacion = false;
                                                        }                                                                                                                
                                                        
                                                        GenerarReportes.this.revalidate();
                                                        GenerarReportes.this.repaint();
                                                        
                                                    }                                                                                                               
                                                                                                  
                                                }catch(Exception e){ e.printStackTrace();}
                                                
                                          
                                                return null;
                                           
                                      }
                                      
                                      
                                  };
                                  
                                  sw.execute();
                                  
                           }
              });
                               
              panelFiltros.add(etiquetaFiltros);
              panelFiltros.add(comboFiltros);
              
              gbc = new GridBagConstraints();
              gbc.gridx = 0;
              gbc.gridy = 0;              
              gbc.weightx = 0.1;              
              gbc.anchor  = GridBagConstraints.EAST;
              gbc.insets = new Insets(5,5,5,5);
              add(panelFiltros,gbc);
                      
              panelFiltroAplicacion = new JPanel(new GridBagLayout());              
              panelFiltroAplicacion.setSize(500, 200);
              
              etiquetaNoAplicacion = new JLabel("Numero de aplicacion : ");              
              gbc = new GridBagConstraints();
              gbc.gridx = 0;                            
              gbc.weightx = 0.1;              
              gbc.anchor  = GridBagConstraints.WEST;
              gbc.insets = new Insets(5,5,5,5);
              panelFiltroAplicacion.add(etiquetaNoAplicacion,gbc);                                     
              
              campoNoAplicacion = new JTextField(9);
              gbc = new GridBagConstraints();
              gbc.gridx = 1;              
              gbc.weightx = 0.1;              
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.EAST;
              panelFiltroAplicacion.add(campoNoAplicacion,gbc);                                     
              
              panelFiltroInstrumento = new JPanel(new GridBagLayout());
                                          
              etiquetaInstrumento = new JLabel("Instrumento : ");
              gbc.gridx = 0;
              gbc.gridy = 0;
              gbc = new GridBagConstraints();              
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaInstrumento,gbc);                                     
              
              comboTipoInstr = new JComboBox<>(tiposInstrumento);
              comboTipoInstr.addActionListener(
                      new ActionListener() {
                          @Override
                          public void actionPerformed(ActionEvent e) {                                                         
                          
                       	         SwingWorker<?, ?> swbe; 
                                 swbe = new SwingWorker<Object, Object>() {
                                     
                                 @Override
                                 protected Object doInBackground() throws Exception {
                                                                                             
                                           panelFiltroInstrumento.remove(comboNombres_cortos);
                        
                                           String itExamen = (String)comboTipoInstr.getSelectedItem();                                                
                        
                                           gbc = new GridBagConstraints();
                        
                                           comboNombres_cortos = new JComboBox<>(traeNombresCortos(itExamen));                                      
                                           gbc = new GridBagConstraints();
                                           gbc.gridx = 6;
                                           gbc.gridy = 0;                                                                                       
                                           gbc.gridwidth = 3;
                                           gbc.insets = new Insets(5,5,5,5);
                                           gbc.anchor = GridBagConstraints.WEST;                                                          
                                           panelFiltroInstrumento.add(comboNombres_cortos,gbc);                                               
                                           panelFiltroInstrumento.revalidate();
                                           panelFiltroInstrumento.repaint();   
                                               
                                           return null;
                              
                                 }
                                                                                                                                                        
                                 private String[] traeNombresCortos(String item){ 

                                         Connection c;
                                         Statement s;
                                         String[] nombres_cortosArray = null;

                                         try{
                                               
                                             c = conexionBase.getC(remoto,"replicasiipo","test","slipknot");
                                             //c = conexionBase.getC(localhost,"replicasiipo","test","slipknot");                                             
   
                                             String select = "select nom_corto from datos_examenes where tipo_instr = '" + item + "'";
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
                     
                                         }catch(SQLException e){ e.printStackTrace(); }
                                              
                                         return nombres_cortosArray;       

                                 } 
                                    
                                 };
                                       
                                 swbe.execute();
                            
                          }
                     
                      }
            
              );
              
              gbc = new GridBagConstraints();                            
              gbc.gridx = 1;
              gbc.gridy = 0;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.EAST;
              panelFiltroInstrumento.add(comboTipoInstr,gbc);
              
    	      datosReporte = new ArrayList<>();    	                                                                                         
              
              etiquetaSubAplicacion = new JLabel("Nombre : ");
              gbc = new GridBagConstraints();
              gbc.gridx = 2;              
              gbc.gridy = 0;              
              gbc.weightx = 0.5;              
              gbc.insets  = new Insets(5,5,5,5);              
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaSubAplicacion,gbc); 
              
              comboNombres_cortos = new JComboBox<>();
              gbc = new GridBagConstraints();
              gbc.gridx = 3;
              gbc.gridy = 0;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor = GridBagConstraints.EAST;              
              panelFiltroInstrumento.add(comboNombres_cortos,gbc);                                                                      
                            
              etiquetaFi = new JLabel("Fecha inicial");
              gbc = new GridBagConstraints();
              gbc.gridx = 0;
              gbc.gridy = 1;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaFi,gbc);   
              
              etiquetaAñoFi = new JLabel("Año : ");
              gbc = new GridBagConstraints();
              gbc.gridx = 1;
              gbc.gridy = 1;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaAñoFi,gbc);   
              
              comboAñoFi = new JComboBox<>(años);
              comboAñoFi.addActionListener(new ActionListener() {

                         @Override
                         public void actionPerformed(ActionEvent e) {
                                 
                                JComboBox<?> event = (JComboBox<?>)e.getSource();
                                añoFiSeleccionado = ((String)event.getSelectedItem()).trim();
                          
                         }
                         
              });
              
              gbc = new GridBagConstraints();
              gbc.gridx = 2;
              gbc.gridy = 1;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.EAST;
              panelFiltroInstrumento.add(comboAñoFi,gbc);                             
              
              etiquetaMesFi = new JLabel("Mes : ");
              gbc = new GridBagConstraints();
              gbc.gridx = 3;
              gbc.gridy = 1;                     
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaMesFi,gbc);   
              
              comboMesFi = new JComboBox<>(meses);
              comboMesFi.addActionListener(new ActionListener(){
                	
                         @Override
                         public void actionPerformed(ActionEvent e) {                                                             
                                 
                                SwingWorker<Void,Void> sw;
                                sw = new SwingWorker<Void,Void>(){                                                                                
                                     
                                @Override
                                protected Void doInBackground() {                                                                                    
                                                    
                                          try{
                                              
                                              System.out.println("en el fondo");
                                              String comando = GenerarReportes.this.comboAñoFi.getActionCommand();
                                              System.out.println("comando " + comando);

                                              int año = 2012;                                                    
                                                    
                                              if( comando.equals("comboBoxChanged") ){                                                                                                                
                                                        
                                                  for( int i = 0; i <= años.length - 1; i++ ){                                                             
                                                       if( añoFiSeleccionado.equals(años[i])){ año = i + 1; }
                                                  }
                                                    
                                                  int mes = comboMesFi.getSelectedIndex() + 1;
                                                   
                                                  System.out.println(año + " " + mes);
                                
                                                  Chronology cronologia = ISOChronology.getInstance();
                                                  DateTimeField dtf = cronologia.dayOfMonth();
                                                  LocalDate ld = new LocalDate(año,mes,1);
                                
                                                  int dias = dtf.getMaximumValue(ld); 
                                                  System.out.println("La cantidad de dias del mes " + mes  + " del año " + añoFiSeleccionado + " son : " + dias);
                                                  String cadenasDias [] = new String[dias];
                                                   
                                                  for( int i = 0; i < dias; i++ ){                                                                                                            
                                                       cadenasDias[i] = String.valueOf( i + 1 );                                                      
                                                  }                                                                                                                                                                                                                            
                                                        
                                                  GenerarReportes.panelFiltroInstrumento.remove(GenerarReportes.this.comboDiaFi);
                                                    
                                                  GenerarReportes.this.comboDiaFi = new JComboBox<>(cadenasDias);
                                                  gbc = new GridBagConstraints();
                                                  gbc.gridx = 6;
                                                  gbc.gridy = 1;    
                                                  gbc.weightx = 0.1;                            
                                                  gbc.insets = new Insets(5,5,5,5);
                                                  gbc.anchor  = GridBagConstraints.EAST;
                                                  GenerarReportes.panelFiltroInstrumento.add(GenerarReportes.this.comboDiaFi,gbc);
                                                        
                                         }}catch(Exception e){ e.printStackTrace();}
                               
                                         return null;
                                         
                                }
                                         
                                @Override
                                public void done(){                                                                                               
                                                
                                       GenerarReportes.panelFiltroInstrumento.revalidate();
                                       GenerarReportes.panelFiltroInstrumento.repaint();
                                              
                                }
                                         
                           };
                                
                           sw.execute();
                                                          
                     }    
                         
              });
              
              gbc = new GridBagConstraints();
              gbc.gridx = 4;
              gbc.gridy = 1;    
              gbc.weightx = 0.1;              
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.EAST;
              panelFiltroInstrumento.add(comboMesFi,gbc); 
              
              etiquetaDiaFi = new JLabel("Dia : ");
              gbc = new GridBagConstraints();
              gbc.gridx = 5;
              gbc.gridy = 1;    
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaDiaFi,gbc);   
              
              comboDiaFi = new JComboBox<>();
              gbc = new GridBagConstraints();
              gbc.gridx = 6;
              gbc.gridy = 1;    
              gbc.weightx = 0.1;                            
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.EAST;
              panelFiltroInstrumento.add(comboDiaFi,gbc);   
              
              etiquetaFf = new JLabel("Fecha final");
              gbc = new GridBagConstraints();
              gbc.gridx = 0;
              gbc.gridy = 2;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaFf,gbc);   
              
              etiquetaAñoFf = new JLabel("Año : ");
              gbc = new GridBagConstraints();
              gbc.gridx = 1;
              gbc.gridy = 2;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaAñoFf,gbc);   
              
              comboAñoFf = new JComboBox<>(años);
              comboAñoFf.addActionListener(new ActionListener() {

                         @Override
                         public void actionPerformed(ActionEvent e) {
                                 
                                JComboBox<?> event = (JComboBox<?>)e.getSource();
                                añoFfSeleccionado = ((String)event.getSelectedItem()).trim();                                
                          
                         }
                         
              });
              
              gbc = new GridBagConstraints();
              gbc.gridx = 2;
              gbc.gridy = 2;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.EAST;
              panelFiltroInstrumento.add(comboAñoFf,gbc);                             
              
              etiquetaMesFf = new JLabel("Mes : ");
              gbc = new GridBagConstraints();
              gbc.gridx = 3;
              gbc.gridy = 2;                     
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaMesFf,gbc);   
              
              comboMesFf = new JComboBox<>(meses);
              comboMesFf.addActionListener(new ActionListener(){
                	
                         @Override
                         public void actionPerformed(ActionEvent e) {                                                             
                                 
                                SwingWorker<Void,Void> sw;
                                sw = new SwingWorker<Void,Void>(){                                                                                
                                     
                                @Override
                                protected Void doInBackground() {                                                                                    
                                                    
                                          try{
                                              
                                              System.out.println("en el fondo");
                                              String comando = GenerarReportes.this.comboAñoFf.getActionCommand();
                                              System.out.println("comando " + comando);

                                              int año = 2012;                                                    
                                                    
                                              if( comando.equals("comboBoxChanged") ){                                                                                                                
                                                        
                                                  for( int i = 0; i <= años.length - 1; i++ ){                                                             
                                                       if( añoFfSeleccionado.equals(años[i])){ año = i + 1; }
                                                  }
                                                    
                                                  int mes = comboMesFf.getSelectedIndex() + 1;
                                                   
                                                  System.out.println(año + " " + mes);
                                
                                                  Chronology cronologia = ISOChronology.getInstance();
                                                  DateTimeField dtf = cronologia.dayOfMonth();
                                                  LocalDate ld = new LocalDate(año,mes,1);
                                
                                                  int dias = dtf.getMaximumValue(ld); 
                                                  System.out.println("La cantidad de dias del mes " + mes  + " del año " + añoFfSeleccionado + " son : " + dias);
                                                  String cadenasDias [] = new String[dias];
                                                   
                                                  for( int i = 0; i < dias; i++ ){                                                                                                            
                                                       cadenasDias[i] = String.valueOf( i + 1 );                                                      
                                                  }                                                                                                                                                                                                                            
                                                        
                                                  GenerarReportes.panelFiltroInstrumento.remove(GenerarReportes.this.comboDiaFf);
                                                    
                                                  GenerarReportes.this.comboDiaFf = new JComboBox<>(cadenasDias);
                                                  gbc         = new GridBagConstraints();
                                                  gbc.gridx   = 6;
                                                  gbc.gridy   = 2;    
                                                  gbc.weightx = 0.1;                            
                                                  gbc.insets  = new Insets(5,5,5,5);
                                                  gbc.anchor  = GridBagConstraints.EAST;
                                                  GenerarReportes.panelFiltroInstrumento.add(GenerarReportes.this.comboDiaFf,gbc);
                                                        
                                         }}catch(Exception e){ e.printStackTrace();}
                               
                                         return null;
                                         
                                }
                                         
                                @Override
                                public void done(){                                                                                               
                                                
                                       GenerarReportes.panelFiltroInstrumento.revalidate();
                                       GenerarReportes.panelFiltroInstrumento.repaint();
                                              
                                }
                                         
                           };
                                
                           sw.execute();
                                                           
                     }    
                         
              });
              
              gbc = new GridBagConstraints();
              gbc.gridx = 4;
              gbc.gridy = 2;    
              gbc.weightx = 0.1;              
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.EAST;
              panelFiltroInstrumento.add(comboMesFf,gbc); 
              
              etiquetaDiaFf = new JLabel("Dia : ");
              gbc = new GridBagConstraints();
              gbc.gridx = 5;
              gbc.gridy = 2;    
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              panelFiltroInstrumento.add(etiquetaDiaFf,gbc);   
              
              comboDiaFf = new JComboBox<>();
              gbc = new GridBagConstraints();
              gbc.gridx = 6;
              gbc.gridy = 2;    
              gbc.weightx = 0.1;                            
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.EAST;
              panelFiltroInstrumento.add(comboDiaFf,gbc);                               
                            
              gbc = new GridBagConstraints();
              gbc.gridx = 3;
              gbc.gridy = 5;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              //panelFiltroAplicacion.add(botonGenerarReporte,gbc);                                                          
              
              gbc = new GridBagConstraints();
              gbc.gridx = 1;
              gbc.gridy = 0;    
              gbc.weightx = 0.1;
              add(panelFiltroAplicacion,gbc);
              
              tabla = new JTable();
             
              panelTabla = new JScrollPane(tabla);              
              panelTabla.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
              panelTabla.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
              
              gbc = new GridBagConstraints();
              gbc.gridx = 0;
              gbc.gridy = 1;    
              gbc.weightx = 1;
              gbc.weighty = 1;    
              gbc.gridwidth = 9;
              gbc.insets = new Insets(0, 10, 0, 10);
              gbc.fill = GridBagConstraints.HORIZONTAL;
              gbc.anchor = GridBagConstraints.NORTH;
              add(panelTabla,gbc);                     
              
              botonGenerarvistaPreviaReporte = new JButton("Generar vista previa");
              botonGenerarvistaPreviaReporte.addActionListener(new ActionListener() {

                      @Override
                      public void actionPerformed(ActionEvent e) {
                          
                             SwingWorker<Void,Void> sw;
                             sw = new SwingWorker<Void,Void>(){
                		     
                 	     Connection c = null;
                             Statement  s = null;                  
                             ResultSet  rsMysql = null;                                                             
                              
                	     @Override
                	     protected Void doInBackground() throws Exception {
                                                                                                        
                                       System.out.println("aplicacion " + estadoFiltroAplicacion);                             
                                       System.out.println("Instrumento " + estadoFiltroInstrumento);                             
                                       System.out.println("Fechas " + estadoFiltroFechas);                             
                		        	                 	                	        	                                                                                                                                 
                                       if( estadoFiltroAplicacion ){
                                           String aplicacion = campoNoAplicacion.getText().trim();
                                           queryReporteAplicacion(aplicacion); 
                                       }                                           
                                       if( estadoFiltroInstrumento ){ queryReporteInstrumento(); }                                           

                                       
                                       return null;
                                       
                	     }
                             
                             public void queryReporteAplicacion(String nomApp){                                                                        
                                    
                                    try{
                                        c = conexionBase.getC(remoto,"ceneval","test","slipknot");
                                        //c = conexionBase.getC(localhost,"ceneval","user","slipknot");                                        
                                        
                                        s = c.createStatement();
                                                                                   
                                        String select;                                                                                                                                                                         
                                        select = "select * from viimagenes where no_aplicacion = '" + nomApp + "'";
                                           
                                        System.out.println(select);
                                           
                                        rsMysql = s.executeQuery(select);                                        
                                                  
                                        if( !rsMysql.isBeforeFirst() ){
                                            
                                             JOptionPane.showMessageDialog(
                                                         null,
                                                         "No existe ese numero de aplicacion",
                                                         "Aplicacion Inexistente",
                                                         JOptionPane.WARNING_MESSAGE);    
                                             
                                        }else{
                                            
                                              DefaultTableModel dtm = new DefaultTableModel();
                                         
                                              TableCellRenderer renderer = new JComponentTableCellRenderer();                                                                                                                                                                                                                  
                                          
                                              for( int l = 0;l <= nombresCantidad;l++){ dtm.addColumn(""); }
                                          
                                              tabla.setModel(dtm);
                                              TableColumnModel columnModel = tabla.getColumnModel();
                                          
                                              for( int k = 0; k <= nombresCantidad; k++ ){
                                                   TableColumn tcTemp = columnModel.getColumn(k);                                                              
                                                   JLabel encabezado = new JLabel(nombresColumnas[k]);
                                                   tcTemp.setHeaderRenderer(renderer);
                                                   tcTemp.setHeaderValue(encabezado);
                                              }
                                                                      
                                              int consecutivo = 1;
                                              while( rsMysql.next() ){               
                                                   
                                                     int numApp         = rsMysql.getInt(2);                                                     
                                                     String nombre      = (rsMysql.getString(4) != null) ? rsMysql.getString(4) : " ";  
                     		                     Date alta          = rsMysql.getDate(5);
                                                     Date registro      = rsMysql.getDate(6);
                                                     int imagReg        = rsMysql.getInt(7);
                                                     int imagRes        = rsMysql.getInt(8);
                                                     int preg           = rsMysql.getInt(9);                                                     
                                                     int pregmc         = rsMysql.getInt(11);
                                                     int pres           = rsMysql.getInt(12);                                                     
                                                     int presmc         = rsMysql.getInt(14);  
                                                     String ruta        = rsMysql.getString(15);                                                     
                                                     String estado      = (rsMysql.getString(17) != null) ? rsMysql.getString(17) : " ";                                                     
                                                     String observacion = (rsMysql.getString(18) != null) ? rsMysql.getString(18) : " ";                                                     
                                                                                                          
                                                     short_name = nombre;
                                                     year = new DateTime(alta).getYear();
                                                     month = new DateTime(alta).getMonthOfYear();
                                                     
                                                     System.out.println(consecutivo + " " +numApp + " " + nombre + " " +alta.toString() + " " + registro.toString() + 
                                                                        " " + imagReg + " " + imagRes + " " + preg + " " + pregmc + " " + pres + 
                                                                        " " + presmc + " " + ruta + " " + estado + " " + 
                                                                        observacion);
                                                     
                                                     Object[] datos = new Object[]{ consecutivo,numApp,nombre,alta.toString(),registro.toString(),imagReg,imagRes,
                                                                                    preg,pregmc,pres,presmc,ruta,estado,observacion };
                             
                                                     datosReporte.add(datos);                             
                                                     dtm.addRow(datos);                                                                  
                                               
                                              }
                                              
                                        }
                                          
                                        s.close();
                                        c.close();
                                        rsMysql.close();
                                                                                                                                                                                                                                                                                                                                                                        
                                    }catch(SQLException | HeadlessException e){ e.printStackTrace(); }
                                    finally{
                                            try{
                                                rsMysql.close();                                                        
                                                s.close();
                                                c.close();
                                            }catch(Exception e){ e.printStackTrace(); }                                  
                                    }
                                    
                             }
                             
                             public void queryReporteInstrumento(){
                                   
                                    try{
                                                                                
                                        //c = conexionBase.getC(localhost,"ceneval","user","slipknot");
                                        c = conexionBase.getC(remoto,"ceneval","test","slipknot");
                                        s = c.createStatement();
                                        
                                        int añoFi = Integer.valueOf(comboAñoFi.getSelectedItem().toString());
                                        String mesFi;
                                        int enteroMesFi = 0;
                                        
                                        for( int i = 0; i <= meses.length - 1; i++ ){
                                             mesFi = comboMesFi.getSelectedItem().toString();   
                                             if( mesFi != null ){
                                                 if( mesFi.equals(meses[i])){
                                                     enteroMesFi = i + 1;
                                                 }
                                             }
                                              
                                        }
                                        
                                        int diaFi = Integer.valueOf(comboDiaFi.getSelectedItem().toString());
                                                                                
                                        DateTime dtFi = new DateTime(añoFi, enteroMesFi,diaFi , 0,0);
                                        
                                        int añoFf = Integer.valueOf(comboAñoFf.getSelectedItem().toString());
                                        String mesFf;
                                        int enteroMesFf = 0;
                                        
                                        for( int i = 0; i <= meses.length - 1; i++ ){
                                             mesFf = comboMesFf.getSelectedItem().toString();   
                                             if( mesFf != null ){
                                                 if( mesFf.equals(meses[i])){
                                                     enteroMesFf = i + 1;
                                                 }
                                             }
                                              
                                        }
                                        
                                        int diaFf = Integer.valueOf(comboDiaFf.getSelectedItem().toString());
                                                                                
                                        DateTime dtFf = new DateTime(añoFf, enteroMesFf,diaFf , 0,0);
                                        
                                        Date dateFi = dtFi.toDate();
                                        Date dateFf = dtFf.toDate();
                                                                                
                                        if( dateFi.after(dateFf) ){
                                            JOptionPane.showMessageDialog(
                                                        null,
                                                        "La fecha inicial no puede ser menor a la final",
                                                        "Fechas Incorrectas",
                                                        JOptionPane.WARNING_MESSAGE); 
                                        }else{
                                        
                                              System.out.println(dateFi + " " + dateFf);
                                              String select;                                                                                   
                                              
                                              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);                    
                                              
                                              String cqfi = sdf.format(dateFi);
                                              String cqff = sdf.format(dateFf);
                                                                                                                          
                                              String instrumento = comboTipoInstr.getSelectedItem().toString().trim();
                                              String nombre = comboNombres_cortos.getSelectedItem().toString().trim();
                                              select = "select * from viimagenes where  instrumento = '" + instrumento + "' and nombre = '" + nombre + "'" +
                                                       " and fecha_alta >= '" + cqfi + "' and fecha_alta <= '" + cqff + "'";
                                           
                                              System.out.println(select);
                                           
                                              rsMysql = s.executeQuery(select);                                        
                                                  
                                               if( !rsMysql.isBeforeFirst() ){
                                            
                                                JOptionPane.showMessageDialog(
                                                         null,
                                                         "No existe ese numero de aplicacion",
                                                         "Aplicacion Inexistente",
                                                         JOptionPane.WARNING_MESSAGE);    
                                             
                                               }else{
                                            
                                                     DefaultTableModel dtm = new DefaultTableModel();
                                         
                                                     TableCellRenderer renderer = new JComponentTableCellRenderer();                                                                                                                                                                                                                  
                                          
                                                     for( int l = 0;l <= nombresCantidad;l++){ dtm.addColumn(""); }
                                          
                                                     tabla.setModel(dtm);
                                                     TableColumnModel columnModel = tabla.getColumnModel();
                                          
                                                     for( int k = 0; k <= nombresCantidad; k++ ){
                                                          TableColumn tcTemp = columnModel.getColumn(k);                                                              
                                                          JLabel encabezado = new JLabel(nombresColumnas[k]);
                                                          tcTemp.setHeaderRenderer(renderer);
                                                          tcTemp.setHeaderValue(encabezado);
                                                     }
                                                            
                                                     int consecutivo = 1;
                                                     while( rsMysql.next() ){               
                                                   
                                                            int numApp         = rsMysql.getInt(2);                                                     
                                                            String nombre_corto      = (rsMysql.getString(4) != null) ? rsMysql.getString(4) : " ";  
                     		                            Date alta          = rsMysql.getDate(5);
                                                            Date registro      = rsMysql.getDate(6);
                                                            int imagReg        = rsMysql.getInt(7);
                                                            int imagRes        = rsMysql.getInt(8);
                                                            int preg           = rsMysql.getInt(9);                                                            
                                                            int pregmc         = rsMysql.getInt(11);
                                                            int pres           = rsMysql.getInt(12);                                                            
                                                            int presmc         = rsMysql.getInt(14);  
                                                            String ruta        = rsMysql.getString(15);                                                                                                         
                                                            String estado      = (rsMysql.getString(17) != null) ? rsMysql.getString(17) : " ";                                                     
                                                            String observacion = (rsMysql.getString(18) != null) ? rsMysql.getString(18) : " ";     
                                                                                                                        
                                                            short_name = nombre;
                                                            year = new DateTime(alta).getYear();
                                                            month = new DateTime(alta).getMonthOfYear();
                                                     
                                                            System.out.println(numApp + " " + nombre_corto + " " +alta.toString() + " " + 
                                                                               registro.toString() + " " + imagReg + " " + imagRes + " " + preg + " " + 
                                                                               " " + pregmc + " " + pres + " " + presmc + " " + 
                                                                               ruta + " " + estado + " " + 
                                                                               observacion);
                                                            
                                                     
                                                            Object[] datos = new Object[]{ consecutivo,numApp,nombre_corto,alta.toString(),registro.toString(),
                                                                                           imagReg,imagRes,preg,pregmc,pres,presmc,ruta,estado,observacion };
                             
                                                           datosReporte.add(datos);                             
                                                           dtm.addRow(datos); 
                                                           consecutivo++;
                                               
                                                     }
                                              
                                               }
                                        
                                        }
                                                                                                                                                                                                                                                                                                                                                                                                                
                                    }catch(SQLException | NumberFormatException | HeadlessException e){ e.printStackTrace(); }
                                    finally{
                                            try{
                                                rsMysql.close();                                                        
                                                s.close();
                                                c.close();
                                            }catch(Exception e){ e.printStackTrace(); }                                  
                                    }
                                  
                             }                                                                                                                    
                             
                             @Override
                             public void done(){
                                  
                                    GenerarReportes.this.botonGenerarReporte.setEnabled(true);
                                    GenerarReportes.this.revalidate();
                                    GenerarReportes.this.repaint();                                                            
                                                                 
                             }
                		                          		                          		                          		                          		  
                	  };
                	  
                	  sw.execute();                	                      

              
                      }
              });
              
              gbc = new GridBagConstraints();
              gbc.gridx = 1;
              gbc.gridy = 5;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;
              add(botonGenerarvistaPreviaReporte,gbc);   
                           
              botonGenerarReporte = new JButton("Generar reporte");
              botonGenerarReporte.setEnabled(false);
              botonGenerarReporte.addActionListener(new ActionListener() {

                      @Override
                      public void actionPerformed(ActionEvent e) {
                           
                              try{ GeneraReportePdf(); }
                              catch(Exception e1){ e1.printStackTrace(); }
              
                      }
              });
              
              gbc = new GridBagConstraints();
              gbc.gridx = 3;
              gbc.gridy = 5;    
              gbc.weightx = 0.1;
              gbc.weighty = 0.1;
              gbc.insets = new Insets(5,5,5,5);
              gbc.anchor  = GridBagConstraints.WEST;              
              add(botonGenerarReporte,gbc);   
                                        
       }                                       
       
       public void GeneraReportePdf(){    	         	    
    	      
    	      try{
    	    	  
    	    	  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    	    	  SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MMHH:mm");
    	  		  String fCadena = sdf.format(new Date());
    	  		  String f1Cadena = sdf1.format(new Date());
    	  		  
    	    	  Document pdf = new Document();
  		          pdf.setPageSize(PageSize.A4.rotate());
  		          PdfWriter.getInstance(pdf,new FileOutputStream("C:\\Users\\Daniel.Meza\\Desktop\\Reporte.pdf"));
  		             	
  		          Chunk espacio1 = new Chunk(new VerticalPositionMark(),260,false);  		            		          
  		          Chunk espacio2 = new Chunk(new VerticalPositionMark(),717,true);
  		          Chunk espacio3 = new Chunk(new VerticalPositionMark(),225,true);		          
  		          
  		          Phrase fraseEncabezado = new Phrase();
  		          fraseEncabezado.add(new Chunk(espacio1));  		          
  		          fraseEncabezado.add(new Chunk("Direccion de procesos ópticos y calificación",new Font(Font.TIMES_ROMAN,12f,Font.BOLD)));
  		          fraseEncabezado.add(new Chunk(espacio2));  		         
  		          fraseEncabezado.add(new Chunk(fCadena,new Font(Font.TIMES_ROMAN,6f,Font.NORMAL)));
  		          fraseEncabezado.add(new Chunk(espacio3));  		          
		          fraseEncabezado.add(new Chunk("Validación de imagenes de lectura óptica " + short_name + " " + meses[month - 1] + " " + year,
		        		                         new Font(Font.TIMES_ROMAN,10f,Font.BOLD)));
		          
  		          
  		          //fraseEncabezado.add(new Chunk("\n Validacion de imagenes de lectura optica"));
  		          
//  		        new Phrase("Direccion de procesos opticos y calificacion.Validacion de imagenes de "+
//                          "lectura optica " + short_name + " " + meses[month - 1] + " " + year + " " + fCadena  		          
  		          HeaderFooter encabezado = new HeaderFooter(fraseEncabezado,false);  		            		          
                  
  		          HeaderFooter pie = new HeaderFooter(new Phrase("",new Font(Font.TIMES_ROMAN,8f,Font.BOLD)),true);
  		           
  		          pdf.setHeader(encabezado);
  		          pdf.setFooter(pie);
  		           
  		          pdf.open();  		          
  		            		            		          
  		          Font fuenteDatos = new Font(Font.TIMES_ROMAN,6f);
		          fuenteDatos.setStyle(Font.NORMAL);  		          
		          
		          float[] anchosCelda = {0.02f,0.04f,0.05f,0.04f,0.04f,0.04f,0.04f,0.04f,0.04f,0.04f,0.04f,0.1f,0.05f,0.1f};
		          PdfPTable tablaPdf;		          
                  tablaPdf = new PdfPTable(anchosCelda);
		          tablaPdf.setWidthPercentage(100);
		          tablaPdf.setTotalWidth((PageSize.A4.getWidth() - pdf.leftMargin() - pdf.rightMargin()) * tablaPdf.getWidthPercentage() / 100);
		          
		          String[] encabezados = {"No.","Aplicacion","Examen","Fecha aplicacion","Fecha alta","Imag Reg","Imag res","Preg",
                                          "Preg Mcontrol","Pres","Pres Mcontrol","Ruta","Estado","Observacion"};
    
                  Font fuenteEncabezados = new Font(Font.TIMES_ROMAN,8f);                  
                  fuenteEncabezados.setStyle(Font.BOLD);
                  
                  for( int i = 0; i <= (encabezados.length - 1); i++ ){
                       Phrase fraseEncabezados = new Phrase();
                       fraseEncabezados.setFont(fuenteEncabezados);
          	           fraseEncabezados.add(encabezados[i]);
          	           PdfPCell celda = new PdfPCell(fraseEncabezados);
                       celda.setFixedHeight(20);  	    	      	            	    	
          	           tablaPdf.addCell(celda);                	   
                  }
		          
                  for( Object[] ao: datosReporte ){  		        	     		        	   
      	               for( Object dato: ao ){      	            	    
      	            	    Phrase frase = new Phrase();
      	            	    frase.setFont(fuenteDatos);
      	            	    if( dato instanceof String ){      	    
      	            	    	frase.add(dato);
      	            	    	PdfPCell celda = new PdfPCell(frase);
      	                        celda.setFixedHeight(20);  	    	      	            	    	
      	            	    	tablaPdf.addCell(celda);
      	            	    }else{ 	      	  
      	            	    	  frase.add(String.valueOf(dato));
      	            	     	  PdfPCell celda = new PdfPCell(frase);
      	            	     	  celda.setFixedHeight(20);        	            	    
        	          	          tablaPdf.addCell(celda);
        	                }
      	            	    
      	               }      	                     	               
      	               
                  } 
                  
                  float alturaTabla = tablaPdf.calculateHeights(true);
                  float alturaEncabezadoTabla = tablaPdf.getHeaderHeight();                  
                                                     
                  Rectangle dimensionPagina = pdf.getPageSize();
                  float alturaPagina = dimensionPagina.getHeight();
                  float alturaEncabezado = encabezado.getHeight();
                  float alturaPie = pie.getHeight();
                  float noPaginasFlotante = (alturaTabla + alturaEncabezado + alturaPie)/alturaPagina;
                  float reales = ((((alturaEncabezado + alturaPie) * noPaginasFlotante) + alturaTabla)/alturaPagina) + 1;                  
                  float resto = reales % 1;
                  
                  if( resto > 0.0 ){
                	  reales += 1;
                  }
                  
                  int noPaginas = Math.round(reales); 
                  
                  System.out.println("Altura pagina " + alturaPagina + " altura Tabla " + alturaTabla + " altura encabezado " + 
                                     alturaEncabezado + " altura pie " + alturaPie);
                  System.out.println("El numero de paginas es " + noPaginas);
                  
  		          pdf.add(tablaPdf);  		            		         
  		            		          
  		          Paragraph operadorSupervisor = new Paragraph();
  		          operadorSupervisor.add(new Chunk("\n Operador"));
  		          operadorSupervisor.setAlignment(Paragraph.ALIGN_JUSTIFIED_ALL);
  		            		          
  		          //pdf.add(operadorSupervisor);
  		          
  		          pdf.close();  		            	   	        
       		          
  		          String workingDir = System.getProperty("user.dir");
  		          System.out.println(workingDir);
			      RandomAccessFileOrArray doc = new RandomAccessFileOrArray("C:\\Users\\Daniel.Meza\\Desktop\\Reporte.pdf",false,true);
			      PdfReader reader = new PdfReader(doc,null);
			      int paginas = reader.getNumberOfPages();
			      PdfContentByte contenido = null;
			      			      
			      AcroFields campos = reader.getAcroFields();
			      
			      Set<String> keys = campos.getFields().keySet();
			      			      
			      for(String key : keys){
			    	  System.out.println("Se llama " + key);
			      }
			      
			      PdfStamper stamper = new PdfStamper(reader,new FileOutputStream("C:\\Users\\Daniel.Meza\\Desktop\\Reporte1.pdf"));
			      contenido = stamper.getOverContent(paginas);
			      			      
			      contenido.beginText();
			      
			      BaseFont bf_times = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1252", false);
			      contenido.setFontAndSize(bf_times,8);
			      contenido.showText("Prueba de rescritura");
			      contenido.endText();
			      System.out.println("El numero de paginas real es " + paginas);
			      
			      stamper.close();
			      
			      reader.close();
				
  		           
    	      }catch(DocumentException | IOException e){ e.printStackTrace(); }    	    	      	             	    	  	       	    	       	              	    	 
    	    
       }              	  
                 
}

class JComponentTableCellRenderer implements TableCellRenderer {
    
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, 
             boolean isSelected, boolean hasFocus, int row, int column) {
             return (JComponent)value;
      }
      
}
