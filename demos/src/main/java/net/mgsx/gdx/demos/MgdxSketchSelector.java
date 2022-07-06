package net.mgsx.gdx.demos;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import net.mgsx.gdx.demos.sketches.BenchmarkFileFormatSketch;
import net.mgsx.gdx.demos.sketches.DefaultSketch;
import net.mgsx.gdx.demos.sketches.GL30Texture3DSketch;
import net.mgsx.gdx.demos.sketches.GL30TransformFeedbackSketch;
import net.mgsx.gdx.demos.sketches.GL31ComputeShaderChainSketch;
import net.mgsx.gdx.demos.sketches.GL31ComputeShaderSSBOSketch;
import net.mgsx.gdx.demos.sketches.GL31ComputeShaderVBOSketch;
import net.mgsx.gdx.demos.sketches.GL31ComputeShadersSketch;
import net.mgsx.gdx.demos.sketches.GL31FrameBufferMultisampleSketch;
import net.mgsx.gdx.demos.sketches.GL31ProgramPipelineSketch;
import net.mgsx.gdx.demos.sketches.GL32BlendingMRTSketch;
import net.mgsx.gdx.demos.sketches.GL32DebugControlSketch;
import net.mgsx.gdx.demos.sketches.GL32GeometryShaderSketch;
import net.mgsx.gdx.demos.sketches.GL32TesselationShaderSimpleSketch;
import net.mgsx.gdx.demos.sketches.GL32TesselationShaderSketch;
import net.mgsx.gdx.demos.sketches.HDRBlurCascadeSketech;
import net.mgsx.gdx.demos.sketches.KTX2Sktech;

public class MgdxSketchSelector extends Dialog
{
	private static final Array<Class<? extends Screen>> sketches = new Array<Class<? extends Screen>>();
	static{
		sketches.addAll(
			DefaultSketch.class,
			BenchmarkFileFormatSketch.class,
			// EXRSketch.class,
			KTX2Sktech.class,
			// GLTFHDRRenderingSketch.class,
			// HDRISketch.class,
			HDRBlurCascadeSketech.class,
			GL30Texture3DSketch.class,
			GL30TransformFeedbackSketch.class,
			GL31ComputeShaderChainSketch.class,
			GL31ComputeShaderVBOSketch.class,
			GL31ComputeShaderSSBOSketch.class,
			GL31ProgramPipelineSketch.class,
			GL31FrameBufferMultisampleSketch.class,
			GL31ComputeShadersSketch.class,
			GL32DebugControlSketch.class,
			GL32GeometryShaderSketch.class,
			GL32TesselationShaderSimpleSketch.class,
			GL32TesselationShaderSketch.class,
			GL32BlendingMRTSketch.class
			);
	}
	
	private Game game;
	private Screen current;
	
	public MgdxSketchSelector(Game game, Skin skin) {
		super("MGdx Sketches", skin);
		this.game = game;
		setModal(false);
		SelectBox<Class<? extends Screen>> selector = new SelectBox<Class<? extends Screen>>(getSkin()){
			@Override
			protected String toString(Class<? extends Screen> item) {
				return item.getSimpleName();
			}
		};
		selector.setItems(sketches);
		selector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setSketch(selector.getSelected());
			}
		});
		getContentTable().add(selector).row();
		setSketch(sketches.first());
	}

	private void setSketch(Class<? extends Screen> sketch) {
		if(current != null){
			current.dispose();
			current = null;
		}
		try {
			current = ClassReflection.newInstance(sketch);
			game.setScreen(current);
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
}
