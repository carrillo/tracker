package tracker;

import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import imgLib2Tools.Average;
import imgLib2Tools.Histogram;
import imgLib2Tools.Substack;
import imgLib2Tools.Wrapper;
import inputOutput.ObjectFileAccess;
import inputOutput.OpenImages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import mpicbg.imglib.algorithm.peak.GaussianPeakFitterND;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.gauss.Gauss;
import net.imglib2.algorithm.math.PickImagePeaks;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import roiTools.DrawOverlay;

public class StartTracker 
{
	private File file; 
	private ImagePlus imp;
	private ParticleCollection particleCollection;  
	
	public StartTracker( final File file )
	{
		setFile( file ); 
		String name = getFile().getName();
		String id = name.substring( 0, name.indexOf( ".") );
		System.out.println( id ); 
		
		final boolean verbose = true;
		setImp( getFile() ); 
		setParticleCollection( findParticles( getFile(), verbose ) ); 
		fit();
		getParticleCollection().analyze(); 
		//writeParticleCollection( "particleCollection.obj" ); 
		getParticleCollection().writeOutputFiles( id ); 			
		
		// add listener to the imageplus slice slider
		SliceObserver sliceObserver = new SliceObserver( imp, new ImagePlusListener() );
	}
	
	/**
	 * This method finds the starting point for the tracking. 
	 * @param image
	 */
	public ParticleCollection findParticles( final File file, final boolean verbose )
	{ 
		ArrayList<Particle> particleList = new ArrayList<Particle>(); 
		//ImagePlus imp = OpenImages.getFloatTypeImp( file ); 
		getImp().show();  
		//getImp().getCalibration().frameInterval 
		
		Img<FloatType> img = ImagePlusAdapter.wrap( getImp() );  
		
		int nrOfDimension = img.numDimensions(); 
		Img<FloatType> subStack = Substack.getSubstack(img, ( nrOfDimension -1) , 0, 49, verbose );  
		Img<net.imglib2.type.numeric.real.FloatType> averageImg = Average.averageOverDimension( subStack, nrOfDimension, verbose );
		//ImageJFunctions.show( averageImg ); 
		Gauss.inFloatInPlace(1.0, averageImg); 
		 
		System.out.println( "finding peaks" ); 
		PickImagePeaks<FloatType> pip = new PickImagePeaks<FloatType>( averageImg );
		
		pip.process(); 
		ArrayList<long[]> peakPos = pip.getPeakList(); 
		
		
		ArrayList<ArrayList<Float>> hist = Histogram.getHistogram( averageImg, 100 ); 
		ArrayList<Float> lastBin = hist.get( hist.size() - 1 ); 
		
		getImp().setOverlay( new Overlay() ); 
		getImp().getOverlay().clear();
		
		
		for( long[] pos : peakPos )
		{ 
			if( likelyPeak( pos, averageImg, lastBin.get( 0 ) ) )
			{
				Point point = new Point( new int[] { (int) pos[ 0 ], (int) pos[ 1 ] }  ); 
				DrawOverlay.addOval( point, 1, 1 , imp );	
				
				
				
				particleList.add( new Particle( getImp(), fitGaussian( averageImg, pos ), this ) ); 
			}
		}
		
		System.out.println( "Found " + particleList.size() + " particles."  );

		/*
		ImageJFunctions.show( averageImg ); 
		ImageJFunctions.show( Extrema.findMaxima( averageImg ) ); 
		
		
		
		
		
		ImageProcessor ip = getImp().getImageStack().getProcessor( 1 );
		float[] pixels = (float[]) ip.getPixels();
		
		//for imglib2
		Img<FloatType> imglib2 = ArrayImgs.floats( pixels, imp.getWidth(), imp.getHeight() );
		//ImageJFunctions.show( imglib2 ); 
		
		// for imglib1
		Image<FloatType> imglib1 =  DevUtil.createImageFromArray( pixels, new int[]{ imp.getWidth(), imp.getHeight() } );
		//ImageJFunctions.show( imglib1 ); 
		//GaussianPeakFitterND<FloatType> fit = new GaussianPeakFitterND<FloatType>( imglib1 );
		//fit.process(point, typical_sigma)
		
		*/
		
		 
		return new ParticleCollection( getImp(), particleList ); 
	}
	
	public double[] fitGaussian( final Img<FloatType> img, final long[] startingPos )
	{
		Image<mpicbg.imglib.type.numeric.real.FloatType> image = Wrapper.wrapToImgLib1( img ); 
		
		GaussianPeakFitterND<mpicbg.imglib.type.numeric.real.FloatType> fit = new GaussianPeakFitterND<mpicbg.imglib.type.numeric.real.FloatType>( image );
		LocalizableByDimCursor<mpicbg.imglib.type.numeric.real.FloatType> cursor = image.createLocalizableByDimCursor();
		cursor.setPosition( new int[]{ (int) startingPos[ 0 ], (int) startingPos[ 1 ] } );
		return fit.process( cursor, new double[]{ 5, 5 } ); 
	}
	
	public boolean likelyPeak( long[] peakPos, final Img<FloatType> img, final float threshold )
	{  
		if( peakPos[ 0 ] == 0 || peakPos[ 1 ] == 0 )
		{
			return false; 
		}
		else
		{
			RectangleShape shape = new RectangleShape(1,false);
			RandomAccess<Neighborhood<FloatType>> a = shape.neighborhoods( img ).randomAccess(); 
			a.setPosition( peakPos );
			
			boolean allBiggerThanThreshold = true; 
			for( final FloatType value : a.get() )
			{
				if( value.get() < threshold )
				{
					allBiggerThanThreshold = false; 
					break; 
				} 
			}
			
			return allBiggerThanThreshold;  
		}
	}
	
	public void fit()
	{
		for( Particle p : getParticleCollection().getParticleList() )
		{
			p.trackOverStack(); 
			
		}
		 
	}
	
	public void writeParticleCollection( final String outputFile )
	{
		ObjectFileAccess.saveObjectToFile( getParticleCollection(), outputFile ); 
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
			
			for( Particle p : getParticleCollection().getParticleList() )
			{
				final int posX = (int) Math.round( p.getPositionArray().get( slice - 1)[ 0 ] );
				final int posY = (int) Math.round( p.getPositionArray().get( slice - 1)[ 1 ] );
				
				
				Point point = new Point( new int[]{ posX, posY } ); 
				DrawOverlay.addOval( point, 1, 1 , imp );
				
			}
			
			
		}		
	}

	private void setFile( final File file ) { this.file = file; } 
	public File getFile() { return this.file; } 
	
	private void setImp( final File file ) { this.imp = OpenImages.getFloatTypeImp( file );  }
	public ImagePlus getImp() { return this.imp; } 
	
	private void setParticleCollection( final ParticleCollection pc ) { this.particleCollection = pc; }
	public ParticleCollection getParticleCollection() { return this.particleCollection; } 
 
	//private ArrayList<Particle> getParticleList() { return this.particleCollection.getParticleList(); } 

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		new ImageJ();
		/*
		final File dir = new File( "/Users/carrillo/temp/runFiles/" );
		for( String name : dir.list() )
		{  
			final File image = new File( dir.getAbsolutePath() + "/" + name );
			new StartTracker( image );
		}
		*/
		final File image = new File(  "/Users/carrillo/temp/20130403/run32bit.tif" );
		//final File image = new File(  "/Users/carrillo/temp/runFiles/flow_02_Opt25_32bit.tif" );
		new StartTracker( image );
		//final File image = new File( "temp.tif" );
		//final File image = new File( "run-file002.tif"); 
		//final File image = new File( "/Volumes/HD-EU2/TIRF/20130305/priorNTP32bit.tif" );
		//

	}

}
