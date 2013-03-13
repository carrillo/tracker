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
import inputOutput.OpenImages;

import java.io.File;
import java.io.FileNotFoundException;
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
import net.imglib2.type.numeric.real.FloatType;
import roiTools.DrawOverlay;

public class StartTracker 
{
	private File file; 
	private ImagePlus imp;
	private ArrayList<Particle> particleList;
	//private ArrayList<double[]> fitParameterList; 
	
	public StartTracker( final File file )
	{
		final boolean verbose = true;
		setFile( file ); 
		setImp( getFile() ); 
		setParticleList( findParticles( getFile(), verbose ) );
		fit(); 
		write(); 
		
		// add listener to the imageplus slice slider
		SliceObserver sliceObserver = new SliceObserver( imp, new ImagePlusListener() );
	}
	
	/**
	 * This method finds the starting point for the tracking. 
	 * @param image
	 */
	public ArrayList<Particle> findParticles( final File file, final boolean verbose )
	{ 
		ArrayList<Particle> particleList = new ArrayList<Particle>(); 
		//ImagePlus imp = OpenImages.getFloatTypeImp( file ); 
		getImp().show(); 
		 
		Img<FloatType> img = ImagePlusAdapter.wrap( getImp() );  
		
		int nrOfDimension = img.numDimensions(); 
		Img<FloatType> subStack = Substack.getSubstack(img, ( nrOfDimension -1) , 0, 49, verbose );  
		Img<net.imglib2.type.numeric.real.FloatType> averageImg = Average.averageOverDimension( subStack, nrOfDimension, verbose );
		Gauss.inFloatInPlace(1.0, averageImg); 
		 
		System.out.println( "finding peaks" ); 
		PickImagePeaks<FloatType> pip = new PickImagePeaks<FloatType>( averageImg );
		System.out.println( "finding peaks. Done" );
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
				
				
				
				particleList.add( new Particle( getImp(), fitGaussian( averageImg, pos ) ) ); 
			}
		}
		

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
		
		 
		return particleList; 
	}
	
	public double[] fitGaussian( final Img<FloatType> img, final long[] startingPos )
	{
		Image<mpicbg.imglib.type.numeric.real.FloatType> image = Wrapper.wrapToImgLib1( img ); 
		
		GaussianPeakFitterND<mpicbg.imglib.type.numeric.real.FloatType> fit = new GaussianPeakFitterND<mpicbg.imglib.type.numeric.real.FloatType>( image );
		LocalizableByDimCursor<mpicbg.imglib.type.numeric.real.FloatType> cursor = image.createLocalizableByDimCursor();
		cursor.setPosition( new int[]{ (int) startingPos[ 0 ], (int) startingPos[ 1 ] } );
		return fit.process( cursor, new double[]{ 3, 3 } ); 
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
		for( Particle p : getParticleList() )
		{
			p.trackOverStack(); 
			p.writeDistance(); 
			p.writeNormalizedPosition();
		}
		 
	}
	
	public void write()
	{
		writeDistanceArray(); 
		writeNormPositionArray(); 
	}
	
	public void writeDistanceArray()
	{
		ArrayList<ArrayList<Double>> distanceMatrix = new ArrayList<ArrayList<Double>>();
		ArrayList<String> idList = new ArrayList<String>(); 
		for( Particle p : getParticleList() )
		{ 
			idList.add( p.toString() ); 
			distanceMatrix.add( p.getDistanceArray() ); 
		}
		
		final File file = new File( "temp/" + getFile().getName() + ".distance"); 
		System.out.println( "Writing distance array to file: " + file.getAbsolutePath() ); 
		try 
		{
			PrintWriter out = new PrintWriter( file );
			String header = "time"; 
			for( String s : idList )
			{
				header += "\t" + s; 
			}
			out.println( header ); 
			
			
			int count = 0;
			String line; 
			for( int i = 0; i < distanceMatrix.get( 0 ).size(); i++ )
			{
				line = "" + count;  
				
				for( ArrayList<Double> distanceList : distanceMatrix )
				{					
					line += "\t" + distanceList.get( i );  
				}
				out.println( line ); 
				count++; 
			}
			out.close(); 
			
		} catch (FileNotFoundException e) 
		{
			System.err.println( "Cannot write to file: " + file + " " + e ); 
			e.printStackTrace();
		} 
		System.out.println( "Writing distance array to file. Done\n----- ");
	}
	
	public void writeNormPositionArray()
	{
		ArrayList<ArrayList<double[]>> positionMatrix = new ArrayList<ArrayList<double[]>>();
		ArrayList<String> idList = new ArrayList<String>(); 
		for( Particle p : getParticleList() )
		{
			idList.add( p.toString() ); 
			positionMatrix.add( p.getNormalizedPositionArray() ); 
		}
		
		final File file = new File( "temp/" + getFile().getName() + ".position"); 
		try 
		{
			System.out.println( "Writing normalized position array to file: " + file.getAbsolutePath() ); 
			PrintWriter out = new PrintWriter( file );
			String header = "time"; 
			for( String s : idList )
			{
				header += "\t" + s; 
			}
			out.println( header ); 
			
			
			int count = 0;
			String line; 
			for( int i = 0; i < positionMatrix.get( 0 ).size(); i++ )
			{
				line = "" + count; 
				
				for( ArrayList<double[]> positionList : positionMatrix )
				{
					line += "\t" + positionList.get( i )[ 1 ] + ";" + positionList.get( i )[ 2 ];  
				}
				out.println( line ); 
				count++; 
			}
			out.close(); 
			
		} catch (FileNotFoundException e) 
		{
			System.err.println( "Cannot write to file: " + file + " " + e ); 
			e.printStackTrace();
		} 
		System.out.println( "Writing normalized position array to file. Done\n----- ");
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

	private void setFile( final File file ) { this.file = file; } 
	public File getFile() { return this.file; } 
	
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
		//final File image = new File( "sampleEasyShort.tif" );
		final File image = new File( "sampleRunShort.tif" );
		//final File image = new File( "run-file002.tif"); 
		//final File image = new File( "/Volumes/HD-EU2/TIRF/20130305/priorNTP32bit.tif" );
		new StartTracker( image );

	}

}
