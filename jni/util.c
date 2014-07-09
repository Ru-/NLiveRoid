/*
 * util.c
 *
 *  Created on: 2013/11/17
 *      Author: Owner
 */


#ifndef UTIL_H_
#define UTIL_H_
#include "util.h"
#endif

#include <string.h>
#include <stdlib.h>

#include <android/log.h>
#define LOG_TAG "NLiveRoid"
#define LOGD(...)  __android_log_print(3, LOG_TAG, __VA_ARGS__)

uint32_t ff_squareTbl[512] = {0, };

int total = 0;

static size_t max_alloc_size= __INT_MAX__;

const uint16_t ff_h263_format[8][2] = {
   { 0, 0 },
   { 128, 96 },
   { 176, 144 },
   { 352, 288 },
   { 704, 576 },
   { 1408, 1152 },
};

const uint8_t ff_mvtab[33][2] =
{
  {1,1}, {1,2}, {1,3}, {1,4}, {3,6}, {5,7}, {4,7}, {3,7},
  {11,9}, {10,9}, {9,9}, {17,10}, {16,10}, {15,10}, {14,10}, {13,10},
  {12,10}, {11,10}, {10,10}, {9,10}, {8,10}, {7,10}, {6,10}, {5,10},
  {4,10}, {7,11}, {6,11}, {5,11}, {4,11}, {3,11}, {2,11}, {3,12},
  {2,12}
};

const uint16_t ff_mpeg1_default_intra_matrix[256] = {
       8, 16, 19, 22, 26, 27, 29, 34,
       16, 16, 22, 24, 27, 29, 34, 37,
       19, 22, 26, 27, 29, 34, 34, 38,
       22, 22, 26, 27, 29, 34, 37, 40,
       22, 26, 27, 29, 32, 35, 40, 48,
       26, 27, 29, 32, 35, 40, 48, 58,
       26, 27, 29, 34, 38, 46, 56, 69,
       27, 29, 35, 38, 46, 56, 69, 83
};

const uint8_t ff_zigzag_direct[64] = {
 0,   1,  8, 16,  9,  2,  3, 10,
 17, 24, 32, 25, 18, 11,  4,  5,
 12, 19, 26, 33, 40, 48, 41, 34,
 27, 20, 13,  6,  7, 14, 21, 28,
 35, 42, 49, 56, 57, 50, 43, 36,
 29, 22, 15, 23, 30, 37, 44, 51,
 58, 59, 52, 45, 38, 31, 39, 46,
 53, 60, 61, 54, 47, 55, 62, 63
};

const uint8_t ff_mpeg1_dc_scale_table[128] = {
//  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
};


static const uint16_t intra_vlc_aic[103][2] = {
{  0x2,  2 }, {  0x6,  3 }, {  0xe,  4 }, {  0xc,  5 },
{  0xd,  5 }, { 0x10,  6 }, { 0x11,  6 }, { 0x12,  6 },
{ 0x16,  7 }, { 0x1b,  8 }, { 0x20,  9 }, { 0x21,  9 },
{ 0x1a,  9 }, { 0x1b,  9 }, { 0x1c,  9 }, { 0x1d,  9 },
{ 0x1e,  9 }, { 0x1f,  9 }, { 0x23, 11 }, { 0x22, 11 },
{ 0x57, 12 }, { 0x56, 12 }, { 0x55, 12 }, { 0x54, 12 },
{ 0x53, 12 }, {  0xf,  4 }, { 0x14,  6 }, { 0x14,  7 },
{ 0x1e,  8 }, {  0xf, 10 }, { 0x21, 11 }, { 0x50, 12 },
{  0xb,  5 }, { 0x15,  7 }, {  0xe, 10 }, {  0x9, 10 },
{ 0x15,  6 }, { 0x1d,  8 }, {  0xd, 10 }, { 0x51, 12 },
{ 0x13,  6 }, { 0x23,  9 }, {  0x7, 11 }, { 0x17,  7 },
{ 0x22,  9 }, { 0x52, 12 }, { 0x1c,  8 }, {  0xc, 10 },
{ 0x1f,  8 }, {  0xb, 10 }, { 0x25,  9 }, {  0xa, 10 },
{ 0x24,  9 }, {  0x6, 11 }, { 0x21, 10 }, { 0x20, 10 },
{  0x8, 10 }, { 0x20, 11 }, {  0x7,  4 }, {  0xc,  6 },
{ 0x10,  7 }, { 0x13,  8 }, { 0x11,  9 }, { 0x12,  9 },
{  0x4, 10 }, { 0x27, 11 }, { 0x26, 11 }, { 0x5f, 12 },
{  0xf,  6 }, { 0x13,  9 }, {  0x5, 10 }, { 0x25, 11 },
{  0xe,  6 }, { 0x14,  9 }, { 0x24, 11 }, {  0xd,  6 },
{  0x6, 10 }, { 0x5e, 12 }, { 0x11,  7 }, {  0x7, 10 },
{ 0x13,  7 }, { 0x5d, 12 }, { 0x12,  7 }, { 0x5c, 12 },
{ 0x14,  8 }, { 0x5b, 12 }, { 0x15,  8 }, { 0x1a,  8 },
{ 0x19,  8 }, { 0x18,  8 }, { 0x17,  8 }, { 0x16,  8 },
{ 0x19,  9 }, { 0x15,  9 }, { 0x16,  9 }, { 0x18,  9 },
{ 0x17,  9 }, {  0x4, 11 }, {  0x5, 11 }, { 0x58, 12 },
{ 0x59, 12 }, { 0x5a, 12 }, {  0x3,  7 },
};

static const int8_t intra_run_aic[102] = {
 0,  0,  0,  0,  0,  0,  0,  0,
 0,  0,  0,  0,  0,  0,  0,  0,
 0,  0,  0,  0,  0,  0,  0,  0,
 0,  1,  1,  1,  1,  1,  1,  1,
 2,  2,  2,  2,  3,  3,  3,  3,
 4,  4,  4,  5,  5,  5,  6,  6,
 7,  7,  8,  8,  9,  9, 10, 11,
12, 13,  0,  0,  0,  0,  0,  0,
 0,  0,  0,  0,  1,  1,  1,  1,
 2,  2,  2,  3,  3,  3,  4,  4,
 5,  5,  6,  6,  7,  7,  8,  9,
10, 11, 12, 13, 14, 15, 16, 17,
18, 19, 20, 21, 22, 23,
};

static const int8_t intra_level_aic[102] = {
 1,  2,  3,  4,  5,  6,  7,  8,
 9, 10, 11, 12, 13, 14, 15, 16,
17, 18, 19, 20, 21, 22, 23, 24,
25,  1,  2,  3,  4,  5,  6,  7,
 1,  2,  3,  4,  1,  2,  3,  4,
 1,  2,  3,  1,  2,  3,  1,  2,
 1,  2,  1,  2,  1,  2,  1,  1,
 1,  1,  1,  2,  3,  4,  5,  6,
 7,  8,  9, 10,  1,  2,  3,  4,
 1,  2,  3,  1,  2,  3,  1,  2,
 1,  2,  1,  2,  1,  2,  1,  1,
 1,  1,  1,  1,  1,  1,  1,  1,
 1,  1,  1,  1,  1,  1,
};


const uint16_t ff_inter_vlc[103][2] = {
{ 0x2, 2 },{ 0xf, 4 },{ 0x15, 6 },{ 0x17, 7 },
{ 0x1f, 8 },{ 0x25, 9 },{ 0x24, 9 },{ 0x21, 10 },
{ 0x20, 10 },{ 0x7, 11 },{ 0x6, 11 },{ 0x20, 11 },
{ 0x6, 3 },{ 0x14, 6 },{ 0x1e, 8 },{ 0xf, 10 },
{ 0x21, 11 },{ 0x50, 12 },{ 0xe, 4 },{ 0x1d, 8 },
{ 0xe, 10 },{ 0x51, 12 },{ 0xd, 5 },{ 0x23, 9 },
{ 0xd, 10 },{ 0xc, 5 },{ 0x22, 9 },{ 0x52, 12 },
{ 0xb, 5 },{ 0xc, 10 },{ 0x53, 12 },{ 0x13, 6 },
{ 0xb, 10 },{ 0x54, 12 },{ 0x12, 6 },{ 0xa, 10 },
{ 0x11, 6 },{ 0x9, 10 },{ 0x10, 6 },{ 0x8, 10 },
{ 0x16, 7 },{ 0x55, 12 },{ 0x15, 7 },{ 0x14, 7 },
{ 0x1c, 8 },{ 0x1b, 8 },{ 0x21, 9 },{ 0x20, 9 },
{ 0x1f, 9 },{ 0x1e, 9 },{ 0x1d, 9 },{ 0x1c, 9 },
{ 0x1b, 9 },{ 0x1a, 9 },{ 0x22, 11 },{ 0x23, 11 },
{ 0x56, 12 },{ 0x57, 12 },{ 0x7, 4 },{ 0x19, 9 },
{ 0x5, 11 },{ 0xf, 6 },{ 0x4, 11 },{ 0xe, 6 },
{ 0xd, 6 },{ 0xc, 6 },{ 0x13, 7 },{ 0x12, 7 },
{ 0x11, 7 },{ 0x10, 7 },{ 0x1a, 8 },{ 0x19, 8 },
{ 0x18, 8 },{ 0x17, 8 },{ 0x16, 8 },{ 0x15, 8 },
{ 0x14, 8 },{ 0x13, 8 },{ 0x18, 9 },{ 0x17, 9 },
{ 0x16, 9 },{ 0x15, 9 },{ 0x14, 9 },{ 0x13, 9 },
{ 0x12, 9 },{ 0x11, 9 },{ 0x7, 10 },{ 0x6, 10 },
{ 0x5, 10 },{ 0x4, 10 },{ 0x24, 11 },{ 0x25, 11 },
{ 0x26, 11 },{ 0x27, 11 },{ 0x58, 12 },{ 0x59, 12 },
{ 0x5a, 12 },{ 0x5b, 12 },{ 0x5c, 12 },{ 0x5d, 12 },
{ 0x5e, 12 },{ 0x5f, 12 },{ 0x3, 7 },
};

const int8_t ff_inter_level[102] = {
  1,  2,  3,  4,  5,  6,  7,  8,
  9, 10, 11, 12,  1,  2,  3,  4,
  5,  6,  1,  2,  3,  4,  1,  2,
  3,  1,  2,  3,  1,  2,  3,  1,
  2,  3,  1,  2,  1,  2,  1,  2,
  1,  2,  1,  1,  1,  1,  1,  1,
  1,  1,  1,  1,  1,  1,  1,  1,
  1,  1,  1,  2,  3,  1,  2,  1,
  1,  1,  1,  1,  1,  1,  1,  1,
  1,  1,  1,  1,  1,  1,  1,  1,
  1,  1,  1,  1,  1,  1,  1,  1,
  1,  1,  1,  1,  1,  1,  1,  1,
  1,  1,  1,  1,  1,  1,
};

const int8_t ff_inter_run[102] = {
  0,  0,  0,  0,  0,  0,  0,  0,
  0,  0,  0,  0,  1,  1,  1,  1,
  1,  1,  2,  2,  2,  2,  3,  3,
  3,  4,  4,  4,  5,  5,  5,  6,
  6,  6,  7,  7,  8,  8,  9,  9,
 10, 10, 11, 12, 13, 14, 15, 16,
 17, 18, 19, 20, 21, 22, 23, 24,
 25, 26,  0,  0,  0,  1,  1,  2,
  3,  4,  5,  6,  7,  8,  9, 10,
 11, 12, 13, 14, 15, 16, 17, 18,
 19, 20, 21, 22, 23, 24, 25, 26,
 27, 28, 29, 30, 31, 32, 33, 34,
 35, 36, 37, 38, 39, 40,
};


RLTable ff_h263_rl_inter = {
    102,
    58,
    ff_inter_vlc,
    ff_inter_run,
    ff_inter_level,
};



const uint16_t ff_inv_aanscales[64] = {
  4096,  2953,  3135,  3483,  4096,  5213,  7568, 14846,
  2953,  2129,  2260,  2511,  2953,  3759,  5457, 10703,
  3135,  2260,  2399,  2666,  3135,  3990,  5793, 11363,
  3483,  2511,  2666,  2962,  3483,  4433,  6436, 12625,
  4096,  2953,  3135,  3483,  4096,  5213,  7568, 14846,
  5213,  3759,  3990,  4433,  5213,  6635,  9633, 18895,
  7568,  5457,  5793,  6436,  7568,  9633, 13985, 27432,
 14846, 10703, 11363, 12625, 14846, 18895, 27432, 53809,
};

const uint8_t ff_alternate_vertical_scan[64] = {
    0,  8,  16, 24,  1,  9,  2, 10,
    17, 25, 32, 40, 48, 56, 57, 49,
    41, 33, 26, 18,  3, 11,  4, 12,
    19, 27, 34, 42, 50, 58, 35, 43,
    51, 59, 20, 28,  5, 13,  6, 14,
    21, 29, 36, 44, 52, 60, 37, 45,
    53, 61, 22, 30,  7, 15, 23, 31,
    38, 46, 54, 62, 39, 47, 55, 63,
};


RLTable ff_rl_intra_aic = {
    102,
    58,
    intra_vlc_aic,
    intra_run_aic,
    intra_level_aic,
};
const AVPixFmtDescriptor av_pix_fmt_descriptors[AV_PIX_FMT_NB] = {
    [AV_PIX_FMT_YUV420P] = {
        .name = "yuv420p",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUYV422] = {
        .name = "yuyv422",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 7 },        /* Y */
            { 0, 3, 2, 0, 7 },        /* U */
            { 0, 3, 4, 0, 7 },        /* V */
        },
    },
    [AV_PIX_FMT_RGB24] = {
        .name = "rgb24",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 2, 1, 0, 7 },        /* R */
            { 0, 2, 2, 0, 7 },        /* G */
            { 0, 2, 3, 0, 7 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGR24] = {
        .name = "bgr24",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 2, 3, 0, 7 },        /* R */
            { 0, 2, 2, 0, 7 },        /* G */
            { 0, 2, 1, 0, 7 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_YUV422P] = {
        .name = "yuv422p",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P] = {
        .name = "yuv444p",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV410P] = {
        .name = "yuv410p",
        .nb_components = 3,
        .log2_chroma_w = 2,
        .log2_chroma_h = 2,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV411P] = {
        .name = "yuv411p",
        .nb_components = 3,
        .log2_chroma_w = 2,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_GRAY8] = {
        .name = "gray",
        .nb_components = 1,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
        },
        .flags = PIX_FMT_PSEUDOPAL,
    },
    [AV_PIX_FMT_MONOWHITE] = {
        .name = "monow",
        .nb_components = 1,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 0 },        /* Y */
        },
        .flags = PIX_FMT_BITSTREAM,
    },
    [AV_PIX_FMT_MONOBLACK] = {
        .name = "monob",
        .nb_components = 1,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 7, 0 },        /* Y */
        },
        .flags = PIX_FMT_BITSTREAM,
    },
    [AV_PIX_FMT_PAL8] = {
        .name = "pal8",
        .nb_components = 1,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },
        },
        .flags = PIX_FMT_PAL,
    },
    [AV_PIX_FMT_YUVJ420P] = {
        .name = "yuvj420p",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUVJ422P] = {
        .name = "yuvj422p",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUVJ444P] = {
        .name = "yuvj444p",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_XVMC_MPEG2_MC] = {
        .name = "xvmcmc",
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_XVMC_MPEG2_IDCT] = {
        .name = "xvmcidct",
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_UYVY422] = {
        .name = "uyvy422",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 2, 0, 7 },        /* Y */
            { 0, 3, 1, 0, 7 },        /* U */
            { 0, 3, 3, 0, 7 },        /* V */
        },
    },
    [AV_PIX_FMT_UYYVYY411] = {
        .name = "uyyvyy411",
        .nb_components = 3,
        .log2_chroma_w = 2,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 3, 2, 0, 7 },        /* Y */
            { 0, 5, 1, 0, 7 },        /* U */
            { 0, 5, 4, 0, 7 },        /* V */
        },
    },
    [AV_PIX_FMT_BGR8] = {
        .name = "bgr8",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 2 },        /* R */
            { 0, 0, 1, 3, 2 },        /* G */
            { 0, 0, 1, 6, 1 },        /* B */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_PSEUDOPAL,
    },
    [AV_PIX_FMT_BGR4] = {
        .name = "bgr4",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 3, 4, 0, 0 },        /* R */
            { 0, 3, 2, 0, 1 },        /* G */
            { 0, 3, 1, 0, 0 },        /* B */
        },
        .flags = PIX_FMT_BITSTREAM | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGR4_BYTE] = {
        .name = "bgr4_byte",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 0 },        /* R */
            { 0, 0, 1, 1, 1 },        /* G */
            { 0, 0, 1, 3, 0 },        /* B */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_PSEUDOPAL,
    },
    [AV_PIX_FMT_RGB8] = {
        .name = "rgb8",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 6, 1 },        /* R */
            { 0, 0, 1, 3, 2 },        /* G */
            { 0, 0, 1, 0, 2 },        /* B */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_PSEUDOPAL,
    },
    [AV_PIX_FMT_RGB4] = {
        .name = "rgb4",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 3, 1, 0, 0 },        /* R */
            { 0, 3, 2, 0, 1 },        /* G */
            { 0, 3, 4, 0, 0 },        /* B */
        },
        .flags = PIX_FMT_BITSTREAM | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_RGB4_BYTE] = {
        .name = "rgb4_byte",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 3, 0 },        /* R */
            { 0, 0, 1, 1, 1 },        /* G */
            { 0, 0, 1, 0, 0 },        /* B */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_PSEUDOPAL,
    },
    [AV_PIX_FMT_NV12] = {
        .name = "nv12",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 1, 1, 0, 7 },        /* U */
            { 1, 1, 2, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_NV21] = {
        .name = "nv21",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 1, 2, 0, 7 },        /* U */
            { 1, 1, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_ARGB] = {
        .name = "argb",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 3, 2, 0, 7 },        /* R */
            { 0, 3, 3, 0, 7 },        /* G */
            { 0, 3, 4, 0, 7 },        /* B */
            { 0, 3, 1, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_RGBA] = {
        .name = "rgba",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 3, 1, 0, 7 },        /* R */
            { 0, 3, 2, 0, 7 },        /* G */
            { 0, 3, 3, 0, 7 },        /* B */
            { 0, 3, 4, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_ABGR] = {
        .name = "abgr",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 3, 4, 0, 7 },        /* R */
            { 0, 3, 3, 0, 7 },        /* G */
            { 0, 3, 2, 0, 7 },        /* B */
            { 0, 3, 1, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_BGRA] = {
        .name = "bgra",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 3, 3, 0, 7 },        /* R */
            { 0, 3, 2, 0, 7 },        /* G */
            { 0, 3, 1, 0, 7 },        /* B */
            { 0, 3, 4, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_0RGB] = {
        .name = "0rgb",
        .nb_components= 3,
        .log2_chroma_w= 0,
        .log2_chroma_h= 0,
        .comp = {
            { 0, 3, 2, 0, 7 },        /* R */
            { 0, 3, 3, 0, 7 },        /* G */
            { 0, 3, 4, 0, 7 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_RGB0] = {
        .name = "rgb0",
        .nb_components= 3,
        .log2_chroma_w= 0,
        .log2_chroma_h= 0,
        .comp = {
            { 0, 3, 1, 0, 7 },        /* R */
            { 0, 3, 2, 0, 7 },        /* G */
            { 0, 3, 3, 0, 7 },        /* B */
            { 0, 3, 4, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_0BGR] = {
        .name = "0bgr",
        .nb_components= 3,
        .log2_chroma_w= 0,
        .log2_chroma_h= 0,
        .comp = {
            { 0, 3, 4, 0, 7 },        /* R */
            { 0, 3, 3, 0, 7 },        /* G */
            { 0, 3, 2, 0, 7 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGR0] = {
        .name = "bgr0",
        .nb_components= 3,
        .log2_chroma_w= 0,
        .log2_chroma_h= 0,
        .comp = {
            { 0, 3, 3, 0, 7 },        /* R */
            { 0, 3, 2, 0, 7 },        /* G */
            { 0, 3, 1, 0, 7 },        /* B */
            { 0, 3, 4, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GRAY16BE] = {
        .name = "gray16be",
        .nb_components = 1,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },       /* Y */
        },
        .flags = PIX_FMT_BE,
    },
    [AV_PIX_FMT_GRAY16LE] = {
        .name = "gray16le",
        .nb_components = 1,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },       /* Y */
        },
    },
    [AV_PIX_FMT_YUV440P] = {
        .name = "yuv440p",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUVJ440P] = {
        .name = "yuvj440p",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUVA420P] = {
        .name = "yuva420p",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
            { 3, 0, 1, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA422P] = {
        .name = "yuva422p",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
            { 3, 0, 1, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA444P] = {
        .name = "yuva444p",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 0, 1, 0, 7 },        /* Y */
            { 1, 0, 1, 0, 7 },        /* U */
            { 2, 0, 1, 0, 7 },        /* V */
            { 3, 0, 1, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA420P9BE] = {
        .name = "yuva420p9be",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
            { 3, 1, 1, 0, 8 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA420P9LE] = {
        .name = "yuva420p9le",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
            { 3, 1, 1, 0, 8 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA422P9BE] = {
        .name = "yuva422p9be",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
            { 3, 1, 1, 0, 8 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA422P9LE] = {
        .name = "yuva422p9le",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
            { 3, 1, 1, 0, 8 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA444P9BE] = {
        .name = "yuva444p9be",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
            { 3, 1, 1, 0, 8 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA444P9LE] = {
        .name = "yuva444p9le",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
            { 3, 1, 1, 0, 8 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA420P10BE] = {
        .name = "yuva420p10be",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
            { 3, 1, 1, 0, 9 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA420P10LE] = {
        .name = "yuva420p10le",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
            { 3, 1, 1, 0, 9 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA422P10BE] = {
        .name = "yuva422p10be",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
            { 3, 1, 1, 0, 9 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA422P10LE] = {
        .name = "yuva422p10le",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
            { 3, 1, 1, 0, 9 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA444P10BE] = {
        .name = "yuva444p10be",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
            { 3, 1, 1, 0, 9 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA444P10LE] = {
        .name = "yuva444p10le",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
            { 3, 1, 1, 0, 9 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA420P16BE] = {
        .name = "yuva420p16be",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
            { 3, 1, 1, 0, 15 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA420P16LE] = {
        .name = "yuva420p16le",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
            { 3, 1, 1, 0, 15 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA422P16BE] = {
        .name = "yuva422p16be",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
            { 3, 1, 1, 0, 15 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA422P16LE] = {
        .name = "yuva422p16le",
        .nb_components = 4,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
            { 3, 1, 1, 0, 15 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA444P16BE] = {
        .name = "yuva444p16be",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
            { 3, 1, 1, 0, 15 },        /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_YUVA444P16LE] = {
        .name = "yuva444p16le",
        .nb_components = 4,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
            { 3, 1, 1, 0, 15 },        /* A */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_VDPAU_H264] = {
        .name = "vdpau_h264",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_VDPAU_MPEG1] = {
        .name = "vdpau_mpeg1",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_VDPAU_MPEG2] = {
        .name = "vdpau_mpeg2",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_VDPAU_WMV3] = {
        .name = "vdpau_wmv3",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_VDPAU_VC1] = {
        .name = "vdpau_vc1",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_VDPAU_MPEG4] = {
        .name = "vdpau_mpeg4",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_RGB48BE] = {
        .name = "rgb48be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 5, 1, 0, 15 },       /* R */
            { 0, 5, 3, 0, 15 },       /* G */
            { 0, 5, 5, 0, 15 },       /* B */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_BE,
    },
    [AV_PIX_FMT_RGB48LE] = {
        .name = "rgb48le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 5, 1, 0, 15 },       /* R */
            { 0, 5, 3, 0, 15 },       /* G */
            { 0, 5, 5, 0, 15 },       /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_RGBA64BE] = {
        .name = "rgba64be",
        .nb_components= 4,
        .log2_chroma_w= 0,
        .log2_chroma_h= 0,
        .comp = {
            { 0, 7, 1, 0, 15 },       /* R */
            { 0, 7, 3, 0, 15 },       /* G */
            { 0, 7, 5, 0, 15 },       /* B */
            { 0, 7, 7, 0, 15 },       /* A */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_BE | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_RGBA64LE] = {
        .name = "rgba64le",
        .nb_components= 4,
        .log2_chroma_w= 0,
        .log2_chroma_h= 0,
        .comp = {
            { 0, 7, 1, 0, 15 },       /* R */
            { 0, 7, 3, 0, 15 },       /* G */
            { 0, 7, 5, 0, 15 },       /* B */
            { 0, 7, 7, 0, 15 },       /* A */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_RGB565BE] = {
        .name = "rgb565be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 0, 3, 4 },        /* R */
            { 0, 1, 1, 5, 5 },        /* G */
            { 0, 1, 1, 0, 4 },        /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_RGB565LE] = {
        .name = "rgb565le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 2, 3, 4 },        /* R */
            { 0, 1, 1, 5, 5 },        /* G */
            { 0, 1, 1, 0, 4 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_RGB555BE] = {
        .name = "rgb555be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 0, 2, 4 },        /* R */
            { 0, 1, 1, 5, 4 },        /* G */
            { 0, 1, 1, 0, 4 },        /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_RGB555LE] = {
        .name = "rgb555le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 2, 2, 4 },        /* R */
            { 0, 1, 1, 5, 4 },        /* G */
            { 0, 1, 1, 0, 4 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_RGB444BE] = {
        .name = "rgb444be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 0, 0, 3 },        /* R */
            { 0, 1, 1, 4, 3 },        /* G */
            { 0, 1, 1, 0, 3 },        /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_RGB444LE] = {
        .name = "rgb444le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 2, 0, 3 },        /* R */
            { 0, 1, 1, 4, 3 },        /* G */
            { 0, 1, 1, 0, 3 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGR48BE] = {
        .name = "bgr48be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 5, 5, 0, 15 },       /* R */
            { 0, 5, 3, 0, 15 },       /* G */
            { 0, 5, 1, 0, 15 },       /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGR48LE] = {
        .name = "bgr48le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 5, 5, 0, 15 },       /* R */
            { 0, 5, 3, 0, 15 },       /* G */
            { 0, 5, 1, 0, 15 },       /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGRA64BE] = {
        .name = "bgra64be",
        .nb_components= 4,
        .log2_chroma_w= 0,
        .log2_chroma_h= 0,
        .comp = {
            { 0, 7, 5, 0, 15 },       /* R */
            { 0, 7, 3, 0, 15 },       /* G */
            { 0, 7, 1, 0, 15 },       /* B */
            { 0, 7, 7, 0, 15 },       /* A */
        },
        .flags = PIX_FMT_BE | PIX_FMT_RGB | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_BGRA64LE] = {
        .name = "bgra64le",
        .nb_components= 4,
        .log2_chroma_w= 0,
        .log2_chroma_h= 0,
        .comp = {
            { 0, 7, 5, 0, 15 },       /* R */
            { 0, 7, 3, 0, 15 },       /* G */
            { 0, 7, 1, 0, 15 },       /* B */
            { 0, 7, 7, 0, 15 },       /* A */
        },
        .flags = PIX_FMT_RGB | PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_BGR565BE] = {
        .name = "bgr565be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 4 },        /* R */
            { 0, 1, 1, 5, 5 },        /* G */
            { 0, 1, 0, 3, 4 },        /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGR565LE] = {
        .name = "bgr565le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 4 },        /* R */
            { 0, 1, 1, 5, 5 },        /* G */
            { 0, 1, 2, 3, 4 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGR555BE] = {
        .name = "bgr555be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 4 },       /* R */
            { 0, 1, 1, 5, 4 },       /* G */
            { 0, 1, 0, 2, 4 },       /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_RGB,
     },
    [AV_PIX_FMT_BGR555LE] = {
        .name = "bgr555le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 4 },        /* R */
            { 0, 1, 1, 5, 4 },        /* G */
            { 0, 1, 2, 2, 4 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_BGR444BE] = {
        .name = "bgr444be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 3 },       /* R */
            { 0, 1, 1, 4, 3 },       /* G */
            { 0, 1, 0, 0, 3 },       /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_RGB,
     },
    [AV_PIX_FMT_BGR444LE] = {
        .name = "bgr444le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 3 },        /* R */
            { 0, 1, 1, 4, 3 },        /* G */
            { 0, 1, 2, 0, 3 },        /* B */
        },
        .flags = PIX_FMT_RGB,
    },
    [AV_PIX_FMT_VAAPI_MOCO] = {
        .name = "vaapi_moco",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_VAAPI_IDCT] = {
        .name = "vaapi_idct",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_VAAPI_VLD] = {
        .name = "vaapi_vld",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_YUV420P9LE] = {
        .name = "yuv420p9le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P9BE] = {
        .name = "yuv420p9be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P10LE] = {
        .name = "yuv420p10le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P10BE] = {
        .name = "yuv420p10be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P12LE] = {
        .name = "yuv420p12le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 11 },        /* Y */
            { 1, 1, 1, 0, 11 },        /* U */
            { 2, 1, 1, 0, 11 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P12BE] = {
        .name = "yuv420p12be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 11 },        /* Y */
            { 1, 1, 1, 0, 11 },        /* U */
            { 2, 1, 1, 0, 11 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P14LE] = {
        .name = "yuv420p14le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 13 },        /* Y */
            { 1, 1, 1, 0, 13 },        /* U */
            { 2, 1, 1, 0, 13 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P14BE] = {
        .name = "yuv420p14be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 13 },        /* Y */
            { 1, 1, 1, 0, 13 },        /* U */
            { 2, 1, 1, 0, 13 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P16LE] = {
        .name = "yuv420p16le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV420P16BE] = {
        .name = "yuv420p16be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P9LE] = {
        .name = "yuv422p9le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P9BE] = {
        .name = "yuv422p9be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P10LE] = {
        .name = "yuv422p10le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P10BE] = {
        .name = "yuv422p10be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P12LE] = {
        .name = "yuv422p12le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 11 },        /* Y */
            { 1, 1, 1, 0, 11 },        /* U */
            { 2, 1, 1, 0, 11 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P12BE] = {
        .name = "yuv422p12be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 11 },        /* Y */
            { 1, 1, 1, 0, 11 },        /* U */
            { 2, 1, 1, 0, 11 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P14LE] = {
        .name = "yuv422p14le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 13 },        /* Y */
            { 1, 1, 1, 0, 13 },        /* U */
            { 2, 1, 1, 0, 13 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P14BE] = {
        .name = "yuv422p14be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 13 },        /* Y */
            { 1, 1, 1, 0, 13 },        /* U */
            { 2, 1, 1, 0, 13 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P16LE] = {
        .name = "yuv422p16le",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV422P16BE] = {
        .name = "yuv422p16be",
        .nb_components = 3,
        .log2_chroma_w = 1,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P16LE] = {
        .name = "yuv444p16le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P16BE] = {
        .name = "yuv444p16be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 15 },        /* Y */
            { 1, 1, 1, 0, 15 },        /* U */
            { 2, 1, 1, 0, 15 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P10LE] = {
        .name = "yuv444p10le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P10BE] = {
        .name = "yuv444p10be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 9 },        /* Y */
            { 1, 1, 1, 0, 9 },        /* U */
            { 2, 1, 1, 0, 9 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P9LE] = {
        .name = "yuv444p9le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P9BE] = {
        .name = "yuv444p9be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 8 },        /* Y */
            { 1, 1, 1, 0, 8 },        /* U */
            { 2, 1, 1, 0, 8 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P12LE] = {
        .name = "yuv444p12le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 11 },        /* Y */
            { 1, 1, 1, 0, 11 },        /* U */
            { 2, 1, 1, 0, 11 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P12BE] = {
        .name = "yuv444p12be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 11 },        /* Y */
            { 1, 1, 1, 0, 11 },        /* U */
            { 2, 1, 1, 0, 11 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P14LE] = {
        .name = "yuv444p14le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 13 },        /* Y */
            { 1, 1, 1, 0, 13 },        /* U */
            { 2, 1, 1, 0, 13 },        /* V */
        },
        .flags = PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_YUV444P14BE] = {
        .name = "yuv444p14be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 0, 1, 1, 0, 13 },        /* Y */
            { 1, 1, 1, 0, 13 },        /* U */
            { 2, 1, 1, 0, 13 },        /* V */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR,
    },
    [AV_PIX_FMT_DXVA2_VLD] = {
        .name = "dxva2_vld",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_VDA_VLD] = {
        .name = "vda_vld",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
    [AV_PIX_FMT_GRAY8A] = {
        .name = "gray8a",
        .nb_components = 2,
        .comp = {
            { 0, 1, 1, 0, 7 },        /* Y */
            { 0, 1, 2, 0, 7 },        /* A */
        },
        .flags = PIX_FMT_ALPHA,
    },
    [AV_PIX_FMT_GBRP] = {
        .name = "gbrp",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 0, 1, 0, 7 },        /* R */
            { 0, 0, 1, 0, 7 },        /* G */
            { 1, 0, 1, 0, 7 },        /* B */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP9LE] = {
        .name = "gbrp9le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 8 },        /* R */
            { 0, 1, 1, 0, 8 },        /* G */
            { 1, 1, 1, 0, 8 },        /* B */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP9BE] = {
        .name = "gbrp9be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 8 },        /* R */
            { 0, 1, 1, 0, 8 },        /* G */
            { 1, 1, 1, 0, 8 },        /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP10LE] = {
        .name = "gbrp10le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 9 },        /* R */
            { 0, 1, 1, 0, 9 },        /* G */
            { 1, 1, 1, 0, 9 },        /* B */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP10BE] = {
        .name = "gbrp10be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 9 },        /* R */
            { 0, 1, 1, 0, 9 },        /* G */
            { 1, 1, 1, 0, 9 },        /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP12LE] = {
        .name = "gbrp12le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 11 },        /* R */
            { 0, 1, 1, 0, 11 },        /* G */
            { 1, 1, 1, 0, 11 },        /* B */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP12BE] = {
        .name = "gbrp12be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 11 },        /* R */
            { 0, 1, 1, 0, 11 },        /* G */
            { 1, 1, 1, 0, 11 },        /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP14LE] = {
        .name = "gbrp14le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 13 },        /* R */
            { 0, 1, 1, 0, 13 },        /* G */
            { 1, 1, 1, 0, 13 },        /* B */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP14BE] = {
        .name = "gbrp14be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 13 },        /* R */
            { 0, 1, 1, 0, 13 },        /* G */
            { 1, 1, 1, 0, 13 },        /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP16LE] = {
        .name = "gbrp16le",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 15 },       /* R */
            { 0, 1, 1, 0, 15 },       /* G */
            { 1, 1, 1, 0, 15 },       /* B */
        },
        .flags = PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_GBRP16BE] = {
        .name = "gbrp16be",
        .nb_components = 3,
        .log2_chroma_w = 0,
        .log2_chroma_h = 0,
        .comp = {
            { 2, 1, 1, 0, 15 },       /* R */
            { 0, 1, 1, 0, 15 },       /* G */
            { 1, 1, 1, 0, 15 },       /* B */
        },
        .flags = PIX_FMT_BE | PIX_FMT_PLANAR | PIX_FMT_RGB,
    },
    [AV_PIX_FMT_VDPAU] = {
        .name = "vdpau",
        .log2_chroma_w = 1,
        .log2_chroma_h = 1,
        .flags = PIX_FMT_HWACCEL,
    },
};

#define avpriv_atomic_int_add_and_fetch atomic_int_add_and_fetch_gcc
static inline int atomic_int_add_and_fetch_gcc(volatile int *ptr, int inc)
{
    return __sync_add_and_fetch(ptr, inc);
}

void av_buffer_unref(AVBufferRef **buf)
{
    AVBuffer *b;

    if (!buf || !*buf)
        return;
    b = (*buf)->buffer;
    av_freep(buf);

    if (!avpriv_atomic_int_add_and_fetch(&b->refcount, -1)) {
        b->free(b->opaque, b->data);
        av_freep(&b);
    }
}

void av_destruct_packet(AVPacket *pkt)
{
    if(pkt->data)av_free(pkt->data);
    pkt->data = NULL;
    pkt->size = 0;
}

void av_free_packet(AVPacket *pkt)
{
//	LOGD("av_free_packet Called");
    if (pkt) {
        int i;

        if (pkt->destruct)
            pkt->destruct(pkt);
        pkt->data            = NULL;
        pkt->size            = 0;

//    	LOGD("av_free_packet A");
        if(pkt->side_data){
        for (i = 0; i < pkt->side_data_elems; i++){
        	if(pkt->side_data[i].size>0)
        	av_free(pkt->side_data[i].data);
        }
//    	LOGD("av_free_packet B");
        av_freep(&pkt->side_data);
        }

//    	LOGD("av_free_packet C");
        pkt->side_data_elems = 0;
    }
}


void *av_realloc(void *ptr, size_t size)
{
	LOGD("av_realloc size:%d",size);
#if CONFIG_MEMALIGN_HACK
    int diff;
#endif

    /* let's disallow possible ambiguous cases */
    if (size > (max_alloc_size - 32))
        return NULL;

#if CONFIG_MEMALIGN_HACK
    //FIXME this isn't aligned correctly, though it probably isn't needed
    if (!ptr)
        return av_malloc(size);
    diff = ((char *)ptr)[-1];
//    av_assert0(diff>0 && diff<=ALIGN);
    ptr = realloc((char *)ptr - diff, size + diff);
    if (ptr)
        ptr = (char *)ptr + diff;
    return ptr;
#elif HAVE_ALIGNED_MALLOC
    return _aligned_realloc(ptr, size + !size, ALIGN);
#else
    return realloc(ptr, size + !size);
#endif
}


AVFrame *avcodec_alloc_frame()
{
    AVFrame *frame = av_malloc(sizeof(AVFrame));

    if (frame == NULL)
        return NULL;

    frame->extended_data = NULL;
    avcodec_get_frame_defaults(frame);

    return frame;
}


AVFrameSideData *av_frame_new_side_data(AVFrame *frame,
                                        enum AVFrameSideDataType type,
                                        int size)
{
    AVFrameSideData *ret, **tmp;

    if (frame->nb_side_data > __INT_MAX__ / sizeof(*frame->side_data) - 1)
        return NULL;

    tmp = av_realloc(frame->side_data,
                     (frame->nb_side_data + 1) * sizeof(*frame->side_data));
    if (!tmp)
        return NULL;
    frame->side_data = tmp;

    ret = av_mallocz(sizeof(*ret));
    if (!ret)
        return NULL;

    ret->data = av_malloc(size);
    if (!ret->data) {
        av_freep(&ret);
        return NULL;
    }

    ret->size = size;
    ret->type = type;

    frame->side_data[frame->nb_side_data++] = ret;

    return ret;
}

int av_frame_copy_props(AVFrame *dst, const AVFrame *src)
{
    int i;


//    LOGD("av_frame_copy_props Called\n");

    dst->key_frame              = src->key_frame;
    dst->pict_type              = src->pict_type;
    dst->sample_aspect_ratio    = src->sample_aspect_ratio;
    dst->pts                    = src->pts;
    dst->repeat_pict            = src->repeat_pict;
    dst->interlaced_frame       = src->interlaced_frame;
    dst->top_field_first        = src->top_field_first;
    dst->palette_has_changed    = src->palette_has_changed;
    dst->sample_rate            = src->sample_rate;
    dst->opaque                 = src->opaque;
#if FF_API_AVFRAME_LAVC
    dst->type                   = src->type;
#endif
    dst->pkt_pts                = src->pkt_pts;
    dst->pkt_dts                = src->pkt_dts;
    dst->pkt_pos                = src->pkt_pos;
    dst->pkt_size               = src->pkt_size;
    dst->pkt_duration           = src->pkt_duration;
    dst->reordered_opaque       = src->reordered_opaque;
    dst->quality                = src->quality;
    dst->best_effort_timestamp  = src->best_effort_timestamp;
    dst->coded_picture_number   = src->coded_picture_number;
    dst->display_picture_number = src->display_picture_number;
//    dst->flags                  = src->flags;
    dst->decode_error_flags     = src->decode_error_flags;
//    dst->colorspace             = src->colorspace;
//    dst->color_range            = src->color_range;
//    LOGD("dst key_frame:%d\n",dst->key_frame              );
//    LOGD("dst pict_type:%d\n",dst->pict_type              );
//    LOGD("dst sample_aspect_ratio:%d\n",dst->sample_aspect_ratio    );
//    LOGD("dst pts:%d\n",dst->pts                    );
//    LOGD("dst repeat_pict:%d\n",dst->repeat_pict            );
//    LOGD("dst interlaced_frame:%d\n",dst->interlaced_frame       );
//    LOGD("dst top_field_first:%d\n",dst->top_field_first        );
//    LOGD("dst palette_has_changed:%d\n",dst->palette_has_changed    );
//    LOGD("dst sample_rate:%d\n",dst->sample_rate            );
//    LOGD("dst opaque:%d\n",dst->opaque                 );
//    LOGD("dst pkt_pts:%d\n",dst->pkt_pts                );
//    LOGD("dst pkt_dts:%d\n",dst->pkt_dts                );
//    LOGD("dst pkt_pos:%d\n",dst->pkt_pos                );
//    LOGD("dst pkt_size:%d\n",dst->pkt_size               );
//    LOGD("dst pkt_duration:%d\n",dst->pkt_duration           );
//    LOGD("dst reordered_opaque:%d\n",dst->reordered_opaque       );
//    LOGD("dst quality:%d\n",dst->quality                );
//    LOGD("dst best_effort_timestamp:%d\n",dst->best_effort_timestamp  );
//    LOGD("dst coded_picture_number:%d\n",dst->coded_picture_number   );
//    LOGD("dst display_picture_number:%d\n",dst->display_picture_number );
//    LOGD("dst decode_error_flags:%d\n",dst->decode_error_flags     );
//    LOGD("dst mb_type:%d\n",src->mb_type  );

//    av_dict_copy(&dst->metadata, src->metadata, 0);

    memcpy(dst->error, src->error, sizeof(dst->error));

//    LOGD("av_frame_copy_props nb_side_data:%d\n",src->nb_side_data,dst->nb_side_data);
    for (i = 0; i < src->nb_side_data; i++) {
        const AVFrameSideData *sd_src = src->side_data[i];
        AVFrameSideData *sd_dst = av_frame_new_side_data(dst, sd_src->type,
                                                         sd_src->size);
        if (!sd_dst) {
            for (i = 0; i < dst->nb_side_data; i++) {
                av_freep(&dst->side_data[i]->data);
                av_freep(&dst->side_data[i]);
//                av_dict_free(&dst->side_data[i]->metadata);
            }
            av_freep(&dst->side_data);
            return -1;
        }
        memcpy(sd_dst->data, sd_src->data, sd_src->size);
//        av_dict_copy(&sd_dst->metadata, sd_src->metadata, 0);
    }

    dst->qscale_table = NULL;
    dst->qstride      = 0;
    dst->qscale_type  = 0;
//    if (src->qp_table_buf) {
//        dst->qp_table_buf = av_buffer_ref(src->qp_table_buf);
//        if (dst->qp_table_buf) {
//            dst->qscale_table = dst->qp_table_buf->data;
//            dst->qstride      = src->qstride;
//            dst->qscale_type  = src->qscale_type;
//        }
//    }

    return 0;
}

void av_frame_unref(AVFrame *frame)
{
    int i;
//
//    for (i = 0; i < frame->nb_side_data; i++) {
//        av_freep(&frame->side_data[i]->data);
//        av_dict_free(&frame->side_data[i]->metadata);
//        av_freep(&frame->side_data[i]);
//    }
//    av_freep(&frame->side_data);

    for (i = 0; i < FF_ARRAY_ELEMS(frame->buf); i++)
        av_buffer_unref(&frame->buf[i]);
//    for (i = 0; i < frame->nb_extended_buf; i++)
//        av_buffer_unref(&frame->extended_buf[i]);
//    av_freep(&frame->extended_buf);
//    av_dict_free(&frame->metadata);
//    av_buffer_unref(&frame->qp_table_buf);

    memset(frame, 0, sizeof(AVFrame));
}


void av_frame_free(AVFrame **frame)
{
    if (!frame || !*frame)
        return;

    av_frame_unref(*frame);
    av_freep(frame);
}

void av_free(void *ptr)
{
	LOGD("av_free Called");
#if CONFIG_MEMALIGN_HACK
    if (ptr) {
        int v= ((char *)ptr)[-1];
//        av_assert0(v>0 && v<=ALIGN);
        free((char *)ptr - v);
    }
#elif HAVE_ALIGNED_MALLOC
    _aligned_free(ptr);
#else
    free(ptr);
#endif
}

void av_freep(void *arg)
{
    void **ptr = (void **)arg;
    av_free(*ptr);
    *ptr = NULL;
}

void *av_malloc(size_t size)
{
    void *ptr = NULL;

    /* let's disallow possible ambiguous cases */
    if (size > (__INT_MAX__ - 32))
        return NULL;
//    printf("malloc size:%d\n",size);
    ptr = malloc(size);
    if(!ptr && !size) {
        size = 1;
        ptr= av_malloc(1);
    }
    return ptr;
}

void *av_mallocz(size_t size)
{
    void *ptr = av_malloc(size);
    if (ptr)
        memset(ptr, 0, size);
    return ptr;
}





/**
 * Release a frame buffer
 */
inline void free_frame_buffer(MpegEncContext *s, Picture *pic)
{
    pic->period_since_free = 0;
    /* WM Image / Screen codecs allocate internal buffers with different
     * dimensions / colorspaces; ignore user-defined callbacks for these. */
    if (s->codec_id != AV_CODEC_ID_WMV3IMAGE &&
        s->codec_id != AV_CODEC_ID_VC1IMAGE  &&
        s->codec_id != AV_CODEC_ID_MSS2)
        ff_thread_release_buffer(s->avctx, &pic->f);
    else
        avcodec_default_release_buffer(s->avctx, &pic->f);
    av_freep(&pic->f.hwaccel_picture_private);
}



static volatile int *allocate_progress(PerThreadContext *p)
{
    int i;

    for (i = 0; i < MAX_BUFFERS; i++)
        if (!p->progress_used[i]) break;

    if (i == MAX_BUFFERS) {
        printf( "allocate_progress() overflow\n");
        return NULL;
    }

    p->progress_used[i] = 1;

    return p->progress[i];
}


inline void free_progress(AVFrame *f)
{
    PerThreadContext *p = f->owner->thread_opaque;
    volatile int *progress = f->thread_opaque;

    p->progress_used[(progress - p->progress[0]) / 2] = 0;
}

void ff_thread_finish_setup(AVCodecContext *avctx) {
    PerThreadContext *p = avctx->thread_opaque;

    if (!(avctx->active_thread_type&FF_THREAD_FRAME)) return;

    if(p->state == STATE_SETUP_FINISHED){
        printf( "Multiple ff_thread_finish_setup() calls\n");
    }

    pthread_mutex_lock(&p->progress_mutex);
    p->state = STATE_SETUP_FINISHED;
    pthread_cond_broadcast(&p->progress_cond);
    pthread_mutex_unlock(&p->progress_mutex);
}

int ff_thread_get_buffer(AVCodecContext *avctx, AVFrame *f)
{
    PerThreadContext *p = avctx->thread_opaque;
    int err;
    volatile int *progress;

    f->owner = avctx;

    ff_init_buffer_info(avctx, f);

    if (!(avctx->active_thread_type&FF_THREAD_FRAME)) {
        f->thread_opaque = NULL;
        return ff_get_buffer(avctx, f);
    }

    if (p->state != STATE_SETTING_UP &&
        (avctx->codec->update_thread_context || (!avctx->thread_safe_callbacks &&
                avctx->get_buffer != avcodec_default_get_buffer))) {
        printf("get_buffer() cannot be called after ff_thread_finish_setup()\n");
        return -1;
    }

    pthread_mutex_lock(&p->parent->buffer_mutex);
    f->thread_opaque = (int*)(progress = allocate_progress(p));

    if (!progress) {
        pthread_mutex_unlock(&p->parent->buffer_mutex);
        return -1;
    }

    progress[0] =
    progress[1] = -1;

    if (avctx->thread_safe_callbacks ||
        avctx->get_buffer == avcodec_default_get_buffer) {
        err = ff_get_buffer(avctx, f);
    } else {
        pthread_mutex_lock(&p->progress_mutex);
        p->requested_frame = f;
        p->state = STATE_GET_BUFFER;
        pthread_cond_broadcast(&p->progress_cond);

        while (p->state != STATE_SETTING_UP)
            pthread_cond_wait(&p->progress_cond, &p->progress_mutex);

        err = p->result;

        pthread_mutex_unlock(&p->progress_mutex);

        if (!avctx->codec->update_thread_context)
            ff_thread_finish_setup(avctx);
    }

    if (err) {
        free_progress(f);
        f->thread_opaque = NULL;
    }
    pthread_mutex_unlock(&p->parent->buffer_mutex);

    return err;
}

/**
 * Always treat the buffer as read-only, even when it has only one
 * reference.
 */
#define AV_BUFFER_FLAG_READONLY (1 << 0)

/**
 * The buffer is always treated as read-only.
 */
#define BUFFER_FLAG_READONLY      (1 << 0)

void av_buffer_default_free(void *opaque, uint8_t *data)
{
    av_free(data);
}
AVBufferRef *av_buffer_create(uint8_t *data, int size,
                              void (*free)(void *opaque, uint8_t *data),
                              void *opaque, int flags)
{
    AVBufferRef *ref = NULL;
    AVBuffer    *buf = NULL;

    buf = av_mallocz(sizeof(*buf));
    if (!buf)
        return NULL;

    buf->data     = data;
    buf->size     = size;
    buf->free     = free ? free : av_buffer_default_free;
    buf->opaque   = opaque;
    buf->refcount = 1;

    if (flags & AV_BUFFER_FLAG_READONLY)
        buf->flags |= BUFFER_FLAG_READONLY;

    ref = av_mallocz(sizeof(*ref));
    if (!ref) {
        av_freep(&buf);
        return NULL;
    }

    ref->buffer = buf;
    ref->data   = data;
    ref->size   = size;

    return ref;
}

#define avpriv_atomic_ptr_cas atomic_ptr_cas_gcc
static inline void *atomic_ptr_cas_gcc(void * volatile *ptr,
                                       void *oldval, void *newval)
{
#ifdef __ARMCC_VERSION
    // armcc will throw an error if ptr is not an integer type
    volatile uintptr_t *tmp = (volatile uintptr_t*)ptr;
    return (void*)__sync_val_compare_and_swap(tmp, oldval, newval);
#else
    return __sync_val_compare_and_swap(ptr, oldval, newval);
#endif
}

/* remove the whole buffer list from the pool and return it */
static BufferPoolEntry *get_pool(AVBufferPool *pool)
{
    BufferPoolEntry *cur = *(void * volatile *)&pool->pool, *last = NULL;

    while (cur != last) {
        last = cur;
        cur = avpriv_atomic_ptr_cas((void * volatile *)&pool->pool, last, NULL);
        if (!cur)
            return NULL;
    }

    return cur;
}
static void add_to_pool(BufferPoolEntry *buf)
{
    AVBufferPool *pool;
    BufferPoolEntry *cur, *end = buf;

    if (!buf)
        return;
    pool = buf->pool;

    while (end->next)
        end = end->next;

    while (avpriv_atomic_ptr_cas((void * volatile *)&pool->pool, NULL, buf)) {
        /* pool is not empty, retrieve it and append it to our list */
        cur = get_pool(pool);
        end->next = cur;
        while (end->next)
            end = end->next;
    }
}


/*
 * This function gets called when the pool has been uninited and
 * all the buffers returned to it.
 */
static void buffer_pool_free(AVBufferPool *pool)
{
    while (pool->pool) {
        BufferPoolEntry *buf = pool->pool;
        pool->pool = buf->next;

        buf->free(buf->opaque, buf->data);
        av_freep(&buf);
    }
    av_freep(&pool);
}
#define CONFIG_MEMORY_POISONING 0
#define FF_MEMORY_POISON 0x2a
static void pool_release_buffer(void *opaque, uint8_t *data)
{
    BufferPoolEntry *buf = opaque;
    AVBufferPool *pool = buf->pool;

    if(CONFIG_MEMORY_POISONING)
        memset(buf->data, FF_MEMORY_POISON, pool->size);

    add_to_pool(buf);
    if (!avpriv_atomic_int_add_and_fetch(&pool->refcount, -1))
        buffer_pool_free(pool);
}

#define avpriv_atomic_int_get atomic_int_get_gcc
static inline int atomic_int_get_gcc(volatile int *ptr)
{
    __sync_synchronize();
    return *ptr;
}


/* allocate a new buffer and override its free() callback so that
 * it is returned to the pool on free */
static AVBufferRef *pool_alloc_buffer(AVBufferPool *pool)
{
    BufferPoolEntry *buf;
    AVBufferRef     *ret;

    ret = pool->alloc(pool->size);
    if (!ret)
        return NULL;

    buf = av_mallocz(sizeof(*buf));
    if (!buf) {
        av_buffer_unref(&ret);
        return NULL;
    }

    buf->data   = ret->buffer->data;
    buf->opaque = ret->buffer->opaque;
    buf->free   = ret->buffer->free;
    buf->pool   = pool;

    ret->buffer->opaque = buf;
    ret->buffer->free   = pool_release_buffer;

    avpriv_atomic_int_add_and_fetch(&pool->refcount, 1);
    avpriv_atomic_int_add_and_fetch(&pool->nb_allocated, 1);

    return ret;
}


//空のパケットの初期化はここ
void av_init_packet(AVPacket *pkt)
{
    pkt->pts                  = AV_NOPTS_VALUE;
    pkt->dts                  = AV_NOPTS_VALUE;
    pkt->pos                  = -1;
    pkt->duration             = 0;
    pkt->convergence_duration = 0;
    pkt->flags                = 0;
    pkt->stream_index         = 0;
    pkt->destruct             = NULL;
    pkt->side_data            = NULL;
    pkt->side_data_elems      = 0;
}

AVBufferRef *av_buffer_pool_get(AVBufferPool *pool)
{
    LOGD("av_buffer_pool_get Called\n");
    AVBufferRef *ret;
    BufferPoolEntry *buf;

    /* check whether the pool is empty */
    buf = get_pool(pool);
    if (!buf && pool->refcount <= pool->nb_allocated) {
        LOGD("Pool race dectected, spining to avoid overallocation and eventual OOM\n");
        while (!buf && avpriv_atomic_int_get(&pool->refcount) <= avpriv_atomic_int_get(&pool->nb_allocated))
            buf = get_pool(pool);
    }

    if (!buf)
        return pool_alloc_buffer(pool);

    /* keep the first entry, return the rest of the list to the pool */
    add_to_pool(buf->next);
    buf->next = NULL;

    ret = av_buffer_create(buf->data, pool->size, pool_release_buffer,
                           buf, 0);
    if (!ret) {
        add_to_pool(buf);
        return NULL;
    }
    avpriv_atomic_int_add_and_fetch(&pool->refcount, 1);

    return ret;
}

AVBufferRef *av_buffer_alloc(int size)
{
    AVBufferRef *ret = NULL;
    uint8_t    *data = NULL;

    data = av_malloc(size);
    if (!data)
        return NULL;

    ret = av_buffer_create(data, size, av_buffer_default_free, NULL, 0);
    if (!ret)
        av_freep(&data);

    return ret;
}

#define avpriv_atomic_int_set atomic_int_set_gcc
static inline void atomic_int_set_gcc(volatile int *ptr, int val)
{
    *ptr = val;
    __sync_synchronize();
}
AVBufferPool *av_buffer_pool_init(int size, AVBufferRef* (*alloc)(int size))
{
    AVBufferPool *pool = av_mallocz(sizeof(*pool));
    if (!pool)
        return NULL;

    pool->size     = size;
    pool->alloc    = alloc ? alloc : av_buffer_alloc;

    avpriv_atomic_int_set(&pool->refcount, 1);

    return pool;
}


AVBufferRef *av_buffer_allocz(int size)
{
    AVBufferRef *ret = av_buffer_alloc(size);
    if (!ret)
        return NULL;

    memset(ret->data, 0, size);
    return ret;
}

void av_buffer_pool_uninit(AVBufferPool **ppool)
{
    AVBufferPool *pool;

    if (!ppool || !*ppool)
        return;
    pool   = *ppool;
    *ppool = NULL;

    if (!avpriv_atomic_int_add_and_fetch(&pool->refcount, -1))
        buffer_pool_free(pool);
}


int video_get_buffer(AVCodecContext *s, AVFrame *pic)
{
    int i;
    int w = s->width;
    int h = s->height;
    InternalBuffer *buf;
    AVCodecInternal *avci = s->internal;

    if (pic->data[0] != NULL) {
        printf( "pic->data[0]!=NULL in avcodec_default_get_buffer\n");
        return -1;
    }
    if (avci->buffer_count >= INTERNAL_BUFFER_SIZE) {
        printf( "buffer_count overflow (missing release_buffer?)\n");
        return -1;
    }

//    if (av_image_check_size(w, h, 0, s) || s->pix_fmt<0) {
//        printf( "video_get_buffer: image parameters invalid\n");
//        return -1;
//    }

    if (!avci->buffer) {
        avci->buffer = av_mallocz((INTERNAL_BUFFER_SIZE + 1) *
                                  sizeof(InternalBuffer));
    }

    buf = &avci->buffer[avci->buffer_count];
    LOGD("video_get_buffer avci->buffer_count:%d",avci->buffer_count);
    if (buf->base[0] && (buf->width != w || buf->height != h || buf->pix_fmt != s->pix_fmt)) {
        for (i = 0; i < AV_NUM_DATA_POINTERS; i++) {
            av_freep(&buf->base[i]);
            buf->data[i] = NULL;
        }
    }

    if (!buf->base[0]) {
        int h_chroma_shift, v_chroma_shift;
        int size[4] = { 0 };
        int tmpsize;
        int unaligned;
        AVPicture picture;
        int stride_align[AV_NUM_DATA_POINTERS];
        const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(s->pix_fmt);
        const int pixel_size = desc->comp[0].step_minus1 + 1;

        av_pix_fmt_get_chroma_sub_sample(s->pix_fmt, &h_chroma_shift,
                                         &v_chroma_shift);

//        LOGD("video_get_buffer A w:%d align:%d\n",w,stride_align);
        avcodec_align_dimensions2(s, &w, &h, stride_align);

//        LOGD("video_get_buffer B w:%d align:%d\n",w,stride_align);
        if (!(s->flags & CODEC_FLAG_EMU_EDGE)) {
            w += EDGE_WIDTH * 2;
            h += EDGE_WIDTH * 2;
        }

        do {
            // NOTE: do not align linesizes individually, this breaks e.g. assumptions
            // that linesize[0] == 2*linesize[1] in the MPEG-encoder for 4:2:2
            av_image_fill_linesizes(picture.linesize, s->pix_fmt, w);
            // increase alignment of w for next try (rhs gives the lowest bit set in w)
            w += w & ~(w - 1);

            unaligned = 0;
            for (i = 0; i < 4; i++)
                unaligned |= picture.linesize[i] % stride_align[i];
        } while (unaligned);

        tmpsize = av_image_fill_pointers(picture.data, s->pix_fmt, h, NULL, picture.linesize);
        if (tmpsize < 0)
            return -1;

        for (i = 0; i < 3 && picture.data[i + 1]; i++)
            size[i] = picture.data[i + 1] - picture.data[i];
        size[i] = tmpsize - (picture.data[i] - picture.data[0]);

        memset(buf->base, 0, sizeof(buf->base));
        memset(buf->data, 0, sizeof(buf->data));

        for (i = 0; i < 4 && size[i]; i++) {
            const int h_shift = i == 0 ? 0 : h_chroma_shift;
            const int v_shift = i == 0 ? 0 : v_chroma_shift;

            buf->linesize[i] = picture.linesize[i];

            buf->base[i] = av_malloc(size[i] + 16); //FIXME 16
            if (buf->base[i] == NULL)return -1;

            // no edge if EDGE EMU or not planar YUV
            if ((s->flags & CODEC_FLAG_EMU_EDGE) || !size[2])
                buf->data[i] = buf->base[i];
            else
                buf->data[i] = buf->base[i] + FFALIGN((buf->linesize[i] * EDGE_WIDTH >> v_shift) + (pixel_size * EDGE_WIDTH >> h_shift), stride_align[i]);
        }
        for (; i < AV_NUM_DATA_POINTERS; i++) {
            buf->base[i]     = buf->data[i] = NULL;
            buf->linesize[i] = 0;
        }
        if (size[1] && !size[2])
            avpriv_set_systematic_pal2((uint32_t *)buf->data[1], s->pix_fmt);
        buf->width   = s->width;
        buf->height  = s->height;
        buf->pix_fmt = s->pix_fmt;
    }

    for (i = 0; i < AV_NUM_DATA_POINTERS; i++) {
        pic->base[i]     = buf->base[i];
        pic->data[i]     = buf->data[i];
        pic->linesize[i] = buf->linesize[i];
    }
    pic->extended_data = pic->data;
    avci->buffer_count++;

    if (s->debug & FF_DEBUG_BUFFERS)
        printf( "default_get_buffer called on pic %p, %d "
                                "buffers used\n", pic, avci->buffer_count);

    return 0;
}

int audio_get_buffer(AVCodecContext *avctx, AVFrame *frame)
{
    AVCodecInternal *avci = avctx->internal;
    int buf_size, ret;

    av_freep(&avci->audio_data);
    buf_size = av_samples_get_buffer_size(NULL, avctx->channels,
                                          frame->nb_samples, avctx->sample_fmt,
                                          0);
    if (buf_size < 0)
        return -1;

    frame->data[0] = av_mallocz(buf_size);
    if (!frame->data[0])
        return -1;

    ret = avcodec_fill_audio_frame(frame, avctx->channels, avctx->sample_fmt,
                                   frame->data[0], buf_size, 0);
    if (ret < 0) {
        av_freep(&frame->data[0]);
        return ret;
    }

    avci->audio_data = frame->data[0];
    if (avctx->debug & FF_DEBUG_BUFFERS)
        printf( "default_get_buffer called on frame %p, "
                                    "internal audio buffer used\n", frame);

    return 0;
}


int avcodec_default_get_buffer(AVCodecContext *avctx, AVFrame *frame)
{
    frame->type = FF_BUFFER_TYPE_INTERNAL;
    switch (avctx->codec_type) {
    case AVMEDIA_TYPE_VIDEO:
        return video_get_buffer(avctx, frame);
    case AVMEDIA_TYPE_AUDIO:
        return audio_get_buffer(avctx, frame);
    default:
        return -1;
    }
}

/**
 * Deallocate a picture.
 */
inline void free_picture(MpegEncContext *s, Picture *pic)
{
	LOGD("free_picture Called");
    int i;

    if (pic->f.data[0] && pic->f.type != FF_BUFFER_TYPE_SHARED) {
        free_frame_buffer(s, pic);
    }

    LOGD("free_picture mb_var:%d mc_mb_var:%d mb_mean:%d mbskip_table:%d",pic->mb_var,pic->mc_mb_var,pic->mb_mean, pic->f.mbskip_table);
    LOGD("free picture qscale_table_base:%d mb_type_base:%d dct_coeff:%d pan_scan:%d", pic->qscale_table_base, pic->mb_type_base, pic->f.dct_coeff, pic->f.pan_scan);
    av_freep(&pic->mb_var);
    av_freep(&pic->mc_mb_var);
    av_freep(&pic->mb_mean);
    av_freep(&pic->f.mbskip_table);
    av_freep(&pic->qscale_table_base);
    pic->f.qscale_table = NULL;
    av_freep(&pic->mb_type_base);
    pic->f.mb_type = NULL;
    av_freep(&pic->f.dct_coeff);
    av_freep(&pic->f.pan_scan);
    pic->f.mb_type = NULL;
    for (i = 0; i < 2; i++) {
        av_freep(&pic->motion_val_base[i]);
        av_freep(&pic->f.ref_index[i]);
        pic->f.motion_val[i] = NULL;
    }

    if (pic->f.type == FF_BUFFER_TYPE_SHARED) {
        for (i = 0; i < 4; i++) {
            pic->f.base[i] =
            pic->f.data[i] = NULL;
        }
        pic->f.type = 0;
    }
}


void avcodec_default_release_buffer(AVCodecContext *s, AVFrame *pic)
{
    int i;
    InternalBuffer *buf, *last;
    AVCodecInternal *avci = s->internal;

//    av_assert0(s->codec_type == AVMEDIA_TYPE_VIDEO);

//    assert(pic->type == FF_BUFFER_TYPE_INTERNAL);
//    assert(avci->buffer_count);

    if (avci->buffer) {
        buf = NULL; /* avoids warning */
        for (i = 0; i < avci->buffer_count; i++) { //just 3-5 checks so is not worth to optimize
            buf = &avci->buffer[i];
            if (buf->data[0] == pic->data[0])
                break;
        }
//        av_assert0(i < avci->buffer_count);
        avci->buffer_count--;
        last = &avci->buffer[avci->buffer_count];

        if (buf != last)
            FFSWAP(InternalBuffer, *buf, *last);
    }

    for (i = 0; i < AV_NUM_DATA_POINTERS; i++)
        pic->data[i] = NULL;
//        pic->base[i]=NULL;

    if (s->debug & FF_DEBUG_BUFFERS)
        printf( "default_release_buffer called on pic %p, %d "
                                "buffers used\n", pic, avci->buffer_count);
}

const AVPixFmtDescriptor *av_pix_fmt_desc_get(enum AVPixelFormat pix_fmt)
{
    if (pix_fmt < 0 || pix_fmt >= AV_PIX_FMT_NB)
        return NULL;
    return &av_pix_fmt_descriptors[pix_fmt];
}


inline
int image_get_linesize(int width, int plane,
                       int max_step, int max_step_comp,
                       const AVPixFmtDescriptor *desc)
{
//	LOGD("image_get_linesize width:%d plane:%d max_step:%d max_step_comp:%d\n",width,plane,max_step,max_step_comp);
    int s, shifted_w, linesize;

    s = (max_step_comp == 1 || max_step_comp == 2) ? desc->log2_chroma_w : 0;
    shifted_w = ((width + (1 << s) - 1)) >> s;
    if (shifted_w && max_step > __INT_MAX__ / shifted_w)
        return -1;
    linesize = max_step * shifted_w;

//    LOGD("IMG_linesize:%d flag_PIX_BIT:%d",linesize,(desc->flags & PIX_FMT_BITSTREAM));
//    if (desc->flags & PIX_FMT_BITSTREAM)//呼ばれてないっぽい
//        linesize = (linesize + 7) >> 3;
    return linesize;
}


/**
 * Clip a signed integer value into the amin-amax range.
 * @param a value to clip
 * @param amin minimum value of the clip range
 * @param amax maximum value of the clip range
 * @return clipped value
 */
inline const int av_clip_c(int a, int amin, int amax)
{
#if defined(HAVE_AV_CONFIG_H) && defined(ASSERT_LEVEL) && ASSERT_LEVEL >= 2
    if (amin > amax) abort();
#endif
    if      (a < amin) return amin;
    else if (a > amax) return amax;
    else               return a;
}


int ff_dct_quantize_c(MpegEncContext *s,
                        int16_t *block, int n,
                        int qscale, int *overflow)
{
//	LOGD("ff_dct_quantize_c Called\n");

    int i, j, level, last_non_zero, q, start_i;
    const int *qmat;
    const uint8_t *scantable= s->intra_scantable.scantable;
    int bias;
    int max=0;
    unsigned int threshold1, threshold2;

//    int index;
//    for(index = 64; index < 64;index++){
//    	LOGD("TXff_dct_quantize_c block[%d]:%d\n",index,block[index]);
//    }//ここは合ってる

    s->dsp.fdct (block);//ここでおかしくなってた

//    for(index = 64; index < 64;index++){
//    	LOGD("AAff_dct_quantize_c block[%d]:%d\n",index,block[index]);
//    }

    if(s->dct_error_sum)
        s->denoise_dct(s, block);

    if (s->mb_intra) {
        if (!s->h263_aic) {
            if (n < 4)
                q = s->y_dc_scale;
            else
                q = s->c_dc_scale;
            q = q << 3;
        } else \
            /* For AIC we skip quant/dequant of INTRADC */
            q = 1 << 3;

        /* note: block[0] is assumed to be positive */
        block[0] = (block[0] + (q >> 1)) / q;
        start_i = 1;
        last_non_zero = 0;
        qmat = n < 4 ? s->q_intra_matrix[qscale] : s->q_chroma_intra_matrix[qscale];
        bias= s->intra_quant_bias<<(QMAT_SHIFT - QUANT_BIAS_SHIFT);
    } else {
        start_i = 0;
        last_non_zero = -1;
        qmat = s->q_inter_matrix[qscale];
        bias= s->inter_quant_bias<<(QMAT_SHIFT - QUANT_BIAS_SHIFT);
    }
    threshold1= (1<<QMAT_SHIFT) - bias - 1;
    threshold2= (threshold1<<1);
    for(i=63;i>=start_i;i--) {
        j = scantable[i];
        level = block[j] * qmat[j];

        if(((unsigned)(level+threshold1))>threshold2){
            last_non_zero = i;
            break;
        }else{
            block[j]=0;
        }
    }
    for(i=start_i; i<=last_non_zero; i++) {
        j = scantable[i];
        level = block[j] * qmat[j];

//        if(   bias+level >= (1<<QMAT_SHIFT)
//           || bias-level >= (1<<QMAT_SHIFT)){
        if(((unsigned)(level+threshold1))>threshold2){
            if(level>0){
                level= (bias + level)>>QMAT_SHIFT;
                block[j]= level;
            }else{
                level= (bias - level)>>QMAT_SHIFT;
                block[j]= -level;
            }
            max |=level;
        }else{
            block[j]=0;
        }
    }

    *overflow= s->max_qcoeff < max; //overflow might have happened

    /* we need this permutation so that we correct the IDCT, we only permute the !=0 elements */
    if (s->dsp.idct_permutation_type != FF_NO_IDCT_PERM)
        ff_block_permute(block, s->dsp.idct_permutation, scantable, last_non_zero);

    return last_non_zero;
}

inline double av_q2d(AVRational a){
    return (double)a.num / (double) a.den;
}

inline double qp2bits(RateControlEntry *rce, double qp)
{
    if (qp <= 0.0) {
        printf( "qp<=0.0\n");
    }
    return rce->qscale * (double)(rce->i_tex_bits + rce->p_tex_bits + 1) / qp;
}

inline double bits2qp(RateControlEntry *rce, double bits)
{
    if (bits < 0.9) {
        printf("bits<0.9\n");
    }
    return rce->qscale * (double)(rce->i_tex_bits + rce->p_tex_bits + 1) / bits;
}

/**
 * Modify the bitrate curve from pass1 for one frame.
 */
double get_qscale(MpegEncContext *s, RateControlEntry *rce,
                         double rate_factor, int frame_num)
{
    RateControlContext *rcc = &s->rc_context;
    AVCodecContext *a       = s->avctx;
    const int pict_type     = rce->new_pict_type;
    const double mb_num     = s->mb_num;
    double q=0.0, bits;
    int i;

    double const_values[] = {
        M_PI,
        M_E,
        rce->i_tex_bits * rce->qscale,
        rce->p_tex_bits * rce->qscale,
        (rce->i_tex_bits + rce->p_tex_bits) * (double)rce->qscale,
        rce->mv_bits / mb_num,
        rce->pict_type == AV_PICTURE_TYPE_B ? (rce->f_code + rce->b_code) * 0.5 : rce->f_code,
        rce->i_count / mb_num,
        rce->mc_mb_var_sum / mb_num,
        rce->mb_var_sum / mb_num,
        rce->pict_type == AV_PICTURE_TYPE_I,
        rce->pict_type == AV_PICTURE_TYPE_P,
        rce->pict_type == AV_PICTURE_TYPE_B,
        rcc->qscale_sum[pict_type] / (double)rcc->frame_count[pict_type],
        a->qcompress,
#if 0
        rcc->last_qscale_for[AV_PICTURE_TYPE_I],
        rcc->last_qscale_for[AV_PICTURE_TYPE_P],
        rcc->last_qscale_for[AV_PICTURE_TYPE_B],
        rcc->next_non_b_qscale,
#endif
        rcc->i_cplx_sum[AV_PICTURE_TYPE_I] / (double)rcc->frame_count[AV_PICTURE_TYPE_I],
        rcc->i_cplx_sum[AV_PICTURE_TYPE_P] / (double)rcc->frame_count[AV_PICTURE_TYPE_P],
        rcc->p_cplx_sum[AV_PICTURE_TYPE_P] / (double)rcc->frame_count[AV_PICTURE_TYPE_P],
        rcc->p_cplx_sum[AV_PICTURE_TYPE_B] / (double)rcc->frame_count[AV_PICTURE_TYPE_B],
        (rcc->i_cplx_sum[pict_type] + rcc->p_cplx_sum[pict_type]) / (double)rcc->frame_count[pict_type],
        0
    };

    bits = av_expr_eval(rcc->rc_eq_eval, const_values, rce);
    LOGD("get_qscale X bits:%e  \n",bits);
    if (isnan(bits)) {
        LOGD( "Error evaluating rc_eq \"%s\"\n", s->avctx->rc_eq);
        return -1;
    }

    rcc->pass1_rc_eq_output_sum += bits;
    bits *= rate_factor;
    if (bits < 0.0)
        bits = 0.0;
    bits += 1.0; // avoid 1/0 issues

    /* user override */
    for (i = 0; i < s->avctx->rc_override_count; i++) {
        RcOverride *rco = s->avctx->rc_override;
        if (rco[i].start_frame > frame_num)
            continue;
        if (rco[i].end_frame < frame_num)
            continue;

        if (rco[i].qscale)
            bits = qp2bits(rce, rco[i].qscale);  // FIXME move at end to really force it?
        else
            bits *= rco[i].quality_factor;
    }

    q = bits2qp(rce, bits);

    /* I/B difference */
    if (pict_type == AV_PICTURE_TYPE_I && s->avctx->i_quant_factor < 0.0)
        q = -q * s->avctx->i_quant_factor + s->avctx->i_quant_offset;
    else if (pict_type == AV_PICTURE_TYPE_B && s->avctx->b_quant_factor < 0.0)
        q = -q * s->avctx->b_quant_factor + s->avctx->b_quant_offset;
    if (q < 1)
        q = 1;

    return q;
}

void av_image_copy_plane(uint8_t       *dst, int dst_linesize,
                         const uint8_t *src, int src_linesize,
                         int bytewidth, int height)
{
    if (!dst || !src)
        return;
//    av_assert0(abs(src_linesize) >= bytewidth);
//    av_assert0(abs(dst_linesize) >= bytewidth);
    for (;height > 0; height--) {
        memcpy(dst, src, bytewidth);
        dst += dst_linesize;
        src += src_linesize;
    }
}



double get_fps(AVCodecContext *avctx)
{
    return 1.0 / av_q2d(avctx->time_base) / FFMAX(avctx->ticks_per_frame, 1);
}


int ff_vbv_update(MpegEncContext *s, int frame_size)
{
    RateControlContext *rcc = &s->rc_context;
    const double fps        = get_fps(s->avctx);
    const int buffer_size   = s->avctx->rc_buffer_size;
    const double min_rate   = s->avctx->rc_min_rate / fps;
    const double max_rate   = s->avctx->rc_max_rate / fps;

//    LOGD( "buffer_size %d rcc->buffer_inde%f frame_size%d %f min_rate%f max_rate:%f\n",
//            buffer_size, rcc->buffer_index, frame_size, min_rate, max_rate);

    if (buffer_size) {
        int left;

        rcc->buffer_index -= frame_size;
        if (rcc->buffer_index < 0) {
            LOGD("rc buffer underflow\n");
            rcc->buffer_index = 0;
        }

        left = buffer_size - rcc->buffer_index - 1;
        rcc->buffer_index += av_clip_c(left, min_rate, max_rate);

        if (rcc->buffer_index > buffer_size) {
            int stuffing = ceil((rcc->buffer_index - buffer_size) / 8);

            if (stuffing < 4 && s->codec_id == AV_CODEC_ID_MPEG4)
                stuffing = 4;
            rcc->buffer_index -= 8 * stuffing;

            if (s->avctx->debug & FF_DEBUG_RC)
            	LOGD( "stuffing %d bytes\n", stuffing);

            return stuffing;
        }
    }
    return 0;
}


//
int64_t av_rescale_rnd(int64_t a, int64_t b, int64_t c, enum AVRounding rnd){
    int64_t r=0;

    if (rnd & AV_ROUND_PASS_MINMAX) {//false

    	if (a == INT64_MIN || a == INT64_MAX)return a;

        rnd -= AV_ROUND_PASS_MINMAX;
    }
    if(a<0 && a != INT64_MIN) {//false
    	return -av_rescale_rnd(-a, b, c, rnd ^ ((rnd>>1)&1));
    }

    if(rnd==AV_ROUND_NEAR_INF) r= c/2;
    else if(rnd&1)             r= c-1;

    if(b <= __INT_MAX__ && c <= __INT_MAX__){
        if(a<=__INT_MAX__)
            return (a * b + r)/c;
        else
            return a/c*b + (a%c*b + r)/c;
    }else{
#if 1
        uint64_t a0= a&0xFFFFFFFF;
        uint64_t a1= a>>32;
        uint64_t b0= b&0xFFFFFFFF;
        uint64_t b1= b>>32;
        uint64_t t1= a0*b1 + a1*b0;
        uint64_t t1a= t1<<32;
        int i;

        a0 = a0*b0 + t1a;
        a1 = a1*b1 + (t1>>32) + (a0<t1a);
        a0 += r;
        a1 += a0<r;

        for(i=63; i>=0; i--){
//            int o= a1 & 0x8000000000000000ULL;
            a1+= a1 + ((a0>>i)&1);
            t1+=t1;
            if(/*o || */c <= a1){
                a1 -= c;
                t1++;
            }
        }
        return t1;
    }
#else
        AVInteger ai;
        ai= av_mul_i(av_int2i(a), av_int2i(b));
        ai= av_add_i(ai, av_int2i(r));

        return av_i2int(av_div_i(ai, av_int2i(c)));
    }
#endif
}




int64_t av_gcd(int64_t a, int64_t b){
    if(b) return av_gcd(b, a%b);
    else  return a;
}


inline void copy_block17(uint8_t *dst, const uint8_t *src, int dstStride, int srcStride, int h)
{
    int i;
    for(i=0; i<h; i++)
    {
        AV_COPY128U(dst, src);
        dst[16]= src[16];
        dst+=dstStride;
        src+=srcStride;
    }
}



int av_samples_get_buffer_size(int *linesize, int nb_channels, int nb_samples,
                               enum AVSampleFormat sample_fmt, int align)
{
    int line_size;
    int sample_size = av_get_bytes_per_sample(sample_fmt);
    int planar      = av_sample_fmt_is_planar(sample_fmt);

    /* validate parameter ranges */
    if (!sample_size || nb_samples <= 0 || nb_channels <= 0)
        return -1;

    /* auto-select alignment if not specified */
    if (!align) {
        align = 1;
        nb_samples = FFALIGN(nb_samples, 32);
    }

    /* check for integer overflow */
    if (nb_channels > INT_MAX / align ||
        (int64_t)nb_channels * nb_samples > (INT_MAX - (align * nb_channels)) / sample_size)
        return -1;

    line_size = planar ? FFALIGN(nb_samples * sample_size,               align) :
                         FFALIGN(nb_samples * sample_size * nb_channels, align);
    if (linesize)
        *linesize = line_size;

    LOGD("av_samples_get_buffer_size line_size:%d\n",line_size);

    return planar ? line_size * nb_channels : line_size;
}


int64_t av_gettime(void)
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (int64_t)tv.tv_sec * 1000000 + tv.tv_usec;
//#if HAVE_GETSYSTEMTIMEASFILETIME
//    FILETIME ft;
//    int64_t t;
//    GetSystemTimeAsFileTime(&ft);
//    t = (int64_t)ft.dwHighDateTime << 32 | ft.dwLowDateTime;
//    return t / 10 - 11644473600000000; /* Jan 1, 1601 */
//#else
//    return -1;
//#endif
}

int av_codec_is_decoder(const AVCodec *codec)
{
    return codec && codec->decode;
}


int64_t av_rescale_q_rnd(int64_t a, AVRational bq, AVRational cq,
                         enum AVRounding rnd)
{
    int64_t b= bq.num * (int64_t)cq.den;
    int64_t c= cq.num * (int64_t)bq.den;
    return av_rescale_rnd(a, b, c, rnd);
}


MAKE_ACCESSORS(AVFrame, frame, int64_t, best_effort_timestamp)
MAKE_ACCESSORS(AVFrame, frame, int64_t, pkt_duration)
MAKE_ACCESSORS(AVFrame, frame, int64_t, pkt_pos)
MAKE_ACCESSORS(AVFrame, frame, int64_t, channel_layout)
MAKE_ACCESSORS(AVFrame, frame, int,     channels)
MAKE_ACCESSORS(AVFrame, frame, int,     sample_rate)
MAKE_ACCESSORS(AVFrame, frame, int,     decode_error_flags)
MAKE_ACCESSORS(AVFrame, frame, int,     pkt_size)
MAKE_ACCESSORS(AVCodecContext, codec, AVRational, pkt_timebase)


void avcodec_get_frame_defaults(AVFrame *frame)
{
    //この関数がMAX_PICTURE_SIZEの36回呼ばれている
#if LIBAVCODEC_VERSION_MAJOR >= 55
     // extended_data should explicitly be freed when needed, this code is unsafe currently
     // also this is not compatible to the <55 ABI/API
    if (frame->extended_data != frame->data && 0)
        av_freep(&frame->extended_data);
#endif
    memset(frame, 0, sizeof(AVFrame));

    frame->pts                   =
    frame->pkt_dts               =
    frame->pkt_pts               = AV_NOPTS_VALUE;
    av_frame_set_best_effort_timestamp(frame, AV_NOPTS_VALUE);
    av_frame_set_pkt_duration         (frame, 0);
    av_frame_set_pkt_pos              (frame, -1);
    av_frame_set_pkt_size             (frame, -1);
    frame->key_frame           = 1;
    frame->sample_aspect_ratio = (AVRational) {0, 1 };
    frame->format              = -1; /* unknown */
    frame->extended_data       = frame->data;
    frame->pict_type = 0;//オリジナルでは入れていない
    frame->pkt_pos = 0;
    frame->pkt_duration = 1;
}

void av_image_fill_max_pixsteps(int max_pixsteps[4], int max_pixstep_comps[4],
                                const AVPixFmtDescriptor *pixdesc)
{
    int i;
    memset(max_pixsteps, 0, 4*sizeof(max_pixsteps[0]));
    if (max_pixstep_comps)
        memset(max_pixstep_comps, 0, 4*sizeof(max_pixstep_comps[0]));

    for (i = 0; i < 4; i++) {
        const AVComponentDescriptor *comp = &(pixdesc->comp[i]);
        if ((comp->step_minus1+1) > max_pixsteps[comp->plane]) {
            max_pixsteps[comp->plane] = comp->step_minus1+1;
            if (max_pixstep_comps)
                max_pixstep_comps[comp->plane] = i;
//            LOGD( "max_pixsteps :%02X \n",max_pixstep_comps[comp->plane]);
        }
    }
//    if(max_pixsteps){
//    	for(i = 0; i<4 ; i++){
//    	LOGD("max_pixsteps[%d]:%d",i,max_pixsteps[i]);
//    	LOGD("max_pixsteps[%d]:%d",i,max_pixsteps[i]);
//    	LOGD("max_pixsteps[%d]:%d",i,max_pixsteps[i]);
//    	LOGD("max_pixsteps[%d]:%d",i,max_pixsteps[i]);
//    	LOGD("max_pixsteps[%d]:%d\n",i,max_pixsteps[i]);
//    	}
//    }
//    if(max_pixstep_comps){
//        	for(i = 0; i<4 ; i++){
//        	LOGD("max_pixstep_comps[%d]:%d",i,max_pixstep_comps[i]);
//        	LOGD("max_pixstep_comps[%d]:%d",i,max_pixstep_comps[i]);
//        	LOGD("max_pixstep_comps[%d]:%d",i,max_pixstep_comps[i]);
//        	LOGD("max_pixstep_comps[%d]:%d",i,max_pixstep_comps[i]);
//        	LOGD("max_pixstep_comps[%d]:%d\n",i,max_pixstep_comps[i]);
//        	}
//        }
//    LOGD("pixdesc :%d\n",pixdesc->comp);
//       if(pixdesc->comp){
//       	for(i = 0; i<4 ; i++){
//       	LOGD("pixdesc :%d",pixdesc->comp[i].depth_minus1);
//       	LOGD("pixdesc :%d",pixdesc->comp[i].offset_plus1);
//       	LOGD("pixdesc :%d",pixdesc->comp[i].shift);
//       	LOGD("pixdesc :%d",pixdesc->comp[i].plane);
//       	LOGD("pixdesc :%d",pixdesc->comp[i].step_minus1);
//       	}
//       }
//   	LOGD("\n");
//       LOGD("pixdesc :%d\n",pixdesc->flags);
//       LOGD("pixdesc :%d\n",pixdesc->log2_chroma_h);
//       LOGD("pixdesc :%d\n",pixdesc->log2_chroma_w);
//       LOGD("pixdesc :%s\n",pixdesc->name);
//       LOGD("pixdesc :%d\n",pixdesc->nb_components);
}


void ff_init_buffer_info(AVCodecContext *s, AVFrame *frame)
{
    if (s->pkt) {
        frame->pkt_pts = s->pkt->pts;
        av_frame_set_pkt_pos     (frame, s->pkt->pos);
        av_frame_set_pkt_duration(frame, s->pkt->duration);
        av_frame_set_pkt_size    (frame, s->pkt->size);
    } else {
        frame->pkt_pts = AV_NOPTS_VALUE;
        av_frame_set_pkt_pos     (frame, -1);
        av_frame_set_pkt_duration(frame, 0);
        av_frame_set_pkt_size    (frame, -1);
    }
    frame->reordered_opaque = s->reordered_opaque;

    switch (s->codec->type) {
    case AVMEDIA_TYPE_VIDEO:
        frame->width               = s->width;
        frame->height              = s->height;
        frame->format              = s->pix_fmt;
        frame->sample_aspect_ratio = s->sample_aspect_ratio;
        break;
    case AVMEDIA_TYPE_AUDIO:
        frame->sample_rate    = s->sample_rate;
        frame->format         = s->sample_fmt;
        frame->channel_layout = s->channel_layout;
        av_frame_set_channels(frame, s->channels);
        break;
    }
}


int av_reduce(int *dst_num, int *dst_den,
              int64_t num, int64_t den, int64_t max)
{
    AVRational a0 = { 0, 1 }, a1 = { 1, 0 };
    int sign = (num < 0) ^ (den < 0);
    int64_t gcd = av_gcd(FFABS(num), FFABS(den));

    if (gcd) {
        num = FFABS(num) / gcd;
        den = FFABS(den) / gcd;
    }
    if (num <= max && den <= max) {
        a1 = (AVRational) { num, den };
        den = 0;
    }

    while (den) {
        uint64_t x        = num / den;
        int64_t next_den  = num - den * x;
        int64_t a2n       = x * a1.num + a0.num;
        int64_t a2d       = x * a1.den + a0.den;

        if (a2n > max || a2d > max) {
            if (a1.num) x =          (max - a0.num) / a1.num;
            if (a1.den) x = FFMIN(x, (max - a0.den) / a1.den);

            if (den * (2 * x * a1.den + a0.den) > num * a1.den)
                a1 = (AVRational) { x * a1.num + a0.num, x * a1.den + a0.den };
            break;
        }

        a0  = a1;
        a1  = (AVRational) { a2n, a2d };
        num = den;
        den = next_den;
    }
//    av_assert2(av_gcd(a1.num, a1.den) <= 1U);

    *dst_num = sign ? -a1.num : a1.num;
    *dst_den = a1.den;

    return den == 0;
}


int ff_get_buffer(AVCodecContext *avctx, AVFrame *frame)
{
    ff_init_buffer_info(avctx, frame);

    return avctx->get_buffer(avctx, frame);
}


int ff_frame_thread_encoder_init(AVCodecContext *avctx, AVDictionary *options){
    int i=0;
    ThreadContext *c;


    if(   !(avctx->thread_type & FF_THREAD_FRAME)
       || !(avctx->codec->capabilities & CODEC_CAP_INTRA_ONLY))
        return 0;

    if(!avctx->thread_count) {
        avctx->thread_count = ff_get_logical_cpus(avctx);
        avctx->thread_count = FFMIN(avctx->thread_count, MAX_THREADS);
    }

    if(avctx->thread_count <= 1)
        return 0;

    if(avctx->thread_count > MAX_THREADS)
        return -1;

//    av_assert0(!avctx->internal->frame_thread_encoder);
    c = avctx->internal->frame_thread_encoder = av_mallocz(sizeof(ThreadContext));
    if(!c)
        return -1;

    c->parent_avctx = avctx;

    c->task_fifo = av_fifo_alloc(sizeof(Task) * BUFFER_SIZE);
    if(!c->task_fifo)
        goto fail;

    pthread_mutex_init(&c->task_fifo_mutex, NULL);
    pthread_mutex_init(&c->finished_task_mutex, NULL);
    pthread_mutex_init(&c->buffer_mutex, NULL);
    pthread_cond_init(&c->task_fifo_cond, NULL);
    pthread_cond_init(&c->finished_task_cond, NULL);
    LOGD("THREAD COUNT:%d",avctx->thread_count);
    for(i=0; i<avctx->thread_count ; i++){
        AVDictionary *tmp = NULL;
        void *tmpv;
        AVCodecContext *thread_avctx = avcodec_alloc_context3(avctx->codec);
        if(!thread_avctx)
            goto fail;
        tmpv = thread_avctx->priv_data;
        *thread_avctx = *avctx;
        thread_avctx->priv_data = tmpv;
        thread_avctx->internal = NULL;
        memcpy(thread_avctx->priv_data, avctx->priv_data, avctx->codec->priv_data_size);
        thread_avctx->thread_count = 1;
        thread_avctx->active_thread_type &= ~FF_THREAD_FRAME;

//        av_dict_copy(&tmp, options, 0);
//        av_dict_set(&tmp, "threads", "1", 0);
//        if(avcodec_open2(thread_avctx, avctx->codec, &tmp) < 0) {
//            av_dict_free(&tmp);
//            goto fail;
//        }
//        av_dict_free(&tmp);
//        av_assert0(!thread_avctx->internal->frame_thread_encoder);
        thread_avctx->internal->frame_thread_encoder = c;
        if(pthread_create(&c->worker[i], NULL, c->worker, thread_avctx)) {
            goto fail;
        }
    }

    avctx->active_thread_type = FF_THREAD_FRAME;

    return 0;
fail:
    avctx->thread_count = i;
    printf("ff_frame_thread_encoder_init failed\n");
    ff_frame_thread_encoder_free(avctx);
    return -1;
}

int ff_alloc_packet2(AVCodecContext *avctx, AVPacket *avpkt, int size)
{
    if (size < 0 || avpkt->size < 0 || size > INT_MAX - FF_INPUT_BUFFER_PADDING_SIZE) {
        LOGD( "Size %d invalid\n", size);
        return-1;
    }

    if (avctx) {
//        av_assert0(!avpkt->data || avpkt->data != avctx->internal->byte_buffer);
        if (!avpkt->data || avpkt->size < size) {
            av_fast_padded_malloc(&avctx->internal->byte_buffer, &avctx->internal->byte_buffer_size, size);
            avpkt->data = avctx->internal->byte_buffer;
            avpkt->size = avctx->internal->byte_buffer_size;
            avpkt->destruct = NULL;
        }
    }

    if (avpkt->data) {
        void *destruct = avpkt->destruct;

        if (avpkt->size < size) {
            printf( "User packet is too small (%d < %d)\n", avpkt->size, size);
            return-1;
        }

        av_init_packet(avpkt);
        avpkt->destruct = destruct;
        avpkt->size     = size;
        return 0;
    } else {
        int ret = av_new_packet(avpkt, size);
        if (ret < 0)
            printf( "Failed to allocate packet of size %d\n", size);
        return ret;
    }
}

static int zero_cmp(void *s, uint8_t *a, uint8_t *b, int stride, int h){
    return 0;
}

void ff_set_cmp(DSPContext* c, me_cmp_func *cmp, int type){
//	LOGD("ff_set_cmp Called\n");
    int i;

    memset(cmp, 0, sizeof(void*)*6);
    //cmpは6個の配列
    for(i=0; i<6; i++){
        switch(type&0xFF){
        case FF_CMP_SAD://0
            cmp[i]= c->sad[i];
            break;
        case FF_CMP_SATD://2
            cmp[i]= c->hadamard8_diff[i];
            break;
        case FF_CMP_SSE://1
            cmp[i]= c->sse[i];
            break;
        case FF_CMP_DCT://3
            cmp[i]= c->dct_sad[i];
            break;
        case FF_CMP_DCT264://14
            cmp[i]= c->dct264_sad[i];
            break;
        case FF_CMP_DCTMAX://13
            cmp[i]= c->dct_max[i];
            break;
        case FF_CMP_PSNR://4
            cmp[i]= c->quant_psnr[i];
            break;
        case FF_CMP_BIT://5
            cmp[i]= c->bit[i];
            break;
        case FF_CMP_RD://6
            cmp[i]= c->rd[i];
            break;
        case FF_CMP_VSAD://8
            cmp[i]= c->vsad[i];
            break;
        case FF_CMP_VSSE://9
            cmp[i]= c->vsse[i];
            break;
        case FF_CMP_ZERO://7
            cmp[i]= zero_cmp;
            break;
        case FF_CMP_NSSE://10
            cmp[i]= c->nsse[i];
            break;
//#if CONFIG_DWT
        case FF_CMP_W53://11
            cmp[i]= c->w53[i];
            break;
        case FF_CMP_W97://12
            cmp[i]= c->w97[i];
            break;
//#endif
        default:
            LOGD("internal error in cmp function selection\n");
        }
    }
}
