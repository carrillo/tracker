package roiTools;

import java.awt.Color;

import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import mpicbg.imglib.cursor.Localizable;

public class DrawOverlay 
{
	public static void DrawCircle( final Localizable position, final int diameter, final ImagePlus imp )
	{
		DrawOval(position, diameter, diameter, imp); 
	}
	
	public static void DrawOval( final Localizable position, final int width, final int height, final ImagePlus imp )
	{
		Overlay o = imp.getOverlay(); 
		
		if( o == null )
		{
			o = new Overlay(); 
			imp.setOverlay( o ); 
		}
		
		o.clear(); 
		
		final int circleXCoord = position.getPosition( 0 ) - Math.round( width/2 ); 
		final int circleYCoord = position.getPosition( 1 ) - Math.round( height/2 );
		
		final OvalRoi or = new OvalRoi( circleXCoord, circleYCoord, width, height );
		or.setStrokeColor( Color.green ); 
		or.setStrokeWidth( 0.1 ); 
		
		o.add( or ); 
	}
	
	public static void addOval( final Localizable position, final int width, final int height, final ImagePlus imp )
	{
		addOval(position, width, height, imp , Color.green ); 
	}
	
	public static void addOval( final Localizable position, final int width, final int height, final ImagePlus imp, final Color color )
	{
		Overlay o = imp.getOverlay(); 
		
		if( o == null )
		{
			o = new Overlay(); 
			imp.setOverlay( o ); 
		}
		 
		
		final int circleXCoord = position.getPosition( 0 ) - Math.round( width/2 ); 
		final int circleYCoord = position.getPosition( 1 ) - Math.round( height/2 );
		
		final OvalRoi or = new OvalRoi( circleXCoord, circleYCoord, width, height );
		or.setStrokeColor( color ); 
		or.setStrokeWidth( 0.1 ); 
		
		o.add( or ); 
	}
}
