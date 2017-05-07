package com.hgxx.whiteboard.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;


import com.hgxx.whiteboard.R;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewHelpers {

		private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
	private static float designWidth;
	private static float designHeight;

//	public static final String SIZE_TYPE_WIDTH = "width";
//	public static final String SIZE_TYPE_HEIGHT = "height";


	/**
	 * Generate a value suitable for use in {@link #setId(int)}.
	 * This value will not collide with ID values generated at build time by aapt for R.id.
	 *
	 * @return a generated ID value
	 */
	public static int generateViewId() {
	    for (;;) {
	        final int result = sNextGeneratedId.get();
	        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
	        int newValue = result + 1;
	        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
	        //if current == result(not modifid by other threads), than update it with newValue, and return result
	        if (sNextGeneratedId.compareAndSet(result, newValue)) {
	            return result;
	        }
	    }
	}

	/**
	 * Find a certain type of child views in a viewgroup
	 * @param targetClazz
	 * 		class of the target view type
	 * @param parent
	 * 		where to find target
	 * @param mode
	 * 		structured_deepfirst: result will be a cascaded array with children arrays in it, a
	 * 						ccording to the view tree
	 * 		deepfirst: all targeted children in a flat array, ordered according to  the search order
     * @return
	 * 		Array of targeted children ordered according to the specific mode
     */
	public static final String SEARCH_TYPE_DEEP_FIRST = "deepfirst";
	public static final String SEARCH_TYPE_STRUCTURED_DEEP_FIRST = "structured_deepfirst";

	public static <T>ArrayList<Object> findViews(Class<T> targetClazz, ViewGroup parent, String mode){
		ArrayList<Object> result = new ArrayList<>();
		for(int i=0;i<parent.getChildCount();i++){
			if(targetClazz.isAssignableFrom(parent.getChildAt(i).getClass())){
				T tempObj = targetClazz.cast(parent.getChildAt(i));
				result.add(tempObj);
			}
			else if(parent.getChildAt(i) instanceof ViewGroup){
				if(mode=="structured_deepfirst"){
					result.add(findViews(targetClazz, (ViewGroup)parent.getChildAt(i), mode));
				}
				else if(mode=="deepfirst")
					result.addAll(findViews(targetClazz, (ViewGroup)parent.getChildAt(i), mode));
				}
			}
		return result;
	}


	/**
	 * @param sizeMeasureSpec
	 * 		get size from sizeMeasureSpec, return 0 if unspecified
	 * @param cliSize
	 * 		user set size, 0 means not define size
     * @return
	 *  	int: final size, use specific size or original size or 0
     */
	public static int getViewSize(int sizeMeasureSpec, int cliSize){
		int mSize = MeasureSpec.getMode(sizeMeasureSpec)!=MeasureSpec.UNSPECIFIED?MeasureSpec
				.getSize(sizeMeasureSpec):0;
		int resultSize = cliSize!=0?cliSize:mSize;
		return resultSize;
	}

	/**
	 * @param demensionInDp
	 * 		dimension u want to convert
	 * @param density
	 * 		display density
     * @return
	 * 		int: result pixels converted from dp
     */
	public static int dpToPixels(float demensionInDp, float density){
		return (int)(density*demensionInDp+0.5f);
	}

	/**
	 * Measure child, no need to convert size to MeasureSpec anymore
	 * @param child
	 * 		child to measure
	 * @param width
	 * 		child witdh
	 * @param height
	 * 		child height
     */
	public static void measureChild(View child, int width, int height){
		int msWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int msHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		child.measure(msWidth, msHeight);
	}


	public static int getDisplayWidth(Context context){
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	public static int getDisplayHeight(Context context){
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static int getStatusBarHeight(Context context){
		int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
		return statusBarHeight;
	}


	// action bar height
	public static int getActionBarHeight(Context context){
		int actionBarHeight = 0;
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize }
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
		return actionBarHeight;
	}


	// navigation bar height
	public static int getNavigationBarHeight(Context context){
		int navigationBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
		return navigationBarHeight;
	}


	/**
	 * @param originalSize
	 * 	design size
	 * @param originalWinSize
	 * 	design windowSize
	 * @param winSize
	 * 	actual windowSize
     * @return
     */
	public static int designPx2Dp(Context context, int originalSize, int originalWinSize, int winSize){
		return (int)((originalSize*(float)winSize/(float)originalWinSize)/context.getResources().getDisplayMetrics().density);
	}
    public interface OnNegativeBtnClickListener{
        void onNegativeBtnClick(DialogInterface dialog, int which);
    }

    public interface OnPositiveBtnClickListener{
        void onPositiveBtnClickListener(DialogInterface dialog, int which);
    }

	/**
	 * @param context
	 * @param title
	 * @param message
	 * @param negBtnText
	 * @param onNegativeBtnClickListener
	 * @param posBtnText
	 * @param onPositiveBtnClickListener
     * @param forcePositive
     */
//    public static void showDialog(final Context context, String title, String message, String negBtnText,
//                            final OnNegativeBtnClickListener onNegativeBtnClickListener,
//                            String posBtnText, final OnPositiveBtnClickListener onPositiveBtnClickListener, boolean forcePositive){
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(title);
//        builder.setMessage(message);
//        if(negBtnText!=null&&onNegativeBtnClickListener!=null){
//            builder.setNegativeButton(negBtnText, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
////					dialog.dismiss();
//					onNegativeBtnClickListener.onNegativeBtnClick(dialog, which);
//                }
//            });
//        }
//
//        if(posBtnText!=null&&onPositiveBtnClickListener!=null){
//            builder.setPositiveButton(posBtnText, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    onPositiveBtnClickListener.onPositiveBtnClickListener(dialog, which);
//                }
//            });
//        }
//
//        if(forcePositive){
//            builder.setCancelable(false);
//        }
//        Dialog d = builder.create();
//        d.show();
//    }

	/**
	 * get size according to dimens -> design_width|design_height
	 * @param size size on the design diagram
     * @return
     */
//	public static float getSizeAccordingToDesign(int size){
//		Resources resources = JupiterBusinessApplication.getContext().getResources();
//		DisplayMetrics dm = resources.getDisplayMetrics();
//
//		designWidth = resources.getDimension(R.dimen.design_width);
//		designHeight = resources.getDimension(R.dimen.design_height);
//
//
//		int designRatio = Math.round(designHeight/designWidth);
//		int screenRation = Math.round(dm.heightPixels/dm.widthPixels);
//
//		float ratio = 1;
//		if(designRatio>screenRation){
//			//fit height
//			ratio = dm.heightPixels/designHeight;
//		}
//		else{
//			//fit width
//			ratio = dm.widthPixels/designWidth;
//		}
//		return ratio*size;
//	}
//
//		/**
//	 *
//	 * @param context
//	 * @param title
//	 * @param message
//	 * @param negBtnText
//	 * @param onNegativeBtnClickListener
//	 * @param posBtnText
//	 * @param onPositiveBtnClickListener
//     * @param forcePositive
//     */
//    public static void showDialog(final Context context, String title, String message, String negBtnText,
//                            final OnNegativeBtnClickListener onNegativeBtnClickListener,
//                            String posBtnText, final OnPositiveBtnClickListener onPositiveBtnClickListener, boolean forcePositive){
//		/**
//		 * Context context, String title, String message, String negBtnText,OnNegativeBtnClickListener onNegativeBtnClickListener,
//		 * String posBtnText, OnPositiveBtnClickListener onPositiveBtnClickListener, boolean forcePositive
//		 */
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(title);
//        builder.setMessage(message);
//        if(negBtnText!=null&&onNegativeBtnClickListener!=null){
//            builder.setNegativeButton(negBtnText, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    onNegativeBtnClickListener.onNegativeBtnClick(dialog, which);
//                }
//            });
//        }
//
//        if(posBtnText!=null&&onPositiveBtnClickListener!=null){
//            builder.setPositiveButton(posBtnText, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    onPositiveBtnClickListener.onPositiveBtnClickListener(dialog, which);
//                }
//            });
//        }
//
//        if(forcePositive){
//            builder.setCancelable(false);
//        }
//        Dialog d = builder.create();
//		d.setCancelable(false);
//		d.setCanceledOnTouchOutside(false);
//        d.show();
//
//    }

}
