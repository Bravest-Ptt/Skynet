package skynet.wheelview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

public class ImageTextButton extends AppCompatButton {
	private final String namespace = "http://www.pengtian.me";
	private int resourceId = 0;
	private Bitmap bitmap;

	public ImageTextButton(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		setClickable(true);
		resourceId = attrs.getAttributeResourceValue(namespace, "icon",R.mipmap.ic_launcher);
		bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
		this.setTextSize(12);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		int x = (this.getMeasuredWidth() - bitmap.getWidth()) >> 1;
		int y = 0;
		canvas.drawBitmap(bitmap, x, y, null);
		canvas.translate(0,(this.getMeasuredHeight() >> 1) - (int) this.getTextSize());
		super.onDraw(canvas);
	}

	public void setIcon(Bitmap bitmap) 
	{
		this.bitmap = bitmap;
		invalidate();
	}

	public void setIcon(int resourceId) 
	{
		this.bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
		invalidate();
	}

}
