package net.mgsx.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class TabPane extends Table
{
	public static class TabPaneStyle {
		public Drawable panesBackground;
		public Drawable tabsBackground;
		public TextButtonStyle tabButtonStyle;
	}
	
	private Stack stack;
	private Table tabs;
	private ButtonGroup<Button> group;
	private Actor defaultPane;
	private Table extraTabs;
	private TabPaneStyle style;

	public TabPane(TabPaneStyle style) {
		super();
		this.style = style;
		
		tabs = new Table();
		stack = new Stack();
		group = new ButtonGroup<Button>();
		
		Table backTable = new Table();
		if(style.panesBackground != null){
			backTable.setBackground(style.panesBackground);
		}
		
		Table tabsBack = new Table();
		if(style.tabsBackground != null){
			tabsBack.setBackground(style.tabsBackground);
		}
		
		
		add(tabsBack).fillX();
		tabsBack.add(tabs).bottom();
		tabsBack.add(extraTabs = new Table(getSkin())).expandX().right().padBottom(4);
		row();
		add(backTable).grow();
		backTable.add(stack).expand().top();
	}
	public Table getExtraTabs() {
		return extraTabs;
	}
	public Button addPane(String name, Actor content){
		TextButton bt = new TextButton(name, style.tabButtonStyle);
		addPane(bt, content);
		return bt;
	}
	public void addPane(Button tab, Actor content){
		group.setMinCheckCount(0);
		tabs.add(tab);
		
		Table t = new Table();
		t.add(content).expandY().top().growX();
		
		stack.add(t);
		t.setVisible(false);
		group.add(tab);
		group.setMinCheckCount(1);
		tab.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(tab.isChecked()) showPane(group.getCheckedIndex());
			}
		});
	}
	
	public void defaultPane(Actor actor){
		defaultPane = actor;
		if(group.getChecked() == null){
			stack.add(actor);
		}
	}

	private void showPane(int index) {
		if(defaultPane != null) defaultPane.remove();
		for(Actor c : stack.getChildren()){
			c.setVisible(index < stack.getChildren().size && c == stack.getChild(index));
		}
	}

	public void setCurrentIndex(int index){
		group.getButtons().get(index).setChecked(true);
		showPane(index); // force show pane if already the current index.
	}
	public int getCurrentIndex() {
		return group.getCheckedIndex();
	}
	
}
