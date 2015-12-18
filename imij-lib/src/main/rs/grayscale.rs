#pragma version(1)
#pragma rs java_package_name(me.mtbii.imij_lib)

//#include "rs_graphics.rsh"

const static float3 gGrayFactor = { 0.299, 0.587, 0.114 };

uchar4 __attribute__((kernel)) grayscale(uchar4 in)
{
    float4 f4 = rsUnpackColor8888(in);
    float3 mix = dot(f4.rgb, gGrayFactor);
    float val = mix.r + mix.g + mix.b;
    float4 result = {val, val, val, 1.0f};
    return rsPackColorTo8888(result);
}