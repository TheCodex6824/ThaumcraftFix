package thecodex6824.thaumcraftfix.mixin.render;

import java.lang.ref.ReferenceQueue;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import thaumcraft.client.lib.ender.ShaderCallback;
import thaumcraft.client.renderers.entity.RenderFluxRift;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumcraftfix.ThaumcraftFix;
import thecodex6824.thaumcraftfix.api.ThaumcraftFixApi;
import thecodex6824.thaumcraftfix.client.internal.RiftData;

@Mixin(RenderFluxRift.class)
public abstract class RenderFluxRiftMixin extends Render<Entity> implements ISelectiveResourceReloadListener {

    @Final
    @Shadow(remap = false)
    private static ResourceLocation starsTexture;

    @Final
    @Shadow(remap = false)
    private ShaderCallback shaderCallback;

    private WeakHashMap<EntityFluxRift, RiftData> buffers = new WeakHashMap<>();
    private ReferenceQueue<EntityFluxRift> cleanupQueue = new ReferenceQueue<>();
    private Thread cleanupThread = null;
    private int shaderProgram = -1;
    private int timeUniform = -1;
    private int yawUniform = -1;
    private int pitchUniform = -1;
    private final ResourceLocation vertexShader = new ResourceLocation(ThaumcraftFixApi.MODID, "shaders/rift.vert");
    private final ResourceLocation fragmentShader = new ResourceLocation(ThaumcraftFixApi.MODID, "shaders/rift.frag");

    private RenderFluxRiftMixin() {
	super(null);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
	if (resourcePredicate.test(VanillaResourceType.SHADERS)) {
	    initShaders();
	}
    }

    private void initShaders() {
	if (shaderProgram != -1) {
	    OpenGlHelper.glDeleteProgram(shaderProgram);
	}

	IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
	int vertex = OpenGlHelper.glCreateShader(GL20.GL_VERTEX_SHADER);
	String fileContent = "";
	try {
	    fileContent = IOUtils.toString(manager.getResource(vertexShader).getInputStream(), StandardCharsets.UTF_8);
	}
	catch (Exception ex) {
	    ThaumcraftFix.instance.getLogger().error("Could not load vertex shader resource");
	}
	GL20.glShaderSource(vertex, fileContent);
	OpenGlHelper.glCompileShader(vertex);
	int status = GL20.glGetShaderi(vertex, GL20.GL_COMPILE_STATUS);
	if (status != GL11.GL_TRUE) {
	    ThaumcraftFix.instance.getLogger().error("Failed to compile vertex shader: {}",
		    OpenGlHelper.glGetShaderInfoLog(vertex, 1024));
	}
	fileContent = "";
	int fragment = OpenGlHelper.glCreateShader(GL20.GL_FRAGMENT_SHADER);
	try {
	    fileContent = IOUtils.toString(manager.getResource(fragmentShader).getInputStream(), StandardCharsets.UTF_8);
	}
	catch (Exception ex) {
	    ThaumcraftFix.instance.getLogger().error("Could not load fragment shader resource");
	}
	GL20.glShaderSource(fragment, fileContent);
	OpenGlHelper.glCompileShader(fragment);
	status = GL20.glGetShaderi(fragment, GL20.GL_COMPILE_STATUS);
	if (status != GL11.GL_TRUE) {
	    ThaumcraftFix.instance.getLogger().error("Failed to compile fragment shader: {}",
		    OpenGlHelper.glGetShaderInfoLog(fragment, 1024));
	}
	int program = OpenGlHelper.glCreateProgram();
	OpenGlHelper.glAttachShader(program, vertex);
	OpenGlHelper.glAttachShader(program, fragment);
	OpenGlHelper.glLinkProgram(program);
	status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
	if (status != GL11.GL_TRUE) {
	    ThaumcraftFix.instance.getLogger().error("Failed to link shader shader: {}",
		    OpenGlHelper.glGetProgramInfoLog(fragment, 1024));
	}
	OpenGlHelper.glDeleteShader(vertex);
	OpenGlHelper.glDeleteShader(fragment);

	shaderProgram = program;
	timeUniform = OpenGlHelper.glGetUniformLocation(program, "time");
	yawUniform = OpenGlHelper.glGetUniformLocation(program, "yaw");
	pitchUniform = OpenGlHelper.glGetUniformLocation(program, "pitch");
	int textureUniform = OpenGlHelper.glGetUniformLocation(program, "texture");
	if (textureUniform != -1) {
	    // we only need to set this once
	    OpenGlHelper.glUseProgram(program);
	    GL20.glUniform1i(textureUniform, 0);
	    OpenGlHelper.glUseProgram(0);
	}
    }

    private ShortBuffer extrudePipe(int numPoints) {
	int currentVertex = 1;
	ShortBuffer out = BufferUtils.createShortBuffer((numPoints - 2) * 6 * RiftData.PIPE_NUM_SIDES + RiftData.PIPE_NUM_SIDES * 3 * 2);
	for (int i = 0; i < RiftData.PIPE_NUM_SIDES - 1; ++i) {
	    out.put((short) (currentVertex + 1));
	    out.put((short) currentVertex++);
	    out.put((short) 0);
	}
	out.put((short) 1);
	out.put((short) currentVertex++);
	out.put((short) 0);

	for (int i = 0; i < numPoints - 3; ++i) {
	    for (int j = 0; j < RiftData.PIPE_NUM_SIDES - 1; ++j) {
		out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES + 1));
		out.put((short) (currentVertex + 1));
		out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES));
		out.put((short) (currentVertex + 1));
		out.put((short) currentVertex);
		out.put((short) (currentVertex++ - RiftData.PIPE_NUM_SIDES));
	    }
	    out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES * 2 + 1));
	    out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES + 1));
	    out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES));
	    out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES + 1));
	    out.put((short) currentVertex);
	    out.put((short) (currentVertex++ - RiftData.PIPE_NUM_SIDES));
	}

	for (int i = 0; i < RiftData.PIPE_NUM_SIDES - 1; ++i) {
	    out.put((short) currentVertex);
	    out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES + i));
	    out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES + i + 1));
	}
	out.put((short) currentVertex);
	out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES + 5));
	out.put((short) (currentVertex - RiftData.PIPE_NUM_SIDES));

	out.rewind();
	return out;
    }

    private RiftData getOrCreateVertexData(EntityFluxRift entity) {
	RiftData data = buffers.compute(entity, (rift, existing) -> {
	    int adjustedPointCount = rift.points.size() - 2;
	    if (existing == null || existing.riftSize != rift.getRiftSize() ||
		    existing.riftSeed != rift.getRiftSeed() || existing.numPoints != adjustedPointCount) {

		if (existing != null) {
		    existing.cleanupBuffers();
		}

		existing = new RiftData(rift, cleanupQueue, adjustedPointCount);
		GL30.glBindVertexArray(existing.vaoId);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, existing.vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 3 * 4 * ((adjustedPointCount - 1) * RiftData.PIPE_NUM_SIDES + 2) * RiftData.NUM_LAYERS, GL15.GL_STREAM_DRAW);

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, existing.eboId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, extrudePipe(adjustedPointCount), GL15.GL_STATIC_DRAW);

		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(0);

		GL30.glBindVertexArray(0);
	    }
	    return existing;
	});

	return data;
    }

    private void setUniforms(float pt) {
	Minecraft mc = Minecraft.getMinecraft();
	Entity view = mc.getRenderViewEntity() != null ? mc.getRenderViewEntity() : mc.player;
	if (timeUniform != -1) {
	    GL20.glUniform1f(timeUniform, view.ticksExisted + pt);
	}
	if (yawUniform != -1) {
	    GL20.glUniform1f(yawUniform, (float) (view.rotationYaw * 2.0F * Math.PI / 360.0F));
	}
	if (pitchUniform != -1) {
	    GL20.glUniform1f(pitchUniform, (float) (-view.rotationPitch * 2.0F * Math.PI / 360.0F));
	}

	GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
	bindTexture(starsTexture);
    }

    private void renderOptimized(EntityFluxRift entity, double x, double y, double z, float yaw, float pt) {
	Minecraft mc = Minecraft.getMinecraft();
	mc.profiler.startSection("TcFixRenderFluxRiftOptimized");
	if (cleanupThread == null) {
	    cleanupThread = new Thread(() -> {
		try {
		    while (true) {
			RiftData data = (RiftData) cleanupQueue.remove();
			// freeing buffers must happen on opengl thread
			mc.addScheduledTask(() -> { data.cleanupBuffers(); });
		    }
		}
		catch (InterruptedException ex) {}
	    }, "TC Fix Native Resource Cleanup Thread");
	    cleanupThread.setDaemon(true);
	    cleanupThread.start();
	}

	if (shaderProgram == -1) {
	    IResourceManager manager = mc.getResourceManager();
	    if (manager instanceof IReloadableResourceManager) {
		((IReloadableResourceManager) manager).registerReloadListener(this);
	    }
	    initShaders();
	}

	// I considered checking the render view entity here, but looking through a telescope shouldn't hide flux rifts
	// new API sometime maybe?
	boolean goggles = EntityUtils.hasGoggles(mc.player);
	float stability = MathHelper.clamp(1.0F - entity.getRiftStability() / 50.0F, 0.0F, 1.5F);
	mc.profiler.startSection("TcFixGetFluxRiftCachedData");
	RiftData data = getOrCreateVertexData(entity);
	mc.profiler.endSection();
	data.stagingBuffer.clear();
	Vec3d up = new Vec3d(0.0, 1.0, 0.0);
	mc.profiler.startSection("TcFixPerFrameFluxRiftData");
	for (int layer = 0; layer < RiftData.NUM_LAYERS; ++layer) {
	    // visual observation suggests that point 0 is not rendered in the normal renderer
	    Vec3d point = entity.points.get(1);
	    float pointDrift = entity.ticksExisted + pt + 10;
	    data.stagingBuffer.put((float) point.x + MathHelper.sin(pointDrift / 50.0F) * 0.1F * stability);
	    data.stagingBuffer.put((float) point.y + MathHelper.sin(pointDrift / 60.0F) * 0.1F * stability);
	    data.stagingBuffer.put((float) point.z + MathHelper.sin(pointDrift / 70.0F) * 0.1F * stability);
	    for (int i = 2; i < entity.points.size() - 1; ++i) {
		pointDrift = entity.ticksExisted + pt;
		if (i > entity.points.size() / 2) {
		    pointDrift -= i * 10;
		}
		else {
		    pointDrift += i * 10;
		}
		double radiusDrift = 1.0 - Math.sin(pointDrift / 8.0F) * 0.1 * stability;
		float xMod = MathHelper.sin(pointDrift / 50.0F) * 0.1F * stability;
		float yMod = MathHelper.sin(pointDrift / 60.0F) * 0.1F * stability;
		float zMod = MathHelper.sin(pointDrift / 70.0F) * 0.1F * stability;
		point = entity.points.get(i);
		Vec3d axis = entity.points.get(i + 1).subtract(entity.points.get(i - 1)).normalize();
		float width = entity.pointsWidth.get(i);
		// thaumcraft sometimes sends widths of 0 for inner points with small rift sizes
		if (width < 0.000001F) {
		    width = entity.getRiftSize() * 0.003125F;
		}
		Vec3d dirVec = axis.crossProduct(up).normalize().scale(width * radiusDrift * (layer < RiftData.NUM_LAYERS - 1 ? 1.25F + 0.5F * layer : 1.0F));
		float angleMod = (float) Math.PI * 2.0F / RiftData.PIPE_NUM_SIDES;
		for (int j = 0; j < RiftData.PIPE_NUM_SIDES; ++j) {
		    Vec3d newPoint = point.add(dirVec);
		    data.stagingBuffer.put((float) newPoint.x + xMod);
		    data.stagingBuffer.put((float) newPoint.y + yMod);
		    data.stagingBuffer.put((float) newPoint.z + zMod);
		    Vec3d term1 = dirVec.scale(MathHelper.cos(angleMod));
		    Vec3d term2 = axis.crossProduct(dirVec).scale(MathHelper.sin(angleMod));
		    Vec3d term3 = axis.scale(axis.dotProduct(dirVec) * (1.0F - MathHelper.cos(angleMod)));
		    dirVec = term1.add(term2).add(term3);
		}
	    }
	    point = entity.points.get(entity.points.size() - 1);
	    pointDrift = entity.ticksExisted + pt - (entity.points.size() - 2) * 10;
	    data.stagingBuffer.put((float) point.x + MathHelper.sin(pointDrift / 50.0F) * 0.1F * stability);
	    data.stagingBuffer.put((float) point.y + MathHelper.sin(pointDrift / 60.0F) * 0.1F * stability);
	    data.stagingBuffer.put((float) point.z + MathHelper.sin(pointDrift / 70.0F) * 0.1F * stability);
	}
	data.stagingBuffer.rewind();
	mc.profiler.endSection();

	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.vboId);
	GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data.stagingBuffer, GL15.GL_STREAM_DRAW);
	// TODO: batch entity rendering to not constantly bind/unbind shader?
	//ShaderHelper.useShader(ShaderHelper.endShader, shaderCallback);
	OpenGlHelper.glUseProgram(shaderProgram);
	setUniforms(pt);
	GlStateManager.enableBlend();
	GlStateManager.pushMatrix();
	GlStateManager.translate(x, y, z);
	mc.profiler.startSection("TcFixFluxRiftDrawElements");
	Entity renderView = mc.getRenderViewEntity() != null ? mc.getRenderViewEntity() : mc.player;
	for (int layer = 0; layer < RiftData.NUM_LAYERS; ++layer) {
	    if (layer < RiftData.NUM_TRANSPARENT_LAYERS) {
		GlStateManager.depthMask(false);
		if (layer == 0 && goggles) {
		    GlStateManager.disableDepth();
		}
	    }
	    else if (entity.getEntityBoundingBox().intersects(renderView.getEntityBoundingBox())) {
		// disable culling so standing inside a rift isn't as weird looking
		// only do this for the nontransparent inner part though
		GlStateManager.disableCull();
	    }
	    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, layer < RiftData.NUM_TRANSPARENT_LAYERS ? DestFactor.ONE : DestFactor.ONE_MINUS_SRC_ALPHA);

	    GL30.glBindVertexArray(data.vaoId);
	    GL32.glDrawElementsBaseVertex(GL11.GL_TRIANGLES, data.numIndices, GL11.GL_UNSIGNED_SHORT,
		    0, layer * ((entity.points.size() - 3) * RiftData.PIPE_NUM_SIDES + 2));

	    if (layer < RiftData.NUM_TRANSPARENT_LAYERS) {
		if (layer == 0 && goggles) {
		    GlStateManager.enableDepth();
		}
		GlStateManager.depthMask(true);
	    }
	    else if (entity.getEntityBoundingBox().intersects(renderView.getEntityBoundingBox())) {
		GlStateManager.enableCull();
	    }
	}
	mc.profiler.endSection();
	GL30.glBindVertexArray(0);
	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	GlStateManager.popMatrix();
	GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
	GlStateManager.disableBlend();
	OpenGlHelper.glUseProgram(0);
	mc.profiler.endSection();
    }

    private static boolean compatCheckDone = false;
    private static boolean compatResult = false;

    private boolean checkCompat() {
	if (!compatCheckDone) {
	    compatResult = GLContext.getCapabilities().OpenGL33;
	    if (!compatResult && Minecraft.getMinecraft().ingameGUI != null) {
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(
			new TextComponentTranslation("thaumcraftfix.text.renderer.failedRiftRendererRequirements")
			.setStyle(new Style().setColor(TextFormatting.YELLOW)));
	    }
	    compatCheckDone = true;
	}

	return compatResult;
    }

    @WrapMethod(method = "doRender")
    @SuppressWarnings("deprecation")
    private void wrapDoRender(Entity entity, double x, double y, double z, float yaw, float pt, Operation<Void> original) {
	Minecraft mc = Minecraft.getMinecraft();
	Profiler profiler = mc.profiler;
	profiler.startSection(RenderFluxRift.class);
	if (entity instanceof EntityFluxRift && ThaumcraftFix.instance.getConfig().client.optimizedFluxRiftRenderer.value() &&
		checkCompat() && mc.gameSettings.useVbo && ((EntityFluxRift) entity).points.size() > 2) {
	    renderOptimized((EntityFluxRift) entity, x, y, z, yaw, pt);
	}
	else {
	    original.call(entity, x, y, z, yaw, pt);
	}
	profiler.endSection();
    }

}
