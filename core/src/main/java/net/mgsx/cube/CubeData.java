package net.mgsx.cube;

import java.nio.ByteBuffer;

import com.badlogic.gdx.math.Vector3;

public class CubeData {
	public enum Type {
		TYPE_1D, TYPE_3D
	}
	public Type type;
	public String title;
	public Integer dimSize;
	public Vector3 domainMin;
	public Vector3 domainMax;
	public ByteBuffer buffer;
}
