package net.mgsx.io;

import java.util.function.Consumer;
import java.util.function.Function;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NativeFileDialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class NFDFileSelector extends FileSelector {

	private static String defaultPath(){
		return defaultPath("");
	}
	private static String defaultPath(String name){
		return defaultPath(Gdx.files.local(name));
	}
	
	private static String defaultPath(FileHandle file){
		String path = file.file().getAbsolutePath();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
        	path = path.replace("/", "\\");
        }
        return path;
	}
	
	private static void dialog(Consumer<FileHandle> handler, Function<PointerBuffer, Integer> dialog){
		PointerBuffer pathPointer = MemoryUtil.memAllocPointer(1);
		try {
            int status = dialog.apply(pathPointer);
            if (status == NativeFileDialog.NFD_OKAY) {
            	String folder = pathPointer.getStringUTF8(0);
            	NativeFileDialog.nNFD_Free(pathPointer.get(0));
            	FileHandle file = Gdx.files.absolute(folder);
            	handler.accept(file);
            }
        }  finally {
        	MemoryUtil.memFree(pathPointer);
        }
	}
	private String formatFilters(String ...exts){
		String filters = "";
		for(int i=0 ; i<exts.length ; i++) filters = i == 0 ? exts[i] : "," + exts[i];
		return filters;
	}
	
	@Override
	public void open(Consumer<FileHandle> handler, String ...exts) {
		dialog(handler, pathPointer->NativeFileDialog.NFD_OpenDialog(formatFilters(exts), defaultPath(), pathPointer));
	}

	@Override
	public void save(Consumer<FileHandle> handler, String defaultName, String ...exts) {
		dialog(handler, pathPointer->NativeFileDialog.NFD_SaveDialog(formatFilters(exts), defaultPath(defaultName), pathPointer));
	}

	@Override
	public void selectFolder(Consumer<FileHandle> handler) {
		dialog(handler, pathPointer->NativeFileDialog.NFD_PickFolder(defaultPath(), pathPointer));
	}

}
