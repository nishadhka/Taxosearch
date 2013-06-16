package com.fuchs.maps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class DialogUtil
{
	
	// Creates a simple custom dialog with an EditText
	protected static AlertDialog createInputDialog(Context context, String title, String defualtText,
			DialogInterface.OnClickListener onPositive, DialogInterface.OnClickListener onNegative, Object tag)
	{
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.prompt, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(promptsView);

		final LinearLayout lin = (LinearLayout) promptsView.findViewById(R.prom.layout);
		lin.setTag(tag);

		final TextView titleText = (TextView) promptsView.findViewById(R.prom.title);
		titleText.setText(title);

		final EditText userInput = (EditText) promptsView.findViewById(R.prom.input);
		userInput.setText(defualtText);

		alertDialogBuilder.setCancelable(false).setPositiveButton("OK", onPositive).setNegativeButton("Cancel", onNegative);

		return alertDialogBuilder.create();

	}

	
}
