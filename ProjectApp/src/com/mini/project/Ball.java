package com.mini.project;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Ball {

	private Float velocityX, velocityY;
	private Integer positionX, positionY;

	private Bitmap ballImage;
	private float deltaModifier;

	/**
	 * Creates a Ball with zero velocity, a random location, and a given delta
	 * modifier (used to alter the "speed").
	 * 
	 * @param imageId
	 *            The resource id of the ball's image.
	 * @param resFile
	 *            The resrouce file in which to use.
	 * @param screenX
	 *            Rough width of the screen in pixels.
	 * @param screenY
	 *            Rough height of the screen in pixels.
	 * @param deltaModifier
	 *            Value by which the distance traveled should be scaled.
	 */
	public Ball(int imageId, Resources resFile, int screenX, int screenY,
			float deltaModifier) {
		this.velocityX = 0.0f;
		this.velocityY = 0.0f;
		this.deltaModifier = deltaModifier;
		this.ballImage = BitmapFactory.decodeResource(resFile, imageId);
		this.positionX = Math.abs((int) (Math.random() * screenX)
				- this.ballImage.getWidth());
		this.positionY = Math.abs((int) (Math.random() * screenY)
				- this.ballImage.getHeight());
	}

	/**
	 * Returns the veocity of the ball on the x-axis.
	 * 
	 * @return
	 */
	public Float getVelocityX() {
		return velocityX;
	}

	/**
	 * Sets the velocity of the ball on the x-axis.
	 * 
	 * @param velocityX
	 */
	public void setVelocityX(Float velocityX) {
		this.velocityX = velocityX;
	}

	/**
	 * Returns the velocity of the ball on the y-axis.
	 */
	public Float getVelocityY() {
		return velocityY;
	}

	/**
	 * Sets the velocity of the ball on the y-axis.
	 * 
	 * @param velocityY
	 */
	public void setVelocityY(Float velocityY) {
		this.velocityY = velocityY;
	}

	/**
	 * Returns the x position of the ball.
	 * 
	 * @return
	 */
	public Integer getPositionX() {
		return positionX;
	}

	/**
	 * Sets the y position of the ball.
	 * 
	 * @param positionX
	 */
	public void setPositionX(Integer positionX) {
		this.positionX = positionX;
	}

	/**
	 * Returns the y position of the ball.
	 * 
	 * @return
	 */
	public Integer getPositionY() {
		return positionY;
	}

	/**
	 * Sets the y position of the ball.
	 * 
	 * @param positionY
	 */
	public void setPositionY(Integer positionY) {
		this.positionY = positionY;
	}

	/**
	 * Calculates the new y position of the ball. The distance traveled is
	 * modified by the {@code deltaModifier} and then the new location is
	 * calculated. If the new position is outside the bounds of the Canvas then
	 * it is not updated and instead its velocity is reset to zero.
	 */
	public void updatePositionY(Integer deltaY, int screenY) {
		int newY = this.positionY + (int) (deltaY * deltaModifier);
		if ((newY + (ballImage.getHeight())) > screenY || newY < 0) {
			velocityY = 0.0f;
		} else {
			this.positionY = newY;
		}
	}

	/**
	 * Calculates the new x position of the ball. The distance traveled is
	 * modified by the {@code deltaModifier} and then the new location is
	 * calculated. If the new position is outside the bounds of the Canvas then
	 * it is not updated and instead its velocity is reset to zero.
	 * 
	 * @param deltaX
	 * @param screenX
	 */
	public void updatePositionX(Integer deltaX, int screenX) {
		int newX = this.positionX + (int) (deltaX * deltaModifier);
		if ((newX + ballImage.getWidth()) > screenX || newX < 0) {
			velocityX = 0.0f;
		} else {
			this.positionX = newX;
		}
	}

	/**
	 * Returns the ball's image.
	 * 
	 * @return Bitmap of the ball.
	 */
	public Bitmap getImage() {
		return ballImage;
	}
}
