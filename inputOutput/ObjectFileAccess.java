package inputOutput;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectFileAccess 
{
	public static void saveObjectToFile( final Object o , final String fileName )
	{
		try 
		{
			FileOutputStream out = new FileOutputStream( fileName );
			ObjectOutputStream save = new ObjectOutputStream( out ); 
			save.writeObject( o ); 
			save.close(); 
			out.close(); 
		} catch (IOException e) 
		{
			System.err.println( "Couldn't save object to file: " + fileName + " " + e ); 
			e.printStackTrace();
		} 
	}
	
	public static Object openObjectFromFile( final String fileName )
	{
		try 
		{
			FileInputStream fileIn = new FileInputStream( fileName );
			ObjectInputStream objIn = new ObjectInputStream( fileIn ); 
			Object o = objIn.read();
			objIn.close(); 
			fileIn.close(); 
			return o; 
		} 
		catch (IOException e) 
		{
			System.err.println( "Couldn't open object from file: " + fileName + " " + e );
			e.printStackTrace();
			return null; 
		} 
	}
}
