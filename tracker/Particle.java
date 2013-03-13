package tracker;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import mpicbg.imglib.algorithm.peak.GaussianPeakFitterND;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.imglib.util.DevUtil;

public class Particle 
{
	private double[] initPos; 
	private ArrayList<double[]> fitParameterList; 
	private ImagePlus imp;
	
	public Particle( final ImagePlus imp, final double[] initPos )
	{
		setFitParameterList( new ArrayList<double[]>() );
		setInitPos( initPos );
		setImp( imp ); 
	}
	
	public void trackOverStack()
	{
		double[] lastFitParameter = getInitPos(); 
		double[] newFitParameter = null; 
		for( int i = 1; i <= getImp().getStackSize(); i++ )
		{
			
			ImageProcessor ip = getImp().getImageStack().getProcessor( i );  
			float[] pixels = (float[]) ip.getPixels();
			
			Image<FloatType> currentStack =  DevUtil.createImageFromArray( pixels, new int[]{ imp.getWidth(), imp.getHeight() } );
			GaussianPeakFitterND<FloatType> fit = new GaussianPeakFitterND<FloatType>( currentStack ); 
			
			LocalizableByDimCursor<FloatType> cursor = currentStack.createLocalizableByDimCursor();
			
			//System.out.println( i + "\t" + lastFitParameter[ 1 ] + " " + lastFitParameter[ 2 ] );
			cursor.setPosition( new int[] {(int) Math.round( lastFitParameter[ 1 ] ), (int) Math.round( lastFitParameter[ 2 ] )} );
			
			  
			newFitParameter = fit.process( cursor, new double[]{ 2.5, 2.5 } );
			
			System.out.println( i + "\t" + newFitParameter[ 1 ] + " " + newFitParameter[ 2 ] + " " + newFitParameter[ 3 ] + " " + newFitParameter[ 4 ] ); 
			getFitParameterList().add( newFitParameter );
			
			//Check if there's no jump
			if( getDistance( newFitParameter, lastFitParameter) <= 5.0 )
				lastFitParameter = newFitParameter; 
		}
	}
	
	public void writeNormalizedPosition()
	{
		final File file = new File( "temp/" + toString() + ".position"); 
		try 
		{
			PrintWriter out = new PrintWriter( file );
			final String header = "time" + "\t" + "posX" + "\t" + "posY" ;
			out.println( header ); 
			
			int count = 0;
			double[] normalizedPos;  
			for( double[] fitParameter : getFitParameterList() )
			{
				normalizedPos = getNormalizedPosition(fitParameter, getInitPos() );  
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
	
	public void writeDistance()
	{
		final File file = new File( "temp/" + toString() + ".distance"); 
		try 
		{
			PrintWriter out = new PrintWriter( file );
			final String header = "time" + "\t" + "distance";
			out.println( header ); 
			
			int count = 0; 
			for( double[] fitParameter : getFitParameterList() )
			{
				out.println( count + "\t" + getDistance( getPositionFromFitParameters( getInitPos() ), getPositionFromFitParameters( fitParameter ) ) );
				count++; 
			}
			
			out.close(); 
			
		} catch (FileNotFoundException e) 
		{
			System.err.println( "Cannot write to file: " + file + " " + e ); 
			e.printStackTrace();
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
	
	private void setInitPos( final double[] initPos ) { this.initPos = initPos; } 
	public double[] getInitPos() { return this.initPos; } 
	
	private void setFitParameterList( final ArrayList<double[]> fitParameterList ) { this.fitParameterList = fitParameterList; } 
	public ArrayList<double[]> getFitParameterList() { return this.fitParameterList; } 
	
	private void setImp( final ImagePlus imp ) { this.imp = imp; } 
	public ImagePlus getImp() { return this.imp; }
}
