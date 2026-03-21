package thecodex6824.thaumcraftfix.client.internal;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import thaumcraft.common.entities.EntityFluxRift;

public class RiftData extends PhantomReference<EntityFluxRift> {

    public static final int NUM_TRANSPARENT_LAYERS = 3;
    public static final int NUM_LAYERS = 4;
    public static final int PIPE_NUM_SIDES = 6;

    public RiftData(EntityFluxRift rift, ReferenceQueue<? super EntityFluxRift> queue, int numPoints) {
	super(rift, queue);
	stagingBuffer = BufferUtils.createFloatBuffer(NUM_LAYERS * ((numPoints - 1) * PIPE_NUM_SIDES + 2) * 3);
	riftSize = rift.getRiftSize();
	riftSeed = rift.getRiftSeed();
	numIndices = (numPoints - 2) * 6 * PIPE_NUM_SIDES + PIPE_NUM_SIDES * 3 * 2;
	this.numPoints = numPoints;
	vaoId = GL30.glGenVertexArrays();
	vboId = GL15.glGenBuffers();
	eboId = GL15.glGenBuffers();
	cleanupDone = new AtomicBoolean(false);
    }

    public void cleanupBuffers() {
	if (cleanupDone.compareAndSet(false, true)) {
	    GL15.glDeleteBuffers(eboId);
	    GL15.glDeleteBuffers(vboId);
	    GL30.glDeleteVertexArrays(vaoId);
	}
    }

    public final int vboId;
    public final int vaoId;
    public final int eboId;
    public final int riftSize;
    public final int riftSeed;
    public final int numIndices;
    public final int numPoints;
    public final FloatBuffer stagingBuffer;
    public final AtomicBoolean cleanupDone;
}
