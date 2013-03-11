package tracker;

import mpicbg.imglib.cursor.Localizable;

public class Point implements Localizable 
{
	protected int[] position; 
	
	public Point( final int[] position )
	{ 
		this.position = position; 
	}

	@Override
	public void fwd(long steps) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fwd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getPosition(int[] position) {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] getPosition() {
		return this.position;
	}

	@Override
	public int getPosition(int dim) {
		return this.position[ dim ];
	}

	@Override
	public String getPositionAsString() {
		return position[ 0 ] + " " + position[ 1 ];
	}

}
