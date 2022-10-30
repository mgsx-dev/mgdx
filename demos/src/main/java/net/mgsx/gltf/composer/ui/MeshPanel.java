package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;

public class MeshPanel extends Table
{
	public MeshPanel(GLTFComposerContext ctx, Mesh mesh) {
		super(ctx.skin);
		defaults().pad(UI.DEFAULT_PADDING).growX();
		
		add(UI.trig(getSkin(), "Show vertices data", ()->showVerticesData(ctx, mesh))).row();
	}

	private void showVerticesData(GLTFComposerContext ctx, Mesh mesh) {
		Table content = new Table(getSkin());
		int hPad = 10;
		int vPad = 4;
		content.defaults().padLeft(hPad).padRight(hPad).padTop(vPad).padBottom(vPad);
		
		// header
		content.add("vertex");
		for(VertexAttribute attr : mesh.getVertexAttributes()){
			int nbFloats;
			if(attr.type == GL20.GL_FLOAT){
				nbFloats = attr.numComponents;
			}else if(attr.type == GL20.GL_UNSIGNED_SHORT || attr.type == GL20.GL_SHORT){
				nbFloats = attr.numComponents / 2;
			}else if(attr.type == GL20.GL_UNSIGNED_BYTE || attr.type == GL20.GL_BYTE){
				nbFloats = attr.numComponents / 4;
			}else{
				// TODO ???
				nbFloats = attr.numComponents;
			}
			content.add(attr.alias).colspan(nbFloats);
		}
		content.row();
		
		// data
		int stride = mesh.getVertexSize() / 4;
		int count = mesh.getNumVertices();
		int size = stride * count;
		float[] vertices = new float[size];
		mesh.getVertices(vertices);
		
		// hard limit
		int hardLimit = 32;
		count = Math.min(hardLimit, count);
		
		for(int i=0, index=0 ; i<count ; i++){
			content.add("#" + i);
			
			for(VertexAttribute attr : mesh.getVertexAttributes()){
				int nbFloats;
				if(attr.type == GL20.GL_FLOAT){
					nbFloats = attr.numComponents;
				}else if(attr.type == GL20.GL_UNSIGNED_SHORT || attr.type == GL20.GL_SHORT){
					nbFloats = attr.numComponents / 2;
				}else if(attr.type == GL20.GL_UNSIGNED_BYTE || attr.type == GL20.GL_BYTE){
					nbFloats = attr.numComponents / 4;
				}else{
					// TODO ???
					nbFloats = attr.numComponents;
				}
				for(int j=0 ; j<nbFloats ; j++, index++){
					content.add(String.valueOf(vertices[index]));
				}
			}
			content.row();
		}
		
		
		Dialog dialog = CUI.dialog(content, "Vertices data (first " + hardLimit + " vertices max)", getSkin());
		dialog.show(ctx.stage);
	}
}
