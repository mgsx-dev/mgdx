package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import net.mgsx.gdx.graphics.glutils.GpuUtils;
import net.mgsx.gdx.utils.AdvancedProfiler;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.UI;

public class SystemModule implements GLTFComposerModule
{
	private Table controls;
	private Label memoryLabel;
	private int memoryMaxKB;
	
	// TODO how to plug in GLProfiler cleanly
	private AdvancedProfiler advancedProfiler;
	private Label labelDC;
	private Label labelCS;
	private Label labelSS;
	private Label labelTB;
	private Label labelVS;
	private Label labelFPS;
	
	private WindowedMean fpsAverage = new WindowedMean(30);

	private float timeout;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		
		// Options
		
		UI.toggle(controls, "GPU VSync", ctx.vsync, v->{
			ctx.vsync = v;
			Gdx.graphics.setVSync(ctx.vsync);
			fpsAverage.clear();
			timeout = 0;
		});
		UI.toggle(controls, "CPU FPS Sync", ctx.fsync, v->{
			ctx.fsync = v;
			if(ctx.fsync){
				Gdx.graphics.setForegroundFPS(ctx.ffps);
			}else{
				Gdx.graphics.setForegroundFPS(0);
			}
			fpsAverage.clear();
			timeout = 0;
		});
		
		// GPU memory
		{
			Table t = new Table(skin);
			t.add("GPU memory: ").padRight(UI.DEFAULT_PADDING);
			if(GpuUtils.hasMemoryInfo()){
				memoryMaxKB = GpuUtils.getMaxMemoryKB();
				memoryLabel = t.add("").getActor();
				t.add(" / " + (memoryMaxKB / 1024) + " MB");
			}else{
				t.add("unknown");
			}
			controls.add(t).row();
		}
		// FPS and stats
		UI.toggle(controls, "GLProfiler", ctx.profiler.isEnabled(), v->{
			if(!ctx.profiler.isEnabled()) ctx.profiler.enable(); else ctx.profiler.disable();
		});
		
		Table stats = UI.table(skin);
		
		controls.add(stats).row();
		labelDC = entry(stats, "draw calls");
		labelCS = entry(stats, "calls");
		labelSS = entry(stats, "shader switches");
		labelTB = entry(stats, "texture bindings");
		labelVS = entry(stats, "polygon count");
		labelFPS = entry(stats, "FPS");
		
		return controls;
	}
	
	private Label entry(Table table, String title) {
		table.add(title);
		Label label = table.add("--").width(60).getActor();
		label.setAlignment(Align.left);
		table.row();
		return label;
	}

	@Override
	public void render(GLTFComposerContext ctx) {
		if(memoryLabel != null){
			memoryLabel.setText((memoryMaxKB - GpuUtils.getAvailableMemoryKB()) / 1024);
		}
	}

	public void beginProfiling(GLTFComposerContext ctx) {
		ctx.profiler.reset();
	}

	public void endProfiling(GLTFComposerContext ctx) {
		float fps = 0;
		float delta = Gdx.graphics.getDeltaTime();
		if(delta > 0){
			fps = 1f / Gdx.graphics.getDeltaTime();
		}
		fpsAverage.addValue(fps);
		
		// limit refresh rate
		timeout -= Gdx.graphics.getDeltaTime();
		if(timeout > 0) return;
		timeout = 1;
		
		// collect
		if(ctx.profiler.isEnabled()){
			labelDC.setText(ctx.profiler.getDrawCalls());
			labelCS.setText(ctx.profiler.getCalls());
			labelSS.setText(ctx.profiler.getShaderSwitches());
			labelTB.setText(ctx.profiler.getTextureBindings());
			labelVS.setText((int) ctx.profiler.getVertexCount().total / 3);
		}
		labelFPS.setText(MathUtils.round(fpsAverage.getMean()));
		
	}
}
