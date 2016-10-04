package waterpower.client.render;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import java.util.*;

import waterpower.client.render.item.IItemIconProvider;
import waterpower.common.item.GlobalItems;

public class IconRegisterService implements IBakedModel {
	
	public static final IconRegisterService INSTANCE = new IconRegisterService();
	
	private IconRegisterService() {
	}
	
	public TextureMap map; 
	public final List<IIconRegister> items = new LinkedList<>();
	
	
	@SubscribeEvent
	public void onPreTextureMapStitch(TextureStitchEvent.Pre event) {
		TextureMap map = event.getMap();
		for (IIconRegister runnable : items) {
			runnable.registerIcons(map);
		}
		for (Item item : GlobalItems.items) {
			if (item instanceof IIconRegister) {
				((IIconRegister) item).registerIcons(map);
			}
		}
	}
	
	//-----------------------------------------
    // IBakedModel
    //-----------------------------------------

    private static final ModelResourceLocation RESOURCE_LOCATION = new ModelResourceLocation("waterpower", "IItemIconProvider");

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent bakeEvent) {
        System.out.println("Models bake");
        bakeEvent.getModelRegistry().putObject(RESOURCE_LOCATION, this);
    }

    public static void setupItemModels() {
        for(Item item : GlobalItems.items) {
            if(item instanceof IItemIconProvider) {
                for (int i = 0; i < Short.MAX_VALUE; i++) {
                    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, i, RESOURCE_LOCATION);
                }
            }
        }
    }
	
    private ItemStack itemStack;
    private VertexFormat vertexFormat = DefaultVertexFormats.ITEM;
    
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

        if (side == null && itemStack != null) {
            ArrayList<BakedQuad> resultQuads = new ArrayList<>();
            IItemIconProvider iconProvider = (IItemIconProvider) itemStack.getItem();
            for(int i = 0; i < iconProvider.getRenderPasses(itemStack) + 1; i++) {
                TextureAtlasSprite atlasSprite = iconProvider.getIcon(itemStack, i);
                if(atlasSprite != null) {
                    resultQuads.addAll(getQuadsForSprite(i, atlasSprite, vertexFormat, Optional.absent()));
                }
            }
            return resultQuads;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        if(itemStack != null) {
            IItemIconProvider iconProvider = (IItemIconProvider) itemStack.getItem();
            return iconProvider.getIcon(itemStack, 0);
        }
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        if(itemStack != null) {
            IItemIconProvider iconProvider = (IItemIconProvider) itemStack.getItem();
            return iconProvider.isHandheld(itemStack) ? Models.HANDHELD_TRANSFORMS : Models.DEFAULT_TRANSFORMS;
        }
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList(Collections.<ItemOverride>emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
                if(stack.getItem() instanceof ItemBlock) {
                     return null;
                }

                IconRegisterService.this.itemStack = stack;
                return IconRegisterService.this;
            }
        };
    }

    public static ImmutableList<BakedQuad> getQuadsForSprite(int tint, TextureAtlasSprite sprite, VertexFormat format, Optional<TRSRTransformation> transform)
    {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        int uMax = sprite.getIconWidth();
        int vMax = sprite.getIconHeight();

        BitSet faces = new BitSet((uMax + 1) * (vMax + 1) * 4);
        for(int f = 0; f < sprite.getFrameCount(); f++)
        {
            int[] pixels = sprite.getFrameTextureData(f)[0];
            boolean ptu;
            boolean[] ptv = new boolean[uMax];
            Arrays.fill(ptv, true);
            for(int v = 0; v < vMax; v++)
            {
                ptu = true;
                for(int u = 0; u < uMax; u++)
                {
                    boolean t = isTransparent(pixels, uMax, vMax, u, v);
                    if(ptu && !t) // left - transparent, right - opaque
                    {
                        addSideQuad(builder, faces, format, transform, EnumFacing.WEST, tint, sprite, uMax, vMax, u, v);
                    }
                    if(!ptu && t) // left - opaque, right - transparent
                    {
                        addSideQuad(builder, faces, format, transform, EnumFacing.EAST, tint, sprite, uMax, vMax, u, v);
                    }
                    if(ptv[u] && !t) // up - transparent, down - opaque
                    {
                        addSideQuad(builder, faces, format, transform, EnumFacing.UP, tint, sprite, uMax, vMax, u, v);
                    }
                    if(!ptv[u] && t) // up - opaque, down - transparent
                    {
                        addSideQuad(builder, faces, format, transform, EnumFacing.DOWN, tint, sprite, uMax, vMax, u, v);
                    }
                    ptu = t;
                    ptv[u] = t;
                }
                if(!ptu) // last - opaque
                {
                    addSideQuad(builder, faces, format, transform, EnumFacing.EAST, tint, sprite, uMax, vMax, uMax, v);
                }
            }
            // last line
            for(int u = 0; u < uMax; u++)
            {
                if(!ptv[u])
                {
                    addSideQuad(builder, faces, format, transform, EnumFacing.DOWN, tint, sprite, uMax, vMax, u, vMax);
                }
            }
        }
        // front
        builder.add(buildQuad(format, transform, EnumFacing.NORTH, sprite, tint,
                0, 0, 7.5f / 16f, sprite.getMinU(), sprite.getMaxV(),
                0, 1, 7.5f / 16f, sprite.getMinU(), sprite.getMinV(),
                1, 1, 7.5f / 16f, sprite.getMaxU(), sprite.getMinV(),
                1, 0, 7.5f / 16f, sprite.getMaxU(), sprite.getMaxV()
        ));
        // back
        builder.add(buildQuad(format, transform, EnumFacing.SOUTH, sprite, tint,
                0, 0, 8.5f / 16f, sprite.getMinU(), sprite.getMaxV(),
                1, 0, 8.5f / 16f, sprite.getMaxU(), sprite.getMaxV(),
                1, 1, 8.5f / 16f, sprite.getMaxU(), sprite.getMinV(),
                0, 1, 8.5f / 16f, sprite.getMinU(), sprite.getMinV()
        ));
        return builder.build();
    }

    private static boolean isTransparent(int[] pixels, int uMax, int vMax, int u, int v)
    {
        return (pixels[u + (vMax - 1 - v) * uMax] >> 24 & 0xFF) == 0;
    }

    private static void addSideQuad(ImmutableList.Builder<BakedQuad> builder, BitSet faces, VertexFormat format, Optional<TRSRTransformation> transform, EnumFacing side, int tint, TextureAtlasSprite sprite, int uMax, int vMax, int u, int v)
    {
        int si = side.ordinal();
        if(si > 4) si -= 2;
        int index = (vMax + 1) * ((uMax + 1) * si + u) + v;
        if(!faces.get(index))
        {
            faces.set(index);
            builder.add(buildSideQuad(format, transform, side, tint, sprite, u, v));
        }
    }

    private static BakedQuad buildSideQuad(VertexFormat format, Optional<TRSRTransformation> transform, EnumFacing side, int tint, TextureAtlasSprite sprite, int u, int v)
    {
        final float eps0 = 30e-5f;
        final float eps1 = 45e-5f;
        final float eps2 = .5f;
        final float eps3 = .5f;
        float x0 = (float)u / sprite.getIconWidth();
        float y0 = (float)v / sprite.getIconHeight();
        float x1 = x0, y1 = y0;
        float z1 = 7.5f / 16f - eps1, z2 = 8.5f / 16f + eps1;
        switch(side)
        {
            case WEST:
                z1 = 8.5f / 16f + eps1;
                z2 = 7.5f / 16f - eps1;
            case EAST:
                y1 = (v + 1f) / sprite.getIconHeight();
                break;
            case DOWN:
                z1 = 8.5f / 16f + eps1;
                z2 = 7.5f / 16f - eps1;
            case UP:
                x1 = (u + 1f) / sprite.getIconWidth();
                break;
            default:
                throw new IllegalArgumentException("can't handle z-oriented side");
        }
        float u0 = 16f * (x0 - side.getDirectionVec().getX() * eps3 / sprite.getIconWidth());
        float u1 = 16f * (x1 - side.getDirectionVec().getX() * eps3 / sprite.getIconWidth());
        float v0 = 16f * (1f - y0 - side.getDirectionVec().getY() * eps3 / sprite.getIconHeight());
        float v1 = 16f * (1f - y1 - side.getDirectionVec().getY() * eps3 / sprite.getIconHeight());
        switch(side)
        {
            case WEST:
            case EAST:
                y0 -= eps1;
                y1 += eps1;
                v0 -= eps2 / sprite.getIconHeight();
                v1 += eps2 / sprite.getIconHeight();
                break;
            case DOWN:
            case UP:
                x0 -= eps1;
                x1 += eps1;
                u0 += eps2 / sprite.getIconWidth();
                u1 -= eps2 / sprite.getIconWidth();
                break;
            default:
                throw new IllegalArgumentException("can't handle z-oriented side");
        }
        switch(side)
        {
            case WEST:
                x0 += eps0;
                x1 += eps0;
                break;
            case EAST:
                x0 -= eps0;
                x1 -= eps0;
                break;
            case DOWN:
                y0 -= eps0;
                y1 -= eps0;
                break;
            case UP:
                y0 += eps0;
                y1 += eps0;
                break;
            default:
                throw new IllegalArgumentException("can't handle z-oriented side");
        }
        return buildQuad(
                format, transform, side.getOpposite(), sprite, tint, // getOpposite is related either to the swapping of V direction, or something else
                x0, y0, z1, sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0),
                x1, y1, z1, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1),
                x1, y1, z2, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1),
                x0, y0, z2, sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0)
        );
    }

    private static BakedQuad buildQuad(
            VertexFormat format, Optional<TRSRTransformation> transform, EnumFacing side, TextureAtlasSprite sprite, int tint,
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3)
    {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setQuadTint(tint);
        builder.setQuadOrientation(side);
        builder.setTexture(sprite);
        putVertex(builder, format, transform, side, x0, y0, z0, u0, v0);
        putVertex(builder, format, transform, side, x1, y1, z1, u1, v1);
        putVertex(builder, format, transform, side, x2, y2, z2, u2, v2);
        putVertex(builder, format, transform, side, x3, y3, z3, u3, v3);
        return builder.build();
    }

    private static void putVertex(UnpackedBakedQuad.Builder builder, VertexFormat format, Optional<TRSRTransformation> transform, EnumFacing side, float x, float y, float z, float u, float v)
    {
        Vector4f vec = new Vector4f();
        for(int e = 0; e < format.getElementCount(); e++)
        {
            switch(format.getElement(e).getUsage())
            {
                case POSITION:
                    if(transform.isPresent())
                    {
                        vec.x = x;
                        vec.y = y;
                        vec.z = z;
                        vec.w = 1;
                        transform.get().getMatrix().transform(vec);
                        builder.put(e, vec.x, vec.y, vec.z, vec.w);
                    }
                    else
                    {
                        builder.put(e, x, y, z, 1);
                    }
                    break;
                case COLOR:
                    builder.put(e, 1f, 1f, 1f, 1f);
                    break;
                case UV: if(format.getElement(e).getIndex() == 0)
                {
                    builder.put(e, u, v, 0f, 1f);
                    break;
                }
                case NORMAL:
                    builder.put(e, side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ(), 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }
}
