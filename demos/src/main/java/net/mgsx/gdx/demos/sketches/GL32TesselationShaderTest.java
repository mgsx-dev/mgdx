package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;

import net.mgsx.gdx.graphics.GL32;

public class GL32TesselationShaderTest extends ScreenAdapter {
	ShaderProgram shader;
	Mesh mesh;
	Matrix4 projection = new Matrix4();
	Matrix4 view = new Matrix4();
	Matrix4 model = new Matrix4();
	Matrix4 combined = new Matrix4();
	Vector3 axis = new Vector3(1, 0, 1).nor();
	float angle = 45;
	private Array<FileHandle> models;
	private int modelIndex = 0;

	public GL32TesselationShaderTest () {

		ShaderStage.geometry.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 150\n" : "#version 320 es\n";
		ShaderStage.tesselationControl.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 400\n" : "#version 320 es\n";
		ShaderStage.tesselationEvaluation.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 400\n" : "#version 320 es\n";

		shader = new ShaderProgram(
			new ShaderPart(ShaderStage.vertex, Gdx.files.classpath("shaders/pnt.vert").readString()),
			new ShaderPart(ShaderStage.tesselationControl, Gdx.files.classpath("shaders/pnt.tesc").readString()),
			new ShaderPart(ShaderStage.tesselationEvaluation, Gdx.files.classpath("shaders/pnt.tese").readString()),
			new ShaderPart(ShaderStage.geometry, Gdx.files.classpath("shaders/pnt.geom").readString()),
			new ShaderPart(ShaderStage.fragment, Gdx.files.classpath("shaders/pnt.frag").readString()));

		if(!shader.isCompiled()){
			throw new GdxRuntimeException(shader.getLog());
		}

		models = new Array<FileHandle>();
		models.add(Gdx.files.internal("models/teapot.g3dj"));
		models.add(Gdx.files.internal("models/torus.g3dj"));
		models.add(Gdx.files.internal("models/sphere.g3dj"));
		models.add(Gdx.files.internal("models/knight.g3dj"));

		nextMesh();
	}
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(new InputMultiplexer(Gdx.input.getInputProcessor(), new InputAdapter(){
			@Override
			public boolean keyDown (int keycode) {
				if(keycode == Input.Keys.SPACE){
					nextMesh();
				}
				return super.keyDown(keycode);
			}
		}));
	}
	
	@Override
	public void hide() {
		Gdx.input.setInputProcessor(((InputMultiplexer)Gdx.input.getInputProcessor()).getProcessors().first());
	}


	private void nextMesh(){
		mesh = new G3dModelLoader(new JsonReader()).loadModel(models.get(modelIndex)).meshes.first();
		modelIndex = (modelIndex+1) % models.size;
	}

	@Override
	public void render (float delta) {
		angle += delta * 40.0f;
		float aspect = Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		projection.setToProjection(1.0f, 20.0f, 60.0f, aspect);
		view.idt().trn(0, 0, -12.0f);
		model.setToRotation(axis, angle);
		combined.set(projection).mul(view).mul(model);

		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.setUniformMatrix("u_mvpMatrix", combined);
		mesh.render(shader, GL32.GL_PATCHES);
	}
}
