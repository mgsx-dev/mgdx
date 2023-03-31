package net.mgsx.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Vector2;

// TODO POC (not tested)
public class MeshUtils {

//	public static void offsetUVs(Node node, Vector2 displacement){
//		for(NodePart part : node.parts){
//			offsetUVs(part.meshPart, displacement);
//		}
//	}
	
	public static void offsetUVs(NodePart part, Vector2 displacement) {
		offsetUVs(part.meshPart.mesh, part.meshPart.offset, part.meshPart.size, displacement);
	}
	public static void offsetUVs(MeshPart meshPart, Vector2 displacement) {
		offsetUVs(meshPart.mesh, meshPart.offset, meshPart.size, displacement);
	}

	public static void offsetUVs(Mesh mesh, int offset, int size, Vector2 displacement) {
		short[] indices = new short[size];
		mesh.getIndices(offset, size, indices, 0);
		int stride = mesh.getVertexSize() / 4;
		float[] vertices = new float[mesh.getNumVertices() * stride];
		mesh.getVertices(vertices);
		VertexAttribute attribute = mesh.getVertexAttribute(Usage.TextureCoordinates);
		int uvOffset = attribute.offset / 4;
		
		for(int i=0 ; i<size ; i++){
			int vertex = (int)(indices[i] & 0xFFFF);
			int uvIndex = vertex * stride + uvOffset;
			vertices[uvIndex] += displacement.x;
			vertices[uvIndex+1] += displacement.y;
		}
		
		mesh.setVertices(vertices);
	}
}
