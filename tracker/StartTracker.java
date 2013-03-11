package tracker;

import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import imgLib2Tools.Average;
import imgLib2Tools.Substack;
import inputOutput.OpenImages;

import java.io.File;
import java.util.ArrayList;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.gauss.Gauss;
import net.imglib2.algorithm.math.PickImagePeaks;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import roiTools.DrawOverlay;

public class StartTracker 
{
	private ImagePlus imp;
	private ArrayList<Particle> particleList;; 
	//private ArrayList<double[]> fitParameterList; 
	
	public StartTracker( final File file )
	{
		setImp( file ); 
		setParticleList( findParticles( file ) );
		//fit(); 
		
		// add listener to the imageplus slice slider
		//SliceObserver sliceObserver = new SliceObserver( imp, new ImagePlusListener() );
	}
	
	/**
	 * This method finds the starting point for the tracking. 
	 * @param image
	 */
	public ArrayList<Particle> findParticles( final File file )
	{
		ArrayList<Particle> particleList = new ArrayList<Particle>(); 
		//ImagePlus imp = OpenImages.getFloatTypeImp( file ); 
		getImp().show(); 
		 
		Img<FloatType> img = ImagePlusAdapter.wrap( getImp() );  
		
		int nrOfDimension = img.numDimensions(); 
		//Img<FloatType> subStack = Substack.getSubstack(img, ( nrOfDimension -1) , 0, 49 );  
		Img<net.imglib2.type.numeric.real.FloatType> averageImg = Average.averageOverDimension( img, nrOfDimension );
		Gauss.inFloatInPlace(1.0, averageImg); 
		 
		
		PickImagePeaks<FloatType> pip = new PickImagePeaks<FloatType>( averageImg ); 
		pip.process(); 
		ArrayList<long[]> peakPos = pip.getPeakList(); 
		
		
		getImp().setOverlay( new Overlay() ); 
		
		getImp().getOverlay().clear(); 
		
		RandomAccess<FloatType> r = averageImg.randomAccess(); 
		for( long[] pos : peakPos )
		{
			r.setPosition( pos ); 
			if( r.get().get() > 500 )
			{
				Point point = new Point( new int[] { (int) pos[ 0 ], (int) pos[ 1 ] }  ); 
				DrawOverlay.addOval( point, 1, 1 , imp );		
				particleList.add( new Particle( getImp(), pos ) ); 
			}
			
		}
		

		/*
		ImageJFunctions.show( averageImg ); 
		ImageJFunctions.show( Extrema.findMaxima( averageImg ) ); 
		
		
		
		
		
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
		
		*/
		
		 
		return particleList; 
	}
	
	public void fit()
	{
		//int[] initPos = new int[]{ 45, 28 }; 
		//Particle p = new Particle( getImp(), initPos);
		//getParticleList().add( p );
		for( Particle p : getParticleList() )
			p.trackOverStack(); 
		 
	}
	
	protected class ImagePlusListener implements SliceListener
	{
		@Override
		public void sliceChanged(ImagePlus arg0)
		{
			int slice = getImp().getCurrentSlice();
			if( getImp().getOverlay() == null )
			{
				getImp().setOverlay( new Overlay() ); 
			}
			
			getImp().getOverlay().clear(); 
			
			for( Particle p : getParticleList() )
			{
				final int posX = (int) Math.round( p.getFitParameterList().get( slice - 1)[ 1 ] );
				final int posY = (int) Math.round( p.getFitParameterList().get( slice - 1)[ 2 ] );
				
				
				Point point = new Point( new int[]{ posX, posY } ); 
				DrawOverlay.addOval( point, 1, 1 , imp );
				
			}
			
			
		}		
	}

	private void setImp( final File file ) { this.imp = OpenImages.getFloatTypeImp( file );  }
	public ImagePlus getImp() { return this.imp; } 
	
	private void setParticleList( final ArrayList<Particle> particleList ) { this.particleList = particleList; } 
	private ArrayList<Particle> getParticleList() { return this.particleList; } 

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		new ImageJ(); 
		//final File image = new File( "sampleEasy.tif" );
		final File image = new File( "/Volumes/HD-EU2/TIRF/20130305/priorNTP8bit.tif" );
		new StartTracker( image );

	}

}
