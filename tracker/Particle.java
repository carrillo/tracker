package tracker;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.ArrayList;

import mpicbg.imglib.algorithm.peak.GaussianPeakFitterND;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.imglib.util.DevUtil;

public class Particle 
{
	private long[] initPos; 
	private ArrayList<double[]> fitParameterList; 
	private ImagePlus imp;
	
	public Particle( final ImagePlus imp, final long[] initPos )
	{
		setFitParameterList( new ArrayList<double[]>() );
		setInitPos( initPos );
		setImp( imp ); 
	}
	
	public void trackOverStack()
	{
		double[] lastFitParameter = new double[]{0,getInitPos()[ 0 ],getInitPos()[ 1 ],0,0};
		
		for( int i = 1; i <= getImp().getStackSize(); i++ )
		{
			
			ImageProcessor ip = getImp().getImageStack().getProcessor( i );  
			float[] pixels = (float[]) ip.getPixels();
			
			Image<FloatType> currentStack =  DevUtil.createImageFromArray( pixels, new int[]{ imp.getWidth(), imp.getHeight() } );
			GaussianPeakFitterND<FloatType> fit = new GaussianPeakFitterND<FloatType>( currentStack ); 
			
			LocalizableByDimCursor<FloatType> cursor = currentStack.createLocalizableByDimCursor();
			
			//System.out.println( i + "\t" + lastFitParameter[ 1 ] + " " + lastFitParameter[ 2 ] );
			cursor.setPosition( new int[] {(int) Math.round( lastFitParameter[ 1 ] ), (int) Math.round( lastFitParameter[ 2 ] )} );
			
			  
			lastFitParameter = fit.process( cursor, new double[]{ 2.5, 2.5 } );
			
			System.out.println( i + "\t" + lastFitParameter[ 1 ] + " " + lastFitParameter[ 2 ] + " " + lastFitParameter[ 3 ] + " " + lastFitParameter[ 4 ] ); 
			getFitParameterList().add( lastFitParameter ); 
		}
	}
	
	private void setInitPos( final long[] initPos ) { this.initPos = initPos; } 
	public long[] getInitPos() { return this.initPos; } 
	
	private void setFitParameterList( final ArrayList<double[]> fitParameterList ) { this.fitParameterList = fitParameterList; } 
	public ArrayList<double[]> getFitParameterList() { return this.fitParameterList; } 
	
	private void setImp( final ImagePlus imp ) { this.imp = imp; } 
	public ImagePlus getImp() { return this.imp; }
}
