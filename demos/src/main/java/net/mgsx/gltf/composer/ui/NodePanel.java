package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.graphics.g3d.ModelUtils;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;

public class NodePanel extends Table
{
	public NodePanel(GLTFComposerContext ctx, Node node) {
		super(ctx.skin);
		defaults().pad(UI.DEFAULT_PADDING).growX();
		
		Table table = this;
		
		UI.header(table, "Node: " + node.id);
		
		UI.toggle(table, "Show selected node only", ctx.showSelectedNodeOnly, value->{
			ctx.showSelectedNodeOnly = value;
			updateParts(ctx, node);
		});
		
		// TODO CUI.control("translation", node.translation);
		table.add("Translation").row();
		table.add("x: " + node.translation.x).row();
		table.add("y: " + node.translation.y).row();
		table.add("z: " + node.translation.z).row();
		
		table.add("Rotation").row();
		table.add("x: " + node.rotation.x).row();
		table.add("y: " + node.rotation.y).row();
		table.add("z: " + node.rotation.z).row();
		table.add("w: " + node.rotation.w).row();
		
		table.add("Scale").row();
		table.add("x: " + node.scale.x).row();
		table.add("y: " + node.scale.y).row();
		table.add("z: " + node.scale.z).row();
		
		// update when selection changed (TODO should be done in listener instead)
		if(ctx.showSelectedNodeOnly){
			updateParts(ctx, node);
		}
	}

	private void updateParts(GLTFComposerContext ctx, Node node) {
		ModelUtils.eachNodePartRecusrsive(ctx.scene.modelInstance.nodes, part->part.enabled = !ctx.showSelectedNodeOnly);
		if(ctx.showSelectedNodeOnly) ModelUtils.eachNodePartRecusrsive(node, part->part.enabled = true);
	}
	
	
}
