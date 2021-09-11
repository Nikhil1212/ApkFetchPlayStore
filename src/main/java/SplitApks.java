
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class SplitApks {
	
	public static String pathToadb="/home/nikhil/Downloads/platform-tools_r30.0.5-linux/platform-tools/adb";


	public static String main(String packageName) throws Exception {
		/**
		 * Considering the app has been installed. The package Name should be the arguement. Modify it later
		 */
		String directoryPath="";
		String uiDump_adb_Sideload_Path="";
		try {
			//String packageName="com.freecharge.android";
			String command1=pathToadb+" shell pm path "+packageName;
			Process process=commandExecution(command1);
			BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String apkPath=bufferedReader.readLine();
			if(apkPath==null|| apkPath.length()==0)
			{
				//InstallerVerificationFrontEnd.updateDatabaseByPassable(packageName, 'E', "App is not currently installed on the device");
				throw new Exception("App did not install");	
			}
			int count=0;
			/**
			 * Command to create a directory that contains the app 
			 */
			directoryPath="/home/nikhil/Documents/apps/InstallerVerification/"+packageName;
			commandExecution("mkdir "+directoryPath);
			String apksPath="";
			while(apkPath!=null)
			{
				System.out.println(apkPath);
				count++;
				apksPath=apksPath+parseToFetchApk(apkPath,directoryPath)+" ";
				apkPath=bufferedReader.readLine();
			}
			commandExecution(pathToadb+" uninstall "+packageName);
			if(count==1)
			{
				/**
				 * Install command
				 */
				String installCommand=pathToadb+" install -g "+apksPath;
				System.out.println(installCommand);
				commandExecution(installCommand);
			}
			else
			{
				/**
				 * App has split-apks
				 */
				String installCommand=pathToadb+" install-multiple -g "+apksPath;
				System.out.println(installCommand);
				commandExecution(installCommand);
			}
			/**
			 * Launch the app and capture the screen
			 */

			
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
		finally {
			commandExecution("rm -r "+directoryPath);
		}
		return uiDump_adb_Sideload_Path;
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

	public static String parseToFetchApk(String apkPath, String directoryPath) throws IOException, InterruptedException {

		String pattern="package:";
		String pathToApk=apkPath.substring(pattern.length());
		String apkName=apkPath.substring(apkPath.lastIndexOf('/')+1);
		String command=pathToadb+" pull "+pathToApk+" "+directoryPath+"/"+apkName;
		System.out.println(command);
		commandExecution(command);
		String apkLocalPath=directoryPath+"/"+apkName;
		return apkLocalPath;
	}

}
