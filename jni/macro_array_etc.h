/*
 * struct_pass_compile.h
 *
 *  Created on: 2013/11/13
 *      Author: Owner
 */

#include <stdint.h>
#include <stddef.h>
#include <stdio.h>
#include <stdint.h>




struct AVRational;
struct MpegEncContext;


#define HAVE_BIGENDIAN 0

//#define NULL 0

#   define NULL_IF_CONFIG_SMALL(x) x

#define AV_CH_FRONT_LEFT             0x00000001
#define AV_CH_FRONT_RIGHT            0x00000002
#define AV_CH_FRONT_CENTER           0x00000004
#define AV_CH_LAYOUT_MONO              (AV_CH_FRONT_CENTER)
#define AV_CH_LAYOUT_STEREO            (AV_CH_FRONT_LEFT|AV_CH_FRONT_RIGHT)

/* number of subbands */
#define SBLIMIT       32

#define CODEC_CAP_SMALL_LAST_FRAME 0x0040
/**
 * Audio encoder supports receiving a different number of samples in each call.
 */
#define CODEC_CAP_VARIABLE_FRAME_SIZE 0x10000

/**
 *  FF_ALLOCZ_OR_GOTO(s->avctx, s->dct_offset, 2 * 64 * sizeof(uint16_t), fail);
 */

#undef DBL_MAX
#define DBL_MAX	((double)1.79769313486231570815e+308L)
#undef __U64
# define __U64(n) n ## ULL
#undef __I64
# define __I64(n) n ## LL
#undef INT64_MIN
#undef INT64_MAX
#undef UINT64_MAX
#undef INT_MAX
#undef INT_MIN
#define INT_MAX 2147483647
#define INT_MIN INT_MAX-1
#define INT64_MAX (__I64(9223372036854775807))
#define INT64_MIN (-__I64(9223372036854775807) - 1)
#define UINT64_MAX (__U64(18446744073709551615))
#define SIZE_MAX (4294967295U)

#define _PARAMS(paramlist)		paramlist
extern double acos _PARAMS((double));
extern double asin _PARAMS((double));
extern double atan2 _PARAMS((double, double));
extern double cosh _PARAMS((double));
extern double sinh _PARAMS((double));
extern double exp _PARAMS((double));
extern double ldexp _PARAMS((double, int));
extern double log _PARAMS((double));
extern double log10 _PARAMS((double));
extern double pow _PARAMS((double, double));
extern double sqrt _PARAMS((double));
extern double fmod _PARAMS((double, double));
extern double atan _PARAMS((double));
extern double cos _PARAMS((double));
extern double sin _PARAMS((double));
extern double tan _PARAMS((double));
extern double tanh _PARAMS((double));
extern double frexp _PARAMS((double, int *));
extern double modf _PARAMS((double, double *));
extern double ceil _PARAMS((double));
extern double fabs _PARAMS((double));
extern double floor _PARAMS((double));


#define FF_ALLOCZ_OR_GOTO(ctx, p, size, label)\
{\
    p = av_mallocz(size);\
    if (p == NULL && (size) != 0) {\
        LOGD( "Cannot allocate memory.\n");\
        goto label;\
    }\
}


#define FF_ALLOC_OR_GOTO(ctx, p, size, label)\
{\
    p = av_malloc(size);\
    if (p == NULL && (size) != 0) {\
        LOGD("Cannot allocate memory.\n");\
        goto label;\
    }\
}

#define FFMAX(a,b) ((a) > (b) ? (a) : (b))
#define FFMIN(a,b) ((a) > (b) ? (b) : (a))
#define FFMAX3(a,b,c) FFMAX(FFMAX(a,b),c)




#define AVCOL_SPC_YCGCO AVCOL_SPC_YCOCG



#define CodecID AVCodecID


#define MAX_THREADS 2
//#define MAX_THREADS 32


#define MAX_PICTURE_COUNT 36//36じゃないとダメだった
//#define MAX_PICTURE_COUNT 10
#define MAX_FCODE 7



#define A1 0.70710678118654752438 // cos(pi*4/16)
#define A2 0.54119610014619698435 // cos(pi*6/16)sqrt(2)
#define A5 0.38268343236508977170 // cos(pi*6/16)
#define A4 1.30656296487637652774 // cos(pi*2/16)sqrt(2)
#define A5 0.38268343236508977170

#define B0 1.00000000000000000000
#define B1 0.72095982200694791383 // (cos(pi*1/16)sqrt(2))^-1
#define B2 0.76536686473017954350 // (cos(pi*2/16)sqrt(2))^-1
#define B3 0.85043009476725644878 // (cos(pi*3/16)sqrt(2))^-1
#define B4 1.00000000000000000000 // (cos(pi*4/16)sqrt(2))^-1
#define B5 1.27275858057283393842 // (cos(pi*5/16)sqrt(2))^-1
#define B6 1.84775906502257351242 // (cos(pi*6/16)sqrt(2))^-1
#define B7 3.62450978541155137218 // (cos(pi*7/16)sqrt(2))^-1




static const float postscale[64]={
B0*B0, B0*B1, B0*B2, B0*B3, B0*B4, B0*B5, B0*B6, B0*B7,
B1*B0, B1*B1, B1*B2, B1*B3, B1*B4, B1*B5, B1*B6, B1*B7,
B2*B0, B2*B1, B2*B2, B2*B3, B2*B4, B2*B5, B2*B6, B2*B7,
B3*B0, B3*B1, B3*B2, B3*B3, B3*B4, B3*B5, B3*B6, B3*B7,
B4*B0, B4*B1, B4*B2, B4*B3, B4*B4, B4*B5, B4*B6, B4*B7,
B5*B0, B5*B1, B5*B2, B5*B3, B5*B4, B5*B5, B5*B6, B5*B7,
B6*B0, B6*B1, B6*B2, B6*B3, B6*B4, B6*B5, B6*B6, B6*B7,
B7*B0, B7*B1, B7*B2, B7*B3, B7*B4, B7*B5, B7*B6, B7*B7,
};


//#define _PARAMS(paramlist)	paramlist
//extern double atan _PARAMS((double));
//extern double cos _PARAMS((double));
//extern double sin _PARAMS((double));
//extern double tan _PARAMS((double));
//extern double tanh _PARAMS((double));
//extern double frexp _PARAMS((double, int *));
//extern double modf _PARAMS((double, double *));
//extern double ceil _PARAMS((double));
//extern double fabs _PARAMS((double));
//extern double floor _PARAMS((double));
//extern double acos _PARAMS((double));
//extern double asin _PARAMS((double));
//extern double atan2 _PARAMS((double, double));
//extern double cosh _PARAMS((double));
//extern double sinh _PARAMS((double));
//extern double exp _PARAMS((double));
//extern double ldexp _PARAMS((double, int));
//extern double log _PARAMS((double));
//extern double log10 _PARAMS((double));
//extern double pow _PARAMS((double, double));
//extern double sqrt _PARAMS((double));
//extern double fmod _PARAMS((double, double));


# ifndef NAN
#  define NAN (__builtin_nanf(""))
# endif


#define CONFIG_FTRAPV 0
#  define INFINITY (__builtin_inff())
#define FF_LAMBDA_MAX (256*128-1)

#define IS_IDENTIFIER_CHAR(c) ((c) - '0' <= 9U || (c) - 'a' <= 25U || (c) - 'A' <= 25U || (c) == '_')

#define FF_ARRAY_ELEMS(a) (sizeof(a) / sizeof((a)[0]))

#define M_E		2.7182818284590452354
#define M_PI		3.14159265358979323846
#ifndef M_PHI
#define M_PHI          1.61803398874989484820   /* phi / golden ratio */
#endif


static const struct {
    const char *name;
    double value;
} constants[] = {
    { "E",   M_E   },
    { "PI",  M_PI  },
    { "PHI", M_PHI },
};

static const uint8_t ff_default_chroma_qscale_table[32] = {
//   0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
     0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
};

//for encode.c
#define MAX_MB_BYTES (30*16*16*3/8 + 120)
#define CONFIG_MJPEG_ENCODER 1
#define CODEC_FLAG_PASS1           0x0200   ///< Use internal 2pass ratecontrol in first pass mode.
#define AV_PKT_FLAG_KEY     0x0001 ///< The packet contains a keyframe
#define INPLACE_OFFSET 16
#define CODEC_FLAG_INPUT_PRESERVED 0x0100
#define CODEC_FLAG_EMU_EDGE        0x4000   ///< Don't draw edges.

/// Max number of frame buffers that can be allocated when using frame threads.
#define MAX_BUFFERS (34+1)

/**
 * Value of Picture.reference when Picture is not a reference picture, but
 * is held for delayed output.
 */
#define DELAYED_PIC_REF 4

#define PIX_FMT_PAL       2 ///< Pixel format has a palette in data[1], values are indexes in this palette.
#define PIX_FMT_HWACCEL   8 ///< Pixel format is an HW accelerated format.
#define PIX_FMT_BITSTREAM 4 ///< All values of a component are bit-wise packed end to end.
#define FFALIGN(x, a) (((x)+(a)-1)&~((a)-1))
#define HAVE_MMX 1
#define PIX_FMT_PSEUDOPAL 64
#if (ARCH_ARM && HAVE_NEON) || ARCH_PPC || HAVE_MMX
#   define STRIDE_ALIGN 16
#else
#   define STRIDE_ALIGN 8
#endif

#define FF_MAX_B_FRAMES 16
#define CODEC_FLAG_PSNR            0x8000   ///< error[?] variables will be set during encoding.

#    define av_used __attribute__((used))
#define DECLARE_ASM_CONST(n,t,v)    static const t av_used __attribute__ ((aligned (n))) v
//for codec_base

/**
 * Codec is experimental and is thus avoided in favor of non experimental
 * encoders
 */
#define CODEC_CAP_EXPERIMENTAL     0x0200
#define AV_OPT_FLAG_AUDIO_PARAM     8
#define AV_OPT_FLAG_VIDEO_PARAM     16
#define AV_OPT_FLAG_SUBTITLE_PARAM  32
#define FF_BUFFER_HINTS_READABLE 0x02 // Codec will read from buffer.

#define FF_SANE_NB_CHANNELS 128U
#define AVERROR_EXPERIMENTAL       (-0x2bb2afa8) ///< Requested feature is flagged experimental. Set strict_std_compliance if you really want to use it.
/**
 * Codec supports avctx->thread_count == 0 (auto).
 */
#define CODEC_CAP_AUTO_THREADS     0x8000
/**
 * Subtitle codec is bitmap based
 */
#define AV_CODEC_PROP_BITMAP_SUB    (1 << 16)
/**
 * Maximum size in bytes of extradata.
 * This value was chosen such that every bit of the buffer is
 * addressable by a 32-bit signed integer as used by get_bits.
 */
#define FF_MAX_EXTRADATA_SIZE ((1 << 28) - FF_INPUT_BUFFER_PADDING_SIZE)

/**
 * @ingroup lavc_decoding
 * Required number of additionally allocated bytes at the end of the input bitstream for decoding.
 * This is mainly needed because some optimized bitstream readers read
 * 32 or 64 bit at once and could read over the end.<br>
 * Note: If the first 23 bits of the additional bytes are not 0, then damaged
 * MPEG bitstreams could cause overread and segfault.
 */
#define FF_INPUT_BUFFER_PADDING_SIZE 16

/**
 * Lock operation used by lockmgr
 */
enum AVLockOp {
  AV_LOCK_CREATE,  ///< Create a mutex
  AV_LOCK_OBTAIN,  ///< Lock the mutex
  AV_LOCK_RELEASE, ///< Unlock the mutex
  AV_LOCK_DESTROY, ///< Free mutex resources
};

/**
 * Codec is intra only.
 */
#define CODEC_CAP_INTRA_ONLY       0x40000000



#define FFERRTAG(a, b, c, d) (-(int)MKTAG(a, b, c, d))

//#define av_assert1(cond) ((void)0)

#define CONFIG_MPEG_XVMC_DECODER 0
/* Unsupported options :
 *              Syntax Arithmetic coding (SAC)
 *              Reference Picture Selection
 *              Independent Segment Decoding */
/* /Fx */
/* codec capabilities */

#define CODEC_CAP_DRAW_HORIZ_BAND 0x0001 ///< Decoder can use draw_horiz_band callback.

/**
 * Codec can export data for HW decoding (VDPAU).
 */
#define CODEC_CAP_HWACCEL_VDPAU    0x0080
#define CONFIG_H261_ENCODER 1
#define CONFIG_WMV2_ENCODER 1
#define CONFIG_MPEG4_ENCODER 1
#define CONFIG_RV10_ENCODER 1
#define CONFIG_RV20_ENCODER 1
#define CONFIG_FLV_ENCODER 1
#define CONFIG_H263_ENCODER 1
#define CONFIG_MPEG1VIDEO_ENCODER 1
#define CONFIG_MPEG2VIDEO_ENCODER 1
#define CONFIG_MSMPEG4V2_ENCODER 1
#define CONFIG_MSMPEG4V3_ENCODER 1
#define CONFIG_MSMPEG4_ENCODER (CONFIG_MSMPEG4V2_ENCODER || \
        CONFIG_MSMPEG4V3_ENCODER || \
        CONFIG_WMV2_ENCODER)


#define FF_LAMBDA_SHIFT 7


// Some broken preprocessors need a second expansion
// to be forced to tokenize __VA_ARGS__
#define E1(x) x
#define DECLARE_ALIGNED(n,t,v)      t __attribute__ ((aligned (n))) v


#define LOCAL_ALIGNED_A(a, t, v, s, o, ...)             \
    uint8_t la_##v[sizeof(t s o) + (a)];                \
    t (*v) o = (void *)FFALIGN((uintptr_t)la_##v, a)

#define LOCAL_ALIGNED_D(a, t, v, s, o, ...)             \
    DECLARE_ALIGNED(a, t, la_##v) s o;                  \
    t (*v) o = la_##v

#define LOCAL_ALIGNED(a, t, v, ...) E1(LOCAL_ALIGNED_A(a, t, v, __VA_ARGS__,,))

#   define LOCAL_ALIGNED_16(t, v, ...) E1(LOCAL_ALIGNED_D(16, t, v, __VA_ARGS__,,))
#   define LOCAL_ALIGNED_8(t, v, ...) E1(LOCAL_ALIGNED_D(8, t, v, __VA_ARGS__,,))

//----------------bswap

#   define av_alias __attribute__((may_alias))
union unaligned_64 { uint64_t l; } __attribute__((packed)) av_alias;
union unaligned_32 { uint32_t l; } __attribute__((packed)) av_alias;
union unaligned_16 { uint16_t l; } __attribute__((packed)) av_alias;

#   define BSWAP32_RN(p) (((const union unaligned_32 *) (p))->l)



#define AV_BSWAP16C(x) (((x) << 8 & 0xff00)  | ((x) >> 8 & 0x00ff))
#define AV_BSWAP32C(x) (AV_BSWAP16C(x) << 16 | AV_BSWAP16C((x) >> 16))
#define AV_BSWAP64C(x) (AV_BSWAP32C(x) << 32 | AV_BSWAP32C((x) >> 32))

#define AV_BSWAPC(s, x) AV_BSWAP##s##C(x)

#ifndef av_bswap16
static inline const uint16_t av_bswap16(uint16_t x)
{
    x= (x>>8) | (x<<8);
    return x;
}
#endif


#ifndef av_bswap32
static inline const uint32_t av_bswap32(uint32_t x)
{
    return AV_BSWAP32C(x);
}
#endif

#ifndef av_bswap64
static inline const uint32_t av_bswap64(uint64_t x)
{
    return (uint64_t)av_bswap32(x) << 32 | av_bswap32(x >> 32);
}
#endif

// be2ne ... big-endian to native-endian
// le2ne ... little-endian to native-endian

#if AV_HAVE_BIGENDIAN
#define av_be2ne16(x) (x)
#define av_be2ne32(x) (x)
#define av_be2ne64(x) (x)
#define av_le2ne16(x) av_bswap16(x)
#define av_le2ne32(x) av_bswap32(x)
#define av_le2ne64(x) av_bswap64(x)
#define AV_BE2NEC(s, x) (x)
#define AV_LE2NEC(s, x) AV_BSWAPC(s, x)
#else
#define av_be2ne16(x) av_bswap16(x)
#define av_be2ne32(x) av_bswap32(x)
#define av_be2ne64(x) av_bswap64(x)
#define av_le2ne16(x) (x)
#define av_le2ne32(x) (x)
#define av_le2ne64(x) (x)
#define AV_BE2NEC(s, x) AV_BSWAPC(s, x)
#define AV_LE2NEC(s, x) (x)
#endif

#define AV_BE2NE16C(x) AV_BE2NEC(16, x)
#define AV_BE2NE32C(x) AV_BE2NEC(32, x)
#define AV_BE2NE64C(x) AV_BE2NEC(64, x)
#define AV_LE2NE16C(x) AV_LE2NEC(16, x)
#define AV_LE2NE32C(x) AV_LE2NEC(32, x)
#define AV_LE2NE64C(x) AV_LE2NEC(64, x)






#define AV_RNA(s, p)    (((const av_alias##s*)(p))->u##s)
#   define AV_RN(s, p) (((const union unaligned_##s *) (p))->l)
#   define AV_WN(s, p, v) ((((union unaligned_##s *) (p))->l) = (v))
#   define AV_RB(s, p)    av_bswap##s(AV_RN##s(p))
#   define AV_WB(s, p, v) AV_WN##s(p, av_bswap##s(v))
#   define AV_RL(s, p)    AV_RN##s(p)
#   define AV_WL(s, p, v) AV_WN##s(p, v)
#	define AV_RB8(x)     (((const uint8_t*)(x))[0])

#	define AV_WB8(p, d)  do { ((uint8_t*)(p))[0] = (d); } while(0)

#	define AV_RL8(x)     AV_RB8(x)

#	define AV_WL8(p, d)  AV_WB8(p, d)

#   define AV_RN16(p) AV_RN(16, p)
#   define AV_RN32(p) AV_RN(32, p)
#   define AV_RN64(p) AV_RN(64, p)

#   define AV_WN16(p, v) AV_WN(16, p, v)
#   define AV_WN32(p, v) AV_WN(32, p, v)
#   define AV_WN64(p, v) AV_WN(64, p, v)

#   define AV_WL16(p, v) AV_WL(16, p, v)



#   define AV_RB16(p)    AV_RB(16, p)
#   define AV_WB16(p, v) AV_WB(16, p, v)
#   define AV_RL16(p)    AV_RL(16, p)
#   define AV_WL16(p, v) AV_WL(16, p, v)
#   define AV_RB32(p)    AV_RB(32, p)
#   define AV_WB32(p, v) AV_WB(32, p, v)
#   define AV_RL32(p)    AV_RL(32, p)
#   define AV_WL32(p, v) AV_WL(32, p, v)

#   define AV_RB64(p)    AV_RB(64, p)
#   define AV_WB64(p, v) AV_WB(64, p, v)
#   define AV_RL64(p)    AV_RL(64, p)
#   define AV_WL64(p, v) AV_WL(64, p, v)

#ifndef AV_RB24
#   define AV_RB24(x)                           \
    ((((const uint8_t*)(x))[0] << 16) |         \
     (((const uint8_t*)(x))[1] <<  8) |         \
      ((const uint8_t*)(x))[2])
#endif
#ifndef AV_WB24
#   define AV_WB24(p, d) do {                   \
        ((uint8_t*)(p))[2] = (d);               \
        ((uint8_t*)(p))[1] = (d)>>8;            \
        ((uint8_t*)(p))[0] = (d)>>16;           \
    } while(0)
#endif

#   define AV_RL24(x)                           \
    ((((const uint8_t*)(x))[2] << 16) |         \
     (((const uint8_t*)(x))[1] <<  8) |         \
      ((const uint8_t*)(x))[0])

#   define AV_WL24(p, d) do {                   \
        ((uint8_t*)(p))[0] = (d);               \
        ((uint8_t*)(p))[1] = (d)>>8;            \
        ((uint8_t*)(p))[2] = (d)>>16;           \
    } while(0)

#ifndef AV_RB48
#   define AV_RB48(x)                                     \
    (((uint64_t)((const uint8_t*)(x))[0] << 40) |         \
     ((uint64_t)((const uint8_t*)(x))[1] << 32) |         \
     ((uint64_t)((const uint8_t*)(x))[2] << 24) |         \
     ((uint64_t)((const uint8_t*)(x))[3] << 16) |         \
     ((uint64_t)((const uint8_t*)(x))[4] <<  8) |         \
      (uint64_t)((const uint8_t*)(x))[5])
#endif
#ifndef AV_WB48
#   define AV_WB48(p, darg) do {                \
        uint64_t d = (darg);                    \
        ((uint8_t*)(p))[5] = (d);               \
        ((uint8_t*)(p))[4] = (d)>>8;            \
        ((uint8_t*)(p))[3] = (d)>>16;           \
        ((uint8_t*)(p))[2] = (d)>>24;           \
        ((uint8_t*)(p))[1] = (d)>>32;           \
        ((uint8_t*)(p))[0] = (d)>>40;           \
    } while(0)
#endif

#ifndef AV_RL48
#   define AV_RL48(x)                                     \
    (((uint64_t)((const uint8_t*)(x))[5] << 40) |         \
     ((uint64_t)((const uint8_t*)(x))[4] << 32) |         \
     ((uint64_t)((const uint8_t*)(x))[3] << 24) |         \
     ((uint64_t)((const uint8_t*)(x))[2] << 16) |         \
     ((uint64_t)((const uint8_t*)(x))[1] <<  8) |         \
      (uint64_t)((const uint8_t*)(x))[0])
#endif
#ifndef AV_WL48
#   define AV_WL48(p, darg) do {                \
        uint64_t d = (darg);                    \
        ((uint8_t*)(p))[0] = (d);               \
        ((uint8_t*)(p))[1] = (d)>>8;            \
        ((uint8_t*)(p))[2] = (d)>>16;           \
        ((uint8_t*)(p))[3] = (d)>>24;           \
        ((uint8_t*)(p))[4] = (d)>>32;           \
        ((uint8_t*)(p))[5] = (d)>>40;           \
    } while(0)
#endif

#define av_restrict restrict

#define MAKE_ACCESSORS(str, name, type, field) \
    type av_##name##_get_##field(const str *s) { return s->field; } \
    void av_##name##_set_##field(str *s, type v) { s->field = v; }


#define AV_COPY(n, d, s) \
    (((av_alias##n*)(d))->u##n = ((const av_alias##n*)(s))->u##n)
#   define AV_COPY16(d, s) AV_COPY(16, d, s)
#   define AV_COPY32(d, s) AV_COPY(32, d, s)
#   define AV_COPY64(d, s) AV_COPY(64, d, s)
