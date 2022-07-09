package net.mgsx.gdx.scenes.scene2d.ui;

import java.util.function.Consumer;
import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

// TODO cleanup
public class UI {
	
	public enum ControlScale{
		LIN, LOG
	}
	public static final float DEFAULT_PADDING = 4;
	
	public static <T extends Actor> T change(T actor, Consumer<ChangeEvent> handler){
		actor.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				handler.accept(event);
			}
		});
		return actor;
	}
	public static <T> void select(SelectBox<T> selectBox, Consumer<T> item) {
		change(selectBox, event->item.accept(selectBox.getSelected()));
	}
	public static <T extends Slider> T changeCompleted(T slider, Consumer<ChangeEvent> handler){
		slider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(!slider.isDragging()) handler.accept(event);
			}
		});
		return slider;
	}
	public static void toggle(Table t, String text, boolean checked, Consumer<Boolean> handler){
		t.add(toggle(t.getSkin(), text, checked, handler)).row();
	}
	
	public static TextButton toggle(Skin skin, String text, boolean checked, Consumer<Boolean> handler){
		CheckBox bt = new CheckBox(text, skin);
		bt.setChecked(checked);
		change(bt, event->handler.accept(bt.isChecked()));
		return bt;
	}
	public static TextButton trig(Skin skin, String text, Runnable handler){
		TextButton bt = new TextButton(text, skin);
		change(bt, event->handler.run());
		return bt;
	}
	public static TextButton primary(Skin skin, String text, Runnable handler){
		TextButton bt = new TextButton(text, skin, "primary");
		change(bt, event->handler.run());
		return bt;
	}

	public static Table colored(Cell<? extends Actor> cell, Color color) {
		cell.getActor().setColor(color);
		return cell.getTable();
	}
	public static Actor clicked(Actor a, Consumer<Event> e) {
		a.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				e.accept(event);
			}
		});
		return a;
	}
	public static Actor clickedOnce(Actor a, Consumer<Event> e) {
		a.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				e.accept(event);
				a.removeListener(this);
			}
		});
		return a;
	}
	public static <T> SelectBox<T> selector(Skin skin, Array<T> items, Object defaultItem, Function<T, String> labeler, Consumer<T> handler) {
		return selector(skin, items, defaultItem, labeler, handler, false);
	}
	public static <T> SelectBox<T> selector(Skin skin, Array<T> items, Object defaultItem, Function<T, String> labeler, Consumer<T> handler, boolean prependIndex) {
		SelectBox<T> selectBox = new SelectBox<T>(skin){
			@Override
			protected String toString(T item) {
				String s = labeler.apply(item);
				if(s == null) s = "";
				if(prependIndex){
					s = getItems().indexOf(item, true) + " - " + s;
				}
				return s;
			}
		};
		selectBox.setItems(items);
		change(selectBox, event->handler.accept(selectBox.getSelected()));
		if(defaultItem != null) selectBox.setSelected((T)defaultItem);
		
		return selectBox;
	}
	public static SelectBox<String> selector(Skin skin, String[] items, int defaultItem, Consumer<Integer> handler) {
		SelectBox<String> selectBox = new SelectBox<String>(skin);
		selectBox.setItems(new Array<String>(items));
		change(selectBox, event->handler.accept(selectBox.getSelectedIndex()));
		if(defaultItem >= 0) selectBox.setSelectedIndex(defaultItem);
		return selectBox;
	}
	public static SelectBox<String> selector(Skin skin, String ...items) {
		SelectBox<String> selectBox = new SelectBox<String>(skin);
		selectBox.setItems(items);
		return selectBox;
	}
	public static Slider slider(float min, float max, float stepSize, boolean vertical, Skin skin, Float value, Consumer<Float> change) {
		return slider(min, max, stepSize, vertical, skin, value, change, null);
	}
	public static Slider slider(float min, float max, float stepSize, boolean vertical, Skin skin, Float value, Consumer<Float> change, Consumer<Float> complete) {
		Slider slider = new Slider(min, max, stepSize, vertical, skin);
		if(value != null) slider.setValue(value);
		if(change != null) change(slider, e->{
			e.stop();
			//e.cancel();
			change.accept(slider.getValue());
		});
		if(complete != null) changeCompleted(slider, e->complete.accept(slider.getValue()));
		return slider;
	}
	public static Slider slider(Table table, String name, float min, float max, float val, Consumer<Float> callback) {
		return slider(table, name, min, max, val, ControlScale.LIN, callback);
	}
	
	public static Slider sliderTable(Table table, String name, float min, float max, float value, ControlScale scale, Consumer<Float> setter) {
		float width = 200;
		float stepSize = (max - min) / width;
		
		float sMin = scale == ControlScale.LOG ? (float)Math.log10(min) : min;
		float sMax = scale == ControlScale.LOG ? (float)Math.log10(max) : max;
		float sVal = scale == ControlScale.LOG ? (float)Math.log10(value) : value;
		float sStep = scale == ControlScale.LOG ? .01f : stepSize;

		Label number = new Label(round(value, sStep), table.getSkin());
		
		Slider slider = slider(sMin, sMax, sStep, false, table.getSkin(), sVal, val->{
			float nVal = scale == ControlScale.LOG ? (float)Math.pow(10, val) : val;
			setter.accept(nVal);
			number.setText(round(nVal, sStep));
		});
		
		table.add(name).left();
		table.add(slider).width(width);
		table.add(number).width(50);
		
		table.row();
		
		return slider;
	}
	public static Slider slider(Table table, String name, float min, float max, float val, ControlScale scale, Consumer<Float> callback) {
		Table t = new Table(table.getSkin());
		t.defaults().pad(2);
		Slider slider = sliderTable(t, name, min, max, val, scale, callback);
		table.add(t).fill();
		table.row();
		return slider;
	}
	public static Slider slideri(Table table, String name, int min, int max, int value, Consumer<Integer> callback) {
		float width = 200;
		Label number = new Label(String.valueOf(value), table.getSkin());
		Slider slider = slider((float)min, (float)max, 1f, false, table.getSkin(), (float)value, val->{
			int ival = MathUtils.round(val);
			callback.accept(ival);
			number.setText(ival);
		});
		Table t = new Table(table.getSkin());
		t.defaults().pad(2);
		
		t.add(name).left();
		t.add(slider).width(width);
		t.add(number).width(50);
		
		table.add(t).fill();
		table.row();
		
		return slider;
	}
	private static String round(float value, float steps){
		int digits = -MathUtils.round((float)Math.log10(steps));
		if(digits == 0){
			return String.valueOf(MathUtils.round(value));
		}
		float factor = (float)Math.pow(10, digits);
		float adj = MathUtils.round(value * factor) / factor;
		return String.valueOf(adj);
	}
	public static void popup(Stage stage, Skin skin, String title, String message) {
		Dialog dialog = new Dialog(title, skin, "dialog");
		Table t = dialog.getContentTable();
		t.defaults().pad(DEFAULT_PADDING);
		t.add(message).row();
		t.add(trig(skin, "OK", ()->dialog.hide()));
		dialog.show(stage);
	}
	public static void header(Table table, String text) {
		table.add(new Label(text, table.getSkin(), "section")).growX().row();
	}
	public static Table table(Skin skin) {
		Table t = new Table(skin);
		t.defaults().pad(DEFAULT_PADDING);
		return t;
	}
	public static class Frame extends Table {

		private Table titleTable;
		private Table contentTable;
		private Table client;

		public Frame(Actor title, Skin skin) {
			super(skin);
			titleTable = new Table(skin);
			if(title != null) titleTable.add(title);
			
			Table titleLeft = new Table(skin);
			titleLeft.setBackground("frame-top-left");
			
			Table titleRight = new Table(skin);
			titleRight.setBackground("frame-top-right");
			
			Table headerTable = new Table(skin);
			
			contentTable = new Table(skin);
			headerTable.add(titleLeft).bottom();
			headerTable.add(titleTable).pad(4);
			headerTable.add(titleRight).growX().bottom();
			
			client = new Table(skin);
			client.setBackground("frame-bottom");
			client.add(contentTable).grow();
			
			add(headerTable).growX().row();
			add(client).grow().row();
		}
		public Table getContentTable(){
			return contentTable;
		}
		public void showContent(boolean enabled) {
			client.clear();
			if(enabled) client.add(contentTable).grow();
			// else client.add("a").grow();
		}
	}
	
	public static Frame frame(String title, Skin skin) {
		Label label = new Label(title, skin);
		label.setColor(Color.LIGHT_GRAY);
		return new Frame(label, skin);
	}
	public static Frame frameToggle(String title, Skin skin, boolean checked, Consumer<Boolean> callback) {
		boolean collapseMode = true; // TODO option ?
		Frame frame = new Frame(null, skin);
		Actor bt = toggle(skin, title, checked, v->{
			callback.accept(v);
			if(collapseMode)
				frame.showContent(v);
			else
				enableRecursive(frame.contentTable, v);
		});
		if(collapseMode)
			frame.showContent(checked);
		else
			enableRecursive(frame.contentTable, checked);
		
		frame.titleTable.add(bt);
		frame.getContentTable().defaults().pad(DEFAULT_PADDING);
		return frame;
	}
	public static void enableRecursive(Actor actor, boolean enabled) {
		if(actor instanceof Disableable){
			((Disableable) actor).setDisabled(!enabled);
		}
		if(actor instanceof Group){
			Group g = (Group)actor;
			for(Actor child : g.getChildren()){
				enableRecursive(child, enabled);
			}
		}
	}
	public static Actor colorBox(Skin skin, Color color, boolean alpha, Consumer<Float> callback) {
		Image img = new Image(skin, "white");
		img.setScaling(Scaling.fill);
		
		Button bt = new Button(skin);
		
		bt.add(img).size(32);
		
		img.setColor(new Color(color.r, color.g, color.b, 1));
		
		Table t = new Table(skin);
		t.add(bt);
		
		return t;
	}
	public static Dialog dialog(Actor content, String title, Skin skin) {
		Dialog dialog = new Dialog(title, skin, "dialog");
		dialog.getContentTable().add(content).row();
		// dialog.getContentTable().add(trig(skin, "Close", ()->dialog.hide()));
		dialog.getTitleTable().add(UI.change(new Button(skin), e->dialog.hide())).pad(0).size(16, 16);
		
		return dialog;
	}
	
	
}