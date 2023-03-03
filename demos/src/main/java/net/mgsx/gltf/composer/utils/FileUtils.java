package net.mgsx.gltf.composer.utils;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class FileUtils {
	public static void browseRecursive(FileHandle directory, Consumer<FileHandle> handler){
		for(FileHandle file : directory.list()){
			if(file.isDirectory()){
				browseRecursive(file, handler);
			}else{
				handler.accept(file);
			}
		}
	}
	public static Array<FileHandle> filterRecursive(FileHandle directory, Predicate<FileHandle> filter){
		Array<FileHandle> files = new Array<>();
		browseRecursive(directory, f->{
			if(filter.test(f)){
				files.add(f);
			}
		});
		return files;
	}
	public static Array<FileHandle> filterRecursive(FileHandle directory, String extension, boolean ignoreCase){
		String extensionLower = extension.toLowerCase();
		return filterRecursive(directory, f->f.extension().toLowerCase().equals(ignoreCase ? extensionLower : extension));
	}
}
