package com.mobidroid.labtts.activity;

import java.util.Hashtable;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class AnimationCoordinator implements AnimationListener,
		OnUtteranceCompletedListener {

	private static final String TAG = AnimationCoordinator.class.getName();

	private boolean bubbleIsFullyDeployed = false;
	private boolean bubbleIsOpening = false;
	private boolean bubbleIsClosing = false;

	private ImageView bubble;
	private ImageView robot;

	private Animation bubbleAppearAnim;
	private Animation bubbleDisappearAnim;

	private Hashtable<String, Integer> utteranceQueue;

	private Animation bubbleDisappearRevAnim;

	private Animation bubbleAppearRevAnim;

	private AnimationDrawable andySpeakAnim;

	private LabTTSActivity mainView;

	/**
	 * 
	 * @param ctx
	 * @param bubble
	 * @param robot
	 */
	public AnimationCoordinator(LabTTSActivity mainView, ImageView bubble,
			ImageView robot) {

		utteranceQueue = new Hashtable<String, Integer>();

		this.mainView = mainView;
		this.bubble = bubble;
		this.robot = robot;

		// Charge les animation SPLEEN
		bubbleAppearAnim = AnimationUtils.loadAnimation(mainView,
				R.anim.bubble_appear);

		bubbleDisappearAnim = AnimationUtils.loadAnimation(mainView,
				R.anim.bubble_disappear);

		// Register animation listener
		bubbleAppearAnim.setAnimationListener(this);
		bubbleDisappearAnim.setAnimationListener(this);
	}

	/**
	 * On fait apparaitre la bulle et disparaitre quand une utterence termine
	 * 
	 * @param text
	 * @param queueAdd
	 */
	public void onUtteranceBegin(String utId, String text, int stackType) {

		boolean wasAlreadySpeaking = false;
		
		if (stackType == TextToSpeech.QUEUE_FLUSH) {
			// Other utterance will never be played
			wasAlreadySpeaking = utteranceQueue.size()!=0;
			utteranceQueue.clear();
		}

		if (utteranceQueue.size() == 0 && !wasAlreadySpeaking) {
			mainView.animateBubble(bubbleAppearAnim);
		}

		utteranceQueue.put(utId, new Integer(stackType));
		startAnimationSpeaking(text);

	}

	/**
	 * @param text
	 */
	private void startAnimationSpeaking(String text) {

		long msDelay = getTTsWarmupDelay(text);

		// On fait un thread qui demarre l'animation dans X ms
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				Log.d(TAG, "Request blabla");
				// Charge l'animation XML et lance l'animation
				mainView.startAndroidMouthAnimation();
			}
		}, msDelay);
	}

	/**
	 * Calcule empirique d'un delais de computing
	 * 
	 * @param text
	 * @return
	 */
	private long getTTsWarmupDelay(String text) {
		return text.length() * 20;
	}

	@Override
	public void onUtteranceCompleted(String utId) {

		Log.d(TAG, "UT ENDED Completed : " + utId);

		utteranceQueue.remove(utId);

		if (utteranceQueue.size() == 0) {
			mainView.stopAndroidMouthAnimation();
		}

		if (utteranceQueue.size() == 0) {
			mainView.animateBubble(bubbleDisappearAnim);
		}
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.animation.Animation.AnimationListener#onAnimationStart(android
	 * .view.animation.Animation)
	 */
	@Override
	public void onAnimationStart(Animation a) {

		if (a.equals(bubbleAppearAnim)) {
			bubbleIsOpening = true;
			bubble.setVisibility(View.VISIBLE);
		} else if (a.equals(bubbleDisappearAnim)) {
			bubbleIsClosing = true;
		}

	}

	@Override
	public void onAnimationEnd(Animation a) {

		if (a.equals(bubbleAppearAnim)) {
			bubbleIsFullyDeployed = true;
			bubbleIsOpening = false;
		} else if (a.equals(bubbleDisappearAnim)) {
			bubbleIsFullyDeployed = false;
			bubbleIsClosing = true;
			bubble.setVisibility(View.INVISIBLE);
		}

	}

	@Override
	public void onAnimationRepeat(Animation a) {
	}

	/**
	 * Calcule le temps qui reste pour cette animation
	 * peut être pratique dans certain cas mais pas 
	 * utilisé pour ce projet...
	 * 
	 * @param anim
	 * @return
	 */
	private long getRemainingAnimationTime(Animation anim) {

		long st = anim.getStartTime();
		long dur = anim.getDuration();
		long now = AnimationUtils.currentAnimationTimeMillis();

		Log.d(TAG, "Start time: " + st);
		Log.d(TAG, "Duration time: " + dur);
		Log.d(TAG, "Now time: " + now);

		return (st + dur) - now;
	}

}
