package utilities;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BaseClass {

	@BeforeSuite
	public void setup() {
	    System.out.println("🚀 BaseClass setup is running...");
	    RestAssured.baseURI = getPropertyValue("baseURI");
	    System.out.println("✅ Base URI Set: " + RestAssured.baseURI);
	}

    public static String getPropertyValue(String key) {
        Properties prop = new Properties();
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/config/config.properties");
            prop.load(fis);
            return prop.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties file", e);
        }
    }
}

