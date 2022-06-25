package com.badlogic.gdx.backends.lwjgl3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;

import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.GL31;

public class Lwjgl3GL31 extends Lwjgl3GL30 implements GL31 {
	
	private final static ByteBuffer tmpByteBuffer = BufferUtils.newByteBuffer(16);
	
	protected void notSupported(){
		throw new GdxRuntimeException("not supported");
	}

	@Override
	public void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
		GL43.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
	}

	@Override
	public void glDispatchComputeIndirect(long indirect) {
		GL43.glDispatchComputeIndirect(indirect);
	}

	@Override
	public void glDrawArraysIndirect(int mode, long indirect) {
		GL40.glDrawArraysIndirect(mode, indirect);
	}

	@Override
	public void glDrawElementsIndirect(int mode, int type, long indirect) {
		GL40.glDrawElementsIndirect(mode, type, indirect);;
	}

	@Override
	public void glFramebufferParameteri(int target, int pname, int param) {
		GL43.glFramebufferParameteri(target, pname, param);
	}

	@Override
	public void glGetFramebufferParameteriv(int target, int pname, int[] params, int offset) {
		notSupported();
		// GL46.glGetFramebufferParameteriv
	}

	@Override
	public void glGetFramebufferParameteriv(int target, int pname, IntBuffer params) {
		GL43.glGetFramebufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params) {
		GL43.glGetProgramInterfaceiv(program, programInterface, pname, params);
	}

	@Override
	public int glGetProgramResourceIndex(int program, int programInterface, String name) {
		return GL43.glGetProgramResourceIndex(program, programInterface, name);
	}

	@Override
	public String glGetProgramResourceName(int program, int programInterface, int index) {
		return GL43.glGetProgramResourceName(program, programInterface, index);
	}

	@Override
	public void glGetProgramResourceiv(int program, int programInterface, int index, int propCount, int[] props,
			int propsOffset, int bufSize, int[] length, int lengthOffset, int[] params, int paramsOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetProgramResourceiv(int program, int programInterface, int index, int propCount, IntBuffer props,
			int bufSize, IntBuffer length, IntBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int glGetProgramResourceLocation(int program, int programInterface, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glUseProgramStages(int pipeline, int stages, int program) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glActiveShaderProgram(int pipeline, int program) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int glCreateShaderProgramv(int type, String[] strings) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glBindProgramPipeline(int pipeline) {
		GL41.glBindProgramPipeline(pipeline);
	}

	@Override
	public void glDeleteProgramPipelines(int n, int[] pipelines, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glDeleteProgramPipelines(int n, IntBuffer pipelines) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGenProgramPipelines(int n, int[] pipelines, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGenProgramPipelines(int n, IntBuffer pipelines) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean glIsProgramPipeline(int pipeline) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void glGetProgramPipelineiv(int pipeline, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetProgramPipelineiv(int pipeline, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1i(int program, int location, int v0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2i(int program, int location, int v0, int v1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform3i(int program, int location, int v0, int v1, int v2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1ui(int program, int location, int v0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2ui(int program, int location, int v0, int v1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform3ui(int program, int location, int v0, int v1, int v2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1f(int program, int location, float v0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2f(int program, int location, float v0, float v1) {
		GL41.glProgramUniform2f(program, location, v0, v1);
	}

	@Override
	public void glProgramUniform3f(int program, int location, float v0, float v1, float v2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1iv(int program, int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1iv(int program, int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2iv(int program, int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2iv(int program, int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform3iv(int program, int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform3iv(int program, int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4iv(int program, int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4iv(int program, int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1uiv(int program, int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1uiv(int program, int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2uiv(int program, int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2uiv(int program, int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform3uiv(int program, int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform3uiv(int program, int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4uiv(int program, int location, int count, int[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4uiv(int program, int location, int count, IntBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1fv(int program, int location, int count, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform1fv(int program, int location, int count, FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2fv(int program, int location, int count, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform2fv(int program, int location, int count, FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform3fv(int program, int location, int count, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform3fv(int program, int location, int count, FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4fv(int program, int location, int count, float[] value, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniform4fv(int program, int location, int count, FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix2x3fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix2x3fv(int program, int location, int count, boolean transpose,
			FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix3x2fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix3x2fv(int program, int location, int count, boolean transpose,
			FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix2x4fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix2x4fv(int program, int location, int count, boolean transpose,
			FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix4x2fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix4x2fv(int program, int location, int count, boolean transpose,
			FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose,
			FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose, float[] value,
			int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose,
			FloatBuffer value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glValidateProgramPipeline(int pipeline) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String glGetProgramPipelineInfoLog(int program) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access,
			int format) {
		GL42.glBindImageTexture(unit, texture, level, layered, layer, access, format);
	}

	@Override
	public void glGetBooleani_v(int target, int index, IntBuffer data) {
		GL46.glGetBooleani_v(target, index, tmpByteBuffer);
		data.put(tmpByteBuffer.asIntBuffer());
	}

	@Override
	public void glMemoryBarrier(int barriers) {
		GL42.glMemoryBarrier(barriers);
	}

	@Override
	public void glMemoryBarrierByRegion(int barriers) {
		GL46.glMemoryBarrierByRegion(barriers);
	}

	@Override
	public void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height,
			boolean fixedsamplelocations) {
		GL43.glTexStorage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
	}

	@Override
	public void glGetMultisamplefv(int pname, int index, float[] val, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetMultisamplefv(int pname, int index, FloatBuffer val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glSampleMaski(int maskNumber, int mask) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetTexLevelParameteriv(int target, int level, int pname, int[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetTexLevelParameteriv(int target, int level, int pname, IntBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetTexLevelParameterfv(int target, int level, int pname, float[] params, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glGetTexLevelParameterfv(int target, int level, int pname, FloatBuffer params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
		GL43.glBindVertexBuffer(bindingindex, buffer, offset, stride);
	}

	@Override
	public void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glVertexAttribBinding(int attribindex, int bindingindex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glVertexBindingDivisor(int bindingindex, int divisor) {
		// TODO Auto-generated method stub
		
	}


}
