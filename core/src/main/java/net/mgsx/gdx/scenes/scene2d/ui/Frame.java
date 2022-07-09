package net.mgsx.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Frame extends Table {

	public static class FrameStyle {
		public Drawable headerBackgroundLeft, headerBackgroundRight, bodyBackground;
	}
	
	private Table titleTable;
	private Table contentTable;
	private Table client;

	public Frame(Actor title, Skin skin) {
		this(title, skin.get(FrameStyle.class));
	}
	public Frame(Actor title, Skin skin, String styleName) {
		this(title, skin.get(styleName, FrameStyle.class));
	}
	public Frame(Actor title, FrameStyle style) {
		titleTable = new Table();
		if(title != null) titleTable.add(title);
		
		Table titleLeft = new Table();
		titleLeft.setBackground(style.headerBackgroundLeft);
		
		Table titleRight = new Table();
		titleRight.setBackground(style.headerBackgroundRight);
		
		Table headerTable = new Table();
		
		contentTable = new Table();
		headerTable.add(titleLeft).bottom();
		headerTable.add(titleTable).pad(4);
		headerTable.add(titleRight).growX().bottom();
		
		client = new Table();
		client.setBackground(style.bodyBackground);
		client.add(contentTable).grow();
		
		add(headerTable).growX().row();
		add(client).grow().row();
	}
	public Table getContentTable(){
		return contentTable;
	}
	public Table getTitleTable() {
		return titleTable;
	}
	public void showContent(boolean enabled) {
		client.clear();
		if(enabled) client.add(contentTable).grow();
	}
}