package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
	    features = "src/test/resources/features",
	    glue = "stepDefinitions",
	    plugin = {
	        "pretty",
	        "html:target/cucumber-reports.html",
	        "json:target/cucumber.json",  
	        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
	    },
	    monochrome = true,
	    tags = "@tag"
	)


public class CucumberTest extends AbstractTestNGCucumberTests {
}

