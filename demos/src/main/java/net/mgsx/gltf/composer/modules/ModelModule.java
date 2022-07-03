package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gdx.graphics.glutils.ColorUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.composer.utils.UI.ControlScale;
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
	
	private static class ModelNode extends Tree.Node<ModelNode, ModelNode, Actor> {
		protected ModelNode addWrapper(String text, Skin skin){
			return addWrapper(text, true, skin);
		}
		protected ModelNode addWrapper(String text, boolean enabled, Skin skin){
			ModelNode wrapper = new ModelNode();
			Label label = new Label(text, skin);
			if(!enabled) label.setColor(Color.LIGHT_GRAY);
			wrapper.setActor(label);
			add(wrapper);
			return wrapper;
		}
		protected ModelNode addWrapper(String text, int count, Skin skin){
			return addWrapper(count > 0 ? text + " (" + count + ")" : text, count>0, skin);
		}
	}
	
	private static class AnimNode extends ModelNode {
		public AnimNode(GLTFComposerContext ctx, Animation animation, Skin skin) {
			Table t = new Table(skin);
			if(animation != null){
				t.add(UI.change(new TextButton("play", skin), e->{
					ctx.scene.animationController.setAnimation(animation.id, -1);
				}));
				t.add(animation.id);
			}else{
				t.add(UI.change(new TextButton("stop", skin), e->{
					ctx.scene.animationController.setAnimation(null);
				}));
			}
			
			setActor(t);
		}
	}
	
	private static class MaterialNode extends ModelNode {

		public MaterialNode(Material material, Skin skin) {
			setActor(new Label(material.id, skin));
			// TODO more use case (textures, etc..)
			for(Attribute attribute : material){
				Table t = new Table(skin);
				if(attribute instanceof FloatAttribute){
					FloatAttribute fa = (FloatAttribute)attribute;
					ModelNode n = new ModelNode();
					UI.slider(t, Attribute.getAttributeAlias(fa.type), 0, 1, fa.value, v->fa.value=v);
					n.setActor(t);
					add(n);
				}
				else if(attribute instanceof ColorAttribute){
					ColorAttribute fa = (ColorAttribute)attribute;
					ModelNode n = new ModelNode();
					boolean rgb = false;
					if(rgb){
						// RGB mode
						UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".r", 0, 1, fa.color.r, v->fa.color.r=v);
						UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".g", 0, 1, fa.color.g, v->fa.color.g=v);
						UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".b", 0, 1, fa.color.b, v->fa.color.b=v);
					}else{
						// HSV mode
						float [] hsv = new float[]{0,0,0,fa.color.a, 1};
						fa.color.toHsv(hsv);
						UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".h", 0, 360, hsv[0], v->{hsv[0]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
						UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".s", 0, 1, hsv[1], v->{hsv[1]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
						UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".v", 0, 1, hsv[2], v->{hsv[2]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
						UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".scale", 1e-3f, 1e3f, hsv[4], ControlScale.LOG, v->{hsv[4]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
					}
					n.setActor(t);
					add(n);
				}
			}
		}
	}
	private static class PartNode extends ModelNode {
		public PartNode(NodePart part, Skin skin) {
			setActor(new Label(part.meshPart.id, skin));
			addWrapper("size: " + part.meshPart.size, skin);
			// TODO mapping point, line, triangle, ..etc
			addWrapper("primitive: " + part.meshPart.primitiveType, skin);
			addWrapper("material: " + part.material.id, skin);

			{
				ModelNode control = new ModelNode();
				control.setActor(UI.toggle(skin, "enabled", part.enabled, value->part.enabled=value));
				add(control);
			}
			// TODO material and mesh part info and bone info
		}
	}
	private static class NodeNode extends ModelNode {
		public NodeNode(Node node, Skin skin) {
			setActor(new Label(node.id, skin));
			// TODO if some parts have bones : display as bone ? or armature ?
			if(node.parts.size > 0){
				ModelNode wrapper = addWrapper("parts", node.parts.size, skin);
				for(NodePart part : node.parts){
					wrapper.add(new PartNode(part, skin));
				}
			}
			if(node.hasChildren()){
				ModelNode wrapper = addWrapper("nodes", node.getChildCount(), skin);
				for(Node child : node.getChildren()){
					wrapper.add(new NodeNode(child, skin));
				}
			}
		}
	}
	
	private static class CameraNode extends ModelNode {
		public CameraNode(Node node, Camera camera, Skin skin) {
			setActor(new Label(node.id, skin));
			// TODO set active ?
		}
	}
	
	private static class LightNode extends ModelNode {
		public LightNode(Node node, BaseLight light, Skin skin) {
			setActor(new Label(node.id, skin));
			// TODO controls ?
		}
	}
	
	private static class MeshNode extends ModelNode
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
	
	private static class SceneNode extends ModelNode {

		public SceneNode(GLTFComposerContext ctx, Scene scene, Skin skin) {
			setActor(new Label("scene", skin));
			{
				ModelNode wrapper = addWrapper("nodes", scene.modelInstance.nodes.size, skin);
				for(Node node : scene.modelInstance.nodes){
					wrapper.add(new NodeNode(node, skin));
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
				if(scene.modelInstance.animations.size > 0){
					wrapper.add(new AnimNode(ctx, null, skin));
				}
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
		}
	}
	
}
