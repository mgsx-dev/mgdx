package net.mgsx.gltf.composer.modules;

import java.util.function.Supplier;

import com.badlogic.gdx.files.FileHandle;
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
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gdx.graphics.g3d.ModelUtils;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.ui.AnimationPanel;
import net.mgsx.gltf.composer.ui.AssetPanel;
import net.mgsx.gltf.composer.ui.MaterialBasicPanel;
import net.mgsx.gltf.composer.ui.MaterialDebugPanel;
import net.mgsx.gltf.composer.ui.MaterialEmissionPanel;
import net.mgsx.gltf.composer.ui.MaterialIridescencePanel;
import net.mgsx.gltf.composer.ui.MaterialOpacityPanel;
import net.mgsx.gltf.composer.ui.MaterialSpecularPanel;
import net.mgsx.gltf.composer.ui.MaterialTransmissionPanel;
import net.mgsx.gltf.composer.ui.MeshPanel;
import net.mgsx.gltf.composer.ui.NodePanel;
import net.mgsx.gltf.composer.utils.ComposerUtils;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.exceptions.GLTFRuntimeException;
import net.mgsx.gltf.loaders.exceptions.GLTFUnsupportedException;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.animation.NodeAnimationHack;
import net.mgsx.gltf.scene3d.model.WeightVector;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/**
 * have a UI to control animations, skelton overlay, etc...
 * 
 * @author mgsx
 *
 */
public class SceneModule implements GLTFComposerModule
{
	Table controls;
	
	ButtonGroup cameraButtonGroup = new ButtonGroup();
	
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
			// wrapper.setSelectable(false);
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
		
		public MaterialNode(GLTFComposerContext ctx, Material material, Skin skin) {
			this.material = material;
			setActor(new Label(material.id, skin));
			addWrapper("Opacity", skin, ()->new MaterialOpacityPanel(ctx, material));
			addWrapper("Emission", skin, ()->new MaterialEmissionPanel(ctx, material));
			addWrapper("Transmission", skin, ()->new MaterialTransmissionPanel(ctx, material));
			addWrapper("Iridescence", skin, ()->new MaterialIridescencePanel(ctx, material));
			addWrapper("Specular", skin, ()->new MaterialSpecularPanel(ctx, material));
			addWrapper("Debug", skin, ()->new MaterialDebugPanel(ctx, material));
		}
		@Override
		public Actor createPane(GLTFComposerContext ctx) {
			return new MaterialBasicPanel(ctx, material);
		}
	}
	private class PartNode extends ModelNode {
		public PartNode(GLTFComposerContext ctx, NodePart part, Skin skin) {
			// setActor(new Label(part.meshPart.id, skin));
			setActor(UI.toggle(skin, part.meshPart.id, part.enabled, v->part.enabled=v));
			// TODO move that to panel ?
			addWrapper("offset: " + part.meshPart.offset, skin);
			addWrapper("size: " + part.meshPart.size, skin);
			addWrapper("primitive: " + ComposerUtils.primitiveString(part.meshPart.primitiveType), skin);
			addWrapper("material: " + part.material.id, skin);
			if(part.bones != null){
				addWrapper("bones: " + part.bones.length, skin);
			}
//			{
//				ModelNode control = new ModelNode();
//				control.setActor(UI.toggle(skin, "enabled", part.enabled, value->part.enabled=value));
//				add(control);
//			}
			// TODO mesh part info and bone info ?
		}
	}
	private class NodeNode extends ModelNode {
		private Node node;
		public NodeNode(GLTFComposerContext ctx, Node node, Skin skin) {
			this.node = node;
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
		@Override
		public Actor createPane(GLTFComposerContext ctx) {
			return new NodePanel(ctx, node);
		}
	}
	
	private class CameraNode extends ModelNode {
		public CameraNode(GLTFComposerContext ctx, Node node, Camera camera, Skin skin) {
			TextButton toggle = UI.toggle(skin, node.id, ctx.cameraAttachment == node, v->ctx.cameraAttachment = v ? node : null);
			cameraButtonGroup.add(toggle);
			setActor(toggle);
			
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
		private Mesh mesh;
		public MeshNode(Mesh mesh, Skin skin) {
			this.mesh = mesh;
			setActor(new Label("mesh", skin));
			addWrapper("vertices: " + mesh.getNumVertices(), skin);
			addWrapper("indices: " + mesh.getNumIndices(), skin);
			ModelNode vx = addWrapper("vertex: " + mesh.getVertexSize() + " bytes", skin);
			for(VertexAttribute atr : mesh.getVertexAttributes()){
				vx.addWrapper(atr.alias + "_" + atr.unit + " " + atr.numComponents + "x" + ComposerUtils.glTypeString(atr.type), skin);
			}
			// setExpanded(true);
		}
		@Override
		public Actor createPane(GLTFComposerContext ctx) {
			return new MeshPanel(ctx, mesh);
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
				Array<NodePart> allParts = ModelUtils.collectNodeParts(scene.modelInstance);
				ModelNode wrapper = addWrapper("parts", allParts.size, skin);
				for(NodePart part : allParts){
					wrapper.add(new PartNode(ctx, part, skin));
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
					wrapper.add(new MaterialNode(ctx, material, skin));
				}
			}
			{
				ModelNode wrapper = addWrapper("animations", scene.modelInstance.animations.size, skin);
				for(Animation animation: scene.modelInstance.animations){
					wrapper.add(new AnimNode(ctx, animation, skin));
				}
			}
			{
				ModelNode wrapper = addWrapper("cameras", scene.cameras.size, skin);
				for(Entry<Node, Camera> e : scene.cameras){
					wrapper.add(new CameraNode(ctx, e.key, e.value, skin));
				}
			}
			{
				ModelNode wrapper = addWrapper("lights", scene.lights.size, skin);
				for(Entry<Node, BaseLight> e : scene.lights){
					wrapper.add(new LightNode(e.key, e.value, skin));
				}
			}
		}
		
		@Override
		public Actor createPane(GLTFComposerContext ctx) {
			return new AssetPanel(ctx, ctx.asset);
		}
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		updateUI(ctx);
		return controls;
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		if(ctx.sceneJustChanged){
			updateUI(ctx);
		}
	}
	
	private void updateUI(GLTFComposerContext ctx){
		// update UI
		controls.clear();
		controls.defaults().growX();
		
		UI.header(controls, "Model");
		
		ctx.overlay.setScene(ctx.asset, ctx.scene);
		
		if(ctx.scene != null){
			
			// TODO options (show selected only)
			// zoom to selected / zoom to scene (auto adjust)
			
			cameraButtonGroup.clear();
			cameraButtonGroup.setMinCheckCount(0);
			cameraButtonGroup.setMaxCheckCount(1);
			
			Tree<ModelNode, ModelNode> tree = new Tree<>(ctx.skin);
			SceneNode sceneNode = new SceneNode(ctx, ctx.scene, ctx.skin);
			tree.getRootNodes().add(sceneNode);
			tree.updateRootNodes();
			
			sceneNode.setExpanded(true);
			
			ScrollPane sp = new ScrollPane(tree);
			sp.setScrollingDisabled(true, false);
			sp.setTouchable(Touchable.childrenOnly);
			controls.add(sp).growY().expandX().left().row();
			
			Table infoPane = new Table(ctx.skin);
			controls.add(infoPane).fill().row();
			
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
			
			if(ctx.overlay.hasBones())
			{
				UI.header(controls, "Overlay");
				
				Frame frame = UI.frameToggle("Bones", ctx.skin, ctx.overlay.displayEnabled, v->ctx.overlay.displayEnabled=v);
				controls.add(frame).growX().row();
				
				Table t = frame.getContentTable();
				t.defaults().expandX().left();
				UI.toggle(t, "Display boxes",ctx.overlay.displayBox, v->ctx.overlay.displayBox=v);
				UI.toggle(t, "Display axis", ctx.overlay.displayAxis, v->ctx.overlay.displayAxis=v);
				UI.toggle(t, "Display parenting", ctx.overlay.displayParenting, v->ctx.overlay.displayParenting=v);
			}
			
		}else{
			controls.add("no model loaded").fill(false);
		}
	}
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		String ext = file.extension().toLowerCase();
		SceneAsset newAsset = null;
		try{
			if(ext.equals("gltf")){
				newAsset = new GLTFLoader().load(file, true);
				ctx.compo.scenesPath.clear();
				ctx.compo.scenesPath.add(file.path());
			}
			else if(ext.equals("glb")){
				newAsset = new GLBLoader().load(file, true);
				ctx.compo.scenesPath.clear();
				ctx.compo.scenesPath.add(file.path());
			}
		}catch(GLTFUnsupportedException e){
			UI.popup(ctx.stage, ctx.skin, "Not supported", e.getMessage());
			return true;
		}catch(GLTFIllegalException e){
			UI.popup(ctx.stage, ctx.skin, "Invalid error", e.getMessage());
			return true;
		}catch(GLTFRuntimeException e){
			UI.popup(ctx.stage, ctx.skin, "Unexpected error", e.getMessage());
			return true;
		}
		if(newAsset != null){
			ctx.setScene(newAsset);
			
			ComposerUtils.fitCameraToScene(ctx);
			
			ctx.sceneJustChanged = true;
			ctx.invalidateShaders();
			return true;
		}
		return false;
	}
	
}
