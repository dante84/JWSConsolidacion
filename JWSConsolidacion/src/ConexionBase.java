
import java.sql.Connection;
import java.sql.DriverManager;

// @author daniel

public class ConexionBase {
    
        private Connection c;        
   
        public Connection getC(String host,String base,String usuario,String pass) {
            
               try{
                   
                   Class.forName("com.mysql.jdbc.Driver");                                     
                   c = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + base,usuario,pass);                                     
                   
               }catch(Exception e){ e.printStackTrace(); }                                          
               
               return c;
               
        }
        
        
        
    
}
