package tracker;

import inputOutput.ObjectFileAccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

public class ParticleCollection implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Particle> particleList; 
	
	public ParticleCollection( final ArrayList<Particle> particleList )
	{
		setParticleList( particleList ); 
	}
	
	
	
	public void writeOutputFiles( final String id )
	{
		writeDistanceArray( id ); 
		writeSDArray( id, 10 );
		writeNormPositionArray( id ); 
		writePositionArray( id ); 
	}
	
	public void writePositionArray( final String fileId )
	{
		ArrayList<ArrayList<double[]>> positionMatrix = new ArrayList<ArrayList<double[]>>();
		ArrayList<String> idList = new ArrayList<String>(); 
		for( Particle p : getParticleList() )
		{
			idList.add( p.toString() ); 
			positionMatrix.add( p.getPositionArray() ); 
		}
		
		final File file = new File( "temp/" + fileId + ".position"); 
		try 
		{
			System.out.println( "Writing position array to file: " + file.getAbsolutePath() ); 
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
		ArrayList<String> idList = new ArrayList<String>(); 
		for( Particle p : getParticleList() )
		{
			idList.add( p.toString() ); 
			positionMatrix.add( p.getNormalizedPositionArray() ); 
		}
		
		final File file = new File( "temp/" + fileId + ".normposition"); 
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
	
	public void writeSDArray( final String fileId, final int windowSize )
	{
		ArrayList<ArrayList<Double>> sdMatrix = new ArrayList<ArrayList<Double>>();
		ArrayList<String> idList = new ArrayList<String>(); 
		for( Particle p : getParticleList() )
		{ 
			idList.add( p.toString() ); 
			sdMatrix.add( p.getSDArray( p.getDistanceArray(), windowSize ) ); 
		}
		
		final File file = new File( "temp/" + fileId + ".sd"); 
		System.out.println( "Writing sd array to file: " + file.getAbsolutePath() ); 
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
		ArrayList<String> idList = new ArrayList<String>(); 
		for( Particle p : getParticleList() )
		{ 
			idList.add( p.toString() ); 
			distanceMatrix.add( p.getDistanceArray() ); 
		}
		
		final File file = new File( "temp/" + fileId + ".distance"); 
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
	
	private void setParticleList( final ArrayList<Particle> particleList ) { this.particleList = particleList; } 
	public ArrayList<Particle> getParticleList() { return this.particleList; }

}
