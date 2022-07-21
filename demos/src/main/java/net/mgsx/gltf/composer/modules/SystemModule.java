package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import net.mgsx.gdx.graphics.glutils.GpuUtils;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.AdvancedProfiler;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.ui.CUI;

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
	private Label memoryCpuLabel;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		controls.defaults().growX();
		
		UI.header(controls, "Profiling");
		
		// Options
		Frame oFrame = UI.frame("Frames", skin);
		controls.add(oFrame).row();
		Table oTable = oFrame.getContentTable();
		oTable.defaults().left();
		UI.toggle(oTable, "GPU VSync", ctx.vsync, v->{
			ctx.vsync = v;
			Gdx.graphics.setVSync(ctx.vsync);
			fpsAverage.clear();
			timeout = 0;
		});
		UI.toggle(oTable, "CPU FPS Sync", ctx.fsync, v->{
			ctx.fsync = v;
			if(ctx.fsync){
				Gdx.graphics.setForegroundFPS(ctx.ffps);
			}else{
				Gdx.graphics.setForegroundFPS(0);
			}
			fpsAverage.clear();
			timeout = 0;
		});
		
		{
			Table fpsTable = UI.table(skin);
			oTable.add(fpsTable).row();
			labelFPS = entry(fpsTable, "FPS");
		}
		
		// Info
		
		Frame iFrame = UI.frame("Info", skin);
		controls.add(iFrame).row();
		Table iTable = iFrame.getContentTable();
		
		// version
		{
			GLVersion ver = Gdx.graphics.getGLVersion();
			iTable.add(ver.getVendorString()).colspan(2).row();
			iTable.add(ver.getRendererString()).colspan(2).row();
			iTable.add(ver.getType() + " " + ver.getMajorVersion() + "." + ver.getMinorVersion() + "." + ver.getReleaseVersion()).colspan(2).row();
		}
		
		// GPU memory
		{
			iTable.add("GPU memory");
			Table t = UI.table(skin);
			if(GpuUtils.hasMemoryInfo()){
				memoryMaxKB = GpuUtils.getMaxMemoryKB();
				memoryLabel = t.add("").getActor();
				memoryLabel.setColor(CUI.dynamicLabelColor);
				t.add(" / " + (memoryMaxKB / 1024) + " MB");
			}else{
				t.add("unknown");
			}
			iTable.add(t).row();
		}
		{
			iTable.add("JVM memory");
			Table t = UI.table(skin);
			int maxKB = (int)(Runtime.getRuntime().maxMemory() / 1024);
			memoryCpuLabel = t.add("").getActor();
			memoryCpuLabel.setColor(CUI.dynamicLabelColor);
			t.add(" / " + (maxKB / 1024) + " MB");
			iTable.add(t).row();
		}
		
		// Stats
		Frame profilerFrame = UI.frameToggle("GL Profiler", skin,  ctx.profiler.isEnabled(), v->{
			if(!ctx.profiler.isEnabled()) ctx.profiler.enable(); else ctx.profiler.disable();
		});
		controls.add(profilerFrame).row();
		
		Table stats = profilerFrame.getContentTable();
		
		labelDC = entry(stats, "draw calls");
		labelCS = entry(stats, "calls");
		labelSS = entry(stats, "shader switches");
		labelTB = entry(stats, "texture bindings");
		labelVS = entry(stats, "polygon count");
		
		return controls;
	}
	
	private Label entry(Table table, String title) {
		table.add(title);
		Label label = table.add("--").width(60).getActor();
		label.setAlignment(Align.left);
		label.setColor(CUI.dynamicLabelColor);
		table.row();
		return label;
	}

	public void beginProfiling(GLTFComposerContext ctx) {
		ctx.profiler.reset();
	}

	public void endProfiling(GLTFComposerContext ctx) {
		float fps = 0;
		float delta = Gdx.graphics.getDeltaTime();
		if(delta > 0){
			fps = 1f / delta;
		}
		fpsAverage.addValue(fps);
		
		// limit refresh rate
		timeout -= delta;
		if(timeout > 0) return;
		timeout = 1;
		
		if(memoryLabel != null){
			memoryLabel.setText((memoryMaxKB - GpuUtils.getAvailableMemoryKB()) / 1024);
		}
		if(memoryCpuLabel != null){
			int used = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20);
			memoryCpuLabel.setText(used);
		}
		
		// collect
		if(ctx.profiler.isEnabled()){
			labelDC.setText(ctx.profiler.getDrawCalls());
			labelCS.setText(ctx.profiler.getCalls());
			labelSS.setText(ctx.profiler.getShaderSwitches());
			labelTB.setText(ctx.profiler.getTextureBindings());
			labelVS.setText((int) ctx.profiler.getVertexCount().total / 3);
		}
		if(fpsAverage.hasEnoughData()){
			labelFPS.setText(MathUtils.round(fpsAverage.getMean()));
		}else{
			labelFPS.setText("--");
		}
		
	}
}
