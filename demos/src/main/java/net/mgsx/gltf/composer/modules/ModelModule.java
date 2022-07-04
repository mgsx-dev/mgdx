package net.mgsx.gltf.composer.modules;

import java.util.function.Supplier;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.ui.AnimationPanel;
import net.mgsx.gltf.composer.ui.MaterialPanel;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.scene3d.animation.NodeAnimationHack;
import net.mgsx.gltf.scene3d.model.WeightVector;
import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * have a UI to control animations, skelton overlay, etc...
 * 
 * @author mgsx
 *
 */
public class ModelModule implements GLTFComposerModule
{
	Table controls;
	
	private class ModelNode extends Tree.Node<ModelNode, ModelNode, Actor> {
		protected ModelNode addWrapper(String text, Skin skin){
			return addWrapper(text, true, skin, null);
		}
		protected ModelNode addWrapper(String text, Skin skin, Supplier<Actor> pane){
			return addWrapper(text, true, skin, pane);
		}
		protected ModelNode addWrapper(String text, boolean enabled, Skin skin){
			return addWrapper(text, enabled, skin, null);
		}
		protected ModelNode addWrapper(String text, boolean enabled, Skin skin, Supplier<Actor> pane){
			ModelNode wrapper = new ModelNode(){
				@Override
				public Actor createPane(GLTFComposerContext ctx) {
					return pane != null ? pane.get() : null;
				}
			};
			Label label = new Label(text, skin);
			if(!enabled) label.setColor(Color.LIGHT_GRAY);
			wrapper.setActor(label);
			add(wrapper);
			return wrapper;
		}
		protected ModelNode addWrapper(String text, int count, Skin skin){
			return addWrapper(count > 0 ? text + " (" + count + ")" : text, count>0, skin);
		}
		public Actor createPane(GLTFComposerContext ctx) {
			return null;
		}
	}
	
	private class NodeAnimNode extends ModelNode {
		public NodeAnimNode(GLTFComposerContext ctx, NodeAnimation nodeAnimation) {
			setActor(new Label(nodeAnimation.node.id, ctx.skin));
			addWrapper("translation", nodeAnimation.translation != null ? nodeAnimation.translation.size : 0, ctx.skin);
			addWrapper("rotation", nodeAnimation.rotation != null ? nodeAnimation.rotation.size : 0, ctx.skin);
			addWrapper("scaling", nodeAnimation.scaling != null ? nodeAnimation.scaling.size : 0, ctx.skin);
			if(nodeAnimation instanceof NodeAnimationHack){
				Array<NodeKeyframe<WeightVector>> weights = ((NodeAnimationHack) nodeAnimation).weights;
				addWrapper("weights", weights != null ? weights.size : 0, ctx.skin);
				
				// TODO interpolation
				// ((NodeAnimationHack) nodeAnimation).translationMode == Interpolation.CUBICSPLINE;
			}
		}
		// TODO details panel with keyframe values and time
	}
	
	private class AnimNode extends ModelNode {
		private Animation animation;
		public AnimNode(GLTFComposerContext ctx, Animation animation, Skin skin) {
			this.animation = animation;
			setActor(new Label(animation.id, skin));
			
			// TODO display or not ? could be an option... or another module with details ?
			boolean showNodes = false;
			if(showNodes){
				for(NodeAnimation nodeAnim : animation.nodeAnimations){
					add(new NodeAnimNode(ctx, nodeAnim));
				}
			}
			
		}
		@Override
		public Actor createPane(GLTFComposerContext ctx) {
			return new AnimationPanel(ctx, animation);
		}
	}
	
	private class MaterialNode extends ModelNode {

		private Material material;
		
		public MaterialNode(Material material, Skin skin) {
			this.material = material;
			setActor(new Label(material.id, skin));
		}
		@Override
		public Actor createPane(GLTFComposerContext ctx) {
			return new MaterialPanel(ctx, material);
		}
	}
	private class PartNode extends ModelNode {
		public PartNode(GLTFComposerContext ctx, NodePart part, Skin skin) {
			setActor(new Label(part.meshPart.id, skin));
			
			// TODO move that to panel ?
			
			addWrapper("size: " + part.meshPart.size, skin);
			// TODO mapping point, line, triangle, ..etc
			addWrapper("primitive: " + part.meshPart.primitiveType, skin);
			addWrapper("material: " + part.material.id, skin, ()->new MaterialPanel(ctx, part.material));

			{
				ModelNode control = new ModelNode();
				control.setActor(UI.toggle(skin, "enabled", part.enabled, value->part.enabled=value));
				add(control);
			}
			// TODO mesh part info and bone info ?
		}
	}
	private class NodeNode extends ModelNode {
		public NodeNode(GLTFComposerContext ctx, Node node, Skin skin) {
			setActor(new Label(node.id, skin));
			// TODO if some parts have bones : display as bone ? or armature ?
			if(node.parts.size > 0){
				ModelNode wrapper = addWrapper("parts", node.parts.size, skin);
				for(NodePart part : node.parts){
					wrapper.add(new PartNode(ctx, part, skin));
				}
			}
			if(node.hasChildren()){
				ModelNode wrapper = addWrapper("children", node.getChildCount(), skin);
				for(Node child : node.getChildren()){
					wrapper.add(new NodeNode(ctx, child, skin));
				}
			}
		}
	}
	
	private class CameraNode extends ModelNode {
		public CameraNode(Node node, Camera camera, Skin skin) {
			setActor(new Label(node.id, skin));
			// TODO set active ?
		}
	}
	
	private class LightNode extends ModelNode {
		public LightNode(Node node, BaseLight light, Skin skin) {
			setActor(new Label(node.id, skin));
			// TODO controls ?
		}
	}
	
	private class MeshNode extends ModelNode
	{
		public MeshNode(Mesh mesh, Skin skin) {
			setActor(new Label("mesh", skin));
			addWrapper("vertices: " + mesh.getNumVertices(), skin);
			addWrapper("indices: " + mesh.getNumIndices(), skin);
			ModelNode vx = addWrapper("vertex: " + mesh.getVertexSize() + " bytes", skin);
			for(VertexAttribute atr : mesh.getVertexAttributes()){
				// TODO map type
				vx.addWrapper(atr.alias + "_" + atr.unit + " " + atr.numComponents + "x" + atr.type, skin);
			}
			// setExpanded(true);
		}
	}
	
	private class SceneNode extends ModelNode {

		public SceneNode(GLTFComposerContext ctx, Scene scene, Skin skin) {
			setActor(new Label("scene", skin));
			{
				ModelNode wrapper = addWrapper("nodes", scene.modelInstance.nodes.size, skin);
				for(Node node : scene.modelInstance.nodes){
					wrapper.add(new NodeNode(ctx, node, skin));
				}
			}
			{
				ModelNode wrapper = addWrapper("meshes", scene.modelInstance.model.meshes.size, skin);
				for(Mesh mesh : scene.modelInstance.model.meshes){
					wrapper.add(new MeshNode(mesh, skin));
				}
			}
			{
				ModelNode wrapper = addWrapper("materials", scene.modelInstance.materials.size, skin);
				for(Material material : scene.modelInstance.materials){
					wrapper.add(new MaterialNode(material, skin));
				}
			}
			{
				ModelNode wrapper = addWrapper("animations", scene.modelInstance.animations.size, skin);
//				if(scene.modelInstance.animations.size > 0){
//					wrapper.add(new AnimNode(ctx, null, skin));
//				}
				for(Animation animation: scene.modelInstance.animations){
					wrapper.add(new AnimNode(ctx, animation, skin));
				}
			}
			{
				ModelNode wrapper = addWrapper("cameras", scene.cameras.size, skin);
				for(Entry<Node, Camera> e : scene.cameras){
					wrapper.add(new CameraNode(e.key, e.value, skin));
				}
			}
			{
				ModelNode wrapper = addWrapper("lights", scene.lights.size, skin);
				for(Entry<Node, BaseLight> e : scene.lights){
					wrapper.add(new LightNode(e.key, e.value, skin));
				}
			}
		}
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = new Table(skin);
		controls.add("no model loaded");
	
		return controls;
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		if(ctx.sceneJustChanged){
			// update UI
			controls.clear();
			controls.defaults().pad(10);
			
			// TODO options (show selected only)
			// zoom to selected / zoom to scene (auto adjust)
			
			Tree<ModelNode, ModelNode> tree = new Tree<>(ctx.skin);
			SceneNode sceneNode = new SceneNode(ctx, ctx.scene, ctx.skin);
			tree.getRootNodes().add(sceneNode);
			tree.updateRootNodes();
			
			sceneNode.setExpanded(true);
			
			ScrollPane sp = new ScrollPane(tree);
			sp.setScrollingDisabled(true, false);
			sp.setTouchable(Touchable.childrenOnly);
			controls.add(sp).grow().row();
			
			Table infoPane = new Table(ctx.skin);
			controls.add(infoPane).row();
			
			tree.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if(actor == tree){
						ModelNode selection = tree.getSelection().getLastSelected();
						infoPane.clear();
						if(selection != null){
							Actor pane = selection.createPane(ctx);
							if(pane != null) infoPane.add(pane);
						}
					}
				}
			});
		}
	}
	
}
