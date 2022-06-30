package net.mgsx.gdx.inputs;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public interface FileDropListener {
	void filesDropped(Array<FileHandle> fileHandles);
}
