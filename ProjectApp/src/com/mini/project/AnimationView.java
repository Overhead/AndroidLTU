package com.mini.project;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AnimationView extends SurfaceView implements
		SurfaceHolder.Callback {

	private AnimationThread animationThread = null;

	/**
	 * Obvious Constructor is obvious. Get holder, give it to game thread.
	 * 
	 * @param context
	 */
	public AnimationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// Need rough screen dimensions for ball placement
		Display display = ((Activity) context).getWindowManager()
				.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		animationThread = new AnimationThread(holder, context, new Handler(),
				width, height);
		setFocusable(true);
	}

	/**
	 * Getter for the view's thread.
	 * 
	 * @return
	 */
	public AnimationThread getThread() {
		return animationThread;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	/**
	 * The view start's its GameThread once it has been created and this is
	 * called by the system.
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		animationThread.setRunning(true);
		animationThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		animationThread.setRunning(false);
	}

	/**
	 * AnimationThread takes care of the drawing, physics updating
	 * 
	 * @author Samuel F. Smith
	 * 
	 */
	public class AnimationThread extends Thread {

		/** Globals */
		private SurfaceHolder surfaceHolder;
		private boolean threadRunning = false;
		private Context context;

		private Bitmap backdropImage;
		private List<Ball> ballList;

		private float xValue, yValue;
		private float gravityX, gravityY;
		private long previousTime, currentTime;
		private int screenX, screenY;

		/** Constants */
		private final static int NUMBER_OF_BALLS = 3;
		private final static int TEXT_SIZE = 50;

		/** Public Constructor */
		public AnimationThread(SurfaceHolder holder, Context context,
				Handler handler, int screenX, int screenY) {
			surfaceHolder = holder;
			this.context = context;

			// Rough estimates of the screen are set.
			initializeScreenDimensions(screenX, screenY);

			// Create balls.
			ballList = new ArrayList<Ball>();
			createBalls();

			// Creates the background image.
			backdropImage = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.logo);
		}

		/**
		 * Initializes the screen dimensions before they can be updated from the
		 * canvas. As soon as the Canvas is drawn they are updated to the
		 * correct dimensions, but these serve as rough locations for the
		 * initial ball locations. ScreenY is shortened due to its often
		 * incorrect value (canvasY < screenY) and balls will become stuck at
		 * their bounds if initially placed outside.
		 */
		private void initializeScreenDimensions(int screenX, int screenY) {
			this.screenX = screenX;
			this.screenY = (int) (screenY * 0.75f);
		}

		@Override
		public void run() {

			while (threadRunning) {
				Canvas c = null;
				c = new Canvas();
				try {
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						updateLocations();
						doDraw(c);
					}
				} finally {
					if (c != null) {
						/*
						 * Sets the screen dimensions correctly using the canvas
						 * and its dimensions.
						 */
						screenX = c.getWidth();
						screenY = c.getHeight();
						surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		/**
		 * Set the thread's running state flag.
		 * 
		 * @param b
		 */
		public void setRunning(boolean running) {
			threadRunning = running;
		}

		/**
		 * Update all entity locations based on elapsed time since last call.
		 */
		private void updateLocations() {
			currentTime = System.currentTimeMillis();
			int elapsed = (int) (currentTime - previousTime);

			updateFallingObjectLocations(elapsed);

			previousTime = currentTime;
		}

		/**
		 * Updates the ball's locations based on the elapsed time and the
		 * current gravitational force in both the x and y directions.
		 * 
		 * @param elapsed
		 */
		private void updateFallingObjectLocations(int elapsed) {

			float time = (float) elapsed / 1000.0f;
			for (Ball b : ballList) {
				int xDelta = (int) (b.getVelocityX() * time + 0.5 * gravityX
						* time * time);
				int yDelta = (int) (b.getVelocityY() * time + 0.5 * gravityY
						* time * time);

				xDelta = -(int) (xDelta);

				float newVelX = b.getVelocityX() + gravityX * elapsed;
				float newVelY = b.getVelocityY() + gravityY * elapsed;

				b.setVelocityX(newVelX);
				b.setVelocityY(newVelY);

				b.updatePositionX(xDelta, screenX);
				b.updatePositionY(yDelta, screenY);
			}

			int pixels = elapsed / 40;
			pixels = pixels * 5;
		}

		/**
		 * Performs all the necessary drawing. (e.g. all entities on the screen,
		 * the background)
		 * 
		 * @param canvas
		 */
		private void doDraw(Canvas canvas) {

			// Clear canvas
			canvas.drawColor(Color.BLACK);

			// Draw the background (Warning! assumes screen > img)
			canvas.drawBitmap(backdropImage,
					(screenX - backdropImage.getWidth()) / 2,
					(screenY - backdropImage.getHeight()) / 2, null);

			
			drawText(canvas);
			drawFallingObjects(canvas);

			previousTime = currentTime;
		}

		
		private void drawText(Canvas canvas){
			Paint paint = new Paint();
			paint.setColor(Color.YELLOW);
			paint.setTextSize(TEXT_SIZE);
			canvas.drawText("X: " + xValue, screenX / 3, screenY / 4, paint);
			canvas.drawText("Y: " + yValue, screenX / 3, (screenY / 4) + TEXT_SIZE, paint);
		}
		
		/**
		 * Draws the balls on the canvas at their current location.
		 * 
		 * @param canvas
		 */
		private void drawFallingObjects(Canvas canvas) {

			for (Ball b : ballList) {
				canvas.drawBitmap(b.getImage(), b.getPositionX(),
						b.getPositionY(), null);
			}
		}

		/**
		 * Creates the balls based on the {@code NUMBER_OF_BALLS} using the
		 * supplied images. Each ball is given a modifier used to scale the
		 * distance traveled.
		 */
		private void createBalls() {

			for (int i = 0; i < NUMBER_OF_BALLS; i++) {
				int id = i % 3;
				Ball b = null;

				switch (id) {
				case 0:
					b = new Ball(R.drawable.red_ball, context.getResources(),
							screenX, screenY, 0.15f);
					break;
				case 1:
					b = new Ball(R.drawable.blue_ball, context.getResources(),
							screenX, screenY, 0.2f);
					break;
				case 2:
					b = new Ball(R.drawable.green_ball, context.getResources(),
							screenX, screenY, 0.25f);
					break;
				}

				ballList.add(b);
			}

		}

		/**
		 * Used to set the thread's gravity captured by the sensors.
		 */
		public void setGravityXandY(float gravityX, float gravityY) {
			this.gravityX = gravityX;
			this.gravityY = gravityY;
		}
		
		public void setTextXandY(float X, float Y){
			this.xValue = X;
			this.yValue = Y;
		}

	}
}
