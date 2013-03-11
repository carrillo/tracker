package imgLib2Tools;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss.Gauss;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class Extrema 
{
	public static Img<BitType> findMaxima( final Img<FloatType> img )
	{
		// Create blurred inputImage
        Img<FloatType> imgBlurred = Gauss.inDouble( new double[]{ 1, 1 }, img );
		 
        // Create a new image for the output
     	ArrayImgFactory<BitType> bitTypeFactory = new ArrayImgFactory<BitType>(); 
     	Img<BitType> output = bitTypeFactory.create( imgBlurred, new BitType() ); 
		  
		 
		// define an interval that is one pixel smaller on each side in each dimension,
        // so that the search in the 8-neighborhood (3x3x3...x3) never goes outside
        // of the defined interval
	    Interval interval = Intervals.expand( imgBlurred, -1 );
	    
	    // create a view on the source with this interval
        RandomAccessibleInterval<FloatType> source = Views.interval( imgBlurred, interval );
        
        // create a Cursor that iterates over the source and checks in a 8-neighborhood
        // if it is a minima
        final Cursor< FloatType > center = Views.iterable( source ).cursor();
        
        // instantiate a RectangleShape to access rectangular local neighborhoods
        // of radius 1 (that is 3x3x...x3 neighborhoods), skipping the center pixel
        // (this corresponds to an 8-neighborhood in 2d or 26-neighborhood in 3d, ...)
        final RectangleShape shape = new RectangleShape( 1, true ); 
        
        // iterate over the set of neighborhoods in the image
        for ( final Neighborhood< FloatType > localNeighborhood : shape.neighborhoods( source ) )
        {
            // what is the value that we investigate?
            // (the center cursor runs over the image in the same iteration order as neighborhood)
            final FloatType centerValue = center.next();
            System.out.println( "CenterValue: " + centerValue ); 
 
            // keep this boolean true as long as no other value in the local neighborhood
            // is larger or equal
            boolean isMaximum = true;
 
            // check if all pixels in the local neighborhood that are smaller
            for ( final FloatType value : localNeighborhood )
            {
                // test if the center is smaller than the current pixel value
                if ( centerValue.compareTo( value ) <= 0 )
                {
                    isMaximum = false;
                    break;
                }
            }
 
            if ( isMaximum )
            {
            	System.out.println( "Found maximum: " + center.get().get() ); 
                // draw a sphere of radius one in the new image
                HyperSphere< BitType > hyperSphere = new HyperSphere< BitType >( output, center, 1 );
 
                // set every value inside the sphere to 1
                for ( BitType value : hyperSphere )
                    value.setOne();
            }
        }
 
        return output;
	}
	
	
}
