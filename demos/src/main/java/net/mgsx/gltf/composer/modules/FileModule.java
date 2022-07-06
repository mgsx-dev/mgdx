package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.UI;

public class FileModule implements GLTFComposerModule
{
	private Table controls;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		controls.add("Drop a model file").row();
		controls.add("Supported files: *.gltf, *.glb").row();
		controls.add().padTop(50).row();
		controls.add("Drop an IBL file").row();
		controls.add("Supported files: *.hdr").row();
	
		return controls;
	}
}
