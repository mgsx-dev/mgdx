package net.mgsx.gltf.composer.ui;

import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import net.mgsx.gdx.scenes.scene2d.ui.UI;

public class CUI extends UI
{
	public static final Color dynamicLabelColor = new Color(Color.ORANGE);

	public static Actor wrappedLabel(Skin skin, String text) {
		Label label = new Label(text, skin);
		label.setWrap(true);
		return label;
	}
	
	public static Actor dynamicLabel(Skin skin, String title, Consumer<Label> update) {
		Label label = new Label("--", skin);
		Table table = new Table(skin){
			@Override
			public void act(float delta) {
				update.accept(label);
				super.act(delta);
			}
		};
		table.add(title);
		table.add(label).width(60).getActor();
		label.setAlignment(Align.left);
		label.setColor(CUI.dynamicLabelColor);
		table.row();
		return table;
	}
}
