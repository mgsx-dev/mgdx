package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.mgsx.gdx.scenes.scene2d.ui.UI;

public class CUI extends UI
{
	public static final Color dynamicLabelColor = new Color(Color.ORANGE);

	public static Actor wrappedLabel(Skin skin, String text) {
		Label label = new Label(text, skin);
		label.setWrap(true);
		return label;
	}
}
