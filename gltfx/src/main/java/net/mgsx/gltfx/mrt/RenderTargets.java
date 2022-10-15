package net.mgsx.gltfx.mrt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gltfx.GLFormat;

public class RenderTargets implements Disposable {
	public static class Usage {
		public final String alias;
		public Usage(String alias) {
			this.alias = alias;
		}
	}
	
	protected static class Layer {
		public final int attachmentIndex;
		public final Usage usage;
		public final GLFormat format;
		public final int samples;
		public Layer(int index, Usage usage, GLFormat format, int samples) {
			super();
			this.attachmentIndex = index;
			this.usage = usage;
			this.format = format;
			this.samples = samples;
		}
	}
	
	protected FrameBuffer fbo;
	
	protected int defaultSamples = 0;
	
	protected final Array<Layer> layers = new Array<Layer>();
	
	protected GLFormat depthFormat, stencilFormat;
	
	public static final Usage COLORS = new Usage("COLOR_LOCATION");
	public static final Usage DEPTH = new Usage("DEPTH_LOCATION");
	public static final Usage STENCIL = new Usage("STENCIL_LOCATION");

	public RenderTargets() {
	}
	
	public FrameBuffer getFrameBuffer() {
		return fbo;
	}
	
	public String buildOptions(){
		String options = "";
		for(Layer layer : layers){
			options += "#define " + layer.usage.alias + " " + layer.attachmentIndex + "\n";
		}
		return options;
	}
	
	public boolean ensureScreenSize(){
		return ensureSize(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
	}
	public boolean ensureSize(int width, int height){
		if(fbo == null || fbo.getWidth() != width || fbo.getHeight() != height){
			if(fbo != null) fbo.dispose();
			fbo = buildFBO(width, height);
			return true;
		}
		return false;
	}
	
	protected FrameBuffer buildFBO(int width, int height) {
		FrameBufferBuilder builder = new FrameBufferBuilder(width, height);
		for(Layer layer : layers){
			builder.addColorTextureAttachment(layer.format.internalFormat, layer.format.format, layer.format.type);
		}
		if(depthFormat != null){
			builder.addDepthRenderBuffer(depthFormat.internalFormat);
		}
		if(stencilFormat != null){
			builder.addDepthRenderBuffer(stencilFormat.internalFormat);
		}
		return builder.build();
	}

	public void begin(){
		fbo.begin();
	}
	
	public void end(){
		fbo.end();
	}
	
	public int addLayer(Usage usage, GLFormat format) {
		return addLayer(usage, format, defaultSamples);
	}
	public int addLayer(Usage usage, GLFormat format, int samples) {
		int index = layers.size;
		layers.add(new Layer(index, usage, format, samples));
		return index;
	}
	public void remove(Usage usage) {
		for(int i=0 ; i<layers.size ; i++){
			if(layers.get(i).usage == usage){
				layers.removeIndex(i);
			}
		}
	}
	public int replaceLayer(Usage usage, GLFormat format) {
		return replaceLayer(usage, format, defaultSamples);
	}
	public int replaceLayer(Usage usage, GLFormat format, int samples) {
		for(int i=0 ; i<layers.size ; i++){
			Layer layer = layers.get(i);
			if(layer.usage == usage){
				layers.set(i, new Layer(i, usage, format, samples));
				return i;
			}
		}
		return addLayer(usage, format, samples);
	}

	public Texture getTexture(Usage usage){
		for(Layer layer : layers){
			if(layer.usage == usage){
				return fbo.getTextureAttachments().get(layer.attachmentIndex);
			}
		}
		return null;
	}
	
	public void reset(){
		if(fbo != null){
			fbo.dispose();
			fbo = null;
		}
	}
	
	@Override
	public void dispose() {
		if(fbo != null){
			fbo.dispose();
			fbo = null;
		}
	}

	public void setDepth(GLFormat depthFormat) {
		this.depthFormat = depthFormat;
	}
	
	public void setStencilFormat(GLFormat stencilFormat) {
		this.stencilFormat = stencilFormat;
	}
	
	public void setDefaultSamples(int defaultSamples) {
		this.defaultSamples = defaultSamples;
	}
	
	public int getWidth() {
		return fbo.getWidth();
	}

	public int getHeight() {
		return fbo.getHeight();
	}

	public Texture getColorBufferTexture() {
		return getTexture(COLORS);
	}
	
	public void clear() {
		reset();
		layers.clear();
		depthFormat = stencilFormat = null;
	}
}
