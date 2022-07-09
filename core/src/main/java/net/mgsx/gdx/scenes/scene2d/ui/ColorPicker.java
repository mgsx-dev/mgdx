package net.mgsx.gdx.scenes.scene2d.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;

public class ColorPicker extends Table {

	private Color colorModel;
	private final float [] hsvModel = new float[3];
	
	private enum Range {
		LINEAR_UNIT, ANGLE_360
	}
	
	private class ColorVector {
		final Slider slider;
		final Supplier<Float> getter;
		public ColorVector(Slider slider, Supplier<Float> getter) {
			super();
			this.slider = slider;
			this.getter = getter;
		}
	}
	
	private final Array<ColorVector> colorVectors = new Array<ColorVector>();
	private Image colorPreview;
	private Runnable callback;
	private Image alphaPreview;

	public ColorPicker(Color colorModel, boolean alpha, Skin skin) {
		this(colorModel, alpha, skin, null);
	}
	public ColorPicker(Color colorModel, boolean alpha, Skin skin, Runnable callback) {
		super(skin);
		this.callback = callback;
		defaults().pad(UI.DEFAULT_PADDING);
		this.colorModel = colorModel;
		colorModel.toHsv(hsvModel);

		TabPane tabPane = new TabPane(skin, "toggle");
		
		tabPane.addPane("HSV", hsvPane());
		tabPane.addPane("RGB", rgbPane());
		tabPane.setCurrentIndex(0);
		
		colorPreview = new Image(skin, "white");
		colorPreview.setColor(colorModel.r, colorModel.g, colorModel.b, 1);

		add(colorPreview).width(64).fillY();
		add(tabPane);
		row();
		
		if(alpha){
			alphaPreview = new Image(skin, "white");
			alphaPreview.setColor(colorModel.a, colorModel.a, colorModel.a, 1);
			add(alphaPreview).width(64).fill();
			add(alphaPane());
			row();
		}
	}

	private Actor alphaPane() {
		Table t = UI.table(getSkin());
		addColorVector(t, "A", ()->colorModel.a, v->colorModel.a=v, this::alphaChanged, Range.LINEAR_UNIT);
		return t;
	}

	private Actor rgbPane() {
		Table t = UI.table(getSkin());
		addColorVector(t, "R", ()->colorModel.r, v->colorModel.r=v, this::rgbChanged, Range.LINEAR_UNIT);
		addColorVector(t, "G", ()->colorModel.g, v->colorModel.g=v, this::rgbChanged, Range.LINEAR_UNIT);
		addColorVector(t, "B", ()->colorModel.b, v->colorModel.b=v, this::rgbChanged, Range.LINEAR_UNIT);
		return t;
	}
	private Actor hsvPane() {
		Table t = UI.table(getSkin());
		addColorVector(t, "H", ()->hsvModel[0], v->hsvModel[0]=v, this::hsvChanged, Range.ANGLE_360);
		addColorVector(t, "S", ()->hsvModel[1], v->hsvModel[1]=v, this::hsvChanged, Range.LINEAR_UNIT);
		addColorVector(t, "V", ()->hsvModel[2], v->hsvModel[2]=v, this::hsvChanged, Range.LINEAR_UNIT);
		return t;
	}
	
	private void rgbChanged(){
		colorModel.toHsv(hsvModel);
	}
	private void hsvChanged(){
		colorModel.fromHsv(hsvModel);
	}
	private void alphaChanged(){
		alphaPreview.setColor(colorModel.a, colorModel.a, colorModel.a, 1);
	}
	
	private void addColorVector(Table table, String name, Supplier<Float> getter, Consumer<Float> setter, Runnable updater, Range range) {
		Slider slider = UI.sliderTable(table, name, 0f, range == Range.ANGLE_360 ? 360 : 1f, getter.get(), ControlScale.LIN, v->{
			setter.accept(v);
			updater.run();
			updateColorVectors();
		});
		colorVectors.add(new ColorVector(slider, getter));
	}

	private void updateColorVectors() {
		for(ColorVector cv : colorVectors){
			cv.slider.setProgrammaticChangeEvents(false);
			cv.slider.setValue(cv.getter.get());
			cv.slider.setProgrammaticChangeEvents(true);
		}
		colorPreview.setColor(colorModel.r, colorModel.g, colorModel.b, 1);
		if(callback != null) callback.run();
	}
	
	
	
}
