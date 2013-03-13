package imgLib2Tools;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.FloatType;

public class Average 
{
	public static Img<FloatType> averageOverDimension( final Img<FloatType> img, final int dimensionToAverageOver, final boolean verbose )
	{
		if( verbose )
			System.out.println( "Averaging image.");
		
		long[] newDim = new long[ img.numDimensions() - 1 ];
		int newIndex = 0;
		long stackCount = 0; 
		for( int i = 0; i < img.numDimensions(); i ++ )
		{
			if( i != ( dimensionToAverageOver - 1 ) )
			{
				newDim[ newIndex ] = img.dimension( i ); 
				newIndex++; 
			}
			else
			{
				stackCount = img.dimension( i ); 
			}
		}
		
		
		
		ArrayImgFactory<FloatType> imgFac = new ArrayImgFactory<FloatType>(); 	
		Img<FloatType> imgOutput = imgFac.create( newDim, new FloatType() );
		Cursor<FloatType> outputCursor = imgOutput.cursor();
		RandomAccess<FloatType> inputRandomAccess = img.randomAccess(); 
		
		long[] currentPos = new long[ newDim.length + 1 ]; 
		while( outputCursor.hasNext() )
		{
			outputCursor.next(); 
			
			//Get output cursor position 
			for( int i = 0; i < newDim.length; i++ )
			{
				currentPos[ i ] = outputCursor.getLongPosition( i ); 
			}
			
			//Loop through all the values in the dimension of interest. 
			for( long j = 0; j < stackCount; j++ )
			{
				currentPos[ newDim.length ] = j; 
				inputRandomAccess.setPosition( currentPos ); 
				
				outputCursor.get().add( inputRandomAccess.get() );  
			}
		}
		
		outputCursor.reset(); 
		while( outputCursor.hasNext() )
		{
			outputCursor.next(); 
			outputCursor.get().mul( 1.0/ ( (float) stackCount) ); 
		}
		
		if( verbose )
			System.out.println( "Done.\n-----");
		
		return imgOutput; 
	}
}
