package net.mgsx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.mgsx.gdx.MgdxGame;
import net.mgsx.gdx.dekstop.MGdxDekstopApplication;
import net.mgsx.gdx.scenes.scene2d.StageScreen;
import net.mgsx.gdx.scenes.scene2d.ui.ColorBox;
import net.mgsx.gdx.scenes.scene2d.ui.UI;

public class SkinText extends MgdxGame
{
	public static void main(String[] args) {
		new MGdxDekstopApplication(new SkinText());
	}
	@Override
	public void create() {
		setScreen(new SkinScreen());
	}
	private static class SkinScreen extends StageScreen {
		private float uiScale = 1;
		public SkinScreen() {
			Skin skin = new Skin(Gdx.files.internal("skins/composer-skin.json"));
			Table root = new Table(skin);
			root.setFillParent(true);
			stage.addActor(root);
			root.defaults().pad(10);
			
			Window window = new Window("Default window", skin);
			window.add("Lorem Ipsum").row();
			window.add("Lorem Ipsum").row();
			
			root.add(window);
			root.row();
			
			root.add("Default label");
			root.add(new TextButton("Default button", skin));
			root.add(new TextButton("Toggle button", skin, "toggle"));
			root.add(UI.selector(skin, "item 1", "item 2", "item 3"));
			root.row();
			
			root.add(new Slider(0, 1, .1f, false, skin));
			root.add(new Slider(0, 1, .1f, true, skin));
			
			Tree tree = new Tree<>(skin);
			Node node1 = new Tree.Node(){};
			node1.setActor(new Label("Parent", skin));
			Node node2 = new Tree.Node(){};
			node2.setActor(new Label("Child", skin));
			node1.add(node2);
			tree.add(node1);
			root.add(tree);
			
			root.add(new CheckBox("Check box", skin));
			
			root.row();
			String bigText = "lorem ipsum\nlorem ipsum\nlorem ipsum" + "lorem ipsum\nlorem ipsum\nlorem ipsum" + 
					"lorem ipsum\nlorem ipsum\nlorem ipsum" + "lorem ipsum\nlorem ipsum\nlorem ipsum" +
					"lorem ipsum\nlorem ipsum\nlorem ipsum" + "lorem ipsum\nlorem ipsum\nlorem ipsum";
			ScrollPane sp = new ScrollPane(new Label(bigText, skin), skin);
			root.add(sp).size(100, 100);
			
			root.add(new ColorBox("Emissive color", new Color(Color.CYAN), true, skin));
		}
		@Override
		public void render(float delta) {
			if(Gdx.input.isKeyJustPressed(Input.Keys.E)){
				uiScale/=2;
				((ScreenViewport)viewport).setUnitsPerPixel(uiScale);
				viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			}
			if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
				uiScale*=2;
				((ScreenViewport)viewport).setUnitsPerPixel(uiScale);
				viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			}
			ScreenUtils.clear(Color.DARK_GRAY);
			super.render(delta);
		}
	}
}
