package com.fuchs.maps;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

// Writes all uncaught exceptions to log file
public class ExceptionHandler implements UncaughtExceptionHandler
{

	String localPath = "/mnt/sdcard/FuchsMaps.crash";
	UncaughtExceptionHandler defHandler;

	static SimpleDateFormat formatter = new SimpleDateFormat();

	public ExceptionHandler(UncaughtExceptionHandler defHandler)
	{
		this.defHandler = defHandler;
	}

	public void uncaughtException(Thread t, Throwable e)
	{
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);

		Date time = new Date();
		printWriter.println("___________");
		printWriter.println();
		printWriter.println(time.toString());
		printWriter.println();
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		printWriter.close();

		writeToFile(stacktrace);

		if (defHandler != null) defHandler.uncaughtException(t, e);
	}

	private void writeToFile(String stacktrace)
	{
		try
		{
			BufferedWriter bos = new BufferedWriter(new FileWriter(localPath, true));
			bos.write(stacktrace);
			bos.flush();
			bos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
