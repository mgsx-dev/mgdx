package net.mgsx.gltf.composer.utils;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;

// TODO cleanup
public class UI {
	
	public enum ControlScale{
		LIN, LOG
	}
	
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
		TextButton bt = new TextButton(text, skin, "toggle");
		bt.setChecked(checked);
		change(bt, event->handler.accept(bt.isChecked()));
		return bt;
	}
	public static TextButton trig(Skin skin, String text, Runnable handler){
		TextButton bt = new TextButton(text, skin);
		change(bt, event->handler.run());
		return bt;
	}

	public static void dialog(Stage stage, Skin skin, String title, String message, IOException e) {
		dialog(stage, skin, title, message + "\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
	}
	public static Dialog dialog(Stage stage, Skin skin, String title, String message) {
		Dialog d = new Dialog(title, skin);
		d.pad(130);
		d.text(message);
		d.button("OK");
		d.pack();
		d.show(stage);
		return d;
	}
	/**
	 * create empty dialog for custom content, caller is responsible for packing and showing
	 * @param stage
	 * @param skin
	 * @param title
	 * @return
	 */
	public static Dialog dialogCustom(Stage stage, Skin skin, String title) {
		Dialog d = new Dialog(title, skin);
		d.pad(130);
		return d;
	}
	public static Dialog dialogEmpty(Stage stage, Skin skin, String title, String message) {
		Dialog d = new Dialog(title, skin);
		d.pad(130);
		d.text(message);
		d.pack();
		d.show(stage);
		return d;
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
		if(change != null) change(slider, e->{
			e.stop();
			//e.cancel();
			change.accept(slider.getValue());
		});
		if(complete != null) changeCompleted(slider, e->complete.accept(slider.getValue()));
		if(value != null) slider.setValue(value);
		return slider;
	}
	public static void slider(Table table, String name, float min, float max, float val, Consumer<Float> callback) {
		slider(table, name, min, max, val, ControlScale.LIN, callback);
	}
	public static void slider(Table table, String name, float min, float max, float val, ControlScale scale, Consumer<Float> callback) {
		float width = 200;
		float stepSize = (max - min) / width;
		
		float sMin = scale == ControlScale.LOG ? (float)Math.log10(min) : min;
		float sMax = scale == ControlScale.LOG ? (float)Math.log10(max) : max;
		float sVal = scale == ControlScale.LOG ? (float)Math.log10(val) : val;
		float sStep = scale == ControlScale.LOG ? .01f : stepSize;

		Label number = new Label(round(sVal, sStep), table.getSkin());
		
		Slider slider = slider(sMin, sMax, sStep, false, table.getSkin(), sVal, value->{
			float nVal = scale == ControlScale.LOG ? (float)Math.pow(10, value) : value;
			callback.accept(nVal);
			number.setText(round(nVal, sStep));
		});
		Table t = new Table(table.getSkin());
		t.defaults().pad(2);
		
		t.add(name).left();
		t.add(slider).width(width);
		t.add(number).width(50);
		
		table.add(t).fill();
		table.row();
	}
	public static void slideri(Table table, String name, int min, int max, int value, Consumer<Integer> callback) {
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
	public static ObjectMap<String, Texture> textures = new ObjectMap<String, Texture>();
	public static Actor icon(String path, int x, int y, int w, int h) {
		Image img = new Image(iconRegion(path, x, y, w, h));
		img.setScaling(Scaling.none);
		return img;
	}
	public static Drawable iconDrawable(String path, int x, int y, int w, int h) {
		return new TextureRegionDrawable(iconRegion(path, x, y, w, h));
	}
	public static TextureRegion iconRegion(String path, int x, int y, int w, int h) {
		Texture texture = textures.get(path);
		if(texture == null) textures.put(path, texture = new Texture(path));
		return new TextureRegion(texture, x, y, w, h);
	}
	
	
}