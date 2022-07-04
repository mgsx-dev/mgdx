package net.mgsx.gdx.graphics.cameras;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

// TODO improve camera with more controls (rotate with numpad, etc)
public class BlenderCamera {
	
	private static class BlenderCameraController extends CameraInputController {
		
		private final Vector3 tangent = new Vector3();
		private final Vector3 prevTarget = new Vector3();
		private final Vector3 prevPosition = new Vector3();
		private Vector3 moveTarget;
		private Vector3 movePosition;
		private final Vector3 homeTarget;
		private float time;
		private float homeDistance;
		private int button;
		
		public BlenderCameraController(Camera camera, Vector3 homeTarget, float homeDistance, int button) {
			super(camera);
			this.homeTarget = homeTarget;
			this.target.set(homeTarget);
			this.homeDistance = homeDistance;
			
			this.activateKey = 0;
			this.alwaysScroll = true;
			this.backwardKey = 0;
			this.forwardButton = -1;
			this.forwardKey = 0;
			this.forwardTarget = false;
			
			// this.rotateAngle;
			this.rotateLeftKey = 0;
			this.rotateRightKey = 0;
			// this.scrollFactor;
			this.scrollTarget = false;
			this.translateTarget = true;
			
		}
		@Override
		public void update() {
			
			// TODO ortho camera, update zoom instead of position when zooming
			
			boolean changed = false;
			
			if(Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) || Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)){
				moveTarget = new Vector3(homeTarget);
				prevTarget.set(target);
				prevPosition.set(camera.position);
				movePosition = new Vector3().set(homeTarget).sub(prevPosition).nor().scl(-homeDistance).add(homeTarget);
				time = 0;
			}
			if(moveTarget != null){
				this.translateButton = -1;
				this.rotateButton = -1;
				this.forwardButton = -1;
				time += Gdx.graphics.getDeltaTime() * 5f;
				this.target.set(prevTarget).lerp(moveTarget, time);
				this.camera.position.set(prevPosition).lerp(movePosition, time);
				if(time >= 1){
					this.target.set(moveTarget);
					camera.position.set(movePosition);
					moveTarget = null;
				}
				camera.up.set(Vector3.Y);
				camera.lookAt(target);
				changed = true;
			}else{
				if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)){
					this.translateButton = -1;
					this.rotateButton = -1;
					this.forwardButton = button;
				}else if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)){
					this.translateButton = button;
					this.rotateButton = -1;
					this.forwardButton = -1;
				}else{
					this.forwardButton = -1;
					this.translateButton = -1;
					this.rotateButton = button;
				}
			}
			
			tangent.set(camera.direction).crs(camera.up).nor();
			
			float rotationSteps = 90f / 6;
			if(Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)){
				camera.rotateAround(target, Vector3.Y, -rotationSteps);
				changed = true;
			}
			if(Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_6)){
				camera.rotateAround(target, Vector3.Y, rotationSteps);
				changed = true;
			}
			if(Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_8)){
				camera.rotateAround(target, tangent, -rotationSteps);
				changed = true;
			}
			if(Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)){
				camera.rotateAround(target, tangent, rotationSteps);
				changed = true;
			}
			
			if(changed){
				camera.update();
			}
			
			this.pinchZoomFactor = 10f;
			this.translateUnits = camera.position.dst(target);
			super.update();
		}
		
	}
	
	private static class CameraConfig<T extends Camera>{
		public BlenderCameraController controller;
		private T camera;
		private Viewport viewport;
		public CameraConfig(T camera, int width, int height, Vector3 homeTarget, float homeDistance, int button) {
			this.camera = camera;
			viewport = new FitViewport(width, height, camera);
			controller = new BlenderCameraController(camera, homeTarget, homeDistance, button);
		}
		public void resize(int width, int height){
			viewport.update(width, height, false);
			// XXX
			camera.viewportWidth = width;
			camera.viewportHeight = height;
			camera.update();
		}
		public void update(){
			controller.update();
		}
		public void set(Vector3 position, Vector3 target, float near, float far) {
			camera.position.set(position);
			camera.up.set(Vector3.Y);
			camera.lookAt(target);
			camera.near = near;
			camera.far = far;
			camera.update();
			controller.target.set(target);
		}
	}
	
	private CameraConfig<PerspectiveCamera> perspective;
	private CameraConfig<OrthographicCamera> orthographic;
	private CameraConfig current;
	
	private int renderWidth = 1920;
	private int renderHeight = 1080;
	
	private InputMultiplexer inputs;
	private int button;
	
	public BlenderCamera(Vector3 homeTarget, float homeDistance){
		this(homeTarget, homeDistance, Input.Buttons.MIDDLE);
	}
	
	public BlenderCamera(Vector3 homeTarget, float homeDistance, int button) {
		this.button = button;
		perspective = new CameraConfig<PerspectiveCamera>(new PerspectiveCamera(), renderWidth, renderHeight, homeTarget, homeDistance, button);
		orthographic = new CameraConfig<OrthographicCamera>(new OrthographicCamera(), renderWidth, renderHeight, homeTarget, homeDistance, button);
		
		// blender defaults
		PerspectiveCamera cam = perspective.camera;
		cam.fieldOfView = 39.6f;
		cam.near = .01f;
		cam.far = 100;
		cam.position.set(1, 1, 1).scl(homeDistance);
		cam.up.set(Vector3.Y);
		cam.lookAt(0,0,0);
		cam.update();
		
		OrthographicCamera ocam = orthographic.camera;
		ocam.zoom = 2f / renderHeight; // 7.314f;
		ocam.near = 0.1f;
		ocam.far = 100;
		ocam.position.set(1, 1, 1).scl(homeDistance);
		ocam.up.set(Vector3.Y);
		ocam.lookAt(0,0,0);
		ocam.update();
		
		current = perspective;
		inputs = new InputMultiplexer(current.controller);
	}
	public InputProcessor getInputs(){
		return inputs;
	}
	private CameraConfig current() {
		return current;
	}
	public void resize(int width, int height){
		perspective.resize(width, height);
		orthographic.resize(width, height);
	}
	public void update(float delta){
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_5)){
			if(current == perspective){
				switchTo(orthographic);
			}else{
				switchTo(perspective);
			}
		}
		if(Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)){
			if(Gdx.input.isButtonJustPressed(button)){
				// TODO center position on geometry, require raycast mesh.
			}
		}
		current().update();
	}
	private void switchTo(CameraConfig toConfig) {
		// sync
		toConfig.camera.position.set(current.camera.position);
		toConfig.camera.direction.set(current.camera.direction);
		toConfig.camera.up.set(current.camera.up);
		toConfig.camera.update();
		
		current = toConfig;
		inputs.clear();
		inputs.addProcessor(current.controller);
	}
	public Camera getCamera(){
		return current().camera;
	}
	public PerspectiveCamera getPerspectiveCamera(){
		return perspective.camera;
	}
	public OrthographicCamera getOrthographicCamera(){
		return orthographic.camera;
	}
	public void apply() {
		current().viewport.apply();
	}
	public void setTarget(float x, float y, float z) {
		perspective.controller.target.set(x,y,z);
		orthographic.controller.target.set(x,y,z);
	}

	public void set(Vector3 position, Vector3 target, float near, float far) {
		perspective.set(position, target, near, far);
		orthographic.set(position, target, near, far);
	}
}
