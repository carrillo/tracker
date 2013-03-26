package tracker;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

import mpicbg.imglib.algorithm.peak.GaussianPeakFitterND;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.util.DevUtil;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.localization.Gaussian;
import net.imglib2.algorithm.localization.LevenbergMarquardtSolver;
import net.imglib2.algorithm.localization.MLEllipticGaussianEstimator;
import net.imglib2.algorithm.localization.PeakFitter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.transform.FastFourierTransformer;

public class Particle implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StartTracker tracker; 
	private double[] initPos; 
	private ArrayList<double[]> fitParameterList; 
	private ArrayList<Double> timeList; 
	private ImagePlus imp;
	
	public Particle( final ImagePlus imp, final double[] initPos, final StartTracker tracker ) 
	{
		setTracker( tracker ); 
		setFitParameterList( new ArrayList<double[]>() );
		setInitPos( initPos );
		setImp( imp ); 
	}
	public Particle() {}; 
	
	public void trackOverStack()
	{
		double[] lastFitParameter = getInitPos(); 
		double[] newFitParameter = null; 
		for( int i = 1; i <= getImp().getStackSize(); i++ )
		{
			
			ImageProcessor ip = getImp().getImageStack().getProcessor( i );  
			float[] pixels = (float[]) ip.getPixels();
			
			/*
			Image<FloatType> currentStack =  DevUtil.createImageFromArray( pixels, new int[]{ imp.getWidth(), imp.getHeight() } );
			GaussianPeakFitterND<FloatType> fit = new GaussianPeakFitterND<FloatType>( currentStack ); 
			LocalizableByDimCursor<FloatType> cursor = currentStack.createLocalizableByDimCursor();
			cursor.setPosition( new int[] {(int) Math.round( lastFitParameter[ 1 ] ), (int) Math.round( lastFitParameter[ 2 ] )} );
			newFitParameter = fit.process( cursor, new double[]{ 2.5, 2.5 } );
			
			
			*/
			Img<FloatType> currentStack = ArrayImgs.floats( pixels, getImp().getWidth(), getImp().getHeight() );
			RandomAccess<FloatType> cursor = currentStack.randomAccess(); 
			cursor.setPosition( new int[]{ (int) Math.round( lastFitParameter[ 1 ]), (int) Math.round( lastFitParameter[ 2 ] ) } ); 
			ArrayList<Localizable> peaks = new ArrayList<Localizable>();
			peaks.add( cursor ); 
			final double sigmaX = Math.sqrt( 1.0 / lastFitParameter[ 3 ] ); 
			final double sigmaY = Math.sqrt( 1.0 / lastFitParameter[ 4 ] ); 
			MLEllipticGaussianEstimator estimator = new MLEllipticGaussianEstimator( new double[]{ sigmaX, sigmaY } ); 
			PeakFitter<FloatType> peakFitter = new PeakFitter<FloatType>( currentStack, peaks, new LevenbergMarquardtSolver(300, 1e-3d, 1e-1d), new Gaussian(), estimator );
			peakFitter.process(); 
			newFitParameter = peakFitter.getResult().get( cursor );  
			 
			
			
			System.out.println( i + "\t" + newFitParameter[ 1 ] + " " + newFitParameter[ 2 ] + " " + newFitParameter[ 3 ] + " " + newFitParameter[ 4 ] ); 
			getFitParameterList().add( newFitParameter );
			
			//Check if there's no jump
			if( getDistance( newFitParameter, lastFitParameter) <= 5.0 )
				lastFitParameter = newFitParameter; 
		}
	}
	
	public void writeNormalizedPosition()
	{
		final File file = new File( "temp/" + toString() + ".normposition"); 
		try 
		{
			PrintWriter out = new PrintWriter( file );
			final String header = "time" + "\t" + "posX" + "\t" + "posY" ;
			out.println( header ); 
			
			int count = 0;  
			for( double[] normalizedPos : getNormalizedPositionArray() )
			{
				out.println( count + "\t" + normalizedPos[ 1 ] + "\t" + normalizedPos[ 2 ] );
				count++; 
			}
			
			out.close(); 
			
		} catch (FileNotFoundException e) 
		{
			System.err.println( "Cannot write to file: " + file + " " + e ); 
			e.printStackTrace();
		} 
	}
	
	public void writePosition()
	{
		final File file = new File( "temp/" + toString() + ".position"); 
		try 
		{
			PrintWriter out = new PrintWriter( file );
			final String header = "time" + "\t" + "posX" + "\t" + "posY" ;
			out.println( header ); 
			
			int count = 0;  
			for( double[] normalizedPos : getPositionArray() )
			{
				out.println( count + "\t" + normalizedPos[ 0 ] + "\t" + normalizedPos[ 1 ] );
				count++; 
			}
			
			out.close(); 
			
		} catch (FileNotFoundException e) 
		{
			System.err.println( "Cannot write to file: " + file + " " + e ); 
			e.printStackTrace();
		} 
	}
	
	public void writeDistance()
	{
		final File file = new File( "temp/" + toString() + ".distance"); 
		try 
		{
			PrintWriter out = new PrintWriter( file );
			final String header = "time" + "\t" + "distance" + "\t" + "sd";
			out.println( header ); 
			
			int count = 0; 
			ArrayList<Double> distanceArray = getDistanceArray(); 
			ArrayList<Double> sdArray = getSDArray( distanceArray, 10 );
			for( int i = 0; i < distanceArray.size(); i++ )
			{
				out.println( count + "\t" + distanceArray.get( i ) + "\t" + sdArray.get( i ) );
				count++; 
			}
			
			out.close(); 
			
		} catch (FileNotFoundException e) 
		{
			System.err.println( "Cannot write to file: " + file + " " + e ); 
			e.printStackTrace();
		} 
	}
	
	public ArrayList<double[]> getPositionArray()
	{ 
		ArrayList<double[]> output = new ArrayList<double[]>(); 
		for( double[] fitParameter : getFitParameterList() )
		{
			output.add( getPositionFromFitParameters( fitParameter ) ); 
		}
		
		return output; 
	}
	
	public ArrayList<double[]> getNormalizedPositionArray()
	{ 
		ArrayList<double[]> output = new ArrayList<double[]>(); 
		for( double[] fitParameter : getFitParameterList() )
		{
			output.add( getNormalizedPosition(fitParameter, getInitPos() ) ); 
		}
		
		return output; 
	}
	
	public ArrayList<Double> getSDArray( final ArrayList<Double> in, final int windowSize )
	{
		final ArrayList<Double> out = new ArrayList<Double>(); 
		
		SummaryStatistics ss = new SummaryStatistics(); 
		for( int i = 0; i < in.size(); i++ )
		{
			if( i < ( windowSize - 1 ) || i >= ( in.size() - windowSize ) )
			{
				out.add( 0.0 ); 
			}
			else
			{
				ss.clear(); 
				for( int j = 0; j < windowSize; j++ )
				{
					ss.addValue( in.get( i + j ) ); 
				}
				out.add( ss.getStandardDeviation() ); 
			}
		}
		
		return out; 
	}
	
	public void fastFourierTransform( final ArrayList<Double> in )
	{
		FastFourierTransformer fft = new FastFourierTransformer(); 
		
		//Generate array holding the data of size 2^n 
		final double nearestPower = Math.ceil( Math.log( in.size() )/Math.log( 2 ) );  
		final double[] inArray = new double[ (int) Math.pow(2.0, nearestPower) ]; 
		
		for( int i = 0; i < in.size(); i++ )
			inArray[ i ] = in.get( i ); 
		
		//Do the transform the ordering of the frequencies will be for example for a 8 datapoint input: 
		// [ 0 1 2 3 4 -3 -2 -1] --> highest frequency is N/2 
		Complex[] transform = fft.transform( inArray );  
		
		//To map frequency to the index 
		//final double totalTime = getTimeList().get( getTimeList().size() -1 ) - getTimeList().get( 0 ); 
		//final double frameRate = totalTime / getTimeList().size();
		final double totalTime = 1; 
		final double frameRate = totalTime / in.size();
		for( int i = 0; i < transform.length; i++ )
		{
			final double power = Math.sqrt( Math.pow( transform[ i ].getReal(), 2) + Math.pow( transform[ i ].getImaginary(), 2 ) );
			if( i <= transform.length / 2 )
			{
				System.out.println( i*frameRate + "\t" + power ); 
			} 
		} 
		
	}
	
	public double[] getPositionFromFitParameters( final double[] fitParameter )
	{
		final double[] output = new double[ 2 ]; 
		output[ 0 ] = fitParameter[ 1 ]; 
		output[ 1 ] = fitParameter[ 2 ]; 
		return output; 
	}
	
	public double[] getNormalizedPosition( final double[] a, final double[] b )
	{
		final double[] output = new double[ a.length ]; 
		for( int i = 0; i < a.length; i++ )
			output[ i ] = a[ i ] - b[ i ]; 

		return output; 
	}
	
	public ArrayList<Double> getDistanceArray()
	{
		ArrayList<Double> output = new ArrayList<Double>(); 
		for( double[] fitParameter : getFitParameterList() )
		{
			output.add( getDistance( getPositionFromFitParameters( getInitPos() ), getPositionFromFitParameters( fitParameter ) ) ) ;
		}
		
		return output; 
	}
	
	public double getDistance( final double[] a, final double[] b )
	{
		double distance = 0; 
		for( int i = 0; i < a.length; i++ )
		{
			distance += Math.pow( ( a[ i ] - b[ i ]), 2 ); 
		}
		return Math.sqrt( distance ); 
	}
	
	public String toString() 
	{
		final long posX = Math.round( getInitPos()[ 1 ] );
		final long posY = Math.round( getInitPos()[ 2 ] );
		return posX + "x" + posY; 
	}
	
	private void setTracker( final StartTracker tracker ) { this.tracker = tracker; } 
	public StartTracker getTracker() { return this.tracker; } 
	
	private void setInitPos( final double[] initPos ) { this.initPos = initPos; } 
	public double[] getInitPos() { return this.initPos; } 
	
	private void setFitParameterList( final ArrayList<double[]> fitParameterList ) { this.fitParameterList = fitParameterList; } 
	public ArrayList<double[]> getFitParameterList() { return this.fitParameterList; }
	
	private void setTimeList( final ArrayList<Double> timelist ) { this.timeList = timelist; } 
	public ArrayList<Double> getTimeList() { return this.timeList; } 
	
	private void setImp( final ImagePlus imp ) { this.imp = imp; } 
	public ImagePlus getImp() { return this.imp; }
	
	public static void main(String[] args) 
	{
		Particle p = new Particle(); 
		
		ArrayList<Double> a = new ArrayList<Double>();
		
		double b; 
		for( int i = 0; i < 8 ; i++ )
		{
			b =  (double) ( i * 1.0/8.0 );
			a.add( Math.sin( 2*Math.PI*b ) ) ; 
		}
		
		p.fastFourierTransform( a ); 
	}
}
