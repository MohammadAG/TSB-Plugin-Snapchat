package com.mohammadag.snapchattintedstatusbarplugin;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!"com.snapchat.android".equals(lpparam.packageName))
			return;

		XposedHelpers.findAndHookMethod("com.snapchat.android.util.SnapchatViewPager",
				lpparam.classLoader, "onPageScrolled", int.class, float.class, int.class,
				new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				ViewGroup pager = (ViewGroup) param.thisObject;
				int position = (Integer) param.args[0];

				int color = -3;
				if (position == 0)
					color = Color.WHITE;
				else if (position == 1)
					color = Color.parseColor("#03a588");
				else if (position == 2)
					color = Color.parseColor("#66000000");
				else if (position == 3)
					color = Color.parseColor("#9b55a0");
				else if (position == 4)
					color = Color.WHITE;

				if (color == -3)
					return;

				StatusBarTintApi.sendColorChangeIntent(color, getIconColorForColor(color),
						-3, -3, pager.getContext());
			}
		});
	}

	public static int getMainColorFromActionBarDrawable(Drawable drawable) throws IllegalArgumentException {
		/* This should fix the bug where a huge part of the ActionBar background is drawn white. */
		Drawable copyDrawable = drawable.getConstantState().newDrawable();

		if (copyDrawable instanceof ColorDrawable) {
			return ((ColorDrawable) drawable).getColor();
		}

		Bitmap bitmap = drawableToBitmap(copyDrawable);
		int pixel = bitmap.getPixel(0, 40);
		int red = Color.red(pixel);
		int blue = Color.blue(pixel);
		int green = Color.green(pixel);
		int alpha = Color.alpha(pixel);
		copyDrawable = null;
		return Color.argb(alpha, red, green, blue);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) throws IllegalArgumentException {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}
		Bitmap bitmap;

		try {
			bitmap = Bitmap.createBitmap(1, 80, Config.ARGB_8888);
			bitmap.setDensity(480);
			Canvas canvas = new Canvas(bitmap); 
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
		} catch (IllegalArgumentException e) {
			throw e;
		}

		return bitmap;
	}

	public static int getIconColorForColor(int color) {
		float hsvMaxValue = 0.7f;
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		float value = hsv[2];

		if (value > hsvMaxValue) {
			return Color.BLACK;
		} else {
			return Color.WHITE;
		}
	}
}
