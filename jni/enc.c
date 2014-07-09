/*
 * set_params.h
 *
 *  Created on: 2013/11/23
 *      Author: Owner
 */


//これで[プロジェクト]→[プロパティ]→[C/C++ 一般]→[インデクサー]
//エディターで開かれたソースおよびヘッダー・ファイルをインデックスにチェックしないとJNIEnvから関数が呼べない
//もしくはeclipseを再起動する

//#undef __cplusplus
#include <string.h>
#include <jni.h>


#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <time.h>
#include <sys/time.h>

#include "libmp3lame/lame.h"
#include "enc.h"
#include "ff_MPV_encode_init.h"


#ifndef UTIL_H_
#include "util.h"
#define UTIL_H_
#endif

#include "get_params.h"

#include "codec_base.h"


#include <android/log.h>
#define LOG_TAG "NLiveRoid"
#define LOGD(...)  __android_log_print(3, LOG_TAG, __VA_ARGS__)

#include <android/bitmap.h>

#define AV_CH_FRONT_LEFT             0x00000001
#define AV_CH_FRONT_RIGHT            0x00000002
#define AV_CH_LAYOUT_STEREO            (AV_CH_FRONT_LEFT|AV_CH_FRONT_RIGHT)


	     //メタ情報1つ分のデータ
	     char video_tag_info_data[12];
	     char audio_tag_info_data[12];
	     //タグ後の4バイト
	     char video_tag_end_data[4];
	     char audio_tag_end_data[4];
	     int audio_ts = 0;

#define ENCODE_BUFFER(func, buf_type, buf_name) do {                        \
    lame_result = func(s->gfp,                                              \
                       (const buf_type *)buf_name[0],                       \
                       (const buf_type *)buf_name[1], frame->nb_samples,    \
                       s->buffer + s->buffer_index,                         \
                       s->buffer_size - s->buffer_index);                   \
} while (0)



typedef struct AVFloatDSPContext {
    /**
     * Calculate the product of two vectors of floats and store the result in
     * a vector of floats.
     *
     * @param dst  output vector
     *             constraints: 32-byte aligned
     * @param src0 first input vector
     *             constraints: 32-byte aligned
     * @param src1 second input vector
     *             constraints: 32-byte aligned
     * @param len  number of elements in the input
     *             constraints: multiple of 16
     */
    void (*vector_fmul)(float *dst, const float *src0, const float *src1,
                        int len);

    /**
     * Multiply a vector of floats by a scalar float and add to
     * destination vector.  Source and destination vectors must
     * overlap exactly or not at all.
     *
     * @param dst result vector
     *            constraints: 32-byte aligned
     * @param src input vector
     *            constraints: 32-byte aligned
     * @param mul scalar value
     * @param len length of vector
     *            constraints: multiple of 16
     */
    void (*vector_fmac_scalar)(float *dst, const float *src, float mul,
                               int len);

    /**
     * Multiply a vector of floats by a scalar float.  Source and
     * destination vectors must overlap exactly or not at all.
     *
     * @param dst result vector
     *            constraints: 16-byte aligned
     * @param src input vector
     *            constraints: 16-byte aligned
     * @param mul scalar value
     * @param len length of vector
     *            constraints: multiple of 4
     */
    void (*vector_fmul_scalar)(float *dst, const float *src, float mul,
                               int len);

    /**
     * Multiply a vector of double by a scalar double.  Source and
     * destination vectors must overlap exactly or not at all.
     *
     * @param dst result vector
     *            constraints: 32-byte aligned
     * @param src input vector
     *            constraints: 32-byte aligned
     * @param mul scalar value
     * @param len length of vector
     *            constraints: multiple of 8
     */
    void (*vector_dmul_scalar)(double *dst, const double *src, double mul,
                               int len);

    /**
     * Overlap/add with window function.
     * Used primarily by MDCT-based audio codecs.
     * Source and destination vectors must overlap exactly or not at all.
     *
     * @param dst  result vector
     *             constraints: 16-byte aligned
     * @param src0 first source vector
     *             constraints: 16-byte aligned
     * @param src1 second source vector
     *             constraints: 16-byte aligned
     * @param win  half-window vector
     *             constraints: 16-byte aligned
     * @param len  length of vector
     *             constraints: multiple of 4
     */
    void (*vector_fmul_window)(float *dst, const float *src0,
                               const float *src1, const float *win, int len);

    /**
     * Calculate the product of two vectors of floats, add a third vector of
     * floats and store the result in a vector of floats.
     *
     * @param dst  output vector
     *             constraints: 32-byte aligned
     * @param src0 first input vector
     *             constraints: 32-byte aligned
     * @param src1 second input vector
     *             constraints: 32-byte aligned
     * @param src1 third input vector
     *             constraints: 32-byte aligned
     * @param len  number of elements in the input
     *             constraints: multiple of 16
     */
    void (*vector_fmul_add)(float *dst, const float *src0, const float *src1,
                            const float *src2, int len);

    /**
     * Calculate the product of two vectors of floats, and store the result
     * in a vector of floats. The second vector of floats is iterated over
     * in reverse order.
     *
     * @param dst  output vector
     *             constraints: 32-byte aligned
     * @param src0 first input vector
     *             constraints: 32-byte aligned
     * @param src1 second input vector
     *             constraints: 32-byte aligned
     * @param src1 third input vector
     *             constraints: 32-byte aligned
     * @param len  number of elements in the input
     *             constraints: multiple of 16
     */
    void (*vector_fmul_reverse)(float *dst, const float *src0,
                                const float *src1, int len);

    /**
     * Calculate the sum and difference of two vectors of floats.
     *
     * @param v1  first input vector, sum output, 16-byte aligned
     * @param v2  second input vector, difference output, 16-byte aligned
     * @param len length of vectors, multiple of 4
     */
    void (*butterflies_float)(float *  v1, float * v2, int len);

    /**
     * Calculate the scalar product of two vectors of floats.
     *
     * @param v1  first vector, 16-byte aligned
     * @param v2  second vector, 16-byte aligned
     * @param len length of vectors, multiple of 4
     *
     * @return sum of elementwise products
     */
    float (*scalarproduct_float)(const float *v1, const float *v2, int len);
} AVFloatDSPContext;



typedef struct MPADecodeHeader {
    int frame_size;
    int error_protection;
    int layer;
    int sample_rate;
    int sample_rate_index; /* between 0 and 8 */
    int bit_rate;
    int nb_channels;
    int mode;
    int mode_ext;
    int lsf;
} MPADecodeHeader;


typedef struct AudioFrame {
    int64_t pts;
    int duration;
} AudioFrame;

typedef struct AudioFrameQueue {
    AVCodecContext *avctx;
    int remaining_delay;
    int remaining_samples;
    AudioFrame *frames;
    unsigned frame_count;
    unsigned frame_alloc;
} AudioFrameQueue;

void ff_af_queue_init(AVCodecContext *avctx, AudioFrameQueue *afq)
{
    afq->avctx = avctx;
    afq->remaining_delay   = avctx->delay;
    afq->remaining_samples = avctx->delay;
    afq->frame_count       = 0;
}


static void vector_fmul_c(float *dst, const float *src0, const float *src1,
                          int len)
{
    int i;
    for (i = 0; i < len; i++)
        dst[i] = src0[i] * src1[i];
}

static void vector_fmac_scalar_c(float *dst, const float *src, float mul,
                                 int len)
{
    int i;
    for (i = 0; i < len; i++)
        dst[i] += src[i] * mul;
}

static void vector_fmul_scalar_c(float *dst, const float *src, float mul,
                                 int len)
{
    int i;
    for (i = 0; i < len; i++)
        dst[i] = src[i] * mul;
}

static void vector_dmul_scalar_c(double *dst, const double *src, double mul,
                                 int len)
{
    int i;
    for (i = 0; i < len; i++)
        dst[i] = src[i] * mul;
}

static void vector_fmul_window_c(float *dst, const float *src0,
                                 const float *src1, const float *win, int len)
{
    int i, j;

    dst  += len;
    win  += len;
    src0 += len;

    for (i = -len, j = len - 1; i < 0; i++, j--) {
        float s0 = src0[i];
        float s1 = src1[j];
        float wi = win[i];
        float wj = win[j];
        dst[i] = s0 * wj - s1 * wi;
        dst[j] = s0 * wi + s1 * wj;
    }
}

static void vector_fmul_add_c(float *dst, const float *src0, const float *src1,
                              const float *src2, int len){
    int i;

    for (i = 0; i < len; i++)
        dst[i] = src0[i] * src1[i] + src2[i];
}

static void vector_fmul_reverse_c(float *dst, const float *src0,
                                  const float *src1, int len)
{
    int i;

    src1 += len-1;
    for (i = 0; i < len; i++)
        dst[i] = src0[i] * src1[-i];
}

static void butterflies_float_c(float * v1, float * v2,
                                int len)
{
    int i;

    for (i = 0; i < len; i++) {
        float t = v1[i] - v2[i];
        v1[i] += v2[i];
        v2[i] = t;
    }
}

float avpriv_scalarproduct_float_c(const float *v1, const float *v2, int len)
{
    float p = 0.0;
    int i;

    for (i = 0; i < len; i++)
        p += v1[i] * v2[i];

    return p;
}



void avpriv_float_dsp_init(AVFloatDSPContext *fdsp, int bit_exact)
{
    fdsp->vector_fmul = vector_fmul_c;
    fdsp->vector_fmac_scalar = vector_fmac_scalar_c;
    fdsp->vector_fmul_scalar = vector_fmul_scalar_c;
    fdsp->vector_dmul_scalar = vector_dmul_scalar_c;
    fdsp->vector_fmul_window = vector_fmul_window_c;
    fdsp->vector_fmul_add = vector_fmul_add_c;
    fdsp->vector_fmul_reverse = vector_fmul_reverse_c;
    fdsp->butterflies_float = butterflies_float_c;
    fdsp->scalarproduct_float = avpriv_scalarproduct_float_c;

#if ARCH_ARM
    ff_float_dsp_init_arm(fdsp);
#elif ARCH_PPC
    ff_float_dsp_init_ppc(fdsp, bit_exact);
#elif ARCH_X86
    ff_float_dsp_init_x86(fdsp);
#elif ARCH_MIPS
    ff_float_dsp_init_mips(fdsp);
#endif
}




typedef struct LAMEContext {
//    AVClass *class;
    AVCodecContext *avctx;
    lame_t *gfp;
    uint8_t *buffer;
    int buffer_index;
    int buffer_size;
    int reservoir;
    float *samples_flt[2];
    AudioFrameQueue afq;
    int abr;
    AVFloatDSPContext fdsp;
} LAMEContext;

FILE* out_file;

const uint16_t avpriv_mpa_freq_tab[3] = { 44100, 48000, 32000 };

const uint16_t avpriv_mpa_bitrate_tab[2][3][15] = {
    { {0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448 },
      {0, 32, 48, 56,  64,  80,  96, 112, 128, 160, 192, 224, 256, 320, 384 },
      {0, 32, 40, 48,  56,  64,  80,  96, 112, 128, 160, 192, 224, 256, 320 } },
    { {0, 32, 48, 56,  64,  80,  96, 112, 128, 144, 160, 176, 192, 224, 256},
      {0,  8, 16, 24,  32,  40,  48,  56,  64,  80,  96, 112, 128, 144, 160},
      {0,  8, 16, 24,  32,  40,  48,  56,  64,  80,  96, 112, 128, 144, 160}
    }
};

//static int check_sample_fmt(AVCodec *codec, enum AVSampleFormat sample_fmt)
//{//p++すると何故か落ちる
//    const enum AVSampleFormat *p = codec->sample_fmts;
//    LOGD("check_sample_fmt codec->sample_fmts:%d\n",codec->sample_fmts );
//    LOGD("check_sample_fmt sample_fmts:%d\n",sample_fmt );
//    return 0;
//    while (*p != AV_SAMPLE_FMT_NONE) {
//        if (*p == sample_fmt)
//            return 1;
//        p++;
//    }
//    return 0;
//}
//
///* just pick the highest supported samplerate */
//static int select_sample_rate(AVCodec *codec)
//{
//    const int *p;
//    int best_samplerate = 0;
//    if (!codec->supported_samplerates)
//        return 44100;
//    p = codec->supported_samplerates;
//    while (*p) {
//        best_samplerate = FFMAX(*p, best_samplerate);
//        p++;
//    }
//    return best_samplerate;
//}
//

void *av_fast_realloc(void *ptr, unsigned int *size, size_t min_size)
{
    if (min_size < *size)
        return ptr;

    min_size = FFMAX(17 * min_size / 16 + 32, min_size);

    ptr = (void*)av_realloc(ptr, min_size);
    /* we could set this to the unmodified min_size but this is safer
     * if the user lost the ptr and uses NULL now
     */
    if (!ptr)
        min_size = 0;

    *size = min_size;

    return ptr;
}


int ff_af_queue_add(AudioFrameQueue *afq, const AVFrame *frame)
{
//    LOGD( "libavcodec/audio_frame_queue#ff_af_queue_add Called\n");
    AudioFrame *new = av_fast_realloc(afq->frames, &afq->frame_alloc, sizeof(*afq->frames)*(afq->frame_count+1));
    if(!new)
        return -1;
    afq->frames = new;
    new += afq->frame_count;

    /* get frame parameters */
    new->duration = frame->nb_samples;
    new->duration += afq->remaining_delay;
    if (frame->pts != AV_NOPTS_VALUE) {
        new->pts = av_rescale_q_rnd(frame->pts,
                                      afq->avctx->time_base,
                                      (AVRational){ 1, afq->avctx->sample_rate },AV_ROUND_NEAR_INF);
        new->pts -= afq->remaining_delay;
        if(afq->frame_count && new[-1].pts >= new->pts)
            LOGD( "Queue input is backward in time\n");
    } else {
        new->pts = AV_NOPTS_VALUE;
    }
    afq->remaining_delay = 0;

    /* add frame sample count */
    afq->remaining_samples += frame->nb_samples;

    afq->frame_count++;
    return 0;
}

#define LAME_BUFFER_SIZE (7200 + 2 * 1152 + 1152 / 4+1000) // FIXME: Buffer size to small? Adding 1000 to make up for it.

static int realloc_buffer(LAMEContext *s)
{
//    LOGD( "lame realloc_buffer Called\n");
    if (!s->buffer || s->buffer_size - s->buffer_index < LAME_BUFFER_SIZE) {
        uint8_t *tmp;
        int new_size = s->buffer_index + 2 * LAME_BUFFER_SIZE;

        tmp = av_realloc(s->buffer, new_size);
        if (!tmp) {
            av_freep(&s->buffer);
            s->buffer_size = s->buffer_index = 0;
            return -1;
        }
        s->buffer      = tmp;
        s->buffer_size = new_size;
    }
    return 0;
}

void ff_af_queue_close(AudioFrameQueue *afq)
{
    if(afq->frame_count)
        LOGD( "%d frames left in the queue on closing\n", afq->frame_count);
    av_freep(&afq->frames);
    memset(afq, 0, sizeof(*afq));
}


AVCodecContext *avcodec_alloc_context3(const AVCodec *codec)
{
    AVCodecContext *avctx= av_malloc(sizeof(AVCodecContext));

    if(avctx==NULL) return NULL;

    if(avcodec_set_context_defaults3(avctx, codec) < 0){
        av_free(avctx);
        return NULL;
    }

    return avctx;
}

int codec_open_test(AVCodecContext *avctx,AVCodec* codec){
    avctx->internal = av_mallocz(sizeof(AVCodecInternal));
    if (!avctx->internal) {
    	LOGD("Failed alloc internal\n");
            return -1;
        }
    avctx->internal->pool = av_mallocz(sizeof(*avctx->internal->pool));
    if (!avctx->internal->pool) {
    	LOGD("Failed alloc pool\n");
            return -1;
    }
//    avctx->priv_data = av_mallocz(codec->priv_data_size);
    avctx->codec_type = codec->type;
    avctx->codec_id   = codec->id;
    avctx->codec = codec;
    //Descriptorはそもそもやってある
    ff_frame_thread_encoder_init(avctx, NULL);
    codec->init(avctx);
    return 0;
}

static uint8_t linear_to_alaw[16384];
static uint8_t linear_to_ulaw[16384];

static av_cold void build_xlaw_table(uint8_t *linear_to_xlaw,
                             int (*xlaw2linear)(unsigned char),
                             int mask)
{
    int i, j, v, v1, v2;

    j = 0;
    for(i=0;i<128;i++) {
        if (i != 127) {
            v1 = xlaw2linear(i ^ mask);
            v2 = xlaw2linear((i + 1) ^ mask);
            v = (v1 + v2 + 4) >> 3;
        } else {
            v = 8192;
        }
        for(;j<v;j++) {
            linear_to_xlaw[8192 + j] = (i ^ mask);
            if (j > 0)
                linear_to_xlaw[8192 - j] = (i ^ (mask ^ 0x80));
        }
    }
    linear_to_xlaw[0] = linear_to_xlaw[1];
}


#define         SIGN_BIT        (0x80)      /* Sign bit for a A-law byte. */
#define         QUANT_MASK      (0xf)       /* Quantization field mask. */
#define         SEG_SHIFT       (4)         /* Left shift for segment number. */
#define         SEG_MASK        (0x70)      /* Segment field mask. */
/*
 * alaw2linear() - Convert an A-law value to 16-bit linear PCM
 *
 */
static av_cold int alaw2linear(unsigned char a_val)
{
        int t;
        int seg;

        a_val ^= 0x55;

        t = a_val & QUANT_MASK;
        seg = ((unsigned)a_val & SEG_MASK) >> SEG_SHIFT;
        if(seg) t= (t + t + 1 + 32) << (seg + 2);
        else    t= (t + t + 1     ) << 3;

        return (a_val & SIGN_BIT) ? t : -t;
}


int av_get_exact_bits_per_sample(enum AVCodecID codec_id)
{
    switch (codec_id) {
    case AV_CODEC_ID_8SVX_EXP:
    case AV_CODEC_ID_8SVX_FIB:
    case AV_CODEC_ID_ADPCM_CT:
    case AV_CODEC_ID_ADPCM_IMA_APC:
    case AV_CODEC_ID_ADPCM_IMA_EA_SEAD:
    case AV_CODEC_ID_ADPCM_IMA_OKI:
    case AV_CODEC_ID_ADPCM_IMA_WS:
    case AV_CODEC_ID_ADPCM_G722:
    case AV_CODEC_ID_ADPCM_YAMAHA:
        return 4;
    case AV_CODEC_ID_PCM_ALAW:
    case AV_CODEC_ID_PCM_MULAW:
    case AV_CODEC_ID_PCM_S8:
    case AV_CODEC_ID_PCM_S8_PLANAR:
    case AV_CODEC_ID_PCM_U8:
    case AV_CODEC_ID_PCM_ZORK:
        return 8;
    case AV_CODEC_ID_PCM_S16BE:
    case AV_CODEC_ID_PCM_S16BE_PLANAR:
    case AV_CODEC_ID_PCM_S16LE:
    case AV_CODEC_ID_PCM_S16LE_PLANAR:
    case AV_CODEC_ID_PCM_U16BE:
    case AV_CODEC_ID_PCM_U16LE:
        return 16;
    case AV_CODEC_ID_PCM_S24DAUD:
    case AV_CODEC_ID_PCM_S24BE:
    case AV_CODEC_ID_PCM_S24LE:
    case AV_CODEC_ID_PCM_S24LE_PLANAR:
    case AV_CODEC_ID_PCM_U24BE:
    case AV_CODEC_ID_PCM_U24LE:
        return 24;
    case AV_CODEC_ID_PCM_S32BE:
    case AV_CODEC_ID_PCM_S32LE:
    case AV_CODEC_ID_PCM_S32LE_PLANAR:
    case AV_CODEC_ID_PCM_U32BE:
    case AV_CODEC_ID_PCM_U32LE:
    case AV_CODEC_ID_PCM_F32BE:
    case AV_CODEC_ID_PCM_F32LE:
        return 32;
    case AV_CODEC_ID_PCM_F64BE:
    case AV_CODEC_ID_PCM_F64LE:
        return 64;
    default:
        return 0;
    }
}

int av_get_bits_per_sample(enum AVCodecID codec_id)
{
    switch (codec_id) {
    case AV_CODEC_ID_ADPCM_SBPRO_2:
        return 2;
    case AV_CODEC_ID_ADPCM_SBPRO_3:
        return 3;
    case AV_CODEC_ID_ADPCM_SBPRO_4:
    case AV_CODEC_ID_ADPCM_IMA_WAV:
    case AV_CODEC_ID_ADPCM_IMA_QT:
    case AV_CODEC_ID_ADPCM_SWF:
    case AV_CODEC_ID_ADPCM_MS:
        return 4;
    default:
        return av_get_exact_bits_per_sample(codec_id);
    }
}

static av_cold int pcm_encode_init(AVCodecContext *avctx)
{
	LOGD("pcm_encode_init Called\n");
    avctx->frame_size = 0;
    switch (avctx->codec->id) {
    case AV_CODEC_ID_PCM_ALAW:
    	build_xlaw_table(linear_to_alaw, alaw2linear, 0xd5);
        break;
    case AV_CODEC_ID_PCM_MULAW:
    	build_xlaw_table(linear_to_alaw, alaw2linear, 0xd5);
        break;
    default:
        break;
    }

    avctx->bits_per_coded_sample = av_get_bits_per_sample(avctx->codec->id);
    avctx->block_align           = avctx->channels * avctx->bits_per_coded_sample / 8;
    avctx->bit_rate              = avctx->block_align * avctx->sample_rate * 8;

    return 0;
}




/**
 * Write PCM samples macro
 * @param type   Datatype of native machine format
 * @param endian bytestream_put_xxx() suffix
 * @param src    Source pointer (variable name)
 * @param dst    Destination pointer (variable name)
 * @param n      Total number of samples (variable name)
 * @param shift  Bitshift (bits)
 * @param offset Sample value offset
 */
#define ENCODE(type, endian, src, dst, n, shift, offset)                \
    samples_ ## type = (const type *) src;                              \
    for (; n > 0; n--) {                                                \
        register type v = (*samples_ ## type++ >> shift) + offset;      \
        bytestream_put_ ## endian(&dst, v);                             \
    }

#define ENCODE_PLANAR(type, endian, dst, n, shift, offset)              \
    n /= avctx->channels;                                               \
    for (c = 0; c < avctx->channels; c++) {                             \
        int i;                                                          \
        samples_ ## type = (const type *) frame->extended_data[c];      \
        for (i = n; i > 0; i--) {                                       \
            register type v = (*samples_ ## type++ >> shift) + offset;  \
            bytestream_put_ ## endian(&dst, v);                         \
        }                                                               \
    }


static inline void bytestream_put_buffer(uint8_t **b,const uint8_t *src,unsigned int size)
{
    memcpy(*b, src, size);
    (*b) += size;
}

static int pcm_encode_frame(AVCodecContext *avctx, AVPacket *avpkt,
                            const AVFrame *frame, int *got_packet_ptr)
{
    int n, c, sample_size, v, ret;
    const short *samples;
    unsigned char *dst;
    const uint8_t *samples_uint8_t;
    const int16_t *samples_int16_t;
    const int32_t *samples_int32_t;
    const int64_t *samples_int64_t;
    const uint16_t *samples_uint16_t;
    const uint32_t *samples_uint32_t;

//	LOGD("pcm_encode_frame Called\n");
    sample_size = av_get_bits_per_sample(avctx->codec->id) / 8;
    n           = frame->nb_samples * avctx->channels;
    samples     = (const short *)frame->data[0];

    if ((ret = ff_alloc_packet2(avctx, avpkt, n * sample_size)) < 0)
        return ret;
    dst = avpkt->data;

//	LOGD("pcm_encode_frame frame->nb_samples:%d \n",frame->nb_samples,avpkt->size,avpkt->data);
    switch (avctx->codec->id) {
#if HAVE_BIGENDIAN
    case AV_CODEC_ID_PCM_F64LE:
        ENCODE(int64_t, le64, samples, dst, n, 0, 0)
        break;
    case AV_CODEC_ID_PCM_S32LE:
    case AV_CODEC_ID_PCM_F32LE:
        ENCODE(int32_t, le32, samples, dst, n, 0, 0)
        break;
    case AV_CODEC_ID_PCM_S32LE_PLANAR:
        ENCODE_PLANAR(int32_t, le32, dst, n, 0, 0)
        break;
    case AV_CODEC_ID_PCM_S16LE:
        ENCODE(int16_t, le16, samples, dst, n, 0, 0)
        break;
    case AV_CODEC_ID_PCM_S16LE_PLANAR:
        ENCODE_PLANAR(int16_t, le16, dst, n, 0, 0)
        break;
    case AV_CODEC_ID_PCM_F64BE:
    case AV_CODEC_ID_PCM_F32BE:
    case AV_CODEC_ID_PCM_S32BE:
    case AV_CODEC_ID_PCM_S16BE:
#else
    case AV_CODEC_ID_PCM_F64LE:
    case AV_CODEC_ID_PCM_F32LE:
    case AV_CODEC_ID_PCM_S32LE:
    case AV_CODEC_ID_PCM_S16LE:
#endif /* HAVE_BIGENDIAN */
    case AV_CODEC_ID_PCM_U8:{
        memcpy(dst, samples, n * sample_size);
//        LOGD("pcm_encode_frame Switch samples:%d channels:%d sample_size:%d n:%d\n",samples,avctx->channels,sample_size,n);
    }
        break;
#if HAVE_BIGENDIAN
    case AV_CODEC_ID_PCM_S16BE_PLANAR:
#else
#endif /* HAVE_BIGENDIAN */
        n /= avctx->channels;
        for (c = 0; c < avctx->channels; c++) {
            const uint8_t *src = frame->extended_data[c];
            bytestream_put_buffer(&dst, src, n * sample_size);
        }
        break;
    default:
        return -1;
    }

    *got_packet_ptr = 1;


    return 0;
}

static const AVCodecDefault libmp3lame_defaults[] = {
    { "b",          "0" },
    { NULL },
};



static const int libmp3lame_sample_rates[] = {
    44100, 48000,  32000, 22050, 24000, 16000, 11025, 12000, 8000, 0
};


av_cold int mp3lame_encode_init(AVCodecContext *avctx)
{
    LOGD( "mp3lame_encode_init Called\n");
    LAMEContext *s = avctx->priv_data;
    int ret;

    s->avctx = avctx;
    s->reservoir = 1;
    /* initialize LAME and get defaults */
    if ((s->gfp = lame_init()) == NULL)
        return -1;


    LOGD("Lame Params channels:%d sample_rate:%d compression_level:%d\n"
    		,avctx->channels,avctx->sample_rate,avctx->compression_level);
    lame_set_num_channels(s->gfp, avctx->channels);
    lame_set_mode(s->gfp,MONO);
    /* sample rate */
    lame_set_in_samplerate (s->gfp, avctx->sample_rate);
    lame_set_out_samplerate(s->gfp, avctx->sample_rate);

    /* algorithmic quality */
    if (avctx->compression_level == FF_COMPRESSION_DEFAULT)
        lame_set_quality(s->gfp, 7);
    else
        lame_set_quality(s->gfp, avctx->compression_level);

    /* rate control */
    LOGD("avctx->bit_rate :%d\n",avctx->bit_rate);
    if (avctx->flags & CODEC_FLAG_QSCALE) {
        lame_set_VBR(s->gfp, vbr_default);
        lame_set_VBR_quality(s->gfp, avctx->global_quality / (float)FF_QP2LAMBDA);
    } else {
    	if (s->abr) {                   // ABR
				LOGD("mp3lame_encode_init s->abr:%d\n",s->abr);
				lame_set_VBR(s->gfp, vbr_abr);
				lame_set_VBR_mean_bitrate_kbps(s->gfp, avctx->bit_rate / 1000);
		} else  {                        // CBR
				LOGD("mp3lame_encode_init avctx->bit_rate:%d\n",avctx->bit_rate);
		}
				lame_set_brate(s->gfp, avctx->bit_rate / 1000);
    }

    /* do not get a Xing VBR header frame from LAME */
    lame_set_bWriteVbrTag(s->gfp,0);//単にffmpegがこうやってた

    /* bit reservoir usage */
    lame_set_disable_reservoir(s->gfp, !s->reservoir);

    /* set specified parameters */
    if (lame_init_params(s->gfp) < 0) {
        ret = -1;
        goto error;
    }

    /* get encoder delay */
    avctx->delay = lame_get_encoder_delay(s->gfp) + 528 + 1;
    ff_af_queue_init(avctx, &s->afq);

    avctx->frame_size  = lame_get_framesize(s->gfp);

#if FF_API_OLD_ENCODE_AUDIO
    avctx->coded_frame = avcodec_alloc_frame();
    if (!avctx->coded_frame) {
        ret = -1;
        goto error;
    }
#endif

    /* allocate float sample buffers */
    if (avctx->sample_fmt == AV_SAMPLE_FMT_FLTP) {
        int ch;
        for (ch = 0; ch < avctx->channels; ch++) {
            s->samples_flt[ch] = av_malloc(avctx->frame_size *
                                           sizeof(*s->samples_flt[ch]));
            if (!s->samples_flt[ch]) {
                ret = -1;
                goto error;
            }
        }
    }

    ret = realloc_buffer(s);
    if (ret < 0)
        goto error;

    avpriv_float_dsp_init(&s->fdsp, avctx->flags & CODEC_FLAG_BITEXACT);

    return 0;
error:
    mp3lame_encode_close(avctx);
    return ret;
}

/**
 * Rescale from sample rate to AVCodecContext.time_base.
 */
static inline int64_t ff_samples_to_time_base(AVCodecContext *avctx,
                                                        int64_t samples)
{
    if(samples == AV_NOPTS_VALUE)
        return AV_NOPTS_VALUE;
    return av_rescale_q_rnd(samples, (AVRational){ 1, avctx->sample_rate },
                        avctx->time_base,AV_ROUND_NEAR_INF);
}

void ff_af_queue_remove(AudioFrameQueue *afq, int nb_samples, int64_t *pts,
                        int *duration)
{
//    LOGD( "ff_af_queue_remove Called\n");
    int64_t out_pts = AV_NOPTS_VALUE;
    int removed_samples = 0;
    int i;

    if (afq->frame_count || afq->frame_alloc) {
        if (afq->frames->pts != AV_NOPTS_VALUE)
            out_pts = afq->frames->pts;
    }
    if(!afq->frame_count)
    	LOGD( "Trying to remove %d samples, but the queue is empty\n", nb_samples);
    if (pts)
        *pts = ff_samples_to_time_base(afq->avctx, out_pts);

    for(i=0; nb_samples && i<afq->frame_count; i++){
        int n= FFMIN(afq->frames[i].duration, nb_samples);
        afq->frames[i].duration -= n;
        nb_samples              -= n;
        removed_samples         += n;
        if(afq->frames[i].pts != AV_NOPTS_VALUE)
            afq->frames[i].pts      += n;
    }
    afq->remaining_samples -= removed_samples;
    i -= i && afq->frames[i-1].duration;
    memmove(afq->frames, afq->frames + i, sizeof(*afq->frames) * (afq->frame_count - i));
    afq->frame_count -= i;

    if(nb_samples){
//        av_assert0(!afq->frame_count);
//        av_assert0(afq->remaining_samples == afq->remaining_delay);
        if(afq->frames && afq->frames[0].pts != AV_NOPTS_VALUE)
            afq->frames[0].pts += nb_samples;
        LOGD( "Trying to remove %d more samples than there are in the queue\n", nb_samples);
    }
    if (duration)
        *duration = ff_samples_to_time_base(afq->avctx, removed_samples);
}


int avpriv_mpegaudio_decode_header(MPADecodeHeader *s, uint32_t header)
{
//    LOGD("avpriv_mpegaudio_decode_header Called\n");
    int sample_rate, frame_size, mpeg25, padding;
    int sample_rate_index, bitrate_index;
    if (header & (1<<20)) {
        s->lsf = (header & (1<<19)) ? 0 : 1;
        mpeg25 = 0;
    } else {
        s->lsf = 1;
        mpeg25 = 1;
    }

    s->layer = 4 - ((header >> 17) & 3);
    /* extract frequency */
    sample_rate_index = (header >> 10) & 3;
    sample_rate = avpriv_mpa_freq_tab[sample_rate_index] >> (s->lsf + mpeg25);
    sample_rate_index += 3 * (s->lsf + mpeg25);
    s->sample_rate_index = sample_rate_index;
    s->error_protection = ((header >> 16) & 1) ^ 1;
    s->sample_rate = sample_rate;

    bitrate_index = (header >> 12) & 0xf;
    padding = (header >> 9) & 1;
    //extension = (header >> 8) & 1;
    s->mode = (header >> 6) & 3;
    s->mode_ext = (header >> 4) & 3;
    //copyright = (header >> 3) & 1;
    //original = (header >> 2) & 1;
    //emphasis = header & 3;

    if (s->mode == MPA_MONO)
        s->nb_channels = 1;
    else
        s->nb_channels = 2;

    if (bitrate_index != 0) {
        frame_size = avpriv_mpa_bitrate_tab[s->lsf][s->layer - 1][bitrate_index];
        s->bit_rate = frame_size * 1000;
        switch(s->layer) {
        case 1:
            frame_size = (frame_size * 12000) / sample_rate;
            frame_size = (frame_size + padding) * 4;
            break;
        case 2:
            frame_size = (frame_size * 144000) / sample_rate;
            frame_size += padding;
            break;
        default:
        case 3:
            frame_size = (frame_size * 144000) / (sample_rate << s->lsf);
            frame_size += padding;
            break;
        }
        s->frame_size = frame_size;
    } else {
        /* if no frame size computed, signal it */
        LOGD("avpriv_mpegaudio_decode_header if no frame size computed, signal it\n");
        return 1;
    }

    return 0;
}


av_cold int mp3lame_encode_close(AVCodecContext *avctx)
{
    LOGD( "mp3lame_encode_close Called\n");
    LAMEContext *s = avctx->priv_data;

#if FF_API_OLD_ENCODE_AUDIO
    av_freep(&avctx->coded_frame);
#endif
    av_freep(&s->samples_flt[0]);
    av_freep(&s->samples_flt[1]);
    av_freep(&s->buffer);

    ff_af_queue_close(&s->afq);

    lame_close(s->gfp);
    return 0;
}

//encode2
int mp3lame_encode_frame(AVCodecContext *avctx, AVPacket *avpkt,
                                const AVFrame *frame, int *got_packet_ptr)
{
//    LOGD( "mp3lame_encode_frame Called\n");
    LAMEContext *s = avctx->priv_data;
    MPADecodeHeader hdr;
    memset(&hdr,0,sizeof(MPADecodeHeader));
    int len, ret;
    int lame_result;

    if (frame) {//通ってる
//        LOGD( "mp3lame_encode_frame frame->pkt_pts :%d\n",frame->pkt_pts);
//        LOGD( "mp3lame_encode_frame frame->pts :%d\n",frame->pts);
//        LOGD( "mp3lame_encode_frame s->buffer_size :%d\n",s->buffer_size);
//        LOGD( "mp3lame_encode_frame s->buffer_index :%d\n",s->buffer_index);
//        LOGD( "mp3lame_encode_frame s->buffer + s->buffer_index :%d\n",s->buffer_size + s->buffer_index);
//        LOGD( "mp3lame_encode_frame s->buffer_size - s->buffer_index :%d\n",s->buffer_size - s->buffer_index);
//        switch (avctx->sample_fmt) {
//        case AV_SAMPLE_FMT_S16P:
//    	lame_result = lame_encode_buffer_template(s->gfp, (const uint8_t *)frame->data[0], (const uint8_t *)frame->data[1], frame->nb_samples, (s->buffer + s->buffer_index), (s->buffer_size - s->buffer_index), 0, 1, 1.0);
//    lame_result = lame_encode_buffer_int(s->gfp,
//                       (const uint8_t *)frame->data[0],
//                       (const uint8_t *)frame->data[1], frame->nb_samples,
//                       (s->buffer + s->buffer_index),
//                       (s->buffer_size - s->buffer_index));
    	lame_result = lame_encode_buffer(s->gfp,
    	                       (const int *)frame->data[0],
    	                       (const int *)frame->data[1], frame->nb_samples,
    	                       (s->buffer + s->buffer_index),
    	                       (s->buffer_size - s->buffer_index));
//            break;
//        case AV_SAMPLE_FMT_S32P:
//            ENCODE_BUFFER(lame_encode_buffer_int, int32_t, frame->data);
//            break;
//        case AV_SAMPLE_FMT_FLTP:
//            if (frame->linesize[0] < 4 * FFALIGN(frame->nb_samples, 8)) {
//                LOGD( "inadequate AVFrame plane padding\n");
//                return -1;
//            }
//            for (ch = 0; ch < avctx->channels; ch++) {
//                s->fdsp.vector_fmul_scalar(s->samples_flt[ch],
//                                           (const float *)frame->data[ch],
//                                           32768.0f,
//                                           FFALIGN(frame->nb_samples, 8));
//            }
//            ENCODE_BUFFER(lame_encode_buffer_float, float, s->samples_flt);
//            break;
//        default:
//            return -1;
//        }
    } else {
        lame_result = lame_encode_flush(s->gfp, s->buffer + s->buffer_index,
                                        (BUFFER_SIZE - s->buffer_index));
    }

    if (lame_result < 0) {
        if (lame_result == -1) {
            LOGD(
                   "lame: output buffer too small (buffer index: %d, free bytes: %d)\n",
                   s->buffer_index, s->buffer_size - s->buffer_index);
        }
        return -1;
    }
    s->buffer_index += lame_result;
    ret = realloc_buffer(s);
    if (ret < 0) {
        LOGD( "error floatlocating output buffer\n");
        return ret;
    }

    /* add current frame to the queue */
    if (frame) {//通ってる
        if ((ret = ff_af_queue_add(&s->afq, frame)) < 0)
            return ret;
    }

    /* Move 1 frame from the LAME buffer to the output packet, if available.
       We have to parse the first frame header in the output buffer to
       determine the frame size. */
    //動画化の為のサイズ情報の入ったヘッダの付加くさい
    if (s->buffer_index < 4)
        return 0;

    ret = avpriv_mpegaudio_decode_header(&hdr, AV_RB32(s->buffer));
	if (ret) {
			LOGD( "free format output not supported\n");
			return -1;
	}
    len = hdr.frame_size;

    if (len <= s->buffer_index) {
        if ((ret = ff_alloc_packet2(avctx, avpkt, len)) < 0)
            return ret;
        memcpy(avpkt->data, s->buffer, len);
        s->buffer_index -= len;
        memmove(s->buffer, s->buffer + len, s->buffer_index);

        /* Get the next frame pts/duration */
        ff_af_queue_remove(&s->afq, avctx->frame_size, &avpkt->pts,
                           &avpkt->duration);

        avpkt->size = len;
        *got_packet_ptr = 1;
    }else{
    	LOGD("Failed mp3lame_encode_frame\n");//結構通る→どういう失敗かよくわからん
    }
    return 0;
}



AVCodec ff_libmp3lame_encoder = {
    .name                  = "libmp3lame",
    .type                  = AVMEDIA_TYPE_AUDIO,
    .id                    = AV_CODEC_ID_MP3,
    .priv_data_size        = sizeof(LAMEContext),
    .init                  = mp3lame_encode_init,
    .encode2               = mp3lame_encode_frame,
    .close                 = mp3lame_encode_close,
    .capabilities          = CODEC_CAP_DELAY | CODEC_CAP_SMALL_LAST_FRAME,
    .sample_fmts           = (const enum AVSampleFormat[]) { AV_SAMPLE_FMT_S32P,
                                                             AV_SAMPLE_FMT_FLTP,
                                                             AV_SAMPLE_FMT_S16P,
                                                             AV_SAMPLE_FMT_NONE },
    .supported_samplerates = libmp3lame_sample_rates,
    .channel_layouts       = (const uint64_t[]) { AV_CH_LAYOUT_MONO,
                                                  AV_CH_LAYOUT_STEREO,
                                                  0 },
    .long_name             = NULL_IF_CONFIG_SMALL("libmp3lame MP3 (MPEG audio layer 3)"),
//    .priv_class            = &libmp3lame_class,
    .defaults              = libmp3lame_defaults,
};









/**
 * AVCodec SIZE 92
LAMEContext SIZE 92
MpegEncContext SIZE 9944
AVCodecContext SIZE 960
AVFormatContext SIZE 92
 */
int test(){
    LOGD("AVCodec SIZE %d\n",sizeof(AVCodec));
    LOGD("LAMEContext SIZE %d\n",sizeof(LAMEContext));
    LOGD("MpegEncContext SIZE %d\n",sizeof(MpegEncContext));
    LOGD("AVCodecContext SIZE %d\n",sizeof(AVCodecContext));
    LOGD("AVFormatContext SIZE %d\n",sizeof(AVCodec));
    return 0;
}

/**
 *
AVCodec ff_flv_encoder = {
    .name           = "flv",
    .type           = AVMEDIA_TYPE_VIDEO,
    .id             = AV_CODEC_ID_FLV1,
    .priv_data_size = sizeof(MpegEncContext),

    .init           = ff_MPV_encode_init,
    .encode2        = ff_MPV_encode_picture,
    .close          = ff_MPV_encode_end,
    .pix_fmts       = (const enum AVPixelFormat[]){ AV_PIX_FMT_YUV420P, AV_PIX_FMT_NONE },
    .long_name      = NULL_IF_CONFIG_SMALL("FLV / Sorenson Spark / Sorenson H.263 (Flash Video)"),
    .priv_class     = &flv_class,
};
 */
AVCodec* alloc_flv_codec(){
	AVCodec *codec = av_malloc(sizeof(AVCodec));
		if(!codec){
				LOGD("Failed alloc codec");
				return NULL;
			}
		memset(codec,0,sizeof(AVCodec));
		codec->id = AV_CODEC_ID_FLV1;
		codec->type = AVMEDIA_TYPE_VIDEO;
		codec->long_name = "FLV / Sorenson Spark / Sorenson H.263 (Flash Video)";
		codec->name = "flv";
		codec->init = ff_MPV_encode_init;
		codec->encode2 = ff_MPV_encode_picture;
		codec->close = ff_MPV_encode_end;
		codec->pix_fmts = (const enum AVPixelFormat[]){ AV_PIX_FMT_YUV420P, AV_PIX_FMT_NONE };
		//AVCodecContextのpriv_dataにはMpegEncContextが入る
		codec->priv_data_size = sizeof(MpegEncContext);

		AVCodec* next = av_malloc(sizeof(AVCodec));
		if(next < 0){
		    	LOGD("Failed next alloc");
		    	return NULL;
		}
		memset(next, 0, sizeof(AVCodec));
	    next->capabilities  = 3;
	    next->id  = AV_CODEC_ID_FLV1;
	    next->long_name = "FLV / Sorenson Spark / Sorenson H.263 (Flash Video)";
	    next->name = "flv";
	    next->pix_fmts  = 13407176;
	    next->max_lowres = 3;
		codec->next = next;
		return codec;
}

AVCodec* alloc_pcm_codec(){
	AVCodec *codec = av_malloc(sizeof(AVCodec));
				if(!codec){
						LOGD("Failed alloc codec");
						return NULL;
					}
				memset(codec,0,sizeof(AVCodec));
				codec->capabilities = 11786689;
				codec->id = AV_CODEC_ID_PCM_S16LE;
				codec->channel_layouts = AV_CH_LAYOUT_MONO;
				codec->long_name = "pcm_s16le long_name";
				codec->name = "pcm_s16le";
				codec->sample_fmts = AV_SAMPLE_FMT_S16P;
				codec->type = AVMEDIA_TYPE_AUDIO;
//				codec->defaults = ;
				AVCodec* next = av_malloc(sizeof(AVCodec));//適当で大丈夫っぽい
				if(next < 0){
				    	LOGD("Failed next alloc");
				    	return -1;
				}
				memset(next, 0, sizeof(AVCodec));
			    next->capabilities  = 2;
			    next->channel_layouts  = AV_CH_LAYOUT_MONO;
			    next->id  = 1112823892;
			    next->long_name  = "Binary text";
			    next->name  = "bintext";
			    next->pix_fmts  = 13407176;
				codec->next = next;

				codec->init = pcm_encode_init;
				codec->encode2 = pcm_encode_frame;
//			    codec->close = ;
//			    codec->priv_data_size =;
			    return codec;
}



AVCodec* alloc_mp3_codec(){
	AVCodec *codec = av_malloc(sizeof(AVCodec));
				if(!codec){
						LOGD("Failed alloc codec");
						return NULL;
					}
				memset(codec,0,sizeof(AVCodec));
				codec->capabilities = 96;
				codec->id = AV_CODEC_ID_MP3;
				codec->channel_layouts = AV_CH_LAYOUT_MONO;
				codec->long_name = "libmp3lame MP3 (MPEG audio layer 3)";
				codec->name = "libmp3lame";
				codec->sample_fmts = AV_SAMPLE_FMT_S16P;
				codec->type = AVMEDIA_TYPE_AUDIO;
				codec->defaults = libmp3lame_defaults;
				AVCodec* next = av_malloc(sizeof(AVCodec));
				if(next < 0){
				    	LOGD("Failed next alloc");
				    	return -1;
				}
				memset(next, 0, sizeof(AVCodec));
			    next->capabilities  = 2;
			    next->channel_layouts  = AV_CH_LAYOUT_MONO;
			    next->id  = 1112823892;
			    next->long_name  = "Binary text";
			    next->name  = "bintext";
			    next->pix_fmts  = 13407176;
				codec->next = next;
				codec->init = mp3lame_encode_init;
				codec->encode2 = mp3lame_encode_frame;
			    codec->close = mp3lame_encode_close;
			    codec->priv_data_size = sizeof(LAMEContext);
			    return codec;
}

AVCodecContext* video_summarize_init(AVCodec *codec)
{
    AVCodecContext* avctx= av_malloc(sizeof(AVCodecContext));
    if(!avctx){
	    	LOGD("Failed set_context_default video\n");
	        av_free(avctx);
	    	return NULL;
    }
    memset(avctx, 0, sizeof(AVCodecContext));


    avctx->codec_type =  AVMEDIA_TYPE_VIDEO;
    avctx->get_buffer          = video_get_buffer;
    avctx->release_buffer      = avcodec_default_release_buffer;
    avctx->get_format          = avcodec_default_get_format;
    avctx->execute             = avcodec_default_execute;
    avctx->execute2            = avcodec_default_execute2;
    avctx->sample_aspect_ratio = (AVRational){0,1};
    avctx->pix_fmt             = AV_PIX_FMT_NONE;
    avctx->sample_fmt          = AV_SAMPLE_FMT_NONE;
    avctx->timecode_frame_start = -1;

    avctx->reget_buffer        = avcodec_default_reget_buffer;
    avctx->reordered_opaque    = AV_NOPTS_VALUE;
	avctx->b_sensitivity = 40;
	avctx->bidir_refine = 1;
//	avctx->bit_rate = 200000;
	avctx->bit_rate = 800000;
	avctx->codec_id = AV_CODEC_ID_FLV1;
	avctx->bit_rate_tolerance = 4000000;
	//定数臭いがなぜこうなるのかわからん
	avctx->i_quant_factor = -0.8;
	avctx->i_quant_offset = 0.0;
	avctx->b_quant_factor = 1.25;
	avctx->b_quant_offset = 1.25;

	avctx->codec = codec;


	AVCodecDescriptor* cdescriptor = av_malloc(sizeof(AVCodecDescriptor));
	if(cdescriptor < 0){
	    	LOGD("Failed cdescriptor alloc");
	    	return NULL;
	}
	memset(cdescriptor, 0, sizeof(AVCodecDescriptor));
	avctx->codec_descriptor = cdescriptor;
	strcpy(avctx->codec_name,"raw");
//	avctx->coded_height = 320;
//	avctx->coded_width = 480;
	avctx->color_primaries = 2;
	avctx->color_trc = 2;
	avctx->colorspace = 2;
	avctx->compression_level = -1;
	avctx->err_recognition = 1;
	avctx->error_concealment = 3;
	avctx->flags = 4194304;
	avctx->frame_skip_cmp = 13;
	avctx->gop_size = 100;//試してみる
	avctx->height = 320;
	avctx->ildct_cmp = 8;
	avctx->inter_quant_bias = 999999;
	avctx->intra_quant_bias = 0;
	avctx->keyint_min = 25;
	avctx->level = -99;
	avctx->lmax = 3658;
	avctx->lmin = 236;
	avctx->max_qdiff = 3;
	avctx->mb_lmax = 3658;
	avctx->mb_lmin = 236;
	avctx->me_method = 5;
	avctx->me_penalty_compensation = 256;
	avctx->me_subpel_quality = 8;
	avctx->mv0_threshold = 256;
	avctx->profile = -99;
	avctx->qblur = 0.500000;
	avctx->qcompress = 0.500000;
	avctx->qmax = 31;
	avctx->qmin = 2;
	avctx->rc_buffer_aggressivity = 1.000000;
	avctx->rc_min_vbv_overflow_use = 3.000000;
	avctx->refs = 1;
	avctx->sample_fmt = -1;
	avctx->scenechange_factor = 6;
	avctx->thread_count = 1;
	avctx->thread_type = 3;
	avctx->ticks_per_frame = 1;
    /* frames per second */
	AVRational* time_base = av_malloc(sizeof(AVRational));
	if(!time_base){
		LOGD("Failed alloc time_base");
		return NULL;
	}
    time_base->den = 2;
    time_base->num = 1;
	avctx->time_base = *time_base;
	avctx->timecode_frame_start = -1;
//	avctx->width = 480;
	avctx->workaround_bugs = 1;

	MpegEncContext* mpegenc = av_malloc(sizeof(MpegEncContext));
	if(!mpegenc){
		LOGD("Failed alloc priv_data");
		return NULL;
	}
	memset(mpegenc, 0, sizeof(MpegEncContext));
	mpegenc->avctx = avctx;
	mpegenc->total_bits = 0;
//	mpegenc->b_back_mv_table[0] = (int16_t)0x00;
//	mpegenc->b_back_mv_table[1] = 0x04;
//	mpegenc->b_back_mv_table_base[0] = 0x00;
//	mpegenc->b_back_mv_table_base[1] = 0x04;
//	mpegenc->b_bidir_back_mv_table[0] = 0x00;
//	mpegenc->b_bidir_back_mv_table[1] = 0x04;
//	mpegenc->b_bidir_back_mv_table_base[0] = 0x00;
//	mpegenc->b_bidir_back_mv_table_base[1] = 0x04;
//	mpegenc->b_bidir_forw_mv_table[0] = 0x00;
//	mpegenc->b_bidir_forw_mv_table[1] = 0x04;
//	mpegenc->b_bidir_forw_mv_table_base[0] = 0x00;
//	mpegenc->b_bidir_forw_mv_table_base[1] = 0x04;
//	mpegenc->b_direct_mv_table[0] = 0x00;
//	mpegenc->b_direct_mv_table[1] = 0x04;
//	mpegenc->b_direct_mv_table_base[0] = 0x00;
//	mpegenc->b_direct_mv_table_base[1] = 0x04;
//	mpegenc->b_forw_mv_table[0] = 0x00;
//	mpegenc->b_forw_mv_table[1] = 0x04;
//	mpegenc->b_forw_mv_table_base[0] = 0x00;
//	mpegenc->b_forw_mv_table_base[1] = 0x04;
	//encode_mb_internalで使わえるのはこの値
//	int64_t block[64] = {-2146590848,-2146590720,-2146590592,-2146590464,-2146590336,-2146590208,-2146590080,-2146589952,-2146589824,-2146589696,-2146589568,-2146589440,-2146589312,-2146589184,-2146589056,-2146588928,-2146588800,-2146588672,-2146588544,-2146588416,-2146588288,-2146588160,-2146588032,-2146587904,-2146587776,-2146587648,-2146587520,-2146587392,-2146587264,-2146587136,-2146587008,-2146586880,-2146586752,-2146586624,-2146586496,-2146586368,-2146586240,-2146586112,-2146585984,-2146585856,-2146585728,-2146585600,-2146585472,-2146585344,-2146585216,-2146585088,-2146584960,-2146584832,-2146584704,-2146584576,-2146584448,-2146584320,-2146584192,-2146584064,-2146583936,-2146583808,-2146583680,-2146583552,-2146583424,-2146583296,-2146583168,-2146583040,-2146582912,-2146582784};
//	int64_t block[64] = {0};
	//	mpegenc->block = block;
	//メンバの値を決めたりしないのであればDSPContextとかを0初期化又はallocする必要はない
//	DSPContext* dsp = av_malloc(sizeof(DSPContext));
//	memset(mpegenc, 0, sizeof(DSPContext));
//	mpegenc->dsp = *dsp;

	Picture *pict = av_malloc(sizeof(Picture));
	if(!pict){
		LOGD("Failed alloc pict");
		return NULL;
	}
	mpegenc->current_picture = *pict;

	avctx->priv_data = mpegenc;
        //ここはオプション入れるのが面倒なのでテストの仕方を考えながら代替策を実装

//    if (codec && codec->defaults) {
//        int ret;
//        const AVCodecDefault *d = codec->defaults;
//        while (d->key) {
////            ret = av_opt_set(avctx, d->key, d->value, 0);//ここはオプション入れるのが面倒なのでテストの仕方を考えながら代替策を実装
//            av_assert0(ret >= 0);
//            d++;
//        }
//    }


//    free(next);//落ちる
    return avctx;
}



int audio_pts = 0,video_pts = 0;
pthread_mutex_t mutex;  // Mutex

int write_video_pkt(AVCodecContext* avctx,AVPacket* pkt){
	int out_size = pkt->size;
    LOGD("encode after write_video size:%d pts:%d\n", out_size,pkt->pts);
	//タグのメタ情報
			         //実データサイズ キーフレーム分1足す?
					 video_tag_info_data[0] = 0x09;
			         video_tag_info_data[1] = (out_size+1) >> 16;
			         video_tag_info_data[2] = (out_size+1) >> 8;
			         video_tag_info_data[3] = out_size+1;
			         //PTS
			         video_tag_info_data[4] = pkt->pts >> 16;
			         video_tag_info_data[5] = pkt->pts >> 8;
			         video_tag_info_data[6] = pkt->pts;
			         video_pts = pkt->pts;
			         video_tag_info_data[11] = (avctx->frame_number-1) % avctx->gop_size == 0? 0x12:0x22;

			         pthread_mutex_lock( &mutex );//IOが同時に発生しないように
			         fwrite(video_tag_info_data, sizeof(char)*12,sizeof(char),out_file);
			         fwrite(pkt->data,sizeof(uint8_t)*out_size, sizeof(uint8_t),out_file);
			         video_tag_end_data[0] = (out_size+11) >> 24;
			         video_tag_end_data[1] = (out_size+11) >> 16;
			         video_tag_end_data[2] = (out_size+11) >> 8;
			         fwrite(video_tag_end_data, sizeof(char)*4,sizeof(char),out_file);
					 pthread_mutex_unlock( &mutex );
					 return 0;

}

int write_pcm(AVCodecContext* avctx,AVPacket* pkt)
{
//		//タグのメタ情報
	int out_size = pkt->size;
						 audio_tag_info_data[0] = 0x08;
						 audio_tag_info_data[1] = out_size >> 16;
						 audio_tag_info_data[2] = out_size >> 8;
						 audio_tag_info_data[3] = out_size;
				         //PTS
						 audio_tag_info_data[4] = pkt->pts >> 16;
						 audio_tag_info_data[5] = pkt->pts >> 8;
						 audio_tag_info_data[6] = pkt->pts;

						 audio_pts = pkt->pts;

						 audio_tag_info_data[11] = 0x3E;

				pthread_mutex_lock( &mutex );//IOが同時に発生しないように
				         fwrite(audio_tag_info_data, sizeof(char)*12,sizeof(char),out_file);
				         fwrite(pkt->data,sizeof(char)*out_size-1, sizeof(char),out_file);
				         audio_tag_end_data[0] = (out_size+11) >> 24;
				         audio_tag_end_data[1] = (out_size+11) >> 16;
				         audio_tag_end_data[2] = (out_size+11) >> 8;
				         audio_tag_end_data[3] = (out_size+11) ;
				         fwrite(audio_tag_end_data, sizeof(char)*4,sizeof(char),out_file);

				 pthread_mutex_unlock( &mutex );
				 LOGD("write_pcm size:%d audio_pts:%d",out_size,audio_pts);
//				 LOGD("writeout WRITE_DEBUG\n");
//				     	int i;
//				     	for(i = 0; i < out_size&&i<30; i++){
//				     		LOGD("%02X ",pkt->data[i]);
//				     	}
//				 		LOGD("\n");
				 return 0;
}

int write_audio_pkt(AVCodecContext* avctx,AVPacket* pkt)
{
	int out_size = pkt->size+1;
//		//タグのメタ情報
						 audio_tag_info_data[0] = 0x08;
						 audio_tag_info_data[1] = out_size >> 16;
						 audio_tag_info_data[2] = out_size >> 8;
						 audio_tag_info_data[3] = out_size;
				         //PTS
						 audio_tag_info_data[4] = audio_pts >> 16;
						 audio_tag_info_data[5] = audio_pts >> 8;
						 audio_tag_info_data[6] = audio_pts;
						 //1152/44100=0.02612244897959183673469387755102//10万回目まで考えるので43分程度は時間がずれない想定
						 if(avctx->frame_number % 1000 == 0){
							 audio_pts += 35;//1+2+2+4
						 }else if(avctx->frame_number % 1000 == 0){
							 audio_pts += 32;//1+2+2
						 }else if(avctx->frame_number % 100 == 0){
							 audio_pts += 30;//1+2
						 }else if(avctx->frame_number % 10 == 0){
							 audio_pts += 27;
						 }else{
							 audio_pts += 26;
						 }

						 audio_tag_info_data[11] = 0x2E;
				pthread_mutex_lock( &mutex );//IOが同時に発生しないように
				         fwrite(audio_tag_info_data, sizeof(char)*12,sizeof(char),out_file);
				         fwrite(pkt->data,sizeof(char)*out_size-1, sizeof(char),out_file);
				         audio_tag_end_data[0] = (out_size+11) >> 24;
				         audio_tag_end_data[1] = (out_size+11) >> 16;
				         audio_tag_end_data[2] = (out_size+11) >> 8;
				         audio_tag_end_data[3] = (out_size+11) ;
				         fwrite(audio_tag_end_data, sizeof(char)*4,sizeof(char),out_file);

				 pthread_mutex_unlock( &mutex );
				 return 0;
}

int write_mp3(AVCodecContext* avctx,AVPacket* pkt)
{
    LOGD("After enc write_AUDIO size:%d pts:%d\n", pkt->size,audio_pts);
	 fwrite(pkt->data,sizeof(char)*pkt->size, sizeof(char),out_file);
	 return 0;
}

void write_boss_pkt(AVCodecContext* avctx,AVPacket* pkt)
{
//   if (!pkt){
//	   LOGD("Failed output write_boss_pkt.");
//   }
   if(avctx->codec_type == AVMEDIA_TYPE_AUDIO){
//		  audio_pts = ((double)frame->pts * avctx->time_base.num /
//				  avctx->time_base.den);
//	   LOGD("write_boss AUDIO pts:%d VIDEO pts:%d\n",audio_pts,video_pts);
//	   while(audio_pts > video_pts){//video_ptsの方が進むまで待機
//		   usleep(1000);//1ms
//	   }
	      write_audio_pkt(avctx, pkt);//-が返ってきたら落とす処理を作るべきか?
   }else{
//      video_pts = ((double)frame->pts * avctx->time_base.num /
//    	  avctx->time_base.den);
//	   LOGD("write_boss VIDEO pts:%d AUDIO pts:%d\n",video_pts,audio_pts);
	    while( audio_pts <= video_pts){//audio_ptsの方が進むまで待機
			   usleep(1000);//1ms
		   }
	      write_video_pkt(avctx, pkt);
   }
}

void write_boss_pcm(AVCodecContext* avctx,AVPacket* pkt)
{

   if(avctx->codec_type == AVMEDIA_TYPE_AUDIO){
	   while(audio_pts > video_pts){//video_ptsの方が進むまで待機
		   usleep(1000);//1ms
	   }
	   write_pcm(avctx, pkt);//-が返ってきたら落とす処理を作るべきか?
   }else{
	    while( audio_pts <= video_pts){//audio_ptsの方が進むまで待機
			   usleep(1000);//1ms
		   }
	      write_video_pkt(avctx, pkt);
   }
}
	uint8_t video_start_data[] = {
	    0x46,0x4C,0x56,0x01,0x01,0x00,0x00,0x00,0x09 ,//5バイト目は0x01映像のみ,0x04音声のみ,0x05両方
	    0x00,0x00,0x00,0x00,//前のタグのサイズ
	    0x12,
	    0x00,0x00,0xB8,
	    0x00,0x00,0x00,0x00,0x00,0x00,0x00,
	    //	メタデータ
	    0x02,0x00,0x0A,0x6F,0x6E,0x4D,0x65,0x74,0x61,0x44,0x61,0x74,0x61,0x08,0x00,0x00,0x00,0x08,0x00,0x08,0x64,0x75,0x72,0x61,0x74,0x69,0x6F,0x6E,0x00,0x3F,0xE7,0x7C,0xED,0x91,0x68,0x72,0xB0,0x00,0x05,0x77,0x69,0x64,0x74,0x68,0x00,0x40,0x7E,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x06,0x68,0x65,0x69,0x67,0x68,0x74,0x00,0x40,0x74,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0D,0x76,0x69,0x64,0x65,0x6F,0x64,0x61,0x74,0x61,0x72,0x61,0x74,0x65,0x00,0x40,0x68,0x6A,0x00,0x00,0x00,0x00,0x00,0x00,0x09,0x66,0x72,0x61,0x6D,0x65,0x72,0x61,0x74,0x65,0x00,0x40,0x2E,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0C,0x76,0x69,0x64,0x65,0x6F,0x63,0x6F,0x64,0x65,0x63,0x69,0x64,0x00,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0x65,0x6E,0x63,0x6F,0x64,0x65,0x72,0x02,0x00,0x0D,0x4C,0x61,0x76,0x66,0x35,0x35,0x2E,0x32,0x32,0x2E,0x31,0x30,0x30,0x00,0x08,0x66,0x69,0x6C,0x65,0x73,0x69,0x7A,0x65,0x00,0x41,0x01,0x42,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x09,0x00,0x00,0x00,0xC3,
	    //総サイズ
	    0x00,0x00,0x00,0xC3,
		};
	uint8_t audio_start_data_mp3[] = {
		0x46,0x4C,0x56,0x01,0x04,0x00,0x00,0x00,0x09,
		0x00,0x00,0x00,0x00,
		0x12,
		0x00,0x00,0xC1,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x02,0x00,0x0A,0x6F,0x6E,0x4D,0x65,0x74,0x61,0x44,0x61,0x74,0x61,0x08,0x00,0x00,0x00,0x08,0x00,0x08,0x64,0x75,0x72,0x61,0x74,0x69,0x6F,0x6E,0x00,0x40,0x23,0xBE,0x76,0xC8,0xB4,0x39,0x58,0x00,0x0D,0x61,0x75,0x64,0x69,0x6F,0x64,0x61,0x74,0x61,0x72,0x61,0x74,0x65,0x00,0x40,0x48,0x6A,0x00,0x00,0x00,0x00,0x00,0x00,0x0F,0x61,0x75,0x64,0x69,0x6F,0x73,0x61,0x6D,0x70,0x6C,0x65,0x72,0x61,0x74,0x65,0x00,0x40,0xE5,0x88,0x80,0x00,0x00,0x00,0x00,0x00,0x0F,0x61,0x75,0x64,0x69,0x6F,0x73,0x61,0x6D,0x70,0x6C,0x65,0x73,0x69,0x7A,0x65,0x00,0x40,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x06,0x73,0x74,0x65,0x72,0x65,0x6F,0x01,0x00,0x00,0x0C,0x61,0x75,0x64,0x69,0x6F,0x63,0x6F,0x64,0x65,0x63,0x69,0x64,0x00,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0x65,0x6E,0x63,0x6F,0x64,0x65,0x72,0x02,0x00,0x0D,0x4C,0x61,0x76,0x66,0x35,0x35,0x2E,0x32,0x32,0x2E,0x31,0x30,0x30,0x00,0x08,0x66,0x69,0x6C,0x65,0x73,0x69,0x7A,0x65,0x00,0x40,0xEF,0xFD,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x09,
		0x00,0x00,0x00,0xCC
		 };
	uint8_t audio_start_data_pcm[] = {
		0x46,0x4C,0x56,0x01,0x04,0x00,0x00,0x00,0x09,
		0x00,0x00,0x00,0x00,
		0x12,
		0x00,0x00,0xC1,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x02,0x00,0x0A,0x6F,0x6E,0x4D,0x65,0x74,0x61,0x44,0x61,0x74,0x61,0x08,0x00,0x00,0x00,0x08,0x00,0x08,0x64,0x75,0x72,0x61,0x74,0x69,0x6F,0x6E,0x00,0x40,0x23,0xB1,0x26,0xE9,0x78,0xD4,0xFE,0x00,0x0D,0x61,0x75,0x64,0x69,0x6F,0x64,0x61,0x74,0x61,0x72,0x61,0x74,0x65,0x00,0x40,0x85,0x88,0x80,0x00,0x00,0x00,0x00,0x00,0x0F,0x61,0x75,0x64,0x69,0x6F,0x73,0x61,0x6D,0x70,0x6C,0x65,0x72,0x61,0x74,0x65,0x00,0x40,0xE5,0x88,0x80,0x00,0x00,0x00,0x00,0x00,0x0F,0x61,0x75,0x64,0x69,0x6F,0x73,0x61,0x6D,0x70,0x6C,0x65,0x73,0x69,0x7A,0x65,0x00,0x40,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x06,0x73,0x74,0x65,0x72,0x65,0x6F,0x01,0x00,0x00,0x0C,0x61,0x75,0x64,0x69,0x6F,0x63,0x6F,0x64,0x65,0x63,0x69,0x64,0x00,0x40,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0x65,0x6E,0x63,0x6F,0x64,0x65,0x72,0x02,0x00,0x0D,0x4C,0x61,0x76,0x66,0x35,0x35,0x2E,0x32,0x32,0x2E,0x31,0x30,0x30,0x00,0x08,0x66,0x69,0x6C,0x65,0x73,0x69,0x7A,0x65,0x00,0x41,0x2A,0x88,0x1A,0x00,0x00,0x00,0x00,0x00,0x00,0x09,
		0x00,0x00,0x00,0xCC
		};
	uint8_t boss_start_data_mp3[]={
		0x46,0x4C,0x56,0x01,0x05,0x00,0x00,0x00,0x09,
		0x00,0x00,0x00,0x00,
		0x12,
		0x00,0x01,0x25,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x02,0x00,0x0A,0x6F,0x6E,0x4D,0x65,0x74,0x61,0x44,0x61,0x74,0x61,0x08,0x00,0x00,0x00,0x0D,0x00,0x08,0x64,0x75,0x72,0x61,0x74,0x69,0x6F,0x6E,0x00,0x40,0x23,0xBE,0x76,0xC8,0xB4,0x39,0x58,0x00,0x05,0x77,0x69,0x64,0x74,0x68,0x00,0x40,0x7E,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x06,0x68,0x65,0x69,0x67,0x68,0x74,0x00,0x40,0x74,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0D,0x76,0x69,0x64,0x65,0x6F,0x64,0x61,0x74,0x61,0x72,0x61,0x74,0x65,0x00,0x40,0x68,0x6A,0x00,0x00,0x00,0x00,0x00,0x00,0x09,0x66,0x72,0x61,0x6D,0x65,0x72,0x61,0x74,0x65,0x00,0x40,0x2E,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0C,0x76,0x69,0x64,0x65,0x6F,0x63,0x6F,0x64,0x65,0x63,0x69,0x64,0x00,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0D,0x61,0x75,0x64,0x69,0x6F,0x64,0x61,0x74,0x61,0x72,0x61,0x74,0x65,0x00,0x40,0x48,0x6A,0x00,0x00,0x00,0x00,0x00,0x00,0x0F,0x61,0x75,0x64,0x69,0x6F,0x73,0x61,0x6D,0x70,0x6C,0x65,0x72,0x61,0x74,0x65,0x00,0x40,0xE5,0x88,0x80,0x00,0x00,0x00,0x00,0x00,0x0F,0x61,0x75,0x64,0x69,0x6F,0x73,0x61,0x6D,0x70,0x6C,0x65,0x73,0x69,0x7A,0x65,0x00,0x40,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x06,0x73,0x74,0x65,0x72,0x65,0x6F,0x01,0x00,0x00,0x0C,0x61,0x75,0x64,0x69,0x6F,0x63,0x6F,0x64,0x65,0x63,0x69,0x64,0x00,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0x65,0x6E,0x63,0x6F,0x64,0x65,0x72,0x02,0x00,0x0D,0x4C,0x61,0x76,0x66,0x35,0x35,0x2E,0x32,0x32,0x2E,0x31,0x30,0x30,0x00,0x08,0x66,0x69,0x6C,0x65,0x73,0x69,0x7A,0x65,0x00,0x41,0x03,0x28,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x09,
		0x00,0x00,0x01,0x30
		};

int boss_flag = 2;
void video_thread(JNIEnv* env){
	 AVCodec *v_codec = alloc_flv_codec();
		 	v_codec->write_func = write_boss_pkt;
			video_encode_example(env,v_codec);
			LOGD("END_THREAD_FOR_VIDEO");
			boss_flag--;
}
void audio_thread(JNIEnv* env){
	AVCodec *a_codec = alloc_mp3_codec();
	a_codec->write_func = write_boss_pkt;
	audio_encode_example(env,a_codec);
	LOGD("END_THREAD_FOR_AUDIO");
	boss_flag--;
}

void audio_thread_pcm(){
	AVCodec *a_codec = alloc_pcm_codec();
	a_codec->write_func = write_boss_pcm;
	audio_write_pcm(a_codec);
	LOGD("END_THREAD_FOR_AUDIO");
	boss_flag--;
}
int startenc(JNIEnv* env,int flag){

	clock_t start,end;
	start = clock();

    out_file = fopen("/sdcard/test_movie.flv", "wb");
    if (!out_file) {
    	LOGD("Could not open output\n");
        exit(1);
    }
	switch(flag){
	case 1:{//Audioのみ
	     memset(audio_tag_info_data,0,11);
	     memset(audio_tag_end_data,0,4);
	     AVCodec *a_codec = alloc_pcm_codec();
	     //FLVの最初のデータとメタデータを書き込む
	        fwrite(audio_start_data_pcm, sizeof(char) * 221,sizeof(char),out_file);
//        fwrite(audio_start_data_mp3, sizeof(char) * 221,sizeof(char),out_file);
//		a_codec->write_func = write_audio_pkt;
		a_codec->write_func = write_pcm;
//        		a_codec->write_func = write_mp3;
//		audio_encode_example(a_codec);
		audio_write_pcm(a_codec);
	}
	break;
	case 2:{//VIdeoのみ
	     memset(video_tag_info_data,0,11);
	     memset(video_tag_end_data,0,4);
	 	 AVCodec *codec = alloc_flv_codec();
	     //FLVの最初のデータとメタデータを書き込む
         fwrite(video_start_data, sizeof(char) * 212,sizeof(char),out_file);
	 	 codec->write_func = write_video_pkt;
         video_encode_example(env,codec);
		}
	break;
	case 3:{//VideoAudio
	     memset(video_tag_info_data,0,11);
	     memset(video_tag_end_data,0,4);
	     memset(audio_tag_info_data,0,11);
	     memset(audio_tag_end_data,0,4);
	     //FLVの最初のデータとメタデータを書き込む
         fwrite(boss_start_data_mp3, sizeof(char) * 321,sizeof(char),out_file);
         //VIDEO,AUDIOそれぞれ実行
         pthread_t video_th, audio_th; // スレッド識別変数
         pthread_mutex_init(&mutex, NULL);
         // スレッドの作成
         pthread_create(&audio_th, NULL, audio_thread_pcm, env);
         pthread_create(&video_th, NULL, video_thread, env);

         int timeout = 0;
         while(boss_flag>0){
         sleep(1);
         LOGD("TEST TGH :%d\n",boss_flag);
         if(timeout > 30){
        	 fclose(out_file);
             LOGD("FORCE END\n");
        	 return -1;
         }
         timeout++;

         }
         // スレッド終了待ち
         pthread_join(video_th,NULL);
         pthread_join(audio_th,NULL);
         pthread_mutex_destroy(&mutex);
	}
	break;
	}
    fclose(out_file);
    LOGD("END_MAIN");
	end = clock();
LOGD("Time was %.2f sec¥n",(double)(end-start)/CLOCKS_PER_SEC);
	return 0;
}

int video_encode_example(JNIEnv *env,AVCodec* codec){

	     if (!codec) {
	         LOGD( "codec not found\n");
	         return -1;
	     }
	     AVCodecContext *avctx = video_summarize_init(codec);
	     int i, ret,out_size, size, outbuf_size;
	     AVFrame *frame;
	     AVPacket *outpkt;
	     int *got_output;

	     avcodec_set_context_defaults3(avctx,codec);
	     frame = (AVFrame*)avcodec_alloc_frame();

	     /* put sample parameters */
	     /* resolution must be a multiple of two */
	     avctx->width = 480;
	     avctx->height = 320;
	     avctx->pix_fmt = AV_PIX_FMT_YUV420P;//ここでは420Pだけど、avpicture_fillはNV21で呼ぶ
//	     avctx->pix_fmt = AV_PIX_FMT_NV21;
//	     avctx->codec_tag = MKTAG('N','V','2','1');
	     /* open it */
	     if (codec_open_test(avctx, codec) < 0) {
	         LOGD( "could not open codec\n");
	         return -1;
	     }
	     /* alloc image and output buffer */
//	     outbuf_size = 100000;
	     outbuf_size = sizeof(AVPacket);
	     outpkt = av_malloc(outbuf_size);
	     size = avctx->width * avctx->height;

	     frame->data[0] = malloc(size * 3 / 2); /* size for YUV 420 */
	     frame->data[1] = frame->data[0] + size;//U
	     frame->data[2] = frame->data[1] + size / 4;//V

	     if(get_linesizes(frame->linesize,avctx->width,AV_PIX_FMT_NV21)<0){
	    	 LOGD("Failed get_linesize");
	    	 return -3;
	     }

	     //データをセットする
	     int fd = open("/sdcard/testYUV.yuv",O_RDONLY|S_IRUSR|S_IROTH|S_IROTH);
	     			if(fd < 0){
	     				LOGD("Failed open file :%d",fd);
	     				return -1;
	     			}
	     			uint8_t buf[size*3/2];
	 	            int read_size;
	 	           read_size = 1;
			   while(read_size){
	 	    	out_size = 0;
	 		            read_size = read(fd, buf, size*3/2);
	 		            if ( read_size > 0){
// 		                	LOGD("INPUT_DATA\n");
//	 		                for(i = 0; i < 30; i++){
//	 		                	LOGD("%02X ",buf[i]);
//	 		                }
// 		                	LOGD("\n");
	 		            } else {
	 		            	LOGD("Read size was empty: %d ",read_size);
	 		                break;
	 		            }

				avpicture_fill(frame->data,frame->linesize,buf,AV_PIX_FMT_NV21,avctx->width,avctx->height,1);

	        	 LOGD( "Before A encode_video2");

	     	    MpegEncContext *s = avctx->priv_data;
	     	 	LOGD("TOTAL_BITS_C:%d\n",s->total_bits);
	         ret = avcodec_encode_video2(avctx, outpkt, frame, &got_output);
	        	 LOGD( "After A encode_video2");
	         out_size = outpkt->size+1;
	         if(ret < 0){
	        	 LOGD( "Failed encode_video2_first\n");
	        	 return -1;
	         }else if(got_output && out_size > 1){
		         codec->write_func(env,avctx,outpkt);
	         }else{
	        	 break;
	         }

	         for (got_output = 1; got_output; i++) {
	        	 //frameがNULLの時しか通らないIFをこっちに持ってきた
//	     	    if (!(avctx->codec->capabilities & CODEC_CAP_DELAY) && !frame) {//capabilitiesの時に呼ばれるから結構呼ばれている
//	     	        av_free_packet(outpkt);
//	     	        av_init_packet(outpkt);
//	     	       outpkt->size = 1;//正常にエンコされる場合はFLVの関係で1を足すのでこの場合0が帰ってくるので1を足す
//	     	        ret = 0;
//	     	    }
 	        	 LOGD( "Before B encode_video2");
		         ret = avcodec_encode_video2(avctx, outpkt, NULL, &got_output);
 	        	 LOGD( "After B encode_video2");
		         out_size = outpkt->size+1;

		         if(ret < 0){
		         	        	 LOGD( "Failed encode_video2_second\n");
		         	        	 return -1;
		         }else if(got_output && out_size > 1){//out_sizeは1を足しているので>1
			         codec->write_func(env,avctx,outpkt);
		         }else{
		        	 break;
		         }
		     }
	 }

	     free(got_output);
	     free(outpkt);
	     avcodec_close(avctx);
	     av_free(avctx);
	     av_free(frame);
	     LOGD("VIDEO_ENC_END\n");
	     video_pts = __INT_MAX__;
	return 0;
}


AVCodecContext* aflv_summarize_init(AVCodec *codec)
{
    AVCodecContext* avctx= av_malloc(sizeof(AVCodecContext));
    memset(avctx, 0, sizeof(AVCodecContext));

    avctx->codec_type =  AVMEDIA_TYPE_VIDEO;
    avctx->get_buffer          = video_get_buffer;
    avctx->release_buffer      = avcodec_default_release_buffer;
    avctx->get_format          = avcodec_default_get_format;
    avctx->execute             = avcodec_default_execute;
//    avctx->execute2            = avcodec_default_execute2;
    avctx->sample_aspect_ratio = (AVRational){0,1};
    avctx->pix_fmt             = AV_PIX_FMT_NONE;
    avctx->sample_fmt          = AV_SAMPLE_FMT_NONE;
    avctx->timecode_frame_start = -1;

    avctx->reget_buffer        = avcodec_default_reget_buffer;
    avctx->reordered_opaque    = AV_NOPTS_VALUE;
	avctx->b_sensitivity = 40;
	avctx->bidir_refine = 1;
	avctx->bit_rate = 200000;
	avctx->codec_id = 22;
	avctx->bit_rate_tolerance = 4000000;
	//定数臭いがわからん
	avctx->i_quant_factor = -0.8;
	avctx->i_quant_offset = 0.0;
	avctx->b_quant_factor = 1.25;
	avctx->b_quant_offset = 1.25;

	avctx->codec = codec;


	AVCodecDescriptor* cdescriptor = av_malloc(sizeof(AVCodecDescriptor));
	if(cdescriptor < 0){
	    	LOGD("Failed cdescriptor alloc");
	    	return NULL;
	}
	memset(cdescriptor, 0, sizeof(AVCodecDescriptor));
	avctx->codec_descriptor = cdescriptor;
	strcpy(avctx->codec_name,"raw");
//	avctx->coded_height = 320;
//	avctx->coded_width = 480;
	avctx->color_primaries = 2;
	avctx->color_trc = 2;
	avctx->colorspace = 2;
	avctx->compression_level = -1;
	avctx->err_recognition = 1;
	avctx->error_concealment = 3;
	avctx->flags = 4194304;
	avctx->frame_skip_cmp = 13;
	avctx->gop_size = 15;//試してみる
	avctx->height = 320;
	avctx->ildct_cmp = 8;
	avctx->inter_quant_bias = 999999;
	avctx->intra_quant_bias = 0;
	avctx->keyint_min = 25;
	avctx->level = -99;
	avctx->lmax = 3658;
	avctx->lmin = 236;
	avctx->max_qdiff = 3;
	avctx->mb_lmax = 3658;
	avctx->mb_lmin = 236;
	avctx->me_method = 5;
	avctx->me_penalty_compensation = 256;
	avctx->me_subpel_quality = 8;
	avctx->mv0_threshold = 256;
	avctx->profile = -99;
	avctx->qblur = 0.500000;
	avctx->qcompress = 0.500000;
	avctx->qmax = 31;
	avctx->qmin = 2;
	avctx->rc_buffer_aggressivity = 1.000000;
	avctx->rc_min_vbv_overflow_use = 3.000000;
	avctx->refs = 1;
	avctx->sample_fmt = -1;
	avctx->scenechange_factor = 6;
	avctx->thread_count = 1;
	avctx->thread_type = 3;
	avctx->ticks_per_frame = 1;
    /* frames per second */
	AVRational* time_base = av_malloc(sizeof(AVRational));
    time_base->den = 15;
    time_base->num = 1;
	avctx->time_base = *time_base;
	avctx->timecode_frame_start = -1;
	avctx->width = 480;
	avctx->workaround_bugs = 1;

	MpegEncContext* mpegenc = av_malloc(sizeof(MpegEncContext));
	if(!mpegenc){
		LOGD("Failed alloc priv_data");
		return NULL;
	}
	memset(mpegenc, 0, sizeof(MpegEncContext));
	mpegenc->avctx = avctx;
//	mpegenc->b_back_mv_table[0] = (int16_t)0x00;
//	mpegenc->b_back_mv_table[1] = 0x04;
//	mpegenc->b_back_mv_table_base[0] = 0x00;
//	mpegenc->b_back_mv_table_base[1] = 0x04;
//	mpegenc->b_bidir_back_mv_table[0] = 0x00;
//	mpegenc->b_bidir_back_mv_table[1] = 0x04;
//	mpegenc->b_bidir_back_mv_table_base[0] = 0x00;
//	mpegenc->b_bidir_back_mv_table_base[1] = 0x04;
//	mpegenc->b_bidir_forw_mv_table[0] = 0x00;
//	mpegenc->b_bidir_forw_mv_table[1] = 0x04;
//	mpegenc->b_bidir_forw_mv_table_base[0] = 0x00;
//	mpegenc->b_bidir_forw_mv_table_base[1] = 0x04;
//	mpegenc->b_direct_mv_table[0] = 0x00;
//	mpegenc->b_direct_mv_table[1] = 0x04;
//	mpegenc->b_direct_mv_table_base[0] = 0x00;
//	mpegenc->b_direct_mv_table_base[1] = 0x04;
//	mpegenc->b_forw_mv_table[0] = 0x00;
//	mpegenc->b_forw_mv_table[1] = 0x04;
//	mpegenc->b_forw_mv_table_base[0] = 0x00;
//	mpegenc->b_forw_mv_table_base[1] = 0x04;
	//encode_mb_internalで使わえるのはこの値
//	int64_t block[64] = {-2146590848,-2146590720,-2146590592,-2146590464,-2146590336,-2146590208,-2146590080,-2146589952,-2146589824,-2146589696,-2146589568,-2146589440,-2146589312,-2146589184,-2146589056,-2146588928,-2146588800,-2146588672,-2146588544,-2146588416,-2146588288,-2146588160,-2146588032,-2146587904,-2146587776,-2146587648,-2146587520,-2146587392,-2146587264,-2146587136,-2146587008,-2146586880,-2146586752,-2146586624,-2146586496,-2146586368,-2146586240,-2146586112,-2146585984,-2146585856,-2146585728,-2146585600,-2146585472,-2146585344,-2146585216,-2146585088,-2146584960,-2146584832,-2146584704,-2146584576,-2146584448,-2146584320,-2146584192,-2146584064,-2146583936,-2146583808,-2146583680,-2146583552,-2146583424,-2146583296,-2146583168,-2146583040,-2146582912,-2146582784};
//	int64_t block[64] = {0};
	//	mpegenc->block = block;
	//メンバの値を決めたりしないのであればDSPContextとかを0初期化又はallocする必要はない
//	DSPContext* dsp = av_malloc(sizeof(DSPContext));
//	memset(mpegenc, 0, sizeof(DSPContext));
//	mpegenc->dsp = *dsp;

	Picture *pict = av_malloc(sizeof(Picture));

	mpegenc->current_picture = *pict;

	avctx->priv_data = mpegenc;

//    free(next);//落ちる
//    free(avctx);
    return avctx;
}


AVCodecContext* audio_summarize_init(AVCodec* codec){


	   //AUDIO用のAVCodecContextを生成する
	AVCodecContext* avctx = av_malloc(sizeof(AVCodecContext));
	    if(!avctx){
	    	LOGD("Failed set_context_default audio\n");
	        av_free(avctx);
	    	return NULL;
	    }
		if(avcodec_set_context_defaults3(avctx, codec) < 0){
			LOGD("Failed avcodec_set_context_defaults3\n");
			return NULL;
		}

		avctx->internal = av_mallocz(sizeof(AVCodecInternal));
		    if (!avctx->internal) {
		    	LOGD("Failed alloc internal\n");
		            exit(1);
		        }
		    avctx->priv_data = av_mallocz(codec->priv_data_size);
		    avctx->codec_type = codec->type;
		    avctx->codec_id   = codec->id;
		    avctx->codec = codec;

		    //AVCodecContextにパラメタをセットする
		    avctx->codec_type =  AVMEDIA_TYPE_AUDIO;
		    avctx->get_buffer          = audio_get_buffer;
		    avctx->release_buffer      = avcodec_default_release_buffer;
		    avctx->get_format          = avcodec_default_get_format;
		    avctx->execute             = avcodec_default_execute;
//		    avctx->execute2            = avcodec_default_execute2;
		    avctx->pix_fmt             = AV_PIX_FMT_NONE;
		    avctx->timecode_frame_start = -1;
		    avctx->reget_buffer        = avcodec_default_reget_buffer;
		    avctx->reordered_opaque    = AV_NOPTS_VALUE;
		    avctx->sample_fmt = AV_SAMPLE_FMT_S16P;//AV_SAMPLE_FMT_S16
			avctx->b_sensitivity = 40;
			avctx->bidir_refine = 1;
			avctx->bit_rate = 32000;
			avctx->bit_rate_tolerance = 32000;//一番低いやつ
			/*
			 *.id        = AV_CODEC_ID_MP3,
	        .type      = AVMEDIA_TYPE_AUDIO,
	        .name      = "mp3",
	        .long_name = NULL_IF_CONFIG_SMALL("MP3 (MPEG audio layer 3)"),
	        .props     = AV_CODEC_PROP_LOSSY,
			 */
			AVCodecDescriptor* cdesc = av_malloc(sizeof(AVCodecDescriptor));
			if(cdesc < 0){
			    	LOGD("Failed cdescriptor alloc");
			    	return NULL;
			}
			memset(cdesc, 0, sizeof(AVCodecDescriptor));
			cdesc->id = 86017;
			cdesc->long_name = "MP3 (MPEG audio layer 3)";
			cdesc->name="mp3";
			cdesc->props = 2;
			cdesc->type = 1;
			avctx->channel_layout = AV_CH_LAYOUT_MONO;
			avctx->channels = 1;
			avctx->codec_descriptor = cdesc;
			avctx->codec_id = 86017;
			avctx->codec_type = 1;
			avctx->compression_level = -1;
			avctx->context_model = 0;
			avctx->cutoff = 0;
			avctx->dark_masking = 0.000000;
			avctx->err_recognition = 1;
			avctx->coded_frame = 0;//何故64なのかよくわからない
			avctx->flags = 4194304;
			avctx->frame_size = 1152;
			//データを入れる
			AVCodecInternal *internal = av_malloc(sizeof(AVCodecInternal));
			memset(internal,0,sizeof(AVCodecInternal));
			avctx->internal = internal;
			avctx->level = -99;
			avctx->max_prediction_order = -1;
			avctx->min_prediction_order = -1;
			avctx->priv_data;//何か入っているが取り敢えず省略
			avctx->profile = -99;
			avctx->request_sample_fmt = -1;
			AVRational* sar = av_malloc(sizeof(AVRational));
			sar->den = 1;
			sar->num = 0;
			avctx->sample_aspect_ratio = *sar;
			avctx->sample_fmt = AV_SAMPLE_FMT_S16P;
			avctx->sample_rate = 44100;
			avctx->thread_count = 1;
			avctx->ticks_per_frame = 1;
			AVRational *time_base = av_malloc(sizeof(AVRational));
		    time_base->den = 44100;
		    time_base->num = 1;
			avctx->time_base = *time_base;
			avctx->timecode_frame_start = -1;

		    //スレッドを初期化する
		    ff_frame_thread_encoder_init(avctx, NULL);
		    //エンコーダを初期化する
		    codec->init(avctx);
	return avctx;
}


AVCodecContext* audio_summarize_init_pcm(AVCodec* codec){


	   //AUDIO用のAVCodecContextを生成する
	AVCodecContext* avctx = av_malloc(sizeof(AVCodecContext));
	    if(!avctx){
	    	LOGD("Failed set_context_default audio\n");
	        av_free(avctx);
	    	return NULL;
	    }
		if(avcodec_set_context_defaults3(avctx, codec) < 0){
			LOGD("Failed avcodec_set_context_defaults3\n");
			return NULL;
		}

		avctx->internal = av_mallocz(sizeof(AVCodecInternal));
		    if (!avctx->internal) {
		    	LOGD("Failed alloc internal\n");
		            exit(1);
		        }
		    avctx->priv_data = av_mallocz(codec->priv_data_size);

			avctx->b_sensitivity = -1;
			avctx->bidir_refine = 1;
			avctx->bit_rate = 705600;
			avctx->bit_rate_tolerance = 0;
			avctx->bits_per_coded_sample = 16;
		    avctx->codec_type = codec->type;
		    avctx->codec_id   = codec->id;
		    avctx->codec = codec;
		    avctx->codec_type =  AVMEDIA_TYPE_AUDIO;
		    AVCodecDescriptor* cdesc = av_malloc(sizeof(AVCodecDescriptor));
			if(cdesc < 0){
					LOGD("Failed cdescriptor alloc");
					return NULL;
			}
			memset(cdesc, 0, sizeof(AVCodecDescriptor));
			cdesc->id = 86017;
			cdesc->long_name = "tekitou";
			cdesc->name="next";
			cdesc->props = 2;
			cdesc->type = 1;
			avctx->codec_descriptor = cdesc;
			avctx->channel_layout = AV_CH_LAYOUT_MONO;
			avctx->channels = 1;
			avctx->compression_level = -1;
			avctx->context_model = 0;
			avctx->cutoff = 0;
			avctx->dark_masking = 0.000000;
			avctx->flags = 4194304;
			avctx->frame_size = 20481;
			avctx->err_recognition = 1;
		    avctx->execute             = avcodec_default_execute;
		    avctx->execute2            = avcodec_default_execute2;
		    avctx->get_buffer          = audio_get_buffer;
		    avctx->get_format          = avcodec_default_get_format;

			AVCodecInternal *internal = av_malloc(sizeof(AVCodecInternal));
			memset(internal,0,sizeof(AVCodecInternal));
			avctx->internal = internal;
			avctx->level = -99;
			avctx->max_prediction_order = -1;
			avctx->min_prediction_order = -1;
			avctx->priv_data;//何か入っているが取り敢えず省略
			avctx->profile = -99;
		    avctx->pix_fmt             = AV_PIX_FMT_NONE;
		    avctx->release_buffer      = avcodec_default_release_buffer;
		    avctx->reget_buffer        = avcodec_default_reget_buffer;
		    avctx->reordered_opaque    = AV_NOPTS_VALUE;
			avctx->request_sample_fmt = 1;
		    avctx->sample_fmt = AV_SAMPLE_FMT_S16P;//AV_SAMPLE_FMT_S16
			AVRational* sar = av_malloc(sizeof(AVRational));
			sar->den = 1;
			sar->num = 0;
			avctx->sample_aspect_ratio = *sar;
		    avctx->timecode_frame_start = -1;
			avctx->sample_fmt = AV_SAMPLE_FMT_S16P;
			avctx->sample_rate = 44100;
			avctx->thread_count = 1;
			avctx->ticks_per_frame = 1;
			AVRational *time_base = av_malloc(sizeof(AVRational));
		    time_base->den = 44100;
		    time_base->num = 1;
			avctx->time_base = *time_base;
			avctx->timecode_frame_start = -1;

		    //スレッドを初期化する
		    ff_frame_thread_encoder_init(avctx, NULL);
		    //エンコーダを初期化する
		    codec->init(avctx);
	return avctx;
}

int audio_write_pcm(JNIEnv* env,AVCodec *codec){
	LOGD("audio_write_pcm Called\n");
	AVCodecContext *avctx = audio_summarize_init_pcm(codec);
	    AVFrame *frame;
	    AVPacket pkt;
		int FRAME_SIZE = 20480;
	    int i,  ret, got_output;


	    /* frame containing input raw audio */
	    frame = avcodec_alloc_frame();
	    if (!frame) {
	    	LOGD("Could not allocate audio frame\n");
	        exit(1);
	    }
	    frame->nb_samples     = avctx->frame_size;
	    frame->format         = avctx->sample_fmt;
	    frame->channel_layout = avctx->channel_layout;

	    frame->extended_data = av_mallocz(sizeof(*frame->extended_data));
	    if(!frame->extended_data){
	    	LOGD("Failed alloc extended_data");
	    	return -1;
	    }
	    frame->data[0] = malloc(FRAME_SIZE*2);
	    frame->data[1] = malloc(FRAME_SIZE);
	    if(!frame->data[0] /*|| !frame->data[1]*/){
	    	LOGD("Failed alloc frame->data[]");
	    	return -1;
	    }
	    memset(frame->data[0],0,FRAME_SIZE);
	    memset(frame->data[1],0,FRAME_SIZE);
	    frame->linesize[0] = FRAME_SIZE;
	    frame->nb_samples = FRAME_SIZE/2;
	    pkt = *(AVPacket*)av_malloc(sizeof(AVPacket)+FRAME_SIZE);

	     av_init_packet(&pkt);

	 	LOGD("audio_write_pcm A\n");
	     AVRational millsec;
	 	millsec.den = 1000;
	 	millsec.num = 1;
	    int input_file = open("/sdcard/testPCM.pcm",O_RDONLY|S_IRUSR|S_IROTH|S_IROTH);
	    			if(input_file < 0){
	    				LOGD("Failed open file :%d",input_file);
	    				return -1;
	    			}
	    			uint8_t buf[FRAME_SIZE];
		            int read_size;
	    while(1){
			            read_size = read(input_file, buf, FRAME_SIZE);
			            if ( read_size > 0){
	//		                for(i = 0; i < 10; i++){
	//		                	LOGD("%02X ",buf[i]);
	//		                }
			            } else {
	 		            	LOGD("Read size was empty: %d ",read_size);
			                goto END_;
			            }

//	    	LOGD("read_size:%d\n",read_size);
	    	for(i = 0; i < FRAME_SIZE; i ++){
	    	frame->data[0][i] = buf[i];
	    	frame->data[1][i] = buf[i];
	    	}

	//        LOGD("frame_number:%d pkt :%d frame:%d got_output:%d\n",avctx->frame_number,pkt,frame,&got_output);
	        /* encode the samples */
        	 LOGD( "Before A encode_audio2");
	        ret = avcodec_encode_audio2(avctx, &pkt, frame, &got_output);
	        LOGD( "After A encode_audio2");
	        if (ret < 0) {
	            LOGD( "Error encoding audio frame ret was :%d\n",ret);
	            exit(1);
	        }
	        if (got_output&&pkt.size > 0) {
		        pkt.size = read_size;
		        pkt.pts = avctx->frame_number*(FRAME_SIZE/2);
		        pkt.pts = av_rescale_q_rnd(pkt.pts, avctx->time_base,millsec,AV_ROUND_NEAR_INF);
	        	codec->write_func(env,avctx,&pkt);
	            av_free_packet(&pkt);
	        }
	    }
	    /* get the delayed frames */
	    for (got_output = 1; got_output; i++) {
       	 LOGD( "Before B encode_audio2");
	        ret = avcodec_encode_audio2(avctx, &pkt, NULL, &got_output);
	       	 LOGD( "After B encode_audio2");
	        if (ret < 0) {
	            LOGD( "Error encoding frame ret was :%d\n",ret);
	            goto END_;
	        }
	        if (got_output&&pkt.size > 0) {
	        pkt.pts = avctx->frame_number*(FRAME_SIZE/2);
	        pkt.pts = av_rescale_q_rnd(pkt.pts, millsec, avctx->time_base,AV_ROUND_NEAR_INF);
        		pkt.size = read_size;
	        	codec->write_func(env,avctx,&pkt);
	            av_free_packet(&pkt);
	        }
	    }

	    END_:

	    if(close(input_file)<0){
	    	LOGD("Failed fd close");
	    }
	    avcodec_close(avctx);
	    av_free(avctx);
	    LOGD("AUDIO_ENC_END\n");
	    audio_pts = __INT_MAX__;
	    return 0;
	return 0;
}
/*
 * Audio encoding example
 */
int audio_encode_example(JNIEnv* env,AVCodec* codec)
{
    AVCodecContext *avctx = audio_summarize_init(codec);
    AVFrame *frame;
    AVPacket pkt;
    int FRAME_SIZE = 1152;//1152
    int i,  ret, got_output;
    int buffer_size;


    /* frame containing input raw audio */
    frame = avcodec_alloc_frame();
    if (!frame) {
    	LOGD("Could not allocate audio frame\n");
        exit(1);
    }
    frame->nb_samples     = avctx->frame_size;
    frame->format         = avctx->sample_fmt;
    frame->channel_layout = avctx->channel_layout;
    /* the codec gives us the frame size, in samples,
     * we calculate the size of the samples buffer in bytes */
    buffer_size = av_samples_get_buffer_size(NULL, avctx->channels, avctx->frame_size,
                                             avctx->sample_fmt, 0);
    /* setup the data pointers in the AVFrame */
    //ここでframe->extended_dataにsamplesがセットされる
    //AVFrameの最初のallocでframe->extended_data = frame->data;しているのでframe->dataにsamplesが入る?
    //buf_sizeとlinesize[0]が2304になる
    //PTSの基準値を求める
//    int64_t pden = avctx->sample_rate;
//    int64_t pnum = (MAX_DEC_SIZE * 8LL) / 16;/*分子のMAX_DEC_SIZEはffmpegのwav_read_packetで使われる1152の刻み?の適当な値、8LLは不明、分母のBPSは、codec_idによって決まる(AV_CODEC_ID_PCM_S16LE)*/
//    LOGD("pnum:%d pden:%d timebase den:%d num:%d round_down:%d\n",pnum ,pden,avctx->time_base.den,avctx->time_base.num,AV_ROUND_DOWN);
//    int64_t duration_value = av_rescale_rnd(1,pnum * (int64_t)avctx->time_base.den, pden * (int64_t)avctx->time_base.num, AV_ROUND_DOWN);
//    LOGD("for PTS duration_value:%d\n",duration_value);

    frame->extended_data = av_mallocz(sizeof(*frame->extended_data));
    if(!frame->extended_data){
    	LOGD("Failed alloc extended_data");
    	return -1;
    }
    frame->data[0] = malloc(FRAME_SIZE*2);
    frame->data[1] = malloc(FRAME_SIZE*2);
    if(!frame->data[0] /*|| !frame->data[1]*/){
    	LOGD("Failed alloc frame->data[]");
    	return -1;
    }
    memset(frame->data[0],0,FRAME_SIZE*2);
    memset(frame->data[1],0,FRAME_SIZE*2);
    frame->linesize[0] = FRAME_SIZE*2;

    pkt = *(AVPacket*)av_malloc(sizeof(AVPacket)+buffer_size);
//    if(!&pkt){
//        	LOGD("Failed alloc pkt");
//        	return -1;
//        }
     av_init_packet(&pkt);

    int input_file = open("/sdcard/testPCM.pcm",O_RDONLY|S_IRUSR|S_IROTH|S_IROTH);
    			if(input_file < 0){
    				LOGD("Failed open file :%d",input_file);
    				return -1;
    			}
    			uint8_t buf[FRAME_SIZE*2];
	            int read_size;
    while(1){
		            read_size = read(input_file, buf, FRAME_SIZE*2);
		            if ( read_size > 0){
//		                for(i = 0; i < 10; i++){
//		                	LOGD("%02X ",buf[i]);
//		                }
		            } else {
 		            	LOGD("Read size was empty: %d ",read_size);
		                goto END_;
		            }

//    	LOGD("read_size:%d\n",read_size);
    	for(i = 0; i < FRAME_SIZE*2; i ++){
    	frame->data[0][i] = buf[i];
    	frame->data[1][i] = buf[i];
    	}

//        LOGD("frame_number:%d pkt :%d frame:%d got_output:%d\n",avctx->frame_number,pkt,frame,&got_output);
        /* encode the samples */
        ret = avcodec_encode_audio2(avctx, &pkt, frame, &got_output);
        if (ret < 0) {
            LOGD( "Error encoding audio frame ret was :%d\n",ret);
            exit(1);
        }
        if (got_output&&pkt.size > 0) {
        	//duration_valueをフィールド化しなきゃいけなくなるからここでやる
        	codec->write_func(env,avctx,&pkt);
            av_free_packet(&pkt);
        }
    }
    /* get the delayed frames */
    for (got_output = 1; got_output; i++) {
        ret = avcodec_encode_audio2(avctx, &pkt, NULL, &got_output);
        if (ret < 0) {
            LOGD( "Error encoding frame ret was :%d\n",ret);
            goto END_;
        }
        if (got_output&&pkt.size > 0) {
        	codec->write_func(env,avctx,&pkt);
            av_free_packet(&pkt);
        }
    }

    END_:

    if(close(input_file)<0){
    	LOGD("Failed fd close");
    }
    avcodec_close(avctx);
    av_free(avctx);
    LOGD("AUDIO_ENC_END\n");
    audio_pts = __INT_MAX__;
    return 0;
}

//--------------------------------------------------------------------

static jobject javaPreviewOrPicture;
static jobject rMic;
static jmethodID sendRtmpMID_A;
static jmethodID sendRtmpMID_V;
static jmethodID testMID;

AVCodecContext *aAvctx;
AVCodecContext *vAvctx;
AVPacket aPkt;
AVFrame *aFrame;
AVPacket* vPkt;
AVFrame *vFrame;
int FRAME_SIZE = 1152;
int offset = 0;
int input_index = 0;
int input_array_size = 0;
short audio_tmp[1152];

int audio_pts;
int video_pts;

int wait_for_sync = 0;
AVPacket* vPkt_2;

//ptsをマイナスにするとcloseされちゃう！？
//video_ptsは毎回普通のvPkt->ptsが入ってくる
int call_java_boss(JNIEnv* env,AVCodecContext* avctx,AVPacket* pkt)
{

	LOGD("call_java_boss_type:%d audio_pts:%d video_pts:%d",avctx->codec_type,audio_pts,video_pts);
   if(avctx->codec_type == AVMEDIA_TYPE_AUDIO){

	   LOGD("Vpkt:%d",vPkt);
	   //次のaudio_ptsが今b保留しているvideoのptsを超える時のみvideoを書き込む
	   LOGD("call_java_boss_type videoOUT size:%d video_pts:%d data:%d",vPkt->size,video_pts,vPkt->data);

		  call_java_mp3(env,avctx, pkt);//-が返ってきたら落とす処理を作るべきか?
   }else{
//	   	    while(wait_for_sync){//audio_pts_eの方が進むまで待機
//	   			   usleep(1000);//1ms
//	   		   }
	   if(audio_pts>=video_pts){
		   call_java_h263(env,avctx, pkt);
	   }else{
		   vAvctx->frame_number--;
//		   if(vPkt_2->data)av_free(vPkt_2->data);
//		   vPkt_2->data

	   }
		   wait_for_sync = 1;
   }
   return 0;
}

int call_java_boss_pic(JNIEnv* env,AVCodecContext* avctx,AVPacket* pkt)
{
	   LOGD("call_java_boss_pic audio_pts:%d video_pts:%d",audio_pts,video_pts);
   if(avctx->codec_type == AVMEDIA_TYPE_AUDIO){
//	   while(audio_pts > video_pts){//video_ptsの方が進むまで待機
//		   usleep(10);// usleepは1/1000ms
//	   }
	      call_java_mp3(env,avctx, pkt);//-が返ってきたら落とす処理を作るべきか?
   }else{
//	    while( audio_pts <= video_pts){//audio_pts_eの方が進むまで待機
//			   usleep(10);//1ms
//		   }
	   video_pts = audio_pts;
	    call_java_encodedbmp(env,avctx, pkt);
	   //10秒?毎しか呼ばない
   }
   return 0;
}

int call_java_mp3(JNIEnv* env,AVCodecContext* avctx,AVPacket* pkt){

	int out_size = pkt->size+1;
	int i,j;
	    LOGD("encodeAudio call_java_mp3 size:%d pts:%d\n", out_size,pkt->pts);
	    //毎回Javaのbyte配列を新たにnewする(送信側で、サイズが合っている配列が必要だから→ここが超えれればほかの方法も取れるけど、結局Java側でArray.copyとかやるのであれば同じなので)
	    		//NewByteArrayでjbyteArrayを生成→一度jbyte*にして、コピーしたらjbyte*だけReleaseByteArrayElementsしてjbyteArrayはJavaのメソッドに送る
	    		jbyteArray data_out = (*env)->NewByteArray(env,out_size);//キーフレームかどうかと前のタグサイズは不要
	    		//これはヘッダーのjbyteArray、データのjbyteArrayの2つを送る必要があるから、データ部分と末尾の4バイト分は不要
	    		jbyte *tmp_d = (*env)->GetByteArrayElements(env,data_out,0);//最後の引数を1にすると落ちる
	    		jbyteArray header_out = (*env)->NewByteArray(env,11);
	    		//これはヘッダーのjbyteArray、データのjbyteArrayの2つを送る必要があるから、データ部分と末尾の4バイト分の配列
				jbyte *tmp_h = (*env)->GetByteArrayElements(env,header_out,0);
				 tmp_h[0] = 0x08;
				 tmp_h[1] = (out_size) >> 16;
				 tmp_h[2] = (out_size) >> 8;
				 tmp_h[3] = out_size;
				 tmp_h[4] = audio_pts >> 16;
				 tmp_h[5] = audio_pts >> 8;
				 tmp_h[6] = audio_pts;
				 tmp_h[7] = 0;
				 tmp_h[8] = 0;
				 tmp_h[9] = 0;
				 tmp_h[10] = 0;

//		         LOGD("call_java_mp3 DATA");
//		         for(i = 0; i < 10; i++){
//		        	 LOGD("%02X ",pkt->data[i]);
//		         }
				    tmp_d[0] = 0x2E;
				    for(i = 0; i < pkt->size; i++){
				    	tmp_d[i+1] = pkt->data[i];
				    }
						 //1152/44100=0.02612244897959183673469387755102//10万回目まで考えるので43分程度は時間がずれない想定
//						 if(avctx->frame_number % 1000 == 0){
//							 audio_pts += 35;//1+2+2+4
//						 }else if(avctx->frame_number % 1000 == 0){
//							 audio_pts += 32;//1+2+2
//						 }else if(avctx->frame_number % 100 == 0){
//							 audio_pts += 30;//1+2
//						 }else if(avctx->frame_number % 10 == 0){
//							 audio_pts += 27;
//						 }else{
//							 audio_pts += 26;
//						 }
				    //ビットレートが32000なのでpktサイズ-11(ヘッダ)*8/3200
//				    LOGD("TEST:%e",((double)out_size-4));
//				    LOGD("TEST:%e",(((double)out_size-4)*8));
//				    LOGD("TEST:%e",((((double)out_size-4)*8)/32000));
//				    LOGD("TEST:%e",(((((double)out_size-4)*8)/32000)*1000));
//				    LOGD("TEST:%e",((double)(((double)out_size-4)*8)/(float)32000)*(double)1000);
				    audio_pts += ((double)((double)((pkt->size-4)*8)/(double)32000)*1000);
						 //※PreviousTagSizeは不要
						 LOGD("Audio_pts:%d",audio_pts);
					     //Java側のコールバックを呼び出すe
						(*env)->CallVoidMethod(env,rMic, sendRtmpMID_A, header_out, data_out,(jint)out_size);

						(*env)->ReleaseByteArrayElements(env,header_out,tmp_h,0);
						(*env)->ReleaseByteArrayElements(env,data_out,tmp_d,0);
				 return 0;

}

int call_java_h263(JNIEnv* env,AVCodecContext* avctx,AVPacket* pkt){
	int i,j=0,out_size = pkt->size;
			LOGD("encodeYUV call_java_h263 size:%d pts:%d\n", out_size,pkt->pts);
			//毎回Javaのbyte配列を新たにnewする(送信側で、サイズが合っている配列が必要だから→ここが超えれればほかの方法も取れるけど、結局Java側でArray.copyとかやるのであれば同じなので)
    		//NewByteArrayでjbyteArrayを生成→一度jbyte*にして、コピーしたらjbyte*だけReleaseByteArrayElementsしてjbyteArrayはJavaのメソッドに送る
    		jbyteArray data_out = (*env)->NewByteArray(env,out_size+1);//キーフレームかどうかと前のタグサイズは不要
    		//これはヘッダーのjbyteArray、データのjbyteArrayの2つを送る必要があるから、データ部分と末尾の4バイト分の配列
    		jbyte *tmp_d = (*env)->GetByteArrayElements(env,data_out,0);//最後の引数を1にすると落ちる
    		jbyteArray header_out = (*env)->NewByteArray(env,11);
    		//これはヘッダーのjbyteArray、データのjbyteArrayの2つを送る必要があるから、データ部分と末尾の4バイト分の配列
			jbyte *tmp_h = (*env)->GetByteArrayElements(env,header_out,0);


    //タグのメタ情報
			         //実データサイズ キーフレーム分1足す?
    tmp_h[0] = 0x09;
    tmp_h[1] = (out_size+1) >> 16;
    tmp_h[2] = (out_size+1) >> 8;
    tmp_h[3] = out_size+1;
			         //PTS
    tmp_h[4] = video_pts >> 16;
    tmp_h[5] = video_pts >> 8;
    tmp_h[6] = video_pts;
    tmp_h[7] = 0;
    tmp_h[8] = 0;
    tmp_h[9] = 0;
    tmp_h[10] = 0;

    tmp_d[0] = (avctx->frame_number-1) % avctx->gop_size == 0? 0x12:0x22;
//			         LOGD("call_java_yuv DATA");
//			         for(i = 0; i < 10; i++){
//			        	 LOGD("%02X ",pkt->data[i]);
//			         }

			         for(i =  1; j < out_size; i++,j++){//mp3じゃなかったら元のjava配列を超えてくることもありえる
			        	 tmp_d[i] = pkt->data[j];
					 }
	     LOGD("CALL_JAVA_METHOD_BEFORE");

	     //Java側のコールバックを呼び出すe
		(*env)->CallVoidMethod(env,javaPreviewOrPicture, sendRtmpMID_V, header_out, data_out,(jint)out_size+1);

		(*env)->ReleaseByteArrayElements(env,header_out,tmp_h,0);
		(*env)->ReleaseByteArrayElements(env,data_out,tmp_d,0);
		     LOGD("Method After");
						return 0;

}

jint Java_nliveroid_nlr_main_BCPlayer_inittest(JNIEnv *env,jobject thiz, jint video_flag,jint audio_flag)
{
	LOGD("TEST --------------- \n");
	return 100;
}

jint Java_com_flazr_rtmp_reader_PictureReader_test(JNIEnv *env,jobject thiz)
{
	LOGD("TEST START-X-------------- \n");
	int test = malloc(99999999);
	LOGD("TEST G--------------- \n");
	startenc(env,2);
	LOGD("TEST X--------------- \n");
	return 100;
}

jint Java_com_flazr_rtmp_reader_PreviewReader_fileTest(JNIEnv *env,jobject thiz)
	{
	LOGD("JNI fileTest Called\n");
	LOGD("TEST FILETEST - \n");
	startenc(env,2);
		return 0;
	}


//Java側のメソッド
 /*
	public void outPut(byte[] test){
		Log.d("NLiveRoid","OUT_PUT ------------ ");
		System.gc();
		Log.d("NLiveRoid","LENGTH ----------- " + test.length);
		for(int i = 0 ; i < 10; i++){
			Log.d("NLiveRoid","OUT : " + test[i]);
		}
	}
 */
//初期化時のテンプレート
jint Java_com_flazr_rtmp_reader_PreviewReader_startTest(JNIEnv *env,jobject thiz)
	{
		LOGD("JNI startTest Called\n");

	jclass clazz = (*env)->GetObjectClass(env,thiz);
	testMID = (*env)->GetMethodID(env,clazz, "outPut", "([B)V");
	javaPreviewOrPicture = (*env)->NewGlobalRef(env,thiz);

		LOGD("JNI startTest END\n");
//	startenc(4);
		return 0;
	}
//エンコード時のテンプレート
jint Java_com_flazr_rtmp_reader_PreviewReader_startTest2(JNIEnv *env,jobject thiz, jbyteArray input)
	{
		LOGD("JNI startTest2 Called\n");
	int i;
	jbyte* input_c = (*env)->GetByteArrayElements(env,input,0);

	for(i = 0; i < 10; i++){
		LOGD("test[%d]:%d",i,input_c[i]);
	}

	jbyteArray jarr = (*env)->NewByteArray(env,12);
	jbyte* j_out = (*env)->GetByteArrayElements(env,jarr,0);

	for(i = 0; i < 12; i++){
		j_out[i] = i+9;
	}

	(*env)->CallVoidMethod(env,javaPreviewOrPicture, testMID,jarr);
	(*env)->ReleaseByteArrayElements(env,input,input_c,0);
	(*env)->ReleaseByteArrayElements(env,jarr,j_out,0);
 		LOGD("JNI startTest END\n");
 		return 0;

	}
int Java_com_flazr_rtmp_reader_PreviewReader_initCamNative(JNIEnv *env,jobject thiz,jint w,jint h,jint fps,jint bit_rate,jint keyframe,jint isusemic)
	{
		LOGD("JNI initCamNative Called\n");

		int i, ret,size,out_size, outbuf_size;
		video_pts = 0;
		AVCodec *codec = (AVCodec*)alloc_flv_codec();
	    vAvctx = (AVCodecContext*)video_summarize_init(codec);
	    avcodec_set_context_defaults3(vAvctx,codec);
	     vAvctx->width = w;
	     vAvctx->height = h;
	     vAvctx->pix_fmt = AV_PIX_FMT_YUV420P;//ここでは420Pだけど、avpicture_fillはNV21で呼ぶ
	     vAvctx->time_base.den = fps;
	     vAvctx->gop_size = keyframe;
	     vAvctx->bit_rate = bit_rate;


	     int x=1; // 0x00000001
	     	if (*(char*)&x) {
	     	        /* little endian. memory image 01 00 00 00 */
	     		LOGD("LITTLE_ENDIAN");
	     	}else{
	     	        /* big endian. memory image 00 00 00 01 */
	     		LOGD("BIG_ENDIAN");
	     	}
	     vFrame = (AVFrame*)avcodec_alloc_frame();//vFrameの初期化

	     /* open it */
	     if (codec_open_test(vAvctx, codec) < 0) {
	         LOGD( "could not open codec\n");
	         return -1;
	     }

	     if(isusemic){
	    	 codec->write_func = call_java_boss;
	     }else{
	    	 codec->write_func = call_java_h263;
	     }
	     LOGD("initCam width: %d height:%d fps:%d bit_rate:%d gop_size:%d",vAvctx->width , vAvctx->height,vAvctx->time_base.den,vAvctx->bit_rate,vAvctx->gop_size);
	     size = vAvctx->width*vAvctx->height;
	     outbuf_size = (sizeof(AVPacket)+size*3/2);//PCでは問題ないのに
	          if(vPkt){
	         	 av_free_packet(vPkt);
	          }
	          vPkt = av_malloc(outbuf_size);
	     	     av_init_packet(vPkt);
	          if(!vPkt){
	         	 LOGD("Failed alloc vPkt");
	         	 return -1;
	          }
	     vFrame->data[0] = av_malloc(size*3/2); /* size for YUV 420 */
	     if(!vFrame->data[0]){
	    	 LOGD("Failed alloc vFrame->data");
	    	 return -1;
	     }
	     vFrame->data[1] = vFrame->data[0] + size;//U
	     vFrame->data[2] = vFrame->data[1] + size / 4;//V
	     if(get_linesizes(vFrame->linesize,vAvctx->width,AV_PIX_FMT_NV21)<0){
	    	 return -3;
	     }
	     LOGD("initCam linesize[0]:%d [1]:%d",vFrame->linesize[0],vFrame->linesize[1]);


	     //メソッドIDのキャッシュ
	 	jclass clazz = (*env)->GetObjectClass(env,thiz);
	 	sendRtmpMID_V = (*env)->GetMethodID(env,clazz, "setGrobalQueue", "([B[BI)V");
	 	javaPreviewOrPicture = (*env)->NewGlobalRef(env,thiz);

		LOGD("JNI initCamNative END\n");

			return 0;
	}

int Java_com_flazr_rtmp_reader_PreviewReader_endCamNative(JNIEnv *env,jobject thiz)
	{
if(vPkt)av_free(vPkt);
if(vAvctx){
	 avcodec_close(vAvctx);
	 av_free(vAvctx);
}
if(vFrame)av_free(vFrame);
LOGD("VIDEO_ENC_END\n");

(*env)->DeleteGlobalRef(env,javaPreviewOrPicture);
javaPreviewOrPicture = NULL;

return 0;
}

jint Java_com_flazr_rtmp_reader_PreviewReader_encodeYUVArray(JNIEnv *env,jobject thiz,jbyteArray input,jboolean portlayt)
	{
		LOGD("JNI encodeYUVArray Called\n");

			/* jbyteArray用のメモリを扱えるようにする */
		jbyte* input_c = (*env)->GetByteArrayElements(env,input,0);

		int ret,got_output,i,array_size = vAvctx->width*vAvctx->height*3/2;

		LOGD("encodeYUVArray A avctx width:%d height:%d vFrame linesize[0]:%d linesize[1]:%d",vAvctx->width,vAvctx->height,vFrame->linesize[0],vFrame->linesize[1]);
		//フレームのデータに読み込ませる
		if(portlayt){
			avpicture_fill(vFrame->data,vFrame->linesize,input_c,AV_PIX_FMT_NV21,vAvctx->width,vAvctx->height,1);
		}else{
		avpicture_fill(vFrame->data,vFrame->linesize,input_c,AV_PIX_FMT_NV21,vAvctx->width,vAvctx->height,1);
		}
		LOGD("vFrame pkt size:%d",vFrame->pkt_size);

//		for(i = 0; i < 10; i++){
//			LOGD("%02X %02X",vFrame->data[0][i],input_c[i]);
//		}
   	 LOGD( "Before A encode_video2");
		ret = avcodec_encode_video2(vAvctx, vPkt, vFrame, &got_output);
				        	 LOGD( "After A encode_video2");
				         if(ret < 0){
				        	 LOGD( "Failed encode_video2_first\n");
				        	 return -1;
				         }else if(got_output && vPkt->size > 0){
				        	 //call_java_boss
				        	 //call_java_h263
				        	 LOGD("Video_real_pts:%d wait_for_sync:%d",(int)vPkt->pts,wait_for_sync);
				        	    video_pts = vPkt->pts;
				        	 vAvctx->codec->write_func(env,vAvctx,vPkt);
				        	 LOGD( "encodeYUV write_func After\n");
				         }else{
				        	 LOGD( "Failed2 encode_video2_first\n");
				        	 return -1;
				         }

				         //flush
				         for (got_output = 1; got_output; i++) {

				        	 LOGD( "Before B encode_video2");
					         ret = avcodec_encode_video2(vAvctx, vPkt, NULL, &got_output);
				        	 LOGD( "After B encode_video2 out_size:%d",vPkt->size);
					         if(ret < 0){//基本的に失敗する?
								 LOGD( "Failed1 encode_video2_second\n");
								 return -1;
					         }else if(got_output && vPkt->size > 0){
					 LOGD("Video_real_pts:%d wait_for_sync:%d",(int)vPkt->pts,wait_for_sync);
					        	    video_pts = vPkt->pts;
					        	 vAvctx->codec->write_func(env,vAvctx, vPkt);
					         }else{
								 LOGD( "Failed2 encode_video2_second\n");
//					        	 return -1;//止まらないようにしておく
					         }
					     }


		(*env)->ReleaseByteArrayElements(env,input,input_c,0);
						LOGD("JNI encodeYUVArray E\n");
			        	 return 0;

	}

jint Java_com_flazr_rtmp_client_RealTimeMic_initMicNative(JNIEnv *env,jobject thiz,jint short_buf,jint mode,jboolean isusecam)
	{
		LOGD("JNI initMicNative Called\n");

		  int i, ret,size,out_size, outbuf_size, buffer_size;
		  input_array_size = short_buf/2;
		    audio_pts = 0;
	     AVCodec *codec = alloc_mp3_codec();
			aAvctx = (AVCodecContext*)audio_summarize_init(codec);
			avcodec_set_context_defaults3(aAvctx,codec);

			     aAvctx->pix_fmt = AV_PIX_FMT_YUV420P;//ここでは420Pだけど、avpicture_fillはNV21で呼ぶ
			     int x=1; // 0x00000001
			     	if (*(char*)&x) {
			     	        /* little endian. memory image 01 00 00 00 */
			     		LOGD("LITTLE_ENDIAN");
			     	}else{
			     	        /* big endian. memory image 00 00 00 01 */
			     		LOGD("BIG_ENDIAN");
			     	}

			     /* open it */
			     if (codec_open_test(aAvctx, codec) < 0) {
			         LOGD( "could not open codec\n");
			         return -1;
			     }

		//		LOGD("JNI initMicNative width:%d height:%d\n",vAvctx->width,vAvctx->height);
			     outbuf_size = sizeof(AVPacket);
			     /* the codec gives us the frame size, in samples,
			      * we calculate the size of the samples buffer in bytes */
			     buffer_size = av_samples_get_buffer_size(NULL, aAvctx->channels, aAvctx->frame_size,
			    		 aAvctx->sample_fmt, 0);
			     aPkt = *(AVPacket*)av_malloc(sizeof(AVPacket)+buffer_size);
		         LOGD( "init_packet Before\n");
		         av_init_packet(&aPkt);
			     if(!&aPkt){
			    	 LOGD("Failed alloc vPkt");
			    	 return -1;
			     }
			     if(mode == 0){
			    	 if(isusecam){
			    		 codec->write_func = call_java_boss;
			    	 }else{
			    		 codec->write_func = call_java_mp3;
			    	 }
			     }else if(mode == 2){
			    	 codec->write_func = call_java_boss_pic;
			     }
			     aFrame = (AVFrame*)avcodec_alloc_frame();//vFrameの初期化
			     aFrame->extended_data = av_mallocz(sizeof(*aFrame->extended_data));
			     if(!aFrame->extended_data){
			     	LOGD("Failed alloc extended_data");
			     	return -1;
			     }
			     aFrame->data[0] = av_malloc(FRAME_SIZE*2);
			     aFrame->data[1] = av_malloc(FRAME_SIZE*2);
			     if(!aFrame->data[0] /*|| !frame->data[1]*/){
			     	LOGD("Failed alloc frame->data[]");
			     	return -1;
			     }
			     memset(aFrame->data[0],0,FRAME_SIZE*2);
			     memset(aFrame->data[1],0,FRAME_SIZE*2);
			     aFrame->linesize[0] = FRAME_SIZE*2;

			     aFrame->nb_samples     = aAvctx->frame_size;
			     aFrame->format         = aAvctx->sample_fmt;
			     aFrame->channel_layout = aAvctx->channel_layout;


		//	     //メソッドIDのキャッシュ

			 	jclass clazz = (*env)->GetObjectClass(env,thiz);
			 	sendRtmpMID_A = (*env)->GetMethodID(env,clazz, "setGrobalQueue", "([B[B)V");
			 	rMic = (*env)->NewGlobalRef(env,thiz);

				LOGD("JNI initMicNative END\n");

			return 0;
	}


int Java_com_flazr_rtmp_client_RealTimeCam_endMicNative(JNIEnv *env,jobject thiz)
	{
	if(&aPkt)av_free(&aPkt);
    if(aAvctx){
    	avcodec_close(aAvctx);
    	av_free(aAvctx);
    }
     if(aFrame)av_free(aFrame);

	     (*env)->DeleteGlobalRef(env,rMic);
	     rMic = NULL;

	     audio_pts = __INT_MAX__;
	     return 0;
	}


jint Java_com_flazr_rtmp_client_RealTimeMic_encodeAudioFrame(JNIEnv *env,jobject thiz,jshortArray input)
	{//1回のフレームで、loop_size分の配列が入ってくる
	//offsetまでは前の半端分が入っている
		LOGD("JNI encodeAudioFrameA offset:%d loop_size:%d\n", offset,input_array_size);
		jshort* input_c = (*env)->GetShortArrayElements(env,input,0);
		int i,j,ret,got_output,ptr = 0,input_index = 0;

		for(i = offset, j =input_index ; i < FRAME_SIZE*2; i+=2,j ++){//最初のループはoffsetから残りを消化する
			aFrame->data[0][i] = input_c[j] & 0xFF;
			aFrame->data[0][i+1] = (input_c[j]>>8) & 0xFF;
			aFrame->data[1][i] = input_c[j] & 0xFF;
			aFrame->data[1][i+1] = (input_c[j]>>8) & 0xFF;
		}
		input_index += FRAME_SIZE-offset;
		offset = 0;
		ret = avcodec_encode_audio2(aAvctx, &aPkt, aFrame, &got_output);
//					if (ret < 0) {
//						LOGD( "Error encoding audio frame ret was :%d\n",ret);
//						return -1;
//					}
					LOGD("JNI encodeAudioFrame got_output:%d aPkt.size:%d\n",got_output,aPkt.size);
					if (got_output&&aPkt.size > 0) {
							aAvctx->codec->write_func(env,aAvctx,&aPkt);
						av_free_packet(&aPkt);
					}

		//lengthは毎回7680
		while(1){
			LOGD("JNI encodeAudioFrame BREAK offset:%d loop_size:%d\n", offset,input_array_size);
			if(input_index + FRAME_SIZE > input_array_size){//encodedがフレームサイズを下回った場合、次の配列に回す
				offset = input_array_size - input_index;
				for(i = 0,j=input_index; i < offset;i+=2,j ++){
					aFrame->data[0][i] = input_c[j] & 0xFF;
					aFrame->data[0][i+1] = (input_c[j]>>8) & 0xFF;
					aFrame->data[1][i] = input_c[j] & 0xFF;
					aFrame->data[1][i+1] = (input_c[j]>>8) & 0xFF;
				}
				break;
			}

			for(i = 0, j =input_index ; i < FRAME_SIZE*2; i+=2,j ++){//通常のループ
				aFrame->data[0][i] = input_c[j] & 0xFF;
				aFrame->data[0][i+1] = (input_c[j]>>8) & 0xFF;
				aFrame->data[1][i] = input_c[j] & 0xFF;
				aFrame->data[1][i+1] = (input_c[j]>>8) & 0xFF;
			}
			input_index += FRAME_SIZE;
			ret = avcodec_encode_audio2(aAvctx, &aPkt, aFrame, &got_output);
//			if (ret < 0) {
//				LOGD( "Error encoding audio frame ret was :%d\n",ret);
//				return -1;
//			}
			LOGD("JNI encodeAudioFrame got_output:%d aPkt.size:%d\n",got_output,aPkt.size);
			if (got_output&&aPkt.size > 0) {
							aAvctx->codec->write_func(env,aAvctx,&aPkt);
				av_free_packet(&aPkt);
			}
		}

(*env)->ReleaseShortArrayElements(env,input,input_c,0);
    LOGD("AUDIO_ENC_END\n");
			return 0;
	}


jint Java_com_flazr_rtmp_client_RealTimeMic_setVolume(JNIEnv *env,jobject thiz,jint vol)
	{
		LOGD("JNI setVolume Called\n");
			return 0;
	}


//以下画像配信


int call_java_bmponly(JNIEnv* env,AVCodecContext* avctx,AVPacket* pkt){
	 video_pts += vAvctx->frame_number * 10000;
	call_java_encodedbmp(env,avctx, pkt);
	return 0;
}

int call_java_encodedbmp(JNIEnv* env,AVCodecContext* avctx,AVPacket* pkt){
	int i,j=0,out_size = pkt->size+1;
	    LOGD("encodedbmp call_java_encodedbmp size:%d pts:%d\n", out_size,pkt->pts);
	    //毎回Javaのbyte配列を新たにnewする(送信側で、サイズが合っている配列が必要だから→ここが超えれればほかの方法も取れるけど、結局Java側でArray.copyとかやるのであれば同じなので)
	    		//NewByteArrayでjbyteArrayを生成→一度jbyte*にして、コピーしたらjbyte*だけReleaseByteArrayElementsしてjbyteArrayはJavaのメソッドに送る
	    		jbyteArray data_out_v = (*env)->NewByteArray(env,out_size);//キーフレームかどうかと静止画の場合は全部1つの配列に含んじゃう
	    		 LOGD("call_java_yuv A");
	    		//これはヘッダーのjbyteArray、データのjbyteArrayの2つを送る必要があるから、データ部分と末尾の4バイト分の配列
	    		jbyte* tmp_d_v = (*env)->GetByteArrayElements(env,data_out_v,0);//最後の引数を1にすると落ちる
	    		 LOGD("call_java_yuv B");
				jbyteArray header_out_v = (*env)->NewByteArray(env,11);
				 LOGD("call_java_yuv C");
				//これはヘッダーのjbyteArray、データのjbyteArrayの2つを送る必要があるから、データ部分と末尾の4バイト分の配列
			 	jbyte* tmp_h_v = (*env)->GetByteArrayElements(env,header_out_v,0);
				 LOGD("call_java_yuv D");
	    //タグのメタ情報
				         //実データサイズ キーフレーム分1足す?
				 tmp_h_v[0] = 0x09;
				 tmp_h_v[1] = (out_size) >> 16;
				 tmp_h_v[2] = (out_size) >> 8;
				 tmp_h_v[3] = out_size;
//ここが呼ばれる前にptsをセットするのでptsは換算しなくていい
//				 video_pts += vAvctx->frame_number * 10000;

				 tmp_h_v[4] = video_pts >> 16;
				 tmp_h_v[5] = video_pts >> 8;
				 tmp_h_v[6] = video_pts;
	    		 tmp_h_v[7] = 0;
	    		 tmp_h_v[8] = 0;
	    		 tmp_h_v[9] = 0;
	    		 tmp_h_v[10] = 0;


	    		 tmp_d_v[0] = (avctx->frame_number-1) % avctx->gop_size == 0? 0x12:0x22;
				 LOGD("call_java_encodedbmp DATA");
				 for(i = 0; i < 10; i++){
					 LOGD("%02X ",pkt->data[i]);
				 }

				 for(i =  1; j < out_size; i++,j++){//mp3じゃなかったら元のjava配列を超えてくることもありえる
					 tmp_d_v[i] = pkt->data[j];
				 }

		     LOGD("CALL_JAVA_METHOD_BEFORE");

		     //Java側のコールバックを呼び出す
			(*env)->CallVoidMethod(env,javaPreviewOrPicture, sendRtmpMID_V,  header_out_v,data_out_v);

			(*env)->ReleaseByteArrayElements(env,header_out_v,tmp_h_v,0);
			(*env)->ReleaseByteArrayElements(env,data_out_v,tmp_d_v,0);
			LOGD("Method After");
	return 0;
}

int Java_com_flazr_rtmp_reader_PictureReader_initBmpNative(JNIEnv *env,jobject thiz,jint w,jint h,jint useMic)
	{

	LOGD("JNI initBmp Called useMic:%d\n",useMic);
	int i, ret,size,out_size, outbuf_size;
	video_pts = 0;

	AVCodec *codec = (AVCodec*)alloc_flv_codec();
    vAvctx = (AVCodecContext*)video_summarize_init(codec);
    avcodec_set_context_defaults3(vAvctx,codec);

     vAvctx->width = w;
     vAvctx->height = h;
     vAvctx->pix_fmt = AV_PIX_FMT_YUV420P;//ここでは420Pだけど、avpicture_fillはNV21で呼ぶ
     vAvctx->time_base.den = 2;//時間的にはどんどんずれていくことになる
     vAvctx->gop_size = 1;

     vFrame = (AVFrame*)avcodec_alloc_frame();//vFrameの初期化

     /* open it */
     if (codec_open_test(vAvctx, codec) < 0) {
         LOGD( "could not open codec\n");
         return -2;
     }

     LOGD("initBmpNative width: %d height:%d fps:%d bit_rate:%d gop_size:%d",vAvctx->width , vAvctx->height,vAvctx->time_base.den,vAvctx->bit_rate,vAvctx->gop_size);
     if(useMic){
    	 codec->write_func = call_java_boss_pic;
     }else{
    	 codec->write_func = call_java_bmponly;
     }
     size = vAvctx->width*vAvctx->height;
     outbuf_size = (sizeof(AVPacket)+size*3/2);//PCでは問題ないのに
     //vPktを解放したいとこだが落ちる
     //     if(vPkt){
     //    	 av_free_packet(vPkt);
     //     }
     vPkt = av_malloc(outbuf_size);
	     av_init_packet(vPkt);
     if(!vPkt){
    	 LOGD("Failed alloc vPkt");
    	 return -2;
     }
     vFrame->data[0] = av_malloc(size*3/2); /* size for YUV 420 */
     if(!vFrame->data[0]){
    	 LOGD("Failed alloc vFrame->data");
    	 return -1;
     }
     vFrame->data[1] = vFrame->data[0] + size;//U
     vFrame->data[2] = vFrame->data[1] + size / 4;//V
     if(get_linesizes(vFrame->linesize,vAvctx->width,AV_PIX_FMT_NV21)<0){
    	 return -3;
     }
     LOGD("initBmp linesize[0]:%d [1]:%d",vFrame->linesize[0],vFrame->linesize[1]);


     //メソッドIDのキャッシュ
 	jclass clazz = (*env)->GetObjectClass(env,thiz);
 	sendRtmpMID_V = (*env)->GetMethodID(env,clazz, "setEncodedBitmap", "([B[B)V");
 	javaPreviewOrPicture = (*env)->NewGlobalRef(env,thiz);

	LOGD("JNI initBmp END\n");

	return 0;
	}

int Java_com_flazr_rtmp_reader_PictureReader_endBmp(JNIEnv *env,jobject thiz)
	{
	LOGD("endBmp vPkt:%d vAvctx:%d vFrame:%d javaPreviewOrPicture:%d",vPkt,vAvctx,vFrame,javaPreviewOrPicture);
	//落ちる
	//		if(vPkt)av_free(vPkt);
		LOGD("VIDEO_ENC_END A\n");
			if(vAvctx){
				 avcodec_close(vAvctx);
				 //落ちる
//				 av_free(vAvctx);
			}
			LOGD("VIDEO_ENC_END B\n");
			if(vFrame)av_free(vFrame);
			LOGD("VIDEO_ENC_END C\n");
			video_pts = __INT_MAX__;
			LOGD("VIDEO_ENC_END\n");
			return 0;
	return 0;
	}



int Java_com_flazr_rtmp_reader_PictureReader_encodeBmp(JNIEnv *env,jobject thiz,jintArray input)
	{

	LOGD("JNI encodeBmp Called\n");

	int ret,got_output,i,j,index,uvIndex,red,green,blue,size = vAvctx->width*vAvctx->height;

		/* jbyteArray用のメモリを扱えるようにする */
	jint* rgb_pixels = (*env)->GetIntArrayElements(env,input,0);

	//RGB to YUV
	LOGD("encodeBmp w:%d h:%d",vAvctx->width,vAvctx->height );


		 for(i=0;i<size;i++) {//Y
			red = (rgb_pixels[i] & 0x00FF0000) >> 16;
			green = (rgb_pixels[i] & 0x0000FF00) >> 8;
			blue = (rgb_pixels[i] & 0x000000FF);
			vFrame->data[0][i] =  (0.257 * red) + (0.504 * green) + (0.098 * blue) + 16;
		}

		 for(i = 0,index = 0,uvIndex = 0; i < vAvctx->height; i++){
			 for(j=0;j<vAvctx->width;j++,index++) {//CbCr
					red = (rgb_pixels[index] & 0x00FF0000) >> 16;
					green = (rgb_pixels[index] & 0x0000FF00) >> 8;
					blue = (rgb_pixels[index] & 0x000000FF);
				 if(i % 2 == 0 && j % 2 == 0){
					 vFrame->data[1][uvIndex++] =  (0.439 * red) - (0.368 * green) - (0.071 * blue) + 128;
					 vFrame->data[1][uvIndex++] =  (-0.148 * red) - (0.291 * green) + (0.439 * blue) + 128;
				}
			 }
		 }


	LOGD("encodeBmp A avctx width:%d height:%d vFrame linesize[0]:%d linesize[1]:%d",vAvctx->width,vAvctx->height,vFrame->linesize[0],vFrame->linesize[1]);
	//フレームのデータに読み込ませる
	LOGD("encodeBmp vFrame pkt size:%d",vFrame->pkt_size);

	for(i = 0; i < 10; i++){
		LOGD("%02X %02X",vFrame->data[0][i],rgb_pixels[i]);
	}

	LOGD("JNI encodeBmp vPkt->size:%d\n",vPkt->size);
	 LOGD( "Before A encode_video2");
	ret = avcodec_encode_video2(vAvctx, vPkt, vFrame, &got_output);
			        	 LOGD( "After A encode_video2");
			         if(ret < 0){
			        	 LOGD( "Failed encode_video2_first --------------------- \n");
			        	 return -1;
			         }else if(got_output && vPkt->size > 0){
			        	 //call_java_encodedbmp
			        	 //call_java_boss_pic
			        	 vAvctx->codec->write_func(env,vAvctx,vPkt);
			        	 LOGD( "encodeBmp write_func After\n");
			         }else{
			        	 LOGD( "Failed2 encode_video2_first\n");
			        	 return -1;
			         }

			         for (got_output = 1; got_output; i++) {

			        	 LOGD( "Before B encode_video2");
				         ret = avcodec_encode_video2(vAvctx, vPkt, NULL, &got_output);
			        	 LOGD( "After B encode_video2 out_size:%d",vPkt->size);
				         if(ret < 0){
							 LOGD( "Failed1 encode_video2_second A\n");
							 return -1;
				         }else if(got_output && vPkt->size > 0){
				        	 vAvctx->codec->write_func(env,vAvctx, vPkt);
				         }else{//サイズが0でも失敗じゃないとした
							 LOGD( "Failed2 encode_video2_second B\n");
				         }
				     }
						(*env)->ReleaseIntArrayElements(env,input,rgb_pixels,0);
					LOGD("JNI encodeBmp END\n");
		        	 return 0;
	}



int Java_com_flazr_rtmp_reader_PictureReader_repeatBmp(JNIEnv *env,jobject thiz,jbyteArray input,jint input_size)
	{

	LOGD("JNI repeatBmp Called\n");
//元からあるデータを使ってPTSだけ付けて送る関数

	int ret,got_output,i,j,array_size = vAvctx->width*vAvctx->height*3/2,red,green,blue;

	/* jbyteArray用のメモリを扱えるようにする */
	jbyte* input_repeat = (*env)->GetByteArrayElements(env,input,0);


	LOGD("repeatBmp A avctx width:%d height:%d vFrame linesize[0]:%d linesize[1]:%d",vAvctx->width,vAvctx->height,vFrame->linesize[0],vFrame->linesize[1]);
	//フレームのデータに読み込ませる
//	avpicture_fill(vFrame->data,vFrame->linesize,input_c,AV_PIX_FMT_NV21,vAvctx->width,vAvctx->height,1);
	LOGD("repeatBmp vFrame pkt data:%d size:%d",vPkt->data,vPkt->size);

	//ここでvPkt->dataがNULLの場合がある→initされた?原因未調査だが。。
	if(!vPkt->data){
		vPkt->data = av_malloc(input_size+1);
	}
	vPkt->size = input_size;//最初のキーフレームを端折ってそのまま突っ込む
	for(i = 0,j = 1; i < input_size; i++,j++){
		vPkt->data[i] = input_repeat[j];
	}
	for(i = 0; i < 10; i++){
		LOGD("%02X %02X",vPkt->data[i],input_repeat[i]);
	}

	vAvctx->codec->write_func(env,vAvctx,vPkt);//call_java_encodedbmp call_java_boss_pic

	(*env)->ReleaseByteArrayElements(env,input,input_repeat,0);
					LOGD("JNI repeatBmp END\n");

			return 0;
	}

//decode_video2を呼ばないといけない+RGB24でフレームを初期化しなきゃいけないようなので中止
//void fill_bitmap(AndroidBitmapInfo*  info, void *pixels, AVFrame *frame)
//{
//avpicture_fill((AVPicture *)frame, buffer, PIX_FMT_RGB24,
//    		 vAvctx->width, vAvctx->height);
//    uint8_t *frameLine;
//    int  yy;
//    for (yy = 0; yy < info->height; yy++) {
//        uint8_t*  line = (uint8_t*)pixels;
//        frameLine = (uint8_t *)frame->data[0] + (yy * frame->linesize[0]);
//        int xx;
//        for (xx = 0; xx < info->width; xx++) {
//            int out_offset = xx * 4;
//            int in_offset = xx * 3;
//            line[out_offset] = frameLine[in_offset];
//            line[out_offset+1] = frameLine[in_offset+1];
//            line[out_offset+2] = frameLine[in_offset+2];
//            line[out_offset+3] = 0;
//        }
//        pixels = (char*)pixels + info->stride;
//    }
//}
//int drawFrame(JNIEnv *env,jint bitmap,int *pixels, AVFrame *frame){
//	int ret = 0;
//	AndroidBitmapInfo  info;
//    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
//        LOGD("AndroidBitmap_getInfo() failed ! error=%d", ret);
//        return -1;
//    }
//
//    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
//        LOGD("AndroidBitmap_lockPixels() failed ! error=%d", ret);
//        return -1;
//    }
//    fill_bitmap(&info, pixels, frame);
//
//    AndroidBitmap_unlockPixels(env, bitmap);
//    return 0;
//}


jint Java_nliveroid_nlr_main_TopTabs_startTest(JNIEnv *env,jobject thiz)
	{
	LOGD("JNI initVideo Called\n");


	startenc(env,2);
//	test_mono();

		return 0;
	}


jint Java_com_flazr_rtmp_reader_startTest(JNIEnv *env,jobject thiz)
	{
	LOGD("JNI initVideo Called\n");

	startenc(env,2);
//	test_mono();

		return 0;
	}
