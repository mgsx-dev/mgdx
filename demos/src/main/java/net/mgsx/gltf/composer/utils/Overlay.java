package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gdx.graphics.g3d.ModelUtils;
import net.mgsx.gltf.data.scene.GLTFSkin;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Overlay {

	// TODO use a set instead to avoid duplicated ?
	private Array<Node> bones = new Array<Node>();
	private final Vector3 bonePos = new Vector3(), parentPos = new Vector3();
	public boolean displayAxis = true, displayBox = true, displayParenting = true;
	public boolean displayEnabled;

	public void setScene(SceneAsset asset, Scene scene) {
			
		bones.clear();
		if(scene != null){
			if(asset != null && asset.data != null && asset.data.skins != null){
				// TODO use GLTF skin data to avoid redundent bones ?
				for(GLTFSkin glSkin : asset.data.skins){
					String name = glSkin.name;
					Node root = scene.modelInstance.getNode(name);
					if(root != null){
						
					}else{
						System.err.println("no node found for skin " + name); // TODO possible case ?
					}
				}
				ModelUtils.eachNodePartRecusrsive(scene.modelInstance.nodes, part->{
					if(part.bones != null){
						for(Entry<Node, Matrix4> entry : part.invBoneBindTransforms){
							Node bone = entry.key;
							bones.add(bone);
						}
					}
				});
			}
		}
	}
	
	public void render(ShapeRenderer shapes, Camera camera, Vector3 target){
		if(displayEnabled && bones.size > 0){
			float s = camera.position.dst(target) / 30f;
			float bs = s / 3;
			shapes.setProjectionMatrix(camera.combined);
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

	public boolean hasBones() {
		return bones.size > 0;
	}

}
