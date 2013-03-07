package inputOutput;

import ij.ImagePlus;
import ij.io.Opener;

import java.io.File;

import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.io.ImgOpener;
import net.imglib2.type.numeric.real.FloatType;

public class OpenImages 
{	
	/**
	 * Create FloatType Image from file. 
	 * @param file
	 * @param imgFactory
	 * @return
	 */
	public static Img<FloatType> getFloatTypeImg( final File file, ImgFactory<FloatType> imgFactory )
	{
		try 
		{
			System.out.println( "Creating image" ); 
			Img< FloatType > img = new ImgOpener().openImg( file.getAbsolutePath(), imgFactory );
			System.out.println( "Done" ); 
			return img; 
		}
		catch (Exception e) 
		{
			System.err.println( "Couldn't open image: " + file.getAbsolutePath() + " " + e );
			return null; 
		}
	}
	
	public static ImagePlus getFloatTypeImp( final File file )
	{
		return new Opener().openImage( file.getAbsolutePath() ); 
	}
	
	/**
	 * Create FloatType Image from file using ArrayImageFactory. 
	 * @param file
	 * @return
	 */
	public static Img<FloatType> getFloatTypeImg( final File file )
	{
		return getFloatTypeImg( file, new ArrayImgFactory<FloatType>() ); 
	}
}
