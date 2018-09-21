package com.liveensure.a.mini.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.liveensure.a.mini.LEApplication;
import com.liveensure.a.mini.R;
import com.liveensure.a.mini.UIForFiniteStateMachine;
import com.liveensure.core.AgentFSM;
import com.liveensure.rest.api.AgentChallengeResponse;
import com.liveensure.util.Log;
import com.liveensure.util.StringHelper;

public class BehaviorChallengeFragment extends BaseChallengeFragment {
	private static final String EXTRA_CHALLENGE = "challengeObject";
	private static final String TAG = BehaviorChallengeFragment.class.getSimpleName();
	AgentChallengeResponse mChallenge;
	private boolean[] mTouchRegionsEnabled;
	private boolean mCaptureComplete;
	private ImageView mBehaviorChallengeGraphic;
	private RelativeLayout mLayout;
	private FrameLayout mLytTouchListener;
	private View touchPane6;
	private View touchPane1;
	private View touchPane2;
	private View touchPane3;
	private View touchPane4;
	private View touchPane5;
	private int[] activeTouchQueue;
	private String mCurrentBehavior;

	public static final BehaviorChallengeFragment newInstance(AgentChallengeResponse chal) {
		BehaviorChallengeFragment f = new BehaviorChallengeFragment();
		Bundle bdl = new Bundle(1);
		bdl.putSerializable(EXTRA_CHALLENGE, chal);
		f.setArguments(bdl);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AgentChallengeResponse chal = (AgentChallengeResponse) getArguments().getSerializable(EXTRA_CHALLENGE);
		if (chal != null) {
			mChallenge = chal;
		}
		activeTouchQueue = new int[2];
		mTouchRegionsEnabled = new boolean[LEApplication.NUM_TOUCH_REGIONS + 1];
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.behavior_challenge_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof UIForFiniteStateMachine) {
			mActivity = activity;
			ui = (UIForFiniteStateMachine) activity;
		} else {
			throw new ClassCastException("Invoking activity for this fragment must implement the UIForFiniteStateMachine interface");
		}
	}

	@Override
	public void onResume() {
		Log.d(TAG, "in onResume()");
		super.onResume();

		mLayout = (RelativeLayout) mActivity.findViewById(R.id.behavior_challenge_fragment_layout);
		mLytTouchListener = (FrameLayout) mActivity.findViewById(R.id.full_screen_touch_listener);
		mBehaviorChallengeGraphic = (ImageView) mActivity.findViewById(R.id.img_behavior_icon);
		touchPane1 = (View) mActivity.findViewById(R.id.behavior_challenge_cell_1);
		touchPane2 = (View) mActivity.findViewById(R.id.behavior_challenge_cell_2);
		touchPane3 = (View) mActivity.findViewById(R.id.behavior_challenge_cell_3);
		touchPane4 = (View) mActivity.findViewById(R.id.behavior_challenge_cell_4);
		touchPane5 = (View) mActivity.findViewById(R.id.behavior_challenge_cell_5);
		touchPane6 = (View) mActivity.findViewById(R.id.behavior_challenge_cell_6);
		float graphicAlpha = (ui.isShowChallengeIndicators()) ? 1.0f : 0f;
		Log.e(TAG, "in behavior challenge, setting graphicAlpha to " + graphicAlpha);

		mBehaviorChallengeGraphic.setAlpha(graphicAlpha);
		setUpTouchRegionListeners();
		fadeInUI();
	}

	@Override
	public void retryChallenge() {
	}

	private void captureComplete() {
		Log.d(TAG, "in captureComplete(), behavior string is " + mCurrentBehavior);
		// capture behavior settings and answer challenge
		String touches = "";
		String newOrientation = String.valueOf(ui.getCurrentOrientation());

		for (int x = 1; x < mTouchRegionsEnabled.length; x++) {
			if (mTouchRegionsEnabled[x]) {
				if (touches.length() > 0)
					touches = touches + "," + String.valueOf(x);
				else
					touches = String.valueOf(x);
			}
		}
		if (touches.length() == 0)
			touches = "0"; // no touches at all == "0"

		String plainAnswer = newOrientation + "::" + touches;
		Log.w(TAG, "Final answer for behavior challenge: " + plainAnswer);

		// pass the answer text to the FSM
		AgentFSM fsm = ui.getAgentFSM();
		// check to make sure we still have a valid session (user could have quit before this code is executed)
		if (fsm == null || fsm.getSession() == null)
			return;
		String hashedAnswer = StringHelper.obscure(fsm.getSession().getSessionToken(), plainAnswer);
		fadeOutUI();
		fsm.makeAnswerChallengeCallWithID(mChallenge.getChallengeID(), hashedAnswer, "2.0");
	}

	private void fadeInUI() {
		Log.e(TAG, "in fadeInUI()");
		mLayout.setAlpha(0f);
		mLayout.setVisibility(View.VISIBLE);
		mLayout.animate().alpha(1.0f).setDuration(200).setListener(null).start();
	}

	private void fadeOutUI() {
		mLayout.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mLayout.setVisibility(View.INVISIBLE);
			}
		});
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setUpTouchRegionListeners() {

		Log.i(TAG, "setting up touch listeners");
		mLytTouchListener.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (mCaptureComplete)
					return true;

				int[] coords = new int[2];
				touchPane2.getLocationOnScreen(coords);
				int tp2left = coords[0];
				touchPane3.getLocationOnScreen(coords);
				int tp3top = coords[1];
				touchPane5.getLocationOnScreen(coords);
				int tp5top = coords[1];
				// find the X and Y values of our region splits
				int vertSplit = tp2left;
				int horizSplit1 = tp3top;
				int horizSplit2 = tp5top;
				int action = (e.getAction() & MotionEvent.ACTION_MASK);

				int pointCount = e.getPointerCount();
				for (int i = 0; i < pointCount; i++) {
					// Log.e(TAG, "working with pointer " + i);
					float touchX = e.getX(i);
					float touchY = e.getY(i);

					int regionAffected = -1;
					if (touchX < vertSplit) {
						if (touchY < horizSplit1) {
							regionAffected = 1;
						} else if (touchY < horizSplit2) {
							regionAffected = 3;
						} else
							regionAffected = 5;
					} else {
						if (touchY < horizSplit1) {
							regionAffected = 2;
						} else if (touchY < horizSplit2) {
							regionAffected = 4;
						} else
							regionAffected = 6;
					}
					if (MotionEvent.ACTION_DOWN == action) {
						// touched this spot
						// Log.i(TAG, "down in region " + regionAffected);
						enqueue(regionAffected);
					}
					if (MotionEvent.ACTION_POINTER_UP == action) {
						// stopped touching at this spot (but still holding one finger down. too bad! still going to consider this done)
						// Log.i(TAG, "up in region " + regionAffected);
						captureComplete();
						// dequeue(regionAffected);
					}
					if (MotionEvent.ACTION_UP == action) {
						// stopped touching at this spot, last finger is finally removed
						// Log.i(TAG, "up in region " + regionAffected);
						captureComplete();
					}
					if (MotionEvent.ACTION_MOVE == action) {
						// stopped touching at this spot
						// Log.i(TAG, "move in region " + regionAffected);
						if (pointCount == 1) {
							// only one finger touching, so clear active queue first
							activeTouchQueue[0] = 0;
							activeTouchQueue[1] = 0;
						}
						enqueue(regionAffected);
					}
					refreshTouchGrid();
				}
				return true;
			}
		});

	}

	protected void dequeue(int i) {
		if (i == 0)
			return;
		if (activeTouchQueue[0] == i) {
			activeTouchQueue[0] = activeTouchQueue[1];
			activeTouchQueue[1] = 0;
		} else if (activeTouchQueue[1] == i) {
			activeTouchQueue[1] = 0;
		}
	}

	protected void enqueue(int i) {
		if (i == 0)
			return;
		if (i == activeTouchQueue[0])
			return;
		activeTouchQueue[1] = activeTouchQueue[0];
		activeTouchQueue[0] = i;
		Log.e(TAG, "after enqueue: " + activeTouchQueue[0] + ", " + activeTouchQueue[1]);
	}

	protected void refreshTouchGrid() {
		Resources res = mActivity.getResources();
		if (activeTouchQueue[0] == 1 || activeTouchQueue[1] == 1) {
			// turn on region 1
			touchPane1.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[1] = true;
		} else {
			// turn off region 1
			touchPane1.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[1] = false;
		}
		if (activeTouchQueue[0] == 2 || activeTouchQueue[1] == 2) {
			// turn on region 2
			touchPane2.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[2] = true;
		} else {
			// turn off region 2
			touchPane2.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[2] = false;
		}
		if (activeTouchQueue[0] == 3 || activeTouchQueue[1] == 3) {
			// turn on region 3
			touchPane3.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[3] = true;
		} else {
			// turn off region 3
			touchPane3.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[3] = false;
		}
		if (activeTouchQueue[0] == 4 || activeTouchQueue[1] == 4) {
			// turn on region 4
			touchPane4.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[4] = true;
		} else {
			// turn off region 4
			touchPane4.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[4] = false;
		}
		if (activeTouchQueue[0] == 5 || activeTouchQueue[1] == 5) {
			// turn on region 5
			touchPane5.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[5] = true;
		} else {
			// turn off region 5
			touchPane5.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[5] = false;
		}
		if (activeTouchQueue[0] == 6 || activeTouchQueue[1] == 6) {
			// turn on region 6
			touchPane6.setBackground(res.getDrawable(R.drawable.stipple_touch));
			mTouchRegionsEnabled[6] = true;
		} else {
			// turn off region 6
			touchPane6.setBackgroundColor(Color.TRANSPARENT);
			mTouchRegionsEnabled[6] = false;
		}

		mCurrentBehavior = "";
		if (activeTouchQueue[0] > 0)
			mCurrentBehavior = "" + activeTouchQueue[0];
		if (activeTouchQueue[1] > 0)
			mCurrentBehavior += "," + activeTouchQueue[1];
	}

}