package tracker;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import inputOutput.ObjectFileAccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.localization.Gaussian;
import net.imglib2.algorithm.localization.LevenbergMarquardtSolver;
import net.imglib2.algorithm.localization.MLGaussianEstimator;
import net.imglib2.algorithm.localization.PeakFitter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import arrayTools.ArrayListDoubleTools;

public class ParticleCollection implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Particle> particleList; 
	private ImagePlus imp;
	
	public ParticleCollection( final ImagePlus imp, final ArrayList<Particle> particleList )
	{
		setImp( imp ); 
		setParticleList( particleList ); 
	}
	
	/*
	public void trackParticles()
	{
		for( int i = 1; i <= getImp().getStackSize(); i++ )
		{	
			System.out.println( "Tracking stack nr: " + i );
			ImageProcessor ip = getImp().getImageStack().getProcessor( i );  
			float[] pixels = (float[]) ip.getPixels();
			Img<FloatType> currentStack = ArrayImgs.floats( pixels, getImp().getWidth(), getImp().getHeight() );
			
			//Fill fit starting positions 
			ArrayList<Localizable> peakPosList = new ArrayList<Localizable>(); 
			ArrayList<Double> sigmaList = new ArrayList<Double>();
			Particle p = null; 
			DescriptiveStatistics ds = new DescriptiveStatistics();
			for( int j = 0; j < getParticleList().size(); j++ )
			{
				p = getParticleList().get( j ); 
				final RandomAccess<FloatType> cursor = currentStack.randomAccess(); 
				
				double[] posToSet; 
				if( p.getDistanceBetweenLastTwoPos() == null  ||  p.getDistanceBetweenLastTwoPos() <= 5 )
				{
					posToSet = p.getLastPosition();  
					sigmaList.add( p.getLastSigma() ); 
				}
				else
				{
					posToSet = p.getSecondLastPosition();
					sigmaList.add( p.getSecondLastSigma() ); 
				}
				
				cursor.setPosition( new int[]{ (int) Math.round( posToSet[ 0 ] ), (int) Math.round( posToSet[ 1 ] )} );
				peakPosList.add( cursor ); 
			}
			
			//Calculate median sigma
			ds.clear(); 
			for( Double d : sigmaList )
			{
				ds.addValue( d ); 
			}
			final double medianSigma = Math.sqrt( 1.0 / ds.getPercentile( 50.0 ) ); 
		
			//Fit using the starting positions
			MLGaussianEstimator estimator = new MLGaussianEstimator( medianSigma, 2 );
			PeakFitter<FloatType> peakFitter = new PeakFitter<FloatType>( currentStack, peakPosList, new LevenbergMarquardtSolver(300, 1e-3d, 1e-1d), new Gaussian(), estimator );
			peakFitter.process();
			
			Map<Localizable, double[]> result = peakFitter.getResult(); 
			for( int j = 0; j < peakPosList.size(); j++ )
			{
				getParticleList().get( j ).addFitParameter( result.get( peakPosList.get( j ) ) );  
			}	
			for( double[] pos : getParticleList().get( 0 ).getPositionArray() )
			{
				System.out.println( pos[ 0 ] + " " + pos[ 1 ] ); 
			}
		}
	}
	*/
	
	public void writeNormalizedDistanceOfHighSDParticles( final String fileId )
	{
		final ArrayList<Particle> lowSd = new ArrayList<Particle>();
		final ArrayList<ArrayList<Double>> lowSdDistance = new ArrayList<ArrayList<Double>>(); 
		final ArrayList<Particle> highSd = new ArrayList<Particle>();
		final ArrayList<String> highSdId = new ArrayList<String>(); 
		for( Particle p : getParticleList() )
		{
			if( p.getMedianSD( p.getDistanceArray(), 10 ) > 0.5 )
			{
				highSd.add( p ); 
				highSdId.add( p.toString() ); 
			}
			else
			{
				lowSd.add( p );
				lowSdDistance.add( p.getDistanceArray() ); 
			}
		}
		final ArrayList<Double> medianLow = ArrayListDoubleTools.getMedian( lowSdDistance ); 
		//final ArrayList<Double> medianHigh = ArrayListDoubleTools.getMedian( highSd );
		
		ArrayList<ArrayList<Double>> normalizedDist = new ArrayList<ArrayList<Double>>(); 
		for( Particle p : highSd )
		{
			normalizedDist.add(  ArrayListDoubleTools.substract( p.getDistanceArray(), medianLow ) );
			//System.out.println( ArrayListDoubleTools.arrayListToString( norm ) ); 
		}
		
		String header = "time"; 
		for( String s : highSdId )
			header += "\t" + s; 
		
 		final File file = new File( "temp/" + fileId + ".highSdDistance");
		try 
		{
			System.out.println( "Writing high sd distance array to file: " + file.getAbsolutePath() ); 
			PrintWriter out = new PrintWriter( file );
			out.println( header  );
			
			
			int count = 0;
			String line; 
			for( int i = 0; i < normalizedDist.get( 0 ).size(); i++ )
			{
				line = "" + count; 
				
				for( ArrayList<Double> d : normalizedDist )
				{ 
					line += "\t" + d.get( i );  
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
		System.out.println( "Writing high sd distance array to file. Done\n----- ");
	}
	
	public void analyze()
	{
		
	}

	
	public void writeOutputFiles( final String id )
	{
		writeDistanceArray( id );
		writeSDArray( id, 10 );
		writeNormPositionArray( id ); 
		writePositionArray( id ); 
		writeMedianSD( id, 10 );
		writeNormalizedDistanceOfHighSDParticles( id ); 
	}
	
	public void writePositionArray( final String fileId )
	{
		ArrayList<ArrayList<double[]>> positionMatrix = new ArrayList<ArrayList<double[]>>(); 
		for( Particle p : getParticleList() )
		{ 
			positionMatrix.add( p.getPositionArray() ); 
		}
		
		final File file = new File( "temp/" + fileId + ".position"); 
		try 
		{
			System.out.println( "Writing position array to file: " + file.getAbsolutePath() ); 
			PrintWriter out = new PrintWriter( file );
			out.println( "time" + "\t" + getParticleIdsString()  );
			
			
			int count = 0;
			String line; 
			for( int i = 0; i < positionMatrix.get( 0 ).size(); i++ )
			{
				line = "" + count; 
				
				for( ArrayList<double[]> positionList : positionMatrix )
				{ 
					line += "\t" + positionList.get( i )[ 0 ] + ";" + positionList.get( i )[ 1 ];  
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
	
	public void writeNormPositionArray( final String fileId )
	{
		ArrayList<ArrayList<double[]>> positionMatrix = new ArrayList<ArrayList<double[]>>(); 
		for( Particle p : getParticleList() )
		{ 
			positionMatrix.add( p.getNormalizedPositionArray( p.getPositionArray().get( 0 ) ) ); 
		}
		
		final File file = new File( "temp/" + fileId + ".normposition"); 
		try 
		{
			System.out.println( "Writing normalized position array to file: " + file.getAbsolutePath() ); 
			PrintWriter out = new PrintWriter( file );
			out.println( "time" + "\t" + getParticleIdsString()  );
			
			
			int count = 0;
			String line; 
			for( int i = 0; i < positionMatrix.get( 0 ).size(); i++ )
			{
				line = "" + count; 
				
				for( ArrayList<double[]> positionList : positionMatrix )
				{
					line += "\t" + positionList.get( i )[ 0 ] + ";" + positionList.get( i )[ 1 ];  
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
	
	public void writeMedianSD( final String fileId, final int windowSize )
	{
		final File file = new File( "temp/" + fileId + ".mediansd");
		System.out.println( "Writing median sd value to file: " + file.getAbsolutePath() );

		try 
		{
			PrintWriter out = new PrintWriter( file );
			out.println( "particleId" + "\t" + "medianSD" ); 
			for( Particle p : getParticleList() )
			{
				out.println( p + "\t" + p.getMedianSD( p.getDistanceArray(), windowSize) ); 
			}
			
			out.close(); 
			System.out.println( "Writing median sd value to file. Done" );
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println( "Cannot write to file: " + file + " " + e ); 
			e.printStackTrace();
		}
		
		
	}
	
	public void writeSDArray( final String fileId, final int windowSize )
	{
		ArrayList<ArrayList<Double>> sdMatrix = new ArrayList<ArrayList<Double>>(); 
		for( Particle p : getParticleList() )
		{  
			sdMatrix.add( p.getSDArray( p.getDistanceArray(), windowSize ) ); 
		}
		
		final File file = new File( "temp/" + fileId + ".sd"); 
		System.out.println( "Writing sd array to file: " + file.getAbsolutePath() ); 
		try 
		{
			PrintWriter out = new PrintWriter( file );
			out.println( "time" + "\t" + getParticleIdsString()  ); 
			
			
			int count = 0;
			String line; 
			for( int i = 0; i < sdMatrix.get( 0 ).size(); i++ )
			{
				line = "" + count;  
				
				for( ArrayList<Double> distanceList : sdMatrix )
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
		System.out.println( "Writing sd array to file. Done\n----- ");
	}
	
	public void writeDistanceArray( final String fileId )
	{
		ArrayList<ArrayList<Double>> distanceMatrix = new ArrayList<ArrayList<Double>>();
		for( Particle p : getParticleList() )
		{ 
			distanceMatrix.add( p.getDistanceArray() ); 
		}
		
		final File file = new File( "temp/" + fileId + ".distance"); 
		System.out.println( "Writing distance array to file: " + file.getAbsolutePath() ); 
		
		try 
		{
			PrintWriter out = new PrintWriter( file );
			out.println( "time" + "\t" + getParticleIdsString()  ); 
			
			
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
	
	/**
	 * Returns the particle Ids as String ids. 
	 * @return
	 */
	public ArrayList<String> getParticleIds()
	{
		ArrayList<String> output = new ArrayList<String>();
		
		for( Particle p : getParticleList() )
			output.add( p.toString() ); 
		
		return output; 
	}
	public String getParticleIdsString()
	{
		String s = ""; 
		for( int i = 0; i < getParticleIds().size(); i++ )
		{
			if( i != 0 )
				s += "\t"; 
			s += getParticleIds().get( i ); 
		}
		return s; 
	}
	
	private void setParticleList( final ArrayList<Particle> particleList ) { this.particleList = particleList; } 
	public ArrayList<Particle> getParticleList() { return this.particleList; }
	
	private void setImp( final ImagePlus imp ) { this.imp = imp; }
	public ImagePlus getImp() { return this.imp; } 

}
