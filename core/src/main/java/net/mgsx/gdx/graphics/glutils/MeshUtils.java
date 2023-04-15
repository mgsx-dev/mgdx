package net.mgsx.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.graphics.g3d.ModelUtils;

// TODO POC (not tested)
public class MeshUtils {

//	public static void offsetUVs(Node node, Vector2 displacement){
//		for(NodePart part : node.parts){
//			offsetUVs(part.meshPart, displacement);
//		}
//	}
	
	private static final Vector3 v = new Vector3();
	private static final Matrix4 m = new Matrix4();
	
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
	public static float[] extractTriangles(ModelInstance modelInstance) {
		int indices = 0;
		Array<Node> nodes = ModelUtils.collectNodes(modelInstance);
		for(Node node : nodes)
			for(NodePart part : node.parts)
				indices += part.meshPart.size;
		float[] triangles = new float[indices * 3];
		int offset = 0;
		for(Node node : nodes)
			for(NodePart part : node.parts)
				offset = extractTriangles(triangles, offset, node, part, modelInstance.transform);
		return triangles;
	}
	private static int extractTriangles(float[] triangles, int triangleOffset, Node node, NodePart part, Matrix4 transform) {
		int size = part.meshPart.size;
		int offset = part.meshPart.offset;
		Mesh mesh = part.meshPart.mesh;
		short[] indices = new short[size];
		mesh.getIndices(offset, size, indices, 0);
		int stride = mesh.getVertexSize() / 4;
		float[] vertices = new float[mesh.getNumVertices() * stride];
		mesh.getVertices(vertices);
		VertexAttribute attribute = mesh.getVertexAttribute(Usage.Position);
		int posOffset = attribute.offset / 4;
		m.set(node.globalTransform).mul(transform);
		for(int i=0 ; i<size ; i+=3){
			int vertex = (int)(indices[i] & 0xFFFF);
			int vindex = vertex * stride + posOffset;
			float x = vertices[vindex];
			float y = vertices[vindex+1];
			float z = vertices[vindex+2];
			v.set(x, y, z).mul(m);
			triangles[triangleOffset++] = v.x;
			triangles[triangleOffset++] = v.y;
			triangles[triangleOffset++] = v.z;
		}
		return triangleOffset;
	}
}
