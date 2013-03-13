package imgLib2Tools;

import java.util.ArrayList;
import java.util.Collections;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public class Histogram 
{
	/**
	 * Returns the pixel values sorted into ArrayLists per bins 
	 * @param img
	 * @param nrOfBins
	 * @return
	 */
	public static ArrayList<ArrayList<Float>> getHistogram( final Img<FloatType> img, final long nrOfBins )
	{
		ArrayList<ArrayList<Float>> output = new ArrayList<ArrayList<Float>>(); 
		
		//Get all pixel values in list
		ArrayList<Float> pixels = new ArrayList<Float>(); 
		Cursor<FloatType> c = img.cursor(); 
		while( c.hasNext() )
		{
			c.next(); 
			pixels.add( c.get().get() ); 
		}
		
		pixels.add( 1.4f ); 
		pixels.add( 1.6f );
		
		Collections.sort( pixels );
		long pixelsPerBins = pixels.size()/nrOfBins;
		if( pixels.size()%nrOfBins != 0 )
			pixelsPerBins++;
		
		ArrayList<Float> currentBin = new ArrayList<Float>();
		int currentCount = 0; 
		for( Float pixel : pixels )
		{
			if( currentCount > pixelsPerBins ) 
			{
				output.add( currentBin ); 
				currentBin = new ArrayList<Float>();
				currentCount = 0; 
			}
			currentBin.add( pixel );
			currentCount++; 
		}
		output.add( currentBin );
				
				
		return output; 
	}
}
