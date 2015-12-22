#pragma version(1)
#pragma rs java_package_name(me.mtbii.imij_lib)


//#include "rs_graphics.rsh"

const static float3 gGrayFactor = { 0.299f, 0.587f, 0.114f };

//Convolution-based parameters
rs_allocation imgIn;
rs_allocation kernel;
uint32_t kernelSize;
uint32_t width;
uint32_t height;

//Sobel parameters
rs_allocation sobelXEdges;
rs_allocation sobelYEdges;

//Threshold parameters
float thresholdMinValue;
float thresholdMaxValue;
float thresholdValue;

uchar4 __attribute__((kernel)) grayscale(const uchar4 in)
{
    float4 f4 = rsUnpackColor8888(in);
    float3 mix = dot(f4.rgb, gGrayFactor);
    float val = mix.r + mix.g + mix.b;
    float4 result = {val, val, val, f4.a}; //rgba
    return rsPackColorTo8888(result);
}

uchar4 __attribute__((kernel)) meanBlur(uint32_t x, uint32_t y)
{
    float4 sum = 0;
    uint count = 0;
    uint delta = kernelSize / 2;

    for (int yi = y-delta; yi <= y+delta; ++yi) {
        for (int xi = x-delta; xi <= x+delta; ++xi) {
            if (xi >= 0 && xi < width && yi >= 0 && yi < height) {
                sum += rsUnpackColor8888(rsGetElementAt_uchar4(imgIn, xi, yi));
                ++count;
            }
        }
    }

    return rsPackColorTo8888(sum/count);
}

uchar4 __attribute__((kernel)) adaptiveThreshold(const uchar4 in, uint32_t x, uint32_t y)
{
    float4 sum = 0;
    uint count = 0;
    uint delta = kernelSize / 2;

    for (int yi = y-delta; yi <= y+delta; ++yi) {
        for (int xi = x-delta; xi <= x+delta; ++xi) {
            if (xi >= 0 && xi < width && yi >= 0 && yi < height) {
                sum += rsUnpackColor8888(rsGetElementAt_uchar4(imgIn, xi, yi));
                ++count;
            }
        }
    }

    float4 threshold = sum / count;
    uchar4 val = rsPackColorTo8888(threshold);

    if(in.r > val.r || in.g > val.g || in.b > val.b){
        val = (uchar4)(thresholdMaxValue, thresholdMaxValue, thresholdMaxValue, 255);
    }
    else{
        val = (uchar4)(0, 0, 0, 0);
    }

    return val;
}

uchar4 __attribute__((kernel)) convolve(uint32_t x, uint32_t y)
{
    float4 sum = 0;
    uint delta = kernelSize / 2;

    for (int yi = y-delta; yi <= y+delta; ++yi) {
        for (int xi = x-delta; xi <= x+delta; ++xi) {
            if (xi >= 0 && xi < width && yi >= 0 && yi < height) {
                sum += rsGetElementAt_float(kernel, xi-(x-delta), yi-(y-delta))*rsUnpackColor8888(rsGetElementAt_uchar4(imgIn, xi, yi));
            }
        }
    }

    return rsPackColorTo8888(sum);
}

uchar4 __attribute__((kernel)) sobelCombine(uint32_t x, uint32_t y)
{
    float4 xValue = rsUnpackColor8888(rsGetElementAt_uchar4(sobelXEdges, x, y));
    float4 yValue = rsUnpackColor8888(rsGetElementAt_uchar4(sobelYEdges, x, y));

    xValue = xValue * xValue;
    yValue = yValue * yValue;

    uchar4 val = rsPackColorTo8888(sqrt(xValue+yValue));

    return val;
}

uchar4 __attribute__((kernel)) constantThreshold(const uchar4 in)
{
    uchar4 val;

    if(in.r > thresholdValue || in.g > thresholdValue || in.b > thresholdValue){
        val = (uchar4)(thresholdMaxValue, thresholdMaxValue, thresholdMaxValue, 255);
    }
    else{
        val = (uchar4)(0, 0, 0, 0);
    }

    return val;
}