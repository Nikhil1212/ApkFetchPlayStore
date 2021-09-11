
/**
 * This class finds out whehther an app has installer verification present or not.
 * Install all the apps and the
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.Scanner;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;


public class InstallingApps {
	public static 	AppiumDriver<MobileElement> driver;
	public static 	DesiredCapabilities cap;
	public static	String xpathInstallButton="//android.widget.Button[@text=\"Install\"]";
	public static	String xpathOpenButton="//android.widget.Button[@text=\"Open\"]";
	public static	String xpathTryAgainButton="//android.widget.Button[@text=\"Try again\"]";
	public static	String xpathAcceptButton="//android.widget.Button[@text=\"Accept\"]";

	public static 	long explicitWaitTimeoutInSeconds = 200L;
	public static String pathToadb="/home/nikhil/Downloads/platform-tools_r30.0.5-linux/platform-tools/adb";

	public static String pathToAppium="/opt/node-v14.15.0-linux-x64/bin/appium";


	public static void main(String[] args) throws InterruptedException, IOException, Exception{


		String filePath="/home/nikhil/Documents/apps/newApps.txt";
		fetchApkFromPlayStore(filePath);
	// Now we are done with all the apps installation

	}



	public static void fetchApkFromPlayStore(String filePath) throws InterruptedException, IOException, SQLException {
		/**
		 * 
		 */
		lockUnlockPhone();
		File file=new File(filePath);
		Scanner scanner=new Scanner(file);
		setup();
		driver=new AndroidDriver<MobileElement>(new URL("http://127.0.0.1:4723/wd/hub"),cap);

		Thread.sleep(10000);
		int count=0;
		/**
		 * With the help of appium, we are able to install the app from the play store in an automated way.
		 */
		while(scanner.hasNext())
		{
			String packageName=scanner.next();
			uninstallApp(packageName);
			System.out.println(packageName);
			installAppFromPlayStore(packageName,driver);
			
			/**
			 * Pull the apk and store it on to the device
			 */
			pullTheApk(packageName);

		}
		scanner.close();
		driver.quit();
	}

	public static void lockUnlockPhone() throws IOException, InterruptedException {

		commandExecution(pathToadb+" shell input keyevent 26"); //Power button (locked)
		commandExecution(pathToadb+" shell input keyevent 26"); //Power button (again pressed) to see the screen.
		commandExecution(pathToadb+" shell input text 1995"); //Password
		commandExecution(pathToadb+" shell input keyevent 66");  //Enter Key

	}



	private static void pullTheApk(String packageName) throws SQLException {
		try {
			//String packageName="com.freecharge.android";
			System.out.println(packageName);
			String command1=pathToadb+" shell pm path "+packageName;
			Process process=commandExecution(command1);
			BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String apkPath=bufferedReader.readLine();
			System.out.println(apkPath);
			if(apkPath==null|| apkPath.length()==0)
			{
				throw new Exception("App did not install");	
			}
			int count=0;
			/**
			 * Command to create a directory that contains the app 
			 */
			String baseApkPath=apkPath.substring(8);
			String directoryPath="/home/nikhil/Documents/apps/dataset/"+packageName;
			commandExecution("mkdir "+directoryPath);
			String apksPath="";
			while(apkPath!=null)
			{
				System.out.println(apkPath);
				count++;
				apksPath=apksPath+SplitApks.parseToFetchApk(apkPath,directoryPath)+" ";
				apkPath=bufferedReader.readLine();
			}
		}
		catch (Exception e) {
			/**
			 * From the catch block while Pulling the apk
			 */
			e.printStackTrace();
			
		}
		
	}



	public static void killTheApp(String packageName) throws IOException, InterruptedException {

		String command="adb shell am force-stop "+packageName;
		commandExecution(command);
	}



	private static void installAppFromPlayStore(String packageName, AppiumDriver<MobileElement> driver2) throws SQLException, InterruptedException {
		openAppInstallationPage(driver,packageName);

		/**
		 * Check if we have reached the Installation Page by checking the presence of "Try Again Button"
		 */
		Thread.sleep(2000);
		if(isButtonEnabled(xpathTryAgainButton))
		{
			String remarks="Unable to locate the presence of the app from Play Store App.";
			System.out.println("Unable to locate the presence of the app: "+packageName+" from Play Store App.");
			System.out.println("Continuing with the next app...");
			return;
			//continue;
		}
		else if (!isButtonEnabled(xpathInstallButton))
		{
			String remarks="May be Premium app.";
			System.out.println(packageName+" :  is a paid app");
			return ;
			//continue;
		}
		waitTillButtonEnabled(xpathInstallButton);

		driver.findElementByXPath(xpathInstallButton).click();

		waitTillButtonEnabled(xpathOpenButton);
		//	updateDatabaseByPassable(packageName, 'Y', "Successful");
		System.out.println("App :"+packageName+" has been installed successfully");
		//System.out.println("Number of apps successfully installed is :" + ++count);

	}
	private static void launchPlayStoreApp() throws IOException, InterruptedException {

		String launchableActivityCommand=pathToadb+" shell monkey -p com.android.vending -c android.intent.category.LAUNCHER 1";
		System.out.println(launchableActivityCommand);
		commandExecution(launchableActivityCommand);
	}

	private static boolean appiumStuffs(AppiumDriver<MobileElement> driver, String packageName) throws Exception{
		// TODO Auto-generated method stub
		uninstallApp(packageName);

		openAppInstallationPage(driver,packageName);

		/**
		 * Check if we have reached the Installation Page by checking the presence of "Try Again Button"
		 */
		Thread.sleep(2000);
		if(isButtonEnabled(xpathTryAgainButton))
		{
			String remarks="Unable to locate the presence of the app from Play Store App.";
			System.out.println("Unable to locate the presence of the app: "+packageName+" from Play Store App.");
			System.out.println("Continuing with the next app...");
			return false;

		}
		if (!isButtonEnabled(xpathInstallButton))
		{
			String remarks="May be a Premium app";
		
			System.out.println(packageName+" :  is a paid app");
			return false;


		}
		waitTillButtonEnabled(xpathInstallButton);

		driver.findElementByXPath(xpathInstallButton).click();

		Thread.sleep(2000);
		/**
		 * 
		 * Check if there is any Additional "Accept Button"
		 */
		if(isButtonEnabled(xpathAcceptButton))
		{
			return false;
			//driver.findElementByXPath(xpathAcceptButton).click();			
		}
		waitTillButtonEnabled(xpathOpenButton);

		driver.findElementByXPath(xpathOpenButton).click();	
		//	updateDatabaseByPassable(packageName, 'Y', "Successful");
		System.out.println("App :"+packageName+" has been installed successfully");
		return true;

	}

	

	private static void waitTillButtonEnabled(String xpathButton) {
		// TODO Auto-generated method stub
		while(true)
		{
			System.out.println("Waiting for the App to get installed");
			if(isButtonEnabled(xpathButton))
			{
				break;
			}
		}
	}

	private static boolean isButtonEnabled(String xpathOpenButton) {
		boolean isElementPresent;
		try
		{
			MobileElement mobileElement =  (MobileElement) driver.findElementByXPath(xpathOpenButton);
			WebDriverWait wait = new WebDriverWait(driver,  explicitWaitTimeoutInSeconds);
			wait.until(ExpectedConditions.visibilityOf(mobileElement));
			isElementPresent = mobileElement.isEnabled();
			return isElementPresent;	
		}
		catch(Exception e)
		{
			isElementPresent = false;
			System.out.println(e.getMessage());
			return isElementPresent;
		}
	}

	private static void openAppInstallationPage(AppiumDriver<MobileElement> driver, String packageName) {
		String myURL="https://play.google.com/store/apps/details?id="+packageName;
		driver.get(myURL);

	}

	public static void uninstallApp(String packageName) throws InterruptedException, IOException {
		String commandToUninstallApp=pathToadb+" uninstall "+packageName;
		System.out.println(commandToUninstallApp);

		commandExecution(commandToUninstallApp);
	}

		public static Process commandExecution(String string) throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			System.out.println("command to be executed is :"+string);
			Process pr = Runtime.getRuntime().exec(string);
			
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		//BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				//as in the first line only we can get the package name.That's why immeditate break;
				System.out.println(line);
			}
			buf.close();
			return pr;
		}



	private static void setup() {
		cap=new DesiredCapabilities();
		cap.setCapability("platformName", "Android");
		cap.setCapability("deviceName", "14011JEC202909");
		cap.setCapability("appPackage", "com.android.vending");
		cap.setCapability("appActivity", ".AssetBrowserActivity");
	}
	

}
