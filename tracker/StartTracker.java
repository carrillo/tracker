package tracker;

import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.process.ImageProcessor;
import inputOutput.OpenImages;

import java.awt.Color;
import java.io.File;

import mpicbg.imglib.algorithm.peak.GaussianPeakFitterND;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.imglib.util.DevUtil;
import mpicbg.imglib.util.Util;

public class StartTracker 
{
	private ImagePlus imp; 
	
	public StartTracker( final File file )
	{
		setImp( file ); 
		findParticles( file );
		
		// add listener to the imageplus slice slider
		SliceObserver sliceObserver = new SliceObserver( imp, new ImagePlusListener() );

	}
	
	/**
	 * This method finds the starting point for the tracking. 
	 * @param image
	 */
	public void findParticles( final File file )
	{
		//ImagePlus imp = OpenImages.getFloatTypeImp( file ); 
		getImp().show(); 
		
		//for( int i = 1; i <= getImp().getStackSize(); i++ )
		
		ImageProcessor ip = getImp().getImageStack().getProcessor( 1 );
		float[] pixels = (float[]) ip.getPixels();
		
		//for imglib2
		//Img<FloatType> imglib2 = ArrayImgs.floats( pixels, imp.getWidth(), imp.getHeight() );
		//ImageJFunctions.show( imglib2 ); 
		
		// for imglib1
		Image<FloatType> imglib1 =  DevUtil.createImageFromArray( pixels, new int[]{ imp.getWidth(), imp.getHeight() } );
		//ImageJFunctions.show( imglib1 ); 
		//GaussianPeakFitterND<FloatType> fit = new GaussianPeakFitterND<FloatType>( imglib1 );
		//fit.process(point, typical_sigma)
		
		// extract peaks to show
		
	}
	
	protected class ImagePlusListener implements SliceListener
	{
		@Override
		public void sliceChanged(ImagePlus arg0)
		{
			int slice = getImp().getCurrentSlice();
			//System.out.println( getImp().getCurrentSlice() );
			
			Overlay o = imp.getOverlay();
			
			if ( o == null )
			{
				o = new Overlay();
				imp.setOverlay( o );
			}
			
			o.clear();
			
			final OvalRoi or = new OvalRoi( 0+slice/100, 10, 20, 30 );

			or.setStrokeColor( Color.green );
			or.setStrokeWidth( 3 );
			//or.setStrokeColor( Color.red );
			
			o.add( or );
		}		
	}

	private void setImp( final File file ) { this.imp = OpenImages.getFloatTypeImp( file );  }
	public ImagePlus getImp() { return this.imp; } 

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		new ImageJ(); 
		final File image = new File( "sampleEasy.tif" );
		new StartTracker( image );

	}

}
