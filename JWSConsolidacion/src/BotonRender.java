
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

//@author Daniel.Meza
 
public class BotonRender extends JButton implements TableCellRenderer{
    
    
	   private static final long serialVersionUID = 1L;

	   public BotonRender(){ setOpaque(true); }

       @Override
       public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
              if(isSelected){
                  setForeground(table.getSelectionForeground());
                  setBackground(table.getSelectionBackground());
              }else {
                      setForeground(table.getForeground());
                      setBackground(UIManager.getColor("Button.background"));
              }
              
              setText((value == null) ? "Agregar" : "Ver");
           
              return this;
 
       }
    
}
