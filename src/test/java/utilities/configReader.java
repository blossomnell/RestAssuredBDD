package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
public class configReader {
 
	  static Properties prop;

	    public Properties init_prop() {
	        prop = new Properties();
	        try {
	            FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/config/config.properties");
	            prop.load(fis);
	        } catch (IOException e) {
	            e.printStackTrace();
	            throw new RuntimeException("Could not load config.properties file.");
	        }
	        return prop;
	    }
}
