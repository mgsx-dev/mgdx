package net.mgsx.gdx.scenes.scene2d.ui;

import java.util.function.Supplier;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Scaling;

public class ColorBox extends Table
{
	private final Supplier<Color> colorModel;
	private boolean alpha;
	private Image colorPreview;
	private Image alphaPreview;

	public ColorBox(String title, Color colorModel, boolean alpha, Skin skin) {
		this(title, ()->colorModel, alpha, skin);
	}
	/** skin should contains "white pixel" */
	public ColorBox(String title, Supplier<Color> colorModel, boolean alpha, Skin skin) {
		super(skin);
		this.colorModel = colorModel;
		this.alpha = alpha;
		
		Button bt = new Button(skin);

		colorPreview = new Image(skin, "white");
		colorPreview.setScaling(Scaling.stretch);
		bt.add(colorPreview).size(16,12);
		bt.row();
		
		if(alpha){
			alphaPreview = new Image(skin, "white");
			alphaPreview.setScaling(Scaling.stretch);
			bt.add(alphaPreview).size(16, 4);
			bt.row();
		}
		
		
		updatePreview();
		
		add(bt);
		UI.change(bt, e->openDialog(title, e.getStage()));
	}
	private void openDialog(String title, Stage stage){
		UI.dialog(createPicker(), title, getSkin()).show(stage);
	}
	private Actor createPicker() {
		return new ColorPicker(colorModel.get(), alpha, getSkin(), this::updatePreview);
	}
	
	private void updatePreview(){
		Color c = colorModel.get();
		colorPreview.setColor(c.r, c.g, c.b, 1);
		if(alpha){
			alphaPreview.setColor(c.a, c.a, c.a, 1);
		}
		fire(new ChangeEvent());
	}
	
}
