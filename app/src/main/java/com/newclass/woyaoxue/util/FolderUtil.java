package com.newclass.woyaoxue.util;

import java.io.File;

import android.content.Context;

public class FolderUtil
{
	public static File rootDir(Context context)
	{
		return context.getFilesDir();
	}

}
