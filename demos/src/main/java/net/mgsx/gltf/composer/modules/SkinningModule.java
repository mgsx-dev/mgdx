package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gdx.graphics.g3d.ModelUtils;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.data.scene.GLTFSkin;

public class SkinningModule implements GLTFComposerModule
{
	private Table controls;
	private Array<Node> bones = new Array<Node>();
	private final Vector3 bonePos = new Vector3(), parentPos = new Vector3();
	private boolean displayAxis, displayBox, displayParenting;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		updateUI(ctx, skin);
		return controls;
	}

	private void updateUI(GLTFComposerContext ctx, Skin skin) {
		controls.clear();
		bones.clear();
		if(ctx.scene != null){
			if(ctx.asset != null && ctx.asset.data != null && ctx.asset.data.skins != null){
				for(GLTFSkin glSkin : ctx.asset.data.skins){
					String name = glSkin.name;
					Node root = ctx.scene.modelInstance.getNode(name);
					if(root != null){
						
					}else{
						System.err.println("no node found for skin " + name); // TODO possible case ?
					}
				}
				ModelUtils.eachNodePartRecusrsive(ctx.scene.modelInstance.nodes, part->{
					if(part.bones != null){
						for(Entry<Node, Matrix4> entry : part.invBoneBindTransforms){
							Node bone = entry.key;
							bones.add(bone);
						}
					}
				});
				
				controls.add("Bones: " + bones.size).row();
				
				
				UI.toggle(controls, "Display boxes", displayBox, v->displayBox=v);
				UI.toggle(controls, "Display axis", displayAxis, v->displayAxis=v);
				UI.toggle(controls, "Display parenting", displayParenting, v->displayParenting=v);
				
			}else{
				
				controls.add("no skeleton found");
			}
			
			
		}else{
			controls.add("no model loaded");
		}
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		if(ctx.sceneJustChanged){
			updateUI(ctx, ctx.skin);
		}
	}
	
	@Override
	public void renderOverlay(GLTFComposerContext ctx, ShapeRenderer shapes) {
		if((displayBox || displayAxis) && bones.size > 0){
			float s = ctx.cameraManager.getCamera().position.dst(ctx.cameraManager.getPerspectiveTarget()) / 30f;
			float bs = s / 3;
			shapes.setProjectionMatrix(ctx.cameraManager.getCamera().combined);
			shapes.begin(ShapeType.Line);
			for(int i=0 ; i<bones.size ; i++){
				Node bone = bones.get(i);
				bonePos.setZero().mul(bone.globalTransform);
				bone.globalTransform.getTranslation(bonePos);
				if(displayBox){
					shapes.setColor(Color.YELLOW);
					shapes.box(bonePos.x-bs/2, bonePos.y-bs/2, bonePos.z+bs/2, bs,bs,bs);
					shapes.setColor(Color.WHITE);
				}
				
				if(displayAxis){
					parentPos.set(1,0,0).mul(bone.globalTransform).sub(bonePos).nor().scl(s).add(bonePos);
					shapes.line(bonePos.x, bonePos.y, bonePos.z, parentPos.x, parentPos.y, parentPos.z, Color.RED, Color.RED);
					
					parentPos.set(0,1,0).mul(bone.globalTransform).sub(bonePos).nor().scl(s).add(bonePos);
					shapes.line(bonePos.x, bonePos.y, bonePos.z, parentPos.x, parentPos.y, parentPos.z, Color.GREEN, Color.GREEN);
					
					parentPos.set(0,0,1).mul(bone.globalTransform).sub(bonePos).nor().scl(s).add(bonePos);
					shapes.line(bonePos.x, bonePos.y, bonePos.z, parentPos.x, parentPos.y, parentPos.z, Color.BLUE, Color.BLUE);
				}
				
				if(displayParenting && bone.hasParent()){
					Node parent = bone.getParent();
					if(bones.contains(parent, true)){
						parent.globalTransform.getTranslation(parentPos);
						shapes.line(bonePos.x, bonePos.y, bonePos.z, parentPos.x, parentPos.y, parentPos.z);
					}
				}
				
			}
			shapes.end();
		}
	}
	
}