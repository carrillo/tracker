package imgLib2Tools;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.real.FloatType;


public class Substack 
{
	public static Img<FloatType> getSubstack( final Img<FloatType> img, final int dimension, final long minStack, final long maxStack )
	{
		final long[] newDim = new long[ img.numDimensions() ];
		for( int i = 0; i < newDim.length; i++ )
		{ 
			newDim[ i ] = img.dimension( i ); 
		}
		newDim[ dimension ] = (maxStack - minStack) + 1; 
		
		
		final ImgFactory<FloatType> factory = img.factory(); 
		final Img<FloatType> outputImg = factory.create( newDim, new FloatType() ); 
		
		final Cursor<FloatType> outputCursor = outputImg.cursor();
		final RandomAccess<FloatType> inputRandomAccess = img.randomAccess();
		
		long[] currentPos = new long[ img.numDimensions() ];
		while( outputCursor.hasNext() )
		{
			outputCursor.next(); 
			//Get output cursor position 
			for( int i = 0; i < newDim.length; i++ )
				currentPos[ i ] = outputCursor.getLongPosition( i ); 
			//Set offset
			currentPos[ dimension ] = currentPos[ dimension ] - minStack; 
			
			
			inputRandomAccess.setPosition( currentPos ); 
			outputCursor.get().set( inputRandomAccess.get().get() ); 
		}
		
		return outputImg; 
	}
}
