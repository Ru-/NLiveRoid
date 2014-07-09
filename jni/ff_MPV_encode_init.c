

#ifndef UTIL_H_
#include "util.h"
#define UTIL_H_
#endif

//util.hの後にincludeしないとおかしくなる
#ifndef H263_ENCODER_INIT_C_
#include "h263_encoder_init.h"
#define H263_ENCODER_INIT_C_
#endif


#include <pthread.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>

#include <android/log.h>
#define LOG_TAG "NLiveRoid"
#define LOGD(...)  __android_log_print(3, LOG_TAG, __VA_ARGS__)

#define EDGE_TOP    1
#define EDGE_BOTTOM 2


#   define pixel  uint8_t
#   define pixel2 uint16_t
#   define pixel4 uint32_t
#   define dctcoef int16_t


#define BIT_DEPTH 8
#define FUNC3(a, b, c)  a ## _ ## b ## c
#define FUNC2(a, b, c)  FUNC3(a, b, c)
#define FUNC(a)  FUNC2(a, BIT_DEPTH,)
#define FUNCC(a) FUNC2(a, BIT_DEPTH, _c)

#define         BYTE_VEC32(c)   ((c)*0x01010101UL)
#define         BYTE_VEC64(c)   ((c)*0x0001000100010001UL)



static uint8_t default_fcode_tab[MAX_MV * 2 + 1];
static uint8_t default_mv_penalty[MAX_FCODE + 1][MAX_MV * 2 + 1];



#define MUL16(a,b) ((a) * (b))
/* pixel operations */
#define MAX_NEG_CROP 1024






int start_mb_y;            ///< start mb_y of this thread (so current thread should process start_mb_y <= row < end_mb_y)
int end_mb_y;              ///< end   mb_y of this thread (so current thread should process start_mb_y <= row < end_mb_y)



//#if FF_API_AV_REVERSE
const uint8_t av_reverse[256]={
0x00,0x80,0x40,0xC0,0x20,0xA0,0x60,0xE0,0x10,0x90,0x50,0xD0,0x30,0xB0,0x70,0xF0,
0x08,0x88,0x48,0xC8,0x28,0xA8,0x68,0xE8,0x18,0x98,0x58,0xD8,0x38,0xB8,0x78,0xF8,
0x04,0x84,0x44,0xC4,0x24,0xA4,0x64,0xE4,0x14,0x94,0x54,0xD4,0x34,0xB4,0x74,0xF4,
0x0C,0x8C,0x4C,0xCC,0x2C,0xAC,0x6C,0xEC,0x1C,0x9C,0x5C,0xDC,0x3C,0xBC,0x7C,0xFC,
0x02,0x82,0x42,0xC2,0x22,0xA2,0x62,0xE2,0x12,0x92,0x52,0xD2,0x32,0xB2,0x72,0xF2,
0x0A,0x8A,0x4A,0xCA,0x2A,0xAA,0x6A,0xEA,0x1A,0x9A,0x5A,0xDA,0x3A,0xBA,0x7A,0xFA,
0x06,0x86,0x46,0xC6,0x26,0xA6,0x66,0xE6,0x16,0x96,0x56,0xD6,0x36,0xB6,0x76,0xF6,
0x0E,0x8E,0x4E,0xCE,0x2E,0xAE,0x6E,0xEE,0x1E,0x9E,0x5E,0xDE,0x3E,0xBE,0x7E,0xFE,
0x01,0x81,0x41,0xC1,0x21,0xA1,0x61,0xE1,0x11,0x91,0x51,0xD1,0x31,0xB1,0x71,0xF1,
0x09,0x89,0x49,0xC9,0x29,0xA9,0x69,0xE9,0x19,0x99,0x59,0xD9,0x39,0xB9,0x79,0xF9,
0x05,0x85,0x45,0xC5,0x25,0xA5,0x65,0xE5,0x15,0x95,0x55,0xD5,0x35,0xB5,0x75,0xF5,
0x0D,0x8D,0x4D,0xCD,0x2D,0xAD,0x6D,0xED,0x1D,0x9D,0x5D,0xDD,0x3D,0xBD,0x7D,0xFD,
0x03,0x83,0x43,0xC3,0x23,0xA3,0x63,0xE3,0x13,0x93,0x53,0xD3,0x33,0xB3,0x73,0xF3,
0x0B,0x8B,0x4B,0xCB,0x2B,0xAB,0x6B,0xEB,0x1B,0x9B,0x5B,0xDB,0x3B,0xBB,0x7B,0xFB,
0x07,0x87,0x47,0xC7,0x27,0xA7,0x67,0xE7,0x17,0x97,0x57,0xD7,0x37,0xB7,0x77,0xF7,
0x0F,0x8F,0x4F,0xCF,0x2F,0xAF,0x6F,0xEF,0x1F,0x9F,0x5F,0xDF,0x3F,0xBF,0x7F,0xFF,
};
//#endif

static const int8_t si_prefixes['z' - 'E' + 1] = {
    ['y'-'E']= -24,
    ['z'-'E']= -21,
    ['a'-'E']= -18,
    ['f'-'E']= -15,
    ['p'-'E']= -12,
    ['n'-'E']= - 9,
    ['u'-'E']= - 6,
    ['m'-'E']= - 3,
    ['c'-'E']= - 2,
    ['d'-'E']= - 1,
    ['h'-'E']=   2,
    ['k'-'E']=   3,
    ['K'-'E']=   3,
    ['M'-'E']=   6,
    ['G'-'E']=   9,
    ['T'-'E']=  12,
    ['P'-'E']=  15,
    ['E'-'E']=  18,
    ['Z'-'E']=  21,
    ['Y'-'E']=  24,
};


const uint8_t ff_h263_loop_filter_strength[32]={
//  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
    0, 1, 1, 2, 2, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9,10,10,10,11,11,11,12,12,12
};


/* Input permutation for the simple_idct_mmx */
static const uint8_t simple_mmx_permutation[64]={
        0x00, 0x08, 0x04, 0x09, 0x01, 0x0C, 0x05, 0x0D,
        0x10, 0x18, 0x14, 0x19, 0x11, 0x1C, 0x15, 0x1D,
        0x20, 0x28, 0x24, 0x29, 0x21, 0x2C, 0x25, 0x2D,
        0x12, 0x1A, 0x16, 0x1B, 0x13, 0x1E, 0x17, 0x1F,
        0x02, 0x0A, 0x06, 0x0B, 0x03, 0x0E, 0x07, 0x0F,
        0x30, 0x38, 0x34, 0x39, 0x31, 0x3C, 0x35, 0x3D,
        0x22, 0x2A, 0x26, 0x2B, 0x23, 0x2E, 0x27, 0x2F,
        0x32, 0x3A, 0x36, 0x3B, 0x33, 0x3E, 0x37, 0x3F,
};

static const uint8_t idct_sse2_row_perm[8] = {0, 4, 1, 5, 2, 6, 3, 7};

//#define BIT_DEPTH 9
//#include "dsputil_template.c"
//#undef BIT_DEPTH
//
//#define BIT_DEPTH 10
//#include "dsputil_template.c"
//#undef BIT_DEPTH
//
//#define BIT_DEPTH 12
//#include "dsputil_template.c"
//#undef BIT_DEPTH
//
//#define BIT_DEPTH 14
//#include "dsputil_template.c"
//#undef BIT_DEPTHc"





#define avg2(a,b) ((a+b+1)>>1)
#define avg4(a,b,c,d) ((a+b+c+d+2)>>2)





void ff_thread_release_buffer(AVCodecContext *avctx, AVFrame *f)
{
    PerThreadContext *p = avctx->thread_opaque;
    FrameThreadContext *fctx;

    if (!f->data[0])
        return;

    if (!(avctx->active_thread_type&FF_THREAD_FRAME)) {
        avctx->release_buffer(avctx, f);
        return;
    }

    if (p->num_released_buffers >= MAX_BUFFERS) {
        LOGD("too many thread_release_buffer calls!\n");
        return;
    }

    if(avctx->debug & FF_DEBUG_BUFFERS)
    	LOGD( "thread_release_buffer called on pic %p\n", f);

    fctx = p->parent;
    pthread_mutex_lock(&fctx->buffer_mutex);
    p->released_buffers[p->num_released_buffers++] = *f;
    pthread_mutex_unlock(&fctx->buffer_mutex);
    memset(f->data, 0, sizeof(f->data));
}




//
//int av_reduce(int *dst_num, int *dst_den,
//              int64_t num, int64_t den, int64_t max)
//{
//    AVRational a0 = { 0, 1 }, a1 = { 1, 0 };
//    int sign = (num < 0) ^ (den < 0);
//    int64_t gcd = av_gcd(FFABS(num), FFABS(den));
//
//    if (gcd) {
//        num = FFABS(num) / gcd;
//        den = FFABS(den) / gcd;
//    }
//    if (num <= max && den <= max) {
//        a1 = (AVRational) { num, den };
//        den = 0;
//    }
//
//    while (den) {
//        uint64_t x        = num / den;
//        int64_t next_den  = num - den * x;
//        int64_t a2n       = x * a1.num + a0.num;
//        int64_t a2d       = x * a1.den + a0.den;
//
//        if (a2n > max || a2d > max) {
//            if (a1.num) x =          (max - a0.num) / a1.num;
//            if (a1.den) x = FFMIN(x, (max - a0.den) / a1.den);
//
//            if (den * (2 * x * a1.den + a0.den) > num * a1.den)
//                a1 = (AVRational) { x * a1.num + a0.num, x * a1.den + a0.den };
//            break;
//        }
//
//        a0  = a1;
//        a1  = (AVRational) { a2n, a2d };
//        num = den;
//        den = next_den;
//    }
//    av_assert2(av_gcd(a1.num, a1.den) <= 1U);
//
//    *dst_num = sign ? -a1.num : a1.num;
//    *dst_den = a1.den;
//
//    return den == 0;
//}

static void dct_unquantize_h263_intra_c(MpegEncContext *s,
                                  int16_t *block, int n, int qscale)
{
    int i, level, qmul, qadd;
    int nCoeffs;

//    assert(s->block_last_index[n]>=0);

    qmul = qscale << 1;

    if (!s->h263_aic) {
        block[0] *= n < 4 ? s->y_dc_scale : s->c_dc_scale;
        qadd = (qscale - 1) | 1;
    }else{
        qadd = 0;
    }
    if(s->ac_pred)
        nCoeffs=63;
    else
        nCoeffs= s->inter_scantable.raster_end[ s->block_last_index[n] ];

    for(i=1; i<=nCoeffs; i++) {
        level = block[i];
        if (level) {
            if (level < 0) {
                level = level * qmul - qadd;
            } else {
                level = level * qmul + qadd;
            }
            block[i] = level;
        }
    }
}

static void dct_unquantize_h263_inter_c(MpegEncContext *s,
                                  int16_t *block, int n, int qscale)
{
    int i, level, qmul, qadd;
    int nCoeffs;

//    assert(s->block_last_index[n]>=0);

    qadd = (qscale - 1) | 1;
    qmul = qscale << 1;

    nCoeffs= s->inter_scantable.raster_end[ s->block_last_index[n] ];

    for(i=0; i<=nCoeffs; i++) {
        level = block[i];
        if (level) {
            if (level < 0) {
                level = level * qmul - qadd;
            } else {
                level = level * qmul + qadd;
            }
            block[i] = level;
        }
    }
}

static void dct_unquantize_mpeg1_intra_c(MpegEncContext *s,
                                   int16_t *block, int n, int qscale)
{
    int i, level, nCoeffs;
    const uint16_t *quant_matrix;

    nCoeffs= s->block_last_index[n];

    block[0] *= n < 4 ? s->y_dc_scale : s->c_dc_scale;
    /* XXX: only mpeg1 */
    quant_matrix = s->intra_matrix;
    for(i=1;i<=nCoeffs;i++) {
        int j= s->intra_scantable.permutated[i];
        level = block[j];
        if (level) {
            if (level < 0) {
                level = -level;
                level = (int)(level * qscale * quant_matrix[j]) >> 3;
                level = (level - 1) | 1;
                level = -level;
            } else {
                level = (int)(level * qscale * quant_matrix[j]) >> 3;
                level = (level - 1) | 1;
            }
            block[j] = level;
        }
    }
}

static void dct_unquantize_mpeg1_inter_c(MpegEncContext *s,
                                   int16_t *block, int n, int qscale)
{
    int i, level, nCoeffs;
    const uint16_t *quant_matrix;

    nCoeffs= s->block_last_index[n];

    quant_matrix = s->inter_matrix;
    for(i=0; i<=nCoeffs; i++) {
        int j= s->intra_scantable.permutated[i];
        level = block[j];
        if (level) {
            if (level < 0) {
                level = -level;
                level = (((level << 1) + 1) * qscale *
                         ((int) (quant_matrix[j]))) >> 4;
                level = (level - 1) | 1;
                level = -level;
            } else {
                level = (((level << 1) + 1) * qscale *
                         ((int) (quant_matrix[j]))) >> 4;
                level = (level - 1) | 1;
            }
            block[j] = level;
        }
    }
}

static void dct_unquantize_mpeg2_intra_c(MpegEncContext *s,
                                   int16_t *block, int n, int qscale)
{
	LOGD("dct_unquantize_mpeg2_intra_c block_last_index\n");
    int i, level, nCoeffs;
    const uint16_t *quant_matrix;

    if(s->alternate_scan) nCoeffs= 63;
    else nCoeffs= s->block_last_index[n];

    block[0] *= n < 4 ? s->y_dc_scale : s->c_dc_scale;
    quant_matrix = s->intra_matrix;
    for(i=1;i<=nCoeffs;i++) {
        int j= s->intra_scantable.permutated[i];
        level = block[j];
        if (level) {
            if (level < 0) {
                level = -level;
                level = (int)(level * qscale * quant_matrix[j]) >> 3;
                level = -level;
            } else {
                level = (int)(level * qscale * quant_matrix[j]) >> 3;
            }
            block[j] = level;
        }
    }
}

static void dct_unquantize_mpeg2_intra_bitexact(MpegEncContext *s,
                                   int16_t *block, int n, int qscale)
{
    int i, level, nCoeffs;
    const uint16_t *quant_matrix;
    int sum=-1;

    if(s->alternate_scan) nCoeffs= 63;
    else nCoeffs= s->block_last_index[n];

    block[0] *= n < 4 ? s->y_dc_scale : s->c_dc_scale;
    sum += block[0];
    quant_matrix = s->intra_matrix;
    for(i=1;i<=nCoeffs;i++) {
        int j= s->intra_scantable.permutated[i];
        level = block[j];
        if (level) {
            if (level < 0) {
                level = -level;
                level = (int)(level * qscale * quant_matrix[j]) >> 3;
                level = -level;
            } else {
                level = (int)(level * qscale * quant_matrix[j]) >> 3;
            }
            block[j] = level;
            sum+=level;
        }
    }
    block[63]^=sum&1;
}

static void dct_unquantize_mpeg2_inter_c(MpegEncContext *s,
                                   int16_t *block, int n, int qscale)
{
    int i, level, nCoeffs;
    const uint16_t *quant_matrix;
    int sum=-1;

    if(s->alternate_scan) nCoeffs= 63;
    else nCoeffs= s->block_last_index[n];

    quant_matrix = s->inter_matrix;
    for(i=0; i<=nCoeffs; i++) {
        int j= s->intra_scantable.permutated[i];
        level = block[j];
        if (level) {
            if (level < 0) {
                level = -level;
                level = (((level << 1) + 1) * qscale *
                         ((int) (quant_matrix[j]))) >> 4;
                level = -level;
            } else {
                level = (((level << 1) + 1) * qscale *
                         ((int) (quant_matrix[j]))) >> 4;
            }
            block[j] = level;
            sum+=level;
        }
    }
    block[63]^=sum&1;
}


void ff_init_scantable(uint8_t *permutation, ScanTable *st, const uint8_t *src_scantable){
    int i;
    int end;

    st->scantable= src_scantable;

    for(i=0; i<64; i++){
        int j;
        j = src_scantable[i];
        st->permutated[i] = permutation[j];
    }

    end=-1;
    for(i=0; i<64; i++){
        int j;
        j = st->permutated[i];
        if(j>end) end=j;
        st->raster_end[i]= end;
    }
}




/**
 * CPU関係は複雑すぎるのでとりあえず省略
 * BIT_DEPTHは多分8だから8にしてしまう
 */
//void ff_emulated_edge_mc_8(uint8_t *buf, const uint8_t *src,
//                                      ptrdiff_t linesize_arg,
//                                      int block_w, int block_h,
//                                      int src_x, int src_y, int w, int h)
//{
//    int x, y;
//    int start_y, start_x, end_y, end_x;
//    int linesize = linesize_arg;
//
//    if (!w || !h)
//        return;
//
//    if (src_y >= h) {
//        src -= src_y * linesize;
//        src += (h - 1) * linesize;
//        src_y = h - 1;
//    } else if (src_y <= -block_h) {
//        src -= src_y * linesize;
//        src += (1 - block_h) * linesize;
//        src_y = 1 - block_h;
//    }
//    if (src_x >= w) {
//        src  += (w - 1 - src_x) * sizeof(pixel);
//        src_x = w - 1;
//    } else if (src_x <= -block_w) {
//        src  += (1 - block_w - src_x) * sizeof(pixel);
//        src_x = 1 - block_w;
//    }
//
//    start_y = FFMAX(0, -src_y);
//    start_x = FFMAX(0, -src_x);
//    end_y = FFMIN(block_h, h-src_y);
//    end_x = FFMIN(block_w, w-src_x);
//    av_assert2(start_y < end_y && block_h);
//    av_assert2(start_x < end_x && block_w);
//
//    w    = end_x - start_x;
//    src += start_y * linesize + start_x * sizeof(pixel);
//    buf += start_x * sizeof(pixel);
//
//    // top
//    for (y = 0; y < start_y; y++) {
//        memcpy(buf, src, w * sizeof(pixel));
//        buf += linesize;
//    }
//
//    // copy existing part
//    for (; y < end_y; y++) {
//        memcpy(buf, src, w * sizeof(pixel));
//        src += linesize;
//        buf += linesize;
//    }
//
//    // bottom
//    src -= linesize;
//    for (; y < block_h; y++) {
//        memcpy(buf, src, w * sizeof(pixel));
//        buf += linesize;
//    }
//
//    buf -= block_h * linesize + start_x * sizeof(pixel);
//    while (block_h--) {
//        pixel *bufp = (pixel *) buf;
//
//        // left
//        for(x = 0; x < start_x; x++) {
//            bufp[x] = bufp[start_x];
//        }
//
//        // right
//        for (x = end_x; x < block_w; x++) {
//            bufp[x] = bufp[end_x - 1];
//        }
//        buf += linesize;
//    }
//}
//
//
//static inline void emulated_edge_mc(uint8_t *buf, const uint8_t *src,
//                                              ptrdiff_t linesize_arg,
//                                              int block_w, int block_h,
//                                              int src_x, int src_y,
//                                              int w, int h,
//                                              emu_edge_core_func *core_fn)
//{
//    int start_y, start_x, end_y, end_x, src_y_add = 0;
//    int linesize = linesize_arg;
//
//    if(!w || !h)
//        return;
//
//    if (src_y >= h) {
//        src -= src_y*linesize;
//        src_y_add = h - 1;
//        src_y     = h - 1;
//    } else if (src_y <= -block_h) {
//        src -= src_y*linesize;
//        src_y_add = 1 - block_h;
//        src_y     = 1 - block_h;
//    }
//    if (src_x >= w) {
//        src   += w - 1 - src_x;
//        src_x  = w - 1;
//    } else if (src_x <= -block_w) {
//        src   += 1 - block_w - src_x;
//        src_x  = 1 - block_w;
//    }
//
//    start_y = FFMAX(0, -src_y);
//    start_x = FFMAX(0, -src_x);
//    end_y   = FFMIN(block_h, h-src_y);
//    end_x   = FFMIN(block_w, w-src_x);
//    av_assert2(start_x < end_x && block_w > 0);
//    av_assert2(start_y < end_y && block_h > 0);
//
//    // fill in the to-be-copied part plus all above/below
//    src += (src_y_add + start_y) * linesize + start_x;
//    buf += start_x;
//    core_fn(buf, src, linesize, start_y, end_y,
//            block_h, start_x, end_x, block_w);
//}
//
//static void emulated_edge_mc_mmx(uint8_t *buf, const uint8_t *src,
//                                             ptrdiff_t linesize,
//                                             int block_w, int block_h,
//                                             int src_x, int src_y, int w, int h)
//{
//    emulated_edge_mc(buf, src, linesize, block_w, block_h, src_x, src_y,
//                     w, h, &emulated_edge_mc);
//}
//
//static int flags, checked;
//
//int av_get_cpu_flags(void)
//{
//    if (checked)
//        return flags;
//
//    if (ARCH_ARM) flags = ff_get_cpu_flags_arm();
//    if (ARCH_X86) flags = ff_get_cpu_flags_x86();
//
//    checked = 1;
//    return flags;
//}
//static void emulated_edge_mc_sse(uint8_t *buf, const uint8_t *src,
//                                             ptrdiff_t linesize,
//                                             int block_w, int block_h,
//                                             int src_x, int src_y, int w, int h)
//{
//    emulated_edge_mc(buf, src, linesize, block_w, block_h, src_x, src_y,
//                     w, h, &emulated_edge_mc);
//}
//av_cold void ff_videodsp_init_x86(VideoDSPContext *ctx, int bpc)
//{
//    int mm_flags = av_get_cpu_flags();
//int mm_flags = 0x10013db;//x86はこの値
//
//    if (bpc <= 8 && mm_flags & AV_CPU_FLAG_MMX) {
//        ctx->emulated_edge_mc = emulated_edge_mc;
//    }
//    if (mm_flags & AV_CPU_FLAG_3DNOW) {
//        ctx->prefetch = ff_prefetch_3dnow;
//    }
//    if (mm_flags & AV_CPU_FLAG_MMXEXT) {
//        ctx->prefetch = ff_prefetch_mmxext;
//    }
//    if (bpc <= 8 && mm_flags & AV_CPU_FLAG_SSE) {
//        ctx->emulated_edge_mc = emulated_edge_mc;
//    }
//}
//
//void ff_videodsp_init(VideoDSPContext *ctx, int bpc)
//{
//        ctx->emulated_edge_mc = ff_emulated_edge_mc_8;
//
//    if (ARCH_ARM)
//        ff_videodsp_init_arm(ctx, bpc);
//    if (ARCH_PPC)
//        ff_videodsp_init_ppc(ctx, bpc);
//    if (ARCH_X86)
//        ff_videodsp_init_x86(ctx, bpc);
//}


typedef int16_t DCTBLOCK[64];
/*
  Unlike our decoder where we approximate the FIXes, we need to use exact
ones here or successive P-frames will drift too much with Reference frame coding
*/

/*
 * Perform the inverse DCT on one block of coefficients.
 */

#define MULTIPLY(var,const)  ((var) * (const))

void ff_j_rev_dct(DCTBLOCK data)
{
  int32_t tmp0, tmp1, tmp2, tmp3;
  int32_t tmp10, tmp11, tmp12, tmp13;
  int32_t z1, z2, z3, z4, z5;
  int32_t d0, d1, d2, d3, d4, d5, d6, d7;
  register int16_t *dataptr;
  int rowctr;

  /* Pass 1: process rows. */
  /* Note results are scaled up by sqrt(8) compared to a true IDCT; */
  /* furthermore, we scale the results by 2**PASS1_BITS. */

  dataptr = data;

  for (rowctr = DCTSIZE-1; rowctr >= 0; rowctr--) {
    /* Due to quantization, we will usually find that many of the input
     * coefficients are zero, especially the AC terms.  We can exploit this
     * by short-circuiting the IDCT calculation for any row in which all
     * the AC terms are zero.  In that case each output is equal to the
     * DC coefficient (with scale factor as needed).
     * With typical images and quantization tables, half or more of the
     * row DCT calculations can be simplified this way.
     */

    register int *idataptr = (int*)dataptr;

    /* WARNING: we do the same permutation as MMX idct to simplify the
       video core */
    d0 = dataptr[0];
    d2 = dataptr[1];
    d4 = dataptr[2];
    d6 = dataptr[3];
    d1 = dataptr[4];
    d3 = dataptr[5];
    d5 = dataptr[6];
    d7 = dataptr[7];

    if ((d1 | d2 | d3 | d4 | d5 | d6 | d7) == 0) {
      /* AC terms all zero */
      if (d0) {
          /* Compute a 32 bit value to assign. */
          int16_t dcval = (int16_t) (d0 << PASS1_BITS);
          register int v = (dcval & 0xffff) | ((dcval << 16) & 0xffff0000);

          idataptr[0] = v;
          idataptr[1] = v;
          idataptr[2] = v;
          idataptr[3] = v;
      }

      dataptr += DCTSIZE;       /* advance pointer to next row */
      continue;
    }

    /* Even part: reverse the even part of the forward DCT. */
    /* The rotator is sqrt(2)*c(-6). */
{
    if (d6) {
            if (d2) {
                    /* d0 != 0, d2 != 0, d4 != 0, d6 != 0 */
                    z1 = MULTIPLY(d2 + d6, FIX_0_541196100);
                    tmp2 = z1 + MULTIPLY(-d6, FIX_1_847759065);
                    tmp3 = z1 + MULTIPLY(d2, FIX_0_765366865);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            } else {
                    /* d0 != 0, d2 == 0, d4 != 0, d6 != 0 */
                    tmp2 = MULTIPLY(-d6, FIX_1_306562965);
                    tmp3 = MULTIPLY(d6, FIX_0_541196100);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            }
    } else {
            if (d2) {
                    /* d0 != 0, d2 != 0, d4 != 0, d6 == 0 */
                    tmp2 = MULTIPLY(d2, FIX_0_541196100);
                    tmp3 = MULTIPLY(d2, FIX_1_306562965);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            } else {
                    /* d0 != 0, d2 == 0, d4 != 0, d6 == 0 */
                    tmp10 = tmp13 = (d0 + d4) << CONST_BITS;
                    tmp11 = tmp12 = (d0 - d4) << CONST_BITS;
            }
      }

    /* Odd part per figure 8; the matrix is unitary and hence its
     * transpose is its inverse.  i0..i3 are y7,y5,y3,y1 respectively.
     */

    if (d7) {
        if (d5) {
            if (d3) {
                if (d1) {
                    /* d1 != 0, d3 != 0, d5 != 0, d7 != 0 */
                    z1 = d7 + d1;
                    z2 = d5 + d3;
                    z3 = d7 + d3;
                    z4 = d5 + d1;
                    z5 = MULTIPLY(z3 + z4, FIX_1_175875602);

                    tmp0 = MULTIPLY(d7, FIX_0_298631336);
                    tmp1 = MULTIPLY(d5, FIX_2_053119869);
                    tmp2 = MULTIPLY(d3, FIX_3_072711026);
                    tmp3 = MULTIPLY(d1, FIX_1_501321110);
                    z1 = MULTIPLY(-z1, FIX_0_899976223);
                    z2 = MULTIPLY(-z2, FIX_2_562915447);
                    z3 = MULTIPLY(-z3, FIX_1_961570560);
                    z4 = MULTIPLY(-z4, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z1 + z3;
                    tmp1 += z2 + z4;
                    tmp2 += z2 + z3;
                    tmp3 += z1 + z4;
                } else {
                    /* d1 == 0, d3 != 0, d5 != 0, d7 != 0 */
                    z2 = d5 + d3;
                    z3 = d7 + d3;
                    z5 = MULTIPLY(z3 + d5, FIX_1_175875602);

                    tmp0 = MULTIPLY(d7, FIX_0_298631336);
                    tmp1 = MULTIPLY(d5, FIX_2_053119869);
                    tmp2 = MULTIPLY(d3, FIX_3_072711026);
                    z1 = MULTIPLY(-d7, FIX_0_899976223);
                    z2 = MULTIPLY(-z2, FIX_2_562915447);
                    z3 = MULTIPLY(-z3, FIX_1_961570560);
                    z4 = MULTIPLY(-d5, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z1 + z3;
                    tmp1 += z2 + z4;
                    tmp2 += z2 + z3;
                    tmp3 = z1 + z4;
                }
            } else {
                if (d1) {
                    /* d1 != 0, d3 == 0, d5 != 0, d7 != 0 */
                    z1 = d7 + d1;
                    z4 = d5 + d1;
                    z5 = MULTIPLY(d7 + z4, FIX_1_175875602);

                    tmp0 = MULTIPLY(d7, FIX_0_298631336);
                    tmp1 = MULTIPLY(d5, FIX_2_053119869);
                    tmp3 = MULTIPLY(d1, FIX_1_501321110);
                    z1 = MULTIPLY(-z1, FIX_0_899976223);
                    z2 = MULTIPLY(-d5, FIX_2_562915447);
                    z3 = MULTIPLY(-d7, FIX_1_961570560);
                    z4 = MULTIPLY(-z4, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z1 + z3;
                    tmp1 += z2 + z4;
                    tmp2 = z2 + z3;
                    tmp3 += z1 + z4;
                } else {
                    /* d1 == 0, d3 == 0, d5 != 0, d7 != 0 */
                    tmp0 = MULTIPLY(-d7, FIX_0_601344887);
                    z1 = MULTIPLY(-d7, FIX_0_899976223);
                    z3 = MULTIPLY(-d7, FIX_1_961570560);
                    tmp1 = MULTIPLY(-d5, FIX_0_509795579);
                    z2 = MULTIPLY(-d5, FIX_2_562915447);
                    z4 = MULTIPLY(-d5, FIX_0_390180644);
                    z5 = MULTIPLY(d5 + d7, FIX_1_175875602);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z3;
                    tmp1 += z4;
                    tmp2 = z2 + z3;
                    tmp3 = z1 + z4;
                }
            }
        } else {
            if (d3) {
                if (d1) {
                    /* d1 != 0, d3 != 0, d5 == 0, d7 != 0 */
                    z1 = d7 + d1;
                    z3 = d7 + d3;
                    z5 = MULTIPLY(z3 + d1, FIX_1_175875602);

                    tmp0 = MULTIPLY(d7, FIX_0_298631336);
                    tmp2 = MULTIPLY(d3, FIX_3_072711026);
                    tmp3 = MULTIPLY(d1, FIX_1_501321110);
                    z1 = MULTIPLY(-z1, FIX_0_899976223);
                    z2 = MULTIPLY(-d3, FIX_2_562915447);
                    z3 = MULTIPLY(-z3, FIX_1_961570560);
                    z4 = MULTIPLY(-d1, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z1 + z3;
                    tmp1 = z2 + z4;
                    tmp2 += z2 + z3;
                    tmp3 += z1 + z4;
                } else {
                    /* d1 == 0, d3 != 0, d5 == 0, d7 != 0 */
                    z3 = d7 + d3;

                    tmp0 = MULTIPLY(-d7, FIX_0_601344887);
                    z1 = MULTIPLY(-d7, FIX_0_899976223);
                    tmp2 = MULTIPLY(d3, FIX_0_509795579);
                    z2 = MULTIPLY(-d3, FIX_2_562915447);
                    z5 = MULTIPLY(z3, FIX_1_175875602);
                    z3 = MULTIPLY(-z3, FIX_0_785694958);

                    tmp0 += z3;
                    tmp1 = z2 + z5;
                    tmp2 += z3;
                    tmp3 = z1 + z5;
                }
            } else {
                if (d1) {
                    /* d1 != 0, d3 == 0, d5 == 0, d7 != 0 */
                    z1 = d7 + d1;
                    z5 = MULTIPLY(z1, FIX_1_175875602);

                    z1 = MULTIPLY(z1, FIX_0_275899380);
                    z3 = MULTIPLY(-d7, FIX_1_961570560);
                    tmp0 = MULTIPLY(-d7, FIX_1_662939225);
                    z4 = MULTIPLY(-d1, FIX_0_390180644);
                    tmp3 = MULTIPLY(d1, FIX_1_111140466);

                    tmp0 += z1;
                    tmp1 = z4 + z5;
                    tmp2 = z3 + z5;
                    tmp3 += z1;
                } else {
                    /* d1 == 0, d3 == 0, d5 == 0, d7 != 0 */
                    tmp0 = MULTIPLY(-d7, FIX_1_387039845);
                    tmp1 = MULTIPLY(d7, FIX_1_175875602);
                    tmp2 = MULTIPLY(-d7, FIX_0_785694958);
                    tmp3 = MULTIPLY(d7, FIX_0_275899380);
                }
            }
        }
    } else {
        if (d5) {
            if (d3) {
                if (d1) {
                    /* d1 != 0, d3 != 0, d5 != 0, d7 == 0 */
                    z2 = d5 + d3;
                    z4 = d5 + d1;
                    z5 = MULTIPLY(d3 + z4, FIX_1_175875602);

                    tmp1 = MULTIPLY(d5, FIX_2_053119869);
                    tmp2 = MULTIPLY(d3, FIX_3_072711026);
                    tmp3 = MULTIPLY(d1, FIX_1_501321110);
                    z1 = MULTIPLY(-d1, FIX_0_899976223);
                    z2 = MULTIPLY(-z2, FIX_2_562915447);
                    z3 = MULTIPLY(-d3, FIX_1_961570560);
                    z4 = MULTIPLY(-z4, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 = z1 + z3;
                    tmp1 += z2 + z4;
                    tmp2 += z2 + z3;
                    tmp3 += z1 + z4;
                } else {
                    /* d1 == 0, d3 != 0, d5 != 0, d7 == 0 */
                    z2 = d5 + d3;

                    z5 = MULTIPLY(z2, FIX_1_175875602);
                    tmp1 = MULTIPLY(d5, FIX_1_662939225);
                    z4 = MULTIPLY(-d5, FIX_0_390180644);
                    z2 = MULTIPLY(-z2, FIX_1_387039845);
                    tmp2 = MULTIPLY(d3, FIX_1_111140466);
                    z3 = MULTIPLY(-d3, FIX_1_961570560);

                    tmp0 = z3 + z5;
                    tmp1 += z2;
                    tmp2 += z2;
                    tmp3 = z4 + z5;
                }
            } else {
                if (d1) {
                    /* d1 != 0, d3 == 0, d5 != 0, d7 == 0 */
                    z4 = d5 + d1;

                    z5 = MULTIPLY(z4, FIX_1_175875602);
                    z1 = MULTIPLY(-d1, FIX_0_899976223);
                    tmp3 = MULTIPLY(d1, FIX_0_601344887);
                    tmp1 = MULTIPLY(-d5, FIX_0_509795579);
                    z2 = MULTIPLY(-d5, FIX_2_562915447);
                    z4 = MULTIPLY(z4, FIX_0_785694958);

                    tmp0 = z1 + z5;
                    tmp1 += z4;
                    tmp2 = z2 + z5;
                    tmp3 += z4;
                } else {
                    /* d1 == 0, d3 == 0, d5 != 0, d7 == 0 */
                    tmp0 = MULTIPLY(d5, FIX_1_175875602);
                    tmp1 = MULTIPLY(d5, FIX_0_275899380);
                    tmp2 = MULTIPLY(-d5, FIX_1_387039845);
                    tmp3 = MULTIPLY(d5, FIX_0_785694958);
                }
            }
        } else {
            if (d3) {
                if (d1) {
                    /* d1 != 0, d3 != 0, d5 == 0, d7 == 0 */
                    z5 = d1 + d3;
                    tmp3 = MULTIPLY(d1, FIX_0_211164243);
                    tmp2 = MULTIPLY(-d3, FIX_1_451774981);
                    z1 = MULTIPLY(d1, FIX_1_061594337);
                    z2 = MULTIPLY(-d3, FIX_2_172734803);
                    z4 = MULTIPLY(z5, FIX_0_785694958);
                    z5 = MULTIPLY(z5, FIX_1_175875602);

                    tmp0 = z1 - z4;
                    tmp1 = z2 + z4;
                    tmp2 += z5;
                    tmp3 += z5;
                } else {
                    /* d1 == 0, d3 != 0, d5 == 0, d7 == 0 */
                    tmp0 = MULTIPLY(-d3, FIX_0_785694958);
                    tmp1 = MULTIPLY(-d3, FIX_1_387039845);
                    tmp2 = MULTIPLY(-d3, FIX_0_275899380);
                    tmp3 = MULTIPLY(d3, FIX_1_175875602);
                }
            } else {
                if (d1) {
                    /* d1 != 0, d3 == 0, d5 == 0, d7 == 0 */
                    tmp0 = MULTIPLY(d1, FIX_0_275899380);
                    tmp1 = MULTIPLY(d1, FIX_0_785694958);
                    tmp2 = MULTIPLY(d1, FIX_1_175875602);
                    tmp3 = MULTIPLY(d1, FIX_1_387039845);
                } else {
                    /* d1 == 0, d3 == 0, d5 == 0, d7 == 0 */
                    tmp0 = tmp1 = tmp2 = tmp3 = 0;
                }
            }
        }
    }
}
    /* Final output stage: inputs are tmp10..tmp13, tmp0..tmp3 */

    dataptr[0] = (int16_t) DESCALE(tmp10 + tmp3, CONST_BITS-PASS1_BITS);
    dataptr[7] = (int16_t) DESCALE(tmp10 - tmp3, CONST_BITS-PASS1_BITS);
    dataptr[1] = (int16_t) DESCALE(tmp11 + tmp2, CONST_BITS-PASS1_BITS);
    dataptr[6] = (int16_t) DESCALE(tmp11 - tmp2, CONST_BITS-PASS1_BITS);
    dataptr[2] = (int16_t) DESCALE(tmp12 + tmp1, CONST_BITS-PASS1_BITS);
    dataptr[5] = (int16_t) DESCALE(tmp12 - tmp1, CONST_BITS-PASS1_BITS);
    dataptr[3] = (int16_t) DESCALE(tmp13 + tmp0, CONST_BITS-PASS1_BITS);
    dataptr[4] = (int16_t) DESCALE(tmp13 - tmp0, CONST_BITS-PASS1_BITS);

    dataptr += DCTSIZE;         /* advance pointer to next row */
  }

  /* Pass 2: process columns. */
  /* Note that we must descale the results by a factor of 8 == 2**3, */
  /* and also undo the PASS1_BITS scaling. */

  dataptr = data;
  for (rowctr = DCTSIZE-1; rowctr >= 0; rowctr--) {
    /* Columns of zeroes can be exploited in the same way as we did with rows.
     * However, the row calculation has created many nonzero AC terms, so the
     * simplification applies less often (typically 5% to 10% of the time).
     * On machines with very fast multiplication, it's possible that the
     * test takes more time than it's worth.  In that case this section
     * may be commented out.
     */

    d0 = dataptr[DCTSIZE*0];
    d1 = dataptr[DCTSIZE*1];
    d2 = dataptr[DCTSIZE*2];
    d3 = dataptr[DCTSIZE*3];
    d4 = dataptr[DCTSIZE*4];
    d5 = dataptr[DCTSIZE*5];
    d6 = dataptr[DCTSIZE*6];
    d7 = dataptr[DCTSIZE*7];

    /* Even part: reverse the even part of the forward DCT. */
    /* The rotator is sqrt(2)*c(-6). */
    if (d6) {
            if (d2) {
                    /* d0 != 0, d2 != 0, d4 != 0, d6 != 0 */
                    z1 = MULTIPLY(d2 + d6, FIX_0_541196100);
                    tmp2 = z1 + MULTIPLY(-d6, FIX_1_847759065);
                    tmp3 = z1 + MULTIPLY(d2, FIX_0_765366865);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            } else {
                    /* d0 != 0, d2 == 0, d4 != 0, d6 != 0 */
                    tmp2 = MULTIPLY(-d6, FIX_1_306562965);
                    tmp3 = MULTIPLY(d6, FIX_0_541196100);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            }
    } else {
            if (d2) {
                    /* d0 != 0, d2 != 0, d4 != 0, d6 == 0 */
                    tmp2 = MULTIPLY(d2, FIX_0_541196100);
                    tmp3 = MULTIPLY(d2, FIX_1_306562965);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            } else {
                    /* d0 != 0, d2 == 0, d4 != 0, d6 == 0 */
                    tmp10 = tmp13 = (d0 + d4) << CONST_BITS;
                    tmp11 = tmp12 = (d0 - d4) << CONST_BITS;
            }
    }

    /* Odd part per figure 8; the matrix is unitary and hence its
     * transpose is its inverse.  i0..i3 are y7,y5,y3,y1 respectively.
     */
    if (d7) {
        if (d5) {
            if (d3) {
                if (d1) {
                    /* d1 != 0, d3 != 0, d5 != 0, d7 != 0 */
                    z1 = d7 + d1;
                    z2 = d5 + d3;
                    z3 = d7 + d3;
                    z4 = d5 + d1;
                    z5 = MULTIPLY(z3 + z4, FIX_1_175875602);

                    tmp0 = MULTIPLY(d7, FIX_0_298631336);
                    tmp1 = MULTIPLY(d5, FIX_2_053119869);
                    tmp2 = MULTIPLY(d3, FIX_3_072711026);
                    tmp3 = MULTIPLY(d1, FIX_1_501321110);
                    z1 = MULTIPLY(-z1, FIX_0_899976223);
                    z2 = MULTIPLY(-z2, FIX_2_562915447);
                    z3 = MULTIPLY(-z3, FIX_1_961570560);
                    z4 = MULTIPLY(-z4, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z1 + z3;
                    tmp1 += z2 + z4;
                    tmp2 += z2 + z3;
                    tmp3 += z1 + z4;
                } else {
                    /* d1 == 0, d3 != 0, d5 != 0, d7 != 0 */
                    z2 = d5 + d3;
                    z3 = d7 + d3;
                    z5 = MULTIPLY(z3 + d5, FIX_1_175875602);

                    tmp0 = MULTIPLY(d7, FIX_0_298631336);
                    tmp1 = MULTIPLY(d5, FIX_2_053119869);
                    tmp2 = MULTIPLY(d3, FIX_3_072711026);
                    z1 = MULTIPLY(-d7, FIX_0_899976223);
                    z2 = MULTIPLY(-z2, FIX_2_562915447);
                    z3 = MULTIPLY(-z3, FIX_1_961570560);
                    z4 = MULTIPLY(-d5, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z1 + z3;
                    tmp1 += z2 + z4;
                    tmp2 += z2 + z3;
                    tmp3 = z1 + z4;
                }
            } else {
                if (d1) {
                    /* d1 != 0, d3 == 0, d5 != 0, d7 != 0 */
                    z1 = d7 + d1;
                    z3 = d7;
                    z4 = d5 + d1;
                    z5 = MULTIPLY(z3 + z4, FIX_1_175875602);

                    tmp0 = MULTIPLY(d7, FIX_0_298631336);
                    tmp1 = MULTIPLY(d5, FIX_2_053119869);
                    tmp3 = MULTIPLY(d1, FIX_1_501321110);
                    z1 = MULTIPLY(-z1, FIX_0_899976223);
                    z2 = MULTIPLY(-d5, FIX_2_562915447);
                    z3 = MULTIPLY(-d7, FIX_1_961570560);
                    z4 = MULTIPLY(-z4, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z1 + z3;
                    tmp1 += z2 + z4;
                    tmp2 = z2 + z3;
                    tmp3 += z1 + z4;
                } else {
                    /* d1 == 0, d3 == 0, d5 != 0, d7 != 0 */
                    tmp0 = MULTIPLY(-d7, FIX_0_601344887);
                    z1 = MULTIPLY(-d7, FIX_0_899976223);
                    z3 = MULTIPLY(-d7, FIX_1_961570560);
                    tmp1 = MULTIPLY(-d5, FIX_0_509795579);
                    z2 = MULTIPLY(-d5, FIX_2_562915447);
                    z4 = MULTIPLY(-d5, FIX_0_390180644);
                    z5 = MULTIPLY(d5 + d7, FIX_1_175875602);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z3;
                    tmp1 += z4;
                    tmp2 = z2 + z3;
                    tmp3 = z1 + z4;
                }
            }
        } else {
            if (d3) {
                if (d1) {
                    /* d1 != 0, d3 != 0, d5 == 0, d7 != 0 */
                    z1 = d7 + d1;
                    z3 = d7 + d3;
                    z5 = MULTIPLY(z3 + d1, FIX_1_175875602);

                    tmp0 = MULTIPLY(d7, FIX_0_298631336);
                    tmp2 = MULTIPLY(d3, FIX_3_072711026);
                    tmp3 = MULTIPLY(d1, FIX_1_501321110);
                    z1 = MULTIPLY(-z1, FIX_0_899976223);
                    z2 = MULTIPLY(-d3, FIX_2_562915447);
                    z3 = MULTIPLY(-z3, FIX_1_961570560);
                    z4 = MULTIPLY(-d1, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 += z1 + z3;
                    tmp1 = z2 + z4;
                    tmp2 += z2 + z3;
                    tmp3 += z1 + z4;
                } else {
                    /* d1 == 0, d3 != 0, d5 == 0, d7 != 0 */
                    z3 = d7 + d3;

                    tmp0 = MULTIPLY(-d7, FIX_0_601344887);
                    z1 = MULTIPLY(-d7, FIX_0_899976223);
                    tmp2 = MULTIPLY(d3, FIX_0_509795579);
                    z2 = MULTIPLY(-d3, FIX_2_562915447);
                    z5 = MULTIPLY(z3, FIX_1_175875602);
                    z3 = MULTIPLY(-z3, FIX_0_785694958);

                    tmp0 += z3;
                    tmp1 = z2 + z5;
                    tmp2 += z3;
                    tmp3 = z1 + z5;
                }
            } else {
                if (d1) {
                    /* d1 != 0, d3 == 0, d5 == 0, d7 != 0 */
                    z1 = d7 + d1;
                    z5 = MULTIPLY(z1, FIX_1_175875602);

                    z1 = MULTIPLY(z1, FIX_0_275899380);
                    z3 = MULTIPLY(-d7, FIX_1_961570560);
                    tmp0 = MULTIPLY(-d7, FIX_1_662939225);
                    z4 = MULTIPLY(-d1, FIX_0_390180644);
                    tmp3 = MULTIPLY(d1, FIX_1_111140466);

                    tmp0 += z1;
                    tmp1 = z4 + z5;
                    tmp2 = z3 + z5;
                    tmp3 += z1;
                } else {
                    /* d1 == 0, d3 == 0, d5 == 0, d7 != 0 */
                    tmp0 = MULTIPLY(-d7, FIX_1_387039845);
                    tmp1 = MULTIPLY(d7, FIX_1_175875602);
                    tmp2 = MULTIPLY(-d7, FIX_0_785694958);
                    tmp3 = MULTIPLY(d7, FIX_0_275899380);
                }
            }
        }
    } else {
        if (d5) {
            if (d3) {
                if (d1) {
                    /* d1 != 0, d3 != 0, d5 != 0, d7 == 0 */
                    z2 = d5 + d3;
                    z4 = d5 + d1;
                    z5 = MULTIPLY(d3 + z4, FIX_1_175875602);

                    tmp1 = MULTIPLY(d5, FIX_2_053119869);
                    tmp2 = MULTIPLY(d3, FIX_3_072711026);
                    tmp3 = MULTIPLY(d1, FIX_1_501321110);
                    z1 = MULTIPLY(-d1, FIX_0_899976223);
                    z2 = MULTIPLY(-z2, FIX_2_562915447);
                    z3 = MULTIPLY(-d3, FIX_1_961570560);
                    z4 = MULTIPLY(-z4, FIX_0_390180644);

                    z3 += z5;
                    z4 += z5;

                    tmp0 = z1 + z3;
                    tmp1 += z2 + z4;
                    tmp2 += z2 + z3;
                    tmp3 += z1 + z4;
                } else {
                    /* d1 == 0, d3 != 0, d5 != 0, d7 == 0 */
                    z2 = d5 + d3;

                    z5 = MULTIPLY(z2, FIX_1_175875602);
                    tmp1 = MULTIPLY(d5, FIX_1_662939225);
                    z4 = MULTIPLY(-d5, FIX_0_390180644);
                    z2 = MULTIPLY(-z2, FIX_1_387039845);
                    tmp2 = MULTIPLY(d3, FIX_1_111140466);
                    z3 = MULTIPLY(-d3, FIX_1_961570560);

                    tmp0 = z3 + z5;
                    tmp1 += z2;
                    tmp2 += z2;
                    tmp3 = z4 + z5;
                }
            } else {
                if (d1) {
                    /* d1 != 0, d3 == 0, d5 != 0, d7 == 0 */
                    z4 = d5 + d1;

                    z5 = MULTIPLY(z4, FIX_1_175875602);
                    z1 = MULTIPLY(-d1, FIX_0_899976223);
                    tmp3 = MULTIPLY(d1, FIX_0_601344887);
                    tmp1 = MULTIPLY(-d5, FIX_0_509795579);
                    z2 = MULTIPLY(-d5, FIX_2_562915447);
                    z4 = MULTIPLY(z4, FIX_0_785694958);

                    tmp0 = z1 + z5;
                    tmp1 += z4;
                    tmp2 = z2 + z5;
                    tmp3 += z4;
                } else {
                    /* d1 == 0, d3 == 0, d5 != 0, d7 == 0 */
                    tmp0 = MULTIPLY(d5, FIX_1_175875602);
                    tmp1 = MULTIPLY(d5, FIX_0_275899380);
                    tmp2 = MULTIPLY(-d5, FIX_1_387039845);
                    tmp3 = MULTIPLY(d5, FIX_0_785694958);
                }
            }
        } else {
            if (d3) {
                if (d1) {
                    /* d1 != 0, d3 != 0, d5 == 0, d7 == 0 */
                    z5 = d1 + d3;
                    tmp3 = MULTIPLY(d1, FIX_0_211164243);
                    tmp2 = MULTIPLY(-d3, FIX_1_451774981);
                    z1 = MULTIPLY(d1, FIX_1_061594337);
                    z2 = MULTIPLY(-d3, FIX_2_172734803);
                    z4 = MULTIPLY(z5, FIX_0_785694958);
                    z5 = MULTIPLY(z5, FIX_1_175875602);

                    tmp0 = z1 - z4;
                    tmp1 = z2 + z4;
                    tmp2 += z5;
                    tmp3 += z5;
                } else {
                    /* d1 == 0, d3 != 0, d5 == 0, d7 == 0 */
                    tmp0 = MULTIPLY(-d3, FIX_0_785694958);
                    tmp1 = MULTIPLY(-d3, FIX_1_387039845);
                    tmp2 = MULTIPLY(-d3, FIX_0_275899380);
                    tmp3 = MULTIPLY(d3, FIX_1_175875602);
                }
            } else {
                if (d1) {
                    /* d1 != 0, d3 == 0, d5 == 0, d7 == 0 */
                    tmp0 = MULTIPLY(d1, FIX_0_275899380);
                    tmp1 = MULTIPLY(d1, FIX_0_785694958);
                    tmp2 = MULTIPLY(d1, FIX_1_175875602);
                    tmp3 = MULTIPLY(d1, FIX_1_387039845);
                } else {
                    /* d1 == 0, d3 == 0, d5 == 0, d7 == 0 */
                    tmp0 = tmp1 = tmp2 = tmp3 = 0;
                }
            }
        }
    }

    /* Final output stage: inputs are tmp10..tmp13, tmp0..tmp3 */

    dataptr[DCTSIZE*0] = (int16_t) DESCALE(tmp10 + tmp3,
                                           CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSIZE*7] = (int16_t) DESCALE(tmp10 - tmp3,
                                           CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSIZE*1] = (int16_t) DESCALE(tmp11 + tmp2,
                                           CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSIZE*6] = (int16_t) DESCALE(tmp11 - tmp2,
                                           CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSIZE*2] = (int16_t) DESCALE(tmp12 + tmp1,
                                           CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSIZE*5] = (int16_t) DESCALE(tmp12 - tmp1,
                                           CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSIZE*3] = (int16_t) DESCALE(tmp13 + tmp0,
                                           CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSIZE*4] = (int16_t) DESCALE(tmp13 - tmp0,
                                           CONST_BITS+PASS1_BITS+3);

    dataptr++;                  /* advance pointer to next column */
  }
}

#   define av_clip_uint8    av_clip_uint8_c

static void put_pixels_clamped_c(const int16_t *block, uint8_t *pixels,
                                 int line_size)
{
    int i;

    /* read the pixels */
    for(i=0;i<8;i++) {
        pixels[0] = av_clip_uint8(block[0]);
        pixels[1] = av_clip_uint8(block[1]);
        pixels[2] = av_clip_uint8(block[2]);
        pixels[3] = av_clip_uint8(block[3]);
        pixels[4] = av_clip_uint8(block[4]);
        pixels[5] = av_clip_uint8(block[5]);
        pixels[6] = av_clip_uint8(block[6]);
        pixels[7] = av_clip_uint8(block[7]);

        pixels += line_size;
        block += 8;
    }
}


static void put_pixels_clamped2_c(const int16_t *block, uint8_t *pixels,
                                 int line_size)
{
    int i;

    /* read the pixels */
    for(i=0;i<2;i++) {
        pixels[0] = av_clip_uint8(block[0]);
        pixels[1] = av_clip_uint8(block[1]);

        pixels += line_size;
        block += 8;
    }
}


static void put_pixels_clamped4_c(const int16_t *block, uint8_t *pixels,
                                 int line_size)
{
    int i;

    /* read the pixels */
    for(i=0;i<4;i++) {
        pixels[0] = av_clip_uint8(block[0]);
        pixels[1] = av_clip_uint8(block[1]);
        pixels[2] = av_clip_uint8(block[2]);
        pixels[3] = av_clip_uint8(block[3]);

        pixels += line_size;
        block += 8;
    }
}

static void add_pixels_clamped_c(const int16_t *block, uint8_t * pixels,
                                 int line_size)
{
    int i;

    /* read the pixels */
    for(i=0;i<8;i++) {
        pixels[0] = av_clip_uint8(pixels[0] + block[0]);
        pixels[1] = av_clip_uint8(pixels[1] + block[1]);
        pixels[2] = av_clip_uint8(pixels[2] + block[2]);
        pixels[3] = av_clip_uint8(pixels[3] + block[3]);
        pixels[4] = av_clip_uint8(pixels[4] + block[4]);
        pixels[5] = av_clip_uint8(pixels[5] + block[5]);
        pixels[6] = av_clip_uint8(pixels[6] + block[6]);
        pixels[7] = av_clip_uint8(pixels[7] + block[7]);
        pixels += line_size;
        block += 8;
    }
}

static void add_pixels_clamped4_c(const int16_t *block, uint8_t *pixels,
                          int line_size)
{
    int i;

    /* read the pixels */
    for(i=0;i<4;i++) {
        pixels[0] = av_clip_uint8(pixels[0] + block[0]);
        pixels[1] = av_clip_uint8(pixels[1] + block[1]);
        pixels[2] = av_clip_uint8(pixels[2] + block[2]);
        pixels[3] = av_clip_uint8(pixels[3] + block[3]);
        pixels += line_size;
        block += 8;
    }
}

static void add_pixels_clamped2_c(const int16_t *block, uint8_t *pixels,
                          int line_size)
{
    int i;

    /* read the pixels */
    for(i=0;i<2;i++) {
        pixels[0] = av_clip_uint8(pixels[0] + block[0]);
        pixels[1] = av_clip_uint8(pixels[1] + block[1]);
        pixels += line_size;
        block += 8;
    }
}


#define DCTSTRIDE 8

void ff_j_rev_dct4(DCTBLOCK data)
{
  int32_t tmp0, tmp1, tmp2, tmp3;
  int32_t tmp10, tmp11, tmp12, tmp13;
  int32_t z1;
  int32_t d0, d2, d4, d6;
  register int16_t *dataptr;
  int rowctr;

  /* Pass 1: process rows. */
  /* Note results are scaled up by sqrt(8) compared to a true IDCT; */
  /* furthermore, we scale the results by 2**PASS1_BITS. */

  data[0] += 4;

  dataptr = data;

  for (rowctr = DCTSIZE-1; rowctr >= 0; rowctr--) {
    /* Due to quantization, we will usually find that many of the input
     * coefficients are zero, especially the AC terms.  We can exploit this
     * by short-circuiting the IDCT calculation for any row in which all
     * the AC terms are zero.  In that case each output is equal to the
     * DC coefficient (with scale factor as needed).
     * With typical images and quantization tables, half or more of the
     * row DCT calculations can be simplified this way.
     */

    register int *idataptr = (int*)dataptr;

    d0 = dataptr[0];
    d2 = dataptr[1];
    d4 = dataptr[2];
    d6 = dataptr[3];

    if ((d2 | d4 | d6) == 0) {
      /* AC terms all zero */
      if (d0) {
          /* Compute a 32 bit value to assign. */
          int16_t dcval = (int16_t) (d0 << PASS1_BITS);
          register int v = (dcval & 0xffff) | ((dcval << 16) & 0xffff0000);

          idataptr[0] = v;
          idataptr[1] = v;
      }

      dataptr += DCTSTRIDE;     /* advance pointer to next row */
      continue;
    }

    /* Even part: reverse the even part of the forward DCT. */
    /* The rotator is sqrt(2)*c(-6). */
    if (d6) {
            if (d2) {
                    /* d0 != 0, d2 != 0, d4 != 0, d6 != 0 */
                    z1 = MULTIPLY(d2 + d6, FIX_0_541196100);
                    tmp2 = z1 + MULTIPLY(-d6, FIX_1_847759065);
                    tmp3 = z1 + MULTIPLY(d2, FIX_0_765366865);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            } else {
                    /* d0 != 0, d2 == 0, d4 != 0, d6 != 0 */
                    tmp2 = MULTIPLY(-d6, FIX_1_306562965);
                    tmp3 = MULTIPLY(d6, FIX_0_541196100);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            }
    } else {
            if (d2) {
                    /* d0 != 0, d2 != 0, d4 != 0, d6 == 0 */
                    tmp2 = MULTIPLY(d2, FIX_0_541196100);
                    tmp3 = MULTIPLY(d2, FIX_1_306562965);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            } else {
                    /* d0 != 0, d2 == 0, d4 != 0, d6 == 0 */
                    tmp10 = tmp13 = (d0 + d4) << CONST_BITS;
                    tmp11 = tmp12 = (d0 - d4) << CONST_BITS;
            }
      }

    /* Final output stage: inputs are tmp10..tmp13, tmp0..tmp3 */

    dataptr[0] = (int16_t) DESCALE(tmp10, CONST_BITS-PASS1_BITS);
    dataptr[1] = (int16_t) DESCALE(tmp11, CONST_BITS-PASS1_BITS);
    dataptr[2] = (int16_t) DESCALE(tmp12, CONST_BITS-PASS1_BITS);
    dataptr[3] = (int16_t) DESCALE(tmp13, CONST_BITS-PASS1_BITS);

    dataptr += DCTSTRIDE;       /* advance pointer to next row */
  }

  /* Pass 2: process columns. */
  /* Note that we must descale the results by a factor of 8 == 2**3, */
  /* and also undo the PASS1_BITS scaling. */

  dataptr = data;
  for (rowctr = DCTSIZE-1; rowctr >= 0; rowctr--) {
    /* Columns of zeroes can be exploited in the same way as we did with rows.
     * However, the row calculation has created many nonzero AC terms, so the
     * simplification applies less often (typically 5% to 10% of the time).
     * On machines with very fast multiplication, it's possible that the
     * test takes more time than it's worth.  In that case this section
     * may be commented out.
     */

    d0 = dataptr[DCTSTRIDE*0];
    d2 = dataptr[DCTSTRIDE*1];
    d4 = dataptr[DCTSTRIDE*2];
    d6 = dataptr[DCTSTRIDE*3];

    /* Even part: reverse the even part of the forward DCT. */
    /* The rotator is sqrt(2)*c(-6). */
    if (d6) {
            if (d2) {
                    /* d0 != 0, d2 != 0, d4 != 0, d6 != 0 */
                    z1 = MULTIPLY(d2 + d6, FIX_0_541196100);
                    tmp2 = z1 + MULTIPLY(-d6, FIX_1_847759065);
                    tmp3 = z1 + MULTIPLY(d2, FIX_0_765366865);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            } else {
                    /* d0 != 0, d2 == 0, d4 != 0, d6 != 0 */
                    tmp2 = MULTIPLY(-d6, FIX_1_306562965);
                    tmp3 = MULTIPLY(d6, FIX_0_541196100);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            }
    } else {
            if (d2) {
                    /* d0 != 0, d2 != 0, d4 != 0, d6 == 0 */
                    tmp2 = MULTIPLY(d2, FIX_0_541196100);
                    tmp3 = MULTIPLY(d2, FIX_1_306562965);

                    tmp0 = (d0 + d4) << CONST_BITS;
                    tmp1 = (d0 - d4) << CONST_BITS;

                    tmp10 = tmp0 + tmp3;
                    tmp13 = tmp0 - tmp3;
                    tmp11 = tmp1 + tmp2;
                    tmp12 = tmp1 - tmp2;
            } else {
                    /* d0 != 0, d2 == 0, d4 != 0, d6 == 0 */
                    tmp10 = tmp13 = (d0 + d4) << CONST_BITS;
                    tmp11 = tmp12 = (d0 - d4) << CONST_BITS;
            }
    }

    /* Final output stage: inputs are tmp10..tmp13, tmp0..tmp3 */

    dataptr[DCTSTRIDE*0] = tmp10 >> (CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSTRIDE*1] = tmp11 >> (CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSTRIDE*2] = tmp12 >> (CONST_BITS+PASS1_BITS+3);
    dataptr[DCTSTRIDE*3] = tmp13 >> (CONST_BITS+PASS1_BITS+3);

    dataptr++;                  /* advance pointer to next column */
  }
}

void ff_j_rev_dct2(DCTBLOCK data){
  int d00, d01, d10, d11;

  data[0] += 4;
  d00 = data[0+0*DCTSTRIDE] + data[1+0*DCTSTRIDE];
  d01 = data[0+0*DCTSTRIDE] - data[1+0*DCTSTRIDE];
  d10 = data[0+1*DCTSTRIDE] + data[1+1*DCTSTRIDE];
  d11 = data[0+1*DCTSTRIDE] - data[1+1*DCTSTRIDE];

  data[0+0*DCTSTRIDE]= (d00 + d10)>>3;
  data[1+0*DCTSTRIDE]= (d01 + d11)>>3;
  data[0+1*DCTSTRIDE]= (d00 - d10)>>3;
  data[1+1*DCTSTRIDE]= (d01 - d11)>>3;
}


static void ff_jref_idct_put(uint8_t *dest, int line_size, int16_t *block)
{
    ff_j_rev_dct (block);
    put_pixels_clamped_c(block, dest, line_size);
}
static void ff_jref_idct_add(uint8_t *dest, int line_size, int16_t *block)
{
    ff_j_rev_dct (block);
    add_pixels_clamped_c(block, dest, line_size);
}

static void ff_jref_idct4_put(uint8_t *dest, int line_size, int16_t *block)
{
    ff_j_rev_dct4 (block);
    put_pixels_clamped4_c(block, dest, line_size);
}
static void ff_jref_idct4_add(uint8_t *dest, int line_size, int16_t *block)
{
    ff_j_rev_dct4 (block);
    add_pixels_clamped4_c(block, dest, line_size);
}

static void ff_jref_idct2_put(uint8_t *dest, int line_size, int16_t *block)
{
    ff_j_rev_dct2 (block);
    put_pixels_clamped2_c(block, dest, line_size);
}
static void ff_jref_idct2_add(uint8_t *dest, int line_size, int16_t *block)
{
    ff_j_rev_dct2 (block);
    add_pixels_clamped2_c(block, dest, line_size);
}

static void ff_jref_idct1_put(uint8_t *dest, int line_size, int16_t *block)
{
    dest[0] = av_clip_uint8((block[0] + 4)>>3);
}
static void ff_jref_idct1_add(uint8_t *dest, int line_size, int16_t *block)
{
    dest[0] = av_clip_uint8(dest[0] + ((block[0] + 4)>>3));
}



typedef int16_t DCTBLOCK[64];
void ff_j_rev_dct1(DCTBLOCK data){
  data[0] = (data[0] + 4)>>3;
}


void ff_put_pixels8x8_c(uint8_t *dst, uint8_t *src, int stride) {
    FUNCC(put_pixels8)(dst, src, stride, 8);
}
void ff_avg_pixels8x8_c(uint8_t *dst, uint8_t *src, int stride) {
    FUNCC(avg_pixels8)(dst, src, stride, 8);
}
void ff_put_pixels16x16_c(uint8_t *dst, uint8_t *src, int stride) {
    FUNCC(put_pixels16)(dst, src, stride, 16);
}
void ff_avg_pixels16x16_c(uint8_t *dst, uint8_t *src, int stride) {
    FUNCC(avg_pixels16)(dst, src, stride, 16);
}

#define W1 90901
#define W2 85627
#define W3 77062
#define W4 65535
#define W5 51491
#define W6 35468
#define W7 18081

#define MUL(a, b)    ((a) * (b))
#define MAC(a, b, c) ((a) += (b) * (c))

#define ROW_SHIFT 15
#define COL_SHIFT 20
#define DC_SHIFT 1
#   define AV_RN64A(p) AV_RNA(64, p)

static inline void FUNC(idctRowCondDC)(int16_t *row, int extra_shift)
{
    int a0, a1, a2, a3, b0, b1, b2, b3;

#if HAVE_FAST_64BIT
#define ROW0_MASK (0xffffLL << 48 * HAVE_BIGENDIAN)
    if (((((uint64_t *)row)[0] & ~ROW0_MASK) | ((uint64_t *)row)[1]) == 0) {
        uint64_t temp;
        if (DC_SHIFT - extra_shift > 0) {
            temp = (row[0] << (DC_SHIFT - extra_shift)) & 0xffff;
        } else {
            temp = (row[0] >> (extra_shift - DC_SHIFT)) & 0xffff;
        }
        temp += temp << 16;
        temp += temp << 32;
        ((uint64_t *)row)[0] = temp;
        ((uint64_t *)row)[1] = temp;
        return;
    }
#else
    if (!(((uint32_t*)row)[1] |
          ((uint32_t*)row)[2] |
          ((uint32_t*)row)[3] |
          row[1])) {
        uint32_t temp;
        if (DC_SHIFT - extra_shift > 0) {
            temp = (row[0] << (DC_SHIFT - extra_shift)) & 0xffff;
        } else {
            temp = (row[0] >> (extra_shift - DC_SHIFT)) & 0xffff;
        }
        temp += temp << 16;
        ((uint32_t*)row)[0]=((uint32_t*)row)[1] =
            ((uint32_t*)row)[2]=((uint32_t*)row)[3] = temp;
        return;
    }
#endif

    a0 = (W4 * row[0]) + (1 << (ROW_SHIFT - 1));
    a1 = a0;
    a2 = a0;
    a3 = a0;

    a0 += W2 * row[2];
    a1 += W6 * row[2];
    a2 -= W6 * row[2];
    a3 -= W2 * row[2];

    b0 = MUL(W1, row[1]);
    MAC(b0, W3, row[3]);
    b1 = MUL(W3, row[1]);
    MAC(b1, -W7, row[3]);
    b2 = MUL(W5, row[1]);
    MAC(b2, -W1, row[3]);
    b3 = MUL(W7, row[1]);
    MAC(b3, -W5, row[3]);

//    if (AV_RN64A(row + 4)) {//ここが意味わからん、配列の4バイト先を構造体の様に参照している
//        a0 +=   W4*row[4] + W6*row[6];
//        a1 += - W4*row[4] - W2*row[6];
//        a2 += - W4*row[4] + W2*row[6];
//        a3 +=   W4*row[4] - W6*row[6];
//
//        MAC(b0,  W5, row[5]);
//        MAC(b0,  W7, row[7]);
//
//        MAC(b1, -W1, row[5]);
//        MAC(b1, -W5, row[7]);
//
//        MAC(b2,  W7, row[5]);
//        MAC(b2,  W3, row[7]);
//
//        MAC(b3,  W3, row[5]);
//        MAC(b3, -W1, row[7]);
//    }

    row[0] = (a0 + b0) >> (ROW_SHIFT + extra_shift);
    row[7] = (a0 - b0) >> (ROW_SHIFT + extra_shift);
    row[1] = (a1 + b1) >> (ROW_SHIFT + extra_shift);
    row[6] = (a1 - b1) >> (ROW_SHIFT + extra_shift);
    row[2] = (a2 + b2) >> (ROW_SHIFT + extra_shift);
    row[5] = (a2 - b2) >> (ROW_SHIFT + extra_shift);
    row[3] = (a3 + b3) >> (ROW_SHIFT + extra_shift);
    row[4] = (a3 - b3) >> (ROW_SHIFT + extra_shift);
}

#define IDCT_COLS do {                                  \
        a0 = W4 * (col[8*0] + ((1<<(COL_SHIFT-1))/W4)); \
        a1 = a0;                                        \
        a2 = a0;                                        \
        a3 = a0;                                        \
                                                        \
        a0 +=  W2*col[8*2];                             \
        a1 +=  W6*col[8*2];                             \
        a2 += -W6*col[8*2];                             \
        a3 += -W2*col[8*2];                             \
                                                        \
        b0 = MUL(W1, col[8*1]);                         \
        b1 = MUL(W3, col[8*1]);                         \
        b2 = MUL(W5, col[8*1]);                         \
        b3 = MUL(W7, col[8*1]);                         \
                                                        \
        MAC(b0,  W3, col[8*3]);                         \
        MAC(b1, -W7, col[8*3]);                         \
        MAC(b2, -W1, col[8*3]);                         \
        MAC(b3, -W5, col[8*3]);                         \
                                                        \
        if (col[8*4]) {                                 \
            a0 +=  W4*col[8*4];                         \
            a1 += -W4*col[8*4];                         \
            a2 += -W4*col[8*4];                         \
            a3 +=  W4*col[8*4];                         \
        }                                               \
                                                        \
        if (col[8*5]) {                                 \
            MAC(b0,  W5, col[8*5]);                     \
            MAC(b1, -W1, col[8*5]);                     \
            MAC(b2,  W7, col[8*5]);                     \
            MAC(b3,  W3, col[8*5]);                     \
        }                                               \
                                                        \
        if (col[8*6]) {                                 \
            a0 +=  W6*col[8*6];                         \
            a1 += -W2*col[8*6];                         \
            a2 +=  W2*col[8*6];                         \
            a3 += -W6*col[8*6];                         \
        }                                               \
                                                        \
        if (col[8*7]) {                                 \
            MAC(b0,  W7, col[8*7]);                     \
            MAC(b1, -W5, col[8*7]);                     \
            MAC(b2,  W3, col[8*7]);                     \
            MAC(b3, -W1, col[8*7]);                     \
        }                                               \
    } while (0)


static inline void FUNC(idctSparseColPut)(pixel *dest, int line_size,
                                          int16_t *col)
{
    int a0, a1, a2, a3, b0, b1, b2, b3;

    IDCT_COLS;

    dest[0] = av_clip_pixel((a0 + b0) >> COL_SHIFT);
    dest += line_size;
    dest[0] = av_clip_pixel((a1 + b1) >> COL_SHIFT);
    dest += line_size;
    dest[0] = av_clip_pixel((a2 + b2) >> COL_SHIFT);
    dest += line_size;
    dest[0] = av_clip_pixel((a3 + b3) >> COL_SHIFT);
    dest += line_size;
    dest[0] = av_clip_pixel((a3 - b3) >> COL_SHIFT);
    dest += line_size;
    dest[0] = av_clip_pixel((a2 - b2) >> COL_SHIFT);
    dest += line_size;
    dest[0] = av_clip_pixel((a1 - b1) >> COL_SHIFT);
    dest += line_size;
    dest[0] = av_clip_pixel((a0 - b0) >> COL_SHIFT);
}


static inline void FUNC(idctSparseColAdd)(pixel *dest, int line_size,
                                          int16_t *col)
{
    int a0, a1, a2, a3, b0, b1, b2, b3;

    IDCT_COLS;

    dest[0] = av_clip_pixel(dest[0] + ((a0 + b0) >> COL_SHIFT));
    dest += line_size;
    dest[0] = av_clip_pixel(dest[0] + ((a1 + b1) >> COL_SHIFT));
    dest += line_size;
    dest[0] = av_clip_pixel(dest[0] + ((a2 + b2) >> COL_SHIFT));
    dest += line_size;
    dest[0] = av_clip_pixel(dest[0] + ((a3 + b3) >> COL_SHIFT));
    dest += line_size;
    dest[0] = av_clip_pixel(dest[0] + ((a3 - b3) >> COL_SHIFT));
    dest += line_size;
    dest[0] = av_clip_pixel(dest[0] + ((a2 - b2) >> COL_SHIFT));
    dest += line_size;
    dest[0] = av_clip_pixel(dest[0] + ((a1 - b1) >> COL_SHIFT));
    dest += line_size;
    dest[0] = av_clip_pixel(dest[0] + ((a0 - b0) >> COL_SHIFT));
}


static inline void FUNC(idctSparseCol)(int16_t *col)
{
    int a0, a1, a2, a3, b0, b1, b2, b3;

    IDCT_COLS;

    col[0 ] = ((a0 + b0) >> COL_SHIFT);
    col[8 ] = ((a1 + b1) >> COL_SHIFT);
    col[16] = ((a2 + b2) >> COL_SHIFT);
    col[24] = ((a3 + b3) >> COL_SHIFT);
    col[32] = ((a3 - b3) >> COL_SHIFT);
    col[40] = ((a2 - b2) >> COL_SHIFT);
    col[48] = ((a1 - b1) >> COL_SHIFT);
    col[56] = ((a0 - b0) >> COL_SHIFT);
}

void FUNC(ff_simple_idct_put)(uint8_t *dest_, int line_size, int16_t *block)
{
    pixel *dest = (pixel *)dest_;
    int i;

    line_size /= sizeof(pixel);

    for (i = 0; i < 8; i++)
        FUNC(idctRowCondDC)(block + i*8, 0);

    for (i = 0; i < 8; i++)
        FUNC(idctSparseColPut)(dest + i, line_size, block + i);
}

void FUNC(ff_simple_idct_add)(uint8_t *dest_, int line_size, int16_t *block)
{
    pixel *dest = (pixel *)dest_;
    int i;

    line_size /= sizeof(pixel);

    for (i = 0; i < 8; i++)
        FUNC(idctRowCondDC)(block + i*8, 0);

    for (i = 0; i < 8; i++)
        FUNC(idctSparseColAdd)(dest + i, line_size, block + i);
}


#define FLOAT float

static const FLOAT prescale[64]={
B0*B0/8, B0*B1/8, B0*B2/8, B0*B3/8, B0*B4/8, B0*B5/8, B0*B6/8, B0*B7/8,
B1*B0/8, B1*B1/8, B1*B2/8, B1*B3/8, B1*B4/8, B1*B5/8, B1*B6/8, B1*B7/8,
B2*B0/8, B2*B1/8, B2*B2/8, B2*B3/8, B2*B4/8, B2*B5/8, B2*B6/8, B2*B7/8,
B3*B0/8, B3*B1/8, B3*B2/8, B3*B3/8, B3*B4/8, B3*B5/8, B3*B6/8, B3*B7/8,
B4*B0/8, B4*B1/8, B4*B2/8, B4*B3/8, B4*B4/8, B4*B5/8, B4*B6/8, B4*B7/8,
B5*B0/8, B5*B1/8, B5*B2/8, B5*B3/8, B5*B4/8, B5*B5/8, B5*B6/8, B5*B7/8,
B6*B0/8, B6*B1/8, B6*B2/8, B6*B3/8, B6*B4/8, B6*B5/8, B6*B6/8, B6*B7/8,
B7*B0/8, B7*B1/8, B7*B2/8, B7*B3/8, B7*B4/8, B7*B5/8, B7*B6/8, B7*B7/8,
};

void FUNC(ff_simple_idct)(int16_t *block)
{
    int i;

    for (i = 0; i < 8; i++)
        FUNC(idctRowCondDC)(block + i*8, 0);

    for (i = 0; i < 8; i++)
        FUNC(idctSparseCol)(block + i);
}


static inline void p8idct(int16_t data[64], FLOAT temp[64], uint8_t *dest, int stride, int x, int y, int type){
    int i;
//    FLOAT av_unused tmp0;
    FLOAT s04, d04, s17, d17, s26, d26, s53, d53;
    FLOAT os07, os16, os25, os34;
    FLOAT od07, od16, od25, od34;

    for(i=0; i<y*8; i+=y){
        s17= temp[1*x + i] + temp[7*x + i];
        d17= temp[1*x + i] - temp[7*x + i];
        s53= temp[5*x + i] + temp[3*x + i];
        d53= temp[5*x + i] - temp[3*x + i];

        od07=  s17 + s53;
        od25= (s17 - s53)*(2*A4);

#if 0 //these 2 are equivalent
        tmp0= (d17 + d53)*(2*A2);
        od34=  d17*( 2*B6) - tmp0;
        od16=  d53*(-2*B2) + tmp0;
#else
        od34=  d17*(2*(B6-A2)) - d53*(2*A2);
        od16=  d53*(2*(A2-B2)) + d17*(2*A2);
#endif

        od16 -= od07;
        od25 -= od16;
        od34 += od25;

        s26 = temp[2*x + i] + temp[6*x + i];
        d26 = temp[2*x + i] - temp[6*x + i];
        d26*= 2*A4;
        d26-= s26;

        s04= temp[0*x + i] + temp[4*x + i];
        d04= temp[0*x + i] - temp[4*x + i];

        os07= s04 + s26;
        os34= s04 - s26;
        os16= d04 + d26;
        os25= d04 - d26;

        if(type==0){
            temp[0*x + i]= os07 + od07;
            temp[7*x + i]= os07 - od07;
            temp[1*x + i]= os16 + od16;
            temp[6*x + i]= os16 - od16;
            temp[2*x + i]= os25 + od25;
            temp[5*x + i]= os25 - od25;
            temp[3*x + i]= os34 - od34;
            temp[4*x + i]= os34 + od34;
        }else if(type==1){
            data[0*x + i]= lrintf(os07 + od07);
            data[7*x + i]= lrintf(os07 - od07);
            data[1*x + i]= lrintf(os16 + od16);
            data[6*x + i]= lrintf(os16 - od16);
            data[2*x + i]= lrintf(os25 + od25);
            data[5*x + i]= lrintf(os25 - od25);
            data[3*x + i]= lrintf(os34 - od34);
            data[4*x + i]= lrintf(os34 + od34);
        }else if(type==2){
            dest[0*stride + i]= av_clip_uint8(((int)dest[0*stride + i]) + lrintf(os07 + od07));
            dest[7*stride + i]= av_clip_uint8(((int)dest[7*stride + i]) + lrintf(os07 - od07));
            dest[1*stride + i]= av_clip_uint8(((int)dest[1*stride + i]) + lrintf(os16 + od16));
            dest[6*stride + i]= av_clip_uint8(((int)dest[6*stride + i]) + lrintf(os16 - od16));
            dest[2*stride + i]= av_clip_uint8(((int)dest[2*stride + i]) + lrintf(os25 + od25));
            dest[5*stride + i]= av_clip_uint8(((int)dest[5*stride + i]) + lrintf(os25 - od25));
            dest[3*stride + i]= av_clip_uint8(((int)dest[3*stride + i]) + lrintf(os34 - od34));
            dest[4*stride + i]= av_clip_uint8(((int)dest[4*stride + i]) + lrintf(os34 + od34));
        }else{
            dest[0*stride + i]= av_clip_uint8(lrintf(os07 + od07));
            dest[7*stride + i]= av_clip_uint8(lrintf(os07 - od07));
            dest[1*stride + i]= av_clip_uint8(lrintf(os16 + od16));
            dest[6*stride + i]= av_clip_uint8(lrintf(os16 - od16));
            dest[2*stride + i]= av_clip_uint8(lrintf(os25 + od25));
            dest[5*stride + i]= av_clip_uint8(lrintf(os25 - od25));
            dest[3*stride + i]= av_clip_uint8(lrintf(os34 - od34));
            dest[4*stride + i]= av_clip_uint8(lrintf(os34 + od34));
        }
    }
}

void ff_faanidct(int16_t block[64]){
    FLOAT temp[64];
    int i;

    for(i=0; i<64; i++)
        temp[i] = block[i] * prescale[i];

    p8idct(block, temp, NULL, 0, 1, 8, 0);
    p8idct(block, temp, NULL, 0, 8, 1, 1);
}

void ff_faanidct_add(uint8_t *dest, int line_size, int16_t block[64]){
    FLOAT temp[64];
    int i;

    for(i=0; i<64; i++)
        temp[i] = block[i] * prescale[i];

    p8idct(block, temp, NULL,         0, 1, 8, 0);
    p8idct(NULL , temp, dest, line_size, 8, 1, 2);
}

void ff_faanidct_put(uint8_t *dest, int line_size, int16_t block[64]){
    FLOAT temp[64];
    int i;

    for(i=0; i<64; i++)
        temp[i] = block[i] * prescale[i];

    p8idct(block, temp, NULL,         0, 1, 8, 0);
    p8idct(NULL , temp, dest, line_size, 8, 1, 3);
}


static void diff_pixels_c(int16_t *block, const uint8_t *s1,
                          const uint8_t *s2, int stride){
    int i;

    /* read the pixels */
    for(i=0;i<8;i++) {
        block[0] = s1[0] - s2[0];
        block[1] = s1[1] - s2[1];
        block[2] = s1[2] - s2[2];
        block[3] = s1[3] - s2[3];
        block[4] = s1[4] - s2[4];
        block[5] = s1[5] - s2[5];
        block[6] = s1[6] - s2[6];
        block[7] = s1[7] - s2[7];
        s1 += stride;
        s2 += stride;
        block += 8;
    }
}

static void put_signed_pixels_clamped_c(const int16_t *block,
                                        uint8_t *pixels,
                                        int line_size)
{
    int i, j;

    for (i = 0; i < 8; i++) {
        for (j = 0; j < 8; j++) {
            if (*block < -128)
                *pixels = 0;
            else if (*block > 127)
                *pixels = 255;
            else
                *pixels = (uint8_t)(*block + 128);
            block++;
            pixels++;
        }
        pixels += (line_size - 8);
    }
}

static int sum_abs_dctelem_c(int16_t *block)
{
    int sum=0, i;
    for(i=0; i<64; i++)
        sum+= FFABS(block[i]);
    return sum;
}

static void gmc1_c(uint8_t *dst, uint8_t *src, int stride, int h, int x16, int y16, int rounder)
{
    const int A=(16-x16)*(16-y16);
    const int B=(   x16)*(16-y16);
    const int C=(16-x16)*(   y16);
    const int D=(   x16)*(   y16);
    int i;

    for(i=0; i<h; i++)
    {
        dst[0]= (A*src[0] + B*src[1] + C*src[stride+0] + D*src[stride+1] + rounder)>>8;
        dst[1]= (A*src[1] + B*src[2] + C*src[stride+1] + D*src[stride+2] + rounder)>>8;
        dst[2]= (A*src[2] + B*src[3] + C*src[stride+2] + D*src[stride+3] + rounder)>>8;
        dst[3]= (A*src[3] + B*src[4] + C*src[stride+3] + D*src[stride+4] + rounder)>>8;
        dst[4]= (A*src[4] + B*src[5] + C*src[stride+4] + D*src[stride+5] + rounder)>>8;
        dst[5]= (A*src[5] + B*src[6] + C*src[stride+5] + D*src[stride+6] + rounder)>>8;
        dst[6]= (A*src[6] + B*src[7] + C*src[stride+6] + D*src[stride+7] + rounder)>>8;
        dst[7]= (A*src[7] + B*src[8] + C*src[stride+7] + D*src[stride+8] + rounder)>>8;
        dst+= stride;
        src+= stride;
    }
}

void ff_gmc_c(uint8_t *dst, uint8_t *src, int stride, int h, int ox, int oy,
                  int dxx, int dxy, int dyx, int dyy, int shift, int r, int width, int height)
{
    int y, vx, vy;
    const int s= 1<<shift;

    width--;
    height--;

    for(y=0; y<h; y++){
        int x;

        vx= ox;
        vy= oy;
        for(x=0; x<8; x++){ //XXX FIXME optimize
            int src_x, src_y, frac_x, frac_y, index;

            src_x= vx>>16;
            src_y= vy>>16;
            frac_x= src_x&(s-1);
            frac_y= src_y&(s-1);
            src_x>>=shift;
            src_y>>=shift;

            if((unsigned)src_x < width){
                if((unsigned)src_y < height){
                    index= src_x + src_y*stride;
                    dst[y*stride + x]= (  (  src[index         ]*(s-frac_x)
                                           + src[index       +1]*   frac_x )*(s-frac_y)
                                        + (  src[index+stride  ]*(s-frac_x)
                                           + src[index+stride+1]*   frac_x )*   frac_y
                                        + r)>>(shift*2);
                }else{
                    index= src_x + av_clip_c(src_y, 0, height)*stride;
                    dst[y*stride + x]= ( (  src[index         ]*(s-frac_x)
                                          + src[index       +1]*   frac_x )*s
                                        + r)>>(shift*2);
                }
            }else{
                if((unsigned)src_y < height){
                    index= av_clip_c(src_x, 0, width) + src_y*stride;
                    dst[y*stride + x]= (  (  src[index         ]*(s-frac_y)
                                           + src[index+stride  ]*   frac_y )*s
                                        + r)>>(shift*2);
                }else{
                    index= av_clip_c(src_x, 0, width) + av_clip_c(src_y, 0, height)*stride;
                    dst[y*stride + x]=    src[index         ];
                }
            }

            vx+= dxx;
            vy+= dyx;
        }
        ox += dxy;
        oy += dyy;
    }
}

static int pix_sum_c(uint8_t * pix, int line_size)
{
    int s, i, j;

    s = 0;
    for (i = 0; i < 16; i++) {
        for (j = 0; j < 16; j += 8) {
            s += pix[0];
            s += pix[1];
            s += pix[2];
            s += pix[3];
            s += pix[4];
            s += pix[5];
            s += pix[6];
            s += pix[7];
            pix += 8;
        }
        pix += line_size - 16;
    }
//	LOGD("pix_sum_c Called pix:%02x return:%d\n",*pix,s);
    return s;
}

static int pix_norm1_c(uint8_t * pix, int line_size)
{
//	LOGD("pix_norm1_c Called\n");
    int s, i, j;
    uint32_t *sq = ff_squareTbl + 256;

    s = 0;
    for (i = 0; i < 16; i++) {
        for (j = 0; j < 16; j += 8) {
#if 0
            s += sq[pix[0]];
            s += sq[pix[1]];
            s += sq[pix[2]];
            s += sq[pix[3]];
            s += sq[pix[4]];
            s += sq[pix[5]];
            s += sq[pix[6]];
            s += sq[pix[7]];
#else
#if HAVE_FAST_64BIT
            register uint64_t x=*(uint64_t*)pix;
            s += sq[x&0xff];
            s += sq[(x>>8)&0xff];
            s += sq[(x>>16)&0xff];
            s += sq[(x>>24)&0xff];
            s += sq[(x>>32)&0xff];
            s += sq[(x>>40)&0xff];
            s += sq[(x>>48)&0xff];
            s += sq[(x>>56)&0xff];
#else
            register uint32_t x=*(uint32_t*)pix;
            s += sq[x&0xff];
            s += sq[(x>>8)&0xff];
            s += sq[(x>>16)&0xff];
            s += sq[(x>>24)&0xff];
            x=*(uint32_t*)(pix+4);
            s += sq[x&0xff];
            s += sq[(x>>8)&0xff];
            s += sq[(x>>16)&0xff];
            s += sq[(x>>24)&0xff];
#endif
#endif
            pix += 8;
        }
        pix += line_size - 16;
    }
//	LOGD("pix_norm1_c pix:%02x return:%d\n",*pix,s);
    return s;
}

static void fill_block16_c(uint8_t *block, uint8_t value, int line_size, int h)
{
	LOGD("fill_block16_c Called\n");
    int i;

    for (i = 0; i < h; i++) {
        memset(block, value, 16);
        block += line_size;
    }
}

static void fill_block8_c(uint8_t *block, uint8_t value, int line_size, int h)
{
	LOGD("fill_block8_c Called\n");
    int i;

    for (i = 0; i < h; i++) {
        memset(block, value, 8);
        block += line_size;
    }
}

static inline int pix_abs16_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("pix_abs16_c Called\n");
    int s, i;

    s = 0;
    for(i=0;i<h;i++) {
        s += abs(pix1[0] - pix2[0]);
        s += abs(pix1[1] - pix2[1]);
        s += abs(pix1[2] - pix2[2]);
        s += abs(pix1[3] - pix2[3]);
        s += abs(pix1[4] - pix2[4]);
        s += abs(pix1[5] - pix2[5]);
        s += abs(pix1[6] - pix2[6]);
        s += abs(pix1[7] - pix2[7]);
        s += abs(pix1[8] - pix2[8]);
        s += abs(pix1[9] - pix2[9]);
        s += abs(pix1[10] - pix2[10]);
        s += abs(pix1[11] - pix2[11]);
        s += abs(pix1[12] - pix2[12]);
        s += abs(pix1[13] - pix2[13]);
        s += abs(pix1[14] - pix2[14]);
        s += abs(pix1[15] - pix2[15]);
        pix1 += line_size;
        pix2 += line_size;
    }
    return s;
}


static int pix_abs16_x2_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("pix_abs16_x2_c Called\n");
    int s, i;

    s = 0;
    for(i=0;i<h;i++) {
        s += abs(pix1[0] - avg2(pix2[0], pix2[1]));
        s += abs(pix1[1] - avg2(pix2[1], pix2[2]));
        s += abs(pix1[2] - avg2(pix2[2], pix2[3]));
        s += abs(pix1[3] - avg2(pix2[3], pix2[4]));
        s += abs(pix1[4] - avg2(pix2[4], pix2[5]));
        s += abs(pix1[5] - avg2(pix2[5], pix2[6]));
        s += abs(pix1[6] - avg2(pix2[6], pix2[7]));
        s += abs(pix1[7] - avg2(pix2[7], pix2[8]));
        s += abs(pix1[8] - avg2(pix2[8], pix2[9]));
        s += abs(pix1[9] - avg2(pix2[9], pix2[10]));
        s += abs(pix1[10] - avg2(pix2[10], pix2[11]));
        s += abs(pix1[11] - avg2(pix2[11], pix2[12]));
        s += abs(pix1[12] - avg2(pix2[12], pix2[13]));
        s += abs(pix1[13] - avg2(pix2[13], pix2[14]));
        s += abs(pix1[14] - avg2(pix2[14], pix2[15]));
        s += abs(pix1[15] - avg2(pix2[15], pix2[16]));
        pix1 += line_size;
        pix2 += line_size;
    }
    return s;
}

static int pix_abs16_y2_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("pix_abs16_y2_c Called\n");
    int s, i;
    uint8_t *pix3 = pix2 + line_size;

    s = 0;
    for(i=0;i<h;i++) {
        s += abs(pix1[0] - avg2(pix2[0], pix3[0]));
        s += abs(pix1[1] - avg2(pix2[1], pix3[1]));
        s += abs(pix1[2] - avg2(pix2[2], pix3[2]));
        s += abs(pix1[3] - avg2(pix2[3], pix3[3]));
        s += abs(pix1[4] - avg2(pix2[4], pix3[4]));
        s += abs(pix1[5] - avg2(pix2[5], pix3[5]));
        s += abs(pix1[6] - avg2(pix2[6], pix3[6]));
        s += abs(pix1[7] - avg2(pix2[7], pix3[7]));
        s += abs(pix1[8] - avg2(pix2[8], pix3[8]));
        s += abs(pix1[9] - avg2(pix2[9], pix3[9]));
        s += abs(pix1[10] - avg2(pix2[10], pix3[10]));
        s += abs(pix1[11] - avg2(pix2[11], pix3[11]));
        s += abs(pix1[12] - avg2(pix2[12], pix3[12]));
        s += abs(pix1[13] - avg2(pix2[13], pix3[13]));
        s += abs(pix1[14] - avg2(pix2[14], pix3[14]));
        s += abs(pix1[15] - avg2(pix2[15], pix3[15]));
        pix1 += line_size;
        pix2 += line_size;
        pix3 += line_size;
    }
    return s;
}
static int pix_abs16_xy2_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("pix_abs16_xy2_c Called\n");
    int s, i;
    uint8_t *pix3 = pix2 + line_size;

    s = 0;
    for(i=0;i<h;i++) {
        s += abs(pix1[0] - avg4(pix2[0], pix2[1], pix3[0], pix3[1]));
        s += abs(pix1[1] - avg4(pix2[1], pix2[2], pix3[1], pix3[2]));
        s += abs(pix1[2] - avg4(pix2[2], pix2[3], pix3[2], pix3[3]));
        s += abs(pix1[3] - avg4(pix2[3], pix2[4], pix3[3], pix3[4]));
        s += abs(pix1[4] - avg4(pix2[4], pix2[5], pix3[4], pix3[5]));
        s += abs(pix1[5] - avg4(pix2[5], pix2[6], pix3[5], pix3[6]));
        s += abs(pix1[6] - avg4(pix2[6], pix2[7], pix3[6], pix3[7]));
        s += abs(pix1[7] - avg4(pix2[7], pix2[8], pix3[7], pix3[8]));
        s += abs(pix1[8] - avg4(pix2[8], pix2[9], pix3[8], pix3[9]));
        s += abs(pix1[9] - avg4(pix2[9], pix2[10], pix3[9], pix3[10]));
        s += abs(pix1[10] - avg4(pix2[10], pix2[11], pix3[10], pix3[11]));
        s += abs(pix1[11] - avg4(pix2[11], pix2[12], pix3[11], pix3[12]));
        s += abs(pix1[12] - avg4(pix2[12], pix2[13], pix3[12], pix3[13]));
        s += abs(pix1[13] - avg4(pix2[13], pix2[14], pix3[13], pix3[14]));
        s += abs(pix1[14] - avg4(pix2[14], pix2[15], pix3[14], pix3[15]));
        s += abs(pix1[15] - avg4(pix2[15], pix2[16], pix3[15], pix3[16]));
        pix1 += line_size;
        pix2 += line_size;
        pix3 += line_size;
    }
    return s;
}

static inline int pix_abs8_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("pix_abs8_c Called\n");
    int s, i;

    s = 0;
    for(i=0;i<h;i++) {
        s += abs(pix1[0] - pix2[0]);
        s += abs(pix1[1] - pix2[1]);
        s += abs(pix1[2] - pix2[2]);
        s += abs(pix1[3] - pix2[3]);
        s += abs(pix1[4] - pix2[4]);
        s += abs(pix1[5] - pix2[5]);
        s += abs(pix1[6] - pix2[6]);
        s += abs(pix1[7] - pix2[7]);
        pix1 += line_size;
        pix2 += line_size;
    }
    return s;
}

static int pix_abs8_x2_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("pix_abs8_x2_c Called\n");
    int s, i;

    s = 0;
    for(i=0;i<h;i++) {
        s += abs(pix1[0] - avg2(pix2[0], pix2[1]));
        s += abs(pix1[1] - avg2(pix2[1], pix2[2]));
        s += abs(pix1[2] - avg2(pix2[2], pix2[3]));
        s += abs(pix1[3] - avg2(pix2[3], pix2[4]));
        s += abs(pix1[4] - avg2(pix2[4], pix2[5]));
        s += abs(pix1[5] - avg2(pix2[5], pix2[6]));
        s += abs(pix1[6] - avg2(pix2[6], pix2[7]));
        s += abs(pix1[7] - avg2(pix2[7], pix2[8]));
        pix1 += line_size;
        pix2 += line_size;
    }
    return s;
}

static int pix_abs8_y2_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("pix_abs8_y2_c Called\n");
    int s, i;
    uint8_t *pix3 = pix2 + line_size;

    s = 0;
    for(i=0;i<h;i++) {
        s += abs(pix1[0] - avg2(pix2[0], pix3[0]));
        s += abs(pix1[1] - avg2(pix2[1], pix3[1]));
        s += abs(pix1[2] - avg2(pix2[2], pix3[2]));
        s += abs(pix1[3] - avg2(pix2[3], pix3[3]));
        s += abs(pix1[4] - avg2(pix2[4], pix3[4]));
        s += abs(pix1[5] - avg2(pix2[5], pix3[5]));
        s += abs(pix1[6] - avg2(pix2[6], pix3[6]));
        s += abs(pix1[7] - avg2(pix2[7], pix3[7]));
        pix1 += line_size;
        pix2 += line_size;
        pix3 += line_size;
    }
    return s;
}

static int pix_abs8_xy2_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("pix_abs8_xy2_c Called\n");
    int s, i;
    uint8_t *pix3 = pix2 + line_size;

    s = 0;
    for(i=0;i<h;i++) {
        s += abs(pix1[0] - avg4(pix2[0], pix2[1], pix3[0], pix3[1]));
        s += abs(pix1[1] - avg4(pix2[1], pix2[2], pix3[1], pix3[2]));
        s += abs(pix1[2] - avg4(pix2[2], pix2[3], pix3[2], pix3[3]));
        s += abs(pix1[3] - avg4(pix2[3], pix2[4], pix3[3], pix3[4]));
        s += abs(pix1[4] - avg4(pix2[4], pix2[5], pix3[4], pix3[5]));
        s += abs(pix1[5] - avg4(pix2[5], pix2[6], pix3[5], pix3[6]));
        s += abs(pix1[6] - avg4(pix2[6], pix2[7], pix3[6], pix3[7]));
        s += abs(pix1[7] - avg4(pix2[7], pix2[8], pix3[7], pix3[8]));
        pix1 += line_size;
        pix2 += line_size;
        pix3 += line_size;
    }
    return s;
}

static inline void put_tpel_pixels_mc00_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){

	LOGD("put_tpel_pixels_mc00_c Called\n");
	switch(width){
    case 2: put_pixels2_8_c (dst, src, stride, height); break;
    case 4: put_pixels4_8_c (dst, src, stride, height); break;
    case 8: put_pixels8_8_c (dst, src, stride, height); break;
    case 16:put_pixels16_8_c(dst, src, stride, height); break;
    }
}

static inline void avg_tpel_pixels_mc00_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){

	LOGD("avg_tpel_pixels_mc00_c Called\n");
	switch(width){
    case 2: avg_pixels2_8_c (dst, src, stride, height); break;
    case 4: avg_pixels4_8_c (dst, src, stride, height); break;
    case 8: avg_pixels8_8_c (dst, src, stride, height); break;
    case 16:avg_pixels16_8_c(dst, src, stride, height); break;
    }
}

static inline void put_tpel_pixels_mc10_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("put_tpel_pixels_mc10_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (683*(2*src[j] + src[j+1] + 1)) >> 11;
      }
      src += stride;
      dst += stride;
    }
}

static inline void put_tpel_pixels_mc20_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("put_tpel_pixels_mc20_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (683*(src[j] + 2*src[j+1] + 1)) >> 11;
      }
      src += stride;
      dst += stride;
    }
}

static inline void put_tpel_pixels_mc01_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("put_tpel_pixels_mc01_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (683*(2*src[j] + src[j+stride] + 1)) >> 11;
      }
      src += stride;
      dst += stride;
    }
}

static inline void put_tpel_pixels_mc11_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("put_tpel_pixels_mc11_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (2731*(4*src[j] + 3*src[j+1] + 3*src[j+stride] + 2*src[j+stride+1] + 6)) >> 15;
      }
      src += stride;
      dst += stride;
    }
}

static inline void put_tpel_pixels_mc12_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("put_tpel_pixels_mc12_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (2731*(3*src[j] + 2*src[j+1] + 4*src[j+stride] + 3*src[j+stride+1] + 6)) >> 15;
      }
      src += stride;
      dst += stride;
    }
}

static inline void put_tpel_pixels_mc02_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("put_tpel_pixels_mc02_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (683*(src[j] + 2*src[j+stride] + 1)) >> 11;
      }
      src += stride;
      dst += stride;
    }
}

static inline void put_tpel_pixels_mc21_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("put_tpel_pixels_mc21_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (2731*(3*src[j] + 4*src[j+1] + 2*src[j+stride] + 3*src[j+stride+1] + 6)) >> 15;
      }
      src += stride;
      dst += stride;
    }
}

static inline void put_tpel_pixels_mc22_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("put_tpel_pixels_mc22_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (2731*(2*src[j] + 3*src[j+1] + 3*src[j+stride] + 4*src[j+stride+1] + 6)) >> 15;
      }
      src += stride;
      dst += stride;
    }
}


static inline void avg_tpel_pixels_mc10_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("avg_tpel_pixels_mc10_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (dst[j] + ((683*(2*src[j] + src[j+1] + 1)) >> 11) + 1) >> 1;
      }
      src += stride;
      dst += stride;
    }
}

static inline void avg_tpel_pixels_mc20_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("avg_tpel_pixels_mc20_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (dst[j] + ((683*(src[j] + 2*src[j+1] + 1)) >> 11) + 1) >> 1;
      }
      src += stride;
      dst += stride;
    }
}

static inline void avg_tpel_pixels_mc01_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("avg_tpel_pixels_mc01_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (dst[j] + ((683*(2*src[j] + src[j+stride] + 1)) >> 11) + 1) >> 1;
      }
      src += stride;
      dst += stride;
    }
}

static inline void avg_tpel_pixels_mc11_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("avg_tpel_pixels_mc11_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (dst[j] + ((2731*(4*src[j] + 3*src[j+1] + 3*src[j+stride] + 2*src[j+stride+1] + 6)) >> 15) + 1) >> 1;
      }
      src += stride;
      dst += stride;
    }
}

static inline void avg_tpel_pixels_mc12_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("avg_tpel_pixels_mc12_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (dst[j] + ((2731*(3*src[j] + 2*src[j+1] + 4*src[j+stride] + 3*src[j+stride+1] + 6)) >> 15) + 1) >> 1;
      }
      src += stride;
      dst += stride;
    }
}

static inline void avg_tpel_pixels_mc02_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("avg_tpel_pixels_mc02_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (dst[j] + ((683*(src[j] + 2*src[j+stride] + 1)) >> 11) + 1) >> 1;
      }
      src += stride;
      dst += stride;
    }
}

static inline void avg_tpel_pixels_mc21_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("avg_tpel_pixels_mc21_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (dst[j] + ((2731*(3*src[j] + 4*src[j+1] + 2*src[j+stride] + 3*src[j+stride+1] + 6)) >> 15) + 1) >> 1;
      }
      src += stride;
      dst += stride;
    }
}

static inline void avg_tpel_pixels_mc22_c(uint8_t *dst, const uint8_t *src, int stride, int width, int height){
	LOGD("avg_tpel_pixels_mc22_c Called\n");
	int i,j;
    for (i=0; i < height; i++) {
      for (j=0; j < width; j++) {
        dst[j] = (dst[j] + ((2731*(2*src[j] + 3*src[j+1] + 3*src[j+stride] + 4*src[j+stride+1] + 6)) >> 15) + 1) >> 1;
      }
      src += stride;
      dst += stride;
    }
}


#define times4(x) x, x, x, x

const uint8_t ff_cropTbl[256 + 2 * 1024] = {
times4(times4(times4(times4(times4(0x00))))),
0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,
0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x1A,0x1B,0x1C,0x1D,0x1E,0x1F,
0x20,0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,0x29,0x2A,0x2B,0x2C,0x2D,0x2E,0x2F,
0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3A,0x3B,0x3C,0x3D,0x3E,0x3F,
0x40,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4A,0x4B,0x4C,0x4D,0x4E,0x4F,
0x50,0x51,0x52,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5A,0x5B,0x5C,0x5D,0x5E,0x5F,
0x60,0x61,0x62,0x63,0x64,0x65,0x66,0x67,0x68,0x69,0x6A,0x6B,0x6C,0x6D,0x6E,0x6F,
0x70,0x71,0x72,0x73,0x74,0x75,0x76,0x77,0x78,0x79,0x7A,0x7B,0x7C,0x7D,0x7E,0x7F,
0x80,0x81,0x82,0x83,0x84,0x85,0x86,0x87,0x88,0x89,0x8A,0x8B,0x8C,0x8D,0x8E,0x8F,
0x90,0x91,0x92,0x93,0x94,0x95,0x96,0x97,0x98,0x99,0x9A,0x9B,0x9C,0x9D,0x9E,0x9F,
0xA0,0xA1,0xA2,0xA3,0xA4,0xA5,0xA6,0xA7,0xA8,0xA9,0xAA,0xAB,0xAC,0xAD,0xAE,0xAF,
0xB0,0xB1,0xB2,0xB3,0xB4,0xB5,0xB6,0xB7,0xB8,0xB9,0xBA,0xBB,0xBC,0xBD,0xBE,0xBF,
0xC0,0xC1,0xC2,0xC3,0xC4,0xC5,0xC6,0xC7,0xC8,0xC9,0xCA,0xCB,0xCC,0xCD,0xCE,0xCF,
0xD0,0xD1,0xD2,0xD3,0xD4,0xD5,0xD6,0xD7,0xD8,0xD9,0xDA,0xDB,0xDC,0xDD,0xDE,0xDF,
0xE0,0xE1,0xE2,0xE3,0xE4,0xE5,0xE6,0xE7,0xE8,0xE9,0xEA,0xEB,0xEC,0xED,0xEE,0xEF,
0xF0,0xF1,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9,0xFA,0xFB,0xFC,0xFD,0xFE,0xFF,
times4(times4(times4(times4(times4(0xFF)))))};








#define put_qpel8_mc00_c  ff_put_pixels8x8_c
#define avg_qpel8_mc00_c  ff_avg_pixels8x8_c
#define put_qpel16_mc00_c ff_put_pixels16x16_c
#define avg_qpel16_mc00_c ff_avg_pixels16x16_c
#define put_no_rnd_qpel8_mc00_c  ff_put_pixels8x8_c
#define put_no_rnd_qpel16_mc00_c ff_put_pixels16x16_8_c




#define BUTTERFLY2(o1,o2,i1,i2) \
o1= (i1)+(i2);\
o2= (i1)-(i2);

#define BUTTERFLY1(x,y) \
{\
    int a,b;\
    a= x;\
    b= y;\
    x= a+b;\
    y= a-b;\
}

#define BUTTERFLYA(x,y) (FFABS((x)+(y)) + FFABS((x)-(y)))

#define SQ(a) ((a)*(a))

static inline const int mid_pred(int a, int b, int c)
{
#if 0
    int t= (a-b)&((a-b)>>31);
    a-=t;
    b+=t;
    b-= (b-c)&((b-c)>>31);
    b+= (a-b)&((a-b)>>31);

    return b;
#else
    if(a>b){
        if(c>b){
            if(c>a) b=a;
            else    b=c;
        }
    }else{
        if(b>c){
            if(c>a) b=c;
            else    b=a;
        }
    }
    return b;
#endif
}

static void wmv2_mspel8_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride, int h){
    uint8_t *cm = ff_cropTbl + MAX_NEG_CROP;
    int i;

    for(i=0; i<h; i++){
        dst[0]= cm[(9*(src[0] + src[1]) - (src[-1] + src[2]) + 8)>>4];
        dst[1]= cm[(9*(src[1] + src[2]) - (src[ 0] + src[3]) + 8)>>4];
        dst[2]= cm[(9*(src[2] + src[3]) - (src[ 1] + src[4]) + 8)>>4];
        dst[3]= cm[(9*(src[3] + src[4]) - (src[ 2] + src[5]) + 8)>>4];
        dst[4]= cm[(9*(src[4] + src[5]) - (src[ 3] + src[6]) + 8)>>4];
        dst[5]= cm[(9*(src[5] + src[6]) - (src[ 4] + src[7]) + 8)>>4];
        dst[6]= cm[(9*(src[6] + src[7]) - (src[ 5] + src[8]) + 8)>>4];
        dst[7]= cm[(9*(src[7] + src[8]) - (src[ 6] + src[9]) + 8)>>4];
        dst+=dstStride;
        src+=srcStride;
    }
}

static void wmv2_mspel8_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride, int w){
    uint8_t *cm = ff_cropTbl + MAX_NEG_CROP;
    int i;

    for(i=0; i<w; i++){
        const int src_1= src[ -srcStride];
        const int src0 = src[0          ];
        const int src1 = src[  srcStride];
        const int src2 = src[2*srcStride];
        const int src3 = src[3*srcStride];
        const int src4 = src[4*srcStride];
        const int src5 = src[5*srcStride];
        const int src6 = src[6*srcStride];
        const int src7 = src[7*srcStride];
        const int src8 = src[8*srcStride];
        const int src9 = src[9*srcStride];
        dst[0*dstStride]= cm[(9*(src0 + src1) - (src_1 + src2) + 8)>>4];
        dst[1*dstStride]= cm[(9*(src1 + src2) - (src0  + src3) + 8)>>4];
        dst[2*dstStride]= cm[(9*(src2 + src3) - (src1  + src4) + 8)>>4];
        dst[3*dstStride]= cm[(9*(src3 + src4) - (src2  + src5) + 8)>>4];
        dst[4*dstStride]= cm[(9*(src4 + src5) - (src3  + src6) + 8)>>4];
        dst[5*dstStride]= cm[(9*(src5 + src6) - (src4  + src7) + 8)>>4];
        dst[6*dstStride]= cm[(9*(src6 + src7) - (src5  + src8) + 8)>>4];
        dst[7*dstStride]= cm[(9*(src7 + src8) - (src6  + src9) + 8)>>4];
        src++;
        dst++;
    }
}

static void put_mspel8_mc10_c(uint8_t *dst, uint8_t *src, int stride){
    uint8_t half[64];
    wmv2_mspel8_h_lowpass(half, src, 8, stride, 8);
    put_pixels8_l2_8(dst, src, half, stride, stride, 8, 8);
}

static void put_mspel8_mc20_c(uint8_t *dst, uint8_t *src, int stride){
    wmv2_mspel8_h_lowpass(dst, src, stride, stride, 8);
}

static void put_mspel8_mc30_c(uint8_t *dst, uint8_t *src, int stride){
    uint8_t half[64];
    wmv2_mspel8_h_lowpass(half, src, 8, stride, 8);
    put_pixels8_l2_8(dst, src+1, half, stride, stride, 8, 8);
}

static void put_mspel8_mc02_c(uint8_t *dst, uint8_t *src, int stride){
    wmv2_mspel8_v_lowpass(dst, src, stride, stride, 8);
}

static void put_mspel8_mc12_c(uint8_t *dst, uint8_t *src, int stride){
    uint8_t halfH[88];
    uint8_t halfV[64];
    uint8_t halfHV[64];
    wmv2_mspel8_h_lowpass(halfH, src-stride, 8, stride, 11);
    wmv2_mspel8_v_lowpass(halfV, src, 8, stride, 8);
    wmv2_mspel8_v_lowpass(halfHV, halfH+8, 8, 8, 8);
    put_pixels8_l2_8(dst, halfV, halfHV, stride, 8, 8, 8);
}
static void put_mspel8_mc32_c(uint8_t *dst, uint8_t *src, int stride){
    uint8_t halfH[88];
    uint8_t halfV[64];
    uint8_t halfHV[64];
    wmv2_mspel8_h_lowpass(halfH, src-stride, 8, stride, 11);
    wmv2_mspel8_v_lowpass(halfV, src+1, 8, stride, 8);
    wmv2_mspel8_v_lowpass(halfHV, halfH+8, 8, 8, 8);
    put_pixels8_l2_8(dst, halfV, halfHV, stride, 8, 8, 8);
}
static void put_mspel8_mc22_c(uint8_t *dst, uint8_t *src, int stride){
    uint8_t halfH[88];
    wmv2_mspel8_h_lowpass(halfH, src-stride, 8, stride, 11);
    wmv2_mspel8_v_lowpass(dst, halfH+8, stride, 8, 8);
}

static int hadamard8_diff8x8_c(/*MpegEncContext*/ void *s, uint8_t *dst, uint8_t *src, int stride, int h){
    int i;
    int temp[64];
    int sum=0;

//    av_assert2(h==8);

    for(i=0; i<8; i++){
        //FIXME try pointer walks
        BUTTERFLY2(temp[8*i+0], temp[8*i+1], src[stride*i+0]-dst[stride*i+0],src[stride*i+1]-dst[stride*i+1]);
        BUTTERFLY2(temp[8*i+2], temp[8*i+3], src[stride*i+2]-dst[stride*i+2],src[stride*i+3]-dst[stride*i+3]);
        BUTTERFLY2(temp[8*i+4], temp[8*i+5], src[stride*i+4]-dst[stride*i+4],src[stride*i+5]-dst[stride*i+5]);
        BUTTERFLY2(temp[8*i+6], temp[8*i+7], src[stride*i+6]-dst[stride*i+6],src[stride*i+7]-dst[stride*i+7]);

        BUTTERFLY1(temp[8*i+0], temp[8*i+2]);
        BUTTERFLY1(temp[8*i+1], temp[8*i+3]);
        BUTTERFLY1(temp[8*i+4], temp[8*i+6]);
        BUTTERFLY1(temp[8*i+5], temp[8*i+7]);

        BUTTERFLY1(temp[8*i+0], temp[8*i+4]);
        BUTTERFLY1(temp[8*i+1], temp[8*i+5]);
        BUTTERFLY1(temp[8*i+2], temp[8*i+6]);
        BUTTERFLY1(temp[8*i+3], temp[8*i+7]);
    }

    for(i=0; i<8; i++){
        BUTTERFLY1(temp[8*0+i], temp[8*1+i]);
        BUTTERFLY1(temp[8*2+i], temp[8*3+i]);
        BUTTERFLY1(temp[8*4+i], temp[8*5+i]);
        BUTTERFLY1(temp[8*6+i], temp[8*7+i]);

        BUTTERFLY1(temp[8*0+i], temp[8*2+i]);
        BUTTERFLY1(temp[8*1+i], temp[8*3+i]);
        BUTTERFLY1(temp[8*4+i], temp[8*6+i]);
        BUTTERFLY1(temp[8*5+i], temp[8*7+i]);

        sum +=
             BUTTERFLYA(temp[8*0+i], temp[8*4+i])
            +BUTTERFLYA(temp[8*1+i], temp[8*5+i])
            +BUTTERFLYA(temp[8*2+i], temp[8*6+i])
            +BUTTERFLYA(temp[8*3+i], temp[8*7+i]);
    }
    return sum;
}

#define WRAPPER8_16_SQ(name8, name16)\
static int name16(void /*MpegEncContext*/ *s, uint8_t *dst, uint8_t *src, int stride, int h){\
    int score=0;\
    score +=name8(s, dst           , src           , stride, 8);\
    score +=name8(s, dst+8         , src+8         , stride, 8);\
    if(h==16){\
        dst += 8*stride;\
        src += 8*stride;\
        score +=name8(s, dst           , src           , stride, 8);\
        score +=name8(s, dst+8         , src+8         , stride, 8);\
    }\
    return score;\
}

WRAPPER8_16_SQ(hadamard8_diff8x8_c, hadamard8_diff16_c)
WRAPPER8_16_SQ(hadamard8_intra8x8_c, hadamard8_intra16_c)
WRAPPER8_16_SQ(dct_sad8x8_c, dct_sad16_c)

WRAPPER8_16_SQ(dct_max8x8_c, dct_max16_c)
WRAPPER8_16_SQ(quant_psnr8x8_c, quant_psnr16_c)
WRAPPER8_16_SQ(rd8x8_c, rd16_c)
WRAPPER8_16_SQ(bit8x8_c, bit16_c)

static int sse16_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
//	LOGD("sse16_c Called ff_squareTbl\n");
    int s, i;
    uint32_t *sq = ff_squareTbl + 256;

    s = 0;
    for (i = 0; i < h; i++) {
        s += sq[pix1[ 0] - pix2[ 0]];
        s += sq[pix1[ 1] - pix2[ 1]];
        s += sq[pix1[ 2] - pix2[ 2]];
        s += sq[pix1[ 3] - pix2[ 3]];
        s += sq[pix1[ 4] - pix2[ 4]];
        s += sq[pix1[ 5] - pix2[ 5]];
        s += sq[pix1[ 6] - pix2[ 6]];
        s += sq[pix1[ 7] - pix2[ 7]];
        s += sq[pix1[ 8] - pix2[ 8]];
        s += sq[pix1[ 9] - pix2[ 9]];
        s += sq[pix1[10] - pix2[10]];
        s += sq[pix1[11] - pix2[11]];
        s += sq[pix1[12] - pix2[12]];
        s += sq[pix1[13] - pix2[13]];
        s += sq[pix1[14] - pix2[14]];
        s += sq[pix1[15] - pix2[15]];

        pix1 += line_size;
        pix2 += line_size;
    }
    return s;
}

static int sse8_c(void *v, uint8_t * pix1, uint8_t * pix2, int line_size, int h)
{
//	LOGD("sse8_c Called ff_squareTbl\n");
    int s, i;
    uint32_t *sq = ff_squareTbl + 256;

    s = 0;
    for (i = 0; i < h; i++) {
        s += sq[pix1[0] - pix2[0]];
        s += sq[pix1[1] - pix2[1]];
        s += sq[pix1[2] - pix2[2]];
        s += sq[pix1[3] - pix2[3]];
        s += sq[pix1[4] - pix2[4]];
        s += sq[pix1[5] - pix2[5]];
        s += sq[pix1[6] - pix2[6]];
        s += sq[pix1[7] - pix2[7]];
        pix1 += line_size;
        pix2 += line_size;
    }
    return s;
}

static int sse4_c(void *v, uint8_t * pix1, uint8_t * pix2, int line_size, int h)
{
//	LOGD("sse4_c Called ff_squareTbl\n");
    int s, i;
    uint32_t *sq = ff_squareTbl + 256;

    s = 0;
    for (i = 0; i < h; i++) {
        s += sq[pix1[0] - pix2[0]];
        s += sq[pix1[1] - pix2[1]];
        s += sq[pix1[2] - pix2[2]];
        s += sq[pix1[3] - pix2[3]];
        pix1 += line_size;
        pix2 += line_size;
    }
    return s;
}

static int vsad16_c(/*MpegEncContext*/ void *c, uint8_t *s1, uint8_t *s2, int stride, int h){
//    LOGD("vsad16_c Called\n");
	int score=0;
    int x,y;

    for(y=1; y<h; y++){
        for(x=0; x<16; x++){
            score+= FFABS(s1[x  ] - s2[x ] - s1[x  +stride] + s2[x +stride]);
        }
        s1+= stride;
        s2+= stride;
    }

    return score;
}

#define VSAD_INTRA(size) \
static int vsad_intra##size##_c(/*MpegEncContext*/ void *c, uint8_t *s, uint8_t *dummy, int stride, int h){ \
    LOGD("vsad_intra Called\n");\
	int score=0;                                                                                            \
    int x,y;                                                                                                \
                                                                                                            \
    for(y=1; y<h; y++){                                                                                     \
        for(x=0; x<size; x+=4){                                                                             \
            score+= FFABS(s[x  ] - s[x  +stride]) + FFABS(s[x+1] - s[x+1+stride])                           \
                   +FFABS(s[x+2] - s[x+2+stride]) + FFABS(s[x+3] - s[x+3+stride]);                          \
        }                                                                                                   \
        s+= stride;                                                                                         \
    }                                                                                                       \
                                                                                                            \
    return score;                                                                                           \
}
VSAD_INTRA(8)
VSAD_INTRA(16)

static int vsse16_c(/*MpegEncContext*/ void *c, uint8_t *s1, uint8_t *s2, int stride, int h){
//    LOGD( "vsse16_c Called\n");
	int score=0;
    int x,y;

    for(y=1; y<h; y++){
        for(x=0; x<16; x++){
            score+= SQ(s1[x  ] - s2[x ] - s1[x  +stride] + s2[x +stride]);
        }
        s1+= stride;
        s2+= stride;
    }

    return score;
}

#define SQ(a) ((a)*(a))
#define VSSE_INTRA(size) \
static int vsse_intra##size##_c(/*MpegEncContext*/ void *c, uint8_t *s, uint8_t *dummy, int stride, int h){ \
	LOGD( "vsse16_c Called\n");\
	int score=0;                                                                                            \
    int x,y;                                                                                                \
                                                                                                            \
    for(y=1; y<h; y++){                                                                                     \
        for(x=0; x<size; x+=4){                                                                               \
            score+= SQ(s[x  ] - s[x  +stride]) + SQ(s[x+1] - s[x+1+stride])                                 \
                   +SQ(s[x+2] - s[x+2+stride]) + SQ(s[x+3] - s[x+3+stride]);                                \
        }                                                                                                   \
        s+= stride;                                                                                         \
    }                                                                                                       \
                                                                                                            \
    return score;                                                                                           \
}
VSSE_INTRA(8)
VSSE_INTRA(16)

static int nsse16_c(void *v, uint8_t *s1, uint8_t *s2, int stride, int h){
    MpegEncContext *c = v;
    int score1=0;
    int score2=0;
    int x,y;

    for(y=0; y<h; y++){
        for(x=0; x<16; x++){
            score1+= (s1[x  ] - s2[x ])*(s1[x  ] - s2[x ]);
        }
        if(y+1<h){
            for(x=0; x<15; x++){
                score2+= FFABS(  s1[x  ] - s1[x  +stride]
                             - s1[x+1] + s1[x+1+stride])
                        -FFABS(  s2[x  ] - s2[x  +stride]
                             - s2[x+1] + s2[x+1+stride]);
            }
        }
        s1+= stride;
        s2+= stride;
    }

    if(c) return score1 + FFABS(score2)*c->avctx->nsse_weight;
    else  return score1 + FFABS(score2)*8;
}

static int nsse8_c(void *v, uint8_t *s1, uint8_t *s2, int stride, int h){
    MpegEncContext *c = v;
    int score1=0;
    int score2=0;
    int x,y;

    for(y=0; y<h; y++){
        for(x=0; x<8; x++){
            score1+= (s1[x  ] - s2[x ])*(s1[x  ] - s2[x ]);
        }
        if(y+1<h){
            for(x=0; x<7; x++){
                score2+= FFABS(  s1[x  ] - s1[x  +stride]
                             - s1[x+1] + s1[x+1+stride])
                        -FFABS(  s2[x  ] - s2[x  +stride]
                             - s2[x+1] + s2[x+1+stride]);
            }
        }
        s1+= stride;
        s2+= stride;
    }

    if(c) return score1 + FFABS(score2)*c->avctx->nsse_weight;
    else  return score1 + FFABS(score2)*8;
}


static int ssd_int8_vs_int16_c(const int8_t *pix1, const int16_t *pix2,
                               int size){
    int score=0;
    int i;
    for(i=0; i<size; i++)
        score += (pix1[i]-pix2[i])*(pix1[i]-pix2[i]);
    return score;
}

// 0x7f7f7f7f or 0x7f7f7f7f7f7f7f7f or whatever, depending on the cpu's native arithmetic size
#define pb_7f (~0UL/255 * 0x7f)
#define pb_80 (~0UL/255 * 0x80)

static void add_bytes_c(uint8_t *dst, uint8_t *src, int w){
    long i;
    for(i=0; i<=w-sizeof(long); i+=sizeof(long)){
        long a = *(long*)(src+i);
        long b = *(long*)(dst+i);
        *(long*)(dst+i) = ((a&pb_7f) + (b&pb_7f)) ^ ((a^b)&pb_80);
    }
    for(; i<w; i++)
        dst[i+0] += src[i+0];
}

static void diff_bytes_c(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int w){
    long i;
#if !HAVE_FAST_UNALIGNED
    if((long)src2 & (sizeof(long)-1)){
        for(i=0; i+7<w; i+=8){
            dst[i+0] = src1[i+0]-src2[i+0];
            dst[i+1] = src1[i+1]-src2[i+1];
            dst[i+2] = src1[i+2]-src2[i+2];
            dst[i+3] = src1[i+3]-src2[i+3];
            dst[i+4] = src1[i+4]-src2[i+4];
            dst[i+5] = src1[i+5]-src2[i+5];
            dst[i+6] = src1[i+6]-src2[i+6];
            dst[i+7] = src1[i+7]-src2[i+7];
        }
    }else
#endif
    for(i=0; i<=w-sizeof(long); i+=sizeof(long)){
        long a = *(long*)(src1+i);
        long b = *(long*)(src2+i);
        *(long*)(dst+i) = ((a|pb_80) - (b&pb_7f)) ^ ((a^b^pb_80)&pb_80);
    }
    for(; i<w; i++)
        dst[i+0] = src1[i+0]-src2[i+0];
}

static void add_hfyu_median_prediction_c(uint8_t *dst, const uint8_t *src1, const uint8_t *diff, int w, int *left, int *left_top){
    int i;
    uint8_t l, lt;

    l= *left;
    lt= *left_top;

    for(i=0; i<w; i++){
        l= mid_pred(l, src1[i], (l + src1[i] - lt)&0xFF) + diff[i];
        lt= src1[i];
        dst[i]= l;
    }

    *left= l;
    *left_top= lt;
}

static void sub_hfyu_median_prediction_c(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int w, int *left, int *left_top){
    int i;
    uint8_t l, lt;

    l= *left;
    lt= *left_top;

    for(i=0; i<w; i++){
        const int pred= mid_pred(l, src1[i], (l + src1[i] - lt)&0xFF);
        lt= src1[i];
        l= src2[i];
        dst[i]= l - pred;
    }

    *left= l;
    *left_top= lt;
}
static int add_hfyu_left_prediction_c(uint8_t *dst, const uint8_t *src, int w, int acc){
    int i;

    for(i=0; i<w-1; i++){
        acc+= src[i];
        dst[i]= acc;
        i++;
        acc+= src[i];
        dst[i]= acc;
    }

    for(; i<w; i++){
        acc+= src[i];
        dst[i]= acc;
    }

    return acc;
}

#if HAVE_BIGENDIAN
#define B 3
#define G 2
#define R 1
#define A 0
#else
#define B 0
#define G 1
#define R 2
#define A 3
#endif
static void add_hfyu_left_prediction_bgr32_c(uint8_t *dst, const uint8_t *src, int w, int *red, int *green, int *blue, int *alpha){
    int i;
    int r,g,b,a;
    r= *red;
    g= *green;
    b= *blue;
    a= *alpha;

    for(i=0; i<w; i++){
        b+= src[4*i+B];
        g+= src[4*i+G];
        r+= src[4*i+R];
        a+= src[4*i+A];

        dst[4*i+B]= b;
        dst[4*i+G]= g;
        dst[4*i+R]= r;
        dst[4*i+A]= a;
    }

    *red= r;
    *green= g;
    *blue= b;
    *alpha= a;
}

static void bswap_buf(uint32_t *dst, const uint32_t *src, int w){
    int i;

    for(i=0; i+8<=w; i+=8){
        dst[i+0]= av_bswap32(src[i+0]);
        dst[i+1]= av_bswap32(src[i+1]);
        dst[i+2]= av_bswap32(src[i+2]);
        dst[i+3]= av_bswap32(src[i+3]);
        dst[i+4]= av_bswap32(src[i+4]);
        dst[i+5]= av_bswap32(src[i+5]);
        dst[i+6]= av_bswap32(src[i+6]);
        dst[i+7]= av_bswap32(src[i+7]);
    }
    for(;i<w; i++){
        dst[i+0]= av_bswap32(src[i+0]);
    }
}

static void bswap16_buf(uint16_t *dst, const uint16_t *src, int len)
{
    while (len--)
        *dst++ = av_bswap16(*src++);
}

static void h263_h_loop_filter_c(uint8_t *src, int stride, int qscale){
    int y;
    const int strength= ff_h263_loop_filter_strength[qscale];

    for(y=0; y<8; y++){
        int d1, d2, ad1;
        int p0= src[y*stride-2];
        int p1= src[y*stride-1];
        int p2= src[y*stride+0];
        int p3= src[y*stride+1];
        int d = (p0 - p3 + 4*(p2 - p1)) / 8;

        if     (d<-2*strength) d1= 0;
        else if(d<-  strength) d1=-2*strength - d;
        else if(d<   strength) d1= d;
        else if(d< 2*strength) d1= 2*strength - d;
        else                   d1= 0;

        p1 += d1;
        p2 -= d1;
        if(p1&256) p1= ~(p1>>31);
        if(p2&256) p2= ~(p2>>31);

        src[y*stride-1] = p1;
        src[y*stride+0] = p2;

        ad1= FFABS(d1)>>1;

        d2= av_clip_c((p0-p3)/4, -ad1, ad1);

        src[y*stride-2] = p0 - d2;
        src[y*stride+1] = p3 + d2;
    }
}


static void h263_v_loop_filter_c(uint8_t *src, int stride, int qscale){
    int x;
    const int strength= ff_h263_loop_filter_strength[qscale];

    for(x=0; x<8; x++){
        int d1, d2, ad1;
        int p0= src[x-2*stride];
        int p1= src[x-1*stride];
        int p2= src[x+0*stride];
        int p3= src[x+1*stride];
        int d = (p0 - p3 + 4*(p2 - p1)) / 8;

        if     (d<-2*strength) d1= 0;
        else if(d<-  strength) d1=-2*strength - d;
        else if(d<   strength) d1= d;
        else if(d< 2*strength) d1= 2*strength - d;
        else                   d1= 0;

        p1 += d1;
        p2 -= d1;
        if(p1&256) p1= ~(p1>>31);
        if(p2&256) p2= ~(p2>>31);

        src[x-1*stride] = p1;
        src[x+0*stride] = p2;

        ad1= FFABS(d1)>>1;

        d2= av_clip_c((p0-p3)/4, -ad1, ad1);

        src[x-2*stride] = p0 - d2;
        src[x+  stride] = p3 + d2;
    }
}

static void h261_loop_filter_c(uint8_t *src, int stride){
    int x,y,xy,yz;
    int temp[64];

    for(x=0; x<8; x++){
        temp[x      ] = 4*src[x           ];
        temp[x + 7*8] = 4*src[x + 7*stride];
    }
    for(y=1; y<7; y++){
        for(x=0; x<8; x++){
            xy = y * stride + x;
            yz = y * 8 + x;
            temp[yz] = src[xy - stride] + 2*src[xy] + src[xy + stride];
        }
    }

    for(y=0; y<8; y++){
        src[  y*stride] = (temp[  y*8] + 2)>>2;
        src[7+y*stride] = (temp[7+y*8] + 2)>>2;
        for(x=1; x<7; x++){
            xy = y * stride + x;
            yz = y * 8 + x;
            src[xy] = (temp[yz-1] + 2*temp[yz] + temp[yz+1] + 8)>>4;
        }
    }
}

static int try_8x8basis_c(int16_t rem[64], int16_t weight[64], int16_t basis[64], int scale){
    int i;
    unsigned int sum=0;

    for(i=0; i<8*8; i++){
        int b= rem[i] + ((basis[i]*scale + (1<<(BASIS_SHIFT - RECON_SHIFT-1)))>>(BASIS_SHIFT - RECON_SHIFT));
        int w= weight[i];
        b>>= RECON_SHIFT;
//        av_assert2(-512<b && b<512);

        sum += (w*b)*(w*b)>>4;
    }
    return sum>>2;
}

static void add_8x8basis_c(int16_t rem[64], int16_t basis[64], int scale){
    int i;

    for(i=0; i<8*8; i++){
        rem[i] += (basis[i]*scale + (1<<(BASIS_SHIFT - RECON_SHIFT-1)))>>(BASIS_SHIFT - RECON_SHIFT);
    }
}

static inline uint32_t clipf_c_one(uint32_t a, uint32_t mini,
                   uint32_t maxi, uint32_t maxisign)
{

    if(a > mini) return mini;
    else if((a^(1U<<31)) > maxisign) return maxi;
    else return a;
}

/**
 * Clip a float value into the amin-amax range.
 * @param a value to clip
 * @param amin minimum value of the clip range
 * @param amax maximum value of the clip range
 * @return clipped value
 */
static inline const float av_clipf_c(float a, float amin, float amax)
{
#if defined(HAVE_AV_CONFIG_H) && defined(ASSERT_LEVEL) && ASSERT_LEVEL >= 2
    if (amin > amax) abort();
#endif
    if      (a < amin) return amin;
    else if (a > amax) return amax;
    else               return a;
}

void vector_clipf_c_opposite_sign(float *dst, const float *src, float *min, float *max, int len){
    int i;
    uint32_t mini = *(uint32_t*)min;
    uint32_t maxi = *(uint32_t*)max;
    uint32_t maxisign = maxi ^ (1U<<31);
    uint32_t *dsti = (uint32_t*)dst;
    const uint32_t *srci = (const uint32_t*)src;
    for(i=0; i<len; i+=8) {
        dsti[i + 0] = clipf_c_one(srci[i + 0], mini, maxi, maxisign);
        dsti[i + 1] = clipf_c_one(srci[i + 1], mini, maxi, maxisign);
        dsti[i + 2] = clipf_c_one(srci[i + 2], mini, maxi, maxisign);
        dsti[i + 3] = clipf_c_one(srci[i + 3], mini, maxi, maxisign);
        dsti[i + 4] = clipf_c_one(srci[i + 4], mini, maxi, maxisign);
        dsti[i + 5] = clipf_c_one(srci[i + 5], mini, maxi, maxisign);
        dsti[i + 6] = clipf_c_one(srci[i + 6], mini, maxi, maxisign);
        dsti[i + 7] = clipf_c_one(srci[i + 7], mini, maxi, maxisign);
    }
}


static void vector_clipf_c(float *dst, const float *src, float min, float max, int len){
    int i;
    if(min < 0 && max > 0) {
        vector_clipf_c_opposite_sign(dst, src, &min, &max, len);
    } else {
        for(i=0; i < len; i+=8) {
            dst[i    ] = av_clipf_c(src[i    ], min, max);
            dst[i + 1] = av_clipf_c(src[i + 1], min, max);
            dst[i + 2] = av_clipf_c(src[i + 2], min, max);
            dst[i + 3] = av_clipf_c(src[i + 3], min, max);
            dst[i + 4] = av_clipf_c(src[i + 4], min, max);
            dst[i + 5] = av_clipf_c(src[i + 5], min, max);
            dst[i + 6] = av_clipf_c(src[i + 6], min, max);
            dst[i + 7] = av_clipf_c(src[i + 7], min, max);
        }
    }
}

static int32_t scalarproduct_int16_c(const int16_t * v1, const int16_t * v2, int order)
{
    int res = 0;

    while (order--)
        res += *v1++ * *v2++;

    return res;
}

static int32_t scalarproduct_and_madd_int16_c(int16_t *v1, const int16_t *v2, const int16_t *v3, int order, int mul)
{
    int res = 0;
    while (order--) {
        res   += *v1 * *v2++;
        *v1++ += mul * *v3++;
    }
    return res;
}

static void apply_window_int16_c(int16_t *output, const int16_t *input,
                                 const int16_t *window, unsigned int len)
{
    int i;
    int len2 = len >> 1;

    for (i = 0; i < len2; i++) {
        int16_t w       = window[i];
        output[i]       = (MUL16(input[i],       w) + (1 << 14)) >> 15;
        output[len-i-1] = (MUL16(input[len-i-1], w) + (1 << 14)) >> 15;
    }
}

static void vector_clip_int32_c(int32_t *dst, const int32_t *src, int32_t min,
                                int32_t max, unsigned int len)
{
    do {
        *dst++ = av_clip_c(*src++, min, max);
        *dst++ = av_clip_c(*src++, min, max);
        *dst++ = av_clip_c(*src++, min, max);
        *dst++ = av_clip_c(*src++, min, max);
        *dst++ = av_clip_c(*src++, min, max);
        *dst++ = av_clip_c(*src++, min, max);
        *dst++ = av_clip_c(*src++, min, max);
        *dst++ = av_clip_c(*src++, min, max);
        len -= 8;
    } while (len > 0);
}

/* 2x2 -> 1x1 */
void ff_shrink22(uint8_t *dst, int dst_wrap,
                     const uint8_t *src, int src_wrap,
                     int width, int height)
{
    int w;
    const uint8_t *s1, *s2;
    uint8_t *d;

    for(;height > 0; height--) {
        s1 = src;
        s2 = s1 + src_wrap;
        d = dst;
        for(w = width;w >= 4; w-=4) {
            d[0] = (s1[0] + s1[1] + s2[0] + s2[1] + 2) >> 2;
            d[1] = (s1[2] + s1[3] + s2[2] + s2[3] + 2) >> 2;
            d[2] = (s1[4] + s1[5] + s2[4] + s2[5] + 2) >> 2;
            d[3] = (s1[6] + s1[7] + s2[6] + s2[7] + 2) >> 2;
            s1 += 8;
            s2 += 8;
            d += 4;
        }
        for(;w > 0; w--) {
            d[0] = (s1[0] + s1[1] + s2[0] + s2[1] + 2) >> 2;
            s1 += 2;
            s2 += 2;
            d++;
        }
        src += 2 * src_wrap;
        dst += dst_wrap;
    }
}

/* 4x4 -> 1x1 */
void ff_shrink44(uint8_t *dst, int dst_wrap,
                     const uint8_t *src, int src_wrap,
                     int width, int height)
{
    int w;
    const uint8_t *s1, *s2, *s3, *s4;
    uint8_t *d;

    for(;height > 0; height--) {
        s1 = src;
        s2 = s1 + src_wrap;
        s3 = s2 + src_wrap;
        s4 = s3 + src_wrap;
        d = dst;
        for(w = width;w > 0; w--) {
            d[0] = (s1[0] + s1[1] + s1[2] + s1[3] +
                    s2[0] + s2[1] + s2[2] + s2[3] +
                    s3[0] + s3[1] + s3[2] + s3[3] +
                    s4[0] + s4[1] + s4[2] + s4[3] + 8) >> 4;
            s1 += 4;
            s2 += 4;
            s3 += 4;
            s4 += 4;
            d++;
        }
        src += 4 * src_wrap;
        dst += dst_wrap;
    }
}

/* 8x8 -> 1x1 */
void ff_shrink88(uint8_t *dst, int dst_wrap,
                     const uint8_t *src, int src_wrap,
                     int width, int height)
{
    int w, i;

    for(;height > 0; height--) {
        for(w = width;w > 0; w--) {
            int tmp=0;
            for(i=0; i<8; i++){
                tmp += src[0] + src[1] + src[2] + src[3] + src[4] + src[5] + src[6] + src[7];
                src += src_wrap;
            }
            *(dst++) = (tmp + 32)>>6;
            src += 8 - 8*src_wrap;
        }
        src += 8*src_wrap - 8*width;
        dst += dst_wrap - width;
    }
}

static void add_pixels8_c(uint8_t *pixels,
                          int16_t *block,
                          int line_size)
{
    int i;

    for(i=0;i<8;i++) {
        pixels[0] += block[0];
        pixels[1] += block[1];
        pixels[2] += block[2];
        pixels[3] += block[3];
        pixels[4] += block[4];
        pixels[5] += block[5];
        pixels[6] += block[6];
        pixels[7] += block[7];
        pixels += line_size;
        block += 8;
    }
}










int hadamard8_intra8x8_c(/*MpegEncContext*/ void *s, uint8_t *src, uint8_t *dummy, int stride, int h){
    int i;
    int temp[64];
    int sum=0;

//    av_assert2(h==8);

    for(i=0; i<8; i++){
        //FIXME try pointer walks
        BUTTERFLY2(temp[8*i+0], temp[8*i+1], src[stride*i+0],src[stride*i+1]);
        BUTTERFLY2(temp[8*i+2], temp[8*i+3], src[stride*i+2],src[stride*i+3]);
        BUTTERFLY2(temp[8*i+4], temp[8*i+5], src[stride*i+4],src[stride*i+5]);
        BUTTERFLY2(temp[8*i+6], temp[8*i+7], src[stride*i+6],src[stride*i+7]);

        BUTTERFLY1(temp[8*i+0], temp[8*i+2]);
        BUTTERFLY1(temp[8*i+1], temp[8*i+3]);
        BUTTERFLY1(temp[8*i+4], temp[8*i+6]);
        BUTTERFLY1(temp[8*i+5], temp[8*i+7]);

        BUTTERFLY1(temp[8*i+0], temp[8*i+4]);
        BUTTERFLY1(temp[8*i+1], temp[8*i+5]);
        BUTTERFLY1(temp[8*i+2], temp[8*i+6]);
        BUTTERFLY1(temp[8*i+3], temp[8*i+7]);
    }

    for(i=0; i<8; i++){
        BUTTERFLY1(temp[8*0+i], temp[8*1+i]);
        BUTTERFLY1(temp[8*2+i], temp[8*3+i]);
        BUTTERFLY1(temp[8*4+i], temp[8*5+i]);
        BUTTERFLY1(temp[8*6+i], temp[8*7+i]);

        BUTTERFLY1(temp[8*0+i], temp[8*2+i]);
        BUTTERFLY1(temp[8*1+i], temp[8*3+i]);
        BUTTERFLY1(temp[8*4+i], temp[8*6+i]);
        BUTTERFLY1(temp[8*5+i], temp[8*7+i]);

        sum +=
             BUTTERFLYA(temp[8*0+i], temp[8*4+i])
            +BUTTERFLYA(temp[8*1+i], temp[8*5+i])
            +BUTTERFLYA(temp[8*2+i], temp[8*6+i])
            +BUTTERFLYA(temp[8*3+i], temp[8*7+i]);
    }

    sum -= FFABS(temp[8*0] + temp[8*4]); // -mean

    return sum;
}


#define QPEL_MC(r, OPNAME, RND, OP) \
static void OPNAME ## mpeg4_qpel8_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride, int h){\
    uint8_t *cm = ff_cropTbl + MAX_NEG_CROP;\
    int i;\
    for(i=0; i<h; i++)\
    {\
        OP(dst[0], (src[0]+src[1])*20 - (src[0]+src[2])*6 + (src[1]+src[3])*3 - (src[2]+src[4]));\
        OP(dst[1], (src[1]+src[2])*20 - (src[0]+src[3])*6 + (src[0]+src[4])*3 - (src[1]+src[5]));\
        OP(dst[2], (src[2]+src[3])*20 - (src[1]+src[4])*6 + (src[0]+src[5])*3 - (src[0]+src[6]));\
        OP(dst[3], (src[3]+src[4])*20 - (src[2]+src[5])*6 + (src[1]+src[6])*3 - (src[0]+src[7]));\
        OP(dst[4], (src[4]+src[5])*20 - (src[3]+src[6])*6 + (src[2]+src[7])*3 - (src[1]+src[8]));\
        OP(dst[5], (src[5]+src[6])*20 - (src[4]+src[7])*6 + (src[3]+src[8])*3 - (src[2]+src[8]));\
        OP(dst[6], (src[6]+src[7])*20 - (src[5]+src[8])*6 + (src[4]+src[8])*3 - (src[3]+src[7]));\
        OP(dst[7], (src[7]+src[8])*20 - (src[6]+src[8])*6 + (src[5]+src[7])*3 - (src[4]+src[6]));\
        dst+=dstStride;\
        src+=srcStride;\
    }\
}\
\
static void OPNAME ## mpeg4_qpel8_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride){\
    const int w=8;\
    uint8_t *cm = ff_cropTbl + MAX_NEG_CROP;\
    int i;\
    for(i=0; i<w; i++)\
    {\
        const int src0= src[0*srcStride];\
        const int src1= src[1*srcStride];\
        const int src2= src[2*srcStride];\
        const int src3= src[3*srcStride];\
        const int src4= src[4*srcStride];\
        const int src5= src[5*srcStride];\
        const int src6= src[6*srcStride];\
        const int src7= src[7*srcStride];\
        const int src8= src[8*srcStride];\
        OP(dst[0*dstStride], (src0+src1)*20 - (src0+src2)*6 + (src1+src3)*3 - (src2+src4));\
        OP(dst[1*dstStride], (src1+src2)*20 - (src0+src3)*6 + (src0+src4)*3 - (src1+src5));\
        OP(dst[2*dstStride], (src2+src3)*20 - (src1+src4)*6 + (src0+src5)*3 - (src0+src6));\
        OP(dst[3*dstStride], (src3+src4)*20 - (src2+src5)*6 + (src1+src6)*3 - (src0+src7));\
        OP(dst[4*dstStride], (src4+src5)*20 - (src3+src6)*6 + (src2+src7)*3 - (src1+src8));\
        OP(dst[5*dstStride], (src5+src6)*20 - (src4+src7)*6 + (src3+src8)*3 - (src2+src8));\
        OP(dst[6*dstStride], (src6+src7)*20 - (src5+src8)*6 + (src4+src8)*3 - (src3+src7));\
        OP(dst[7*dstStride], (src7+src8)*20 - (src6+src8)*6 + (src5+src7)*3 - (src4+src6));\
        dst++;\
        src++;\
    }\
}\
\
static void OPNAME ## mpeg4_qpel16_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride, int h){\
    uint8_t *cm = ff_cropTbl + MAX_NEG_CROP;\
    int i;\
    \
    for(i=0; i<h; i++)\
    {\
        OP(dst[ 0], (src[ 0]+src[ 1])*20 - (src[ 0]+src[ 2])*6 + (src[ 1]+src[ 3])*3 - (src[ 2]+src[ 4]));\
        OP(dst[ 1], (src[ 1]+src[ 2])*20 - (src[ 0]+src[ 3])*6 + (src[ 0]+src[ 4])*3 - (src[ 1]+src[ 5]));\
        OP(dst[ 2], (src[ 2]+src[ 3])*20 - (src[ 1]+src[ 4])*6 + (src[ 0]+src[ 5])*3 - (src[ 0]+src[ 6]));\
        OP(dst[ 3], (src[ 3]+src[ 4])*20 - (src[ 2]+src[ 5])*6 + (src[ 1]+src[ 6])*3 - (src[ 0]+src[ 7]));\
        OP(dst[ 4], (src[ 4]+src[ 5])*20 - (src[ 3]+src[ 6])*6 + (src[ 2]+src[ 7])*3 - (src[ 1]+src[ 8]));\
        OP(dst[ 5], (src[ 5]+src[ 6])*20 - (src[ 4]+src[ 7])*6 + (src[ 3]+src[ 8])*3 - (src[ 2]+src[ 9]));\
        OP(dst[ 6], (src[ 6]+src[ 7])*20 - (src[ 5]+src[ 8])*6 + (src[ 4]+src[ 9])*3 - (src[ 3]+src[10]));\
        OP(dst[ 7], (src[ 7]+src[ 8])*20 - (src[ 6]+src[ 9])*6 + (src[ 5]+src[10])*3 - (src[ 4]+src[11]));\
        OP(dst[ 8], (src[ 8]+src[ 9])*20 - (src[ 7]+src[10])*6 + (src[ 6]+src[11])*3 - (src[ 5]+src[12]));\
        OP(dst[ 9], (src[ 9]+src[10])*20 - (src[ 8]+src[11])*6 + (src[ 7]+src[12])*3 - (src[ 6]+src[13]));\
        OP(dst[10], (src[10]+src[11])*20 - (src[ 9]+src[12])*6 + (src[ 8]+src[13])*3 - (src[ 7]+src[14]));\
        OP(dst[11], (src[11]+src[12])*20 - (src[10]+src[13])*6 + (src[ 9]+src[14])*3 - (src[ 8]+src[15]));\
        OP(dst[12], (src[12]+src[13])*20 - (src[11]+src[14])*6 + (src[10]+src[15])*3 - (src[ 9]+src[16]));\
        OP(dst[13], (src[13]+src[14])*20 - (src[12]+src[15])*6 + (src[11]+src[16])*3 - (src[10]+src[16]));\
        OP(dst[14], (src[14]+src[15])*20 - (src[13]+src[16])*6 + (src[12]+src[16])*3 - (src[11]+src[15]));\
        OP(dst[15], (src[15]+src[16])*20 - (src[14]+src[16])*6 + (src[13]+src[15])*3 - (src[12]+src[14]));\
        dst+=dstStride;\
        src+=srcStride;\
    }\
}\
\
static void OPNAME ## mpeg4_qpel16_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride){\
    uint8_t *cm = ff_cropTbl + MAX_NEG_CROP;\
    int i;\
    const int w=16;\
    for(i=0; i<w; i++)\
    {\
        const int src0= src[0*srcStride];\
        const int src1= src[1*srcStride];\
        const int src2= src[2*srcStride];\
        const int src3= src[3*srcStride];\
        const int src4= src[4*srcStride];\
        const int src5= src[5*srcStride];\
        const int src6= src[6*srcStride];\
        const int src7= src[7*srcStride];\
        const int src8= src[8*srcStride];\
        const int src9= src[9*srcStride];\
        const int src10= src[10*srcStride];\
        const int src11= src[11*srcStride];\
        const int src12= src[12*srcStride];\
        const int src13= src[13*srcStride];\
        const int src14= src[14*srcStride];\
        const int src15= src[15*srcStride];\
        const int src16= src[16*srcStride];\
        OP(dst[ 0*dstStride], (src0 +src1 )*20 - (src0 +src2 )*6 + (src1 +src3 )*3 - (src2 +src4 ));\
        OP(dst[ 1*dstStride], (src1 +src2 )*20 - (src0 +src3 )*6 + (src0 +src4 )*3 - (src1 +src5 ));\
        OP(dst[ 2*dstStride], (src2 +src3 )*20 - (src1 +src4 )*6 + (src0 +src5 )*3 - (src0 +src6 ));\
        OP(dst[ 3*dstStride], (src3 +src4 )*20 - (src2 +src5 )*6 + (src1 +src6 )*3 - (src0 +src7 ));\
        OP(dst[ 4*dstStride], (src4 +src5 )*20 - (src3 +src6 )*6 + (src2 +src7 )*3 - (src1 +src8 ));\
        OP(dst[ 5*dstStride], (src5 +src6 )*20 - (src4 +src7 )*6 + (src3 +src8 )*3 - (src2 +src9 ));\
        OP(dst[ 6*dstStride], (src6 +src7 )*20 - (src5 +src8 )*6 + (src4 +src9 )*3 - (src3 +src10));\
        OP(dst[ 7*dstStride], (src7 +src8 )*20 - (src6 +src9 )*6 + (src5 +src10)*3 - (src4 +src11));\
        OP(dst[ 8*dstStride], (src8 +src9 )*20 - (src7 +src10)*6 + (src6 +src11)*3 - (src5 +src12));\
        OP(dst[ 9*dstStride], (src9 +src10)*20 - (src8 +src11)*6 + (src7 +src12)*3 - (src6 +src13));\
        OP(dst[10*dstStride], (src10+src11)*20 - (src9 +src12)*6 + (src8 +src13)*3 - (src7 +src14));\
        OP(dst[11*dstStride], (src11+src12)*20 - (src10+src13)*6 + (src9 +src14)*3 - (src8 +src15));\
        OP(dst[12*dstStride], (src12+src13)*20 - (src11+src14)*6 + (src10+src15)*3 - (src9 +src16));\
        OP(dst[13*dstStride], (src13+src14)*20 - (src12+src15)*6 + (src11+src16)*3 - (src10+src16));\
        OP(dst[14*dstStride], (src14+src15)*20 - (src13+src16)*6 + (src12+src16)*3 - (src11+src15));\
        OP(dst[15*dstStride], (src15+src16)*20 - (src14+src16)*6 + (src13+src15)*3 - (src12+src14));\
        dst++;\
        src++;\
    }\
}\
\
static void OPNAME ## qpel8_mc10_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t half[64];\
    put ## RND ## mpeg4_qpel8_h_lowpass(half, src, 8, stride, 8);\
    OPNAME ## pixels8_l2_8(dst, src, half, stride, stride, 8, 8);\
}\
\
static void OPNAME ## qpel8_mc20_c(uint8_t *dst, uint8_t *src, int stride){\
    OPNAME ## mpeg4_qpel8_h_lowpass(dst, src, stride, stride, 8);\
}\
\
static void OPNAME ## qpel8_mc30_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t half[64];\
    put ## RND ## mpeg4_qpel8_h_lowpass(half, src, 8, stride, 8);\
    OPNAME ## pixels8_l2_8(dst, src+1, half, stride, stride, 8, 8);\
}\
\
static void OPNAME ## qpel8_mc01_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t half[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(half, full, 8, 16);\
    OPNAME ## pixels8_l2_8(dst, full, half, stride, 16, 8, 8);\
}\
\
static void OPNAME ## qpel8_mc02_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    copy_block9(full, src, 16, stride, 9);\
    OPNAME ## mpeg4_qpel8_v_lowpass(dst, full, stride, 16);\
}\
\
static void OPNAME ## qpel8_mc03_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t half[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(half, full, 8, 16);\
    OPNAME ## pixels8_l2_8(dst, full+16, half, stride, 16, 8, 8);\
}\
void ff_ ## OPNAME ## qpel8_mc11_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfV[64];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfV, full, 8, 16);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l4_8(dst, full, halfH, halfV, halfHV, stride, 16, 8, 8, 8, 8);\
}\
static void OPNAME ## qpel8_mc11_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## pixels8_l2_8(halfH, halfH, full, 8, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l2_8(dst, halfH, halfHV, stride, 8, 8, 8);\
}\
void ff_ ## OPNAME ## qpel8_mc31_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfV[64];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfV, full+1, 8, 16);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l4_8(dst, full+1, halfH, halfV, halfHV, stride, 16, 8, 8, 8, 8);\
}\
static void OPNAME ## qpel8_mc31_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## pixels8_l2_8(halfH, halfH, full+1, 8, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l2_8(dst, halfH, halfHV, stride, 8, 8, 8);\
}\
void ff_ ## OPNAME ## qpel8_mc13_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfV[64];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfV, full, 8, 16);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l4_8(dst, full+16, halfH+8, halfV, halfHV, stride, 16, 8, 8, 8, 8);\
}\
static void OPNAME ## qpel8_mc13_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## pixels8_l2_8(halfH, halfH, full, 8, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l2_8(dst, halfH+8, halfHV, stride, 8, 8, 8);\
}\
void ff_ ## OPNAME ## qpel8_mc33_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfV[64];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full  , 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfV, full+1, 8, 16);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l4_8(dst, full+17, halfH+8, halfV, halfHV, stride, 16, 8, 8, 8, 8);\
}\
static void OPNAME ## qpel8_mc33_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## pixels8_l2_8(halfH, halfH, full+1, 8, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l2_8(dst, halfH+8, halfHV, stride, 8, 8, 8);\
}\
static void OPNAME ## qpel8_mc21_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t halfH[72];\
    uint8_t halfHV[64];\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, src, 8, stride, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l2_8(dst, halfH, halfHV, stride, 8, 8, 8);\
}\
static void OPNAME ## qpel8_mc23_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t halfH[72];\
    uint8_t halfHV[64];\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, src, 8, stride, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l2_8(dst, halfH+8, halfHV, stride, 8, 8, 8);\
}\
void ff_ ## OPNAME ## qpel8_mc12_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfV[64];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfV, full, 8, 16);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l2_8(dst, halfV, halfHV, stride, 8, 8, 8);\
}\
static void OPNAME ## qpel8_mc12_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## pixels8_l2_8(halfH, halfH, full, 8, 8, 16, 9);\
    OPNAME ## mpeg4_qpel8_v_lowpass(dst, halfH, stride, 8);\
}\
void ff_ ## OPNAME ## qpel8_mc32_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    uint8_t halfV[64];\
    uint8_t halfHV[64];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfV, full+1, 8, 16);\
    put ## RND ## mpeg4_qpel8_v_lowpass(halfHV, halfH, 8, 8);\
    OPNAME ## pixels8_l2_8(dst, halfV, halfHV, stride, 8, 8, 8);\
}\
static void OPNAME ## qpel8_mc32_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[16*9];\
    uint8_t halfH[72];\
    copy_block9(full, src, 16, stride, 9);\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, full, 8, 16, 9);\
    put ## RND ## pixels8_l2_8(halfH, halfH, full+1, 8, 8, 16, 9);\
    OPNAME ## mpeg4_qpel8_v_lowpass(dst, halfH, stride, 8);\
}\
static void OPNAME ## qpel8_mc22_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t halfH[72];\
    put ## RND ## mpeg4_qpel8_h_lowpass(halfH, src, 8, stride, 9);\
    OPNAME ## mpeg4_qpel8_v_lowpass(dst, halfH, stride, 8);\
}\
\
static void OPNAME ## qpel16_mc10_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t half[256];\
    put ## RND ## mpeg4_qpel16_h_lowpass(half, src, 16, stride, 16);\
    OPNAME ## pixels16_l2_8(dst, src, half, stride, stride, 16, 16);\
}\
\
static void OPNAME ## qpel16_mc20_c(uint8_t *dst, uint8_t *src, int stride){\
    OPNAME ## mpeg4_qpel16_h_lowpass(dst, src, stride, stride, 16);\
}\
\
static void OPNAME ## qpel16_mc30_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t half[256];\
    put ## RND ## mpeg4_qpel16_h_lowpass(half, src, 16, stride, 16);\
    OPNAME ## pixels16_l2_8(dst, src+1, half, stride, stride, 16, 16);\
}\
\
static void OPNAME ## qpel16_mc01_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t half[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(half, full, 16, 24);\
    OPNAME ## pixels16_l2_8(dst, full, half, stride, 24, 16, 16);\
}\
\
static void OPNAME ## qpel16_mc02_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    copy_block17(full, src, 24, stride, 17);\
    OPNAME ## mpeg4_qpel16_v_lowpass(dst, full, stride, 24);\
}\
\
static void OPNAME ## qpel16_mc03_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t half[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(half, full, 16, 24);\
    OPNAME ## pixels16_l2_8(dst, full+24, half, stride, 24, 16, 16);\
}\
void ff_ ## OPNAME ## qpel16_mc11_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfV[256];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfV, full, 16, 24);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l4_8(dst, full, halfH, halfV, halfHV, stride, 24, 16, 16, 16, 16);\
}\
static void OPNAME ## qpel16_mc11_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## pixels16_l2_8(halfH, halfH, full, 16, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l2_8(dst, halfH, halfHV, stride, 16, 16, 16);\
}\
void ff_ ## OPNAME ## qpel16_mc31_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfV[256];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfV, full+1, 16, 24);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l4_8(dst, full+1, halfH, halfV, halfHV, stride, 24, 16, 16, 16, 16);\
}\
static void OPNAME ## qpel16_mc31_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## pixels16_l2_8(halfH, halfH, full+1, 16, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l2_8(dst, halfH, halfHV, stride, 16, 16, 16);\
}\
void ff_ ## OPNAME ## qpel16_mc13_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfV[256];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfV, full, 16, 24);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l4_8(dst, full+24, halfH+16, halfV, halfHV, stride, 24, 16, 16, 16, 16);\
}\
static void OPNAME ## qpel16_mc13_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## pixels16_l2_8(halfH, halfH, full, 16, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l2_8(dst, halfH+16, halfHV, stride, 16, 16, 16);\
}\
void ff_ ## OPNAME ## qpel16_mc33_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfV[256];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full  , 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfV, full+1, 16, 24);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l4_8(dst, full+25, halfH+16, halfV, halfHV, stride, 24, 16, 16, 16, 16);\
}\
static void OPNAME ## qpel16_mc33_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## pixels16_l2_8(halfH, halfH, full+1, 16, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l2_8(dst, halfH+16, halfHV, stride, 16, 16, 16);\
}\
static void OPNAME ## qpel16_mc21_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t halfH[272];\
    uint8_t halfHV[256];\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, src, 16, stride, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l2_8(dst, halfH, halfHV, stride, 16, 16, 16);\
}\
static void OPNAME ## qpel16_mc23_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t halfH[272];\
    uint8_t halfHV[256];\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, src, 16, stride, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l2_8(dst, halfH+16, halfHV, stride, 16, 16, 16);\
}\
void ff_ ## OPNAME ## qpel16_mc12_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfV[256];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfV, full, 16, 24);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l2_8(dst, halfV, halfHV, stride, 16, 16, 16);\
}\
static void OPNAME ## qpel16_mc12_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## pixels16_l2_8(halfH, halfH, full, 16, 16, 24, 17);\
    OPNAME ## mpeg4_qpel16_v_lowpass(dst, halfH, stride, 16);\
}\
void ff_ ## OPNAME ## qpel16_mc32_old_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    uint8_t halfV[256];\
    uint8_t halfHV[256];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfV, full+1, 16, 24);\
    put ## RND ## mpeg4_qpel16_v_lowpass(halfHV, halfH, 16, 16);\
    OPNAME ## pixels16_l2_8(dst, halfV, halfHV, stride, 16, 16, 16);\
}\
static void OPNAME ## qpel16_mc32_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t full[24*17];\
    uint8_t halfH[272];\
    copy_block17(full, src, 24, stride, 17);\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, full, 16, 24, 17);\
    put ## RND ## pixels16_l2_8(halfH, halfH, full+1, 16, 16, 24, 17);\
    OPNAME ## mpeg4_qpel16_v_lowpass(dst, halfH, stride, 16);\
}\
static void OPNAME ## qpel16_mc22_c(uint8_t *dst, uint8_t *src, int stride){\
    uint8_t halfH[272];\
    put ## RND ## mpeg4_qpel16_h_lowpass(halfH, src, 16, stride, 17);\
    OPNAME ## mpeg4_qpel16_v_lowpass(dst, halfH, stride, 16);\
}

#define op_avg(a, b) a = (((a)+cm[((b) + 16)>>5]+1)>>1)
#define op_avg_no_rnd(a, b) a = (((a)+cm[((b) + 15)>>5])>>1)
#define op_put(a, b) a = cm[((b) + 16)>>5]
#define op_put_no_rnd(a, b) a = cm[((b) + 15)>>5]

QPEL_MC(0, put_       , _       , op_put)
QPEL_MC(1, put_no_rnd_, _no_rnd_, op_put_no_rnd)
QPEL_MC(0, avg_       , _       , op_avg)
//QPEL_MC(1, avg_no_rnd , _       , op_avg)
#undef op_avg
#undef op_avg_no_rnd
#undef op_put
#undef op_put_no_rnd
/*
 * DSP utils
 * Copyright (c) 2000, 2001 Fabrice Bellard
 * Copyright (c) 2002-2004 Michael Niedermayer <michaelni@gmx.at>
 *
 * gmc & q-pel & 32/64 bit based MC by Michael Niedermayer <michaelni@gmx.at>
 *
 * This file is part of FFmpeg.
 *
 * FFmpeg is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * FFmpeg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with FFmpeg; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

/*
 * @file
 * DSP utils
 */


/* draw the edges of width 'w' of an image of size width, height */
//FIXME check that this is ok for mpeg4 interlaced
static void FUNCC(draw_edges)(uint8_t *p_buf, int p_wrap, int width, int height, int w, int h, int sides)
{
//	LOGD("standard draw_edges Called\n");
    pixel *buf = (pixel*)p_buf;
    int wrap = p_wrap / sizeof(pixel);
    pixel *ptr, *last_line;
    int i;

    /* left and right */
    ptr = buf;
    for(i=0;i<height;i++) {
        memset(ptr - w, ptr[0], w);
        memset(ptr + width, ptr[width-1], w);
        ptr += wrap;
    }

    /* top and bottom + corners */
    buf -= w;
    last_line = buf + (height - 1) * wrap;
    if (sides & EDGE_TOP)
        for(i = 0; i < h; i++)
            memcpy(buf - (i + 1) * wrap, buf, (width + w + w) * sizeof(pixel)); // top
    if (sides & EDGE_BOTTOM)
        for (i = 0; i < h; i++)
            memcpy(last_line + (i + 1) * wrap, last_line, (width + w + w) * sizeof(pixel)); // bottom
}

//static void get_pixels_8_c
static void FUNCC(get_pixels)(int16_t *block,
                              const uint8_t *_pixels,
                              int line_size)
{
//	LOGD("get_pixels Called _pixels:%d line_size:%d\n",*_pixels,line_size);
    const pixel *pixels = (const pixel *) _pixels;
    int i;
    /* read the pixels */
    for(i=0;i<8;i++) {
        block[0] = pixels[0];
        block[1] = pixels[1];
        block[2] = pixels[2];
        block[3] = pixels[3];
        block[4] = pixels[4];
        block[5] = pixels[5];
        block[6] = pixels[6];
        block[7] = pixels[7];
//    	LOGD("%02X %02X %02X %02X %02X %02X %02X %02X    ",pixels[0],pixels[1],pixels[2],pixels[3],pixels[4],pixels[5],pixels[6],pixels[7]);
        pixels += line_size / sizeof(pixel);
        block += 8;
    }
}

static void FUNCC(clear_block)(int16_t *block)
{
    memset(block, 0, sizeof(int16_t)*64);
}

static void FUNCC(clear_blocks)(int16_t *blocks)
{
    memset(blocks, 0, sizeof(int16_t)*6*64);
}



/* read the pixels */
#define DCTELEM_FUNCS(dctcoef, suffix)                                  \
void FUNCC(get_pixels ## suffix)(int16_t *_block,    \
                                        const uint8_t *_pixels,         \
                                        int line_size)                  \
{                                                                       \
	\
    const pixel *pixels = (const pixel *) _pixels;                      \
    dctcoef * block = (dctcoef *) _block;                    \
    int i;                                                              \
                                                                        \
                                                   \
    for(i=0;i<8;i++) {                                                  \
        block[0] = pixels[0];                                           \
        block[1] = pixels[1];                                           \
        block[2] = pixels[2];                                           \
        block[3] = pixels[3];                                           \
        block[4] = pixels[4];                                           \
        block[5] = pixels[5];                                           \
        block[6] = pixels[6];                                           \
        block[7] = pixels[7];                                           \
        pixels += line_size / sizeof(pixel);                            \
        block += 8;                                                     \
    }                                                                   \
}                                                                       \
                                                                        \
void FUNCC(clear_block ## suffix)(int16_t *block)                \
{                                                                       \
    memset(block, 0, sizeof(dctcoef)*64);                               \
}                                                                       \
                                                                        \
/**                                                                     \
 * memset(blocks, 0, sizeof(int16_t)*6*64)                              \
 */                                                                     \
void FUNCC(clear_blocks ## suffix)(int16_t *blocks)              \
{                                                                       \
    memset(blocks, 0, sizeof(dctcoef)*6*64);                            \
}

DCTELEM_FUNCS(int16_t, _16)
#if BIT_DEPTH > 8
DCTELEM_FUNCS(dctcoef, _32)
#endif



//ここからhpelとか--------------------------------------------------------------------------

#   define no_rnd_avg_pixel4 no_rnd_avg32
#   define    rnd_avg_pixel4    rnd_avg32

inline uint32_t rnd_avg32(uint32_t a, uint32_t b)
{
    return (a | b) - (((a ^ b) & ~BYTE_VEC32(0x01)) >> 1);
}

inline uint32_t no_rnd_avg32(uint32_t a, uint32_t b)
{
    return (a & b) + (((a ^ b) & ~BYTE_VEC32(0x01)) >> 1);
}

inline uint64_t rnd_avg64(uint64_t a, uint64_t b)
{
    return (a | b) - (((a ^ b) & ~BYTE_VEC64(0x01)) >> 1);
}

inline uint64_t no_rnd_avg64(uint64_t a, uint64_t b)
{
    return (a & b) + (((a ^ b) & ~BYTE_VEC64(0x01)) >> 1);
}



#define CALL_2X_PIXELS(a, b, n)\
void a(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
    b(block  , pixels  , line_size, h);\
    b(block+n, pixels+n, line_size, h);\
}

#define DEF_HPEL(OPNAME, OP) \
inline void FUNCC(OPNAME ## _pixels2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    int i;\
    for(i=0; i<h; i++){\
        OP(*((pixel2*)(block  )), AV_RN2P(pixels  ));\
        pixels+=line_size;\
        block +=line_size;\
    }\
}\
inline void FUNCC(OPNAME ## _pixels4)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    int i;\
    for(i=0; i<h; i++){\
        OP(*((pixel4*)(block  )), AV_RN4P(pixels  ));\
        pixels+=line_size;\
        block +=line_size;\
    }\
}\
inline void FUNCC(OPNAME ## _pixels8)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    int i;\
    for(i=0; i<h; i++){\
        OP(*((pixel4*)(block                )), AV_RN4P(pixels                ));\
        OP(*((pixel4*)(block+4*sizeof(pixel))), AV_RN4P(pixels+4*sizeof(pixel)));\
        pixels+=line_size;\
        block +=line_size;\
    }\
}\
\
inline void FUNC(OPNAME ## _pixels8_l2)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, \
                                                int src_stride1, int src_stride2, int h){\
	\
    int i;\
    for(i=0; i<h; i++){\
        pixel4 a,b;\
        a= AV_RN4P(&src1[i*src_stride1  ]);\
        b= AV_RN4P(&src2[i*src_stride2  ]);\
        OP(*((pixel4*)&dst[i*dst_stride  ]), rnd_avg_pixel4(a, b));\
        a= AV_RN4P(&src1[i*src_stride1+4*sizeof(pixel)]);\
        b= AV_RN4P(&src2[i*src_stride2+4*sizeof(pixel)]);\
        OP(*((pixel4*)&dst[i*dst_stride+4*sizeof(pixel)]), rnd_avg_pixel4(a, b));\
    }\
}\
\
inline void FUNC(OPNAME ## _pixels4_l2)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, \
                                                int src_stride1, int src_stride2, int h){\
	\
    int i;\
    for(i=0; i<h; i++){\
        pixel4 a,b;\
        a= AV_RN4P(&src1[i*src_stride1  ]);\
        b= AV_RN4P(&src2[i*src_stride2  ]);\
        OP(*((pixel4*)&dst[i*dst_stride  ]), rnd_avg_pixel4(a, b));\
    }\
}\
\
inline void FUNC(OPNAME ## _pixels2_l2)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride,int src_stride1, int src_stride2, int h){\
	\
    int i;\
    for(i=0; i<h; i++){\
        pixel4 a,b;\
        a= AV_RN2P(&src1[i*src_stride1  ]);\
        b= AV_RN2P(&src2[i*src_stride2  ]);\
        OP(*((pixel2*)&dst[i*dst_stride  ]), rnd_avg_pixel4(a, b));\
    }\
}\
\
inline void FUNC(OPNAME ## _pixels16_l2)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, \
                                                int src_stride1, int src_stride2, int h){\
	\
    FUNC(OPNAME ## _pixels8_l2)(dst  , src1  , src2  , dst_stride, src_stride1, src_stride2, h);\
    FUNC(OPNAME ## _pixels8_l2)(dst+8*sizeof(pixel), src1+8*sizeof(pixel), src2+8*sizeof(pixel), dst_stride, src_stride1, src_stride2, h);\
}\
\
CALL_2X_PIXELS(FUNCC(OPNAME ## _pixels16)    , FUNCC(OPNAME ## _pixels8)    , 8*sizeof(pixel))


#define op_avg(a, b) a = rnd_avg_pixel4(a, b)
#define op_put(a, b) a = b

DEF_HPEL(avg, op_avg)
DEF_HPEL(put, op_put)
#undef op_avg
#undef op_put

#define PIXOP2(OPNAME, OP) \
inline void FUNC(OPNAME ## _no_rnd_pixels8_l2)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, \
                                                int src_stride1, int src_stride2, int h){\
	\
    int i;\
    for(i=0; i<h; i++){\
        pixel4 a,b;\
        a= AV_RN4P(&src1[i*src_stride1  ]);\
        b= AV_RN4P(&src2[i*src_stride2  ]);\
        OP(*((pixel4*)&dst[i*dst_stride  ]), no_rnd_avg_pixel4(a, b));\
        a= AV_RN4P(&src1[i*src_stride1+4*sizeof(pixel)]);\
        b= AV_RN4P(&src2[i*src_stride2+4*sizeof(pixel)]);\
        OP(*((pixel4*)&dst[i*dst_stride+4*sizeof(pixel)]), no_rnd_avg_pixel4(a, b));\
    }\
}\
\
inline void FUNC(OPNAME ## _no_rnd_pixels16_l2)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, \
                                                int src_stride1, int src_stride2, int h){\
	\
    FUNC(OPNAME ## _no_rnd_pixels8_l2)(dst  , src1  , src2  , dst_stride, src_stride1, src_stride2, h);\
    FUNC(OPNAME ## _no_rnd_pixels8_l2)(dst+8*sizeof(pixel), src1+8*sizeof(pixel), src2+8*sizeof(pixel), dst_stride, src_stride1, src_stride2, h);\
}\
\
inline void FUNCC(OPNAME ## _no_rnd_pixels8_x2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    FUNC(OPNAME ## _no_rnd_pixels8_l2)(block, pixels, pixels+sizeof(pixel), line_size, line_size, line_size, h);\
}\
\
inline void FUNCC(OPNAME ## _pixels8_x2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    FUNC(OPNAME ## _pixels8_l2)(block, pixels, pixels+sizeof(pixel), line_size, line_size, line_size, h);\
}\
\
inline void FUNCC(OPNAME ## _no_rnd_pixels8_y2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    FUNC(OPNAME ## _no_rnd_pixels8_l2)(block, pixels, pixels+line_size, line_size, line_size, line_size, h);\
}\
\
inline void FUNCC(OPNAME ## _pixels8_y2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    FUNC(OPNAME ## _pixels8_l2)(block, pixels, pixels+line_size, line_size, line_size, line_size, h);\
}\
\
inline void FUNC(OPNAME ## _pixels8_l4)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, const uint8_t *src3, const uint8_t *src4,\
                 int dst_stride, int src_stride1, int src_stride2,int src_stride3,int src_stride4, int h){\
    /* FIXME HIGH BIT DEPTH */\
		\
    int i;\
    for(i=0; i<h; i++){\
        uint32_t a, b, c, d, l0, l1, h0, h1;\
        a= AV_RN32(&src1[i*src_stride1]);\
        b= AV_RN32(&src2[i*src_stride2]);\
        c= AV_RN32(&src3[i*src_stride3]);\
        d= AV_RN32(&src4[i*src_stride4]);\
        l0=  (a&0x03030303UL)\
           + (b&0x03030303UL)\
           + 0x02020202UL;\
        h0= ((a&0xFCFCFCFCUL)>>2)\
          + ((b&0xFCFCFCFCUL)>>2);\
        l1=  (c&0x03030303UL)\
           + (d&0x03030303UL);\
        h1= ((c&0xFCFCFCFCUL)>>2)\
          + ((d&0xFCFCFCFCUL)>>2);\
        OP(*((uint32_t*)&dst[i*dst_stride]), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
        a= AV_RN32(&src1[i*src_stride1+4]);\
        b= AV_RN32(&src2[i*src_stride2+4]);\
        c= AV_RN32(&src3[i*src_stride3+4]);\
        d= AV_RN32(&src4[i*src_stride4+4]);\
        l0=  (a&0x03030303UL)\
           + (b&0x03030303UL)\
           + 0x02020202UL;\
        h0= ((a&0xFCFCFCFCUL)>>2)\
          + ((b&0xFCFCFCFCUL)>>2);\
        l1=  (c&0x03030303UL)\
           + (d&0x03030303UL);\
        h1= ((c&0xFCFCFCFCUL)>>2)\
          + ((d&0xFCFCFCFCUL)>>2);\
        OP(*((uint32_t*)&dst[i*dst_stride+4]), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
    }\
}\
\
inline void FUNCC(OPNAME ## _pixels4_x2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    FUNC(OPNAME ## _pixels4_l2)(block, pixels, pixels+sizeof(pixel), line_size, line_size, line_size, h);\
}\
\
inline void FUNCC(OPNAME ## _pixels4_y2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    FUNC(OPNAME ## _pixels4_l2)(block, pixels, pixels+line_size, line_size, line_size, line_size, h);\
}\
\
inline void FUNCC(OPNAME ## _pixels2_x2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    FUNC(OPNAME ## _pixels2_l2)(block, pixels, pixels+sizeof(pixel), line_size, line_size, line_size, h);\
}\
\
inline void FUNCC(OPNAME ## _pixels2_y2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h){\
	\
    FUNC(OPNAME ## _pixels2_l2)(block, pixels, pixels+line_size, line_size, line_size, line_size, h);\
}\
\
inline void FUNC(OPNAME ## _no_rnd_pixels8_l4)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, const uint8_t *src3, const uint8_t *src4,\
                 int dst_stride, int src_stride1, int src_stride2,int src_stride3,int src_stride4, int h){\
    /* FIXME HIGH BIT DEPTH*/\
		\
    int i;\
    for(i=0; i<h; i++){\
        uint32_t a, b, c, d, l0, l1, h0, h1;\
        a= AV_RN32(&src1[i*src_stride1]);\
        b= AV_RN32(&src2[i*src_stride2]);\
        c= AV_RN32(&src3[i*src_stride3]);\
        d= AV_RN32(&src4[i*src_stride4]);\
        l0=  (a&0x03030303UL)\
           + (b&0x03030303UL)\
           + 0x01010101UL;\
        h0= ((a&0xFCFCFCFCUL)>>2)\
          + ((b&0xFCFCFCFCUL)>>2);\
        l1=  (c&0x03030303UL)\
           + (d&0x03030303UL);\
        h1= ((c&0xFCFCFCFCUL)>>2)\
          + ((d&0xFCFCFCFCUL)>>2);\
        OP(*((uint32_t*)&dst[i*dst_stride]), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
        a= AV_RN32(&src1[i*src_stride1+4]);\
        b= AV_RN32(&src2[i*src_stride2+4]);\
        c= AV_RN32(&src3[i*src_stride3+4]);\
        d= AV_RN32(&src4[i*src_stride4+4]);\
        l0=  (a&0x03030303UL)\
           + (b&0x03030303UL)\
           + 0x01010101UL;\
        h0= ((a&0xFCFCFCFCUL)>>2)\
          + ((b&0xFCFCFCFCUL)>>2);\
        l1=  (c&0x03030303UL)\
           + (d&0x03030303UL);\
        h1= ((c&0xFCFCFCFCUL)>>2)\
          + ((d&0xFCFCFCFCUL)>>2);\
        OP(*((uint32_t*)&dst[i*dst_stride+4]), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
    }\
}\
inline void FUNC(OPNAME ## _pixels16_l4)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, const uint8_t *src3, const uint8_t *src4,\
                 int dst_stride, int src_stride1, int src_stride2,int src_stride3,int src_stride4, int h){\
	\
    FUNC(OPNAME ## _pixels8_l4)(dst  , src1  , src2  , src3  , src4  , dst_stride, src_stride1, src_stride2, src_stride3, src_stride4, h);\
    FUNC(OPNAME ## _pixels8_l4)(dst+8*sizeof(pixel), src1+8*sizeof(pixel), src2+8*sizeof(pixel), src3+8*sizeof(pixel), src4+8*sizeof(pixel), dst_stride, src_stride1, src_stride2, src_stride3, src_stride4, h);\
}\
inline void FUNC(OPNAME ## _no_rnd_pixels16_l4)(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, const uint8_t *src3, const uint8_t *src4,\
                 int dst_stride, int src_stride1, int src_stride2,int src_stride3,int src_stride4, int h){\
	\
    FUNC(OPNAME ## _no_rnd_pixels8_l4)(dst  , src1  , src2  , src3  , src4  , dst_stride, src_stride1, src_stride2, src_stride3, src_stride4, h);\
    FUNC(OPNAME ## _no_rnd_pixels8_l4)(dst+8*sizeof(pixel), src1+8*sizeof(pixel), src2+8*sizeof(pixel), src3+8*sizeof(pixel), src4+8*sizeof(pixel), dst_stride, src_stride1, src_stride2, src_stride3, src_stride4, h);\
}\
\
inline void FUNCC(OPNAME ## _pixels2_xy2)(uint8_t *_block, const uint8_t *_pixels, ptrdiff_t line_size, int h)\
{\
	\
        int i, a0, b0, a1, b1;\
        pixel *block = (pixel*)_block;\
        const pixel *pixels = (const pixel*)_pixels;\
        line_size >>= sizeof(pixel)-1;\
        a0= pixels[0];\
        b0= pixels[1] + 2;\
        a0 += b0;\
        b0 += pixels[2];\
\
        pixels+=line_size;\
        for(i=0; i<h; i+=2){\
            a1= pixels[0];\
            b1= pixels[1];\
            a1 += b1;\
            b1 += pixels[2];\
\
            block[0]= (a1+a0)>>2; /* FIXME non put */\
            block[1]= (b1+b0)>>2;\
\
            pixels+=line_size;\
            block +=line_size;\
\
            a0= pixels[0];\
            b0= pixels[1] + 2;\
            a0 += b0;\
            b0 += pixels[2];\
\
            block[0]= (a1+a0)>>2;\
            block[1]= (b1+b0)>>2;\
            pixels+=line_size;\
            block +=line_size;\
        }\
}\
\
inline void FUNCC(OPNAME ## _pixels4_xy2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h)\
{\
	\
        /* FIXME HIGH BIT DEPTH */\
        int i;\
        const uint32_t a= AV_RN32(pixels  );\
        const uint32_t b= AV_RN32(pixels+1);\
        uint32_t l0=  (a&0x03030303UL)\
                    + (b&0x03030303UL)\
                    + 0x02020202UL;\
        uint32_t h0= ((a&0xFCFCFCFCUL)>>2)\
                   + ((b&0xFCFCFCFCUL)>>2);\
        uint32_t l1,h1;\
\
        pixels+=line_size;\
        for(i=0; i<h; i+=2){\
            uint32_t a= AV_RN32(pixels  );\
            uint32_t b= AV_RN32(pixels+1);\
            l1=  (a&0x03030303UL)\
               + (b&0x03030303UL);\
            h1= ((a&0xFCFCFCFCUL)>>2)\
              + ((b&0xFCFCFCFCUL)>>2);\
            OP(*((uint32_t*)block), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
            pixels+=line_size;\
            block +=line_size;\
            a= AV_RN32(pixels  );\
            b= AV_RN32(pixels+1);\
            l0=  (a&0x03030303UL)\
               + (b&0x03030303UL)\
               + 0x02020202UL;\
            h0= ((a&0xFCFCFCFCUL)>>2)\
              + ((b&0xFCFCFCFCUL)>>2);\
            OP(*((uint32_t*)block), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
            pixels+=line_size;\
            block +=line_size;\
        }\
}\
\
inline void FUNCC(OPNAME ## _pixels8_xy2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h)\
{\
	\
    /* FIXME HIGH BIT DEPTH */\
    int j;\
    for(j=0; j<2; j++){\
        int i;\
        const uint32_t a= AV_RN32(pixels  );\
        const uint32_t b= AV_RN32(pixels+1);\
        uint32_t l0=  (a&0x03030303UL)\
                    + (b&0x03030303UL)\
                    + 0x02020202UL;\
        uint32_t h0= ((a&0xFCFCFCFCUL)>>2)\
                   + ((b&0xFCFCFCFCUL)>>2);\
        uint32_t l1,h1;\
\
        pixels+=line_size;\
        for(i=0; i<h; i+=2){\
            uint32_t a= AV_RN32(pixels  );\
            uint32_t b= AV_RN32(pixels+1);\
            l1=  (a&0x03030303UL)\
               + (b&0x03030303UL);\
            h1= ((a&0xFCFCFCFCUL)>>2)\
              + ((b&0xFCFCFCFCUL)>>2);\
            OP(*((uint32_t*)block), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
            pixels+=line_size;\
            block +=line_size;\
            a= AV_RN32(pixels  );\
            b= AV_RN32(pixels+1);\
            l0=  (a&0x03030303UL)\
               + (b&0x03030303UL)\
               + 0x02020202UL;\
            h0= ((a&0xFCFCFCFCUL)>>2)\
              + ((b&0xFCFCFCFCUL)>>2);\
            OP(*((uint32_t*)block), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
            pixels+=line_size;\
            block +=line_size;\
        }\
        pixels+=4-line_size*(h+1);\
        block +=4-line_size*h;\
    }\
}\
\
inline void FUNCC(OPNAME ## _no_rnd_pixels8_xy2)(uint8_t *block, const uint8_t *pixels, ptrdiff_t line_size, int h)\
{\
	\
    /* FIXME HIGH BIT DEPTH */\
    int j;\
    for(j=0; j<2; j++){\
        int i;\
        const uint32_t a= AV_RN32(pixels  );\
        const uint32_t b= AV_RN32(pixels+1);\
        uint32_t l0=  (a&0x03030303UL)\
                    + (b&0x03030303UL)\
                    + 0x01010101UL;\
        uint32_t h0= ((a&0xFCFCFCFCUL)>>2)\
                   + ((b&0xFCFCFCFCUL)>>2);\
        uint32_t l1,h1;\
\
        pixels+=line_size;\
        for(i=0; i<h; i+=2){\
            uint32_t a= AV_RN32(pixels  );\
            uint32_t b= AV_RN32(pixels+1);\
            l1=  (a&0x03030303UL)\
               + (b&0x03030303UL);\
            h1= ((a&0xFCFCFCFCUL)>>2)\
              + ((b&0xFCFCFCFCUL)>>2);\
            OP(*((uint32_t*)block), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
            pixels+=line_size;\
            block +=line_size;\
            a= AV_RN32(pixels  );\
            b= AV_RN32(pixels+1);\
            l0=  (a&0x03030303UL)\
               + (b&0x03030303UL)\
               + 0x01010101UL;\
            h0= ((a&0xFCFCFCFCUL)>>2)\
              + ((b&0xFCFCFCFCUL)>>2);\
            OP(*((uint32_t*)block), h0+h1+(((l0+l1)>>2)&0x0F0F0F0FUL));\
            pixels+=line_size;\
            block +=line_size;\
        }\
        pixels+=4-line_size*(h+1);\
        block +=4-line_size*h;\
    }\
}\
\
CALL_2X_PIXELS(FUNCC(OPNAME ## _pixels16_x2) , FUNCC(OPNAME ## _pixels8_x2) , 8*sizeof(pixel))\
CALL_2X_PIXELS(FUNCC(OPNAME ## _pixels16_y2) , FUNCC(OPNAME ## _pixels8_y2) , 8*sizeof(pixel))\
CALL_2X_PIXELS(FUNCC(OPNAME ## _pixels16_xy2), FUNCC(OPNAME ## _pixels8_xy2), 8*sizeof(pixel))\
CALL_2X_PIXELS(FUNCC(OPNAME ## _no_rnd_pixels16)    , FUNCC(OPNAME ## _pixels8) , 8*sizeof(pixel))\
CALL_2X_PIXELS(FUNCC(OPNAME ## _no_rnd_pixels16_x2) , FUNCC(OPNAME ## _no_rnd_pixels8_x2) , 8*sizeof(pixel))\
CALL_2X_PIXELS(FUNCC(OPNAME ## _no_rnd_pixels16_y2) , FUNCC(OPNAME ## _no_rnd_pixels8_y2) , 8*sizeof(pixel))\
CALL_2X_PIXELS(FUNCC(OPNAME ## _no_rnd_pixels16_xy2), FUNCC(OPNAME ## _no_rnd_pixels8_xy2), 8*sizeof(pixel))\



#define op_avg(a, b) a = rnd_avg_pixel4(a, b)
#define op_put(a, b) a = b
#define put_no_rnd_pixels8_8_c put_pixels8_8_c

PIXOP2(avg, op_avg)
PIXOP2(put, op_put)

#undef op_avg
#undef op_put

//--------------------------------------------------------------------------


void FUNCC(ff_put_pixels8x8)(uint8_t *dst, uint8_t *src, int stride) {
    FUNCC(put_pixels8)(dst, src, stride, 8);
}
void FUNCC(ff_avg_pixels8x8)(uint8_t *dst, uint8_t *src, int stride) {
    FUNCC(avg_pixels8)(dst, src, stride, 8);
}
void FUNCC(ff_put_pixels16x16)(uint8_t *dst, uint8_t *src, int stride) {
    FUNCC(put_pixels16)(dst, src, stride, 16);
}
void FUNCC(ff_avg_pixels16x16)(uint8_t *dst, uint8_t *src, int stride) {
    FUNCC(avg_pixels16)(dst, src, stride, 16);
}



#define DIRAC_PIXOP(OPNAME2, OPNAME, EXT)\
void ff_ ## OPNAME2 ## _dirac_pixels8_ ## EXT(uint8_t *dst, const uint8_t *src[5], int stride, int h)\
{\
    if (h&3)\
        ff_ ## OPNAME2 ## _dirac_pixels8_c(dst, src, stride, h);\
    else\
        OPNAME ## _pixels8_ ## EXT(dst, src[0], stride, h);\
}\
void ff_ ## OPNAME2 ## _dirac_pixels16_ ## EXT(uint8_t *dst, const uint8_t *src[5], int stride, int h)\
{\
    if (h&3)\
        ff_ ## OPNAME2 ## _dirac_pixels16_c(dst, src, stride, h);\
    else\
        OPNAME ## _pixels16_ ## EXT(dst, src[0], stride, h);\
}\
void ff_ ## OPNAME2 ## _dirac_pixels32_ ## EXT(uint8_t *dst, const uint8_t *src[5], int stride, int h)\
{\
    if (h&3) {\
        ff_ ## OPNAME2 ## _dirac_pixels32_c(dst, src, stride, h);\
    } else {\
        OPNAME ## _pixels16_ ## EXT(dst   , src[0]   , stride, h);\
        OPNAME ## _pixels16_ ## EXT(dst+16, src[0]+16, stride, h);\
    }\
}

//DIRAC_PIXOP(put, put, mmx)
//DIRAC_PIXOP(avg, avg, mmx)

int dct_sad8x8_c(/*MpegEncContext*/ void *c, uint8_t *src1, uint8_t *src2, int stride, int h){
    LOGD("row_idct_mmx Called\n");
	MpegEncContext * const s= (MpegEncContext *)c;
    LOCAL_ALIGNED_16(int16_t, temp, [64]);

//    av_assert2(h==8);

    s->dsp.diff_pixels(temp, src1, src2, stride);
    s->dsp.fdct(temp);
    return s->dsp.sum_abs_dctelem(temp);
}

int dct_max8x8_c(/*MpegEncContext*/ void *c, uint8_t *src1, uint8_t *src2, int stride, int h){
	LOGD("dct_max8x8_c Called\n");

	MpegEncContext * const s= (MpegEncContext *)c;
    LOCAL_ALIGNED_16(int16_t, temp, [64]);
    int sum=0, i;

//    av_assert2(h==8);

    s->dsp.diff_pixels(temp, src1, src2, stride);
    s->dsp.fdct(temp);

    for(i=0; i<64; i++)
        sum= FFMAX(sum, FFABS(temp[i]));

    return sum;
}

int quant_psnr8x8_c(/*MpegEncContext*/ void *c, uint8_t *src1, uint8_t *src2, int stride, int h){
    LOGD("quant_psnr8x8_c block_last_index\n");
	MpegEncContext * const s= (MpegEncContext *)c;
    LOCAL_ALIGNED_16(int16_t, temp, [64*2]);
    int16_t * const bak = temp+64;
    int sum=0, i;

//    av_assert2(h==8);
    s->mb_intra=0;

    s->dsp.diff_pixels(temp, src1, src2, stride);

    memcpy(bak, temp, 64*sizeof(int16_t));

    s->block_last_index[0/*FIXME*/]= s->fast_dct_quantize(s, temp, 0/*FIXME*/, s->qscale, &i);
    s->dct_unquantize_inter(s, temp, 0, s->qscale);
    ff_simple_idct_8(temp); //FIXME

    for(i=0; i<64; i++)
        sum+= (temp[i]-bak[i])*(temp[i]-bak[i]);

    return sum;
}

static inline void copy_block8(uint8_t *dst, const uint8_t *src, int dstStride, int srcStride, int h)
{
    int i;
    for(i=0; i<h; i++)
    {
        AV_COPY64U(dst, src);
        dst+=dstStride;
        src+=srcStride;
    }
}

inline void copy_block9(uint8_t *dst, const uint8_t *src, int dstStride, int srcStride, int h)
{
    int i;
    for(i=0; i<h; i++)
    {
        AV_COPY64U(dst, src);
        dst[8]= src[8];
        dst+=dstStride;
        src+=srcStride;
    }
}



int rd8x8_c(/*MpegEncContext*/ void *c, uint8_t *src1, uint8_t *src2, int stride, int h){


	LOGD("rd8x8_c block_last_index\n");
	MpegEncContext * const s= (MpegEncContext *)c;
    const uint8_t *scantable= s->intra_scantable.permutated;
    LOCAL_ALIGNED_16(int16_t, temp, [64]);
    LOCAL_ALIGNED_16(uint8_t, lsrc1, [64]);
    LOCAL_ALIGNED_16(uint8_t, lsrc2, [64]);
    int i, last, run, bits, level, distortion, start_i;
    const int esc_length= s->ac_esc_length;
    uint8_t * length;
    uint8_t * last_length;

//    av_assert2(h==8);

    copy_block8(lsrc1, src1, 8, stride, 8);
    copy_block8(lsrc2, src2, 8, stride, 8);

    s->dsp.diff_pixels(temp, lsrc1, lsrc2, 8);

    s->block_last_index[0/*FIXME*/]= last= s->fast_dct_quantize(s, temp, 0/*FIXME*/, s->qscale, &i);

    bits=0;

    if (s->mb_intra) {
        start_i = 1;
        length     = s->intra_ac_vlc_length;
        last_length= s->intra_ac_vlc_last_length;
        bits+= s->luma_dc_vlc_length[temp[0] + 256]; //FIXME chroma
    } else {
        start_i = 0;
        length     = s->inter_ac_vlc_length;
        last_length= s->inter_ac_vlc_last_length;
    }

    if(last>=start_i){
        run=0;
        for(i=start_i; i<last; i++){
            int j= scantable[i];
            level= temp[j];

            if(level){
                level+=64;
                if((level&(~127)) == 0){
                    bits+= length[UNI_AC_ENC_INDEX(run, level)];
                }else
                    bits+= esc_length;
                run=0;
            }else
                run++;
        }
        i= scantable[last];

        level= temp[i] + 64;

//        av_assert2(level - 64);

        if((level&(~127)) == 0){
            bits+= last_length[UNI_AC_ENC_INDEX(run, level)];
        }else
            bits+= esc_length;

    }

    if(last>=0){
        if(s->mb_intra)
            s->dct_unquantize_intra(s, temp, 0, s->qscale);
        else
            s->dct_unquantize_inter(s, temp, 0, s->qscale);
    }

    s->dsp.idct_add(lsrc2, 8, temp);

    distortion= s->dsp.sse[1](NULL, lsrc2, lsrc1, 8, 8);

    return distortion + ((bits*s->qscale*s->qscale*109 + 64)>>7);
}

int bit8x8_c(/*MpegEncContext*/ void *c, uint8_t *src1, uint8_t *src2, int stride, int h){

	LOGD("bit8x8_c block_last_index\n");
	MpegEncContext * const s= (MpegEncContext *)c;
    const uint8_t *scantable= s->intra_scantable.permutated;
    LOCAL_ALIGNED_16(int16_t, temp, [64]);
    int i, last, run, bits, level, start_i;
    const int esc_length= s->ac_esc_length;
    uint8_t * length;
    uint8_t * last_length;

//    av_assert2(h==8);

    s->dsp.diff_pixels(temp, src1, src2, stride);

    s->block_last_index[0/*FIXME*/]= last= s->fast_dct_quantize(s, temp, 0/*FIXME*/, s->qscale, &i);

    bits=0;

    if (s->mb_intra) {
        start_i = 1;
        length     = s->intra_ac_vlc_length;
        last_length= s->intra_ac_vlc_last_length;
        bits+= s->luma_dc_vlc_length[temp[0] + 256]; //FIXME chroma
    } else {
        start_i = 0;
        length     = s->inter_ac_vlc_length;
        last_length= s->inter_ac_vlc_last_length;
    }

    if(last>=start_i){
        run=0;
        for(i=start_i; i<last; i++){
            int j= scantable[i];
            level= temp[j];

            if(level){
                level+=64;
                if((level&(~127)) == 0){
                    bits+= length[UNI_AC_ENC_INDEX(run, level)];
                }else
                    bits+= esc_length;
                run=0;
            }else
                run++;
        }
        i= scantable[last];

        level= temp[i] + 64;

//        av_assert2(level - 64);

        if((level&(~127)) == 0){
            bits+= last_length[UNI_AC_ENC_INDEX(run, level)];
        }else
            bits+= esc_length;
    }

    return bits;
}




#    define LOCAL_MANGLE(a) #a
#define MANGLE(a) "_" LOCAL_MANGLE(a)


#undef COL_SHIFT
#undef ROW_SHIFT
//#define COL_SHIFT 6
#define ROW_SHIFT 11
#define COL_SHIFT 20 // 6

#define round(bias) ((int)(((bias)+0.5) * (1<<ROW_SHIFT)))
#define rounder(bias) {round (bias), round (bias)}

/* MMX row IDCT */

#define mmx_table(c1,c2,c3,c4,c5,c6,c7) {  c4,  c2,  c4,  c6,   \
                                           c4,  c6, -c4, -c2,   \
                                           c1,  c3,  c3, -c7,   \
                                           c5,  c7, -c1, -c5,   \
                                           c4, -c6,  c4, -c2,   \
                                          -c4,  c2,  c4, -c6,   \
                                           c5, -c1,  c7, -c5,   \
                                           c7,  c3,  c3, -c1 }


#define mmxext_table(c1,c2,c3,c4,c5,c6,c7)      {  c4,  c2, -c4, -c2,   \
                                                   c4,  c6,  c4,  c6,   \
                                                   c1,  c3, -c1, -c5,   \
                                                   c5,  c7,  c3, -c7,   \
                                                   c4, -c6,  c4, -c6,   \
                                                  -c4,  c2,  c4, -c2,   \
                                                   c5, -c1,  c3, -c1,   \
                                                   c7,  c3,  c7, -c5 }


DECLARE_ALIGNED(8, const int32_t, rounder0)[] = rounder ((1 << (COL_SHIFT - 1)) - 0.5);
DECLARE_ALIGNED(8, const int32_t, rounder4)[] = rounder (0);
//DECLARE_ALIGNED(8, static const int32_t, rounder1)[] =
//    rounder (1.25683487303);        /* C1*(C1/C4+C1+C7)/2 */
//DECLARE_ALIGNED(8, static const int32_t, rounder7)[] =
//    rounder (-0.25);                /* C1*(C7/C4+C7-C1)/2 */
//DECLARE_ALIGNED(8, static const int32_t, rounder2)[] =
//    rounder (0.60355339059);        /* C2 * (C6+C2)/2 */
//DECLARE_ALIGNED(8, static const int32_t, rounder6)[] =
//    rounder (-0.25);                /* C2 * (C6-C2)/2 */
//DECLARE_ALIGNED(8, static const int32_t, rounder3)[] =
//    rounder (0.087788325588);       /* C3*(-C3/C4+C3+C5)/2 */
//DECLARE_ALIGNED(8, static const int32_t, rounder5)[] =
//    rounder (-0.441341716183);      /* C3*(-C5/C4+C5-C3)/2 */



#define declare_idct(idct,table,idct_row_head,idct_row,idct_row_tail,idct_row_mid) \
void idct (int16_t * const block)                                       \
{                                                                       \
    DECLARE_ALIGNED(16, static const int16_t, table04)[] =              \
        table (22725, 21407, 19266, 16384, 12873,  8867, 4520);         \
    DECLARE_ALIGNED(16, static const int16_t, table17)[] =              \
        table (31521, 29692, 26722, 22725, 17855, 12299, 6270);         \
    DECLARE_ALIGNED(16, static const int16_t, table26)[] =              \
        table (29692, 27969, 25172, 21407, 16819, 11585, 5906);         \
    DECLARE_ALIGNED(16, static const int16_t, table35)[] =              \
        table (26722, 25172, 22654, 19266, 15137, 10426, 5315);         \
                                                                        \
    idct_row_head (block, 0*8, table04);                                \
    idct_row (table04, rounder0);                                       \
    idct_row_mid (block, 0*8, 4*8, table04);                            \
    idct_row (table04, rounder4);                                       \
    idct_row_mid (block, 4*8, 1*8, table17);                            \
    idct_row (table17, rounder1);                                       \
    idct_row_mid (block, 1*8, 7*8, table17);                            \
    idct_row (table17, rounder7);                                       \
    idct_row_mid (block, 7*8, 2*8, table26);                            \
    idct_row (table26, rounder2);                                       \
    idct_row_mid (block, 2*8, 6*8, table26);                            \
    idct_row (table26, rounder6);                                       \
    idct_row_mid (block, 6*8, 3*8, table35);                            \
    idct_row (table35, rounder3);                                       \
    idct_row_mid (block, 3*8, 5*8, table35);                            \
    idct_row (table35, rounder5);                                       \
    idct_row_tail (block, 5*8);                                         \
                                                                        \
    idct_col (block, 0);                                                \
    idct_col (block, 4);                                                \
}


#define C0 23170 //cos(i*M_PI/16)*sqrt(2)*(1<<14) + 0.5
#define C1 22725 //cos(i*M_PI/16)*sqrt(2)*(1<<14) + 0.5
#define C2 21407 //cos(i*M_PI/16)*sqrt(2)*(1<<14) + 0.5
#define C3 19266 //cos(i*M_PI/16)*sqrt(2)*(1<<14) + 0.5
#define C4 16383 //cos(i*M_PI/16)*sqrt(2)*(1<<14) - 0.5
#define C5 12873 //cos(i*M_PI/16)*sqrt(2)*(1<<14) + 0.5
#define C6 8867  //cos(i*M_PI/16)*sqrt(2)*(1<<14) + 0.5
#define C7 4520  //cos(i*M_PI/16)*sqrt(2)*(1<<14) + 0.5







typedef struct xmm_reg { uint64_t a, b; } xmm_reg;
DECLARE_ALIGNED(16, const xmm_reg,  ff_pb_80)   = { 0x8080808080808080ULL, 0x8080808080808080ULL };





#define SET_HPEL_FUNCS(PFX, IDX, SIZE, CPU)                                     \
    do {                                                                        \
        c->PFX ## _pixels_tab IDX [0] = PFX ## _pixels ## SIZE ## _     ## CPU; \
        c->PFX ## _pixels_tab IDX [1] = PFX ## _pixels ## SIZE ## _x2_  ## CPU; \
        c->PFX ## _pixels_tab IDX [2] = PFX ## _pixels ## SIZE ## _y2_  ## CPU; \
        c->PFX ## _pixels_tab IDX [3] = PFX ## _pixels ## SIZE ## _xy2_ ## CPU; \
    } while (0)




typedef void emulated_edge_mc_func(uint8_t *dst, const uint8_t *src,
                                   ptrdiff_t linesize, int block_w, int block_h,
                                   int src_x, int src_y, int w, int h);



void FUNC(ff_emulated_edge_mc)(uint8_t *buf, const uint8_t *src,
                                      ptrdiff_t linesize_arg,
                                      int block_w, int block_h,
                                      int src_x, int src_y, int w, int h)
{
    int x, y;
    int start_y, start_x, end_y, end_x;
    int linesize = linesize_arg;

    if (!w || !h)
        return;

    if (src_y >= h) {
        src -= src_y * linesize;
        src += (h - 1) * linesize;
        src_y = h - 1;
    } else if (src_y <= -block_h) {
        src -= src_y * linesize;
        src += (1 - block_h) * linesize;
        src_y = 1 - block_h;
    }
    if (src_x >= w) {
        src  += (w - 1 - src_x) * sizeof(pixel);
        src_x = w - 1;
    } else if (src_x <= -block_w) {
        src  += (1 - block_w - src_x) * sizeof(pixel);
        src_x = 1 - block_w;
    }

    start_y = FFMAX(0, -src_y);
    start_x = FFMAX(0, -src_x);
    end_y = FFMIN(block_h, h-src_y);
    end_x = FFMIN(block_w, w-src_x);
//    av_assert2(start_y < end_y && block_h);
//    av_assert2(start_x < end_x && block_w);

    w    = end_x - start_x;
    src += start_y * linesize + start_x * sizeof(pixel);
    buf += start_x * sizeof(pixel);

    // top
    for (y = 0; y < start_y; y++) {
        memcpy(buf, src, w * sizeof(pixel));
        buf += linesize;
    }

    // copy existing part
    for (; y < end_y; y++) {
        memcpy(buf, src, w * sizeof(pixel));
        src += linesize;
        buf += linesize;
    }

    // bottom
    src -= linesize;
    for (; y < block_h; y++) {
        memcpy(buf, src, w * sizeof(pixel));
        buf += linesize;
    }

    buf -= block_h * linesize + start_x * sizeof(pixel);
    while (block_h--) {
        pixel *bufp = (pixel *) buf;

        // left
        for(x = 0; x < start_x; x++) {
            bufp[x] = bufp[start_x];
        }

        // right
        for (x = end_x; x < block_w; x++) {
            bufp[x] = bufp[end_x - 1];
        }
        buf += linesize;
    }
}



static av_cold void dsputil_init_mmx(DSPContext *c, AVCodecContext *avctx,
                                     int mm_flags)
{
	LOGD("dsputil_init_mmx Called\n");
    const int high_bit_depth = avctx->bits_per_raw_sample > 8;

//    c->put_pixels_clamped        = ff_put_pixels_clamped_mmx;//呼ばないとダメか?？
//    c->put_signed_pixels_clamped = ff_put_signed_pixels_clamped_mmx;//呼ばないとダメか?？
//    c->add_pixels_clamped        = ff_add_pixels_clamped_mmx;//呼ばないとダメか?？

    if (!high_bit_depth) {
    	LOGD("SET_MMX_DRAW_EDGES\n");
//        c->clear_block  = clear_block_mmx;
//        c->clear_blocks = clear_blocks_mmx;
////        c->draw_edges   = draw_edges_mmx;
//
//        SET_HPEL_FUNCS(put,        [0], 16, mmx);
//        SET_HPEL_FUNCS(put_no_rnd, [0], 16, mmx);
//        SET_HPEL_FUNCS(avg,        [0], 16, mmx);
//        SET_HPEL_FUNCS(avg_no_rnd,    , 16, mmx);
//        SET_HPEL_FUNCS(put,        [1],  8, mmx);
//        SET_HPEL_FUNCS(put_no_rnd, [1],  8, mmx);
//        SET_HPEL_FUNCS(avg,        [1],  8, mmx);
    }

//#if CONFIG_VIDEODSP && (ARCH_X86_32 || !HAVE_YASM)
//    c->gmc = gmc_mmx;//あかんか
//#endif

//    c->add_bytes = add_bytes_mmx;//呼ばなあかんか

//    if (CONFIG_H263_DECODER || CONFIG_H263_ENCODER) {
//        c->h263_v_loop_filter = ff_h263_v_loop_filter_mmx;//アセンブラ難しいのとs->loop_filterが0で呼ばれてないので飛ばす
//        c->h263_h_loop_filter = ff_h263_h_loop_filter_mmx;//アセンブラ難しいのとs->loop_filterが0で呼ばれてないので飛ばす
//    }

//    c->vector_clip_int32 = ff_vector_clip_int32_mmx;//アセンブラ難しいのと呼ばれてなかったのでおk

}




#define QPEL_OP(OPNAME, ROUNDER, RND, MMX)                              \
static void OPNAME ## qpel8_mc00_ ## MMX (uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    ff_ ## OPNAME ## pixels8_ ## MMX(dst, src, stride, 8);              \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc10_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t temp[8];                                                   \
    uint8_t * const half = (uint8_t*)temp;                              \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(half, src, 8,        \
                                                   stride, 8);          \
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, src, half,                 \
                                        stride, stride, 8);             \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc20_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    ff_ ## OPNAME ## mpeg4_qpel8_h_lowpass_ ## MMX(dst, src, stride,    \
                                                   stride, 8);          \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc30_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t temp[8];                                                   \
    uint8_t * const half = (uint8_t*)temp;                              \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(half, src, 8,        \
                                                   stride, 8);          \
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, src + 1, half, stride,     \
                                        stride, 8);                     \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc01_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t temp[8];                                                   \
    uint8_t * const half = (uint8_t*)temp;                              \
    ff_put ## RND ## mpeg4_qpel8_v_lowpass_ ## MMX(half, src,           \
                                                   8, stride);          \
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, src, half,                 \
                                        stride, stride, 8);             \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc02_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    ff_ ## OPNAME ## mpeg4_qpel8_v_lowpass_ ## MMX(dst, src,            \
                                                   stride, stride);     \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc03_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t temp[8];                                                   \
    uint8_t * const half = (uint8_t*)temp;                              \
    ff_put ## RND ## mpeg4_qpel8_v_lowpass_ ## MMX(half, src,           \
                                                   8, stride);          \
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, src + stride, half, stride,\
                                        stride, 8);                     \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc11_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[8 + 9];                                               \
    uint8_t * const halfH  = ((uint8_t*)half) + 64;                     \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_put ## RND ## pixels8_l2_ ## MMX(halfH, src, halfH, 8,           \
                                        stride, 9);                     \
    ff_put ## RND ## mpeg4_qpel8_v_lowpass_ ## MMX(halfHV, halfH, 8, 8);\
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, halfH, halfHV,             \
                                        stride, 8, 8);                  \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc31_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[8 + 9];                                               \
    uint8_t * const halfH  = ((uint8_t*)half) + 64;                     \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_put ## RND ## pixels8_l2_ ## MMX(halfH, src + 1, halfH, 8,       \
                                        stride, 9);                     \
    ff_put ## RND ## mpeg4_qpel8_v_lowpass_ ## MMX(halfHV, halfH, 8, 8);\
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, halfH, halfHV,             \
                                        stride, 8, 8);                  \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc13_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[8 + 9];                                               \
    uint8_t * const halfH  = ((uint8_t*)half) + 64;                     \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_put ## RND ## pixels8_l2_ ## MMX(halfH, src, halfH, 8,           \
                                        stride, 9);                     \
    ff_put ## RND ## mpeg4_qpel8_v_lowpass_ ## MMX(halfHV, halfH, 8, 8);\
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, halfH + 8, halfHV,         \
                                        stride, 8, 8);                  \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc33_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[8 + 9];                                               \
    uint8_t * const halfH  = ((uint8_t*)half) + 64;                     \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_put ## RND ## pixels8_l2_ ## MMX(halfH, src + 1, halfH, 8,       \
                                        stride, 9);                     \
    ff_put ## RND ## mpeg4_qpel8_v_lowpass_ ## MMX(halfHV, halfH, 8, 8);\
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, halfH + 8, halfHV,         \
                                        stride, 8, 8);                  \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc21_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[8 + 9];                                               \
    uint8_t * const halfH  = ((uint8_t*)half) + 64;                     \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_put ## RND ## mpeg4_qpel8_v_lowpass_ ## MMX(halfHV, halfH, 8, 8);\
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, halfH, halfHV,             \
                                        stride, 8, 8);                  \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc23_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[8 + 9];                                               \
    uint8_t * const halfH  = ((uint8_t*)half) + 64;                     \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_put ## RND ## mpeg4_qpel8_v_lowpass_ ## MMX(halfHV, halfH, 8, 8);\
    ff_ ## OPNAME ## pixels8_l2_ ## MMX(dst, halfH + 8, halfHV,         \
                                        stride, 8, 8);                  \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc12_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[8 + 9];                                               \
    uint8_t * const halfH = ((uint8_t*)half);                           \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_put ## RND ## pixels8_l2_ ## MMX(halfH, src, halfH,              \
                                        8, stride, 9);                  \
    ff_ ## OPNAME ## mpeg4_qpel8_v_lowpass_ ## MMX(dst, halfH,          \
                                                   stride, 8);          \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc32_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[8 + 9];                                               \
    uint8_t * const halfH = ((uint8_t*)half);                           \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_put ## RND ## pixels8_l2_ ## MMX(halfH, src + 1, halfH, 8,       \
                                        stride, 9);                     \
    ff_ ## OPNAME ## mpeg4_qpel8_v_lowpass_ ## MMX(dst, halfH,          \
                                                   stride, 8);          \
}                                                                       \
                                                                        \
static void OPNAME ## qpel8_mc22_ ## MMX(uint8_t *dst, uint8_t *src,    \
                                         int stride)                    \
{                                                                       \
    uint64_t half[9];                                                   \
    uint8_t * const halfH = ((uint8_t*)half);                           \
    ff_put ## RND ## mpeg4_qpel8_h_lowpass_ ## MMX(halfH, src, 8,       \
                                                   stride, 9);          \
    ff_ ## OPNAME ## mpeg4_qpel8_v_lowpass_ ## MMX(dst, halfH,          \
                                                   stride, 8);          \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc00_ ## MMX (uint8_t *dst, uint8_t *src,  \
                                           int stride)                  \
{                                                                       \
    ff_ ## OPNAME ## pixels16_ ## MMX(dst, src, stride, 16);            \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc10_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t temp[32];                                                  \
    uint8_t * const half = (uint8_t*)temp;                              \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(half, src, 16,      \
                                                    stride, 16);        \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, src, half, stride,        \
                                         stride, 16);                   \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc20_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    ff_ ## OPNAME ## mpeg4_qpel16_h_lowpass_ ## MMX(dst, src,           \
                                                    stride, stride, 16);\
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc30_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t temp[32];                                                  \
    uint8_t * const half = (uint8_t*)temp;                              \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(half, src, 16,      \
                                                    stride, 16);        \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, src + 1, half,            \
                                         stride, stride, 16);           \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc01_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t temp[32];                                                  \
    uint8_t * const half = (uint8_t*)temp;                              \
    ff_put ## RND ## mpeg4_qpel16_v_lowpass_ ## MMX(half, src, 16,      \
                                                    stride);            \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, src, half, stride,        \
                                         stride, 16);                   \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc02_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    ff_ ## OPNAME ## mpeg4_qpel16_v_lowpass_ ## MMX(dst, src,           \
                                                    stride, stride);    \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc03_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t temp[32];                                                  \
    uint8_t * const half = (uint8_t*)temp;                              \
    ff_put ## RND ## mpeg4_qpel16_v_lowpass_ ## MMX(half, src, 16,      \
                                                    stride);            \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, src+stride, half,         \
                                         stride, stride, 16);           \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc11_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[16 * 2 + 17 * 2];                                     \
    uint8_t * const halfH  = ((uint8_t*)half) + 256;                    \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_put ## RND ## pixels16_l2_ ## MMX(halfH, src, halfH, 16,         \
                                         stride, 17);                   \
    ff_put ## RND ## mpeg4_qpel16_v_lowpass_ ## MMX(halfHV, halfH,      \
                                                    16, 16);            \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, halfH, halfHV,            \
                                         stride, 16, 16);               \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc31_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[16 * 2 + 17 * 2];                                     \
    uint8_t * const halfH  = ((uint8_t*)half) + 256;                    \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_put ## RND ## pixels16_l2_ ## MMX(halfH, src + 1, halfH, 16,     \
                                         stride, 17);                   \
    ff_put ## RND ## mpeg4_qpel16_v_lowpass_ ## MMX(halfHV, halfH,      \
                                                    16, 16);            \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, halfH, halfHV,            \
                                         stride, 16, 16);               \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc13_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[16 * 2 + 17 * 2];                                     \
    uint8_t * const halfH  = ((uint8_t*)half) + 256;                    \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_put ## RND ## pixels16_l2_ ## MMX(halfH, src, halfH, 16,         \
                                         stride, 17);                   \
    ff_put ## RND ## mpeg4_qpel16_v_lowpass_ ## MMX(halfHV, halfH,      \
                                                    16, 16);            \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, halfH + 16, halfHV,       \
                                         stride, 16, 16);               \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc33_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[16 * 2 + 17 * 2];                                     \
    uint8_t * const halfH  = ((uint8_t*)half) + 256;                    \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_put ## RND ## pixels16_l2_ ## MMX(halfH, src + 1, halfH, 16,     \
                                         stride, 17);                   \
    ff_put ## RND ## mpeg4_qpel16_v_lowpass_ ## MMX(halfHV, halfH,      \
                                                    16, 16);            \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, halfH + 16, halfHV,       \
                                         stride, 16, 16);               \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc21_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[16 * 2 + 17 * 2];                                     \
    uint8_t * const halfH  = ((uint8_t*)half) + 256;                    \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_put ## RND ## mpeg4_qpel16_v_lowpass_ ## MMX(halfHV, halfH,      \
                                                    16, 16);            \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, halfH, halfHV,            \
                                         stride, 16, 16);               \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc23_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[16 * 2 + 17 * 2];                                     \
    uint8_t * const halfH  = ((uint8_t*)half) + 256;                    \
    uint8_t * const halfHV = ((uint8_t*)half);                          \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_put ## RND ## mpeg4_qpel16_v_lowpass_ ## MMX(halfHV, halfH,      \
                                                    16, 16);            \
    ff_ ## OPNAME ## pixels16_l2_ ## MMX(dst, halfH + 16, halfHV,       \
                                         stride, 16, 16);               \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc12_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[17 * 2];                                              \
    uint8_t * const halfH = ((uint8_t*)half);                           \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_put ## RND ## pixels16_l2_ ## MMX(halfH, src, halfH, 16,         \
                                         stride, 17);                   \
    ff_ ## OPNAME ## mpeg4_qpel16_v_lowpass_ ## MMX(dst, halfH,         \
                                                    stride, 16);        \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc32_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[17 * 2];                                              \
    uint8_t * const halfH = ((uint8_t*)half);                           \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_put ## RND ## pixels16_l2_ ## MMX(halfH, src + 1, halfH, 16,     \
                                         stride, 17);                   \
    ff_ ## OPNAME ## mpeg4_qpel16_v_lowpass_ ## MMX(dst, halfH,         \
                                                    stride, 16);        \
}                                                                       \
                                                                        \
static void OPNAME ## qpel16_mc22_ ## MMX(uint8_t *dst, uint8_t *src,   \
                                          int stride)                   \
{                                                                       \
    uint64_t half[17 * 2];                                              \
    uint8_t * const halfH = ((uint8_t*)half);                           \
    ff_put ## RND ## mpeg4_qpel16_h_lowpass_ ## MMX(halfH, src, 16,     \
                                                    stride, 17);        \
    ff_ ## OPNAME ## mpeg4_qpel16_v_lowpass_ ## MMX(dst, halfH,         \
                                                    stride, 16);        \
}

//QPEL_OP(put_,          ff_pw_16, _,        mmxext)
//QPEL_OP(avg_,          ff_pw_16, _,        mmxext)
//QPEL_OP(put_no_rnd_,   ff_pw_15, _no_rnd_, mmxext)

#define SET_QPEL_FUNCS(PFX, IDX, SIZE, CPU, PREFIX)                          \
    do {                                                                     \
    c->PFX ## _pixels_tab[IDX][ 0] = PREFIX ## PFX ## SIZE ## _mc00_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 1] = PREFIX ## PFX ## SIZE ## _mc10_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 2] = PREFIX ## PFX ## SIZE ## _mc20_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 3] = PREFIX ## PFX ## SIZE ## _mc30_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 4] = PREFIX ## PFX ## SIZE ## _mc01_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 5] = PREFIX ## PFX ## SIZE ## _mc11_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 6] = PREFIX ## PFX ## SIZE ## _mc21_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 7] = PREFIX ## PFX ## SIZE ## _mc31_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 8] = PREFIX ## PFX ## SIZE ## _mc02_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][ 9] = PREFIX ## PFX ## SIZE ## _mc12_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][10] = PREFIX ## PFX ## SIZE ## _mc22_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][11] = PREFIX ## PFX ## SIZE ## _mc32_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][12] = PREFIX ## PFX ## SIZE ## _mc03_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][13] = PREFIX ## PFX ## SIZE ## _mc13_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][14] = PREFIX ## PFX ## SIZE ## _mc23_ ## CPU; \
    c->PFX ## _pixels_tab[IDX][15] = PREFIX ## PFX ## SIZE ## _mc33_ ## CPU; \
    } while (0)



#define AV_TOSTRING(s) #s
#define S(s) AV_TOSTRING(s) //AV_STRINGIFY is too long

//DECLARE_ALIGNED(16, static const int16_t, fdct_one_corr)[8] = { X8(1) };
//DECLARE_ALIGNED(16, static const int16_t, fdct_tg_all_16)[24] = {
//    X8(13036),  // tg * (2<<16) + 0.5
//    X8(27146),  // tg * (2<<16) + 0.5
//    X8(-21746)  // tg * (2<<16) + 0.5
//};
//DECLARE_ALIGNED(16, static const int16_t, ocos_4_16)[8] = {
//    X8(23170)   //cos * (2<<15) + 0.5
//};


//
//static const struct
//{
// DECLARE_ALIGNED(16, const int16_t, tab_frw_01234567_sse2)[256];
//} tab_frw_01234567_sse2 =
//{{
////DECLARE_ALIGNED(16, static const int16_t, tab_frw_01234567_sse2)[] = {  // forward_dct coeff table
//#define TABLE_SSE2 C4,  C4,  C1,  C3, -C6, -C2, -C1, -C5, \
//                   C4,  C4,  C5,  C7,  C2,  C6,  C3, -C7, \
//                  -C4,  C4,  C7,  C3,  C6, -C2,  C7, -C5, \
//                   C4, -C4,  C5, -C1,  C2, -C6,  C3, -C1,
//// c1..c7 * cos(pi/4) * 2^15
//#define C1 22725
//#define C2 21407
//#define C3 19266
//#define C4 16384
//#define C5 12873
//#define C6 8867
//#define C7 4520
//TABLE_SSE2
//
//#undef C1
//#undef C2
//#undef C3
//#undef C4
//#undef C5
//#undef C6
//#undef C7
//#define C1 31521
//#define C2 29692
//#define C3 26722
//#define C4 22725
//#define C5 17855
//#define C6 12299
//#define C7 6270
//TABLE_SSE2
//
//#undef C1
//#undef C2
//#undef C3
//#undef C4
//#undef C5
//#undef C6
//#undef C7
//#define C1 29692
//#define C2 27969
//#define C3 25172
//#define C4 21407
//#define C5 16819
//#define C6 11585
//#define C7 5906
//TABLE_SSE2
//
//#undef C1
//#undef C2
//#undef C3
//#undef C4
//#undef C5
//#undef C6
//#undef C7
//#define C1 26722
//#define C2 25172
//#define C3 22654
//#define C4 19266
//#define C5 15137
//#define C6 10426
//#define C7 5315
//TABLE_SSE2
//
//#undef C1
//#undef C2
//#undef C3
//#undef C4
//#undef C5
//#undef C6
//#undef C7
//#define C1 22725
//#define C2 21407
//#define C3 19266
//#define C4 16384
//#define C5 12873
//#define C6 8867
//#define C7 4520
//TABLE_SSE2
//
//#undef C1
//#undef C2
//#undef C3
//#undef C4
//#undef C5
//#undef C6
//#undef C7
//#define C1 26722
//#define C2 25172
//#define C3 22654
//#define C4 19266
//#define C5 15137
//#define C6 10426
//#define C7 5315
//TABLE_SSE2
//
//#undef C1
//#undef C2
//#undef C3
//#undef C4
//#undef C5
//#undef C6
//#undef C7
//#define C1 29692
//#define C2 27969
//#define C3 25172
//#define C4 21407
//#define C5 16819
//#define C6 11585
//#define C7 5906
//TABLE_SSE2
//
//#undef C1
//#undef C2
//#undef C3
//#undef C4
//#undef C5
//#undef C6
//#undef C7
//#define C1 31521
//#define C2 29692
//#define C3 26722
//#define C4 22725
//#define C5 17855
//#define C6 12299
//#define C7 6270
//TABLE_SSE2
//}};

#define BITS_FRW_ACC   3 //; 2 or 3 for accuracy
#define SHIFT_FRW_COL  BITS_FRW_ACC
#define SHIFT_FRW_ROW  (BITS_FRW_ACC + 17 - 3)
#define RND_FRW_ROW    (1 << (SHIFT_FRW_ROW-1))



#    define XMM_CLOBBERS_ONLY(...)



void ff_fdct_sse2(int16_t *block)
{
	LOGD("ff_fdct_sse2 Called\n");

    DECLARE_ALIGNED(16, int64_t, align_tmp)[16];
    int16_t * const block1 = (int16_t*)align_tmp;

//    fdct_col_sse2(block, block1, 0);
//    fdct_row_sse2(block1, block);
}




av_cold void ff_dsputil_init_pix_mmx(DSPContext *c, AVCodecContext *avctx)
{
//	int mm_flags = av_get_cpu_flags();
int mm_flags = 0x10013db;//x86はこの値

    if (mm_flags & AV_CPU_FLAG_MMX) {
//        c->pix_abs[0][0] = sad16_mmx;
//        c->pix_abs[0][1] = sad16_x2_mmx;
//        c->pix_abs[0][2] = sad16_y2_mmx;
//        c->pix_abs[0][3] = sad16_xy2_mmx;
//        c->pix_abs[1][0] = sad8_mmx;
//        c->pix_abs[1][1] = sad8_x2_mmx;
//        c->pix_abs[1][2] = sad8_y2_mmx;
//        c->pix_abs[1][3] = sad8_xy2_mmx;

//        c->sad[0]= sad16_mmx;//落ちたのでやめた
//        c->sad[1]= sad8_mmx;
    }
    if ((mm_flags & AV_CPU_FLAG_SSE2) && !(mm_flags & AV_CPU_FLAG_3DNOW) && avctx->codec_id != AV_CODEC_ID_SNOW) {
//        c->sad[0]= sad16_sse2;
    }
}


av_cold void ff_dsputilenc_init_mmx(DSPContext *c, AVCodecContext *avctx)
{
	LOGD("ff_dsputilenc_init_mmx Called\n");
//    int mm_flags = av_get_cpu_flags();
	int mm_flags = 0x10013db;//x86はこの値
    int bit_depth = avctx->bits_per_raw_sample;

			c->get_pixels = get_pixels_8_c;

//#if HAVE_YASM
//    if (EXTERNAL_MMX(mm_flags)) {
//        if (bit_depth <= 8)//見当たらない
//            c->get_pixels = ff_get_pixels_mmx;
//        c->diff_pixels = ff_diff_pixels_mmx;
//        c->pix_sum = ff_pix_sum16_mmx;

//        c->pix_norm1 = ff_pix_norm1_mmx;//見当たらない
//    }
//    if (EXTERNAL_SSE2(mm_flags))
//        if (bit_depth <= 8)
//            c->get_pixels = ff_get_pixels_sse2;//見当たらない
//#endif /* HAVE_YASM */

//#if HAVE_INLINE_ASM
    if (mm_flags & AV_CPU_FLAG_MMX) {
        const int dct_algo = avctx->dct_algo;
        if (avctx->bits_per_raw_sample <= 8 &&
            (dct_algo==FF_DCT_AUTO || dct_algo==FF_DCT_MMX)) {
            if(mm_flags & AV_CPU_FLAG_SSE2){
//                c->fdct = ff_fdct_sse2;
            } else if (mm_flags & AV_CPU_FLAG_MMXEXT) {
//                c->fdct = ff_fdct_mmxext;
            }else{
//                c->fdct = ff_fdct_mmx;
            }
        }


//        c->diff_bytes= diff_bytes_mmx;//あかんか
//        c->sum_abs_dctelem= sum_abs_dctelem_mmx;//あかんか

//        c->sse[0] = sse16_mmx;//あかんか
//        c->sse[1] = sse8_mmx;//あかんか
//        c->vsad[4]= vsad_intra16_mmx;//あかんか

//        c->nsse[0] = nsse16_mmx;//あかんか
//        c->nsse[1] = nsse8_mmx;//あかんか
        if(!(avctx->flags & CODEC_FLAG_BITEXACT)){
//            c->vsad[0] = vsad16_mmx;//あかんか
        }

        if(!(avctx->flags & CODEC_FLAG_BITEXACT)){
//            c->try_8x8basis= try_8x8basis_mmx;//実装はあるっぽいけどめんどい
        }
//        c->add_8x8basis= add_8x8basis_mmx;//実装はあるっぽいけどめんどい

//        c->ssd_int8_vs_int16 = ssd_int8_vs_int16_mmx;//あかんか

        if (mm_flags & AV_CPU_FLAG_MMXEXT) {
//            c->sum_abs_dctelem = sum_abs_dctelem_mmxext;//何故かdefineできない
//            c->vsad[4]         = vsad_intra16_mmxext;//あかんか

            if(!(avctx->flags & CODEC_FLAG_BITEXACT)){
//                c->vsad[0] = vsad16_mmxext;//あかんか
            }

//            c->sub_hfyu_median_prediction = sub_hfyu_median_prediction_mmxext;//あかんか
        }

        if(mm_flags & AV_CPU_FLAG_SSE2){
//            c->sum_abs_dctelem= sum_abs_dctelem_sse2;//何故かdefineできない
        }

//#if HAVE_SSSE3_INLINE
        if(mm_flags & AV_CPU_FLAG_SSSE3){
            if(!(avctx->flags & CODEC_FLAG_BITEXACT)){
//                c->try_8x8basis= try_8x8basis_ssse3;//見当たらない
            }
//            c->add_8x8basis= add_8x8basis_ssse3;//見当たらない
//            c->sum_abs_dctelem= sum_abs_dctelem_ssse3;//何故かdefineできない
        }
//#endif

        if(mm_flags & AV_CPU_FLAG_3DNOW){
            if(!(avctx->flags & CODEC_FLAG_BITEXACT)){//3dnowはなし
//                c->try_8x8basis= try_8x8basis_3dnow;
            }
//            c->add_8x8basis= add_8x8basis_3dnow;
        }
    }
//#endif /* HAVE_INLINE_ASM */

//    if (EXTERNAL_MMX(mm_flags)) {
//        c->hadamard8_diff[0] = ff_hadamard8_diff16_mmx;//defineできない
//        c->hadamard8_diff[1] = ff_hadamard8_diff_mmx;

//        if (EXTERNAL_MMXEXT(mm_flags)) {
//            c->hadamard8_diff[0] = ff_hadamard8_diff16_mmxext;//defineできない
//            c->hadamard8_diff[1] = ff_hadamard8_diff_mmxext;
//        }

//        if (EXTERNAL_SSE2(mm_flags)) {
//            c->sse[0] = ff_sse16_sse2;//見当たらない

//#if HAVE_ALIGNED_STACK
//            c->hadamard8_diff[0] = ff_hadamard8_diff16_sse2;//見当たらない
//            c->hadamard8_diff[1] = ff_hadamard8_diff_sse2;
//#endif
//        }

//        if (EXTERNAL_SSSE3(mm_flags) ) {
//            c->hadamard8_diff[0] = ff_hadamard8_diff16_ssse3;//見当たらない
//            c->hadamard8_diff[1] = ff_hadamard8_diff_ssse3;
//        }
//    }
    LOGD("ff_dsputil_init_pix_mmx Before");
    ff_dsputil_init_pix_mmx(c, avctx);
}

av_cold void ff_dsputil_init_mmx(DSPContext *c, AVCodecContext *avctx)
{
	LOGD("ff_dsputil_init_mmx Called\n");
//    int mm_flags = av_get_cpu_flags();
	int mm_flags = 0x10013db;//x86はこの値

#if HAVE_7REGS && HAVE_INLINE_ASM
    if (mm_flags & AV_CPU_FLAG_CMOV)
        c->add_hfyu_median_prediction = add_hfyu_median_prediction_cmov;
#endif

    if (mm_flags & AV_CPU_FLAG_MMX) {
//#if HAVE_INLINE_ASM
        const int idct_algo = avctx->idct_algo;

        if (avctx->lowres == 0 && avctx->bits_per_raw_sample <= 8) {
            if (idct_algo == FF_IDCT_AUTO || idct_algo == FF_IDCT_SIMPLEMMX) {
//                c->idct_put              = ff_simple_idct_put_mmx;
//                c->idct_add              = ff_simple_idct_add_mmx;
//                c->idct                  = ff_simple_idct_mmx;
                c->idct_permutation_type = FF_SIMPLE_IDCT_PERM;
//#if CONFIG_GPL
            } else if (idct_algo == FF_IDCT_LIBMPEG2MMX) {
                if (mm_flags & AV_CPU_FLAG_MMX2) {
//                    c->idct_put = ff_libmpeg2mmx2_idct_put;//呼ばなあかんか
//                    c->idct_add = ff_libmpeg2mmx2_idct_add;//呼ばなあかんか
//                    c->idct     = ff_mmxext_idct;//呼ばなあかんか
                } else {
//                    c->idct_put = ff_libmpeg2mmx_idct_put;//呼ばなあかんか
//                    c->idct_add = ff_libmpeg2mmx_idct_add;//呼ばなあかんか
//                    c->idct     = ff_mmx_idct;//呼ばなあかんか
                }
                c->idct_permutation_type = FF_LIBMPEG2_IDCT_PERM;
//#endif


            }
//            else if (idct_algo == FF_IDCT_XVIDMMX) {
//                if (mm_flags & AV_CPU_FLAG_SSE2) {
//                    c->idct_put              = ff_idct_xvid_sse2_put;
//                    c->idct_add              = ff_idct_xvid_sse2_add;
//                    c->idct                  = ff_idct_xvid_sse2;
//                    c->idct_permutation_type = FF_SSE2_IDCT_PERM;
//                } else if (mm_flags & AV_CPU_FLAG_MMXEXT) {
//                    c->idct_put              = ff_idct_xvid_mmxext_put;
//                    c->idct_add              = ff_idct_xvid_mmxext_add;
//                    c->idct                  = ff_idct_xvid_mmxext;
//                } else {
//                    c->idct_put              = ff_idct_xvid_mmx_put;
//                    c->idct_add              = ff_idct_xvid_mmx_add;
//                    c->idct                  = ff_idct_xvid_mmx;
//                }
//            }
        }
//#endif /* HAVE_INLINE_ASM */

        dsputil_init_mmx(c, avctx, mm_flags);
    }

//    if (mm_flags & AV_CPU_FLAG_MMXEXT)
//        dsputil_init_mmxext(c, avctx, mm_flags);//あかんか

//    if (mm_flags & AV_CPU_FLAG_3DNOW)//ここ以外true
//        dsputil_init_3dnow(c, avctx, mm_flags);

//    if (mm_flags & AV_CPU_FLAG_SSE)
//        dsputil_init_sse(c, avctx, mm_flags);//呼ばなあかんか

//    if (mm_flags & AV_CPU_FLAG_SSE2)
//        dsputil_init_sse2(c, avctx, mm_flags);//あかんか

//    if (mm_flags & AV_CPU_FLAG_SSSE3)
//        dsputil_init_ssse3(c, avctx, mm_flags);

//    if (mm_flags & AV_CPU_FLAG_SSE4)
//        dsputil_init_sse4(c, avctx, mm_flags);

        ff_dsputilenc_init_mmx(c, avctx);
}

void ff_init_scantable_permutation(uint8_t *idct_permutation,
                                   int idct_permutation_type)
{
    int i;

    switch(idct_permutation_type){
    case FF_NO_IDCT_PERM:
        for(i=0; i<64; i++)
            idct_permutation[i]= i;
        break;
    case FF_LIBMPEG2_IDCT_PERM:
        for(i=0; i<64; i++)
            idct_permutation[i]= (i & 0x38) | ((i & 6) >> 1) | ((i & 1) << 2);
        break;
    case FF_SIMPLE_IDCT_PERM:
        for(i=0; i<64; i++)
            idct_permutation[i]= simple_mmx_permutation[i];
        break;
    case FF_TRANSPOSE_IDCT_PERM:
        for(i=0; i<64; i++)
            idct_permutation[i]= ((i&7)<<3) | (i>>3);
        break;
    case FF_PARTTRANS_IDCT_PERM:
        for(i=0; i<64; i++)
            idct_permutation[i]= (i&0x24) | ((i&3)<<3) | ((i>>3)&3);
        break;
    case FF_SSE2_IDCT_PERM:
        for(i=0; i<64; i++)
            idct_permutation[i]= (i&0x38) | idct_sse2_row_perm[i&7];
        break;
    default:
        LOGD( "Internal error, IDCT permutation not set\n");
    }
}


inline void row_fdct_8(int16_t *data)
{
//	LOGD("slow row_fdct Called\n");
  int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
  int tmp10, tmp11, tmp12, tmp13;
  int z1, z2, z3, z4, z5;
  int16_t *dataptr;
  int ctr;

  /* Pass 1: process rows. */
  /* Note results are scaled up by sqrt(8) compared to a true DCT; */
  /* furthermore, we scale the results by 2**PASS1_BITS. */

//  int index;
//  for(index = 64; index < 64;index++){
//  	LOGD("row_fdct A block[%d]:%d\n",index,data[index]);
//  }
  dataptr = data;
  for (ctr = DCTSIZE-1; ctr >= 0; ctr--) {
    tmp0 = dataptr[0] + dataptr[7];
    tmp7 = dataptr[0] - dataptr[7];
    tmp1 = dataptr[1] + dataptr[6];
    tmp6 = dataptr[1] - dataptr[6];
    tmp2 = dataptr[2] + dataptr[5];
    tmp5 = dataptr[2] - dataptr[5];
    tmp3 = dataptr[3] + dataptr[4];
    tmp4 = dataptr[3] - dataptr[4];

    /* Even part per LL&M figure 1 --- note that published figure is faulty;
     * rotator "sqrt(2)*c1" should be "sqrt(2)*c6".
     */

    tmp10 = tmp0 + tmp3;
    tmp13 = tmp0 - tmp3;
    tmp11 = tmp1 + tmp2;
    tmp12 = tmp1 - tmp2;

    dataptr[0] = (int16_t) ((tmp10 + tmp11) << PASS1_BITS);
    dataptr[4] = (int16_t) ((tmp10 - tmp11) << PASS1_BITS);

    z1 = MULTIPLY(tmp12 + tmp13, FIX_0_541196100);
    dataptr[2] = (int16_t) DESCALE(z1 + MULTIPLY(tmp13, FIX_0_765366865),
                                   CONST_BITS-PASS1_BITS);
    dataptr[6] = (int16_t) DESCALE(z1 + MULTIPLY(tmp12, - FIX_1_847759065),
                                   CONST_BITS-PASS1_BITS);

    /* Odd part per figure 8 --- note paper omits factor of sqrt(2).
     * cK represents cos(K*pi/16).
     * i0..i3 in the paper are tmp4..tmp7 here.
     */

    z1 = tmp4 + tmp7;
    z2 = tmp5 + tmp6;
    z3 = tmp4 + tmp6;
    z4 = tmp5 + tmp7;
    z5 = MULTIPLY(z3 + z4, FIX_1_175875602); /* sqrt(2) * c3 */

    tmp4 = MULTIPLY(tmp4, FIX_0_298631336); /* sqrt(2) * (-c1+c3+c5-c7) */
    tmp5 = MULTIPLY(tmp5, FIX_2_053119869); /* sqrt(2) * ( c1+c3-c5+c7) */
    tmp6 = MULTIPLY(tmp6, FIX_3_072711026); /* sqrt(2) * ( c1+c3+c5-c7) */
    tmp7 = MULTIPLY(tmp7, FIX_1_501321110); /* sqrt(2) * ( c1+c3-c5-c7) */
    z1 = MULTIPLY(z1, - FIX_0_899976223); /* sqrt(2) * (c7-c3) */
    z2 = MULTIPLY(z2, - FIX_2_562915447); /* sqrt(2) * (-c1-c3) */
    z3 = MULTIPLY(z3, - FIX_1_961570560); /* sqrt(2) * (-c3-c5) */
    z4 = MULTIPLY(z4, - FIX_0_390180644); /* sqrt(2) * (c5-c3) */

    z3 += z5;
    z4 += z5;

    dataptr[7] = (int16_t) DESCALE(tmp4 + z1 + z3, CONST_BITS-PASS1_BITS);
    dataptr[5] = (int16_t) DESCALE(tmp5 + z2 + z4, CONST_BITS-PASS1_BITS);
    dataptr[3] = (int16_t) DESCALE(tmp6 + z2 + z3, CONST_BITS-PASS1_BITS);
    dataptr[1] = (int16_t) DESCALE(tmp7 + z1 + z4, CONST_BITS-PASS1_BITS);

    dataptr += DCTSIZE;         /* advance pointer to next row */
  }
}

void FUNC(ff_jpeg_fdct_islow)(int16_t *data)
{
//	LOGD("ff_jpeg_fdct_islow Called\n");
  int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
  int tmp10, tmp11, tmp12, tmp13;
  int z1, z2, z3, z4, z5;
  int16_t *dataptr;
  int ctr;

//  int index;
//  for(index = 64; index < 64; index++){
//	  LOGD("FDCT_1_block[%d]:%d\n",index,data[index]);
//  }
//  LOGD("\n");

  FUNC(row_fdct)(data);

//  for(index = 64; index < 64; index++){
//	  LOGD("FDCT_2_block[%d]:%d\n",index,data[index]);
//  }
//  LOGD("\n");
  /* Pass 2: process columns.
   * We remove the PASS1_BITS scaling, but leave the results scaled up
   * by an overall factor of 8.
   */

  dataptr = data;
  for (ctr = DCTSIZE-1; ctr >= 0; ctr--) {
    tmp0 = dataptr[DCTSIZE*0] + dataptr[DCTSIZE*7];
    tmp7 = dataptr[DCTSIZE*0] - dataptr[DCTSIZE*7];
    tmp1 = dataptr[DCTSIZE*1] + dataptr[DCTSIZE*6];
    tmp6 = dataptr[DCTSIZE*1] - dataptr[DCTSIZE*6];
    tmp2 = dataptr[DCTSIZE*2] + dataptr[DCTSIZE*5];
    tmp5 = dataptr[DCTSIZE*2] - dataptr[DCTSIZE*5];
    tmp3 = dataptr[DCTSIZE*3] + dataptr[DCTSIZE*4];
    tmp4 = dataptr[DCTSIZE*3] - dataptr[DCTSIZE*4];

    /* Even part per LL&M figure 1 --- note that published figure is faulty;
     * rotator "sqrt(2)*c1" should be "sqrt(2)*c6".
     */

    tmp10 = tmp0 + tmp3;
    tmp13 = tmp0 - tmp3;
    tmp11 = tmp1 + tmp2;
    tmp12 = tmp1 - tmp2;

    dataptr[DCTSIZE*0] = DESCALE(tmp10 + tmp11, OUT_SHIFT);
    dataptr[DCTSIZE*4] = DESCALE(tmp10 - tmp11, OUT_SHIFT);

    z1 = MULTIPLY(tmp12 + tmp13, FIX_0_541196100);
    dataptr[DCTSIZE*2] = DESCALE(z1 + MULTIPLY(tmp13, FIX_0_765366865),
                                 CONST_BITS + OUT_SHIFT);
    dataptr[DCTSIZE*6] = DESCALE(z1 + MULTIPLY(tmp12, - FIX_1_847759065),
                                 CONST_BITS + OUT_SHIFT);

    /* Odd part per figure 8 --- note paper omits factor of sqrt(2).
     * cK represents cos(K*pi/16).
     * i0..i3 in the paper are tmp4..tmp7 here.
     */

    z1 = tmp4 + tmp7;
    z2 = tmp5 + tmp6;
    z3 = tmp4 + tmp6;
    z4 = tmp5 + tmp7;
    z5 = MULTIPLY(z3 + z4, FIX_1_175875602); /* sqrt(2) * c3 */

    tmp4 = MULTIPLY(tmp4, FIX_0_298631336); /* sqrt(2) * (-c1+c3+c5-c7) */
    tmp5 = MULTIPLY(tmp5, FIX_2_053119869); /* sqrt(2) * ( c1+c3-c5+c7) */
    tmp6 = MULTIPLY(tmp6, FIX_3_072711026); /* sqrt(2) * ( c1+c3+c5-c7) */
    tmp7 = MULTIPLY(tmp7, FIX_1_501321110); /* sqrt(2) * ( c1+c3-c5-c7) */
    z1 = MULTIPLY(z1, - FIX_0_899976223); /* sqrt(2) * (c7-c3) */
    z2 = MULTIPLY(z2, - FIX_2_562915447); /* sqrt(2) * (-c1-c3) */
    z3 = MULTIPLY(z3, - FIX_1_961570560); /* sqrt(2) * (-c3-c5) */
    z4 = MULTIPLY(z4, - FIX_0_390180644); /* sqrt(2) * (c5-c3) */

    z3 += z5;
    z4 += z5;

    dataptr[DCTSIZE*7] = DESCALE(tmp4 + z1 + z3, CONST_BITS + OUT_SHIFT);
    dataptr[DCTSIZE*5] = DESCALE(tmp5 + z2 + z4, CONST_BITS + OUT_SHIFT);
    dataptr[DCTSIZE*3] = DESCALE(tmp6 + z2 + z3, CONST_BITS + OUT_SHIFT);
    dataptr[DCTSIZE*1] = DESCALE(tmp7 + z1 + z4, CONST_BITS + OUT_SHIFT);

    dataptr++;                  /* advance pointer to next column */
  }
}



/*
 * The secret of DCT2-4-8 is really simple -- you do the usual 1-DCT
 * on the rows and then, instead of doing even and odd, part on the columns
 * you do even part two times.
 */
void FUNC(ff_fdct248_islow)(int16_t *data)
{
	LOGD("ff_fdct248_islow Called\n");
  int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
  int tmp10, tmp11, tmp12, tmp13;
  int z1;
  int16_t *dataptr;
  int ctr;

  FUNC(row_fdct)(data);

  /* Pass 2: process columns.
   * We remove the PASS1_BITS scaling, but leave the results scaled up
   * by an overall factor of 8.
   */

  dataptr = data;
  for (ctr = DCTSIZE-1; ctr >= 0; ctr--) {
     tmp0 = dataptr[DCTSIZE*0] + dataptr[DCTSIZE*1];
     tmp1 = dataptr[DCTSIZE*2] + dataptr[DCTSIZE*3];
     tmp2 = dataptr[DCTSIZE*4] + dataptr[DCTSIZE*5];
     tmp3 = dataptr[DCTSIZE*6] + dataptr[DCTSIZE*7];
     tmp4 = dataptr[DCTSIZE*0] - dataptr[DCTSIZE*1];
     tmp5 = dataptr[DCTSIZE*2] - dataptr[DCTSIZE*3];
     tmp6 = dataptr[DCTSIZE*4] - dataptr[DCTSIZE*5];
     tmp7 = dataptr[DCTSIZE*6] - dataptr[DCTSIZE*7];

     tmp10 = tmp0 + tmp3;
     tmp11 = tmp1 + tmp2;
     tmp12 = tmp1 - tmp2;
     tmp13 = tmp0 - tmp3;

     dataptr[DCTSIZE*0] = DESCALE(tmp10 + tmp11, OUT_SHIFT);
     dataptr[DCTSIZE*4] = DESCALE(tmp10 - tmp11, OUT_SHIFT);

     z1 = MULTIPLY(tmp12 + tmp13, FIX_0_541196100);
     dataptr[DCTSIZE*2] = DESCALE(z1 + MULTIPLY(tmp13, FIX_0_765366865),
                                  CONST_BITS+OUT_SHIFT);
     dataptr[DCTSIZE*6] = DESCALE(z1 + MULTIPLY(tmp12, - FIX_1_847759065),
                                  CONST_BITS+OUT_SHIFT);

     tmp10 = tmp4 + tmp7;
     tmp11 = tmp5 + tmp6;
     tmp12 = tmp5 - tmp6;
     tmp13 = tmp4 - tmp7;

     dataptr[DCTSIZE*1] = DESCALE(tmp10 + tmp11, OUT_SHIFT);
     dataptr[DCTSIZE*5] = DESCALE(tmp10 - tmp11, OUT_SHIFT);

     z1 = MULTIPLY(tmp12 + tmp13, FIX_0_541196100);
     dataptr[DCTSIZE*3] = DESCALE(z1 + MULTIPLY(tmp13, FIX_0_765366865),
                                  CONST_BITS + OUT_SHIFT);
     dataptr[DCTSIZE*7] = DESCALE(z1 + MULTIPLY(tmp12, - FIX_1_847759065),
                                  CONST_BITS + OUT_SHIFT);

     dataptr++;                 /* advance pointer to next column */
  }
}



static inline void row_fdct(int16_t * data){
		LOGD("jfdctstrow_fdct Called\n");
  int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
  int tmp10, tmp11, tmp12, tmp13;
  int z1, z2, z3, z4, z5, z11, z13;
  int16_t *dataptr;
  int ctr;

  /* Pass 1: process rows. */

  dataptr = data;
  for (ctr = DCTSIZE-1; ctr >= 0; ctr--) {
    tmp0 = dataptr[0] + dataptr[7];
    tmp7 = dataptr[0] - dataptr[7];
    tmp1 = dataptr[1] + dataptr[6];
    tmp6 = dataptr[1] - dataptr[6];
    tmp2 = dataptr[2] + dataptr[5];
    tmp5 = dataptr[2] - dataptr[5];
    tmp3 = dataptr[3] + dataptr[4];
    tmp4 = dataptr[3] - dataptr[4];

    /* Even part */

    tmp10 = tmp0 + tmp3;        /* phase 2 */
    tmp13 = tmp0 - tmp3;
    tmp11 = tmp1 + tmp2;
    tmp12 = tmp1 - tmp2;

    dataptr[0] = tmp10 + tmp11; /* phase 3 */
    dataptr[4] = tmp10 - tmp11;

    z1 = MULTIPLY(tmp12 + tmp13, FIX_0_707106781); /* c4 */
    dataptr[2] = tmp13 + z1;    /* phase 5 */
    dataptr[6] = tmp13 - z1;

    /* Odd part */

    tmp10 = tmp4 + tmp5;        /* phase 2 */
    tmp11 = tmp5 + tmp6;
    tmp12 = tmp6 + tmp7;

    /* The rotator is modified from fig 4-8 to avoid extra negations. */
    z5 = MULTIPLY(tmp10 - tmp12, FIX_0_382683433); /* c6 */
    z2 = MULTIPLY(tmp10, FIX_0_541196100) + z5;    /* c2-c6 */
    z4 = MULTIPLY(tmp12, FIX_1_306562965) + z5;    /* c2+c6 */
    z3 = MULTIPLY(tmp11, FIX_0_707106781);         /* c4 */

    z11 = tmp7 + z3;            /* phase 5 */
    z13 = tmp7 - z3;

    dataptr[5] = z13 + z2;      /* phase 6 */
    dataptr[3] = z13 - z2;
    dataptr[1] = z11 + z4;
    dataptr[7] = z11 - z4;

    dataptr += DCTSIZE;         /* advance pointer to next row */
  }
}

/*
 * Perform the forward DCT on one block of samples.
 */

void ff_fdct_ifast (int16_t * data)
{
	LOGD("ff_fdct_ifast Called\n");
  int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
  int tmp10, tmp11, tmp12, tmp13;
  int z1, z2, z3, z4, z5, z11, z13;
  int16_t *dataptr;
  int ctr;

  row_fdct(data);

  /* Pass 2: process columns. */

  dataptr = data;
  for (ctr = DCTSIZE-1; ctr >= 0; ctr--) {
    tmp0 = dataptr[DCTSIZE*0] + dataptr[DCTSIZE*7];
    tmp7 = dataptr[DCTSIZE*0] - dataptr[DCTSIZE*7];
    tmp1 = dataptr[DCTSIZE*1] + dataptr[DCTSIZE*6];
    tmp6 = dataptr[DCTSIZE*1] - dataptr[DCTSIZE*6];
    tmp2 = dataptr[DCTSIZE*2] + dataptr[DCTSIZE*5];
    tmp5 = dataptr[DCTSIZE*2] - dataptr[DCTSIZE*5];
    tmp3 = dataptr[DCTSIZE*3] + dataptr[DCTSIZE*4];
    tmp4 = dataptr[DCTSIZE*3] - dataptr[DCTSIZE*4];

    /* Even part */

    tmp10 = tmp0 + tmp3;        /* phase 2 */
    tmp13 = tmp0 - tmp3;
    tmp11 = tmp1 + tmp2;
    tmp12 = tmp1 - tmp2;

    dataptr[DCTSIZE*0] = tmp10 + tmp11; /* phase 3 */
    dataptr[DCTSIZE*4] = tmp10 - tmp11;

    z1 = MULTIPLY(tmp12 + tmp13, FIX_0_707106781); /* c4 */
    dataptr[DCTSIZE*2] = tmp13 + z1; /* phase 5 */
    dataptr[DCTSIZE*6] = tmp13 - z1;

    /* Odd part */

    tmp10 = tmp4 + tmp5;        /* phase 2 */
    tmp11 = tmp5 + tmp6;
    tmp12 = tmp6 + tmp7;

    /* The rotator is modified from fig 4-8 to avoid extra negations. */
    z5 = MULTIPLY(tmp10 - tmp12, FIX_0_382683433); /* c6 */
    z2 = MULTIPLY(tmp10, FIX_0_541196100) + z5; /* c2-c6 */
    z4 = MULTIPLY(tmp12, FIX_1_306562965) + z5; /* c2+c6 */
    z3 = MULTIPLY(tmp11, FIX_0_707106781); /* c4 */

    z11 = tmp7 + z3;            /* phase 5 */
    z13 = tmp7 - z3;

    dataptr[DCTSIZE*5] = z13 + z2; /* phase 6 */
    dataptr[DCTSIZE*3] = z13 - z2;
    dataptr[DCTSIZE*1] = z11 + z4;
    dataptr[DCTSIZE*7] = z11 - z4;

    dataptr++;                  /* advance pointer to next column */
  }
}


void ff_faandct248(int16_t *data)
{
    FLOAT tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
    FLOAT tmp10, tmp11, tmp12, tmp13;
    FLOAT temp[64];
    int i;


    row_fdct2(temp, data);

    for (i=0; i<8; i++) {
        tmp0 = temp[8*0 + i] + temp[8*1 + i];
        tmp1 = temp[8*2 + i] + temp[8*3 + i];
        tmp2 = temp[8*4 + i] + temp[8*5 + i];
        tmp3 = temp[8*6 + i] + temp[8*7 + i];
        tmp4 = temp[8*0 + i] - temp[8*1 + i];
        tmp5 = temp[8*2 + i] - temp[8*3 + i];
        tmp6 = temp[8*4 + i] - temp[8*5 + i];
        tmp7 = temp[8*6 + i] - temp[8*7 + i];

        tmp10 = tmp0 + tmp3;
        tmp11 = tmp1 + tmp2;
        tmp12 = tmp1 - tmp2;
        tmp13 = tmp0 - tmp3;

        data[8*0 + i] = lrintf(postscale[8*0 + i] * (tmp10 + tmp11));
        data[8*4 + i] = lrintf(postscale[8*4 + i] * (tmp10 - tmp11));

        tmp12 += tmp13;
        tmp12 *= A1;
        data[8*2 + i] = lrintf(postscale[8*2 + i] * (tmp13 + tmp12));
        data[8*6 + i] = lrintf(postscale[8*6 + i] * (tmp13 - tmp12));

        tmp10 = tmp4 + tmp7;
        tmp11 = tmp5 + tmp6;
        tmp12 = tmp5 - tmp6;
        tmp13 = tmp4 - tmp7;

        data[8*1 + i] = lrintf(postscale[8*0 + i] * (tmp10 + tmp11));
        data[8*5 + i] = lrintf(postscale[8*4 + i] * (tmp10 - tmp11));

        tmp12 += tmp13;
        tmp12 *= A1;
        data[8*3 + i] = lrintf(postscale[8*2 + i] * (tmp13 + tmp12));
        data[8*7 + i] = lrintf(postscale[8*6 + i] * (tmp13 - tmp12));
    }
}
/*
 * Perform the forward 2-4-8 DCT on one block of samples.
 */

void ff_fdct_ifast248 (int16_t * data)
{
	LOGD("ff_fdct_ifast248 Called\n");
  int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
  int tmp10, tmp11, tmp12, tmp13;
  int z1;
  int16_t *dataptr;
  int ctr;

  row_fdct(data);

  /* Pass 2: process columns. */

  dataptr = data;
  for (ctr = DCTSIZE-1; ctr >= 0; ctr--) {
    tmp0 = dataptr[DCTSIZE*0] + dataptr[DCTSIZE*1];
    tmp1 = dataptr[DCTSIZE*2] + dataptr[DCTSIZE*3];
    tmp2 = dataptr[DCTSIZE*4] + dataptr[DCTSIZE*5];
    tmp3 = dataptr[DCTSIZE*6] + dataptr[DCTSIZE*7];
    tmp4 = dataptr[DCTSIZE*0] - dataptr[DCTSIZE*1];
    tmp5 = dataptr[DCTSIZE*2] - dataptr[DCTSIZE*3];
    tmp6 = dataptr[DCTSIZE*4] - dataptr[DCTSIZE*5];
    tmp7 = dataptr[DCTSIZE*6] - dataptr[DCTSIZE*7];

    /* Even part */

    tmp10 = tmp0 + tmp3;
    tmp11 = tmp1 + tmp2;
    tmp12 = tmp1 - tmp2;
    tmp13 = tmp0 - tmp3;

    dataptr[DCTSIZE*0] = tmp10 + tmp11;
    dataptr[DCTSIZE*4] = tmp10 - tmp11;

    z1 = MULTIPLY(tmp12 + tmp13, FIX_0_707106781);
    dataptr[DCTSIZE*2] = tmp13 + z1;
    dataptr[DCTSIZE*6] = tmp13 - z1;

    tmp10 = tmp4 + tmp7;
    tmp11 = tmp5 + tmp6;
    tmp12 = tmp5 - tmp6;
    tmp13 = tmp4 - tmp7;

    dataptr[DCTSIZE*1] = tmp10 + tmp11;
    dataptr[DCTSIZE*5] = tmp10 - tmp11;

    z1 = MULTIPLY(tmp12 + tmp13, FIX_0_707106781);
    dataptr[DCTSIZE*3] = tmp13 + z1;
    dataptr[DCTSIZE*7] = tmp13 - z1;

    dataptr++;                        /* advance pointer to next column */
  }
}


inline void row_fdct2(FLOAT temp[64], int16_t *data)
{
	LOGD("row_fdct Called\n");
    FLOAT tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
    FLOAT tmp10, tmp11, tmp12, tmp13;
    FLOAT z2, z4, z11, z13;
//    FLOAT av_unused z5;
    int i;

    for (i=0; i<8*8; i+=8) {
        tmp0= data[0 + i] + data[7 + i];
        tmp7= data[0 + i] - data[7 + i];
        tmp1= data[1 + i] + data[6 + i];
        tmp6= data[1 + i] - data[6 + i];
        tmp2= data[2 + i] + data[5 + i];
        tmp5= data[2 + i] - data[5 + i];
        tmp3= data[3 + i] + data[4 + i];
        tmp4= data[3 + i] - data[4 + i];

        tmp10= tmp0 + tmp3;
        tmp13= tmp0 - tmp3;
        tmp11= tmp1 + tmp2;
        tmp12= tmp1 - tmp2;

        temp[0 + i]= tmp10 + tmp11;
        temp[4 + i]= tmp10 - tmp11;

        tmp12 += tmp13;
        tmp12 *= A1;
        temp[2 + i]= tmp13 + tmp12;
        temp[6 + i]= tmp13 - tmp12;

        tmp4 += tmp5;
        tmp5 += tmp6;
        tmp6 += tmp7;

#if 0
        z5= (tmp4 - tmp6) * A5;
        z2= tmp4*A2 + z5;
        z4= tmp6*A4 + z5;
#else
        z2= tmp4*(A2+A5) - tmp6*A5;
        z4= tmp6*(A4-A5) + tmp4*A5;
#endif
        tmp5*=A1;

        z11= tmp7 + tmp5;
        z13= tmp7 - tmp5;

        temp[5 + i]= z13 + z2;
        temp[3 + i]= z13 - z2;
        temp[1 + i]= z11 + z4;
        temp[7 + i]= z11 - z4;
    }
}

void ff_faandct(int16_t *data)
{
	LOGD("ff_faandct Called\n");
	float tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
	float tmp10, tmp11, tmp12, tmp13;
	float z2, z4, z11, z13;
//	float av_unused z5;
	float temp[64];
    int i;

    row_fdct2(temp, data);

    for (i=0; i<8; i++) {
        tmp0= temp[8*0 + i] + temp[8*7 + i];
        tmp7= temp[8*0 + i] - temp[8*7 + i];
        tmp1= temp[8*1 + i] + temp[8*6 + i];
        tmp6= temp[8*1 + i] - temp[8*6 + i];
        tmp2= temp[8*2 + i] + temp[8*5 + i];
        tmp5= temp[8*2 + i] - temp[8*5 + i];
        tmp3= temp[8*3 + i] + temp[8*4 + i];
        tmp4= temp[8*3 + i] - temp[8*4 + i];

        tmp10= tmp0 + tmp3;
        tmp13= tmp0 - tmp3;
        tmp11= tmp1 + tmp2;
        tmp12= tmp1 - tmp2;

        data[8*0 + i]= lrintf(postscale[8*0 + i] * (tmp10 + tmp11));
        data[8*4 + i]= lrintf(postscale[8*4 + i] * (tmp10 - tmp11));

        tmp12 += tmp13;
        tmp12 *= A1;
        data[8*2 + i]= lrintf(postscale[8*2 + i] * (tmp13 + tmp12));
        data[8*6 + i]= lrintf(postscale[8*6 + i] * (tmp13 - tmp12));

        tmp4 += tmp5;
        tmp5 += tmp6;
        tmp6 += tmp7;

#if 0
        z5= (tmp4 - tmp6) * A5;
        z2= tmp4*A2 + z5;
        z4= tmp6*A4 + z5;
#else
        z2= tmp4*(A2+A5) - tmp6*A5;
        z4= tmp6*(A4-A5) + tmp4*A5;
#endif
        tmp5*=A1;

        z11= tmp7 + tmp5;
        z13= tmp7 - tmp5;

        data[8*5 + i]= lrintf(postscale[8*5 + i] * (z13 + z2));
        data[8*3 + i]= lrintf(postscale[8*3 + i] * (z13 - z2));
        data[8*1 + i]= lrintf(postscale[8*1 + i] * (z11 + z4));
        data[8*7 + i]= lrintf(postscale[8*7 + i] * (z11 - z4));
    }
}


typedef int DWTELEM;
#define DWT_97 0
#define DWT_53 1
#define W_AM 3
#define W_AO 0
#define W_AS 1

#define W_BM 1
#define W_BO 8
#define W_BS 4

#define W_CM 1
#define W_CO 0
#define W_CS 0

#define W_DM 3
#define W_DO 4
#define W_DS 3


static inline int mirror(int v, int m)
{
    while ((unsigned)v > (unsigned)m) {
        v = -v;
        if (v < 0)
            v += 2 * m;
    }
    return v;
}

static void vertical_decompose53iH0(DWTELEM *b0, DWTELEM *b1, DWTELEM *b2,
                                    int width)
{
    int i;

    for (i = 0; i < width; i++)
        b1[i] -= (b0[i] + b2[i]) >> 1;
}

static void vertical_decompose53iL0(DWTELEM *b0, DWTELEM *b1, DWTELEM *b2,
                                    int width)
{
    int i;

    for (i = 0; i < width; i++)
        b1[i] += (b0[i] + b2[i] + 2) >> 2;
}
static void vertical_decompose97iH0(DWTELEM *b0, DWTELEM *b1, DWTELEM *b2,
                                    int width)
{
    int i;

    for (i = 0; i < width; i++)
        b1[i] -= (W_AM * (b0[i] + b2[i]) + W_AO) >> W_AS;
}

static void vertical_decompose97iH1(DWTELEM *b0, DWTELEM *b1, DWTELEM *b2,
                                    int width)
{
    int i;

    for (i = 0; i < width; i++)
        b1[i] += (W_CM * (b0[i] + b2[i]) + W_CO) >> W_CS;
}

static void vertical_decompose97iL0(DWTELEM *b0, DWTELEM *b1, DWTELEM *b2,
                                    int width)
{
    int i;

    for (i = 0; i < width; i++)
        b1[i] = (16 * 4 * b1[i] - 4 * (b0[i] + b2[i]) + W_BO * 5 + (5 << 27)) /
                (5 * 16) - (1 << 23);
}

static void vertical_decompose97iL1(DWTELEM *b0, DWTELEM *b1, DWTELEM *b2,
                                    int width)
{
    int i;

    for (i = 0; i < width; i++)
        b1[i] += (W_DM * (b0[i] + b2[i]) + W_DO) >> W_DS;
}
static inline void lift(DWTELEM *dst, DWTELEM *src, DWTELEM *ref,
                                  int dst_step, int src_step, int ref_step,
                                  int width, int mul, int add, int shift,
                                  int highpass, int inverse)
{
    const int mirror_left  = !highpass;
    const int mirror_right = (width & 1) ^ highpass;
    const int w            = (width >> 1) - 1 + (highpass & width);
    int i;

#define LIFT(src, ref, inv) ((src) + ((inv) ? -(ref) : +(ref)))
    if (mirror_left) {
        dst[0] = LIFT(src[0], ((mul * 2 * ref[0] + add) >> shift), inverse);
        dst   += dst_step;
        src   += src_step;
    }

    for (i = 0; i < w; i++)
        dst[i * dst_step] = LIFT(src[i * src_step],
                                 ((mul * (ref[i * ref_step] +
                                          ref[(i + 1) * ref_step]) +
                                   add) >> shift),
                                 inverse);

    if (mirror_right)
        dst[w * dst_step] = LIFT(src[w * src_step],
                                 ((mul * 2 * ref[w * ref_step] + add) >> shift),
                                 inverse);
}

static inline void liftS(DWTELEM *dst, DWTELEM *src, DWTELEM *ref,
                                   int dst_step, int src_step, int ref_step,
                                   int width, int mul, int add, int shift,
                                   int highpass, int inverse)
{
    const int mirror_left  = !highpass;
    const int mirror_right = (width & 1) ^ highpass;
    const int w            = (width >> 1) - 1 + (highpass & width);
    int i;

//    av_assert1(shift == 4);
#define LIFTS(src, ref, inv)                                            \
    ((inv) ? (src) + (((ref) + 4 * (src)) >> shift)                     \
           : -((-16 * (src) + (ref) + add /                             \
                4 + 1 + (5 << 25)) / (5 * 4) - (1 << 23)))
    if (mirror_left) {
        dst[0] = LIFTS(src[0], mul * 2 * ref[0] + add, inverse);
        dst   += dst_step;
        src   += src_step;
    }

    for (i = 0; i < w; i++)
        dst[i * dst_step] = LIFTS(src[i * src_step],
                                  mul * (ref[i * ref_step] +
                                         ref[(i + 1) * ref_step]) + add,
                                  inverse);

    if (mirror_right)
        dst[w * dst_step] = LIFTS(src[w * src_step],
                                  mul * 2 * ref[w * ref_step] + add,
                                  inverse);
}
static void horizontal_decompose53i(DWTELEM *b, DWTELEM *temp, int width)
{
    const int width2 = width >> 1;
    int x;
    const int w2 = (width + 1) >> 1;

    for (x = 0; x < width2; x++) {
        temp[x]      = b[2 * x];
        temp[x + w2] = b[2 * x + 1];
    }
    if (width & 1)
        temp[x] = b[2 * x];
    lift(b + w2, temp + w2, temp,   1, 1, 1, width, -1, 0, 1, 1, 0);
    lift(b,      temp,      b + w2, 1, 1, 1, width,  1, 2, 2, 0, 0);

}

static void horizontal_decompose97i(DWTELEM *b, DWTELEM *temp, int width)
{
    const int w2 = (width + 1) >> 1;

    lift(temp + w2, b + 1, b,         1, 2, 2, width, W_AM, W_AO, W_AS, 1, 1);
    liftS(temp,     b,     temp + w2, 1, 2, 1, width, W_BM, W_BO, W_BS, 0, 0);
    lift(b + w2, temp + w2, temp,     1, 1, 1, width, W_CM, W_CO, W_CS, 1, 0);
    lift(b,      temp,      b + w2,   1, 1, 1, width, W_DM, W_DO, W_DS, 0, 0);
}
static void spatial_decompose53i(DWTELEM *buffer, DWTELEM *temp,
                                 int width, int height, int stride)
{
    int y;
    DWTELEM *b0 = buffer + mirror(-2 - 1, height - 1) * stride;
    DWTELEM *b1 = buffer + mirror(-2,     height - 1) * stride;

    for (y = -2; y < height; y += 2) {
        DWTELEM *b2 = buffer + mirror(y + 1, height - 1) * stride;
        DWTELEM *b3 = buffer + mirror(y + 2, height - 1) * stride;

        if (y + 1 < (unsigned)height)
            horizontal_decompose53i(b2, temp, width);
        if (y + 2 < (unsigned)height)
            horizontal_decompose53i(b3, temp, width);

        if (y + 1 < (unsigned)height)
            vertical_decompose53iH0(b1, b2, b3, width);
        if (y + 0 < (unsigned)height)
            vertical_decompose53iL0(b0, b1, b2, width);

        b0 = b2;
        b1 = b3;
    }
}
static void spatial_decompose97i(DWTELEM *buffer, DWTELEM *temp,
                                 int width, int height, int stride)
{
    int y;
    DWTELEM *b0 = buffer + mirror(-4 - 1, height - 1) * stride;
    DWTELEM *b1 = buffer + mirror(-4,     height - 1) * stride;
    DWTELEM *b2 = buffer + mirror(-4 + 1, height - 1) * stride;
    DWTELEM *b3 = buffer + mirror(-4 + 2, height - 1) * stride;

    for (y = -4; y < height; y += 2) {
        DWTELEM *b4 = buffer + mirror(y + 3, height - 1) * stride;
        DWTELEM *b5 = buffer + mirror(y + 4, height - 1) * stride;

        if (y + 3 < (unsigned)height)
            horizontal_decompose97i(b4, temp, width);
        if (y + 4 < (unsigned)height)
            horizontal_decompose97i(b5, temp, width);

        if (y + 3 < (unsigned)height)
            vertical_decompose97iH0(b3, b4, b5, width);
        if (y + 2 < (unsigned)height)
            vertical_decompose97iL0(b2, b3, b4, width);
        if (y + 1 < (unsigned)height)
            vertical_decompose97iH1(b1, b2, b3, width);
        if (y + 0 < (unsigned)height)
            vertical_decompose97iL1(b0, b1, b2, width);

        b0 = b2;
        b1 = b3;
        b2 = b4;
        b3 = b5;
    }
}
void ff_spatial_dwt(DWTELEM *buffer, DWTELEM *temp, int width, int height,
                    int stride, int type, int decomposition_count)
{
    int level;

    for (level = 0; level < decomposition_count; level++) {
        switch (type) {
        case DWT_97:
            spatial_decompose97i(buffer, temp,
                                 width >> level, height >> level,
                                 stride << level);
            break;
        case DWT_53:
            spatial_decompose53i(buffer, temp,
                                 width >> level, height >> level,
                                 stride << level);
            break;
        }
    }
}

static inline int w_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size,
                      int w, int h, int type)
{
    int s, i, j;
    const int dec_count = w == 8 ? 3 : 4;
    int tmp[32 * 32], tmp2[32];
    int level, ori;
    static const int scale[2][2][4][4] = {
        {
            { // 9/7 8x8 dec=3
                { 268, 239, 239, 213 },
                { 0,   224, 224, 152 },
                { 0,   135, 135, 110 },
            },
            { // 9/7 16x16 or 32x32 dec=4
                { 344, 310, 310, 280 },
                { 0,   320, 320, 228 },
                { 0,   175, 175, 136 },
                { 0,   129, 129, 102 },
            }
        },
        {
            { // 5/3 8x8 dec=3
                { 275, 245, 245, 218 },
                { 0,   230, 230, 156 },
                { 0,   138, 138, 113 },
            },
            { // 5/3 16x16 or 32x32 dec=4
                { 352, 317, 317, 286 },
                { 0,   328, 328, 233 },
                { 0,   180, 180, 140 },
                { 0,   132, 132, 105 },
            }
        }
    };

    for (i = 0; i < h; i++) {
        for (j = 0; j < w; j += 4) {
            tmp[32 * i + j + 0] = (pix1[j + 0] - pix2[j + 0]) << 4;
            tmp[32 * i + j + 1] = (pix1[j + 1] - pix2[j + 1]) << 4;
            tmp[32 * i + j + 2] = (pix1[j + 2] - pix2[j + 2]) << 4;
            tmp[32 * i + j + 3] = (pix1[j + 3] - pix2[j + 3]) << 4;
        }
        pix1 += line_size;
        pix2 += line_size;
    }

    ff_spatial_dwt(tmp, tmp2, w, h, 32, type, dec_count);

    s = 0;
//    av_assert1(w == h);
    for (level = 0; level < dec_count; level++)
        for (ori = level ? 1 : 0; ori < 4; ori++) {
            int size   = w >> (dec_count - level);
            int sx     = (ori & 1) ? size : 0;
            int stride = 32 << (dec_count - level);
            int sy     = (ori & 2) ? stride >> 1 : 0;

            for (i = 0; i < size; i++)
                for (j = 0; j < size; j++) {
                    int v = tmp[sx + sy + i * stride + j] *
                            scale[type][dec_count - 3][level][ori];
                    s += FFABS(v);
                }
        }
//    av_assert1(s >= 0);
    return s >> 9;
}
static int w53_8_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
    return w_c(v, pix1, pix2, line_size, 8, h, 1);
}
static int w53_16_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
    return w_c(v, pix1, pix2, line_size, 16, h, 1);
}

static int w97_8_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
    return w_c(v, pix1, pix2, line_size, 8, h, 0);
}
static int w97_16_c(void *v, uint8_t *pix1, uint8_t *pix2, int line_size, int h)
{
    return w_c(v, pix1, pix2, line_size, 16, h, 0);
}

void ff_dsputil_init_dwt(DSPContext *c)
{
    c->w53[0] = w53_16_c;
    c->w53[1] = w53_8_c;
    c->w97[0] = w97_16_c;
    c->w97[1] = w97_8_c;
}

int ff_check_alignment(void){
    static int did_fail=0;
    LOCAL_ALIGNED_16(int, aligned, [4]);

    if((intptr_t)aligned & 15){
        if(!did_fail){
#if HAVE_MMX || HAVE_ALTIVEC
            printf("Compiler did not align stack variables. Libavcodec has been miscompiled\n"
                "and may be very slow or crash. This is not a bug in libavcodec,\n"
                "but in the compiler. You may try recompiling using gcc >= 4.2.\n"
                "Do not report crashes to FFmpeg developers.\n");
#endif
            did_fail=1;
        }
        return -1;
    }
    return 0;
}

av_cold void ff_dsputil_init(DSPContext* c, AVCodecContext *avctx)
{
	LOGD("ff_dsputil_init Called\n");
    ff_check_alignment();
////#if CONFIG_ENCODERS
    	if (avctx->bits_per_raw_sample == 10) {//avctx->bits_per_raw_sampleは0でくる
//           c->fdct    = ff_jpeg_fdct_islow_10;
//           c->fdct248 = ff_fdct248_islow_10;
       } else {
        if(avctx->dct_algo==FF_DCT_FASTINT) {
            c->fdct    = ff_fdct_ifast;
            c->fdct248 = ff_fdct_ifast248;
        }
        else if(avctx->dct_algo==FF_DCT_FAAN) {
            c->fdct    = ff_faandct;
            c->fdct248 = ff_faandct248;
        }
        else {
            c->fdct    = ff_jpeg_fdct_islow_8; //slow/accurate/default
            c->fdct248 = ff_fdct248_islow_8;
        }
    }
    	//#endif //CONFIG_ENCODERS

    if(avctx->lowres==1){
        c->idct_put= ff_jref_idct4_put;
        c->idct_add= ff_jref_idct4_add;
        c->idct    = ff_j_rev_dct4;
        c->idct_permutation_type= FF_NO_IDCT_PERM;
    }else if(avctx->lowres==2){
        c->idct_put= ff_jref_idct2_put;
        c->idct_add= ff_jref_idct2_add;
        c->idct    = ff_j_rev_dct2;
        c->idct_permutation_type= FF_NO_IDCT_PERM;
    }else if(avctx->lowres==3){
        c->idct_put= ff_jref_idct1_put;
        c->idct_add= ff_jref_idct1_add;
        c->idct    = ff_j_rev_dct1;
        c->idct_permutation_type= FF_NO_IDCT_PERM;
    }else{
        if(avctx->idct_algo==FF_IDCT_INT){
            c->idct_put= ff_jref_idct_put;
            c->idct_add= ff_jref_idct_add;
            c->idct    = ff_j_rev_dct;
            c->idct_permutation_type= FF_LIBMPEG2_IDCT_PERM;
        }else if(avctx->idct_algo==FF_IDCT_FAAN){
            c->idct_put= ff_faanidct_put;
            c->idct_add= ff_faanidct_add;
            c->idct    = ff_faanidct;
            c->idct_permutation_type= FF_NO_IDCT_PERM;
        }else{ //accurate/default
            c->idct_put = ff_simple_idct_put_8;
            c->idct_add = ff_simple_idct_add_8;
            c->idct     = ff_simple_idct_8;
            c->idct_permutation_type= FF_NO_IDCT_PERM;
        }
    }

    c->diff_pixels = diff_pixels_c;
    c->put_pixels_clamped = put_pixels_clamped_c;
    c->put_signed_pixels_clamped = put_signed_pixels_clamped_c;
    c->add_pixels_clamped = add_pixels_clamped_c;
    c->sum_abs_dctelem = sum_abs_dctelem_c;
    c->gmc1 = gmc1_c;
    c->gmc = ff_gmc_c;
	LOGD("Before ff_dsputil_init PIX:%d\n",c->pix_sum);
    c->pix_sum = pix_sum_c;
    c->pix_norm1 = pix_norm1_c;
	LOGD("SET ff_dsputil_init DSP :%d pix_sum:%d\n",c,c->pix_sum);

    c->fill_block_tab[0] = fill_block16_c;
    c->fill_block_tab[1] = fill_block8_c;

    /* TODO [0] 16  [1] 8 */
    c->pix_abs[0][0] = pix_abs16_c;
    c->pix_abs[0][1] = pix_abs16_x2_c;
    c->pix_abs[0][2] = pix_abs16_y2_c;
    c->pix_abs[0][3] = pix_abs16_xy2_c;
    c->pix_abs[1][0] = pix_abs8_c;
    c->pix_abs[1][1] = pix_abs8_x2_c;
    c->pix_abs[1][2] = pix_abs8_y2_c;
    c->pix_abs[1][3] = pix_abs8_xy2_c;

    c->put_tpel_pixels_tab[ 0] = put_tpel_pixels_mc00_c;
    c->put_tpel_pixels_tab[ 1] = put_tpel_pixels_mc10_c;
    c->put_tpel_pixels_tab[ 2] = put_tpel_pixels_mc20_c;
    c->put_tpel_pixels_tab[ 4] = put_tpel_pixels_mc01_c;
    c->put_tpel_pixels_tab[ 5] = put_tpel_pixels_mc11_c;
    c->put_tpel_pixels_tab[ 6] = put_tpel_pixels_mc21_c;
    c->put_tpel_pixels_tab[ 8] = put_tpel_pixels_mc02_c;
    c->put_tpel_pixels_tab[ 9] = put_tpel_pixels_mc12_c;
    c->put_tpel_pixels_tab[10] = put_tpel_pixels_mc22_c;

    c->avg_tpel_pixels_tab[ 0] = avg_tpel_pixels_mc00_c;
    c->avg_tpel_pixels_tab[ 1] = avg_tpel_pixels_mc10_c;
    c->avg_tpel_pixels_tab[ 2] = avg_tpel_pixels_mc20_c;
    c->avg_tpel_pixels_tab[ 4] = avg_tpel_pixels_mc01_c;
    c->avg_tpel_pixels_tab[ 5] = avg_tpel_pixels_mc11_c;
    c->avg_tpel_pixels_tab[ 6] = avg_tpel_pixels_mc21_c;
    c->avg_tpel_pixels_tab[ 8] = avg_tpel_pixels_mc02_c;
    c->avg_tpel_pixels_tab[ 9] = avg_tpel_pixels_mc12_c;
    c->avg_tpel_pixels_tab[10] = avg_tpel_pixels_mc22_c;

#define dspfunc(PFX, IDX, NUM) \
    c->PFX ## _pixels_tab[IDX][ 0] = PFX ## NUM ## _mc00_c; \
    c->PFX ## _pixels_tab[IDX][ 1] = PFX ## NUM ## _mc10_c; \
    c->PFX ## _pixels_tab[IDX][ 2] = PFX ## NUM ## _mc20_c; \
    c->PFX ## _pixels_tab[IDX][ 3] = PFX ## NUM ## _mc30_c; \
    c->PFX ## _pixels_tab[IDX][ 4] = PFX ## NUM ## _mc01_c; \
    c->PFX ## _pixels_tab[IDX][ 5] = PFX ## NUM ## _mc11_c; \
    c->PFX ## _pixels_tab[IDX][ 6] = PFX ## NUM ## _mc21_c; \
    c->PFX ## _pixels_tab[IDX][ 7] = PFX ## NUM ## _mc31_c; \
    c->PFX ## _pixels_tab[IDX][ 8] = PFX ## NUM ## _mc02_c; \
    c->PFX ## _pixels_tab[IDX][ 9] = PFX ## NUM ## _mc12_c; \
    c->PFX ## _pixels_tab[IDX][10] = PFX ## NUM ## _mc22_c; \
    c->PFX ## _pixels_tab[IDX][11] = PFX ## NUM ## _mc32_c; \
    c->PFX ## _pixels_tab[IDX][12] = PFX ## NUM ## _mc03_c; \
    c->PFX ## _pixels_tab[IDX][13] = PFX ## NUM ## _mc13_c; \
    c->PFX ## _pixels_tab[IDX][14] = PFX ## NUM ## _mc23_c; \
    c->PFX ## _pixels_tab[IDX][15] = PFX ## NUM ## _mc33_c

    dspfunc(put_qpel, 0, 16);
    dspfunc(put_no_rnd_qpel, 0, 16);

    dspfunc(avg_qpel, 0, 16);
    /* dspfunc(avg_no_rnd_qpel, 0, 16); */

    dspfunc(put_qpel, 1, 8);
    dspfunc(put_no_rnd_qpel, 1, 8);

    dspfunc(avg_qpel, 1, 8);
    /* dspfunc(avg_no_rnd_qpel, 1, 8); */

#undef dspfunc

    c->put_mspel_pixels_tab[0]= ff_put_pixels8x8_c;
    c->put_mspel_pixels_tab[1]= put_mspel8_mc10_c;
    c->put_mspel_pixels_tab[2]= put_mspel8_mc20_c;
    c->put_mspel_pixels_tab[3]= put_mspel8_mc30_c;
    c->put_mspel_pixels_tab[4]= put_mspel8_mc02_c;
    c->put_mspel_pixels_tab[5]= put_mspel8_mc12_c;
    c->put_mspel_pixels_tab[6]= put_mspel8_mc22_c;
    c->put_mspel_pixels_tab[7]= put_mspel8_mc32_c;

#define SET_CMP_FUNC(name) \
    c->name[0]= name ## 16_c;\
    c->name[1]= name ## 8x8_c;

    SET_CMP_FUNC(hadamard8_diff)
    c->hadamard8_diff[4]= hadamard8_intra16_c;
    c->hadamard8_diff[5]= hadamard8_intra8x8_c;
    SET_CMP_FUNC(dct_sad)
    SET_CMP_FUNC(dct_max)
//#if CONFIG_GPL
//    SET_CMP_FUNC(dct264_sad)//オリジナルではこれをやっている
//#endif
    c->sad[0]= pix_abs16_c;
    c->sad[1]= pix_abs8_c;
    c->sse[0]= sse16_c;
    c->sse[1]= sse8_c;
    c->sse[2]= sse4_c;
    SET_CMP_FUNC(quant_psnr)
    SET_CMP_FUNC(rd)
    SET_CMP_FUNC(bit)
    c->vsad[0]= vsad16_c;
    c->vsad[4]= vsad_intra16_c;
    c->vsad[5]= vsad_intra8_c;
    c->vsse[0]= vsse16_c;
    c->vsse[4]= vsse_intra16_c;
    c->vsse[5]= vsse_intra8_c;
    c->nsse[0]= nsse16_c;
    c->nsse[1]= nsse8_c;

    ff_dsputil_init_dwt(c);

    c->ssd_int8_vs_int16 = ssd_int8_vs_int16_c;

    c->add_bytes= add_bytes_c;
    c->diff_bytes= diff_bytes_c;
    c->add_hfyu_median_prediction= add_hfyu_median_prediction_c;
    c->sub_hfyu_median_prediction= sub_hfyu_median_prediction_c;
    c->add_hfyu_left_prediction  = add_hfyu_left_prediction_c;
    c->add_hfyu_left_prediction_bgr32 = add_hfyu_left_prediction_bgr32_c;
    c->bswap_buf= bswap_buf;
    c->bswap16_buf = bswap16_buf;

        c->h263_h_loop_filter= h263_h_loop_filter_c;
        c->h263_v_loop_filter= h263_v_loop_filter_c;

    c->h261_loop_filter= h261_loop_filter_c;

    c->try_8x8basis= try_8x8basis_c;
    c->add_8x8basis= add_8x8basis_c;

    c->vector_clipf = vector_clipf_c;
    c->scalarproduct_int16 = scalarproduct_int16_c;
    c->scalarproduct_and_madd_int16 = scalarproduct_and_madd_int16_c;
    c->apply_window_int16 = apply_window_int16_c;
    c->vector_clip_int32 = vector_clip_int32_c;

    c->shrink[0]= av_image_copy_plane;
    c->shrink[1]= ff_shrink22;
    c->shrink[2]= ff_shrink44;
    c->shrink[3]= ff_shrink88;

    c->add_pixels8 = add_pixels8_c;

#define hpel_funcs(prefix, idx, num) \
    c->prefix ## _pixels_tab idx [0] = prefix ## _pixels ## num ## _8_c; \
    c->prefix ## _pixels_tab idx [1] = prefix ## _pixels ## num ## _x2_8_c; \
    c->prefix ## _pixels_tab idx [2] = prefix ## _pixels ## num ## _y2_8_c; \
    c->prefix ## _pixels_tab idx [3] = prefix ## _pixels ## num ## _xy2_8_c

    hpel_funcs(put, [0], 16);
    hpel_funcs(put, [1],  8);
    hpel_funcs(put, [2],  4);
    hpel_funcs(put, [3],  2);
    hpel_funcs(put_no_rnd, [0], 16);
    hpel_funcs(put_no_rnd, [1],  8);
    hpel_funcs(avg, [0], 16);
    hpel_funcs(avg, [1],  8);
    hpel_funcs(avg, [2],  4);
    hpel_funcs(avg, [3],  2);
    hpel_funcs(avg_no_rnd, , 16);


#undef FUNC
#undef FUNCC
#define FUNC(f, depth) f ## _ ## depth
#define FUNCC(f, depth) f ## _ ## depth ## _c
    LOGD("SET_STANDARD_DRAW_EDGES\n");
    c->draw_edges                    = FUNCC(draw_edges, 8);
    c->clear_block                   = FUNCC(clear_block, 8);
    c->clear_blocks                  = FUNCC(clear_blocks, 8);


//#define BIT_DEPTH_FUNCS(depth, dct)\
//    c->get_pixels                    = FUNCC(get_pixels   ## dct   , depth);\
//    c->draw_edges                    = FUNCC(draw_edges            , depth);\
//    c->clear_block                   = FUNCC(clear_block  ## dct   , depth);\
//    c->clear_blocks                  = FUNCC(clear_blocks ## dct   , depth);\

//    switch (avctx->bits_per_raw_sample) {
//    case 9:
//        if (c->dct_bits == 32) {
//            BIT_DEPTH_FUNCS(9, _32);
//        } else {
//            BIT_DEPTH_FUNCS(9, _16);
//        }
//        break;
//    case 10:
//        if (c->dct_bits == 32) {
//            BIT_DEPTH_FUNCS(10, _32);
//        } else {
//            BIT_DEPTH_FUNCS(10, _16);
//        }
//        break;
//    case 12:
//        if (c->dct_bits == 32) {
//            BIT_DEPTH_FUNCS(12, _32);
//        } else {
//            BIT_DEPTH_FUNCS(12, _16);
//        }
//        break;
//    case 14:
//        if (c->dct_bits == 32) {
//            BIT_DEPTH_FUNCS(14, _32);
//        } else {
//            BIT_DEPTH_FUNCS(14, _16);
//        }
//        break;
//    default:
//        if(avctx->bits_per_raw_sample<=8 || avctx->codec_type != AVMEDIA_TYPE_VIDEO) {
//            BIT_DEPTH_FUNCS(8, _16);
//        }
//        break;
//    }



#define BIT_DEPTH_FUNCS(depth) \
    c->get_pixels                    = FUNCC(get_pixels,   depth);

        if(avctx->bits_per_raw_sample<=8 || avctx->codec_type != AVMEDIA_TYPE_VIDEO) {
            BIT_DEPTH_FUNCS(8);
        }


    if (HAVE_MMX)        ff_dsputil_init_mmx   (c, avctx);//これを無くすとencode_mb_internalのget_pixelsのところでエラーする


//    if (ARCH_ARM)        ff_dsputil_init_arm   (c, avctx);//ARMアーキテクチャの場合ここが必要そう
//    if (HAVE_VIS)        ff_dsputil_init_vis   (c, avctx);
//    if (ARCH_ALPHA)      ff_dsputil_init_alpha (c, avctx);
//    if (ARCH_PPC)        ff_dsputil_init_ppc   (c, avctx);
//    if (ARCH_SH4)        ff_dsputil_init_sh4   (c, avctx);
//    if (ARCH_BFIN)       ff_dsputil_init_bfin  (c, avctx);

    ff_init_scantable_permutation(c->idct_permutation,
                                  c->idct_permutation_type);
	LOGD("ff_dsputil_init 4 dsp:%d\n",c);//ここまではある
}



typedef void emu_edge_core_func(uint8_t *buf, const uint8_t *src,
                                x86_reg linesize, x86_reg start_y,
                                x86_reg end_y, x86_reg block_h,
                                x86_reg start_x, x86_reg end_x,
                                x86_reg block_w);
extern emu_edge_core_func ff_emu_edge_core_mmx;
extern emu_edge_core_func ff_emu_edge_core_sse;
static void emulated_edge_mc_mmx(uint8_t *buf, const uint8_t *src,
                                             ptrdiff_t linesize,
                                             int block_w, int block_h,
                                             int src_x, int src_y, int w, int h)
{
//    emulated_edge_mc(buf, src, linesize, block_w, block_h, src_x, src_y,
//                     w, h, &ff_emu_edge_core_mmx);//実態がわからない
}


static void emulated_edge_mc_sse(uint8_t *buf, const uint8_t *src,
                                             ptrdiff_t linesize,
                                             int block_w, int block_h,
                                             int src_x, int src_y, int w, int h)
{
//    emulated_edge_mc(buf, src, linesize, block_w, block_h, src_x, src_y,
//                     w, h, &ff_emu_edge_core_sse);//実態がわからない
}
av_cold void ff_videodsp_init_x86(VideoDSPContext *ctx, int bpc)
{
//#if HAVE_YASM
//    int mm_flags = av_get_cpu_flags();
	int mm_flags = 0x10013db;//x86はこの値

//#if ARCH_X86_32
    if (bpc <= 8 && (mm_flags & AV_CPU_FLAG_MMX)) {
        ctx->emulated_edge_mc = emulated_edge_mc_mmx;
    }
    if (mm_flags & AV_CPU_FLAG_3DNOW) {
//        ctx->prefetch = ff_prefetch_3dnow;
    }
//#endif /* ARCH_X86_32 */
    if (mm_flags & AV_CPU_FLAG_MMXEXT) {
//        ctx->prefetch = ff_prefetch_mmxext;
    }
    if (bpc <= 8 && (mm_flags & AV_CPU_FLAG_SSE)) {
        ctx->emulated_edge_mc = emulated_edge_mc_sse;
    }
//#endif /* HAVE_YASM */
}


static void just_return(uint8_t *buf, ptrdiff_t stride, int h)
{
}

void ff_videodsp_init(VideoDSPContext *ctx, int bpc)
{
    ctx->prefetch = just_return;//なんの実装もない関数だけど実装してないとエラーする
    if (bpc <= 8) {
        ctx->emulated_edge_mc = ff_emulated_edge_mc_8;
    } else {
//        ctx->emulated_edge_mc = ff_emulated_edge_mc_16;//ff_emulated_edge_mc_8が定義された
    }

//    if (ARCH_ARM)
//        ff_videodsp_init_arm(ctx, bpc);
//    if (ARCH_PPC)
//        ff_videodsp_init_ppc(ctx, bpc);
//    if (ARCH_X86)
        ff_videodsp_init_x86(ctx, bpc);
}

/* init common dct for both encoder and decoder */
av_cold int ff_dct_common_init(MpegEncContext *s)
{
	LOGD("ff_dct_common_init Called\n");
    ff_dsputil_init(&s->dsp, s->avctx);
	LOGD("ff_dct_common_init 0DSP:%d\n",&s->dsp);
//    ff_h264chroma_init(&s->h264chroma, 8); //for lowres
    ff_videodsp_init(&s->vdsp, s->avctx->bits_per_raw_sample);

    s->dct_unquantize_h263_intra = dct_unquantize_h263_intra_c;
    s->dct_unquantize_h263_inter = dct_unquantize_h263_inter_c;
    s->dct_unquantize_mpeg1_intra = dct_unquantize_mpeg1_intra_c;
    s->dct_unquantize_mpeg1_inter = dct_unquantize_mpeg1_inter_c;
    s->dct_unquantize_mpeg2_intra = dct_unquantize_mpeg2_intra_c;
    if (s->flags & CODEC_FLAG_BITEXACT)
        s->dct_unquantize_mpeg2_intra = dct_unquantize_mpeg2_intra_bitexact;
    s->dct_unquantize_mpeg2_inter = dct_unquantize_mpeg2_inter_c;

#if ARCH_X86
    ff_MPV_common_init_x86(s);
#elif ARCH_ALPHA
    ff_MPV_common_init_axp(s);
#elif ARCH_ARM
    ff_MPV_common_init_arm(s);
#elif HAVE_ALTIVEC
    ff_MPV_common_init_altivec(s);
#elif ARCH_BFIN
    ff_MPV_common_init_bfin(s);
#endif

    /* load & permutate scantables
     * note: only wmv uses different ones
     */
    if (s->alternate_scan) {
        ff_init_scantable(s->dsp.idct_permutation, &s->inter_scantable  , ff_alternate_vertical_scan);
        ff_init_scantable(s->dsp.idct_permutation, &s->intra_scantable  , ff_alternate_vertical_scan);
    } else {
        ff_init_scantable(s->dsp.idct_permutation, &s->inter_scantable  , ff_zigzag_direct);
        ff_init_scantable(s->dsp.idct_permutation, &s->intra_scantable  , ff_zigzag_direct);
    }
    ff_init_scantable(s->dsp.idct_permutation, &s->intra_h_scantable, ff_alternate_horizontal_scan);
    ff_init_scantable(s->dsp.idct_permutation, &s->intra_v_scantable, ff_alternate_vertical_scan);

    return 0;
}


void avcodec_get_chroma_sub_sample(enum AVPixelFormat pix_fmt, int *h_shift, int *v_shift)
{
    const AVPixFmtDescriptor *desc = av_pix_fmt_desc_get(pix_fmt);
//    LOGD("avcodec_get_chroma_sub_sample desc name:%s\n",desc->name);
//    av_assert0(desc);
    *h_shift = desc->log2_chroma_w;
    *v_shift = desc->log2_chroma_h;
}


/**
 * Locale-independent conversion of ASCII characters to uppercase.
 */
static inline int av_toupper(int c)
{
    if (c >= 'a' && c <= 'z')
        c ^= 0x20;
    return c;
}


unsigned int avpriv_toupper4(unsigned int x)
{
    return av_toupper(x & 0xFF)
           + (av_toupper((x >> 8) & 0xFF) << 8)
           + (av_toupper((x >> 16) & 0xFF) << 16)
           + (av_toupper((x >> 24) & 0xFF) << 24);
}




static inline void ff_update_block_index(MpegEncContext *s){
    const int block_size= 8 >> s->avctx->lowres;

    s->block_index[0]+=2;
    s->block_index[1]+=2;
    s->block_index[2]+=2;
    s->block_index[3]+=2;
    s->block_index[4]++;
    s->block_index[5]++;
    s->dest[0]+= 2*block_size;
    s->dest[1]+= block_size;
    s->dest[2]+= block_size;
}

static void mpeg_er_decode_mb(void *opaque, int ref, int mv_dir, int mv_type,
                              int (*mv)[2][4][2],
                              int mb_x, int mb_y, int mb_intra, int mb_skipped)
{
    MpegEncContext *s = opaque;

    s->mv_dir     = mv_dir;
    s->mv_type    = mv_type;
    s->mb_intra   = mb_intra;
    s->mb_skipped = mb_skipped;
    s->mb_x       = mb_x;
    s->mb_y       = mb_y;
    memcpy(s->mv, mv, sizeof(*mv));

    ff_init_block_index(s);
    ff_update_block_index(s);

    s->dsp.clear_blocks(s->block[0]);

    s->dest[0] = s->current_picture.f.data[0] + (s->mb_y *  16                       * s->linesize)   + s->mb_x *  16;
    s->dest[1] = s->current_picture.f.data[1] + (s->mb_y * (16 >> s->chroma_y_shift) * s->uvlinesize) + s->mb_x * (16 >> s->chroma_x_shift);
    s->dest[2] = s->current_picture.f.data[2] + (s->mb_y * (16 >> s->chroma_y_shift) * s->uvlinesize) + s->mb_x * (16 >> s->chroma_x_shift);

//    assert(ref == 0);
    ff_MPV_decode_mb(s, s->block);
}

static int init_er(MpegEncContext *s)
{
    ERContext *er = &s->er;
    int mb_array_size = s->mb_height * s->mb_stride;
    int i;

    er->avctx       = s->avctx;
    er->dsp         = &s->dsp;

    er->mb_index2xy = s->mb_index2xy;
    er->mb_num      = s->mb_num;
    er->mb_width    = s->mb_width;
    er->mb_height   = s->mb_height;
    er->mb_stride   = s->mb_stride;
    er->b8_stride   = s->b8_stride;

    er->er_temp_buffer     = av_malloc(s->mb_height * s->mb_stride);
    er->error_status_table = av_mallocz(mb_array_size);
    if (!er->er_temp_buffer || !er->error_status_table)
        goto fail;

    er->mbskip_table  = s->mbskip_table;
    er->mbintra_table = s->mbintra_table;

    for (i = 0; i < FF_ARRAY_ELEMS(s->dc_val); i++)
        er->dc_val[i] = s->dc_val[i];

    er->decode_mb = mpeg_er_decode_mb;
    er->opaque    = s;

    return 0;
fail:
    av_freep(&er->er_temp_buffer);
    av_freep(&er->error_status_table);
    return -1;
}
/**
 * Initialize and allocates MpegEncContext fields dependent on the resolution.
 */
static int init_context_frame(MpegEncContext *s)
{
    int y_size, c_size, yc_size, i, mb_array_size, mv_table_size, x, y;

    s->mb_width   = (s->width + 15) / 16;
    s->mb_stride  = s->mb_width + 1;
    s->b8_stride  = s->mb_width * 2 + 1;
    s->b4_stride  = s->mb_width * 4 + 1;
    mb_array_size = s->mb_height * s->mb_stride;
    LOGD("init_context_frame s->mb_height:%d mb_stride:%d",s->mb_height,s->mb_stride);
    mv_table_size = (s->mb_height + 2) * s->mb_stride + 1;

    /* set default edge pos, will be overriden
     * in decode_header if needed */
    s->h_edge_pos = s->mb_width * 16;
    s->v_edge_pos = s->mb_height * 16;
    LOGD("init_context_frame width:%d height:%d",s->mb_width,s->mb_height);
    s->mb_num     = s->mb_width * s->mb_height;

    s->block_wrap[0] =
    s->block_wrap[1] =
    s->block_wrap[2] =
    s->block_wrap[3] = s->b8_stride;
    s->block_wrap[4] =
    s->block_wrap[5] = s->mb_stride;

    y_size  = s->b8_stride * (2 * s->mb_height + 1);
    c_size  = s->mb_stride * (s->mb_height + 1);
    yc_size = y_size + 2   * c_size;

    LOGD("init_context_frame s->mb_num:%d sizeof(int):%d",s->mb_num,sizeof(int));
    FF_ALLOCZ_OR_GOTO(s->avctx, s->mb_index2xy, (s->mb_num + 1) * sizeof(int), fail); // error ressilience code looks cleaner with this
    for (y = 0; y < s->mb_height; y++)
        for (x = 0; x < s->mb_width; x++)
            s->mb_index2xy[x + y * s->mb_width] = x + y * s->mb_stride;

    s->mb_index2xy[s->mb_height * s->mb_width] = (s->mb_height - 1) * s->mb_stride + s->mb_width; // FIXME really needed?

    if (s->encoding) {
        /* Allocate MV tables */
        FF_ALLOCZ_OR_GOTO(s->avctx, s->p_mv_table_base,                 mv_table_size * 2 * sizeof(int16_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->b_forw_mv_table_base,            mv_table_size * 2 * sizeof(int16_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->b_back_mv_table_base,            mv_table_size * 2 * sizeof(int16_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->b_bidir_forw_mv_table_base,      mv_table_size * 2 * sizeof(int16_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->b_bidir_back_mv_table_base,      mv_table_size * 2 * sizeof(int16_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->b_direct_mv_table_base,          mv_table_size * 2 * sizeof(int16_t), fail)
        s->p_mv_table            = s->p_mv_table_base + s->mb_stride + 1;
        s->b_forw_mv_table       = s->b_forw_mv_table_base + s->mb_stride + 1;
        s->b_back_mv_table       = s->b_back_mv_table_base + s->mb_stride + 1;
        s->b_bidir_forw_mv_table = s->b_bidir_forw_mv_table_base + s->mb_stride + 1;
        s->b_bidir_back_mv_table = s->b_bidir_back_mv_table_base + s->mb_stride + 1;
        s->b_direct_mv_table     = s->b_direct_mv_table_base + s->mb_stride + 1;

        /* Allocate MB type table */
        FF_ALLOCZ_OR_GOTO(s->avctx, s->mb_type, mb_array_size * sizeof(uint16_t), fail) // needed for encoding

        FF_ALLOCZ_OR_GOTO(s->avctx, s->lambda_table, mb_array_size * sizeof(int), fail)

        FF_ALLOC_OR_GOTO(s->avctx, s->cplx_tab,mb_array_size * sizeof(float), fail);
        FF_ALLOC_OR_GOTO(s->avctx, s->bits_tab, mb_array_size * sizeof(float), fail);

    }

    if (s->codec_id == AV_CODEC_ID_MPEG4 ||
        (s->flags & CODEC_FLAG_INTERLACED_ME)) {
        /* interlaced direct mode decoding tables */
        for (i = 0; i < 2; i++) {
            int j, k;
            for (j = 0; j < 2; j++) {
                for (k = 0; k < 2; k++) {
                    FF_ALLOCZ_OR_GOTO(s->avctx,
                                      s->b_field_mv_table_base[i][j][k],
                                      mv_table_size * 2 * sizeof(int16_t),
                                      fail);
                    s->b_field_mv_table[i][j][k] = s->b_field_mv_table_base[i][j][k] +
                                                   s->mb_stride + 1;
                }
                FF_ALLOCZ_OR_GOTO(s->avctx, s->b_field_select_table [i][j], mb_array_size * 2 * sizeof(uint8_t), fail)
                FF_ALLOCZ_OR_GOTO(s->avctx, s->p_field_mv_table_base[i][j], mv_table_size * 2 * sizeof(int16_t), fail)
                s->p_field_mv_table[i][j] = s->p_field_mv_table_base[i][j] + s->mb_stride + 1;
            }
            FF_ALLOCZ_OR_GOTO(s->avctx, s->p_field_select_table[i], mb_array_size * 2 * sizeof(uint8_t), fail)
        }
    }
    if (s->out_format == FMT_H263) {
        /* cbp values */
        FF_ALLOCZ_OR_GOTO(s->avctx, s->coded_block_base, y_size, fail);
        s->coded_block = s->coded_block_base + s->b8_stride + 1;

        /* cbp, ac_pred, pred_dir */
        FF_ALLOCZ_OR_GOTO(s->avctx, s->cbp_table     , mb_array_size * sizeof(uint8_t), fail);
        FF_ALLOCZ_OR_GOTO(s->avctx, s->pred_dir_table, mb_array_size * sizeof(uint8_t), fail);
    }

    if (s->h263_pred || s->h263_plus || !s->encoding) {
        /* dc values */
        // MN: we need these for  error resilience of intra-frames
        FF_ALLOCZ_OR_GOTO(s->avctx, s->dc_val_base, yc_size * sizeof(int16_t), fail);
        s->dc_val[0] = s->dc_val_base + s->b8_stride + 1;
        s->dc_val[1] = s->dc_val_base + y_size + s->mb_stride + 1;
        s->dc_val[2] = s->dc_val[1] + c_size;
        for (i = 0; i < yc_size; i++)
            s->dc_val_base[i] = 1024;
    }

    /* which mb is a intra block */
    FF_ALLOCZ_OR_GOTO(s->avctx, s->mbintra_table, mb_array_size, fail);
    memset(s->mbintra_table, 1, mb_array_size);

    /* init macroblock skip table */
    FF_ALLOCZ_OR_GOTO(s->avctx, s->mbskip_table, mb_array_size + 2, fail);
    // Note the + 1 is for  a quicker mpeg4 slice_end detection

    return init_er(s);
fail:
    return -1;
}


static int init_duplicate_context(MpegEncContext *s)
{
    int y_size = s->b8_stride * (2 * s->mb_height + 1);
    int c_size = s->mb_stride * (s->mb_height + 1);
    int yc_size = y_size + 2 * c_size;
    int i;

    s->edge_emu_buffer =
    s->me.scratchpad   =
    s->me.temp         =
    s->rd_scratchpad   =
    s->b_scratchpad    =
    s->obmc_scratchpad = NULL;

    if (s->encoding) {
        FF_ALLOCZ_OR_GOTO(s->avctx, s->me.map,
                          ME_MAP_SIZE * sizeof(uint32_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->me.score_map,
                          ME_MAP_SIZE * sizeof(uint32_t), fail)
        if (s->avctx->noise_reduction) {
            FF_ALLOCZ_OR_GOTO(s->avctx, s->dct_error_sum,
                              2 * 64 * sizeof(int), fail)
        }
    }
    FF_ALLOCZ_OR_GOTO(s->avctx, s->blocks, 64 * 12 * 2 * sizeof(int16_t), fail)
    s->block = s->blocks[0];

    for (i = 0; i < 12; i++) {
        s->pblocks[i] = &s->block[i];
    }

    if (s->out_format == FMT_H263) {
        /* ac values */
        FF_ALLOCZ_OR_GOTO(s->avctx, s->ac_val_base,
                          yc_size * sizeof(int16_t) * 16, fail);
        s->ac_val[0] = s->ac_val_base + s->b8_stride + 1;
        s->ac_val[1] = s->ac_val_base + y_size + s->mb_stride + 1;
        s->ac_val[2] = s->ac_val[1] + c_size;
    }

    return 0;
fail:
    return -1; // free() through ff_MPV_common_end()
}



static void free_duplicate_context(MpegEncContext *s)
{
    if (s == NULL)
        return;

    av_freep(&s->edge_emu_buffer);
    av_freep(&s->me.scratchpad);
    s->me.temp =
    s->rd_scratchpad =
    s->b_scratchpad =
    s->obmc_scratchpad = NULL;

    av_freep(&s->dct_error_sum);
    av_freep(&s->me.map);
    av_freep(&s->me.score_map);
    av_freep(&s->blocks);
    av_freep(&s->ac_val_base);
    s->block = NULL;
}

/**
 * Frees and resets MpegEncContext fields depending on the resolution.
 * Is used during resolution changes to avoid a full reinitialization of the
 * codec.
 */
static int free_context_frame(MpegEncContext *s)
{
    int i, j, k;

    av_freep(&s->mb_type);
    av_freep(&s->p_mv_table_base);
    av_freep(&s->b_forw_mv_table_base);
    av_freep(&s->b_back_mv_table_base);
    av_freep(&s->b_bidir_forw_mv_table_base);
    av_freep(&s->b_bidir_back_mv_table_base);
    av_freep(&s->b_direct_mv_table_base);
    s->p_mv_table            = NULL;
    s->b_forw_mv_table       = NULL;
    s->b_back_mv_table       = NULL;
    s->b_bidir_forw_mv_table = NULL;
    s->b_bidir_back_mv_table = NULL;
    s->b_direct_mv_table     = NULL;
    for (i = 0; i < 2; i++) {
        for (j = 0; j < 2; j++) {
            for (k = 0; k < 2; k++) {
                av_freep(&s->b_field_mv_table_base[i][j][k]);
                s->b_field_mv_table[i][j][k] = NULL;
            }
            av_freep(&s->b_field_select_table[i][j]);
            av_freep(&s->p_field_mv_table_base[i][j]);
            s->p_field_mv_table[i][j] = NULL;
        }
        av_freep(&s->p_field_select_table[i]);
    }

    av_freep(&s->dc_val_base);
    av_freep(&s->coded_block_base);
    av_freep(&s->mbintra_table);
    av_freep(&s->cbp_table);
    av_freep(&s->pred_dir_table);

    av_freep(&s->mbskip_table);

    av_freep(&s->er.error_status_table);
    av_freep(&s->er.er_temp_buffer);
    av_freep(&s->mb_index2xy);
    av_freep(&s->lambda_table);

    av_freep(&s->cplx_tab);
    av_freep(&s->bits_tab);

    s->linesize = s->uvlinesize = 0;

    for (i = 0; i < 3; i++)
        av_freep(&s->visualization_buffer[i]);

    return 0;
}


/* init common structure for both encoder and decoder */
void ff_MPV_common_end(MpegEncContext *s)
{
    int i;

    if (s->slice_context_count > 1) {
        for (i = 0; i < s->slice_context_count; i++) {
            free_duplicate_context(s->thread_context[i]);
        }
        for (i = 1; i < s->slice_context_count; i++) {
            av_freep(&s->thread_context[i]);
        }
        s->slice_context_count = 1;
    } else free_duplicate_context(s);

    av_freep(&s->parse_context.buffer);
    s->parse_context.buffer_size = 0;

    av_freep(&s->bitstream_buffer);
    s->allocated_bitstream_buffer_size = 0;

    av_freep(&s->avctx->stats_out);
    av_freep(&s->ac_stats);

    if(s->q_chroma_intra_matrix   != s->q_intra_matrix  ) av_freep(&s->q_chroma_intra_matrix);
    if(s->q_chroma_intra_matrix16 != s->q_intra_matrix16) av_freep(&s->q_chroma_intra_matrix16);
    s->q_chroma_intra_matrix=   NULL;
    s->q_chroma_intra_matrix16= NULL;
    av_freep(&s->q_intra_matrix);
    av_freep(&s->q_inter_matrix);
    av_freep(&s->q_intra_matrix16);
    av_freep(&s->q_inter_matrix16);
    av_freep(&s->input_picture);
    av_freep(&s->reordered_input_picture);
    av_freep(&s->dct_offset);

    if (s->picture && !s->avctx->internal->is_copy) {
        for (i = 0; i < s->picture_count; i++) {
            free_picture(s, &s->picture[i]);
        }
    }
    av_freep(&s->picture);

    free_context_frame(s);

    if (!(s->avctx->active_thread_type & FF_THREAD_FRAME))
        avcodec_default_free_buffers(s->avctx);

    s->context_initialized      = 0;
    s->last_picture_ptr         =
    s->next_picture_ptr         =
    s->current_picture_ptr      = NULL;
    s->linesize = s->uvlinesize = 0;
}

/* init static data */
av_cold void ff_dsputil_static_init(void)
{
//	LOGD("ff_dsputil_static_init Called ff_squareTbl\n");
    int i;

    for(i=0;i<512;i++) {
        ff_squareTbl[i] = (i - 256) * (i - 256);
//        LOGD("%d ",ff_squareTbl[i]);
    }
//    LOGD("\n");

}
/**
 * init common structure for both encoder and decoder.
 * this assumes that some variables like width/height are already set
 */
av_cold int ff_MPV_common_init(MpegEncContext *s)
{
    LOGD( "ff_MPV_common_init Called height:%d\n",s->height);
    int i;
    int nb_slices = (HAVE_THREADS &&
                     s->avctx->active_thread_type) ?
                    s->avctx->thread_count : 1;

    if (s->encoding && s->avctx->slices)
        nb_slices = s->avctx->slices;

    if (s->codec_id == AV_CODEC_ID_MPEG2VIDEO && !s->progressive_sequence)
        s->mb_height = (s->height + 31) / 32 * 2;
    else if (s->codec_id != AV_CODEC_ID_H264)
        s->mb_height = (s->height + 15) / 16;

    if (s->avctx->pix_fmt == AV_PIX_FMT_NONE) {
       LOGD( "decoding to AV_PIX_FMT_NONE is not supported.\n");
        return -1;
    }

    if (nb_slices > MAX_THREADS || (nb_slices > s->mb_height && s->mb_height)) {
        int max_slices;
        if (s->mb_height)
            max_slices = FFMIN(MAX_THREADS, s->mb_height);
        else
            max_slices = MAX_THREADS;
        LOGD( "too many threads/slices (%d),"
               " reducing to %d\n", nb_slices, max_slices);
        nb_slices = max_slices;
    }

//    if ((s->width || s->height) &&
//        av_image_check_size(s->width, s->height, 0, s->avctx))
//        return -1;

    ff_dct_common_init(s);//ここでff_dsputil_init→pix_sumがセットされている

    s->flags  = s->avctx->flags;
    s->flags2 = s->avctx->flags2;

    /* set chroma shifts */
    avcodec_get_chroma_sub_sample(s->avctx->pix_fmt, &s->chroma_x_shift, &s->chroma_y_shift);

    /* convert fourcc to upper case */
    s->codec_tag        = avpriv_toupper4(s->avctx->codec_tag);
    s->stream_codec_tag = avpriv_toupper4(s->avctx->stream_codec_tag);

    s->avctx->coded_frame = &s->current_picture.f;

    if (s->encoding) {
    	LOGD("ff_MPV_common_init s->encoding true\n");
        if (s->msmpeg4_version) {
            FF_ALLOCZ_OR_GOTO(s->avctx, s->ac_stats,
                                2 * 2 * (MAX_LEVEL + 1) *
                                (MAX_RUN + 1) * 2 * sizeof(int), fail);
        }
        FF_ALLOCZ_OR_GOTO(s->avctx, s->avctx->stats_out, 256, fail);
        FF_ALLOCZ_OR_GOTO(s->avctx, s->q_intra_matrix,          64 * 32   * sizeof(int), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->q_chroma_intra_matrix,   64 * 32   * sizeof(int), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->q_inter_matrix,          64 * 32   * sizeof(int), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->q_intra_matrix16,        64 * 32 * 2 * sizeof(uint16_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->q_chroma_intra_matrix16, 64 * 32 * 2 * sizeof(uint16_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->q_inter_matrix16,        64 * 32 * 2 * sizeof(uint16_t), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->input_picture,           MAX_PICTURE_COUNT * sizeof(Picture *), fail)
        FF_ALLOCZ_OR_GOTO(s->avctx, s->reordered_input_picture, MAX_PICTURE_COUNT * sizeof(Picture *), fail)

        if (s->avctx->noise_reduction) {
            FF_ALLOCZ_OR_GOTO(s->avctx, s->dct_offset, 2 * 64 * sizeof(uint16_t), fail);
        }
    }

    //ここで32×thread_countの値でフレームを初期化している
    //s->pictureをアロケートしている
    s->picture_count = MAX_PICTURE_COUNT * FFMAX(1, s->avctx->thread_count);
    LOGD("MPV_init alloc s->picture count is:%d",s->picture_count);
    FF_ALLOCZ_OR_GOTO(s->avctx, s->picture,
                      s->picture_count * sizeof(Picture), fail);
    for (i = 0; i < s->picture_count; i++) {
        avcodec_get_frame_defaults(&s->picture[i].f);
    }

        if (init_context_frame(s))
            goto fail;

        s->parse_context.state = -1;

        s->context_initialized = 1;
        s->thread_context[0]   = s;
        LOGD("ff_MPV_common_init SET s->thread_context[0]->dsp:%d\n",&s->thread_context[0]->dsp);

//     if (s->width && s->height) {
        if (nb_slices > 1) {
            for (i = 1; i < nb_slices; i++) {
                s->thread_context[i] = av_malloc(sizeof(MpegEncContext));
                memcpy(s->thread_context[i], s, sizeof(MpegEncContext));
            }

            for (i = 0; i < nb_slices; i++) {
                if (init_duplicate_context(s->thread_context[i]) < 0)//ここでblockを初期化している
                    goto fail;
                    s->thread_context[i]->start_mb_y =
                        (s->mb_height * (i) + nb_slices / 2) / nb_slices;
                    s->thread_context[i]->end_mb_y   =
                        (s->mb_height * (i + 1) + nb_slices / 2) / nb_slices;
            }
        } else {
            if (init_duplicate_context(s) < 0)
                goto fail;
            s->start_mb_y = 0;
            s->end_mb_y   = s->mb_height;
        }
        s->slice_context_count = nb_slices;
//     }
        ff_dsputil_static_init();
        LOGD( "ff_MPV_common_init end \n");
    return 0;
 fail:
	LOGD("fail MPV_encode_init\n");
    ff_MPV_common_end(s);
    return -1;
}




void ff_block_permute(int16_t *block, uint8_t *permutation, const uint8_t *scantable, int last)
{
    int i;
    int16_t temp[64];

    if(last<=0) return;
    //if(permutation[1]==1) return; //FIXME it is ok but not clean and might fail for some permutations

    for(i=0; i<=last; i++){
        const int j= scantable[i];
        temp[j]= block[j];
        block[j]=0;
    }
//    int index;
//    for(index = 0; index < 5;index++){
//    	LOGD("ff_block_permute A block[%d]:%d\n",index,block[index]);
//    }
    for(i=0; i<=last; i++){
        const int j= scantable[i];
        const int perm_j= permutation[j];
        block[perm_j]= temp[j];
    }
}

static void denoise_dct_c(MpegEncContext *s, int16_t *block){
    const int intra= s->mb_intra;
    int i;

    s->dct_count[intra]++;

    for(i=0; i<64; i++){
        int level= block[i];

        if(level){
            if(level>0){
                s->dct_error_sum[intra][i] += level;
                level -= s->dct_offset[intra][i];
                if(level<0) level=0;
            }else{
                s->dct_error_sum[intra][i] -= level;
                level += s->dct_offset[intra][i];
                if(level>0) level=0;
            }
            block[i]= level;
        }
    }
}



static int dct_quantize_trellis_c(MpegEncContext *s,
                                  int16_t *block, int n,
                                  int qscale, int *overflow){
    const int *qmat;
    const uint8_t *scantable= s->intra_scantable.scantable;
    const uint8_t *perm_scantable= s->intra_scantable.permutated;
    int max=0;
    unsigned int threshold1, threshold2;
    int bias=0;
    int run_tab[65];
    int level_tab[65];
    int score_tab[65];
    int survivor[65];
    int survivor_count;
    int last_run=0;
    int last_level=0;
    int last_score= 0;
    int last_i;
    int coeff[2][64];
    int coeff_count[64];
    int qmul, qadd, start_i, last_non_zero, i, dc;
    const int esc_length= s->ac_esc_length;
    uint8_t * length;
    uint8_t * last_length;
    const int lambda= s->lambda2 >> (FF_LAMBDA_SHIFT - 6);

    s->dsp.fdct (block);

    if(s->dct_error_sum)
        s->denoise_dct(s, block);
    qmul= qscale*16;
    qadd= ((qscale-1)|1)*8;

    if (s->mb_intra) {
        int q;
        if (!s->h263_aic) {
            if (n < 4)
                q = s->y_dc_scale;
            else
                q = s->c_dc_scale;
            q = q << 3;
        } else{
            /* For AIC we skip quant/dequant of INTRADC */
            q = 1 << 3;
            qadd=0;
        }

        /* note: block[0] is assumed to be positive */
        block[0] = (block[0] + (q >> 1)) / q;
        start_i = 1;
        last_non_zero = 0;
        qmat = n < 4 ? s->q_intra_matrix[qscale] : s->q_chroma_intra_matrix[qscale];
        if(s->mpeg_quant || s->out_format == FMT_MPEG1)
            bias= 1<<(QMAT_SHIFT-1);
        length     = s->intra_ac_vlc_length;
        last_length= s->intra_ac_vlc_last_length;
    } else {
        start_i = 0;
        last_non_zero = -1;
        qmat = s->q_inter_matrix[qscale];
        length     = s->inter_ac_vlc_length;
        last_length= s->inter_ac_vlc_last_length;
    }
    last_i= start_i;

    threshold1= (1<<QMAT_SHIFT) - bias - 1;
    threshold2= (threshold1<<1);

    for(i=63; i>=start_i; i--) {
        const int j = scantable[i];
        int level = block[j] * qmat[j];

        if(((unsigned)(level+threshold1))>threshold2){
            last_non_zero = i;
            break;
        }
    }

    for(i=start_i; i<=last_non_zero; i++) {
        const int j = scantable[i];
        int level = block[j] * qmat[j];

//        if(   bias+level >= (1<<(QMAT_SHIFT - 3))
//           || bias-level >= (1<<(QMAT_SHIFT - 3))){
        if(((unsigned)(level+threshold1))>threshold2){
            if(level>0){
                level= (bias + level)>>QMAT_SHIFT;
                coeff[0][i]= level;
                coeff[1][i]= level-1;
//                coeff[2][k]= level-2;
            }else{
                level= (bias - level)>>QMAT_SHIFT;
                coeff[0][i]= -level;
                coeff[1][i]= -level+1;
//                coeff[2][k]= -level+2;
            }
            coeff_count[i]= FFMIN(level, 2);
//            av_assert2(coeff_count[i]);
            max |=level;
        }else{
            coeff[0][i]= (level>>31)|1;
            coeff_count[i]= 1;
        }
    }

    *overflow= s->max_qcoeff < max; //overflow might have happened

    if(last_non_zero < start_i){
        memset(block + start_i, 0, (64-start_i)*sizeof(int16_t));
        return last_non_zero;
    }

    score_tab[start_i]= 0;
    survivor[0]= start_i;
    survivor_count= 1;

    for(i=start_i; i<=last_non_zero; i++){
        int level_index, j, zero_distortion;
        int dct_coeff= FFABS(block[ scantable[i] ]);
        int best_score=256*256*256*120;

        if (s->dsp.fdct == ff_fdct_ifast)
            dct_coeff= (dct_coeff*ff_inv_aanscales[ scantable[i] ]) >> 12;
        zero_distortion= dct_coeff*dct_coeff;

        for(level_index=0; level_index < coeff_count[i]; level_index++){
            int distortion;
            int level= coeff[level_index][i];
            const int alevel= FFABS(level);
            int unquant_coeff;

//            av_assert2(level);

            if(s->out_format == FMT_H263){
                unquant_coeff= alevel*qmul + qadd;
            }else{ //MPEG1
                j= s->dsp.idct_permutation[ scantable[i] ]; //FIXME optimize
                if(s->mb_intra){
                        unquant_coeff = (int)(  alevel  * qscale * s->intra_matrix[j]) >> 3;
                        unquant_coeff =   (unquant_coeff - 1) | 1;
                }else{
                        unquant_coeff = (((  alevel  << 1) + 1) * qscale * ((int) s->inter_matrix[j])) >> 4;
                        unquant_coeff =   (unquant_coeff - 1) | 1;
                }
                unquant_coeff<<= 3;
            }

            distortion= (unquant_coeff - dct_coeff) * (unquant_coeff - dct_coeff) - zero_distortion;
            level+=64;
            if((level&(~127)) == 0){
                for(j=survivor_count-1; j>=0; j--){
                    int run= i - survivor[j];
                    int score= distortion + length[UNI_AC_ENC_INDEX(run, level)]*lambda;
                    score += score_tab[i-run];

                    if(score < best_score){
                        best_score= score;
                        run_tab[i+1]= run;
                        level_tab[i+1]= level-64;
                    }
                }

                if(s->out_format == FMT_H263){
                    for(j=survivor_count-1; j>=0; j--){
                        int run= i - survivor[j];
                        int score= distortion + last_length[UNI_AC_ENC_INDEX(run, level)]*lambda;
                        score += score_tab[i-run];
                        if(score < last_score){
                            last_score= score;
                            last_run= run;
                            last_level= level-64;
                            last_i= i+1;
                        }
                    }
                }
            }else{
                distortion += esc_length*lambda;
                for(j=survivor_count-1; j>=0; j--){
                    int run= i - survivor[j];
                    int score= distortion + score_tab[i-run];

                    if(score < best_score){
                        best_score= score;
                        run_tab[i+1]= run;
                        level_tab[i+1]= level-64;
                    }
                }

                if(s->out_format == FMT_H263){
                  for(j=survivor_count-1; j>=0; j--){
                        int run= i - survivor[j];
                        int score= distortion + score_tab[i-run];
                        if(score < last_score){
                            last_score= score;
                            last_run= run;
                            last_level= level-64;
                            last_i= i+1;
                        }
                    }
                }
            }
        }

        score_tab[i+1]= best_score;

        //Note: there is a vlc code in mpeg4 which is 1 bit shorter then another one with a shorter run and the same level
        if(last_non_zero <= 27){
            for(; survivor_count; survivor_count--){
                if(score_tab[ survivor[survivor_count-1] ] <= best_score)
                    break;
            }
        }else{
            for(; survivor_count; survivor_count--){
                if(score_tab[ survivor[survivor_count-1] ] <= best_score + lambda)
                    break;
            }
        }

        survivor[ survivor_count++ ]= i+1;
    }

    if(s->out_format != FMT_H263){
        last_score= 256*256*256*120;
        for(i= survivor[0]; i<=last_non_zero + 1; i++){
            int score= score_tab[i];
            if(i) score += lambda*2; //FIXME exacter?

            if(score < last_score){
                last_score= score;
                last_i= i;
                last_level= level_tab[i];
                last_run= run_tab[i];
            }
        }
    }

    s->coded_score[n] = last_score;

    dc= FFABS(block[0]);
    last_non_zero= last_i - 1;
    memset(block + start_i, 0, (64-start_i)*sizeof(int16_t));

    if(last_non_zero < start_i)
        return last_non_zero;

    if(last_non_zero == 0 && start_i == 0){
        int best_level= 0;
        int best_score= dc * dc;

        for(i=0; i<coeff_count[0]; i++){
            int level= coeff[i][0];
            int alevel= FFABS(level);
            int unquant_coeff, score, distortion;

            if(s->out_format == FMT_H263){
                    unquant_coeff= (alevel*qmul + qadd)>>3;
            }else{ //MPEG1
                    unquant_coeff = (((  alevel  << 1) + 1) * qscale * ((int) s->inter_matrix[0])) >> 4;
                    unquant_coeff =   (unquant_coeff - 1) | 1;
            }
            unquant_coeff = (unquant_coeff + 4) >> 3;
            unquant_coeff<<= 3 + 3;

            distortion= (unquant_coeff - dc) * (unquant_coeff - dc);
            level+=64;
            if((level&(~127)) == 0) score= distortion + last_length[UNI_AC_ENC_INDEX(0, level)]*lambda;
            else                    score= distortion + esc_length*lambda;

            if(score < best_score){
                best_score= score;
                best_level= level - 64;
            }
        }
        block[0]= best_level;
        s->coded_score[n] = best_score - dc*dc;
        if(best_level == 0) return -1;
        else                return last_non_zero;
    }

    i= last_i;
//    av_assert2(last_level);

    block[ perm_scantable[last_non_zero] ]= last_level;
    i -= last_run + 1;

    for(; i>start_i; i -= run_tab[i] + 1){
        block[ perm_scantable[i-1] ]= level_tab[i];
    }

    return last_non_zero;
}

av_cold int ff_dct_encode_init(MpegEncContext *s) {
//    if (ARCH_X86)
//        ff_dct_encode_init_x86(s);

//    for (i = 0; i < 64; i++)
//        inv_zigzag_direct16[ff_zigzag_direct[i]] = i + 1;
    if (!s->dct_quantize)
        s->dct_quantize = ff_dct_quantize_c;
    if (!s->denoise_dct)
        s->denoise_dct  = denoise_dct_c;
    s->fast_dct_quantize = s->dct_quantize;
    if (s->avctx->trellis)
        s->dct_quantize  = dct_quantize_trellis_c;

    return 0;
}

static int zero_cmp(void *s, uint8_t *a, uint8_t *b, int stride, int h){
    return 0;
}





void ff_convert_matrix(DSPContext *dsp, int (*qmat)[64],
                       uint16_t (*qmat16)[2][64],
                       const uint16_t *quant_matrix,
                       int bias, int qmin, int qmax, int intra)
{
    int qscale;
    int shift = 0;

    for (qscale = qmin; qscale <= qmax; qscale++) {
        int i;
        if (dsp->fdct == ff_jpeg_fdct_islow_8 ||
            dsp->fdct == ff_faandct) {
            for (i = 0; i < 64; i++) {
                const int j = dsp->idct_permutation[i];
                /* 16 <= qscale * quant_matrix[i] <= 7905
                 * Assume x = ff_aanscales[i] * qscale * quant_matrix[i]
                 *             19952 <=              x  <= 249205026
                 * (1 << 36) / 19952 >= (1 << 36) / (x) >= (1 << 36) / 249205026
                 *           3444240 >= (1 << 36) / (x) >= 275 */

                qmat[qscale][i] = (int)((UINT64_C(1) << QMAT_SHIFT) /
                                        (qscale * quant_matrix[j]));
            }
        } else if (dsp->fdct == ff_fdct_ifast) {
            for (i = 0; i < 64; i++) {
                const int j = dsp->idct_permutation[i];
                /* 16 <= qscale * quant_matrix[i] <= 7905
                 * Assume x = ff_aanscales[i] * qscale * quant_matrix[i]
                 *             19952 <=              x  <= 249205026
                 * (1 << 36) / 19952 >= (1 << 36) / (x) >= (1 << 36) / 249205026
                 *           3444240 >= (1 << 36) / (x) >= 275 */

                qmat[qscale][i] = (int)((UINT64_C(1) << (QMAT_SHIFT + 14)) /
                                        (ff_aanscales[i] * (int64_t)qscale * quant_matrix[j]));
            }
        } else {
            for (i = 0; i < 64; i++) {
                const int j = dsp->idct_permutation[i];
                /* We can safely suppose that 16 <= quant_matrix[i] <= 255
                 * Assume x = qscale * quant_matrix[i]
                 * So             16 <=              x  <= 7905
                 * so (1 << 19) / 16 >= (1 << 19) / (x) >= (1 << 19) / 7905
                 * so          32768 >= (1 << 19) / (x) >= 67 */
                qmat[qscale][i] = (int)((UINT64_C(1) << QMAT_SHIFT) /
                                        (qscale * quant_matrix[j]));
                //qmat  [qscale][i] = (1 << QMAT_SHIFT_MMX) /
                //                    (qscale * quant_matrix[i]);
                qmat16[qscale][0][i] = (1 << QMAT_SHIFT_MMX) /
                                       (qscale * quant_matrix[j]);

                if (qmat16[qscale][0][i] == 0 ||
                    qmat16[qscale][0][i] == 128 * 256)
                    qmat16[qscale][0][i] = 128 * 256 - 1;
                qmat16[qscale][1][i] =
                    ROUNDED_DIV(bias << (16 - QUANT_BIAS_SHIFT),
                                qmat16[qscale][0][i]);
            }
        }

        for (i = intra; i < 64; i++) {
            int64_t max = 8191;
            if (dsp->fdct == ff_fdct_ifast) {
                max = (8191LL * ff_aanscales[i]) >> 14;
            }
            while (((max * qmat[qscale][i]) >> shift) > __INT_MAX__) {
                shift++;
            }
        }
    }
    if (shift) {
    	LOGD("Warning, QMAT_SHIFT is larger than %d, overflows possible\n",(QMAT_SHIFT - shift));
    }
}


/**
 * Locale-independent conversion of ASCII isspace.
 */
static inline int av_isspace(int c)
{
    return c == ' ' || c == '\f' || c == '\n' || c == '\r' || c == '\t' || c == '\v';
}


static double etime(double v)
{
    return av_gettime() * 0.000001;
}

static int strmatch(const char *s, const char *prefix)
{
    int i;
    for (i=0; prefix[i]; i++) {
        if (prefix[i] != s[i]) return 0;
    }
    /* return 1 only if the s identifier is terminated */
    return !IS_IDENTIFIER_CHAR(s[i]);
}

double av_strtod(const char *numstr, char **tail)
{
    double d;
    char *next;
    if(numstr[0]=='0' && (numstr[1]|0x20)=='x') {
        d = strtoul(numstr, &next, 16);
    } else
        d = strtod(numstr, &next);
    /* if parsing succeeded, check for and interpret postfixes */
    if (next!=numstr) {
        if (next[0] == 'd' && next[1] == 'B') {
            /* treat dB as decibels instead of decibytes */
            d = pow(10, d / 20);
            next += 2;
        } else if (*next >= 'E' && *next <= 'z') {
            int e= si_prefixes[*next - 'E'];
            if (e) {
                if (next[1] == 'i') {
                    d*= pow( 2, e/0.3);
                    next+=2;
                } else {
                    d*= pow(10, e);
                    next++;
                }
            }
        }

        if (*next=='B') {
            d*=8;
            next++;
        }
    }
    /* if requested, fill in tail with the position after the last parsed
       character */
    if (tail)
        *tail = next;
    return d;
}


void av_expr_free(AVExpr *e)
{
    if (!e) return;
    av_expr_free(e->param[0]);
    av_expr_free(e->param[1]);
    av_expr_free(e->param[2]);
    av_freep(&e->var);
    av_freep(&e);
}

static int parse_primary(AVExpr **e, Parser *p)
{
    AVExpr *d = av_mallocz(sizeof(AVExpr));
    char *next = p->s, *s0 = p->s;
    int ret, i;

    if (!d)
        return -1;

    /* number */
    d->value = av_strtod(p->s, &next);
    if (next != p->s) {
        d->type = e_value;
        p->s= next;
        *e = d;
        return 0;
    }
    d->value = 1;

    /* named constants */
    for (i=0; p->const_names && p->const_names[i]; i++) {
        if (strmatch(p->s, p->const_names[i])) {
            p->s+= strlen(p->const_names[i]);
            d->type = e_const;
            d->a.const_index = i;
            *e = d;
            return 0;
        }
    }
    for (i = 0; i < FF_ARRAY_ELEMS(constants); i++) {
        if (strmatch(p->s, constants[i].name)) {
            p->s += strlen(constants[i].name);
            d->type = e_value;
            d->value = constants[i].value;
            *e = d;
            return 0;
        }
    }

    p->s= strchr(p->s, '(');
    if (p->s==NULL) {
        LOGD("Undefined constant or missing '(' in '%s'\n", s0);
        p->s= next;
        av_expr_free(d);
        return -1;
    }
    p->s++; // "("
    if (*next == '(') { // special case do-nothing
        av_freep(&d);
        if ((ret = parse_expr(&d, p)) < 0)
            return ret;
        if (p->s[0] != ')') {
           LOGD( "Missing ')' in '%s'\n", s0);
            av_expr_free(d);
            return -1;
        }
        p->s++; // ")"
        *e = d;
        return 0;
    }
    if ((ret = parse_expr(&(d->param[0]), p)) < 0) {
        av_expr_free(d);
        return ret;
    }
    if (p->s[0]== ',') {
        p->s++; // ","
        parse_expr(&d->param[1], p);
    }
    if (p->s[0]== ',') {
        p->s++; // ","
        parse_expr(&d->param[2], p);
    }
    if (p->s[0] != ')') {
        LOGD( "Missing ')' or too many args in '%s'\n", s0);
        av_expr_free(d);
        return -1;
    }
    p->s++; // ")"

    d->type = e_func0;
         if (strmatch(next, "sinh"  )) d->a.func0 = sinh;
    else if (strmatch(next, "cosh"  )) d->a.func0 = cosh;
    else if (strmatch(next, "tanh"  )) d->a.func0 = tanh;
    else if (strmatch(next, "sin"   )) d->a.func0 = sin;
    else if (strmatch(next, "cos"   )) d->a.func0 = cos;
    else if (strmatch(next, "tan"   )) d->a.func0 = tan;
    else if (strmatch(next, "atan"  )) d->a.func0 = atan;
    else if (strmatch(next, "asin"  )) d->a.func0 = asin;
    else if (strmatch(next, "acos"  )) d->a.func0 = acos;
    else if (strmatch(next, "exp"   )) d->a.func0 = exp;
    else if (strmatch(next, "log"   )) d->a.func0 = log;
    else if (strmatch(next, "abs"   )) d->a.func0 = fabs;
    else if (strmatch(next, "time"  )) d->a.func0 = etime;
    else if (strmatch(next, "squish")) d->type = e_squish;
    else if (strmatch(next, "gauss" )) d->type = e_gauss;
    else if (strmatch(next, "mod"   )) d->type = e_mod;
    else if (strmatch(next, "max"   )) d->type = e_max;
    else if (strmatch(next, "min"   )) d->type = e_min;
    else if (strmatch(next, "eq"    )) d->type = e_eq;
    else if (strmatch(next, "gte"   )) d->type = e_gte;
    else if (strmatch(next, "gt"    )) d->type = e_gt;
    else if (strmatch(next, "lte"   )) d->type = e_lte;
    else if (strmatch(next, "lt"    )) d->type = e_lt;
    else if (strmatch(next, "ld"    )) d->type = e_ld;
    else if (strmatch(next, "isnan" )) d->type = e_isnan;
    else if (strmatch(next, "isinf" )) d->type = e_isinf;
    else if (strmatch(next, "st"    )) d->type = e_st;
    else if (strmatch(next, "while" )) d->type = e_while;
    else if (strmatch(next, "taylor")) d->type = e_taylor;
    else if (strmatch(next, "root"  )) d->type = e_root;
    else if (strmatch(next, "floor" )) d->type = e_floor;
    else if (strmatch(next, "ceil"  )) d->type = e_ceil;
    else if (strmatch(next, "trunc" )) d->type = e_trunc;
    else if (strmatch(next, "sqrt"  )) d->type = e_sqrt;
    else if (strmatch(next, "not"   )) d->type = e_not;
    else if (strmatch(next, "pow"   )) d->type = e_pow;
    else if (strmatch(next, "print" )) d->type = e_print;
    else if (strmatch(next, "random")) d->type = e_random;
    else if (strmatch(next, "hypot" )) d->type = e_hypot;
    else if (strmatch(next, "gcd"   )) d->type = e_gcd;
    else if (strmatch(next, "if"    )) d->type = e_if;
    else if (strmatch(next, "ifnot" )) d->type = e_ifnot;
    else {
        for (i=0; p->func1_names && p->func1_names[i]; i++) {
            if (strmatch(next, p->func1_names[i])) {
                d->a.func1 = p->funcs1[i];
                d->type = e_func1;
                *e = d;
                return 0;
            }
        }

        for (i=0; p->func2_names && p->func2_names[i]; i++) {
            if (strmatch(next, p->func2_names[i])) {
                d->a.func2 = p->funcs2[i];
                d->type = e_func2;
                *e = d;
                return 0;
            }
        }

        LOGD( "Unknown function in '%s'\n", s0);
        av_expr_free(d);
        return -1;
    }

    *e = d;
    return 0;
}


static AVExpr *new_eval_expr(int type, int value, AVExpr *p0, AVExpr *p1)
{
    AVExpr *e = av_mallocz(sizeof(AVExpr));
    if (!e)
        return NULL;
    e->type     =type   ;
    e->value    =value  ;
    e->param[0] =p0     ;
    e->param[1] =p1     ;
    return e;
}


static int parse_pow(AVExpr **e, Parser *p, int *sign)
{
    *sign= (*p->s == '+') - (*p->s == '-');
    p->s += *sign&1;
    return parse_primary(e, p);
}



static int parse_dB(AVExpr **e, Parser *p, int *sign)
{
	LOGD("parse_dB\n");
    /* do not filter out the negative sign when parsing a dB value.
       for example, -3dB is not the same as -(3dB) */
//    if (*p->s == '-') {//nextが初期化されていなかった→やることが不明
//    	LOGD("parse_dB *p->s == '-'\n");
//        char *next;
////        double av_unused v = strtod(p->s, &next);
//        if (next != p->s && next[0] == 'd' && next[1] == 'B') {
//            *sign = 0;
//            return parse_primary(e, p);
//        }
//    }
    return parse_pow(e, p, sign);
}

static int parse_factor(AVExpr **e, Parser *p)
{
    int sign, sign2, ret;
    AVExpr *e0, *e1, *e2;
    if ((ret = parse_dB(&e0, p, &sign)) < 0)
        return ret;
    while(p->s[0]=='^'){
        e1 = e0;
        p->s++;
        if ((ret = parse_dB(&e2, p, &sign2)) < 0) {
            av_expr_free(e1);
            return ret;
        }
        e0 = new_eval_expr(e_pow, 1, e1, e2);
        if (!e0) {
            av_expr_free(e1);
            av_expr_free(e2);
            return -1;
        }
        if (e0->param[1]) e0->param[1]->value *= (sign2|1);
    }
    if (e0) e0->value *= (sign|1);

    *e = e0;
    return 0;
}




static int parse_term(AVExpr **e, Parser *p)
{
    int ret;
    AVExpr *e0, *e1, *e2;
    if ((ret = parse_factor(&e0, p)) < 0)
        return ret;
    while (p->s[0]=='*' || p->s[0]=='/') {
        int c= *p->s++;
        e1 = e0;
        if ((ret = parse_factor(&e2, p)) < 0) {
            av_expr_free(e1);
            return ret;
        }
        e0 = new_eval_expr(c == '*' ? e_mul : e_div, 1, e1, e2);
        if (!e0) {
            av_expr_free(e1);
            av_expr_free(e2);
            return -1;
        }
    }
    *e = e0;
    return 0;
}


static int parse_subexpr(AVExpr **e, Parser *p)
{
    int ret;
    AVExpr *e0, *e1, *e2;
    if ((ret = parse_term(&e0, p)) < 0)
        return ret;
    while (*p->s == '+' || *p->s == '-') {
        e1 = e0;
        if ((ret = parse_term(&e2, p)) < 0) {
            av_expr_free(e1);
            return ret;
        }
        e0 = new_eval_expr(e_add, 1, e1, e2);
        if (!e0) {
            av_expr_free(e1);
            av_expr_free(e2);
            return -1;
        }
    };

    *e = e0;
    return 0;
}

int parse_expr(AVExpr **e, Parser *p)
{
    int ret;
    AVExpr *e0, *e1, *e2;
    if (p->stack_index <= 0) //protect against stack overflows
        return -1;
    p->stack_index--;

    if ((ret = parse_subexpr(&e0, p)) < 0)
        return ret;
    while (*p->s == ';') {
        p->s++;
        e1 = e0;
        if ((ret = parse_subexpr(&e2, p)) < 0) {
            av_expr_free(e1);
            return ret;
        }
        e0 = new_eval_expr(e_last, 1, e1, e2);
        if (!e0) {
            av_expr_free(e1);
            av_expr_free(e2);
            return -1;
        }
    };

    p->stack_index++;
    *e = e0;
    return 0;
}

static int verify_expr(AVExpr *e)
{
    if (!e) return 0;
    switch (e->type) {
        case e_value:
        case e_const: return 1;
        case e_func0:
        case e_func1:
        case e_squish:
        case e_ld:
        case e_gauss:
        case e_isnan:
        case e_isinf:
        case e_floor:
        case e_ceil:
        case e_trunc:
        case e_sqrt:
        case e_not:
        case e_random:
            return verify_expr(e->param[0]) && !e->param[1];
        case e_print:
            return verify_expr(e->param[0])
                   && (!e->param[1] || verify_expr(e->param[1]));
        case e_if:
        case e_ifnot:
        case e_taylor:
            return verify_expr(e->param[0]) && verify_expr(e->param[1])
                   && (!e->param[2] || verify_expr(e->param[2]));
        default: return verify_expr(e->param[0]) && verify_expr(e->param[1]) && !e->param[2];
    }
}

int av_expr_parse(AVExpr **expr, const char *s,
                  const char * const *const_names,
                  const char * const *func1_names, double (* const *funcs1)(void *, double),
                  const char * const *func2_names, double (* const *funcs2)(void *, double, double),
                  int log_offset, void *log_ctx)
{
    Parser p = { 0 };
    AVExpr *e = NULL;
    char *w = av_malloc(strlen(s) + 1);
    char *wp = w;
    const char *s0 = s;
    int ret = 0;

    if (!w)
        return -1;

    while (*s)
        if (!av_isspace(*s++)) *wp++ = s[-1];
    *wp++ = 0;

    p.stack_index=100;
    p.s= w;
    p.const_names = const_names;
    p.funcs1      = funcs1;
    p.func1_names = func1_names;
    p.funcs2      = funcs2;
    p.func2_names = func2_names;
    p.log_offset = log_offset;
    p.log_ctx    = log_ctx;

    if ((ret = parse_expr(&e, &p)) < 0)
        goto end;
    if (*p.s) {
        av_expr_free(e);
        LOGD( "Invalid chars '%s' at the end of expression '%s'\n", p.s, s0);
        ret = -1;
        goto end;
    }
    if (!verify_expr(e)) {
        av_expr_free(e);
        ret = -1;
        goto end;
    }
    e->var= av_mallocz(sizeof(double) *VARS);
    *expr = e;
end:
    av_free(w);
    return ret;
}


static double eval_expr(Parser *p, AVExpr *e)
{
//	LOGD("eval_expr Called type:%d\n",e->type);
    switch (e->type) {
        case e_value:  return e->value;
        case e_const: {
        	return e->value * p->const_values[e->a.const_index];
        }
        case e_func0:  return e->value * e->a.func0(eval_expr(p, e->param[0]));
        case e_func1:  return e->value * e->a.func1(p->opaque, eval_expr(p, e->param[0]));
        case e_func2:  return e->value * e->a.func2(p->opaque, eval_expr(p, e->param[0]), eval_expr(p, e->param[1]));
        case e_squish: return 1/(1+exp(4*eval_expr(p, e->param[0])));
        case e_gauss: { double d = eval_expr(p, e->param[0]); return exp(-d*d/2)/sqrt(2*M_PI); }
        case e_ld:     return e->value * p->var[av_clip_c(eval_expr(p, e->param[0]), 0, VARS-1)];
        case e_isnan:  return e->value * !!isnan(eval_expr(p, e->param[0]));
        case e_isinf:  return e->value * !!isinf(eval_expr(p, e->param[0]));
        case e_floor:  return e->value * floor(eval_expr(p, e->param[0]));
        case e_ceil :  return e->value * ceil (eval_expr(p, e->param[0]));
        case e_trunc:  return e->value * trunc(eval_expr(p, e->param[0]));
        case e_sqrt:   return e->value * sqrt (eval_expr(p, e->param[0]));
        case e_not:    return e->value * (eval_expr(p, e->param[0]) == 0);
        case e_if:     return e->value * (eval_expr(p, e->param[0]) ? eval_expr(p, e->param[1]) :
                                          e->param[2] ? eval_expr(p, e->param[2]) : 0);
        case e_ifnot:  return e->value * (!eval_expr(p, e->param[0]) ? eval_expr(p, e->param[1]) :
                                          e->param[2] ? eval_expr(p, e->param[2]) : 0);
        case e_print: {
            double x = eval_expr(p, e->param[0]);
//            int level = e->param[1] ? av_clip(eval_expr(p, e->param[1]), INT_MIN, INT_MAX) : AV_LOG_INFO;
//            av_log(p, level, "%f\n", x);
            return x;
        }
        case e_random:{
            int idx= av_clip_c(eval_expr(p, e->param[0]), 0, VARS-1);
            uint64_t r= isnan(p->var[idx]) ? 0 : p->var[idx];
            r= r*1664525+1013904223;
            p->var[idx]= r;
            return e->value * (r * (1.0/UINT64_MAX));
        }
        case e_while: {
            double d = NAN;
            while (eval_expr(p, e->param[0]))
                d=eval_expr(p, e->param[1]);
            return d;
        }
        case e_taylor: {
            double t = 1, d = 0, v;
            double x = eval_expr(p, e->param[1]);
            int id = e->param[2] ? av_clip_c(eval_expr(p, e->param[2]), 0, VARS-1) : 0;
            int i;
            double var0 = p->var[id];
            for(i=0; i<1000; i++) {
                double ld = d;
                p->var[id] = i;
                v = eval_expr(p, e->param[0]);
                d += t*v;
                if(ld==d && v)
                    break;
                t *= x / (i+1);
            }
            p->var[id] = var0;
            return d;
        }
        case e_root: {
            int i, j;
            double low = -1, high = -1, v, low_v = -DBL_MAX, high_v = DBL_MAX;
            double var0 = p->var[0];
            double x_max = eval_expr(p, e->param[1]);
            for(i=-1; i<1024; i++) {
                if(i<255) {
                    p->var[0] = av_reverse[i&255]*x_max/255;
                } else {
                    p->var[0] = x_max*pow(0.9, i-255);
                    if (i&1) p->var[0] *= -1;
                    if (i&2) p->var[0] += low;
                    else     p->var[0] += high;
                }
                v = eval_expr(p, e->param[0]);
                if (v<=0 && v>low_v) {
                    low    = p->var[0];
                    low_v  = v;
                }
                if (v>=0 && v<high_v) {
                    high   = p->var[0];
                    high_v = v;
                }
                if (low>=0 && high>=0){
                    for (j=0; j<1000; j++) {
                        p->var[0] = (low+high)*0.5;
                        if (low == p->var[0] || high == p->var[0])
                            break;
                        v = eval_expr(p, e->param[0]);
                        if (v<=0) low = p->var[0];
                        if (v>=0) high= p->var[0];
                        if (isnan(v)) {
                            low = high = v;
                            break;
                        }
                    }
                    break;
                }
            }
            p->var[0] = var0;
            return -low_v<high_v ? low : high;
        }
        default: {
            double d = eval_expr(p, e->param[0]);
            double d2 = eval_expr(p, e->param[1]);
            switch (e->type) {
                case e_mod:{
            		LOGD("eval_expr e_mod\n");
                	return e->value * (d - floor((!CONFIG_FTRAPV || d2) ? d / d2 : d * INFINITY) * d2);
                }
                case e_gcd:{
            		LOGD("eval_expr e_god\n");
            		return e->value * av_gcd(d,d2);
                }
                case e_max: return e->value * (d >  d2 ?   d : d2);
                case e_min: return e->value * (d <  d2 ?   d : d2);
                case e_eq:  return e->value * (d == d2 ? 1.0 : 0.0);
                case e_gt:  return e->value * (d >  d2 ? 1.0 : 0.0);
                case e_gte: return e->value * (d >= d2 ? 1.0 : 0.0);
                case e_lt:  return e->value * (d <  d2 ? 1.0 : 0.0);
                case e_lte:{
            		return e->value * (d <= d2 ? 1.0 : 0.0);
                }
                case e_pow://18か?
                	{
                		return e->value * pow(d, d2);
                	}
                case e_mul: return e->value * (d * d2);
                case e_div: return e->value * ((!CONFIG_FTRAPV || d2 ) ? (d / d2) : d * INFINITY);
                case e_add: return e->value * (d + d2);
                case e_last:return e->value * d2;
                case e_st : return e->value * (p->var[av_clip_c(d, 0, VARS-1)]= d2);
                case e_hypot:return e->value * (sqrt(d*d + d2*d2));
            }
        }
    }
    return NAN;
}
double av_expr_eval(AVExpr *e, const double *const_values, void *opaque)
{
    Parser p = { 0 };
    p.var= e->var;

//    if(const_values){
//    LOGD("av_expr_eval const_values:%e\n",*const_values);
//    }
    p.const_values = const_values;
    p.opaque     = opaque;
    return eval_expr(&p, e);
}

/**
 * Get the qmin & qmax for pict_type.
 */
static void get_qminmax(int *qmin_ret, int *qmax_ret, MpegEncContext *s, int pict_type)
{
    int qmin = s->avctx->lmin;
    int qmax = s->avctx->lmax;

//    assert(qmin <= qmax);

    switch (pict_type) {
    case AV_PICTURE_TYPE_B:
        qmin = (int)(qmin * FFABS(s->avctx->b_quant_factor) + s->avctx->b_quant_offset + 0.5);
        qmax = (int)(qmax * FFABS(s->avctx->b_quant_factor) + s->avctx->b_quant_offset + 0.5);
        break;
    case AV_PICTURE_TYPE_I:
        qmin = (int)(qmin * FFABS(s->avctx->i_quant_factor) + s->avctx->i_quant_offset + 0.5);
        qmax = (int)(qmax * FFABS(s->avctx->i_quant_factor) + s->avctx->i_quant_offset + 0.5);
        break;
    }

    qmin = av_clip_c(qmin, 1, FF_LAMBDA_MAX);
    qmax = av_clip_c(qmax, 1, FF_LAMBDA_MAX);

    if (qmax < qmin)
        qmax = qmin;

    *qmin_ret = qmin;
    *qmax_ret = qmax;
}


static double modify_qscale(MpegEncContext *s, RateControlEntry *rce,
                            double q, int frame_num)
{
    RateControlContext *rcc  = &s->rc_context;
    const double buffer_size = s->avctx->rc_buffer_size;
    const double fps         = get_fps(s->avctx);
    const double min_rate    = s->avctx->rc_min_rate / fps;
    const double max_rate    = s->avctx->rc_max_rate / fps;
    const int pict_type      = rce->new_pict_type;
    int qmin, qmax;

    get_qminmax(&qmin, &qmax, s, pict_type);

    /* modulation */
    if (s->avctx->rc_qmod_freq &&
        frame_num % s->avctx->rc_qmod_freq == 0 &&
        pict_type == AV_PICTURE_TYPE_P)
        q *= s->avctx->rc_qmod_amp;

    /* buffer overflow/underflow protection */
    if (buffer_size) {
        double expected_size = rcc->buffer_index;
        double q_limit;

        if (min_rate) {
            double d = 2 * (buffer_size - expected_size) / buffer_size;
            if (d > 1.0)
                d = 1.0;
            else if (d < 0.0001)
                d = 0.0001;
            q *= pow(d, 1.0 / s->avctx->rc_buffer_aggressivity);

            q_limit = bits2qp(rce,
                              FFMAX((min_rate - buffer_size + rcc->buffer_index) *
                                    s->avctx->rc_min_vbv_overflow_use, 1));

            if (q > q_limit) {
                if (s->avctx->debug & FF_DEBUG_RC)
                    LOGD( "limiting QP %f -> %f\n", q, q_limit);
                q = q_limit;
            }
        }

        if (max_rate) {
            double d = 2 * expected_size / buffer_size;
            if (d > 1.0)
                d = 1.0;
            else if (d < 0.0001)
                d = 0.0001;
            q /= pow(d, 1.0 / s->avctx->rc_buffer_aggressivity);

            q_limit = bits2qp(rce,
                              FFMAX(rcc->buffer_index *
                                    s->avctx->rc_max_available_vbv_use,
                                    1));
            if (q < q_limit) {
                if (s->avctx->debug & FF_DEBUG_RC)
                    LOGD(  "limiting QP %f -> %f\n", q, q_limit);
                q = q_limit;
            }
        }
    }
    LOGD( "q:%f max:%f min:%f size:%f index:%f agr:%f\n",
            q, max_rate, min_rate, buffer_size, rcc->buffer_index,
            s->avctx->rc_buffer_aggressivity);
    if (s->avctx->rc_qsquish == 0.0 || qmin == qmax) {
        if (q < qmin)
            q = qmin;
        else if (q > qmax)
            q = qmax;
    } else {
        double min2 = log(qmin);
        double max2 = log(qmax);

        q  = log(q);
        q  = (q - min2) / (max2 - min2) - 0.5;
        q *= -4.0;
        q  = 1.0 / (1.0 + exp(q));
        q  = q * (max2 - min2) + min2;

        q = exp(q);
    }

    return q;
}

static double get_diff_limited_q(MpegEncContext *s, RateControlEntry *rce, double q)
{
    RateControlContext *rcc   = &s->rc_context;
    AVCodecContext *a         = s->avctx;
    const int pict_type       = rce->new_pict_type;
    const double last_p_q     = rcc->last_qscale_for[AV_PICTURE_TYPE_P];
    const double last_non_b_q = rcc->last_qscale_for[rcc->last_non_b_pict_type];

    if (pict_type == AV_PICTURE_TYPE_I &&
        (a->i_quant_factor > 0.0 || rcc->last_non_b_pict_type == AV_PICTURE_TYPE_P))
        q = last_p_q * FFABS(a->i_quant_factor) + a->i_quant_offset;
    else if (pict_type == AV_PICTURE_TYPE_B &&
             a->b_quant_factor > 0.0)
        q = last_non_b_q * a->b_quant_factor + a->b_quant_offset;
    if (q < 1)
        q = 1;

    /* last qscale / qdiff stuff */
    if (rcc->last_non_b_pict_type == pict_type || pict_type != AV_PICTURE_TYPE_I) {
        double last_q     = rcc->last_qscale_for[pict_type];
        const int maxdiff = FF_QP2LAMBDA * a->max_qdiff;

        if (q > last_q + maxdiff)
            q = last_q + maxdiff;
        else if (q < last_q - maxdiff)
            q = last_q - maxdiff;
    }

    rcc->last_qscale_for[pict_type] = q; // Note we cannot do that after blurring

    if (pict_type != AV_PICTURE_TYPE_B)
        rcc->last_non_b_pict_type = pict_type;

    return q;
}

//2-pass code
static int init_pass2(MpegEncContext *s)
{
    RateControlContext *rcc = &s->rc_context;
    AVCodecContext *a       = s->avctx;
    int i, toobig;
    double fps             = get_fps(s->avctx);
    double complexity[5]   = { 0 }; // approximate bits at quant=1
    uint64_t const_bits[5] = { 0 }; // quantizer independent bits
    uint64_t all_const_bits;
    uint64_t all_available_bits = (uint64_t)(s->bit_rate *
                                             (double)rcc->num_entries / fps);
    double rate_factor          = 0;
    double step;
    const int filter_size = (int)(a->qblur * 4) | 1;
    double expected_bits;
    double *qscale, *blurred_qscale, qscale_sum;

    /* find complexity & const_bits & decide the pict_types */
    for (i = 0; i < rcc->num_entries; i++) {
        RateControlEntry *rce = &rcc->entry[i];

        rce->new_pict_type                = rce->pict_type;
        rcc->i_cplx_sum[rce->pict_type]  += rce->i_tex_bits * rce->qscale;
        rcc->p_cplx_sum[rce->pict_type]  += rce->p_tex_bits * rce->qscale;
        rcc->mv_bits_sum[rce->pict_type] += rce->mv_bits;
        rcc->frame_count[rce->pict_type]++;

        complexity[rce->new_pict_type] += (rce->i_tex_bits + rce->p_tex_bits) *
                                          (double)rce->qscale;
        const_bits[rce->new_pict_type] += rce->mv_bits + rce->misc_bits;
    }

    all_const_bits = const_bits[AV_PICTURE_TYPE_I] +
                     const_bits[AV_PICTURE_TYPE_P] +
                     const_bits[AV_PICTURE_TYPE_B];

    if (all_available_bits < all_const_bits) {
        LOGD("requested bitrate is too low\n");
        return -1;
    }

    qscale         = av_malloc(sizeof(double) * rcc->num_entries);
    blurred_qscale = av_malloc(sizeof(double) * rcc->num_entries);
    toobig = 0;

    for (step = 256 * 256; step > 0.0000001; step *= 0.5) {
        expected_bits = 0;
        rate_factor  += step;

        rcc->buffer_index = s->avctx->rc_buffer_size / 2;

        /* find qscale */
        for (i = 0; i < rcc->num_entries; i++) {
            RateControlEntry *rce = &rcc->entry[i];

            qscale[i] = get_qscale(s, &rcc->entry[i], rate_factor, i);
            rcc->last_qscale_for[rce->pict_type] = qscale[i];
        }
//        assert(filter_size % 2 == 1);

        /* fixed I/B QP relative to P mode */
        for (i = FFMAX(0, rcc->num_entries - 300); i < rcc->num_entries; i++) {
            RateControlEntry *rce = &rcc->entry[i];

            qscale[i] = get_diff_limited_q(s, rce, qscale[i]);
        }

        for (i = rcc->num_entries - 1; i >= 0; i--) {
            RateControlEntry *rce = &rcc->entry[i];

            qscale[i] = get_diff_limited_q(s, rce, qscale[i]);
        }

        /* smooth curve */
        for (i = 0; i < rcc->num_entries; i++) {
            RateControlEntry *rce = &rcc->entry[i];
            const int pict_type   = rce->new_pict_type;
            int j;
            double q = 0.0, sum = 0.0;

            for (j = 0; j < filter_size; j++) {
                int index    = i + j - filter_size / 2;
                double d     = index - i;
                double coeff = a->qblur == 0 ? 1.0 : exp(-d * d / (a->qblur * a->qblur));

                if (index < 0 || index >= rcc->num_entries)
                    continue;
                if (pict_type != rcc->entry[index].new_pict_type)
                    continue;
                q   += qscale[index] * coeff;
                sum += coeff;
            }
            blurred_qscale[i] = q / sum;
        }

        /* find expected bits */
        for (i = 0; i < rcc->num_entries; i++) {
            RateControlEntry *rce = &rcc->entry[i];
            double bits;

            rce->new_qscale = modify_qscale(s, rce, blurred_qscale[i], i);

            bits  = qp2bits(rce, rce->new_qscale) + rce->mv_bits + rce->misc_bits;
            bits += 8 * ff_vbv_update(s, bits);

            rce->expected_bits = expected_bits;
            expected_bits     += bits;
        }

        LOGD( "expected_bits: %f all_available_bits: %d rate_factor: %f\n",
                expected_bits, (int)all_available_bits, rate_factor);
        if (expected_bits > all_available_bits) {
            rate_factor -= step;
            ++toobig;
        }
    }
    av_free(qscale);
    av_free(blurred_qscale);

    /* check bitrate calculations and print info */
    qscale_sum = 0.0;
    for (i = 0; i < rcc->num_entries; i++) {
        LOGD( "[lavc rc] entry[%d].new_qscale = %.3f  qp = %.3f\n",
                i,
                rcc->entry[i].new_qscale,
                rcc->entry[i].new_qscale / FF_QP2LAMBDA);
        qscale_sum += av_clip_c(rcc->entry[i].new_qscale / FF_QP2LAMBDA,
                              s->avctx->qmin, s->avctx->qmax);
    }
//    assert(toobig <= 40);
    LOGD("[lavc rc] requested bitrate: %d bps  expected bitrate: %d bps\n",
           s->bit_rate,
           (int)(expected_bits / ((double)all_available_bits / s->bit_rate)));
    LOGD("[lavc rc] estimated target average qp: %.3f\n",
           (float)qscale_sum / rcc->num_entries);
    if (toobig == 0) {
    	LOGD(  "[lavc rc] Using all of requested bitrate is not "
               "necessary for this video with these parameters.\n");
    } else if (toobig == 40) {
    	LOGD( "[lavc rc] Error: bitrate too low for this video "
               "with these parameters.\n");
        return -1;
    } else if (fabs(expected_bits / all_available_bits - 1.0) > 0.01) {
    	LOGD(  "[lavc rc] Error: 2pass curve failed to converge\n");
        return -1;
    }

    return 0;
}

int ff_rate_control_init(MpegEncContext *s)
{
	LOGD("ff_rate_control_init Called\n");
    RateControlContext *rcc = &s->rc_context;
    int i, res;
    static const char * const const_names[] = {
        "PI",
        "E",
        "iTex",
        "pTex",
        "tex",
        "mv",
        "fCode",
        "iCount",
        "mcVar",
        "var",
        "isI",
        "isP",
        "isB",
        "avgQP",
        "qComp",
#if 0
        "lastIQP",
        "lastPQP",
        "lastBQP",
        "nextNonBQP",
#endif
        "avgIITex",
        "avgPITex",
        "avgPPTex",
        "avgBPTex",
        "avgTex",
        NULL
    };
    static double (* const func1[])(void *, double) = {
        (void *)bits2qp,
        (void *)qp2bits,
        NULL
    };
    static const char * const func1_names[] = {
        "bits2qp",
        "qp2bits",
        NULL
    };

    if (!s->avctx->rc_max_available_vbv_use && s->avctx->rc_buffer_size) {
        if (s->avctx->rc_max_rate) {
            s->avctx->rc_max_available_vbv_use = av_clipf_c(s->avctx->rc_max_rate/(s->avctx->rc_buffer_size*get_fps(s->avctx)), 1.0/3, 1.0);
        } else
            s->avctx->rc_max_available_vbv_use = 1.0;
    }

    res = av_expr_parse(&rcc->rc_eq_eval,
                        s->avctx->rc_eq ? s->avctx->rc_eq : "tex^qComp",
                        const_names, func1_names, func1,
                        NULL, NULL, 0, s->avctx);
    if (res < 0) {
        LOGD( "Error parsing rc_eq \"%s\"\n", s->avctx->rc_eq);
        return res;
    }

    for (i = 0; i < 5; i++) {
        rcc->pred[i].coeff = FF_QP2LAMBDA * 7.0;
        rcc->pred[i].count = 1.0;
        rcc->pred[i].decay = 0.4;

        rcc->i_cplx_sum [i] =
        rcc->p_cplx_sum [i] =
        rcc->mv_bits_sum[i] =
        rcc->qscale_sum [i] =
        rcc->frame_count[i] = 1; // 1 is better because of 1/0 and such

        rcc->last_qscale_for[i] = FF_QP2LAMBDA * 5;
    }
    rcc->buffer_index = s->avctx->rc_initial_buffer_occupancy;
    if (!rcc->buffer_index)
        rcc->buffer_index = s->avctx->rc_buffer_size * 3 / 4;

    if (s->flags & CODEC_FLAG_PASS2) {
        int i;
        char *p;

        /* find number of pics */
        p = s->avctx->stats_in;
        for (i = -1; p; i++)
            p = strchr(p + 1, ';');
        i += s->max_b_frames;
        if (i <= 0 || i >= __INT_MAX__ / sizeof(RateControlEntry))
            return -1;
        rcc->entry       = av_mallocz(i * sizeof(RateControlEntry));
        rcc->num_entries = i;

        /* init all to skipped p frames
         * (with b frames we might have a not encoded frame at the end FIXME) */
        for (i = 0; i < rcc->num_entries; i++) {//呼ばれてないっぽい
            RateControlEntry *rce = &rcc->entry[i];
            LOGD("ff_rate_control_init mb_num:%d\n",s->mb_num);
            rce->pict_type  = rce->new_pict_type = AV_PICTURE_TYPE_P;
            rce->qscale     = rce->new_qscale    = FF_QP2LAMBDA * 2;
            rce->misc_bits  = s->mb_num + 10;
            rce->mb_var_sum = s->mb_num * 100;
        }

        /* read stats */
        p = s->avctx->stats_in;
        for (i = 0; i < rcc->num_entries - s->max_b_frames; i++) {
            RateControlEntry *rce;
            int picture_number;
            int e;
            char *next;

            next = strchr(p, ';');
            if (next) {
                (*next) = 0; // sscanf in unbelievably slow on looong strings // FIXME copy / do not write
                next++;
            }
            e = sscanf(p, " in:%d ", &picture_number);

//            assert(picture_number >= 0);
//            assert(picture_number < rcc->num_entries);
            rce = &rcc->entry[picture_number];

            e += sscanf(p, " in:%*d out:%*d type:%d q:%f itex:%d ptex:%d mv:%d misc:%d fcode:%d bcode:%d mc-var:%d var:%d icount:%d skipcount:%d hbits:%d",
                        &rce->pict_type, &rce->qscale, &rce->i_tex_bits, &rce->p_tex_bits,
                        &rce->mv_bits, &rce->misc_bits,
                        &rce->f_code, &rce->b_code,
                        &rce->mc_mb_var_sum, &rce->mb_var_sum,
                        &rce->i_count, &rce->skip_count, &rce->header_bits);
            if (e != 14) {
                LOGD( "statistics are damaged at line %d, parser out=%d\n",
                       i, e);
                return -1;
            }

            p = next;
        }

        if (init_pass2(s) < 0)
            return -1;

        // FIXME maybe move to end
        if ((s->flags & CODEC_FLAG_PASS2) && s->avctx->rc_strategy == FF_RC_STRATEGY_XVID) {
#if CONFIG_LIBXVID
            return ff_xvid_rate_control_init(s);
#else
            LOGD("Xvid ratecontrol requires libavcodec compiled with Xvid support.\n");
            return -1;
#endif
        }
    }

    if (!(s->flags & CODEC_FLAG_PASS2)) {
        rcc->short_term_qsum   = 0.001;
        rcc->short_term_qcount = 0.001;

        rcc->pass1_rc_eq_output_sum = 0.001;
        rcc->pass1_wanted_bits      = 0.001;

        if (s->avctx->qblur > 1.0) {
            LOGD( "qblur too large\n");
            return -1;
        }
        /* init stuff with the user specified complexity */
        if (s->avctx->rc_initial_cplx) {
            for (i = 0; i < 60 * 30; i++) {
                double bits = s->avctx->rc_initial_cplx * (i / 10000.0 + 1.0) * s->mb_num;
                RateControlEntry rce;

                if (i % ((s->gop_size + 3) / 4) == 0)
                    rce.pict_type = AV_PICTURE_TYPE_I;
                else if (i % (s->max_b_frames + 1))
                    rce.pict_type = AV_PICTURE_TYPE_B;
                else
                    rce.pict_type = AV_PICTURE_TYPE_P;

                rce.new_pict_type = rce.pict_type;
                rce.mc_mb_var_sum = bits * s->mb_num / 100000;
                rce.mb_var_sum    = s->mb_num;

                rce.qscale    = FF_QP2LAMBDA * 2;
                rce.f_code    = 2;
                rce.b_code    = 1;
                rce.misc_bits = 1;

                if (s->pict_type == AV_PICTURE_TYPE_I) {
                    rce.i_count    = s->mb_num;
                    LOGD("rate_init i_tex_bits:%d",bits);
                    rce.i_tex_bits = bits;
                    rce.p_tex_bits = 0;
                    rce.mv_bits    = 0;
                } else {
                    rce.i_count    = 0; // FIXME we do know this approx
                    LOGD("rate_init i_tex_bits:%d",0);
                    rce.i_tex_bits = 0;
                    rce.p_tex_bits = bits * 0.9;
                    rce.mv_bits    = bits * 0.1;
                }
                rcc->i_cplx_sum[rce.pict_type]  += rce.i_tex_bits * rce.qscale;
                rcc->p_cplx_sum[rce.pict_type]  += rce.p_tex_bits * rce.qscale;
                rcc->mv_bits_sum[rce.pict_type] += rce.mv_bits;
                rcc->frame_count[rce.pict_type]++;

                LOGD("rate_init A i_tex_bits:%d",rce.i_tex_bits);
                get_qscale(s, &rce, rcc->pass1_wanted_bits / rcc->pass1_rc_eq_output_sum, i);

                // FIXME misbehaves a little for variable fps
                rcc->pass1_wanted_bits += s->bit_rate / get_fps(s->avctx);
            }
        }
    }
    LOGD("rate_init E i_tex_b:%d  s->mb_num:%d get_fps:%e\n",s->i_tex_bits, s->mb_num,get_fps(s->avctx));
    return 0;
}


int ff_MPV_encode_init(AVCodecContext *avctx)
{
    LOGD( "ff_MPV_encode_init Called\n");
    MpegEncContext *s = avctx->priv_data;
    int i;
    int chroma_h_shift, chroma_v_shift;
    //重要デフォルトのMpegEncの処置
    s->y_dc_scale_table      =
    s->c_dc_scale_table      = ff_mpeg1_dc_scale_table;
    s->chroma_qscale_table   = ff_default_chroma_qscale_table;
    s->progressive_frame     = 1;
    s->progressive_sequence  = 1;
    s->picture_structure     = PICT_FRAME;
    s->coded_picture_number  = 0;
    s->picture_number        = 0;
    s->input_picture_number  = 0;
    s->picture_in_gop_number = 0;
    s->f_code                = 1;
    s->b_code                = 1;
    s->picture_range_start   = 0;
    //    s->picture_range_end     = MAX_PICTURE_COUNT;
    s->picture_range_end     = 20;//リアルタイムエンコでも、少ない数だとfind_unused_pictureでオーバーフローとして扱う
    s->slice_context_count   = 1;
    for (i = -16; i < 16; i++) {
        default_fcode_tab[i + MAX_MV] = 1;
    }
    s->me.mv_penalty = default_mv_penalty;
    s->fcode_tab     = default_fcode_tab;
    s->y_dc_scale_table      =
    s->c_dc_scale_table      = ff_mpeg1_dc_scale_table;
    s->chroma_qscale_table   = ff_default_chroma_qscale_table;
    s->progressive_frame     = 1;
    s->progressive_sequence  = 1;
    s->picture_structure     = PICT_FRAME;
    s->coded_picture_number  = 0;
    s->picture_number        = 0;
    s->input_picture_number  = 0;
    s->picture_in_gop_number = 0;
    s->f_code                = 1;
    s->b_code                = 1;
    s->picture_range_start   = 0;
    //    s->picture_range_end     = MAX_PICTURE_COUNT;
    s->picture_range_end     = 20;//リアルタイムエンコでも、少ない数だとfind_unused_pictureでオーバーフローとして扱う
    s->slice_context_count   = 1;
    s->bit_rate = avctx->bit_rate;
    s->width    = avctx->width;
    s->height   = avctx->height;

    if (avctx->gop_size > 600 &&
        avctx->strict_std_compliance > FF_COMPLIANCE_EXPERIMENTAL) {
        LOGD("keyframe interval too large!, reducing it from %d to %d\n",
               avctx->gop_size, 600);
        avctx->gop_size = 600;
    }
    s->gop_size     = avctx->gop_size;
    s->avctx        = avctx;
    s->flags        = avctx->flags;
    s->flags2       = avctx->flags2;
    s->max_b_frames = avctx->max_b_frames;
    s->codec_id     = AV_CODEC_ID_FLV1;

    switch (avctx->pix_fmt) {
    case AV_PIX_FMT_YUVJ444P:
//    case AV_PIX_FMT_YUV444P:
//        s->chroma_format = CHROMA_444;
//        break;
    case AV_PIX_FMT_YUVJ422P:
//    case AV_PIX_FMT_YUV422P:
//        s->chroma_format = CHROMA_422;
//        break;
    case AV_PIX_FMT_YUVJ420P:
    case AV_PIX_FMT_YUV420P:
    default:
        s->chroma_format = CHROMA_420;
        break;
    }

#if FF_API_MPV_GLOBAL_OPTS
    if (avctx->luma_elim_threshold)
        s->luma_elim_threshold   = avctx->luma_elim_threshold;
    if (avctx->chroma_elim_threshold)
        s->chroma_elim_threshold = avctx->chroma_elim_threshold;
#endif

    s->strict_std_compliance = avctx->strict_std_compliance;
    s->quarter_sample     = (avctx->flags & CODEC_FLAG_QPEL) != 0;
    s->mpeg_quant         = avctx->mpeg_quant;
    s->rtp_mode           = !!avctx->rtp_payload_size;
    s->intra_dc_precision = avctx->intra_dc_precision;
    s->user_specified_pts = AV_NOPTS_VALUE;

    if (s->gop_size <= 1) {
        s->intra_only = 1;
        s->gop_size   = 12;
    } else {
        s->intra_only = 0;
    }

    s->me_method = avctx->me_method;

    /* Fixed QSCALE */
    s->fixed_qscale = !!(avctx->flags & CODEC_FLAG_QSCALE);

#if FF_API_MPV_GLOBAL_OPTS
    if (s->flags & CODEC_FLAG_QP_RD)
        s->mpv_flags |= FF_MPV_FLAG_QP_RD;
#endif

    s->adaptive_quant = (s->avctx->lumi_masking ||
                         s->avctx->dark_masking ||
                         s->avctx->temporal_cplx_masking ||
                         s->avctx->spatial_cplx_masking  ||
                         s->avctx->p_masking      ||
                         s->avctx->border_masking ||
                         (s->mpv_flags & FF_MPV_FLAG_QP_RD)) &&
                        !s->fixed_qscale;

    s->loop_filter      = !!(s->flags & CODEC_FLAG_LOOP_FILTER);

    if (avctx->rc_max_rate && !avctx->rc_buffer_size) {
//        switch(avctx->codec_id) {
//        case AV_CODEC_ID_MPEG1VIDEO:
//        case AV_CODEC_ID_MPEG2VIDEO:
//            avctx->rc_buffer_size = FFMAX(avctx->rc_max_rate, 15000000) * 112L / 15000000 * 16384;
//            break;
//        case AV_CODEC_ID_MPEG4:
//        case AV_CODEC_ID_MSMPEG4V1:
//        case AV_CODEC_ID_MSMPEG4V2:
//        case AV_CODEC_ID_MSMPEG4V3:
//            if       (avctx->rc_max_rate >= 15000000) {
//                avctx->rc_buffer_size = 320 + (avctx->rc_max_rate - 15000000L) * (760-320) / (38400000 - 15000000);
//            } else if(avctx->rc_max_rate >=  2000000) {
//                avctx->rc_buffer_size =  80 + (avctx->rc_max_rate -  2000000L) * (320- 80) / (15000000 -  2000000);
//            } else if(avctx->rc_max_rate >=   384000) {
//                avctx->rc_buffer_size =  40 + (avctx->rc_max_rate -   384000L) * ( 80- 40) / ( 2000000 -   384000);
//            } else
//                avctx->rc_buffer_size = 40;
//            avctx->rc_buffer_size *= 16384;
//            break;
//        }
        if (avctx->rc_buffer_size) {
            LOGD("Automatically choosing VBV buffer size of %d kbyte\n", avctx->rc_buffer_size/8192);
        }
    }

    if ((!avctx->rc_max_rate) != (!avctx->rc_buffer_size)) {
        LOGD( "Either both buffer size and max rate or neither must be specified\n");
        if (avctx->rc_max_rate && !avctx->rc_buffer_size)
            return -1;
    }

    if (avctx->rc_min_rate && avctx->rc_max_rate != avctx->rc_min_rate) {
    	LOGD( "Warning min_rate > 0 but min_rate != max_rate isn't recommended!\n");
    }

    if (avctx->rc_min_rate && avctx->rc_min_rate > avctx->bit_rate) {
    	LOGD( "bitrate below min bitrate\n");
        return -1;
    }

    if (avctx->rc_max_rate && avctx->rc_max_rate < avctx->bit_rate) {
    	LOGD( "bitrate above max bitrate\n");
        return -1;
    }

    if (avctx->rc_max_rate &&
        avctx->rc_max_rate == avctx->bit_rate &&
        avctx->rc_max_rate != avctx->rc_min_rate) {
    	LOGD( "impossible bitrate constraints, this will fail\n");
    }

    if (avctx->rc_buffer_size &&
        avctx->bit_rate * (int64_t)avctx->time_base.num >
            avctx->rc_buffer_size * (int64_t)avctx->time_base.den) {
    	LOGD( "VBV buffer too small for bitrate\n");
        return -1;
    }

	LOGD( "fixed_qscale:%d bit_rate:%d\n",s->fixed_qscale,avctx->bit_rate);
	LOGD( "den :%d num:%d \n",avctx->time_base.den ,avctx->time_base.num);
	LOGD( "bit_rate_tolerance:%d \n",avctx->bit_rate_tolerance);

    if (!s->fixed_qscale && avctx->bit_rate * av_q2d(avctx->time_base) >
            avctx->bit_rate_tolerance) {
    	LOGD( "bitrate tolerance too small for bitrate\n");
        return -1;
    }

    if (s->avctx->rc_max_rate &&
        s->avctx->rc_min_rate == s->avctx->rc_max_rate &&
        (s->codec_id == AV_CODEC_ID_MPEG1VIDEO ||
         s->codec_id == AV_CODEC_ID_MPEG2VIDEO) &&
        90000LL * (avctx->rc_buffer_size - 1) >
            s->avctx->rc_max_rate * 0xFFFFLL) {
    	LOGD(  "Warning vbv_delay will be set to 0xFFFF (=VBR) as the "
               "specified vbv buffer is too large for the given bitrate!\n");
    }

//    if ((s->flags & CODEC_FLAG_4MV)  && s->codec_id != AV_CODEC_ID_MPEG4 &&
//        s->codec_id != AV_CODEC_ID_H263 && s->codec_id != AV_CODEC_ID_H263P &&
//        s->codec_id != AV_CODEC_ID_FLV1) {
//        av_log(avctx, AV_LOG_ERROR, "4MV not supported by codec\n");
//        return -1;
//    }

    if (s->obmc && s->avctx->mb_decision != FF_MB_DECISION_SIMPLE) {
        LOGD( "OBMC is only supported with simple mb decision\n");
        return -1;
    }

    if (s->quarter_sample && s->codec_id != AV_CODEC_ID_MPEG4) {
    	LOGD("qpel not supported by codec\n");
        return -1;
    }

    if (s->max_b_frames                    &&
        s->codec_id != AV_CODEC_ID_MPEG4      &&
        s->codec_id != AV_CODEC_ID_MPEG1VIDEO &&
        s->codec_id != AV_CODEC_ID_MPEG2VIDEO) {
    	LOGD( "b frames not supported by codec\n");
        return -1;
    }

    if ((s->codec_id == AV_CODEC_ID_MPEG4 ||
         s->codec_id == AV_CODEC_ID_H263  ||
         s->codec_id == AV_CODEC_ID_H263P) &&
        (avctx->sample_aspect_ratio.num > 255 ||
         avctx->sample_aspect_ratio.den > 255)) {
    	LOGD( "Invalid pixel aspect ratio %i/%i, limit is 255/255 reducing\n",
               avctx->sample_aspect_ratio.num, avctx->sample_aspect_ratio.den);
        av_reduce(&avctx->sample_aspect_ratio.num, &avctx->sample_aspect_ratio.den,
                   avctx->sample_aspect_ratio.num,  avctx->sample_aspect_ratio.den, 255);
    }

    if ((s->codec_id == AV_CODEC_ID_H263  ||
         s->codec_id == AV_CODEC_ID_H263P) &&
        (avctx->width  > 2048 ||
         avctx->height > 1152 )) {
    	LOGD("H.263 does not support resolutions above 2048x1152\n");
        return -1;
    }
    if ((s->codec_id == AV_CODEC_ID_H263  ||
         s->codec_id == AV_CODEC_ID_H263P) &&
        ((avctx->width &3) ||
         (avctx->height&3) )) {
    	LOGD("w/h must be a multiple of 4\n");
        return -1;
    }

    if (s->codec_id == AV_CODEC_ID_MPEG1VIDEO &&
        (avctx->width  > 4095 ||
         avctx->height > 4095 )) {
    	LOGD("MPEG-1 does not support resolutions above 4095x4095\n");
        return -1;
    }

    if (s->codec_id == AV_CODEC_ID_MPEG2VIDEO &&
        (avctx->width  > 16383 ||
         avctx->height > 16383 )) {
    	LOGD( "MPEG-2 does not support resolutions above 16383x16383\n");
        return -1;
    }

    if (s->codec_id == AV_CODEC_ID_RV10 &&
        ((avctx->width &15) ||
         (avctx->height&15) )) {
    	LOGD( "width and height must be a multiple of 16\n");
        return -1;
    }

    if (s->codec_id == AV_CODEC_ID_RV20 &&
        ((avctx->width &3) ||
         (avctx->height&3) )) {
    	LOGD( "width and height must be a multiple of 4\n");
        return -1;
    }

    if ((s->codec_id == AV_CODEC_ID_WMV1 ||
         s->codec_id == AV_CODEC_ID_WMV2) &&
         (avctx->width & 1)) {
    	LOGD("width must be multiple of 2\n");
         return -1;
    }

    if ((s->flags & (CODEC_FLAG_INTERLACED_DCT | CODEC_FLAG_INTERLACED_ME)) &&
        s->codec_id != AV_CODEC_ID_MPEG4 && s->codec_id != AV_CODEC_ID_MPEG2VIDEO) {
    	LOGD( "interlacing not supported by codec\n");
        return -1;
    }
    // mpeg2 uses that too
    if (s->mpeg_quant && s->codec_id != AV_CODEC_ID_MPEG4) {
    	LOGD( "mpeg2 style quantization not supported by codec\n");
        return -1;
    }

#if FF_API_MPV_GLOBAL_OPTS
    if (s->flags & CODEC_FLAG_CBP_RD)
        s->mpv_flags |= FF_MPV_FLAG_CBP_RD;
#endif

    if ((s->mpv_flags & FF_MPV_FLAG_CBP_RD) && !avctx->trellis) {
        LOGD( "CBP RD needs trellis quant\n");
        return -1;
    }

    if ((s->mpv_flags & FF_MPV_FLAG_QP_RD) &&
        s->avctx->mb_decision != FF_MB_DECISION_RD) {
    	LOGD("QP RD needs mbd=2\n");
        return -1;
    }

    if (s->avctx->scenechange_threshold < 1000000000 &&
        (s->flags & CODEC_FLAG_CLOSED_GOP)) {
    	LOGD("closed gop with scene change detection are not supported yet, "
               "set threshold to 1000000000\n");
        return -1;
    }

    if (s->flags & CODEC_FLAG_LOW_DELAY) {
        if (s->codec_id != AV_CODEC_ID_MPEG2VIDEO) {
        	LOGD("low delay forcing is only available for mpeg2\n");
            return -1;
        }
        if (s->max_b_frames != 0) {
            LOGD( "b frames cannot be used with low delay\n");
            return -1;
        }
    }

    if (s->q_scale_type == 1) {
        if (avctx->qmax > 12) {
        	LOGD("non linear quant only supports qmax <= 12 currently\n");
            return -1;
        }
    }

    if (s->avctx->thread_count > 1         &&
        s->codec_id != AV_CODEC_ID_MPEG4      &&
        s->codec_id != AV_CODEC_ID_MPEG1VIDEO &&
        s->codec_id != AV_CODEC_ID_MPEG2VIDEO &&
        s->codec_id != AV_CODEC_ID_MJPEG      &&
        (s->codec_id != AV_CODEC_ID_H263P)) {
    	LOGD("multi threaded encoding not supported by codec\n");
        return -1;
    }

    if (s->avctx->thread_count < 1) {
    	LOGD( "automatic thread number detection not supported by codec, "
               "patch welcome\n");
        return -1;
    }

    if (s->avctx->slices > 1 || s->avctx->thread_count > 1)
        s->rtp_mode = 1;

    if (s->avctx->thread_count > 1 && s->codec_id == AV_CODEC_ID_H263P)
        s->h263_slice_structured = 1;

    if (!avctx->time_base.den || !avctx->time_base.num) {
    	LOGD( "framerate not set\n");
        return -1;
    }

    i = (__INT_MAX__ / 2 + 128) >> 8;
    if (avctx->me_threshold >= i) {
    	LOGD( "me_threshold too large, max is %d\n",
               i - 1);
        return -1;
    }
    if (avctx->mb_threshold >= i) {
    	LOGD("mb_threshold too large, max is %d\n",
               i - 1);
        return -1;
    }

    if (avctx->b_frame_strategy && (avctx->flags & CODEC_FLAG_PASS2)) {
    	LOGD( "notice: b_frame_strategy only affects the first pass\n");
        avctx->b_frame_strategy = 0;
    }

    i = av_gcd(avctx->time_base.den, avctx->time_base.num);
    if (i > 1) {
    	LOGD("removing common factors from framerate\n");
        avctx->time_base.den /= i;
        avctx->time_base.num /= i;
        //return -1;
    }

    if (s->mpeg_quant || s->codec_id == AV_CODEC_ID_MPEG1VIDEO || s->codec_id == AV_CODEC_ID_MPEG2VIDEO || s->codec_id == AV_CODEC_ID_MJPEG || s->codec_id==AV_CODEC_ID_AMV) {
        // (a + x * 3 / 8) / x
        s->intra_quant_bias = 3 << (QUANT_BIAS_SHIFT - 3);
        s->inter_quant_bias = 0;
    } else {
        s->intra_quant_bias = 0;
        // (a - x / 4) / x
        s->inter_quant_bias = -(1 << (QUANT_BIAS_SHIFT - 2));
    }

    if (avctx->intra_quant_bias != FF_DEFAULT_QUANT_BIAS)
        s->intra_quant_bias = avctx->intra_quant_bias;
    if (avctx->inter_quant_bias != FF_DEFAULT_QUANT_BIAS)
        s->inter_quant_bias = avctx->inter_quant_bias;

    LOGD( "intra_quant_bias = %d inter_quant_bias = %d\n",s->intra_quant_bias,s->inter_quant_bias);

    avcodec_get_chroma_sub_sample(avctx->pix_fmt, &chroma_h_shift, &chroma_v_shift);

    if (avctx->codec_id == AV_CODEC_ID_MPEG4 &&
        s->avctx->time_base.den > (1 << 16) - 1) {
    	LOGD( "timebase %d/%d not supported by MPEG 4 standard, "
               "the maximum admitted value for the timebase denominator "
               "is %d\n", s->avctx->time_base.num, s->avctx->time_base.den,
               (1 << 16) - 1);
        return -1;
    }

    s->time_increment_bits = av_log2(s->avctx->time_base.den - 1) + 1;

    LOGD( "time_increment_bits end\n");
#if FF_API_MPV_GLOBAL_OPTS
    if (avctx->flags2 & CODEC_FLAG2_SKIP_RD)
        s->mpv_flags |= FF_MPV_FLAG_SKIP_RD;
    if (avctx->flags2 & CODEC_FLAG2_STRICT_GOP)
        s->mpv_flags |= FF_MPV_FLAG_STRICT_GOP;
    if (avctx->quantizer_noise_shaping)
        s->quantizer_noise_shaping = avctx->quantizer_noise_shaping;
#endif


    s->out_format      = FMT_H263;
    s->h263_flv        = 2; /* format = 1; 11-bit codes */
    s->unrestricted_mv = 1;
    s->rtp_mode  = 0; /* don't allow GOB */
    avctx->delay = 0;
    s->low_delay = 1;

    LOGD( "switch end\n");
    avctx->has_b_frames = !s->low_delay;

    s->encoding = 1;

    s->progressive_frame    =
    s->progressive_sequence = !(((avctx->flags & (CODEC_FLAG_INTERLACED_DCT)) |
                                                CODEC_FLAG_INTERLACED_ME) ||
                                s->alternate_scan);

    /* init */
    if (ff_MPV_common_init(s) < 0)
        return -1;
    LOGD("ff_MPV_encode_init thread_context[0]:%d\n",&s->thread_context[0]->dsp);
    ff_dct_encode_init(s);
    if ( s->modified_quant)
        s->chroma_qscale_table = ff_h263_chroma_qscale_table;

    s->quant_precision = 5;

    LOGD("set_cmp_before\n");
    ff_set_cmp(&s->dsp, s->dsp.ildct_cmp, s->avctx->ildct_cmp);
    ff_set_cmp(&s->dsp, s->dsp.frame_skip_cmp, s->avctx->frame_skip_cmp);

    if (s->out_format == FMT_H263)
    	h263_encode_init(s);

    /* init q matrix */
    for (i = 0; i < 64; i++) {
        int j = s->dsp.idct_permutation[i];
        if (s->codec_id == AV_CODEC_ID_MPEG4 &&
            s->mpeg_quant) {
            s->intra_matrix[j] = ff_mpeg4_default_intra_matrix[i];
            s->inter_matrix[j] = ff_mpeg4_default_non_intra_matrix[i];
        } else if (s->out_format == FMT_H263 || s->out_format == FMT_H261) {
            s->intra_matrix[j] =
            s->inter_matrix[j] = ff_mpeg1_default_non_intra_matrix[i];
        } else {
            /* mpeg1/2 */
            s->intra_matrix[j] = ff_mpeg1_default_intra_matrix[i];
            s->inter_matrix[j] = ff_mpeg1_default_non_intra_matrix[i];
        }
        if (s->avctx->intra_matrix)
            s->intra_matrix[j] = s->avctx->intra_matrix[i];
        if (s->avctx->inter_matrix)
            s->inter_matrix[j] = s->avctx->inter_matrix[i];
    }

    /* precompute matrix */
    /* for mjpeg, we do include qscale in the matrix */
    if (s->out_format != FMT_MJPEG) {
        ff_convert_matrix(&s->dsp, s->q_intra_matrix, s->q_intra_matrix16,
                          s->intra_matrix, s->intra_quant_bias, avctx->qmin,
                          31, 1);
        ff_convert_matrix(&s->dsp, s->q_inter_matrix, s->q_inter_matrix16,
                          s->inter_matrix, s->inter_quant_bias, avctx->qmin,
                          31, 0);
    }

    if (ff_rate_control_init(s) < 0)
        return -1;

    LOGD( "ff_MPV_common_init end dsp:%d pix_sum:%d\n", &s->dsp,s->dsp.pix_sum);
    return 0;
}


void ff_rate_control_uninit(MpegEncContext *s)
{
    RateControlContext *rcc = &s->rc_context;


    av_expr_free(rcc->rc_eq_eval);
    av_freep(&rcc->entry);

#if CONFIG_LIBXVID
    if ((s->flags & CODEC_FLAG_PASS2) && s->avctx->rc_strategy == FF_RC_STRATEGY_XVID)
        ff_xvid_rate_control_uninit(s);
#endif
}

void ff_mjpeg_encode_close(MpegEncContext *s)
{
    av_free(s->mjpeg_ctx);
}

av_cold int ff_MPV_encode_end(AVCodecContext *avctx)
{
    MpegEncContext *s = avctx->priv_data;

    ff_rate_control_uninit(s);

    ff_MPV_common_end(s);
    if (s->out_format == FMT_MJPEG)
        ff_mjpeg_encode_close(s);

    av_freep(&avctx->extradata);

    return 0;
}

