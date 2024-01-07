package com.flansmod.client;

import com.flansmod.common.ContentManager;
import com.flansmod.common.FlansMod;
import net.minecraftforge.fml.common.ICrashCallable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FlansCrash implements ICrashCallable {
	@Override
	public String getLabel() {
		return "Flan's Mod Content Packs";
	}

	@Override
	public String call() throws Exception {
		ClassLoader classloader = (net.minecraft.server.MinecraftServer.class).getClassLoader();
		Method method = (java.net.URLClassLoader.class).getDeclaredMethod("addURL", java.net.URL.class);
		method.setAccessible(true);

		List<File> contentPacks = getContentList(method, classloader);
		StringBuilder builder = new StringBuilder();
		for(File file: contentPacks) {
			builder.append("\n").append(file.getName()).append(" (filepath: ").append(file.getAbsolutePath()).append(")");
		}
		return builder.toString();
	}

	public List<File> getContentList(Method method, ClassLoader classloader) {
		List<File> contentPacks = new ArrayList<File>();
		for (File file : FlansMod.flanDir.listFiles()) {
			//Load folders and valid zip files
			if (file.isDirectory() || Pattern.compile("(.+)\\.(zip|jar)$").matcher(file.getName()).matches()) {
				//Add the directory to the content pack list
				contentPacks.add(file);
			}
		}
		return contentPacks;
	}
}
