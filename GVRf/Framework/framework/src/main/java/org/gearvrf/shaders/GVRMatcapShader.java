package org.gearvrf.shaders;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

public class GVRMatcapShader extends GVRShader {
    private static String fragTemplate;
    private static String vtxTemplate;

    public GVRMatcapShader(GVRContext gvrContext) {
        super("", "sampler2D u_texture", "float3 a_position float3 a_normal", GLSLESVersion.VULKAN);
        fragTemplate = TextFile.readTextFile(gvrContext.getContext(), R.raw.matcapfrag);
        vtxTemplate = TextFile.readTextFile(gvrContext.getContext(), R.raw.matcapvert);

        setSegment("FragmentTemplate", fragTemplate);
        setSegment("VertexTemplate", vtxTemplate);

    }
}
