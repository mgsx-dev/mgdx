package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.GL30;

import net.mgsx.gdx.Mgdx;

public class MgdxLwjgl3Application extends Lwjgl3Application
{

	public MgdxLwjgl3Application(ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		super(listener, config);
	}

	public MgdxLwjgl3Application(ApplicationListener listener) {
		super(listener);
	}
	
	@Override
	void createWindow(Lwjgl3Window window, Lwjgl3ApplicationConfiguration config, long sharedContext) {
		super.createWindow(window, config, sharedContext);
		
		GL30 gl30 = null;
		if(Mgdx.glMax != null) gl30 = Mgdx.glMax;
		else if(Mgdx.gl32 != null) gl30 = Mgdx.gl32;
		else if(Mgdx.gl31 != null) gl30 = Mgdx.gl31;
		if(gl30 != null){
			window.getGraphics().setGL30(gl30);
			window.getGraphics().setGL30(gl30);
		}
	}

}
