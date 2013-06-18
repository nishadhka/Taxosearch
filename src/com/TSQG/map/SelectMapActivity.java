package com.TSQG.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.ListActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SelectMapActivity extends ListActivity
{
	ArrayAdapter<String> adapter;

	@Override
	protected void onResume()
	{
		File myPath = new File(MyPreferences.getMapFolder(this));
		if (myPath.exists())
		{
			ArrayList<String> files = new ArrayList<String>(Arrays.asList(myPath.list()));
			for (int i = 0; i < files.size(); i++)
			{
				if (!files.get(i).endsWith("sqlitedb"))
				{
					files.remove(i);
					i--;
				}
			}

			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, files);
			setListAdapter(adapter);
		}
		else
		{
			Toast.makeText(this, "Unable to find maps folder, check your settings", Toast.LENGTH_LONG).show();
			finish();
		}

		super.onResume();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		String file = adapter.getItem(position);
		MyPreferences.setMapFile(this, file);

		setResult(RESULT_OK);
		finish();
	}
}
