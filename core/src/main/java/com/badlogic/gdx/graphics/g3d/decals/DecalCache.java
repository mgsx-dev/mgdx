package com.badlogic.gdx.graphics.g3d.decals;

import java.util.Comparator;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;

public class DecalCache implements RenderableProvider {
	
	private static class MeshCache {
		Mesh mesh;
		float [] vertices;
		int vertexIndex;
		int indexCount;
		public Material material;
		public DecalMaterial decalMaterial;
	}

	private static final Comparator<MeshCache> comparator = new Comparator<DecalCache.MeshCache>() {
		@Override
		public int compare(MeshCache o1, MeshCache o2) {
			BlendingAttribute b1 = o1.material.get(BlendingAttribute.class, BlendingAttribute.Type);
			BlendingAttribute b2 = o2.material.get(BlendingAttribute.class, BlendingAttribute.Type);
			boolean add1 = b1.destFunction == GL20.GL_ONE;
			boolean add2 = b2.destFunction == GL20.GL_ONE;
			if(add1 == add2) return 0;
			if(add1) return -1;
			return 1;
		}
	};

	private final Array<MeshCache> meshPool = new Array<MeshCache>();
	
	private final ObjectMap<DecalMaterial, Array<MeshCache>> meshes = new ObjectMap<DecalMaterial, Array<MeshCache>>();
	
	private final Array<MeshCache> orderedMeshes = new Array<MeshCache>();
	
	private final VertexAttributes vertexAttributes = new VertexAttributes(
			new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), 
			new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE), 
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
	
	private final int vertexStride = vertexAttributes.vertexSize / Float.BYTES;
	private final int decalStride = vertexStride * 4;
	
	private final ShaderProvider shaderProvider;
	
	public DecalCache(ShaderProvider shaderProvider) {
		super();
		this.shaderProvider = shaderProvider;
	}

	public void begin(){
		for(Entry<DecalMaterial, Array<MeshCache>> entry : meshes){
			for(MeshCache cache : entry.value){
				cache.vertexIndex = 0;
				cache.indexCount = 0;
				cache.material = null;
				meshPool.add(cache);
			}
		}
		// TODO fix decal cache list recycling (GC)
		meshes.clear();
		orderedMeshes.clear();
	}
	
	public void add(Decal decal){
		DecalMaterial material = decal.getMaterial();
		Array<MeshCache> meshCaches = meshes.get(material);
		if(meshCaches == null) meshes.put(material, meshCaches = new Array<MeshCache>());
		MeshCache meshCache;
		if(meshCaches.size > 0){
			meshCache = meshCaches.peek();
			if(meshCache.vertexIndex >= meshCache.vertices.length){
				meshCaches.add(meshCache = createMeshCache(material));
			}
		}else{
			meshCaches.add(meshCache = createMeshCache(material));
		}
		System.arraycopy(decal.getVertices(), 0, meshCache.vertices, meshCache.vertexIndex, decalStride);
		meshCache.vertexIndex += decalStride;
		meshCache.indexCount += 6;
	}
	
	public void end(){
		for(Entry<DecalMaterial, Array<MeshCache>> entry : meshes){
			for(MeshCache cache : entry.value){
				cache.mesh.setVertices(cache.vertices, 0, cache.vertexIndex);
			}
			orderedMeshes.addAll(entry.value);
		}
		orderedMeshes.sort(comparator);
	}
	
	private MeshCache createMeshCache(DecalMaterial material){
		MeshCache cache = meshPool.size > 0 ? meshPool.pop() : null;
		if(cache == null){
			cache = new MeshCache();
			cache.mesh = createMesh();
			cache.vertices = new float[cache.mesh.getMaxVertices() * vertexStride];
		}
		cache.material = createMaterial(material);
		cache.decalMaterial = material;
		return cache;
	}
	
	private Mesh createMesh(){
		int maxSprites = ((1 << 16) - 1) / 4;
		int maxVertices = maxSprites * 4;
		int maxIndices = maxSprites * 6;
		Mesh mesh = new Mesh(false, maxVertices, maxIndices, vertexAttributes);
		short [] indices = new short[maxIndices];
		for(int i=0, lastIndex=0 ; i<maxIndices ; i+=6, lastIndex+=4){
			indices[i+0] = (short)lastIndex;
			indices[i+1] = (short)(lastIndex+2);
			indices[i+2] = (short)(lastIndex+1);
			
			indices[i+3] = (short)(lastIndex+1);
			indices[i+4] = (short)(lastIndex+2);
			indices[i+5] = (short)(lastIndex+3);
		}
		mesh.setIndices(indices);
		return mesh;
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		for(MeshCache cache : orderedMeshes){
			Renderable r = pool.obtain();
			r.meshPart.mesh = cache.mesh;
			r.meshPart.offset = 0;
			r.meshPart.size = cache.indexCount;
			r.material = cache.material;
			r.meshPart.primitiveType = GL20.GL_TRIANGLES;
			r.shader = shaderProvider.getShader(r);
			r.userData = cache.decalMaterial;
			renderables.add(r);
		}
	}
	
	protected Material createMaterial(DecalMaterial decalMaterial){
		Material m = new Material();
		m.set(new BlendingAttribute(decalMaterial.getSrcBlendFactor(), decalMaterial.getDstBlendFactor()));
		m.set(new DepthTestAttribute(false));
		// TODO this requires to be in same package, could be fixed in libgdx
		m.set(TextureAttribute.createDiffuse(decalMaterial.textureRegion));
		return m;
	}
	
}
