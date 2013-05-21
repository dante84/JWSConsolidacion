
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;

//@author Daniel.Meza

class ButtonEditor extends DefaultCellEditor {
      
	  private static final long serialVersionUID = 1L;
	  protected JButton button;
      private String label;
      private boolean isPushed;
      JFrame mainPanel;
      private Object aplicacion;     
      private ConexionBase conexionBase;
      private final String remoto = "172.16.50.14";
      
      public ButtonEditor(JCheckBox checkBox,JFrame applet) {
           
             super(checkBox);
             
             conexionBase = new ConexionBase();
             
             mainPanel = applet;
             button = new JButton();
             button.setOpaque(true);
             button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                           fireEditingStopped();
                    }
             });
             
      }

      @Override
      public Component getTableCellEditorComponent(JTable table, Object value,boolean isSelected, int row, int column) {
    
             if (isSelected) {
                 button.setForeground(table.getSelectionForeground());
                 button.setBackground(table.getSelectionBackground());
             } else {
                     button.setForeground(table.getForeground());
                     button.setBackground(table.getBackground());
             }
             
             label = (value == null) ? "" : value.toString();
             button.setText(label);
             isPushed = true;             
             
             int fila = table.getSelectedRow();
             aplicacion = table.getValueAt(fila,1);
             
             return button;
             
      }

      @Override
      public Object getCellEditorValue() {
          
             final JDialog f = new JDialog(mainPanel);                  
             
             if (isPushed) {
                                  
                 ResultSet rs = null;
                 Statement s  = null;
                 String observacion = "";
                                                                                      
                 try{
                                          
                     //Connection c = DriverManager.getConnection("jdbc:mysql://172.16.34.21:3306/ceneval","user","slipknot");                                                            
                     Connection c = conexionBase.getC(remoto,"ceneval","test","slipknot");
                     
                     s = c.createStatement();
                                                
                     System.out.println("antes del select");
                     rs = s.executeQuery("select observacion from viimagenes where no_aplicacion = " + aplicacion);
                     
                     if( rs.isBeforeFirst() ){
                         
                         while( rs.next() ){
                                observacion = rs.getString(1);
                         }
                                                                             
                     }
                     
                     
                 }catch(Exception e){ e.printStackTrace(); }
                                                           
                 System.out.println("Llego el objeto = " + aplicacion);
                 JPanel panel = new JPanel();
                 panel.setSize(150,100);
                 panel.setLayout(new GridBagLayout());                 
                 final JTextArea texto = new JTextArea(observacion);                 
                 texto.setBorder(new BevelBorder(BevelBorder.LOWERED));
                 texto.setVisible(true);                 
                 JScrollPane scrollTexto = new JScrollPane(texto,
                                                           JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                 scrollTexto.setSize(100,80);                
                 
                 GridBagConstraints gbc = new GridBagConstraints();
                 gbc.gridheight = 2;
                 gbc.insets = new Insets(5,5,5,5);
                 gbc.fill = GridBagConstraints.REMAINDER;
                 panel.add(texto,gbc);
                 
                 JButton botonCancelar = new JButton("Cancelar");
                 botonCancelar.addActionListener(new ActionListener() {

                       @Override
                       public void actionPerformed(ActionEvent e) {
                              f.dispose();
                       }
                 });
                                  
                 JButton botonGuardar = new JButton("Guardar observacion");
                 botonGuardar.addActionListener(new ActionListener() {

                       @Override
                       public void actionPerformed(ActionEvent e) {
                           
                              SwingWorker<?, ?> sw;
                              sw = new SwingWorker<Object, Object>() {

                                  @Override
                                  protected Void doInBackground() throws Exception {
                                      
                                            String text = texto.getText();
                                            ResultSet rs = null;
                                            Statement s  = null;
                                            //Connection c = DriverManager.getConnection("jdbc:mysql://172.16.34.21:3306/ceneval","user","slipknot");
                                            Connection c = conexionBase.getC(remoto,"ceneval","test","slipknot");
                                            
                                            try{
                                                
                                                System.out.println("En el try");
                                                Class.forName("com.mysql.jdbc.Driver");                   
                                                
                                                s = c.createStatement();
                                                 
                                                System.out.println("antes del select");
                                                rs = s.executeQuery("select no_aplicacion from viimagenes where no_aplicacion = " + aplicacion);
                                                 
                                                System.out.println("antes de ver si tiene datos");
                                                if( !rs.isBeforeFirst() ){
                                                      
                                                    String insert = "insert into viimagenes (no_aplicacion,observacion) values('" + aplicacion + "','" + text + 
                                                                      "'";
                                                    System.out.println(insert);
                                                    s.execute("insert into viimagenes (no_aplicacion,observacion) values('" + aplicacion + "','" + text  + "')");                                                      
                                                    
                                                }else{
                                                       s  = c.createStatement();                                                               
                                                       while( rs.next() ){
                                                              String update = "update viimagenes set observacion = '"+ text + "' where no_aplicacion = '" + 
                                                                              rs.getString(1) + "'";
                                                              System.out.println(update);
                                                              s.execute(update);                                                           
                                                       }
                                                                                                           
                                                }
                                                                                                
                                                
                                            }catch(Exception e){ e.printStackTrace(); }
                                            
                                            finally{
                                                    try{
                                                        rs.close();
                                                        s.close();
                                                        c.close();                                                      
                                                    }catch(SQLException sqle){ sqle.printStackTrace(); }
                                            }
                                            
                                            return null;
                                            
                                  }
                                  
                                  @Override
                                  public void done(){
                                      
                                          JOptionPane.showMessageDialog(
                                                            null,
                                                            "Observacion guardada",
                                                            "Exito",
                                                            JOptionPane.INFORMATION_MESSAGE);
                                         f.dispose();
                                          
                                  }
                                  
                              };   
                              
                              sw.execute();
                           
                       }
                       
                 });
                 
                 gbc = new GridBagConstraints();                 
                 gbc.gridy = 2;                 
                 gbc.insets = new Insets(5,5,5,5);                 
                 panel.add(botonGuardar,gbc);                                  
                 
                 f.setTitle("Observacion");      
                 f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                 f.setContentPane(panel);
                 f.setModal(false);
                 f.setLocationByPlatform(true);
                 f.setSize(400,350);
                 f.setVisible(true);                                                                                                      
                                                                    
             }
             
             isPushed = false;
             
             return label;
             
      }

      @Override
      public boolean stopCellEditing() {
          
             isPushed = false;
             
             return super.stopCellEditing();
             
      }

      @Override
      protected void fireEditingStopped() {
                super.fireEditingStopped();
      }
      
}
