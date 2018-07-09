package testautomation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import com.csvreader.CsvWriter;

public class ReadLeagueTable {

	public static ExtentReports report;
	public static ExtentTest logger;
	Properties properties;
	long startTime = 12345678910L;
	long endTime = 12345678910L;
	long execTime = 12345678910L;
	File file = null;
	WebDriver driver = null;

	
	@BeforeSuite
	public void beforeSuite() throws IOException
	{
		File prop = new File("test.properties");
		FileInputStream fileInput = new FileInputStream(prop);
		properties = new Properties();
		properties.load(fileInput);		
		
		report=new ExtentReports(properties.getProperty("ExtentReportPath"), true);
		report.loadConfig(new File(properties.getProperty("ExtentConfigPath")));
		logger=report.startTest("Verify Premier League table");
	}
	@BeforeTest
	public void setup() throws Exception {
		
		//Record Start time of execution
		Date date = new Date();
		startTime = date.getTime();
		logger.log(LogStatus.INFO, "Start time of execution:   " + startTime );

		System.out.println("Start Time in milliseconds: " + startTime);
		System.setProperty("webdriver.chrome.driver",properties.getProperty("chromedriverPath"));
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		logger.log(LogStatus.INFO, "Browser started");

		
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		driver.get("https://www.premierleague.com/tables");
		logger.log(LogStatus.INFO, "Premier League Website launched");

	}

	@AfterTest
	public void tearDown() throws Exception {
		driver.quit();

		//Record End time of execution
		Date date = new Date();
		endTime = date.getTime();
		execTime = endTime - startTime;
		logger.log(LogStatus.INFO, "End time of execution:   " + execTime );
		System.out.println("Total execution time in milliseconds is:  " + execTime);
		
		logger.log(LogStatus.INFO,"Total time in hh:mm:ss format:   " + String.format("%02d:%02d:%02d", 
				TimeUnit.MILLISECONDS.toHours(execTime),
				TimeUnit.MILLISECONDS.toMinutes(execTime) -  
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(execTime)), // The change is in this line
				TimeUnit.MILLISECONDS.toSeconds(execTime) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(execTime))));   

		//Rename the file with Total Exeuction time
		file.renameTo(new File("Output CSV File\\" + execTime + ".csv"));
		logger.log(LogStatus.INFO, "Renamed file with Total Execution time");

		report.endTest(logger);
		report.flush();
		
	}

	@Test
	public void print_data() throws IOException {

		int Col_count = 0;
		
		// Get number of Rows In League table.
		int Row_count = driver.findElements(By.xpath("(//table//tbody)[1]/tr")).size();
		System.out.println("Number Of Rows = " + Row_count);

		// Get number of Columns In League table.
		Col_count = driver.findElements(By.xpath("(//table//tbody)[1]/tr[1]/td")).size();
		System.out.println("Number Of Columns = " + Col_count);

		//Create a csv file
		file = new File("Output CSV File\\"+"PremierLeagueData.csv");
		if (!file.exists()) {
			file.createNewFile();
		}
		logger.log(LogStatus.INFO, "Created CSV File");


		FileWriter fw = new FileWriter(file, true);
		CsvWriter writer = new CsvWriter(fw, ',');

		//Find all the header elements of League Table
		List<WebElement> irow2 = driver.findElements(By.xpath("(//table//thead)[1]/tr/th"));

		for (int k = 0; k < irow2.size(); k++) {
			
			//Add all Header text values to a List
			List<String> HeaderList = new ArrayList<String>();
			String headerText = irow2.get(k).getText().toString();
			HeaderList.add(headerText);

			//Collect all String objects and concatenate them, using the separator "comma"
			String collect = HeaderList.stream().collect(Collectors.joining(","));

			//Write all the header values of League Table to CSV File
			writer.write(collect);


		}

		logger.log(LogStatus.INFO, "Written Table Header values to CSV File");
		//Marking end of line in csv file after writing All Header values
		writer.endRecord();

		//Divided xpath In three parts to pass Row and Column count values.
		String first_part = "(//table//tbody)[1]/tr[";
		String second_part = "]/td[";
		String third_part = "]";

		//Used for loop for number of rows.
		for (int i = 1; i <= Row_count; i += 2) {
			
			//Used for loop for number of columns.
			for (int j = 1; j <= Col_count; j++) {
			
				// Prepared final xpath of specific cell as per values of i and j.
				String final_xpath = first_part + i + second_part + j + third_part;
				
				//Retrieve value from located cell of League Table
				String Table_data = driver.findElement(By.xpath(final_xpath)).getText();

				//Add all Table content values to a List
				List<String> TableDataList = new ArrayList<String>();
				TableDataList.add(Table_data);

				//Collect all String objects and concatenate them, using the separator "comma"
				String collect1 = TableDataList.stream().collect(Collectors.joining(","));

				//Write all the League Table contents to CSV File
				writer.write(collect1);

			}

			//Marking end of line in csv file after writing Table content values for a particular row
			writer.endRecord();

			
		}
		logger.log(LogStatus.INFO, "Written Table content values to CSV File");
		writer.close();

	}

}
